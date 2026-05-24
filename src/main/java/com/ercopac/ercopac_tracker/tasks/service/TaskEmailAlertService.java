package com.ercopac.ercopac_tracker.tasks.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class TaskEmailAlertService {

    private final JavaMailSender mailSender;

    public TaskEmailAlertService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTaskAlert(String to, String subject, String message) {
        if (to == null || to.isBlank()) {
            System.out.println("EMAIL NOT SENT: recipient is empty");
            return;
        }

        try {
            System.out.println("SENDING EMAIL TO: " + to);

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(message);

            mailSender.send(mail);

            System.out.println("EMAIL SENT SUCCESSFULLY TO: " + to);
        } catch (Exception e) {
            System.out.println("EMAIL SEND FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}