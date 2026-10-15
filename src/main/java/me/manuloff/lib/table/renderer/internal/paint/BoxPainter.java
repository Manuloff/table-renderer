package me.manuloff.lib.table.renderer.internal.paint;

import me.manuloff.lib.table.renderer.internal.paint.background.BackgroundPainter;
import me.manuloff.lib.table.renderer.internal.paint.border.BorderPainter;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.border.BorderStyle;
import me.manuloff.lib.table.renderer.style.box.Shadow;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * Отрисовка оформления блока в порядке CSS: внешние тени, фон, внутренние тени, рамка.
 * Контур (outline) рисуется отдельно - после содержимого.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class BoxPainter {

    private BoxPainter() {
    }

    /**
     * Тени, фон и рамка блока.
     *
     * @param r прямоугольник по внешнему краю рамки
     */
    public static void paint(Graphics2D g, Rectangle2D r, BoxDecor d) {
        if (r.getWidth() <= 0 || r.getHeight() <= 0) return;
        Radii radii = Radii.of(d.radius, r.getWidth(), r.getHeight());
        ShadowPainter.paintOuter(g, r, radii, d.shadows);
        paintBackground(g, r, d, radii);
        paintInsetShadows(g, r, d, radii);
        paintBorder(g, r, d, radii);
    }

    /**
     * Фон: рисуется по внешнему краю рамки, слои позиционируются по области под рамкой.
     */
    public static void paintBackground(Graphics2D g, Rectangle2D r, BoxDecor d, Radii radii) {
        if (d.background == null && d.layers.isEmpty()) return;
        BackgroundPainter.paint(g, d.background, d.layers, radii.shape(r), paddingBox(r, d));
    }

    /**
     * Внутренние тени: рисуются внутри рамки, поверх фона.
     */
    public static void paintInsetShadows(Graphics2D g, Rectangle2D r, BoxDecor d, Radii radii) {
        if (d.shadows.stream().noneMatch(Shadow::isInset)) return;
        Rectangle2D pb = paddingBox(r, d);
        Radii pr = radii.inset(d.topWidth(), d.rightWidth(), d.bottomWidth(), d.leftWidth());
        ShadowPainter.paintInset(g, pr.shape(pb), pb, pr, d.shadows);
    }

    /**
     * Рамка блока со скруглением.
     */
    public static void paintBorder(Graphics2D g, Rectangle2D r, BoxDecor d, Radii radii) {
        BorderPainter.paint(g, r, new BorderSide[]{d.top, d.right, d.bottom, d.left}, radii);
    }

    /**
     * Контур (outline) снаружи рамки на расстоянии {@code outlineOffset}.
     */
    public static void paintOutline(Graphics2D g, Rectangle2D r, BoxDecor d) {
        if (!d.hasOutline()) return;
        BorderSide o = d.outline;
        double e = o.getWidth() + d.outlineOffset;
        Rectangle2D outer = new Rectangle2D.Double(r.getX() - e, r.getY() - e, r.getWidth() + 2 * e, r.getHeight() + 2 * e);
        if (outer.getWidth() <= 0 || outer.getHeight() <= 0) return;
        Radii radii = Radii.of(d.radius, r.getWidth(), r.getHeight()).outset(e);
        BorderSide side = o.getStyle() == BorderStyle.HIDDEN ? BorderSide.NONE : o;
        BorderPainter.paint(g, outer, new BorderSide[]{side, side, side, side}, radii);
    }

    /**
     * @return прямоугольник области под рамкой
     */
    public static Rectangle2D paddingBox(Rectangle2D r, BoxDecor d) {
        double l = d.leftWidth();
        double t = d.topWidth();
        return new Rectangle2D.Double(r.getX() + l, r.getY() + t,
                Math.max(0, r.getWidth() - l - d.rightWidth()), Math.max(0, r.getHeight() - t - d.bottomWidth()));
    }

    /**
     * @return насколько тени и контур выходят за пределы блока: {сверху, справа, снизу, слева}
     */
    public static double[] inkOverflow(BoxDecor d) {
        double[] e = ShadowPainter.overflow(d.shadows);
        if (d.hasOutline()) {
            double o = d.outline.getWidth() + d.outlineOffset;
            for (int i = 0; i < 4; i++) e[i] = Math.max(e[i], o);
        }
        return e;
    }

    /**
     * @return прямоугольник, расширенный на {сверху, справа, снизу, слева}
     */
    public static Rectangle2D expand(Rectangle2D r, double[] e) {
        return new Rectangle2D.Double(r.getX() - e[3], r.getY() - e[0], r.getWidth() + e[1] + e[3], r.getHeight() + e[0] + e[2]);
    }
}
