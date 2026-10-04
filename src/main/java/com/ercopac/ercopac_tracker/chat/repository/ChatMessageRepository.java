package com.ercopac.ercopac_tracker.chat.repository;

import com.ercopac.ercopac_tracker.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderBySentAtAsc(Long sessionId);
}