package com.ercopac.ercopac_tracker.notifications.web;

import com.ercopac.ercopac_tracker.notifications.dto.NotificationDto;
import com.ercopac.ercopac_tracker.notifications.service.NotificationQueryService;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationQueryService service;
    private final SecurityUtils securityUtils;

    public NotificationController(
            NotificationQueryService service,
            SecurityUtils securityUtils
    ) {
        this.service = service;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/mine")
    public List<NotificationDto> mine() {
        return service.getMyNotifications(
                securityUtils.getCurrentOrganisationId(),
                securityUtils.getCurrentUserId()
        );
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        service.markAsRead(
                securityUtils.getCurrentOrganisationId(),
                securityUtils.getCurrentUserId(),
                id
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        service.markAllAsRead(
                securityUtils.getCurrentOrganisationId(),
                securityUtils.getCurrentUserId()
        );
        return ResponseEntity.ok().build();
    }
}