package me.manuloff.lib.table.renderer.internal.paint.background;

import me.manuloff.lib.table.renderer.style.background.ColorStop;
import me.manuloff.lib.table.renderer.style.background.ConicGradient;
import me.manuloff.lib.table.renderer.style.background.Gradient;
import me.manuloff.lib.table.renderer.style.background.LinearGradient;
import me.manuloff.lib.table.renderer.style.background.RadialGradient;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Преобразование градиентов стиля в {@link Paint} Java2D по правилам CSS.
 *
 * @author Manuloff
 * @since 2.0
 */
final class GradientPaints {

    private static final double EPSILON = 1e-5;

    private GradientPaints() {
    }

    /**
     * @param box область позиционирования градиента
     */
    static Paint of(Gradient gradient, Rectangle2D box) {
        List<Double> positionList = new ArrayList<>();
        List<Color> colorList = new ArrayList<>();
        prepareStops(gradient.getStops(), positions(gradient.getStops()), positionList, colorList);
        double[] positions = new double[positionList.size()];
        for (int i = 0; i < positions.length; i++) positions[i] = positionList.get(i);
        Color[] colors = colorList.toArray(new Color[0]);

        if (gradient instanceof LinearGradient) {
            return linear((LinearGradient) gradient, box, positions, colors);
        }
        if (gradient instanceof RadialGradient) {
            return radial((RadialGradient) gradient, box, positions, colors);
        }
        ConicGradient conic = (ConicGradient) gradient;
        return new ConicPaint(box.getX() + box.getWidth() * conic.getCenterX(), box.getY() + box.getHeight() * conic.getCenterY(),
                conic.getFrom(), positions, colors, conic.isRepeating());
    }

    /**
     * Готовит точки для Java2D, которая смешивает цвета без предумножения альфы: прозрачная точка
     * получает RGB соседей (между двумя разными цветами - раздваивается), иначе переход идёт через
     * "грязный" чёрный.
     */
    private static void prepareStops(List<ColorStop> stops, double[] positions, List<Double> outPositions,
                                     List<Color> outColors) {
        int n = stops.size();
        for (int i = 0; i < n; i++) {
            Color c = stops.get(i).getColor();
            if (c.getAlpha() != 0) {
                outPositions.add(positions[i]);
                outColors.add(c);
                continue;
            }
            Color before = i > 0 ? stops.get(i - 1).getColor() : null;
            Color after = i + 1 < n ? stops.get(i + 1).getColor() : null;
            if (before != null && before.getAlpha() == 0) before = null;
            if (after != null && after.getAlpha() == 0) after = null;
            if (before != null) {
                outPositions.add(positions[i]);
                outColors.add(transparent(before));
            }
            if (after != null && (before == null || (before.getRGB() & 0xFFFFFF) != (after.getRGB() & 0xFFFFFF))) {
                outPositions.add(positions[i]);
                outColors.add(transparent(after));
            }
            if (before == null && after == null) {
                outPositions.add(positions[i]);
                outColors.add(c);
            }
        }
    }

