package me.manuloff.lib.table.renderer.docs;

import me.manuloff.lib.table.renderer.Row;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.style.background.ColorStop;
import me.manuloff.lib.table.renderer.style.background.Gradient;
import me.manuloff.lib.table.renderer.style.background.ImageLayer;
import me.manuloff.lib.table.renderer.style.background.RadialGradient;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.box.Shadow;
import me.manuloff.lib.table.renderer.theme.Themes;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Примеры для docs/backgrounds.md.
 */
final class BackgroundExamples {

    private BackgroundExamples() {
    }

    @Example("gradients")
    static Table gradients() {
        // region gradients
        Table table = new Table().theme(Themes.PLAIN);
        table.style(s -> s.separate(8));
        table.cellStyle(s -> s.padding(26, 14).center().bold().color(Color.WHITE).borderRadius(8).width(140));

        Row row = table.addRow("linear 135°", "horizontal", "radial", "circle");
        row.cell(0).style(s -> s.background(Gradient.linear(135, new Color(0xF97316), new Color(0xDB2777))));
        row.cell(1).style(s -> s.background(Gradient.horizontal(
                new Color(0x06B6D4), new Color(0x3B82F6), new Color(0x8B5CF6))));
        row.cell(2).style(s -> s.background(Gradient.radial(
                new Color(0xFDE047), new Color(0xF97316), new Color(0x7C2D12))));
        row.cell(3).style(s -> s.background(Gradient.circle(0.3, 0.3, RadialGradient.Extent.FARTHEST_CORNER,
                ColorStop.of(new Color(0xA7F3D0)), ColorStop.of(new Color(0x065F46)))));

        row = table.addRow("conic", "полосы", "флаг", "слои");
        row.cell(0).style(s -> s.background(Gradient.conic(
                Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED)));
        row.cell(1).style(s -> s.background(Gradient.repeatingLinear(45,
                ColorStop.of(new Color(0x334155), 0), ColorStop.of(new Color(0x334155), 0.05),
                ColorStop.of(new Color(0x475569), 0.05), ColorStop.of(new Color(0x475569), 0.1))));
        row.cell(2).style(s -> s.color(Color.BLACK).background(Gradient.horizontal(
                ColorStop.of(new Color(0x0055A4), 0), ColorStop.of(new Color(0x0055A4), 1 / 3.0),
                ColorStop.of(Color.WHITE, 1 / 3.0), ColorStop.of(Color.WHITE, 2 / 3.0),
                ColorStop.of(new Color(0xEF4135), 2 / 3.0), ColorStop.of(new Color(0xEF4135), 1))));
        row.cell(3).style(s -> s.background(new Color(0x0F172A),
                Gradient.radial(ColorStop.of(new Color(255, 255, 255, 90), 0), ColorStop.of(new Color(255, 255, 255, 0), 0.6)),
                Gradient.vertical(new Color(0x7C3AED), new Color(0x0F172A))));
        // endregion
        return table;
    }

    @Example("image-backgrounds")
    static Table imageBackgrounds() {
        BufferedImage pattern = Samples.checker(12, new Color(0xE2E8F0), new Color(0xCBD5E1));
        BufferedImage photo = Samples.photo(200, 120);
        // region image-backgrounds
        Table table = new Table().theme(Themes.PLAIN);
        table.style(s -> s.separate(8));
        table.cellStyle(s -> s.padding(30, 14).center().bold().color(Color.WHITE).borderRadius(8).width(140));

        Row row = table.addRow("плитка", "cover", "contain", "своя позиция");
        row.cell(0).style(s -> s.background(ImageLayer.tiled(pattern)).color(new Color(0x334155)));
        row.cell(1).style(s -> s.background(ImageLayer.cover(photo)));
        row.cell(2).style(s -> s.background(new Color(0x1E293B), ImageLayer.contain(photo)));
        row.cell(3).style(s -> s.background(new Color(0x1E293B), ImageLayer.builder()
                .image(photo).width(60d).alignX(1).alignY(1).repeat(false).build()));
        // endregion
        return table;
    }

    @Example("row-backgrounds")
    static Table rowBackgrounds() {
        // region row-backgrounds
        Table table = new Table().theme(Themes.PLAIN);
        table.cellStyle(s -> s.padding(8, 14));
        // Фон секции рисуется одним слоем - градиент идёт через всю шапку
        table.head().style(s -> s.bold().color(Color.WHITE)
                .background(Gradient.horizontal(new Color(0x0EA5E9), new Color(0x6366F1))));
        table.header("Задача", "Исполнитель", "Срок");
        table.addRow("Макет", "Анна", "12.05");
        table.addRow("Вёрстка", "Пётр", "20.05")
                .style(s -> s.background(Gradient.horizontal(new Color(0xFEF3C7), new Color(0xFFFFFF))));
        table.addRow("Тесты", "Олег", "27.05");
        // endregion
        return table;
    }

    @Example("shadows")
    static Table shadows() {
        // region shadows
        Table table = new Table().theme(Themes.PLAIN);
        table.style(s -> s.separate(24).padding(8).margin(8));
        table.cellStyle(s -> s.padding(22, 18).center().background(Color.WHITE).borderRadius(10).width(140));

        Row row = table.addRow("мягкая", "жёсткая", "внутренняя", "несколько");
        row.cell(0).style(s -> s.boxShadow(Shadow.of(0, 4, 12, new Color(0, 0, 0, 60))));
        row.cell(1).style(s -> s.border(2, new Color(0x1E293B)).boxShadow(Shadow.of(6, 6, 0, new Color(0x1E293B))));
        row.cell(2).style(s -> s.background(new Color(0xE2E8F0))
                .boxShadow(Shadow.inset(0, 3, 8, 0, new Color(0, 0, 0, 90))));
        row.cell(3).style(s -> s.boxShadow(
                Shadow.of(0, 0, 0, 3, new Color(0x6366F1)),
                Shadow.of(0, 10, 25, -4, new Color(0x6366F1))));
        // endregion
        return table;
    }

    @Example("effects")
    static Table effects() {
        BufferedImage pattern = Samples.checker(8, new Color(0xF1F5F9), Color.WHITE);
        // region effects
        Table table = new Table().theme(Themes.PLAIN);
        table.style(s -> s.separate(12).padding(6).background(ImageLayer.tiled(pattern)));
        table.cellStyle(s -> s.padding(14).center().bold().background(new Color(0x3B82F6)).color(Color.WHITE));

        Row row = table.addRow("opacity 1", "opacity 0.5", "hidden", "outline");
        row.cell(1).style(s -> s.opacity(0.5));
        row.cell(2).style(s -> s.hidden(true));       // место сохраняется
        row.cell(3).style(s -> s.outline(BorderSide.dashed(2, new Color(0xEF4444))).outlineOffset(3));
        // endregion
        return table;
    }
}
