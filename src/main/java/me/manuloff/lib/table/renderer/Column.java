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

    public Column(Table table, int index) {
        this.table = table;
        this.index = index;
    }

    @NonNull
    public List<Cell> getCells() {
        return this.table.getCells().stream()
                .filter(cell -> cell.getCol() == this.index)
                .collect(Collectors.toList());
    }

    // === Styles ===

    @NonNull
    public TableStyle tableStyle() {
        return this.tableStyle;
    }

    @NonNull
    public Column tableStyle(Consumer<TableStyle> consumer) {
        consumer.accept(this.tableStyle);
        return this;
    }

    @NonNull
    public TextStyle textStyle() {
        return this.textStyle;
    }

    @NonNull
    public Column textStyle(Consumer<TextStyle> consumer) {
        consumer.accept(this.textStyle);
        return this;
    }

    // === Fixed width ===

    
    public Integer fixedWidth() {
        return this.fixedWidth;
    }

    @NonNull
    public Column fixedWidth(Integer width) {
        this.fixedWidth = width;
        return this;
    }
}
