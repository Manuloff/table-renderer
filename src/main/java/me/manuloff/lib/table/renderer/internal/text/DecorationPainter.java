package me.manuloff.lib.table.renderer.internal.text;

import me.manuloff.lib.table.renderer.internal.paint.border.BorderStrokes;
import me.manuloff.lib.table.renderer.style.border.BorderStyle;
import me.manuloff.lib.table.renderer.style.text.TextDecoration;
import me.manuloff.lib.table.renderer.style.text.TextDecorationStyle;

import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Линии оформления текста: подчёркивание, надчёркивание, зачёркивание.
 * Положение и толщина линии берутся из шрифта блока, который задал оформление.
 *
 * @author Manuloff
 * @since 2.0
 */
final class DecorationPainter {

    private enum Line {
        UNDERLINE, OVERLINE, LINE_THROUGH
    }

    /**
     * Подчёркивание и надчёркивание - рисуются до текста.
     */
    void paintOver(Graphics2D g, TextFragment f, double ox, double baseline) {
        paint(g, f, ox, baseline, false);
    }

    /**
     * Зачёркивание - рисуется поверх текста.
     */
    void paintThrough(Graphics2D g, TextFragment f, double ox, double baseline) {
        paint(g, f, ox, baseline, true);
    }

    private void paint(Graphics2D g, TextFragment f, double ox, double baseline, boolean through) {
        InlineBox box = f.box;
        if (box.hidden || box.decorations.isEmpty() || f.width <= 0) return;
        Composite old = TextPainter.applyOpacity(g, box.opacity);
        for (InlineBox.Decoration d : box.decorations) {
            TextDecoration deco = d.decoration;
            if (through) {
                if (deco.isLineThrough()) paintLine(g, f, ox, baseline, d, Line.LINE_THROUGH);
            } else {
                if (deco.isUnderline()) paintLine(g, f, ox, baseline, d, Line.UNDERLINE);
                if (deco.isOverline()) paintLine(g, f, ox, baseline, d, Line.OVERLINE);
            }
        }
        g.setComposite(old);
    }

    private void paintLine(Graphics2D g, TextFragment f, double ox, double baseline, InlineBox.Decoration d, Line kind) {
        TextMetrics m = d.origin.metrics;
        TextDecoration deco = d.decoration;
        double originBaseline = baseline - d.origin.shift;
        double t;
        double y;
        switch (kind) {
            case UNDERLINE:
                t = deco.getThickness() != null ? deco.getThickness() : m.underlineThickness;
                y = originBaseline + (deco.getUnderlineOffset() != null ? deco.getUnderlineOffset() : m.underlineOffset);
                break;
            case OVERLINE:
                t = deco.getThickness() != null ? deco.getThickness() : m.underlineThickness;
                y = originBaseline - m.ascent;
                break;
            default:
                t = deco.getThickness() != null ? deco.getThickness() : m.strikethroughThickness;
                y = originBaseline + m.strikethroughOffset - t / 2;
                break;
        }

        double x1 = ox + f.x;
        double x2 = x1 + f.width;
        g.setColor(d.color);
        switch (deco.getStyle()) {
            case DOUBLE: {
                double direction = kind == Line.OVERLINE ? -1 : 1;
                g.fill(new Rectangle2D.Double(x1, y, x2 - x1, t));
                g.fill(new Rectangle2D.Double(x1, y + direction * t * 2, x2 - x1, t));
                break;
            }
            case DOTTED:
            case DASHED: {
                BorderStyle style = deco.getStyle() == TextDecorationStyle.DOTTED ? BorderStyle.DOTTED : BorderStyle.DASHED;
                g.setStroke(BorderStrokes.dashed(style, t));
                g.draw(new Line2D.Double(x1, y + t / 2, x2, y + t / 2));
                g.setStroke(new BasicStroke());
                break;
            }
            case WAVY:
                g.setStroke(new BasicStroke((float) t, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(wave(x1, x2, y + t / 2, t));
                g.setStroke(new BasicStroke());
                break;
            default:
                g.fill(new Rectangle2D.Double(x1, y, x2 - x1, t));
                break;
        }
    }

    /**
     * Синусоида для волнистой линии.
     */
    private static Path2D wave(double x1, double x2, double y, double thickness) {
        double amplitude = Math.max(thickness, 1);
        double length = amplitude * 5;
        double center = y + amplitude / 2;
        Path2D.Double path = new Path2D.Double();
        path.moveTo(x1, center);
        for (double x = x1; x <= x2; x += 0.5) {
            path.lineTo(x, center - amplitude * Math.sin((x - x1) / length * 2 * Math.PI));
        }
        return path;
    }
}
