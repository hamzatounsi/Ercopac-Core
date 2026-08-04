package com.ercopac.ercopac_tracker.auth;

import com.ercopac.ercopac_tracker.auth.AuthDtos.LoginRequest;
import com.ercopac.ercopac_tracker.auth.AuthDtos.LoginResponse;
import com.ercopac.ercopac_tracker.auth.passwordreset.PasswordResetRequestRepository;
import com.ercopac.ercopac_tracker.auth.passwordreset.PasswordResetStatus;
import com.ercopac.ercopac_tracker.organisation.domain.OrganisationStatus;
import com.ercopac.ercopac_tracker.security.JwtService;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final LoginAttemptService loginAttemptService;

    public AuthController(
            AuthenticationManager authManager,
            JwtService jwtService,
            UserRepository userRepository,
            PasswordResetRequestRepository passwordResetRequestRepository,
            LoginAttemptService loginAttemptService
    ) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req, HttpServletRequest httpRequest) {
        String username = req == null || req.username() == null ? "" : req.username().trim();
        String password = req == null ? "" : req.password();
        String ipAddress = clientIp(httpRequest);

        loginAttemptService.assertNotLocked(username);
        AppUser candidateUser = userRepository.findByEmailIgnoreCase(username).orElse(null);
        int maxFailedAttempts = candidateUser != null && candidateUser.getOrganisation() != null
                ? candidateUser.getOrganisation().getMaxFailedLogins()
                : 5;

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (AuthenticationException ex) {
            loginAttemptService.recordFailure(username, ipAddress, maxFailedAttempts);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        AppUser user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authenticated user not found"));

        validateAccountCanLogin(user);

        boolean resetPending = passwordResetRequestRepository.existsByUser_IdAndStatus(
                user.getId(),
                PasswordResetStatus.PENDING
        );

        boolean resetApproved = passwordResetRequestRepository.existsByUser_IdAndStatus(
                user.getId(),
                PasswordResetStatus.APPROVED
        );

        if (resetPending) {
            loginAttemptService.recordSuccess(username, ipAddress);
            return new LoginResponse(
                    null,
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name(),
                    null,
                    null,
                    null,
                    true,
                    "PENDING",
                    null,
                    "Password reset request is waiting for admin approval."
            );
        }

        if (resetApproved) {
            String resetToken = passwordResetRequestRepository
                    .findFirstByUser_EmailAndStatusOrderByApprovedAtDesc(
                            user.getEmail(),
                            PasswordResetStatus.APPROVED
                    )
                    .filter(request -> request.getTokenExpiresAt() != null
                            && request.getTokenExpiresAt().isAfter(java.time.LocalDateTime.now()))
                    .map(request -> request.getToken())
                    .orElse(null);

            loginAttemptService.recordSuccess(username, ipAddress);
            return new LoginResponse(
                    null,
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name(),
                    null,
                    null,
                    null,
                    true,
                    "APPROVED",
                    resetToken,
                    "Password reset approved. Please set a new password."
            );
        }

        Long organisationId = user.getOrganisation() != null ? user.getOrganisation().getId() : null;
        String organisationCode = user.getOrganisation() != null ? user.getOrganisation().getCode() : null;
        String organisationName = user.getOrganisation() != null ? user.getOrganisation().getName() : null;

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                organisationId,
                organisationName,
                sessionDurationMillis(user)
        );

        loginAttemptService.recordSuccess(username, ipAddress);

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                organisationId,
                organisationCode,
                organisationName,
                false,
                null,
                null,
                null
        );
    }

    private long sessionDurationMillis(AppUser user) {
        String configured = user.getOrganisation() == null
                ? null
                : user.getOrganisation().getSessionTimeout();

        return switch (configured == null ? "4_HOURS" : configured.trim().toUpperCase()) {
            case "1_HOUR" -> 60 * 60 * 1000L;
            case "8_HOURS" -> 8 * 60 * 60 * 1000L;
            case "12_HOURS" -> 12 * 60 * 60 * 1000L;
            default -> 4 * 60 * 60 * 1000L;
        };
    }

    private void validateAccountCanLogin(AppUser user) {
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this account.");
        }

        if (user.getOrganisation() != null
                && (!user.getOrganisation().isActive()
                || user.getOrganisation().getStatus() == OrganisationStatus.SUSPENDED)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied for this account.");
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
