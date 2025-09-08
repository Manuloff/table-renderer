package me.manuloff.lib.table.renderer.style;

import lombok.*;
import lombok.experimental.Accessors;

import java.awt.*;

/**
 * @author Manuloff
 * @since 19:40 03.09.2025
 */
@Data
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public final class TextStyle implements Cloneable {

	public static final TextStyle DEFAULT = new TextStyle(
			"Arial",
			Font.PLAIN,
			12,
			Color.BLACK,
			Alignment.TOP_LEFT,
			1F
	);

	//

	private String fontName;
	private Integer fontStyle;
	private Integer fontSize;

	private Color textColor;
	private Alignment textAlignment;
	private Float lineHeight;

	@SneakyThrows
	@Override
	public TextStyle clone() {
		TextStyle style = (TextStyle) super.clone();

		style.fontName = this.fontName;
		style.fontStyle = this.fontStyle;
		style.fontSize = this.fontSize;

		style.textColor = this.textColor;
		style.textAlignment = this.textAlignment;
		style.lineHeight = this.lineHeight;

		return style;
	}

	public void applyTo(@NonNull TextStyle style) {
		if (this.fontName != null) style.fontName = this.fontName;
		if (this.fontStyle != null) style.fontStyle = this.fontStyle;
		if (this.fontSize != null) style.fontSize = this.fontSize;

		if (this.textColor != null) style.textColor = this.textColor;
		if (this.textAlignment != null) style.textAlignment = this.textAlignment;
		if (this.lineHeight != null) style.lineHeight = this.lineHeight;
	}
}
