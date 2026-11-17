package com.ercopac.ercopac_tracker.user.web;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
public class UserPreferencesController {

    // Les 4 clés EXACTES que ton frontend attend (CrmNotificationPreference)
    private static final Map<String, Boolean> DEFAULTS = Map.of(
        "emailNotifications", true,
        "stageChangeAlerts", true,
        "closingDateReminders", false,
        "commentNotifications", true   // ← ON, comme ton frontend
    );

    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Boolean>> getPreferences() {
        return ResponseEntity.ok(DEFAULTS);
    }

    @PutMapping("/preferences")
    public ResponseEntity<Map<String, Boolean>> savePreferences(
            @RequestBody(required = false) Map<String, Object> body) {

        // On reconstruit l'objet complet à partir de ce qu'envoie le frontend,
        // en gardant les valeurs par défaut pour toute clé manquante.
        Map<String, Boolean> saved = new HashMap<>(DEFAULTS);

        if (body != null) {
            saved.put("emailNotifications", toBool(body.get("emailNotifications"), DEFAULTS.get("emailNotifications")));
            saved.put("stageChangeAlerts",  toBool(body.get("stageChangeAlerts"),  DEFAULTS.get("stageChangeAlerts")));
            saved.put("closingDateReminders", toBool(body.get("closingDateReminders"), DEFAULTS.get("closingDateReminders")));
            saved.put("commentNotifications", toBool(body.get("commentNotifications"), DEFAULTS.get("commentNotifications")));
        }

        // ✅ On renvoie l'objet complet → this.preferences = value garde les 4 clés
        return ResponseEntity.ok(saved);
    }

    // Convertit un Boolean, un String "true"/"false", ou null → boolean robuste
    private boolean toBool(Object v, boolean fallback) {
        if (v == null) return fallback;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(v.toString());
    }
}