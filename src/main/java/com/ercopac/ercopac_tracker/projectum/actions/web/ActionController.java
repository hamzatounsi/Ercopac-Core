package com.ercopac.ercopac_tracker.projectum.actions.web;

import com.ercopac.ercopac_tracker.projectum.actions.dto.ActionCommentDto;
import com.ercopac.ercopac_tracker.projectum.actions.dto.ActionItemDto;
import com.ercopac.ercopac_tracker.projectum.actions.dto.ActionSummaryDto;
import com.ercopac.ercopac_tracker.projectum.actions.dto.UpsertActionItemRequest;
import com.ercopac.ercopac_tracker.projectum.actions.service.ActionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/actions")
public class ActionController {

    private final ActionService actionService;

    private static final String ACTIONS_READ =
        "@permissionChecker.canRead(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).ACTIONS)";

    private static final String ACTIONS_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).ACTIONS)";

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @GetMapping
    @PreAuthorize(ACTIONS_READ)
    public ResponseEntity<List<ActionItemDto>> getProjectActions(@PathVariable Long projectId) {
        return ResponseEntity.ok(actionService.getProjectActions(projectId));
    }

    @GetMapping("/summary")
    @PreAuthorize(ACTIONS_READ)
    public ResponseEntity<ActionSummaryDto> getSummary(@PathVariable Long projectId) {
        return ResponseEntity.ok(actionService.getSummary(projectId));
    }

    @PostMapping
    @PreAuthorize(ACTIONS_WRITE)
    public ResponseEntity<ActionItemDto> create(@PathVariable Long projectId,
                                                @Valid @RequestBody UpsertActionItemRequest request) {
        return ResponseEntity.ok(actionService.create(projectId, request));
    }

    @PutMapping("/{actionId}")
    @PreAuthorize(ACTIONS_WRITE)
    public ResponseEntity<ActionItemDto> update(@PathVariable Long projectId,
                                                @PathVariable Long actionId,
                                                @Valid @RequestBody UpsertActionItemRequest request) {
        return ResponseEntity.ok(actionService.update(projectId, actionId, request));
    }

    @DeleteMapping("/{actionId}")
    @PreAuthorize(ACTIONS_WRITE)
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable Long actionId) {
        actionService.delete(projectId, actionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{actionId}/comments")
    @PreAuthorize(ACTIONS_WRITE)
    public ResponseEntity<ActionCommentDto> addComment(@PathVariable Long projectId,
                                                       @PathVariable Long actionId,
                                                       @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(actionService.addComment(projectId, actionId, payload.get("text")));
    }

    @GetMapping("/assignees")
    @PreAuthorize(ACTIONS_READ)
    public ResponseEntity<List<String>> getAvailableAssignees(@PathVariable Long projectId) {
        return ResponseEntity.ok(actionService.getAvailableAssignees(projectId));
    }
}