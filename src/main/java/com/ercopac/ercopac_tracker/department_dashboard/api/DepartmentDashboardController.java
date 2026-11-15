package com.ercopac.ercopac_tracker.department_dashboard.api;

import com.ercopac.ercopac_tracker.department.service.DepartmentHolidayService;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentContextDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentHolidayDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentManagerDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentSupplierDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.MyDepartmentResponseDto;
import com.ercopac.ercopac_tracker.department_dashboard.request.CreateDepartmentHolidayRequest;
import com.ercopac.ercopac_tracker.department_dashboard.request.DepartmentOverviewQuery;
import com.ercopac.ercopac_tracker.department_dashboard.service.DepartmentDashboardService;
import com.ercopac.ercopac_tracker.department_dashboard.service.DepartmentWorkspaceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department-dashboard")
@CrossOrigin(origins = "*")
public class DepartmentDashboardController {

    private static final String DEPARTMENT_READ =
            "hasAnyAuthority('PROJECT_MANAGER', 'ROLE_PROJECT_MANAGER', 'PROJECT_MANAGER_LEAD', 'ROLE_PROJECT_MANAGER_LEAD', " +
                    "'DEPARTMENT_MANAGER', 'ROLE_DEPARTMENT_MANAGER') or " +
                    "@permissionChecker.canRead(authentication, " +
                    "T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).DEPARTMENT_DASHBOARD)";

    private static final String DEPARTMENT_WRITE =
            "@permissionChecker.canWrite(authentication, T(com.ercopac.ercopac_tracker.platform_permissions.domain.PermissionModule).DEPARTMENT_DASHBOARD)";

    private final DepartmentDashboardService departmentDashboardService;
    private final DepartmentHolidayService departmentHolidayService;
    private final DepartmentWorkspaceService departmentWorkspaceService;

    public DepartmentDashboardController(
            DepartmentDashboardService departmentDashboardService,
            DepartmentHolidayService departmentHolidayService,
            DepartmentWorkspaceService departmentWorkspaceService
    ) {
        this.departmentDashboardService = departmentDashboardService;
        this.departmentHolidayService = departmentHolidayService;
        this.departmentWorkspaceService = departmentWorkspaceService;
    }

    /** Department header (name, chief, resource types in scope) and what the current user may change. */
    @GetMapping("/context")
    @PreAuthorize(DEPARTMENT_READ)
    public DepartmentContextDto getContext(@RequestParam(required = false) Long managerId) {
        return departmentWorkspaceService.getContext(managerId);
    }

    /** Active suppliers linked to the department's resource types. */
    @GetMapping("/suppliers")
    @PreAuthorize(DEPARTMENT_READ)
    public List<DepartmentSupplierDto> getSuppliers(@RequestParam(required = false) Long managerId) {
        return departmentWorkspaceService.getSuppliers(managerId);
    }

    @GetMapping("/managers")
    @PreAuthorize(DEPARTMENT_READ)
    public List<DepartmentManagerDto> getManagers() {
        return departmentDashboardService.getManagers();
    }

    @GetMapping("/overview")
    @PreAuthorize(DEPARTMENT_READ)
    public MyDepartmentResponseDto getOverview(
            @RequestParam(required = false) Long managerId,
            @RequestParam(defaultValue = "week") String timelineView,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "8") int span
    ) {
        return departmentDashboardService.getOverview(
                new DepartmentOverviewQuery(managerId, timelineView, offset, span)
        );
    }

    @PostMapping("/holidays")
    @PreAuthorize(DEPARTMENT_WRITE)
    public DepartmentHolidayDto createHoliday(@RequestBody CreateDepartmentHolidayRequest request) {
        return departmentHolidayService.createHoliday(request);
    }

    @DeleteMapping("/holidays/{holidayId}")
    @PreAuthorize(DEPARTMENT_WRITE)
    public void deleteHoliday(@PathVariable Long holidayId) {
        departmentHolidayService.deleteHoliday(holidayId);
    }

    @GetMapping("/overview-by-department")
    @PreAuthorize(DEPARTMENT_READ)
    public MyDepartmentResponseDto getOverviewByDepartment(
            @RequestParam String departmentCode,
            @RequestParam(defaultValue = "week") String timelineView,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "52") int span
    ) {
        return departmentDashboardService.getOverviewByDepartment(
                departmentCode,
                timelineView,
                offset,
                span
        );
    }

    @GetMapping("/departments")
    @PreAuthorize(DEPARTMENT_READ)
    public List<String> getDepartments() {
        return departmentDashboardService.getDepartments();
    }
}
