package me.manuloff.lib.table.renderer.style.text;

/**
 * Горизонтальное выравнивание содержимого (аналог CSS text-align).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum TextAlign {
    LEFT,
    CENTER,
    RIGHT,
    /**
     * По ширине: последняя строка и строки перед явным переносом выравниваются влево.
     */
    JUSTIFY
}
