package me.manuloff.lib.table.renderer.style;

import lombok.NonNull;

/**
 * Выравнивание текста в ячейке по горизонтали и вертикали (девять позиций).
 *
 * @author Manuloff
 * @since 1.0
 */
public enum Alignment {

    /** Слева сверху. */
    TOP_LEFT(TextAlign.LEFT, TextVAlign.TOP),
    /** По центру сверху. */
    TOP_CENTER(TextAlign.CENTER, TextVAlign.TOP),
    /** Справа сверху. */
    TOP_RIGHT(TextAlign.RIGHT, TextVAlign.TOP),

    /** Слева по вертикальному центру. */
    CENTER_LEFT(TextAlign.LEFT, TextVAlign.CENTER),
    /** По центру ячейки. */
    CENTER(TextAlign.CENTER, TextVAlign.CENTER),
    /** Справа по вертикальному центру. */
    CENTER_RIGHT(TextAlign.RIGHT, TextVAlign.CENTER),

    /** Слева снизу. */
    BOTTOM_LEFT(TextAlign.LEFT, TextVAlign.BOTTOM),
    /** По центру снизу. */
    BOTTOM_CENTER(TextAlign.CENTER, TextVAlign.BOTTOM),
    /** Справа снизу. */
    BOTTOM_RIGHT(TextAlign.RIGHT, TextVAlign.BOTTOM);

    private final TextAlign horizontal;
    private final TextVAlign vertical;

    Alignment(TextAlign horizontal, TextVAlign vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    /**
     * @return горизонтальная составляющая выравнивания
     */
    @NonNull
    public TextAlign getHorizontal() {
        return horizontal;
    }

    /**
     * @return вертикальная составляющая выравнивания
     */
    @NonNull
    public TextVAlign getVertical() {
        return vertical;
    }

    /**
     * Горизонталь слева, вертикаль сверху ({@link #TOP_LEFT}).
     */
    @NonNull
    public static Alignment left() {
        return TOP_LEFT;
    }

    /**
     * Горизонталь по центру, вертикаль сверху ({@link #TOP_CENTER}).
     * Не путать с {@link #CENTER}.
     */
    @NonNull
    public static Alignment center() {
        return TOP_CENTER;
    }

    /**
     * Горизонталь справа, вертикаль сверху ({@link #TOP_RIGHT}).
     */
    @NonNull
    public static Alignment right() {
        return TOP_RIGHT;
    }

    /**
     * Горизонтальное выравнивание.
     */
    public enum TextAlign {
        LEFT, CENTER, RIGHT
    }

    /**
     * Вертикальное выравнивание.
     */
    public enum TextVAlign {
        TOP, CENTER, BOTTOM
    }
}
