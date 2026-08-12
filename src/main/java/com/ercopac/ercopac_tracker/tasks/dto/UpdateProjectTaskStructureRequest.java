package com.ercopac.ercopac_tracker.tasks.dto;

import java.util.List;

/**
 * The complete, ordered hierarchy for a project's existing tasks. Task data is
 * intentionally excluded: this endpoint only changes the Gantt structure.
 */
public record UpdateProjectTaskStructureRequest(List<TaskStructureItem> tasks) {
    public record TaskStructureItem(Long taskId, Long parentId, Integer displayOrder) {}
}
