package me.manuloff.lib.table.renderer.style.table;

/**
 * Алгоритм расчёта ширины колонок (аналог CSS table-layout).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum TableLayout {
    /**
     * Ширина колонок зависит от содержимого.
     */
    AUTO,
    /**
     * Ширина колонок берётся из заданных ширин колонок и первой строки, содержимое не учитывается.
     */
    FIXED
}
