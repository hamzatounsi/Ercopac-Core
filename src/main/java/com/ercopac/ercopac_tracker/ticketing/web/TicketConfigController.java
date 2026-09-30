package com.ercopac.ercopac_tracker.ticketing.web;

import com.ercopac.ercopac_tracker.projects.domain.Project;
import com.ercopac.ercopac_tracker.projects.repository.ProjectRepository;
import com.ercopac.ercopac_tracker.ticketing.domain.EquipmentType;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketPriorityConfig;
import com.ercopac.ercopac_tracker.ticketing.domain.TicketStatusConfig;
import com.ercopac.ercopac_tracker.ticketing.repository.EquipmentTypeRepository;
import com.ercopac.ercopac_tracker.ticketing.service.TicketConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform-settings")
public class TicketConfigController {

    private final TicketConfigService configService;
    private final ProjectRepository projectRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;

    public TicketConfigController(
            TicketConfigService configService,
            ProjectRepository projectRepository,
            EquipmentTypeRepository equipmentTypeRepository
    ) {
        this.configService = configService;
        this.projectRepository = projectRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
    }

    // ==========================================
    // 1. STATUSES & PRIORITIES (Existant)
    // ==========================================
    
    @GetMapping("/statuses")
    public ResponseEntity<List<TicketStatusConfig>> getStatuses() {
        return ResponseEntity.ok(configService.getAllStatuses());
    }

    @PutMapping("/statuses/{id}")
    public ResponseEntity<TicketStatusConfig> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        String label = (String) request.get("label");
        String description = (String) request.get("description");
        boolean active = (boolean) request.get("active");
        
        return ResponseEntity.ok(configService.updateStatus(id, label, description, active));
    }

    @GetMapping("/priorities")
    public ResponseEntity<List<TicketPriorityConfig>> getPriorities() {
        return ResponseEntity.ok(configService.getAllPriorities());
    }

    @PutMapping("/priorities/{id}")
    public ResponseEntity<TicketPriorityConfig> updatePriority(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        
        int firstResponseSla = request.get("firstResponseSlaHours");
        int resolutionSla = request.get("resolutionSlaHours");
        
        return ResponseEntity.ok(configService.updatePriority(id, firstResponseSla, resolutionSla));
    }

    // ==========================================
    // 2. ASSETS : PROJECTS (Nouveau)
    // ==========================================

    @GetMapping("/assets/projects")
    public ResponseEntity<List<Project>> getProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
    }

    @PostMapping("/assets/projects")
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @DeleteMapping("/assets/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 3. ASSETS : EQUIPMENT CATALOGUE (Nouveau)
    // ==========================================

    @GetMapping("/assets/equipment")
    public ResponseEntity<List<EquipmentType>> getEquipment() {
        return ResponseEntity.ok(equipmentTypeRepository.findAllByActiveTrueOrderByNameAsc());
    }

    @PostMapping("/assets/equipment")
    public ResponseEntity<EquipmentType> createEquipment(@RequestBody EquipmentType equipment) {
        return ResponseEntity.ok(equipmentTypeRepository.save(equipment));
    }

    @PutMapping("/assets/equipment/{id}")
    public ResponseEntity<EquipmentType> updateEquipment(
            @PathVariable Long id, 
            @RequestBody EquipmentType equipment) {
        equipment.setId(id);
        return ResponseEntity.ok(equipmentTypeRepository.save(equipment));
    }

    @DeleteMapping("/assets/equipment/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentTypeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}