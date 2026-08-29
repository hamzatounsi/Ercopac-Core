package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CrmNotificationPreferenceRepository extends JpaRepository<CrmNotificationPreference, Long> {
    Optional<CrmNotificationPreference> findByOrganisation_IdAndUser_Id(Long organisationId, Long userId);
}
