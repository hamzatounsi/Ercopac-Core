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

    @Scheduled(fixedDelay = 60000)
    public void retryFailedNotifications() {
        List<Notification> failed = repository
                .findTop20ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                        NotificationStatus.FAILED,
                        LocalDateTime.now()
                );

        for (Notification notification : failed) {
            if (notification.getRetryCount() < 3) {
                notificationService.sendAsync(notification.getId());
            }
        }
    }
}