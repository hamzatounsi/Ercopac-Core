package com.ercopac.ercopac_tracker.milestone.controller;

import com.ercopac.ercopac_tracker.milestone.domain.MilestoneType;
import com.ercopac.ercopac_tracker.milestone.domain.ProjectMilestone;
import com.ercopac.ercopac_tracker.milestone.repository.MilestoneTypeRepository;
import com.ercopac.ercopac_tracker.milestone.repository.ProjectMilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {

    @Autowired
    private ProjectMilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneTypeRepository milestoneTypeRepository;

    // ✅ ENDPOINT DE DIAGNOSTIC (Utilise ? pour éviter toute erreur de type)
    @GetMapping("/debug")
    public ResponseEntity<?> debugMilestones() {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            List<ProjectMilestone> allMilestones = milestoneRepository.findAll();
            debug.put("totalMilestones", allMilestones.size());
            
            List<Map<String, Object>> sampleData = new ArrayList<>();
            for (ProjectMilestone m : allMilestones) {
                if (m.getId() != null && List.of(20L, 24L, 72L, 76L).contains(m.getId())) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", m.getId());
                    data.put("milestoneTypeId", m.getMilestoneTypeId());
                    data.put("milestoneDate", m.getMilestoneDate());
                    data.put("projectId", m.getProjectId());
                    sampleData.add(data);
                }
            }
            debug.put("sampleMilestones", sampleData);
            
            Long milestoneTypeCount = milestoneTypeRepository.count();
            debug.put("totalMilestoneTypes", milestoneTypeCount);
            
            List<Map<String, Object>> typesSample = new ArrayList<>();
            List<MilestoneType> allTypes = milestoneTypeRepository.findAll();
            for (int i = 0; i < Math.min(5, allTypes.size()); i++) {
                MilestoneType mt = allTypes.get(i);
                Map<String, Object> typeData = new HashMap<>();
                typeData.put("id", mt.getId());
                typeData.put("code", mt.getCode());
                typeData.put("label", mt.getLabel());
                typeData.put("color", mt.getColor());
                typesSample.add(typeData);
            }
            debug.put("milestoneTypesSample", typesSample);
            
            return ResponseEntity.ok(debug);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("debugError", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ✅ ENDPOINT PRINCIPAL AVEC PIÈGE À ERREURS (Utilise ? pour éviter toute erreur de type)
    @GetMapping("/range")
    public ResponseEntity<?> getMilestonesByDateRange(
            @RequestParam List<Long> projectIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            // On appelle le repository
            List<ProjectMilestone> milestones = milestoneRepository.findByProjectIdInAndMilestoneDateBetween(projectIds, startDate, endDate);
            return ResponseEntity.ok(milestones);
            
        } catch (Exception e) {
            // 🚨 C'est ici qu'on vole l'erreur au serveur pour l'afficher dans votre navigateur !
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("errorType", e.getClass().getSimpleName()); 
            errorDetails.put("errorMessage", e.getMessage());      
            
            // On retourne l'erreur en JSON au lieu d'un 500 vide
            return ResponseEntity.status(500).body(errorDetails);
        }
    }
}