package org.example.game_engine_service.controller;

import org.example.DTO.TurnSwitchDTO;
import org.example.game_engine_service.config.TimerRefereeClient;
import org.example.game_engine_service.model.DTO.MoveMessageDTO;

import org.example.game_engine_service.model.DTO.RedisMoveEvent;
import org.example.game_engine_service.service.GameStateService;
import org.example.game_engine_service.service.MoveEventPublisher;
import org.example.game_engine_service.service.GameResultService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class GameWebSocketController {

    private final GameStateService gameStateService;
    private final MoveEventPublisher publisher;
    private final GameResultService resultService;
    private final TimerRefereeClient timerClient;

    public GameWebSocketController(GameStateService gameStateService,
                                   MoveEventPublisher publisher,
                                   GameResultService resultService,
                                   TimerRefereeClient timerClient) {
        this.gameStateService = gameStateService;
        this.publisher = publisher;
        this.resultService = resultService;
        this.timerClient = timerClient;
    }

    @MessageMapping("/move")
    @SendToUser("/queue/errors")
    public void handleMove(
            @Payload MoveMessageDTO dto,
            Map<String, Object> session,
            Principal principal
    ) {
        if (principal == null) {
            System.out.println("❌ ERROR: Principal is null! User is not authenticated.");
            return;
        }
        //trying to get user id from the principal in config/StompPrincipal
        Long userId = Long.valueOf(principal.getName());
        System.out.println("We have received move from the frontend with details: "+"\n userId: "+userId + "\n DTO: "+dto);

        gameStateService.validateTurn(
                dto.matchId(),
                userId
        );

        String uci  = convertToAlgebraic(dto.fromRow(), dto.fromCol(), dto.toRow(), dto.toCol(), dto.promotion());

        String newFen =
                gameStateService.validateAndApplyMove(
                        dto.matchId(),
                        dto.uci(),
                        userId
                );

//        String nextTurn = newFen.contains(" w ") ? "WHITE" : "BLACK";
//        timerClient.switchTurn(new TurnSwitchDTO(dto.matchId(), nextTurn));

        RedisMoveEvent event = new RedisMoveEvent(
                dto.matchId(),
                dto.uci(),
                newFen,
                userId
        );
        publisher.publish(event);

        resultService.evaluate(dto.matchId(), newFen);
    }

    private String convertToAlgebraic(int fromRow, int fromCol, int toRow, int toCol,String promotion) {
        char fromFile = (char) ('a' + fromCol);
        int fromRank = 8 - fromRow;

        char toFile = (char) ('a' + toCol);
        int toRank = 8 - toRow;

        return "" + fromFile + fromRank + toFile + toRank+promotion;
    }

    @MessageMapping("/match/sync")
    @SendToUser("/queue/sync")
    public Map<Object, Object> syncGame(@Payload String matchId) {
        // Fetches the entire Hash (fen, turn, playerIds, etc.)
        return gameStateService.getMatchState(matchId);
    }
}
