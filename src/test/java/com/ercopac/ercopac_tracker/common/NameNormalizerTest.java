package com.ercopac.ercopac_tracker.common;

import com.ercopac.ercopac_tracker.admin.domain.Customer;
import com.ercopac.ercopac_tracker.organisation.domain.Organisation;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.domain.Supplier;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class NameNormalizerTest {

    @Test
    void upperTrimsCollapsesAndUppercases() {
        assertThat(NameNormalizer.upper("  acme   steel\tworks ")).isEqualTo("ACME STEEL WORKS");
    }

    @Test
    void blankBecomesNull() {
        assertThat(NameNormalizer.upper(null)).isNull();
        assertThat(NameNormalizer.upper("   ")).isNull();
    }

    @Test
    void uppercaseDoesNotDependOnServerLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(NameNormalizer.upper("istanbul")).isEqualTo("ISTANBUL");
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void matchKeyIgnoresCaseAndSpacing() {
        assertThat(NameNormalizer.matchKey("Acme  Corp")).isEqualTo(NameNormalizer.matchKey(" ACME corp "));
    }

    @Test
    void entitySettersStoreUppercaseNames() {
        Customer customer = new Customer();
        customer.setName(" acme corp ");
        Supplier supplier = new Supplier();
        supplier.setName("steel  partners");
        Organisation organisation = new Organisation();
        organisation.setName("ercopac");
        AppUser user = new AppUser("jane doe", "Jane.Doe@Example.com", "hash", Role.EMPLOYEE);

        assertThat(customer.getName()).isEqualTo("ACME CORP");
        assertThat(supplier.getName()).isEqualTo("STEEL PARTNERS");
        assertThat(organisation.getName()).isEqualTo("ERCOPAC");
        assertThat(user.getFullName()).isEqualTo("JANE DOE");
        // Logins and emails are never uppercased.
        assertThat(user.getEmail()).isEqualTo("Jane.Doe@Example.com");
    }
}
