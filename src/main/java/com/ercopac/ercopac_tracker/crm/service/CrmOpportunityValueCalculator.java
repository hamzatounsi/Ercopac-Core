package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Authoritative CRM opportunity value calculations. */
public final class CrmOpportunityValueCalculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private CrmOpportunityValueCalculator() { }

    /**
     * Calcule la Valeur Totale Nette.
     * La remise s'applique UNIQUEMENT à la valeur matériel. Les services ne sont pas remisés.
     */
    public static BigDecimal total(CrmOpportunity opportunity) {
        BigDecimal material = zero(opportunity.getMaterialValue());
        BigDecimal services = zero(opportunity.getServicesValue());
        BigDecimal discount = zero(opportunity.getDiscount());

        // 1. Appliquer la remise uniquement sur le matériel
        BigDecimal discountedMaterial = material
                .multiply(ONE_HUNDRED.subtract(discount))
                .divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP);

        // 2. La valeur totale est : Matériel (avec remise) + Services (sans remise)
        return money(discountedMaterial.add(services));
    }

    /**
     * Retourne la valeur après remise (identique à total() désormais, 
     * car total() inclut déjà la logique de remise sur le matériel).
     */
    public static BigDecimal discounted(CrmOpportunity opportunity) {
        return total(opportunity);
    }

    /**
     * Calcule le revenu attendu en appliquant la probabilité sur la valeur totale nette.
     */
    public static BigDecimal expectedRevenue(CrmOpportunity opportunity) {
        int probability = opportunity.getProbability() != null ? opportunity.getProbability() : 0;
        BigDecimal probFactor = BigDecimal.valueOf(probability);
        
        return money(total(opportunity)
                .multiply(probFactor)
                .divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP));
    }

    /**
     * Pour compatibilité : retourne la valeur totale (qui est déjà la valeur nette).
     */
    public static BigDecimal expectedRevenueBeforeDiscount(CrmOpportunity opportunity) {
        return total(opportunity);
    }

    /**
     * Calcule le montant exact de la remise (uniquement sur le matériel).
     */
    public static BigDecimal discountAmount(CrmOpportunity opportunity) {
        BigDecimal material = zero(opportunity.getMaterialValue());
        BigDecimal discount = zero(opportunity.getDiscount());
        
        return money(material
                .multiply(discount)
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