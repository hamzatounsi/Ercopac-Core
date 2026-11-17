package com.ercopac.ercopac_tracker.projectum.finance.web;

import com.ercopac.ercopac_tracker.config.SecurityConfig;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.security.PermissionChecker;
import com.ercopac.ercopac_tracker.projectum.finance.service.FinanceService;
import com.ercopac.ercopac_tracker.projectum.forecast.service.ForecastService;
import com.ercopac.ercopac_tracker.projectum.forecast.web.ForecastController;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {FinanceController.class, ForecastController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class FinanceForecastDeleteAuthorizationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean FinanceService financeService;
    @MockitoBean ForecastService forecastService;
    @MockitoBean(name = "permissionChecker") PermissionChecker permissionChecker;

    @Test
    @WithMockUser(username = "pm@example.com", authorities = "PROJECT_MANAGER")
    void deletesAreForbiddenWithoutWritePermission() throws Exception {
        when(permissionChecker.canWrite(any(), eq(PermissionModule.FINANCE))).thenReturn(false);
        when(permissionChecker.canWrite(any(), eq(PermissionModule.FORECAST))).thenReturn(false);

        mockMvc.perform(delete("/api/projects/1/finance/5")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/projects/1/forecast/5")).andExpect(status().isForbidden());

        verify(financeService, never()).deleteEntry(anyLong(), anyLong());
        verify(forecastService, never()).deleteForecastEntries(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = "pm@example.com", authorities = "PROJECT_MANAGER")
    void forecastWritePermissionOnlyAllowsDeletingForecastEntries() throws Exception {
        when(permissionChecker.canWrite(any(), eq(PermissionModule.FINANCE))).thenReturn(false);
        when(permissionChecker.canWrite(any(), eq(PermissionModule.FORECAST))).thenReturn(true);

        mockMvc.perform(delete("/api/projects/1/forecast/5")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/projects/1/finance/5")).andExpect(status().isForbidden());

        verify(forecastService).deleteForecastEntries(1L, 5L);
        verify(financeService, never()).deleteEntry(anyLong(), anyLong());
    }
}
