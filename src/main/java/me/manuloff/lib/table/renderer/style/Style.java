package me.manuloff.lib.table.renderer.style;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.manuloff.lib.table.renderer.style.background.BackgroundLayer;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.border.BorderStyle;
import me.manuloff.lib.table.renderer.style.border.Corners;
import me.manuloff.lib.table.renderer.style.box.Insets;
import me.manuloff.lib.table.renderer.style.box.Overflow;
import me.manuloff.lib.table.renderer.style.box.Shadow;
import me.manuloff.lib.table.renderer.style.box.Size;
import me.manuloff.lib.table.renderer.style.text.BaselineShift;
import me.manuloff.lib.table.renderer.style.text.OverflowWrap;
import me.manuloff.lib.table.renderer.style.text.TextAlign;
import me.manuloff.lib.table.renderer.style.text.TextDecoration;
import me.manuloff.lib.table.renderer.style.text.TextTransform;
import me.manuloff.lib.table.renderer.style.text.VerticalAlign;
import me.manuloff.lib.table.renderer.style.text.WhiteSpace;
import me.manuloff.lib.table.renderer.style.text.WordBreak;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Стиль ячейки, заголовка таблицы или фрагмента текста.
 * <p>
 * Каждое свойство может быть не задано ({@code null}) - тогда значение берётся с более общего уровня
 * (см. порядок уровней в {@link me.manuloff.lib.table.renderer.Table}), а если не задано нигде -
 * используется значение по умолчанию, указанное в описании свойства.
 * Фрагменты текста ({@link me.manuloff.lib.table.renderer.content.Span}) наследуют текстовые свойства своей ячейки,
 * а свойства блока (фон, рамка, отступы, тень) у них собственные.
 *
 * <pre>{@code
 * cell.style(s -> s.background(Color.YELLOW).bold().padding(4, 8).border(1, Color.GRAY));
 * }</pre>
 *
 * Для каждого свойства есть метод чтения ({@code s.color()}) и записи ({@code s.color(Color.RED)});
 * для частых сочетаний - удобные методы ({@link #padding(double, double)}, {@link #ellipsis()},
 * {@link #center()} и т.д.).
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
@EqualsAndHashCode
@ToString
public final class Style {

    /**
     * Насыщенность шрифта: тонкий.
     */
    public static final int THIN = 100;
    /**
     * Насыщенность шрифта: светлый.
     */
    public static final int LIGHT = 300;
    /**
     * Насыщенность шрифта: обычный.
     */
    public static final int NORMAL = 400;
    /**
     * Насыщенность шрифта: средний.
     */
    public static final int MEDIUM = 500;
    /**
     * Насыщенность шрифта: полужирный.
     */
    public static final int SEMI_BOLD = 600;
    /**
     * Насыщенность шрифта: жирный.
     */
    public static final int BOLD = 700;
    /**
     * Насыщенность шрифта: сверхжирный.
     */
    public static final int BLACK = 900;

    /**
     * Маркер обрезки текста - многоточие.
     */
    public static final String ELLIPSIS = "…";

    // ============================================
    // Фон
    // ============================================

    /**
     * Цвет фона. По умолчанию - нет.
     */
    private Color background;
    /**
     * Слои фона поверх цвета: градиенты и изображения; первый слой - верхний. По умолчанию - нет.
     */
    private List<BackgroundLayer> backgroundLayers;

    // ============================================
    // Рамки и оформление блока
    // ============================================

    /**
     * Верхняя рамка. По умолчанию - {@link BorderSide#NONE}.
     */
    private BorderSide borderTop;
    /**
     * Правая рамка. По умолчанию - {@link BorderSide#NONE}.
     */
    private BorderSide borderRight;
    /**
     * Нижняя рамка. По умолчанию - {@link BorderSide#NONE}.
     */
    private BorderSide borderBottom;
    /**
     * Левая рамка. По умолчанию - {@link BorderSide#NONE}.
     */
    private BorderSide borderLeft;
    /**
     * Скругление углов. В модели {@link me.manuloff.lib.table.renderer.style.table.BorderCollapse#COLLAPSE}
     * для ячеек не применяется. По умолчанию - без скругления.
     */
    private Corners borderRadius;

    /**
     * Верхний внутренний отступ. По умолчанию - 0.
     */
    private Double paddingTop;
    /**
     * Правый внутренний отступ. По умолчанию - 0.
     */
    private Double paddingRight;
    /**
     * Нижний внутренний отступ. По умолчанию - 0.
     */
    private Double paddingBottom;
    /**
     * Левый внутренний отступ. По умолчанию - 0.
     */
    private Double paddingLeft;

    /**
     * Тени блока; первая - верхняя. По умолчанию - нет.
     */
    private List<Shadow> boxShadow;

    /**
     * Контур снаружи рамки (не влияет на размеры). По умолчанию - нет.
     */
    private BorderSide outline;
    /**
     * Расстояние от рамки до контура. По умолчанию - 0.
     */
    private Double outlineOffset;

    /**
     * Непрозрачность 0..1: фон, рамка и содержимое как единое целое. По умолчанию - 1.
     */
    private Double opacity;
    /**
     * Скрыть отрисовку, сохранив место (аналог visibility: hidden). По умолчанию - false.
     */
    private Boolean hidden;
    /**
     * Обрезка содержимого по области под рамкой. Также включает маркер {@link #textOverflow}.
     * По умолчанию - {@link Overflow#VISIBLE}.
     */
    private Overflow overflow;

    // ============================================
    // Размеры
    // ============================================

    /**
     * Ширина ячейки с отступами и рамкой: в пикселях или процентах от ширины таблицы.
     * Содержимое шире заданного значения всё равно расширит колонку. По умолчанию - по содержимому.
     */
    private Size width;
    /**
     * Минимальная ширина ячейки. По умолчанию - нет.
     */
    private Double minWidth;
    /**
     * Максимальная ширина ячейки: текст переносится, чтобы уложиться в неё. По умолчанию - нет.
     */
    private Double maxWidth;
    /**
     * Минимальная высота ячейки (строки) с отступами и рамкой. По умолчанию - по содержимому.
     */
    private Double height;
    /**
     * Максимальная высота ячейки; лишнее содержимое обрезается при {@link Overflow#HIDDEN}. По умолчанию - нет.
     */
    private Double maxHeight;

    // ============================================
    // Выравнивание
    // ============================================

    /**
     * Выравнивание по горизонтали. По умолчанию - {@link TextAlign#LEFT}.
     */
    private TextAlign textAlign;
    /**
     * Выравнивание по вертикали. По умолчанию - {@link VerticalAlign#MIDDLE}.
     */
    private VerticalAlign verticalAlign;

    // ============================================
    // Шрифт
    // ============================================

    /**
     * Семейства шрифтов в порядке приоритета: имена установленных шрифтов, логические имена Java
     * ({@code SansSerif}, {@code Serif}, {@code Monospaced}, {@code Dialog}) или общие имена CSS
     * ({@code sans-serif}, {@code serif}, {@code monospace}). По умолчанию - {@code SansSerif}.
     */
    private List<String> fontFamily;
    /**
     * Размер шрифта в пикселях. По умолчанию - 14.
     */
    private Double fontSize;
    /**
     * Насыщенность 1..1000 (400 - обычный, 700 - жирный). По умолчанию - 400.
     */
    private Integer fontWeight;
    /**
     * Курсив. По умолчанию - false.
     */
    private Boolean italic;
    /**
     * Капитель: строчные буквы рисуются уменьшенными заглавными. По умолчанию - false.
     */
    private Boolean smallCaps;
    /**
     * Кернинг. По умолчанию - true.
     */
    private Boolean kerning;
    /**
     * Лигатуры. По умолчанию - true.
     */
    private Boolean ligatures;

    // ============================================
    // Текст
    // ============================================

    /**
     * Цвет текста. По умолчанию - чёрный.
     */
    private Color color;
    /**
     * Высота строки - множитель размера шрифта. По умолчанию - по метрикам шрифта (около 1.15).
     */
    private Double lineHeight;
    /**
     * Дополнительный интервал после каждого символа в пикселях. По умолчанию - 0.
     */
    private Double letterSpacing;
    /**
     * Дополнительный интервал после каждого пробела в пикселях. По умолчанию - 0.
     */
    private Double wordSpacing;
    /**
     * Отступ первой строки в пикселях. По умолчанию - 0.
     */
    private Double textIndent;
    /**
     * Преобразование регистра. По умолчанию - {@link TextTransform#NONE}.
     */
    private TextTransform textTransform;
    /**
     * Линии оформления. Распространяются на вложенные фрагменты. По умолчанию - нет.
     */
    private TextDecoration textDecoration;
    /**
     * Тени текста; первая - верхняя. По умолчанию - нет.
     */
    private List<Shadow> textShadow;
    /**
     * Толщина обводки букв. По умолчанию - 0.
     */
    private Double textStrokeWidth;
    /**
     * Цвет обводки букв. По умолчанию - цвет текста.
     */
    private Color textStrokeColor;

    // ============================================
    // Перенос и обрезка
    // ============================================

    /**
     * Обработка пробелов и переносов. По умолчанию - {@link WhiteSpace#PRE_LINE}: пробелы схлопываются,
     * символы {@code \n} в тексте переносят строку.
     */
    private WhiteSpace whiteSpace;
    /**
     * Разрыв внутри слов. По умолчанию - {@link WordBreak#NORMAL}.
     */
    private WordBreak wordBreak;
    /**
     * Разрыв слов, не помещающихся в строку. По умолчанию - {@link OverflowWrap#NORMAL}.
     */
    private OverflowWrap overflowWrap;
    /**
     * Учитывать мягкие переносы (U+00AD): при разрыве в этом месте рисуется дефис. По умолчанию - true.
     */
    private Boolean hyphens;
    /**
     * Ширина табуляции в пробелах (для {@link WhiteSpace#PRE} и {@link WhiteSpace#PRE_WRAP}). По умолчанию - 8.
     */
    private Integer tabSize;
    /**
     * Маркер обрезки текста, не поместившегося по ширине (при {@link Overflow#HIDDEN}) или по количеству строк
     * ({@link #lineClamp}). Пустая строка - обрезать без маркера. По умолчанию - пустая строка.
     */
    private String textOverflow;
    /**
     * Максимальное количество строк; последняя завершается маркером {@link #textOverflow}. По умолчанию - нет.
     */
    private Integer lineClamp;

    // ============================================
    // Фрагменты текста
    // ============================================

    /**
     * Верхний или нижний индекс (для фрагментов текста). По умолчанию - {@link BaselineShift#NONE}.
     */
    private BaselineShift baselineShift;
    /**
     * Подъём фрагмента над базовой линией в пикселях (отрицательное - опускание). По умолчанию - 0.
     */
    private Double baselineOffset;

    // ============================================
    // Создание и слияние
    // ============================================

    private static final Field[] FIELDS = collectFields();

    /**
     * @return новый пустой стиль
     */
    public static Style create() {
        return new Style();
    }

    /**
     * @return копия стиля
     */
    public Style copy() {
        return new Style().apply(this);
    }

    /**
     * Переносит в этот стиль все заданные свойства другого стиля (незаданные не затрагиваются).
     */
    public Style apply(@NonNull Style other) {
        try {
            for (Field f : FIELDS) {
                Object value = f.get(other);
                if (value != null) f.set(this, value);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    /**
     * Сбрасывает все свойства (делает их незаданными).
     */
    public Style clear() {
        try {
            for (Field f : FIELDS) f.set(this, null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    /**
     * @return true, если ни одно свойство не задано
     */
    public boolean isEmpty() {
        try {
            for (Field f : FIELDS) {
                if (f.get(this) != null) return false;
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return true;
    }

    private static Field[] collectFields() {
        List<Field> list = new ArrayList<>();
        for (Field f : Style.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            list.add(f);
        }
        return list.toArray(new Field[0]);
    }

    // ============================================
    // Фон
    // ============================================

    /**
     * @return цвет фона или null
     */
    public Color background() {
        return this.background;
    }

    /**
     * Задаёт цвет фона.
     */
    public Style background(Color color) {
        this.background = color;
        return this;
    }

    /**
     * Задаёт слои фона (градиенты, изображения); первый - верхний.
     */
    public Style background(BackgroundLayer... layers) {
        this.backgroundLayers = List.of(layers);
        return this;
    }

    /**
     * Задаёт цвет фона и слои поверх него.
     */
    public Style background(Color color, BackgroundLayer... layers) {
        this.background = color;
        this.backgroundLayers = List.of(layers);
        return this;
    }

    /**
     * Задаёт слои фона (копия списка).
     */
    public Style backgroundLayers(List<BackgroundLayer> layers) {
        this.backgroundLayers = layers == null ? null : List.copyOf(layers);
        return this;
    }

    // ============================================
    // Рамки
    // ============================================

    /**
     * Одинаковая рамка со всех сторон.
     */
    public Style border(BorderSide side) {
        return border(side, side, side, side);
    }

    /**
     * Сплошная рамка со всех сторон.
     */
    public Style border(double width, Color color) {
        return border(BorderSide.solid(width, color));
    }

    /**
     * Рамка указанного стиля со всех сторон.
     */
    public Style border(double width, BorderStyle style, Color color) {
        return border(BorderSide.of(width, style, color));
    }

    /**
     * Рамки каждой стороны.
     */
    public Style border(BorderSide top, BorderSide right, BorderSide bottom, BorderSide left) {
        this.borderTop = top;
        this.borderRight = right;
        this.borderBottom = bottom;
        this.borderLeft = left;
        return this;
    }

    /**
     * Верхняя и нижняя рамки.
     */
    public Style borderHorizontal(BorderSide side) {
        this.borderTop = side;
        this.borderBottom = side;
        return this;
    }

    /**
     * Левая и правая рамки.
     */
    public Style borderVertical(BorderSide side) {
        this.borderLeft = side;
        this.borderRight = side;
        return this;
    }

    /**
     * Убирает рамки со всех сторон.
     */
    public Style noBorder() {
        return border(BorderSide.NONE);
    }

    /**
     * Одинаковое скругление всех углов.
     */
    public Style borderRadius(double radius) {
        this.borderRadius = Corners.of(radius);
        return this;
    }

    /**
     * Скругление каждого угла.
     */
    public Style borderRadius(Corners corners) {
        this.borderRadius = corners;
        return this;
    }

    // ============================================
    // Отступы
    // ============================================

    /**
     * Одинаковые отступы со всех сторон.
     */
    public Style padding(double all) {
        return padding(all, all, all, all);
    }

    /**
     * Отступы: сверху и снизу - {@code vertical}, слева и справа - {@code horizontal}.
     */
    public Style padding(double vertical, double horizontal) {
        return padding(vertical, horizontal, vertical, horizontal);
    }

    /**
     * Отступы каждой стороны.
     */
    public Style padding(double top, double right, double bottom, double left) {
        this.paddingTop = nonNegative(top, "Padding");
        this.paddingRight = nonNegative(right, "Padding");
        this.paddingBottom = nonNegative(bottom, "Padding");
        this.paddingLeft = nonNegative(left, "Padding");
        return this;
    }

    /**
     * Отступы каждой стороны.
     */
    public Style padding(@NonNull Insets insets) {
        return padding(insets.getTop(), insets.getRight(), insets.getBottom(), insets.getLeft());
    }

    /**
     * Верхний отступ.
     */
    public Style paddingTop(double value) {
        this.paddingTop = nonNegative(value, "Padding");
        return this;
    }

    /**
     * Правый отступ.
     */
    public Style paddingRight(double value) {
        this.paddingRight = nonNegative(value, "Padding");
        return this;
    }

    /**
     * Нижний отступ.
     */
    public Style paddingBottom(double value) {
        this.paddingBottom = nonNegative(value, "Padding");
        return this;
    }

    /**
     * Левый отступ.
     */
    public Style paddingLeft(double value) {
        this.paddingLeft = nonNegative(value, "Padding");
        return this;
    }

    // ============================================
    // Эффекты
    // ============================================

    /**
     * @return тени блока или null
     */
    public List<Shadow> boxShadow() {
        return this.boxShadow;
    }

    /**
     * Задаёт тени блока; первая - верхняя.
     */
    public Style boxShadow(Shadow... shadows) {
        this.boxShadow = List.of(shadows);
        return this;
    }

    /**
     * Задаёт тени блока (копия списка).
     */
    public Style boxShadow(List<Shadow> shadows) {
        this.boxShadow = shadows == null ? null : List.copyOf(shadows);
        return this;
    }

    /**
     * Сплошной контур снаружи рамки.
     */
    public Style outline(double width, Color color) {
        this.outline = BorderSide.solid(width, color);
        return this;
    }

    /**
     * Расстояние от рамки до контура (может быть отрицательным - контур внутри).
     */
    public Style outlineOffset(double offset) {
        this.outlineOffset = offset;
        return this;
    }

    /**
     * Непрозрачность 0..1.
     */
    public Style opacity(double opacity) {
        if (opacity < 0 || opacity > 1) throw new IllegalArgumentException("Opacity must be in [0, 1]: " + opacity);
        this.opacity = opacity;
        return this;
    }

    // ============================================
    // Размеры
    // ============================================

    /**
     * Ширина в пикселях или процентах.
     */
    public Style width(Size width) {
        this.width = width;
        return this;
    }

    /**
     * Ширина в пикселях.
     */
    public Style width(double px) {
        this.width = Size.px(px);
        return this;
    }

    /**
     * Ширина в процентах от ширины таблицы.
     */
    public Style widthPercent(double percent) {
        this.width = Size.percent(percent);
        return this;
    }

    /**
     * Минимальная ширина в пикселях.
     */
    public Style minWidth(double px) {
        this.minWidth = nonNegative(px, "Min width");
        return this;
    }

    /**
     * Максимальная ширина в пикселях: текст будет переноситься.
     */
    public Style maxWidth(double px) {
        this.maxWidth = nonNegative(px, "Max width");
        return this;
    }

    /**
     * Минимальная высота в пикселях.
     */
    public Style height(double px) {
        this.height = nonNegative(px, "Height");
        return this;
    }

    /**
     * Максимальная высота в пикселях.
     */
    public Style maxHeight(double px) {
        this.maxHeight = nonNegative(px, "Max height");
        return this;
    }

    // ============================================
    // Выравнивание
    // ============================================

    /**
     * Выравнивание по горизонтали и вертикали.
     */
    public Style align(TextAlign horizontal, VerticalAlign vertical) {
        this.textAlign = horizontal;
        this.verticalAlign = vertical;
        return this;
    }

    /**
     * Выравнивание по левому краю.
     */
    public Style left() {
        this.textAlign = TextAlign.LEFT;
        return this;
    }

    /**
     * Выравнивание по правому краю (удобно для чисел).
     */
    public Style right() {
        this.textAlign = TextAlign.RIGHT;
        return this;
    }

    /**
     * Центрирование по горизонтали и вертикали.
     */
    public Style center() {
        return align(TextAlign.CENTER, VerticalAlign.MIDDLE);
    }

    // ============================================
    // Шрифт
    // ============================================

    /**
     * Семейство и размер шрифта.
     */
    public Style font(String family, double size) {
        this.fontFamily = List.of(family);
        return fontSize(size);
    }

    /**
     * @return семейства шрифтов или null
     */
    public List<String> fontFamily() {
        return this.fontFamily;
    }

    /**
     * Семейства шрифтов в порядке приоритета.
     */
    public Style fontFamily(String... families) {
        this.fontFamily = List.of(families);
        return this;
    }

    /**
     * Семейства шрифтов в порядке приоритета (копия списка).
     */
    public Style fontFamily(List<String> families) {
        this.fontFamily = families == null ? null : List.copyOf(families);
        return this;
    }

    /**
     * Размер шрифта в пикселях.
     */
    public Style fontSize(double size) {
        if (size <= 0) throw new IllegalArgumentException("Font size must be positive: " + size);
        this.fontSize = size;
        return this;
    }

    /**
     * Насыщенность шрифта 1..1000 (см. константы {@link #NORMAL}, {@link #BOLD} и др.).
     */
    public Style fontWeight(int weight) {
        if (weight < 1 || weight > 1000) throw new IllegalArgumentException("Font weight must be in [1, 1000]: " + weight);
        this.fontWeight = weight;
        return this;
    }

    /**
     * Жирный шрифт.
     */
    public Style bold() {
        return fontWeight(BOLD);
    }

    /**
     * Жирный или обычный шрифт.
     */
    public Style bold(boolean bold) {
        return fontWeight(bold ? BOLD : NORMAL);
    }

    /**
     * Курсив.
     */
    public Style italic() {
        this.italic = true;
        return this;
    }

    /**
     * @return курсив или null, если не задано
     */
    public Boolean isItalic() {
        return this.italic;
    }

    // ============================================
    // Текст
    // ============================================

    /**
     * Высота строки - множитель размера шрифта (1.5 - полуторный интервал).
     */
    public Style lineHeight(double factor) {
        this.lineHeight = nonNegative(factor, "Line height");
        return this;
    }

    /**
     * Дополнительный интервал после каждого символа.
     */
    public Style letterSpacing(double px) {
        this.letterSpacing = px;
        return this;
    }

    /**
     * Дополнительный интервал после каждого пробела.
     */
    public Style wordSpacing(double px) {
        this.wordSpacing = px;
        return this;
    }

    /**
     * Отступ первой строки.
     */
    public Style textIndent(double px) {
        this.textIndent = px;
        return this;
    }

    /**
     * Все буквы заглавные.
     */
    public Style uppercase() {
        this.textTransform = TextTransform.UPPERCASE;
        return this;
    }

    /**
     * Сплошное подчёркивание цветом текста.
     */
    public Style underline() {
        this.textDecoration = TextDecoration.underline();
        return this;
    }

    /**
     * Сплошное зачёркивание цветом текста.
     */
    public Style lineThrough() {
        this.textDecoration = TextDecoration.lineThrough();
        return this;
    }

    /**
     * Сплошное надчёркивание цветом текста.
     */
    public Style overline() {
        this.textDecoration = TextDecoration.overline();
        return this;
    }

    /**
     * @return тени текста или null
     */
    public List<Shadow> textShadow() {
        return this.textShadow;
    }

    /**
     * Тени текста; первая - верхняя. Расширение и внутренние тени не используются.
     */
    public Style textShadow(Shadow... shadows) {
        this.textShadow = List.of(shadows);
        return this;
    }

    /**
     * Тени текста (копия списка).
     */
    public Style textShadow(List<Shadow> shadows) {
        this.textShadow = shadows == null ? null : List.copyOf(shadows);
        return this;
    }

    /**
     * Обводка букв.
     */
    public Style textStroke(double width, Color color) {
        this.textStrokeWidth = nonNegative(width, "Text stroke width");
        this.textStrokeColor = color;
        return this;
    }

    // ============================================
    // Перенос и обрезка
    // ============================================

    /**
     * Текст в одну строку без переносов.
     */
    public Style nowrap() {
        this.whiteSpace = WhiteSpace.NOWRAP;
        return this;
    }

    /**
     * Одна строка, не поместившийся текст заменяется многоточием. Чтобы ограничить ширину ячейки,
     * задайте {@link #width(double)} или {@link #maxWidth(double)}.
     */
    public Style ellipsis() {
        this.whiteSpace = WhiteSpace.NOWRAP;
        this.overflow = Overflow.HIDDEN;
        this.textOverflow = ELLIPSIS;
        return this;
    }

    /**
     * Не больше {@code lines} строк; если маркер обрезки не задан, используется многоточие.
     */
    public Style lineClamp(int lines) {
        if (lines < 1) throw new IllegalArgumentException("Line clamp must be at least 1: " + lines);
        this.lineClamp = lines;
        if (this.textOverflow == null) this.textOverflow = ELLIPSIS;
        return this;
    }

    /**
     * Ширина табуляции в пробелах.
     */
    public Style tabSize(int spaces) {
        if (spaces < 0) throw new IllegalArgumentException("Tab size can't be negative: " + spaces);
        this.tabSize = spaces;
        return this;
    }

    // ============================================
    // Фрагменты текста
    // ============================================

    /**
     * Нижний индекс (обычно вместе с уменьшенным размером шрифта).
     */
    public Style sub() {
        this.baselineShift = BaselineShift.SUB;
        return this;
    }

    /**
     * Верхний индекс (обычно вместе с уменьшенным размером шрифта).
     */
    public Style sup() {
        this.baselineShift = BaselineShift.SUPER;
        return this;
    }

    /**
     * Подъём над базовой линией в пикселях (отрицательное значение - опускание).
     */
    public Style baselineOffset(double px) {
        this.baselineOffset = px;
        return this;
    }

    private static double nonNegative(double value, String name) {
        if (value < 0 || Double.isNaN(value)) throw new IllegalArgumentException(name + " can't be negative: " + value);
        return value;
    }
}
