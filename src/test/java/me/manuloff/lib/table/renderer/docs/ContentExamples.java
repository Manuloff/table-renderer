package me.manuloff.lib.table.renderer.docs;

import me.manuloff.lib.table.renderer.Row;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.content.RichText;
import me.manuloff.lib.table.renderer.content.Span;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.box.Shadow;
import me.manuloff.lib.table.renderer.theme.Themes;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Примеры для docs/content.md.
 */
final class ContentExamples {

    private ContentExamples() {
    }

    @Example("rich-text")
    static Table richText() {
        // region rich-text
        Table table = new Table();
        table.header("Заказ", "Состав");
        RichText order = new RichText()
                .text("Пицца ")
                .text("«Маргарита»", s -> s.italic())
                .text(" × 2").br()
                .text("Скидка 10%", s -> s.color(new Color(0x16A34A)).bold())
                .text(" — ")
                .text("1 180 ₽", s -> s.lineThrough().color(Color.GRAY))
                .bold(" 1 062 ₽");
        table.addRow("№ 1042", order).cell(0).style(Style::bold);
        // endregion
        return table;
    }

    @Example("badges")
    static Table badges() {
        // region badges
        Table table = new Table().theme(Themes.MINIMAL);
        table.header("Задача", "Метки", "Статус");
        table.addRow("Рендер таблиц", tags("java", "render"), badge("готово", 0xDCFCE7, 0x166534));
        table.addRow("Документация", tags("docs"), badge("в работе", 0xFEF3C7, 0x92400E));
        table.addRow("Тесты", tags("junit", "ci"), badge("ожидает", 0xE5E7EB, 0x374151));
        // endregion
        return table;
    }

    // region badges-helpers
    static Span badge(String text, int background, int color) {
        return Span.of(text, s -> s
                .background(new Color(background)).color(new Color(color))
                .bold().fontSize(12).padding(2, 8).borderRadius(9));
    }

    static RichText tags(String... names) {
        RichText text = new RichText();
        for (String name : names) {
            text.text(name, s -> s
                    .background(new Color(0xEEF2FF)).color(new Color(0x4338CA)).fontSize(12)
                    .padding(2, 7).borderRadius(4).border(1, new Color(0xC7D2FE)));
            text.text(" ");
        }
        return text;
    }
    // endregion

    @Example("inline-images")
    static Table inlineImages() {
        BufferedImage ru = Samples.flag(0xFFFFFF, 0x0039A6, 0xD52B1E, false);
        BufferedImage de = Samples.flag(0x000000, 0xDD0000, 0xFFCE00, false);
        BufferedImage avatar = Samples.photo(64, 64);
        // region inline-images
        Table table = new Table();
        table.header("Страна", "Флаг", "Аватар");

        Row row = table.addRow();
        row.addCell(null).content(c -> c.image(ru, 18, 12, s -> s.baselineOffset(-1)).text(" Россия"));
        row.addCell(ru);                                     // изображение как значение ячейки
        row.addCell(null).content(c -> c.image(avatar, 36, 36, s -> s
                .borderRadius(18).border(2, Color.WHITE)
                .boxShadow(Shadow.of(0, 1, 4, new Color(0, 0, 0, 80)))));

        table.addRow(new RichText().image(de, 18, 12, s -> s.baselineOffset(-1)).text(" Германия"), de, "—");
        table.cellStyle(s -> s.center());
        // endregion
        return table;
    }

    @Example("sub-sup")
    static Table subSup() {
        // region sub-sup
        Table table = new Table();
        table.header("Формула", "Описание");
        table.addRow(new RichText().text("H").text("2", s -> s.sub().fontSize(10)).text("O"), "Вода");
        table.addRow(new RichText().text("E = mc").text("2", s -> s.sup().fontSize(10)), "Эквивалентность массы и энергии");
        table.addRow(new RichText().text("x").text("i", s -> s.sub().fontSize(10).italic())
                .text("2", s -> s.sup().fontSize(10)), "Квадрат элемента");
        // endregion
        return table;
    }
}
