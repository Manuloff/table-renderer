package me.manuloff.lib.table.renderer.style.background;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.awt.image.BufferedImage;

/**
 * Фоновое изображение (аналог CSS background-image: url() с background-size, -position и -repeat).
 *
 * <pre>{@code
 * ImageLayer.builder().image(logo).fit(ObjectFit.CONTAIN).alignX(1).repeat(false).build()
 * }</pre>
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
@Builder(toBuilder = true)
public class ImageLayer implements BackgroundLayer {

    @NonNull
    BufferedImage image;
    /**
     * Как вписать изображение в блок, если не заданы {@link #width}/{@link #height}.
     */
    @NonNull
    @Builder.Default
    ObjectFit fit = ObjectFit.NONE;
    /**
     * Явная ширина плитки; если задана только одна сторона, вторая считается по пропорциям.
     */
    Double width;
    Double height;
    /**
     * Положение по горизонтали: 0 - слева, 0.5 - по центру, 1 - справа (аналог процентов background-position).
     */
    @Builder.Default
    double alignX = 0;
    /**
     * Положение по вертикали: 0 - сверху, 1 - снизу.
     */
    @Builder.Default
    double alignY = 0;
    @Builder.Default
    boolean repeatX = true;
    @Builder.Default
    boolean repeatY = true;
    /**
     * Сглаживание при масштабировании; false - пиксельное (pixelated).
     */
    @Builder.Default
    boolean smooth = true;

    /**
     * @return изображение, заполняющее блок с сохранением пропорций (лишнее обрезается), по центру
     */
    public static ImageLayer cover(BufferedImage image) {
        return builder().image(image).fit(ObjectFit.COVER).alignX(0.5).alignY(0.5).repeatX(false).repeatY(false).build();
    }

    /**
     * @return изображение, вписанное в блок целиком, по центру
     */
    public static ImageLayer contain(BufferedImage image) {
        return builder().image(image).fit(ObjectFit.CONTAIN).alignX(0.5).alignY(0.5).repeatX(false).repeatY(false).build();
    }

    /**
     * @return изображение, повторяющееся плиткой от левого верхнего угла
     */
    public static ImageLayer tiled(BufferedImage image) {
        return builder().image(image).build();
    }

    /**
     * Builder с удобным методом установки повторения по обеим осям.
     */
    public static class ImageLayerBuilder {
        /**
         * Повторение по обеим осям.
         */
        public ImageLayerBuilder repeat(boolean repeat) {
            return repeatX(repeat).repeatY(repeat);
        }
    }
}
