package me.manuloff.lib.table.renderer.internal.paint.effect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Заливка фигуры с гауссовым размытием - основа теней блоков и текста.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class BlurPainter {

    private BlurPainter() {
    }

    /**
     * Заливает фигуру цветом с размытием. Радиус размытия - как в CSS (сигма гауссианы равна половине радиуса).
     *
     * @param blur радиус размытия; 0 - обычная заливка
     */
    public static void fill(Graphics2D g, Shape shape, Color color, double blur) {
        if (color.getAlpha() == 0) return;
        if (blur <= 0.01) {
            g.setColor(color);
            g.fill(shape);
            return;
        }

        double sigma = blur / 2 * DeviceArea.scaleOf(g);
        double margin = blur * 1.5;
        Rectangle2D b = shape.getBounds2D();
        Rectangle2D bounds = new Rectangle2D.Double(b.getX() - margin, b.getY() - margin,
                b.getWidth() + 2 * margin, b.getHeight() + 2 * margin);

        // Запас, чтобы размытие у края видимой области не "обрезалось"
        DeviceArea area = DeviceArea.of(g, bounds, (int) Math.ceil(sigma * 3));
        if (area == null) return;
        if (area.isTooLarge()) {
            g.setColor(color);
            g.fill(shape);
            return;
        }

        BufferedImage mask = area.createImage();
        Graphics2D mg = area.createGraphics(mask, g);
        mg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        mg.setComposite(AlphaComposite.SrcOver);
        mg.setColor(Color.WHITE);
        mg.fill(shape);
        mg.dispose();

        int[] pixels = ((DataBufferInt) mask.getRaster().getDataBuffer()).getData();
        int[] alpha = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) alpha[i] = pixels[i] >>> 24;
        Blur.gaussian(alpha, area.width, area.height, sigma);

        int rgb = color.getRGB() & 0xFFFFFF;
        int ca = color.getAlpha();
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = ((alpha[i] * ca / 255) << 24) | rgb;
        }
        area.drawBack(g, mask, null);
    }
}
