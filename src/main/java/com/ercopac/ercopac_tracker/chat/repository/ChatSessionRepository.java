package com.ercopac.ercopac_tracker.chat.repository;

import com.ercopac.ercopac_tracker.chat.domain.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByStatus(String status);
    List<ChatSession> findByCustomerId(Long customerId);
    List<ChatSession> findByAgentId(Long agentId);
}