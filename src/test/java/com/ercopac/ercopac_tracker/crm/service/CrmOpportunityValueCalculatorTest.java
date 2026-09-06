package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrmOpportunityValueCalculatorTest {
    @Test
    void calculatesSupervisorScenariosWithCurrencyPrecision() {
        CrmOpportunity opportunity = new CrmOpportunity();
        opportunity.setMaterialValue(new BigDecimal("240000"));
        opportunity.setServicesValue(new BigDecimal("80000"));
        opportunity.setDiscount(new BigDecimal("5"));
        opportunity.setProbability(75);

        assertEquals(new BigDecimal("320000.00"), CrmOpportunityValueCalculator.total(opportunity));
        assertEquals(new BigDecimal("304000.00"), CrmOpportunityValueCalculator.discounted(opportunity));
        assertEquals(new BigDecimal("228000.00"), CrmOpportunityValueCalculator.expectedRevenue(opportunity));
        assertEquals(true, CrmOpportunityValueCalculator.splitMatches(
                new BigDecimal("200000"), new BigDecimal("120000"), CrmOpportunityValueCalculator.total(opportunity)));
        assertEquals(true, CrmOpportunityValueCalculator.splitMatches(
                new BigDecimal("160000"), new BigDecimal("160000"), CrmOpportunityValueCalculator.total(opportunity)));
        assertEquals(false, CrmOpportunityValueCalculator.splitMatches(
                new BigDecimal("100000"), new BigDecimal("100000"), CrmOpportunityValueCalculator.total(opportunity)));
    }

    @Test
    void nullValuesAndDiscountRemainSafeForExistingRecords() {
        CrmOpportunity opportunity = new CrmOpportunity();
        opportunity.setDiscount(null);
        opportunity.setProbability(null);

        assertEquals(new BigDecimal("0.00"), CrmOpportunityValueCalculator.total(opportunity));
        assertEquals(new BigDecimal("0.00"), CrmOpportunityValueCalculator.expectedRevenue(opportunity));
    }
}
