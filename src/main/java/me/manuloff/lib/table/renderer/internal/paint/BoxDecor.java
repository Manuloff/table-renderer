package me.manuloff.lib.table.renderer.internal.paint;

import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.background.BackgroundLayer;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.border.Corners;
import me.manuloff.lib.table.renderer.style.box.Shadow;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

/**
 * Оформление прямоугольного блока - всё, что нужно {@link BoxPainter}: фон, рамка, скругление, тени, контур.
 * Общий формат для ячеек ({@link Style}), таблицы ({@link TableStyle}) и строчных фрагментов.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class BoxDecor {

    final Color background;
    final List<BackgroundLayer> layers;
    final BorderSide top;
    final BorderSide right;
    final BorderSide bottom;
    final BorderSide left;
    final Corners radius;
    final List<Shadow> shadows;
    final BorderSide outline;
    final double outlineOffset;

    private BoxDecor(Color background, List<BackgroundLayer> layers, BorderSide top, BorderSide right, BorderSide bottom,
                     BorderSide left, Corners radius, List<Shadow> shadows, BorderSide outline, double outlineOffset) {
        this.background = background;
        this.layers = layers == null ? Collections.emptyList() : layers;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        this.radius = radius == null ? Corners.ZERO : radius;
        this.shadows = shadows == null ? Collections.emptyList() : shadows;
        this.outline = outline;
        this.outlineOffset = outlineOffset;
    }

    /**
     * @param s вычисленный стиль (все свойства оформления заданы)
     */
    public static BoxDecor of(Style s) {
        return new BoxDecor(s.background(), s.backgroundLayers(), s.borderTop(), s.borderRight(), s.borderBottom(),
                s.borderLeft(), s.borderRadius(), s.boxShadow(), s.outline(), s.outlineOffset());
    }

    /**
     * @param s стиль таблицы
     */
    public static BoxDecor of(TableStyle s) {
        return new BoxDecor(s.background(), s.backgroundLayers(), s.borderTop(), s.borderRight(), s.borderBottom(),
                s.borderLeft(), s.borderRadius(), s.boxShadow(), s.outline(), s.outlineOffset());
    }

    /**
     * @return то же оформление без рамок и скругления (ячейки в модели схлопывающихся рамок:
     * рамки рисуются отдельно, скругление не применяется)
     */
    public BoxDecor withoutBorders() {
        return new BoxDecor(this.background, this.layers, BorderSide.NONE, BorderSide.NONE, BorderSide.NONE,
                BorderSide.NONE, Corners.ZERO, this.shadows, this.outline, this.outlineOffset);
    }

    /**
     * @return оформление части строчного фрагмента: у "разрезанных" переносом сторон нет рамки и скругления
     */
    public BoxDecor slice(boolean hasStart, boolean hasEnd) {
        if (hasStart && hasEnd) return this;
        Corners r = new Corners(hasStart ? this.radius.getTopLeft() : 0, hasEnd ? this.radius.getTopRight() : 0,
                hasEnd ? this.radius.getBottomRight() : 0, hasStart ? this.radius.getBottomLeft() : 0);
        return new BoxDecor(this.background, this.layers, this.top, hasEnd ? this.right : BorderSide.NONE, this.bottom,
                hasStart ? this.left : BorderSide.NONE, r, this.shadows, this.outline, this.outlineOffset);
    }

    boolean hasOutline() {
        return this.outline != null && this.outline.isVisible();
    }

    double topWidth() {
        return this.top.effectiveWidth();
    }

    double rightWidth() {
        return this.right.effectiveWidth();
    }

    double bottomWidth() {
        return this.bottom.effectiveWidth();
    }

    double leftWidth() {
        return this.left.effectiveWidth();
    }
}
