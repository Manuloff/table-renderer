package me.manuloff.lib.table.renderer;

import lombok.Data;
import lombok.Getter;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.TextStyle;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ячейка таблицы.
 *
 * @author Manuloff
 * @since 1.0
 */
@Data
@Getter
public final class Cell {

    private final Table table;

    private final int row;
    private final int col;
    private final int rowspan;
    private final int colspan;

    private final TableStyle tableStyle = new TableStyle();
    private final TextStyle textStyle = new TextStyle();

    // Hover/Focus/Active состояния
    private final TableStyle hoverStyle = new TableStyle();
    private final TableStyle focusStyle = new TableStyle();
    private final TableStyle activeStyle = new TableStyle();

    // Тексты в ячейке
    private List<Text> texts = new LinkedList<>();

    // Фиксированные размеры ячейки
    private Integer fixedWidth;
    private Integer fixedHeight;
    private Integer minWidth;
    private Integer maxWidth;
    private Integer minHeight;
    private Integer maxHeight;

    // Состояния
    private boolean isHovered;
    private boolean isFocused;
    private boolean isActive;

    /**
     * Создаёт ячейку, принадлежащую указанной таблице.
     * Обычно вызывается из {@link Table#getCell(int, int, int, int)}.
     *
     * @param table   родительская таблица
     * @param row     индекс строки левого верхнего угла
     * @param col     индекс колонки левого верхнего угла
     * @param rowspan число занимаемых строк
     * @param colspan число занимаемых колонок
     */
    public Cell(Table table, int row, int col, int rowspan, int colspan) {
        this.table = table;
        this.row = row;
        this.col = col;
        this.rowspan = rowspan;
        this.colspan = colspan;
    }

    // === Text ===

    /**
     * Заменяет содержимое ячейки списком текстовых фрагментов.
     */
    public Cell setTexts(List<Text> texts) {
        this.texts = texts;
        return this;
    }

    /**
     * Заменяет содержимое одним текстовым фрагментом.
     */
    public Cell setText(String text) {
        this.texts = Collections.singletonList(new Text(text));
        return this;
    }

    /**
     * Заменяет содержимое одним фрагментом {@link Text}.
     */
    public Cell setText(Text text) {
        this.texts = Collections.singletonList(text);
        return this;
    }

    /**
     * Добавляет текстовый фрагмент в конец содержимого.
     */
    public Cell appendText(String text) {
        this.texts.add(new Text(text));
        return this;
    }

    /**
     * Добавляет готовый фрагмент {@link Text} в конец содержимого.
     */
    public Cell appendText(Text text) {
        this.texts.add(text);
        return this;
    }

    /**
     * Добавляет маркер новой строки {@link Text#NEW_LINE}.
     */
    public Cell appendNewLine() {
        this.texts.add(Text.NEW_LINE);
        return this;
    }

    // === Styles ===

    /**
     * @return стиль оформления этой ячейки
     */
    public TableStyle tableStyle() {
        return this.tableStyle;
    }

