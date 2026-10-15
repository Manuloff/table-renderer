package me.manuloff.lib.table.renderer.style.background;

import lombok.Getter;

import java.util.List;

/**
 * Линейный градиент. Создаётся через {@link Gradient#linear}, {@link Gradient#vertical},
 * {@link Gradient#horizontal} или {@link Gradient#repeatingLinear}.
 * <p>
 * Длина градиентной линии вычисляется как в CSS: линия проходит через центр блока под заданным углом,
 * а её концы такие, что угловые точки блока получают цвета первой и последней опорных точек.
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
public final class LinearGradient extends Gradient {

    /**
     * Угол в градусах: 0 - вверх, 90 - вправо, 180 - вниз.
     */
    private final double angle;

    LinearGradient(double angle, List<ColorStop> stops, boolean repeating) {
        super(stops, repeating);
        this.angle = angle;
    }
}
