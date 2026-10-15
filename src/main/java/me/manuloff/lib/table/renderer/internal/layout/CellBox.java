package me.manuloff.lib.table.renderer.internal.layout;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.internal.text.Paragraph;
import me.manuloff.lib.table.renderer.internal.text.TextBlock;
import me.manuloff.lib.table.renderer.style.Style;

import java.awt.geom.Rectangle2D;

/**
 * Ячейка в сетке таблицы: положение, вычисленный стиль, содержимое и геометрия.
 * Заполняется по шагам раскладки ({@link TableLayoutEngine}).
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
@Accessors(fluent = true)
public final class CellBox {

    private final Cell cell;
    /**
     * Первая строка и колонка сетки, занимаемые ячейкой.
     */
    private final int row;
    private final int col;
    private final int rowspan;
    private final int colspan;
    /**
     * Индекс строки внутри секции и количество строк секции (для условных правил).
     */
    private final int sectionRowIndex;
    private final int sectionRowCount;

    /**
     * Вычисленный стиль: все обязательные свойства заданы.
     */
    Style style;
    Paragraph paragraph;
    TextBlock block;
    /**
     * В ячейке нет содержимого (после обработки пробелов).
     */
    boolean empty;

    /**
     * Толщины рамки для раскладки: в раздельной модели - собственные, в модели схлопывания - половины общих рамок.
     */
    double borderTop;
    double borderRight;
    double borderBottom;
    double borderLeft;

    /**
     * Минимальная и предпочтительная ширина ячейки (с отступами и рамкой).
     */
    double minWidth;
    double prefWidth;
    /**
     * Ширина из стиля в пикселях (NaN - не задана).
     */
    double fixedWidth = Double.NaN;
    /**
     * Ширина из стиля в процентах от таблицы (NaN - не задана).
     */
    double percentWidth = Double.NaN;

    /**
     * Необходимая высота (с отступами и рамкой) и расстояние от верха до базовой линии первой строки.
     */
    double neededHeight;
    double baselineOffset;

    /**
     * Прямоугольник ячейки относительно блока таблицы (в модели схлопывания - от центра до центра общих рамок).
     */
    final Rectangle2D.Double bounds = new Rectangle2D.Double();
    /**
     * Сдвиг текста вниз внутри области содержимого (вертикальное выравнивание).
     */
    double contentOffset;

    CellBox(Cell cell, int row, int col, int rowspan, int colspan, int sectionRowIndex, int sectionRowCount) {
        this.cell = cell;
        this.row = row;
        this.col = col;
        this.rowspan = rowspan;
        this.colspan = colspan;
        this.sectionRowIndex = sectionRowIndex;
        this.sectionRowCount = sectionRowCount;
    }

    /**
     * @return рамка и отступ слева
     */
    public double insetLeft() {
        return this.borderLeft + this.style.paddingLeft();
    }

    /**
     * @return рамка и отступ справа
     */
    public double insetRight() {
        return this.borderRight + this.style.paddingRight();
    }

    /**
     * @return рамка и отступ сверху
     */
    public double insetTop() {
        return this.borderTop + this.style.paddingTop();
    }

    /**
     * @return рамка и отступ снизу
     */
    public double insetBottom() {
        return this.borderBottom + this.style.paddingBottom();
    }

    /**
     * @return рамки и отступы слева и справа
     */
    public double horizontalInsets() {
        return insetLeft() + insetRight();
    }

    /**
     * @return рамки и отступы сверху и снизу
     */
    public double verticalInsets() {
        return insetTop() + insetBottom();
    }

    /**
     * @return область содержимого (внутри отступов)
     */
    public Rectangle2D content() {
        return new Rectangle2D.Double(this.bounds.x + insetLeft(), this.bounds.y + insetTop(),
                Math.max(0, this.bounds.width - horizontalInsets()), Math.max(0, this.bounds.height - verticalInsets()));
    }

    /**
     * @return область под рамкой (внутри рамки, включая отступы)
     */
    public Rectangle2D paddingBox() {
        return new Rectangle2D.Double(this.bounds.x + this.borderLeft, this.bounds.y + this.borderTop,
                Math.max(0, this.bounds.width - this.borderLeft - this.borderRight),
                Math.max(0, this.bounds.height - this.borderTop - this.borderBottom));
    }
}
