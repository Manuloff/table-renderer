package me.manuloff.lib.table.renderer.style.text;

/**
 * Правила разрыва внутри слов (аналог CSS word-break).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum WordBreak {
    /**
     * Обычные правила.
     */
    NORMAL,
    /**
     * Разрыв допускается между любыми символами.
     */
    BREAK_ALL,
    /**
     * Запрет разрывов внутри текста CJK.
     */
    KEEP_ALL
}
