package com.ercopac.ercopac_tracker.department_dashboard.service;

import com.ercopac.ercopac_tracker.department.service.DepartmentHolidayService;
import com.ercopac.ercopac_tracker.department_dashboard.dto.*;
import com.ercopac.ercopac_tracker.department_dashboard.request.DepartmentOverviewQuery;
import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskResourceAssignment;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskResourceAssignmentRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DepartmentDashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final DepartmentHolidayService departmentHolidayService;
    private final ProjectTaskRepository projectTaskRepository;
    private final TaskResourceAssignmentRepository taskResourceAssignmentRepository;

    public DepartmentDashboardService(UserRepository userRepository,
                                      ProjectRepository projectRepository,
                                      DepartmentHolidayService departmentHolidayService,
                                      ProjectTaskRepository projectTaskRepository,
                                      TaskResourceAssignmentRepository taskResourceAssignmentRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.departmentHolidayService = departmentHolidayService;
        this.projectTaskRepository = projectTaskRepository;
        this.taskResourceAssignmentRepository = taskResourceAssignmentRepository;
    }

    public List<DepartmentManagerDto> getManagers() {
        Long currentOrgId = getCurrentOrganisationIdOrThrow();

        return userRepository.findByOrganisation_IdAndRoleOrderByFullNameAsc(
                        currentOrgId,
                        Role.DEPARTMENT_MANAGER
                )
                .stream()
                .map(this::toManagerDto)
                .toList();
    }

    public MyDepartmentResponseDto getOverview(DepartmentOverviewQuery query) {
        String timelineView = normalizeTimelineView(query.timelineView());
        int offset = query.offset();
        int span = query.span() <= 0 ? ("day".equals(timelineView) ? 28 : 16) : query.span();

        Long currentOrgId = getCurrentOrganisationIdOrThrow();
        AppUser currentUser = getCurrentUserOrThrow();

        AppUser manager;

        if (currentUser.getRole() == Role.DEPARTMENT_MANAGER) {
            manager = currentUser;
        } else {
            if (query == null || query.managerId() == null) {
                throw new IllegalArgumentException("managerId is required for this user.");
            }

            manager = userRepository.findByIdAndOrganisation_Id(query.managerId(), currentOrgId)
                    .orElseThrow(() -> new EntityNotFoundException("Department manager not found with id: " + query.managerId()));

            if (manager.getRole() != Role.DEPARTMENT_MANAGER) {
                throw new AccessDeniedException("Selected user is not a department manager.");
            }
        }

        String departmentCode = manager.getDepartmentCode();

        List<DepartmentMemberDto> realMembers = userRepository
                .findByOrganisation_IdAndDepartmentCodeOrderByFullNameAsc(currentOrgId, departmentCode)
                .stream()
                .filter(AppUser::isActive)
                .map(this::toMemberDto)
                .toList();

        List<DepartmentMemberDto> members = new ArrayList<>(realMembers);

        long genericBaseId = -1L;
        for (String resourceType : getDepartmentResourceTypes(departmentCode)) {
            members.add(new DepartmentMemberDto(
                    genericBaseId--,
                    "G-" + resourceType,
                    null,
                    null,
                    departmentCode,
                    resourceType,
                    "GENERIC",
                    null,
                    true,
                    8,
                    5,
                    List.of(1, 2, 3, 4, 5),
                    "#6b7280"
            ));
        }

        List<Long> realMemberIds = realMembers.stream()
                .map(DepartmentMemberDto::id)
                .filter(Objects::nonNull)
                .toList();

        List<DepartmentHolidayDto> holidays = departmentHolidayService.findHolidays(currentOrgId, realMemberIds);
        List<DepartmentTimelineColumnDto> timelineColumns = buildTimelineColumns(timelineView, offset, span);

        List<ProjectTask> departmentTasks = loadDepartmentTasks(currentOrgId, departmentCode, realMemberIds);
        Map<Long, List<TaskResourceAssignment>> assignmentsByTaskId = loadAssignmentsByTaskId(departmentTasks);

        List<DepartmentResourceRowDto> resourceRows = buildResourceRows(
                members,
                holidays,
                departmentTasks,
                assignmentsByTaskId
        );

        List<DepartmentProjectBlockDto> projectBlocks = buildProjectBlocks(
                currentOrgId,
                departmentTasks
        );

        List<DepartmentWeeklyStatDto> weeklyStats = buildWeeklyStats(
                departmentTasks,
                timelineColumns,
                timelineView
        );

        DepartmentOverviewDto overview = new DepartmentOverviewDto(
                toManagerDto(manager),
                departmentCode,
                members,
                holidays,
                timelineColumns,
                resourceRows,
                projectBlocks,
                weeklyStats
        );

        return new MyDepartmentResponseDto(overview);
    }

    private List<DepartmentTimelineColumnDto> buildTimelineColumns(String timelineView, int offset, int span) {
        LocalDate today = LocalDate.now();
        List<DepartmentTimelineColumnDto> columns = new ArrayList<>();

        if ("day".equals(timelineView)) {
            LocalDate start = today.plusDays(offset);
            for (int i = 0; i < span; i++) {
                LocalDate date = start.plusDays(i);
                columns.add(new DepartmentTimelineColumnDto(
                        date,
                        date,
                        String.valueOf(date.getDayOfMonth()),
                        date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                        isWeekend(date),
                        date.equals(today)
                ));
            }
            return columns;
        }

        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY).plusWeeks(offset);
        for (int i = 0; i < span; i++) {
            LocalDate start = startOfWeek.plusWeeks(i);
            LocalDate end = start.plusDays(6);
            columns.add(new DepartmentTimelineColumnDto(
                    start,
                    end,
                    "W" + start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                    start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + start.getYear(),
                    false,
                    !today.isBefore(start) && !today.isAfter(end)
            ));
        }

        return columns;
    }

    private List<DepartmentResourceRowDto> buildResourceRows(List<DepartmentMemberDto> members,
                                                         List<DepartmentHolidayDto> holidays,
                                                         List<ProjectTask> tasks,
                                                         Map<Long, List<TaskResourceAssignment>> assignmentsByTaskId) {
    Map<Long, List<DepartmentHolidayDto>> holidaysByMember = holidays.stream()
            .collect(Collectors.groupingBy(DepartmentHolidayDto::memberId));

    Map<Long, DepartmentMemberDto> membersById = members.stream()
            .collect(Collectors.toMap(DepartmentMemberDto::id, m -> m));

    Map<String, DepartmentMemberDto> genericMembersByResourceType = members.stream()
            .filter(m -> m.id() != null && m.id() < 0)
            .filter(m -> m.resourceType() != null && !m.resourceType().isBlank())
            .collect(Collectors.toMap(
                    m -> m.resourceType().toUpperCase(Locale.ROOT),
                    m -> m,
                    (a, b) -> a
            ));

    Map<Long, List<DepartmentTimelineItemDto>> itemsByMemberId = new LinkedHashMap<>();
    for (DepartmentMemberDto member : members) {
        itemsByMemberId.put(member.id(), new ArrayList<>());
    }

    for (ProjectTask task : tasks) {
        LocalDate start = firstNonNullDate(task.getActualStart(), task.getPlannedStart(), task.getBaselineStart());
        LocalDate end = firstNonNullDate(task.getActualEnd(), task.getPlannedEnd(), task.getBaselineEnd());

        if (start == null || end == null) {
            continue;
        }

        Project project = projectRepository.findById(task.getProjectId()).orElse(null);

        Set<Long> targetMemberIds = new LinkedHashSet<>();

        if (task.getAssignedUser() != null && membersById.containsKey(task.getAssignedUser().getId())) {
            targetMemberIds.add(task.getAssignedUser().getId());
        }

        List<TaskResourceAssignment> assignments = assignmentsByTaskId.getOrDefault(task.getId(), Collections.emptyList());
        for (TaskResourceAssignment assignment : assignments) {
            if (assignment.getAssignedUser() != null && membersById.containsKey(assignment.getAssignedUser().getId())) {
                targetMemberIds.add(assignment.getAssignedUser().getId());
            }
        }

        if (!targetMemberIds.isEmpty()) {
            for (Long memberId : targetMemberIds) {
                DepartmentMemberDto member = membersById.get(memberId);

                itemsByMemberId.get(memberId).add(new DepartmentTimelineItemDto(
                        task.getProjectId(),
                        project != null ? project.getCode() : null,
                        project != null ? project.getName() : null,
                        task.getId(),
                        task.getName(),
                        task.getTaskType(),
                        start,
                        end,
                        safeProgress(task.getPercentComplete()),
                        task.getDepartmentCode(),
                        task.getResourceType(),
                        false,
                        false,
                        null,
                        member.internal()
                ));
            }
        } else {
            String resourceType = task.getResourceType();

            if ((resourceType == null || resourceType.isBlank()) && !assignments.isEmpty()) {
                resourceType = assignments.stream()
                        .map(TaskResourceAssignment::getResourceType)
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .findFirst()
                        .orElse(null);
            }

            if (resourceType != null) {
                DepartmentMemberDto genericMember =
                        genericMembersByResourceType.get(resourceType.toUpperCase(Locale.ROOT));

                if (genericMember != null) {
                    itemsByMemberId.get(genericMember.id()).add(new DepartmentTimelineItemDto(
                            task.getProjectId(),
                            project != null ? project.getCode() : null,
                            project != null ? project.getName() : null,
                            task.getId(),
                            task.getName(),
                            task.getTaskType(),
                            start,
                            end,
                            safeProgress(task.getPercentComplete()),
                            task.getDepartmentCode(),
                            resourceType,
                            false,
                            false,
                            null,
                            genericMember.internal()
                    ));
                }
            }
        }
    }

    for (DepartmentHolidayDto holiday : holidays) {
        DepartmentMemberDto member = membersById.get(holiday.memberId());
        if (member == null) {
            continue;
        }

        itemsByMemberId.get(holiday.memberId()).add(new DepartmentTimelineItemDto(
                null,
                null,
                null,
                null,
                "Holiday",
                "holiday",
                holiday.fromDate(),
                holiday.toDate(),
                0,
                member.departmentCode(),
                member.resourceType(),
                false,
                true,
                holiday.note(),
                member.internal()
        ));
    }

    List<DepartmentResourceRowDto> rows = new ArrayList<>();

    for (DepartmentMemberDto member : members) {
        List<DepartmentTimelineItemDto> items = itemsByMemberId.getOrDefault(member.id(), new ArrayList<>());
        items.sort(Comparator.comparing(DepartmentTimelineItemDto::startDate, Comparator.nullsLast(LocalDate::compareTo)));
        rows.add(new DepartmentResourceRowDto(member, items));
    }

    return rows;
}
    private List<DepartmentProjectBlockDto> buildProjectBlocks(Long organisationId,
                                                               List<ProjectTask> tasks) {
        Map<Long, List<ProjectTask>> tasksByProject = tasks.stream()
                .collect(Collectors.groupingBy(ProjectTask::getProjectId, LinkedHashMap::new, Collectors.toList()));

        List<DepartmentProjectBlockDto> blocks = new ArrayList<>();

        for (Map.Entry<Long, List<ProjectTask>> entry : tasksByProject.entrySet()) {
            Long projectId = entry.getKey();

            Project project = projectRepository.findById(projectId)
                    .filter(p -> p.getOrganisation() != null && Objects.equals(p.getOrganisation().getId(), organisationId))
                    .orElse(null);

            if (project == null) {
                continue;
            }

            List<DepartmentActivityRowDto> rows = entry.getValue()
                    .stream()
                    .sorted(Comparator
                            .comparing(ProjectTask::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(ProjectTask::getId, Comparator.nullsLast(Long::compareTo)))
                    .map(task -> new DepartmentActivityRowDto(
                            task.getId(),
                            task.getWbsCode(),
                            task.getName(),
                            task.getTaskType(),
                            task.getDepartmentCode(),
                            task.getBaselineStart(),
                            task.getBaselineEnd(),
                            firstNonNullDate(task.getActualStart(), task.getPlannedStart()),
                            firstNonNullDate(task.getActualEnd(), task.getPlannedEnd()),
                            task.getDurationDays(),
                            safeProgress(task.getPercentComplete()),
                            isSummaryTask(task)
                    ))
                    .toList();

            blocks.add(new DepartmentProjectBlockDto(
                    project.getId(),
                    project.getCode(),
                    project.getName(),
                    project.getProjectPhase(),
                    rows
            ));
        }

        return blocks;
    }

    private List<DepartmentWeeklyStatDto> buildWeeklyStats(List<ProjectTask> tasks,
                                                        List<DepartmentTimelineColumnDto> timelineColumns,
                                                        String timelineView) {
        if (!"week".equals(timelineView) || timelineColumns.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Set<Long>> resourceCountByTypeAndWeek = new HashMap<>();
        Map<String, Integer> plannedHoursByTypeAndWeek = new HashMap<>();

        for (ProjectTask task : tasks) {
            LocalDate start = firstNonNullDate(task.getActualStart(), task.getPlannedStart(), task.getBaselineStart());
            LocalDate end = firstNonNullDate(task.getActualEnd(), task.getPlannedEnd(), task.getBaselineEnd());

            if (start == null || end == null) {
                continue;
            }

            String resourceType = task.getResourceType() != null ? task.getResourceType() : "UNKNOWN";
            Integer taskHours = task.getPlannedHours() != null ? task.getPlannedHours().intValue() : 0;

            List<TaskResourceAssignment> assignments = Collections.emptyList();
            if (task.getProjectId() != null && task.getId() != null) {
                assignments = taskResourceAssignmentRepository.findByProjectIdAndTaskIdOrderByIdAsc(task.getProjectId(), task.getId());
            }

            for (DepartmentTimelineColumnDto col : timelineColumns) {
                if (end.isBefore(col.startDate()) || start.isAfter(col.endDate())) {
                    continue;
                }

                String key = resourceType + "|" + col.startDate();

                if (task.getAssignedUser() != null) {
                    resourceCountByTypeAndWeek
                            .computeIfAbsent(key, k -> new HashSet<>())
                            .add(task.getAssignedUser().getId());
                }

                for (TaskResourceAssignment assignment : assignments) {
                    if (assignment.getAssignedUser() != null) {
                        resourceCountByTypeAndWeek
                                .computeIfAbsent(key, k -> new HashSet<>())
                                .add(assignment.getAssignedUser().getId());
                    }
                }

                plannedHoursByTypeAndWeek.merge(key, taskHours, Integer::sum);
            }
        }

        List<DepartmentWeeklyStatDto> stats = new ArrayList<>();

        for (DepartmentTimelineColumnDto col : timelineColumns) {
            Set<String> resourceTypes = tasks.stream()
                    .map(ProjectTask::getResourceType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            for (String resourceType : resourceTypes) {
                String key = resourceType + "|" + col.startDate();

                stats.add(new DepartmentWeeklyStatDto(
                        col.startDate(),
                        col.endDate(),
                        resourceType,
                        resourceCountByTypeAndWeek.getOrDefault(key, Collections.emptySet()).size(),
                        plannedHoursByTypeAndWeek.getOrDefault(key, 0)
                ));
            }
        }

        return stats;
    }
    private DepartmentManagerDto toManagerDto(AppUser user) {
        return new DepartmentManagerDto(
                safeLong(readProperty(user, "id")),
                safeString(firstNonNull(
                        readProperty(user, "fullName"),
                        readProperty(user, "name"),
                        readProperty(user, "username"),
                        readProperty(user, "email")
                )),
                safeString(readProperty(user, "email")),
                safeString(firstNonNull(
                        readProperty(user, "departmentCode"),
                        readProperty(user, "department"),
                        "GENERAL"
                )),
                safeString(firstNonNull(
                        readProperty(user, "resourceType"),
                        readProperty(user, "restype"),
                        readProperty(user, "role")
                )),
                safeString(readProperty(user, "role"))
        );
    }

    private DepartmentMemberDto toMemberDto(AppUser user) {
        return new DepartmentMemberDto(
                safeLong(readProperty(user, "id")),
                safeString(firstNonNull(
                        readProperty(user, "fullName"),
                        readProperty(user, "name"),
                        readProperty(user, "username"),
                        readProperty(user, "email")
                )),
                safeString(firstNonNull(readProperty(user, "employeeCode"), readProperty(user, "code"))),
                safeString(readProperty(user, "email")),
                safeString(firstNonNull(readProperty(user, "departmentCode"), readProperty(user, "department"), "GENERAL")),
                safeString(firstNonNull(readProperty(user, "resourceType"), readProperty(user, "restype"), "GENERAL")),
                safeString(readProperty(user, "role")),
                safeString(readProperty(user, "seniority")),
                safeBoolean(firstNonNull(readProperty(user, "internalUser"), Boolean.TRUE)),
                safeInteger(firstNonNull(readProperty(user, "hoursPerDay"), readProperty(user, "hpd"), 8)),
                safeInteger(firstNonNull(readProperty(user, "daysPerWeek"), readProperty(user, "dpw"), 5)),
                extractWorkdays(user),
                safeString(readProperty(user, "color"))
        );
    }

    private List<Integer> extractWorkdays(AppUser user) {
        String workdays = user.getWorkdays();

        if (workdays == null || workdays.isBlank()) {
            return List.of(1, 2, 3, 4, 5);
        }

        String normalized = workdays.trim().toUpperCase(Locale.ROOT);

        if ("MON-FRI".equals(normalized)) {
            return List.of(1, 2, 3, 4, 5);
        }
        if ("MON-SAT".equals(normalized)) {
            return List.of(1, 2, 3, 4, 5, 6);
        }
        if ("SUN-THU".equals(normalized)) {
            return List.of(7, 1, 2, 3, 4);
        }

        return List.of(1, 2, 3, 4, 5);
    }

    private String normalizeTimelineView(String value) {
        if (value == null) {
            return "week";
        }
        return "day".equalsIgnoreCase(value) ? "day" : "week";
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private Long getCurrentOrganisationIdOrThrow() {
        Object value = authDetails().get("organisationId");
        if (value == null) {
            throw new AccessDeniedException("Organisation ID not found in authentication details.");
        }
        return Long.valueOf(value.toString());
    }

    private AppUser getCurrentUserOrThrow() {
        Object userIdValue = authDetails().get("userId");
        if (userIdValue == null) {
            throw new AccessDeniedException("User ID not found in authentication details.");
        }

        Long userId = Long.valueOf(userIdValue.toString());

        return userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found."));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> authDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user found.");
        }
        Object details = authentication.getDetails();
        if (!(details instanceof Map<?, ?>)) {
            throw new AccessDeniedException("Authentication details are missing.");
        }
        return (Map<String, Object>) details;
    }

    private Long extractOrganisationId(Object bean) {
        Object organisationId = readProperty(bean, "organisationId");
        if (organisationId != null) {
            return safeLong(organisationId);
        }

        Object organisation = readProperty(bean, "organisation");
        if (organisation != null) {
            return safeLong(readProperty(organisation, "id"));
        }

        return null;
    }

    private Object readProperty(Object bean, String property) {
        if (bean == null || property == null) {
            return null;
        }
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (pd.getName().equals(property)) {
                return wrapper.getPropertyValue(property);
            }
        }
        return null;
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String safeString(Object value) {
        return value == null ? null : value.toString();
    }

    private Long safeLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private Integer safeInteger(Object value) {
        return value == null ? null : Integer.valueOf(value.toString());
    }

    private boolean safeBoolean(Object value) {
        return value != null && Boolean.parseBoolean(value.toString());
    }

   private List<ProjectTask> loadDepartmentTasks(Long organisationId,
                                              String departmentCode,
                                              List<Long> memberIds) {
    Set<Long> memberIdSet = new HashSet<>(memberIds);

    List<ProjectTask> allTasks = projectTaskRepository.findAll()
            .stream()
            .filter(ProjectTask::getActive)
            .filter(task -> {
                if (task.getAssignedUser() != null
                        && task.getAssignedUser().getOrganisation() != null
                        && !Objects.equals(task.getAssignedUser().getOrganisation().getId(), organisationId)) {
                    return false;
                }
                return true;
            })
            .toList();

    List<ProjectTask> result = new ArrayList<>();

    for (ProjectTask task : allTasks) {
        boolean include = false;

        if (task.getAssignedUser() != null && memberIdSet.contains(task.getAssignedUser().getId())) {
            include = true;
        }

        if (!include && departmentCode != null && departmentCode.equals(task.getDepartmentCode())) {
            include = true;
        }

        if (!include && task.getProjectId() != null && task.getId() != null) {
            List<TaskResourceAssignment> assignments =
                    taskResourceAssignmentRepository.findByProjectIdAndTaskIdOrderByIdAsc(task.getProjectId(), task.getId());

            include = assignments.stream()
                    .anyMatch(a -> a.getAssignedUser() != null && memberIdSet.contains(a.getAssignedUser().getId()));
        }

        if (include) {
            result.add(task);
        }
    }

    return result.stream()
            .sorted(Comparator
                    .comparing(ProjectTask::getProjectId, Comparator.nullsLast(Long::compareTo))
                    .thenComparing(ProjectTask::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ProjectTask::getId, Comparator.nullsLast(Long::compareTo)))
            .toList();
}

    private Map<Long, List<TaskResourceAssignment>> loadAssignmentsByTaskId(List<ProjectTask> tasks) {
        Map<Long, List<TaskResourceAssignment>> map = new HashMap<>();

        for (ProjectTask task : tasks) {
            if (task.getProjectId() == null || task.getId() == null) {
                map.put(task.getId(), Collections.emptyList());
                continue;
            }

            List<TaskResourceAssignment> assignments =
                    taskResourceAssignmentRepository.findByProjectIdAndTaskIdOrderByIdAsc(task.getProjectId(), task.getId());

            map.put(task.getId(), assignments);
        }

        return map;
    }

    private LocalDate firstNonNullDate(LocalDate... dates) {
        for (LocalDate date : dates) {
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    private Integer safeProgress(Integer value) {
        if (value == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, value));
    }

    private boolean isSummaryTask(ProjectTask task) {
        if (task.getTaskType() == null) {
            return false;
        }
        String type = task.getTaskType().toUpperCase(Locale.ROOT);
        return type.contains("SUMMARY") || type.equals("SUM");
    }

    private List<String> getDepartmentResourceTypes(String departmentCode) {
        if (departmentCode == null || departmentCode.isBlank()) {
            return Collections.emptyList();
        }

        return switch (departmentCode.toUpperCase(Locale.ROOT)) {
            case "SW" -> List.of("PC", "PLC");
            case "MFC" -> List.of("MFC.M", "MFC.E");
            case "INST" -> List.of("MEC", "ELECT", "INST");
            case "PM" -> List.of("PM");
            case "ME" -> List.of("ME");
            case "CE" -> List.of("CE");
            case "PRC" -> List.of("PRC");
            case "QA" -> List.of("QA");
            case "HSE" -> List.of("HSE");
            case "FIN" -> List.of("FIN");
            case "CS" -> List.of("CS");
            default -> List.of(departmentCode.toUpperCase(Locale.ROOT));
        };
    }
}