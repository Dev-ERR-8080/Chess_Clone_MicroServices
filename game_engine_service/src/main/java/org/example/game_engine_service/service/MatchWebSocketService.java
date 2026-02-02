package org.example.game_engine_service.service;


import org.example.game_engine_service.model.DTO.RedisMoveEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public MatchWebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastMove(RedisMoveEvent event) {
        System.out.println("[BROADCAST-MOVE] Sending to /topic/match/" + event);
        messagingTemplate.convertAndSend(
                "/topic/match/" + event.getMatchId(),
                event
        );
    }
    // Add this to MatchWebSocketService
    public void broadcastGeneric(String matchId, Object payload) {
        System.out.println("[BROADCAST-GENERIC] Sending to /topic/match/" + matchId + " Payload: " + payload);
        // Standard STOMP broadcast to the match-specific topic
        messagingTemplate.convertAndSend("/topic/match/" + matchId, payload);
    }
}
