package me.manuloff.lib.table.renderer.internal.layout;

import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.Column;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.content.Inline;
import me.manuloff.lib.table.renderer.content.RichText;
import me.manuloff.lib.table.renderer.internal.style.Styles;
import me.manuloff.lib.table.renderer.rule.CellContext;
import me.manuloff.lib.table.renderer.rule.StyleRule;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.box.Size;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Каскад стилей ячейки и преобразование её значения в содержимое.
 * <p>
 * Порядок уровней: значения по умолчанию, заголовочная ячейка (жирный), стиль ячеек таблицы, секция,
 * колонка, строка, условные правила, сама ячейка. Фон секции, колонки и строки, а также ширина колонки
 * в каскад не входят - они обрабатываются на своём уровне (слои фона, ширина колонки).
 *
 * @author Manuloff
 * @since 2.0
 */
final class CellStyleResolver {

    private CellStyleResolver() {
    }

    static Style resolve(Table table, CellBox box, int columnCount) {
        Cell cell = box.cell();
        Style s = Styles.base();
        if (cell.header()) s.bold();
        s.apply(table.cellStyle());
        s.apply(groupLevel(cell.section().style(), false));
        Column column = table.columns().get(box.col());
        if (column != null) s.apply(groupLevel(column.style(), true));
        s.apply(groupLevel(cell.row().style(), false));
        if (!table.rules().isEmpty()) {
            CellContext ctx = new GridCellContext(box, columnCount);
            for (StyleRule rule : table.rules()) {
                if (rule.getMatcher().matches(ctx)) s.apply(rule.getStyle());
            }
        }
        s.apply(cell.style());
        return s;
    }

    /**
     * Стиль группы (секции, колонки, строки) без свойств, которые обрабатываются на уровне группы.
     */
    private static Style groupLevel(Style style, boolean column) {
        boolean hasBackground = style.background() != null || style.backgroundLayers() != null;
        boolean hasWidth = column && style.width() != null;
        if (!hasBackground && !hasWidth) return style;
        Style copy = style.copy().background((Color) null).backgroundLayers(null);
        if (column) copy.width((Size) null);
        return copy;
    }

    /**
     * Содержимое ячейки: явное форматированное содержимое или преобразованное значение.
     */
    static RichText contentOf(Table table, Cell cell) {
        if (!cell.content().isEmpty()) return cell.content();
        Object value = cell.value();
        if (value == null) return new RichText();
        // Span - одновременно RichText и Inline: его стиль должен сохраниться, поэтому сначала Inline
        if (value instanceof Inline) return new RichText().append((Inline) value);
        if (value instanceof RichText) return (RichText) value;
        if (value instanceof BufferedImage) return new RichText().image((BufferedImage) value);
        if (value instanceof CharSequence) return RichText.of(value.toString());
        String text = table.formatter().apply(value);
        return RichText.of(text == null ? "" : text);
    }
}
