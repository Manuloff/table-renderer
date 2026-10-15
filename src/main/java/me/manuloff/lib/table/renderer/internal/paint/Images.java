package me.manuloff.lib.table.renderer.internal.paint;

import me.manuloff.lib.table.renderer.style.background.ObjectFit;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Масштабирование и отрисовка растровых изображений.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Images {

    private Images() {
    }

    /**
     * Рисует изображение в прямоугольник.
     *
     * @param smooth сглаживание при масштабировании (false - по ближайшему пикселю)
     */
    public static void draw(Graphics2D g, BufferedImage img, double x, double y, double w, double h, boolean smooth) {
        Object old = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, smooth
                ? RenderingHints.VALUE_INTERPOLATION_BICUBIC : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        AffineTransform t = new AffineTransform();
        t.translate(x, y);
        t.scale(w / img.getWidth(), h / img.getHeight());
        g.drawImage(img, t, null);
        if (old != null) g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, old);
    }

    /**
     * @return размер {ширина, высота} изображения {@code iw x ih}, вписанного в {@code areaW x areaH}
     */
    public static double[] fit(ObjectFit fit, double iw, double ih, double areaW, double areaH) {
        switch (fit) {
            case FILL:
                return new double[]{areaW, areaH};
            case CONTAIN: {
                double k = Math.min(areaW / iw, areaH / ih);
                return new double[]{iw * k, ih * k};
            }
            case COVER: {
                double k = Math.max(areaW / iw, areaH / ih);
                return new double[]{iw * k, ih * k};
            }
            case SCALE_DOWN: {
                double k = Math.min(1, Math.min(areaW / iw, areaH / ih));
                return new double[]{iw * k, ih * k};
            }
            default:
                return new double[]{iw, ih};
        }
    }
}
