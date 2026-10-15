package me.manuloff.lib.table.renderer.internal.paint.effect;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Групповая прозрачность (аналог CSS opacity): содержимое рисуется в отдельный слой,
 * который затем накладывается с заданной непрозрачностью. В отличие от прозрачности каждой операции,
 * перекрывающиеся части группы не "просвечивают" друг через друга.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class OpacityLayer {

    private OpacityLayer() {
    }

    /**
     * @param opacity непрозрачность 0..1
     * @param bounds  область рисования в координатах графики (за её пределами содержимое обрезается)
     * @param painter рисование содержимого
     */
    public static void paint(Graphics2D g, double opacity, Rectangle2D bounds, Consumer<Graphics2D> painter) {
        if (opacity >= 1) {
            painter.accept(g);
            return;
        }
        if (opacity <= 0) return;

        AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity);
        DeviceArea area = DeviceArea.of(g, bounds, 0);
        if (area == null) return;
        if (area.isTooLarge()) {
            // Без слоя: прозрачность каждой операции
            Graphics2D direct = (Graphics2D) g.create();
            direct.setComposite(composite);
            painter.accept(direct);
            direct.dispose();
            return;
        }

        BufferedImage layer = area.createImage();
        Graphics2D lg = area.createGraphics(layer, g);
        if (g.getClip() != null) lg.clip(g.getClip());
        painter.accept(lg);
        lg.dispose();
        area.drawBack(g, layer, composite);
    }
}
