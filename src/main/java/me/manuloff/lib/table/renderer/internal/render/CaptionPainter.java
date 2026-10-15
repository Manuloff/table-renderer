package me.manuloff.lib.table.renderer.internal.render;

import me.manuloff.lib.table.renderer.internal.layout.CaptionBox;
import me.manuloff.lib.table.renderer.internal.paint.BoxDecor;
import me.manuloff.lib.table.renderer.internal.paint.BoxPainter;
import me.manuloff.lib.table.renderer.internal.paint.effect.OpacityLayer;
import me.manuloff.lib.table.renderer.internal.text.TextPainter;
import me.manuloff.lib.table.renderer.style.Style;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Отрисовка заголовка таблицы: оформление, текст с вертикальным выравниванием, контур.
 *
 * @author Manuloff
 * @since 2.0
 */
final class CaptionPainter {

    private final CaptionBox caption;
    private final TextPainter text;

    CaptionPainter(CaptionBox caption, TextPainter text) {
        this.caption = caption;
        this.text = text;
    }

    /**
     * @return насколько тени и контур заголовка выходят за его пределы: {сверху, справа, снизу, слева}
     */
    double[] inkOverflow() {
        return BoxPainter.inkOverflow(BoxDecor.of(this.caption.style()));
    }

    /**
     * @param r прямоугольник заголовка
     */
    void paint(Graphics2D g, Rectangle2D r) {
        Style s = this.caption.style();
        if (Boolean.TRUE.equals(s.hidden())) return;
        BoxDecor decor = BoxDecor.of(s);
        Rectangle2D area = BoxPainter.expand(r, inkOverflow());
        OpacityLayer.paint(g, s.opacity(), area, gg -> {
            BoxPainter.paint(gg, r, decor);
            double x = r.getX() + this.caption.insetLeft();
            double y = r.getY() + this.caption.insetTop() + verticalOffset(r);
            this.text.paint(gg, this.caption.block(), x, y);
            BoxPainter.paintOutline(gg, r, decor);
        });
    }

    private double verticalOffset(Rectangle2D r) {
        Style s = this.caption.style();
        double area = r.getHeight() - this.caption.insetTop() - s.paddingBottom() - s.borderBottom().effectiveWidth();
        double free = Math.max(0, area - this.caption.block().height());
        switch (s.verticalAlign()) {
            case BOTTOM:
                return free;
            case MIDDLE:
                return free / 2;
            default:
                return 0;
        }
    }
}
