package me.manuloff.lib.table.renderer.internal.paint.border;

import me.manuloff.lib.table.renderer.internal.paint.Radii;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.border.BorderStyle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Отрисовка рамки блока со своими стилями, толщинами и цветами сторон и скруглёнными углами.
 * <p>
 * Рамка - кольцо между внешним и внутренним контурами; каждая сторона - часть кольца, отсечённая
 * трапецией от внешних углов к внутренним (как в браузерах).
 *
 * @author Manuloff
 * @since 2.0
 */
public final class BorderPainter {

    private static final int TOP = 0;
    private static final int RIGHT = 1;
    private static final int BOTTOM = 2;
    private static final int LEFT = 3;

    private BorderPainter() {
    }

    /**
     * @param r     внешний прямоугольник рамки
     * @param sides стороны в порядке top, right, bottom, left
     * @param radii радиусы внешнего контура
     */
    public static void paint(Graphics2D g, Rectangle2D r, BorderSide[] sides, Radii radii) {
        boolean any = false;
        for (BorderSide s : sides) any |= s.isVisible();
        if (!any) return;

        double[] w = widths(sides);
        BorderSide first = sides[TOP];
        boolean uniform = true;
        for (BorderSide s : sides) {
            uniform &= s.getStyle() == first.getStyle() && s.getWidth() == first.getWidth() && s.getColor().equals(first.getColor());
        }

        if (uniform) {
            switch (first.getStyle()) {
                case SOLID:
                    g.setColor(first.getColor());
                    g.fill(ring(r, radii, w, 0, 1));
                    return;
                case DOUBLE:
                    g.setColor(first.getColor());
                    g.fill(ring(r, radii, w, 0, 1.0 / 3));
                    g.fill(ring(r, radii, w, 2.0 / 3, 1));
                    return;
                case DASHED:
                case DOTTED:
                    strokeCenter(g, r, radii, w, first);
                    return;
                default:
                    break;
            }
        }

        // Стороны с одинаковой отрисовкой объединяем, чтобы на стыках не было швов от сглаживания
        boolean[] done = new boolean[4];
        for (int i = 0; i < 4; i++) {
            if (done[i] || !sides[i].isVisible()) continue;
            Area wedge = new Area(wedge(r, w, i));
            for (int j = i + 1; j < 4; j++) {
                if (!done[j] && sameLook(sides[i], sides[j], i, j)) {
                    wedge.add(new Area(wedge(r, w, j)));
                    done[j] = true;
                }
            }
            done[i] = true;
            paintSide(g, r, radii, w, sides[i], i, wedge);
        }
    }

    /**
     * Стороны рисуются одинаково, если совпадают стиль, толщина и цвет, а для объёмных стилей - ещё и освещённость.
     */
    private static boolean sameLook(BorderSide a, BorderSide b, int ia, int ib) {
        if (a.getStyle() != b.getStyle() || a.getWidth() != b.getWidth() || !a.getColor().equals(b.getColor())) return false;
        switch (a.getStyle()) {
            case GROOVE:
            case RIDGE:
            case INSET:
            case OUTSET:
                return isTopLeft(ia) == isTopLeft(ib);
            case DASHED:
            case DOTTED:
                return false;
            default:
                return true;
        }
    }

    private static boolean isTopLeft(int side) {
        return side == TOP || side == LEFT;
    }

    private static double[] widths(BorderSide[] sides) {
        return new double[]{sides[TOP].effectiveWidth(), sides[RIGHT].effectiveWidth(),
                sides[BOTTOM].effectiveWidth(), sides[LEFT].effectiveWidth()};
    }

