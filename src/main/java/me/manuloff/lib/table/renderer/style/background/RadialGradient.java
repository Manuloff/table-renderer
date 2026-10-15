package me.manuloff.lib.table.renderer.style.background;

import lombok.Getter;

import java.util.List;

/**
 * Радиальный (круговой или эллиптический) градиент. Создаётся через {@link Gradient#radial},
 * {@link Gradient#circle}, {@link Gradient#ellipse} или {@link Gradient#repeatingRadial}.
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
public final class RadialGradient extends Gradient {

    /**
     * До какой точки блока растягивается радиус (аналог ключевых слов размера в CSS).
     */
    public enum Extent {
        /**
         * До ближайшей стороны.
         */
        CLOSEST_SIDE,
        /**
         * До ближайшего угла.
         */
        CLOSEST_CORNER,
        /**
         * До самой дальней стороны.
         */
        FARTHEST_SIDE,
        /**
         * До самого дальнего угла (по умолчанию).
         */
        FARTHEST_CORNER
    }

    /**
     * Круг (true) или эллипс с пропорциями блока (false).
     */
    private final boolean circle;
    /**
     * Ключевой размер или null, если радиусы заданы явно.
     */
    private final Extent extent;
    /**
     * Явный радиус по горизонтали в пикселях (если {@link #extent} равен null).
     */
    private final double radiusX;
    /**
     * Явный радиус по вертикали в пикселях (если {@link #extent} равен null).
     */
    private final double radiusY;
    /**
     * Центр по горизонтали - доля ширины блока.
     */
    private final double centerX;
    /**
     * Центр по вертикали - доля высоты блока.
     */
    private final double centerY;

    RadialGradient(boolean circle, Extent extent, double radiusX, double radiusY, double centerX, double centerY,
                   List<ColorStop> stops, boolean repeating) {
        super(stops, repeating);
        if (radiusX < 0 || radiusY < 0) throw new IllegalArgumentException("Gradient radius can't be negative");
        this.circle = circle;
        this.extent = extent;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.centerX = centerX;
        this.centerY = centerY;
    }
}
