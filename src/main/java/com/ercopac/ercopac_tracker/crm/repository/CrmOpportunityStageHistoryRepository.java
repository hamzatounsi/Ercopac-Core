package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunityStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CrmOpportunityStageHistoryRepository extends JpaRepository<CrmOpportunityStageHistory, Long> {
    List<CrmOpportunityStageHistory> findByOpportunity_IdAndOrganisation_IdOrderByEnteredAtDesc(Long opportunityId, Long organisationId);
}
