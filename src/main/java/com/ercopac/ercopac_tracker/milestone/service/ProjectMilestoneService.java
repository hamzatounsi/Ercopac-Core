package com.ercopac.ercopac_tracker.milestone.service;

import com.ercopac.ercopac_tracker.milestone.domain.ProjectMilestone;
import com.ercopac.ercopac_tracker.milestone.dto.ProjectMilestoneDto;
import com.ercopac.ercopac_tracker.milestone.repository.ProjectMilestoneRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectMilestoneService {

    private final ProjectMilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;
    private final SecurityUtils securityUtils;

    public ProjectMilestoneService(ProjectMilestoneRepository milestoneRepository,
                                   ProjectRepository projectRepository,
                                   ProjectTaskRepository taskRepository,
                                   SecurityUtils securityUtils) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ProjectMilestoneDto> getMilestonesByProject(Long projectId) {
        Long orgId = securityUtils.getCurrentOrganisationId();
        List<ProjectMilestone> milestones;
        
        if (securityUtils.isPlatformUser()) {
            milestones = milestoneRepository.findByProjectIdOrderByMilestoneDateAsc(projectId);
        } else {
            milestones = milestoneRepository.findByProjectIdAndOrganisationIdOrderByMilestoneDateAsc(projectId, orgId);
        }
        
        return milestones.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectMilestoneDto> getMilestonesForPMCalendar(Long pmId, LocalDate startDate, LocalDate endDate) {
        Long orgId = securityUtils.getCurrentOrganisationId();
        List<ProjectMilestone> milestones;
        
        if (securityUtils.isPlatformUser()) {
            milestones = milestoneRepository.findByProjectManagerIdAndDateRange(pmId, startDate, endDate);
        } else {
            milestones = milestoneRepository.findByOrganisationIdAndDateRange(orgId, startDate, endDate);
        }
        
        return milestones.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectMilestoneDto> getMilestonesByDateRange(List<Long> projectIds, LocalDate startDate, LocalDate endDate) {
        // ✅ Call the new bulletproof method
        List<ProjectTask> milestoneTasks = taskRepository.findMilestoneTasksByDateRange(projectIds, startDate, endDate);
        
        System.out.println("DEBUG: Found " + milestoneTasks.size() + " milestone tasks for projects " + projectIds);
        
        return milestoneTasks.stream().map(this::mapTaskToMilestoneDto).collect(Collectors.toList());
    }

    // ✅ ADD THIS NEW HELPER METHOD:
    private ProjectMilestoneDto mapTaskToMilestoneDto(ProjectTask task) {
        ProjectMilestoneDto dto = new ProjectMilestoneDto();
        dto.setId(task.getId());
        dto.setProjectId(task.getProjectId());
        dto.setTaskId(task.getId());
        dto.setMilestoneTypeId(task.getMilestoneTypeId());
        
        // Use baselineStart, fallback to plannedStart for the date
        dto.setMilestoneDate(task.getBaselineStart() != null ? task.getBaselineStart() : task.getPlannedStart());
        
        // Enrich with Project Info
        if (task.getProject() != null) {
            dto.setProjectCode(task.getProject().getCode());
            dto.setProjectName(task.getProject().getName());
        }
        
        // Enrich with Milestone Type info (This is where the COLOR and LETTER come from!)
        if (task.getMilestoneType() != null) {
            dto.setMilestoneTypeCode(task.getMilestoneType().getCode());
            dto.setMilestoneTypeLabel(task.getMilestoneType().getLabel());
            dto.setMilestoneTypeColor(task.getMilestoneType().getColor());
            dto.setMilestoneTypeLetterCode(task.getMilestoneType().getLetterCode());
        }
        
        return dto;
    }

    public ProjectMilestoneDto createMilestone(ProjectMilestoneDto dto) {
        Project project = projectRepository.findById(dto.getProjectId())
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        ProjectMilestone milestone = new ProjectMilestone();
        milestone.setProjectId(dto.getProjectId());
        milestone.setTaskId(dto.getTaskId());
        milestone.setMilestoneTypeId(dto.getMilestoneTypeId());
        milestone.setMilestoneDate(dto.getMilestoneDate());
        milestone.setStatus(dto.getStatus() != null ? dto.getStatus() : "A");
        milestone.setPmCode(dto.getPmCode());
        milestone.setNotes(dto.getNotes());

        return toDto(milestoneRepository.save(milestone));
    }

    public ProjectMilestoneDto updateMilestone(Long id, ProjectMilestoneDto dto) {
        ProjectMilestone milestone = milestoneRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));

        milestone.setMilestoneTypeId(dto.getMilestoneTypeId());
        milestone.setMilestoneDate(dto.getMilestoneDate());
        milestone.setStatus(dto.getStatus());
        milestone.setPmCode(dto.getPmCode());
        milestone.setNotes(dto.getNotes());

        return toDto(milestoneRepository.save(milestone));
    }

    public void deleteMilestone(Long id) {
        milestoneRepository.deleteById(id);
    }

    private ProjectMilestoneDto toDto(ProjectMilestone milestone) {
        ProjectMilestoneDto dto = new ProjectMilestoneDto();
        dto.setId(milestone.getId());
        dto.setProjectId(milestone.getProjectId());
        dto.setTaskId(milestone.getTaskId());
        dto.setMilestoneTypeId(milestone.getMilestoneTypeId());
        dto.setMilestoneDate(milestone.getMilestoneDate());
        dto.setStatus(milestone.getStatus());
        dto.setPmCode(milestone.getPmCode());
        dto.setNotes(milestone.getNotes());

        if (milestone.getProject() != null) {
            dto.setProjectCode(milestone.getProject().getCode());
            dto.setProjectName(milestone.getProject().getName());
        }

        if (milestone.getTask() != null) {
            dto.setTaskWbsCode(milestone.getTask().getWbsCode());
            dto.setTaskName(milestone.getTask().getName());
        }

        if (milestone.getMilestoneType() != null) {
            dto.setMilestoneTypeCode(milestone.getMilestoneType().getCode());
            dto.setMilestoneTypeLabel(milestone.getMilestoneType().getLabel());
            dto.setMilestoneTypeColor(milestone.getMilestoneType().getColor());
            dto.setMilestoneTypeLetterCode(milestone.getMilestoneType().getLetterCode());
        }

        return dto;
    }
}