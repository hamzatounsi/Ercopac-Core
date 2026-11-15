package com.ercopac.ercopac_tracker.customer.service;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.admin.repository.CustomerRepository;
import com.ercopac.ercopac_tracker.customer.dto.CustomerOptionDto;
import com.ercopac.ercopac_tracker.customer.dto.CustomerReferences;
import com.ercopac.ercopac_tracker.customer.dto.CustomerRequest;
import com.ercopac.ercopac_tracker.customer.dto.CustomerResponse;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.organisation.repository.OrganisationRepository;
import com.ercopac.ercopac_tracker.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerManagementServiceTest {

    private static final Long ORG = 10L;
    private static final Long OTHER_ORG = 20L;

    @Mock private CustomerRepository customerRepository;
    @Mock private OrganisationRepository organisationRepository;
    @Mock private CustomerUsageService usageService;
    @Mock private SecurityUtils securityUtils;

    private CustomerManagementService service;
    private Organisation organisation;

    @BeforeEach
    void setUp() {
        service = new CustomerManagementService(customerRepository, organisationRepository, usageService, securityUtils);
        organisation = new Organisation();
        organisation.setId(ORG);
        organisation.setName("Ercopac");
        when(securityUtils.getCurrentOrganisationId()).thenReturn(ORG);
        when(organisationRepository.findById(ORG)).thenReturn(Optional.of(organisation));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usageService.referencesOf(any())).thenReturn(new CustomerReferences(0, 0, 0));
    }

    @Test
    void listIsAlwaysScopedToTheAdministratorsOrganisation() {
        when(customerRepository.search(eq(ORG), anyBoolean(), anyBoolean(), anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.list("acme", null, 0, 20, "name", "asc");

        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);
        verify(customerRepository).search(eq(ORG), eq(false), eq(false), pattern.capture(), any(Pageable.class));
        assertThat(pattern.getValue()).isEqualTo("%acme%");
    }

    @Test
    void listClampsPageSizeAndFallsBackToNameSort() {
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(customerRepository.search(eq(ORG), anyBoolean(), anyBoolean(), anyString(), pageable.capture()))
                .thenReturn(new PageImpl<>(List.of()));

        service.list(null, true, -3, 10_000, "password", "desc");

        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(CustomerManagementService.MAX_PAGE_SIZE);
        assertThat(pageable.getValue().getSort().getOrderFor("name")).isNotNull();
    }

    @Test
    void createStoresUppercaseNameInCurrentOrganisationAndGeneratesCode() {
        when(customerRepository.existsByOrganisation_IdAndCustomerCodeIgnoreCase(ORG, "CUST-0001")).thenReturn(false);

        CustomerResponse created = service.create(request(null, "  acme   corp "));

        assertThat(created.name()).isEqualTo("ACME CORP");
        assertThat(created.customerCode()).isEqualTo("CUST-0001");
        assertThat(created.organisationId()).isEqualTo(ORG);
        assertThat(created.active()).isTrue();
    }

    @Test
    void duplicateNameIgnoringCaseIsRejected() {
        when(customerRepository.existsByOrganisation_IdAndNameIgnoreCase(ORG, "ACME CORP")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("C-1", "Acme  corp")))
                .isInstanceOfSatisfying(CustomerConflictException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(CustomerConflictException.NAME_EXISTS));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void renamingToTheSameNameOfItselfIsAllowed() {
        Customer existing = customer(5L, organisation, "ACME CORP", true);
        when(customerRepository.findByIdAndOrganisation_Id(5L, ORG)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByOrganisation_IdAndNameIgnoreCaseAndIdNot(ORG, "ACME CORP", 5L)).thenReturn(false);

        CustomerResponse updated = service.update(5L, request("C-1", "acme corp"));

        assertThat(updated.name()).isEqualTo("ACME CORP");
    }

    @Test
    void customerOfAnotherOrganisationIsNotFound() {
        // The repository lookup is organisation-scoped: id 7 exists only in OTHER_ORG.
        when(customerRepository.findByIdAndOrganisation_Id(7L, ORG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(7L, request("C-7", "Other")))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode().value()).isEqualTo(404));
        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex ->
                        assertThat(ex.getStatusCode().value()).isEqualTo(404));
        verify(customerRepository, never()).delete(any());
    }

    @Test
    void referencedCustomerCannotBeDeleted() {
        Customer existing = customer(5L, organisation, "ACME CORP", true);
        when(customerRepository.findByIdAndOrganisation_Id(5L, ORG)).thenReturn(Optional.of(existing));
        when(usageService.referencesOf(existing)).thenReturn(new CustomerReferences(2, 1, 0));

        assertThatThrownBy(() -> service.delete(5L))
                .isInstanceOfSatisfying(CustomerConflictException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(CustomerConflictException.IN_USE);
                    assertThat(ex.getReferences().total()).isEqualTo(3);
                });
        verify(customerRepository, never()).delete(any());
    }

    @Test
    void unreferencedCustomerIsDeleted() {
        Customer existing = customer(5L, organisation, "ACME CORP", true);
        when(customerRepository.findByIdAndOrganisation_Id(5L, ORG)).thenReturn(Optional.of(existing));

        service.delete(5L);

        verify(customerRepository).delete(existing);
    }

    @Test
    void deactivateKeepsTheCustomer() {
        Customer existing = customer(5L, organisation, "ACME CORP", true);
        when(customerRepository.findByIdAndOrganisation_Id(5L, ORG)).thenReturn(Optional.of(existing));

        CustomerResponse response = service.setActive(5L, false);

        assertThat(response.active()).isFalse();
        verify(customerRepository, never()).delete(any());
    }

    @Test
    void optionsOnlyListActiveCustomersButCanIncludeTheCurrentInactiveOne() {
        Customer active = customer(1L, organisation, "ACME CORP", true);
        Customer inactive = customer(2L, organisation, "OLD CORP", false);
        when(customerRepository.findActiveOptions(eq(ORG), eq("%"), any(Pageable.class))).thenReturn(List.of(active));
        when(customerRepository.findByIdAndOrganisation_Id(2L, ORG)).thenReturn(Optional.of(inactive));

        assertThat(service.options(null, null)).extracting(CustomerOptionDto::id).containsExactly(1L);
        assertThat(service.options(null, 2L))
                .extracting(CustomerOptionDto::id, CustomerOptionDto::active)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(2L, false), org.assertj.core.groups.Tuple.tuple(1L, true));
    }

    @Test
    void optionsNeverIncludeACustomerOfAnotherOrganisation() {
        when(customerRepository.findActiveOptions(eq(ORG), eq("%"), any(Pageable.class))).thenReturn(List.of());
        when(customerRepository.findByIdAndOrganisation_Id(99L, ORG)).thenReturn(Optional.empty());

        assertThat(service.options(null, 99L)).isEmpty();
        verify(customerRepository, never()).findActiveOptions(eq(OTHER_ORG), anyString(), any(Pageable.class));
    }

    private static CustomerRequest request(String code, String name) {
        return new CustomerRequest(code, name, null, null, null, null, null, null, null, null, true);
    }

    private static Customer customer(Long id, Organisation organisation, String name, boolean active) {
        Customer customer = new Customer();
        org.springframework.test.util.ReflectionTestUtils.setField(customer, "id", id);
        customer.setOrganisation(organisation);
        customer.setCustomerCode("C-" + id);
        customer.setName(name);
        customer.setActive(active);
        return customer;
    }
}
