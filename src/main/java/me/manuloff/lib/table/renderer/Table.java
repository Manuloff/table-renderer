package me.manuloff.lib.table.renderer;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.builder.TableBuilder;
import me.manuloff.lib.table.renderer.content.RichText;
import me.manuloff.lib.table.renderer.render.RenderOptions;
import me.manuloff.lib.table.renderer.render.RenderedTable;
import me.manuloff.lib.table.renderer.render.TableRenderer;
import me.manuloff.lib.table.renderer.rule.CellMatcher;
import me.manuloff.lib.table.renderer.rule.StyleRule;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.theme.Theme;
import me.manuloff.lib.table.renderer.theme.Themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Таблица - корневой объект модели: секции со строками и ячейками, колонки, заголовок, стили и условные правила.
 *
 * <pre>{@code
 * Table table = new Table();
 * table.header("Товар", "Кол-во", "Цена");
 * table.addRow("Яблоки", 3, 120.5);
 * table.addRow("Груши", 5, 99.9);
 * table.rule(Match.body().and(Match.column(1, 2)), s -> s.right());
 * table.render().write(Path.of("table.png"));
 * }</pre>
 *
 * <h2>Структура</h2>
 * Таблица состоит из шапки ({@link #head()}), одного или нескольких тел ({@link #body()}, {@link #addBody()})
 * и подвала ({@link #foot()}). Секции содержат строки ({@link Row}), строки - ячейки ({@link Cell}).
 * Ячейки раскладываются по сетке с учётом объединений (colspan/rowspan), как в HTML.
 *
 * <h2>Стили</h2>
 * Итоговый стиль ячейки складывается из уровней, каждый следующий перекрывает заданные свойства предыдущего:
 * <ol>
 *     <li>стиль ячеек таблицы ({@link #cellStyle()});</li>
 *     <li>стиль секции ({@link Section#style()});</li>
 *     <li>стиль колонки ({@link Column#style()});</li>
 *     <li>стиль строки ({@link Row#style()});</li>
 *     <li>условные правила в порядке добавления ({@link #rule(CellMatcher, Consumer)});</li>
 *     <li>стиль самой ячейки ({@link Cell#style()}).</li>
 * </ol>
 * Исключения:
 * <ul>
 *     <li>фон (цвет и слои) секций, колонок и строк рисуется, как в CSS, отдельным слоем на геометрии
 *     всей секции/колонки/строки - например, градиент шапки идёт через всю строку;</li>
 *     <li>ширина колонки ({@link Column#width(double)}) задаёт ширину колонки, а не каждой ячейки.</li>
 * </ul>
 * Оформление блока таблицы (рамка, фон, тень, ширина, модель рамок) задаётся отдельно - {@link #style()}.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Table {

    private final TableStyle style = new TableStyle();
    private final Style cellStyle = new Style();
    private final Section head = new Section(this, Section.Kind.HEAD);
    private final List<Section> bodies = new ArrayList<>();
    private final Section foot = new Section(this, Section.Kind.FOOT);
    private final Map<Integer, Column> columns = new TreeMap<>();
    private final List<StyleRule> rules = new ArrayList<>();
    private Caption caption;
    private Function<Object, String> formatter = String::valueOf;

    /**
     * Создаёт пустую таблицу с оформлением {@link Themes#DEFAULT}: тонкая серая сетка,
     * отступы в ячейках, жирная шапка на светлом фоне.
     */
    public Table() {
        this.bodies.add(new Section(this, Section.Kind.BODY));
        Themes.DEFAULT.apply(this);
    }

    /**
     * Создаёт таблицу из заголовков и строк данных.
     *
     * @param header заголовки колонок (пустой массив - без шапки)
     * @param rows   строки тела
     */
    public static Table of(@NonNull Object[] header, @NonNull Object[][] rows) {
        Table table = new Table();
        if (header.length > 0) table.header(header);
        table.body().addRows(rows);
        return table;
    }

    /**
     * Начинает построение таблицы из коллекции объектов.
     *
     * <pre>{@code
     * Table table = Table.from(users)
     *         .column("Имя", User::getName)
     *         .column("Возраст", User::getAge, c -> c.style(Style::right))
     *         .build();
     * }</pre>
     *
     * @see TableBuilder
     */
    public static <T> TableBuilder<T> from(@NonNull Iterable<T> items) {
        return TableBuilder.of(items);
    }

    // ============================================
    // Секции
    // ============================================

    /**
     * @return шапка таблицы (thead); рисуется первой
     */
    public Section head() {
        return this.head;
    }

    /**
     * @return первое тело таблицы (tbody)
     */
    public Section body() {
        return this.bodies.get(0);
    }

    /**
     * Добавляет ещё одно тело таблицы - отдельную группу строк. Объединение строк (rowspan)
     * не выходит за пределы группы.
     *
     * @return новое тело
     */
    public Section addBody() {
        Section section = new Section(this, Section.Kind.BODY);
        this.bodies.add(section);
        return section;
    }

    /**
     * @return все тела таблицы в порядке добавления
     */
    public List<Section> bodies() {
        return Collections.unmodifiableList(this.bodies);
    }

    /**
     * @return подвал таблицы (tfoot); рисуется последним
     */
    public Section foot() {
        return this.foot;
    }

    /**
     * @return все секции в порядке отрисовки: шапка, тела, подвал
     */
    public List<Section> sections() {
        List<Section> list = new ArrayList<>(this.bodies.size() + 2);
        list.add(this.head);
        list.addAll(this.bodies);
        list.add(this.foot);
        return list;
    }

    // ============================================
    // Быстрое заполнение
    // ============================================

    /**
     * Добавляет строку в шапку. Её ячейки - заголовочные.
     *
     * @return добавленная строка
     */
    public Row header(Object... values) {
        return this.head.addRow(values);
    }

    /**
     * Добавляет строку в последнее тело таблицы.
     *
     * @param values значения ячеек (строки, числа, {@link RichText}, изображения и т.д.)
     * @return добавленная строка
     */
    public Row addRow(Object... values) {
        return lastBody().addRow(values);
    }

    /**
     * Добавляет строку в последнее тело таблицы.
     *
     * @return добавленная строка
     */
    public Row addRow(@NonNull Iterable<?> values) {
        return lastBody().addRow(values);
    }

    /**
     * Добавляет строку в подвал.
     *
     * @return добавленная строка
     */
    public Row footer(Object... values) {
        return this.foot.addRow(values);
    }

    private Section lastBody() {
        return this.bodies.get(this.bodies.size() - 1);
    }

    // ============================================
    // Колонки
    // ============================================

    /**
     * @param index индекс колонки (с нуля)
     * @return колонка (создаётся при первом обращении)
     */
    public Column column(int index) {
        if (index < 0) throw new IllegalArgumentException("Column index can't be negative: " + index);
        return this.columns.computeIfAbsent(index, i -> new Column(this, i));
    }

    /**
     * Настраивает колонку.
     */
    public Table column(int index, @NonNull Consumer<Column> consumer) {
        consumer.accept(column(index));
        return this;
    }

    /**
     * @return колонки, к которым обращались (индекс - колонка)
     */
    public Map<Integer, Column> columns() {
        return Collections.unmodifiableMap(this.columns);
    }

    // ============================================
    // Заголовок
    // ============================================

    /**
     * @return заголовок таблицы (создаётся при первом обращении; пустой заголовок не рисуется)
     */
    public Caption caption() {
        if (this.caption == null) this.caption = new Caption();
        return this.caption;
    }

    /**
     * Задаёт текст заголовка.
     */
    public Table caption(@NonNull String text) {
        caption().text(text);
        return this;
    }

    /**
     * Настраивает заголовок.
     */
    public Table caption(@NonNull Consumer<Caption> consumer) {
        consumer.accept(caption());
        return this;
    }

    /**
     * @return true, если заголовок задан и не пуст
     */
    public boolean hasCaption() {
        return this.caption != null && !this.caption.content().isEmpty();
    }

    /**
     * Удаляет заголовок вместе с его стилем.
     */
    public Table removeCaption() {
        this.caption = null;
        return this;
    }

    // ============================================
    // Стили
    // ============================================

    /**
     * @return стиль блока таблицы (фон, рамка, тень, ширина, модель рамок)
     */
    public TableStyle style() {
        return this.style;
    }

    /**
     * Настраивает стиль блока таблицы.
     */
    public Table style(@NonNull Consumer<TableStyle> consumer) {
        consumer.accept(this.style);
        return this;
    }

    /**
     * @return стиль по умолчанию для всех ячеек; его текстовые свойства наследует и заголовок таблицы
     */
    public Style cellStyle() {
        return this.cellStyle;
    }

    /**
     * Настраивает стиль по умолчанию для всех ячеек.
     */
    public Table cellStyle(@NonNull Consumer<Style> consumer) {
        consumer.accept(this.cellStyle);
        return this;
    }

    /**
     * Добавляет условное правило: стиль применяется ко всем ячейкам, удовлетворяющим условию.
     * Правила применяются в порядке добавления, после стиля строки и до стиля ячейки.
     *
     * <pre>{@code
     * table.rule(Match.body().and(Match.evenRows()), s -> s.background(new Color(0xF5F5F5)));
     * }</pre>
     *
     * @see me.manuloff.lib.table.renderer.rule.Match
     */
    public Table rule(@NonNull CellMatcher matcher, @NonNull Consumer<Style> style) {
        return addRule(matcher, style, false);
    }

    /**
     * Добавляет правило от имени темы оформления. Такие правила удаляются при применении другой темы.
     * Предназначено для реализаций {@link Theme}.
     */
    public Table themeRule(@NonNull CellMatcher matcher, @NonNull Consumer<Style> style) {
        return addRule(matcher, style, true);
    }

    private Table addRule(CellMatcher matcher, Consumer<Style> style, boolean theme) {
        Style s = new Style();
        style.accept(s);
        this.rules.add(new StyleRule(matcher, s, theme));
        return this;
    }

    /**
     * @return все условные правила в порядке применения
     */
    public List<StyleRule> rules() {
        return Collections.unmodifiableList(this.rules);
    }

    /**
     * Удаляет все условные правила, включая правила темы.
     */
    public Table clearRules() {
        this.rules.clear();
        return this;
    }

    /**
     * Применяет тему оформления. Правила предыдущей темы удаляются, пользовательские правила сохраняются.
     *
     * @see Themes
     */
    public Table theme(@NonNull Theme theme) {
        this.rules.removeIf(StyleRule::isTheme);
        theme.apply(this);
        return this;
    }

    // ============================================
    // Форматирование значений
    // ============================================

    /**
     * Задаёт преобразование значений ячеек в текст. Используется для всех значений, кроме строк,
     * {@link RichText}, строчных узлов и изображений. По умолчанию - {@link String#valueOf(Object)}.
     *
     * <pre>{@code
     * table.formatter(v -> v instanceof Double ? String.format("%.2f", v) : String.valueOf(v));
     * }</pre>
     */
    public Table formatter(@NonNull Function<Object, String> formatter) {
        this.formatter = formatter;
        return this;
    }

    /**
     * @return преобразование значений ячеек в текст
     */
    public Function<Object, String> formatter() {
        return this.formatter;
    }

    // ============================================
    // Рендер
    // ============================================

    /**
     * Рисует таблицу с параметрами по умолчанию (масштаб 1, прозрачный фон).
     */
    public RenderedTable render() {
        return TableRenderer.render(this, RenderOptions.DEFAULT);
    }

    /**
     * Рисует таблицу с указанными параметрами.
     */
    public RenderedTable render(@NonNull RenderOptions options) {
        return TableRenderer.render(this, options);
    }
}
