package com.ercopac.ercopac_tracker.projectum.actions.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.projectum.actions.domain.*;
import com.ercopac.ercopac_tracker.projectum.actions.dto.*;
import com.ercopac.ercopac_tracker.projectum.actions.repository.ActionItemRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.user.UserRepository;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ActionService {

    private final ActionItemRepository actionItemRepository;
    private final ProjectRepository projectRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    public ActionService(ActionItemRepository actionItemRepository,
                         ProjectRepository projectRepository,
                         SecurityUtils securityUtils,
                        UserRepository userRepository) {
        this.actionItemRepository = actionItemRepository;
        this.projectRepository = projectRepository;
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
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
    // ✅ NOUVEAU : Récupérer les actions de l'utilisateur connecté
    @Transactional(readOnly = true)
    public List<ActionItemDto> getMyActions() {
        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) {
            throw new IllegalStateException("User has no organisation");
        }
        
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        String fullName = userRepository.findByIdAndOrganisation_Id(userId, orgId)
                .map(user -> user.getFullName())
                .orElse("");
        List<ActionItem> actions = actionItemRepository.findMyActions(userId, fullName, orgId);
        
        return actions.stream().map(item -> {
            ActionItemDto dto = toDto(item);
            if (item.getProject() != null) {
                dto.setProjectId(item.getProject().getId());
                dto.setProjectCode(item.getProject().getCode());
                dto.setProjectName(item.getProject().getName());
            }
            return dto;
        }).toList();
    }

    /** Allows an employee to move only an action that is assigned to them. */
    public ActionItemDto updateMyActionStatus(Long actionId, String status) {
        if (status == null || !List.of("todo", "doing", "review", "blocked", "done").contains(status)) {
            throw new IllegalArgumentException("Unsupported action status");
        }

        Long organisationId = securityUtils.getCurrentOrganisationId();
        Long userId = securityUtils.getCurrentUserId();
        String fullName = userRepository.findByIdAndOrganisation_Id(userId, organisationId)
                .map(user -> user.getFullName())
                .orElse("");

        ActionItem item = actionItemRepository.findAssignedToUser(actionId, userId, fullName, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Action not assigned to current employee"));
        item.setStatus(status);
        ActionItemDto dto = toDto(actionItemRepository.save(item));
        dto.setProjectId(item.getProject().getId());
        dto.setProjectCode(item.getProject().getCode());
        dto.setProjectName(item.getProject().getName());
        return dto;
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
                userRepository.findByOrganisation_IdAndFullNameIgnoreCase(
                                item.getOrganisation().getId(), name.trim())
                        .ifPresent(assignee::setAssigneeUser);
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

    @Transactional(readOnly = true)
    public List<String> getAvailableAssignees(Long projectId) {
        Project project = getAccessibleProject(projectId);

        if (securityUtils.isPlatformUser()) {
            return userRepository.findAll()
                    .stream()
                    .map(user -> user.getFullName())
                    .filter(name -> name != null && !name.isBlank())
                    .distinct()
                    .sorted()
                    .toList();
        }

        Long organisationId = project.getOrganisation().getId();

        return userRepository.findByOrganisation_IdOrderByFullNameAsc(organisationId)
                .stream()
                .map(user -> user.getFullName())
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public record DownloadedActionAttachment(
        String fileName,
        String contentType,
        byte[] data
) {}

public List<ActionAttachmentDto> uploadAttachments(
        Long projectId,
        Long actionId,
        List<MultipartFile> files
) {
    if (files == null || files.isEmpty()) {
        throw new IllegalArgumentException("No files selected");
    }

    ActionItem item = getAccessibleAction(projectId, actionId);

    for (MultipartFile file : files) {
        if (file.isEmpty()) continue;

        try {
            ActionAttachment attachment = new ActionAttachment();
            attachment.setActionItem(item);
            attachment.setFileName(file.getOriginalFilename());
            attachment.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setData(file.getBytes());

            item.getAttachments().add(attachment);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read uploaded file: " + file.getOriginalFilename(), e);
        }
    }

    ActionItem saved = actionItemRepository.save(item);

    return saved.getAttachments()
            .stream()
            .map(this::toAttachmentDto)
            .toList();
}

@Transactional(readOnly = true)
public DownloadedActionAttachment downloadAttachment(
        Long projectId,
        Long actionId,
        Long attachmentId
) {
    ActionItem item = getAccessibleAction(projectId, actionId);

    ActionAttachment attachment = item.getAttachments()
            .stream()
            .filter(a -> a.getId().equals(attachmentId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));

    return new DownloadedActionAttachment(
            attachment.getFileName(),
            attachment.getContentType() == null ? "application/octet-stream" : attachment.getContentType(),
            attachment.getData()
    );
}

public void deleteAttachment(Long projectId, Long actionId, Long attachmentId) {
    ActionItem item = getAccessibleAction(projectId, actionId);

    boolean removed = item.getAttachments()
            .removeIf(a -> a.getId().equals(attachmentId));

    if (!removed) {
        throw new IllegalArgumentException("Attachment not found");
    }

    actionItemRepository.save(item);
}

private ActionItem getAccessibleAction(Long projectId, Long actionId) {
    Project project = getAccessibleProject(projectId);

    if (securityUtils.isPlatformUser()) {
        return actionItemRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Action not found"));
    }

    return actionItemRepository.findByIdAndProjectIdAndOrganisationId(
            actionId,
            projectId,
            project.getOrganisation().getId()
    ).orElseThrow(() -> new IllegalArgumentException("Action not found"));
}
}
