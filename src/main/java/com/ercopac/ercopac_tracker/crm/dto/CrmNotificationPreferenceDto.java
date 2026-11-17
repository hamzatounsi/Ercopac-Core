package com.ercopac.ercopac_tracker.crm.dto;

public record CrmNotificationPreferenceDto(
    boolean emailNotifications,
    boolean stageChangeAlerts,
    boolean closingDateReminders,
    boolean commentNotifications
) {
    // Le record est juste un conteneur de données. Aucune logique métier ici.
}