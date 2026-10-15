package me.manuloff.lib.table.renderer.docs;

import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.docs.Samples.Employee;
import me.manuloff.lib.table.renderer.rule.Match;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.theme.Themes;

import java.awt.Color;
import java.util.List;

import static me.manuloff.lib.table.renderer.docs.ContentExamples.badge;

/**
 * Примеры для docs/data.md.
 */
final class BuilderExamples {

    private BuilderExamples() {
    }

    @Example("builder")
    static Table builder() {
        List<Employee> staff = Samples.employees();
        // region builder
        double total = staff.stream().mapToDouble(Employee::getSalary).sum();

        Table table = Table.from(staff)
                .column("Сотрудник", Employee::getName, c -> c.style(Style::bold))
                .column("Должность", Employee::getPosition)
                .column("Возраст", Employee::getAge, c -> c.style(Style::right).headerStyle(Style::right))
                .column("Зарплата", Employee::getSalary, c -> c
                        .format((Double v) -> String.format("%,.0f ₽", v))
                        .style(Style::right).headerStyle(Style::right))
                .column("Статус", Employee::isActive, c -> c
                        .cell((cell, e) -> cell.value(e.isActive()
                                ? badge("активен", 0xDCFCE7, 0x166534)
                                : badge("в отпуске", 0xFEF3C7, 0x92400E))))
                .onRow((row, e) -> {
                    if (!e.isActive()) row.style(s -> s.color(new Color(0x64748B)));
                })
                .footer("Итого", "", "", String.format("%,.0f ₽", total), "")
                .configure(t -> t
                        .theme(Themes.STRIPED)
                        .caption("Сотрудники")
                        .rule(Match.foot().and(Match.column(3)), Style::right))
                .build();
        // endregion
        return table;
    }

    @Example("builder-simple")
    static Table builderSimple() {
        // region builder-simple
        Table table = Table.of(
                new Object[]{"Язык", "Год", "Автор"},
                new Object[][]{
                        {"Java", 1995, "Джеймс Гослинг"},
                        {"Kotlin", 2011, "JetBrains"},
                        {"Scala", 2004, "Мартин Одерски"}
                });
        // endregion
        return table;
    }
}
