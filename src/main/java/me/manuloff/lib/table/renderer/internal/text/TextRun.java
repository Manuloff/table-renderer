package me.manuloff.lib.table.renderer.internal.text;

import java.awt.Font;

/**
 * Прогон: непрерывный диапазон символов одного строчного блока, рисуемых одним шрифтом.
 * Изображение всегда занимает отдельный прогон из одного символа.
 *
 * @author Manuloff
 * @since 2.0
 */
final class TextRun {

    final int start;
    int end;
    final InlineBox box;
    final Font font;
    /**
     * Строчные буквы, нарисованные уменьшенными заглавными (капитель).
     */
    final boolean smallCaps;
    final boolean image;

    TextRun(int start, int end, InlineBox box, Font font, boolean smallCaps, boolean image) {
        this.start = start;
        this.end = end;
        this.box = box;
        this.font = font;
        this.smallCaps = smallCaps;
        this.image = image;
    }

    /**
     * @return true, если символ можно добавить в этот прогон
     */
    boolean accepts(InlineBox box, Font font, boolean smallCaps, boolean image) {
        return !image && !this.image && this.box == box && this.font.equals(font) && this.smallCaps == smallCaps;
    }
}
