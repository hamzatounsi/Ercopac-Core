package com.ercopac.ercopac_tracker.projectum.finance.service;

import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceWbsHierarchyTest {
    private FinanceEntry row(long id, int level, Long parentId) {
        FinanceEntry row = new FinanceEntry();
        row.setId(id);
        row.setLevel(level);
        row.setParentId(parentId);
        return row;
    }

    @Test
    void derivesLegacyParentsAndHonoursPersistedMoves() {
        FinanceEntry root = row(1, 1, null);
        FinanceEntry mechanical = row(2, 2, null);
        FinanceEntry electrical = row(3, 2, null);
        FinanceEntry expense = row(4, 3, null);
        List<FinanceEntry> rows = List.of(root, mechanical, electrical, expense);
        assertEquals(3L, FinanceWbsHierarchy.parents(rows).get(4L));
        expense.setParentId(2L);
        assertEquals(2L, FinanceWbsHierarchy.parents(rows).get(4L));
    }

    @Test
    void persistedCycleFallsBackToTheOrderDerivedParent() {
        FinanceEntry root = row(1, 1, null);
        FinanceEntry a = row(2, 2, 3L);
        FinanceEntry b = row(3, 3, 2L);
        Map<Long, Long> parents = FinanceWbsHierarchy.parents(List.of(root, a, b));
        assertEquals(1L, parents.get(2L));
        assertEquals(2L, parents.get(3L));
        assertTrue(FinanceWbsHierarchy.isSelfOrDescendant(3L, 2L, parents));
        assertFalse(FinanceWbsHierarchy.isSelfOrDescendant(1L, 2L, parents));
    }

    @Test
    void rollupSumsLeavesOnlyAcrossAllLevels() {
        Map<Long, Long> parents = new HashMap<>();
        parents.put(1L, null);
        parents.put(2L, 1L);
        parents.put(3L, 2L);
        parents.put(4L, 2L);
        parents.put(5L, 1L);
        Map<Long, BigDecimal> values = Map.of(1L, BigDecimal.valueOf(9999), 2L, BigDecimal.valueOf(9999),
                3L, BigDecimal.valueOf(500), 4L, BigDecimal.valueOf(70), 5L, BigDecimal.valueOf(30));
        Map<Long, BigDecimal> totals = FinanceWbsHierarchy.rollup(parents.keySet(), parents, values);
        assertEquals(0, BigDecimal.valueOf(570).compareTo(totals.get(2L)));
        assertEquals(0, BigDecimal.valueOf(600).compareTo(totals.get(1L)));
        assertEquals(0, BigDecimal.valueOf(500).compareTo(totals.get(3L)));
    }
}
