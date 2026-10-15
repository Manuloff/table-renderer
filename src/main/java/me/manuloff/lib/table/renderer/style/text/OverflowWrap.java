package me.manuloff.lib.table.renderer.style.text;

/**
 * Разрыв слов, не помещающихся в строку (аналог CSS overflow-wrap).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum OverflowWrap {
    /**
     * Длинное слово выходит за границы.
     */
    NORMAL,
    /**
     * Длинное слово разрывается, но не влияет на минимальную ширину колонки.
     */
    BREAK_WORD,
    /**
     * Длинное слово разрывается, колонка может сужаться до одного символа.
     */
    ANYWHERE
}
