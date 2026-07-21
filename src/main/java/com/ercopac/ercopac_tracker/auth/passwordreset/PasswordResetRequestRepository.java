package com.ercopac.ercopac_tracker.auth.passwordreset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {

    List<PasswordResetRequest> findByOrganisationIdAndStatusOrderByRequestedAtDesc(
            Long organisationId,
            PasswordResetStatus status
    );

    Optional<PasswordResetRequest> findByToken(String token);

    boolean existsByUser_IdAndStatus(Long userId, PasswordResetStatus status);

    long countByOrganisationIdAndStatus(Long organisationId, PasswordResetStatus status);

    Optional<PasswordResetRequest> findFirstByUser_EmailAndStatusOrderByApprovedAtDesc(
            String email,
            PasswordResetStatus status
    );
}
