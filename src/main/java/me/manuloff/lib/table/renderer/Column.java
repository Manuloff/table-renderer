package me.manuloff.lib.table.renderer;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.box.Size;

import java.util.function.Consumer;

/**
 * Колонка таблицы.
 * <p>
 * Стиль колонки применяется к ячейкам, начинающимся в этой колонке (поверх стиля секции, под стилем строки),
 * со следующими особенностями:
 * <ul>
 *     <li>ширина ({@link #width(double)}) задаёт ширину самой колонки;</li>
 *     <li>фон рисуется одним слоем на всю высоту колонки.</li>
 * </ul>
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Column {

    private final Table table;
    private final int index;
    private final Style style = new Style();

    Column(Table table, int index) {
        this.table = table;
        this.index = index;
    }

    /**
     * @return таблица, которой принадлежит колонка
     */
    public Table table() {
        return this.table;
    }

    /**
     * @return индекс колонки (с нуля)
     */
    public int index() {
        return this.index;
    }

    /**
     * @return стиль ячеек колонки
     */
    public Style style() {
        return this.style;
    }

    /**
     * Настраивает стиль ячеек колонки.
     */
    public Column style(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.style);
        return this;
    }

    /**
     * Задаёт ширину колонки в пикселях (включая отступы и рамки ячеек).
     */
    public Column width(double px) {
        this.style.width(px);
        return this;
    }

    /**
     * Задаёт ширину колонки в пикселях или процентах от ширины таблицы.
     */
    public Column width(@NonNull Size size) {
        this.style.width(size);
        return this;
    }
}
