package com.ercopac.ercopac_tracker.projectum.finance;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceEntryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceImportResultDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.FinanceSummaryDto;
import com.ercopac.ercopac_tracker.projectum.finance.dto.UpsertFinanceEntryRequest;
import com.ercopac.ercopac_tracker.projectum.finance.repository.FinanceEntryRepository;
import com.ercopac.ercopac_tracker.projectum.finance.service.FinanceService;
import com.ercopac.ercopac_tracker.projectum.finance.settings.domain.FinanceWbsRowType;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.FinanceWbsTemplateRowDto;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ImportFinanceWbsTemplateRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.dto.SaveFinanceSettingsRequest;
import com.ercopac.ercopac_tracker.projectum.finance.settings.service.FinanceSettingsService;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastRowDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.ForecastSummaryDto;
import com.ercopac.ercopac_tracker.projectum.forecast.dto.UpsertForecastEntryRequest;
import com.ercopac.ercopac_tracker.projectum.forecast.service.ForecastService;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end Finance/Forecast workflow on the real services and an in-memory database:
 * WBS import, editing, forecasting, deleting, earned value, tenant isolation and refresh consistency.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:financeflow;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.hikari.schema=PUBLIC",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed.enabled=false",
        "app.bootstrap-owner.enabled=false",
        "projectum.ai.enabled=false"
})
@Transactional
class FinanceForecastWorkflowTest {

    private static final String TOTAL = "P25", DESIGN = "P25.D", ELETTRICO = "P25.D.E", EXTERNAL = "P25.D.E.EXT",
            ENGINEERING = "P25.D.E.ENG", MECCANICO = "P25.D.M", MATERIALS = "P25.D.M.MAT", SITE = "P25.S", TRAVEL = "P25.S.TRV",
            LABOUR = "P25.S.HRS";

    @Autowired FinanceService financeService;
    @Autowired ForecastService forecastService;
    @Autowired FinanceSettingsService settingsService;
    @Autowired OrganisationRepository organisationRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired FinanceEntryRepository financeEntryRepository;
    @Autowired EntityManager entityManager;

    private Organisation orgA;
    private Organisation orgB;
    private Project project;
    private Project otherProject;
    private Project foreignProject;
    private final String week = FinanceService.weekKey(LocalDate.now());
    private final String nextWeek = FinanceService.weekKey(LocalDate.now().plusWeeks(1));

    @BeforeEach
    void setUp() {
        orgA = organisation("Org A", "ORG-A");
        orgB = organisation("Org B", "ORG-B");
        project = project("P25", orgA, 10L);
        otherProject = project("P26", orgA, 99L);
        foreignProject = project("Q25", orgB, 20L);
        authenticate(orgA, 10L, "PROJECT_MANAGER_LEAD");
        settingsService.importWbsTemplate(project.getId(), importRequest(standardWbs(), false));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    // ---------- 1. Excel import ----------

    @Test
    void importPreservesHierarchyAndFinanceAndForecastShowTheSameTree() {
        Map<String, FinanceEntryDto> finance = finance();
        List<ForecastRowDto> forecast = forecastGrid();

        assertEquals(standardWbs().stream().map(FinanceWbsTemplateRowDto::getCodeTemplate).toList(),
                financeService.getProjectFinance(project.getId()).stream().map(FinanceEntryDto::getWbsCode).toList());
        assertEquals(financeService.getProjectFinance(project.getId()).stream().map(FinanceEntryDto::getWbsCode).toList(),
                forecast.stream().map(ForecastRowDto::getWbsCode).toList());
        assertEquals(finance.get(ELETTRICO).getId(), finance.get(EXTERNAL).getParentId());
        assertEquals(finance.get(DESIGN).getId(), finance.get(ELETTRICO).getParentId());
        assertEquals(finance.get(TOTAL).getId(), finance.get(DESIGN).getParentId());
        for (ForecastRowDto row : forecast) {
            FinanceEntryDto financeRow = finance.get(row.getWbsCode());
            assertEquals(financeRow.getId(), row.getFinanceEntryId());
            assertEquals(financeRow.getParentId(), row.getParentId());
            assertEquals(financeRow.getLevel(), row.getLevel());
            assertEquals(financeRow.getDescription(), row.getDescription());
            assertEquals(financeRow.isLeaf(), row.isLeaf());
        }
    }

    @Test
    void importRejectsInvalidRowsWithRowNumberAndReasonAndSavesNothing() {
        Project fresh = project("P27", orgA, 10L);
        List<FinanceWbsTemplateRowDto> rows = List.of(
                wbs("A", "Total", 1, FinanceWbsRowType.SUMMARY),
                wbs("A.1.1", "Too deep", 3, FinanceWbsRowType.COST),
                wbs("", "No code", 2, FinanceWbsRowType.COST),
                wbs("A", "Duplicate", 2, FinanceWbsRowType.COST));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> settingsService.importWbsTemplate(fresh.getId(), importRequest(rows, false)));
        assertTrue(error.getMessage().contains("Row 3: missing WBS code"), error.getMessage());
        assertTrue(error.getMessage().contains("Row 4: duplicate WBS code A (already on row 1)"), error.getMessage());

        IllegalArgumentException levels = assertThrows(IllegalArgumentException.class,
                () -> settingsService.importWbsTemplate(fresh.getId(), importRequest(rows.subList(0, 2), false)));
        assertTrue(levels.getMessage().contains("Row 2 (A.1.1): level 3 cannot follow level 1"), levels.getMessage());
        assertTrue(settingsService.getSettings(fresh.getId()).getTemplateRows().isEmpty());
    }

