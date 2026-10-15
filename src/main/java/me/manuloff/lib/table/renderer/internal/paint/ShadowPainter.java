package me.manuloff.lib.table.renderer.internal.paint;

import me.manuloff.lib.table.renderer.internal.paint.effect.BlurPainter;
import me.manuloff.lib.table.renderer.style.box.Shadow;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Тени блока (аналог CSS box-shadow): внешние видны только снаружи блока, внутренние - внутри рамки.
 * Первая тень в списке - верхняя, поэтому тени рисуются с конца.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class ShadowPainter {

    private ShadowPainter() {
    }

    /**
     * @param r     прямоугольник блока по внешнему краю рамки
     * @param radii скругление блока
     */
    public static void paintOuter(Graphics2D g, Rectangle2D r, Radii radii, List<Shadow> shadows) {
        if (shadows.isEmpty()) return;
        Shape element = radii.shape(r);
        for (int i = shadows.size() - 1; i >= 0; i--) {
            Shadow s = shadows.get(i);
            if (s.isInset()) continue;
            double spread = s.getSpread();
            double w = r.getWidth() + 2 * spread;
            double h = r.getHeight() + 2 * spread;
            if (w <= 0 || h <= 0) continue;
            Shape shadow = radii.outset(spread).shape(r.getX() - spread + s.getOffsetX(),
                    r.getY() - spread + s.getOffsetY(), w, h);

            // Область "всё, кроме блока" с запасом на размытие
            Rectangle2D bounds = shadow.getBounds2D().createUnion(r);
            double m = s.getBlur() * 2 + 2;
            Area outside = new Area(new Rectangle2D.Double(bounds.getX() - m, bounds.getY() - m,
                    bounds.getWidth() + 2 * m, bounds.getHeight() + 2 * m));
            outside.subtract(new Area(element));

            Graphics2D sg = (Graphics2D) g.create();
            sg.clip(outside);
            BlurPainter.fill(sg, shadow, s.getColor(), s.getBlur());
            sg.dispose();
        }
    }

    /**
     * @param padding контур области под рамкой
     * @param pb      прямоугольник области под рамкой
     * @param pr      скругление области под рамкой
     */
    public static void paintInset(Graphics2D g, Shape padding, Rectangle2D pb, Radii pr, List<Shadow> shadows) {
        for (int i = shadows.size() - 1; i >= 0; i--) {
            Shadow s = shadows.get(i);
            if (!s.isInset()) continue;
            double spread = s.getSpread();
            double m = s.getBlur() * 2 + Math.abs(s.getOffsetX()) + Math.abs(s.getOffsetY()) + Math.abs(spread) + 2;

            // Тень - всё вокруг "окна", уменьшенного на spread и сдвинутого на смещение тени
            Area region = new Area(new Rectangle2D.Double(pb.getX() - m, pb.getY() - m,
                    pb.getWidth() + 2 * m, pb.getHeight() + 2 * m));
            double hw = pb.getWidth() - 2 * spread;
            double hh = pb.getHeight() - 2 * spread;
            if (hw > 0 && hh > 0) {
                region.subtract(new Area(pr.outset(-spread).shape(pb.getX() + spread + s.getOffsetX(),
                        pb.getY() + spread + s.getOffsetY(), hw, hh)));
            }
            Graphics2D sg = (Graphics2D) g.create();
            sg.clip(padding);
            BlurPainter.fill(sg, region, s.getColor(), s.getBlur());
            sg.dispose();
        }
    }

    /**
     * @return насколько внешние тени выходят за пределы блока: {сверху, справа, снизу, слева}
     */
    static double[] overflow(List<Shadow> shadows) {
        double top = 0, right = 0, bottom = 0, left = 0;
        for (Shadow s : shadows) {
            if (s.isInset()) continue;
            double e = s.getSpread() + s.getBlur() * 1.5;
            top = Math.max(top, e - s.getOffsetY());
            bottom = Math.max(bottom, e + s.getOffsetY());
            left = Math.max(left, e - s.getOffsetX());
            right = Math.max(right, e + s.getOffsetX());
        }
        return new double[]{top, right, bottom, left};
    }
}
