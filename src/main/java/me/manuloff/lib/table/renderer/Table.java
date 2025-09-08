package me.manuloff.lib.table.renderer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.TextStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Manuloff
 * @since 13:47 21.01.2025
 */
@Data
@Getter(AccessLevel.NONE)
public final class Table {

	@Getter
	private final List<Cell> cells = new ArrayList<>();

	private final Map<Integer, Row> rows = new HashMap<>();
	private final Map<Integer, Column> columns = new HashMap<>();

	@NonNull
	private final TableStyle tableStyle = TableStyle.DEFAULT;
	@NonNull
	private final TextStyle textStyle = TextStyle.DEFAULT;

	@NonNull
	public Cell getCell(int row, int col) {
		return this.getCell(row, col, 1, 1);
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

	@NonNull
	public Row getRow(int row) {
		return this.rows.computeIfAbsent(row, i -> new Row(this, i));
	}

	@NonNull
	public Column getColumn(int col) {
		return this.columns.computeIfAbsent(col, i -> new Column(this, i));
	}

	@NonNull
	public TableStyle tableStyle() {
		return this.tableStyle;
	}

	@NonNull
	public Table tableStyle(@NonNull Consumer<TableStyle> consumer) {
		consumer.accept(this.tableStyle);
		return this;
	}

	@NonNull
	public TextStyle textStyle() {
		return this.textStyle;
	}

	@NonNull
	public Table textStyle(@NonNull Consumer<TextStyle> consumer) {
		consumer.accept(this.textStyle);
		return this;
	}

	@NonNull
	public RenderedTable render() {
		return new RenderedTable(this);
	}
}
