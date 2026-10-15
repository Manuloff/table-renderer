package me.manuloff.lib.table.renderer.internal.layout;

import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.internal.text.FontResolver;
import me.manuloff.lib.table.renderer.internal.text.Paragraph;
import me.manuloff.lib.table.renderer.internal.text.TextMeasurer;
import me.manuloff.lib.table.renderer.render.RenderOptions;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.table.BorderCollapse;
import me.manuloff.lib.table.renderer.style.table.TableLayout;

/**
 * Раскладка таблицы. Шаги:
 * <ol>
 *     <li>{@link TableGrid} - размещение ячеек в сетке;</li>
 *     <li>{@link CellStyleResolver} - каскад стилей и содержимое ячеек;</li>
 *     <li>{@link CollapsedBorders} - общие рамки (в модели схлопывания) и толщины рамок для раскладки;</li>
 *     <li>{@link ColumnWidths} - ширина колонок;</li>
 *     <li>{@link RowHeights} - высота строк;</li>
 *     <li>положения линий сетки (с привязкой к пикселям устройства), геометрия ячеек, вертикальное выравнивание;</li>
 *     <li>{@link CaptionBox} - заголовок по ширине таблицы.</li>
 * </ol>
 *
 * @author Manuloff
 * @since 2.0
 */
public final class TableLayoutEngine {

    private final Table table;
    private final TableStyle ts;
    private final RenderOptions options;
    private final FontResolver fonts;
    private final TextMeasurer measurer;
    private final boolean collapse;
    private final double spacingX;
    private final double spacingY;

    private TableLayoutEngine(Table table, RenderOptions options, FontResolver fonts, TextMeasurer measurer) {
        this.table = table;
        this.ts = table.style();
        this.options = options;
        this.fonts = fonts;
        this.measurer = measurer;
        this.collapse = this.ts.borderCollapse() == BorderCollapse.COLLAPSE;
        this.spacingX = this.collapse ? 0 : this.ts.horizontalSpacing();
        this.spacingY = this.collapse ? 0 : this.ts.verticalSpacing();
    }

    /**
     * @return разложенная таблица
     */
    public static LaidOutTable layout(Table table, RenderOptions options, FontResolver fonts, TextMeasurer measurer) {
        return new TableLayoutEngine(table, options, fonts, measurer).run();
    }

    private LaidOutTable run() {
        TableGrid grid = TableGrid.build(this.table);
        for (CellBox cb : grid.cells) {
            cb.style = CellStyleResolver.resolve(this.table, cb, grid.columnCount);
            cb.paragraph = Paragraph.build(CellStyleResolver.contentOf(this.table, cb.cell()), cb.style,
                    this.fonts, this.measurer, this.options.getLocale());
            cb.empty = cb.paragraph.isEmpty();
        }

        CollapsedBorders collapsed = resolveBorders(grid);
        double[] insets = gridInsets(collapsed);
        CaptionBox caption = CaptionBox.of(this.table, this.fonts, this.measurer, this.options.getLocale());

        // Ширины колонок и линии по горизонтали
        double[] widths = columnWidths(grid, insets, caption);
        double[] xs = lines(insets[3], widths, this.spacingX);
        int n = grid.columnCount;
        // Таблица без колонок всё равно должна вместить заголовок
        double width = n == 0
                ? snap(Math.max(insets[3] + insets[1], caption == null ? 0 : caption.minWidth()))
                : snap(xs[n] - this.spacingX + insets[1]);

        // Высоты строк и линии по вертикали
        int rows = grid.rows.size();
        double gridHeightTarget = this.ts.height() == null ? Double.NaN : this.ts.height() - insets[0] - insets[2];
        RowHeights heights = RowHeights.compute(grid.rows, grid.cells, widths, this.spacingX, this.spacingY,
                gridHeightTarget);
        double[] ys = lines(insets[0], heights.heights, this.spacingY);
        double height = rows == 0 ? snap(insets[0] + insets[2]) : snap(ys[rows] - this.spacingY + insets[2]);
        if (this.ts.height() != null) height = Math.max(height, this.ts.height());

        for (CellBox cb : grid.cells) placeCell(cb, xs, ys, heights.baselines);
        if (caption != null) caption.layout(width);

        return new LaidOutTable(this.table, this.collapse, grid, collapsed, caption, width, height, xs, ys,
                this.spacingX, this.spacingY);
    }

