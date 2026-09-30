package com.ercopac.ercopac_tracker.chat.service;

import com.ercopac.ercopac_tracker.chat.domain.ChatMessage;
import com.ercopac.ercopac_tracker.chat.domain.ChatSession;
import com.ercopac.ercopac_tracker.chat.repository.ChatMessageRepository;
import com.ercopac.ercopac_tracker.chat.repository.ChatSessionRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatService(ChatSessionRepository sessionRepository, ChatMessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    public List<ChatSession> getActiveSessions() {
        return sessionRepository.findByStatus("ACTIVE");
    }

    public List<ChatMessage> getSessionMessages(Long sessionId) {
        return messageRepository.findBySessionIdOrderBySentAtAsc(sessionId);
    }

    public ChatMessage sendMessage(Long sessionId, AppUser sender, String content, boolean isInternal) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        
        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setSender(sender);
        message.setContent(content);
        message.setInternal(isInternal);
        message.setSentAt(Instant.now());
        
        return messageRepository.save(message);
    }

    public ChatSession createSession(AppUser customer, AppUser agent) {
        ChatSession session = new ChatSession();
        session.setCustomer(customer);
        session.setAgent(agent);
        session.setStatus("ACTIVE");
        session.setCreatedAt(Instant.now());
        
        return sessionRepository.save(session);
    }

    public void closeSession(Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus("CLOSED");
        session.setClosedAt(Instant.now());
        sessionRepository.save(session);
    }
}