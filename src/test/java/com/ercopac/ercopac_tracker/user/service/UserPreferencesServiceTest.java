package com.ercopac.ercopac_tracker.user.service;

import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.dto.UpdateUserPreferencesRequest;
import com.ercopac.ercopac_tracker.user.dto.UserPreferencesDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

    @Mock UserRepository users;
    @Mock SecurityUtils securityUtils;

    private UserPreferencesService service;
    private AppUser me;

    @BeforeEach
    void setUp() {
        service = new UserPreferencesService(users, securityUtils);
        me = new AppUser("Sara Ben Ali", "sara@example.com", "hash", Role.EMPLOYEE);
        ReflectionTestUtils.setField(me, "id", 7L);
        lenient().when(securityUtils.getCurrentUserId()).thenReturn(7L);
        lenient().when(users.findById(7L)).thenReturn(Optional.of(me));
    }

    @Test
    void existingUsersGetTheDefaultsUntilTheyChoose() {
        assertThat(service.current()).isEqualTo(new UserPreferencesDto("en", "light", false, false));
    }

    @Test
    void updatesOnlyTheSignedInUserAndOnlyTheGivenFields() {
        me.setPreferredTheme("dark");
        when(users.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        UserPreferencesDto result = service.update(new UpdateUserPreferencesRequest("ar", null));

        assertThat(result).isEqualTo(new UserPreferencesDto("ar", "dark", true, true));
        assertThat(me.getPreferredLanguage()).isEqualTo("ar");
        verify(users).findById(7L);
        verify(users, never()).findById(argThat(id -> !id.equals(7L)));
    }

    @Test
    void anEmptyUpdateIsRejected() {
        assertThatThrownBy(() -> service.update(new UpdateUserPreferencesRequest(null, null)))
                .isInstanceOf(ResponseStatusException.class);
        verify(users, never()).save(any());
    }

    @Test
    void inactiveUsersCannotReadOrSavePreferences() {
        me.setActive(false);
        assertThatThrownBy(() -> service.current()).isInstanceOf(ResponseStatusException.class);
    }
}
