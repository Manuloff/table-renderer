package me.manuloff.lib.table.renderer.style.text;

/**
 * Смещение строчного фрагмента относительно базовой линии (аналог CSS vertical-align для текста).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum BaselineShift {
    NONE,
    /**
     * Нижний индекс.
     */
    SUB,
    /**
     * Верхний индекс.
     */
    SUPER
}
