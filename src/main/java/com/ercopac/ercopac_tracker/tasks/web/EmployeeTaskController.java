package com.ercopac.ercopac_tracker.tasks.web;

import com.ercopac.ercopac_tracker.tasks.dto.ProjectScheduleTaskResponse;
import com.ercopac.ercopac_tracker.tasks.service.ProjectTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employee/tasks")
public class EmployeeTaskController {

    private final ProjectTaskService projectTaskService;

    public EmployeeTaskController(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE','ROLE_EMPLOYEE')")
    public ResponseEntity<List<ProjectScheduleTaskResponse>> getMyTasks() {
        return ResponseEntity.ok(projectTaskService.getMyAssignedTasks());
    }
}
