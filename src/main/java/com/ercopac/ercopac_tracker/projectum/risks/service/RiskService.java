package com.ercopac.ercopac_tracker.projectum.risks.service;

import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskItem;
import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskItemDto;
import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskSummaryDto;
import com.ercopac.ercopac_tracker.projectum.risks.dto.UpsertRiskItemRequest;
import com.ercopac.ercopac_tracker.projectum.risks.repository.RiskItemRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class RiskService {

    private final RiskItemRepository riskItemRepository;
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;

    public RiskService(RiskItemRepository riskItemRepository,
                       ProjectRepository projectRepository,
                       SecurityUtils securityUtils) {
        this.riskItemRepository = riskItemRepository;
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<RiskItemDto> getProjectRisks(Long projectId) {
        Project project = getAccessibleProject(projectId);

        List<RiskItem> items = securityUtils.isPlatformUser()
                ? riskItemRepository.findAllByProjectIdOrderByIdAsc(projectId)
                : riskItemRepository.findAllByProjectIdAndOrganisationIdOrderByIdAsc(
                    projectId, project.getOrganisation().getId());

        return items.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RiskSummaryDto getSummary(Long projectId) {
        List<RiskItemDto> items = getProjectRisks(projectId);

        RiskSummaryDto dto = new RiskSummaryDto();
        dto.setTotal(items.size());

        long riskExposure = 0;
        long oppExposure = 0;

        for (RiskItemDto item : items) {
            int rv = item.getRiskValue() == null ? 0 : item.getRiskValue();

            if ("crit".equals(item.getRiskLevel())) dto.setCritical(dto.getCritical() + 1);
            else if ("hi".equals(item.getRiskLevel())) dto.setHigh(dto.getHigh() + 1);
            else if ("med".equals(item.getRiskLevel())) dto.setMedium(dto.getMedium() + 1);
            else dto.setLow(dto.getLow() + 1);

            if (!"closed".equalsIgnoreCase(item.getState())) {
                dto.setOpenRisks(dto.getOpenRisks() + 1);
            }

            if ("open".equalsIgnoreCase(item.getVarianceStatus())) {
                dto.setPendingVariance(dto.getPendingVariance() + 1);
            }

            if ("opportunity".equalsIgnoreCase(item.getRiskType())) {
                dto.setOpportunityCount(dto.getOpportunityCount() + 1);
                oppExposure += rv;
            } else {
                dto.setRiskCount(dto.getRiskCount() + 1);
                riskExposure += rv;
            }
        }

        dto.setNetExposureScore(riskExposure - oppExposure);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<RiskItemDto> getPendingApprovals(Long projectId) {
        return getProjectRisks(projectId).stream()
                .filter(item ->
                        ("variance".equalsIgnoreCase(item.getState()) || "cr".equalsIgnoreCase(item.getState()))
                                && "open".equalsIgnoreCase(item.getVarianceStatus()))
                .toList();
    }

    public RiskItemDto create(Long projectId, UpsertRiskItemRequest request) {
        Project project = getAccessibleProject(projectId);

        RiskItem item = new RiskItem();
        item.setProject(project);
        item.setOrganisation(project.getOrganisation());
        applyEditableFields(item, request);

        return toDto(riskItemRepository.save(item));
    }

    public RiskItemDto update(Long projectId, Long itemId, UpsertRiskItemRequest request) {
        RiskItem item = getAccessibleRiskItem(projectId, itemId);
        applyEditableFields(item, request);
        return toDto(riskItemRepository.save(item));
    }

    public RiskItemDto approve(Long projectId, Long itemId) {
        RiskItem item = getAccessibleRiskItem(projectId, itemId);

        if (!requiresApproval(item)) {
            throw new IllegalArgumentException("This risk does not require approval");
        }

        item.setVarianceStatus("approved");
        item.setApprovedBy(getCurrentUsername());
        item.setApprovedAt(LocalDate.now());

        return toDto(riskItemRepository.save(item));
    }

    public RiskItemDto reject(Long projectId, Long itemId) {
        RiskItem item = getAccessibleRiskItem(projectId, itemId);

        if (!requiresApproval(item)) {
            throw new IllegalArgumentException("This risk does not require approval");
        }

        item.setState("managing");
        item.setVarianceStatus(null);
        item.setApprovedBy(null);
        item.setApprovedAt(null);

        return toDto(riskItemRepository.save(item));
    }

    public void delete(Long projectId, Long itemId) {
        RiskItem item = getAccessibleRiskItem(projectId, itemId);
        riskItemRepository.delete(item);
    }

    private void applyEditableFields(RiskItem item, UpsertRiskItemRequest request) {
        item.setRiskType(normalizeRiskType(request.getRiskType()));
        item.setState(normalizeState(request.getState()));
        item.setDescription(request.getDescription());
        item.setInputDate(request.getInputDate());
        item.setDueDate(request.getDueDate());
        item.setMitigation(request.getMitigation());
        item.setOwnerDept(request.getOwnerDept());
        item.setOwner(request.getOwner());
        item.setWbsCode(request.getWbsCode());
        item.setImpact(request.getImpact() == null ? 1 : request.getImpact());
        item.setProbability(request.getProbability() == null ? 1 : request.getProbability());
        item.setNotes(request.getNotes());

        if (requiresApproval(item)) {
            if (item.getVarianceStatus() == null || item.getVarianceStatus().isBlank()) {
                item.setVarianceStatus("open");
            }
        } else {
            item.setVarianceStatus(null);
            item.setApprovedBy(null);
            item.setApprovedAt(null);
        }
    }

    private boolean requiresApproval(RiskItem item) {
        return "variance".equalsIgnoreCase(item.getState()) || "cr".equalsIgnoreCase(item.getState());
    }

    private String normalizeRiskType(String riskType) {
        if (riskType == null) return "risk";
        String value = riskType.trim().toLowerCase();
        return "opportunity".equals(value) ? "opportunity" : "risk";
    }

    private String normalizeState(String state) {
        if (state == null || state.isBlank()) return "new";
        return state.trim().toLowerCase();
    }

    private RiskItem getAccessibleRiskItem(Long projectId, Long itemId) {
        Project project = getAccessibleProject(projectId);

        return securityUtils.isPlatformUser()
                ? riskItemRepository.findById(itemId)
                    .filter(item -> item.getProject().getId().equals(projectId))
                    .orElseThrow(() -> new IllegalArgumentException("Risk item not found"))
                : riskItemRepository.findByIdAndProjectIdAndOrganisationId(
                    itemId, projectId, project.getOrganisation().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Risk item not found"));
    }

    private RiskItemDto toDto(RiskItem item) {
        RiskItemDto dto = new RiskItemDto();
        dto.setId(item.getId());
        dto.setRiskType(item.getRiskType());
        dto.setState(item.getState());
        dto.setDescription(item.getDescription());
        dto.setInputDate(item.getInputDate());
        dto.setDueDate(item.getDueDate());
        dto.setMitigation(item.getMitigation());
        dto.setOwnerDept(item.getOwnerDept());
        dto.setOwner(item.getOwner());
        dto.setWbsCode(item.getWbsCode());
        dto.setImpact(item.getImpact());
        dto.setProbability(item.getProbability());

        int rv = (item.getImpact() == null ? 1 : item.getImpact())
                * (item.getProbability() == null ? 1 : item.getProbability());

        dto.setRiskValue(rv);
        dto.setRiskLevel(mapRiskLevel(rv));
        dto.setVarianceStatus(item.getVarianceStatus());
        dto.setApprovedBy(item.getApprovedBy());
        dto.setApprovedAt(item.getApprovedAt());
        dto.setNotes(item.getNotes());
        return dto;
    }

    private String mapRiskLevel(int rv) {
        if (rv >= 17) return "crit";
        if (rv >= 10) return "hi";
        if (rv >= 5) return "med";
        return "low";
    }

    private Project getAccessibleProject(Long projectId) {
        if (securityUtils.isPlatformUser()) {
            return projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        }

        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) {
            throw new IllegalStateException("User has no organisation");
        }

        return projectRepository.findByIdAndOrganisationId(projectId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Project not accessible"));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "system";
        }
        return authentication.getName();
    }
}