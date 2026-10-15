package me.manuloff.lib.table.renderer.internal.paint.border;

import me.manuloff.lib.table.renderer.style.border.BorderStyle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

/**
 * Общие для рамок кисти и оттенки: штрихи и точки, затемнённые и осветлённые цвета объёмных стилей.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class BorderStrokes {

    private BorderStrokes() {
    }

    /**
     * @return штриховая ({@link BorderStyle#DASHED}) или точечная ({@link BorderStyle#DOTTED}) линия
     */
    public static Stroke dashed(BorderStyle style, double width) {
        float w = (float) width;
        if (style == BorderStyle.DOTTED) {
            // Круглые точки диаметром в толщину линии с шагом в две толщины
            return new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, new float[]{0, w * 2}, 0);
        }
        float dash = Math.max(w * 3, 3);
        float gap = Math.max(w * 2, 2);
        return new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{dash, gap}, 0);
    }

    /**
     * @return затемнённый цвет для "теневой" части объёмных рамок
     */
    public static Color darker(Color c) {
        return mix(c, Color.BLACK, 0.4);
    }

    /**
     * @return осветлённый цвет для "освещённой" части объёмных рамок
     */
    public static Color lighter(Color c) {
        return mix(c, Color.WHITE, 0.45);
    }

    private static Color mix(Color c, Color with, double k) {
        return new Color(
                (int) Math.round(c.getRed() + (with.getRed() - c.getRed()) * k),
                (int) Math.round(c.getGreen() + (with.getGreen() - c.getGreen()) * k),
                (int) Math.round(c.getBlue() + (with.getBlue() - c.getBlue()) * k),
                c.getAlpha());
    }
}
