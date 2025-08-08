package com.Backend.LinkitBackend.Configuration;

import com.Backend.LinkitBackend.Constants.AppConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private String FRONTEND_URL_DEV = AppConstants.FRONTEND_URL_DEV;
    private String FRONTEND_URL_PROD = AppConstants.FRONTEND_URL_PROD;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/clipboard")  // Connection establishment
                .setAllowedOrigins(FRONTEND_URL_DEV, FRONTEND_URL_PROD) // Allow CORS for localhost
                .withSockJS(); // Endpoint for WebSocket connection
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // Topic for broadcasting messages
        registry.setApplicationDestinationPrefixes("/app");
    }
}
