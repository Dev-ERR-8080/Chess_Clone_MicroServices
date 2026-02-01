package org.example.timer_referee_service.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class TimerRefereeService {

    private RedisTemplate<String, Object> redisTemplate;
    private SimpMessagingTemplate messagingTemplate;

    private static final String TIMER_KEY_PREFIX = "timer:match:";

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

        for (String key : keys) {
            processMatchTick(key);
        }
    }

    private void processMatchTick(String key) {
        Map<Object, Object> state = redisTemplate.opsForHash().entries(key);
        if (!"ONGOING".equals(state.get("status"))) return;

        long now = System.currentTimeMillis();
        long lastTick = Long.parseLong((String) state.get("lastTickAt"));
        long elapsed = now - lastTick;

        String turn = (String) state.get("activeTurn");
        String timeKey = turn.equals("WHITE") ? "whiteTimeMs" : "blackTimeMs";

        long currentTime = Long.parseLong((String) state.get(timeKey));
        long newTime = Math.max(0, currentTime - elapsed);

        // Update Redis
        redisTemplate.opsForHash().put(key, timeKey, String.valueOf(newTime));
        redisTemplate.opsForHash().put(key, "lastTickAt", String.valueOf(now));

        String matchId = key.replace(TIMER_KEY_PREFIX, "");

        // BROADCAST TICK
        messagingTemplate.convertAndSend("/topic/match/" + matchId, Optional.of(Map.of(
                "type", "TICK",
                "whiteTimeMs", (turn.equals("WHITE") ? newTime : (long) state.get("whiteTimeMs")),
                "blackTimeMs", (turn.equals("BLACK") ? newTime : (long) state.get("blackTimeMs")),
                "turn", turn
        )));

        // TIMEOUT CHECK
        if (newTime <= 0) {
            handleTimeout(matchId, turn);
        }
    }

    private void handleTimeout(String matchId, String loserColor) {
        redisTemplate.opsForHash().put(TIMER_KEY_PREFIX + matchId, "status", "FINISHED");

        // Notify Game Service via Feign Client or Kafka to finalize result
        // gameServiceClient.notifyTimeout(matchId, loserColor);

        messagingTemplate.convertAndSend("/topic/match/" + matchId, Optional.of(Map.of(
                "type", "TIMEOUT",
                "loser", loserColor
        )));
    }
}