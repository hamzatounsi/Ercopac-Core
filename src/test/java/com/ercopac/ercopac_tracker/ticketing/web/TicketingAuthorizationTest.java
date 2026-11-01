package com.ercopac.ercopac_tracker.ticketing.web;

import com.ercopac.ercopac_tracker.config.SecurityConfig;
import com.ercopac.ercopac_tracker.platform_permissions.security.PermissionChecker;
import com.ercopac.ercopac_tracker.security.JwtAuthFilter;
import com.ercopac.ercopac_tracker.security.JwtService;
import com.ercopac.ercopac_tracker.ticketing.dto.TicketDtos.*;
import com.ercopac.ercopac_tracker.ticketing.service.TicketAnalyticsService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketCustomerService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketSettingsService;
import com.ercopac.ercopac_tracker.ticketing.service.TicketTranslationService;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// H24 Lead access to the Ticketing settings endpoints (URL rule in SecurityConfig + @PreAuthorize on the controllers)
@WebMvcTest(controllers = {TicketSettingsController.class, TicketCustomerController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class TicketingAuthorizationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean TicketSettingsService settings;
    @MockitoBean TicketAnalyticsService analytics;
    @MockitoBean TicketTranslationService translation;
    @MockitoBean TicketCustomerService customers;
    @MockitoBean(name = "permissionChecker") PermissionChecker permissionChecker;

    @Test
    @WithMockUser(username = "lead@example.com", authorities = {"H24_LEAD", "ROLE_H24_LEAD"})
    void h24LeadReadsTheH24ListAndTheCascadingL2Choices() throws Exception {
        when(settings.agents("H24")).thenReturn(List.of());
        when(settings.resourceTypes()).thenReturn(List.of(new ResourceTypeOption(3L, "PLC", "PLC Engineer")));
        when(settings.employees(3L, true)).thenReturn(List.of(new EmployeeOption(9L, "Nora PLC", "n@example.test", 3L, "PLC Engineer", true, null)));

        mockMvc.perform(get("/api/tickets/settings/agents").param("group", "H24")).andExpect(status().isOk());
        mockMvc.perform(get("/api/tickets/resource-types")).andExpect(status().isOk()).andExpect(jsonPath("$[0].label").value("PLC Engineer"));
        mockMvc.perform(get("/api/tickets/employees").param("resourceTypeId", "3").param("l2Only", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].fullName").value("Nora PLC"));
    }

    @Test
    @WithMockUser(username = "lead@example.com", authorities = {"H24_LEAD", "ROLE_H24_LEAD"})
    void h24LeadAddsAManualCustomerProject() throws Exception {
        when(customers.createManualProject(any(), any())).thenReturn(new ProjectDto(4L, "P-2019-01", "Old line retrofit", null, 2019, true));

        mockMvc.perform(post("/api/tickets/customers/7/projects").contentType("application/json")
                        .content("{\"name\":\"Old line retrofit\",\"projectNumber\":\"P-2019-01\",\"year\":2019}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.manual").value(true));
    }

    @Test
    @WithMockUser(username = "agent@example.com", authorities = {"H24", "ROLE_H24"})
    void h24AgentCannotAddCustomerProjects() throws Exception {
        mockMvc.perform(post("/api/tickets/customers/7/projects").contentType("application/json").content("{\"name\":\"X\"}"))
                .andExpect(status().isForbidden());
        verify(customers, never()).createManualProject(any(), any());
    }

    @Test
    @WithMockUser(username = "employee@example.com", authorities = {"EMPLOYEE", "ROLE_EMPLOYEE"})
    void rolesOutsideTicketingAreRejected() throws Exception {
        mockMvc.perform(get("/api/tickets/employees").param("resourceTypeId", "3")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/tickets/settings/agents")).andExpect(status().isForbidden());
        verifyNoInteractions(settings);
    }
}
