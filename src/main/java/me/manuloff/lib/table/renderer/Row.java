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
 * Строка таблицы.
 *
 * @author Manuloff
 * @since 1.0
 */
@Data
@Getter
public final class Row {

    private final Table table;
    private final int index;

    @NonNull
    private final TableStyle tableStyle = new TableStyle();
    @NonNull
    private final TextStyle textStyle = new TextStyle();

    // Фиксированная высота строки (null = авто)
    private Integer fixedHeight;

    /**
     * Создаёт строку указанной таблицы. Обычно вызывается из {@link Table#getRow(int)}.
     *
     * @param table родительская таблица
     * @param index индекс строки (с нуля)
     */
    public Row(Table table, int index) {
        this.table = table;
        this.index = index;
    }

    /**
     * @return ячейки, у которых {@link Cell#getRow()} совпадает с индексом этой строки
     */
    @NonNull
    public List<Cell> getCells() {
        return this.table.getCells().stream()
                .filter(cell -> cell.getRow() == this.index)
                .collect(Collectors.toList());
    }

    // === Styles ===

    /**
     * @return стиль оформления строки
     */
    @NonNull
    public TableStyle tableStyle() {
        return this.tableStyle;
    }

    /**
     * Настраивает {@link TableStyle} строки и возвращает её.
     */
    @NonNull
    public Row tableStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.tableStyle);
        return this;
    }

    /**
     * @return стиль текста строки
     */
    @NonNull
    public TextStyle textStyle() {
        return this.textStyle;
    }

    /**
     * Настраивает {@link TextStyle} строки и возвращает её.
     */
    @NonNull
    public Row textStyle(Consumer<TextStyle> consumer) {
        consumer.accept(this.textStyle);
        return this;
    }

    // === Fixed height ===

    /**
     * @return фиксированная высота строки или {@code null}, если высота считается автоматически
     */
    public Integer fixedHeight() {
        return this.fixedHeight;
    }

    /**
     * Задаёт фиксированную высоту строки в пикселях; {@code null} возвращает авто-расчёт.
     */
    @NonNull
    public Row fixedHeight(Integer height) {
        this.fixedHeight = height;
        return this;
    }
}
