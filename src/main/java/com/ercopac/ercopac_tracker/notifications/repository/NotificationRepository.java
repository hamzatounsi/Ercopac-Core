package com.ercopac.ercopac_tracker.notifications.repository;
import com.ercopac.ercopac_tracker.notifications.domain.Notification;
import com.ercopac.ercopac_tracker.notifications.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop20ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            NotificationStatus status,
            LocalDateTime now
    );
    List<Notification> findByOrganisationIdAndRecipientUserIdOrderByCreatedAtDesc(
            Long organisationId,
            Long recipientUserId
    );
    List<Notification> findByOrganisationIdAndRecipientUserIdAndReadByUserFalse(
            Long organisationId,
            Long recipientUserId
    );
}