package com.ercopac.ercopac_tracker.tasks.service;

import com.ercopac.ercopac_tracker.tasks.domain.TaskConsoleConfig;
import com.ercopac.ercopac_tracker.tasks.domain.TaskConsoleLog;
import com.ercopac.ercopac_tracker.tasks.dto.TaskConsoleConfigDto;
import com.ercopac.ercopac_tracker.tasks.dto.TaskConsoleLogDto;
import com.ercopac.ercopac_tracker.tasks.repository.TaskConsoleConfigRepository;
import com.ercopac.ercopac_tracker.tasks.repository.TaskConsoleLogRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskConsoleService {

    private final TaskConsoleConfigRepository configRepository;
    private final TaskConsoleLogRepository logRepository;

    public TaskConsoleService(
            TaskConsoleConfigRepository configRepository,
            TaskConsoleLogRepository logRepository
    ) {
        this.configRepository = configRepository;
        this.logRepository = logRepository;
    }

    public TaskConsoleConfigDto getConfig(Long organisationId, Long projectId, Long taskId) {
        TaskConsoleConfig config = configRepository
                .findByOrganisationIdAndProjectIdAndTaskId(organisationId, projectId, taskId)
                .orElseGet(() -> createDefaultConfig(organisationId, projectId, taskId));

        return toConfigDto(config);
    }

    public TaskConsoleConfigDto saveConfig(
            Long organisationId,
            Long projectId,
            Long taskId,
            TaskConsoleConfigDto request
    ) {
        TaskConsoleConfig config = configRepository
                .findByOrganisationIdAndProjectIdAndTaskId(organisationId, projectId, taskId)
                .orElseGet(() -> createDefaultConfig(organisationId, projectId, taskId));

        config.setCheckpoint25(request.checkpoint25());
        config.setCheckpoint50(request.checkpoint50());
        config.setCheckpoint75(request.checkpoint75());
        config.setChannel(request.channel() == null ? "APP_ALERT" : request.channel());
        config.setNotifyPm(request.notifyPm());
        config.setNotifyOwner(request.notifyOwner());
        config.setNotifyDeptManager(request.notifyDeptManager());
        config.setNotifyEveryone(request.notifyEveryone());
        config.setUpdatedAt(LocalDateTime.now());

        TaskConsoleConfig saved = configRepository.save(config);

        addLog(
                organisationId,
                projectId,
                taskId,
                "Console notification settings updated.",
                "INFO",
                saved.getChannel(),
                buildNotifyTarget(saved)
        );

        return toConfigDto(saved);
    }

    public List<TaskConsoleLogDto> getLogs(Long organisationId, Long projectId, Long taskId) {
        return logRepository
                .findByOrganisationIdAndProjectIdAndTaskIdOrderByCreatedAtDesc(
                        organisationId,
                        projectId,
                        taskId
                )
                .stream()
                .map(this::toLogDto)
                .toList();
    }

    public void clearLogs(Long organisationId, Long projectId, Long taskId) {
        logRepository.deleteByOrganisationIdAndProjectIdAndTaskId(
                organisationId,
                projectId,
                taskId
        );
    }

    public void addLog(
            Long organisationId,
            Long projectId,
            Long taskId,
            String message,
            String severity,
            String channel,
            String notifyTarget
    ) {
        TaskConsoleLog log = new TaskConsoleLog();
        log.setOrganisationId(organisationId);
        log.setProjectId(projectId);
        log.setTaskId(taskId);
        log.setMessage(message);
        log.setSeverity(severity == null ? "INFO" : severity);
        log.setChannel(channel == null ? "APP_ALERT" : channel);
        log.setNotifyTarget(notifyTarget == null ? "PM" : notifyTarget);
        log.setCreatedAt(LocalDateTime.now());

        logRepository.save(log);
    }

    private TaskConsoleConfig createDefaultConfig(Long organisationId, Long projectId, Long taskId) {
        TaskConsoleConfig config = new TaskConsoleConfig();
        config.setOrganisationId(organisationId);
        config.setProjectId(projectId);
        config.setTaskId(taskId);
        config.setCheckpoint25(true);
        config.setCheckpoint50(true);
        config.setCheckpoint75(true);
        config.setChannel("APP_ALERT");
        config.setNotifyPm(true);
        config.setNotifyOwner(true);
        config.setNotifyDeptManager(false);
        config.setNotifyEveryone(false);
        config.setUpdatedAt(LocalDateTime.now());

        return configRepository.save(config);
    }

    private TaskConsoleConfigDto toConfigDto(TaskConsoleConfig config) {
        return new TaskConsoleConfigDto(
                config.getId(),
                config.getProjectId(),
                config.getTaskId(),
                config.isCheckpoint25(),
                config.isCheckpoint50(),
                config.isCheckpoint75(),
                config.getChannel(),
                config.isNotifyPm(),
                config.isNotifyOwner(),
                config.isNotifyDeptManager(),
                config.isNotifyEveryone(),
                config.getUpdatedAt()
        );
    }

    private TaskConsoleLogDto toLogDto(TaskConsoleLog log) {
        return new TaskConsoleLogDto(
                log.getId(),
                log.getProjectId(),
                log.getTaskId(),
                log.getMessage(),
                log.getSeverity(),
                log.getChannel(),
                log.getNotifyTarget(),
                log.getCreatedAt()
        );
    }

    private String buildNotifyTarget(TaskConsoleConfig config) {
        if (config.isNotifyEveryone()) return "ALL";

        StringBuilder sb = new StringBuilder();

        if (config.isNotifyPm()) sb.append("PM, ");
        if (config.isNotifyOwner()) sb.append("OWNER, ");
        if (config.isNotifyDeptManager()) sb.append("DEPT_MANAGER, ");

        if (sb.isEmpty()) return "NONE";

        return sb.substring(0, sb.length() - 2);
    }
}