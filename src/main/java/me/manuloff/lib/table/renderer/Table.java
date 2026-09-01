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

    // Качество рендеринга
    private RenderQuality renderQuality = RenderQuality.HIGH;

    // Фиксированная ширина таблицы
    private Integer fixedWidth;
    // Фиксированная высота таблицы
    private Integer fixedHeight;

    // === Cell ===

    @NonNull
    public Cell getCell(int row, int col) {
        return getCell(row, col, 1, 1);
    }

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

    @NonNull
    public TableStyle tableStyle() {
        return this.tableStyle;
    }

    @NonNull
    public Table tableStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.tableStyle);
        return this;
    }

    @NonNull
    public TextStyle textStyle() {
        return this.textStyle;
    }

    @NonNull
    public Table textStyle(Consumer<TextStyle> consumer) {
        consumer.accept(this.textStyle);
        return this;
    }

    // === Render ===

    @NonNull
    public RenderedTable render() {
        return new RenderedTable(this);
    }
}
