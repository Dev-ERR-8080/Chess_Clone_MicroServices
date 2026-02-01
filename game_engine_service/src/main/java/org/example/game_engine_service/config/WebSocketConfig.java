package org.example.game_engine_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketIdentityInterceptor interceptor;

    public WebSocketConfig(WebSocketIdentityInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // Client subscriptions
        registry.enableSimpleBroker("/topic","/queue");
        registry.setUserDestinationPrefix("/user");
        // Client -> Server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("game/ws")
                .setAllowedOriginPatterns("http://localhost:3000")
                .addInterceptors(new HttpHandshakeInterceptor())
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Pull from the attributes we set in the HandshakeInterceptor
                    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                    String userId = (sessionAttributes != null)
                            ? (String) sessionAttributes.get("userId")
                            : null;

                    if (userId != null) {
                        accessor.setUser(new StompPrincipal(userId));
                        System.out.println("✅ WebSocket Principal mapped to UserID: " + userId);
                    } else {
                        System.out.println("❌ UserID still missing in STOMP CONNECT");
                    }
                }
                return message;
            }
        });
    }
}
