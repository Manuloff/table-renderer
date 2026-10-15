package me.manuloff.lib.table.renderer.style.text;

/**
 * Преобразование регистра (аналог CSS text-transform).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum TextTransform {
    NONE,
    UPPERCASE,
    LOWERCASE,
    /**
     * Первая буква каждого слова - заглавная.
     */
    CAPITALIZE
}
