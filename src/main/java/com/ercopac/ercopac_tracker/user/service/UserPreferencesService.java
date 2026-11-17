package com.ercopac.ercopac_tracker.user.service;

import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import com.ercopac.ercopac_tracker.user.dto.UpdateUserPreferencesRequest;
import com.ercopac.ercopac_tracker.user.dto.UserPreferencesDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Language and theme of the signed-in user. The user is always taken from the token, never from the request. */
@Service
public class UserPreferencesService {

    public static final String DEFAULT_LANGUAGE = "en";
    public static final String DEFAULT_THEME = "light";

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public UserPreferencesService(UserRepository userRepository, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public UserPreferencesDto current() {
        return toDto(currentUser());
    }

    @Transactional
    public UserPreferencesDto update(UpdateUserPreferencesRequest request) {
        if (request == null || (request.language() == null && request.theme() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide a language or a theme.");
        }
        AppUser user = currentUser();
        if (request.language() != null) user.setPreferredLanguage(request.language());
        if (request.theme() != null) user.setPreferredTheme(request.theme());
        return toDto(userRepository.save(user));
    }

    private AppUser currentUser() {
        Long userId = securityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .filter(AppUser::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }

    private UserPreferencesDto toDto(AppUser user) {
        String language = user.getPreferredLanguage();
        String theme = user.getPreferredTheme();
        return new UserPreferencesDto(
                language != null ? language : DEFAULT_LANGUAGE,
                theme != null ? theme : DEFAULT_THEME,
                language != null,
                theme != null);
    }
}
