package me.manuloff.lib.table.renderer.internal.layout;

import me.manuloff.lib.table.renderer.Column;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.box.Overflow;
import me.manuloff.lib.table.renderer.style.box.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Расчёт ширины колонок - аналог алгоритмов CSS {@code table-layout: auto} и {@code fixed}.
 *
 * <h2>Автоматическая раскладка</h2>
 * <ol>
 *     <li>Для каждой ячейки - минимальная (самое длинное слово) и предпочтительная (текст без переносов) ширина.</li>
 *     <li>Колонка получает максимум по своим одиночным ячейкам; объединённые ячейки досыпают недостающую
 *     ширину в свои колонки пропорционально их предпочтительной ширине.</li>
 *     <li>Ширина сетки: заданная ширина таблицы, иначе сумма предпочтительных, ограниченная доступной шириной;
 *     но не меньше суммы минимальных.</li>
 *     <li>Распределение: каждой колонке минимум, затем по очереди процентным, фиксированным и автоматическим
 *     колонкам до желаемой ширины; остаток - автоматическим колонкам пропорционально.</li>
 * </ol>
 *
 * <h2>Фиксированная раскладка</h2>
 * Ширины берутся из колонок и ячеек первой строки, остаток делится поровну между прочими колонками.
 * Содержимое на ширину не влияет.
 *
 * @author Manuloff
 * @since 2.0
 */
final class ColumnWidths {

    private static final double EPSILON = 1e-6;

    /**
     * Группы колонок в порядке приоритета при распределении ширины.
     */
    private static final int PERCENT = 0;
    private static final int FIXED = 1;
    private static final int AUTO = 2;

    private final List<CellBox> cells;
    private final int n;
    private final Map<Integer, Column> columns;
    private final TableStyle ts;
    private final double spacing;

    ColumnWidths(List<CellBox> cells, int columnCount, Map<Integer, Column> columns, TableStyle ts, double spacing) {
        this.cells = cells;
        this.n = columnCount;
        this.columns = columns;
        this.ts = ts;
        this.spacing = spacing;
    }

    /**
     * Вычисляет минимальную и предпочтительную ширину ячейки.
     */
    static void measure(CellBox cb) {
        Style s = cb.style;
        double ins = cb.horizontalInsets();
        // Содержимое, которое может обрезаться, не мешает сужать колонку
        double min = s.overflow() == Overflow.HIDDEN ? ins : cb.paragraph.minContentWidth() + ins;
        double pref = cb.paragraph.maxContentWidth() + ins;
        if (s.minWidth() != null) {
            min = Math.max(min, s.minWidth());
            pref = Math.max(pref, s.minWidth());
        }
        if (s.maxWidth() != null) {
            pref = Math.max(min, Math.min(pref, s.maxWidth()));
        }
        Size w = s.width();
        if (w != null) {
            if (w.isPercent()) {
                cb.percentWidth = w.getValue();
            } else {
                cb.fixedWidth = Math.max(w.getValue(), min);
                pref = cb.fixedWidth;
            }
        }
        cb.minWidth = min;
        cb.prefWidth = pref;
    }

    // ============================================
    // Автоматическая раскладка
    // ============================================

    /**
     * @param extras     ширина таблицы вне колонок (рамки, отступы, промежутки)
     * @param available  доступная ширина или NaN
     * @param target     заданная ширина таблицы или NaN
     * @param captionMin минимальная ширина заголовка таблицы (0, если его нет)
     */
    double[] auto(double extras, double available, double target, double captionMin) {
        int n = this.n;
        double[] min = new double[n];
        double[] pref = new double[n];
        double[] fixed = new double[n];
        double[] percent = new double[n];
        Arrays.fill(fixed, Double.NaN);
        Arrays.fill(percent, Double.NaN);

        for (CellBox cb : this.cells) {
            if (cb.colspan() != 1) continue;
            int c = cb.col();
            min[c] = Math.max(min[c], cb.minWidth);
            pref[c] = Math.max(pref[c], cb.prefWidth);
            fixed[c] = max(fixed[c], cb.fixedWidth);
            percent[c] = max(percent[c], cb.percentWidth);
        }
        applyColumnWidths(fixed, percent, min);
        for (int c = 0; c < n; c++) {
            if (!Double.isNaN(fixed[c])) pref[c] = Math.max(min[c], fixed[c]);
            pref[c] = Math.max(pref[c], min[c]);
        }
        distributeSpanning(min, pref);

        double grid = gridWidth(extras, available, target, captionMin, sum(min), sum(pref));
        return distribute(grid, min, pref, fixed, percent);
    }

    /**
     * Ширины, заданные колонкам ({@link Column#width(double)}).
     */
    private void applyColumnWidths(double[] fixed, double[] percent, double[] min) {
        for (Map.Entry<Integer, Column> e : this.columns.entrySet()) {
            int c = e.getKey();
            Size w = e.getValue().style().width();
            if (c >= this.n || w == null) continue;
            if (w.isPercent()) {
                percent[c] = w.getValue();
            } else {
                fixed[c] = Math.max(w.getValue(), min[c]);
            }
        }
    }

    /**
     * Объединённые ячейки добавляют недостающую ширину в свои колонки (сначала узкие объединения).
     */
    private void distributeSpanning(double[] min, double[] pref) {
        List<CellBox> spanning = new ArrayList<>();
        for (CellBox cb : this.cells) {
            if (cb.colspan() > 1) spanning.add(cb);
        }
        spanning.sort(Comparator.comparingInt(CellBox::colspan));
        for (CellBox cb : spanning) {
            double inner = (cb.colspan() - 1) * this.spacing;
            distributeExcess(min, pref, cb.col(), cb.colspan(), cb.minWidth - inner);
            distributeExcess(pref, pref, cb.col(), cb.colspan(), cb.prefWidth - inner);
            for (int c = cb.col(); c < cb.col() + cb.colspan(); c++) pref[c] = Math.max(pref[c], min[c]);
        }
    }

