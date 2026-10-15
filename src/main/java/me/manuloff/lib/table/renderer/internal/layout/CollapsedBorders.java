package me.manuloff.lib.table.renderer.internal.layout;

import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.border.BorderStyle;

/**
 * Общие рамки модели {@code border-collapse: collapse}: для каждого отрезка сетки выбирается одна рамка
 * из претендентов (рамки соседних ячеек и таблицы) по правилам CSS 2.1, п. 17.6.2.1:
 * <ol>
 *     <li>{@link BorderStyle#HIDDEN} побеждает всегда, {@link BorderStyle#NONE} - проигрывает всегда;</li>
 *     <li>побеждает более толстая рамка;</li>
 *     <li>при равной толщине - по стилю: double, solid, dashed, dotted, ridge, outset, groove, inset;</li>
 *     <li>при равном стиле рамка ячейки сильнее рамки таблицы;</li>
 *     <li>при полном равенстве - левая/верхняя ячейка.</li>
 * </ol>
 *
 * @author Manuloff
 * @since 2.0
 */
public final class CollapsedBorders {

    private static final int ORIGIN_TABLE = 0;
    private static final int ORIGIN_CELL = 1;

    private final int rows;
    private final int cols;
    /**
     * Горизонтальные отрезки: [линия 0..rows][колонка 0..cols-1].
     */
    private final BorderSide[][] horizontal;
    /**
     * Вертикальные отрезки: [строка 0..rows-1][линия 0..cols].
     */
    private final BorderSide[][] vertical;
    private final int[][] horizontalOrigin;
    private final int[][] verticalOrigin;

    CollapsedBorders(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.horizontal = new BorderSide[rows + 1][cols];
        this.vertical = new BorderSide[rows][cols + 1];
        this.horizontalOrigin = new int[rows + 1][cols];
        this.verticalOrigin = new int[rows][cols + 1];
    }

    /**
     * @return количество строк сетки
     */
    public int rows() {
        return this.rows;
    }

    /**
     * @return количество колонок сетки
     */
    public int cols() {
        return this.cols;
    }

    /**
     * @return рамка горизонтального отрезка на линии {@code line} в колонке {@code col} (может быть null)
     */
    public BorderSide horizontal(int line, int col) {
        return this.horizontal[line][col];
    }

    /**
     * @return рамка вертикального отрезка в строке {@code row} на линии {@code line} (может быть null)
     */
    public BorderSide vertical(int row, int line) {
        return this.vertical[row][line];
    }

    // ============================================
    // Разрешение конфликтов
    // ============================================

    /**
     * Ячейки добавляются в порядке сетки (сверху вниз, слева направо): при полном равенстве
     * остаётся рамка, добавленная раньше, - левая или верхняя.
     */
    void addCell(CellBox c) {
        for (int k = c.col(); k < c.col() + c.colspan(); k++) {
            offerHorizontal(c.row(), k, c.style.borderTop(), ORIGIN_CELL);
            offerHorizontal(c.row() + c.rowspan(), k, c.style.borderBottom(), ORIGIN_CELL);
        }
        for (int k = c.row(); k < c.row() + c.rowspan(); k++) {
            offerVertical(k, c.col(), c.style.borderLeft(), ORIGIN_CELL);
            offerVertical(k, c.col() + c.colspan(), c.style.borderRight(), ORIGIN_CELL);
        }
    }

    void addTable(TableStyle style) {
        for (int c = 0; c < this.cols; c++) {
            offerHorizontal(0, c, style.borderTop(), ORIGIN_TABLE);
            offerHorizontal(this.rows, c, style.borderBottom(), ORIGIN_TABLE);
        }
        for (int r = 0; r < this.rows; r++) {
            offerVertical(r, 0, style.borderLeft(), ORIGIN_TABLE);
            offerVertical(r, this.cols, style.borderRight(), ORIGIN_TABLE);
        }
    }

    private void offerHorizontal(int line, int col, BorderSide side, int origin) {
        if (wins(side, origin, this.horizontal[line][col], this.horizontalOrigin[line][col])) {
            this.horizontal[line][col] = side;
            this.horizontalOrigin[line][col] = origin;
        }
    }

