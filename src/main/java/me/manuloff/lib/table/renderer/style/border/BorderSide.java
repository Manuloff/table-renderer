package me.manuloff.lib.table.renderer.style.border;

import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.awt.Color;

/**
 * Одна сторона рамки: толщина, стиль и цвет (аналог CSS border-top и т.д.).
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
@With
public class BorderSide {

    /**
     * Нет рамки.
     */
    public static final BorderSide NONE = new BorderSide(0, BorderStyle.NONE, Color.BLACK);
    /**
     * Скрытая рамка: в модели {@link me.manuloff.lib.table.renderer.style.table.BorderCollapse#COLLAPSE} подавляет рамки соседних ячеек.
     */
    public static final BorderSide HIDDEN = new BorderSide(0, BorderStyle.HIDDEN, Color.BLACK);

    double width;
    @NonNull
    BorderStyle style;
    @NonNull
    Color color;

    /**
     * @param width толщина в пикселях (не отрицательная)
     */
    public BorderSide(double width, @NonNull BorderStyle style, @NonNull Color color) {
        if (width < 0) throw new IllegalArgumentException("Border width can't be negative: " + width);
        this.width = width;
        this.style = style;
        this.color = color;
    }

    /**
     * @return рамка указанного стиля
     */
    public static BorderSide of(double width, BorderStyle style, Color color) {
        return new BorderSide(width, style, color);
    }

    /**
     * @return сплошная рамка
     */
    public static BorderSide solid(double width, Color color) {
        return new BorderSide(width, BorderStyle.SOLID, color);
    }

    /**
     * @return штриховая рамка
     */
    public static BorderSide dashed(double width, Color color) {
        return new BorderSide(width, BorderStyle.DASHED, color);
    }

    /**
     * @return точечная рамка
     */
    public static BorderSide dotted(double width, Color color) {
        return new BorderSide(width, BorderStyle.DOTTED, color);
    }

    /**
     * @return двойная рамка (две линии по трети толщины)
     */
    public static BorderSide doubled(double width, Color color) {
        return new BorderSide(width, BorderStyle.DOUBLE, color);
    }

    /**
     * @return true, если рамка рисуется (есть стиль, толщина и непрозрачный цвет)
     */
    public boolean isVisible() {
        return this.style != BorderStyle.NONE && this.style != BorderStyle.HIDDEN && this.width > 0
                && this.color.getAlpha() > 0;
    }

    /**
     * @return толщина, занимаемая рамкой при раскладке (0 для NONE и HIDDEN)
     */
    public double effectiveWidth() {
        return this.style == BorderStyle.NONE || this.style == BorderStyle.HIDDEN ? 0 : this.width;
    }
}
