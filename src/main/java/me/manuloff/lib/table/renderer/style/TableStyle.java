package me.manuloff.lib.table.renderer.style;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.awt.*;
import java.util.List;

/**
 * Стиль таблицы (аналог CSS table/td/th стилей).
 * Поддерживает все основные CSS свойства.
 *
 * @author Manuloff
 * @since 1.0
 */
@Data
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public final class TableStyle implements Cloneable {

    /**
     * Стиль по умолчанию
     */
    public static final TableStyle DEFAULT = new TableStyle();

    // === Базовые свойства ===

    /** Цвет фона. {@code null} — без заливки. */
    private Color backgroundColor;
    /** Внутренние отступы. По умолчанию {@code new Padding(10)}. */
    private Padding padding;

    // === Ширина и высота ===

    /** Желаемая ширина (хранится в стиле; раскладку рендерер берёт из размеров Table/Row/Column/Cell). */
    private Integer width;
    /** Желаемая высота (хранится в стиле; раскладку рендерер берёт из размеров Table/Row/Column/Cell). */
    private Integer height;
    private Integer minWidth;
    private Integer maxWidth;
    private Integer minHeight;
    private Integer maxHeight;

    // === Границы ===

    /** Рамка по сторонам. По умолчанию {@code Border.DEFAULT}. */
    private Border border;
    /** Толщина рамки (хранится в стиле; рендерер рисует стороны из {@link #border}). */
    private Integer borderWidth;
    /** Цвет рамки (хранится в стиле; рендерер рисует стороны из {@link #border}). */
    private Color borderColor;

    // === Border-collapse и spacing ===

    /** Режим объединения рамок. По умолчанию {@link BorderCollapse#COLLAPSE}. */
    private BorderCollapse borderCollapse;
    /** Зазор между ячейками в режиме {@code SEPARATE}. По умолчанию {@code 0}. */
    private Integer borderSpacing;

    // === Внешний вид ===

    /** Радиус скругления углов. По умолчанию {@code 0}. */
    private Float borderRadius;

    // === Конструктор с базовыми значениями ===
    {
        this.padding = new Padding(10);
        this.border = Border.DEFAULT;
        this.borderCollapse = BorderCollapse.COLLAPSE;
        this.borderSpacing = 0;
        this.borderRadius = 0f;
    }

    @Override
    public TableStyle clone() {
        TableStyle cloned = new TableStyle();

        cloned.backgroundColor = this.backgroundColor;
        cloned.padding = this.padding;
        cloned.width = this.width;
        cloned.height = this.height;
        cloned.minWidth = this.minWidth;
        cloned.maxWidth = this.maxWidth;
        cloned.minHeight = this.minHeight;
        cloned.maxHeight = this.maxHeight;
        cloned.border = this.border;
        cloned.borderWidth = this.borderWidth;
        cloned.borderColor = this.borderColor;
        cloned.borderCollapse = this.borderCollapse;
        cloned.borderSpacing = this.borderSpacing;
        cloned.borderRadius = this.borderRadius;

        return cloned;
    }

    /**
     * Применяет значения этого стиля к целевому стилю (не-null значения перезаписывают)
     */
    public void applyTo(@NonNull TableStyle target) {
        if (this.backgroundColor != null) target.backgroundColor = this.backgroundColor;
        if (this.padding != null) target.padding = this.padding;

        if (this.width != null) target.width = this.width;
        if (this.height != null) target.height = this.height;
        if (this.minWidth != null) target.minWidth = this.minWidth;
        if (this.maxWidth != null) target.maxWidth = this.maxWidth;
        if (this.minHeight != null) target.minHeight = this.minHeight;
        if (this.maxHeight != null) target.maxHeight = this.maxHeight;

        if (this.border != null) target.border = this.border;
        if (this.borderWidth != null) target.borderWidth = this.borderWidth;
        if (this.borderColor != null) target.borderColor = this.borderColor;

        if (this.borderCollapse != null) target.borderCollapse = this.borderCollapse;
        if (this.borderSpacing != null) target.borderSpacing = this.borderSpacing;

        if (this.borderRadius != null) target.borderRadius = this.borderRadius;
    }

    // === Удобные методы ===

    /**
     * Устанавливает border сплошной линией
     */
    public TableStyle border(int width, Color color) {
        Border.BorderSide side = new Border.BorderSide(BorderStyle.SOLID, width, color);
        this.border = new Border(side, side, side, side);
        return this;
    }

    /**
     * Устанавливает border с указанным стилем
     */
    public TableStyle border(BorderStyle style, int width, Color color) {
        Border.BorderSide side = new Border.BorderSide(style, width, color);
        this.border = new Border(side, side, side, side);
        return this;
    }
}
