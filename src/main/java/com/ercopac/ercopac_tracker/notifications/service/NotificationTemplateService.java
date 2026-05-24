package com.ercopac.ercopac_tracker.notifications.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationTemplateService {

    public String taskCheckpointTemplate(
            String taskName,
            String projectName,
            Integer progress,
            String message
    ) {
        return """
                <div style="font-family:Arial,sans-serif;background:#f4f6f8;padding:24px;">
                  <div style="max-width:620px;margin:auto;background:white;border-radius:14px;padding:24px;border:1px solid #e5e7eb;">
                    <h2 style="margin:0 0 12px;color:#111827;">Projectum Task Alert</h2>

                    <p style="font-size:14px;color:#374151;">
                      A task checkpoint notification has been triggered.
                    </p>

                    <div style="background:#f9fafb;border-radius:10px;padding:16px;margin:18px 0;">
                      <p><strong>Project:</strong> %s</p>
                      <p><strong>Task:</strong> %s</p>
                      <p><strong>Progress:</strong> %s%%</p>
                      <p><strong>Message:</strong> %s</p>
                    </div>

                    <p style="font-size:12px;color:#6b7280;">
                      This is an automated notification from Projectum.
                    </p>
                  </div>
                </div>
                """.formatted(
                safe(projectName),
                safe(taskName),
                progress == null ? 0 : progress,
                safe(message)
        );
    }

    private String safe(String value) {
        return value == null ? "—" : value;
    }
}