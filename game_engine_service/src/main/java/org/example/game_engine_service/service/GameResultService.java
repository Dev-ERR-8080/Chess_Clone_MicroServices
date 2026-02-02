package org.example.game_engine_service.service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import org.example.game_engine_service.model.DTO.GameOverEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
@Service
public class GameResultService {

    private final RedisTemplate<String, Object> redis;
    private final RabbitTemplate rabbitTemplate;

    public GameResultService(
            RedisTemplate<String, Object> redis,
            RabbitTemplate rabbitTemplate
    ) {
        this.redis = redis;
        this.rabbitTemplate = rabbitTemplate;
    }

    // Existing FEN-based evaluation (Checkmate/Draw)
    public void evaluate(String matchId, String fen) {
        Board board = new Board();
        board.loadFromFen(fen);

        if (board.isMated()) {
            handleGameOver(matchId, "CHECKMATE", board.getSideToMove() == Side.WHITE ? "BLACK" : "WHITE", fen);
        } else if (board.isDraw()) {
            handleGameOver(matchId, "DRAW", null, fen);
        }
    }

    // NEW: Logic for Timeout (Called when Timer Service signals a flag fall)
    public void handleTimeout(String matchId, String loserColor, String currentFen) {
        // If White timed out, Black is the winner
        String winnerColor = "WHITE".equalsIgnoreCase(loserColor) ? "BLACK" : "WHITE";
        handleGameOver(matchId, "TIMEOUT", winnerColor, currentFen);
    }

    private void handleGameOver(
            String matchId,
            String reason,
            String winnerColor, // Pass color instead of raw IDs to simplify
            String fen
    ) {
        String key = "match:" + matchId;

        // 1. Check if match exists and isn't already finished
        Object currentStatus = redis.opsForHash().get(key, "status");
        if ("FINISHED".equals(currentStatus)) return;

        // 2. Resolve Winner ID
        Long whiteId = Long.valueOf(redis.opsForHash().get(key, "whitePlayerId").toString());
        Long blackId = Long.valueOf(redis.opsForHash().get(key, "blackPlayerId").toString());

        Long winnerId = null;
        if (winnerColor != null) {
            winnerId = "WHITE".equalsIgnoreCase(winnerColor) ? whiteId : blackId;
        }

        // 3. Update the Official Match Record in Redis
        // This fixes your 'HGETALL match:xxx' showing ONGOING issue
        redis.opsForHash().put(key, "status", "FINISHED");
        if (winnerId != null) {
            redis.opsForHash().put(key, "winnerId", winnerId.toString());
        }

        System.out.println("[GAME-ENGINE] Game Over: " + matchId + " Reason: " + reason + " Winner: " + winnerColor);

        // 4. Notify downstream (User Service for ELO, History Service, etc.)
        rabbitTemplate.convertAndSend(
                "game.events.exchange",
                "game.over",
                new GameOverEvent(
                        matchId,
                        winnerId,
                        reason,
                        fen
                )
        );
    }
}