    @Test
    void importingTheSameFileTwiceCreatesNoDuplicates() {
        Map<String, Long> idsBefore = ids();
        settingsService.importWbsTemplate(project.getId(), importRequest(standardWbs(), false));
        settingsService.importWbsTemplate(project.getId(), importRequest(standardWbs(), true));

        assertEquals(idsBefore, ids());
        assertEquals(standardWbs().size(), settingsService.getSettings(project.getId()).getTemplateRows().size());
    }

    @Test
    void importStopsInsteadOfOverwritingNodesThatHoldData() {
        setValues(EXTERNAL, 1000, 0, 0);
        List<FinanceWbsTemplateRowDto> changed = standardWbs();
        changed.get(3).setDescription("External Services (renamed by file)");

        ResponseStatusException conflict = assertConflict(
                () -> settingsService.importWbsTemplate(project.getId(), importRequest(changed, false)));
        assertTrue(conflict.getReason().contains("Row 4 (P25.D.E.EXT) holds budget; the import would change its description"),
                conflict.getReason());

        List<FinanceWbsTemplateRowDto> withoutExternal = standardWbs();
        withoutExternal.remove(3);
        ResponseStatusException removal = assertConflict(
                () -> settingsService.importWbsTemplate(project.getId(), importRequest(withoutExternal, true)));
        assertTrue(removal.getReason().contains("WBS P25.D.E.EXT holds budget and would be removed"), removal.getReason());

        refresh();
        assertEquals("External Services", finance().get(EXTERNAL).getDescription());
        assertEquals(0, new BigDecimal("1000").compareTo(finance().get(EXTERNAL).getBudget()));
    }

    // ---------- 2. Editing nodes ----------

    @Test
    void renamingANodeKeepsItsFinancialAndForecastData() {
        setValues(EXTERNAL, 1000, 200, 100);
        forecast(EXTERNAL, week, 500);
        Long id = ids().get(EXTERNAL);

        List<FinanceWbsTemplateRowDto> rows = settingsRows();
        rows.get(3).setCodeTemplate("P25.D.E.SRV");
        rows.get(3).setDescription("Outsourced services");
        save(rows);
        refresh();

        FinanceEntryDto renamed = finance().get("P25.D.E.SRV");
        assertEquals(id, renamed.getId());
        assertNull(finance().get(EXTERNAL));
        assertEquals("Outsourced services", renamed.getDescription());
        assertMoney(1000, renamed.getBudget());
        assertMoney(200, renamed.getActualCost());
        assertMoney(500, renamed.getForecast());
        assertMoney(500, forecastRows().get("P25.D.E.SRV").getRemainingForecastCost());
    }

    @Test
    void addedNodeAppearsInFinanceAndForecast() {
        List<FinanceWbsTemplateRowDto> rows = settingsRows();
        rows.add(rows.size(), wbs("P25.S.NEW", "Site equipment", 3, FinanceWbsRowType.COST));
        save(rows);
        refresh();

        assertEquals(ids().get(SITE), finance().get("P25.S.NEW").getParentId());
        assertEquals(ids().get(SITE), forecastRows().get("P25.S.NEW").getParentId());
    }

