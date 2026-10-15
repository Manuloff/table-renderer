package me.manuloff.lib.table.renderer.internal.text;

import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.text.OverflowWrap;
import me.manuloff.lib.table.renderer.style.text.WhiteSpace;
import me.manuloff.lib.table.renderer.style.text.WordBreak;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import static me.manuloff.lib.table.renderer.internal.text.ParagraphText.EPSILON;
import static me.manuloff.lib.table.renderer.internal.text.ParagraphText.NEWLINE;
import static me.manuloff.lib.table.renderer.internal.text.ParagraphText.OBJECT;
import static me.manuloff.lib.table.renderer.internal.text.ParagraphText.SOFT_HYPHEN;

/**
 * Разбиение абзаца на строки.
 * <p>
 * Места возможного переноса находятся по правилам Unicode ({@link BreakIterator}) и уточняются стилем
 * ({@link WhiteSpace}, {@link WordBreak}, мягкие переносы). Текст между соседними местами переноса - сегмент;
 * строки набираются из сегментов жадно. Слишком длинный сегмент разрывается внутри при
 * {@link OverflowWrap#BREAK_WORD} / {@link OverflowWrap#ANYWHERE}.
 *
 * @author Manuloff
 * @since 2.0
 */
final class LineBreaker {

    /**
     * Неразрывный участок текста между соседними местами переноса.
     */
    private static final class Segment {
        int start;
        int end;
        /**
         * Конец без висящих пробелов.
         */
        int contentEnd;
        /**
         * Ширина целиком (если за сегментом строка продолжается).
         */
        double fullWidth;
        /**
         * Ширина без висящих пробелов (если сегмент последний в строке).
         */
        double fitWidth;
        boolean forcedAfter;
    }

    private final ParagraphText p;
    private final List<Segment> segments = new ArrayList<>();
    private double minContent;
    private double maxContent;

    LineBreaker(ParagraphText paragraph) {
        this.p = paragraph;
        boolean[] soft = findBreaks();
        int start = 0;
        for (int i = 1; i <= paragraph.length; i++) {
            if (i == paragraph.length || soft[i]) {
                this.segments.add(segment(start, i));
                start = i;
            }
        }
        computeIntrinsicWidths();
    }

    /**
     * @return минимальная ширина: самый широкий неразрывный участок
     */
    double minContentWidth() {
        return this.minContent;
    }

    /**
     * @return ширина без переносов, кроме принудительных
     */
    double maxContentWidth() {
        return this.maxContent;
    }

    // ============================================
    // Места переноса
    // ============================================

    /**
     * @return soft[i] - можно ли перенести строку перед символом i
     */
    private boolean[] findBreaks() {
        int n = this.p.length;
        boolean[] soft = new boolean[n + 1];
        if (n == 0) return soft;

        BreakIterator bi = BreakIterator.getLineInstance(this.p.locale);
        bi.setText(this.p.text);
        for (int b = bi.first(); b != BreakIterator.DONE; b = bi.next()) {
            if (b > 0 && b < n) soft[b] = true;
        }

        for (int i = 1; i < n; i++) {
            char prev = this.p.charAt(i - 1);
            char cur = this.p.charAt(i);
            if (prev == NEWLINE) {
                soft[i] = true;
                continue;
            }
            if (Character.isLowSurrogate(cur)) {
                soft[i] = false;
                continue;
            }
            // Правила переноса определяет стиль символа перед местом разрыва
            Style s = this.p.styleAt(i - 1);
            WhiteSpace ws = s.whiteSpace();
            if (ws == WhiteSpace.NOWRAP || ws == WhiteSpace.PRE) {
                soft[i] = false;
                continue;
            }
            if (prev == OBJECT || cur == OBJECT) {
                soft[i] = true;
                continue;
            }
            boolean prevText = !Character.isWhitespace(prev);
            boolean curText = !Character.isWhitespace(cur);
            if (s.wordBreak() == WordBreak.BREAK_ALL && prevText && curText) {
                soft[i] = true;
            } else if (s.wordBreak() == WordBreak.KEEP_ALL && Character.isLetter(prev) && Character.isLetter(cur)) {
                soft[i] = false;
            }
            if (prev == SOFT_HYPHEN && !Boolean.TRUE.equals(s.hyphens())) {
                soft[i] = false;
            }
        }
        return soft;
    }

