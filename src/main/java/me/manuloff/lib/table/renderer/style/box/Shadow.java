package me.manuloff.lib.table.renderer.style.box;

import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.awt.Color;

/**
 * Тень блока или текста (аналог CSS box-shadow / text-shadow).
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
@With
public class Shadow {

    /**
     * Смещение по горизонтали.
     */
    double offsetX;
    /**
     * Смещение по вертикали.
     */
    double offsetY;
    /**
     * Радиус размытия (не отрицательный).
     */
    double blur;
    /**
     * Расширение тени (для тени текста не используется).
     */
    double spread;
    @NonNull
    Color color;
    /**
     * Внутренняя тень (для тени текста не используется).
     */
    boolean inset;

    /**
     * @param blur радиус размытия (не отрицательный)
     */
    public Shadow(double offsetX, double offsetY, double blur, double spread, @NonNull Color color, boolean inset) {
        if (blur < 0) throw new IllegalArgumentException("Shadow blur can't be negative: " + blur);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.blur = blur;
        this.spread = spread;
        this.color = color;
        this.inset = inset;
    }

    /**
     * @return внешняя тень
     */
    public static Shadow of(double offsetX, double offsetY, double blur, Color color) {
        return new Shadow(offsetX, offsetY, blur, 0, color, false);
    }

    /**
     * @return внешняя тень с расширением (отрицательное - сужение)
     */
    public static Shadow of(double offsetX, double offsetY, double blur, double spread, Color color) {
        return new Shadow(offsetX, offsetY, blur, spread, color, false);
    }

    /**
     * @return внутренняя тень
     */
    public static Shadow inset(double offsetX, double offsetY, double blur, double spread, Color color) {
        return new Shadow(offsetX, offsetY, blur, spread, color, true);
    }
}
