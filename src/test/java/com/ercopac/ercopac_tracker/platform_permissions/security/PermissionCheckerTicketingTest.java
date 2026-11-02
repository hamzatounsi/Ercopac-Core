package com.ercopac.ercopac_tracker.platform_permissions.security;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.repository.RolePermissionRepository;
import com.ercopac.ercopac_tracker.platform_permissions.service.PlatformPermissionService;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_permissions.dto.RolePermissionDto;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// The Ticketing module in the platform permission matrix: fixed for the Ticketing roles, like CRM for CRM roles
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermissionCheckerTicketingTest {
    @Mock RolePermissionRepository permissionRepository;
    @Mock UserRepository userRepository;
    @Mock OrganisationRepository organisationRepository;

    @Test
    void h24LeadAlwaysHasTicketingAccess() {
        PermissionChecker checker = checkerFor(Role.H24_LEAD);

        assertTrue(checker.canRead(authentication(), PermissionModule.TICKETING));
        assertTrue(checker.canWrite(authentication(), PermissionModule.TICKETING));
    }

    @Test
    void employeeWithoutAGrantHasNoTicketingAccess() {
        when(permissionRepository.findByOrganisation_IdAndRoleAndModule(any(), any(), any())).thenReturn(Optional.empty());
        PermissionChecker checker = checkerFor(Role.EMPLOYEE);

        assertFalse(checker.canRead(authentication(), PermissionModule.TICKETING));
        assertFalse(checker.canWrite(authentication(), PermissionModule.TICKETING));
    }

    @Test
    void ownerMatrixShowsTicketingAsFixedAccessForH24Lead() {
        when(permissionRepository.findByOrganisation_IdAndRole(11L, Role.H24_LEAD)).thenReturn(List.of());
        PlatformPermissionService service = new PlatformPermissionService(organisationRepository, permissionRepository);

        RolePermissionDto ticketing = service.getPermissions(11L, Role.H24_LEAD).stream()
                .filter(p -> "TICKETING".equals(p.module)).findFirst().orElseThrow();

        assertTrue(ticketing.canRead);
        assertTrue(ticketing.canWrite);
        assertEquals("Ticketing Workspace", ticketing.group);
        assertTrue(service.getRoles().contains("H24_LEAD"));
    }

    private PermissionChecker checkerFor(Role role) {
        Organisation organisation = new Organisation();
        organisation.setId(11L);
        AppUser user = new AppUser("Support User", "support@example.com", "hash", role);
        user.setOrganisation(organisation);
        when(userRepository.findByEmail1("support@example.com")).thenReturn(Optional.of(user));
        return new PermissionChecker(permissionRepository, userRepository);
    }

    private Authentication authentication() {
        return new UsernamePasswordAuthenticationToken("support@example.com", "n/a");
    }
}
