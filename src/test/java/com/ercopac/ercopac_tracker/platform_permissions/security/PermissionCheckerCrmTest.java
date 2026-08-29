package com.ercopac.ercopac_tracker.platform_permissions.security;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule;
import com.ercopac.ercopac_tracker.platform_permissions.repository.RolePermissionRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionCheckerCrmTest {
    @Mock RolePermissionRepository permissionRepository;
    @Mock UserRepository userRepository;

    @Test
    void salesManagerLeadHasCrmWriteAndExclusiveManagerViewAccess() {
        PermissionChecker checker = checkerFor(Role.SALES_MANAGER_LEAD);
        Authentication authentication = authentication();

        assertTrue(checker.canRead(authentication, PermissionModule.CRM));
        assertTrue(checker.canWrite(authentication, PermissionModule.CRM));
        assertTrue(checker.canAccessCrmManagerView(authentication));
    }

    @Test
    void salesManagerCanWriteCrmButCannotAccessManagerView() {
        PermissionChecker checker = checkerFor(Role.SALES_MANAGER);
        Authentication authentication = authentication();

        assertTrue(checker.canRead(authentication, PermissionModule.CRM));
        assertTrue(checker.canWrite(authentication, PermissionModule.CRM));
        assertFalse(checker.canAccessCrmManagerView(authentication));
    }

    @Test
    void systemEngineerHasSalesManagerCrmAccessButNotManagerView() {
        PermissionChecker checker = checkerFor(Role.SYSTEM_ENGINEER);
        Authentication authentication = authentication();

        assertTrue(checker.canRead(authentication, PermissionModule.CRM));
        assertTrue(checker.canWrite(authentication, PermissionModule.CRM));
        assertFalse(checker.canAccessCrmManagerView(authentication));
    }

    private PermissionChecker checkerFor(Role role) {
        Organisation organisation = new Organisation();
        organisation.setId(11L);
        AppUser user = new AppUser("CRM User", "crm@example.com", "hash", role);
        user.setOrganisation(organisation);
        when(userRepository.findByEmail1("crm@example.com")).thenReturn(Optional.of(user));
        return new PermissionChecker(permissionRepository, userRepository);
    }

    private Authentication authentication() {
        return new UsernamePasswordAuthenticationToken("crm@example.com", "unused");
    }
}
