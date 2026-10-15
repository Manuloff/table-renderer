package me.manuloff.lib.table.renderer;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.content.RichText;
import me.manuloff.lib.table.renderer.style.Style;

import java.util.function.Consumer;

/**
 * Заголовок таблицы (аналог caption). Располагается над или под таблицей
 * ({@link me.manuloff.lib.table.renderer.style.TableStyle#captionSide}) и занимает её ширину.
 * <p>
 * Текстовые свойства по умолчанию наследуются от {@link Table#cellStyle()}; текст выравнивается по центру.
 * Фон, рамка и отступы задаются собственным стилем заголовка.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Caption {

    private final RichText content = new RichText();
    private final Style style = new Style();

    Caption() {
    }

    /**
     * @return содержимое заголовка
     */
    public RichText content() {
        return this.content;
    }

    /**
     * Заполняет содержимое заголовка.
     */
    public Caption content(@NonNull Consumer<RichText> consumer) {
        consumer.accept(this.content);
        return this;
    }

    /**
     * Заменяет содержимое заголовка текстом.
     */
    public Caption text(@NonNull String text) {
        this.content.clear().text(text);
        return this;
    }

    /**
     * @return стиль заголовка
     */
    public Style style() {
        return this.style;
    }

    /**
     * Настраивает стиль заголовка.
     */
    public Caption style(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.style);
        return this;
    }
}
