package com.ercopac.ercopac_tracker.config;

import com.ercopac.ercopac_tracker.security.JwtAuthFilter;
import com.ercopac.ercopac_tracker.security.JwtService;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.org_admin.service.OrganisationAdminService;
import com.ercopac.ercopac_tracker.org_admin.web.OrganisationAdminController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrganisationAdminController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class SecurityConfigAuthorizationTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private OrganisationAdminService organisationAdminService;

    @Test
    @WithMockUser(authorities = "ORG_ADMIN")
    void organisationAdminCanAccessOnlyOrganisationAdministrationBoundary() throws Exception {
        when(organisationAdminService.getRoles()).thenReturn(List.of());

        mockMvc.perform(get("/api/org-admin/roles"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/platform/probe"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/api/gm/probe"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
