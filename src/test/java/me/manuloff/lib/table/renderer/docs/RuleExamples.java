package me.manuloff.lib.table.renderer.docs;

import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.rule.Match;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.theme.Theme;
import me.manuloff.lib.table.renderer.theme.Themes;

import java.awt.Color;

/**
 * Примеры для docs/rules-and-themes.md.
 */
final class RuleExamples {

    private RuleExamples() {
    }

    private static Table sales() {
        Table table = new Table();
        table.caption("Продажи за первый квартал");
        table.header("Товар", "Январь", "Февраль", "Март", "Итого");
        table.addRow("Яблоки", 120, 135, 150, 405);
        table.addRow("Груши", 80, 95, 70, 245);
        table.addRow("Сливы", 45, 50, 62, 157);
        table.addRow("Виноград", 210, 190, 230, 630);
        table.footer("Всего", 455, 470, 512, 1437);
        table.rule(Match.column(1, 2, 3, 4), Style::right);
        table.rule(Match.column(4), Style::bold);
        return table;
    }

    @Example("rules")
    static Table rules() {
        // region rules
        Table table = new Table();
        table.header("Счёт", "Изменение", "Остаток");
        table.addRow("Основной", 1500, 12000);
        table.addRow("Резерв", -300, 4700);
        table.addRow("Кредит", -1200, -8000);
        table.addRow("Бонусы", 50, 640);

        // "Зебра" в теле таблицы
        table.rule(Match.body().and(Match.evenRows()), s -> s.background(new Color(0xF3F4F6)));
        // Числовые колонки - вправо
        table.rule(Match.column(1, 2), Style::right);
        // Отрицательные числа - красным
        table.rule(Match.numeric().and(Match.value(v -> ((Number) v).doubleValue() < 0)),
                s -> s.color(new Color(0xDC2626)));
        // Своё условие: последняя колонка в теле - жирная
        table.rule(c -> c.isLastColumn() && !c.isHeader(), Style::bold);
        // endregion
        return table;
    }

    @Example("theme-default")
    static Table themeDefault() {
        return sales().theme(Themes.DEFAULT);
    }

    @Example("theme-grid")
    static Table themeGrid() {
        return sales().theme(Themes.GRID);
    }

    @Example("theme-minimal")
    static Table themeMinimal() {
        return sales().theme(Themes.MINIMAL);
    }

    @Example("theme-striped")
    static Table themeStriped() {
        return sales().theme(Themes.STRIPED);
    }

    @Example("theme-dark")
    static Table themeDark() {
        return sales().theme(Themes.DARK);
    }

    @Example("theme-modern")
    static Table themeModern() {
        return sales().theme(Themes.MODERN);
    }

    // region custom-theme
    static final Theme CORPORATE = table -> {
        Color brand = new Color(0x0F766E);
        Themes.PLAIN.apply(table);                         // начать с чистого листа
        table.style().border(BorderSide.solid(2, brand)).background(Color.WHITE);
        table.cellStyle().padding(7, 12).borderBottom(BorderSide.solid(1, new Color(0xCCE7E4)));
        table.head().style().bold().color(Color.WHITE).background(brand);
        table.foot().style().bold().background(new Color(0xF0FDFA));
        table.themeRule(Match.body().and(Match.evenRows()), s -> s.background(new Color(0xF8FAFA)));
    };
    // endregion

    @Example("custom-theme-usage")
    static Table customTheme() {
        // region custom-theme-usage
        Table table = sales().theme(CORPORATE);
        // endregion
        return table;
    }
}
