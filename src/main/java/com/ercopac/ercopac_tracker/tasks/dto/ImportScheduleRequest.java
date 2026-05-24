package com.ercopac.ercopac_tracker.tasks.dto;

import java.util.List;

public record ImportScheduleRequest(
        List<UpdateProjectTaskRequest> tasks
) {}