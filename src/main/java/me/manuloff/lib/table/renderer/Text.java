package me.manuloff.lib.table.renderer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.TextStyle;

import java.util.function.Consumer;

/**
 * @author Manuloff
 * @since 22:48 03.09.2025
 */
@Data
public final class Text {

	public static final Text NEW_LINE = new Text("\n");

	@NonNull
	private final String text;

	@NonNull
	@Getter(AccessLevel.NONE)
	private final TextStyle textStyle = new TextStyle();

	//

	@NonNull
	public Text textStyle(@NonNull Consumer<TextStyle> consumer) {
		consumer.accept(this.textStyle);
		return this;
	}

	@NonNull
	public TextStyle textStyle() {
		return this.textStyle;
	}
}
