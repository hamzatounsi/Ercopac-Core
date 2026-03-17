package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.tasks.domain.ProjectTask;
import com.ercopac.ercopac_tracker.tasks.repository.ProjectTaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProjectTaskSeeder implements CommandLineRunner {

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;

    public ProjectTaskSeeder(ProjectRepository projectRepository,
                             ProjectTaskRepository projectTaskRepository) {
        this.projectRepository = projectRepository;
        this.projectTaskRepository = projectTaskRepository;
    }

    @Override
    public void run(String... args) {
        List<Project> projects = projectRepository.findAll();

        if (projects.isEmpty()) {
            return;
        }

        for (Project project : projects) {
            if (projectTaskRepository.countByProjectId(project.getId()) == 0) {
                seedDemoTasksForProject(project);
            }
        }
    }

    private void seedDemoTasksForProject(Project project) {
        LocalDate projectStart = project.getPlannedStart() != null
                ? project.getPlannedStart()
                : LocalDate.of(2026, 2, 1);

        List<ProjectTask> tasks = new ArrayList<>();

        tasks.add(createTask(
                project.getId(),
                "1",
                "Project Management",
                "SUMMARY",
                "PM",
                projectStart,
                projectStart.plusDays(110),
                projectStart,
                projectStart.plusDays(115),
                55,
                100,
                1,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "1.1",
                "Kickoff Meeting",
                "MILESTONE",
                "PM",
                projectStart.plusDays(2),
                projectStart.plusDays(2),
                projectStart.plusDays(2),
                projectStart.plusDays(2),
                100,
                200,
                2,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "1.2",
                "Planning Validation",
                "ACTIVITY",
                "PM",
                projectStart.plusDays(3),
                projectStart.plusDays(8),
                projectStart.plusDays(4),
                projectStart.plusDays(10),
                100,
                220,
                3,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "2",
                "Design",
                "SUMMARY",
                "ME",
                projectStart.plusDays(9),
                projectStart.plusDays(35),
                projectStart.plusDays(11),
                projectStart.plusDays(38),
                72,
                300,
                4,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "2.1",
                "Mechanical Design",
                "ACTIVITY",
                "ME",
                projectStart.plusDays(9),
                projectStart.plusDays(20),
                projectStart.plusDays(11),
                projectStart.plusDays(22),
                85,
                320,
                5,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "2.2",
                "Electrical Design",
                "ACTIVITY",
                "CE",
                projectStart.plusDays(12),
                projectStart.plusDays(24),
                projectStart.plusDays(14),
                projectStart.plusDays(28),
                60,
                330,
                6,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "2.3",
                "Software Design",
                "ACTIVITY",
                "SW",
                projectStart.plusDays(15),
                projectStart.plusDays(35),
                projectStart.plusDays(18),
                projectStart.plusDays(38),
                45,
                340,
                7,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "2.4",
                "Customer Design Approval",
                "MILESTONE",
                "PM",
                projectStart.plusDays(36),
                projectStart.plusDays(36),
                projectStart.plusDays(40),
                projectStart.plusDays(40),
                0,
                350,
                8,
                true
        ));

        tasks.add(createTask(
                project.getId(),
                "3",
                "Procurement",
                "SUMMARY",
                "PRC",
                projectStart.plusDays(37),
                projectStart.plusDays(60),
                projectStart.plusDays(41),
                projectStart.plusDays(66),
                25,
                400,
                9,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "3.1",
                "Long Lead Items",
                "ACTIVITY",
                "PRC",
                projectStart.plusDays(37),
                projectStart.plusDays(50),
                projectStart.plusDays(41),
                projectStart.plusDays(56),
                30,
                420,
                10,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "3.2",
                "Standard Components",
                "ACTIVITY",
                "PRC",
                projectStart.plusDays(39),
                projectStart.plusDays(60),
                projectStart.plusDays(43),
                projectStart.plusDays(66),
                20,
                430,
                11,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "4",
                "Manufacturing",
                "SUMMARY",
                "MFC",
                projectStart.plusDays(61),
                projectStart.plusDays(88),
                projectStart.plusDays(67),
                projectStart.plusDays(94),
                10,
                500,
                12,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "4.1",
                "Mechanical Assembly",
                "ACTIVITY",
                "MFC",
                projectStart.plusDays(61),
                projectStart.plusDays(74),
                projectStart.plusDays(67),
                projectStart.plusDays(80),
                15,
                520,
                13,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "4.2",
                "Electrical Assembly",
                "ACTIVITY",
                "MFC",
                projectStart.plusDays(68),
                projectStart.plusDays(82),
                projectStart.plusDays(74),
                projectStart.plusDays(88),
                8,
                530,
                14,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "4.3",
                "Internal Testing",
                "ACTIVITY",
                "SW",
                projectStart.plusDays(83),
                projectStart.plusDays(88),
                projectStart.plusDays(89),
                projectStart.plusDays(94),
                0,
                540,
                15,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "5",
                "FAT & Delivery",
                "SUMMARY",
                "PM",
                projectStart.plusDays(89),
                projectStart.plusDays(101),
                projectStart.plusDays(95),
                projectStart.plusDays(108),
                0,
                600,
                16,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "5.1",
                "Factory Acceptance Test",
                "MILESTONE",
                "PM",
                projectStart.plusDays(92),
                projectStart.plusDays(92),
                projectStart.plusDays(99),
                projectStart.plusDays(99),
                0,
                620,
                17,
                true
        ));

        tasks.add(createTask(
                project.getId(),
                "5.2",
                "Packaging & Shipment",
                "ACTIVITY",
                "MFC",
                projectStart.plusDays(93),
                projectStart.plusDays(101),
                projectStart.plusDays(100),
                projectStart.plusDays(108),
                0,
                630,
                18,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "6",
                "Site Installation",
                "SUMMARY",
                "MFC",
                projectStart.plusDays(102),
                projectStart.plusDays(120),
                projectStart.plusDays(109),
                projectStart.plusDays(128),
                0,
                700,
                19,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "6.1",
                "Mechanical Installation",
                "ACTIVITY",
                "MFC",
                projectStart.plusDays(102),
                projectStart.plusDays(110),
                projectStart.plusDays(109),
                projectStart.plusDays(117),
                0,
                720,
                20,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "6.2",
                "Electrical Installation",
                "ACTIVITY",
                "CE",
                projectStart.plusDays(106),
                projectStart.plusDays(114),
                projectStart.plusDays(113),
                projectStart.plusDays(121),
                0,
                730,
                21,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "6.3",
                "Commissioning",
                "ACTIVITY",
                "SW",
                projectStart.plusDays(115),
                projectStart.plusDays(120),
                projectStart.plusDays(122),
                projectStart.plusDays(128),
                0,
                740,
                22,
                false
        ));

        tasks.add(createTask(
                project.getId(),
                "7",
                "Go Live",
                "MILESTONE",
                "PM",
                projectStart.plusDays(121),
                projectStart.plusDays(121),
                projectStart.plusDays(129),
                projectStart.plusDays(129),
                0,
                800,
                23,
                true
        ));

        projectTaskRepository.saveAll(tasks);
    }

    private ProjectTask createTask(Long projectId,
                                   String wbsCode,
                                   String name,
                                   String taskType,
                                   String departmentCode,
                                   LocalDate baselineStart,
                                   LocalDate baselineEnd,
                                   LocalDate plannedStart,
                                   LocalDate plannedEnd,
                                   Integer percentComplete,
                                   Integer priority,
                                   Integer displayOrder,
                                   Boolean customerMilestone) {

        ProjectTask task = new ProjectTask();
        task.setProjectId(projectId);
        task.setWbsCode(wbsCode);
        task.setName(name);
        task.setTaskType(taskType);
        task.setDepartmentCode(departmentCode);
        task.setBaselineStart(baselineStart);
        task.setBaselineEnd(baselineEnd);
        task.setPlannedStart(plannedStart);
        task.setPlannedEnd(plannedEnd);
        task.setPercentComplete(percentComplete);
        task.setPriority(priority);
        task.setDisplayOrder(displayOrder);
        task.setCustomerMilestone(customerMilestone);
        task.setActive(true);
        task.setScheduleMode("AUTO");

        if ("MILESTONE".equalsIgnoreCase(taskType)) {
            task.setDurationDays(0);
        } else {
            int duration = (int) java.time.temporal.ChronoUnit.DAYS.between(plannedStart, plannedEnd) + 1;
            task.setDurationDays(Math.max(duration, 1));
        }

        task.setDescription(name + " - demo seeded task for schedule visualization.");
        return task;
    }
}