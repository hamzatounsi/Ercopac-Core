package com.ercopac.ercopac_tracker.projectum.finance.service;

import com.ercopac.ercopac_tracker.projectum.finance.domain.FinanceEntry;
import java.math.BigDecimal;
import java.util.*;

/**
 * Resolves the project's ordered WBS once, including rows created before parent_id existed.
 * Finance and Forecast both use this class so they always show the same tree and totals.
 */
public final class FinanceWbsHierarchy {
    private FinanceWbsHierarchy() {}

    public static Map<Long, Long> parents(List<FinanceEntry> rows) {
        Map<Long, Long> derived = new HashMap<>();
        Map<Long, Long> result = new HashMap<>();
        Deque<FinanceEntry> stack = new ArrayDeque<>();
        Set<Long> ids = new HashSet<>();
        rows.forEach(row -> ids.add(row.getId()));
        for (FinanceEntry row : rows) {
            int level = row.getLevel() == null ? 1 : row.getLevel();
            while (!stack.isEmpty() && (stack.peek().getLevel() == null ? 1 : stack.peek().getLevel()) >= level) stack.pop();
            Long derivedParent = stack.isEmpty() ? null : stack.peek().getId();
            Long parentId = row.getParentId();
            if (parentId == null || !ids.contains(parentId) || parentId.equals(row.getId())) parentId = derivedParent;
            derived.put(row.getId(), derivedParent);
            result.put(row.getId(), parentId);
            stack.push(row);
        }
        // A persisted parent chain that loops back would hide rows from every total; fall back to the
        // order-derived parent, which always points to an earlier row and therefore cannot loop.
        for (Long id : ids) if (isInCycle(id, result)) result.put(id, derived.get(id));
        return result;
    }

    public static Map<Long, List<Long>> children(List<FinanceEntry> rows) {
        Map<Long, List<Long>> children = new HashMap<>();
        parents(rows).forEach((id, parent) -> {
            if (parent != null) children.computeIfAbsent(parent, ignored -> new ArrayList<>()).add(id);
        });
        return children;
    }

    public static Set<Long> leafIds(Collection<Long> ids, Map<Long, Long> parents) {
        Set<Long> leaves = new HashSet<>(ids);
        leaves.removeAll(parents.values());
        return leaves;
    }

    /**
     * Sums leaf values into every ancestor. Values stored on non-leaf rows are ignored, so a
     * parent total is always exactly the sum of the leaves below it.
     */
    public static Map<Long, BigDecimal> rollup(Collection<Long> ids, Map<Long, Long> parents, Map<Long, BigDecimal> leafValues) {
        Map<Long, BigDecimal> totals = new HashMap<>();
        ids.forEach(id -> totals.put(id, BigDecimal.ZERO));
        for (Long leaf : leafIds(ids, parents)) {
            BigDecimal value = leafValues.getOrDefault(leaf, BigDecimal.ZERO);
            if (value == null || value.signum() == 0) continue;
            Set<Long> visited = new HashSet<>();
            for (Long node = leaf; node != null && totals.containsKey(node) && visited.add(node); node = parents.get(node))
                totals.merge(node, value, BigDecimal::add);
        }
        return totals;
    }

    /** True when {@code candidate} is {@code nodeId} itself or one of its descendants. */
    public static boolean isSelfOrDescendant(Long candidate, Long nodeId, Map<Long, Long> parents) {
        Set<Long> visited = new HashSet<>();
        for (Long node = candidate; node != null && visited.add(node); node = parents.get(node))
            if (node.equals(nodeId)) return true;
        return false;
    }

    private static boolean isInCycle(Long id, Map<Long, Long> parents) {
        Set<Long> visited = new HashSet<>();
        for (Long node = parents.get(id); node != null; node = parents.get(node)) {
            if (node.equals(id)) return true;
            if (!visited.add(node)) return false;
        }
        return false;
    }
}