    private void offerVertical(int row, int line, BorderSide side, int origin) {
        if (wins(side, origin, this.vertical[row][line], this.verticalOrigin[row][line])) {
            this.vertical[row][line] = side;
            this.verticalOrigin[row][line] = origin;
        }
    }

    private static boolean wins(BorderSide candidate, int candidateOrigin, BorderSide current, int currentOrigin) {
        if (candidate == null) return false;
        if (current == null) return true;
        if (current.getStyle() == BorderStyle.HIDDEN) return false;
        if (candidate.getStyle() == BorderStyle.HIDDEN) return true;
        if (candidate.getStyle() == BorderStyle.NONE || candidate.getWidth() <= 0) return false;
        if (current.getStyle() == BorderStyle.NONE || current.getWidth() <= 0) return true;
        if (candidate.getWidth() != current.getWidth()) return candidate.getWidth() > current.getWidth();
        int a = rank(candidate.getStyle());
        int b = rank(current.getStyle());
        if (a != b) return a > b;
        return candidateOrigin > currentOrigin;
    }

    /**
     * @return сила стиля при равной толщине (больше - сильнее)
     */
    public static int rank(BorderStyle style) {
        switch (style) {
            case DOUBLE:
                return 8;
            case SOLID:
                return 7;
            case DASHED:
                return 6;
            case DOTTED:
                return 5;
            case RIDGE:
                return 4;
            case OUTSET:
                return 3;
            case GROOVE:
                return 2;
            case INSET:
                return 1;
            default:
                return 0;
        }
    }

    // ============================================
    // Толщины
    // ============================================

    /**
     * @return толщина горизонтального отрезка (0, если рамки нет)
     */
    public double horizontalWidth(int line, int col) {
        BorderSide s = this.horizontal[line][col];
        return s == null ? 0 : s.effectiveWidth();
    }

    /**
     * @return толщина вертикального отрезка (0, если рамки нет)
     */
    public double verticalWidth(int row, int line) {
        BorderSide s = this.vertical[row][line];
        return s == null ? 0 : s.effectiveWidth();
    }

    /**
     * Задаёт ячейке половины общих рамок как толщины для раскладки.
     */
    void applyTo(CellBox c) {
        double top = 0;
        double bottom = 0;
        for (int k = c.col(); k < c.col() + c.colspan(); k++) {
            top = Math.max(top, horizontalWidth(c.row(), k));
            bottom = Math.max(bottom, horizontalWidth(c.row() + c.rowspan(), k));
        }
        double left = 0;
        double right = 0;
        for (int k = c.row(); k < c.row() + c.rowspan(); k++) {
            left = Math.max(left, verticalWidth(k, c.col()));
            right = Math.max(right, verticalWidth(k, c.col() + c.colspan()));
        }
        c.borderTop = top / 2;
        c.borderBottom = bottom / 2;
        c.borderLeft = left / 2;
        c.borderRight = right / 2;
    }

    /**
     * @return половины толщины внешних рамок: {сверху, справа, снизу, слева}
     */
    double[] outerHalves() {
        double top = 0, right = 0, bottom = 0, left = 0;
        for (int c = 0; c < this.cols; c++) {
            top = Math.max(top, horizontalWidth(0, c));
            bottom = Math.max(bottom, horizontalWidth(this.rows, c));
        }
        for (int r = 0; r < this.rows; r++) {
            left = Math.max(left, verticalWidth(r, 0));
            right = Math.max(right, verticalWidth(r, this.cols));
        }
        return new double[]{top / 2, right / 2, bottom / 2, left / 2};
    }

    /**
     * @return общая сторона, если все внешние рамки одинаковы и видимы, иначе null
     */
    public BorderSide uniformOuter() {
        if (this.cols == 0 || this.rows == 0) return null;
        BorderSide first = this.horizontal[0][0];
        if (first == null || !first.isVisible()) return null;
        for (int c = 0; c < this.cols; c++) {
            if (!first.equals(this.horizontal[0][c]) || !first.equals(this.horizontal[this.rows][c])) return null;
        }
        for (int r = 0; r < this.rows; r++) {
            if (!first.equals(this.vertical[r][0]) || !first.equals(this.vertical[r][this.cols])) return null;
        }
        return first;
    }
}
