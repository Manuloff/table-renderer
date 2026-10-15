package me.manuloff.lib.table.renderer.internal.layout;

import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.rule.CellContext;

/**
 * Положение ячейки в сетке для условных правил.
 *
 * @author Manuloff
 * @since 2.0
 */
final class GridCellContext implements CellContext {

    private final CellBox box;
    private final int columnCount;

    GridCellContext(CellBox box, int columnCount) {
        this.box = box;
        this.columnCount = columnCount;
    }

    @Override
    public Cell cell() {
        return this.box.cell();
    }

    @Override
    public int rowIndex() {
        return this.box.sectionRowIndex();
    }

    @Override
    public int rowCount() {
        return this.box.sectionRowCount();
    }

    @Override
    public int tableRowIndex() {
        return this.box.row();
    }

    @Override
    public int columnIndex() {
        return this.box.col();
    }

    @Override
    public int columnCount() {
        return this.columnCount;
    }
}
