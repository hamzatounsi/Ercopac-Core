package com.ercopac.ercopac_tracker.notifications.service;

import com.ercopac.ercopac_tracker.notifications.domain.Notification;
import com.ercopac.ercopac_tracker.notifications.domain.NotificationChannel;
import com.ercopac.ercopac_tracker.notifications.domain.NotificationStatus;
import com.ercopac.ercopac_tracker.notifications.dto.NotificationRequest;
import com.ercopac.ercopac_tracker.notifications.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final EmailProvider emailProvider;

    public NotificationService(
            NotificationRepository repository,
            EmailProvider emailProvider
    ) {
        this.repository = repository;
        this.emailProvider = emailProvider;
    }

    @Transactional
    public Notification create(NotificationRequest request) {
        Notification notification = new Notification();

        notification.setOrganisationId(request.organisationId());
        notification.setProjectId(request.projectId());
        notification.setTaskId(request.taskId());
        notification.setRecipientUserId(request.recipientUserId());
        notification.setRecipientEmail(request.recipientEmail());
        notification.setChannel(request.channel());
        notification.setSeverity(request.severity() == null ? "INFO" : request.severity());
        notification.setSubject(request.subject());
        notification.setMessage(request.message());
        notification.setHtmlBody(request.htmlBody());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRetryCount(0);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setNextRetryAt(LocalDateTime.now());

        Notification saved = repository.save(notification);

        sendAsync(saved.getId());

        return saved;
    }

    @Async
    public void sendAsync(Long notificationId) {
        repository.findById(notificationId).ifPresent(this::sendNow);
    }

    @Transactional
    public void sendNow(Notification notification) {
        try {
            if (notification.getChannel() == NotificationChannel.EMAIL) {
                emailProvider.sendEmail(
                        notification.getRecipientEmail(),
                        notification.getSubject(),
                        notification.getHtmlBody()
                );
            }

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setErrorMessage(e.getMessage());
            notification.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
        }

        repository.save(notification);
    }
}