    private CollapsedBorders resolveBorders(TableGrid grid) {
        if (!this.collapse) {
            for (CellBox cb : grid.cells) {
                cb.borderTop = cb.style.borderTop().effectiveWidth();
                cb.borderRight = cb.style.borderRight().effectiveWidth();
                cb.borderBottom = cb.style.borderBottom().effectiveWidth();
                cb.borderLeft = cb.style.borderLeft().effectiveWidth();
            }
            return null;
        }
        CollapsedBorders collapsed = new CollapsedBorders(grid.rows.size(), grid.columnCount);
        for (CellBox cb : grid.cells) collapsed.addCell(cb);
        collapsed.addTable(this.ts);
        for (CellBox cb : grid.cells) collapsed.applyTo(cb);
        return collapsed;
    }

    /**
     * @return расстояние от края блока таблицы до сетки ячеек: {сверху, справа, снизу, слева}
     */
    private double[] gridInsets(CollapsedBorders collapsed) {
        if (collapsed != null) return collapsed.outerHalves();
        TableStyle ts = this.ts;
        return new double[]{
                ts.borderTop().effectiveWidth() + ts.padding().getTop() + this.spacingY,
                ts.borderRight().effectiveWidth() + ts.padding().getRight() + this.spacingX,
                ts.borderBottom().effectiveWidth() + ts.padding().getBottom() + this.spacingY,
                ts.borderLeft().effectiveWidth() + ts.padding().getLeft() + this.spacingX
        };
    }

    private double[] columnWidths(TableGrid grid, double[] insets, CaptionBox caption) {
        int n = grid.columnCount;
        double extras = insets[1] + insets[3] + Math.max(0, n - 1) * this.spacingX;
        double available = this.options.getAvailableWidth() == null ? Double.NaN
                : this.options.getAvailableWidth() - this.ts.margin().horizontal();
        double target = this.ts.width() == null ? Double.NaN : this.ts.width().resolve(available);

        for (CellBox cb : grid.cells) ColumnWidths.measure(cb);
        ColumnWidths widths = new ColumnWidths(grid.cells, n, this.table.columns(), this.ts, this.spacingX);
        if (this.ts.layout() == TableLayout.FIXED && !Double.isNaN(target)) {
            return widths.fixed(Math.max(0, target - extras));
        }
        return widths.auto(extras, available, target, caption == null ? 0 : caption.minWidth());
    }

    /**
     * @return положения линий: начало, затем после каждого размера и промежутка (с привязкой к пикселям)
     */
    private double[] lines(double start, double[] sizes, double spacing) {
        double[] lines = new double[sizes.length + 1];
        double pos = start;
        for (int i = 0; i <= sizes.length; i++) {
            lines[i] = snap(pos);
            if (i < sizes.length) pos += sizes[i] + spacing;
        }
        return lines;
    }

    /**
     * Прямоугольник ячейки и вертикальное выравнивание содержимого.
     */
    private void placeCell(CellBox cb, double[] xs, double[] ys, double[] rowBaselines) {
        double x0 = xs[cb.col()];
        double y0 = ys[cb.row()];
        double x1 = xs[cb.col() + cb.colspan()] - this.spacingX;
        double y1 = ys[cb.row() + cb.rowspan()] - this.spacingY;
        cb.bounds.setRect(x0, y0, x1 - x0, y1 - y0);

        double free = cb.bounds.height - cb.verticalInsets() - cb.block.height();
        switch (cb.style.verticalAlign()) {
            case TOP:
                cb.contentOffset = 0;
                break;
            case BOTTOM:
                cb.contentOffset = Math.max(0, free);
                break;
            case BASELINE:
                cb.contentOffset = rowBaselines[cb.row()] - cb.baselineOffset;
                break;
            default:
                cb.contentOffset = Math.max(0, free / 2);
                break;
        }
    }

    /**
     * Привязка координаты к пикселям устройства (чётное округление, чтобы половинки рамок не "уезжали").
     */
    private double snap(double v) {
        double scale = this.options.getScale();
        return Math.rint(v * scale) / scale;
    }
}
