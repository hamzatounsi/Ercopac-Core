package com.ercopac.ercopac_tracker.crm.dto;
import java.time.LocalDate;
import java.util.List;
public record CrmEquipmentReportDto(List<EquipmentTotal> totals, List<ShipmentRow> shipments, List<EquipmentDetail> details) {
 public record EquipmentTotal(String equipmentName,String equipmentCode,long quantity,long opportunities){}
 public record ShipmentRow(String opportunity,String account,String stage,String owner,LocalDate closingDate,LocalDate shipmentDate,String status){}
 public record EquipmentDetail(Long opportunityId,String opportunity,String account,String stage,String opportunityType,String owner,String equipmentName,String equipmentCode,Integer quantity,LocalDate shipmentDate){}
}
