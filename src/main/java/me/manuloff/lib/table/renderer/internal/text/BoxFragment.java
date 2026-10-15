package me.manuloff.lib.table.renderer.internal.text;

/**
 * Часть стилизованного фрагмента, попавшая в одну строку. Если фрагмент перенесён на несколько строк,
 * левый край (рамка, скругление) есть только у первой части, правый - только у последней.
 *
 * @author Manuloff
 * @since 2.0
 */
final class BoxFragment {

    final InlineBox box;
    double left;
    double right;
    boolean hasStart;
    boolean hasEnd;

    BoxFragment(InlineBox box) {
        this.box = box;
    }
}