    @Test
    void movingALeafRecalculatesBothBranchesAndKeepsTheProjectTotal() {
        setValues(EXTERNAL, 1000, 0, 0);
        setValues(MATERIALS, 200, 0, 0);
        forecast(MATERIALS, week, 300);
        BigDecimal totalBefore = finance().get(TOTAL).getBudget();

        List<FinanceWbsTemplateRowDto> rows = settingsRows();
        FinanceWbsTemplateRowDto materials = rows.remove(6);
        rows.add(5, materials); // directly after Engineering, still level 4 -> now under DESIGN Elettrico
        save(rows);
        refresh();

        Map<String, FinanceEntryDto> finance = finance();
        assertEquals(finance.get(ELETTRICO).getId(), finance.get(MATERIALS).getParentId());
        assertMoney(1200, finance.get(ELETTRICO).getBudget());
        assertMoney(0, finance.get(MECCANICO).getBudget());
        assertMoney(300, finance.get(ELETTRICO).getForecast());
        assertMoney(0, finance.get(MECCANICO).getForecast());
        assertEquals(0, totalBefore.compareTo(finance.get(TOTAL).getBudget()));
        assertMoney(300, forecastRows().get(ELETTRICO).getRemainingForecastCost());
        assertMoney(0, forecastRows().get(MECCANICO).getRemainingForecastCost());
    }

    @Test
    void invalidMovesAreRejectedAndNothingChanges() {
        setValues(EXTERNAL, 1000, 0, 0);
        Map<String, Long> idsBefore = ids();

        List<FinanceWbsTemplateRowDto> orphan = settingsRows();
        orphan.get(6).setLevel(5); // Materials two levels below DESIGN Meccanico
        IllegalArgumentException levelError = assertThrows(IllegalArgumentException.class, () -> save(orphan));
        assertTrue(levelError.getMessage().contains("level 5 cannot follow level 3"), levelError.getMessage());

        List<FinanceWbsTemplateRowDto> underDataNode = settingsRows();
        underDataNode.get(4).setLevel(5); // Engineering would become a child of External Services, which holds budget
        ResponseStatusException conflict = assertConflict(() -> save(underDataNode));
        assertTrue(conflict.getReason().contains("WBS node P25.D.E.EXT holds budget and must stay a leaf"), conflict.getReason());

        refresh();
        assertEquals(idsBefore, ids());
        assertEquals(ids().get(ELETTRICO), finance().get(ENGINEERING).getParentId());
    }

    // ---------- 3. Forecasts ----------

    @Test
    void weeklyForecastUpdatesParentGrandparentAndProjectTotalsWithoutTouchingOtherWeeks() {
        forecast(EXTERNAL, nextWeek, 50);
        forecast(ENGINEERING, week, 70);
        BigDecimal elettrico = finance().get(ELETTRICO).getForecast();
        BigDecimal design = finance().get(DESIGN).getForecast();
        BigDecimal total = finance().get(TOTAL).getForecast();

        forecast(EXTERNAL, week, 500);
        assertTotals(elettrico.add(money(500)), design.add(money(500)), total.add(money(500)));

        forecast(EXTERNAL, week, 800);
        assertTotals(elettrico.add(money(800)), design.add(money(800)), total.add(money(800)));

        forecast(EXTERNAL, week, 0);
        assertTotals(elettrico, design, total);

        Map<String, ForecastRowDto> grid = forecastRows();
        assertMoney(50, cell(grid.get(EXTERNAL), nextWeek));
        assertMoney(0, cell(grid.get(EXTERNAL), week));
        assertMoney(70, cell(grid.get(ENGINEERING), week));
        assertMoney(70, cell(grid.get(ELETTRICO), week));
        assertMoney(50, cell(grid.get(ELETTRICO), nextWeek));
    }

    @Test
    void hourForecastsAreConvertedToCostBeforeReachingParents() {
        forecast(LABOUR, week, 10); // 10 hours at the row rate of 50
        Map<String, ForecastRowDto> grid = forecastRows();

        assertMoney(10, grid.get(LABOUR).getTotalForecast());
        assertMoney(500, grid.get(LABOUR).getRemainingForecastCost());
        assertMoney(500, cell(grid.get(SITE), week));
        assertMoney(500, finance().get(LABOUR).getForecast());
        assertMoney(500, finance().get(TOTAL).getForecast());
    }

