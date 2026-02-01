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
        messagingTemplate.convertAndSend(
                "/topic/match/" + event.getMatchId(),
                event
        );
    }
}
