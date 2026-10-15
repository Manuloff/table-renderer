package me.manuloff.lib.table.renderer.style.background;

import lombok.Getter;
import lombok.NonNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Градиент - слой фона (аналог CSS {@code linear-gradient}, {@code radial-gradient}, {@code conic-gradient}
 * и их {@code repeating-} вариантов).
 * <p>
 * Градиенты создаются фабричными методами этого класса:
 * <pre>{@code
 * Gradient.vertical(Color.WHITE, Color.LIGHT_GRAY)
 * Gradient.linear(135, ColorStop.of(Color.RED, 0), ColorStop.of(Color.BLUE, 0.7))
 * Gradient.radial(Color.YELLOW, Color.ORANGE)
 * Gradient.conic(Color.RED, Color.GREEN, Color.BLUE, Color.RED)
 * }</pre>
 * Позиции опорных точек ({@link ColorStop}) задаются долями длины градиента: 0 - начало, 1 - конец.
 * Прозрачные точки корректно смешиваются с соседними цветами.
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
public abstract class Gradient implements BackgroundLayer {

    /**
     * Опорные точки (не меньше двух).
     */
    private final List<ColorStop> stops;
    /**
     * Повторять ли узор за пределами первой и последней точки.
     */
    private final boolean repeating;

    Gradient(@NonNull List<ColorStop> stops, boolean repeating) {
        if (stops.size() < 2) throw new IllegalArgumentException("Gradient requires at least two color stops");
        this.stops = Collections.unmodifiableList(new ArrayList<>(stops));
        this.repeating = repeating;
    }

    // ============================================
    // Линейные
    // ============================================

    /**
     * Линейный градиент сверху вниз с равномерно распределёнными цветами.
     */
    public static LinearGradient vertical(@NonNull Color... colors) {
        return linear(180, colors);
    }

    /**
     * Линейный градиент слева направо с равномерно распределёнными цветами.
     */
    public static LinearGradient horizontal(@NonNull Color... colors) {
        return linear(90, colors);
    }

    /**
     * Линейный градиент сверху вниз с явными опорными точками.
     */
    public static LinearGradient vertical(@NonNull ColorStop... stops) {
        return linear(180, stops);
    }

    /**
     * Линейный градиент слева направо с явными опорными точками.
     */
    public static LinearGradient horizontal(@NonNull ColorStop... stops) {
        return linear(90, stops);
    }

    /**
     * Линейный градиент с равномерно распределёнными цветами.
     *
     * @param angle угол в градусах, как в CSS: 0 - вверх, 90 - вправо, 180 - вниз
     */
    public static LinearGradient linear(double angle, @NonNull Color... colors) {
        return new LinearGradient(angle, stops(colors), false);
    }

    /**
     * Линейный градиент с явными опорными точками.
     *
     * @param angle угол в градусах, как в CSS: 0 - вверх, 90 - вправо, 180 - вниз
     */
    public static LinearGradient linear(double angle, @NonNull ColorStop... stops) {
        return new LinearGradient(angle, List.of(stops), false);
    }

    /**
     * Повторяющийся линейный градиент: узор между первой и последней точкой повторяется (полосы, штриховка).
     */
    public static LinearGradient repeatingLinear(double angle, @NonNull ColorStop... stops) {
        return new LinearGradient(angle, List.of(stops), true);
    }

    // ============================================
    // Радиальные
    // ============================================

    /**
     * Эллиптический градиент из центра блока до самого дальнего угла.
     */
    public static RadialGradient radial(@NonNull Color... colors) {
        return radial(stops(colors).toArray(new ColorStop[0]));
    }

    /**
     * Эллиптический градиент из центра блока до самого дальнего угла.
     */
    public static RadialGradient radial(@NonNull ColorStop... stops) {
        return new RadialGradient(false, RadialGradient.Extent.FARTHEST_CORNER, 0, 0, 0.5, 0.5, List.of(stops), false);
    }

    /**
     * Круговой градиент с размером по ключевому слову.
     *
     * @param centerX центр по горизонтали (доля ширины: 0 - левый край, 1 - правый)
     * @param centerY центр по вертикали (доля высоты: 0 - верх, 1 - низ)
     * @param extent  до какой точки блока растягивается радиус
     */
    public static RadialGradient circle(double centerX, double centerY, @NonNull RadialGradient.Extent extent,
                                        @NonNull ColorStop... stops) {
        return new RadialGradient(true, extent, 0, 0, centerX, centerY, List.of(stops), false);
    }

    /**
     * Круговой градиент с явным радиусом.
     *
     * @param radius радиус в пикселях
     */
    public static RadialGradient circle(double centerX, double centerY, double radius, @NonNull ColorStop... stops) {
        return new RadialGradient(true, null, radius, radius, centerX, centerY, List.of(stops), false);
    }

    /**
     * Эллиптический градиент с явными радиусами.
     *
     * @param radiusX радиус по горизонтали в пикселях
     * @param radiusY радиус по вертикали в пикселях
     */
    public static RadialGradient ellipse(double centerX, double centerY, double radiusX, double radiusY,
                                         @NonNull ColorStop... stops) {
        return new RadialGradient(false, null, radiusX, radiusY, centerX, centerY, List.of(stops), false);
    }

    /**
     * Повторяющийся эллиптический градиент (концентрические кольца).
     */
    public static RadialGradient repeatingRadial(double centerX, double centerY, double radiusX, double radiusY,
                                                 @NonNull ColorStop... stops) {
        return new RadialGradient(false, null, radiusX, radiusY, centerX, centerY, List.of(stops), true);
    }

    // ============================================
    // Конические
    // ============================================

    /**
     * Конический градиент вокруг центра блока, начиная сверху по часовой стрелке.
     */
    public static ConicGradient conic(@NonNull Color... colors) {
        return new ConicGradient(0, 0.5, 0.5, stops(colors), false);
    }

    /**
     * Конический градиент.
     *
     * @param from    начальный угол в градусах (0 - вверх, по часовой стрелке)
     * @param centerX центр по горизонтали (доля ширины)
     * @param centerY центр по вертикали (доля высоты)
     * @param stops   опорные точки; позиции - доли полного оборота
     */
    public static ConicGradient conic(double from, double centerX, double centerY, @NonNull ColorStop... stops) {
        return new ConicGradient(from, centerX, centerY, List.of(stops), false);
    }

    /**
     * Повторяющийся конический градиент (лучи, "шахматка" по кругу).
     */
    public static ConicGradient repeatingConic(double from, double centerX, double centerY, @NonNull ColorStop... stops) {
        return new ConicGradient(from, centerX, centerY, List.of(stops), true);
    }

    private static List<ColorStop> stops(Color[] colors) {
        List<ColorStop> list = new ArrayList<>(colors.length);
        for (Color c : colors) list.add(ColorStop.of(c));
        return list;
    }
}