    private Segment segment(int start, int end) {
        Segment seg = new Segment();
        seg.start = start;
        seg.end = end;
        seg.contentEnd = this.p.trimEnd(start, end);
        seg.forcedAfter = this.p.charAt(end - 1) == NEWLINE;
        double edges = this.p.edges(start, end);
        seg.fullWidth = this.p.chars(start, end) + edges;
        seg.fitWidth = this.p.chars(start, seg.contentEnd) + edges;
        // На мягком переносе в конце строки появится дефис
        if (this.p.charAt(end - 1) == SOFT_HYPHEN && end < this.p.length) {
            seg.fitWidth += this.p.hyphenWidth(end - 1);
        }
        return seg;
    }

    private void computeIntrinsicWidths() {
        double indent = this.p.rootStyle.textIndent();
        double min = 0;
        double max = 0;
        double line = indent;
        for (int k = 0; k < this.segments.size(); k++) {
            Segment seg = this.segments.get(k);
            double w = seg.fitWidth;
            if (overflowWrap(seg.start) == OverflowWrap.ANYWHERE) {
                w = this.p.maxGraphemeWidth(seg.start, seg.contentEnd);
            }
            if (k == 0) w += indent;
            min = Math.max(min, w);

            if (seg.forcedAfter || k == this.segments.size() - 1) {
                max = Math.max(max, line + seg.fitWidth);
                line = 0;
            } else {
                line += seg.fullWidth;
            }
        }
        this.minContent = Math.max(0, min);
        this.maxContent = Math.max(this.minContent, max);
    }

    private OverflowWrap overflowWrap(int index) {
        return this.p.styleAt(index).overflowWrap();
    }

    // ============================================
    // Разбиение
    // ============================================

    /**
     * Жадно набирает строки ширины не больше {@code available}.
     */
    List<LineRange> breakLines(double available) {
        List<LineRange> lines = new ArrayList<>();
        if (this.p.length == 0) return lines;

        double indent = this.p.rootStyle.textIndent();
        int k = 0;
        int segStart = this.segments.get(0).start;
        while (k < this.segments.size()) {
            int lineStart = segStart;
            double width = lines.isEmpty() ? indent : 0;
            int lineEnd = -1;
            boolean forced = false;

            while (k < this.segments.size()) {
                Segment seg = this.segments.get(k);
                // Сегмент мог быть частично перенесён на предыдущую строку
                Segment part = segStart > seg.start ? segment(segStart, seg.end) : seg;
                boolean first = lineEnd < 0;
                if (width + part.fitWidth > available + EPSILON && !first) break;

                if (first && width + part.fitWidth > available + EPSILON && canBreakInside(part)) {
                    int split = emergencySplit(part, available - width);
                    if (split > part.start && split < part.contentEnd) {
                        lineEnd = split;
                        segStart = split;
                        break;
                    }
                }
                width += part.fullWidth;
                lineEnd = part.end;
                k++;
                if (k < this.segments.size()) segStart = this.segments.get(k).start;
                if (part.forcedAfter) {
                    forced = true;
                    break;
                }
            }
            boolean hyphen = lineEnd < this.p.length && !forced && this.p.charAt(lineEnd - 1) == SOFT_HYPHEN;
            lines.add(new LineRange(lineStart, lineEnd, forced, hyphen));
        }
        return lines;
    }

    private boolean canBreakInside(Segment seg) {
        Style s = this.p.styleAt(seg.start);
        WhiteSpace ws = s.whiteSpace();
        return s.overflowWrap() != OverflowWrap.NORMAL && ws != WhiteSpace.NOWRAP && ws != WhiteSpace.PRE;
    }

    /**
     * @return позиция разрыва внутри сегмента: самый длинный помещающийся префикс (минимум одна графема)
     */
    private int emergencySplit(Segment seg, double available) {
        BreakIterator bi = this.p.graphemes();
        int best = bi.following(seg.start);
        int next = best;
        while (next != BreakIterator.DONE && next < seg.contentEnd) {
            if (this.p.fit(seg.start, next) > available + EPSILON) break;
            best = next;
            next = bi.next();
        }
        return best;
    }
}
