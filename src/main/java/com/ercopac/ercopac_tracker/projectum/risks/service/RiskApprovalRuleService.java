package com.ercopac.ercopac_tracker.projectum.risks.service;

import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskApprovalRule;
import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskApprovalRuleDto;
import com.ercopac.ercopac_tracker.projectum.risks.dto.UpsertRiskApprovalRuleRequest;
import com.ercopac.ercopac_tracker.projectum.risks.repository.RiskApprovalRuleRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RiskApprovalRuleService {

    private final RiskApprovalRuleRepository ruleRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    public RiskApprovalRuleService(RiskApprovalRuleRepository ruleRepository,
                                   SecurityUtils securityUtils,
                                   UserRepository userRepository) {
        this.ruleRepository = ruleRepository;
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<RiskApprovalRuleDto> getRules(Long projectId) {
        Long orgId = securityUtils.getCurrentOrganisationId();

        List<RiskApprovalRule> rules = projectId != null
            ? ruleRepository.findAllByOrganisationIdAndProjectIdOrderByMinRiskValueAsc(orgId, projectId)
            : ruleRepository.findAllByOrganisationIdAndProjectIdIsNullOrderByMinRiskValueAsc(orgId);

        return rules.stream().map(this::toDto).toList();
    }

    public RiskApprovalRuleDto create(Long projectId, UpsertRiskApprovalRuleRequest request) {
        Long orgId = securityUtils.getCurrentOrganisationId();

        RiskApprovalRule rule = new RiskApprovalRule();
        rule.setOrganisationId(orgId);
        rule.setProjectId(projectId);
        applyFields(rule, request);

        return toDto(ruleRepository.save(rule));
    }

    public RiskApprovalRuleDto update(Long ruleId, UpsertRiskApprovalRuleRequest request) {
        Long orgId = securityUtils.getCurrentOrganisationId();

        RiskApprovalRule rule = ruleRepository.findById(ruleId)
            .filter(r -> r.getOrganisationId().equals(orgId))
            .orElseThrow(() -> new IllegalArgumentException("Rule not found"));

        applyFields(rule, request);
        return toDto(ruleRepository.save(rule));
    }

    public void delete(Long ruleId) {
        Long orgId = securityUtils.getCurrentOrganisationId();
        ruleRepository.deleteByIdAndOrganisationId(ruleId, orgId);
    }

    private void applyFields(RiskApprovalRule rule, UpsertRiskApprovalRuleRequest request) {
        rule.setRiskLevel(request.getRiskLevel());
        rule.setMinRiskValue(request.getMinRiskValue());
        rule.setApproverRole(request.getApproverRole());
        rule.setApproverUserId(request.getApproverUserId());
    }

    private RiskApprovalRuleDto toDto(RiskApprovalRule rule) {
        RiskApprovalRuleDto dto = new RiskApprovalRuleDto();
        dto.setId(rule.getId());
        dto.setProjectId(rule.getProjectId());
        dto.setRiskLevel(rule.getRiskLevel());
        dto.setMinRiskValue(rule.getMinRiskValue());
        dto.setApproverRole(rule.getApproverRole());
        dto.setApproverUserId(rule.getApproverUserId());

        // resolve approver name
        if (rule.getApproverUserId() != null) {
            userRepository.findById(rule.getApproverUserId())
                .ifPresent(u -> dto.setApproverUserName(u.getFullName()));
        }

        return dto;
    }
}