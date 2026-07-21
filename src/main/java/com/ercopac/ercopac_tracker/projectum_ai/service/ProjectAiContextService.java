package com.ercopac.ercopac_tracker.projectum_ai.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.risks.repository.RiskItemRepository;
import com.ercopac.ercopac_tracker.projectum.actions.repository.ActionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectAiContextService {

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;
    private final FinanceEntryRepository financeRepository;
    private final RiskItemRepository riskRepository;
    private final ActionItemRepository actionRepository;

    public String buildPlatformProjectContext(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Long organisationId = project.getOrganisation() != null
                ? project.getOrganisation().getId()
                : null;

        if (organisationId == null) {
            throw new RuntimeException("Project organisation not found");
        }

        return buildProjectContext(projectId, organisationId);
    }

    public String buildProjectContext(Long projectId, Long organisationId) {
        Project project = projectRepository.findByIdAndOrganisationId(projectId, organisationId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        var tasks = taskRepository.findByProjectIdAndOrganisationIdOrderByDisplayOrderAscIdAsc(projectId, organisationId);
        var finance = financeRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(projectId, organisationId);
        var risks = riskRepository.findAllByProjectIdAndOrganisation_IdOrderByIdAsc(projectId, organisationId);
        var actions = actionRepository.findAllByProjectIdAndOrganisationIdOrderByIdAsc(projectId, organisationId);

        return """
                PROJECTUM PROJECT CONTEXT

                Project:
                - Name: %s
                - Code: %s
                - Status: %s
                - Progress: %s
                - Customer: %s
                - Category: %s
                - Type: %s

                Tasks:
                %s

                Finance:
                %s

                Risks:
                %s

                Actions:
                %s
                """.formatted(
                project.getName(),
                project.getCode(),
                "Not available",
                project.getProgress(),
                project.getCustomer(),
                project.getCategory(),
                project.getProjectType(),
                tasks,
                finance,
                risks,
                actions
        );
    }
}
