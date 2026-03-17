package com.ercopac.ercopac_tracker.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoleTestController {

    @GetMapping("/gm/hello")
    public String gmHello() {
        return "Hello GENERAL_MANAGER ✅";
    }

    @GetMapping("/department/hello")
    public String departmentHello() {
        return "Hello DEPARTMENT_MANAGER ✅";
    }

    @GetMapping("/employee/hello")
    public String employeeHello() {
        return "Hello EMPLOYEE ✅";
    }
}
