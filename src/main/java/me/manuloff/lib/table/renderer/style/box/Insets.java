package me.manuloff.lib.table.renderer.style.box;

import lombok.Value;

/**
 * Отступы с четырёх сторон (padding / margin).
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
public class Insets {

    /**
     * Нулевые отступы.
     */
    public static final Insets ZERO = new Insets(0, 0, 0, 0);

    double top;
    double right;
    double bottom;
    double left;

    /**
     * @return одинаковые отступы со всех сторон
     */
    public static Insets of(double all) {
        return new Insets(all, all, all, all);
    }

    /**
     * @return отступы: сверху и снизу - vertical, слева и справа - horizontal
     */
    public static Insets of(double vertical, double horizontal) {
        return new Insets(vertical, horizontal, vertical, horizontal);
    }

    /**
     * @return отступы каждой стороны
     */
    public static Insets of(double top, double right, double bottom, double left) {
        return new Insets(top, right, bottom, left);
    }

    /**
     * @return сумма левого и правого отступов
     */
    public double horizontal() {
        return this.left + this.right;
    }

    /**
     * @return сумма верхнего и нижнего отступов
     */
    public double vertical() {
        return this.top + this.bottom;
    }
}
