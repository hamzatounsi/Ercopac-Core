package com.ercopac.ercopac_tracker.notifications.service;

import com.ercopac.ercopac_tracker.notifications.domain.Notification;
import com.ercopac.ercopac_tracker.notifications.domain.NotificationStatus;
import com.ercopac.ercopac_tracker.notifications.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationRetryWorker {

    private final NotificationRepository repository;
    private final NotificationService notificationService;

    public NotificationRetryWorker(
            NotificationRepository repository,
            NotificationService notificationService
    ) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelay = 15000)
    public void processPendingAndFailedNotifications() {
        LocalDateTime now = LocalDateTime.now();

        List<Notification> pending = repository
                .findTop20ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                        NotificationStatus.PENDING,
                        now
                );

        List<Notification> failed = repository
                .findTop20ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                        NotificationStatus.FAILED,
                        now
                );

        for (Notification notification : pending) {
            notificationService.sendAsync(notification.getId());
        }

        for (Notification notification : failed) {
            if (notification.getRetryCount() < 3) {
                notificationService.sendAsync(notification.getId());
            }
        }
    }
}