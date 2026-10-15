package me.manuloff.lib.table.renderer.internal.text;

import java.awt.Font;
import java.awt.font.LineMetrics;

/**
 * Вертикальные метрики шрифта: высота над и под базовой линией, положение линий оформления.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class TextMetrics {

    /**
     * Строка для измерения: заглавная, буквы с выносными элементами и кириллица.
     */
    private static final String SAMPLE = "XgjyЙ";

    public final double ascent;
    public final double descent;
    public final double leading;
    /**
     * Смещение подчёркивания вниз от базовой линии.
     */
    public final double underlineOffset;
    public final double underlineThickness;
    /**
     * Смещение зачёркивания от базовой линии (отрицательное - вверх).
     */
    public final double strikethroughOffset;
    public final double strikethroughThickness;

    TextMetrics(Font font) {
        LineMetrics lm = font.getLineMetrics(SAMPLE, FontResolver.FRC);
        double size = font.getSize2D();
        this.ascent = lm.getAscent();
        this.descent = lm.getDescent();
        this.leading = lm.getLeading();
        // Некоторые шрифты не сообщают метрики линий - используем типичные пропорции
        this.underlineOffset = lm.getUnderlineOffset() > 0 ? lm.getUnderlineOffset() : size * 0.1;
        this.underlineThickness = Math.max(lm.getUnderlineThickness(), size / 16);
        this.strikethroughOffset = lm.getStrikethroughOffset() < 0 ? lm.getStrikethroughOffset() : -size * 0.28;
        this.strikethroughThickness = Math.max(lm.getStrikethroughThickness(), size / 16);
    }

    /**
     * @return высота строки для line-height: normal
     */
    public double normalLineHeight() {
        return this.ascent + this.descent + this.leading;
    }
}
