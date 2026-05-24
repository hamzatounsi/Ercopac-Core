package com.ercopac.ercopac_tracker.user;

public class ResourceTypeDto {
    private Long id;
    private String code;
    private String label;
    private String colour;

    public ResourceTypeDto() {}

    public ResourceTypeDto(Long id, String code, String label, String colour) {
        this.id = id;
        this.code = code;
        this.label = label;
        this.colour = colour;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }
}