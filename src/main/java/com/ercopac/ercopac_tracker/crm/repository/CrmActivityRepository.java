package com.ercopac.ercopac_tracker.crm.repository;
 
import com.ercopac.ercopac_tracker.crm.domain.CrmActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;
 
public interface CrmActivityRepository extends JpaRepository<CrmActivity, Long> {
 
    // Recent activity feed for dashboard (latest N)
    List<CrmActivity> findByOrganisation_IdOrderByCreatedAtDesc(
            Long orgId, Pageable pageable);
 
    List<CrmActivity> findByLead_IdOrderByCreatedAtDesc(Long leadId);
 
    List<CrmActivity> findByOpportunity_IdOrderByCreatedAtDesc(Long opportunityId);
}