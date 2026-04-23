package com.ercopac.ercopac_tracker.projectum.finance.settings.dto;

import java.util.List;

public class ApplyFinanceTemplateRequest {
    private List<Long> projectIds;

    public List<Long> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(List<Long> projectIds) {
        this.projectIds = projectIds;
    }
}