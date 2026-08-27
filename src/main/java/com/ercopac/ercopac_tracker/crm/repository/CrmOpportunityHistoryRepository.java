package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CrmOpportunityHistoryRepository extends JpaRepository<CrmOpportunityHistory, Long> {
    List<CrmOpportunityHistory> findByOpportunity_IdAndOrganisation_IdOrderByCreatedAtDesc(Long opportunityId, Long organisationId);
}
