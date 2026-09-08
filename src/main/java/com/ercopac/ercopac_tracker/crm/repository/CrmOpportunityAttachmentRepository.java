package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunityAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CrmOpportunityAttachmentRepository extends JpaRepository<CrmOpportunityAttachment, Long> {
    List<CrmOpportunityAttachment> findByOpportunity_IdAndOrganisation_IdOrderByUploadedAtDesc(Long opportunityId, Long organisationId);
    Optional<CrmOpportunityAttachment> findByIdAndOpportunity_IdAndOrganisation_Id(Long id, Long opportunityId, Long organisationId);
    @Modifying
    @Transactional
    void deleteAllByOpportunity_IdAndOrganisation_Id(Long opportunityId, Long organisationId);
}
