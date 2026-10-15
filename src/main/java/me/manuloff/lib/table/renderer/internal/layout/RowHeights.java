package me.manuloff.lib.table.renderer.internal.layout;

import me.manuloff.lib.table.renderer.Row;
import me.manuloff.lib.table.renderer.Section;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.text.VerticalAlign;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Расчёт высоты строк:
 * <ol>
 *     <li>каждая ячейка раскладывает текст по своей ширине и сообщает нужную высоту;</li>
 *     <li>строка получает максимум по своим одиночным ячейкам и минимальную высоту из стиля строки;
 *     ячейки с выравниванием {@link VerticalAlign#BASELINE} выравнивают первые строки текста;</li>
 *     <li>объединённые по вертикали ячейки досыпают высоту в свои строки пропорционально;</li>
 *     <li>если задана минимальная высота таблицы, лишнее место делится между строками тела.</li>
 * </ol>
 *
 * @author Manuloff
 * @since 2.0
 */
final class RowHeights {

    private static final double EPSILON = 1e-6;

    /**
     * Высоты строк.
     */
    final double[] heights;
    /**
     * Для каждой строки - расстояние от её верха до общей базовой линии ячеек с выравниванием BASELINE.
     */
    final double[] baselines;

    private RowHeights(double[] heights, double[] baselines) {
        this.heights = heights;
        this.baselines = baselines;
    }

    /**
     * @param colWidths       ширины колонок
     * @param spacingX        промежуток между колонками
     * @param spacingY        промежуток между строками
     * @param gridHeightTarget минимальная высота сетки строк или NaN
     */
    static RowHeights compute(List<Row> rows, List<CellBox> cells, double[] colWidths, double spacingX,
                              double spacingY, double gridHeightTarget) {
        int count = rows.size();
        double[] h = new double[count];
        for (int r = 0; r < count; r++) {
            Double rowHeight = rows.get(r).style().height();
            if (rowHeight != null) h[r] = rowHeight;
        }

        for (CellBox cb : cells) layoutContent(cb, colWidths, spacingX);

        double[] above = new double[count];
        double[] below = new double[count];
        for (CellBox cb : cells) {
            if (cb.style.verticalAlign() != VerticalAlign.BASELINE) continue;
            above[cb.row()] = Math.max(above[cb.row()], cb.baselineOffset);
            if (cb.rowspan() == 1) {
                below[cb.row()] = Math.max(below[cb.row()], cb.neededHeight - cb.baselineOffset);
            }
        }
        for (CellBox cb : cells) {
            if (cb.rowspan() != 1) continue;
            double needed = cb.neededHeight;
            if (cb.style.verticalAlign() == VerticalAlign.BASELINE) {
                needed = Math.max(needed, above[cb.row()] + below[cb.row()]);
            }
            h[cb.row()] = Math.max(h[cb.row()], needed);
        }

        distributeSpanning(cells, h, spacingY);
        if (!Double.isNaN(gridHeightTarget)) stretch(rows, h, gridHeightTarget - (count - 1) * spacingY);
        return new RowHeights(h, above);
    }

    /**
     * Раскладывает текст ячейки по её ширине и вычисляет нужную высоту.
     */
    private static void layoutContent(CellBox cb, double[] colWidths, double spacingX) {
        // Неокруглённая ширина: привязка колонок к пикселям не должна вызывать лишних переносов
        double width = (cb.colspan() - 1) * spacingX;
        for (int c = cb.col(); c < cb.col() + cb.colspan(); c++) width += colWidths[c];
        cb.block = cb.paragraph.layout(Math.max(0, width - cb.horizontalInsets()));

        Style s = cb.style;
        double needed = cb.block.height() + cb.verticalInsets();
        if (s.height() != null) needed = Math.max(needed, s.height());
        if (s.maxHeight() != null) needed = Math.max(cb.verticalInsets(), Math.min(needed, s.maxHeight()));
        cb.neededHeight = needed;

        double first = cb.block.firstBaseline();
        cb.baselineOffset = cb.insetTop() + (Double.isNaN(first) ? cb.block.height() : first);
    }

    private static void distributeSpanning(List<CellBox> cells, double[] h, double spacingY) {
        List<CellBox> spanning = new ArrayList<>();
        for (CellBox cb : cells) {
            if (cb.rowspan() > 1) spanning.add(cb);
        }
        spanning.sort(Comparator.comparingInt(CellBox::rowspan));
        for (CellBox cb : spanning) {
            double inner = (cb.rowspan() - 1) * spacingY;
            ColumnWidths.distributeExcess(h, h.clone(), cb.row(), cb.rowspan(), cb.neededHeight - inner);
        }
    }

    /**
     * Растягивает строки тела (или все строки, если тела нет) до заданной суммарной высоты.
     */
    private static void stretch(List<Row> rows, double[] h, double target) {
        double current = ColumnWidths.sum(h);
        if (h.length == 0 || target <= current + EPSILON) return;
        List<Integer> targets = new ArrayList<>();
        for (int r = 0; r < h.length; r++) {
            if (rows.get(r).section().kind() == Section.Kind.BODY) targets.add(r);
        }
        if (targets.isEmpty()) for (int r = 0; r < h.length; r++) targets.add(r);
        double weight = 0;
        for (int r : targets) weight += h[r];
        double extra = target - current;
        for (int r : targets) h[r] += weight > EPSILON ? extra * h[r] / weight : extra / targets.size();
    }
}
