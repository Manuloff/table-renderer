package me.manuloff.lib.table.renderer.theme;

import me.manuloff.lib.table.renderer.Table;
/**
 * Тема оформления - набор стилей, применяемых к таблице. Готовые темы - в {@link Themes}.
 *
 * @author Manuloff
 * @since 2.0
 */
@FunctionalInterface
public interface Theme {

    void apply(Table table);

    default Theme andThen(Theme next) {
        return table -> {
            apply(table);
            next.apply(table);
        };
    }
}
