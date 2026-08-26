package com.ercopac.ercopac_tracker.employee_workspace.web;

import com.ercopac.ercopac_tracker.employee_workspace.dto.EmployeeProjectDto;
import com.ercopac.ercopac_tracker.employee_workspace.dto.UpdateEmployeeActionStatusRequest;
import com.ercopac.ercopac_tracker.employee_workspace.service.EmployeeProjectService;
import com.ercopac.ercopac_tracker.projectum.actions.dto.ActionItemDto;
import com.ercopac.ercopac_tracker.projectum.actions.service.ActionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
