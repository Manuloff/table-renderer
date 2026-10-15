package me.manuloff.lib.table.renderer.internal.render;

import me.manuloff.lib.table.renderer.internal.layout.CellBox;
import me.manuloff.lib.table.renderer.internal.layout.LaidOutTable;
import me.manuloff.lib.table.renderer.internal.paint.BoxDecor;
import me.manuloff.lib.table.renderer.internal.paint.BoxPainter;
import me.manuloff.lib.table.renderer.internal.paint.Radii;
import me.manuloff.lib.table.renderer.internal.paint.ShadowPainter;
import me.manuloff.lib.table.renderer.internal.paint.border.BorderPainter;
import me.manuloff.lib.table.renderer.internal.paint.border.CollapsedBorderPainter;
import me.manuloff.lib.table.renderer.internal.paint.effect.OpacityLayer;
import me.manuloff.lib.table.renderer.internal.text.TextPainter;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.border.BorderSide;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * Отрисовка блока таблицы в порядке CSS:
 * <ol>
 *     <li>внешняя тень, фон, внутренняя тень и (в раздельной модели) рамка таблицы;</li>
 *     <li>оформление ячеек (с фонами колонок, секций и строк);</li>
 *     <li>общие рамки (в модели схлопывания);</li>
 *     <li>содержимое ячеек;</li>
 *     <li>контуры ячеек и таблицы.</li>
 * </ol>
 * Содержимое обрезается по скруглению таблицы. Прозрачность таблицы применяется ко всему вместе.
 *
 * @author Manuloff
 * @since 2.0
 */
final class TablePainter {

    private final LaidOutTable layout;
    private final CellPainter cells;
    private final Rectangle2D inkBounds;

    TablePainter(LaidOutTable layout, TextPainter text) {
        this.layout = layout;
        this.inkBounds = inkBounds(layout);
        this.cells = new CellPainter(layout, text, this.inkBounds);
    }

    /**
     * @return область таблицы вместе с тенями и контурами ячеек и таблицы
     */
    static Rectangle2D inkBounds(LaidOutTable layout) {
        Rectangle2D r = BoxPainter.expand(layout.tableBox(), BoxPainter.inkOverflow(BoxDecor.of(layout.style())));
        for (CellBox cb : layout.cells()) {
            r.add(BoxPainter.expand(cb.bounds(), BoxPainter.inkOverflow(BoxDecor.of(cb.style()))));
        }
        return r;
    }

    /**
     * Рисует таблицу; точка (0, 0) графики - левый верхний угол блока таблицы.
     */
    void paint(Graphics2D g) {
        OpacityLayer.paint(g, this.layout.style().opacity(), this.inkBounds, this::paintContent);
    }

    private void paintContent(Graphics2D g) {
        LaidOutTable l = this.layout;
        TableStyle ts = l.style();
        Rectangle2D box = l.tableBox();
        BoxDecor decor = BoxDecor.of(ts);
        Radii radii = Radii.of(ts.borderRadius(), box.getWidth(), box.getHeight());

        ShadowPainter.paintOuter(g, box, radii, ts.boxShadow());
        BoxPainter.paintBackground(g, box, decor, radii);
        BoxPainter.paintInsetShadows(g, box, decor, radii);
        if (!l.collapse()) BoxPainter.paintBorder(g, box, decor, radii);

        Graphics2D content = (Graphics2D) g.create();
        if (!radii.isZero()) content.clip(contentClip(box, radii));

        for (CellBox cb : l.cells()) this.cells.paintBox(content, cb);
        if (l.collapse()) paintCollapsedBorders(g, content, box, radii);
        for (CellBox cb : l.cells()) this.cells.paintContent(content, cb);
        content.dispose();

        for (CellBox cb : l.cells()) this.cells.paintOutline(g, cb);
        BoxPainter.paintOutline(g, box, decor);
    }

    /**
     * Область обрезки содержимого: скругление таблицы (в раздельной модели - внутри её рамки).
     */
    private Shape contentClip(Rectangle2D box, Radii radii) {
        if (this.layout.collapse()) return radii.shape(box);
        TableStyle ts = this.layout.style();
        double t = ts.borderTop().effectiveWidth();
        double r = ts.borderRight().effectiveWidth();
        double b = ts.borderBottom().effectiveWidth();
        double l = ts.borderLeft().effectiveWidth();
        return radii.inset(t, r, b, l).shape(box.getX() + l, box.getY() + t, box.getWidth() - l - r, box.getHeight() - t - b);
    }

    /**
     * Общие рамки. У скруглённой таблицы с одинаковой внешней рамкой внешняя рамка рисуется
     * скруглённым контуром, иначе её отрезки обрезаются по скруглению.
     */
    private void paintCollapsedBorders(Graphics2D g, Graphics2D clipped, Rectangle2D box, Radii radii) {
        LaidOutTable l = this.layout;
        BorderSide uniform = radii.isZero() ? null : l.collapsedBorders().uniformOuter();
        CollapsedBorderPainter.paint(clipped, l.collapsedBorders(), l.columnLines(), l.rowLines(), uniform != null);
        if (uniform != null) {
            BorderPainter.paint(g, box, new BorderSide[]{uniform, uniform, uniform, uniform}, radii);
        }
    }
}
