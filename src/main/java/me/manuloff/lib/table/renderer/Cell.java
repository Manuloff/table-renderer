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

    public Cell(Table table, int row, int col, int rowspan, int colspan) {
        this.table = table;
        this.row = row;
        this.col = col;
        this.rowspan = rowspan;
        this.colspan = colspan;
    }

    // === Text ===

    public Cell setTexts(List<Text> texts) {
        this.texts = texts;
        return this;
    }

    public Cell setText(String text) {
        this.texts = Collections.singletonList(new Text(text));
        return this;
    }

    public Cell setText(Text text) {
        this.texts = Collections.singletonList(text);
        return this;
    }

    public Cell appendText(String text) {
        this.texts.add(new Text(text));
        return this;
    }

    public Cell appendText(Text text) {
        this.texts.add(text);
        return this;
    }

    public Cell appendNewLine() {
        this.texts.add(Text.NEW_LINE);
        return this;
    }

    // === Styles ===

    public TableStyle tableStyle() {
        return this.tableStyle;
    }

    public Cell tableStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.tableStyle);
        return this;
    }

    public TextStyle textStyle() {
        return this.textStyle;
    }

    public Cell textStyle(Consumer<TextStyle> consumer) {
        consumer.accept(this.textStyle);
        return this;
    }

    // === Hover style ===

    public TableStyle hoverStyle() {
        return this.hoverStyle;
    }

    public Cell hoverStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.hoverStyle);
        return this;
    }

    // === Focus style ===

    public TableStyle focusStyle() {
        return this.focusStyle;
    }

    public Cell focusStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.focusStyle);
        return this;
    }

    // === Active style ===

    public TableStyle activeStyle() {
        return this.activeStyle;
    }

    public Cell activeStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.activeStyle);
        return this;
    }

    // === Fixed dimensions ===

    public Integer fixedWidth() {
        return this.fixedWidth;
    }

    public Cell fixedWidth(Integer width) {
        this.fixedWidth = width;
        return this;
    }

    public Integer fixedHeight() {
        return this.fixedHeight;
    }

    public Cell fixedHeight(Integer height) {
        this.fixedHeight = height;
        return this;
    }

    public Integer minWidth() {
        return this.minWidth;
    }

    public Cell minWidth(Integer width) {
        this.minWidth = width;
        return this;
    }

    public Integer maxWidth() {
        return this.maxWidth;
    }

    public Cell maxWidth(Integer width) {
        this.maxWidth = width;
        return this;
    }

    public Integer minHeight() {
        return this.minHeight;
    }

    public Cell minHeight(Integer height) {
        this.minHeight = height;
        return this;
    }

    public Integer maxHeight() {
        return this.maxHeight;
    }

    public Cell maxHeight(Integer height) {
        this.maxHeight = height;
        return this;
    }

    // === State ===

    public Cell hovered(boolean hovered) {
        this.isHovered = hovered;
        return this;
    }

    public Cell focused(boolean focused) {
        this.isFocused = focused;
        return this;
    }

    public Cell active(boolean active) {
        this.isActive = active;
        return this;
    }

    // === Effective style resolution ===

    /**
     * Возвращает эффективный TableStyle с учётом состояния
     */
    public TableStyle getEffectiveTableStyle() {
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
     * Возвращает эффективный TextStyle с учётом иерархии
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
