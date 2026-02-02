package org.example.timer_referee_service.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class TimerRefereeService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String TIMER_KEY_PREFIX = "timer:match:";
    private static final String REDIS_TOPIC_PREFIX = "match:";

    public void initializeTimer(String matchId, String gameType) {
        long base = switch (gameType) {
            case "BULLET" -> 60_000L;
            case "BLITZ" -> 180_000L;
            case "RAPID" -> 600_000L;
            default -> 1_800_000L; // Classic 30 min
        };

        String key = TIMER_KEY_PREFIX + matchId;
        Map<String, String> data = Map.of(
                "whiteTimeMs", String.valueOf(base),
                "blackTimeMs", String.valueOf(base),
                "activeTurn", "WHITE",
                "lastTickAt", String.valueOf(System.currentTimeMillis()),
                "status", "ONGOING"
        );

        redisTemplate.opsForHash().putAll(key, data);
    }

    public void handleTurnSwitch(String matchId, String nextTurn) {
        String key = TIMER_KEY_PREFIX + matchId;
        Map<Object, Object> state = redisTemplate.opsForHash().entries(key);

        if (state.isEmpty() || !"ONGOING".equals(state.get("status"))) {
            return;
        }

        long now = System.currentTimeMillis();
        long lastTick = Long.parseLong((String) state.get("lastTickAt"));
        long elapsed = now - lastTick;

        // The player who JUST moved is the OPPOSITE of nextTurn
        String justMovedColor = nextTurn.equals("WHITE") ? "BLACK" : "WHITE";
        String timeKey = justMovedColor.equals("WHITE") ? "whiteTimeMs" : "blackTimeMs";

        long currentTime = Long.parseLong((String) state.get(timeKey));

        // Final deduction for the move duration
        long finalTime = Math.max(0, currentTime - elapsed);

        // Update Redis: Save final time, set the new turn, and reset the tick anchor
        redisTemplate.opsForHash().put(key, timeKey, String.valueOf(finalTime));
        redisTemplate.opsForHash().put(key, "activeTurn", nextTurn);
        redisTemplate.opsForHash().put(key, "lastTickAt", String.valueOf(now));

        System.out.println("🔄 Turn switched to " + nextTurn + " for match " + matchId);
    }

    @Scheduled(fixedRate = 1000)
    public void globalTick() {
        // Retrieve all active timer keys from Redis
        Set<String> keys = redisTemplate.keys(TIMER_KEY_PREFIX + "*");
        System.out.println("[TIMER] Scanning... Found " + (keys != null ? keys.size() : 0) + " active matches.");
        if(keys == null || keys.isEmpty()) return;
        for (String key : keys) {
            processMatchTick(key);
        }
    }

    private void processMatchTick(String key) {
        Map<Object, Object> state = redisTemplate.opsForHash().entries(key);
        if (state.isEmpty() || !"ONGOING".equals(state.get("status"))) return;

        long now = System.currentTimeMillis();
        long lastTick = getLongFromRedis(state.get("lastTickAt"));
        long elapsed = now - lastTick;

        String turn = state.get("activeTurn").toString().replace("\"", "");
        String timeKey = turn.equals("WHITE") ? "whiteTimeMs" : "blackTimeMs";

        long currentTime = getLongFromRedis(state.get(timeKey));
        long newTime = Math.max(0, currentTime - elapsed);

        // Update Redis
        redisTemplate.opsForHash().put(key, timeKey, String.valueOf(newTime));
        redisTemplate.opsForHash().put(key, "lastTickAt", String.valueOf(now));

        String matchId = key.replace(TIMER_KEY_PREFIX, "");

        long whiteTime = getLongFromRedis(state.get("whiteTimeMs"));
        long blackTime = getLongFromRedis(state.get("blackTimeMs"));

       Map<String,Object> tickPayload = Map.of(
                "type", "TICK",
                "matchId",matchId,
                "whiteTimeMs", whiteTime,
                "blackTimeMs", blackTime,
                "turn", turn
        );
        // BROADCAST TICK
        System.out.println("[TIMER] Publishing Tick for Match: " + matchId + " | White: " + whiteTime + " | Black: " + blackTime);
        redisTemplate.convertAndSend(REDIS_TOPIC_PREFIX + matchId, tickPayload);

        // TIMEOUT CHECK
        if (newTime <= 0) {
            handleTimeout(matchId, turn);
        }
    }

    private void handleTimeout(String matchId, String loserColor) {
        redisTemplate.opsForHash().put(TIMER_KEY_PREFIX + matchId, "status", "FINISHED");

        // Notify Game Service via Feign Client or Kafka to finalize result
        // gameServiceClient.notifyTimeout(matchId, loserColor);

        Map<String, Object> timeOutEvent =Map.of(
                "type", "TIMEOUT",
                "matchId", matchId,
                "winner", loserColor.equals("WHITE")? "BLACK" :"WHITE",
                "loser", loserColor,
                "reason","TIME RAN OUT"
        );

        redisTemplate.convertAndSend(REDIS_TOPIC_PREFIX+matchId,timeOutEvent);
        rabbitTemplate.convertAndSend("game.commands.exchange", "game.timeout.key", timeOutEvent);
    }

    // Safe conversion helper
    private long getLongFromRedis(Object value) {
        if (value == null) return 0L;
        // Remove extra quotes if Jackson added them
        String cleanValue = value.toString().replace("\"", "");
        return Long.parseLong(cleanValue);
    }
}