package me.manuloff.lib.table.renderer.style;

import lombok.*;
import lombok.experimental.Accessors;

import java.awt.*;

/**
 * @author Manuloff
 * @since 19:46 03.09.2025
 */
@Data
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public final class TableStyle implements Cloneable {

	public static final TableStyle DEFAULT = new TableStyle(
			Color.WHITE,
			new Padding(1),
			1,
			Color.BLACK,
			0,
			0
	);

	//

	private Color backgroundColor;

	private Padding padding;

	private Integer borderWidth;
	private Color borderColor;

	private Integer minCellWidth;
	private Integer minCellHeight;

	//

	@Override
	@SneakyThrows
	public TableStyle clone() {
		TableStyle tableStyle = (TableStyle) super.clone();

		tableStyle.backgroundColor = this.backgroundColor;

		tableStyle.padding = this.padding;

		tableStyle.borderWidth = this.borderWidth;
		tableStyle.borderColor = this.borderColor;

		tableStyle.minCellWidth = this.minCellWidth;
		tableStyle.minCellHeight = this.minCellHeight;

		return tableStyle;
	}

	public void applyTo(@NonNull TableStyle style) {
		if (this.backgroundColor != null) style.backgroundColor = this.backgroundColor;

		if (this.padding != null) style.padding = this.padding;

		if (this.borderWidth != null) style.borderWidth = this.borderWidth;
		if (this.borderColor != null) style.borderColor = this.borderColor;

		if (this.minCellWidth != null) style.minCellWidth = this.minCellWidth;
		if (this.minCellHeight != null) style.minCellHeight = this.minCellHeight;
	}
}
