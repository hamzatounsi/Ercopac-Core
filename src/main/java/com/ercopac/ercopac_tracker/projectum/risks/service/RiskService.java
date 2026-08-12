package com.ercopac.ercopac_tracker.projectum.risks.service;

import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskItem;
import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskExposureItemDto;
import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskItemDto;
import com.ercopac.ercopac_tracker.projectum.risks.dto.RiskSummaryDto;
import com.ercopac.ercopac_tracker.projectum.risks.dto.UpsertRiskItemRequest;
import com.ercopac.ercopac_tracker.projectum.risks.repository.RiskItemRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.dto.ResourceUserDto;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.ResourceType;
import com.ercopac.ercopac_tracker.user.ResourceTypeRepository;
import com.ercopac.ercopac_tracker.user.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.ercopac.ercopac_tracker.user.ResourceTypeDto;

import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;


@Service
@Transactional
public class RiskService {

    private final RiskItemRepository riskItemRepository;
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final ProjectTaskRepository projectTaskRepository;
    public RiskService(RiskItemRepository riskItemRepository,
                       ProjectRepository projectRepository,
                       SecurityUtils securityUtils,UserRepository userRepository,
                       ResourceTypeRepository resourceTypeRepository,ProjectTaskRepository projectTaskRepository) {
        this.riskItemRepository = riskItemRepository;
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.projectTaskRepository = projectTaskRepository;
    }

