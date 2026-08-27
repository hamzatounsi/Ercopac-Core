package com.ercopac.ercopac_tracker.projects.service;

import com.ercopac.ercopac_tracker.gm_dashboard.dto.ProjectDashboardRowDto;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.planning.domain.ProjectPlanning;
import com.ercopac.ercopac_tracker.planning.repository.ProjectPlanningRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.domain.ProjectApplicationType;
import com.ercopac.ercopac_tracker.projects.dto.ProjectDetailsResponse;
import com.ercopac.ercopac_tracker.projects.dto.ProjectFormOptionsResponse;
import com.ercopac.ercopac_tracker.projects.dto.UpsertProjectRequest;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import com.ercopac.ercopac_tracker.admin.repository.ProjectCategoryRepository;
import com.ercopac.ercopac_tracker.admin.domain.ProjectCategory;
import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.admin.repository.CustomerRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectPlanningRepository projectPlanningRepository;
    private final SecurityUtils securityUtils;
    private final ProjectCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProjectAccessService projectAccessService;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectPlanningRepository projectPlanningRepository,
                          SecurityUtils securityUtils,
                          ProjectCategoryRepository categoryRepository,
                          UserRepository userRepository,
                          CustomerRepository customerRepository,
                          ProjectAccessService projectAccessService) {
        this.projectRepository = projectRepository;
        this.projectPlanningRepository = projectPlanningRepository;
        this.securityUtils = securityUtils;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.projectAccessService = projectAccessService;
    }

    public ProjectFormOptionsResponse getFormOptions() {
        Long organisationId = requireCurrentOrganisationId();
        return new ProjectFormOptionsResponse(
                categoryRepository.findByOrganisation_IdOrderByNameAsc(organisationId).stream()
                        .filter(ProjectCategory::isActive)
                        .map(category -> new ProjectFormOptionsResponse.CategoryOption(category.getId(), category.getName())).toList(),
                customerRepository.findByOrganisation_IdOrderByNameAsc(organisationId).stream()
                        .filter(Customer::isActive)
                        .map(customer -> new ProjectFormOptionsResponse.CustomerOption(
                                customer.getId(), customer.getCustomerCode(), customer.getName())).toList(),
                projectManagerCandidates(organisationId),
                salesManagerCandidates(organisationId)
        );
    }

    public ProjectDetailsResponse getProjectDetailsById(Long projectId) {
        Project project = getAccessibleProjectById(projectId);
        Optional<ProjectPlanning> planningOpt = projectPlanningRepository.findByProjectId(projectId);

        ProjectDetailsResponse response = new ProjectDetailsResponse();
        response.setId(project.getId());
        response.setCode(project.getCode());
        response.setName(project.getName());
        response.setShortName(project.getShortName());
        response.setPortfolio(project.getPortfolio());
        response.setOrgAssignment(project.getOrgAssignment());
        response.setCountry(project.getCountry());
        response.setProjectType(project.getProjectType());
        response.setProjectPhase(project.getProjectPhase());
        response.setPriority(project.getPriority());
        response.setPlannedStart(project.getPlannedStart());
        response.setPlannedEnd(project.getPlannedEnd());
        response.setProjectBudget(project.getProjectBudget());
        response.setTotalProjectBudget(project.getTotalProjectBudget());
        response.setProjectManagerId(project.getProjectManagerId());
        response.setCustomer(project.getCustomer());
        response.setCustomerId(project.getCustomerId());
        response.setComment(project.getComment());

        if (planningOpt.isPresent()) {
            ProjectPlanning planning = planningOpt.get();
            response.setExpectedStart(planning.getExpectedStart());
            response.setExpectedEnd(planning.getExpectedEnd());
            response.setProjectCalendar(planning.getProjectCalendar());
            response.setProbability(planning.getProbability());
            response.setKeywords(planning.getKeywords());
            response.setSubcontractors(planning.getSubcontractors());
        }

        return response;
    }

    private Project getAccessibleProjectById(Long projectId) {
        return projectAccessService.getAccessibleProject(projectId);
    }

    private Long requireCurrentOrganisationId() {
        Long orgId = securityUtils.getCurrentOrganisationId();
        if (orgId == null) {
            throw new IllegalStateException("User has no organisation");
        }
        return orgId;
    }

    @Transactional
    public ProjectDashboardRowDto createProject(UpsertProjectRequest request) {
        Project project = new Project();
        Long organisationId = requireCurrentOrganisationId();
        applyRequest(project, request, organisationId);

        if (!securityUtils.isPlatformUser()) {
            Organisation organisation = new Organisation();
            organisation.setId(organisationId);
            project.setOrganisation(organisation);
        }

        Project saved = projectRepository.save(project);
        return toDashboardDto(saved);
    }

    @Transactional
    public ProjectDashboardRowDto updateProject(Long id, UpsertProjectRequest request) {
        Project project = getAccessibleProjectById(id);
        applyRequest(project, request, requireCurrentOrganisationId());

        if (!securityUtils.isPlatformUser()) {
            Long orgId = requireCurrentOrganisationId();
            if (project.getOrganisation() == null || !orgId.equals(project.getOrganisation().getId())) {
                throw new IllegalArgumentException("Project not accessible");
            }
        }

        Project saved = projectRepository.save(project);
        return toDashboardDto(saved);
    }

    /** Lead-only ownership operation. The organisation scope always comes from the authenticated user. */
    @Transactional
    public ProjectDashboardRowDto assignProjectManager(Long projectId, Long projectManagerId) {
        if (!securityUtils.hasAnyRole("PROJECT_MANAGER_LEAD")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Project Manager Lead can assign project managers.");
        }
        if (projectManagerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project manager ID is required.");
        }
        Long organisationId = requireCurrentOrganisationId();
        AppUser lead = userRepository.findByIdAndOrganisation_Id(securityUtils.getCurrentUserId(), organisationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
        if (!lead.isActive() || lead.getRole() != Role.PROJECT_MANAGER_LEAD) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Project Manager Lead can assign project managers.");
        }
        Project project = projectRepository.findByIdAndOrganisationId(projectId, organisationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        AppUser manager = userRepository.findByIdAndOrganisation_Id(projectManagerId, organisationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project manager not found."));
        if (!manager.isActive() || !manager.getRole().isProjectManagerRole()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user is not an eligible Project Manager.");
        }
        project.setProjectManagerId(manager.getId());
        project.setProjectManagerName(manager.getFullName());
        return toDashboardDto(projectRepository.save(project));
    }

    @Transactional
    public void archiveProject(Long id) {
        Project project = getAccessibleProjectById(id);
        project.setArchived(true);
        project.setProjectPhase("ARCHIVED");
        projectRepository.save(project);
    }

    private void applyRequest(Project project, UpsertProjectRequest request, Long organisationId) {
        project.setCode(request.getCode());
        project.setName(request.getName());
        project.setShortName(request.getShortName());
        Customer customer = request.getCustomerId() == null ? null : customerRepository
                .findByIdAndOrganisation_Id(request.getCustomerId(), organisationId)
                .filter(Customer::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Customer is not available for this organisation"));
        project.setCustomerEntity(customer);
        project.setCustomer(customer == null ? null : customer.getName());
        if (request.getCategoryId() == null) throw new IllegalArgumentException("Category is required");
        ProjectCategory category = categoryRepository.findByIdAndOrganisation_Id(request.getCategoryId(), organisationId)
                .filter(ProjectCategory::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Category is not available for this organisation"));
        project.setCategory(category.getName());
        project.setCountry(request.getCountry());
        project.setProjectType(request.getProjectType());
        project.setProjectPhase(request.getProjectPhase());
        project.setRiskLevel(request.getRiskLevel());
        project.setPlannedStart(request.getPlannedStart());
        project.setPlannedEnd(request.getPlannedEnd());
        project.setProjectBudget(request.getProjectBudget());
        project.setEstimatedCost(request.getEstimatedCost());
        applyOwnership(project, request, organisationId);
        project.setComment(request.getComment());
        if (request.getApplicationType() != null && !request.getApplicationType().isBlank()) {
            project.setApplicationType(
                ProjectApplicationType.valueOf(request.getApplicationType().toUpperCase())
            );
        } else if (project.getApplicationType() == null) {
            project.setApplicationType(ProjectApplicationType.PROJECTUM);
        }
    }

    private void applyOwnership(Project project, UpsertProjectRequest request, Long organisationId) {
        Long projectManagerId;
        if (securityUtils.hasAnyRole("PROJECT_MANAGER")) {
            projectManagerId = securityUtils.getCurrentUserId();
        } else if (securityUtils.hasAnyRole("PROJECT_MANAGER_LEAD")) {
            projectManagerId = request.getProjectManagerId();
        } else {
            // Ownership assignment is lead-only. Non-PM project editors cannot smuggle a manager ID into a general update.
            if (request.getProjectManagerId() != null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Project Manager Lead can assign project managers.");
            }
            projectManagerId = project.getProjectManagerId();
        }
        if (projectManagerId != null) {
            AppUser user = eligibleProjectManager(projectManagerId, organisationId);
            project.setProjectManagerId(user.getId());
            project.setProjectManagerName(user.getFullName());
        } else { project.setProjectManagerId(null); project.setProjectManagerName(null); }
        if (request.getSalesManagerId() != null) {
            project.setSalesManagerName(eligibleSalesManager(request.getSalesManagerId(), organisationId).getFullName());
        } else { project.setSalesManagerName(null); }
        // Keep historical program-manager values intact; new and edited projects no longer collect this field.
    }

    private AppUser eligibleUser(Long userId, Long organisationId, Role role) {
        AppUser user = userRepository.findByIdAndOrganisation_Id(userId, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Selected user is not available for this organisation"));
        if (!user.isActive() || user.getRole() != role) throw new IllegalArgumentException("Selected user is not eligible for this ownership role");
        return user;
    }

    private AppUser eligibleProjectManager(Long userId, Long organisationId) {
        AppUser user = userRepository.findByIdAndOrganisation_Id(userId, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Selected user is not available for this organisation"));
        if (!user.isActive() || !user.getRole().isProjectManagerRole()) {
            throw new IllegalArgumentException("Selected user is not eligible for the Project Manager role");
        }
        return user;
    }

    private AppUser eligibleSalesManager(Long userId, Long organisationId) {
        AppUser user = userRepository.findByIdAndOrganisation_Id(userId, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Selected user is not available for this organisation"));
        if (!user.isActive() || !user.getRole().isSalesManagerRole()) {
            throw new IllegalArgumentException("Selected user is not eligible for the Sales Manager role");
        }
        return user;
    }

    private java.util.List<ProjectFormOptionsResponse.UserOption> usersForRole(Long organisationId, Role role) {
        return userRepository.findByOrganisation_IdAndRoleOrderByFullNameAsc(organisationId, role).stream()
                .filter(AppUser::isActive).map(this::toUserOption).toList();
    }

    private List<ProjectFormOptionsResponse.UserOption> projectManagerCandidates(Long organisationId) {
        return userRepository.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(
                organisationId, List.of(Role.PROJECT_MANAGER, Role.PROJECT_MANAGER_LEAD))
                .stream().map(this::toUserOption).toList();
    }

    private List<ProjectFormOptionsResponse.UserOption> salesManagerCandidates(Long organisationId) {
        return userRepository.findByOrganisation_IdAndRoleInAndActiveTrueOrderByFullNameAsc(
                organisationId, List.of(Role.SALES_MANAGER, Role.SALES_MANAGER_LEAD))
                .stream().map(this::toUserOption).toList();
    }

    private ProjectFormOptionsResponse.UserOption toUserOption(AppUser user) {
        return new ProjectFormOptionsResponse.UserOption(user.getId(), user.getFullName(), user.getDepartmentCode(),
                user.getResourceType() == null ? null : user.getResourceType().getCode());
    }

    private ProjectDashboardRowDto toDashboardDto(Project p) {
        ProjectDashboardRowDto dto = new ProjectDashboardRowDto();
        dto.setId(p.getId());
        dto.setCode(p.getCode());
        dto.setName(p.getName());
        dto.setShortName(p.getShortName());
        dto.setCustomer(p.getCustomer());
        dto.setCustomerId(p.getCustomerId());
        dto.setCategory(p.getCategory());
        dto.setCountry(p.getCountry());
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
        dto.setArchived(Boolean.TRUE.equals(p.getArchived()));
        dto.setApplicationType(
            p.getApplicationType() != null ? p.getApplicationType().name() : "PROJECTUM"
        );
        return dto;
    }
}
