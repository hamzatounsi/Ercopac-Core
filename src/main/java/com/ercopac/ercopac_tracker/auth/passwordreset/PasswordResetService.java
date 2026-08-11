package com.ercopac.ercopac_tracker.auth.passwordreset;

import com.ercopac.ercopac_tracker.auth.AuthDtos.CheckApprovedResetResponse;
import com.ercopac.ercopac_tracker.auth.AuthDtos.ForgotPasswordRequest;
import com.ercopac.ercopac_tracker.auth.AuthDtos.ResetPasswordRequest;
import com.ercopac.ercopac_tracker.notifications.domain.NotificationChannel;
import com.ercopac.ercopac_tracker.notifications.dto.NotificationRequest;
import com.ercopac.ercopac_tracker.notifications.service.NotificationService;
import com.ercopac.ercopac_tracker.notifications.service.ProjectumMailService;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class PasswordResetService {

    private final PasswordResetRequestRepository repository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final ProjectumMailService mailService;
    private final String frontendBaseUrl;

    public PasswordResetService(
            PasswordResetRequestRepository repository,
            UserRepository userRepository,
            NotificationService notificationService,
            PasswordEncoder passwordEncoder,
            ProjectumMailService mailService,
            @Value("${projectum.frontend-url:http://localhost:4200}") String frontendBaseUrl
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
    }

    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElse(null);

        // Security: do not reveal if email exists or not
        if (user == null || !user.isActive() || user.getOrganisation() == null) {
            return;
        }

        if (repository.existsByUser_IdAndStatus(user.getId(), PasswordResetStatus.PENDING)) {
            return;
        }
        Long organisationId = user.getOrganisation().getId();

        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setUser(user);
        resetRequest.setOrganisationId(organisationId);
        resetRequest.setStatus(PasswordResetStatus.PENDING);
        resetRequest.setRequestedAt(LocalDateTime.now());
        repository.save(resetRequest);
        notifyAdmins(user, resetRequest);
    }

    @Transactional(readOnly = true)
    public List<PasswordResetRequestDto> pendingRequestDtos(Long organisationId) {
        return repository.findByOrganisationIdAndStatusOrderByRequestedAtDesc(
                organisationId,
                PasswordResetStatus.PENDING
        ).stream().map(req -> new PasswordResetRequestDto(
                req.getId(),
                req.getUser().getId(),
                req.getUser().getFullName(),
                req.getUser().getEmail(),
                req.getStatus().name(),
                req.getRequestedAt()
        )).toList();
    }

    @Transactional(readOnly = true)
    public CheckApprovedResetResponse checkApproved(String email) {
        return repository.findFirstByUser_EmailAndStatusOrderByApprovedAtDesc(
                email,
                PasswordResetStatus.APPROVED
        ).map(req -> {
            if (req.getTokenExpiresAt() == null || req.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
                return new CheckApprovedResetResponse(false, null, "Reset request expired.");
            }

            return new CheckApprovedResetResponse(true, null, "Reset request approved. Please use the reset link sent to your email.");
        }).orElse(new CheckApprovedResetResponse(false, null, "No approved reset request found."));
    }

    private void notifyAdmins(AppUser user, PasswordResetRequest resetRequest) {
        Long organisationId = user.getOrganisation().getId();

        List<AppUser> admins = userRepository.findByOrganisation_IdAndRoleOrderByFullNameAsc(
                organisationId,
                Role.ORG_ADMIN
        );

        if (admins.isEmpty()) {
            admins = userRepository.findByOrganisation_IdAndRoleOrderByFullNameAsc(
                    organisationId,
                    Role.PROJECT_MANAGER
            );
        }

        for (AppUser admin : admins) {
            if (Boolean.FALSE.equals(admin.getEmailNotificationsEnabled())) {
                continue;
            }

            mailService.sendText(admin.getEmail(), "Password reset request - Projectum",
                    "User: " + user.getFullName() + "\nEmail: " + user.getEmail()
                            + "\nRequested: " + resetRequest.getRequestedAt()
                            + "\nReview in Projectum: " + frontendBaseUrl + "/workspace");
        }
    }

    @Transactional(readOnly = true)
    public List<PasswordResetRequest> pendingRequests(Long organisationId) {
        return repository.findByOrganisationIdAndStatusOrderByRequestedAtDesc(
                organisationId,
                PasswordResetStatus.PENDING
        );
    }

    @Transactional
    public void approve(Long requestId, Long adminUserId) {
        PasswordResetRequest request = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Password reset request not found"));

        validateAdminCanManageRequest(request, adminUserId);

        if (request.getStatus() != PasswordResetStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        String token = generateSecureToken();

        request.setStatus(PasswordResetStatus.APPROVED);
        request.setApprovedByUserId(adminUserId);
        request.setApprovedAt(LocalDateTime.now());
        request.setToken(token);
        request.setTokenExpiresAt(LocalDateTime.now().plusHours(1));

        repository.save(request);

        sendResetLinkToUser(request);
    }

    @Transactional
    public void reject(Long requestId, Long adminUserId, String note) {
        PasswordResetRequest request = repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Password reset request not found"));

        validateAdminCanManageRequest(request, adminUserId);

        if (request.getStatus() != PasswordResetStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setStatus(PasswordResetStatus.REJECTED);
        request.setApprovedByUserId(adminUserId);
        request.setRejectedAt(LocalDateTime.now());
        request.setAdminNote(note);

        repository.save(request);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetRequest resetRequest = repository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        if (resetRequest.getStatus() != PasswordResetStatus.APPROVED) {
            throw new IllegalStateException("Reset token is not active");
        }

        if (resetRequest.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            resetRequest.setStatus(PasswordResetStatus.EXPIRED);
            repository.save(resetRequest);
            throw new IllegalStateException("Reset token expired");
        }

        AppUser user = resetRequest.getUser();
        int minimumLength = user.getOrganisation() == null
                ? 8
                : Math.max(8, user.getOrganisation().getPasswordMinLength());
        if (request.newPassword() == null || request.newPassword().length() < minimumLength) {
            throw new IllegalArgumentException(
                    "Password must contain at least " + minimumLength + " characters"
            );
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetRequest.setStatus(PasswordResetStatus.USED);
        resetRequest.setUsedAt(LocalDateTime.now());
        resetRequest.setToken(null);
        repository.save(resetRequest);
    }

    private void sendResetLinkToUser(PasswordResetRequest request) {
        AppUser user = request.getUser();

        String resetLink = frontendBaseUrl + "/reset-password?token=" + request.getToken();
        mailService.sendText(user.getEmail(), "Reset your Projectum password",
                "Projectum password reset\n\nUse this link within 45 minutes:\n" + resetLink
                        + "\n\nIf you did not request this, you can ignore this email.");

        NotificationRequest notification = new NotificationRequest(
                request.getOrganisationId(),
                null,
                null,
                user.getId(),
                user.getEmail(),
                NotificationChannel.EMAIL,
                "INFO",
                "Reset your Projectum password",
                "Your password reset request was approved.",
                """
                <div style="font-family:Arial,sans-serif">
                  <h2>Password reset approved</h2>
                  <p>Your password reset request was approved.</p>
                  <p>Click the link below to create a new password:</p>
                  <p><a href="%s">Reset password</a></p>
                  <p>This link expires in 1 hour.</p>
                </div>
                """.formatted(resetLink)
        );

        var saved = notificationService.create(notification);
        notificationService.sendAsync(saved.getId());
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void validateAdminCanManageRequest(PasswordResetRequest request, Long adminUserId) {
        AppUser admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        if (admin.getRole() == Role.PLATFORM_OWNER) {
            return;
        }

        Long adminOrganisationId = admin.getOrganisation() != null ? admin.getOrganisation().getId() : null;
        if (adminOrganisationId == null || !adminOrganisationId.equals(request.getOrganisationId())) {
            throw new IllegalArgumentException("Password reset request not accessible");
        }
    }
}
