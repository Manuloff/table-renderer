package me.manuloff.lib.table.renderer.style;

/**
 * Стиль границы (аналог CSS border-style).
 *
 * @author Manuloff
 * @since 1.0
 */
public enum BorderStyle {

    /** Без линии. */
    NONE(0, false),
    /** Сплошная линия. */
    SOLID(1, true),
    /** Штриховая линия. */
    DASHED(2, true),
    /** Пунктир (точки). */
    DOTTED(3, true),
    /** Двойная линия. */
    DOUBLE(4, true);

    private final int type;
    private final boolean filled;

    BorderStyle(int type, boolean filled) {
        this.type = type;
        this.filled = filled;
    }

    /**
     * @return числовой идентификатор стиля
     */
    public int getType() {
        return type;
    }

    /**
     * @return {@code true}, если стиль предполагает видимую линию
     */
    public boolean isFilled() {
        return filled;
    }
}
