package me.manuloff.lib.table.renderer.internal.text;

import java.util.List;

/**
 * Строка разложенного абзаца. Координаты фрагментов по горизонтали - от начала строки
 * (до сдвига выравнивания {@link #offset}).
 *
 * @author Manuloff
 * @since 2.0
 */
final class TextLine {

    /**
     * Верх строки относительно верха абзаца.
     */
    double top;
    double height;
    /**
     * Положение базовой линии от верха строки.
     */
    double baseline;
    /**
     * Ширина содержимого строки.
     */
    double width;
    /**
     * Сдвиг строки при выравнивании по центру или вправо.
     */
    double offset;

    List<TextFragment> texts;
    List<ImageFragment> images;
    /**
     * Части оформленных строчных фрагментов (фон, рамка), попавшие в строку; внешние раньше вложенных.
     */
    List<BoxFragment> boxes;
}
