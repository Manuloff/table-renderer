package me.manuloff.lib.table.renderer.style.text;

import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.awt.Color;

/**
 * Линии оформления текста (аналог CSS text-decoration).
 * Оформление распространяется на вложенные фрагменты текста и складывается с их собственным оформлением.
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
@With
public class TextDecoration {

    /**
     * Без оформления.
     */
    public static final TextDecoration NONE = new TextDecoration(false, false, false, TextDecorationStyle.SOLID, null, null, null);

    boolean underline;
    boolean overline;
    boolean lineThrough;
    @NonNull
    TextDecorationStyle style;
    /**
     * Цвет линий; null - цвет текста.
     */
    Color color;
    /**
     * Толщина линий; null - из метрик шрифта.
     */
    Double thickness;
    /**
     * Смещение подчёркивания от базовой линии; null - из метрик шрифта.
     */
    Double underlineOffset;

    /**
     * @return сплошное подчёркивание
     */
    public static TextDecoration underline() {
        return NONE.withUnderline(true);
    }

    /**
     * @return сплошное надчёркивание
     */
    public static TextDecoration overline() {
        return NONE.withOverline(true);
    }

    /**
     * @return сплошное зачёркивание
     */
    public static TextDecoration lineThrough() {
        return NONE.withLineThrough(true);
    }

    /**
     * @return true, если ни одной линии нет
     */
    public boolean isNone() {
        return !this.underline && !this.overline && !this.lineThrough;
    }
}
