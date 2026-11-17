package com.ercopac.ercopac_tracker.crm.dto;

/**
 * DTO pour les préférences de notification.
 * Utilise Boolean (objet) pour commentNotifications afin de gérer les nulls 
 * provenant d'anciens clients de manière élégante.
 */
public record CrmNotificationPreferenceDto(
        boolean emailNotifications,
        boolean stageChangeAlerts,
        boolean closingDateReminders,
        Boolean commentNotifications
) {
    // Constructeur de compatibilité pour les anciens clients qui n'envoient que 3 paramètres.
    // Il force commentNotifications à true (ON) par défaut.
    public CrmNotificationPreferenceDto(boolean emailNotifications, boolean stageChangeAlerts, boolean closingDateReminders) {
        this(emailNotifications, stageChangeAlerts, closingDateReminders, true);
    }
}