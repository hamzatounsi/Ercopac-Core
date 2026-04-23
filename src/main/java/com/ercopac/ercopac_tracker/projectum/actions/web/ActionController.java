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

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<List<ActionItemDto>> getProjectActions(@PathVariable Long projectId) {
        return ResponseEntity.ok(actionService.getProjectActions(projectId));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<ActionSummaryDto> getSummary(@PathVariable Long projectId) {
        return ResponseEntity.ok(actionService.getSummary(projectId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<ActionItemDto> create(@PathVariable Long projectId,
                                                @Valid @RequestBody UpsertActionItemRequest request) {
        return ResponseEntity.ok(actionService.create(projectId, request));
    }

    @PutMapping("/{actionId}")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<ActionItemDto> update(@PathVariable Long projectId,
                                                @PathVariable Long actionId,
                                                @Valid @RequestBody UpsertActionItemRequest request) {
        return ResponseEntity.ok(actionService.update(projectId, actionId, request));
    }

    @DeleteMapping("/{actionId}")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable Long actionId) {
        actionService.delete(projectId, actionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{actionId}/comments")
    @PreAuthorize("hasAnyRole('GENERAL_MANAGER','ORG_ADMIN','PLATFORM_OWNER','PLATFORM_ADMIN')")
    public ResponseEntity<ActionCommentDto> addComment(@PathVariable Long projectId,
                                                       @PathVariable Long actionId,
                                                       @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(actionService.addComment(projectId, actionId, payload.get("text")));
    }
}