//package org.example.game_engine_service.service;
//
//import org.example.game_engine_service.model.DTO.MoveBroadcastDTO;
//import org.example.game_engine_service.model.DTO.MoveMessageDTO;
//import org.example.game_engine_service.model.MatchState;
//import org.example.game_engine_service.model.MatchStatus;
//import org.example.game_engine_service.model.PieceColor;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class MoveProcessingService {
//
//
//    private final GameStateService stateService;
//    private final RedisTemplate<String, Object> redis;
//
//
//    public MoveProcessingService(
//            GameStateService stateService,
//            RedisTemplate<String, Object> redis
//    ) {
//        this.stateService = stateService;
//        this.redis = redis;
//    }
//
//
//    public void processMove(MoveMessageDTO dto, Long userId) {
//
//
//        MatchState state = (MatchState) stateService.getMatchState(dto.matchId());
//
//        if (!state.isPlayer(userId)) {
//            throw new SecurityException("Not your match");
//        }
//
//        if (state.getStatus() != MatchStatus.ONGOING)
//            throw new IllegalStateException("Match not active");
//
//
//        PieceColor playerColor = resolveColor(state, userId);
//
//
//        if (playerColor != state.getTurn())
//            throw new IllegalStateException("Not your turn");
//
//
//// ♟️ Validate move using chess rules engine (next step)
//
//
//// 🔁 Update Redis state
//        PieceColor nextTurn = playerColor.opposite();
//        stateService.updateMatchState(dto.matchId(), dto.fenAfter(), nextTurn.name());
//
//
//// 📢 Publish to Redis Pub/Sub
//        MoveBroadcastDTO broadcast = new MoveBroadcastDTO(
//                dto.matchId(),
//                dto.uci(),
//                dto.san(),
//                dto.fenAfter(),
//                nextTurn.name()
//        );
//
//
//        redis.convertAndSend(
//                "match:" + dto.matchId(),
//                broadcast
//        );
//    }
//
//
//    private PieceColor resolveColor(MatchState state, Long userId) {
//        if (state.getWhitePlayerId().equals(userId)) return PieceColor.WHITE;
//        if (state.getBlackPlayerId().equals(userId)) return PieceColor.BLACK;
//        throw new IllegalStateException("User not in match");
//    }
//}
//
//// Duplicate of Game Service