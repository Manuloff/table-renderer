package me.manuloff.lib.table.renderer.style.border;

import lombok.Value;
import lombok.With;

/**
 * Радиусы скругления углов (аналог CSS border-radius).
 * Если сумма радиусов на стороне больше её длины, все радиусы пропорционально уменьшаются.
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
@With
public class Corners {

    /**
     * Без скругления.
     */
    public static final Corners ZERO = new Corners(0, 0, 0, 0);

    double topLeft;
    double topRight;
    double bottomRight;
    double bottomLeft;

    /**
     * Радиусы углов по часовой стрелке, начиная с левого верхнего (не отрицательные).
     */
    public Corners(double topLeft, double topRight, double bottomRight, double bottomLeft) {
        if (topLeft < 0 || topRight < 0 || bottomRight < 0 || bottomLeft < 0) {
            throw new IllegalArgumentException("Corner radius can't be negative");
        }
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
    }

    /**
     * @return одинаковое скругление всех углов
     */
    public static Corners of(double radius) {
        return new Corners(radius, radius, radius, radius);
    }

    /**
     * @return скругление каждого угла (по часовой стрелке от левого верхнего)
     */
    public static Corners of(double topLeft, double topRight, double bottomRight, double bottomLeft) {
        return new Corners(topLeft, topRight, bottomRight, bottomLeft);
    }

    /**
     * @return скругление только верхних углов
     */
    public static Corners top(double radius) {
        return new Corners(radius, radius, 0, 0);
    }

    /**
     * @return скругление только нижних углов
     */
    public static Corners bottom(double radius) {
        return new Corners(0, 0, radius, radius);
    }

    /**
     * @return true, если все углы прямые
     */
    public boolean isZero() {
        return this.topLeft == 0 && this.topRight == 0 && this.bottomRight == 0 && this.bottomLeft == 0;
    }
}
