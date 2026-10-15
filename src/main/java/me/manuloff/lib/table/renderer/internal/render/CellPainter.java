package me.manuloff.lib.table.renderer.internal.render;

import me.manuloff.lib.table.renderer.Column;
import me.manuloff.lib.table.renderer.internal.layout.CellBox;
import me.manuloff.lib.table.renderer.internal.layout.LaidOutTable;
import me.manuloff.lib.table.renderer.internal.paint.BoxDecor;
import me.manuloff.lib.table.renderer.internal.paint.BoxPainter;
import me.manuloff.lib.table.renderer.internal.paint.Radii;
import me.manuloff.lib.table.renderer.internal.paint.background.BackgroundPainter;
import me.manuloff.lib.table.renderer.internal.paint.effect.OpacityLayer;
import me.manuloff.lib.table.renderer.internal.text.TextPainter;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.box.Overflow;
import me.manuloff.lib.table.renderer.style.table.EmptyCells;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * Отрисовка ячеек в два прохода, как в CSS: сначала оформление всех ячеек (под ним - фоны колонок,
 * секций и строк), затем - после рамок - содержимое.
 *
 * @author Manuloff
 * @since 2.0
 */
final class CellPainter {

    private final LaidOutTable layout;
    private final TextPainter text;
    /**
     * Область, внутри которой рисуется содержимое ячеек с прозрачностью (вся таблица с тенями).
     */
    private final Rectangle2D inkBounds;

    CellPainter(LaidOutTable layout, TextPainter text, Rectangle2D inkBounds) {
        this.layout = layout;
        this.text = text;
        this.inkBounds = inkBounds;
    }

    /**
     * Фоны групп, тени, фон и рамка ячейки (рамка - только в раздельной модели).
     */
    void paintBox(Graphics2D g, CellBox cb) {
        Style s = cb.style();
        if (Boolean.TRUE.equals(s.hidden())) return;
        if (!this.layout.collapse() && cb.empty() && this.layout.style().emptyCells() == EmptyCells.HIDE) return;

        BoxDecor decor = this.layout.collapse() ? BoxDecor.of(s).withoutBorders() : BoxDecor.of(s);
        paintGroupBackgrounds(g, cb, decor);
        Rectangle2D area = BoxPainter.expand(cb.bounds(), BoxPainter.inkOverflow(decor));
        OpacityLayer.paint(g, s.opacity(), area, gg -> BoxPainter.paint(gg, cb.bounds(), decor));
    }

    /**
     * Фоны колонки, секции и строки под ячейкой: позиционируются по своей геометрии и видны только
     * в пределах ячейки (как слои фона таблицы в CSS).
     */
    private void paintGroupBackgrounds(Graphics2D g, CellBox cb, BoxDecor decor) {
        Column column = this.layout.table().columns().get(cb.col());
        Style[] styles = {column == null ? null : column.style(), cb.cell().section().style(), cb.cell().row().style()};
        Shape clip = null;
        for (int i = 0; i < styles.length; i++) {
            Style s = styles[i];
            if (s == null || (s.background() == null && s.backgroundLayers() == null)) continue;
            if (clip == null) {
                Radii radii = this.layout.collapse() ? Radii.ZERO
                        : Radii.of(cb.style().borderRadius(), cb.bounds().width, cb.bounds().height);
                clip = radii.shape(cb.bounds());
            }
            Rectangle2D origin = i == 0 ? this.layout.columnRect(cb.col())
                    : i == 1 ? this.layout.sectionRect(cb.cell().section())
                    : this.layout.rowRect(cb.row());
            BackgroundPainter.paint(g, s.background(), s.backgroundLayers(), clip, origin);
        }
    }

    /**
     * Текст ячейки с вертикальным выравниванием; при {@link Overflow#HIDDEN} - с обрезкой по области под рамкой.
     */
    void paintContent(Graphics2D g, CellBox cb) {
        Style s = cb.style();
        if (Boolean.TRUE.equals(s.hidden()) || cb.block() == null || cb.block().isEmpty()) return;

        Graphics2D cg = (Graphics2D) g.create();
        if (s.overflow() == Overflow.HIDDEN) {
            Rectangle2D pb = cb.paddingBox();
            if (this.layout.collapse()) {
                cg.clip(pb);
            } else {
                Radii radii = Radii.of(s.borderRadius(), cb.bounds().width, cb.bounds().height)
                        .inset(cb.borderTop(), cb.borderRight(), cb.borderBottom(), cb.borderLeft());
                cg.clip(radii.shape(pb));
            }
        }
        Rectangle2D content = cb.content();
        double x = content.getX();
        double y = content.getY() + cb.contentOffset();
        OpacityLayer.paint(cg, s.opacity(), this.inkBounds, gg -> this.text.paint(gg, cb.block(), x, y));
        cg.dispose();
    }

    /**
     * Контур ячейки - поверх всего.
     */
    void paintOutline(Graphics2D g, CellBox cb) {
        if (!Boolean.TRUE.equals(cb.style().hidden())) BoxPainter.paintOutline(g, cb.bounds(), BoxDecor.of(cb.style()));
    }
}