    /**
     * Настраивает {@link TableStyle} ячейки и возвращает её.
     */
    public Cell tableStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.tableStyle);
        return this;
    }

    /**
     * @return стиль текста этой ячейки
     */
    public TextStyle textStyle() {
        return this.textStyle;
    }

    /**
     * Настраивает {@link TextStyle} ячейки и возвращает её.
     */
    public Cell textStyle(Consumer<TextStyle> consumer) {
        consumer.accept(this.textStyle);
        return this;
    }

    // === Hover style ===

    /**
     * @return стиль, накладываемый при {@link #hovered(boolean) hovered(true)}
     */
    public TableStyle hoverStyle() {
        return this.hoverStyle;
    }

    /**
     * Настраивает hover-стиль ячейки и возвращает её.
     */
    public Cell hoverStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.hoverStyle);
        return this;
    }

    // === Focus style ===

    /**
     * @return стиль, накладываемый при {@link #focused(boolean) focused(true)}
     */
    public TableStyle focusStyle() {
        return this.focusStyle;
    }

    /**
     * Настраивает focus-стиль ячейки и возвращает её.
     */
    public Cell focusStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.focusStyle);
        return this;
    }

    // === Active style ===

    /**
     * @return стиль, накладываемый при {@link #active(boolean) active(true)}
     */
    public TableStyle activeStyle() {
        return this.activeStyle;
    }

    /**
     * Настраивает active-стиль ячейки и возвращает её.
     */
    public Cell activeStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.activeStyle);
        return this;
    }

    // === Fixed dimensions ===

    /**
     * @return фиксированная ширина ячейки или {@code null}
     */
    public Integer fixedWidth() {
        return this.fixedWidth;
    }

    /**
     * Задаёт фиксированную ширину ячейки в пикселях.
     */
    public Cell fixedWidth(Integer width) {
        this.fixedWidth = width;
        return this;
    }

    /**
     * @return фиксированная высота ячейки или {@code null}
     */
    public Integer fixedHeight() {
        return this.fixedHeight;
    }

    /**
     * Задаёт фиксированную высоту ячейки в пикселях.
     */
    public Cell fixedHeight(Integer height) {
        this.fixedHeight = height;
        return this;
    }

    /**
     * @return минимальная ширина ячейки или {@code null}
     */
    public Integer minWidth() {
        return this.minWidth;
    }

    /**
     * Задаёт минимальную ширину ячейки в пикселях.
     */
    public Cell minWidth(Integer width) {
        this.minWidth = width;
        return this;
    }

    /**
     * @return максимальная ширина ячейки или {@code null}
     */
    public Integer maxWidth() {
        return this.maxWidth;
    }

    /**
     * Задаёт максимальную ширину ячейки в пикселях.
     */
    public Cell maxWidth(Integer width) {
        this.maxWidth = width;
        return this;
    }

    /**
     * @return минимальная высота ячейки или {@code null}
     */
    public Integer minHeight() {
        return this.minHeight;
    }

    /**
     * Задаёт минимальную высоту ячейки в пикселях.
     */
    public Cell minHeight(Integer height) {
        this.minHeight = height;
        return this;
    }

    /**
     * @return максимальная высота ячейки или {@code null}
     */
    public Integer maxHeight() {
        return this.maxHeight;
    }

    /**
     * Задаёт максимальную высоту ячейки в пикселях.
     */
    public Cell maxHeight(Integer height) {
        this.maxHeight = height;
        return this;
    }

    // === State ===

    /**
     * Включает или выключает hover-состояние (накладывается {@link #hoverStyle()}).
     */
    public Cell hovered(boolean hovered) {
        this.isHovered = hovered;
        return this;
    }

    /**
     * Включает или выключает focus-состояние (накладывается {@link #focusStyle()}).
     */
    public Cell focused(boolean focused) {
        this.isFocused = focused;
        return this;
    }

    /**
     * Включает или выключает active-состояние (накладывается {@link #activeStyle()}).
     */
    public Cell active(boolean active) {
        this.isActive = active;
        return this;
    }

    // === Effective style resolution ===

    /**
     * Возвращает эффективный TableStyle с учётом иерархии и состояния.
     */
        TableStyle style = new TableStyle();

        // Базовый стиль таблицы
        table.tableStyle().applyTo(style);
        // Строка
        table.getRow(row).tableStyle().applyTo(style);
        // Колонка
        table.getColumn(col).tableStyle().applyTo(style);
        // Row group
        applyRowGroupStyle(style);
        // Ячейка
        tableStyle().applyTo(style);

        // Hover/Focus/Active overrides
        if (isActive) {
            activeStyle().applyTo(style);
        }
        if (isFocused) {
            focusStyle().applyTo(style);
        }
        if (isHovered) {
            hoverStyle().applyTo(style);
        }

        return style;
    }

    /**
     * Возвращает эффективный TextStyle с учётом иерархии.
     */
    public TextStyle getEffectiveTextStyle() {
        TextStyle style = new TextStyle();

        table.textStyle().applyTo(style);
        table.getRow(row).textStyle().applyTo(style);
        table.getColumn(col).textStyle().applyTo(style);
        applyRowGroupTextStyle(style);
        textStyle().applyTo(style);

        return style;
    }

    private void applyRowGroupStyle(TableStyle style) {
        for (RowGroup group : new RowGroup[]{table.thead(), table.tbody(), table.tfoot()}) {
            if (group.getRowIndices().contains(row)) {
                group.tableStyle().applyTo(style);
                break;
            }
        }
    }

    private void applyRowGroupTextStyle(TextStyle style) {
        for (RowGroup group : new RowGroup[]{table.thead(), table.tbody(), table.tfoot()}) {
            if (group.getRowIndices().contains(row)) {
                group.textStyle().applyTo(style);
                break;
            }
        }
    }
}
