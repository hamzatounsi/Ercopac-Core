package com.ercopac.ercopac_tracker.milestone.controller; // ⚠️ Adaptez le package si nécessaire

import com.ercopac.ercopac_tracker.milestone.domain.MilestoneType;
import com.ercopac.ercopac_tracker.milestone.domain.ProjectMilestone;
import com.ercopac.ercopac_tracker.milestone.repository.MilestoneTypeRepository;
import com.ercopac.ercopac_tracker.milestone.repository.ProjectMilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/milestones") // ⚠️ Adaptez le chemin si votre contrôleur utilise un autre @RequestMapping
public class MilestoneController {

    // ✅ 1. Injection CORRECTE des repositories (pas de "Object" !)
    @Autowired
    private ProjectMilestoneRepository milestoneRepository;

    @Autowired
    private MilestoneTypeRepository milestoneTypeRepository;

    // ✅ 2. Endpoint de diagnostic
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugMilestones() {
        Map<String, Object> debug = new HashMap<>();
        
        // A. Vérifier le nombre total de milestones
        List<ProjectMilestone> allMilestones = milestoneRepository.findAll();
        debug.put("totalMilestones", allMilestones.size());
        
        // B. Vérifier les IDs spécifiques (20, 24, 72, 76)
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
        
        // C. Vérifier le nombre de types de milestones en base
        Long milestoneTypeCount = milestoneTypeRepository.count();
        debug.put("totalMilestoneTypes", milestoneTypeCount);
        
        // D. Vérifier le contenu de la table milestone_types (échantillon de 5)
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
    }

    // ... Gardez ici le reste de vos méthodes existantes (getMilestones, etc.) ...
}