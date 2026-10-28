package com.ercopac.ercopac_tracker.projectum_ai.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.risks.repository.RiskItemRepository;
import com.ercopac.ercopac_tracker.projectum.actions.repository.ActionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProjectAiContextService {

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository taskRepository;
    private final FinanceEntryRepository financeRepository;
    private final RiskItemRepository riskRepository;
    private final ActionItemRepository actionRepository;

    @Transactional(readOnly = true)
    public String buildPlatformProjectContext(Long projectId, String question) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        if (project.getOrganisation() == null) throw new IllegalArgumentException("Project organisation not found");
        return formatContext(project, project.getOrganisation().getId(), question);
    }

    @Transactional(readOnly = true)
    public String buildProjectContext(Long projectId, Long organisationId, String question) {
        if (projectId == null || organisationId == null) throw new IllegalArgumentException("Project not found");
        Project project = projectRepository.findByIdAndOrganisationId(projectId, organisationId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        return formatContext(project, organisationId, question);
    }

    private String formatContext(Project project, Long organisationId, String question) {
        String q = question.toLowerCase(Locale.ROOT);
        boolean tasks = hasAny(q, "task", "schedule", "milestone", "date", "deadline", "delay");
        boolean finance = hasAny(q, "financ", "cost", "budget", "forecast", "spend", "revenue", "sales");
        boolean risks = hasAny(q, "risk", "issue", "mitigation", "block");
        boolean actions = hasAny(q, "action", "todo", "next step");
        boolean overview = hasAny(q, "progress", "status", "name", "customer", "project end", "project start", "code");
        boolean report = q.contains("report");
        int rowLimit = report ? 2 : 6;
        if (hasAny(q, "summar", "report", "management focus") || !(tasks || finance || risks || actions || overview)) {
            tasks = finance = risks = actions = true;
        }

        StringBuilder out = new StringBuilder(1200)
                .append("Project: ").append(clean(project.getName())).append(" [").append(clean(project.getCode())).append("]")
                .append("; progress=").append(project.getProgress())
                .append("%; customer=").append(clean(project.getCustomer()))
                .append("; planned end=").append(project.getPlannedEnd())
                .append("; project budget=").append(project.getProjectBudget()).append('\n');
        if (tasks) {
            var rows = taskRepository.findByProjectIdAndOrganisationIdOrderByDisplayOrderAscIdAsc(project.getId(), organisationId);
            out.append("Tasks/milestones (total ").append(rows.size()).append("; sample only):\n");
            var selected = rows.stream()
                    .filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()) && (t.getPercentComplete() == null || t.getPercentComplete() < 100))
                    .limit(rowLimit).toList();
            if (selected.isEmpty()) selected = rows.stream().limit(rowLimit).toList();
            selected.forEach(t -> out.append("- ").append(clean(t.getWbsCode())).append(' ')
                    .append(clean(t.getName())).append("; type=").append(clean(t.getTaskType()))
                    .append("; status=").append(clean(t.getStatus())).append("; progress=").append(t.getPercentComplete())
                    .append("%; planned end=").append(t.getPlannedEnd()).append("; actual end=").append(t.getActualEnd()).append('\n'));
        }
        if (finance) {
            var rows = financeRepository.findAllByProjectIdAndOrganisationIdOrderByWbsCodeAsc(project.getId(), organisationId);
            out.append("Finance rows (total ").append(rows.size()).append("; sample only; do not sum summary and child rows):\n");
            rows.stream().limit(rowLimit).forEach(f -> out.append("- ").append(clean(f.getWbsCode())).append(' ')
                    .append(clean(f.getDescription())).append("; budget=").append(f.getBudget())
                    .append("; actual cost=").append(f.getActualCost()).append("; forecast=").append(f.getForecast()).append('\n'));
        }
        if (risks) {
            var rows = riskRepository.findAllByProjectIdAndOrganisation_IdOrderByIdAsc(project.getId(), organisationId);
            out.append("Risks (total ").append(rows.size()).append("; sample only):\n");
            rows.stream().limit(rowLimit).forEach(r -> out.append("- ").append(clean(r.getDescription()))
                    .append("; state=").append(clean(r.getState())).append("; due=").append(r.getDueDate())
                    .append("; mitigation=").append(clean(r.getMitigation())).append('\n'));
        }
        if (actions) {
            var rows = actionRepository.findAllByProjectIdAndOrganisationIdOrderByIdAsc(project.getId(), organisationId);
            out.append("Actions (total ").append(rows.size()).append("; sample only):\n");
            rows.stream().limit(rowLimit).forEach(a -> out.append("- ").append(clean(a.getTitle()))
                    .append("; status=").append(clean(a.getStatus())).append("; due=").append(a.getDueDate()).append('\n'));
        }
        return out.toString();
    }

    private boolean hasAny(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private String clean(Object value) {
        if (value == null) return "n/a";
        String text = value.toString().replace('\n', ' ').replace('\r', ' ').trim();
        return text.length() > 140 ? text.substring(0, 140) : text;
    }
}
