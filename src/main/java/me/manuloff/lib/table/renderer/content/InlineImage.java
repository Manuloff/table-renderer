package me.manuloff.lib.table.renderer.content;

import lombok.Getter;
import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.background.ObjectFit;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Изображение внутри текста (иконка, аватар, флаг).
 * Нижний край изображения стоит на базовой линии; поднять или опустить его можно через
 * {@link Style#baselineOffset(double)}. Из стиля также используются рамка, скругление, отступы,
 * тень, прозрачность и фон.
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
public final class InlineImage implements Inline {

    @NonNull
    private final BufferedImage image;
    /**
     * Ширина содержимого; null - по пропорциям или исходная.
     */
    private final Double width;
    private final Double height;
    @NonNull
    private ObjectFit fit = ObjectFit.FILL;
    @NonNull
    private final Style style = new Style();

    /**
     * Изображение с заданным размером содержимого; null - по пропорциям или исходный размер.
     */
    public InlineImage(@NonNull BufferedImage image, Double width, Double height) {
        if (width != null && width < 0 || height != null && height < 0) {
            throw new IllegalArgumentException("Image size can't be negative");
        }
        this.image = image;
        this.width = width;
        this.height = height;
    }

    /**
     * Способ вписывания изображения в заданный размер. По умолчанию - {@link ObjectFit#FILL}.
     */
    public InlineImage fit(@NonNull ObjectFit fit) {
        this.fit = fit;
        return this;
    }

    /**
     * Настраивает оформление изображения.
     */
    public InlineImage style(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.style);
        return this;
    }

    /**
     * @return ширина содержимого с учётом пропорций
     */
    public double contentWidth() {
        if (this.width != null) return this.width;
        if (this.height != null) return this.height * this.image.getWidth() / Math.max(1, this.image.getHeight());
        return this.image.getWidth();
    }

    /**
     * @return высота содержимого с учётом пропорций
     */
    public double contentHeight() {
        if (this.height != null) return this.height;
        if (this.width != null) return this.width * this.image.getHeight() / Math.max(1, this.image.getWidth());
        return this.image.getHeight();
    }
}
