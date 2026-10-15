package me.manuloff.lib.table.renderer.docs;

import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.rule.Match;

import java.awt.Color;

/**
 * Примеры для docs/styles.md.
 */
final class StyleExamples {

    private StyleExamples() {
    }

    @Example("cascade")
    static Table cascade() {
        // region cascade
        Table table = new Table();
        table.cellStyle(s -> s.fontSize(13).color(new Color(0x334155)));     // 1. все ячейки
        table.head().style(s -> s.uppercase().letterSpacing(1));             // 2. секция
        table.column(2).style(s -> s.right());                              // 3. колонка
        table.header("Город", "Страна", "Население");
        table.addRow("Москва", "Россия", "13 010 112");
        table.addRow("Токио", "Япония", "13 960 000");
        table.addRow("Париж", "Франция", "2 102 650")
                .style(s -> s.background(new Color(0xFEF9C3)));            // 4. строка
        table.rule(Match.body().and(Match.column(0)), s -> s.bold());        // 5. правило
        table.body().row(0).cell(2).style(s -> s.color(Color.RED));         // 6. ячейка
        // endregion
        return table;
    }
}
