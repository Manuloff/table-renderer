package me.manuloff.lib.table.renderer.internal.text;

import me.manuloff.lib.table.renderer.content.Inline;
import me.manuloff.lib.table.renderer.content.InlineImage;
import me.manuloff.lib.table.renderer.content.LineBreak;
import me.manuloff.lib.table.renderer.content.RichText;
import me.manuloff.lib.table.renderer.content.Span;
import me.manuloff.lib.table.renderer.content.TextNode;
import me.manuloff.lib.table.renderer.internal.style.Styles;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.text.TextTransform;
import me.manuloff.lib.table.renderer.style.text.WhiteSpace;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static me.manuloff.lib.table.renderer.internal.text.ParagraphText.NEWLINE;
import static me.manuloff.lib.table.renderer.internal.text.ParagraphText.OBJECT;
import static me.manuloff.lib.table.renderer.internal.text.ParagraphText.SOFT_HYPHEN;

/**
 * Превращает {@link RichText} в {@link ParagraphText}: вычисляет стили фрагментов, применяет
 * преобразование регистра и правила обработки пробелов ({@link WhiteSpace}), делит текст на прогоны шрифтов
 * (с запасными шрифтами для отсутствующих символов и капителью).
 *
 * @author Manuloff
 * @since 2.0
 */
final class ParagraphTextBuilder {

    /**
     * Размер строчных букв в капители относительно основного шрифта.
     */
    private static final float SMALL_CAPS_SCALE = 0.7f;

    private final FontResolver fonts;
    private final TextMeasurer measurer;
    private final Locale locale;

    private final List<InlineBox> boxes = new ArrayList<>();
    private final StringBuilder sb = new StringBuilder();
    private int[] boxOf = new int[64];
    /**
     * Последний добавленный символ - схлопываемый пробел (следующий пробел нужно пропустить).
     */
    private boolean lastCollapsibleSpace;

    private ParagraphTextBuilder(FontResolver fonts, TextMeasurer measurer, Locale locale) {
        this.fonts = fonts;
        this.measurer = measurer;
        this.locale = locale;
    }

    static ParagraphText build(RichText content, Style rootStyle, FontResolver fonts, TextMeasurer measurer, Locale locale) {
        ParagraphTextBuilder builder = new ParagraphTextBuilder(fonts, measurer, locale);
        InlineBox root = builder.newBox(null, rootStyle, null);
        builder.walk(content, root);
        root.end = builder.sb.length();
        return builder.finish(rootStyle);
    }

    // ============================================
    // Обход содержимого
    // ============================================

    private InlineBox newBox(InlineBox parent, Style style, InlineImage image) {
        InlineBox box = new InlineBox(this.boxes.size(), parent, style, image, this.fonts);
        box.start = this.sb.length();
        this.boxes.add(box);
        return box;
    }

    private void walk(RichText content, InlineBox box) {
        for (Inline inline : content.children()) {
            if (inline instanceof TextNode) {
                appendText(((TextNode) inline).getText(), box);
            } else if (inline instanceof Span) {
                Span span = (Span) inline;
                InlineBox child = newBox(box, Styles.inline(box.style, span.style()), null);
                walk(span, child);
                child.end = this.sb.length();
            } else if (inline instanceof LineBreak) {
                emit(NEWLINE, box);
                this.lastCollapsibleSpace = false;
            } else if (inline instanceof InlineImage) {
                InlineImage image = (InlineImage) inline;
                InlineBox child = newBox(box, Styles.inline(box.style, image.getStyle()), image);
                emit(OBJECT, child);
                child.end = this.sb.length();
                this.lastCollapsibleSpace = false;
            }
        }
    }

