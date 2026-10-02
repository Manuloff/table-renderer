package me.manuloff.lib.table.renderer;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


import lombok.experimental.Accessors;
import me.manuloff.lib.table.renderer.style.RenderQuality;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.TextStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Таблица - основной элемент для создания и рендеринга таблиц.
 *
 * @author Manuloff
 * @since 1.0
 */
@Data
@NoArgsConstructor
@Accessors(fluent = true)
public final class Table {

    private final List<Cell> cells = new ArrayList<>();

    private final Map<Integer, Row> rows = new HashMap<>();
    private final Map<Integer, Column> columns = new HashMap<>();

    @NonNull
    private final TableStyle tableStyle = new TableStyle();
    @NonNull
    private final TextStyle textStyle = new TextStyle();

    /**
     * Качество рендеринга. По умолчанию {@link RenderQuality#HIGH}.
     */
    private RenderQuality renderQuality = RenderQuality.HIGH;

    /**
     * Фиксированная логическая ширина таблицы в пикселях; {@code null} — по содержимому.
     */
    private Integer fixedWidth;
    /**
     * Фиксированная логическая высота таблицы в пикселях; {@code null} — по содержимому.
     */
    private Integer fixedHeight;

    // === Cell ===

    /**
     * Возвращает ячейку {@code 1×1} в позиции {@code (row, col)}.
     * Если позиция уже занята существующей ячейкой (в том числе из-за span), возвращается она.
     *
     * @param row индекс строки (с нуля)
     * @param col индекс колонки (с нуля)
     * @throws IllegalArgumentException если индекс отрицательный
     */
    @NonNull
    public Cell getCell(int row, int col) {
        return getCell(row, col, 1, 1);
    }

    /**
     * Возвращает ячейку с указанным объединением.
     * Если позиция пересекается с уже созданной ячейкой, возвращается существующая
     * (новые {@code rowspan}/{@code colspan} при этом не применяются).
     *
     * @param row     индекс строки левого верхнего угла (с нуля)
     * @param col     индекс колонки левого верхнего угла (с нуля)
     * @param rowspan число занимаемых строк, не меньше 1
     * @param colspan число занимаемых колонок, не меньше 1
     * @throws IllegalArgumentException если индекс отрицательный или span меньше 1
     */
    @NonNull
    public Cell getCell(int row, int col, int rowspan, int colspan) {
        if (row < 0) throw new IllegalArgumentException("Row can't be negative. Provided: " + row);
        if (col < 0) throw new IllegalArgumentException("Column can't be negative. Provided: " + col);
        if (rowspan < 1) throw new IllegalArgumentException("Rowspan must be at least 1. Provided: " + rowspan);
        if (colspan < 1) throw new IllegalArgumentException("Colspan must be at least 1. Provided: " + colspan);

        for (Cell cell : this.cells) {
            if (row >= cell.getRow() && row < cell.getRow() + cell.getRowspan()
                    && col >= cell.getCol() && col < cell.getCol() + cell.getColspan()) {
                return cell;
            }
        }

        Cell cell = new Cell(this, row, col, rowspan, colspan);
        this.cells.add(cell);

        return cell;
    }

    // === Row ===

    /**
     * Возвращает строку с индексом {@code row}, создавая её при первом обращении.
     *
     * @param row индекс строки (с нуля)
     */
    @NonNull
    public Row getRow(int row) {
        return this.rows.computeIfAbsent(row, i -> new Row(this, i));
    }

    /**
     * @return количество строк в таблице (максимальный индекс + 1)
     */
    public int getRowCount() {
        if (cells.isEmpty()) return 0;
        int maxRow = 0;
        for (Cell cell : cells) {
            maxRow = Math.max(maxRow, cell.getRow() + cell.getRowspan() - 1);
        }
        return maxRow + 1;
    }

    // === Column ===

    /**
     * Возвращает колонку с индексом {@code col}, создавая её при первом обращении.
     *
     * @param col индекс колонки (с нуля)
     */
    @NonNull
    public Column getColumn(int col) {
        return this.columns.computeIfAbsent(col, i -> new Column(this, i));
    }

    /**
     * @return количество колонок в таблице (максимальный индекс + 1)
     */
    public int getColumnCount() {
        if (cells.isEmpty()) return 0;
        int maxCol = 0;
        for (Cell cell : cells) {
            maxCol = Math.max(maxCol, cell.getCol() + cell.getColspan() - 1);
        }
        return maxCol + 1;
    }

    // === Table styles ===

    /**
     * @return глобальный стиль оформления таблицы
     */
    @NonNull
    public TableStyle tableStyle() {
        return this.tableStyle;
    }

    /**
     * Настраивает глобальный {@link TableStyle} и возвращает эту таблицу.
     */
    @NonNull
    public Table tableStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.tableStyle);
        return this;
    }

    /**
     * @return глобальный стиль текста таблицы
     */
    @NonNull
    public TextStyle textStyle() {
        return this.textStyle;
    }

    /**
     * Настраивает глобальный {@link TextStyle} и возвращает эту таблицу.
     */
    @NonNull
    public Table textStyle(Consumer<TextStyle> consumer) {
        consumer.accept(this.textStyle);
        return this;
    }

    // === Render ===

    /**
     * Считает раскладку и рисует таблицу в изображение.
     *
     * @return результат рендеринга с логическим размером и {@link java.awt.image.BufferedImage}
     */
    @NonNull
    public RenderedTable render() {
        return new RenderedTable(this);
    }
}
