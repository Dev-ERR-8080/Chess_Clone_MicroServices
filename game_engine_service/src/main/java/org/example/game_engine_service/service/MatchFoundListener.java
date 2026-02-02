package org.example.game_engine_service.service;

import org.example.DTO.MatchFoundEvent;
import org.example.DTO.TimerInitDTO;
import org.example.game_engine_service.config.RabbitConfig;
import org.example.game_engine_service.config.TimerRefereeClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchFoundListener {

    private final GameStateService gameStateService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TimerRefereeClient timerClient;

    public MatchFoundListener(GameStateService gameStateService, SimpMessagingTemplate messagingTemplate, TimerRefereeClient timerClient) {
        this.gameStateService = gameStateService;
        this.messagingTemplate = messagingTemplate;
        this.timerClient = timerClient;
    }

    @RabbitListener(queues = RabbitConfig.MATCH_FOUND_QUEUE)
    public void onMatchFound(MatchFoundEvent event) {
        try {
            System.out.println("🎯 MATCH FOUND EVENT RECEIVED: " + event.getMatchId());

            gameStateService.initializeMatch(
                    event.getMatchId(),
                    event.getPlayer1Id(),
                    event.getPlayer2Id(),
                    event.getGameType().name()
            );
            messagingTemplate.convertAndSendToUser(
                    event.player1Id().toString(),
                    "/queue/match-found",
                    event
            );
            System.out.println("✅ WS dispatch completed → userId={}" + event.player1Id());

            messagingTemplate.convertAndSendToUser(
                    event.player2Id().toString(),
                    "/queue/match-found",
                    event
            );
            System.out.println("✅ WS dispatch completed → userId={}" + event.player2Id());
            try {
                System.out.println("MatchId: "+event.getMatchId()+"GameType: "+event.getGameType());
                timerClient.startTimer(new TimerInitDTO(event.getMatchId(), event.getGameType().name()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to start timer service for match: " + event.matchId());
            }
        } catch (Exception e){
            System.out.println(" Unexpected runtime error in game engine service: "+e.getMessage());
        }
    }
}
