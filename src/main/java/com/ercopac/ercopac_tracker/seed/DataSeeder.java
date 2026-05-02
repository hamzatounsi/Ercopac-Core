package com.ercopac.ercopac_tracker.seed;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final OrganisationRepository organisationRepo;
    private final PasswordEncoder encoder;

    public DataSeeder(
            UserRepository userRepo,
            OrganisationRepository organisationRepo,
            PasswordEncoder encoder
    ) {
        this.userRepo = userRepo;
        this.organisationRepo = organisationRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        Organisation ercopac = createOrUpdateOrganisation("ERCOPAC", "ERCOPAC");

        createOrUpdatePlatformUser(
                "Platform Owner",
                "owner@ercopac.com",
                "Owner123!",
                Role.PLATFORM_OWNER,
                "#111827"
        );

        createOrUpdatePlatformUser(
                "Platform Admin",
                "platformadmin@ercopac.com",
                "Platform123!",
                Role.PLATFORM_ADMIN,
                "#334155"
        );

        createOrUpdateOrgUser(
                "Organisation Admin",
                "orgadmin@ercopac.com",
                "Org123!",
                Role.ORG_ADMIN,
                ercopac,
                "EXEC",
                "Organisation Administrator",
                "ADMINISTRATION",
                "SENIOR",
                "#875a7b"
        );

        createOrUpdateOrgUser(
                "General Manager",
                "gm@ercopac.com",
                "Gm123!",
                Role.GENERAL_MANAGER,
                ercopac,
                "EXEC",
                "General Manager",
                "MANAGEMENT",
                "EXECUTIVE",
                "#2563eb"
        );

        createOrUpdateOrgUser(
                "PMO User",
                "pmo@ercopac.com",
                "Pmo123!",
                Role.PMO,
                ercopac,
                "PMO",
                "PMO Officer",
                "PMO",
                "SENIOR",
                "#0f766e"
        );

        createOrUpdateOrgUser(
                "Department Manager",
                "dm@ercopac.com",
                "Dm123!",
                Role.DEPARTMENT_MANAGER,
                ercopac,
                "ME",
                "Mechanical Department Manager",
                "MANAGEMENT",
                "SENIOR",
                "#0891b2"
        );

        createOrUpdateOrgUser(
                "Employee User",
                "employee@ercopac.com",
                "Employee123!",
                Role.EMPLOYEE,
                ercopac,
                "ME",
                "Mechanical Engineer",
                "ME",
                "MID",
                "#22d3ee"
        );
    }

    private Organisation createOrUpdateOrganisation(String name, String code) {
        Organisation organisation = organisationRepo.findByCode(code).orElseGet(Organisation::new);
        organisation.setName(name);
        organisation.setCode(code);
        organisation.setActive(true);
        return organisationRepo.save(organisation);
    }

    private AppUser createOrUpdatePlatformUser(
            String fullName,
            String email,
            String rawPassword,
            Role role,
            String color
    ) {
        AppUser user = userRepo.findByEmail(email).orElseGet(AppUser::new);

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setRole(role);
        user.setOrganisation(null);
        user.setDepartmentCode(null);
        user.setJobTitle(role.name());
        user.setResourceType("PLATFORM");
        user.setSeniority("EXECUTIVE");
        user.setHoursPerDay(8);
        user.setDaysPerWeek(5);
        user.setWorkdays("MON-FRI");
        user.setColor(color);
        user.setInternalUser(true);
        user.setActive(true);

        return userRepo.save(user);
    }

    private AppUser createOrUpdateOrgUser(
            String fullName,
            String email,
            String rawPassword,
            Role role,
            Organisation organisation,
            String departmentCode,
            String jobTitle,
            String resourceType,
            String seniority,
            String color
    ) {
        AppUser user = userRepo.findByEmail(email).orElseGet(AppUser::new);

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setRole(role);
        user.setOrganisation(organisation);
        user.setDepartmentCode(departmentCode);
        user.setJobTitle(jobTitle);
        user.setResourceType(resourceType);
        user.setSeniority(seniority);
        user.setHoursPerDay(8);
        user.setDaysPerWeek(5);
        user.setWorkdays("MON-FRI");
        user.setColor(color);
        user.setInternalUser(true);
        user.setActive(true);

        return userRepo.save(user);
    }
}