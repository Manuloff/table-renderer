package me.manuloff.lib.table.renderer;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Строка таблицы.
 * <p>
 * Стиль строки применяется ко всем её ячейкам (поверх стилей секции и колонки), а фон строки рисуется
 * одним слоем на всю её ширину. Высота из стиля - минимальная высота строки.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Row {

    private final Section section;
    private final List<Cell> cells = new ArrayList<>();
    private final Style style = new Style();

    Row(Section section) {
        this.section = section;
    }

    /**
     * @return секция, которой принадлежит строка
     */
    public Section section() {
        return this.section;
    }

    /**
     * @return таблица, которой принадлежит строка
     */
    public Table table() {
        return this.section.table();
    }

    /**
     * @return индекс строки внутри секции или -1, если строка удалена
     */
    public int index() {
        return this.section.rows().indexOf(this);
    }

    // ============================================
    // Ячейки
    // ============================================

    /**
     * Добавляет ячейку. В шапке ячейки по умолчанию заголовочные.
     *
     * @param value значение: строка, число, {@link me.manuloff.lib.table.renderer.content.RichText},
     *              изображение и т.д. (см. {@link Cell#value(Object)})
     * @return добавленная ячейка
     */
    public Cell addCell(Object value) {
        Cell cell = new Cell(this, value, this.section.kind() == Section.Kind.HEAD);
        this.cells.add(cell);
        return cell;
    }

    /**
     * Добавляет заголовочную ячейку (аналог th), например заголовок строки в теле таблицы.
     *
     * @return добавленная ячейка
     */
    public Cell addHeaderCell(Object value) {
        return addCell(value).header(true);
    }

    /**
     * Добавляет ячейки из значений.
     */
    public Row addCells(Object... values) {
        for (Object value : values) addCell(value);
        return this;
    }

    /**
     * Добавляет ячейки из значений.
     */
    public Row addCells(@NonNull Iterable<?> values) {
        for (Object value : values) addCell(value);
        return this;
    }

    /**
     * @param index индекс ячейки в строке (не колонки: объединённая ячейка занимает один индекс)
     */
    public Cell cell(int index) {
        return this.cells.get(index);
    }

    /**
     * @return ячейки строки
     */
    public List<Cell> cells() {
        return Collections.unmodifiableList(this.cells);
    }

    /**
     * Удаляет ячейку из строки.
     */
    public Row removeCell(@NonNull Cell cell) {
        this.cells.remove(cell);
        return this;
    }

    // ============================================
    // Стиль
    // ============================================

    /**
     * @return стиль, применяемый ко всем ячейкам строки
     */
    public Style style() {
        return this.style;
    }

    /**
     * Настраивает стиль ячеек строки.
     */
    public Row style(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.style);
        return this;
    }

    /**
     * Задаёт минимальную высоту строки.
     */
    public Row height(double height) {
        this.style.height(height);
        return this;
    }
}
