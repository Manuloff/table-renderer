package me.manuloff.lib.table.renderer.internal.text;

import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.text.WhiteSpace;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

/**
 * Подготовленный текст абзаца: строка после обработки пробелов, строчные блоки, прогоны шрифтов
 * и края блоков. Умеет измерять ширину любого диапазона.
 * <p>
 * Особые символы в тексте:
 * <ul>
 *     <li>{@link #NEWLINE} - принудительный перенос (нулевой ширины);</li>
 *     <li>{@link #OBJECT} - место изображения;</li>
 *     <li>{@link #SOFT_HYPHEN} - мягкий перенос (невидим, при разрыве строки рисуется дефис).</li>
 * </ul>
 *
 * @author Manuloff
 * @since 2.0
 */
final class ParagraphText {

    static final char NEWLINE = '\n';
    static final char OBJECT = '￼';
    static final char SOFT_HYPHEN = '­';
    /**
     * Допуск при сравнении ширин (погрешность вычислений с плавающей точкой).
     */
    static final double EPSILON = 0.01;

    final String text;
    final int length;
    final Style rootStyle;
    final Locale locale;
    final TextMeasurer measurer;

    /**
     * Блоки в порядке создания (внешние раньше вложенных); нулевой - корень.
     */
    final List<InlineBox> boxes;
    /**
     * Индекс блока для каждого символа.
     */
    final int[] boxOf;
    final List<TextRun> runs;
    /**
     * Индекс прогона для каждого символа.
     */
    final int[] runOf;
    /**
     * Блоки, начинающиеся перед символом i (внешние первыми), и заканчивающиеся перед ним (вложенные первыми).
     */
    final List<List<InlineBox>> startsAt;
    final List<List<InlineBox>> endsAt;
    /**
     * Суммарная ширина левых краёв блоков, начинающихся в позиции i, и правых краёв - заканчивающихся в ней.
     */
    final double[] startEdgeSum;
    final double[] endEdgeSum;

    ParagraphText(String text, Style rootStyle, Locale locale, TextMeasurer measurer, List<InlineBox> boxes,
                  int[] boxOf, List<TextRun> runs, int[] runOf, List<List<InlineBox>> startsAt,
                  List<List<InlineBox>> endsAt, double[] startEdgeSum, double[] endEdgeSum) {
        this.text = text;
        this.length = text.length();
        this.rootStyle = rootStyle;
        this.locale = locale;
        this.measurer = measurer;
        this.boxes = boxes;
        this.boxOf = boxOf;
        this.runs = runs;
        this.runOf = runOf;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.startEdgeSum = startEdgeSum;
        this.endEdgeSum = endEdgeSum;
    }

    char charAt(int index) {
        return this.text.charAt(index);
    }

    InlineBox root() {
        return this.boxes.get(0);
    }

    InlineBox boxAt(int index) {
        return this.boxes.get(this.boxOf[index]);
    }

    TextRun runAt(int index) {
        return this.runs.get(this.runOf[index]);
    }

    Style styleAt(int index) {
        return boxAt(index).style;
    }

    BreakIterator graphemes() {
        BreakIterator bi = BreakIterator.getCharacterInstance(this.locale);
        bi.setText(this.text);
        return bi;
    }

    // ============================================
    // Измерение
    // ============================================

    /**
     * @return ширина символов диапазона [a, b) без краёв строчных блоков
     */
    double chars(int a, int b) {
        if (a >= b) return 0;
        double w = 0;
        for (int r = this.runOf[a]; r < this.runs.size(); r++) {
            TextRun run = this.runs.get(r);
            if (run.start >= b) break;
            int s = Math.max(a, run.start);
            int e = Math.min(b, run.end);
            if (s < e) w += runWidth(run, s, e, 0);
        }
        return w;
    }

    /**
     * @return ширина части прогона
     * @param extraPerSpace дополнительный интервал на пробел (выравнивание по ширине)
     */
    double runWidth(TextRun run, int s, int e, double extraPerSpace) {
        if (run.image) return run.box.imageOuterWidth();
        String str = display(run, s, e);
        if (str.isEmpty()) return 0;
        Style style = run.box.style;
        return this.measurer.width(run.font, str, style.letterSpacing(), style.wordSpacing() + extraPerSpace);
    }

    /**
     * @return отображаемый текст части прогона: без служебных символов, с учётом капители
     */
    String display(TextRun run, int s, int e) {
        StringBuilder out = null;
        for (int i = s; i < e; i++) {
            char c = this.text.charAt(i);
            if (c == SOFT_HYPHEN || c == NEWLINE || c == OBJECT) {
                if (out == null) out = new StringBuilder(this.text.substring(s, i));
            } else if (out != null) {
                out.append(c);
            }
        }
        String str = out == null ? this.text.substring(s, e) : out.toString();
        return run.smallCaps ? str.toUpperCase(this.locale) : str;
    }

    /**
     * @return ширина краёв строчных блоков внутри диапазона [a, b)
     */
    double edges(int a, int b) {
        double w = 0;
        for (int p = a; p < b; p++) w += this.startEdgeSum[p];
        for (int p = a + 1; p <= b; p++) w += this.endEdgeSum[p];
        return w;
    }

    /**
     * @return ширина диапазона в конце строки: без висящих пробелов, с краями блоков
     */
    double fit(int a, int b) {
        return chars(a, trimEnd(a, b)) + edges(a, b);
    }

    /**
     * Отбрасывает висящие в конце диапазона пробелы и переводы строк (в режиме pre пробелы не висят).
     *
     * @return новый конец диапазона
     */
    int trimEnd(int start, int end) {
        int e = end;
        while (e > start) {
            char c = this.text.charAt(e - 1);
            if (c == NEWLINE) {
                e--;
            } else if (c == ' ' && styleAt(e - 1).whiteSpace() != WhiteSpace.PRE) {
                e--;
            } else {
                break;
            }
        }
        return e;
    }

    /**
     * @return ширина дефиса, рисуемого при разрыве на мягком переносе в позиции {@code index}
     */
    double hyphenWidth(int index) {
        TextRun run = runAt(index);
        return this.measurer.width(run.font, "-", run.box.style.letterSpacing(), 0);
    }

    /**
     * @return ширина самой широкой графемы диапазона
     */
    double maxGraphemeWidth(int a, int b) {
        BreakIterator bi = graphemes();
        double max = 0;
        int prev = a;
        int next = a < this.length ? bi.following(a) : BreakIterator.DONE;
        while (next != BreakIterator.DONE && next <= b) {
            max = Math.max(max, chars(prev, next));
            prev = next;
            next = bi.next();
        }
        return max;
    }

    /**
     * @return количество пробелов в диапазоне (места растяжения при выравнивании по ширине)
     */
    int countSpaces(int a, int b) {
        int count = 0;
        for (int i = a; i < b; i++) {
            if (this.text.charAt(i) == ' ') count++;
        }
        return count;
    }
}
