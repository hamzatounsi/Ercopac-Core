package com.ercopac.ercopac_tracker.ticketing.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration(proxyBeanMethods = false)
@EnableWebSocket
public class TicketWebSocketConfig implements WebSocketConfigurer {

    private final TicketWebSocketHandler ticketWebSocketHandler;
    private final TicketWebSocketHandshakeInterceptor handshakeInterceptor;
    private final String frontendBaseUrl;

    public TicketWebSocketConfig(
            TicketWebSocketHandler ticketWebSocketHandler,
            TicketWebSocketHandshakeInterceptor handshakeInterceptor,
            @Value("${app.frontend-base-url:http://localhost:4200}") String frontendBaseUrl
    ) {
        this.ticketWebSocketHandler = ticketWebSocketHandler;
        this.handshakeInterceptor = handshakeInterceptor;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ticketWebSocketHandler, "/ws/tickets")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns(frontendBaseUrl, "http://127.0.0.1:4200");
    }
}
