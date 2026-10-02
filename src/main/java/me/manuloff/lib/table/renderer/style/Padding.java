package me.manuloff.lib.table.renderer.style;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Отступы (аналог CSS padding).
 * Immutable - все методы возвращают новый экземпляр.
 *
 * @author Manuloff
 * @since 1.0
 */
@Data
@Accessors(fluent = true)
public final class Padding {

    public static final Padding DEFAULT = new Padding(1, 1, 1, 1);
    public static final Padding ZERO = new Padding(0, 0, 0, 0);

    private final int top;
    private final int bottom;
    private final int left;
    private final int right;

    /**
     * Создаёт отступы для каждой стороны.
     *
     * @throws IllegalArgumentException если любое значение отрицательное
     */
    public Padding(int top, int bottom, int left, int right) {
        validatePadding(top, "Top");
        validatePadding(bottom, "Bottom");
        validatePadding(left, "Left");
        validatePadding(right, "Right");

        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    /**
     * Вертикальные и горизонтальные отступы
     */
    public Padding(int vertical, int horizontal) {
        this(vertical, vertical, horizontal, horizontal);
    }

    /**
     * Одинаковые отступы со всех сторон
     */
    public Padding(int padding) {
        this(padding, padding, padding, padding);
    }

    private static void validatePadding(int value, String paddingName) {
        if (value < 0) {
            throw new IllegalArgumentException(paddingName + " padding can't be negative. Provided: " + value);
        }
    }

    /**
     * @return копия Padding
     */
    public Padding copy() {
        return new Padding(top, bottom, left, right);
    }

    /**
     * @return сумма top и bottom
     */
    public int verticalSum() {
        return top + bottom;
    }

    /**
     * @return сумма left и right
     */
    public int horizontalSum() {
        return left + right;
    }
}
