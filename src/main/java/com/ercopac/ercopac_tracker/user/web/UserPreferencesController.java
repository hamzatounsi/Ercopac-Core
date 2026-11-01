package com.ercopac.ercopac_tracker.user.web;

import com.ercopac.ercopac_tracker.user.dto.UpdateUserPreferencesRequest;
import com.ercopac.ercopac_tracker.user.dto.UserPreferencesDto;
import com.ercopac.ercopac_tracker.user.service.UserPreferencesService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/preferences")
public class UserPreferencesController {

    private final UserPreferencesService preferencesService;

    public UserPreferencesController(UserPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public UserPreferencesDto get() {
        return preferencesService.current();
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public UserPreferencesDto update(@Valid @RequestBody UpdateUserPreferencesRequest request) {
        return preferencesService.update(request);
    }
}
