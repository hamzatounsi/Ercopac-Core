package com.ercopac.ercopac_tracker.user;

import java.math.BigDecimal;

public class ResourceTypeDto {
    private Long id;
    private String code;
    private String label;
    private String colour;
    private BigDecimal defaultRate; // ✅ AJOUTE CETTE LIGNE

    public ResourceTypeDto() {}

    // Modifie le constructeur pour inclure defaultRate
    public ResourceTypeDto(Long id, String code, String label, String colour, BigDecimal defaultRate) {
        this.id = id;
        this.code = code;
        this.label = label;
        this.colour = colour;
        this.defaultRate = defaultRate; // ✅ AJOUTE CETTE LIGNE
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getColour() { return colour; }
    public void setColour(String colour) { this.colour = colour; }

    // ✅ AJOUTE CES GETTER/SETTER
    public BigDecimal getDefaultRate() { return defaultRate; }
    public void setDefaultRate(BigDecimal defaultRate) { this.defaultRate = defaultRate; }
}