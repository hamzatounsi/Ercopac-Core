package com.ercopac.ercopac_tracker.notifications.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ProjectumMailService {
    private static final Logger log = LoggerFactory.getLogger(ProjectumMailService.class);
    private final JavaMailSender mailSender;
    private final String from;
    private final boolean deliveryEnabled;

    public ProjectumMailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String from,
            @Value("${app.mail.delivery-enabled:true}") boolean deliveryEnabled) {
        this.mailSender = mailSender;
        this.from = from;
        this.deliveryEnabled = deliveryEnabled;
    }

    public void sendText(String recipient, String subject, String body) {
        if (!deliveryEnabled) {
            log.info("Email delivery is disabled; suppressed message to {}", recipient);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from, "Projectum");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (Exception exception) {
            log.error("Projectum email delivery failed for recipient {}", recipient, exception);
            throw new IllegalStateException("Email could not be delivered", exception);
        }
    }
}
