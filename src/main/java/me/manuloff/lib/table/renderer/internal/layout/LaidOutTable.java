package me.manuloff.lib.table.renderer.internal.layout;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.manuloff.lib.table.renderer.Section;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.style.TableStyle;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Результат раскладки таблицы - всё, что нужно для отрисовки. Координаты отсчитываются от левого верхнего
 * угла блока таблицы (по внешнему краю рамки).
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
@Accessors(fluent = true)
public final class LaidOutTable {

    private final Table table;
    private final TableStyle style;
    /**
     * Модель схлопывающихся рамок.
     */
    private final boolean collapse;
    private final List<CellBox> cells;
    /**
     * Общие рамки (только в модели схлопывания, иначе null).
     */
    private final CollapsedBorders collapsedBorders;
    /**
     * Заголовок или null.
     */
    private final CaptionBox caption;
    private final double width;
    private final double height;

    private final TableGrid grid;
    /**
     * Левые края колонок (раздельная модель) или линии сетки (схлопывание): columnCount + 1 значений.
     */
    private final double[] columnLines;
    /**
     * Верхние края строк или линии сетки: rowCount + 1 значений.
     */
    private final double[] rowLines;
    private final double spacingX;
    private final double spacingY;

    LaidOutTable(Table table, boolean collapse, TableGrid grid, CollapsedBorders collapsedBorders, CaptionBox caption,
                 double width, double height, double[] columnLines, double[] rowLines, double spacingX, double spacingY) {
        this.table = table;
        this.style = table.style();
        this.collapse = collapse;
        this.grid = grid;
        this.cells = grid.cells;
        this.collapsedBorders = collapsedBorders;
        this.caption = caption;
        this.width = width;
        this.height = height;
        this.columnLines = columnLines;
        this.rowLines = rowLines;
        this.spacingX = spacingX;
        this.spacingY = spacingY;
    }

    /**
     * @return прямоугольник блока таблицы
     */
    public Rectangle2D tableBox() {
        return new Rectangle2D.Double(0, 0, this.width, this.height);
    }

    /**
     * @return прямоугольник строки по всем колонкам
     */
    public Rectangle2D rowRect(int row) {
        return gridRect(row, row + 1, 0, this.grid.columnCount);
    }

    /**
     * @return прямоугольник колонки по всем строкам
     */
    public Rectangle2D columnRect(int col) {
        return gridRect(0, this.grid.rows.size(), col, col + 1);
    }

    /**
     * @return прямоугольник секции (пустой, если в секции нет строк)
     */
    public Rectangle2D sectionRect(Section section) {
        int[] range = this.grid.rowRange(section);
        return range == null ? new Rectangle2D.Double() : gridRect(range[0], range[1], 0, this.grid.columnCount);
    }

    private Rectangle2D gridRect(int r0, int r1, int c0, int c1) {
        double x0 = this.columnLines[c0];
        double y0 = this.rowLines[r0];
        double x1 = this.columnLines[c1] - this.spacingX;
        double y1 = this.rowLines[r1] - this.spacingY;
        return new Rectangle2D.Double(x0, y0, Math.max(0, x1 - x0), Math.max(0, y1 - y0));
    }
}
