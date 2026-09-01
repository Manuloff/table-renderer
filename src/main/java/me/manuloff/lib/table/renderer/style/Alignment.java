package me.manuloff.lib.table.renderer.style;

import lombok.NonNull;

/**
 * Горизонтальное выравнивание текста (аналог CSS text-align).
 *
 * @author Manuloff
 * @since 1.0
 */
public enum Alignment {

    TOP_LEFT(TextAlign.LEFT, TextVAlign.TOP),
    TOP_CENTER(TextAlign.CENTER, TextVAlign.TOP),
    TOP_RIGHT(TextAlign.RIGHT, TextVAlign.TOP),

    CENTER_LEFT(TextAlign.LEFT, TextVAlign.CENTER),
    CENTER(TextAlign.CENTER, TextVAlign.CENTER),
    CENTER_RIGHT(TextAlign.RIGHT, TextVAlign.CENTER),

    BOTTOM_LEFT(TextAlign.LEFT, TextVAlign.BOTTOM),
    BOTTOM_CENTER(TextAlign.CENTER, TextVAlign.BOTTOM),
    BOTTOM_RIGHT(TextAlign.RIGHT, TextVAlign.BOTTOM);

    private final TextAlign horizontal;
    private final TextVAlign vertical;

    Alignment(TextAlign horizontal, TextVAlign vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @NonNull
    public TextAlign getHorizontal() {
        return horizontal;
    }

    @NonNull
    public TextVAlign getVertical() {
        return vertical;
    }

    /**
     * Только горизонтальное выравнивание (верх)
     */
    @NonNull
    public static Alignment left() {
        return TOP_LEFT;
    }

    @NonNull
    public static Alignment center() {
        return TOP_CENTER;
    }

    @NonNull
    public static Alignment right() {
        return TOP_RIGHT;
    }

    public enum TextAlign {
        LEFT, CENTER, RIGHT
    }

    public enum TextVAlign {
        TOP, CENTER, BOTTOM
    }
}
