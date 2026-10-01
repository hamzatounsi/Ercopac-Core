package com.ercopac.ercopac_tracker.crm.service;

import com.ercopac.ercopac_tracker.crm.domain.CrmOpportunity;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CrmOpportunityValueCalculator {

    /**
     * Calcule la Valeur Nette (après application de la remise).
     * C'est cette valeur qui doit être stockée dans entity.setValue()
     */
    public static BigDecimal total(CrmOpportunity opp) {
        if (opp == null) return BigDecimal.ZERO;
        
        BigDecimal material = opp.getMaterialValue() != null ? opp.getMaterialValue() : BigDecimal.ZERO;
        BigDecimal services = opp.getServicesValue() != null ? opp.getServicesValue() : BigDecimal.ZERO;
        BigDecimal baseValue = material.add(services);
        
        BigDecimal discount = opp.getDiscount() != null ? opp.getDiscount() : BigDecimal.ZERO;
        
        // Formule : Valeur de base * (1 - (Remise / 100))
        // On utilise 4 décimales pour le facteur de division afin d'éviter les pertes de précision
        BigDecimal discountFactor = BigDecimal.ONE.subtract(
            discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        
        BigDecimal netValue = baseValue.multiply(discountFactor);
        
        // On arrondit finalement à 2 décimales pour la monnaie
        return netValue.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcule le Revenu Attendu (Expected Revenue).
     * Il est basé sur la Valeur Nette (après remise), PAS sur la valeur brute.
     */
    public static BigDecimal expectedRevenue(CrmOpportunity opp) {
        if (opp == null) return BigDecimal.ZERO;
        
        // On réutilise la méthode total() qui contient déjà la logique de remise
        BigDecimal netValue = total(opp); 
        
        int probability = opp.getProbability() != null ? opp.getProbability() : 0;
        
        // Formule : Valeur Nette * (Probabilité / 100)
        BigDecimal probFactor = BigDecimal.valueOf(probability).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        
        return netValue.multiply(probFactor).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Méthode de commodité pour le DTO (retourne la même chose que total)
     */
    public static BigDecimal discounted(CrmOpportunity opp) {
        return total(opp);
    }

    /**
     * ✅ MÉTHODE MANQUANTE AJOUTÉE ICI ✅
     * Vérifie si la somme de deux valeurs (left + right) est égale à la valeur totale.
     * Utilisé pour valider les répartitions (sales split, resale split) dans CrmService.validateSplit().
     * On normalise à 2 décimales pour éviter les faux négatifs dus aux micro-différences d'arrondi.
     */
    public static boolean splitMatches(BigDecimal left, BigDecimal right, BigDecimal total) {
        if (total == null) total = BigDecimal.ZERO;
        if (left == null) left = BigDecimal.ZERO;
        if (right == null) right = BigDecimal.ZERO;
        
        BigDecimal sum = left.add(right).setScale(2, RoundingMode.HALF_UP);
        BigDecimal normalizedTotal = total.setScale(2, RoundingMode.HALF_UP);
        
        return sum.compareTo(normalizedTotal) == 0;
    }
}