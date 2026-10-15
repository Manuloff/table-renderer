package me.manuloff.lib.table.renderer.docs;

import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.render.Fonts;
import me.manuloff.lib.table.renderer.render.RenderOptions;
import me.manuloff.lib.table.renderer.render.RenderedTable;
import me.manuloff.lib.table.renderer.render.TableRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Примеры для docs/rendering.md (только код).
 */
final class RenderingExamples {

    private RenderingExamples() {
    }

    static void options(Table table) {
        // region render-options
        RenderOptions options = RenderOptions.builder()
                .scale(2)                          // изображение вдвое крупнее, раскладка та же
                .background(Color.WHITE)           // фон холста (по умолчанию прозрачный)
                .availableWidth(600d)              // ширина "окна": длинный текст будет переноситься
                .fallbackFonts(List.of("Segoe UI Emoji", "Dialog"))
                .locale(Locale.forLanguageTag("ru"))
                .build();
        RenderedTable result = table.render(options);
        // endregion
    }

    static void output(Table table, OutputStream out) {
        RenderedTable result = table.render();
        // region render-output
        BufferedImage image = result.image();           // готовое изображение
        result.write(Path.of("table.png"));             // формат по расширению: png, jpg, bmp, gif
        result.write("png", out);                        // в поток (например, ответ HTTP)
        byte[] png = result.toPng();                     // в массив байтов (например, для отправки в мессенджер)
        // endregion
    }

    static void geometry(Table table) {
        // region render-geometry
        RenderedTable result = table.render();
        Cell cell = table.body().row(0).cell(1);
        Rectangle2D bounds = result.cellBounds(cell);    // где нарисована ячейка
        Cell clicked = result.cellAt(120, 45);           // какая ячейка в точке
        // endregion
    }

    static void graphics(Table table, Graphics2D graphics) {
        // region render-graphics
        // Например, SVGGraphics2D из Apache Batik или PdfBoxGraphics2D
        Rectangle2D size = TableRenderer.paint(table, graphics, RenderOptions.DEFAULT);
        // endregion
    }

    static void fonts(Table table) {
        // region render-fonts
        String family = Fonts.register(Path.of("fonts/Inter-Regular.ttf"));
        Fonts.register(Path.of("fonts/Inter-Bold.ttf"));    // начертания того же семейства
        table.cellStyle(s -> s.fontFamily(family, "SansSerif"));
        // endregion
    }
}
