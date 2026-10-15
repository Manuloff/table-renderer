package me.manuloff.lib.table.renderer.docs;

import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.render.RenderOptions;
import me.manuloff.lib.table.renderer.rule.Match;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.theme.Themes;

import java.awt.Color;
import java.nio.file.Path;

/**
 * Примеры для docs/getting-started.md и README.md.
 */
final class GettingStartedExamples {

    private GettingStartedExamples() {
    }

    @Example("quick-start")
    static Table quickStart() {
        // region quick-start
        Table table = new Table();
        table.caption("Продажи за квартал");
        table.header("Товар", "Январь", "Февраль", "Март");
        table.addRow("Яблоки", 120, 135, 150);
        table.addRow("Груши", 80, 95, 70);
        table.addRow("Сливы", 45, 50, 62);
        table.footer("Итого", 245, 280, 282);

        // Числа - по правому краю
        table.rule(Match.column(1, 2, 3), Style::right);
        // endregion
        return table;
    }

    @Example("quick-start-themed")
    static Table quickStartThemed() {
        Table table = quickStart();
        // region quick-start-themed
        table.theme(Themes.MODERN);
        // endregion
        return table;
    }

    static void save(Table table) {
        // region quick-start-save
        table.render().write(Path.of("table.png"));

        // Чётче на экранах высокой плотности и с белым фоном
        table.render(RenderOptions.builder().scale(2).background(Color.WHITE).build())
                .write(Path.of("table@2x.png"));
        // endregion
    }
}
