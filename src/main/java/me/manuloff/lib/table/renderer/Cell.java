package me.manuloff.lib.table.renderer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.TextStyle;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Manuloff
 * @since 14:00 21.01.2025
 */
@Data
public final class Cell {

	@Getter(AccessLevel.NONE)
	private final Table table;

	private final int row;
	private final int col;
	private final int rowspan;
	private final int colspan;

	@NonNull
	@Getter(AccessLevel.NONE)
	private final TableStyle tableStyle = new TableStyle();
	@NonNull
	@Getter(AccessLevel.NONE)
	private final TextStyle textStyle = new TextStyle();

	@NonNull
	private List<Text> texts = new LinkedList<>();

	@NonNull
	public Cell setTexts(@NonNull List<Text> texts) {
		this.texts = texts;
		return this;
	}

	@NonNull
	public Cell setText(@NonNull String text) {
		this.texts = List.of(new Text(text));
		return this;
	}

	@NonNull
	public Cell setText(@NonNull Text text) {
		this.texts = List.of(text);
		return this;
	}

	@NonNull
	public Cell appendText(@NonNull String text) {
		this.texts.add(new Text(text));
		return this;
	}

	@NonNull
	public Cell appendText(@NonNull Text text) {
		this.texts.add(text);
		return this;
	}

	@NonNull
	public Cell appendNewLine() {
		this.texts.add(Text.NEW_LINE);
		return this;
	}

	//

	@NonNull
	public TableStyle tableStyle() {
		return tableStyle;
	}

	@NonNull
	public Cell tableStyle(@NonNull Consumer<TableStyle> consumer) {
		consumer.accept(this.tableStyle);
		return this;
	}

	@NonNull
	public TextStyle textStyle() {
		return textStyle;
	}

	@NonNull
	public TextStyle textStyle(@NonNull Consumer<TextStyle> consumer) {
		consumer.accept(this.textStyle);
		return textStyle;
	}

}
