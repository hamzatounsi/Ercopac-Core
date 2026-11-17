package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/** Creates the one production platform owner when it is missing. */
@Component
@ConditionalOnProperty(name = "app.bootstrap-owner.enabled", havingValue = "true", matchIfMissing = true)
public class DefaultPlatformOwnerBootstrap implements CommandLineRunner {

    static final String DEFAULT_OWNER_EMAIL = "owner@projectum.com";
    static final String DEFAULT_OWNER_PASSWORD = "Projectum123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String ownerEmail;
    private final String ownerPassword;

    public DefaultPlatformOwnerBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-owner.email:" + DEFAULT_OWNER_EMAIL + "}") String ownerEmail,
            @Value("${app.bootstrap-owner.password:" + DEFAULT_OWNER_PASSWORD + "}") String ownerPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ownerEmail = ownerEmail;
        this.ownerPassword = ownerPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmailIgnoreCase(ownerEmail).isPresent()) {
            return;
        }

        AppUser owner = new AppUser(
                "Platform Owner",
                ownerEmail,
                passwordEncoder.encode(ownerPassword),
                Role.PLATFORM_OWNER
        );
        owner.setActive(true);
        owner.setOrganisation(null);
        owner.setDepartment(null);
        owner.setResourceType(null);
        owner.setInternalUser(false);
        owner.setJobTitle("Platform Owner");
        userRepository.save(owner);
    }
}
