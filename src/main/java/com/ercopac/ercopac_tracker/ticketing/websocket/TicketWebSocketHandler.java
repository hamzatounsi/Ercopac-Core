package com.ercopac.ercopac_tracker.ticketing.websocket;

import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.MessageRequest;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.TicketMessageDto;
import com.ercopac.ercopac_tracker.ticketing.service.TicketService;
import com.ercopac.ercopac_tracker.user.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TicketWebSocketHandler extends TextWebSocketHandler {

    private final TicketService ticketService;
    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> subscribers = new ConcurrentHashMap<>();

    public TicketWebSocketHandler(TicketService ticketService, ObjectMapper objectMapper) {
        this.ticketService = ticketService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!(session.getAttributes().get("userId") instanceof Long)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid ticket channel token"));
            return;
        }
        send(session, Map.of("type", "connected"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        JsonNode payload = objectMapper.readTree(textMessage.getPayload());
        String action = payload.path("action").asText();
        Long ticketId = payload.hasNonNull("ticketId") ? payload.get("ticketId").asLong() : null;
        Long userId = (Long) session.getAttributes().get("userId");

        if (ticketId == null || userId == null || !ticketService.canAccess(ticketId, userId)) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Ticket access denied"));
            return;
        }

        if ("subscribe".equals(action)) {
            subscribers.computeIfAbsent(ticketId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
            send(session, Map.of("type", "subscribed", "ticketId", ticketId));
            return;
        }

        if ("message".equals(action)) {
            TicketMessageDto saved = ticketService.addMessageForUser(
                    ticketId,
                    userId,
                    new MessageRequest(payload.path("message").asText(), payload.path("internalNote").asBoolean(false))
            );
            broadcast(ticketId, saved);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        subscribers.values().forEach(sessions -> sessions.remove(session));
    }

    public void broadcastMessage(Long ticketId, TicketMessageDto message) {
        broadcast(ticketId, message);
    }

    private void broadcast(Long ticketId, TicketMessageDto message) {
        for (WebSocketSession session : subscribers.getOrDefault(ticketId, Set.of())) {
            try {
                String role = (String) session.getAttributes().get("role");
                if (message.internalNote() && Role.CLIENT.name().equals(role)) {
                    continue;
                }
                send(session, Map.of("type", "message", "ticketId", ticketId, "message", message));
            } catch (Exception ignored) {
                // A closed/stale client is removed by the WebSocket lifecycle callback.
            }
        }
    }

    private void send(WebSocketSession session, Object payload) throws Exception {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        }
    }
}
