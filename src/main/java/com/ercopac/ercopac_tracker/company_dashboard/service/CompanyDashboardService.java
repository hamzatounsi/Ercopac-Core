package com.ercopac.ercopac_tracker.company_dashboard.service;

import com.ercopac.ercopac_tracker.company_dashboard.dto.CompanyDashboardDto;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.projectum.actions.domain.ActionItem;
import com.ercopac.ercopac_tracker.projectum.actions.repository.ActionItemRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.risks.domain.RiskItem;
import com.ercopac.ercopac_tracker.projectum.risks.repository.RiskItemRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CompanyDashboardService {
    private final SecurityUtils securityUtils;
    private final OrganisationRepository organisationRepository;
    private final ProjectRepository projectRepository;
    private final FinanceEntryRepository financeRepository;
    private final RiskItemRepository riskRepository;
    private final ActionItemRepository actionRepository;

    public CompanyDashboardService(SecurityUtils securityUtils, OrganisationRepository organisationRepository,
                                   ProjectRepository projectRepository, FinanceEntryRepository financeRepository,
                                   RiskItemRepository riskRepository, ActionItemRepository actionRepository) {
        this.securityUtils = securityUtils; this.organisationRepository = organisationRepository;
        this.projectRepository = projectRepository; this.financeRepository = financeRepository;
        this.riskRepository = riskRepository; this.actionRepository = actionRepository;
    }

    public CompanyDashboardDto getDashboard() {
        Long organisationId = securityUtils.getCurrentOrganisationId();
        String organisationName = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new IllegalStateException("Organisation not found")).getName();
        List<Project> projects = projectRepository.findAllByOrganisationId(organisationId);
        Map<Long, Project> projectById = projects.stream().collect(Collectors.toMap(Project::getId, Function.identity()));
        List<FinanceEntry> finance = financeRepository.findAllByOrganisationId(organisationId);
        List<RiskItem> risks = riskRepository.findAllByOrganisation_IdOrderByIdDesc(organisationId);
        List<ActionItem> actions = actionRepository.findAllByOrganisationIdOrderByDueDateAscIdAsc(organisationId);
        LocalDate today = LocalDate.now();
        List<Project> active = projects.stream().filter(p -> !Boolean.TRUE.equals(p.getArchived()) && !"COMPLETED".equalsIgnoreCase(p.getProjectPhase())).toList();
        int delayed = (int) active.stream().filter(p -> p.getPlannedEnd() != null && p.getPlannedEnd().isBefore(today)).count();
        int atRisk = (int) active.stream().filter(p -> "HIGH".equalsIgnoreCase(p.getRiskLevel()) || "MEDIUM".equalsIgnoreCase(p.getRiskLevel())).count();
        int onSchedule = Math.max(0, active.size() - delayed - atRisk);
        int averageProgress = projects.isEmpty() ? 0 : (int) Math.round(projects.stream().map(Project::getProgress).filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average().orElse(0));
        BigDecimal totalBudget = projects.stream().map(Project::getProjectBudget).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal actualCost = finance.stream().map(FinanceEntry::getActualCost).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal forecastCost = finance.stream().map(FinanceEntry::getForecast).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<RiskItem> openRisks = risks.stream().filter(r -> !"closed".equalsIgnoreCase(r.getState())).toList();
        int highRisks = (int) openRisks.stream().filter(r -> impactScore(r.getImpact()) >= 4 || (r.getProbability() != null && r.getProbability() >= 70)).count();
        List<ActionItem> openActions = actions.stream().filter(a -> !"done".equalsIgnoreCase(a.getStatus())).toList();
        int overdueActions = (int) openActions.stream().filter(a -> a.getDueDate() != null && a.getDueDate().isBefore(today)).count();
        List<CompanyDashboardDto.AlertRow> alerts = buildAlerts(delayed, highRisks, overdueActions, totalBudget, actualCost);
        return new CompanyDashboardDto(organisationName, projects.size(), active.size(), onSchedule, atRisk, delayed,
                averageProgress, totalBudget, actualCost, forecastCost, openRisks.size(), highRisks, openActions.size(), overdueActions,
                projects.stream().sorted(Comparator.comparing(Project::getName, Comparator.nullsLast(String::compareToIgnoreCase))).limit(8)
                        .map(p -> new CompanyDashboardDto.ProjectRow(p.getName(), p.getCode(), p.getProjectManagerName(), p.getProjectPhase(),
                                p.getProgress() == null ? 0 : p.getProgress(), health(p, today), p.getProjectBudget())).toList(),
                openRisks.stream().sorted(Comparator.comparing(RiskItem::getProbability, Comparator.nullsLast(Comparator.reverseOrder()))).limit(6)
                        .map(r -> new CompanyDashboardDto.RiskRow(r.getDescription(), projectName(projectById, r), r.getImpact(), r.getProbability(), r.getState())).toList(), alerts);
    }

    private List<CompanyDashboardDto.AlertRow> buildAlerts(int delayed, int highRisks, int overdueActions, BigDecimal budget, BigDecimal actual) {
        java.util.ArrayList<CompanyDashboardDto.AlertRow> alerts = new java.util.ArrayList<>();
        if (delayed > 0) alerts.add(new CompanyDashboardDto.AlertRow("critical", delayed + " delayed project(s)", "Planned end date has passed."));
        if (highRisks > 0) alerts.add(new CompanyDashboardDto.AlertRow("warning", highRisks + " high exposure risk(s)", "Open risks require executive attention."));
        if (overdueActions > 0) alerts.add(new CompanyDashboardDto.AlertRow("critical", overdueActions + " overdue action(s)", "Action due date has passed."));
        if (budget.signum() > 0 && actual.compareTo(budget) > 0) alerts.add(new CompanyDashboardDto.AlertRow("warning", "Portfolio over budget", "Actual cost exceeds approved project budget."));
        return alerts;
    }
    private int impactScore(String impact) { try { return Integer.parseInt(impact); } catch (Exception ignored) { return 0; } }
    private String projectName(Map<Long, Project> projects, RiskItem risk) { return risk.getProject() == null ? "—" : projects.getOrDefault(risk.getProject().getId(), risk.getProject()).getName(); }
    private String health(Project project, LocalDate today) {
        if (project.getPlannedEnd() != null && project.getPlannedEnd().isBefore(today)) return "Delayed";
        if ("HIGH".equalsIgnoreCase(project.getRiskLevel()) || "MEDIUM".equalsIgnoreCase(project.getRiskLevel())) return "At risk";
        return "On track";
    }
}
