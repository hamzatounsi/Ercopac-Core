package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CrmOpportunityHistoryRepository extends JpaRepository<CrmOpportunityHistory, Long> {
    List<CrmOpportunityHistory> findByOpportunity_IdAndOrganisation_IdOrderByCreatedAtDesc(Long opportunityId, Long organisationId);
    @Modifying
    @Transactional
    void deleteAllByOpportunity_IdAndOrganisation_Id(Long opportunityId, Long organisationId);
}
