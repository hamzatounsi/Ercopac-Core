package com.ercopac.ercopac_tracker.projectum.actions.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.projectum.actions.domain.*;
import com.ercopac.ercopac_tracker.projectum.actions.dto.*;
import com.ercopac.ercopac_tracker.projectum.actions.repository.ActionItemRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ActionService {

    private final ActionItemRepository actionItemRepository;
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;

    public ActionService(ActionItemRepository actionItemRepository,
                         ProjectRepository projectRepository,
                         SecurityUtils securityUtils) {
        this.actionItemRepository = actionItemRepository;
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ActionItemDto> getProjectActions(Long projectId) {
        Project project = getAccessibleProject(projectId);

        List<ActionItem> rows = securityUtils.isPlatformUser()
                ? actionItemRepository.findAllByProjectIdOrderByIdAsc(projectId)
                : actionItemRepository.findAllByProjectIdAndOrganisationIdOrderByIdAsc(
                    projectId, project.getOrganisation().getId());

        return rows.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ActionSummaryDto getSummary(Long projectId) {
        List<ActionItemDto> rows = getProjectActions(projectId);

        ActionSummaryDto dto = new ActionSummaryDto();
        dto.setTotal(rows.size());

        LocalDate today = LocalDate.now();

        for (ActionItemDto row : rows) {
            switch (row.getStatus()) {
                case "todo" -> dto.setTodo(dto.getTodo() + 1);
                case "doing" -> dto.setDoing(dto.getDoing() + 1);
                case "review" -> dto.setReview(dto.getReview() + 1);
                case "blocked" -> dto.setBlocked(dto.getBlocked() + 1);
                case "done" -> dto.setDone(dto.getDone() + 1);
            }

            if (Boolean.TRUE.equals(row.getCustomerVisible())) dto.setCustomerVisible(dto.getCustomerVisible() + 1);
            else dto.setInternalOnly(dto.getInternalOnly() + 1);

            if (row.getDueDate() != null && row.getDueDate().isBefore(today) && !"done".equals(row.getStatus())) {
                dto.setOverdue(dto.getOverdue() + 1);
            }
        }

        return dto;
    }

    public ActionItemDto create(Long projectId, UpsertActionItemRequest request) {
        Project project = getAccessibleProject(projectId);

        ActionItem item = new ActionItem();
        item.setProject(project);
        item.setOrganisation(project.getOrganisation());
        apply(item, request);

        return toDto(actionItemRepository.save(item));
    }

    public ActionItemDto update(Long projectId, Long actionId, UpsertActionItemRequest request) {
        Project project = getAccessibleProject(projectId);

        ActionItem item = securityUtils.isPlatformUser()
                ? actionItemRepository.findById(actionId).orElseThrow(() -> new IllegalArgumentException("Action not found"))
                : actionItemRepository.findByIdAndProjectIdAndOrganisationId(
                    actionId, projectId, project.getOrganisation().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Action not found"));

        apply(item, request);
        return toDto(actionItemRepository.save(item));
    }

    public void delete(Long projectId, Long actionId) {
        Project project = getAccessibleProject(projectId);

        ActionItem item = securityUtils.isPlatformUser()
                ? actionItemRepository.findById(actionId).orElseThrow(() -> new IllegalArgumentException("Action not found"))
                : actionItemRepository.findByIdAndProjectIdAndOrganisationId(
                    actionId, projectId, project.getOrganisation().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Action not found"));

        actionItemRepository.delete(item);
    }

    public ActionCommentDto addComment(Long projectId, Long actionId, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment text is required");
        }

        Project project = getAccessibleProject(projectId);

        ActionItem item = securityUtils.isPlatformUser()
                ? actionItemRepository.findById(actionId).orElseThrow(() -> new IllegalArgumentException("Action not found"))
                : actionItemRepository.findByIdAndProjectIdAndOrganisationId(
                    actionId, projectId, project.getOrganisation().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Action not found"));

        ActionComment comment = new ActionComment();
        comment.setActionItem(item);
        comment.setAuthor(currentActor());
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());

        item.getComments().add(comment);
        actionItemRepository.save(item);

        return toCommentDto(comment);
    }

    private void apply(ActionItem item, UpsertActionItemRequest request) {
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setActionType(request.getActionType());
        item.setDepartmentCode(request.getDepartmentCode());
        item.setPriority(request.getPriority());
        item.setStatus(request.getStatus());
        item.setCustomerVisible(Boolean.TRUE.equals(request.getCustomerVisible()));
        item.setInsertedDate(request.getInsertedDate() == null ? LocalDate.now() : request.getInsertedDate());
        item.setDueDate(request.getDueDate());

        item.getAssignees().clear();
        if (request.getAssignees() != null) {
            for (String name : request.getAssignees()) {
                if (name == null || name.isBlank()) continue;
                ActionAssignee assignee = new ActionAssignee();
                assignee.setActionItem(item);
                assignee.setAssigneeName(name.trim());
                item.getAssignees().add(assignee);
            }
        }
    }

    private ActionItemDto toDto(ActionItem item) {
        ActionItemDto dto = new ActionItemDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setActionType(item.getActionType());
        dto.setDepartmentCode(item.getDepartmentCode());
        dto.setPriority(item.getPriority());
        dto.setStatus(item.getStatus());
        dto.setCustomerVisible(item.getCustomerVisible());
        dto.setInsertedDate(item.getInsertedDate());
        dto.setDueDate(item.getDueDate());
        dto.setAssignees(item.getAssignees().stream().map(ActionAssignee::getAssigneeName).toList());
        dto.setComments(item.getComments().stream().map(this::toCommentDto).toList());
        dto.setAttachments(item.getAttachments().stream().map(this::toAttachmentDto).toList());
        return dto;
    }

    private ActionCommentDto toCommentDto(ActionComment comment) {
        ActionCommentDto dto = new ActionCommentDto();
        dto.setId(comment.getId());
        dto.setAuthor(comment.getAuthor());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

    private ActionAttachmentDto toAttachmentDto(ActionAttachment attachment) {
        ActionAttachmentDto dto = new ActionAttachmentDto();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setContentType(attachment.getContentType());
        dto.setFileSize(attachment.getFileSize());
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

    private String currentActor() {
        String username = securityUtils.getCurrentUsername();
        return username == null || username.isBlank() ? "User" : username;
    }
}