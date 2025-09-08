package me.manuloff.lib.table.renderer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.TextStyle;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Manuloff
 * @since 18:46 21.01.2025
 */
@Data
@Getter(AccessLevel.NONE)
public final class Row {

	private final Table table;
	@Getter
	private final int index;

	@NonNull
	private final TableStyle tableStyle = new TableStyle();
	@NonNull
	private final TextStyle textStyle = new TextStyle();

	public List<Cell> getCells() {
		return this.table.getCells().stream()
				.filter(cell -> cell.getRow() == this.index)
				.collect(Collectors.toList());
	}

	//

	@NonNull
	public TableStyle tableStyle() {
		return this.tableStyle;
	}

	@NonNull
	public Row tableStyle(@NonNull Consumer<TableStyle> consumer) {
		consumer.accept(this.tableStyle);
		return this;
	}

	@NonNull
	public TextStyle textStyle() {
		return this.textStyle;
	}

	@NonNull
	public Row textStyle(@NonNull Consumer<TextStyle> consumer) {
		consumer.accept(this.textStyle);
		return this;
	}
}
