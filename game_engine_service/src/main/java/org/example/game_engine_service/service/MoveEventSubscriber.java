package org.example.game_engine_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.game_engine_service.model.DTO.RedisMoveEvent;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class MoveEventSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final MatchWebSocketService wsService;

    public MoveEventSubscriber(ObjectMapper objectMapper, MatchWebSocketService wsService) {
        this.objectMapper = objectMapper;
        this.wsService = wsService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            System.out.println("[GAME-ENGINE] Received Redis Message: " + body);
            // 1. Read as a generic JsonNode first
            JsonNode root = objectMapper.readTree(message.getBody());
            // 2. Extract the 'type' to decide the action
            String type = root.has("type") ? root.path("type").asText() : "MOVE";
            JsonNode matchIdNode = root.get("matchId");
            if (matchIdNode == null || matchIdNode.isNull()) {
                System.err.println("[GAME-ENGINE] Received message without matchId! Type: " + type);
                return; // Skip this message instead of crashing
            }
            String matchId = matchIdNode.asText();

            System.out.println("[GAME-ENGINE] Received event Type: " + type);

            if ("TICK".equals(type) || "TIMEOUT".equals(type)) {
                // Handle Timer updates: Broadcast raw JSON to the match topic
                System.err.println("[GAME-ENGINE] Received Tick Data" );
                Object mapPayload = objectMapper.convertValue(root,Object.class);
                wsService.broadcastGeneric(matchId, mapPayload);
            } else {
                // Handle Move updates: Continue using your typed logic
                // This ensures your existing broadcastMove(event) logic remains untouched
                System.err.println("[GAME-ENGINE] Received Move Data" );
                wsService.broadcastMove(objectMapper.treeToValue(root, RedisMoveEvent.class));
            }

        } catch (Exception e) {
            System.err.println("[GAME-ENGINE] Subscriber Error: " + e.getMessage());
            System.err.println("Error processing Redis message: " + e.getMessage());
        }
    }
}