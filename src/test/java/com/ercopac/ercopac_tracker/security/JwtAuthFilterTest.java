package com.ercopac.ercopac_tracker.security;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private FilterChain chain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthFilter(jwtService, userRepository);
    }

    @Test
    void rejectsTokenWhenOrganisationClaimNoLongerMatchesDatabase() throws Exception {
        AppUser user = currentUser(7L, 10L, Role.ORG_ADMIN, true);
        when(jwtService.extractUsername("token")).thenReturn("admin@example.com");
        when(jwtService.extractUserId("token")).thenReturn(7L);
        when(jwtService.extractRole("token")).thenReturn("ORG_ADMIN");
        when(jwtService.extractOrganisationId("token")).thenReturn(99L);
        when(userRepository.findByEmail1("admin@example.com")).thenReturn(Optional.of(user));

        MockHttpServletResponse response = filter("token");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Authentication is no longer valid");
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsTokenForDeactivatedUser() throws Exception {
        AppUser user = currentUser(7L, 10L, Role.ORG_ADMIN, false);
        when(jwtService.extractUsername("token")).thenReturn("admin@example.com");
        when(userRepository.findByEmail1("admin@example.com")).thenReturn(Optional.of(user));

        MockHttpServletResponse response = filter("token");

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsOrganisationRoleWithoutTenantContext() throws Exception {
        AppUser user = new AppUser("Admin", "admin@example.com", "hash", Role.ORG_ADMIN);
        ReflectionTestUtils.setField(user, "id", 7L);
        user.setActive(true);
        when(jwtService.extractUsername("token")).thenReturn("admin@example.com");
        when(userRepository.findByEmail1("admin@example.com")).thenReturn(Optional.of(user));

        MockHttpServletResponse response = filter("token");

        assertThat(response.getStatus()).isEqualTo(401);
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private MockHttpServletResponse filter(String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/org-admin/overview");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    private AppUser currentUser(Long userId, Long organisationId, Role role, boolean active) {
        Organisation organisation = new Organisation();
        organisation.setId(organisationId);
        organisation.setName("Tenant");
        organisation.setCode("TENANT");

        AppUser user = new AppUser("Admin", "admin@example.com", "hash", role);
        ReflectionTestUtils.setField(user, "id", userId);
        user.setOrganisation(organisation);
        user.setActive(active);
        return user;
    }
}
