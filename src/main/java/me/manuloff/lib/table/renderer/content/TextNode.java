package me.manuloff.lib.table.renderer.content;

import lombok.NonNull;
import lombok.Value;

/**
 * Фрагмент текста без собственного стиля.
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
public class TextNode implements Inline {

    @NonNull
    String text;
}
