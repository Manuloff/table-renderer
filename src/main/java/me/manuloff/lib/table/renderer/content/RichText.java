package me.manuloff.lib.table.renderer.content;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.Style;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Форматированный текст: последовательность фрагментов текста, стилизованных фрагментов, переносов и изображений.
 *
 * <pre>{@code
 * cell.content(c -> c
 *         .text("Итого: ")
 *         .text("1 250 ₽", s -> s.bold().color(Color.RED))
 *         .br()
 *         .image(icon, 16, 16));
 * }</pre>
 *
 * @author Manuloff
 * @since 2.0
 */
public class RichText {

    private final List<Inline> children = new ArrayList<>();

    /**
     * @return форматированный текст из одной строки
     */
    public static RichText of(String text) {
        return new RichText().text(text);
    }

    // === Добавление ===

    /**
     * Добавляет текст. Символы {@code \n} становятся переносами строк, если стиль это допускает
     * (по умолчанию - всегда, см. {@link me.manuloff.lib.table.renderer.style.text.WhiteSpace}).
     */
    public RichText text(@NonNull String text) {
        if (!text.isEmpty()) this.children.add(new TextNode(text));
        return this;
    }

    /**
     * Добавляет текст с собственным стилем.
     */
    public RichText text(@NonNull String text, @NonNull Consumer<Style> style) {
        Span span = new Span();
        style.accept(span.style());
        span.text(text);
        this.children.add(span);
        return this;
    }

    /**
     * Добавляет жирный текст.
     */
    public RichText bold(@NonNull String text) {
        return text(text, Style::bold);
    }

    /**
     * Добавляет текст курсивом.
     */
    public RichText italic(@NonNull String text) {
        return text(text, Style::italic);
    }

    /**
     * Добавляет вложенный стилизованный фрагмент с произвольным содержимым.
     */
    public RichText span(@NonNull Consumer<Style> style, @NonNull Consumer<RichText> content) {
        Span span = new Span();
        style.accept(span.style());
        content.accept(span);
        this.children.add(span);
        return this;
    }

    /**
     * Добавляет готовый строчный узел.
     */
    public RichText append(@NonNull Inline inline) {
        this.children.add(inline);
        return this;
    }

    /**
     * Добавляет всё содержимое другого текста.
     */
    public RichText append(@NonNull RichText other) {
        if (other == this) throw new IllegalArgumentException("Can't append text to itself");
        this.children.addAll(other.children);
        return this;
    }

    /**
     * Принудительный перенос строки.
     */
    public RichText br() {
        this.children.add(LineBreak.INSTANCE);
        return this;
    }

    /**
     * Добавляет изображение в исходном размере.
     */
    public RichText image(@NonNull BufferedImage image) {
        this.children.add(new InlineImage(image, null, null));
        return this;
    }

    /**
     * Добавляет изображение заданного размера.
     */
    public RichText image(@NonNull BufferedImage image, double width, double height) {
        this.children.add(new InlineImage(image, width, height));
        return this;
    }

    /**
     * Добавляет изображение заданного размера с оформлением (рамка, скругление, тень, подъём над базовой линией).
     */
    public RichText image(@NonNull BufferedImage image, double width, double height, @NonNull Consumer<Style> style) {
        InlineImage img = new InlineImage(image, width, height);
        style.accept(img.getStyle());
        this.children.add(img);
        return this;
    }

    /**
     * Удаляет всё содержимое.
     */
    public RichText clear() {
        this.children.clear();
        return this;
    }

    // === Чтение ===

    /**
     * @return узлы верхнего уровня
     */
    public List<Inline> children() {
        return Collections.unmodifiableList(this.children);
    }

    /**
     * @return true, если узлов нет
     */
    public boolean isEmpty() {
        return this.children.isEmpty();
    }

    /**
     * @return текст без форматирования; переносы - {@code \n}, изображения пропускаются
     */
    public String plainText() {
        StringBuilder sb = new StringBuilder();
        appendPlain(this, sb);
        return sb.toString();
    }

    private static void appendPlain(RichText text, StringBuilder sb) {
        for (Inline inline : text.children) {
            if (inline instanceof TextNode) {
                sb.append(((TextNode) inline).getText());
            } else if (inline instanceof Span) {
                appendPlain((Span) inline, sb);
            } else if (inline instanceof LineBreak) {
                sb.append('\n');
            }
        }
    }

    @Override
    public String toString() {
        return plainText();
    }
}
