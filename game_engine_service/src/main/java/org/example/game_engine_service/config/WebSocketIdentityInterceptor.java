package org.example.game_engine_service.config;


import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
@Component
public class WebSocketIdentityInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String userIdHeader =
                    accessor.getFirstNativeHeader("X-User-Id");

            if (userIdHeader == null) {
                System.out.println("❌ WS Connect attempt without X-User-Id header");
                throw new IllegalStateException("Missing X-User-Id header");
            }
            accessor.setUser(() -> userIdHeader);
            System.out.println("✅ WS User Authenticated: " + userIdHeader);
            Long userId = Long.parseLong(userIdHeader);

            accessor.getSessionAttributes().put("userId", userId);
        }

        return message;
    }
}
