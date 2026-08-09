package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Creates the one production platform owner when it is missing. */
@Component
public class DefaultPlatformOwnerBootstrap implements CommandLineRunner {

    static final String DEFAULT_OWNER_EMAIL = "owner@projectum.com";
    static final String DEFAULT_OWNER_PASSWORD = "Projectum123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultPlatformOwnerBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmailIgnoreCase(DEFAULT_OWNER_EMAIL).isPresent()) {
            return;
        }

        AppUser owner = new AppUser(
                "Platform Owner",
                DEFAULT_OWNER_EMAIL,
                passwordEncoder.encode(DEFAULT_OWNER_PASSWORD),
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
