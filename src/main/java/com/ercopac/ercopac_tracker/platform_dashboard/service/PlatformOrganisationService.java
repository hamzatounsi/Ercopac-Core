package com.ercopac.ercopac_tracker.platform_dashboard.service;

import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.domain.OrganisationStatus;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.CreateOrganisationWithAdminRequest;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.CreateOrganisationWithAdminResponse;
import com.ercopac.ercopac_tracker.platform_dashboard.dto.PlatformOrganisationDto;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
        validateCreateRequest(request);

        String orgCode = request.organisationCode.trim().toUpperCase();
        String adminEmail = request.adminEmail.trim().toLowerCase();

        organisationRepo.findByCode(orgCode).ifPresent(existing -> {
            throw new IllegalArgumentException("Organisation code already exists");
        });

        userRepo.findByEmail(adminEmail).ifPresent(existing -> {
            throw new IllegalArgumentException("Admin email already exists");
        });

        Organisation organisation = new Organisation();
        organisation.setName(request.organisationName.trim());
        organisation.setCode(orgCode);
        organisation.setCountry(request.country);
        organisation.setDomain(request.domain);
        organisation.setPlan(request.plan != null ? request.plan : "STARTER");
        organisation.setStatus(OrganisationStatus.TRIAL);
        organisation.setActive(true);
        organisation.setMonthlyRevenue(request.monthlyRevenue);
        organisation.setHealthScore(100);
        organisation.setBillingEmail(request.billingEmail);
        organisation.setVatNumber(request.vatNumber);
        organisation.setPaymentMethod(
                request.paymentMethod != null ? request.paymentMethod : "SEPA_DIRECT_DEBIT"
        );
        organisation.setWarehouseLimit(request.warehouseLimit);
        organisation.setUserLimit(request.userLimit);
        organisation.setAdminLicenceLimit(request.adminLicenceLimit);
        organisation.setSpecialistLicenceLimit(request.specialistLicenceLimit);
        organisation.setSupervisorLicenceLimit(request.supervisorLicenceLimit);
        organisation.setOperatorLicenceLimit(request.operatorLicenceLimit);
        organisation.setReadonlyLicenceLimit(request.readonlyLicenceLimit);

        organisation.setHealthScore(request.healthScore);

        organisation.setBillingEmail(request.billingEmail);
        organisation.setVatNumber(request.vatNumber);
        organisation.setPaymentMethod(request.paymentMethod);

        organisation.setForce2faAdmins(request.force2faAdmins);
        organisation.setForce2faSpecialists(request.force2faSpecialists);
        organisation.setForce2faOperators(request.force2faOperators);
        organisation.setDefault2faMethod(request.default2faMethod);
        organisation.setSessionTimeout(request.sessionTimeout);
        organisation.setMaxFailedLogins(request.maxFailedLogins);
        organisation.setPasswordMinLength(request.passwordMinLength);
        organisation.setPasswordExpiry(request.passwordExpiry);

        organisation.setInternalNotes(request.internalNotes);
        organisation.setFlagAtRisk(request.flagAtRisk);
        organisation.setFlagPaymentOverdue(request.flagPaymentOverdue);
        organisation.setFlagUpsellOpportunity(request.flagUpsellOpportunity);
        organisation.setFlagVipPriority(request.flagVipPriority);
        organisation.setFlagPilotFeatures(request.flagPilotFeatures);
        organisation.setFlagUnderReview(request.flagUnderReview);

        organisation = organisationRepo.save(organisation);

        AppUser admin = new AppUser();
        admin.setFullName(request.adminFullName != null ? request.adminFullName.trim() : "Organisation Admin");
        admin.setEmail(adminEmail);
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

    public List<PlatformOrganisationDto> getAllOrganisations() {
        return organisationRepo.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public PlatformOrganisationDto getOrganisation(Long id) {
        Organisation organisation = findOrganisation(id);
        return toDto(organisation);
    }

    @Transactional
    public PlatformOrganisationDto updateOrganisation(
            Long id,
            CreateOrganisationWithAdminRequest request
    ) {
        Organisation organisation = findOrganisation(id);

        if (request.organisationName != null && !request.organisationName.isBlank()) {
            organisation.setName(request.organisationName.trim());
        }

        if (request.organisationCode != null && !request.organisationCode.isBlank()) {
            String newCode = request.organisationCode.trim().toUpperCase();

            organisationRepo.findByCode(newCode).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Organisation code already exists");
                }
            });

            organisation.setCode(newCode);
        }

        if (request.country != null) {
            organisation.setCountry(request.country.trim());
        }

        if (request.domain != null) {
            organisation.setDomain(request.domain.trim());
        }

        if (request.plan != null && !request.plan.isBlank()) {
            organisation.setPlan(request.plan.trim().toUpperCase());
        }
        if (request.billingEmail != null) {
        organisation.setBillingEmail(request.billingEmail.trim());
        }

        if (request.vatNumber != null) {
            organisation.setVatNumber(request.vatNumber.trim());
        }

        if (request.paymentMethod != null) {
            organisation.setPaymentMethod(request.paymentMethod.trim());
        }

        if (request.adminFullName != null) {
            userRepo.findFirstByOrganisationIdAndRole(id, Role.ORG_ADMIN)
                    .ifPresent(admin -> {
                        admin.setFullName(request.adminFullName.trim());
                        userRepo.save(admin);
                    });
        }

        organisation.setMonthlyRevenue(request.monthlyRevenue);
        organisation.setWarehouseLimit(request.warehouseLimit);
        organisation.setUserLimit(request.userLimit);

        organisation.setAdminLicenceLimit(request.adminLicenceLimit);
        organisation.setSpecialistLicenceLimit(request.specialistLicenceLimit);
        organisation.setSupervisorLicenceLimit(request.supervisorLicenceLimit);
        organisation.setOperatorLicenceLimit(request.operatorLicenceLimit);
        organisation.setReadonlyLicenceLimit(request.readonlyLicenceLimit);

        organisation.setHealthScore(request.healthScore);

        organisation.setForce2faAdmins(request.force2faAdmins);
        organisation.setForce2faSpecialists(request.force2faSpecialists);
        organisation.setForce2faOperators(request.force2faOperators);
        organisation.setDefault2faMethod(request.default2faMethod);
        organisation.setSessionTimeout(request.sessionTimeout);
        organisation.setMaxFailedLogins(request.maxFailedLogins);
        organisation.setPasswordMinLength(request.passwordMinLength);
        organisation.setPasswordExpiry(request.passwordExpiry);

        organisation.setInternalNotes(request.internalNotes);
        organisation.setFlagAtRisk(request.flagAtRisk);
        organisation.setFlagPaymentOverdue(request.flagPaymentOverdue);
        organisation.setFlagUpsellOpportunity(request.flagUpsellOpportunity);
        organisation.setFlagVipPriority(request.flagVipPriority);
        organisation.setFlagPilotFeatures(request.flagPilotFeatures);
        organisation.setFlagUnderReview(request.flagUnderReview);

        return toDto(organisationRepo.save(organisation));
    }

    @Transactional
    public PlatformOrganisationDto updateStatus(Long id, String status) {
        Organisation organisation = findOrganisation(id);

        try {
            organisation.setStatus(OrganisationStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid organisation status: " + status);
        }

        return toDto(organisationRepo.save(organisation));
    }

    private Organisation findOrganisation(Long id) {
        return organisationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found"));
    }

    private void validateCreateRequest(CreateOrganisationWithAdminRequest request) {
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
    }

    private PlatformOrganisationDto toDto(Organisation organisation) {
        PlatformOrganisationDto dto = new PlatformOrganisationDto();

        dto.id = organisation.getId();
        dto.name = organisation.getName();
        dto.code = organisation.getCode();
        dto.country = organisation.getCountry();
        dto.domain = organisation.getDomain();
        dto.status = organisation.getStatus();
        dto.plan = organisation.getPlan();
        dto.warehouseLimit = organisation.getWarehouseLimit();
        dto.userLimit = organisation.getUserLimit();

        dto.adminLicenceLimit = organisation.getAdminLicenceLimit();
        dto.specialistLicenceLimit = organisation.getSpecialistLicenceLimit();
        dto.supervisorLicenceLimit = organisation.getSupervisorLicenceLimit();
        dto.operatorLicenceLimit = organisation.getOperatorLicenceLimit();
        dto.readonlyLicenceLimit = organisation.getReadonlyLicenceLimit();

        dto.monthlyRevenue = organisation.getMonthlyRevenue();
        dto.healthScore = organisation.getHealthScore();
        dto.createdAt = organisation.getCreatedAt();

        dto.billingEmail = organisation.getBillingEmail();
        dto.vatNumber = organisation.getVatNumber();
        dto.paymentMethod = organisation.getPaymentMethod();

        dto.force2faAdmins = organisation.getForce2faAdmins();
        dto.force2faSpecialists = organisation.getForce2faSpecialists();
        dto.force2faOperators = organisation.getForce2faOperators();
        dto.default2faMethod = organisation.getDefault2faMethod();
        dto.sessionTimeout = organisation.getSessionTimeout();
        dto.maxFailedLogins = organisation.getMaxFailedLogins();
        dto.passwordMinLength = organisation.getPasswordMinLength();
        dto.passwordExpiry = organisation.getPasswordExpiry();

        dto.internalNotes = organisation.getInternalNotes();
        dto.flagAtRisk = organisation.isFlagAtRisk();
        dto.flagPaymentOverdue = organisation.isFlagPaymentOverdue();
        dto.flagUpsellOpportunity = organisation.isFlagUpsellOpportunity();
        dto.flagVipPriority = organisation.isFlagVipPriority();
        dto.flagPilotFeatures = organisation.isFlagPilotFeatures();
        dto.flagUnderReview = organisation.isFlagUnderReview();
        userRepo.findFirstByOrganisationIdAndRole(
                organisation.getId(),
                Role.ORG_ADMIN
        ).ifPresent(admin -> dto.adminFullName = admin.getFullName());

        return dto;
    }
}