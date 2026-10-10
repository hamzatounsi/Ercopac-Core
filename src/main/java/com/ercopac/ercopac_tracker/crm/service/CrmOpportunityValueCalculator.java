package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Authoritative CRM opportunity value calculations. */
public final class CrmOpportunityValueCalculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private CrmOpportunityValueCalculator() { }

    /**
     * ✅ TOTAL VALUE : Matériel + Services (SANS remise)
     */
    public static BigDecimal total(CrmOpportunity opportunity) {
        return money(zero(opportunity.getMaterialValue()).add(zero(opportunity.getServicesValue())));
    }

    /**
     * ✅ EXPECTED REVENUE : Applique la remise UNIQUEMENT sur le matériel + probabilité
     */
    public static BigDecimal expectedRevenue(CrmOpportunity opportunity) {
        if (opportunity == null) return BigDecimal.ZERO;
        
        BigDecimal material = zero(opportunity.getMaterialValue());
        BigDecimal services = zero(opportunity.getServicesValue());
        BigDecimal discount = zero(opportunity.getDiscount());
        int probability = opportunity.getProbability() != null ? opportunity.getProbability() : 0;

        // 1. Appliquer la remise UNIQUEMENT sur le matériel
        BigDecimal discountFactor = BigDecimal.ONE.subtract(
            discount.divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP)
        );
        BigDecimal discountedMaterial = material.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
        
        // 2. Valeur nette = Matériel remis + Services (sans remise)
        BigDecimal netValue = discountedMaterial.add(services);
        
        // 3. Appliquer la probabilité
        BigDecimal probFactor = BigDecimal.valueOf(probability).divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);
        return money(netValue.multiply(probFactor));
    }

    /**
     * ✅ VALEUR APRÈS REMISE (sans probabilité) - pour affichage
     */
    public static BigDecimal discounted(CrmOpportunity opportunity) {
        if (opportunity == null) return BigDecimal.ZERO;
        
        BigDecimal material = zero(opportunity.getMaterialValue());
        BigDecimal services = zero(opportunity.getServicesValue());
        BigDecimal discount = zero(opportunity.getDiscount());

        BigDecimal discountFactor = BigDecimal.ONE.subtract(
            discount.divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP)
        );
        BigDecimal discountedMaterial = material.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
        
        return money(discountedMaterial.add(services));
    }

    public static BigDecimal expectedRevenueBeforeDiscount(CrmOpportunity opportunity) {
        return total(opportunity);
    }

    public static BigDecimal discountAmount(CrmOpportunity opportunity) {
        if (opportunity == null) return BigDecimal.ZERO;
        
        BigDecimal material = zero(opportunity.getMaterialValue());
        BigDecimal discount = zero(opportunity.getDiscount());
        
        // Remise UNIQUEMENT sur le matériel
        return money(material.multiply(discount).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP));
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