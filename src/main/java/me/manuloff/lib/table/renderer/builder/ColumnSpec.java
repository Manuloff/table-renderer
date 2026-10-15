package me.manuloff.lib.table.renderer.builder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.box.Size;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Описание колонки в {@link TableBuilder}: заголовок, способ получения значения из элемента и оформление.
 *
 * @param <T> тип элементов
 * @author Manuloff
 * @since 2.0
 */
@Getter(AccessLevel.PACKAGE)
public final class ColumnSpec<T> {

    private final Object header;
    private final Function<? super T, ?> extractor;
    private final Style style = new Style();
    private final Style headerStyle = new Style();
    private Function<Object, ?> format;
    private BiConsumer<Cell, ? super T> customizer;
    private Size width;

    ColumnSpec(Object header, Function<? super T, ?> extractor) {
        this.header = header;
        this.extractor = extractor;
    }

    /**
     * Преобразует значение перед записью в ячейку. Для null преобразование не вызывается.
     * Результат может быть строкой, числом, {@link me.manuloff.lib.table.renderer.content.RichText}
     * или изображением.
     *
     * <pre>{@code
     * c.format((Double v) -> String.format("%,.2f ₽", v))
     * }</pre>
     *
     * @param <V> тип значения колонки
     */
    @SuppressWarnings("unchecked")
    public <V> ColumnSpec<T> format(@NonNull Function<V, ?> format) {
        Function<Object, ?> f = (Function<Object, ?>) format;
        this.format = v -> v == null ? null : f.apply(v);
        return this;
    }

    /**
     * Стиль ячеек этой колонки в теле таблицы.
     */
    public ColumnSpec<T> style(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.style);
        return this;
    }

    /**
     * Стиль ячейки шапки этой колонки.
     */
    public ColumnSpec<T> headerStyle(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.headerStyle);
        return this;
    }

    /**
     * Ширина колонки в пикселях.
     */
    public ColumnSpec<T> width(double px) {
        this.width = Size.px(px);
        return this;
    }

    /**
     * Ширина колонки в пикселях или процентах.
     */
    public ColumnSpec<T> width(@NonNull Size size) {
        this.width = size;
        return this;
    }

    /**
     * Произвольная настройка ячейки по элементу (вызывается после записи значения и стиля).
     *
     * <pre>{@code
     * c.cell((cell, order) -> { if (order.isUrgent()) cell.style(Style::bold); })
     * }</pre>
     */
    public ColumnSpec<T> cell(@NonNull BiConsumer<Cell, ? super T> customizer) {
        this.customizer = customizer;
        return this;
    }

    /**
     * @return значение колонки для элемента (с учётом форматирования)
     */
    Object valueOf(T item) {
        Object value = this.extractor.apply(item);
        return this.format == null ? value : this.format.apply(value);
    }
}
