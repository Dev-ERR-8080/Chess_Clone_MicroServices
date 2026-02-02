package org.example.game_engine_service.service;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TimeoutListener {

    private final GameResultService gameResultService;
    private final RedisTemplate<String, Object> redis;

    public TimeoutListener(GameResultService gameResultService, RedisTemplate<String, Object> redis) {
        this.gameResultService = gameResultService;
        this.redis = redis;
    }

    @RabbitListener(queues = "game.timeout.queue")
    public void onTimeoutReceived(Map<String, Object> data) {
        String matchId = (String) data.get("matchId");
        String loser = (String) data.get("loser");

        // Fetch current FEN from the official match record
        String currentFen = (String) redis.opsForHash().get("match:" + matchId, "fen");

        System.out.println("[GAME-ENGINE] Received Timeout Command for match: " + matchId);

        // This will now update the match:xxx status to FINISHED
        gameResultService.handleTimeout(matchId, loser, currentFen);
    }
}