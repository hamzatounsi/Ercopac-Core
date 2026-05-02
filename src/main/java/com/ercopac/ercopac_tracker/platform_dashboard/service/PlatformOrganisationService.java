package com.ercopac.ercopac_tracker.platform_dashboard.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.CreateOrganisationWithAdminRequest;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.CreateOrganisationWithAdminResponse;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PlatformOrganisationService {

    private final OrganisationRepository organisationRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public PlatformOrganisationService(
            OrganisationRepository organisationRepo,
            UserRepository userRepo,
            PasswordEncoder encoder
    ) {
        this.organisationRepo = organisationRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Transactional
    public CreateOrganisationWithAdminResponse createOrganisationWithAdmin(
            CreateOrganisationWithAdminRequest request
    ) {
        if (request.organisationName == null || request.organisationName.isBlank()) {
            throw new IllegalArgumentException("Organisation name is required");
        }

        if (request.organisationCode == null || request.organisationCode.isBlank()) {
            throw new IllegalArgumentException("Organisation code is required");
        }

        if (request.adminEmail == null || request.adminEmail.isBlank()) {
            throw new IllegalArgumentException("Admin email is required");
        }

        if (request.adminPassword == null || request.adminPassword.length() < 6) {
            throw new IllegalArgumentException("Admin password must contain at least 6 characters");
        }

        String orgCode = request.organisationCode.trim().toUpperCase();

        organisationRepo.findByCode(orgCode).ifPresent(existing -> {
            throw new IllegalArgumentException("Organisation code already exists");
        });

        userRepo.findByEmail(request.adminEmail.trim().toLowerCase()).ifPresent(existing -> {
            throw new IllegalArgumentException("Admin email already exists");
        });

        Organisation organisation = new Organisation();
        organisation.setName(request.organisationName.trim());
        organisation.setCode(orgCode);
        organisation.setActive(true);
        organisation = organisationRepo.save(organisation);

        AppUser admin = new AppUser();
        admin.setFullName(request.adminFullName);
        admin.setEmail(request.adminEmail.trim().toLowerCase());
        admin.setPasswordHash(encoder.encode(request.adminPassword));
        admin.setRole(Role.ORG_ADMIN);
        admin.setOrganisation(organisation);
        admin.setDepartmentCode("EXEC");
        admin.setJobTitle("Organisation Administrator");
        admin.setResourceType("ADMINISTRATION");
        admin.setSeniority("SENIOR");
        admin.setHoursPerDay(8);
        admin.setDaysPerWeek(5);
        admin.setWorkdays("MON-FRI");
        admin.setColor("#875a7b");
        admin.setInternalUser(true);
        admin.setActive(true);

        admin = userRepo.save(admin);

        return new CreateOrganisationWithAdminResponse(
                organisation.getId(),
                organisation.getName(),
                organisation.getCode(),
                admin.getId(),
                admin.getEmail()
        );
    }
}