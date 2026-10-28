package com.ercopac.ercopac_tracker.ticketing.web;

import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.service.DirectChatService;
import com.ercopac.ercopac_tracker.ticketing.websocket.TicketWebSocketHandler;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** Live chat between two users (support staff and clients); messages are pushed on the /ws/tickets websocket. */
@RestController
@RequestMapping("/api/tickets/chat")
public class DirectChatController {

    private static final String ACCESS = "hasAnyRole('SALES_MANAGER_LEAD', 'H24', 'H24_LEAD', 'CLIENT')";

    private final DirectChatService service;
    private final TicketWebSocketHandler socket;

    public DirectChatController(DirectChatService service, TicketWebSocketHandler socket) {
        this.service = service;
        this.socket = socket;
    }

    @GetMapping("/contacts")
    @PreAuthorize(ACCESS)
    public List<ChatContact> contacts() {
        return service.contacts();
    }

    @GetMapping("/conversations")
    @PreAuthorize(ACCESS)
    public List<ChatConversation> conversations() {
        return service.conversations();
    }

    @GetMapping("/{userId}/messages")
    @PreAuthorize(ACCESS)
    public List<ChatMessageDto> messages(@PathVariable Long userId) {
        return service.messages(userId);
    }

    @PostMapping("/{userId}/messages")
    @PreAuthorize(ACCESS)
    public ChatMessageDto send(@PathVariable Long userId, @Valid @RequestBody ChatMessageRequest request) {
        ChatMessageDto saved = service.send(userId, request);
        Map<String, Object> event = Map.of("type", "direct", "message", saved);
        socket.sendToUser(saved.recipientId(), event);
        socket.sendToUser(saved.senderId(), event);
        return saved;
    }
}
