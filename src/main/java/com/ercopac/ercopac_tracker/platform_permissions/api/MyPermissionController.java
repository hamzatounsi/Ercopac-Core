package com.ercopac.ercopac_tracker.platform_permissions.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ercopac.ercopac_tracker.platform_permissions.service.MyPermissionService;

@RestController
@RequestMapping("/api/permissions/me")
public class MyPermissionController {

    private final MyPermissionService service;

    public MyPermissionController(MyPermissionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> myPermissions() {
        return ResponseEntity.ok(service.getMyPermissions());
    }
}