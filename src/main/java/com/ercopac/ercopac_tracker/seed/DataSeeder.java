package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.domain.OrganisationStatus;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        Organisation org = organisationRepository.findByCode("PHARMA")
                .orElseGet(() -> {
                    Organisation o = new Organisation();
                    o.setName("PharmaStore GmbH");
                    o.setCode("PHARMA");
                    o.setActive(true);
                    o.setStatus(OrganisationStatus.ACTIVE);
                    o.setCountry("Germany");
                    o.setDomain("pharmastore.com");
                    o.setPlan("PROFESSIONAL");
                    o.setOrgAdminLicenceLimit(3);
                    o.setGeneralManagerLicenceLimit(5);
                    o.setDepartmentManagerLicenceLimit(10);
                    o.setEmployeeLicenceLimit(50);
                    return organisationRepository.save(o);
                });

        seedUser(
                "hamza@projectum.com",
                "Hamza Tounsi",
                "Hamza123!",
                Role.PLATFORM_OWNER,
                null
        );

        seedUser(
                "admin@pharmastore.com",
                "PharmaStore Admin",
                "Hamza123!",
                Role.ORG_ADMIN,
                org
        );

        seedUser(
                "gm@pharmastore.com",
                "PharmaStore General Manager",
                "Hamza123!",
                Role.GENERAL_MANAGER,
                org
        );

        seedUser(
                "dm@pharmastore.com",
                "Engineering Department Manager",
                "Hamza123!",
                Role.DEPARTMENT_MANAGER,
                org
        );

        seedUser(
                "employee@pharmastore.com",
                "PharmaStore Employee",
                "Hamza123!",
                Role.EMPLOYEE,
                org
        );
    }

    private void seedUser(
            String email,
            String fullName,
            String rawPassword,
            Role role,
            Organisation organisation
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            AppUser existing = userRepository.findByEmail(email).orElseThrow();
            existing.setPasswordHash(passwordEncoder.encode(rawPassword));
            existing.setActive(true);
            existing.setRole(role);
            existing.setOrganisation(organisation);
            userRepository.save(existing);
            return;
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setActive(true);
        user.setInternalUser(true);
        user.setOrganisation(organisation);
        userRepository.save(user);
    }
}