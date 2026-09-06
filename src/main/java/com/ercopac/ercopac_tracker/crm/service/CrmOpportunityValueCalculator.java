package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Authoritative CRM opportunity value calculations. */
public final class CrmOpportunityValueCalculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private CrmOpportunityValueCalculator() { }

    public static BigDecimal total(CrmOpportunity opportunity) {
        return money(zero(opportunity.getMaterialValue()).add(zero(opportunity.getServicesValue())));
    }

    public static BigDecimal discounted(CrmOpportunity opportunity) {
        BigDecimal discount = zero(opportunity.getDiscount());
        return money(total(opportunity)
                .multiply(ONE_HUNDRED.subtract(discount))
                .divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP));
    }

    public static BigDecimal expectedRevenue(CrmOpportunity opportunity) {
        int probability = opportunity.getProbability() == null ? 0 : opportunity.getProbability();
        return money(discounted(opportunity)
                .multiply(BigDecimal.valueOf(probability))
                .divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP));
    }

    public static boolean splitMatches(BigDecimal left, BigDecimal right, BigDecimal total) {
        if (left == null && right == null) return true;
        return money(zero(left).add(zero(right))).compareTo(money(total)) == 0;
    }

    public static BigDecimal money(BigDecimal value) {
        return zero(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
