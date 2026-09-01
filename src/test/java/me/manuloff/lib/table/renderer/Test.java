package me.manuloff.lib.table.renderer;

import me.manuloff.lib.table.renderer.style.*;
import me.manuloff.lib.table.renderer.style.Border;
import me.manuloff.lib.table.renderer.style.BorderStyle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Тестовый класс для демонстрации всех возможностей Table Renderer.
 *
 * @author Manuloff
 * @since 1.0
 */
public class Test {

    public static void main(String[] args) throws IOException {
        System.out.println("=== Testing Table Renderer ===\n");

        test01_BasicTable();
//        test02_SeparateBorders();
//        test03_BorderStyles();
//        test04_TableWidth();
//        test05_ColumnWidth();
//        test06_RowHeight();
//        test07_TextWrap();
//        test08_VerticalAlign();
//        test09_BoxShadow();
//        test10_BorderRadius();
//        test11_HoverStates();
//        test12_MinMaxDimensions();
//        test13_RenderQuality();
//        test14_RowGroups();
//        test15_TextStyles();
//        test16_ComplexTable();

        System.out.println("\n=== All tests completed ===");
    }

    // ============================================
    // 1. Базовая таблица
    // ============================================
    private static void test01_BasicTable() throws IOException {
        System.out.println("Test 1: Basic table");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .padding(new Padding(8, 12))
                .border(1, new Color(0xCCCCCC))
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(14)
                .textColor(Color.BLACK)
                .textAlignment(Alignment.CENTER)
                .wordWrap(false);

        table.renderQuality(RenderQuality.MAXIMUM);

        // Заполняем таблицу данными
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 4; col++) {
                table.getCell(row, col).setText("Cell [" + row + "," + col + "]");
            }
        }

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_01_basic.png"));
        System.out.println("  OK -> test_01_basic.png (" + image.getWidth() + "x" + image.getHeight() + ")");
    }

    // ============================================
    // 2. Раздельные границы (border-collapse: separate)
    // ============================================
    private static void test02_SeparateBorders() throws IOException {
        System.out.println("Test 2: Separate borders (border-collapse: separate)");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.YELLOW);

        table.textStyle()
                .fontName("Arial")
                .fontSize(20)
                .textColor(Color.WHITE)
                .textAlignment(Alignment.BOTTOM_RIGHT);

        // Стиль заголовка

        table.getColumn(0).textStyle().bold();
        table.getColumn(1).textStyle().italic();
        table.getColumn(2).textStyle().underline(true);

        // Заполняем
        table.getCell(0, 0).setText("First column");
        table.getCell(0, 1).setText("Second column");
        table.getCell(0, 2).setText("Third column");
        table.getCell(1, 0).setText("First Data");
        table.getCell(1, 1).setText("Second Data");
        table.getCell(1, 2).setText("Third Data");

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_02_separate.png"));
        System.out.println("  OK -> test_02_separate.png");
    }

    // ============================================
    // 3. Раздельные стили границ (SOLID, DASHED, DOTTED, DOUBLE)
    // ============================================
    private static void test03_BorderStyles() throws IOException {
        System.out.println("Test 3: Border styles (solid, dashed, dotted, double)");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(12)
                .textAlignment(Alignment.CENTER);

        // Разные стили границ для разных строк
        table.getCell(0, 0).setText("SOLID");
        table.getCell(0, 0).tableStyle().border(Border.all(new Border.BorderSide(BorderStyle.SOLID, 2, Color.RED)));

        table.getCell(0, 1).setText("DASHED");
        table.getCell(0, 1).tableStyle().border(Border.all(new Border.BorderSide(BorderStyle.DASHED, 2, Color.BLUE)));

        table.getCell(0, 2).setText("DOTTED");
        table.getCell(0, 2).tableStyle().border(Border.all(new Border.BorderSide(BorderStyle.DOTTED, 2, Color.GREEN)));

        table.getCell(0, 3).setText("DOUBLE");
        table.getCell(0, 3).tableStyle().border(Border.all(new Border.BorderSide(BorderStyle.DOUBLE, 3, Color.MAGENTA)));

        // NONE
        table.getCell(1, 0).setText("NONE");
        table.getCell(1, 0).tableStyle().border(Border.all(new Border.BorderSide(BorderStyle.NONE, 0, null)));
        table.getCell(1, 0).tableStyle().backgroundColor(new Color(0xFFE0E0));

        // Разные стороны
        Border.BorderSide topSide = new Border.BorderSide(BorderStyle.SOLID, 3, Color.RED);
        Border.BorderSide bottomSide = new Border.BorderSide(BorderStyle.SOLID, 3, Color.BLUE);
        Border.BorderSide leftSide = new Border.BorderSide(BorderStyle.SOLID, 3, Color.GREEN);
        Border.BorderSide rightSide = new Border.BorderSide(BorderStyle.SOLID, 3, Color.ORANGE);

        table.getCell(1, 1).setText("Custom\nBorders");
        table.getCell(1, 1).tableStyle().border(new Border(topSide, rightSide, bottomSide, leftSide));

        table.getCell(1, 2).setText("withTop");
        table.getCell(1, 2).tableStyle().border(
                new Border().withTop(new Border.BorderSide(BorderStyle.SOLID, 3, Color.RED))
                        .withBottom(new Border.BorderSide(BorderStyle.SOLID, 1, Color.GRAY))
        );

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_03_border_styles.png"));
        System.out.println("  OK -> test_03_border_styles.png");
    }

    // ============================================
    // 4. Ширина таблицы
    // ============================================
    private static void test04_TableWidth() throws IOException {
        System.out.println("Test 4: Table width");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, Color.BLACK)
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(12)
                .textAlignment(Alignment.CENTER);

        table.fixedWidth(600);

        for (int col = 0; col < 3; col++) {
            table.getCell(0, col).setText("Col " + (col + 1));
        }

        BufferedImage image = table.render().getImage();
        System.out.println("  Width: " + image.getWidth() + " (expected: 600)");
        ImageIO.write(image, "png", new File("test_04_table_width.png"));
        System.out.println("  OK -> test_04_table_width.png");
    }

    // ============================================
    // 5. Ширина колонок
    // ============================================
    private static void test05_ColumnWidth() throws IOException {
        System.out.println("Test 5: Column width");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, Color.BLACK)
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(12);

        // Фиксированная ширина колонок
        table.getColumn(0).fixedWidth(50);
        table.getColumn(1).fixedWidth(200);
        table.getColumn(2).fixedWidth(100);

        table.getCell(0, 0).setText("Narrow");
        table.getCell(0, 1).setText("This is a wider column with more content");
        table.getCell(0, 2).setText("Medium");

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_05_column_width.png"));
        System.out.println("  OK -> test_05_column_width.png (width: " + image.getWidth() + ")");
    }

    // ============================================
    // 6. Высота таблицы и строк
    // ============================================
    private static void test06_RowHeight() throws IOException {
        System.out.println("Test 6: Row height");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, Color.BLACK)
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(12);

        // Фиксированная высота строк
        table.getRow(0).fixedHeight(60);
        table.getRow(1).fixedHeight(40);

        table.getCell(0, 0).setText("Tall row");
        table.getCell(1, 0).setText("Short row");

        BufferedImage image = table.render().getImage();
        System.out.println("  Height: " + image.getHeight());
        ImageIO.write(image, "png", new File("test_06_row_height.png"));
        System.out.println("  OK -> test_06_row_height.png");
    }

    // ============================================
    // 7. Перенос и обрезка текста
    // ============================================
    private static void test07_TextWrap() throws IOException {
        System.out.println("Test 7: Text wrap and overflow");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, Color.BLACK)
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(14);

        table.getColumn(0).fixedWidth(100);

        // С переносом
        table.getCell(0, 0).setText("This is a long text that should wrap to multiple lines");
        table.getCell(0, 0).textStyle().wordWrap(true);

        // Без переноса
        table.getCell(1, 0).setText("Long text without wrap - goes beyond boundaries");
        table.getCell(1, 0).textStyle().wordWrap(false);

        // С обрезкой (ellipsis)
        table.getCell(2, 0).setText("Text with ellipsis");
        table.getCell(2, 0).textStyle().wordWrap(true).textOverflow("ellipsis");

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_07_text_wrap.png"));
        System.out.println("  OK -> test_07_text_wrap.png");
    }

    // ============================================
    // 8. Вертикальное выравнивание
    // ============================================
    private static void test08_VerticalAlign() throws IOException {
        System.out.println("Test 8: Vertical align");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, Color.BLACK)
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(12);

        table.getRow(0).fixedHeight(80);

        table.getCell(0, 0).setText("TOP");
        table.getCell(0, 0).textStyle().verticalAlign(VerticalAlign.TOP);
        table.getCell(0, 0).tableStyle().backgroundColor(new Color(0xE3F2FD));

        table.getCell(0, 1).setText("MIDDLE");
        table.getCell(0, 1).textStyle().verticalAlign(VerticalAlign.MIDDLE);
        table.getCell(0, 1).tableStyle().backgroundColor(new Color(0xE8F5E9));

        table.getCell(0, 2).setText("BOTTOM");
        table.getCell(0, 2).textStyle().verticalAlign(VerticalAlign.BOTTOM);
        table.getCell(0, 2).tableStyle().backgroundColor(new Color(0xFFF3E0));

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_08_vertical_align.png"));
        System.out.println("  OK -> test_08_vertical_align.png");
    }

    // ============================================
    // 9. Тени (box-shadow)
    // ============================================
    private static void test09_BoxShadow() throws IOException {
        System.out.println("Test 9: Box shadow");

        Table table = new Table();

        BoxShadow shadow = new BoxShadow()
                .add(0, 4, 8, 0, new Color(0, 0, 0, 80))
                .add(0, 8, 16, 0, new Color(0, 0, 0, 40));

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .boxShadow(shadow)
                .border(1, new Color(0xCCCCCC))
                .borderRadius(8f);

        table.textStyle()
                .fontName("Arial")
                .fontSize(14)
                .textAlignment(Alignment.CENTER);

        for (int col = 0; col < 3; col++) {
            table.getCell(0, col).setText("Shadow " + (col + 1));
        }

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_09_box_shadow.png"));
        System.out.println("  OK -> test_09_box_shadow.png");
    }

    // ============================================
    // 10. Скруглённые углы
    // ============================================
    private static void test10_BorderRadius() throws IOException {
        System.out.println("Test 10: Border radius");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, Color.BLACK)
                .borderRadius(16f);

        table.textStyle()
                .fontName("Arial")
                .fontSize(14)
                .textAlignment(Alignment.CENTER);

        table.getCell(0, 0).setText("Rounded Table");
        table.getCell(0, 0).tableStyle().borderRadius(8f);

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_10_border_radius.png"));
        System.out.println("  OK -> test_10_border_radius.png");
    }

    // ============================================
    // 11. Hover/Focus/Active состояния
    // ============================================
    private static void test11_HoverStates() throws IOException {
        System.out.println("Test 11: Hover/Focus/Active states");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, new Color(0xEEEEEE))
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(13);

        // Заголовок
        for (int col = 0; col < 3; col++) {
            table.getCell(0, col).setText("Header " + (col + 1));
        }
        table.getRow(0).tableStyle().backgroundColor(new Color(0x607D8B));
        table.getRow(0).textStyle().textColor(Color.WHITE).bold(true);

        // Hover состояние
        table.getCell(1, 0).setText("Hover Me");
        table.getCell(1, 0).hovered(true);
        table.getCell(1, 0).hoverStyle().backgroundColor(new Color(0xE3F2FD));
        table.getCell(1, 0).hoverStyle().border(
				Border.all(new Border.BorderSide(BorderStyle.SOLID, 2, new Color(0x2196F3))));

        // Focus состояние
        table.getCell(1, 1).setText("Focused");
        table.getCell(1, 1).focused(true);
        table.getCell(1, 1).focusStyle().backgroundColor(new Color(0xFFF8E1));
        table.getCell(1, 1).focusStyle().border(
				Border.all(new Border.BorderSide(BorderStyle.SOLID, 3, new Color(0xFFC107))));

        // Active состояние
        table.getCell(1, 2).setText("Active");
        table.getCell(1, 2).active(true);
        table.getCell(1, 2).activeStyle().backgroundColor(new Color(0xFFEBEE));
        table.getCell(1, 2).activeStyle().border(
				Border.all(new Border.BorderSide(BorderStyle.SOLID, 2, new Color(0xF44336))));

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_11_hover_states.png"));
        System.out.println("  OK -> test_11_hover_states.png");
    }

    // ============================================
    // 12. Min/Max dimensions для ячеек
    // ============================================
    private static void test12_MinMaxDimensions() throws IOException {
        System.out.println("Test 12: Min/Max dimensions");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, Color.BLACK)
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(12)
                .textAlignment(Alignment.CENTER);

        // Ячейка с minWidth
        table.getCell(0, 0).setText("minW=150");
        table.getCell(0, 0).minWidth(150);

        // Ячейка с maxWidth
        table.getCell(0, 1).setText("Long text with maxWidth=80 should be constrained");
        table.getCell(0, 1).maxWidth(80);

        // Ячейка с minHeight
        table.getCell(1, 0).setText("minH=60");
        table.getCell(1, 0).minHeight(60);
        table.getCell(1, 0).tableStyle().backgroundColor(new Color(0xE8F5E9));

        // Ячейка с maxHeight
        table.getCell(1, 1).setText("Cell with maxHeight=30");
        table.getCell(1, 1).maxHeight(30);
        table.getCell(1, 1).tableStyle().backgroundColor(new Color(0xFFEBEE));

        // Фиксированные размеры
        table.getCell(2, 0).setText("fixed 100x60");
        table.getCell(2, 0).fixedWidth(100);
        table.getCell(2, 0).fixedHeight(60);
        table.getCell(2, 0).tableStyle().backgroundColor(new Color(0xE3F2FD));

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_12_min_max.png"));
        System.out.println("  OK -> test_12_min_max.png");
    }

    // ============================================
    // 13. Пресеты качества рендеринга
    // ============================================
    private static void test13_RenderQuality() throws IOException {
        System.out.println("Test 13: Render quality presets");

        for (RenderQuality quality : Arrays.asList(
                RenderQuality.LOW,
                RenderQuality.MEDIUM,
                RenderQuality.HIGH,
                RenderQuality.MAXIMUM)) {

            Table table = new Table();
            table.renderQuality(quality);

            table.tableStyle()
                    .backgroundColor(Color.WHITE)
                    .border(1, Color.BLACK)
                    .borderCollapse(BorderCollapse.COLLAPSE);

            table.textStyle()
                    .fontName("Arial")
                    .fontSize(14)
                    .textAlignment(Alignment.CENTER);

            table.getCell(0, 0).setText("Quality: " + quality.name());

            long start = System.currentTimeMillis();
            BufferedImage image = table.render().getImage();
            long time = System.currentTimeMillis() - start;

            String filename = "test_13_quality_" + quality.name().toLowerCase() + ".png";
            ImageIO.write(image, "png", new File(filename));
            System.out.println("  " + quality.name() + ": " + image.getWidth() + "x" + image.getHeight() + " in " + time + "ms -> " + filename);
        }
        System.out.println("  OK -> test_13_quality_*.png");
    }

    // ============================================
    // 14. Row groups (thead/tbody/tfoot)
    // ============================================
    private static void test14_RowGroups() throws IOException {
        System.out.println("Test 14: Row groups (thead/tbody/tfoot)");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, Color.BLACK)
                .borderCollapse(BorderCollapse.COLLAPSE);

        table.textStyle()
                .fontName("Arial")
                .fontSize(13)
                .textAlignment(Alignment.CENTER);

        // Заполняем данными
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 3; col++) {
                table.getCell(row, col).setText("Row " + row + ", Col " + col);
            }
        }

        // Настраиваем группы строк
        table.autoConfigureRowGroups(1, 1); // 1 header, 1 footer

        // Стили для thead
        table.thead()
                .tableStyle()
                .backgroundColor(new Color(0x3F51B5));
        table.thead().textStyle()
                .textColor(Color.WHITE)
                .bold(true);

        // Стили для tbody
        table.tbody()
                .tableStyle()
                .backgroundColor(Color.WHITE);

        // Стили для tfoot
        table.tfoot()
                .tableStyle()
                .backgroundColor(new Color(0xFFC107));
        table.tfoot()
                .textStyle()
                .textColor(Color.BLACK)
                .bold(true);

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_14_row_groups.png"));
        System.out.println("  OK -> test_14_row_groups.png");
    }

    // ============================================
    // 15. Text styles (bold, italic, underline, strikethrough)
    // ============================================
    private static void test15_TextStyles() throws IOException {
        System.out.println("Test 15: Text styles");

        Table table = new Table();

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, Color.BLACK)
                .borderCollapse(BorderCollapse.COLLAPSE)
                .padding(new Padding(8));

        table.textStyle()
                .fontName("Arial")
                .fontSize(14);

        // Bold
        table.getCell(0, 0).setText("Bold Text");
        table.getCell(0, 0).textStyle().bold(true);

        // Italic
        table.getCell(0, 1).setText("Italic Text");
        table.getCell(0, 1).textStyle().italic(true);

        // Bold + Italic
        table.getCell(0, 2).setText("Bold Italic");
        table.getCell(0, 2).textStyle().bold(true).italic(true);

        // Underline
        table.getCell(1, 0).setText("Underlined");
        table.getCell(1, 0).textStyle().underline(true);

        // Strikethrough
        table.getCell(1, 1).setText("Strikethrough");
        table.getCell(1, 1).textStyle().strikethrough(true);

        // Different colors
        table.getCell(1, 2).setText("Red Text");
        table.getCell(1, 2).textStyle().textColor(Color.RED);

        // Line height
        table.getCell(2, 0).setText("Line height 1.5\nSecond line\nThird line");
        table.getCell(2, 0).textStyle().lineHeight(1.5f);
        table.getCell(2, 0).fixedHeight(100);
        table.getCell(2, 0).tableStyle().backgroundColor(new Color(0xFFF8E1));

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_15_text_styles.png"));
        System.out.println("  OK -> test_15_text_styles.png");
    }

    // ============================================
    // 16. Комплексная таблица (все функции вместе)
    // ============================================
    private static void test16_ComplexTable() throws IOException {
        System.out.println("Test 16: Complex table (all features)");

        Table table = new Table();

        // Стиль таблицы
        BoxShadow shadow = new BoxShadow()
                .add(0, 3, 6, 0, new Color(0, 0, 0, 50));

        table.tableStyle()
                .backgroundColor(Color.WHITE)
                .border(1, new Color(0xDDDDDD))
                .borderCollapse(BorderCollapse.COLLAPSE)
                .borderRadius(8f)
                .boxShadow(shadow)
                .padding(new Padding(4));

        // Глобальные текстовые стили
        table.textStyle()
                .fontName("Arial")
                .fontSize(12)
                .textAlignment(Alignment.CENTER);

        // Заголовок
        String[] headers = {"#", "Name", "Email", "Status"};
        for (int col = 0; col < headers.length; col++) {
            table.getCell(0, col).setText(headers[col]);
        }

        // Стиль заголовка
        table.getRow(0).tableStyle()
                .backgroundColor(new Color(0x1976D2))
                .borderRadius(8f);
        table.getRow(0).textStyle()
                .textColor(Color.WHITE)
                .bold(true);

        // Настройка ширины колонок
        table.getColumn(0).fixedWidth(40);
        table.getColumn(1).fixedWidth(150);
        table.getColumn(2).fixedWidth(200);
        table.getColumn(3).fixedWidth(80);

        // Данные
        String[][] data = {
                {"1", "Ivan Petrov", "ivan@example.com", "Active"},
                {"2", "Maria Sidorova", "maria@example.com", "Active"},
                {"3", "Alexey Volkov", "alex@example.com", "Inactive"},
        };

        for (int row = 1; row <= data.length; row++) {
            for (int col = 0; col < data[row - 1].length; col++) {
                table.getCell(row, col).setText(data[row - 1][col]);
            }

            // Hover эффект для строк
            final int currentRow = row;
            table.getRow(row).tableStyle()
                    .hoverBackgroundColor(new Color(0xE3F2FD));
        }

        // Последняя строка - footer
        int footerRow = data.length + 1;
        for (int col = 0; col < 4; col++) {
            table.getCell(footerRow, col).setText("Footer " + (col + 1));
        }
        table.getRow(footerRow).tableStyle().backgroundColor(new Color(0xFFC107));
        table.getRow(footerRow).textStyle().bold(true);

        // Row groups
        table.autoConfigureRowGroups(1, 1);

        // Качество
        table.renderQuality(RenderQuality.HIGH);

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("test_16_complex.png"));
        System.out.println("  OK -> test_16_complex.png (" + image.getWidth() + "x" + image.getHeight() + ")");
    }
}
