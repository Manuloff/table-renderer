package me.manuloff.lib.table.renderer.internal.text;

import me.manuloff.lib.table.renderer.internal.paint.BoxDecor;
import me.manuloff.lib.table.renderer.internal.paint.BoxPainter;
import me.manuloff.lib.table.renderer.internal.paint.Images;
import me.manuloff.lib.table.renderer.internal.paint.Radii;
import me.manuloff.lib.table.renderer.internal.paint.effect.BlurPainter;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.box.Shadow;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Отрисовка разложенного абзаца. Порядок для каждой строки, как в браузерах:
 * оформление фрагментов, изображения, тени текста, подчёркивание и надчёркивание, текст и обводка, зачёркивание.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class TextPainter {

    private final TextMeasurer measurer;
    private final DecorationPainter decorations = new DecorationPainter();

    /**
     * @param measurer измеритель, которым раскладывался текст
     */
    public TextPainter(TextMeasurer measurer) {
        this.measurer = measurer;
    }

    /**
     * @param x левый край области содержимого
     * @param y верх первой строки
     */
    public void paint(Graphics2D g, TextBlock block, double x, double y) {
        for (TextLine line : block.lines) {
            double baseline = y + line.top + line.baseline;
            double ox = x + line.offset;

            for (BoxFragment f : line.boxes) paintBox(g, f, ox, baseline);
            for (ImageFragment f : line.images) paintImage(g, f, ox, baseline);
            for (TextFragment f : line.texts) paintShadows(g, f, ox, baseline);
            for (TextFragment f : line.texts) this.decorations.paintOver(g, f, ox, baseline);
            for (TextFragment f : line.texts) paintText(g, f, ox, baseline);
            for (TextFragment f : line.texts) this.decorations.paintThrough(g, f, ox, baseline);
        }
    }

    /**
     * Включает прозрачность фрагмента.
     *
     * @return прежний режим наложения - его нужно восстановить
     */
    static Composite applyOpacity(Graphics2D g, double opacity) {
        Composite old = g.getComposite();
        if (opacity < 1) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity));
        return old;
    }

    // ============================================
    // Оформление и изображения
    // ============================================

    private void paintBox(Graphics2D g, BoxFragment f, double ox, double baseline) {
        InlineBox box = f.box;
        if (box.hidden || !box.paintsBox()) return;
        Style s = box.style;
        // Высота фона фрагмента - по шрифту, плюс вертикальные отступы и рамка (на раскладку не влияют)
        double bb = baseline - box.shift;
        double top = bb - box.metrics.ascent - s.paddingTop() - s.borderTop().effectiveWidth();
        double bottom = bb + box.metrics.descent + s.paddingBottom() + s.borderBottom().effectiveWidth();
        Rectangle2D r = new Rectangle2D.Double(ox + f.left, top, f.right - f.left, bottom - top);
        BoxDecor decor = BoxDecor.of(s).slice(f.hasStart, f.hasEnd);

        Composite old = applyOpacity(g, box.opacity);
        BoxPainter.paint(g, r, decor);
        BoxPainter.paintOutline(g, r, decor);
        g.setComposite(old);
    }

    private void paintImage(Graphics2D g, ImageFragment f, double ox, double baseline) {
        InlineBox box = f.box;
        if (box.hidden) return;
        Style s = box.style;
        double h = box.imageOuterHeight();
        Rectangle2D r = new Rectangle2D.Double(ox + f.x, baseline - box.shift - h, box.imageOuterWidth(), h);
        BoxDecor decor = BoxDecor.of(s);

        Composite old = applyOpacity(g, box.opacity);
        BoxPainter.paint(g, r, decor);

        double l = s.borderLeft().effectiveWidth() + s.paddingLeft();
        double t = s.borderTop().effectiveWidth() + s.paddingTop();
        double rt = s.borderRight().effectiveWidth() + s.paddingRight();
        double b = s.borderBottom().effectiveWidth() + s.paddingBottom();
        Rectangle2D content = new Rectangle2D.Double(r.getX() + l, r.getY() + t, r.getWidth() - l - rt, r.getHeight() - t - b);
        if (content.getWidth() > 0 && content.getHeight() > 0) {
            BufferedImage img = box.image.getImage();
            double[] size = Images.fit(box.image.getFit(), img.getWidth(), img.getHeight(),
                    content.getWidth(), content.getHeight());
            Radii radii = Radii.of(s.borderRadius(), r.getWidth(), r.getHeight()).inset(t, rt, b, l);
            Graphics2D ig = (Graphics2D) g.create();
            ig.clip(radii.shape(content));
            Images.draw(ig, img, content.getCenterX() - size[0] / 2, content.getCenterY() - size[1] / 2,
                    size[0], size[1], true);
            ig.dispose();
        }
        BoxPainter.paintOutline(g, r, decor);
        g.setComposite(old);
    }

    // ============================================
    // Текст
    // ============================================

    private void paintShadows(Graphics2D g, TextFragment f, double ox, double baseline) {
        InlineBox box = f.box;
        List<Shadow> shadows = box.style.textShadow();
        if (box.hidden || shadows.isEmpty()) return;
        Shape outline = this.measurer.outline(f.font, f.text, ox + f.x, baseline - box.shift, f.letterSpacing, f.wordSpacing);
        Composite old = applyOpacity(g, box.opacity);
        // Первая тень - верхняя, поэтому рисуем с конца
        for (int i = shadows.size() - 1; i >= 0; i--) {
            Shadow s = shadows.get(i);
            Shape shifted = AffineTransform.getTranslateInstance(s.getOffsetX(), s.getOffsetY()).createTransformedShape(outline);
            BlurPainter.fill(g, shifted, s.getColor(), s.getBlur());
        }
        g.setComposite(old);
    }

    private void paintText(Graphics2D g, TextFragment f, double ox, double baseline) {
        InlineBox box = f.box;
        if (box.hidden) return;
        Style s = box.style;
        double x = ox + f.x;
        double y = baseline - box.shift;

        Composite old = applyOpacity(g, box.opacity);
        g.setColor(s.color());
        this.measurer.draw(g, f.font, f.text, x, y, f.letterSpacing, f.wordSpacing);

        double stroke = s.textStrokeWidth();
        if (stroke > 0) {
            Shape outline = this.measurer.outline(f.font, f.text, x, y, f.letterSpacing, f.wordSpacing);
            g.setColor(s.textStrokeColor() != null ? s.textStrokeColor() : s.color());
            g.setStroke(new BasicStroke((float) stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(outline);
            g.setStroke(new BasicStroke());
        }
        g.setComposite(old);
    }
}
