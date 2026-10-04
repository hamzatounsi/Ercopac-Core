package com.ercopac.ercopac_tracker.chat.web;

import com.ercopac.ercopac_tracker.chat.domain.ChatMessage;
import com.ercopac.ercopac_tracker.chat.domain.ChatSession;
import com.ercopac.ercopac_tracker.chat.service.ChatService;
import com.ercopac.ercopac_tracker.user.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<List<ChatSession>> getActiveSessions() {
        return ResponseEntity.ok(chatService.getActiveSessions());
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessage>> getSessionMessages(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getSessionMessages(sessionId));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal AppUser user
    ) {
        String content = (String) request.get("content");
        boolean isInternal = (boolean) request.getOrDefault("isInternal", false);
        
        ChatMessage message = chatService.sendMessage(sessionId, user, content, isInternal);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(
            @RequestBody Map<String, Long> request,
            @AuthenticationPrincipal AppUser user
    ) {
        // Logique pour créer une session (customer ou agent)
        return ResponseEntity.ok(chatService.createSession(null, user));
    }

    @PatchMapping("/sessions/{sessionId}/close")
    public ResponseEntity<Void> closeSession(@PathVariable Long sessionId) {
        chatService.closeSession(sessionId);
        return ResponseEntity.ok().build();
    }
}