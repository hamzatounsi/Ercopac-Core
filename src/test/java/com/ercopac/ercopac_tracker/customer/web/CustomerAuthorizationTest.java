package com.ercopac.ercopac_tracker.customer.web;

import com.ercopac.ercopac_tracker.config.SecurityConfig;
import com.ercopac.ercopac_tracker.customer.dto.CustomerReferences;
import com.ercopac.ercopac_tracker.customer.service.CustomerConflictException;
import com.ercopac.ercopac_tracker.customer.service.CustomerManagementService;
import com.ercopac.ercopac_tracker.org_admin.dto.OrgAdminDtos;
import com.ercopac.ercopac_tracker.platform_permissions.security.PermissionChecker;
import com.ercopac.ercopac_tracker.security.JwtAuthFilter;
import com.ercopac.ercopac_tracker.security.JwtService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Customer Management is ORG_ADMIN only; customer options are for roles that pick customers in forms.
@WebMvcTest(controllers = {OrgAdminCustomerController.class, CustomerOptionsController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class CustomerAuthorizationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean CustomerManagementService service;
    @MockitoBean(name = "permissionChecker") PermissionChecker permissionChecker;

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ORG_ADMIN", "ROLE_ORG_ADMIN"})
    void orgAdminListsCustomers() throws Exception {
        when(service.list(any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(new OrgAdminDtos.PageResponse<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/org-admin/customers").param("search", "acme")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "pm@example.com", authorities = {"PROJECT_MANAGER", "ROLE_PROJECT_MANAGER"})
    void otherRolesCannotManageCustomers() throws Exception {
        mockMvc.perform(get("/api/org-admin/customers")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/org-admin/customers").contentType("application/json").content("{\"name\":\"X\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/org-admin/customers/5")).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ORG_ADMIN", "ROLE_ORG_ADMIN"})
    void deletingAReferencedCustomerReturnsConflictWithReferenceCounts() throws Exception {
        doThrow(CustomerConflictException.inUse(new CustomerReferences(2, 1, 0))).when(service).delete(5L);

        mockMvc.perform(delete("/api/org-admin/customers/5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_IN_USE"))
                .andExpect(jsonPath("$.references.projects").value(2))
                .andExpect(jsonPath("$.references.total").value(3));
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ORG_ADMIN", "ROLE_ORG_ADMIN"})
    void customerWithoutNameIsRejected() throws Exception {
        mockMvc.perform(post("/api/org-admin/customers").contentType("application/json").content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
        verify(service, never()).create(any());
    }

    @Test
    @WithMockUser(username = "pm@example.com", authorities = {"PROJECT_MANAGER", "ROLE_PROJECT_MANAGER"})
    void projectManagerReadsCustomerOptions() throws Exception {
        when(service.options(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/customers/options")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "client@example.com", authorities = {"CLIENT", "ROLE_CLIENT"})
    void clientCannotListCustomers() throws Exception {
        mockMvc.perform(get("/api/customers/options")).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