    /**
     * Добавляет текст по правилам white-space: схлопывание пробелов, сохранение переводов строк, табуляция.
     */
    private void appendText(String raw, InlineBox box) {
        WhiteSpace ws = box.style.whiteSpace();
        boolean collapse = ws == WhiteSpace.NORMAL || ws == WhiteSpace.NOWRAP || ws == WhiteSpace.PRE_LINE;
        boolean keepNewlines = ws == WhiteSpace.PRE || ws == WhiteSpace.PRE_WRAP || ws == WhiteSpace.PRE_LINE;
        String t = transform(raw, box.style.textTransform());

        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            if (c == '\r') {
                if (i + 1 < t.length() && t.charAt(i + 1) == '\n') continue;
                c = '\n';
            }
            if (c == '\n') {
                if (keepNewlines) {
                    emit(NEWLINE, box);
                    this.lastCollapsibleSpace = false;
                    continue;
                }
                c = ' ';
            }
            if (c == '\t' || c == '\f') {
                if (!collapse) {
                    appendTab(box);
                    continue;
                }
                c = ' ';
            }
            if (c == ' ' && collapse) {
                int len = this.sb.length();
                // Пробелы в начале строки и повторные пробелы схлопываются
                if (len == 0 || this.sb.charAt(len - 1) == NEWLINE || this.lastCollapsibleSpace) continue;
                emit(' ', box);
                this.lastCollapsibleSpace = true;
                continue;
            }
            emit(c, box);
            this.lastCollapsibleSpace = false;
        }
    }

    /**
     * Табуляция: пробелы до следующей позиции, кратной tabSize (по количеству символов в строке).
     */
    private void appendTab(InlineBox box) {
        int tab = Math.max(1, box.style.tabSize());
        int column = this.sb.length() - (this.sb.lastIndexOf("\n") + 1);
        int spaces = tab - column % tab;
        for (int k = 0; k < spaces; k++) emit(' ', box);
        this.lastCollapsibleSpace = false;
    }

    private String transform(String s, TextTransform transform) {
        switch (transform) {
            case UPPERCASE:
                return s.toUpperCase(this.locale);
            case LOWERCASE:
                return s.toLowerCase(this.locale);
            case CAPITALIZE: {
                StringBuilder out = new StringBuilder(s.length());
                char prev = this.sb.length() == 0 ? ' ' : this.sb.charAt(this.sb.length() - 1);
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    boolean wordStart = !Character.isLetterOrDigit(prev) && prev != '\'' && prev != '’';
                    out.append(wordStart && Character.isLetter(c) ? Character.toTitleCase(c) : c);
                    prev = c;
                }
                return out.toString();
            }
            default:
                return s;
        }
    }

    private void emit(char c, InlineBox box) {
        int len = this.sb.length();
        if (len == this.boxOf.length) {
            int[] grown = new int[len * 2];
            System.arraycopy(this.boxOf, 0, grown, 0, len);
            this.boxOf = grown;
        }
        this.boxOf[len] = box.index;
        this.sb.append(c);
    }

    // ============================================
    // Завершение
    // ============================================

    private ParagraphText finish(Style rootStyle) {
        String text = this.sb.toString();
        int n = text.length();
        int[] boxOf = new int[n];
        System.arraycopy(this.boxOf, 0, boxOf, 0, n);

        List<List<InlineBox>> startsAt = emptyLists(n + 1);
        List<List<InlineBox>> endsAt = emptyLists(n + 1);
        double[] startEdgeSum = new double[n + 1];
        double[] endEdgeSum = new double[n + 1];
        for (InlineBox box : this.boxes) {
            if (!hasEdges(box)) continue;
            add(startsAt, box.start, box);
            startEdgeSum[box.start] += box.startEdge;
            endEdgeSum[box.end] += box.endEdge;
        }
        for (int i = this.boxes.size() - 1; i >= 0; i--) {
            InlineBox box = this.boxes.get(i);
            if (hasEdges(box)) add(endsAt, box.end, box);
        }

        List<TextRun> runs = new ArrayList<>();
        int[] runOf = buildRuns(text, boxOf, runs);
        return new ParagraphText(text, rootStyle, this.locale, this.measurer, this.boxes, boxOf, runs, runOf,
                startsAt, endsAt, startEdgeSum, endEdgeSum);
    }

    /**
     * Фрагменты, у которых есть края и фон: непустые стилизованные фрагменты (не корень и не изображения).
     */
    private static boolean hasEdges(InlineBox box) {
        return !box.isRoot() && box.image == null && box.start < box.end;
    }

    private static List<List<InlineBox>> emptyLists(int size) {
        List<List<InlineBox>> lists = new ArrayList<>(size);
        for (int i = 0; i < size; i++) lists.add(Collections.emptyList());
        return lists;
    }

    private static void add(List<List<InlineBox>> lists, int index, InlineBox box) {
        List<InlineBox> list = lists.get(index);
        if (list.isEmpty()) {
            list = new ArrayList<>(2);
            lists.set(index, list);
        }
        list.add(box);
    }

    /**
     * Делит текст на прогоны: смена блока, шрифта (запасной шрифт для отсутствующих символов) или капители.
     *
     * @return индекс прогона для каждого символа
     */
    private int[] buildRuns(String text, int[] boxOf, List<TextRun> runs) {
        int n = text.length();
        int[] runOf = new int[n];
        TextRun current = null;
        int i = 0;
        while (i < n) {
            int cp = text.codePointAt(i);
            int len = Character.charCount(cp);
            InlineBox box = this.boxes.get(boxOf[i]);

            Font font;
            boolean smallCaps = false;
            boolean image = cp == OBJECT && box.image != null;
            if (image) {
                font = box.font;
            } else if (Character.isWhitespace(cp) || cp == SOFT_HYPHEN || Character.isISOControl(cp)) {
                // Служебные символы не меняют шрифт текущего прогона
                boolean same = current != null && current.box == box && !current.image;
                font = same ? current.font : box.font;
                smallCaps = same && current.smallCaps;
            } else {
                font = fontFor(box, cp);
                if (Boolean.TRUE.equals(box.style.smallCaps()) && Character.isLowerCase(cp)) {
                    smallCaps = true;
                    font = font.deriveFont(font.getSize2D() * SMALL_CAPS_SCALE);
                }
            }

            if (current != null && current.accepts(box, font, smallCaps, image)) {
                current.end = i + len;
            } else {
                current = new TextRun(i, i + len, box, font, smallCaps, image);
                runs.add(current);
            }
            for (int k = 0; k < len; k++) runOf[i + k] = runs.size() - 1;
            i += len;
        }
        return runOf;
    }

    /**
     * @return первый шрифт цепочки блока, в котором есть символ
     */
    private static Font fontFor(InlineBox box, int codePoint) {
        for (Font f : box.fontChain) {
            if (f.canDisplay(codePoint)) return f;
        }
        return box.font;
    }
}
