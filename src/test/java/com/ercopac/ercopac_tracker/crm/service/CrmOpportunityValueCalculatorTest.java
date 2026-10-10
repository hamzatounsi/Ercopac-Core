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

        // 1. Total = Matériel + Services (SANS remise) = 240 000 + 80 000 = 320 000.00
        assertEquals(new BigDecimal("320000.00"), CrmOpportunityValueCalculator.total(opportunity));
        
        // 2. Discounted = (Matériel * 0.95) + Services = (240 000 * 0.95) + 80 000 = 228 000 + 80 000 = 308 000.00
        assertEquals(new BigDecimal("308000.00"), CrmOpportunityValueCalculator.discounted(opportunity));
        
        // 3. Expected Revenue Before Discount = Total = 320 000.00
        assertEquals(new BigDecimal("320000.00"), CrmOpportunityValueCalculator.expectedRevenueBeforeDiscount(opportunity));
        
        // 4. Discount Amount = Matériel * 5% = 240 000 * 0.05 = 12 000.00
        assertEquals(new BigDecimal("12000.00"), CrmOpportunityValueCalculator.discountAmount(opportunity));
        
        // 5. Expected Revenue (75%) = Discounted Total * 0.75 = 308 000 * 0.75 = 231 000.00
        assertEquals(new BigDecimal("231000.00"), CrmOpportunityValueCalculator.expectedRevenue(opportunity));
        
        // 6. Expected Revenue (50%) = 308 000 * 0.50 = 154 000.00
        opportunity.setProbability(50);
        assertEquals(new BigDecimal("154000.00"), CrmOpportunityValueCalculator.expectedRevenue(opportunity));
        
        // 7. Expected Revenue (20%) = 308 000 * 0.20 = 61 600.00
        opportunity.setProbability(20);
        assertEquals(new BigDecimal("61600.00"), CrmOpportunityValueCalculator.expectedRevenue(opportunity));
        
        // 8. Expected Revenue (100%) = 308 000 * 1.00 = 308 000.00
        opportunity.setProbability(100);
        assertEquals(new BigDecimal("308000.00"), CrmOpportunityValueCalculator.expectedRevenue(opportunity));
        
        // 9. Expected Revenue (100%, Discount 0%) = 320 000 * 1.00 = 320 000.00
        opportunity.setDiscount(BigDecimal.ZERO);
        assertEquals(new BigDecimal("320000.00"), CrmOpportunityValueCalculator.expectedRevenue(opportunity));
        
        // 10. Expected Revenue (100%, Discount 10%) = (240 000 * 0.90) + 80 000 = 216 000 + 80 000 = 296 000.00
        opportunity.setDiscount(new BigDecimal("10"));
        assertEquals(new BigDecimal("296000.00"), CrmOpportunityValueCalculator.expectedRevenue(opportunity));
        
        // 11. Split Matches (Total is 320 000.00)
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