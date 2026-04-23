package com.ercopac.ercopac_tracker.department.service;

import com.ercopac.ercopac_tracker.department.domain.DepartmentHoliday;
import com.ercopac.ercopac_tracker.department.repository.DepartmentHolidayRepository;
import com.ercopac.ercopac_tracker.department_dashboard.dto.DepartmentHolidayDto;
import com.ercopac.ercopac_tracker.department_dashboard.request.CreateDepartmentHolidayRequest;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DepartmentHolidayService {

    private final DepartmentHolidayRepository departmentHolidayRepository;
    private final UserRepository userRepository;

    public DepartmentHolidayService(DepartmentHolidayRepository departmentHolidayRepository,
                                    UserRepository userRepository) {
        this.departmentHolidayRepository = departmentHolidayRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentHolidayDto> findHolidays(Long organisationId, List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        return departmentHolidayRepository.findByOrganisationIdAndMember_IdIn(organisationId, memberIds)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public DepartmentHolidayDto createHoliday(CreateDepartmentHolidayRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null.");
        }
        if (request.memberId() == null) {
            throw new IllegalArgumentException("Member ID is required.");
        }
        if (request.fromDate() == null || request.toDate() == null) {
            throw new IllegalArgumentException("From/to dates are required.");
        }
        if (request.toDate().isBefore(request.fromDate())) {
            throw new IllegalArgumentException("To date cannot be before from date.");
        }

        Long currentOrgId = getCurrentOrganisationIdOrThrow();
        Long currentUserId = getCurrentUserIdOrNull();

        AppUser member = userRepository.findById(request.memberId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + request.memberId()));

        Long memberOrgId = extractOrganisationId(member);
        if (memberOrgId != null && !memberOrgId.equals(currentOrgId)) {
            throw new AccessDeniedException("Cannot create holiday for member of another organisation.");
        }

        DepartmentHoliday holiday = new DepartmentHoliday();
        holiday.setOrganisationId(currentOrgId);
        holiday.setMember(member);
        holiday.setFromDate(request.fromDate());
        holiday.setToDate(request.toDate());
        holiday.setNote(request.note());
        holiday.setCreatedBy(currentUserId);

        DepartmentHoliday saved = departmentHolidayRepository.save(holiday);
        return toDto(saved);
    }

    public void deleteHoliday(Long holidayId) {
        Long currentOrgId = getCurrentOrganisationIdOrThrow();

        DepartmentHoliday holiday = departmentHolidayRepository.findByIdAndOrganisationId(holidayId, currentOrgId)
                .orElseThrow(() -> new EntityNotFoundException("Holiday not found with id: " + holidayId));

        departmentHolidayRepository.delete(holiday);
    }

    private DepartmentHolidayDto toDto(DepartmentHoliday holiday) {
        return new DepartmentHolidayDto(
                holiday.getId(),
                safeLong(readProperty(holiday.getMember(), "id")),
                safeString(firstNonNull(
                        readProperty(holiday.getMember(), "fullName"),
                        readProperty(holiday.getMember(), "name"),
                        readProperty(holiday.getMember(), "username"),
                        readProperty(holiday.getMember(), "email")
                )),
                holiday.getFromDate(),
                holiday.getToDate(),
                holiday.getNote()
        );
    }

    private Long getCurrentOrganisationIdOrThrow() {
        Object value = authDetails().get("organisationId");
        if (value == null) {
            throw new AccessDeniedException("Organisation ID not found in authentication details.");
        }
        return Long.valueOf(value.toString());
    }

    private Long getCurrentUserIdOrNull() {
        Object value = authDetails().get("userId");
        return value == null ? null : Long.valueOf(value.toString());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> authDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user found.");
        }
        Object details = authentication.getDetails();
        if (!(details instanceof Map<?, ?>)) {
            throw new AccessDeniedException("Authentication details are missing.");
        }
        return (Map<String, Object>) details;
    }

    private Long extractOrganisationId(Object bean) {
        Object organisationId = readProperty(bean, "organisationId");
        if (organisationId != null) {
            return safeLong(organisationId);
        }

        Object organisation = readProperty(bean, "organisation");
        if (organisation != null) {
            return safeLong(readProperty(organisation, "id"));
        }

        return null;
    }

    private Object readProperty(Object bean, String property) {
        if (bean == null || property == null) {
            return null;
        }
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (pd.getName().equals(property)) {
                return wrapper.getPropertyValue(property);
            }
        }
        return null;
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Long safeLong(Object value) {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private String safeString(Object value) {
        return value == null ? null : value.toString();
    }
}