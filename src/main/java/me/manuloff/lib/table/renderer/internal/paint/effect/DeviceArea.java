package me.manuloff.lib.table.renderer.internal.paint.effect;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Прямоугольник в пикселях устройства для промежуточного растра: область рисования в координатах
 * графики, переведённая через её трансформацию и обрезанная её текущей областью отсечения.
 *
 * @author Manuloff
 * @since 2.0
 */
final class DeviceArea {

    /**
     * Ограничение размера промежуточного растра (в пикселях); больше - эффект упрощается.
     */
    private static final long MAX_PIXELS = 64L * 1024 * 1024;

    final int x;
    final int y;
    final int width;
    final int height;

    private DeviceArea(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * @param bounds область в координатах графики
     * @param pad    запас в пикселях устройства с каждой стороны
     * @return область или null, если она пуста
     */
    static DeviceArea of(Graphics2D g, Rectangle2D bounds, int pad) {
        AffineTransform transform = g.getTransform();
        Rectangle2D device = transform.createTransformedShape(bounds).getBounds2D();
        Shape clip = g.getClip();
        if (clip != null) {
            Rectangle2D.intersect(device, transform.createTransformedShape(clip).getBounds2D(), device);
        }
        int x = (int) Math.floor(device.getX());
        int y = (int) Math.floor(device.getY());
        int w = (int) Math.ceil(device.getMaxX()) - x;
        int h = (int) Math.ceil(device.getMaxY()) - y;
        if (w <= 0 || h <= 0) return null;
        return new DeviceArea(x - pad, y - pad, w + 2 * pad, h + 2 * pad);
    }

    boolean isTooLarge() {
        return (long) this.width * this.height > MAX_PIXELS;
    }

    /**
     * @return прозрачный растр размера области
     */
    BufferedImage createImage() {
        return new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Графика растра с той же системой координат, что у {@code g}.
     */
    Graphics2D createGraphics(BufferedImage image, Graphics2D g) {
        Graphics2D ig = image.createGraphics();
        ig.setRenderingHints(g.getRenderingHints());
        ig.translate(-this.x, -this.y);
        ig.transform(g.getTransform());
        return ig;
    }

    /**
     * Рисует растр области обратно на графику (в пикселях устройства, с её областью отсечения).
     */
    void drawBack(Graphics2D g, BufferedImage image, Composite composite) {
        Graphics2D out = (Graphics2D) g.create();
        out.setTransform(new AffineTransform());
        if (composite != null) out.setComposite(composite);
        out.drawImage(image, this.x, this.y, null);
        out.dispose();
    }

    /**
     * @return масштаб координат графики в пиксели устройства
     */
    static double scaleOf(Graphics2D g) {
        return Math.sqrt(Math.abs(g.getTransform().getDeterminant()));
    }
}
