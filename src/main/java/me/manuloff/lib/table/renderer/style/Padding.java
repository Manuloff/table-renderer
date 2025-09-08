package me.manuloff.lib.table.renderer.style;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * @author Manuloff
 * @since 15:56 21.01.2025
 */
@Data
@Accessors(fluent = true)
public final class Padding {

	private final int top;
	private final int bottom;
	private final int left;
	private final int right;

	public Padding(int top, int bottom, int left, int right) {
		validatePadding(top, "Top");
		validatePadding(bottom, "Bottom");
		validatePadding(left, "Left");
		validatePadding(right, "Right");

		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	public Padding(int vertical, int horizontal) {
		this(vertical, vertical, horizontal, horizontal);
	}

	public Padding(int padding) {
		this(padding, padding, padding, padding);
	}

	private static void validatePadding(int value, @NonNull String paddingName) {
		if (value < 0) {
			throw new IllegalArgumentException(paddingName + " padding can't be negative. Provided: " + value);
		}
	}
}
