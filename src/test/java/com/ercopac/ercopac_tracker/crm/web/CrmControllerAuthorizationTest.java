package com.ercopac.ercopac_tracker.crm.web;

import com.ercopac.ercopac_tracker.config.SecurityConfig;
import com.ercopac.ercopac_tracker.crm.dto.CrmManagerViewDto;
import com.ercopac.ercopac_tracker.crm.service.CrmService;
import com.ercopac.ercopac_tracker.crm.service.CrmEquipmentService;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CrmController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class CrmControllerAuthorizationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean CrmService service;
    @MockitoBean CrmEquipmentService equipmentService;
    @MockitoBean(name = "permissionChecker") PermissionChecker permissionChecker;

    @Test
    @WithMockUser(username = "engineer@example.com", authorities = "SYSTEM_ENGINEER")
    void systemEngineerCanReadButDirectWriteRequestReturnsForbidden() throws Exception {
        when(permissionChecker.canRead(any(), eq(PermissionModule.CRM))).thenReturn(true);
        when(permissionChecker.canWrite(any(), eq(PermissionModule.CRM))).thenReturn(false);
        when(service.getAccounts(11L, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/crm/organisations/11/accounts"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/crm/organisations/11/accounts")
                        .contentType("application/json")
                        .content("{\"name\":\"Forbidden account\"}"))
                .andExpect(status().isForbidden());

        verify(service, never()).createAccount(any(), any());
    }

    @Test
    @WithMockUser(username = "sales@example.com", authorities = "SALES_MANAGER")
    void salesManagerDirectManagerViewRequestReturnsForbidden() throws Exception {
        when(permissionChecker.canAccessCrmManagerView(any())).thenReturn(false);

        mockMvc.perform(get("/api/crm/organisations/11/manager"))
                .andExpect(status().isForbidden());

        verify(service, never()).getManagerView(any(), anyInt());
    }

    @Test
    @WithMockUser(username = "lead@example.com", authorities = "SALES_MANAGER_LEAD")
    void salesManagerLeadCanAccessManagerView() throws Exception {
        when(permissionChecker.canAccessCrmManagerView(any())).thenReturn(true);
        when(service.getManagerView(eq(11L), anyInt())).thenReturn(new CrmManagerViewDto(List.of(), List.of(), 2026));

        mockMvc.perform(get("/api/crm/organisations/11/manager").param("year", "2026"))
                .andExpect(status().isOk());
    }
}
