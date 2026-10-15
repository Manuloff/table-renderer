package me.manuloff.lib.table.renderer.rule;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.Section;

import java.util.function.Predicate;

/**
 * Готовые условия выбора ячеек.
 *
 * <pre>{@code
 * table.rule(Match.body().and(Match.evenRows()), s -> s.background(new Color(0xF7F7F7)));
 * table.rule(Match.column(2, 3), s -> s.right());
 * table.rule(Match.value(v -> v instanceof Number && ((Number) v).doubleValue() < 0), s -> s.color(Color.RED));
 * }</pre>
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Match {

    private Match() {
    }

    /**
     * Все ячейки.
     */
    public static CellMatcher all() {
        return c -> true;
    }

    /**
     * Ячейки, не удовлетворяющие условию.
     */
    public static CellMatcher not(@NonNull CellMatcher matcher) {
        return matcher.negate();
    }

    // === Секции ===

    /**
     * Ячейки шапки.
     */
    public static CellMatcher head() {
        return section(Section.Kind.HEAD);
    }

    /**
     * Ячейки тел таблицы.
     */
    public static CellMatcher body() {
        return section(Section.Kind.BODY);
    }

    /**
     * Ячейки подвала.
     */
    public static CellMatcher foot() {
        return section(Section.Kind.FOOT);
    }

    /**
     * Ячейки секций указанного вида.
     */
    public static CellMatcher section(@NonNull Section.Kind kind) {
        return c -> c.section().kind() == kind;
    }

    /**
     * Заголовочные ячейки (ячейки шапки и ячейки, помеченные {@link me.manuloff.lib.table.renderer.Cell#header(boolean)}).
     */
    public static CellMatcher headerCells() {
        return CellContext::isHeader;
    }

    // === Строки ===

    /**
     * Строки с индексами внутри секции.
     */
    public static CellMatcher row(int... indexes) {
        return c -> contains(indexes, c.rowIndex());
    }

    /**
     * Чётные по счёту строки секции: 2-я, 4-я, ... (индексы 1, 3, ...), как CSS {@code :nth-child(even)}.
     */
    public static CellMatcher evenRows() {
        return c -> c.rowIndex() % 2 == 1;
    }

    /**
     * Нечётные по счёту строки секции: 1-я, 3-я, ... (индексы 0, 2, ...), как CSS {@code :nth-child(odd)}.
     */
    public static CellMatcher oddRows() {
        return c -> c.rowIndex() % 2 == 0;
    }

    /**
     * Каждая {@code step}-я строка секции, начиная с индекса {@code offset} (аналог {@code :nth-child(An+B)}).
     */
    public static CellMatcher everyRow(int step, int offset) {
        if (step < 1) throw new IllegalArgumentException("Step must be at least 1: " + step);
        return c -> c.rowIndex() >= offset && (c.rowIndex() - offset) % step == 0;
    }

    /**
     * Ячейки первой строки секции.
     */
    public static CellMatcher firstRow() {
        return CellContext::isFirstRow;
    }

    /**
     * Ячейки, доходящие до последней строки секции.
     */
    public static CellMatcher lastRow() {
        return CellContext::isLastRow;
    }

    // === Колонки ===

    /**
     * Ячейки, начинающиеся в колонках с указанными индексами.
     */
    public static CellMatcher column(int... indexes) {
        return c -> contains(indexes, c.columnIndex());
    }

    /**
     * Чётные по счёту колонки: 2-я, 4-я, ... (индексы 1, 3, ...).
     */
    public static CellMatcher evenColumns() {
        return c -> c.columnIndex() % 2 == 1;
    }

    /**
     * Нечётные по счёту колонки: 1-я, 3-я, ... (индексы 0, 2, ...).
     */
    public static CellMatcher oddColumns() {
        return c -> c.columnIndex() % 2 == 0;
    }

    /**
     * Ячейки первой колонки.
     */
    public static CellMatcher firstColumn() {
        return CellContext::isFirstColumn;
    }

    /**
     * Ячейки, доходящие до последней колонки.
     */
    public static CellMatcher lastColumn() {
        return CellContext::isLastColumn;
    }

    // === Содержимое ===

    /**
     * Ячейки, значение которых удовлетворяет условию.
     */
    public static CellMatcher value(@NonNull Predicate<Object> predicate) {
        return c -> predicate.test(c.value());
    }

    /**
     * Ячейки с числовыми значениями.
     */
    public static CellMatcher numeric() {
        return c -> c.value() instanceof Number;
    }

    private static boolean contains(int[] values, int value) {
        for (int v : values) {
            if (v == value) return true;
        }
        return false;
    }
}
