package me.manuloff.lib.table.renderer.render;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.awt.Color;
import java.util.List;
import java.util.Locale;

/**
 * Параметры рендера.
 *
 * <pre>{@code
 * table.render(RenderOptions.builder().scale(2).background(Color.WHITE).build());
 * }</pre>
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
@Builder(toBuilder = true)
public class RenderOptions {

    /**
     * Параметры по умолчанию: масштаб 1, прозрачный фон, без ограничения ширины.
     */
    public static final RenderOptions DEFAULT = builder().build();

    /**
     * Масштаб (плотность пикселей): 2 - изображение в два раза больше с той же раскладкой, как на Retina-экранах.
     */
    @Builder.Default
    double scale = 1;

    /**
     * Фон всего изображения; null - прозрачный.
     */
    Color background;

    /**
     * Доступная ширина (как ширина окна в браузере): ограничивает автоматическую ширину таблицы,
     * служит базой для процентной ширины таблицы. null - не ограничено.
     */
    Double availableWidth;

    /**
     * Сглаживание фигур и текста.
     */
    @Builder.Default
    boolean antialiasing = true;

    /**
     * Шрифты для символов, которых нет в шрифтах из стиля (эмодзи, иероглифы и т.д.).
     */
    @NonNull
    @Builder.Default
    List<String> fallbackFonts = List.of("Dialog", "Segoe UI Symbol", "Segoe UI Emoji", "Noto Sans", "DejaVu Sans");

    /**
     * Локаль для правил переноса строк и изменения регистра.
     */
    @NonNull
    @Builder.Default
    Locale locale = Locale.getDefault();

    private RenderOptions(double scale, Color background, Double availableWidth, boolean antialiasing,
                          @NonNull List<String> fallbackFonts, @NonNull Locale locale) {
        if (!(scale > 0) || Double.isInfinite(scale)) throw new IllegalArgumentException("Scale must be positive: " + scale);
        if (availableWidth != null && availableWidth < 0) {
            throw new IllegalArgumentException("Available width can't be negative: " + availableWidth);
        }
        this.scale = scale;
        this.background = background;
        this.availableWidth = availableWidth;
        this.antialiasing = antialiasing;
        this.fallbackFonts = List.copyOf(fallbackFonts);
        this.locale = locale;
    }
}
