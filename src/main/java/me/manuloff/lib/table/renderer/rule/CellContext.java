package me.manuloff.lib.table.renderer.rule;

import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.Row;
import me.manuloff.lib.table.renderer.Section;
import me.manuloff.lib.table.renderer.Table;
/**
 * Положение ячейки в таблице - передаётся в {@link CellMatcher} при применении условных правил.
 * Индексы начинаются с 0.
 *
 * @author Manuloff
 * @since 2.0
 */
public interface CellContext {

    Cell cell();

    default Row row() {
        return cell().row();
    }

    default Section section() {
        return cell().section();
    }

    default Table table() {
        return cell().table();
    }

    default Object value() {
        return cell().value();
    }

    default boolean isHeader() {
        return cell().header();
    }

    /**
     * @return индекс строки внутри секции
     */
    int rowIndex();

    /**
     * @return количество строк в секции
     */
    int rowCount();

    /**
     * @return индекс строки среди всех строк таблицы (шапка, тела, подвал - в порядке отрисовки)
     */
    int tableRowIndex();

    /**
     * @return индекс колонки, в которой начинается ячейка
     */
    int columnIndex();

    /**
     * @return количество колонок таблицы
     */
    int columnCount();

    default boolean isFirstRow() {
        return rowIndex() == 0;
    }

    default boolean isLastRow() {
        return rowIndex() + cell().rowspan() >= rowCount();
    }

    default boolean isFirstColumn() {
        return columnIndex() == 0;
    }

    default boolean isLastColumn() {
        return columnIndex() + cell().colspan() >= columnCount();
    }
}
