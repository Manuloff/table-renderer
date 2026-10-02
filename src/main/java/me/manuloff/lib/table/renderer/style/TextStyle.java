package me.manuloff.lib.table.renderer.style;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import java.awt.*;

/**
 * Стиль текста (аналог CSS text/fon styles).
 *
 * @author Manuloff
 * @since 1.0
 */
@Data
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public final class TextStyle implements Cloneable {

    /**
     * Стиль по умолчанию
     */
    public static final TextStyle DEFAULT = new TextStyle();

    // === Шрифт ===

    /** Имя семейства шрифта. По умолчанию {@code Arial}. */
    private String fontName;
    /** Базовый {@link Font} style, если не заданы флаги bold/italic. По умолчанию {@link Font#PLAIN}. */
    private Integer fontStyle;
    /** Размер шрифта в пунктах. По умолчанию {@code 12}. */
    private Integer fontSize;

    // === Текст ===

    /** Цвет текста. По умолчанию чёрный. */
    private Color textColor;
    /** Выравнивание блока текста в ячейке. По умолчанию {@link Alignment#TOP_LEFT}. */
    private Alignment textAlignment;
    /** Множитель межстрочного интервала. По умолчанию {@code 1.0}. */
    private Float lineHeight;

    // === Вертикальное выравнивание ===

    /** Отдельное вертикальное выравнивание. По умолчанию {@code TOP}. */
    private VerticalAlign verticalAlign;

    // === Перенос текста ===

    /** Перенос по словам. По умолчанию {@code false}. */
    private Boolean wordWrap;
    /** Флаг переполнения (хранится в стиле). По умолчанию {@code false}. */
    private Boolean overflow;
    /** Режим обрезки переполнения. По умолчанию {@code "ellipsis"}. */
    private String textOverflow;

    // === Дополнительные эффекты ===

    /** Жирное начертание. По умолчанию {@code false}. */
    private Boolean bold;
    /** Курсив. По умолчанию {@code false}. */
    private Boolean italic;
    /** Подчёркивание. По умолчанию {@code false}. */
    private Boolean underline;
    /** Зачёркивание. По умолчанию {@code false}. */
    private Boolean strikethrough;

    // === Конструктор с базовыми значениями ===
    {
        this.fontName = "Arial";
        this.fontStyle = Font.PLAIN;
        this.fontSize = 12;
        this.textColor = Color.BLACK;
        this.textAlignment = Alignment.TOP_LEFT;
        this.lineHeight = 1.0f;
        this.verticalAlign = VerticalAlign.TOP;
        this.wordWrap = false;
        this.overflow = false;
        this.textOverflow = "ellipsis";
        this.bold = false;
        this.italic = false;
        this.underline = false;
        this.strikethrough = false;
    }

    /**
     * Конструктор с основными параметрами
     */
    public TextStyle(String fontName, Integer fontStyle, Integer fontSize,
                     Color textColor, Alignment textAlignment, Float lineHeight) {
        this();
        this.fontName = fontName;
        this.fontStyle = fontStyle;
        this.fontSize = fontSize;
        this.textColor = textColor;
        this.textAlignment = textAlignment;
        this.lineHeight = lineHeight;
    }

    @Override
    public TextStyle clone() {
        TextStyle cloned = new TextStyle();

        cloned.fontName = this.fontName;
        cloned.fontStyle = this.fontStyle;
        cloned.fontSize = this.fontSize;

        cloned.textColor = this.textColor;
        cloned.textAlignment = this.textAlignment;
        cloned.lineHeight = this.lineHeight;

        cloned.verticalAlign = this.verticalAlign;

        cloned.wordWrap = this.wordWrap;
        cloned.overflow = this.overflow;
        cloned.textOverflow = this.textOverflow;

        cloned.bold = this.bold;
        cloned.italic = this.italic;
        cloned.underline = this.underline;
        cloned.strikethrough = this.strikethrough;

        return cloned;
    }

    /**
     * Применяет значения этого стиля к целевому стилю (не-null значения перезаписывают)
     */
    public void applyTo(TextStyle target) {
        if (target == null) {
            throw new IllegalArgumentException("Target style cannot be null");
        }

        if (this.fontName != null) target.fontName = this.fontName;
        if (this.fontStyle != null) target.fontStyle = this.fontStyle;
        if (this.fontSize != null) target.fontSize = this.fontSize;

        if (this.textColor != null) target.textColor = this.textColor;
        if (this.textAlignment != null) target.textAlignment = this.textAlignment;
        if (this.lineHeight != null) target.lineHeight = this.lineHeight;

        if (this.verticalAlign != null) target.verticalAlign = this.verticalAlign;

        if (this.wordWrap != null) target.wordWrap = this.wordWrap;
        if (this.overflow != null) target.overflow = this.overflow;
        if (this.textOverflow != null) target.textOverflow = this.textOverflow;

        if (this.bold != null) target.bold = this.bold;
        if (this.italic != null) target.italic = this.italic;
        if (this.underline != null) target.underline = this.underline;
        if (this.strikethrough != null) target.strikethrough = this.strikethrough;
    }

    // === Удобные методы ===

    /**
     * Устанавливает fontStyle в BOLD
     */
    public TextStyle bold() {
        this.bold = true;
        return this;
    }

    /**
     * Устанавливает fontStyle в ITALIC
     */
    public TextStyle italic() {
        this.italic = true;
        return this;
    }

    /**
     * Устанавливает fontStyle в BOLD_ITALIC
     */
    public TextStyle boldItalic() {
        this.bold = true;
        this.italic = true;
        return this;
    }

    /**
     * Отключает перенос текста (текст будет выходить за границы)
     */
    public TextStyle noWrap() {
        this.wordWrap = false;
        return this;
    }

    /**
     * Включает перенос текста (по словам)
     */
    public TextStyle wrap() {
        this.wordWrap = true;
        return this;
    }

    /**
     * @return реальный fontStyle с учётом bold/italic флагов
     */
    public int getEffectiveFontStyle() {
        int style = (fontStyle != null) ? fontStyle : Font.PLAIN;

        if (Boolean.TRUE.equals(bold) && Boolean.TRUE.equals(italic)) {
            return Font.BOLD | Font.ITALIC;
        }
        if (Boolean.TRUE.equals(bold)) {
            return Font.BOLD;
        }
        if (Boolean.TRUE.equals(italic)) {
            return Font.ITALIC;
        }
        return style;
    }
}
