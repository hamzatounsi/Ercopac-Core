package com.ercopac.ercopac_tracker.employee_workspace.web;

import com.ercopac.ercopac_tracker.employee_workspace.dto.EmployeeProjectDto;
import com.ercopac.ercopac_tracker.employee_workspace.dto.UpdateEmployeeActionStatusRequest;
import com.ercopac.ercopac_tracker.employee_workspace.service.EmployeeProjectService;
import com.ercopac.ercopac_tracker.projectum.actions.dto.ActionAttachmentDto;
import com.ercopac.ercopac_tracker.projectum.actions.dto.ActionItemDto;
import com.ercopac.ercopac_tracker.projectum.actions.dto.ActionCommentDto;
import com.ercopac.ercopac_tracker.projectum.actions.service.ActionService;
import com.ercopac.ercopac_tracker.tasks.dto.ProjectTaskHistoryDto;
import com.ercopac.ercopac_tracker.tasks.service.ProjectTaskHistoryService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee")
@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ROLE_EMPLOYEE')")
public class EmployeeWorkspaceController {
    private final ActionService actionService;
    private final EmployeeProjectService employeeProjectService;
    private final ProjectTaskHistoryService taskHistoryService;

    public EmployeeWorkspaceController(ActionService actionService, EmployeeProjectService employeeProjectService,
                                       ProjectTaskHistoryService taskHistoryService) {
        this.actionService = actionService;
        this.employeeProjectService = employeeProjectService;
        this.taskHistoryService = taskHistoryService;
    }

    @GetMapping("/actions")
    public List<ActionItemDto> getMyActions() {
        return actionService.getMyActions();
    }

    @PostMapping("/actions/{actionId}/comments")
    public ActionCommentDto addMyActionComment(@PathVariable Long actionId,
                                               @RequestBody Map<String, String> request) {
        return actionService.addMyActionComment(actionId, request.get("text"));
    }

    @GetMapping("/projects/{projectId}/tasks/{taskId}/comments")
    public List<ProjectTaskHistoryDto> getMyTaskComments(@PathVariable Long projectId, @PathVariable Long taskId) {
        return taskHistoryService.getMyTaskComments(projectId, taskId);
    }

    @PostMapping("/projects/{projectId}/tasks/{taskId}/comments")
    public ProjectTaskHistoryDto addMyTaskComment(@PathVariable Long projectId, @PathVariable Long taskId,
                                                  @RequestBody Map<String, String> request) {
        return taskHistoryService.addMyTaskComment(projectId, taskId, request.get("text"));
    }

    @PostMapping(value = "/actions/{actionId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ActionAttachmentDto> uploadMyActionAttachments(@PathVariable Long actionId,
                                                              @RequestParam("files") List<MultipartFile> files) {
        return actionService.uploadMyActionAttachments(actionId, files);
    }

    @GetMapping("/actions/{actionId}/attachments/{attachmentId}/download")
    public ResponseEntity<ByteArrayResource> downloadMyActionAttachment(@PathVariable Long actionId,
                                                                        @PathVariable Long attachmentId) {
        ActionService.DownloadedActionAttachment file = actionService.downloadMyActionAttachment(actionId, attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(new ByteArrayResource(file.data()));
    }

    @DeleteMapping("/actions/{actionId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteMyActionAttachment(@PathVariable Long actionId, @PathVariable Long attachmentId) {
        actionService.deleteMyActionAttachment(actionId, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/actions/{actionId}/status")
    public ResponseEntity<ActionItemDto> updateMyActionStatus(
            @PathVariable Long actionId,
            @Valid @RequestBody UpdateEmployeeActionStatusRequest request) {
        return ResponseEntity.ok(actionService.updateMyActionStatus(actionId, request.status()));
    }

    @GetMapping("/projects")
    public List<EmployeeProjectDto> getMyProjects() {
        return employeeProjectService.getMyProjects();
    }
}
