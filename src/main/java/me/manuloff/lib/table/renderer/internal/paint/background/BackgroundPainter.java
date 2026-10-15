package me.manuloff.lib.table.renderer.internal.paint.background;

import me.manuloff.lib.table.renderer.internal.paint.Images;
import me.manuloff.lib.table.renderer.style.background.BackgroundLayer;
import me.manuloff.lib.table.renderer.style.background.Gradient;
import me.manuloff.lib.table.renderer.style.background.ImageLayer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Отрисовка фона: цвет и слои (градиенты, изображения) - как CSS background.
 * Слои рисуются снизу вверх: последний в списке - нижний.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class BackgroundPainter {

    /**
     * Защита от зацикливания при очень мелких плитках.
     */
    private static final int MAX_TILES = 100_000;

    private BackgroundPainter() {
    }

    /**
     * @param clip   область отрисовки (обычно контур по внешнему краю рамки)
     * @param origin область позиционирования слоёв (обычно область под рамкой)
     */
    public static void paint(Graphics2D g, Color color, List<BackgroundLayer> layers, Shape clip, Rectangle2D origin) {
        boolean hasColor = color != null && color.getAlpha() > 0;
        boolean hasLayers = layers != null && !layers.isEmpty();
        if (!hasColor && !hasLayers) return;

        Graphics2D bg = (Graphics2D) g.create();
        try {
            if (hasColor) {
                bg.setColor(color);
                bg.fill(clip);
            }
            if (!hasLayers) return;
            for (int i = layers.size() - 1; i >= 0; i--) {
                BackgroundLayer layer = layers.get(i);
                if (layer instanceof Gradient) {
                    if (origin.getWidth() <= 0 || origin.getHeight() <= 0) continue;
                    bg.setPaint(GradientPaints.of((Gradient) layer, origin));
                    bg.fill(clip);
                } else if (layer instanceof ImageLayer) {
                    Graphics2D ig = (Graphics2D) bg.create();
                    ig.clip(clip);
                    paintImage(ig, (ImageLayer) layer, clip.getBounds2D(), origin);
                    ig.dispose();
                }
            }
        } finally {
            bg.dispose();
        }
    }

    /**
     * Рисует изображение-слой: размер плитки, положение, повторение по осям.
     */
    private static void paintImage(Graphics2D g, ImageLayer layer, Rectangle2D area, Rectangle2D origin) {
        BufferedImage img = layer.getImage();
        double iw = img.getWidth();
        double ih = img.getHeight();
        if (iw <= 0 || ih <= 0) return;

        double[] size = tileSize(layer, iw, ih, origin.getWidth(), origin.getHeight());
        double tw = size[0];
        double th = size[1];
        if (tw <= 0 || th <= 0) return;

        double x0 = origin.getX() + (origin.getWidth() - tw) * layer.getAlignX();
        double y0 = origin.getY() + (origin.getHeight() - th) * layer.getAlignY();

        double startX = x0;
        double endX = x0 + tw;
        if (layer.isRepeatX()) {
            startX = x0 - Math.ceil((x0 - area.getMinX()) / tw) * tw;
            endX = area.getMaxX();
        }
        double startY = y0;
        double endY = y0 + th;
        if (layer.isRepeatY()) {
            startY = y0 - Math.ceil((y0 - area.getMinY()) / th) * th;
            endY = area.getMaxY();
        }

        int tiles = 0;
        for (double y = startY; y < endY - 1e-9 && tiles < MAX_TILES; y += th) {
            for (double x = startX; x < endX - 1e-9 && tiles < MAX_TILES; x += tw) {
                Images.draw(g, img, x, y, tw, th, layer.isSmooth());
                tiles++;
            }
        }
    }

    /**
     * @return размер плитки: явный (с сохранением пропорций по второй стороне) или по способу вписывания
     */
    private static double[] tileSize(ImageLayer layer, double iw, double ih, double areaW, double areaH) {
        Double w = layer.getWidth();
        Double h = layer.getHeight();
        if (w != null || h != null) {
            double tw = w != null ? w : h * iw / ih;
            double th = h != null ? h : w * ih / iw;
            return new double[]{tw, th};
        }
        return Images.fit(layer.getFit(), iw, ih, areaW, areaH);
    }
}
