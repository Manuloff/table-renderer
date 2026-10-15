package me.manuloff.lib.table.renderer.internal.paint;

import me.manuloff.lib.table.renderer.style.border.Corners;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Радиусы углов прямоугольника (по горизонтали и вертикали для каждого угла) и построение контура.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Radii {

    /**
     * Прямые углы.
     */
    public static final Radii ZERO = new Radii(0, 0, 0, 0, 0, 0, 0, 0);

    /**
     * Константа аппроксимации четверти эллипса кубической кривой Безье.
     */
    private static final double KAPPA = 0.5522847498;

    /**
     * Радиусы углов: {угол}{ось} - верхний левый, верхний правый, нижний правый, нижний левый.
     */
    private final double tlx, tly, trx, tRy, brx, bry, blx, bly;

    private Radii(double tlx, double tly, double trx, double tRy, double brx, double bry, double blx, double bly) {
        this.tlx = Math.max(0, tlx);
        this.tly = Math.max(0, tly);
        this.trx = Math.max(0, trx);
        this.tRy = Math.max(0, tRy);
        this.brx = Math.max(0, brx);
        this.bry = Math.max(0, bry);
        this.blx = Math.max(0, blx);
        this.bly = Math.max(0, bly);
    }

    /**
     * Радиусы для блока заданного размера: если сумма радиусов на стороне больше её длины,
     * все радиусы уменьшаются в одинаковое число раз (как в CSS).
     */
    public static Radii of(Corners c, double width, double height) {
        if (c == null || c.isZero()) return ZERO;
        double f = 1;
        f = factor(f, width, c.getTopLeft() + c.getTopRight());
        f = factor(f, width, c.getBottomLeft() + c.getBottomRight());
        f = factor(f, height, c.getTopLeft() + c.getBottomLeft());
        f = factor(f, height, c.getTopRight() + c.getBottomRight());
        return new Radii(c.getTopLeft() * f, c.getTopLeft() * f, c.getTopRight() * f, c.getTopRight() * f,
                c.getBottomRight() * f, c.getBottomRight() * f, c.getBottomLeft() * f, c.getBottomLeft() * f);
    }

    private static double factor(double f, double side, double sum) {
        return sum > side && sum > 0 ? Math.min(f, Math.max(0, side) / sum) : f;
    }

    /**
     * @return true, если все углы прямые
     */
    public boolean isZero() {
        return this.tlx == 0 && this.tly == 0 && this.trx == 0 && this.tRy == 0
                && this.brx == 0 && this.bry == 0 && this.blx == 0 && this.bly == 0;
    }

    /**
     * Радиусы внутреннего контура, отстоящего от внешнего на указанные расстояния.
     */
    public Radii inset(double top, double right, double bottom, double left) {
        if (isZero()) return this;
        return new Radii(this.tlx - left, this.tly - top, this.trx - right, this.tRy - top,
                this.brx - right, this.bry - bottom, this.blx - left, this.bly - bottom);
    }

    /**
     * Радиусы контура, расширенного на {@code spread} (ненулевые радиусы увеличиваются).
     */
    public Radii outset(double spread) {
        if (isZero()) return this;
        return new Radii(grow(this.tlx, spread), grow(this.tly, spread), grow(this.trx, spread), grow(this.tRy, spread),
                grow(this.brx, spread), grow(this.bry, spread), grow(this.blx, spread), grow(this.bly, spread));
    }

    private static double grow(double r, double spread) {
        return r > 0 ? Math.max(0, r + spread) : 0;
    }

    /**
     * @return контур прямоугольника со скруглёнными углами
     */
    public Shape shape(Rectangle2D r) {
        return shape(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * @return контур прямоугольника со скруглёнными углами (радиусы уменьшаются, если не помещаются)
     */
    public Shape shape(double x, double y, double w, double h) {
        if (w <= 0 || h <= 0) return new Rectangle2D.Double(x, y, Math.max(0, w), Math.max(0, h));
        if (isZero()) return new Rectangle2D.Double(x, y, w, h);

        // Радиусы не могут превышать размеры (внутренний контур может стать меньше суммы радиусов)
        Radii r = this;
        double f = 1;
        f = factor(f, w, r.tlx + r.trx);
        f = factor(f, w, r.blx + r.brx);
        f = factor(f, h, r.tly + r.bly);
        f = factor(f, h, r.tRy + r.bry);
        if (f < 1) {
            r = new Radii(r.tlx * f, r.tly * f, r.trx * f, r.tRy * f, r.brx * f, r.bry * f, r.blx * f, r.bly * f);
        }

        Path2D.Double p = new Path2D.Double();
        p.moveTo(x + r.tlx, y);
        p.lineTo(x + w - r.trx, y);
        if (r.trx > 0 || r.tRy > 0) {
            p.curveTo(x + w - r.trx + r.trx * KAPPA, y, x + w, y + r.tRy - r.tRy * KAPPA, x + w, y + r.tRy);
        }
        p.lineTo(x + w, y + h - r.bry);
        if (r.brx > 0 || r.bry > 0) {
            p.curveTo(x + w, y + h - r.bry + r.bry * KAPPA, x + w - r.brx + r.brx * KAPPA, y + h, x + w - r.brx, y + h);
        }
        p.lineTo(x + r.blx, y + h);
        if (r.blx > 0 || r.bly > 0) {
            p.curveTo(x + r.blx - r.blx * KAPPA, y + h, x, y + h - r.bly + r.bly * KAPPA, x, y + h - r.bly);
        }
        p.lineTo(x, y + r.tly);
        if (r.tlx > 0 || r.tly > 0) {
            p.curveTo(x, y + r.tly - r.tly * KAPPA, x + r.tlx - r.tlx * KAPPA, y, x + r.tlx, y);
        }
        p.closePath();
        return p;
    }
}
