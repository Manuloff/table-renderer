package me.manuloff.lib.table.renderer.style.table;

/**
 * Модель рамок таблицы (аналог CSS border-collapse).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum BorderCollapse {
    /**
     * Соседние ячейки делят общую рамку, конфликты разрешаются по правилам CSS.
     */
    COLLAPSE,
    /**
     * У каждой ячейки своя рамка, между ячейками - border-spacing.
     */
    SEPARATE
}
