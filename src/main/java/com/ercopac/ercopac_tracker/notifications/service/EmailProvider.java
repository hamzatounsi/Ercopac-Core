package com.ercopac.ercopac_tracker.notifications.service;

public interface EmailProvider {
    void sendEmail(String to, String subject, String htmlBody);
}