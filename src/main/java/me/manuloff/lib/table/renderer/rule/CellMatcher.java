package me.manuloff.lib.table.renderer.rule;

import lombok.NonNull;

/**
 * Условие выбора ячеек для условных правил стиля ({@link me.manuloff.lib.table.renderer.Table#rule}).
 * Готовые условия - в {@link Match}.
 *
 * @author Manuloff
 * @since 2.0
 */
@FunctionalInterface
public interface CellMatcher {

    boolean matches(CellContext cell);

    default CellMatcher and(@NonNull CellMatcher other) {
        return c -> matches(c) && other.matches(c);
    }

    default CellMatcher or(@NonNull CellMatcher other) {
        return c -> matches(c) || other.matches(c);
    }

    default CellMatcher negate() {
        return c -> !matches(c);
    }
}
