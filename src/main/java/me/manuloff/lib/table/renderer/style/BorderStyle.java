package me.manuloff.lib.table.renderer.style;

/**
 * Стиль границы (аналог CSS border-style).
 *
 * @author Manuloff
 * @since 1.0
 */
public enum BorderStyle {

    NONE(0, false),
    SOLID(1, true),
    DASHED(2, true),
    DOTTED(3, true),
    DOUBLE(4, true);

    private final int type;
    private final boolean filled;

    BorderStyle(int type, boolean filled) {
        this.type = type;
        this.filled = filled;
    }

    public int getType() {
        return type;
    }

    public boolean isFilled() {
        return filled;
    }
}
