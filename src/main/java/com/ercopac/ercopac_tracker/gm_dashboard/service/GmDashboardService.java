package com.ercopac.ercopac_tracker.gm_dashboard.service;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectDashboardRowDto;
import com.ercopac.ercopac_tracker.kpi.domain.HealthStatus;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.domain.ProjectApplicationType;
import com.ercopac.ercopac_tracker.projects.service.ProjectAccessService;
import com.ercopac.ercopac_tracker.projects.service.ProjectProgressService;
import com.ercopac.ercopac_tracker.user.AppUser; // ✅ Importer AppUser
import com.ercopac.ercopac_tracker.user.UserRepository; // ✅ Importer UserRepository
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class GmDashboardService {

    private final ProjectAccessService projectAccessService;
    private final ProjectProgressService projectProgressService;
    private final UserRepository userRepository; // ✅ Ajouter ce champ

    public GmDashboardService(ProjectAccessService projectAccessService,
                              ProjectProgressService projectProgressService,
                              UserRepository userRepository) { // ✅ Ajouter au constructeur
        this.projectAccessService = projectAccessService;
        this.projectProgressService = projectProgressService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectDashboardRowDto> getProjects(String applicationType) {
        LocalDate today = LocalDate.now();
        ProjectApplicationType type = ProjectApplicationType.valueOf(applicationType.toUpperCase());

        return getAccessibleProjects(type)
                .stream()
                .map(p -> {
                    ProjectDashboardRowDto dto = new ProjectDashboardRowDto();
                    dto.setId(p.getId());
                    dto.setCode(p.getCode());
                    dto.setName(p.getName());
                    dto.setShortName(p.getShortName());
                    dto.setCustomer(p.getCustomer());
                    dto.setCustomerId(p.getCustomerId());
                    dto.setCategory(p.getCategory());
                    dto.setCountry(p.getCountry());
                    dto.setPortfolio(p.getPortfolio());
                    dto.setProjectType(p.getProjectType());
                    dto.setProjectPhase(p.getProjectPhase());
                    dto.setRiskLevel(p.getRiskLevel());
                    dto.setProjectManagerName(p.getProjectManagerName());
                    dto.setProgramManagerName(p.getProgramManagerName());
                    dto.setSalesManagerName(p.getSalesManagerName());
                    dto.setPlannedStart(p.getPlannedStart());
                    dto.setPlannedEnd(p.getPlannedEnd());
                    dto.setProjectBudget(p.getProjectBudget());
                    dto.setEstimatedCost(p.getEstimatedCost());
                    dto.setProgressPercent(projectProgressService.calculate(p.getId()));
                    dto.setArchived(p.getArchived());
                    dto.setTimeHealth(computeTimeHealth(p.getPlannedEnd(), today).name());
                    dto.setApplicationType(
                        p.getApplicationType() != null ? p.getApplicationType().name() : "PROJECTUM"
                    );
                    
                    // ✅ RÉCUPÉRER LE DÉPARTEMENT DU PM DEPUIS AppUser
                    if (p.getProjectManagerId() != null) {
                        Optional<AppUser> pmOpt = userRepository.findById(p.getProjectManagerId());
                        if (pmOpt.isPresent()) {
                            AppUser pm = pmOpt.get();
                            dto.setPmDepartmentCode(pm.getDepartmentCode()); // ← Utilise la méthode getDepartmentCode() de AppUser
                        }
                    }
                    
                    return dto;
                })
                .toList();
    }
    private List<Project> getAccessibleProjects(ProjectApplicationType applicationType) {
        return projectAccessService.getAccessibleProjects(applicationType);
    }

    private HealthStatus computeTimeHealth(LocalDate plannedEnd, LocalDate today) {
        if (plannedEnd == null) {
            return HealthStatus.NA;
        }

        if (today.isAfter(plannedEnd)) {
            return HealthStatus.RED;
        }

        if (!today.isBefore(plannedEnd.minusDays(7))) {
            return HealthStatus.YELLOW;
        }

        return HealthStatus.GREEN;
    }
}
