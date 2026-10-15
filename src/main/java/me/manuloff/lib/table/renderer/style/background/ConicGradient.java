package me.manuloff.lib.table.renderer.style.background;

import lombok.Getter;

import java.util.List;

/**
 * Конический градиент: цвет меняется по углу вокруг центра. Создаётся через {@link Gradient#conic}
 * или {@link Gradient#repeatingConic}. Позиции опорных точек - доли полного оборота.
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
public final class ConicGradient extends Gradient {

    /**
     * Начальный угол в градусах (0 - вверх, по часовой стрелке).
     */
    private final double from;
    /**
     * Центр по горизонтали - доля ширины блока.
     */
    private final double centerX;
    /**
     * Центр по вертикали - доля высоты блока.
     */
    private final double centerY;

    ConicGradient(double from, double centerX, double centerY, List<ColorStop> stops, boolean repeating) {
        super(stops, repeating);
        this.from = from;
        this.centerX = centerX;
        this.centerY = centerY;
    }
}
