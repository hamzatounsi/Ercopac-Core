package com.ercopac.ercopac_tracker.planning.dto;

import jakarta.validation.constraints.NotBlank;

public class RenameProjectBaselineRequest {
    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}