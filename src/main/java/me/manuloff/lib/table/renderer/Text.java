package me.manuloff.lib.table.renderer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.TextStyle;

import java.util.function.Consumer;

/**
 * Текстовый фрагмент в ячейке с собственным стилем.
 *
 * @author Manuloff
 * @since 1.0
 */
@Data
public final class Text {

    /**
     * Маркер новой строки
     */
    public static final Text NEW_LINE = new Text("\n");

    @NonNull
    private final String text;

    @NonNull
    @Getter(AccessLevel.NONE)
    private final TextStyle textStyle = new TextStyle();

    /**
     * Создаёт текстовый фрагмент с указанной строкой.
     *
     * @param text содержимое фрагмента
     */
    public Text(String text) {
        this.text = text;
    }

    /**
     * Настраивает стиль этого фрагмента и возвращает его.
     */
    @NonNull
    public Text textStyle(Consumer<TextStyle> consumer) {
        consumer.accept(this.textStyle);
        return this;
    }

    /**
     * @return стиль этого текстового фрагмента
     */
    @NonNull
    public TextStyle textStyle() {
        return this.textStyle;
    }

    /**
     * Помечает текст как жирный
     */
    @NonNull
    public Text bold() {
        this.textStyle.bold(true);
        return this;
    }

    /**
     * Помечает текст как курсив
     */
    @NonNull
    public Text italic() {
        this.textStyle.italic(true);
        return this;
    }

    /**
     * Устанавливает цвет текста
     */
    @NonNull
    public Text color(java.awt.Color color) {
        this.textStyle.textColor(color);
        return this;
    }

    /**
     * Устанавливает размер шрифта
     */
    @NonNull
    public Text size(int size) {
        this.textStyle.fontSize(size);
        return this;
    }

    /**
     * Устанавливает имя шрифта
     */
    @NonNull
    public Text font(String fontName) {
        this.textStyle.fontName(fontName);
        return this;
    }
}
