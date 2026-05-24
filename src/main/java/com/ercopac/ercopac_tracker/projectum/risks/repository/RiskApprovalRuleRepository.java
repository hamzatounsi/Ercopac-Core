package com.ercopac.ercopac_tracker.projectum.risks.repository;

import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskApprovalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RiskApprovalRuleRepository extends JpaRepository<RiskApprovalRule, Long> {

    List<RiskApprovalRule> findAllByOrganisationIdAndProjectIdOrderByMinRiskValueAsc(
        Long organisationId, Long projectId);

    List<RiskApprovalRule> findAllByOrganisationIdAndProjectIdIsNullOrderByMinRiskValueAsc(
        Long organisationId);

    void deleteByIdAndOrganisationId(Long id, Long organisationId);
}