    private static Color transparent(Color c) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), 0);
    }

    /**
     * Нормализация позиций по CSS: первая - 0, последняя - 1, неубывание, равномерное распределение пропусков.
     */
    static double[] positions(List<ColorStop> stops) {
        int n = stops.size();
        double[] p = new double[n];
        boolean[] known = new boolean[n];
        for (int i = 0; i < n; i++) {
            Double pos = stops.get(i).getPosition();
            if (pos != null) {
                p[i] = pos;
                known[i] = true;
            }
        }
        if (!known[0]) {
            p[0] = 0;
            known[0] = true;
        }
        if (!known[n - 1]) {
            p[n - 1] = Math.max(1, maxKnown(p, known));
            known[n - 1] = true;
        }
        double max = p[0];
        for (int i = 0; i < n; i++) {
            if (known[i]) {
                p[i] = Math.max(p[i], max);
                max = p[i];
            }
        }
        int i = 0;
        while (i < n) {
            if (known[i]) {
                i++;
                continue;
            }
            int j = i;
            while (!known[j]) j++;
            double a = p[i - 1];
            double b = p[j];
            int count = j - i + 1;
            for (int k = i; k < j; k++) p[k] = a + (b - a) * (k - i + 1) / count;
            i = j;
        }
        return p;
    }

    private static double maxKnown(double[] p, boolean[] known) {
        double m = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < p.length; i++) {
            if (known[i]) m = Math.max(m, p[i]);
        }
        return m;
    }

    /**
     * Приводит позиции к строго возрастающим долям в [0, 1].
     */
    private static float[] fractions(double[] p, double lo, double hi) {
        float[] f = new float[p.length];
        double span = hi - lo;
        double prev = -1;
        for (int i = 0; i < p.length; i++) {
            double v = span <= 0 ? 0 : (p[i] - lo) / span;
            v = Math.max(v, prev + EPSILON);
            f[i] = (float) v;
            prev = v;
        }
        double last = f[f.length - 1];
        if (last > 1) {
            for (int i = 0; i < f.length; i++) f[i] = (float) (f[i] / last);
            // Сжатие могло нарушить строгое возрастание при очень близких точках
            for (int i = 1; i < f.length; i++) {
                if (f[i] <= f[i - 1]) f[i] = Math.nextUp(f[i - 1]);
            }
            f[f.length - 1] = Math.min(1f, f[f.length - 1]);
        }
        return f;
    }

    private static Paint linear(LinearGradient g, Rectangle2D box, double[] p, Color[] colors) {
        double w = box.getWidth();
        double h = box.getHeight();
        double angle = Math.toRadians(g.getAngle());
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double length = Math.abs(w * sin) + Math.abs(h * cos);
        if (length <= 0) length = 1;
        double cx = box.getCenterX();
        double cy = box.getCenterY();
        double dx = sin;
        double dy = -cos;
        double sx = cx - dx * length / 2;
        double sy = cy - dy * length / 2;

        double lo;
        double hi;
        CycleMethod cycle;
        if (g.isRepeating()) {
            lo = p[0];
            hi = p[p.length - 1];
            cycle = CycleMethod.REPEAT;
            if (hi - lo <= EPSILON) return colors[colors.length - 1];
        } else {
            lo = Math.min(0, p[0]);
            hi = Math.max(1, p[p.length - 1]);
            cycle = CycleMethod.NO_CYCLE;
        }
        Point2D start = new Point2D.Double(sx + dx * length * lo, sy + dy * length * lo);
        Point2D end = new Point2D.Double(sx + dx * length * hi, sy + dy * length * hi);
        if (start.distance(end) < EPSILON) end = new Point2D.Double(start.getX() + EPSILON, start.getY());
        return new LinearGradientPaint(start, end, fractions(p, lo, hi), colors, cycle, ColorSpaceType.SRGB,
                new AffineTransform());
    }

    private static Paint radial(RadialGradient g, Rectangle2D box, double[] p, Color[] colors) {
        double w = box.getWidth();
        double h = box.getHeight();
        double cx = w * g.getCenterX();
        double cy = h * g.getCenterY();

        double rx;
        double ry;
        if (g.getExtent() == null) {
            rx = g.getRadiusX();
            ry = g.isCircle() ? rx : g.getRadiusY();
        } else {
            double left = Math.abs(cx);
            double right = Math.abs(w - cx);
            double top = Math.abs(cy);
            double bottom = Math.abs(h - cy);
            switch (g.getExtent()) {
                case CLOSEST_SIDE:
                    rx = Math.min(left, right);
                    ry = Math.min(top, bottom);
                    if (g.isCircle()) rx = ry = Math.min(rx, ry);
                    break;
                case FARTHEST_SIDE:
                    rx = Math.max(left, right);
                    ry = Math.max(top, bottom);
                    if (g.isCircle()) rx = ry = Math.max(rx, ry);
                    break;
                case CLOSEST_CORNER: {
                    double sx = Math.min(left, right);
                    double sy = Math.min(top, bottom);
                    if (g.isCircle()) {
                        rx = ry = Math.hypot(sx, sy);
                    } else {
                        rx = sx * Math.sqrt(2);
                        ry = sy * Math.sqrt(2);
                    }
                    break;
                }
                default: {
                    double sx = Math.max(left, right);
                    double sy = Math.max(top, bottom);
                    if (g.isCircle()) {
                        rx = ry = Math.hypot(sx, sy);
                    } else {
                        rx = sx * Math.sqrt(2);
                        ry = sy * Math.sqrt(2);
                    }
                    break;
                }
            }
        }
        rx = Math.max(rx, EPSILON);
        ry = Math.max(ry, EPSILON);

        double hi;
        CycleMethod cycle;
        if (g.isRepeating()) {
            hi = p[p.length - 1];
            cycle = CycleMethod.REPEAT;
            if (hi <= EPSILON) return colors[colors.length - 1];
        } else {
            hi = Math.max(1, p[p.length - 1]);
            cycle = CycleMethod.NO_CYCLE;
        }
        double[] clamped = p.clone();
        for (int i = 0; i < clamped.length; i++) clamped[i] = Math.max(0, clamped[i]);

        AffineTransform t = new AffineTransform();
        t.translate(box.getX() + cx, box.getY() + cy);
        t.scale(1, ry / rx);
        return new RadialGradientPaint(new Point2D.Double(0, 0), (float) (rx * hi), new Point2D.Double(0, 0),
                fractions(clamped, 0, hi), colors, cycle, ColorSpaceType.SRGB, t);
    }

    static int interpolate(Color a, Color b, double k) {
        int alpha = (int) Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * k);
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * k);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * k);
        int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * k);
        return (alpha << 24) | (r << 16) | (g << 8) | bl;
    }
}
