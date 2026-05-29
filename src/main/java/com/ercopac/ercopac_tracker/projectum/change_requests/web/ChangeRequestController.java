package com.ercopac.ercopac_tracker.projectum.change_requests.web;

import com.ercopac.ercopac_tracker.projectum.change_requests.dto.ChangeRequestDto;
import com.ercopac.ercopac_tracker.projectum.change_requests.dto.ChangeRequestSummaryDto;
import com.ercopac.ercopac_tracker.projectum.change_requests.dto.UpsertChangeRequestRequest;
import com.ercopac.ercopac_tracker.projectum.change_requests.service.ChangeRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ercopac.ercopac_tracker.projectum.change_requests.dto.ChangeRequestAttachmentDto;
import com.ercopac.ercopac_tracker.projectum.change_requests.dto.ChangeRequestAttachmentDownload;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
@RestController
@RequestMapping("/api/projects/{projectId}/change-requests")
public class ChangeRequestController {

    private final ChangeRequestService changeRequestService;

    private static final String CR_READ =
        "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FORECAST)";

    private static final String CR_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).FORECAST)";

    public ChangeRequestController(ChangeRequestService changeRequestService) {
        this.changeRequestService = changeRequestService;
    }

    @GetMapping
    @PreAuthorize(CR_READ)
    public ResponseEntity<List<ChangeRequestDto>> getProjectChangeRequests(@PathVariable Long projectId) {
        return ResponseEntity.ok(changeRequestService.getProjectChangeRequests(projectId));
    }

    @GetMapping("/summary")
    @PreAuthorize(CR_READ)
    public ResponseEntity<ChangeRequestSummaryDto> getSummary(@PathVariable Long projectId) {
        return ResponseEntity.ok(changeRequestService.getSummary(projectId));
    }

    @PostMapping
    @PreAuthorize(CR_WRITE)
    public ResponseEntity<ChangeRequestDto> create(@PathVariable Long projectId,
                                                   @Valid @RequestBody UpsertChangeRequestRequest request) {
        return ResponseEntity.ok(changeRequestService.create(projectId, request));
    }

    @PutMapping("/{crId}")
    @PreAuthorize(CR_WRITE)
    public ResponseEntity<ChangeRequestDto> update(@PathVariable Long projectId,
                                                   @PathVariable Long crId,
                                                   @Valid @RequestBody UpsertChangeRequestRequest request) {
        return ResponseEntity.ok(changeRequestService.update(projectId, crId, request));
    }

    @DeleteMapping("/{crId}")
    @PreAuthorize(CR_WRITE)
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable Long crId) {
        changeRequestService.delete(projectId, crId);
        return ResponseEntity.noContent().build();
    }

@PostMapping("/{crId}/attachments")
@PreAuthorize(CR_WRITE)
public ResponseEntity<List<ChangeRequestAttachmentDto>> uploadAttachments(
    @PathVariable Long projectId,
    @PathVariable Long crId,
    @RequestParam("files") List<MultipartFile> files) {
  return ResponseEntity.ok(changeRequestService.uploadAttachments(projectId, crId, files));
}

@GetMapping("/{crId}/attachments/{attachmentId}/download")
@PreAuthorize(CR_READ)
public ResponseEntity<Resource> downloadAttachment(
    @PathVariable Long projectId,
    @PathVariable Long crId,
    @PathVariable Long attachmentId) {
  var result = changeRequestService.downloadAttachment(projectId, crId, attachmentId);
  return ResponseEntity.ok()
    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.getFileName() + "\"")
    .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .body(result.getResource());
}

@DeleteMapping("/{crId}/attachments/{attachmentId}")
@PreAuthorize(CR_WRITE)
public ResponseEntity<Void> deleteAttachment(
    @PathVariable Long projectId,
    @PathVariable Long crId,
    @PathVariable Long attachmentId) {
  changeRequestService.deleteAttachment(projectId, crId, attachmentId);
  return ResponseEntity.noContent().build();
}
}