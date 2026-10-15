package me.manuloff.lib.table.renderer.style.box;

/**
 * Обработка содержимого, выходящего за ячейку (аналог CSS overflow).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum Overflow {
    /**
     * Содержимое может выходить за границы.
     */
    VISIBLE,
    /**
     * Содержимое обрезается по внутренней границе рамки.
     */
    HIDDEN
}
