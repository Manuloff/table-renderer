package me.manuloff.lib.table.renderer.rule;

import lombok.NonNull;
import lombok.Value;
import me.manuloff.lib.table.renderer.style.Style;

/**
 * Условное правило стиля: стиль применяется ко всем ячейкам, удовлетворяющим условию.
 * Правила добавляются через {@link me.manuloff.lib.table.renderer.Table#rule}.
 *
 * @author Manuloff
 * @since 2.0
 */
@Value
public class StyleRule {

    @NonNull
    CellMatcher matcher;
    @NonNull
    Style style;
    /**
     * Правило добавлено темой оформления (такие правила заменяются при применении другой темы).
     */
    boolean theme;
}
