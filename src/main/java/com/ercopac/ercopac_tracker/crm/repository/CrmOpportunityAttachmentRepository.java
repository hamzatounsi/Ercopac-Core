package com.ercopac.ercopac_tracker.crm.repository;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunityAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CrmOpportunityAttachmentRepository extends JpaRepository<CrmOpportunityAttachment, Long> {
    List<CrmOpportunityAttachment> findByOpportunity_IdAndOrganisation_IdOrderByUploadedAtDesc(Long opportunityId, Long organisationId);
    Optional<CrmOpportunityAttachment> findByIdAndOpportunity_IdAndOrganisation_Id(Long id, Long opportunityId, Long organisationId);
}