    @Test
    void costForecastRequiresALeafAndCannotLinkAGanttTask() {
        IllegalArgumentException parent = assertThrows(IllegalArgumentException.class, () -> forecast(ELETTRICO, week, 100));
        assertTrue(parent.getMessage().contains("Select a leaf Finance WBS row"));

        IllegalArgumentException gantt = assertThrows(IllegalArgumentException.class,
                () -> forecastService.updateLinkedScheduleWbs(project.getId(), ids().get(EXTERNAL), "1.1"));
        assertTrue(gantt.getMessage().contains("Only hour forecasts can link to a schedule task"));
    }

    // ---------- 4. Deleting ----------

    @Test
    void deletingAForecastEntryUpdatesTotalsAndKeepsTheWbsNode() {
        forecast(EXTERNAL, week, 500);
        forecast(EXTERNAL, nextWeek, 50);
        forecast(ENGINEERING, week, 70);

        assertEquals(2, forecastService.deleteForecastEntries(project.getId(), ids().get(EXTERNAL)));
        refresh();

        assertNotNull(finance().get(EXTERNAL));
        assertMoney(0, finance().get(EXTERNAL).getForecast());
        assertMoney(70, finance().get(ELETTRICO).getForecast());
        assertMoney(70, forecastRows().get(TOTAL).getRemainingForecastCost());
        assertTrue(forecastRows().containsKey(EXTERNAL));
    }

    @Test
    void deletingAWbsNodeWithChildrenOrDataIsBlockedAndNothingIsLost() {
        setValues(EXTERNAL, 1000, 0, 0);
        forecast(ENGINEERING, week, 70);
        int rowsBefore = finance().size();

        ResponseStatusException children = assertConflict(() -> financeService.deleteEntry(project.getId(), ids().get(ELETTRICO)));
        assertTrue(children.getReason().contains("Cannot delete WBS node P25.D.E because it has child nodes"), children.getReason());
        ResponseStatusException data = assertConflict(() -> financeService.deleteEntry(project.getId(), ids().get(EXTERNAL)));
        assertTrue(data.getReason().contains("because it holds budget"), data.getReason());
        ResponseStatusException forecast = assertConflict(() -> financeService.deleteEntry(project.getId(), ids().get(ENGINEERING)));
        assertTrue(forecast.getReason().contains("because it holds forecast entries"), forecast.getReason());

        List<FinanceWbsTemplateRowDto> withoutExternal = settingsRows();
        withoutExternal.remove(3);
        ResponseStatusException settings = assertConflict(() -> save(withoutExternal));
        assertTrue(settings.getReason().contains("Cannot delete WBS node P25.D.E.EXT because it holds budget"), settings.getReason());

        refresh();
        assertEquals(rowsBefore, finance().size());
        assertMoney(1000, finance().get(EXTERNAL).getBudget());
        assertMoney(70, finance().get(ENGINEERING).getForecast());
    }

    @Test
    void deletingAnEmptyLeafRemovesItPermanently() {
        financeService.deleteEntry(project.getId(), ids().get(TRAVEL));
        refresh();

        assertNull(finance().get(TRAVEL));
        assertFalse(forecastRows().containsKey(TRAVEL));
        assertTrue(settingsRows().stream().noneMatch(row -> TRAVEL.equals(row.getCodeTemplate())));

        settingsService.applyTemplate(project.getId(), new com.ercopac.ercopac_tracker.projectum.finance.settings.dto.ApplyFinanceTemplateRequest());
        refresh();
        assertNull(finance().get(TRAVEL), "the deleted node must not come back on the next apply");
    }

    // ---------- 5. Calculations ----------

