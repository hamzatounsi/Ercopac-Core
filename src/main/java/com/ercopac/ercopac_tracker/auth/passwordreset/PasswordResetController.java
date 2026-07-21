package com.ercopac.ercopac_tracker.auth.passwordreset;

import com.ercopac.ercopac_tracker.auth.AuthDtos.CheckApprovedResetRequest;
import com.ercopac.ercopac_tracker.auth.AuthDtos.CheckApprovedResetResponse;
import com.ercopac.ercopac_tracker.auth.AuthDtos.ForgotPasswordRequest;
import com.ercopac.ercopac_tracker.auth.AuthDtos.MessageResponse;
import com.ercopac.ercopac_tracker.auth.AuthDtos.PasswordResetDecisionRequest;
import com.ercopac.ercopac_tracker.auth.AuthDtos.ResetPasswordRequest;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetService service;
    private final SecurityUtils securityUtils;

    public PasswordResetController(
            PasswordResetService service,
            SecurityUtils securityUtils
    ) {
        this.service = service;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/request")
    public MessageResponse requestReset(@RequestBody ForgotPasswordRequest request) {
        service.requestReset(request);

        return new MessageResponse(
                "If this email exists, a password reset request has been sent for approval."
        );
    }

    @PostMapping("/reset")
    public MessageResponse resetPassword(@RequestBody ResetPasswordRequest request) {
        service.resetPassword(request);
        return new MessageResponse("Password updated successfully.");
    }

    @GetMapping("/pending")
    public List<PasswordResetRequestDto> pending() {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        return service.pendingRequestDtos(organisationId);
    }

    @PostMapping("/{id}/approve")
    public MessageResponse approve(@PathVariable Long id) {
        Long adminUserId = securityUtils.getCurrentUserId();
        service.approve(id, adminUserId);
        return new MessageResponse("Password reset request approved.");
    }

    @PostMapping("/{id}/reject")
    public MessageResponse reject(
            @PathVariable Long id,
            @RequestBody(required = false) PasswordResetDecisionRequest request
    ) {
        Long adminUserId = securityUtils.getCurrentUserId();
        service.reject(id, adminUserId, request != null ? request.note() : null);
        return new MessageResponse("Password reset request rejected.");
    }

    @PostMapping("/check-approved")
    public CheckApprovedResetResponse checkApproved(@RequestBody CheckApprovedResetRequest request) {
        return service.checkApproved(request.email());
    }
}