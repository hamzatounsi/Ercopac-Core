package com.ercopac.ercopac_tracker.crm.repository;
 
import com.ercopac.ercopac_tracker.crm.domain.CrmActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.util.List;
 
public interface CrmActivityRepository extends JpaRepository<CrmActivity, Long> {
	// Ajoutez cette méthode si elle n'existe pas déjà
	@Modifying
	@Transactional
	void deleteAllByOpportunity_IdAndOrganisation_Id(Long opportunityId, Long organisationId);
    List<CrmActivity> findByOrganisation_IdOrderByCreatedAtDesc(
            Long orgId, Pageable pageable);
 
    List<CrmActivity> findByLead_IdOrderByCreatedAtDesc(Long leadId);

    List<CrmActivity> findByLead_IdAndOrganisation_IdOrderByCreatedAtDesc(Long leadId, Long orgId);
 
    List<CrmActivity> findByOpportunity_IdOrderByCreatedAtDesc(Long opportunityId);
}
