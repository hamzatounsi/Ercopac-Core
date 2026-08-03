package com.ercopac.ercopac_tracker.ticketing.websocket;

import com.ercopac.ercopac_tracker.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

@Component
public class TicketWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;

    public TicketWebSocketHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        try {
            Claims claims = jwtService.parseClaims(queryParameter(request.getURI().getRawQuery(), "token"));
            Object userId = claims.get("userId");
            String role = claims.get("role", String.class);
            if (userId == null || role == null || role.isBlank()) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            attributes.put("userId", Long.valueOf(userId.toString()));
            attributes.put("role", role.replace("ROLE_", ""));
            return true;
        } catch (Exception ex) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // Authentication is completed before the WebSocket session is created.
    }

    private String queryParameter(String query, String name) {
        if (query == null || query.isBlank()) {
            return "";
        }

        return Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .filter(pair -> pair.length == 2 && name.equals(pair[0]))
                .map(pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8))
                .findFirst()
                .orElse("");
    }
}
