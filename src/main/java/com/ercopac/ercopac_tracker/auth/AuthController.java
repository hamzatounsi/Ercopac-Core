package com.ercopac.ercopac_tracker.auth;

import com.ercopac.ercopac_tracker.security.JwtService;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.ercopac.ercopac_tracker.auth.AuthDtos.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, UserRepository userRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        AppUser user = userRepository.findByEmail(req.username())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Long organisationId = user.getOrganisation() != null ? user.getOrganisation().getId() : null;
        String organisationCode = user.getOrganisation() != null ? user.getOrganisation().getCode() : null;
        String organisationName = user.getOrganisation() != null ? user.getOrganisation().getName() : null;

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                organisationId
        );

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                organisationId,
                organisationCode,
                organisationName
        );
    }
}