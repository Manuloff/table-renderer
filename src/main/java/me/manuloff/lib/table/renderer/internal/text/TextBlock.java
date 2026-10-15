package me.manuloff.lib.table.renderer.internal.text;

import java.util.List;

/**
 * Абзац, разложенный по строкам заданной ширины. Координаты строк и фрагментов отсчитываются
 * от левого верхнего угла области содержимого.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class TextBlock {

    final List<TextLine> lines;
    private final double height;

    TextBlock(List<TextLine> lines) {
        this.lines = lines;
        double h = 0;
        for (TextLine line : lines) {
            line.top = h;
            h += line.height;
        }
        this.height = h;
    }

    /**
     * @return суммарная высота строк
     */
    public double height() {
        return this.height;
    }

    /**
     * @return true, если строк нет (пустое содержимое)
     */
    public boolean isEmpty() {
        return this.lines.isEmpty();
    }

    /**
     * @return количество строк
     */
    public int lineCount() {
        return this.lines.size();
    }

    /**
     * @return расстояние от верха до базовой линии первой строки или NaN, если строк нет
     */
    public double firstBaseline() {
        return this.lines.isEmpty() ? Double.NaN : this.lines.get(0).baseline;
    }

    /**
     * @return правый край самой длинной строки с учётом выравнивания
     */
    public double contentWidth() {
        double w = 0;
        for (TextLine line : this.lines) w = Math.max(w, line.offset + line.width);
        return w;
    }
}
