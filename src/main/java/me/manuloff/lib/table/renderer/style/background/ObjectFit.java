package me.manuloff.lib.table.renderer.style.background;

/**
 * Вписывание изображения в заданный размер (аналог CSS object-fit / background-size).
 *
 * @author Manuloff
 * @since 2.0
 */
public enum ObjectFit {
    /**
     * Растянуть, игнорируя пропорции.
     */
    FILL,
    /**
     * Вписать целиком с сохранением пропорций.
     */
    CONTAIN,
    /**
     * Заполнить целиком с сохранением пропорций, обрезая лишнее.
     */
    COVER,
    /**
     * Исходный размер.
     */
    NONE,
    /**
     * Как NONE или CONTAIN - что меньше.
     */
    SCALE_DOWN
}
