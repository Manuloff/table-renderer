package me.manuloff.lib.table.renderer.internal.text;

import me.manuloff.lib.table.renderer.content.InlineImage;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.text.BaselineShift;
import me.manuloff.lib.table.renderer.style.text.TextDecoration;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Строчный блок абзаца: корень (ячейка или заголовок), стилизованный фрагмент или изображение.
 * Хранит вычисленный стиль и всё, что из него следует для раскладки: шрифты, метрики, высоту строки,
 * подъём базовой линии, ширину краёв и линии оформления.
 *
 * @author Manuloff
 * @since 2.0
 */
final class InlineBox {

    /**
     * Подъём верхнего индекса и опускание нижнего - доли размера шрифта родителя.
     */
    private static final double SUPER_SHIFT = 0.36;
    private static final double SUB_SHIFT = 0.2;

    /**
     * Линия оформления вместе с блоком, который её задал: положение и толщина линии
     * считаются по шрифту этого блока, как в CSS.
     */
    static final class Decoration {
        final TextDecoration decoration;
        final Color color;
        final InlineBox origin;

        Decoration(TextDecoration decoration, Color color, InlineBox origin) {
            this.decoration = decoration;
            this.color = color;
            this.origin = origin;
        }
    }

    final int index;
    final InlineBox parent;
    final Style style;
    /**
     * Изображение (для блока-изображения) или null.
     */
    final InlineImage image;

    final List<Font> fontChain;
    final Font font;
    final TextMetrics metrics;
    final double lineHeight;
    /**
     * Подъём базовой линии блока над базовой линией абзаца.
     */
    final double shift;
    /**
     * Ширина левого и правого краёв фрагмента (отступ + рамка).
     */
    final double startEdge;
    final double endEdge;
    /**
     * Линии оформления: собственные и унаследованные от предков.
     */
    final List<Decoration> decorations;
    /**
     * Итоговая прозрачность с учётом предков.
     */
    final double opacity;
    final boolean hidden;

    /**
     * Диапазон символов абзаца, принадлежащих блоку: [start, end).
     */
    int start;
    int end;

    InlineBox(int index, InlineBox parent, Style style, InlineImage image, FontResolver fonts) {
        this.index = index;
        this.parent = parent;
        this.style = style;
        this.image = image;
        this.fontChain = fonts.chain(style);
        this.font = this.fontChain.get(0);
        this.metrics = fonts.metrics(this.font);
        this.lineHeight = style.lineHeight() == null
                ? this.metrics.normalLineHeight()
                : style.lineHeight() * style.fontSize();
        this.hidden = Boolean.TRUE.equals(style.hidden());

        if (parent == null) {
            this.shift = 0;
            this.startEdge = 0;
            this.endEdge = 0;
            this.opacity = 1;
            this.decorations = ownDecorations(Collections.emptyList());
            return;
        }

        this.shift = parent.shift + ownShift(parent.style.fontSize());
        // Края изображения входят в его ширину
        this.startEdge = image != null ? 0 : style.paddingLeft() + style.borderLeft().effectiveWidth();
        this.endEdge = image != null ? 0 : style.paddingRight() + style.borderRight().effectiveWidth();
        this.opacity = parent.opacity * style.opacity();
        this.decorations = ownDecorations(parent.decorations);
    }

    private double ownShift(double parentFontSize) {
        double shift = this.style.baselineOffset();
        if (this.style.baselineShift() == BaselineShift.SUPER) shift += parentFontSize * SUPER_SHIFT;
        if (this.style.baselineShift() == BaselineShift.SUB) shift -= parentFontSize * SUB_SHIFT;
        return shift;
    }

    private List<Decoration> ownDecorations(List<Decoration> inherited) {
        TextDecoration own = this.style.textDecoration();
        if (own == null || own.isNone()) return inherited;
        List<Decoration> list = new ArrayList<>(inherited);
        list.add(new Decoration(own, own.getColor() != null ? own.getColor() : this.style.color(), this));
        return list;
    }

    boolean isRoot() {
        return this.parent == null;
    }

    /**
     * @return true, если у фрагмента есть оформление (фон, рамка, тень, контур)
     */
    boolean paintsBox() {
        Style s = this.style;
        return s.background() != null || !s.backgroundLayers().isEmpty() || !s.boxShadow().isEmpty()
                || s.borderTop().isVisible() || s.borderRight().isVisible()
                || s.borderBottom().isVisible() || s.borderLeft().isVisible()
                || (s.outline() != null && s.outline().isVisible());
    }

    // === Изображения ===

    double imageOuterWidth() {
        Style s = this.style;
        return this.image.contentWidth() + s.paddingLeft() + s.paddingRight()
                + s.borderLeft().effectiveWidth() + s.borderRight().effectiveWidth();
    }

    double imageOuterHeight() {
        Style s = this.style;
        return this.image.contentHeight() + s.paddingTop() + s.paddingBottom()
                + s.borderTop().effectiveWidth() + s.borderBottom().effectiveWidth();
    }
}