    private static void paintSide(Graphics2D g, Rectangle2D r, Radii radii, double[] w, BorderSide side, int index, Area wedge) {
        Color c = side.getColor();
        switch (side.getStyle()) {
            case SOLID:
                fill(g, ring(r, radii, w, 0, 1), wedge, c);
                break;
            case DOUBLE:
                fill(g, ring(r, radii, w, 0, 1.0 / 3), wedge, c);
                fill(g, ring(r, radii, w, 2.0 / 3, 1), wedge, c);
                break;
            case GROOVE:
            case RIDGE: {
                boolean groove = side.getStyle() == BorderStyle.GROOVE;
                boolean topLeft = isTopLeft(index);
                Color outer = groove == topLeft ? BorderStrokes.darker(c) : BorderStrokes.lighter(c);
                Color inner = groove == topLeft ? BorderStrokes.lighter(c) : BorderStrokes.darker(c);
                fill(g, ring(r, radii, w, 0, 0.5), wedge, outer);
                fill(g, ring(r, radii, w, 0.5, 1), wedge, inner);
                break;
            }
            case INSET:
            case OUTSET: {
                boolean inset = side.getStyle() == BorderStyle.INSET;
                fill(g, ring(r, radii, w, 0, 1), wedge, inset == isTopLeft(index) ? BorderStrokes.darker(c) : BorderStrokes.lighter(c));
                break;
            }
            case DASHED:
            case DOTTED: {
                Graphics2D sg = (Graphics2D) g.create();
                sg.clip(wedge);
                strokeCenter(sg, r, radii, w, side);
                sg.dispose();
                break;
            }
            default:
                break;
        }
    }

    private static void fill(Graphics2D g, Shape ring, Area wedge, Color color) {
        Area a = new Area(ring);
        a.intersect(wedge);
        g.setColor(color);
        g.fill(a);
    }

    /**
     * Часть кольца рамки между долями толщины {@code from} и {@code to} (0 - внешний край, 1 - внутренний).
     */
    private static Shape ring(Rectangle2D r, Radii radii, double[] w, double from, double to) {
        Area outer = new Area(contour(r, radii, w, from));
        outer.subtract(new Area(contour(r, radii, w, to)));
        return outer;
    }

    /**
     * Контур на доле {@code k} толщины рамки внутрь от внешнего края.
     */
    private static Shape contour(Rectangle2D r, Radii radii, double[] w, double k) {
        double t = w[TOP] * k;
        double rt = w[RIGHT] * k;
        double b = w[BOTTOM] * k;
        double l = w[LEFT] * k;
        return radii.inset(t, rt, b, l).shape(r.getX() + l, r.getY() + t,
                r.getWidth() - l - rt, r.getHeight() - t - b);
    }

    private static void strokeCenter(Graphics2D g, Rectangle2D r, Radii radii, double[] w, BorderSide side) {
        double width = side.getWidth();
        Shape center = contour(r, radii, w, 0.5);
        g.setColor(side.getColor());
        g.setStroke(BorderStrokes.dashed(side.getStyle(), width));
        g.draw(center);
        g.setStroke(new BasicStroke());
    }

    /**
     * Трапеция стороны: от внешних углов к внутренним, с запасом наружу.
     */
    private static Shape wedge(Rectangle2D r, double[] w, int side) {
        double x = r.getX();
        double y = r.getY();
        double x2 = r.getMaxX();
        double y2 = r.getMaxY();
        double ix = x + w[LEFT];
        double iy = y + w[TOP];
        double ix2 = x2 - w[RIGHT];
        double iy2 = y2 - w[BOTTOM];
        double e = 2;
        Path2D.Double p = new Path2D.Double();
        switch (side) {
            case TOP:
                p.moveTo(x - e, y - e);
                p.lineTo(x2 + e, y - e);
                p.lineTo(ix2, iy);
                p.lineTo(ix, iy);
                break;
            case RIGHT:
                p.moveTo(x2 + e, y - e);
                p.lineTo(x2 + e, y2 + e);
                p.lineTo(ix2, iy2);
                p.lineTo(ix2, iy);
                break;
            case BOTTOM:
                p.moveTo(x2 + e, y2 + e);
                p.lineTo(x - e, y2 + e);
                p.lineTo(ix, iy2);
                p.lineTo(ix2, iy2);
                break;
            default:
                p.moveTo(x - e, y2 + e);
                p.lineTo(x - e, y - e);
                p.lineTo(ix, iy);
                p.lineTo(ix, iy2);
                break;
        }
        p.closePath();
        return p;
    }
}
