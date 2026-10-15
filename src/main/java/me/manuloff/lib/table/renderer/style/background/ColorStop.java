package me.manuloff.lib.table.renderer.style.background;

import lombok.NonNull;
import lombok.Value;

import java.awt.Color;

/**
 * Опорная точка градиента. Позиция - доля длины градиента (0..1, допускаются значения вне диапазона);
 * null означает равномерное распределение между соседними точками.
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
public class ColorStop {

    @NonNull
    Color color;
    Double position;

    /**
     * @return точка с автоматической позицией
     */
    public static ColorStop of(Color color) {
        return new ColorStop(color, null);
    }

    /**
     * @return точка на позиции (доля длины градиента)
     */
    public static ColorStop of(Color color, double position) {
        return new ColorStop(color, position);
    }
}
