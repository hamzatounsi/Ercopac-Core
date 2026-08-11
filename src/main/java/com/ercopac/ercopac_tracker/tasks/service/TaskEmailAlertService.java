package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.notifications.service.ProjectumMailService;
import org.springframework.stereotype.Service;

@Service
public class TaskEmailAlertService {

    private final ProjectumMailService mailService;

    public TaskEmailAlertService(ProjectumMailService mailService) {
        this.mailService = mailService;
    }

    public void sendTaskAlert(String to, String subject, String message) {
        if (to == null || to.isBlank()) {
            System.out.println("EMAIL NOT SENT: recipient is empty");
            return;
        }

        mailService.sendText(to, subject, message);
    }
}
