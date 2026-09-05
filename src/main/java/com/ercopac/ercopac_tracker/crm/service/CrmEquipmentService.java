package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmEquipmentType;
import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunityEquipment;
import com.ercopac.ercopac_tracker.crm.domain.CrmReportSchedule;
import com.ercopac.ercopac_tracker.crm.dto.CrmEquipmentReportDto;
import com.ercopac.ercopac_tracker.crm.dto.CrmEquipmentTypeDto;
import com.ercopac.ercopac_tracker.crm.dto.CrmOpportunityEquipmentDto;
import com.ercopac.ercopac_tracker.crm.dto.CrmReportScheduleDto;
import com.ercopac.ercopac_tracker.crm.repository.CrmEquipmentTypeRepository;
import com.ercopac.ercopac_tracker.crm.repository.CrmOpportunityEquipmentRepository;
import com.ercopac.ercopac_tracker.crm.repository.CrmOpportunityRepository;
import com.ercopac.ercopac_tracker.crm.repository.CrmReportScheduleRepository;
import com.ercopac.ercopac_tracker.notifications.service.ProjectumMailService;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CrmEquipmentService {
    private static final Logger log = LoggerFactory.getLogger(CrmEquipmentService.class);
    private static final Set<String> REPORT_TYPES = Set.of(
            "WORLD_MAP", "BY_COUNTRY", "TIMELINE", "VALUE_SPLIT", "ERCOPAC_TF",
            "ERCOPAC_RESALE", "EXPECTED_REVENUE", "MONTHLY_OVERVIEW", "CS_PROJECTS",
            "BP_PROJECTS", "EQUIPMENT_OVERVIEW", "EQUIPMENT_SHIPMENT_ON_TIME");
    private static final Set<String> FREQUENCIES = Set.of("DAILY", "WEEKLY", "BIWEEKLY", "MONTHLY", "QUARTERLY");

    private final CrmEquipmentTypeRepository types;
    private final CrmOpportunityEquipmentRepository equipment;
    private final CrmOpportunityRepository opportunities;
    private final CrmReportScheduleRepository schedules;
    private final OrganisationRepository organisations;
    private final SecurityUtils security;
    private final ProjectumMailService mail;

    public CrmEquipmentService(CrmEquipmentTypeRepository types,
                               CrmOpportunityEquipmentRepository equipment,
                               CrmOpportunityRepository opportunities,
                               CrmReportScheduleRepository schedules,
                               OrganisationRepository organisations,
                               SecurityUtils security,
                               ProjectumMailService mail) {
        this.types = types;
        this.equipment = equipment;
        this.opportunities = opportunities;
        this.schedules = schedules;
        this.organisations = organisations;
        this.security = security;
        this.mail = mail;
    }

    private Long tenant(Long id) {
        Long actual = security.getCurrentOrganisationId();
        if (!Objects.equals(actual, id) && !security.isPlatformUser()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Organisation access denied.");
        }
        return id;
    }

    private Organisation org(Long id) {
        tenant(id);
        return organisations.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found."));
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private CrmEquipmentType type(Long orgId, Long id) {
        return types.findByIdAndOrganisation_Id(id, orgId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment type not found."));
    }

    public List<CrmEquipmentTypeDto> types(Long orgId, boolean inactive) {
        Long tenantId = tenant(orgId);
        return (inactive ? types.findByOrganisation_IdOrderByNameAsc(tenantId)
                : types.findByOrganisation_IdAndActiveTrueOrderByNameAsc(tenantId)).stream().map(this::dto).toList();
    }

    public CrmEquipmentTypeDto saveType(Long orgId, Long id, CrmEquipmentTypeDto dto) {
        CrmEquipmentType entity = id == null ? new CrmEquipmentType() : type(tenant(orgId), id);
        if (dto.code() == null || dto.code().isBlank() || dto.name() == null || dto.name().isBlank()) {
            throw bad("Equipment code and name are required.");
        }
        entity.setOrganisation(org(orgId));
        entity.setCode(dto.code().trim().toUpperCase(Locale.ROOT));
        entity.setName(dto.name().trim());
        entity.setActive(dto.active());
        try {
            return dto(types.save(entity));
        } catch (Exception exception) {
            throw bad("Equipment code and name must be unique in this organisation.");
        }
    }

    public void deleteType(Long orgId, Long id) {
        CrmEquipmentType entity = type(tenant(orgId), id);
        entity.setActive(false);
        types.save(entity);
    }

    private CrmEquipmentTypeDto dto(CrmEquipmentType entity) {
        return new CrmEquipmentTypeDto(entity.getId(), entity.getCode(), entity.getName(), entity.isActive());
    }

    public List<CrmOpportunityEquipmentDto> opportunityEquipment(Long orgId, Long opportunityId) {
        tenant(orgId);
        requireOpportunity(orgId, opportunityId);
        return equipment.findByOpportunity_IdAndOrganisation_IdOrderById(opportunityId, orgId).stream()
                .map(item -> new CrmOpportunityEquipmentDto(item.getEquipmentType().getId(),
                        item.getEquipmentType().getCode(), item.getEquipmentType().getName(), item.getQuantity()))
                .toList();
    }

    public List<CrmOpportunityEquipmentDto> replaceOpportunityEquipment(Long orgId,
                                                                         Long opportunityId,
                                                                         List<CrmOpportunityEquipmentDto> rows) {
        tenant(orgId);
        CrmOpportunity opportunity = requireOpportunity(orgId, opportunityId);
        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (CrmOpportunityEquipmentDto row : Optional.ofNullable(rows).orElse(List.of())) {
            if (row.equipmentTypeId() == null || row.quantity() == null || row.quantity() < 1) {
                throw bad("Every equipment item needs a positive quantity.");
            }
            merged.merge(row.equipmentTypeId(), row.quantity(), Math::addExact);
        }
        equipment.deleteByOpportunity_IdAndOrganisation_Id(opportunityId, orgId);
        for (Map.Entry<Long, Integer> row : merged.entrySet()) {
            CrmOpportunityEquipment item = new CrmOpportunityEquipment();
            item.setOrganisation(opportunity.getOrganisation());
            item.setOpportunity(opportunity);
            item.setEquipmentType(type(orgId, row.getKey()));
            item.setQuantity(row.getValue());
            equipment.save(item);
        }
        return opportunityEquipment(orgId, opportunityId);
    }

    private CrmOpportunity requireOpportunity(Long orgId, Long id) {
        return opportunities.findByIdAndOrganisation_Id(id, orgId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found."));
    }

    public CrmEquipmentReportDto report(Long orgId, String stage, String typeFilter) {
        tenant(orgId);
        return buildReport(orgId, stage, typeFilter);
    }

    private CrmEquipmentReportDto buildReport(Long orgId, String stage, String typeFilter) {
        Set<Long> allowed = opportunities.findByOrganisation_IdOrderByCreatedAtDesc(orgId).stream()
                .filter(opportunity -> matchesStage(opportunity, stage) && matchesType(opportunity, typeFilter))
                .map(CrmOpportunity::getId)
                .collect(Collectors.toSet());
        List<CrmOpportunityEquipment> rows = equipment.findByOrganisation_Id(orgId).stream()
                .filter(item -> allowed.contains(item.getOpportunity().getId())).toList();
        Map<String, List<CrmOpportunityEquipment>> grouped = rows.stream().collect(Collectors.groupingBy(
                item -> item.getEquipmentType().getName(), LinkedHashMap::new, Collectors.toList()));
        List<CrmEquipmentReportDto.EquipmentTotal> totals = grouped.values().stream()
                .map(values -> new CrmEquipmentReportDto.EquipmentTotal(
                        values.get(0).getEquipmentType().getName(), values.get(0).getEquipmentType().getCode(),
                        values.stream().mapToLong(CrmOpportunityEquipment::getQuantity).sum(),
                        values.stream().map(item -> item.getOpportunity().getId()).distinct().count()))
                .sorted(Comparator.comparingLong(CrmEquipmentReportDto.EquipmentTotal::quantity).reversed()).toList();
        List<CrmEquipmentReportDto.ShipmentRow> shipments = rows.stream().map(item -> {
            CrmOpportunity opportunity = item.getOpportunity();
            String status = opportunity.getShipmentDate() == null ? "Pending"
                    : opportunity.getClosingDate() != null && !opportunity.getShipmentDate().isAfter(opportunity.getClosingDate())
                    ? "On Time" : "Late";
            return new CrmEquipmentReportDto.ShipmentRow(opportunity.getName(), opportunity.getAccountName(),
                    opportunity.getStage() == null ? null : opportunity.getStage().getName(),
                    opportunity.getOwner() == null ? null : opportunity.getOwner().getFullName(),
                    opportunity.getClosingDate(), opportunity.getShipmentDate(), status);
        }).distinct().toList();
        List<CrmEquipmentReportDto.EquipmentDetail> details = rows.stream().map(item -> {
            CrmOpportunity opportunity = item.getOpportunity();
            return new CrmEquipmentReportDto.EquipmentDetail(opportunity.getId(), opportunity.getName(),
                    opportunity.getAccountName(), opportunity.getStage() == null ? null : opportunity.getStage().getName(),
                    opportunity.getOpportunityType(), opportunity.getOwner() == null ? null : opportunity.getOwner().getFullName(),
                    item.getEquipmentType().getName(), item.getEquipmentType().getCode(), item.getQuantity(),
                    opportunity.getShipmentDate());
        }).toList();
        return new CrmEquipmentReportDto(totals, shipments, details);
    }

    private boolean matchesStage(CrmOpportunity opportunity, String stage) {
        return stage == null || stage.isBlank() || "all".equalsIgnoreCase(stage)
                || opportunity.getStage() != null && stage.equals(opportunity.getStage().getName());
    }

    private boolean matchesType(CrmOpportunity opportunity, String typeFilter) {
        return typeFilter == null || typeFilter.isBlank() || "all".equalsIgnoreCase(typeFilter)
                || typeFilter.equalsIgnoreCase(opportunity.getOpportunityType());
    }

    public List<CrmReportScheduleDto> schedules(Long orgId) {
        return schedules.findByOrganisation_IdOrderByNextRunAtAsc(tenant(orgId)).stream().map(this::scheduleDto).toList();
    }

    public CrmReportScheduleDto saveSchedule(Long orgId, Long id, CrmReportScheduleDto dto) {
        CrmReportSchedule schedule = id == null ? new CrmReportSchedule()
                : schedules.findByIdAndOrganisation_Id(id, tenant(orgId)).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found."));
        String reportType = dto.reportType() == null ? "" : dto.reportType().trim().toUpperCase(Locale.ROOT);
        String typeFilter = dto.typeFilter() == null ? "ALL" : dto.typeFilter().trim().toUpperCase(Locale.ROOT);
        String frequency = dto.frequency() == null ? "" : dto.frequency().trim().toUpperCase(Locale.ROOT);
        if (!REPORT_TYPES.contains(reportType) || !Set.of("ALL", "BP", "CS").contains(typeFilter)
                || dto.recipients() == null || dto.recipients().isBlank() || !FREQUENCIES.contains(frequency)) {
            throw bad("Invalid report schedule.");
        }
        schedule.setOrganisation(org(orgId));
        schedule.setReportType(reportType);
        schedule.setTypeFilter(typeFilter);
        schedule.setRecipients(dto.recipients().trim());
        schedule.setFrequency(frequency);
        schedule.setActive(dto.active());
        schedule.setNextRunAt(dto.nextRunAt() == null ? LocalDateTime.now().plusMinutes(1) : dto.nextRunAt());
        return scheduleDto(schedules.save(schedule));
    }

    public void deleteSchedule(Long orgId, Long id) {
        schedules.delete(schedules.findByIdAndOrganisation_Id(id, tenant(orgId)).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found.")));
    }

    private CrmReportScheduleDto scheduleDto(CrmReportSchedule schedule) {
        return new CrmReportScheduleDto(schedule.getId(), schedule.getReportType(),
                Optional.ofNullable(schedule.getTypeFilter()).orElse("ALL"), schedule.getRecipients(),
                schedule.getFrequency(), schedule.isActive(), schedule.getLastSentAt(), schedule.getNextRunAt());
    }

    @Scheduled(fixedDelayString = "${crm.report-schedule.poll-ms:60000}")
    public void runDueSchedules() {
        for (CrmReportSchedule schedule : schedules.findByActiveTrueAndNextRunAtLessThanEqual(LocalDateTime.now())) {
            try {
                String subject = "Projectum CRM: " + schedule.getReportType().replace('_', ' ');
                String body = scheduledReportBody(schedule);
                for (String recipient : schedule.getRecipients().split("[,;\\s]+")) {
                    if (!recipient.isBlank()) mail.sendText(recipient, subject, body);
                }
                schedule.setLastSentAt(LocalDateTime.now());
                schedule.setNextRunAt(next(schedule.getNextRunAt(), schedule.getFrequency()));
                schedules.save(schedule);
                log.info("Sent CRM schedule {}", schedule.getId());
            } catch (Exception exception) {
                log.error("CRM schedule {} failed", schedule.getId(), exception);
                schedule.setNextRunAt(LocalDateTime.now().plusMinutes(15));
                schedules.save(schedule);
            }
        }
    }

    private String scheduledReportBody(CrmReportSchedule schedule) {
        Long orgId = schedule.getOrganisation().getId();
        String storedType = Optional.ofNullable(schedule.getTypeFilter()).orElse("ALL");
        String effectiveType = switch (schedule.getReportType()) {
            case "CS_PROJECTS" -> "CS";
            case "BP_PROJECTS" -> "BP";
            default -> "ALL".equals(storedType) ? null : storedType;
        };
        if (schedule.getReportType().startsWith("EQUIPMENT_")) {
            CrmEquipmentReportDto report = buildReport(orgId, null, effectiveType);
            long units = report.totals().stream().mapToLong(CrmEquipmentReportDto.EquipmentTotal::quantity).sum();
            long tracked = report.details().stream().map(CrmEquipmentReportDto.EquipmentDetail::opportunityId).distinct().count();
            return "Scheduled CRM report\n\nReport: " + schedule.getReportType().replace('_', ' ')
                    + "\nType: " + storedType + "\nEquipment types: " + report.totals().size()
                    + "\nOpportunities tracked: " + tracked + "\nTotal units: " + units;
        }
        List<CrmOpportunity> reportOpportunities = opportunities.findByOrganisation_IdOrderByCreatedAtDesc(orgId).stream()
                .filter(opportunity -> matchesType(opportunity, effectiveType)).toList();
        BigDecimal total = reportOpportunities.stream().map(CrmOpportunity::getValue)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weighted = reportOpportunities.stream().map(opportunity ->
                        Optional.ofNullable(opportunity.getValue()).orElse(BigDecimal.ZERO)
                                .multiply(BigDecimal.valueOf(Optional.ofNullable(opportunity.getProbability()).orElse(0)))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long countries = reportOpportunities.stream().map(opportunity -> opportunity.getAccount() == null
                        ? null : opportunity.getAccount().getCountry())
                .filter(value -> value != null && !value.isBlank()).collect(Collectors.toCollection(LinkedHashSet::new)).size();
        return "Scheduled CRM report\n\nReport: " + schedule.getReportType().replace('_', ' ')
                + "\nType: " + (effectiveType == null ? "ALL" : effectiveType)
                + "\nOpportunities: " + reportOpportunities.size() + "\nCountries: " + countries
                + "\nPipeline value: " + total + "\nExpected revenue: " + weighted;
    }

    private LocalDateTime next(LocalDateTime from, String frequency) {
        return switch (frequency) {
            case "DAILY" -> from.plusDays(1);
            case "WEEKLY" -> from.plusWeeks(1);
            case "BIWEEKLY" -> from.plusWeeks(2);
            case "QUARTERLY" -> from.plusMonths(3);
            default -> from.plusMonths(1);
        };
    }
}
