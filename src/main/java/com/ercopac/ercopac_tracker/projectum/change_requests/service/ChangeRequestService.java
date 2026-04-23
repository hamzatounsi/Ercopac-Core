package com.ercopac.ercopac_tracker.projectum.change_requests.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.projectum.change_requests.domain.ChangeRequest;
import com.ercopac.ercopac_tracker.projectum.change_requests.domain.ChangeRequestAttachment;
import com.ercopac.ercopac_tracker.projectum.change_requests.domain.ChangeRequestHistoryEntry;
import com.ercopac.ercopac_tracker.projectum.change_requests.dto.*;
import com.ercopac.ercopac_tracker.projectum.change_requests.repository.ChangeRequestRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChangeRequestService {

    private final ChangeRequestRepository changeRequestRepository;
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;

    public ChangeRequestService(ChangeRequestRepository changeRequestRepository,
                                ProjectRepository projectRepository,
                                SecurityUtils securityUtils) {
        this.changeRequestRepository = changeRequestRepository;
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ChangeRequestDto> getProjectChangeRequests(Long projectId) {
        Project project = getAccessibleProject(projectId);

        List<ChangeRequest> rows = securityUtils.isPlatformUser()
                ? changeRequestRepository.findAllByProjectIdOrderByIdAsc(projectId)
                : changeRequestRepository.findAllByProjectIdAndOrganisationIdOrderByIdAsc(
                    projectId, project.getOrganisation().getId());

        return rows.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ChangeRequestSummaryDto getSummary(Long projectId) {
        List<ChangeRequestDto> rows = getProjectChangeRequests(projectId);

        long open = 0, submitted = 0, accepted = 0, refused = 0, cancelled = 0;
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (ChangeRequestDto row : rows) {
            String status = row.getStatus() == null ? "" : row.getStatus();
            switch (status) {
                case "open" -> open++;
                case "submitted" -> submitted++;
                case "accepted" -> accepted++;
                case "refused" -> refused++;
                case "cancelled" -> cancelled++;
            }

            totalValue = totalValue.add(nvl(row.getValueAmount()));
            totalCost = totalCost.add(nvl(row.getCostAmount()));
        }

        BigDecimal totalMargin = totalValue.subtract(totalCost);
        BigDecimal totalMarginPercent = totalValue.signum() == 0
                ? BigDecimal.ZERO
                : totalMargin.multiply(BigDecimal.valueOf(100)).divide(totalValue, 1, RoundingMode.HALF_UP);

        ChangeRequestSummaryDto dto = new ChangeRequestSummaryDto();
        dto.setTotalCount(rows.size());
        dto.setOpenCount(open);
        dto.setSubmittedCount(submitted);
        dto.setAcceptedCount(accepted);
        dto.setRefusedCount(refused);
        dto.setCancelledCount(cancelled);
        dto.setTotalValue(totalValue);
        dto.setTotalCost(totalCost);
        dto.setTotalMargin(totalMargin);
        dto.setTotalMarginPercent(totalMarginPercent);
        return dto;
    }

    public ChangeRequestDto create(Long projectId, UpsertChangeRequestRequest request) {
        Project project = getAccessibleProject(projectId);

        ChangeRequest cr = new ChangeRequest();
        cr.setProject(project);
        cr.setOrganisation(project.getOrganisation());
        apply(cr, request);
        addHistory(cr, "Created", currentActor());

        return toDto(changeRequestRepository.save(cr));
    }

    public ChangeRequestDto update(Long projectId, Long crId, UpsertChangeRequestRequest request) {
        Project project = getAccessibleProject(projectId);

        ChangeRequest cr = securityUtils.isPlatformUser()
                ? changeRequestRepository.findById(crId).orElseThrow(() -> new IllegalArgumentException("Change request not found"))
                : changeRequestRepository.findByIdAndProjectIdAndOrganisationId(
                    crId, projectId, project.getOrganisation().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Change request not found"));

        String previousStatus = cr.getStatus();
        apply(cr, request);

        if (previousStatus != null && !previousStatus.equals(request.getStatus())) {
            addHistory(cr, "Status → " + request.getStatus(), currentActor());
        }

        return toDto(changeRequestRepository.save(cr));
    }

    public void delete(Long projectId, Long crId) {
        Project project = getAccessibleProject(projectId);

        ChangeRequest cr = securityUtils.isPlatformUser()
                ? changeRequestRepository.findById(crId).orElseThrow(() -> new IllegalArgumentException("Change request not found"))
                : changeRequestRepository.findByIdAndProjectIdAndOrganisationId(
                    crId, projectId, project.getOrganisation().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Change request not found"));

        changeRequestRepository.delete(cr);
    }

    private void apply(ChangeRequest cr, UpsertChangeRequestRequest request) {
        cr.setTitle(request.getTitle());
        cr.setStatus(request.getStatus());
        cr.setRequestDate(request.getRequestDate());
        cr.setValueAmount(nvl(request.getValueAmount()));
        cr.setCostAmount(nvl(request.getCostAmount()));
        cr.setOwner(request.getOwner());
        cr.setNote(request.getNote());
    }

    private void addHistory(ChangeRequest cr, String action, String actor) {
        ChangeRequestHistoryEntry entry = new ChangeRequestHistoryEntry();
        entry.setChangeRequest(cr);
        entry.setAction(action);
        entry.setPerformedBy(actor);
        entry.setCreatedAt(LocalDateTime.now());
        cr.getHistory().add(entry);
    }

    private ChangeRequestDto toDto(ChangeRequest cr) {
        ChangeRequestDto dto = new ChangeRequestDto();
        dto.setId(cr.getId());
        dto.setCode("CR-" + String.format("%03d", cr.getId()));
        dto.setTitle(cr.getTitle());
        dto.setStatus(cr.getStatus());
        dto.setRequestDate(cr.getRequestDate());
        dto.setValueAmount(nvl(cr.getValueAmount()));
        dto.setCostAmount(nvl(cr.getCostAmount()));

        BigDecimal margin = nvl(cr.getValueAmount()).subtract(nvl(cr.getCostAmount()));
        dto.setMarginAmount(margin);

        BigDecimal marginPercent = nvl(cr.getValueAmount()).signum() == 0
                ? BigDecimal.ZERO
                : margin.multiply(BigDecimal.valueOf(100)).divide(nvl(cr.getValueAmount()), 1, RoundingMode.HALF_UP);
        dto.setMarginPercent(marginPercent);

        dto.setOwner(cr.getOwner());
        dto.setNote(cr.getNote());

        dto.setAttachments(
            cr.getAttachments().stream().map(this::toAttachmentDto).toList()
        );
        dto.setHistory(
            cr.getHistory().stream().map(this::toHistoryDto).toList()
        );

        return dto;
    }

    private ChangeRequestAttachmentDto toAttachmentDto(ChangeRequestAttachment a) {
        ChangeRequestAttachmentDto dto = new ChangeRequestAttachmentDto();
        dto.setId(a.getId());
        dto.setFileName(a.getFileName());
        dto.setContentType(a.getContentType());
        dto.setFileSize(a.getFileSize());
        return dto;
    }

    private ChangeRequestHistoryDto toHistoryDto(ChangeRequestHistoryEntry h) {
        ChangeRequestHistoryDto dto = new ChangeRequestHistoryDto();
        dto.setId(h.getId());
        dto.setAction(h.getAction());
        dto.setPerformedBy(h.getPerformedBy());
        dto.setCreatedAt(h.getCreatedAt());
        return dto;
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

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String currentActor() {
        String username = securityUtils.getCurrentUsername();
        return username == null || username.isBlank() ? "User" : username;
    }
}