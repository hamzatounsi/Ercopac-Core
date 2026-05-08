package com.ercopac.ercopac_tracker.crm.dto;
 
public class CrmPipelineStageDto {
    private Long id;
    private String name;
    private String color;
    private Integer displayOrder;
    private boolean won;
    private boolean lost;
    private int opportunityCount; // computed
 
    public CrmPipelineStageDto() {}
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }
    public boolean isLost() { return lost; }
    public void setLost(boolean lost) { this.lost = lost; }
    public int getOpportunityCount() { return opportunityCount; }
    public void setOpportunityCount(int opportunityCount) { this.opportunityCount = opportunityCount; }
}