package me.manuloff.lib.table.renderer;

import me.manuloff.lib.table.renderer.render.RenderOptions;
import me.manuloff.lib.table.renderer.render.RenderedTable;
import me.manuloff.lib.table.renderer.rule.Match;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.box.Insets;
import me.manuloff.lib.table.renderer.style.table.CaptionSide;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableRenderTest {

    private static final RenderOptions OPTIONS = RenderOptions.DEFAULT;

    @Test
    void emptyTableRenders() {
        RenderedTable r = new Table().render();
        assertTrue(r.image().getWidth() >= 1);
        assertTrue(r.image().getHeight() >= 1);
    }

    @Test
    void scaleMultipliesImageSize() {
        Table t = Table.of(new Object[]{"A", "B"}, new Object[][]{{1, 2}, {3, 4}});
        RenderedTable r1 = t.render(OPTIONS);
        RenderedTable r2 = t.render(RenderOptions.builder().scale(2).build());
        assertEquals(r1.image().getWidth() * 2, r2.image().getWidth(), 2);
        assertEquals(r1.width(), r2.width(), 1);
    }

    @Test
    void cellsArePlacedWithSpans() {
        Table t = new Table();
        Row r0 = t.addRow();
        Cell a = r0.addCell("A").rowspan(2);
        Cell b = r0.addCell("B").colspan(2);
        Row r1 = t.addRow();
        Cell c = r1.addCell("C");
        Cell d = r1.addCell("D");

        RenderedTable r = t.render(OPTIONS);
        Rectangle2D ra = r.cellBounds(a);
        Rectangle2D rb = r.cellBounds(b);
        Rectangle2D rc = r.cellBounds(c);
        Rectangle2D rd = r.cellBounds(d);

        // A занимает две строки, C и D во второй строке начинаются правее A
        assertEquals(ra.getMaxY(), rc.getMaxY(), 0.01);
        assertEquals(ra.getMaxX(), rc.getX(), 0.01);
        assertEquals(rc.getMaxX(), rd.getX(), 0.01);
        // B занимает две колонки - над C и D
        assertEquals(rc.getX(), rb.getX(), 0.01);
        assertEquals(rd.getMaxX(), rb.getMaxX(), 0.01);
    }

    @Test
    void rowspanDoesNotCrossSection() {
        Table t = new Table();
        Cell h = t.header("H", "X").cell(0).rowspan(5);
        Cell body = t.addRow("B", "Y").cell(0);
        RenderedTable r = t.render(OPTIONS);
        assertTrue(r.cellBounds(h).getMaxY() <= r.cellBounds(body).getY() + 0.01);
    }

    @Test
    void fixedColumnWidthIsRespected() {
        Table t = new Table();
        t.column(0).width(200);
        Cell c = t.addRow("short", "x").cell(0);
        assertEquals(200, t.render(OPTIONS).cellBounds(c).getWidth(), 1);
    }

    @Test
    void tableWidthIsRespected() {
        Table t = new Table();
        t.style(s -> s.width(500));
        t.addRow("a", "b", "c");
        assertEquals(500, t.render(OPTIONS).tableBounds().getWidth(), 1);
    }

    @Test
    void percentTableWidthUsesAvailableWidth() {
        Table t = new Table();
        t.style(s -> s.widthPercent(50));
        t.addRow("a", "b");
        RenderedTable r = t.render(RenderOptions.builder().availableWidth(800d).build());
        assertEquals(400, r.tableBounds().getWidth(), 1);
    }

    @Test
    void availableWidthWrapsLongText() {
        Table t = new Table();
        Cell c = t.addRow("слово ".repeat(100)).cell(0);
        RenderedTable r = t.render(RenderOptions.builder().availableWidth(300d).build());
        assertTrue(r.tableBounds().getWidth() <= 300.5);
        assertTrue(r.cellBounds(c).getHeight() > 100, "Text must wrap into several lines");
    }

    @Test
    void longTextIsNotWrappedWithoutConstraints() {
        Table t = new Table();
        Cell c = t.addRow("Груши очень длинное название товара").cell(0);
        Cell single = t.addRow("x").cell(0);
        RenderedTable r = t.render(OPTIONS);
        assertEquals(r.cellBounds(single).getHeight(), r.cellBounds(c).getHeight(), 0.01);
    }

    @Test
    void ellipsisDoesNotWidenColumn() {
        Table t = new Table();
        Cell c = t.addRow("Очень длинный текст ".repeat(20)).cell(0).style(s -> s.ellipsis().width(150));
        assertEquals(150, t.render(OPTIONS).cellBounds(c).getWidth(), 1);
    }

    @Test
    void marginAddsSpaceAroundTable() {
        Table t = new Table();
        t.style(s -> s.margin(Insets.of(10, 20, 30, 40)));
        t.addRow("a");
        RenderedTable r = t.render(OPTIONS);
        Rectangle2D tb = r.tableBounds();
        assertEquals(40, tb.getX(), 0.01);
        assertEquals(10, tb.getY(), 0.01);
        assertEquals(tb.getWidth() + 60, r.width(), 1);
        assertEquals(tb.getHeight() + 40, r.height(), 1);
    }

    @Test
    void captionSide() {
        Table t = new Table().caption("Заголовок");
        t.addRow("a");
        RenderedTable top = t.render(OPTIONS);
        assertTrue(top.captionBounds().getMaxY() <= top.tableBounds().getY() + 0.01);

        t.style(s -> s.captionSide(CaptionSide.BOTTOM));
        RenderedTable bottom = t.render(OPTIONS);
        assertTrue(bottom.captionBounds().getY() >= bottom.tableBounds().getMaxY() - 0.01);

        assertNull(new Table().render().captionBounds());
    }

    @Test
    void shadowExpandsCanvas() {
        Table t = new Table();
        t.style(s -> s.boxShadow(me.manuloff.lib.table.renderer.style.box.Shadow.of(0, 0, 20, Color.BLACK)));
        t.addRow("a");
        RenderedTable r = t.render(OPTIONS);
        assertTrue(r.tableBounds().getX() > 10, "Canvas must include the shadow");
    }

    @Test
    void backgroundsArePainted() {
        Table t = new Table();
        t.cellStyle(s -> s.noBorder().padding(10));
        Cell c = t.addRow("").cell(0).style(s -> s.background(Color.RED).width(40).height(40));
        RenderedTable r = t.render(OPTIONS);
        Rectangle2D b = r.cellBounds(c);
        int rgb = r.image().getRGB((int) b.getCenterX(), (int) b.getCenterY());
        assertEquals(0xFFFF0000, rgb);
    }

    @Test
    void rowBackgroundSpansCells() {
        Table t = new Table();
        t.cellStyle(s -> s.noBorder());
        Row row = t.addRow("", "").style(s -> s.background(Color.BLUE).height(30));
        row.cell(0).style(s -> s.width(30));
        row.cell(1).style(s -> s.width(30));
        RenderedTable r = t.render(OPTIONS);
        Rectangle2D b = r.cellBounds(row.cell(1));
        assertEquals(0xFF0000FF, r.image().getRGB((int) b.getCenterX(), (int) b.getCenterY()));
    }

    @Test
    void rulesApplyInOrderAndCellWins() {
        Table t = new Table();
        t.cellStyle(s -> s.noBorder());
        t.rule(Match.all(), s -> s.background(Color.RED));
        t.rule(Match.column(1), s -> s.background(Color.GREEN));
        Row row = t.addRow("", "", "");
        for (Cell c : row.cells()) c.style(s -> s.width(20).height(20));
        row.cell(2).style(s -> s.background(Color.BLUE));
        RenderedTable r = t.render(OPTIONS);
        int[] expected = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF};
        for (int i = 0; i < 3; i++) {
            Rectangle2D b = r.cellBounds(row.cell(i));
            assertEquals(expected[i], r.image().getRGB((int) b.getCenterX(), (int) b.getCenterY()), "cell " + i);
        }
    }

    @Test
    void matchContext() {
        Table t = new Table();
        t.header("h1", "h2");
        for (int i = 0; i < 4; i++) t.addRow(i, i);
        List<Integer> seen = new java.util.ArrayList<>();
        t.rule(Match.body().and(Match.evenRows()).and(Match.firstColumn()), s -> s.bold());
        t.rule(c -> {
            if (c.section().kind() == Section.Kind.BODY && c.isFirstColumn()) seen.add(c.rowIndex());
            return false;
        }, s -> {
        });
        t.render(OPTIONS);
        assertEquals(List.of(0, 1, 2, 3), seen);
    }

    @Test
    void collapsedBorderWiderWins() {
        Table t = new Table();
        t.cellStyle(s -> s.border(BorderSide.solid(1, Color.BLACK)).padding(10));
        Row row = t.addRow("a", "b");
        row.cell(1).style(s -> s.borderLeft(BorderSide.solid(6, Color.RED)));
        RenderedTable r = t.render(OPTIONS);
        Rectangle2D b = r.cellBounds(row.cell(1));
        // Общая рамка между ячейками - красная толщиной 6, по центру линии
        int rgb = r.image().getRGB((int) Math.round(b.getX()), (int) b.getCenterY());
        assertEquals(0xFFFF0000, rgb);
    }

    @Test
    void cellAt() {
        Table t = new Table();
        Cell c = t.addRow("a", "b").cell(1);
        RenderedTable r = t.render(OPTIONS);
        Rectangle2D b = r.cellBounds(c);
        assertEquals(c, r.cellAt(b.getCenterX(), b.getCenterY()));
        assertNull(r.cellAt(-5, -5));
    }

    @Test
    void writesFormats() {
        RenderedTable r = Table.of(new Object[]{"A"}, new Object[][]{{1}}).render();
        assertTrue(r.toPng().length > 0);
        assertTrue(r.toBytes("jpg").length > 0);
        assertThrows(IllegalArgumentException.class, () -> r.toBytes("nope"));
    }

    @Test
    void builderCreatesColumns() {
        Table t = Table.from(List.of("one", "three"))
                .column("Value", s -> s)
                .column("Length", String::length)
                .footer("Total", 8)
                .build();
        assertEquals(1, t.head().rows().size());
        assertEquals(2, t.body().rows().size());
        assertEquals(5, t.body().row(1).cell(1).value());
        assertEquals(1, t.foot().rows().size());
        assertNotNull(t.render());
    }

    @Test
    void formatterIsUsedForValues() {
        Table t = new Table().formatter(v -> "#" + v);
        t.addRow(42);
        assertNotNull(t.render());
    }

    @Test
    void imagesAsValues() {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Table t = new Table();
        Cell c = t.addRow(img).cell(0).style(s -> s.padding(0).noBorder());
        RenderedTable r = t.render(OPTIONS);
        assertTrue(r.cellBounds(c).getWidth() >= 10);
    }

    @Test
    void invalidArguments() {
        Table t = new Table();
        assertThrows(IllegalArgumentException.class, () -> t.addRow("a").cell(0).colspan(0));
        assertThrows(IllegalArgumentException.class, () -> t.column(-1));
        assertThrows(IllegalArgumentException.class, () -> new me.manuloff.lib.table.renderer.style.Style().opacity(2));
        assertThrows(IllegalArgumentException.class, () -> RenderOptions.builder().scale(0).build());
    }

    @Test
    void themeReplacesPreviousThemeRules() {
        Table t = new Table();
        t.rule(Match.all(), s -> s.bold());
        t.theme(me.manuloff.lib.table.renderer.theme.Themes.STRIPED);
        assertEquals(2, t.rules().size());
        t.theme(me.manuloff.lib.table.renderer.theme.Themes.MINIMAL);
        assertEquals(1, t.rules().size(), "Only the user rule must remain");
    }

    @Test
    void spanValueKeepsItsStyle() {
        Table t = new Table();
        t.cellStyle(s -> s.noBorder().padding(0));
        Cell c = t.addRow(me.manuloff.lib.table.renderer.content.Span.of("    ", s -> s.background(Color.RED)
                .whiteSpace(me.manuloff.lib.table.renderer.style.text.WhiteSpace.PRE))).cell(0);
        RenderedTable r = t.render(OPTIONS);
        Rectangle2D b = r.cellBounds(c);
        assertEquals(0xFFFF0000, r.image().getRGB((int) b.getCenterX(), (int) b.getCenterY()));
    }

    @Test
    void columnWidthAppliesToColumnNotSpanningCell() {
        Table t = new Table();
        t.column(0).width(100);
        Cell wide = t.addRow("a").cell(0).colspan(2);
        Cell first = t.addRow("x", "y").cell(0);
        RenderedTable r = t.render(OPTIONS);
        assertEquals(100, r.cellBounds(first).getWidth(), 1);
        assertTrue(r.cellBounds(wide).getWidth() > 100);
    }

    @Test
    void captionWidensEmptyTable() {
        Table t = new Table().caption("Довольно длинный заголовок таблицы");
        RenderedTable r = t.render(OPTIONS);
        assertTrue(r.tableBounds().getWidth() > 40, "Table must fit the longest caption word");
        assertEquals(r.tableBounds().getWidth(), r.captionBounds().getWidth(), 0.01);
    }
}
