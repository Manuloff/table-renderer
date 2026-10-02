package me.manuloff.lib.table.renderer;

import lombok.Data;
import lombok.NonNull;
import lombok.Getter;


import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.TextStyle;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Колонка таблицы.
 *
 * @author Manuloff
 * @since 1.0
 */
@Data
@Getter
public final class Column {

    private final Table table;
    private final int index;

    @NonNull
    private final TableStyle tableStyle = new TableStyle();
    @NonNull
    private final TextStyle textStyle = new TextStyle();

    // Фиксированная ширина колонки (null = авто)
    private Integer fixedWidth;

    /**
     * Создаёт колонку указанной таблицы. Обычно вызывается из {@link Table#getColumn(int)}.
     *
     * @param table родительская таблица
     * @param index индекс колонки (с нуля)
     */
    public Column(Table table, int index) {
        this.table = table;
        this.index = index;
    }

    /**
     * @return ячейки, у которых {@link Cell#getCol()} совпадает с индексом этой колонки
     */
    @NonNull
    public List<Cell> getCells() {
        return this.table.getCells().stream()
                .filter(cell -> cell.getCol() == this.index)
                .collect(Collectors.toList());
    }

    // === Styles ===

    /**
     * @return стиль оформления колонки
     */
    @NonNull
    public TableStyle tableStyle() {
        return this.tableStyle;
    }

    /**
     * Настраивает {@link TableStyle} колонки и возвращает её.
     */
    @NonNull
    public Column tableStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.tableStyle);
        return this;
    }

    /**
     * @return стиль текста колонки
     */
    @NonNull
    public TextStyle textStyle() {
        return this.textStyle;
    }

    /**
     * Настраивает {@link TextStyle} колонки и возвращает её.
     */
    @NonNull
    public Column textStyle(Consumer<TextStyle> consumer) {
        consumer.accept(this.textStyle);
        return this;
    }

    // === Fixed width ===

    /**
     * @return фиксированная ширина колонки или {@code null}, если ширина считается автоматически
     */
    public Integer fixedWidth() {
        return this.fixedWidth;
    }

    /**
     * Задаёт фиксированную ширину колонки в пикселях; {@code null} возвращает авто-расчёт.
     */
    @NonNull
    public Column fixedWidth(Integer width) {
        this.fixedWidth = width;
        return this;
    }
}