    private double gridWidth(double extras, double available, double target, double captionMin,
                             double sumMin, double sumPref) {
        double grid;
        if (!Double.isNaN(target)) {
            grid = target - extras;
        } else {
            grid = sumPref;
            if (!Double.isNaN(available)) grid = Math.min(grid, available - extras);
        }
        if (this.ts.maxWidth() != null) grid = Math.min(grid, this.ts.maxWidth() - extras);
        if (this.ts.minWidth() != null) grid = Math.max(grid, this.ts.minWidth() - extras);
        grid = Math.max(grid, captionMin - extras);
        return Math.max(grid, sumMin);
    }

    private double[] distribute(double grid, double[] min, double[] pref, double[] fixed, double[] percent) {
        int n = this.n;
        double[] w = min.clone();
        double remaining = grid - sum(min);

        double[] want = new double[n];
        int[] group = new int[n];
        for (int c = 0; c < n; c++) {
            if (!Double.isNaN(percent[c])) {
                group[c] = PERCENT;
                want[c] = Math.max(min[c], percent[c] * grid / 100);
            } else {
                group[c] = Double.isNaN(fixed[c]) ? AUTO : FIXED;
                want[c] = pref[c];
            }
        }

        // До желаемой ширины - по группам в порядке приоритета
        for (int g = PERCENT; g <= AUTO && remaining > EPSILON; g++) {
            double need = 0;
            for (int c = 0; c < n; c++) {
                if (group[c] == g) need += want[c] - w[c];
            }
            if (need <= EPSILON) continue;
            double give = Math.min(remaining, need);
            for (int c = 0; c < n; c++) {
                if (group[c] == g) w[c] += (want[c] - w[c]) * give / need;
            }
            remaining -= give;
        }

        // Остаток - автоматическим колонкам, иначе фиксированным, иначе процентным
        if (remaining > EPSILON) {
            for (int g : new int[]{AUTO, FIXED, PERCENT}) {
                if (spread(w, group, g, remaining)) break;
            }
        }
        return w;
    }

    /**
     * Раздаёт ширину колонкам группы пропорционально их ширине.
     *
     * @return false, если в группе нет колонок
     */
    private static boolean spread(double[] w, int[] group, int g, double amount) {
        double weight = 0;
        int count = 0;
        for (int c = 0; c < w.length; c++) {
            if (group[c] == g) {
                weight += w[c];
                count++;
            }
        }
        if (count == 0) return false;
        for (int c = 0; c < w.length; c++) {
            if (group[c] == g) w[c] += weight > EPSILON ? amount * w[c] / weight : amount / count;
        }
        return true;
    }

    // ============================================
    // Фиксированная раскладка
    // ============================================

    /**
     * @param grid ширина сетки (ширина таблицы без рамок, отступов и промежутков)
     */
    double[] fixed(double grid) {
        int n = this.n;
        double[] w = new double[n];
        boolean[] set = new boolean[n];
        for (Map.Entry<Integer, Column> e : this.columns.entrySet()) {
            int c = e.getKey();
            Size width = e.getValue().style().width();
            if (c < n && width != null) {
                w[c] = width.resolve(grid);
                set[c] = true;
            }
        }
        for (CellBox cb : this.cells) {
            if (cb.row() != 0 || cb.style.width() == null) continue;
            boolean anySet = false;
            for (int c = cb.col(); c < cb.col() + cb.colspan(); c++) anySet |= set[c];
            if (anySet) continue;
            double total = cb.style.width().resolve(grid);
            for (int c = cb.col(); c < cb.col() + cb.colspan(); c++) {
                w[c] = Math.max(0, (total - (cb.colspan() - 1) * this.spacing) / cb.colspan());
                set[c] = true;
            }
        }

        double used = 0;
        int auto = 0;
        for (int c = 0; c < n; c++) {
            if (set[c]) used += w[c];
            else auto++;
        }
        if (auto > 0) {
            double rest = Math.max(0, grid - used) / auto;
            for (int c = 0; c < n; c++) {
                if (!set[c]) w[c] = rest;
            }
        } else if (used < grid && n > 0) {
            // Все ширины заданы и меньше таблицы - растягиваем пропорционально
            for (int c = 0; c < n; c++) w[c] += used > 0 ? (grid - used) * w[c] / used : (grid - used) / n;
        }
        return w;
    }

    // ============================================
    // Утилиты
    // ============================================

    /**
     * Добавляет значениям диапазона, чтобы их сумма была не меньше {@code required},
     * пропорционально весам (или поровну, если веса нулевые).
     */
    static void distributeExcess(double[] values, double[] weights, int from, int count, double required) {
        double current = 0;
        double weight = 0;
        for (int c = from; c < from + count; c++) {
            current += values[c];
            weight += weights[c];
        }
        double excess = required - current;
        if (excess <= EPSILON) return;
        for (int c = from; c < from + count; c++) {
            values[c] += weight > EPSILON ? excess * weights[c] / weight : excess / count;
        }
    }

    static double sum(double[] values) {
        double s = 0;
        for (double v : values) s += v;
        return s;
    }

    private static double max(double current, double value) {
        if (Double.isNaN(value)) return current;
        return Double.isNaN(current) ? value : Math.max(current, value);
    }
}
