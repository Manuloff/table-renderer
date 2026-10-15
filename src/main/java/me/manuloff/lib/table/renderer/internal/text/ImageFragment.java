package me.manuloff.lib.table.renderer.internal.text;

/**
 * Изображение в строке. Нижний край стоит на базовой линии блока изображения.
 *
 * @author Manuloff
 * @since 2.0
 */
final class ImageFragment {

    final InlineBox box;
    final double x;

    ImageFragment(InlineBox box, double x) {
        this.box = box;
        this.x = x;
    }
}
