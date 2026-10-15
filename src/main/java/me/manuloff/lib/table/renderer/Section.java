package me.manuloff.lib.table.renderer;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Группа строк таблицы: шапка (thead), тело (tbody) или подвал (tfoot).
 * Шапка всегда рисуется первой, подвал - последним, независимо от порядка заполнения.
 * <p>
 * Стиль секции применяется ко всем её ячейкам, а фон секции рисуется одним слоем на всю её площадь.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Section {

    /**
     * Вид секции.
     */
    public enum Kind {
        /**
         * Шапка: ячейки по умолчанию заголовочные.
         */
        HEAD,
        /**
         * Тело таблицы.
         */
        BODY,
        /**
         * Подвал.
         */
        FOOT
    }

    private final Table table;
    private final Kind kind;
    private final List<Row> rows = new ArrayList<>();
    private final Style style = new Style();

    Section(Table table, Kind kind) {
        this.table = table;
        this.kind = kind;
    }

    /**
     * @return таблица, которой принадлежит секция
     */
    public Table table() {
        return this.table;
    }

    /**
     * @return вид секции
     */
    public Kind kind() {
        return this.kind;
    }

    // ============================================
    // Строки
    // ============================================

    /**
     * Добавляет строку с ячейками из значений.
     *
     * @return добавленная строка
     */
    public Row addRow(Object... values) {
        Row row = new Row(this);
        row.addCells(values);
        this.rows.add(row);
        return row;
    }

    /**
     * Добавляет строку с ячейками из значений.
     *
     * @return добавленная строка
     */
    public Row addRow(@NonNull Iterable<?> values) {
        Row row = new Row(this);
        row.addCells(values);
        this.rows.add(row);
        return row;
    }

    /**
     * Добавляет несколько строк.
     */
    public Section addRows(@NonNull Object[][] rows) {
        for (Object[] values : rows) addRow(values);
        return this;
    }

    /**
     * @param index индекс строки внутри секции
     */
    public Row row(int index) {
        return this.rows.get(index);
    }

    /**
     * @return строки секции
     */
    public List<Row> rows() {
        return Collections.unmodifiableList(this.rows);
    }

    /**
     * Удаляет строку из секции.
     */
    public Section removeRow(@NonNull Row row) {
        this.rows.remove(row);
        return this;
    }

    /**
     * @return true, если в секции нет строк
     */
    public boolean isEmpty() {
        return this.rows.isEmpty();
    }

    // ============================================
    // Стиль
    // ============================================

    /**
     * @return стиль, применяемый ко всем ячейкам секции
     */
    public Style style() {
        return this.style;
    }

    /**
     * Настраивает стиль ячеек секции.
     */
    public Section style(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.style);
        return this;
    }
}
