package me.manuloff.lib.table.renderer.content;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.Style;

import java.util.function.Consumer;

/**
 * Стилизованный фрагмент текста. Текстовые свойства наследуются вложенными фрагментами;
 * фон, рамка, скругление, отступы по горизонтали и тень рисуются вокруг фрагмента (удобно для "бейджей").
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Span extends RichText implements Inline {

    private final Style style = new Style();

    /**
     * @return фрагмент с текстом и стилем
     */
    public static Span of(@NonNull String text, @NonNull Consumer<Style> style) {
        Span span = new Span();
        style.accept(span.style);
        span.text(text);
        return span;
    }

    /**
     * @return стиль фрагмента
     */
    public Style style() {
        return this.style;
    }

    /**
     * Настраивает стиль фрагмента.
     */
    public Span style(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.style);
        return this;
    }
}
