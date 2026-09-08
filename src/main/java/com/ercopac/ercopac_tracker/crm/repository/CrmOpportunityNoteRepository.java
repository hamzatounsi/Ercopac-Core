package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunityNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CrmOpportunityNoteRepository extends JpaRepository<CrmOpportunityNote, Long> {
    List<CrmOpportunityNote> findByOpportunity_IdAndOrganisation_IdOrderByCreatedAtAsc(Long opportunityId, Long organisationId);
    Optional<CrmOpportunityNote> findByIdAndOpportunity_IdAndOrganisation_Id(Long id, Long opportunityId, Long organisationId);
    @Modifying
    @Transactional
    void deleteAllByOpportunity_IdAndOrganisation_Id(Long opportunityId, Long organisationId);
}
