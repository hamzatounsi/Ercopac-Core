package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunityStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CrmOpportunityStageHistoryRepository extends JpaRepository<CrmOpportunityStageHistory, Long> {
    List<CrmOpportunityStageHistory> findByOpportunity_IdAndOrganisation_IdOrderByEnteredAtDesc(Long opportunityId, Long organisationId);
    @Modifying
    @Transactional
    void deleteAllByOpportunity_IdAndOrganisation_Id(Long opportunityId, Long organisationId);
}