    @Test
    void eacAndVacUseActualPlusRemainingForecastWithoutDoubleCountingCommitments() {
        setValues(EXTERNAL, 1000, 300, 400);
        forecast(EXTERNAL, week, 500);
        FinanceEntry parent = financeEntryRepository.findById(ids().get(ELETTRICO)).orElseThrow();
        parent.setBudget(new BigDecimal("99999")); // legacy value stored on a parent must be ignored
        parent.setActualCost(new BigDecimal("99999"));
        refresh();

        FinanceEntryDto external = finance().get(EXTERNAL);
        assertMoney(800, external.getEac());
        assertMoney(200, external.getVariance());
        FinanceEntryDto elettrico = finance().get(ELETTRICO);
        assertMoney(1000, elettrico.getBudget());
        assertMoney(300, elettrico.getActualCost());
        assertMoney(400, elettrico.getCommitment());
        assertMoney(800, elettrico.getEac());

        FinanceSummaryDto summary = financeService.getProjectFinanceSummary(project.getId());
        assertMoney(1000, summary.getTotalBudget());
        assertMoney(400, summary.getTotalCommitment());
        assertMoney(800, summary.getTotalEac());
        assertMoney(200, summary.getTotalVariance());
        ForecastSummaryDto forecastSummary = forecastService.getSummary(project.getId(), 12);
        assertMoney(800, forecastSummary.getTotalEac());
        assertMoney(200, forecastSummary.getTotalVariance());
    }

    @Test
    void valueImportReportsOverwritesUnknownCodesAndIgnoresParentSubtotals() {
        setValues(EXTERNAL, 1000, 0, 0);

        IllegalArgumentException unknown = assertThrows(IllegalArgumentException.class, () -> financeService.importEntries(
                project.getId(), List.of(values(EXTERNAL, 1000, 0, 0), values("NOPE", 1, 0, 0)), false));
        assertTrue(unknown.getMessage().contains("Row 2: WBS NOPE does not exist in Finance Settings"), unknown.getMessage());

        List<UpsertFinanceEntryRequest> update = List.of(values(EXTERNAL, 1200, 0, 0), values(ELETTRICO, 5000, 0, 0));
        ResponseStatusException conflict = assertConflict(() -> financeService.importEntries(project.getId(), update, false));
        assertTrue(conflict.getReason().contains("Row 1 (P25.D.E.EXT): budget 1000 -> 1200"), conflict.getReason());
        refresh();
        assertMoney(1000, finance().get(EXTERNAL).getBudget());

        FinanceImportResultDto result = financeService.importEntries(project.getId(), update, true);
        assertEquals(1, result.getUpdatedRows());
        assertTrue(result.getMessages().get(0).contains("1 parent row(s) ignored"));
        refresh();
        assertMoney(1200, finance().get(ELETTRICO).getBudget());
    }

    // ---------- 6. Isolation ----------

    @Test
    void otherTenantsAndOtherProjectsCannotReadChangeOrDelete() {
        forecast(EXTERNAL, week, 500);
        Long external = ids().get(EXTERNAL);
        Long projectId = project.getId();

        authenticate(orgB, 20L, "PROJECT_MANAGER_LEAD");
        assertDenied(() -> financeService.getProjectFinance(projectId));
        assertDenied(() -> forecastService.getForecastGrid(projectId, 12, "week"));
        assertDenied(() -> settingsService.getSettings(projectId));
        assertDenied(() -> forecastService.upsertForecast(projectId, forecastRequest(external, EXTERNAL, week, 1)));
        assertDenied(() -> forecastService.deleteForecastEntries(projectId, external));
        assertDenied(() -> financeService.deleteEntry(projectId, external));
        assertDenied(() -> settingsService.resetForecastBaseline(projectId));
        assertDenied(() -> settingsService.savePaymentSchedule(projectId, new com.ercopac.ercopac_tracker.projectum.finance.settings.dto.SavePaymentScheduleRequest()));
        // A row id of another tenant's project is not found from the caller's own project either
        assertThrows(IllegalArgumentException.class, () -> forecastService.deleteForecastEntries(foreignProject.getId(), external));

        authenticate(orgA, 10L, "PROJECT_MANAGER_LEAD");
        assertThrows(IllegalArgumentException.class, () -> forecastService.deleteForecastEntries(otherProject.getId(), external));
        assertThrows(IllegalArgumentException.class, () -> financeService.deleteEntry(otherProject.getId(), external));

        authenticate(orgA, 99L, "PROJECT_MANAGER"); // manages otherProject only
        assertDenied(() -> financeService.getProjectFinance(projectId));
        assertDenied(() -> forecastService.deleteForecastEntries(projectId, external));

        authenticate(orgA, 10L, "PROJECT_MANAGER_LEAD");
        refresh();
        assertMoney(500, finance().get(EXTERNAL).getForecast());
    }

