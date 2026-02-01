package org.example.matchmaking_service.service;


import org.example.DTO.GameType;
import org.example.DTO.MatchFoundEvent;
import org.example.matchmaking_service.model.DTO.MatchmakingResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class MatchmakingService {

    private final RedisTemplate<String, String> redis;
//    private final StringRedisTemplate redis;
    private final RabbitTemplate rabbitTemplate;

    public MatchmakingService(
            RedisTemplate<String, String> redis,
            RabbitTemplate rabbitTemplate
    ) {
        this.redis = redis;
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean joinQueue(Long userId, int rating, GameType mode) {

        String key = "matchmaking:" + mode.name();

        // Prevent duplicates
        if (redis.opsForZSet().score(key, userId.toString()) != null) {
            throw new RuntimeException("Already in matchmaking");
        }

        long joinedAt = System.currentTimeMillis();


        // Add player
        redis.opsForZSet().add(key, userId.toString(), rating);
        redis.opsForHash().put("matchmaking:joinedAt",userId.toString(),String.valueOf(joinedAt));


        Optional<Long> opponent = findOpponent(userId,rating,mode,joinedAt);

        if(opponent.isEmpty()){
            return false;
        }

        String status = createMatch(userId, opponent.get(), mode);
        return true;
    }

    public Optional<Long> findOpponent(Long userId, int rating,GameType mode,long joinedAt) {

        long waitSeconds = (System.currentTimeMillis() - joinedAt) / 1000;

        int allowedDiff = Math.min(500,50 + ((int) waitSeconds / 10) * 50);

        int min = rating - allowedDiff;
        int max = rating + allowedDiff;

        String Key = "matchmaking:"+mode.name();

        Set<String> candidates =
                redis.opsForZSet().rangeByScore(
                        Key,
                        min,
                        max
                );

        for (String candidate : candidates) {
            if (!candidate.equals(userId.toString())) {
                return Optional.of(Long.valueOf(candidate));
            }
        }

        return Optional.empty();
    }

    private String createMatch(
            Long p1,
            Long p2,
            GameType mode
    ) {
        String queueKey = "matchmaking:" + mode.name();

        redis.opsForZSet().remove(queueKey, p1.toString(), p2.toString());
        redis.opsForHash().delete("matchmaking:joinedAt",
                p1.toString(), p2.toString());
        System.out.println(redis);

        publishMatch(p1, p2, mode);
        logQueueState(queueKey, mode);
        return ("Match has been published to players with ID's: \n"+p1+"\n"+p2);
    }


    private void publishMatch(Long p1, Long p2, GameType mode) {

        MatchFoundEvent event = new MatchFoundEvent(
                UUID.randomUUID().toString(),
                p1,
                p2,
                mode
        );

        rabbitTemplate.convertAndSend(
                "match.found.exchange",
                "match.found",
                event
        );

        System.out.println("Match published: " + p1 + " vs " + p2);
    }

    public void cancel(Long userId) {

        for (GameType mode : GameType.values()) {
            String key = "matchmaking:" + mode.name();
            redis.opsForZSet().remove(key, userId.toString());
        }

        redis.opsForHash().delete("matchmaking:joinedAt", userId.toString());

        System.out.println("User cancelled matchmaking: " + userId);
    }

    @Scheduled(fixedRate = 5000)
    public void cleanupExpiredPlayers() {

        long now = System.currentTimeMillis();
        long timeoutMillis = 90_000;

        Map<Object, Object> joinedMap =
                redis.opsForHash().entries("matchmaking:joinedAt");

        for (Map.Entry<Object, Object> entry : joinedMap.entrySet()) {

            String userId = entry.getKey().toString();
            long joinedAt = Long.parseLong(entry.getValue().toString());

            if (now - joinedAt > timeoutMillis) {

                for (GameType mode : GameType.values()) {
                    redis.opsForZSet().remove(
                            "matchmaking:" + mode.name(),
                            userId
                    );
                }

                redis.opsForHash().delete("matchmaking:joinedAt", userId);

                System.out.println("Removed expired player: " + userId);
            }
        }
    }

    private void logQueueState(String queueKey, GameType mode) {

        Set<String> remainingPlayers =
                redis.opsForZSet().range(queueKey, 0, -1);

        Long queueSize = redis.opsForZSet().size(queueKey);

        System.out.println("========== MATCHMAKING QUEUE STATE ==========");
        System.out.println("Game Mode : " + mode);
        System.out.println("Queue Key : " + queueKey);
        System.out.println("Queue Size: " + queueSize);

        if (remainingPlayers == null || remainingPlayers.isEmpty()) {
            System.out.println("Queue is EMPTY");
        } else {
            for (String playerId : remainingPlayers) {
                Double rating =
                        redis.opsForZSet().score(queueKey, playerId);

                System.out.println(
                        "PlayerId=" + playerId +
                                " | Rating=" + rating
                );
            }
        }

        System.out.println("============================================");
    }


}
