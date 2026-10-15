package me.manuloff.lib.table.renderer;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.content.RichText;
import me.manuloff.lib.table.renderer.style.Style;

import java.util.function.Consumer;

/**
 * Ячейка таблицы.
 * <p>
 * Содержимое задаётся одним из двух способов:
 * <ul>
 *     <li>значением ({@link #value(Object)}): строки выводятся как есть, {@link RichText}, строчные узлы
 *     и изображения ({@code BufferedImage}) - как содержимое, остальное - через
 *     {@link Table#formatter(java.util.function.Function)};</li>
 *     <li>форматированным текстом ({@link #content(Consumer)}), который имеет приоритет над значением.</li>
 * </ul>
 * Значение доступно условным правилам ({@link me.manuloff.lib.table.renderer.rule.CellContext#value()}),
 * поэтому удобно хранить в ячейке исходные данные, а оформлять их правилами.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Cell {

    private final Row row;
    private final RichText content = new RichText();
    private final Style style = new Style();

    private Object value;
    private int colspan = 1;
    private int rowspan = 1;
    private boolean header;

    Cell(Row row, Object value, boolean header) {
        this.row = row;
        this.value = value;
        this.header = header;
    }

    // ============================================
    // Связи
    // ============================================

    /**
     * @return строка, которой принадлежит ячейка
     */
    public Row row() {
        return this.row;
    }

    /**
     * @return секция, которой принадлежит ячейка
     */
    public Section section() {
        return this.row.section();
    }

    /**
     * @return таблица, которой принадлежит ячейка
     */
    public Table table() {
        return this.row.section().table();
    }

    // ============================================
    // Содержимое
    // ============================================

    /**
     * @return значение ячейки
     */
    public Object value() {
        return this.value;
    }

    /**
     * Задаёт значение ячейки и очищает явное содержимое.
     */
    public Cell value(Object value) {
        this.value = value;
        this.content.clear();
        return this;
    }

    /**
     * Задаёт текст ячейки (то же, что {@link #value(Object)} со строкой).
     */
    public Cell text(String text) {
        return value(text);
    }

    /**
     * @return явное форматированное содержимое; если оно не пустое, значение при отрисовке игнорируется
     */
    public RichText content() {
        return this.content;
    }

    /**
     * Заполняет явное форматированное содержимое.
     *
     * <pre>{@code
     * cell.content(c -> c.text("Итого: ").bold("1 250 ₽"));
     * }</pre>
     */
    public Cell content(@NonNull Consumer<RichText> consumer) {
        consumer.accept(this.content);
        return this;
    }

    // ============================================
    // Стиль
    // ============================================

    /**
     * @return собственный стиль ячейки (самый приоритетный уровень)
     */
    public Style style() {
        return this.style;
    }

    /**
     * Настраивает стиль ячейки.
     */
    public Cell style(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.style);
        return this;
    }

    // ============================================
    // Объединение
    // ============================================

    /**
     * @return количество занимаемых колонок
     */
    public int colspan() {
        return this.colspan;
    }

    /**
     * Задаёт количество занимаемых колонок.
     */
    public Cell colspan(int colspan) {
        if (colspan < 1) throw new IllegalArgumentException("Colspan must be at least 1: " + colspan);
        this.colspan = colspan;
        return this;
    }

    /**
     * @return количество занимаемых строк
     */
    public int rowspan() {
        return this.rowspan;
    }

    /**
     * Задаёт количество занимаемых строк. Объединение не выходит за пределы секции.
     */
    public Cell rowspan(int rowspan) {
        if (rowspan < 1) throw new IllegalArgumentException("Rowspan must be at least 1: " + rowspan);
        this.rowspan = rowspan;
        return this;
    }

    /**
     * Задаёт объединение по колонкам и строкам.
     */
    public Cell span(int colspan, int rowspan) {
        return colspan(colspan).rowspan(rowspan);
    }

    // ============================================
    // Заголовочная ячейка
    // ============================================

    /**
     * @return true для заголовочной ячейки (аналог th): по умолчанию - все ячейки шапки
     */
    public boolean header() {
        return this.header;
    }

    /**
     * Помечает ячейку как заголовочную. Заголовочные ячейки по умолчанию жирные и выбираются
     * условием {@link me.manuloff.lib.table.renderer.rule.Match#headerCells()}.
     */
    public Cell header(boolean header) {
        this.header = header;
        return this;
    }

    @Override
    public String toString() {
        return "Cell[" + (this.content.isEmpty() ? this.value : this.content) + "]";
    }
}