    // ---------- 7. Refresh ----------

    @Test
    void freshReadsOfFinanceAndForecastReturnTheSameWbsAndLatestValues() {
        setValues(EXTERNAL, 1000, 300, 100);
        forecast(EXTERNAL, week, 400);
        forecast(LABOUR, nextWeek, 2);
        List<FinanceWbsTemplateRowDto> rows = settingsRows();
        rows.get(2).setDescription("DESIGN Elettrico (rev B)");
        save(rows);
        refresh();

        List<FinanceEntryDto> finance = financeService.getProjectFinance(project.getId());
        Map<Long, ForecastRowDto> forecast = forecastGrid().stream()
                .collect(Collectors.toMap(ForecastRowDto::getFinanceEntryId, Function.identity()));
        assertEquals(finance.size(), forecast.size());
        for (FinanceEntryDto row : finance) {
            ForecastRowDto other = forecast.get(row.getId());
            assertEquals(row.getWbsCode(), other.getWbsCode());
            assertEquals(row.getDescription(), other.getDescription());
            assertEquals(row.getParentId(), other.getParentId());
            assertEquals(0, row.getForecast().compareTo(other.getRemainingForecastCost()), row.getWbsCode());
            assertEquals(0, row.getBudget().compareTo(other.getBudget()), row.getWbsCode());
            assertEquals(0, row.getActualCost().compareTo(other.getActualCost()), row.getWbsCode());
        }
        assertEquals("DESIGN Elettrico (rev B)", finance().get(ELETTRICO).getDescription());
        assertMoney(500, finance().get(TOTAL).getForecast()); // 400 EUR + 2 h x 50
    }

    // ---------- helpers ----------

    private List<FinanceWbsTemplateRowDto> standardWbs() {
        FinanceWbsTemplateRowDto labour = wbs(LABOUR, "Site labour", 3, FinanceWbsRowType.HOUR);
        labour.setHourRate(new BigDecimal("50"));
        return new ArrayList<>(List.of(
                wbs(TOTAL, "Project total", 1, FinanceWbsRowType.SUMMARY),
                wbs(DESIGN, "DESIGN", 2, FinanceWbsRowType.SUMMARY),
                wbs(ELETTRICO, "DESIGN Elettrico", 3, FinanceWbsRowType.SUMMARY),
                wbs(EXTERNAL, "External Services", 4, FinanceWbsRowType.COST),
                wbs(ENGINEERING, "Engineering", 4, FinanceWbsRowType.COST),
                wbs(MECCANICO, "DESIGN Meccanico", 3, FinanceWbsRowType.SUMMARY),
                wbs(MATERIALS, "Materials", 4, FinanceWbsRowType.COST),
                wbs(SITE, "SITE", 2, FinanceWbsRowType.SUMMARY),
                wbs(TRAVEL, "Travel", 3, FinanceWbsRowType.EXPENSES),
                labour));
    }

    private FinanceWbsTemplateRowDto wbs(String code, String description, int level, FinanceWbsRowType type) {
        FinanceWbsTemplateRowDto row = new FinanceWbsTemplateRowDto();
        row.setCodeTemplate(code);
        row.setDescription(description);
        row.setLevel(level);
        row.setType(type);
        return row;
    }

    private ImportFinanceWbsTemplateRequest importRequest(List<FinanceWbsTemplateRowDto> rows, boolean replace) {
        ImportFinanceWbsTemplateRequest request = new ImportFinanceWbsTemplateRequest();
        request.setRows(rows);
        request.setReplaceExisting(replace);
        return request;
    }

    private List<FinanceWbsTemplateRowDto> settingsRows() {
        return new ArrayList<>(settingsService.getSettings(project.getId()).getTemplateRows());
    }

    private void save(List<FinanceWbsTemplateRowDto> rows) {
        for (int i = 0; i < rows.size(); i++) rows.get(i).setSortOrder(i + 1);
        SaveFinanceSettingsRequest request = new SaveFinanceSettingsRequest();
        request.setDefaultHourlyRate(new BigDecimal("65"));
        request.setTemplateRows(rows);
        settingsService.saveSettings(project.getId(), request);
    }

