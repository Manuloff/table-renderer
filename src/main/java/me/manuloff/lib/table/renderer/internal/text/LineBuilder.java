package me.manuloff.lib.table.renderer.internal.text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.manuloff.lib.table.renderer.internal.text.ParagraphText.EPSILON;

/**
 * Сборка строки из диапазона текста: расстановка фрагментов по горизонтали, части оформленных
 * строчных фрагментов, высота строки и положение базовой линии, обрезка с маркером.
 *
 * @author Manuloff
 * @since 2.0
 */
final class LineBuilder {

    private final ParagraphText p;

    LineBuilder(ParagraphText paragraph) {
        this.p = paragraph;
    }

    /**
     * Строит строку из диапазона [a, b) - без висящих пробелов.
     *
     * @param indent        отступ первой строки
     * @param extraPerSpace дополнительный интервал на пробел (выравнивание по ширине)
     * @param suffix        маркер обрезки или дефис в конце строки; null - без суффикса
     */
    TextLine build(int a, int b, double indent, double extraPerSpace, String suffix) {
        TextLine line = new TextLine();
        line.texts = new ArrayList<>();
        line.images = new ArrayList<>();
        Map<InlineBox, BoxFragment> fragments = new LinkedHashMap<>();
        Set<InlineBox> participants = new LinkedHashSet<>();
        participants.add(this.p.root());

        double x = indent;
        if (a < b) openContinuedBoxes(a, x, fragments);

        int pos = a;
        while (pos < b) {
            TextRun run = this.p.runAt(pos);
            int end = Math.min(b, run.end);

            for (InlineBox box : this.p.startsAt.get(pos)) {
                BoxFragment f = new BoxFragment(box);
                f.left = x;
                f.hasStart = true;
                fragments.put(box, f);
                x += box.startEdge;
            }

            if (run.image) {
                line.images.add(new ImageFragment(run.box, x));
                x += run.box.imageOuterWidth();
            } else {
                String str = this.p.display(run, pos, end);
                if (!str.isEmpty()) {
                    double letterSpacing = run.box.style.letterSpacing();
                    double wordSpacing = run.box.style.wordSpacing() + extraPerSpace;
                    double w = this.p.measurer.width(run.font, str, letterSpacing, wordSpacing);
                    line.texts.add(new TextFragment(run.box, x, w, str, run.font, letterSpacing, wordSpacing));
                    x += w;
                }
            }
            addWithAncestors(participants, run.box);

            for (InlineBox box : this.p.endsAt.get(end)) {
                x += box.endEdge;
                BoxFragment f = fragments.get(box);
                if (f != null) {
                    f.right = x;
                    f.hasEnd = true;
                }
            }
            pos = end;
        }
        for (BoxFragment f : fragments.values()) {
            if (!f.hasEnd) f.right = x;
        }

        // Дефис на пустой строке не рисуется
        if (suffix != null && (b > a || !suffix.equals("-"))) {
            InlineBox box = b > a ? this.p.boxAt(b - 1) : this.p.root();
            double w = this.p.measurer.width(box.font, suffix, box.style.letterSpacing(), 0);
            line.texts.add(new TextFragment(box, x, w, suffix, box.font, box.style.letterSpacing(), 0));
            x += w;
            addWithAncestors(participants, box);
        }

        line.width = x;
        line.boxes = new ArrayList<>(fragments.values());
        verticalMetrics(line, participants);
        return line;
    }

    /**
     * Фрагменты, начавшиеся на предыдущих строках, продолжаются с начала этой строки без левого края.
     */
    private void openContinuedBoxes(int a, double x, Map<InlineBox, BoxFragment> fragments) {
        List<InlineBox> open = new ArrayList<>();
        for (InlineBox box = this.p.boxAt(a); box != null && !box.isRoot(); box = box.parent) {
            if (box.start < a && box.image == null) open.add(box);
        }
        Collections.reverse(open);
        for (InlineBox box : open) {
            BoxFragment f = new BoxFragment(box);
            f.left = x;
            fragments.put(box, f);
        }
    }

    private static void addWithAncestors(Set<InlineBox> set, InlineBox box) {
        for (InlineBox b = box; b != null; b = b.parent) {
            if (!set.add(b)) break;
        }
    }

    /**
     * Высота строки и базовая линия: каждый строчный блок занимает свою высоту строки вокруг своей
     * базовой линии (с учётом подъёма), изображения стоят на базовой линии.
     */
    private static void verticalMetrics(TextLine line, Set<InlineBox> participants) {
        double above = 0;
        double below = 0;
        for (InlineBox box : participants) {
            if (box.image != null) {
                above = Math.max(above, box.shift + box.imageOuterHeight());
                below = Math.max(below, -box.shift);
            } else {
                TextMetrics m = box.metrics;
                double halfLeading = (box.lineHeight - (m.ascent + m.descent)) / 2;
                above = Math.max(above, box.shift + m.ascent + halfLeading);
                below = Math.max(below, -box.shift + m.descent + halfLeading);
            }
        }
        line.baseline = above;
        line.height = Math.max(0, above + below);
    }

    /**
     * Строка, обрезанная так, чтобы вместе с маркером уместиться в ширину.
     *
     * @param always добавить маркер, даже если строка помещается целиком (обрезка по количеству строк)
     */
    TextLine truncated(LineRange range, String marker, double width, double indent, boolean always) {
        int a = range.start;
        int end = this.p.trimEnd(a, range.end);
        if (!always && indent + this.p.fit(a, end) <= width + EPSILON) {
            return build(a, end, indent, 0, null);
        }

        InlineBox markerBox = end > a ? this.p.boxAt(end - 1) : this.p.root();
        double markerWidth = this.p.measurer.width(markerBox.font, marker, markerBox.style.letterSpacing(), 0);

        if (indent + this.p.fit(a, end) + markerWidth > width + EPSILON) {
            BreakIterator bi = this.p.graphemes();
            int cut = a;
            int next = a < this.p.length ? bi.following(a) : BreakIterator.DONE;
            while (next != BreakIterator.DONE && next <= end) {
                if (indent + this.p.fit(a, next) + markerWidth > width + EPSILON) break;
                cut = next;
                next = bi.next();
            }
            end = this.p.trimEnd(a, cut);
        }
        return build(a, end, indent, 0, marker);
    }
}
