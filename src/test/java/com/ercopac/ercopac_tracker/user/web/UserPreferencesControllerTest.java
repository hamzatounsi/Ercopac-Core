package com.ercopac.ercopac_tracker.user.web;

import com.ercopac.ercopac_tracker.config.SecurityConfig;
import com.ercopac.ercopac_tracker.platform_permissions.security.PermissionChecker;
import com.ercopac.ercopac_tracker.security.JwtAuthFilter;
import com.ercopac.ercopac_tracker.security.JwtService;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.dto.UpdateUserPreferencesRequest;
import com.ercopac.ercopac_tracker.user.dto.UserPreferencesDto;
import com.ercopac.ercopac_tracker.user.service.UserPreferencesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// GET/PUT /api/users/me/preferences: signed-in users only, validated values
@WebMvcTest(controllers = UserPreferencesController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class UserPreferencesControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean UserPreferencesService preferences;
    @MockitoBean(name = "permissionChecker") PermissionChecker permissionChecker;

    @Test
    void anonymousRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/users/me/preferences")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/users/me/preferences").contentType("application/json").content("{\"theme\":\"dark\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(preferences);
    }

    @Test
    @WithMockUser(username = "client@example.com", authorities = {"CLIENT", "ROLE_CLIENT"})
    void anySignedInRoleReadsItsPreferences() throws Exception {
        when(preferences.current()).thenReturn(new UserPreferencesDto("en", "light", false, false));

        mockMvc.perform(get("/api/users/me/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.theme").value("light"))
                .andExpect(jsonPath("$.languageSaved").value(false));
    }

    @Test
    @WithMockUser(username = "employee@example.com", authorities = {"EMPLOYEE", "ROLE_EMPLOYEE"})
    void savesValidPreferences() throws Exception {
        when(preferences.update(new UpdateUserPreferencesRequest("ar", "dim"))).thenReturn(new UserPreferencesDto("ar", "dim", true, true));

        mockMvc.perform(put("/api/users/me/preferences").contentType("application/json").content("{\"language\":\"ar\",\"theme\":\"dim\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("ar"))
                .andExpect(jsonPath("$.themeSaved").value(true));
    }

    @Test
    @WithMockUser(username = "employee@example.com", authorities = {"EMPLOYEE", "ROLE_EMPLOYEE"})
    void rejectsUnsupportedValuesWithATranslatableCode() throws Exception {
        mockMvc.perform(put("/api/users/me/preferences").contentType("application/json").content("{\"language\":\"xx\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.field").value("language"));
        mockMvc.perform(put("/api/users/me/preferences").contentType("application/json").content("{\"theme\":\"neon\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("theme"));
        verify(preferences, never()).update(any());
    }

    @Test
    @WithMockUser(username = "employee@example.com", authorities = {"EMPLOYEE", "ROLE_EMPLOYEE"})
    void ignoresAUserIdInTheBody() throws Exception {
        when(preferences.update(any())).thenReturn(new UserPreferencesDto("fr", "light", true, false));

        mockMvc.perform(put("/api/users/me/preferences").contentType("application/json").content("{\"userId\":99,\"language\":\"fr\"}"))
                .andExpect(status().isOk());
        verify(preferences).update(new UpdateUserPreferencesRequest("fr", null));
    }
}