    private void setValues(String code, int budget, int actual, int commitment) {
        financeService.importEntries(project.getId(), List.of(values(code, budget, actual, commitment)), true);
    }

    private UpsertFinanceEntryRequest values(String code, int budget, int actual, int commitment) {
        UpsertFinanceEntryRequest request = new UpsertFinanceEntryRequest();
        request.setWbsCode(code);
        request.setBudget(BigDecimal.valueOf(budget));
        request.setActualCost(BigDecimal.valueOf(actual));
        request.setCommitment(BigDecimal.valueOf(commitment));
        return request;
    }

    private void forecast(String code, String period, int amount) {
        forecastService.upsertForecast(project.getId(), forecastRequest(ids().get(code), code, period, amount));
    }

    private UpsertForecastEntryRequest forecastRequest(Long id, String code, String period, int amount) {
        UpsertForecastEntryRequest request = new UpsertForecastEntryRequest();
        request.setFinanceEntryId(id);
        request.setWbsCode(code);
        request.setPeriodKey(period);
        request.setAmount(BigDecimal.valueOf(amount));
        return request;
    }

    private void assertTotals(BigDecimal elettrico, BigDecimal design, BigDecimal total) {
        Map<String, FinanceEntryDto> finance = finance();
        assertEquals(0, elettrico.compareTo(finance.get(ELETTRICO).getForecast()), "DESIGN Elettrico");
        assertEquals(0, design.compareTo(finance.get(DESIGN).getForecast()), "DESIGN");
        assertEquals(0, total.compareTo(finance.get(TOTAL).getForecast()), "project total");
        Map<String, ForecastRowDto> grid = forecastRows();
        assertEquals(0, elettrico.compareTo(grid.get(ELETTRICO).getRemainingForecastCost()), "forecast DESIGN Elettrico");
        assertEquals(0, design.compareTo(grid.get(DESIGN).getRemainingForecastCost()), "forecast DESIGN");
        assertEquals(0, total.compareTo(grid.get(TOTAL).getRemainingForecastCost()), "forecast project total");
    }

    private Map<String, FinanceEntryDto> finance() {
        return financeService.getProjectFinance(project.getId()).stream()
                .collect(Collectors.toMap(FinanceEntryDto::getWbsCode, Function.identity()));
    }

    private List<ForecastRowDto> forecastGrid() {
        return forecastService.getForecastGrid(project.getId(), 12, "week");
    }

    private Map<String, ForecastRowDto> forecastRows() {
        return forecastGrid().stream().collect(Collectors.toMap(ForecastRowDto::getWbsCode, Function.identity()));
    }

    private Map<String, Long> ids() {
        return finance().values().stream().collect(Collectors.toMap(FinanceEntryDto::getWbsCode, FinanceEntryDto::getId));
    }

    private BigDecimal cell(ForecastRowDto row, String period) {
        return row.getPeriods().stream().filter(cell -> cell.getPeriodKey().equals(period)).findFirst().orElseThrow().getAmount();
    }

    private void refresh() {
        entityManager.flush();
        entityManager.clear();
    }

    private ResponseStatusException assertConflict(Executable call) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, call);
        assertEquals(HttpStatus.CONFLICT.value(), error.getStatusCode().value());
        return error;
    }

    private void assertDenied(Executable call) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, call);
        assertEquals("Project not accessible", error.getMessage());
    }

    private static BigDecimal money(int value) {
        return BigDecimal.valueOf(value);
    }

    private static void assertMoney(int expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    private Organisation organisation(String name, String code) {
        Organisation organisation = new Organisation();
        organisation.setName(name);
        organisation.setCode(code);
        return organisationRepository.save(organisation);
    }

    private Project project(String code, Organisation organisation, Long managerId) {
        Project project = new Project();
        project.setCode(code);
        project.setName("Project " + code);
        project.setOrganisation(organisation);
        project.setProjectManagerId(managerId);
        return projectRepository.save(project);
    }

    private void authenticate(Organisation organisation, Long userId, String role) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user" + userId + "@example.com", null,
                List.of(new SimpleGrantedAuthority(role), new SimpleGrantedAuthority("ROLE_" + role)));
        Map<String, Object> details = new HashMap<>();
        details.put("userId", userId);
        details.put("organisationId", organisation.getId());
        token.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
