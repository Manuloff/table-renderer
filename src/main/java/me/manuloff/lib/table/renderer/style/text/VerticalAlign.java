package me.manuloff.lib.table.renderer.style.text;

/**
 * Вертикальное выравнивание содержимого ячейки (аналог CSS vertical-align).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum VerticalAlign {
    TOP,
    MIDDLE,
    BOTTOM,
    /**
     * Первые строки всех ячеек строки таблицы выравниваются по общей базовой линии.
     */
    BASELINE
}
