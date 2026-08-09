package com.ercopac.ercopac_tracker.projects.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjectProgressService {
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;

    public ProjectProgressService(ProjectRepository projectRepository, ProjectTaskRepository projectTaskRepository) {
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
    }

    public int calculate(Long projectId) {
        return (int) Math.round(projectTaskRepository.findByProjectId(projectId).stream()
                .filter(task -> Boolean.TRUE.equals(task.getActive()))
                .filter(task -> !"SUMMARY".equalsIgnoreCase(task.getTaskType()))
                .mapToInt(task -> task.getPercentComplete() == null ? 0 : task.getPercentComplete())
                .average().orElse(0));
    }

    public void recalculate(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        project.setProgress(calculate(projectId));
        projectRepository.save(project);
    }
}
