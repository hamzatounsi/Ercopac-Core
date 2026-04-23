package com.ercopac.ercopac_tracker.department_dashboard.api;

import com.ercopac.ercopac_tracker.department.service.DepartmentHolidayService;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentHolidayDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentManagerDto;
import com.ercopac.ercopac_tracker.department_dashboard.dto.MyDepartmentResponseDto;
import com.ercopac.ercopac_tracker.department_dashboard.request.CreateDepartmentHolidayRequest;
import com.ercopac.ercopac_tracker.department_dashboard.request.DepartmentOverviewQuery;
import com.ercopac.ercopac_tracker.department_dashboard.service.DepartmentDashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department-dashboard")
@CrossOrigin(origins = "*")
public class DepartmentDashboardController {

    private final DepartmentDashboardService departmentDashboardService;
    private final DepartmentHolidayService departmentHolidayService;

    public DepartmentDashboardController(DepartmentDashboardService departmentDashboardService,
                                         DepartmentHolidayService departmentHolidayService) {
        this.departmentDashboardService = departmentDashboardService;
        this.departmentHolidayService = departmentHolidayService;
    }

    @GetMapping("/managers")
    public List<DepartmentManagerDto> getManagers() {
        return departmentDashboardService.getManagers();
    }
    
    @GetMapping("/overview")
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
    public DepartmentHolidayDto createHoliday(@RequestBody CreateDepartmentHolidayRequest request) {
        return departmentHolidayService.createHoliday(request);
    }

    @DeleteMapping("/holidays/{holidayId}")
    public void deleteHoliday(@PathVariable Long holidayId) {
        departmentHolidayService.deleteHoliday(holidayId);
    }
}