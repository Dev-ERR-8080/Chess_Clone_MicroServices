package org.example.game_engine_service.service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.example.game_engine_service.model.MatchState;
import org.example.game_engine_service.model.MatchStatus;
import org.example.game_engine_service.model.PieceColor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameStateService {

    private final StringRedisTemplate redis;

    public GameStateService(StringRedisTemplate redis) {
        this.redis = redis;
    }
    private String matchKey(String matchId) {
        return "match:" + matchId;
    }

    public void initializeMatch(String matchId, Long whiteId, Long blackId, String mode) {
        String key = matchKey(matchId);
        Map<String, String> data = new HashMap<>();
        data.put("whitePlayerId", whiteId.toString());
        data.put("blackPlayerId", blackId.toString());
        data.put("turn", "WHITE");
        data.put("status", "ONGOING");
        data.put("fen", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        data.put("mode",mode);

        redis.opsForHash().putAll(key, data);
//        redis.opsForValue().set(fenKey(matchId), data.get("fen"));

        System.out.println("♟️ Game initialized correctly in Redis Hash for match " + matchId);
    }

    public void updateMatchState(String matchId, String newFen, String nextTurn) {
        String key = matchKey(matchId);
        redis.opsForHash().put(key, "fen", newFen);
        redis.opsForHash().put(key, "turn", nextTurn);
    }

    public Map<Object, Object> getMatchState(String matchId) {
        return redis.opsForHash().entries(matchKey(matchId));
    }

    public Board loadBoard(String matchId) {
        String fen = (String) redis.opsForHash().get(matchKey(matchId),"fen");
        if (fen == null) {
            throw new IllegalStateException("Match not initialized");
        }
        Board board = new Board();
        board.loadFromFen(fen);
        return board;
    }

    public void validateTurn(String matchId, Long userId) {

        PieceColor turn = PieceColor.valueOf(
                (String) redis.opsForHash().get(matchKey(matchId), "turn")
        );

        Long whiteId = Long.valueOf(
                redis.opsForHash().get(matchKey(matchId), "whitePlayerId").toString()
        );

        Long blackId = Long.valueOf(
                redis.opsForHash().get(matchKey(matchId), "blackPlayerId").toString()
        );

        if (turn == PieceColor.WHITE && !whiteId.equals(userId)) {
            throw new IllegalStateException("Not your turn");
        }

        if (turn == PieceColor.BLACK && !blackId.equals(userId)) {
            throw new IllegalStateException("Not your turn");
        }
    }


    public String validateAndApplyMove(
            String matchId,
            String uci,
            Long userId
    ) {
        Board board = loadBoard(matchId);

        Move move;
        try {
            Side side = board.getSideToMove();
            move = new Move(uci, side);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid UCI move");
        }

        if (!board.isMoveLegal(move, true)) {
            System.out.println("FEN during failure: " + board.getFen());
            System.out.println("Move in board during failure: " + move);
            throw new IllegalStateException("Illegal move");
        }

        board.doMove(move);

        // Toggle turn
        PieceColor nextTurn =
                board.getSideToMove() == Side.WHITE
                        ? PieceColor.WHITE
                        : PieceColor.BLACK;

        String newFen = board.getFen();

        redis.opsForHash().put(matchKey(matchId), "fen", newFen);
        redis.opsForHash().put(matchKey(matchId), "turn", nextTurn.name());

        return newFen;
    }

//    private String fenKey(String matchId) {
//        return "game:" + matchId + ":fen";
//    }

}
