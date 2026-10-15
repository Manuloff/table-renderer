package me.manuloff.lib.table.renderer.style.box;

import lombok.Value;

/**
 * Размер в пикселях или процентах.
 * Проценты ширины ячейки/колонки считаются от ширины таблицы, ширины таблицы - от
 * {@code RenderOptions#availableWidth}.
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
public class Size {

    double value;
    boolean percent;

    /**
     * @return размер в пикселях
     */
    public static Size px(double value) {
        if (value < 0) throw new IllegalArgumentException("Size can't be negative: " + value);
        return new Size(value, false);
    }

    /**
     * @return размер в процентах
     */
    public static Size percent(double value) {
        if (value < 0) throw new IllegalArgumentException("Size can't be negative: " + value);
        return new Size(value, true);
    }

    /**
     * @param base база для процентов; если неизвестна (NaN), процентный размер даёт NaN
     */
    public double resolve(double base) {
        return this.percent ? this.value * base / 100 : this.value;
    }

    @Override
    public String toString() {
        return this.value + (this.percent ? "%" : "px");
    }
}