    @Transactional(readOnly = true)
    public List<RiskItemDto> getProjectRisks(Long projectId) {
        Project project = getAccessibleProject(projectId);

        List<RiskItem> items = securityUtils.isPlatformUser()
                ? riskItemRepository.findAllByProjectIdOrderByIdAsc(projectId)
                : riskItemRepository.findAllByProjectIdAndOrganisation_IdOrderByIdAsc(
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

        List<RiskExposureItemDto> riskItems = new ArrayList<>();
        List<RiskExposureItemDto> oppItems = new ArrayList<>();

        for (RiskItemDto item : items) {
            int rv = item.getRiskValue() == null ? 0 : item.getRiskValue();

            if ("crit".equals(item.getRiskLevel()))      dto.setCritical(dto.getCritical() + 1);
            else if ("hi".equals(item.getRiskLevel()))   dto.setHigh(dto.getHigh() + 1);
            else if ("med".equals(item.getRiskLevel()))  dto.setMedium(dto.getMedium() + 1);
            else                                          dto.setLow(dto.getLow() + 1);

            if (!"closed".equalsIgnoreCase(item.getState())) {
                dto.setOpenRisks(dto.getOpenRisks() + 1);
            }

            if ("open".equalsIgnoreCase(item.getVarianceStatus())) {
                dto.setPendingVariance(dto.getPendingVariance() + 1);
            }

            RiskExposureItemDto exposureItem = new RiskExposureItemDto(
                item.getId(),
                item.getRiskCode(),
                truncate(item.getDescription(), 30),
                rv,
                item.getRiskLevel()
            );

            if ("opportunity".equalsIgnoreCase(item.getRiskType())) {
                dto.setOpportunityCount(dto.getOpportunityCount() + 1);
                oppExposure += rv;
                oppItems.add(exposureItem);
            } else {
                dto.setRiskCount(dto.getRiskCount() + 1);
                riskExposure += rv;
                riskItems.add(exposureItem);
            }
        }

        dto.setNetExposureScore(riskExposure - oppExposure);
        dto.setRiskExposureItems(riskItems);
        dto.setOpportunityItems(oppItems);

        return dto;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
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
    @Transactional(readOnly = true)
    public List<RiskItemDto> getAllPendingApprovalsForOrg() {
        Long orgId = securityUtils.getCurrentOrganisationId();

        if (orgId == null) {
            throw new IllegalStateException("User has no organisation");
        }

        return riskItemRepository
            .findAllByOrganisation_IdAndStateInAndVarianceStatusOrderByIdAsc(
                orgId,
                List.of("variance", "cr"),
                "open"
            )
            .stream()
            .map(this::toDto)
            .toList();
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
        Long organisationId = item.getProject() != null && item.getProject().getOrganisation() != null
                ? item.getProject().getOrganisation().getId()
                : null;
        if (organisationId == null) {
            throw new IllegalStateException("Risk project has no organisation");
        }

        item.setRiskType(normalizeRiskType(request.getRiskType()));
        item.setState(normalizeState(request.getState()));
        item.setDescription(request.getDescription());
        item.setInputDate(request.getInputDate());
        item.setDueDate(request.getDueDate());
        item.setMitigation(request.getMitigation());
        if (request.getResourceTypeId() != null) {
            ResourceType resourceType = resourceTypeRepository
                .findByIdAndOrganisation_Id(request.getResourceTypeId(), organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Resource type not found in project organisation"));
            item.setResourceType(resourceType);
        } else {
            item.setResourceType(null);
        }

     // OwnerUser → find by ID
     if (request.getOwnerUserId() != null) {
         AppUser ownerUser = userRepository
             .findByIdAndOrganisation_Id(request.getOwnerUserId(), organisationId)
             .orElseThrow(() -> new IllegalArgumentException("Risk owner not found in project organisation"));
         item.setOwnerUser(ownerUser);
     } else {
         item.setOwnerUser(null);
     }
        item.setWbsCode(request.getWbsCode());
        item.setImpact(request.getImpact() == null ? "1" : request.getImpact());
        item.setProbability(request.getProbability() == null ? 10: request.getProbability());
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
                : riskItemRepository.findByIdAndProjectIdAndOrganisation_Id(
                    itemId, projectId, project.getOrganisation().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Risk item not found"));
    }

    private RiskItemDto toDto(RiskItem item) {
        RiskItemDto dto = new RiskItemDto();

        dto.setId(item.getId());
        dto.setRiskCode("R-" + String.format("%03d", item.getId()));
        dto.setProjectId(item.getProject().getId());
        dto.setProjectCode(item.getProject().getCode());

        dto.setRiskType(item.getRiskType());
        dto.setState(item.getState());
        dto.setDescription(item.getDescription());
        dto.setInputDate(item.getInputDate());
        dto.setDueDate(item.getDueDate());
        dto.setMitigation(item.getMitigation());
        dto.setWbsCode(item.getWbsCode());
        dto.setImpact(item.getImpact());
        dto.setProbability(item.getProbability());

        // ResourceType FK
        if (item.getResourceType() != null) {
            ResourceType rt = item.getResourceType();
            dto.setResourceTypeId(rt.getId());
            dto.setResourceTypeCode(rt.getCode());
            dto.setResourceTypeLabel(rt.getLabel());
            dto.setResourceTypeColour(rt.getColour());
        }

        // OwnerUser FK
        if (item.getOwnerUser() != null) {
            AppUser owner = item.getOwnerUser();
            dto.setOwnerUserId(owner.getId());
            dto.setOwnerUserName(owner.getFullName());
            dto.setOwnerUserCode(owner.getEmployeeCode());
        }

     // CHANGE the RV calculation:
        int rv = calculateRiskValue(item.getImpact(), item.getProbability());
        dto.setRiskValue(rv);
        dto.setRiskLevel(mapRiskLevel(rv));
        dto.setVarianceStatus(item.getVarianceStatus());
        dto.setApprovedBy(item.getApprovedBy());
        dto.setApprovedAt(item.getApprovedAt());
        dto.setNotes(item.getNotes());

        return dto;
    }

    private int calculateRiskValue(String impact, Integer probability) {
        int impactScore;

        try {
            impactScore = Integer.parseInt(impact == null ? "1" : impact.trim());
        } catch (NumberFormatException e) {
            impactScore = 1;
        }

        impactScore = Math.max(1, Math.min(5, impactScore));

        int probabilityPercent = probability == null ? 10 : probability;
        probabilityPercent = Math.max(10, Math.min(100, probabilityPercent));

        int probabilityScore = (int) Math.ceil(probabilityPercent / 20.0);
        probabilityScore = Math.max(1, Math.min(5, probabilityScore));

        return impactScore * probabilityScore;
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
    public List<ResourceTypeDto> getResourceTypes(Long projectId) {
        Long orgId = securityUtils.getCurrentOrganisationId();
        return resourceTypeRepository.findByOrganisation_IdAndActiveTrue(orgId)
            .stream()
            .filter(ResourceType::isAssignable)
            .map(rt ->  new ResourceTypeDto(rt.getId(), rt.getCode(), rt.getLabel(), rt.getColour(), rt.getDefaultRate())) // ✅ NOUVEAU (5 paramètres)
            .toList();
    }

    public List<ResourceUserDto> getUsersByResourceType(Long projectId, Long resourceTypeId) {
        Long orgId = securityUtils.getCurrentOrganisationId();
        return userRepository
            .findByOrganisation_IdAndResourceType_IdAndActiveTrue(orgId, resourceTypeId)
            .stream()
            .map(u -> new ResourceUserDto(
                u.getId(),
                u.getFullName(),
                u.getResourceType() != null ? u.getResourceType().getCode() : null,
                u.getDepartmentCode(),
                u.getColor()        // ← ADD this 5th argument
            ))
            .toList();
    }

    public List<String> getWbsCodes(Long projectId) {
        return projectTaskRepository
            .findByProjectIdOrderByDisplayOrderAsc(projectId)
            .stream()
            .map(t -> t.getWbsCode())
            .filter(Objects::nonNull)
            .filter(s -> !s.isBlank())
            .distinct()
            .toList();
    }
    
}
