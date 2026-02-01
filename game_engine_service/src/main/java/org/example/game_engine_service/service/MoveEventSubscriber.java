package org.example.game_engine_service.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.game_engine_service.model.DTO.RedisMoveEvent;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class MoveEventSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final MatchWebSocketService wsService;

    public MoveEventSubscriber(ObjectMapper objectMapper,
                               MatchWebSocketService wsService) {
        this.objectMapper = objectMapper;
        this.wsService = wsService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            RedisMoveEvent event =
                    objectMapper.readValue(message.getBody(), RedisMoveEvent.class);

            wsService.broadcastMove(event);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
