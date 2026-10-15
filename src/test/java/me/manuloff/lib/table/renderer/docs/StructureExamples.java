package me.manuloff.lib.table.renderer.docs;

import me.manuloff.lib.table.renderer.Row;
import me.manuloff.lib.table.renderer.Section;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.rule.Match;
import me.manuloff.lib.table.renderer.style.Style;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Примеры для docs/structure.md.
 */
final class StructureExamples {

    private StructureExamples() {
    }

    @Example("sections")
    static Table sections() {
        // region sections
        Table table = new Table();
        table.header("Квартал", "Выручка");          // шапка
        table.addRow("I", 1200);                      // первое тело
        table.addRow("II", 1350);

        Section second = table.addBody();             // второе тело - отдельная группа строк
        second.style(s -> s.background(new Color(0xFFF7E6)));
        second.addRow("III (прогноз)", 1400);
        second.addRow("IV (прогноз)", 1500);

        table.footer("Год", 5450);                    // подвал - всегда внизу
        table.rule(Match.column(1), Style::right);
        // endregion
        return table;
    }

    @Example("spans")
    static Table spans() {
        // region spans
        Table table = new Table();
        table.cellStyle(s -> s.center());

        Row top = table.head().addRow();
        top.addCell("Регион").rowspan(2);
        top.addCell("2025").colspan(2);
        top.addCell("2026").colspan(2);
        table.header("I пол.", "II пол.", "I пол.", "II пол.");

        Row north = table.addRow();
        north.addHeaderCell("Север").rowspan(2);       // заголовок строки на две строки
        north.addCells(10, 20, 30, 40);
        table.addRow(11, 21, 31, 41);                  // первая колонка занята объединением

        Row south = table.addRow("Юг");
        south.addCell("Нет данных").colspan(4).style(s -> s.italic().color(Color.GRAY));
        // endregion
        return table;
    }

    @Example("columns")
    static Table columns() {
        // region columns
        Table table = new Table();
        table.column(0).width(140);                                // ширина колонки
        table.column(1).style(s -> s.right().color(new Color(0x1D4ED8)));
        table.column(2).style(s -> s.background(new Color(0xF0FDF4)));  // фон - через всю колонку

        table.header("Тариф", "Цена", "Описание");
        table.addRow("Базовый", "0 ₽", "Для знакомства");
        table.addRow("Стандарт", "490 ₽", "Для небольших команд");
        table.addRow("Про", "1 990 ₽", "Всё и сразу");
        // endregion
        return table;
    }

    @Example("caption")
    static Table caption() {
        // region caption
        Table table = new Table();
        table.caption(c -> c
                .text("Курсы валют")
                .style(s -> s.bold().fontSize(16).color(new Color(0x1E3A8A))));
        table.header("Валюта", "Курс");
        table.addRow("USD", "92,15");
        table.addRow("EUR", "99,80");
        table.rule(Match.column(1), Style::right);
        // endregion
        return table;
    }

    @Example("values")
    static Table values() {
        // region values
        DateTimeFormatter dates = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        Table table = new Table()
                .formatter(v -> {
                    if (v instanceof BigDecimal) return String.format("%,.2f ₽", v);
                    if (v instanceof LocalDate) return ((LocalDate) v).format(dates);
                    if (v instanceof Boolean) return (Boolean) v ? "да" : "нет";
                    return String.valueOf(v);
                });
        table.header("Дата", "Сумма", "Оплачен");
        table.addRow(LocalDate.of(2026, 3, 1), new BigDecimal("12500.5"), true);
        table.addRow(LocalDate.of(2026, 3, 15), new BigDecimal("990"), false);
        table.addRow(List.of(LocalDate.of(2026, 4, 2), new BigDecimal("47000"), true));

        // Значение доступно правилам - оформление по данным
        table.rule(Match.value(Boolean.FALSE::equals), s -> s.color(Color.RED).bold());
        table.rule(Match.column(1), Style::right);
        // endregion
        return table;
    }
}
