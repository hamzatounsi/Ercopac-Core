package com.ercopac.ercopac_tracker.notifications.service;

import com.ercopac.ercopac_tracker.notifications.domain.Notification;
import com.ercopac.ercopac_tracker.notifications.dto.NotificationDto;
import com.ercopac.ercopac_tracker.notifications.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationQueryService {

    private final NotificationRepository repository;

    public NotificationQueryService(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<NotificationDto> getMyNotifications(Long organisationId, Long userId) {
        return repository
                .findByOrganisationIdAndRecipientUserIdOrderByCreatedAtDesc(
                        organisationId,
                        userId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getProjectId(),
                n.getTaskId(),
                n.getChannel() != null ? n.getChannel().name() : null,
                n.getStatus() != null ? n.getStatus().name() : null,
                n.getSeverity(),
                n.getSubject(),
                n.getMessage(),
                n.getCreatedAt(),
                n.getSentAt()
        );
    }
}