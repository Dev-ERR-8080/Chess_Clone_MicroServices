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

    public void evaluate(String matchId, String fen) {

        Board board = new Board();
        board.loadFromFen(fen);

        if (board.isMated()) {
            handleGameOver(matchId, "CHECKMATE", board);
        } else if (board.isDraw()) {
            handleGameOver(matchId, "DRAW", board);
        }
    }

    private void handleGameOver(
            String matchId,
            String reason,
            Board board
    ) {
        String key = "match:" + matchId;

        Long whiteId = Long.valueOf(
                redis.opsForHash().get(key, "whitePlayerId").toString()
        );
        Long blackId = Long.valueOf(
                redis.opsForHash().get(key, "blackPlayerId").toString()
        );

        Long winnerId = null;

        if ("CHECKMATE".equals(reason)) {
            winnerId = board.getSideToMove() == Side.WHITE
                    ? blackId
                    : whiteId;
        }

        redis.opsForHash().put(key, "status", "FINISHED");

        rabbitTemplate.convertAndSend(
                "game.events.exchange",
                "game.over",
                new GameOverEvent(
                        matchId,
                        winnerId,
                        reason,
                        board.getFen()
                )
        );
    }
}
