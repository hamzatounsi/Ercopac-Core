package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.LostReason;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LostReasonRepository extends JpaRepository<LostReason, Long> {
    List<LostReason> findByOrganisation_IdOrderByCreatedAtAsc(Long orgId);
    List<LostReason> findByOrganisation_IdAndActiveTrueOrderByCreatedAtAsc(Long orgId);
    Optional<LostReason> findByIdAndOrganisation_Id(Long id, Long orgId);
}