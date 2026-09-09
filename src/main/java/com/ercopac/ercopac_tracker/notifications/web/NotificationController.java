package com.ercopac.ercopac_tracker.notifications.web;

import com.ercopac.ercopac_tracker.notifications.domain.Notification;
import com.ercopac.ercopac_tracker.notifications.dto.NotificationDto;
import com.ercopac.ercopac_tracker.notifications.dto.NotificationRequest;
import com.ercopac.ercopac_tracker.notifications.service.NotificationQueryService;
import com.ercopac.ercopac_tracker.notifications.service.NotificationService; // ✅ AJOUTÉ
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationQueryService queryService;
    private final NotificationService notificationService; // ✅ AJOUTÉ
    private final SecurityUtils securityUtils;

    public NotificationController(
            NotificationQueryService queryService,
            NotificationService notificationService, // ✅ AJOUTÉ
            SecurityUtils securityUtils
    ) {
        this.queryService = queryService;
        this.notificationService = notificationService; // ✅ AJOUTÉ
        this.securityUtils = securityUtils;
    }

    @GetMapping("/mine")
    public List<NotificationDto> mine() {
        return queryService.getMyNotifications(
                securityUtils.getCurrentOrganisationId(),
                securityUtils.getCurrentUserId()
        );
    }

    // ✅ CORRIGÉ : @PostMapping tout court pour avoir l'URL /api/notifications
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationRequest request) {
        Notification notification = notificationService.create(request);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        queryService.markAsRead(
                securityUtils.getCurrentOrganisationId(),
                securityUtils.getCurrentUserId(),
                id
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        queryService.markAllAsRead(
                securityUtils.getCurrentOrganisationId(),
                securityUtils.getCurrentUserId()
        );
        return ResponseEntity.ok().build();
    }
}