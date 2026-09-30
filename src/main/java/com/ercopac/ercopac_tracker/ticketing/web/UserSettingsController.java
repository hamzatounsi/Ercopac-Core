package com.ercopac.ercopac_tracker.ticketing.web;

import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform-settings/users")
public class UserSettingsController {

    private final UserRepository userRepository;

    public UserSettingsController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/customers")
    public ResponseEntity<List<AppUser>> getCustomers() {
        List<AppUser> customers = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(Role.CLIENT))
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/h24-agents")
    public ResponseEntity<List<AppUser>> getH24Agents() {
        List<AppUser> agents = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(Role.H24) || u.getRoles().contains(Role.H24_LEAD))
                .collect(Collectors.toList());
        return ResponseEntity.ok(agents);
    }
    @PatchMapping("/{id}/promote-l2")
    public ResponseEntity<Void> promoteToL2(@PathVariable Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setL2Technician(true);
        userRepository.save(user);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/l2-technicians")
    public ResponseEntity<List<AppUser>> getL2Technicians() {
        List<AppUser> techs = userRepository.findAll().stream()
                // ✅ CORRECTION : isL2Technician() est un boolean primitif, pas besoin de vérifier != null
                .filter(u -> u.isL2Technician()) 
                .collect(Collectors.toList());
        return ResponseEntity.ok(techs);
    }
}