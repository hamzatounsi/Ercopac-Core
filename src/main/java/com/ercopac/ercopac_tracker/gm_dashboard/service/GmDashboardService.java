package com.ercopac.ercopac_tracker.gm_dashboard.service;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectDashboardRowDto;
import com.ercopac.ercopac_tracker.kpi.domain.HealthStatus;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class GmDashboardService {

    private final ProjectRepository projectRepository;

    public GmDashboardService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectDashboardRowDto> getProjects() {
        LocalDate today = LocalDate.now();

        return projectRepository.findAll()
                .stream()
                .map(p -> new ProjectDashboardRowDto(
                        p.getId(),
                        p.getCode(),
                        p.getName(),
                        p.getPlannedStart(),
                        p.getPlannedEnd(),
                        computeTimeHealth(p.getPlannedEnd(), today)
                ))
                .toList();
    }

    private HealthStatus computeTimeHealth(LocalDate plannedEnd, LocalDate today) {
        if (plannedEnd == null) return HealthStatus.NA;
        if (today.isAfter(plannedEnd)) return HealthStatus.RED;
        if (!today.isAfter(plannedEnd.minusDays(7))) return HealthStatus.YELLOW;
        return HealthStatus.GREEN;
    }
}