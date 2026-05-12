package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.domain.TaskDependency;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskDependencyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class ProjectTaskSeeder implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final TaskDependencyRepository taskDependencyRepository;

    public ProjectTaskSeeder(
            ProjectRepository projectRepository,
            ProjectTaskRepository projectTaskRepository,
            TaskDependencyRepository taskDependencyRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.taskDependencyRepository = taskDependencyRepository;
    }

    @Override
    public void run(String... args) {
        List<Project> projects = projectRepository.findAll();
        if (projects.isEmpty()) return;

        for (Project project : projects) {
            if (projectTaskRepository.countByProjectId(project.getId()) == 0) {
                seedDemoTasksForProject(project);
            }
        }
    }

    private void seedDemoTasksForProject(Project project) {
        Long pid = project.getId();
        LocalDate d0 = project.getPlannedStart() != null
                ? project.getPlannedStart()
                : LocalDate.of(2026, 2, 1);

        // ── Map wbsCode → saved task (for building dependencies) ──
        Map<String, ProjectTask> saved = new LinkedHashMap<>();

        // ══════════════════════════════════════════════════════════
        // 1  PROJECT MANAGEMENT
        // ══════════════════════════════════════════════════════════
        saved.put("1", save(pid, "1", "Project Management", "SUMMARY", "PM", 1,
                d0, d0.plusDays(130),
                d0, d0.plusDays(135), 60, false, 100));

        saved.put("1.1", save(pid, "1.1", "Kickoff Meeting", "MILESTONE", "PM", 2,
                d0.plusDays(2), d0.plusDays(2),
                d0.plusDays(2), d0.plusDays(2), 100, false, 200));

        saved.put("1.2", save(pid, "1.2", "Planning & Scope Validation", "ACTIVITY", "PM", 3,
                d0.plusDays(3), d0.plusDays(9),
                d0.plusDays(3), d0.plusDays(10), 100, false, 210));

        saved.put("1.3", save(pid, "1.3", "Risk Register Setup", "ACTIVITY", "PM", 4,
                d0.plusDays(5), d0.plusDays(10),
                d0.plusDays(5), d0.plusDays(12), 80, false, 220));

        saved.put("1.4", save(pid, "1.4", "Project Baseline Approved", "MILESTONE", "PM", 5,
                d0.plusDays(11), d0.plusDays(11),
                d0.plusDays(12), d0.plusDays(12), 0, true, 230));

        // ══════════════════════════════════════════════════════════
        // 2  ENGINEERING
        // ══════════════════════════════════════════════════════════
        saved.put("2", save(pid, "2", "Engineering", "SUMMARY", "ME", 6,
                d0.plusDays(10), d0.plusDays(50),
                d0.plusDays(12), d0.plusDays(55), 50, false, 300));

        saved.put("2.1", save(pid, "2.1", "Mechanical Engineering", "SUMMARY", "ME", 7,
                d0.plusDays(10), d0.plusDays(35),
                d0.plusDays(12), d0.plusDays(38), 70, false, 310));

        saved.put("2.1.1", save(pid, "2.1.1", "Structural Analysis", "ACTIVITY", "ME", 8,
                d0.plusDays(10), d0.plusDays(18),
                d0.plusDays(12), d0.plusDays(20), 100, false, 311));

        saved.put("2.1.2", save(pid, "2.1.2", "3D Modelling", "ACTIVITY", "ME", 9,
                d0.plusDays(19), d0.plusDays(28),
                d0.plusDays(21), d0.plusDays(30), 80, false, 312));

        saved.put("2.1.3", save(pid, "2.1.3", "Drawing Package", "ACTIVITY", "ME", 10,
                d0.plusDays(29), d0.plusDays(35),
                d0.plusDays(31), d0.plusDays(38), 40, false, 313));

        saved.put("2.2", save(pid, "2.2", "Electrical Engineering", "SUMMARY", "CE", 11,
                d0.plusDays(14), d0.plusDays(40),
                d0.plusDays(16), d0.plusDays(44), 45, false, 320));

        saved.put("2.2.1", save(pid, "2.2.1", "Power Architecture", "ACTIVITY", "CE", 12,
                d0.plusDays(14), d0.plusDays(22),
                d0.plusDays(16), d0.plusDays(24), 90, false, 321));

        saved.put("2.2.2", save(pid, "2.2.2", "Control Schematics", "ACTIVITY", "CE", 13,
                d0.plusDays(23), d0.plusDays(32),
                d0.plusDays(25), d0.plusDays(35), 50, false, 322));

        saved.put("2.2.3", save(pid, "2.2.3", "Cable Routing & BOM", "ACTIVITY", "CE", 14,
                d0.plusDays(33), d0.plusDays(40),
                d0.plusDays(36), d0.plusDays(44), 10, false, 323));

        saved.put("2.3", save(pid, "2.3", "Software Engineering", "SUMMARY", "SW", 15,
                d0.plusDays(18), d0.plusDays(50),
                d0.plusDays(20), d0.plusDays(55), 35, false, 330));

        saved.put("2.3.1", save(pid, "2.3.1", "SW Architecture Design", "ACTIVITY", "SW", 16,
                d0.plusDays(18), d0.plusDays(26),
                d0.plusDays(20), d0.plusDays(28), 100, false, 331));

        saved.put("2.3.2", save(pid, "2.3.2", "PLC Programming", "ACTIVITY", "PLC", 17,
                d0.plusDays(27), d0.plusDays(38),
                d0.plusDays(29), d0.plusDays(42), 60, false, 332));

        saved.put("2.3.3", save(pid, "2.3.3", "HMI Development", "ACTIVITY", "SW", 18,
                d0.plusDays(35), d0.plusDays(48),
                d0.plusDays(38), d0.plusDays(53), 20, false, 333));

        saved.put("2.3.4", save(pid, "2.3.4", "SCADA Integration", "ACTIVITY", "SW", 19,
                d0.plusDays(44), d0.plusDays(50),
                d0.plusDays(47), d0.plusDays(55), 0, false, 334));

        saved.put("2.4", save(pid, "2.4", "Customer Design Approval", "MILESTONE", "PM", 20,
                d0.plusDays(51), d0.plusDays(51),
                d0.plusDays(56), d0.plusDays(56), 0, true, 350));

        // ══════════════════════════════════════════════════════════
        // 3  PROCUREMENT
        // ══════════════════════════════════════════════════════════
        saved.put("3", save(pid, "3", "Procurement", "SUMMARY", "PRC", 21,
                d0.plusDays(36), d0.plusDays(75),
                d0.plusDays(39), d0.plusDays(80), 20, false, 400));

        saved.put("3.1", save(pid, "3.1", "Long Lead Items", "ACTIVITY", "PRC", 22,
                d0.plusDays(36), d0.plusDays(60),
                d0.plusDays(39), d0.plusDays(65), 35, false, 410));

        saved.put("3.2", save(pid, "3.2", "Standard Components", "ACTIVITY", "PRC", 23,
                d0.plusDays(40), d0.plusDays(60),
                d0.plusDays(43), d0.plusDays(65), 25, false, 420));

        saved.put("3.3", save(pid, "3.3", "Electrical Components", "ACTIVITY", "PRC", 24,
                d0.plusDays(42), d0.plusDays(65),
                d0.plusDays(45), d0.plusDays(70), 15, false, 430));

        saved.put("3.4", save(pid, "3.4", "Software Licenses", "ACTIVITY", "PRC", 25,
                d0.plusDays(38), d0.plusDays(50),
                d0.plusDays(40), d0.plusDays(55), 50, false, 440));

        saved.put("3.5", save(pid, "3.5", "PO Confirmed", "MILESTONE", "PRC", 26,
                d0.plusDays(44), d0.plusDays(44),
                d0.plusDays(48), d0.plusDays(48), 0, true, 450));

        saved.put("3.6", save(pid, "3.6", "All Deliveries Received", "MILESTONE", "PRC", 27,
                d0.plusDays(75), d0.plusDays(75),
                d0.plusDays(80), d0.plusDays(80), 0, false, 460));

        // ══════════════════════════════════════════════════════════
        // 4  MANUFACTURING
        // ══════════════════════════════════════════════════════════
        saved.put("4", save(pid, "4", "Manufacturing", "SUMMARY", "MFC", 28,
                d0.plusDays(76), d0.plusDays(108),
                d0.plusDays(81), d0.plusDays(114), 0, false, 500));

        saved.put("4.1", save(pid, "4.1", "Panel Fabrication", "ACTIVITY", "MFC", 29,
                d0.plusDays(76), d0.plusDays(86),
                d0.plusDays(81), d0.plusDays(91), 0, false, 510));

        saved.put("4.2", save(pid, "4.2", "Mechanical Assembly", "ACTIVITY", "MFC", 30,
                d0.plusDays(80), d0.plusDays(92),
                d0.plusDays(85), d0.plusDays(97), 0, false, 520));

        saved.put("4.3", save(pid, "4.3", "Electrical Wiring", "ACTIVITY", "MFC", 31,
                d0.plusDays(87), d0.plusDays(98),
                d0.plusDays(92), d0.plusDays(104), 0, false, 530));

        saved.put("4.4", save(pid, "4.4", "SW Loading & Config", "ACTIVITY", "SW", 32,
                d0.plusDays(93), d0.plusDays(100),
                d0.plusDays(98), d0.plusDays(106), 0, false, 540));

        saved.put("4.5", save(pid, "4.5", "Internal QC Test", "ACTIVITY", "QA", 33,
                d0.plusDays(99), d0.plusDays(108),
                d0.plusDays(105), d0.plusDays(114), 0, false, 550));

        // ══════════════════════════════════════════════════════════
        // 5  FAT & DELIVERY
        // ══════════════════════════════════════════════════════════
        saved.put("5", save(pid, "5", "FAT & Delivery", "SUMMARY", "PM", 34,
                d0.plusDays(109), d0.plusDays(125),
                d0.plusDays(115), d0.plusDays(132), 0, false, 600));

        saved.put("5.1", save(pid, "5.1", "Factory Acceptance Test", "MILESTONE", "PM", 35,
                d0.plusDays(112), d0.plusDays(112),
                d0.plusDays(118), d0.plusDays(118), 0, true, 610));

        saved.put("5.2", save(pid, "5.2", "Snag List Resolution", "ACTIVITY", "MFC", 36,
                d0.plusDays(113), d0.plusDays(118),
                d0.plusDays(119), d0.plusDays(124), 0, false, 620));

        saved.put("5.3", save(pid, "5.3", "Packaging & Shipment", "ACTIVITY", "MFC", 37,
                d0.plusDays(119), d0.plusDays(125),
                d0.plusDays(125), d0.plusDays(132), 0, false, 630));

        // ══════════════════════════════════════════════════════════
        // 6  SITE INSTALLATION
        // ══════════════════════════════════════════════════════════
        saved.put("6", save(pid, "6", "Site Installation", "SUMMARY", "MFC", 38,
                d0.plusDays(126), d0.plusDays(148),
                d0.plusDays(133), d0.plusDays(156), 0, false, 700));

        saved.put("6.1", save(pid, "6.1", "Site Preparation", "ACTIVITY", "MFC", 39,
                d0.plusDays(126), d0.plusDays(130),
                d0.plusDays(133), d0.plusDays(137), 0, false, 710));

        saved.put("6.2", save(pid, "6.2", "Mechanical Installation", "ACTIVITY", "MFC", 40,
                d0.plusDays(128), d0.plusDays(136),
                d0.plusDays(135), d0.plusDays(143), 0, false, 720));

        saved.put("6.3", save(pid, "6.3", "Electrical Installation", "ACTIVITY", "CE", 41,
                d0.plusDays(132), d0.plusDays(140),
                d0.plusDays(139), d0.plusDays(147), 0, false, 730));

        saved.put("6.4", save(pid, "6.4", "Commissioning", "ACTIVITY", "SW", 42,
                d0.plusDays(140), d0.plusDays(148),
                d0.plusDays(147), d0.plusDays(156), 0, false, 740));

        // ══════════════════════════════════════════════════════════
        // 7  FINAL ACCEPTANCE
        // ══════════════════════════════════════════════════════════
        saved.put("7", save(pid, "7", "Final Acceptance", "SUMMARY", "PM", 43,
                d0.plusDays(149), d0.plusDays(158),
                d0.plusDays(157), d0.plusDays(166), 0, false, 800));

        saved.put("7.1", save(pid, "7.1", "SAT — Site Acceptance Test", "MILESTONE", "PM", 44,
                d0.plusDays(150), d0.plusDays(150),
                d0.plusDays(158), d0.plusDays(158), 0, true, 810));

        saved.put("7.2", save(pid, "7.2", "Operator Training", "ACTIVITY", "PM", 45,
                d0.plusDays(151), d0.plusDays(154),
                d0.plusDays(159), d0.plusDays(162), 0, false, 820));

        saved.put("7.3", save(pid, "7.3", "Documentation Handover", "ACTIVITY", "PM", 46,
                d0.plusDays(152), d0.plusDays(156),
                d0.plusDays(160), d0.plusDays(164), 0, false, 830));

        saved.put("7.4", save(pid, "7.4", "Final Acceptance Certificate", "MILESTONE", "PM", 47,
                d0.plusDays(157), d0.plusDays(157),
                d0.plusDays(165), d0.plusDays(165), 0, true, 840));

        saved.put("7.5", save(pid, "7.5", "Project Closure", "MILESTONE", "PM", 48,
                d0.plusDays(158), d0.plusDays(158),
                d0.plusDays(166), d0.plusDays(166), 0, false, 850));

        // ══════════════════════════════════════════════════════════
        // SEED DEPENDENCIES
        // ══════════════════════════════════════════════════════════
        List<TaskDependency> deps = new ArrayList<>();

        // 1.x chain
        dep(deps, saved, pid, "1.1", "1.2", "FS", 0); // After kickoff → planning
        dep(deps, saved, pid, "1.2", "1.3", "SS", 0); // Risk setup starts with planning
        dep(deps, saved, pid, "1.2", "1.4", "FS", 0); // Baseline after planning done

        // Engineering starts after baseline approved
        dep(deps, saved, pid, "1.4", "2.1.1", "FS", 0);
        dep(deps, saved, pid, "1.4", "2.2.1", "FS", 2);
        dep(deps, saved, pid, "1.4", "2.3.1", "FS", 3);

        // Mechanical chain
        dep(deps, saved, pid, "2.1.1", "2.1.2", "FS", 0);
        dep(deps, saved, pid, "2.1.2", "2.1.3", "FS", 0);

        // Electrical chain
        dep(deps, saved, pid, "2.2.1", "2.2.2", "FS", 0);
        dep(deps, saved, pid, "2.2.2", "2.2.3", "FS", 0);

        // Software chain
        dep(deps, saved, pid, "2.3.1", "2.3.2", "FS", 0);
        dep(deps, saved, pid, "2.3.1", "2.3.3", "SS", 5);
        dep(deps, saved, pid, "2.3.2", "2.3.4", "FS", 0);
        dep(deps, saved, pid, "2.3.3", "2.3.4", "FS", 0);

        // Design approval needs all engineering done
        dep(deps, saved, pid, "2.1.3", "2.4", "FS", 0);
        dep(deps, saved, pid, "2.2.3", "2.4", "FS", 0);
        dep(deps, saved, pid, "2.3.4", "2.4", "FS", 0);

        // Procurement starts after design approval
        dep(deps, saved, pid, "2.4", "3.1", "FS", 0);
        dep(deps, saved, pid, "2.4", "3.2", "FS", 0);
        dep(deps, saved, pid, "2.2.3", "3.3", "FS", 0);  // Elec BOM drives elec procurement
        dep(deps, saved, pid, "2.3.1", "3.4", "FS", 0);  // SW arch drives license procurement

        // PO milestone: after long leads + standard ordered
        dep(deps, saved, pid, "3.1", "3.5", "SS", 5);
        dep(deps, saved, pid, "3.2", "3.5", "SS", 5);

        // All deliveries after all procurement
        dep(deps, saved, pid, "3.1", "3.6", "FS", 0);
        dep(deps, saved, pid, "3.2", "3.6", "FS", 0);
        dep(deps, saved, pid, "3.3", "3.6", "FS", 0);
        dep(deps, saved, pid, "3.4", "3.6", "FS", 0);

        // Manufacturing after all deliveries
        dep(deps, saved, pid, "3.6", "4.1", "FS", 0);
        dep(deps, saved, pid, "3.6", "4.2", "FS", 2);
        dep(deps, saved, pid, "4.1", "4.3", "FS", 0);   // Panel done → wire
        dep(deps, saved, pid, "4.2", "4.3", "SS", 3);   // Mech + panel before wiring
        dep(deps, saved, pid, "4.3", "4.4", "FS", 0);   // Wired before SW load
        dep(deps, saved, pid, "4.4", "4.5", "FS", 0);   // SW loaded before QC test

        // FAT after internal test
        dep(deps, saved, pid, "4.5", "5.1", "FS", 0);
        dep(deps, saved, pid, "5.1", "5.2", "FS", 0);   // Snag list after FAT
        dep(deps, saved, pid, "5.2", "5.3", "FS", 0);   // Ship after snags resolved

        // Site installation after shipment
        dep(deps, saved, pid, "5.3", "6.1", "FS", 5);   // 5-day transit lag
        dep(deps, saved, pid, "6.1", "6.2", "FS", 0);
        dep(deps, saved, pid, "6.2", "6.3", "SS", 2);   // Electrical starts 2 days after mech
        dep(deps, saved, pid, "6.3", "6.4", "FS", 0);   // Commission after elect
        dep(deps, saved, pid, "6.2", "6.4", "FF", 0);   // Commission finishes with mech

        // Final acceptance
        dep(deps, saved, pid, "6.4", "7.1", "FS", 0);   // SAT after commissioning
        dep(deps, saved, pid, "7.1", "7.2", "FS", 0);
        dep(deps, saved, pid, "7.1", "7.3", "FS", 0);
        dep(deps, saved, pid, "7.2", "7.4", "FS", 0);
        dep(deps, saved, pid, "7.3", "7.4", "FS", 0);
        dep(deps, saved, pid, "7.4", "7.5", "FS", 0);

        taskDependencyRepository.saveAll(deps);
    }

    // ── Helper: save a task and return it ────────────────────────
    private ProjectTask save(Long projectId,
                             String wbsCode,
                             String name,
                             String taskType,
                             String departmentCode,
                             int displayOrder,
                             LocalDate baselineStart,
                             LocalDate baselineEnd,
                             LocalDate plannedStart,
                             LocalDate plannedEnd,
                             int percentComplete,
                             boolean customerMilestone,
                             int priority) {

        ProjectTask t = new ProjectTask();
        t.setProjectId(projectId);
        t.setWbsCode(wbsCode);
        t.setName(name);
        t.setTaskType(taskType);
        t.setDepartmentCode(departmentCode);
        t.setDisplayOrder(displayOrder);
        t.setBaselineStart(baselineStart);
        t.setBaselineEnd(baselineEnd);
        t.setPlannedStart(plannedStart);
        t.setPlannedEnd(plannedEnd);
        t.setPercentComplete(percentComplete);
        t.setCustomerMilestone(customerMilestone);
        t.setPriority(priority);
        t.setActive(true);
        t.setScheduleMode("AUTO");
        t.setDescription(name + " — seeded demo task.");

        // Compute outlineLevel from wbsCode dots
        int level = (int) wbsCode.chars().filter(c -> c == '.').count() + 1;
        t.setOutlineLevel(level);

        // Duration
        if ("MILESTONE".equalsIgnoreCase(taskType)) {
            t.setDurationDays(0);
        } else {
            int dur = (int) ChronoUnit.DAYS.between(plannedStart, plannedEnd) + 1;
            t.setDurationDays(Math.max(1, dur));
        }

        return projectTaskRepository.save(t);
    }

    // ── Helper: create a FS/SS/FF/SF dependency ──────────────────
    private void dep(List<TaskDependency> list,
                     Map<String, ProjectTask> saved,
                     Long projectId,
                     String predWbs,
                     String succWbs,
                     String type,
                     int lagDays) {

        ProjectTask pred = saved.get(predWbs);
        ProjectTask succ = saved.get(succWbs);
        if (pred == null || succ == null) return;

        // Avoid duplicate
        boolean exists = list.stream().anyMatch(d ->
                d.getPredecessorTaskId().equals(pred.getId()) &&
                d.getSuccessorTaskId().equals(succ.getId()) &&
                d.getDependencyType().equalsIgnoreCase(type)
        );
        if (exists) return;

        TaskDependency dep = new TaskDependency();
        dep.setProjectId(projectId);
        dep.setPredecessorTaskId(pred.getId());
        dep.setSuccessorTaskId(succ.getId());
        dep.setDependencyType(type);
        dep.setLagDays(lagDays);
        list.add(dep);
    }
}