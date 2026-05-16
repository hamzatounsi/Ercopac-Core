package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public DataSeeder(
            UserRepository userRepo,
            PasswordEncoder encoder
    ) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {

        createOrUpdatePlatformOwner(
                "Hamza Tounsi",
                "hamza@projectum.com",
                "Hamza123!"
        );
    }

    private AppUser createOrUpdatePlatformOwner(
            String fullName,
            String email,
            String rawPassword
    ) {

        AppUser user = userRepo.findByEmail(email)
                .orElseGet(AppUser::new);

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(rawPassword));

        user.setRole(Role.PLATFORM_OWNER);

        // PLATFORM USER = NO ORGANISATION
        user.setOrganisation(null);

        user.setDepartmentCode(null);
        user.setEmployeeCode(null);
        user.setJobTitle("PLATFORM OWNER");

        user.setResourceType(null);

        user.setSeniority("EXECUTIVE");

        user.setHoursPerDay(8);
        user.setDaysPerWeek(5);
        user.setWorkdays("MON-FRI");

        user.setColor("#111827");

        user.setInternalUser(true);
        user.setActive(true);

        return userRepo.save(user);
    }
}