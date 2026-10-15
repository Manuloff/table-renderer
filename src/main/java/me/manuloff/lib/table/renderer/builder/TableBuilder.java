package me.manuloff.lib.table.renderer.builder;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.Row;
import me.manuloff.lib.table.renderer.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Построитель таблицы из коллекции объектов: каждая колонка описывается заголовком и функцией,
 * извлекающей значение из элемента.
 *
 * <pre>{@code
 * Table table = Table.from(orders)
 *         .column("№", Order::getId)
 *         .column("Клиент", Order::getClient)
 *         .column("Сумма", Order::getTotal, c -> c
 *                 .format((Double v) -> String.format("%,.2f ₽", v))
 *                 .style(Style::right))
 *         .onRow((row, order) -> {
 *             if (order.isOverdue()) row.style(s -> s.color(Color.RED));
 *         })
 *         .footer("Итого", "", total)
 *         .build();
 * }</pre>
 *
 * @param <T> тип элементов
 * @author Manuloff
 * @since 2.0
 */
public final class TableBuilder<T> {

    private final Iterable<T> items;
    private final List<ColumnSpec<T>> columns = new ArrayList<>();
    private final List<Object[]> footers = new ArrayList<>();
    private BiConsumer<Row, ? super T> rowCustomizer;
    private Consumer<Table> tableCustomizer;
    private boolean showHeader = true;

    private TableBuilder(Iterable<T> items) {
        this.items = items;
    }

    /**
     * Начинает построение таблицы из коллекции. То же, что {@link Table#from(Iterable)}.
     */
    public static <T> TableBuilder<T> of(@NonNull Iterable<T> items) {
        return new TableBuilder<>(items);
    }

    /**
     * Добавляет колонку.
     *
     * @param header    заголовок (строка или любое значение ячейки)
     * @param extractor значение колонки для элемента
     */
    public TableBuilder<T> column(Object header, @NonNull Function<? super T, ?> extractor) {
        this.columns.add(new ColumnSpec<>(header, extractor));
        return this;
    }

    /**
     * Добавляет колонку с настройкой: форматирование, стиль, ширина.
     *
     * @param header    заголовок (строка или любое значение ячейки)
     * @param extractor значение колонки для элемента
     * @param config    настройка колонки
     */
    public TableBuilder<T> column(Object header, @NonNull Function<? super T, ?> extractor,
                                  @NonNull Consumer<ColumnSpec<T>> config) {
        ColumnSpec<T> spec = new ColumnSpec<>(header, extractor);
        config.accept(spec);
        this.columns.add(spec);
        return this;
    }

    /**
     * Не создавать шапку с заголовками колонок.
     */
    public TableBuilder<T> withoutHeader() {
        this.showHeader = false;
        return this;
    }

    /**
     * Настройка строки по элементу (например, подсветка). Вызывается после заполнения ячеек строки.
     */
    public TableBuilder<T> onRow(@NonNull BiConsumer<Row, ? super T> customizer) {
        this.rowCustomizer = customizer;
        return this;
    }

    /**
     * Добавляет строку в подвал.
     */
    public TableBuilder<T> footer(Object... values) {
        this.footers.add(values);
        return this;
    }

    /**
     * Произвольная настройка готовой таблицы: тема, правила, заголовок. Можно вызывать несколько раз.
     */
    public TableBuilder<T> configure(@NonNull Consumer<Table> customizer) {
        this.tableCustomizer = this.tableCustomizer == null ? customizer : this.tableCustomizer.andThen(customizer);
        return this;
    }

    /**
     * @return новая таблица
     */
    public Table build() {
        Table table = new Table();

        for (int i = 0; i < this.columns.size(); i++) {
            ColumnSpec<T> spec = this.columns.get(i);
            if (spec.getWidth() != null) table.column(i).width(spec.getWidth());
        }

        if (this.showHeader) {
            Row header = table.head().addRow();
            for (ColumnSpec<T> spec : this.columns) {
                header.addCell(spec.getHeader()).style().apply(spec.getHeaderStyle());
            }
        }

        for (T item : this.items) {
            Row row = table.body().addRow();
            for (ColumnSpec<T> spec : this.columns) {
                Cell cell = row.addCell(spec.valueOf(item));
                cell.style().apply(spec.getStyle());
                if (spec.getCustomizer() != null) spec.getCustomizer().accept(cell, item);
            }
            if (this.rowCustomizer != null) this.rowCustomizer.accept(row, item);
        }

        for (Object[] values : this.footers) {
            table.foot().addRow(values);
        }

        if (this.tableCustomizer != null) this.tableCustomizer.accept(table);
        return table;
    }
}
