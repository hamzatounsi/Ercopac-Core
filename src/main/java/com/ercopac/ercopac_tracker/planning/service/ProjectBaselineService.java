package com.ercopac.ercopac_tracker.planning.service;

import com.ercopac.ercopac_tracker.planning.domain.ProjectBaseline;
import com.ercopac.ercopac_tracker.planning.dto.CreateProjectBaselineRequest;
import com.ercopac.ercopac_tracker.planning.dto.ProjectBaselineDto;
import com.ercopac.ercopac_tracker.planning.repository.ProjectBaselineRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectBaselineService {

    private final ProjectBaselineRepository baselineRepository;
    private final SecurityUtils securityUtils;

    public ProjectBaselineService(
        ProjectBaselineRepository baselineRepository,
        SecurityUtils securityUtils
    ) {
        this.baselineRepository = baselineRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<ProjectBaselineDto> getProjectBaselines(Long projectId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        return baselineRepository
            .findByProjectIdAndOrganisationIdOrderByCreatedAtDesc(projectId, organisationId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    public ProjectBaselineDto createBaseline(Long projectId, CreateProjectBaselineRequest request) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        ProjectBaseline baseline = new ProjectBaseline();
        baseline.setOrganisationId(organisationId);
        baseline.setProjectId(projectId);
        baseline.setName(request.getName().trim());
        baseline.setSnapshotJson(request.getSnapshotJson());

        return toDto(baselineRepository.save(baseline));
    }

    public void deleteBaseline(Long projectId, Long baselineId) {
        Long organisationId = securityUtils.getCurrentOrganisationId();

        ProjectBaseline baseline = baselineRepository
            .findByIdAndProjectIdAndOrganisationId(baselineId, projectId, organisationId)
            .orElseThrow(() -> new EntityNotFoundException("Baseline not found"));

        baselineRepository.delete(baseline);
    }

    private ProjectBaselineDto toDto(ProjectBaseline baseline) {
        return new ProjectBaselineDto(
            baseline.getId(),
            baseline.getProjectId(),
            baseline.getName(),
            baseline.getCreatedAt(),
            baseline.getSnapshotJson()
        );
    }
    
}