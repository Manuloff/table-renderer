package me.manuloff.lib.table.renderer.internal.paint.background;

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Конический градиент (в Java2D нет готового аналога).
 *
 * @author Manuloff
 * @since 2.0
 */
final class ConicPaint implements Paint {

    private static final int TABLE_SIZE = 2048;
    private static final ColorModel MODEL = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);

    private final double centerX;
    private final double centerY;
    /**
     * Начальный угол в радианах (0 - вверх, по часовой стрелке).
     */
    private final double from;
    /**
     * Цвет для доли оборота [0, 1) - ARGB.
     */
    private final int[] table;

    /**
     * @param fractions позиции цветов в долях оборота, неубывающие
     * @param repeating повторять узор с периодом от первой до последней позиции
     */
    ConicPaint(double centerX, double centerY, double fromDegrees, double[] fractions, Color[] colors, boolean repeating) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.from = Math.toRadians(fromDegrees);
        this.table = new int[TABLE_SIZE];

        double first = fractions[0];
        double period = fractions[fractions.length - 1] - first;
        for (int i = 0; i < TABLE_SIZE; i++) {
            double t = (double) i / TABLE_SIZE;
            if (repeating && period > 0) {
                t = first + (((t - first) % period) + period) % period;
            }
            this.table[i] = colorAt(t, fractions, colors);
        }
    }

    private static int colorAt(double t, double[] fractions, Color[] colors) {
        if (t <= fractions[0]) return colors[0].getRGB();
        int last = fractions.length - 1;
        if (t >= fractions[last]) return colors[last].getRGB();
        for (int i = 1; i <= last; i++) {
            if (t <= fractions[i]) {
                double span = fractions[i] - fractions[i - 1];
                double k = span <= 0 ? 1 : (t - fractions[i - 1]) / span;
                return GradientPaints.interpolate(colors[i - 1], colors[i], k);
            }
        }
        return colors[last].getRGB();
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
                                      AffineTransform xform, RenderingHints hints) {
        AffineTransform inverse;
        try {
            inverse = xform.createInverse();
        } catch (NoninvertibleTransformException e) {
            inverse = new AffineTransform();
        }
        AffineTransform toUser = inverse;
        return new PaintContext() {
            @Override
            public void dispose() {
            }

            @Override
            public ColorModel getColorModel() {
                return MODEL;
            }

            @Override
            public Raster getRaster(int x, int y, int w, int h) {
                WritableRaster raster = MODEL.createCompatibleWritableRaster(w, h);
                int[] data = new int[w * h];
                Point2D.Double p = new Point2D.Double();
                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        p.setLocation(x + i + 0.5, y + j + 0.5);
                        toUser.transform(p, p);
                        double angle = Math.atan2(p.x - ConicPaint.this.centerX, ConicPaint.this.centerY - p.y)
                                - ConicPaint.this.from;
                        double t = angle / (2 * Math.PI);
                        t -= Math.floor(t);
                        data[j * w + i] = ConicPaint.this.table[Math.min(TABLE_SIZE - 1, (int) (t * TABLE_SIZE))];
                    }
                }
                raster.setDataElements(0, 0, w, h, data);
                return raster;
            }
        };
    }

    @Override
    public int getTransparency() {
        return TRANSLUCENT;
    }
}
