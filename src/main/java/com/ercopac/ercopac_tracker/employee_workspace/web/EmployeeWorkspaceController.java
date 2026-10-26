package com.ercopac.ercopac_tracker.employee_workspace.web;

import com.ercopac.ercopac_tracker.employee_workspace.dto.EmployeeProjectDto;
import com.ercopac.ercopac_tracker.employee_workspace.dto.UpdateEmployeeActionStatusRequest;
import com.ercopac.ercopac_tracker.employee_workspace.service.EmployeeProjectService;
import com.ercopac.ercopac_tracker.projectum.actions.dto.ActionAttachmentDto;
import com.ercopac.ercopac_tracker.projectum.actions.dto.ActionItemDto;
import com.ercopac.ercopac_tracker.projectum.actions.service.ActionService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ROLE_EMPLOYEE')")
public class EmployeeWorkspaceController {
    private final ActionService actionService;
    private final EmployeeProjectService employeeProjectService;

    public EmployeeWorkspaceController(ActionService actionService, EmployeeProjectService employeeProjectService) {
        this.actionService = actionService;
        this.employeeProjectService = employeeProjectService;
    }

    @GetMapping("/actions")
    public List<ActionItemDto> getMyActions() {
        return actionService.getMyActions();
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
