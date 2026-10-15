package me.manuloff.lib.table.renderer.internal.text;

/**
 * Диапазон символов одной строки до раскладки.
 *
 * @author Manuloff
 * @since 2.0
 */
final class LineRange {

    final int start;
    final int end;
    /**
     * Строка закончилась принудительным переносом (такие строки не растягиваются по ширине).
     */
    final boolean forced;
    /**
     * Строка разорвана на мягком переносе - в конце рисуется дефис.
     */
    final boolean hyphenated;

    LineRange(int start, int end, boolean forced, boolean hyphenated) {
        this.start = start;
        this.end = end;
        this.forced = forced;
        this.hyphenated = hyphenated;
    }
}
