package me.manuloff.lib.table.renderer.internal.text;

import java.awt.Font;

/**
 * Кусок текста строки, рисуемый одним шрифтом.
 *
 * @author Manuloff
 * @since 2.0
 */
final class TextFragment {

    final InlineBox box;
    final double x;
    final double width;
    final String text;
    final Font font;
    final double letterSpacing;
    /**
     * Межсловный интервал с учётом растяжения при выравнивании по ширине.
     */
    final double wordSpacing;

    TextFragment(InlineBox box, double x, double width, String text, Font font, double letterSpacing, double wordSpacing) {
        this.box = box;
        this.x = x;
        this.width = width;
        this.text = text;
        this.font = font;
        this.letterSpacing = letterSpacing;
        this.wordSpacing = wordSpacing;
    }
}
