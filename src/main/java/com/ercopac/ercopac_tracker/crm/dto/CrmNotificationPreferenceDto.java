package com.ercopac.ercopac_tracker.crm.dto;

// commentNotifications: null (older clients) keeps the stored value / default ON
public record CrmNotificationPreferenceDto(boolean emailNotifications, boolean stageChangeAlerts, boolean closingDateReminders, Boolean commentNotifications) {
    public CrmNotificationPreferenceDto(boolean emailNotifications, boolean stageChangeAlerts, boolean closingDateReminders) {
        this(emailNotifications, stageChangeAlerts, closingDateReminders, true);
    }
}
