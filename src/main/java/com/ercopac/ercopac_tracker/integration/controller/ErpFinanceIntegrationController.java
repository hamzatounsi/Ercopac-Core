package com.ercopac.ercopac_tracker.integration.controller;

import com.ercopac.ercopac_tracker.integration.entity.ErpProject;
import com.ercopac.ercopac_tracker.integration.entity.ErpWbsItem;
import com.ercopac.ercopac_tracker.integration.repository.ErpProjectRepository;
import com.ercopac.ercopac_tracker.integration.repository.ErpWbsItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/integrations/erp")
public class ErpFinanceIntegrationController {
    
    @Autowired
    private ErpProjectRepository projectRepo;
    
    @Autowired
    private ErpWbsItemRepository wbsItemRepo;
    
    private static final String API_KEY = "test-poc-key-2026";
    
    @PostMapping("/wbs-finance")
    public ResponseEntity<?> receiveWBSFinance(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Object> payload) {
        
        // 1. Authentifier
        if (!("Bearer " + API_KEY).equals(auth)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid API key"));
        }
        
        try {
            String projectCode = (String) payload.get("projectCode");
            String projectName = (String) payload.get("projectName");
            String companyCode = (String) payload.get("companyCode");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> wbsItems = (List<Map<String, Object>>) payload.get("wbsItems");
            
            // 2. Créer ou récupérer le projet
            final ErpProject projectToSave = projectRepo.findByErpProjectCode(projectCode)
                .orElseGet(() -> {
                    ErpProject p = new ErpProject();
                    p.setErpProjectCode(projectCode);
                    return p;
                });
            
            projectToSave.setProjectName(projectName);
            projectToSave.setCompanyCode(companyCode);
            projectToSave.setLastSyncedAt(LocalDateTime.now());
            final ErpProject savedProject = projectRepo.save(projectToSave);
            
            // 3. Créer/mettre à jour les items WBS
            int count = 0;
            for (Map<String, Object> itemDTO : wbsItems) {
                Long erpItemId = ((Number) itemDTO.get("erpItemId")).longValue();
                
                final ErpWbsItem itemToSave = wbsItemRepo.findByErpItemId(erpItemId)
                    .orElseGet(() -> {
                        ErpWbsItem i = new ErpWbsItem();
                        i.setErpItemId(erpItemId);
                        i.setProject(savedProject);
                        return i;
                    });
                
                itemToSave.setWbsCode((String) itemDTO.get("wbsCode"));
                itemToSave.setParentWbsCode((String) itemDTO.get("parentWbsCode"));
                itemToSave.setWbsLevel((Integer) itemDTO.get("level"));
                itemToSave.setDescription((String) itemDTO.get("description"));
                itemToSave.setItemType((String) itemDTO.get("type"));
                itemToSave.setDepartment((String) itemDTO.get("department"));
                itemToSave.setSequenceOrder((Integer) itemDTO.get("sequence"));
                
                // Gestion de quantity
                Object qtyObj = itemDTO.get("quantity");
                if (qtyObj != null) {
                    itemToSave.setQuantity(BigDecimal.valueOf(((Number) qtyObj).doubleValue()));
                }
                
                // ✅ NOUVEAUX CHAMPS : Commitment et Actual Cost
                Object commitmentObj = itemDTO.get("commitment");
                if (commitmentObj != null) {
                    itemToSave.setCommitment(BigDecimal.valueOf(((Number) commitmentObj).doubleValue()));
                }
                
                Object actualCostObj = itemDTO.get("actualCost");
                if (actualCostObj != null) {
                    itemToSave.setActualCost(BigDecimal.valueOf(((Number) actualCostObj).doubleValue()));
                }
                
                // Calculs automatiques
                BigDecimal budget = itemToSave.getBudget() != null ? itemToSave.getBudget() : BigDecimal.ZERO;
                BigDecimal actual = itemToSave.getActualCost() != null ? itemToSave.getActualCost() : BigDecimal.ZERO;
                itemToSave.setForecast(budget.subtract(actual));
                itemToSave.setVariance(budget.subtract(actual.add(itemToSave.getForecast())));
                
                itemToSave.setLastSyncedAt(LocalDateTime.now());
                
                wbsItemRepo.save(itemToSave);
                count++;
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "projectCode", projectCode,
                "itemsProcessed", count
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/projects/{projectCode}/wbs")
    public ResponseEntity<?> getProjectWBS(@PathVariable String projectCode) {
        return projectRepo.findByErpProjectCode(projectCode)
            .map(project -> {
                List<ErpWbsItem> items = wbsItemRepo.findByProjectId(project.getId());
                return ResponseEntity.ok(Map.of("project", project, "wbsItems", items));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}