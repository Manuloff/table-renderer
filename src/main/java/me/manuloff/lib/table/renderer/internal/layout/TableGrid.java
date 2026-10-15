package me.manuloff.lib.table.renderer.internal.layout;

import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.Row;
import me.manuloff.lib.table.renderer.Section;
import me.manuloff.lib.table.renderer.Table;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Сетка таблицы: строки в порядке отрисовки и ячейки, размещённые с учётом объединений
 * по алгоритму HTML (ячейка занимает первое свободное место в строке, rowspan не выходит за секцию).
 *
 * @author Manuloff
 * @since 2.0
 */
final class TableGrid {

    /**
     * Строки в порядке отрисовки: шапка, тела, подвал.
     */
    final List<Row> rows;
    /**
     * Ячейки в порядке сетки: сверху вниз, слева направо.
     */
    final List<CellBox> cells;
    final int columnCount;
    /**
     * Диапазоны строк непустых секций: [первая, последняя + 1].
     */
    private final Map<Section, int[]> sectionRows;

    private TableGrid(List<Row> rows, List<CellBox> cells, int columnCount, Map<Section, int[]> sectionRows) {
        this.rows = Collections.unmodifiableList(rows);
        this.cells = Collections.unmodifiableList(cells);
        this.columnCount = columnCount;
        this.sectionRows = sectionRows;
    }

    static TableGrid build(Table table) {
        List<Row> rows = new ArrayList<>();
        List<CellBox> cells = new ArrayList<>();
        List<BitSet> occupied = new ArrayList<>();
        Map<Section, int[]> sectionRows = new IdentityHashMap<>();
        int columnCount = 0;

        for (Section section : table.sections()) {
            List<Row> sectionRowList = section.rows();
            int count = sectionRowList.size();
            if (count > 0) sectionRows.put(section, new int[]{rows.size(), rows.size() + count});
            for (int ri = 0; ri < count; ri++) {
                Row row = sectionRowList.get(ri);
                int r = rows.size();
                rows.add(row);
                ensure(occupied, r);

                int col = 0;
                for (Cell cell : row.cells()) {
                    col = occupied.get(r).nextClearBit(col);
                    int rowspan = Math.max(1, Math.min(cell.rowspan(), count - ri));
                    int colspan = cell.colspan();
                    for (int k = r; k < r + rowspan; k++) {
                        ensure(occupied, k);
                        occupied.get(k).set(col, col + colspan);
                    }
                    cells.add(new CellBox(cell, r, col, rowspan, colspan, ri, count));
                    columnCount = Math.max(columnCount, col + colspan);
                    col += colspan;
                }
            }
        }
        return new TableGrid(rows, cells, columnCount, sectionRows);
    }

    private static void ensure(List<BitSet> occupied, int row) {
        while (occupied.size() <= row) occupied.add(new BitSet());
    }

    /**
     * @return диапазон строк секции [первая, последняя + 1] или null, если секция пуста
     */
    int[] rowRange(Section section) {
        return this.sectionRows.get(section);
    }
}
