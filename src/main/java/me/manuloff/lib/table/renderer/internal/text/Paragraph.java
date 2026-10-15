package me.manuloff.lib.table.renderer.internal.text;

import me.manuloff.lib.table.renderer.content.RichText;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.box.Overflow;
import me.manuloff.lib.table.renderer.style.text.TextAlign;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static me.manuloff.lib.table.renderer.internal.text.ParagraphText.EPSILON;

/**
 * Абзац форматированного текста - точка входа текстового движка.
 * <p>
 * Конвейер:
 * <ol>
 *     <li>{@link ParagraphTextBuilder} - стили фрагментов, обработка пробелов, прогоны шрифтов;</li>
 *     <li>{@link LineBreaker} - места переноса, минимальная/максимальная ширина, разбиение на строки;</li>
 *     <li>{@link LineBuilder} - сборка строк: фрагменты, высота, обрезка с маркером;</li>
 *     <li>выравнивание строк ({@link #layout(double)}), затем отрисовка {@link TextPainter}.</li>
 * </ol>
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Paragraph {

    private final ParagraphText text;
    private final LineBreaker breaker;
    private final LineBuilder builder;

    private Paragraph(ParagraphText text) {
        this.text = text;
        this.breaker = new LineBreaker(text);
        this.builder = new LineBuilder(text);
    }

    /**
     * @param content   содержимое
     * @param rootStyle вычисленный стиль блока (ячейки или заголовка таблицы)
     */
    public static Paragraph build(RichText content, Style rootStyle, FontResolver fonts, TextMeasurer measurer,
                                  Locale locale) {
        return new Paragraph(ParagraphTextBuilder.build(content, rootStyle, fonts, measurer, locale));
    }

    /**
     * @return минимальная ширина: самый широкий неразрывный участок
     */
    public double minContentWidth() {
        return this.breaker.minContentWidth();
    }

    /**
     * @return ширина без переносов, кроме принудительных
     */
    public double maxContentWidth() {
        return this.breaker.maxContentWidth();
    }

    /**
     * @return true, если в абзаце нет содержимого (после обработки пробелов)
     */
    public boolean isEmpty() {
        return this.text.length == 0;
    }

    /**
     * Раскладывает абзац по строкам заданной ширины: переносы, ограничение количества строк,
     * обрезка с маркером, выравнивание.
     */
    public TextBlock layout(double width) {
        Style root = this.text.rootStyle;
        List<LineRange> ranges = this.breaker.breakLines(width);

        Integer clamp = root.lineClamp();
        boolean clamped = clamp != null && ranges.size() > clamp;
        if (clamped) ranges = ranges.subList(0, clamp);

        String marker = root.textOverflow();
        boolean hasMarker = marker != null && !marker.isEmpty();
        boolean overflowHidden = root.overflow() == Overflow.HIDDEN;
        boolean justify = root.textAlign() == TextAlign.JUSTIFY;

        List<TextLine> lines = new ArrayList<>(ranges.size());
        for (int i = 0; i < ranges.size(); i++) {
            LineRange range = ranges.get(i);
            boolean last = i == ranges.size() - 1;
            double indent = i == 0 ? root.textIndent() : 0;

            TextLine line;
            if (clamped && last && hasMarker) {
                line = this.builder.truncated(range, marker, width, indent, true);
            } else if (overflowHidden && hasMarker && indent + this.text.fit(range.start, range.end) > width + EPSILON) {
                line = this.builder.truncated(range, marker, width, indent, false);
            } else {
                line = lineOf(range, indent, 0);
                if (justify && !last && !range.forced) line = justified(line, range, indent, width);
            }
            line.offset = alignmentOffset(root.textAlign(), width - line.width);
            lines.add(line);
        }
        return new TextBlock(lines);
    }

    private TextLine lineOf(LineRange range, double indent, double extraPerSpace) {
        int end = this.text.trimEnd(range.start, range.end);
        return this.builder.build(range.start, end, indent, extraPerSpace, range.hyphenated ? "-" : null);
    }

    /**
     * Выравнивание по ширине: свободное место делится между пробелами строки.
     */
    private TextLine justified(TextLine line, LineRange range, double indent, double width) {
        int spaces = this.text.countSpaces(range.start, this.text.trimEnd(range.start, range.end));
        double free = width - line.width;
        if (spaces == 0 || free <= EPSILON) return line;
        return lineOf(range, indent, free / spaces);
    }

    /**
     * Не помещающаяся строка выравнивается по левому краю.
     */
    private static double alignmentOffset(TextAlign align, double free) {
        free = Math.max(0, free);
        switch (align) {
            case CENTER:
                return free / 2;
            case RIGHT:
                return free;
            default:
                return 0;
        }
    }
}
