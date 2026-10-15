package me.manuloff.lib.table.renderer.style;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.manuloff.lib.table.renderer.style.background.BackgroundLayer;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.border.Corners;
import me.manuloff.lib.table.renderer.style.box.Insets;
import me.manuloff.lib.table.renderer.style.box.Shadow;
import me.manuloff.lib.table.renderer.style.box.Size;
import me.manuloff.lib.table.renderer.style.table.BorderCollapse;
import me.manuloff.lib.table.renderer.style.table.CaptionSide;
import me.manuloff.lib.table.renderer.style.table.EmptyCells;
import me.manuloff.lib.table.renderer.style.table.TableLayout;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

/**
 * Стиль блока таблицы: фон, рамка, отступы, тень, размеры и параметры табличной раскладки.
 * Стиль ячеек задаётся отдельно - через {@link Style}.
 *
 * <pre>{@code
 * table.style(s -> s.separate(4).background(Color.WHITE).borderRadius(8).margin(16));
 * }</pre>
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
@ToString
public final class TableStyle {

    /**
     * Цвет фона таблицы. По умолчанию - нет.
     */
    private Color background;
    /**
     * Слои фона поверх цвета; первый - верхний.
     */
    @NonNull
    private List<BackgroundLayer> backgroundLayers = Collections.emptyList();

    /**
     * Верхняя рамка таблицы. В модели {@link BorderCollapse#COLLAPSE} рамки таблицы участвуют
     * в разрешении конфликтов с рамками крайних ячеек.
     */
    @NonNull
    private BorderSide borderTop = BorderSide.NONE;
    /**
     * Правая рамка таблицы.
     */
    @NonNull
    private BorderSide borderRight = BorderSide.NONE;
    /**
     * Нижняя рамка таблицы.
     */
    @NonNull
    private BorderSide borderBottom = BorderSide.NONE;
    /**
     * Левая рамка таблицы.
     */
    @NonNull
    private BorderSide borderLeft = BorderSide.NONE;
    /**
     * Скругление таблицы. Содержимое обрезается по скруглению; в модели {@link BorderCollapse#COLLAPSE}
     * одинаковая внешняя рамка рисуется скруглённой.
     */
    @NonNull
    private Corners borderRadius = Corners.ZERO;

    /**
     * Внутренний отступ между рамкой таблицы и ячейками (в модели {@link BorderCollapse#COLLAPSE} не используется).
     */
    @NonNull
    private Insets padding = Insets.ZERO;
    /**
     * Внешний отступ: пустое место вокруг таблицы и заголовка на изображении.
     */
    @NonNull
    private Insets margin = Insets.ZERO;

    /**
     * Тени таблицы; первая - верхняя. Изображение расширяется, чтобы вместить их.
     */
    @NonNull
    private List<Shadow> boxShadow = Collections.emptyList();
    /**
     * Контур снаружи рамки таблицы. По умолчанию - нет.
     */
    private BorderSide outline;
    /**
     * Расстояние от рамки до контура.
     */
    private double outlineOffset;
    /**
     * Непрозрачность таблицы целиком 0..1.
     */
    private double opacity = 1;

    /**
     * Ширина таблицы с рамкой (пиксели или проценты от {@code RenderOptions#availableWidth}).
     * По умолчанию - по содержимому.
     */
    private Size width;
    /**
     * Минимальная ширина таблицы с рамкой.
     */
    private Double minWidth;
    /**
     * Максимальная ширина таблицы с рамкой: текст в ячейках будет переноситься.
     */
    private Double maxWidth;
    /**
     * Минимальная высота таблицы: лишнее место распределяется между строками тела.
     */
    private Double height;

    /**
     * Модель рамок. По умолчанию - {@link BorderCollapse#COLLAPSE}.
     */
    @NonNull
    private BorderCollapse borderCollapse = BorderCollapse.COLLAPSE;
    /**
     * Расстояние между ячейками по горизонтали в модели {@link BorderCollapse#SEPARATE}.
     */
    private double horizontalSpacing;
    /**
     * Расстояние между ячейками по вертикали в модели {@link BorderCollapse#SEPARATE}.
     */
    private double verticalSpacing;
    /**
     * Алгоритм ширины колонок. По умолчанию - {@link TableLayout#AUTO}.
     */
    @NonNull
    private TableLayout layout = TableLayout.AUTO;
    /**
     * Положение заголовка. По умолчанию - {@link CaptionSide#TOP}.
     */
    @NonNull
    private CaptionSide captionSide = CaptionSide.TOP;
    /**
     * Отрисовка пустых ячеек в модели {@link BorderCollapse#SEPARATE}. По умолчанию - {@link EmptyCells#SHOW}.
     */
    @NonNull
    private EmptyCells emptyCells = EmptyCells.SHOW;

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
    public TableStyle background(Color color) {
        this.background = color;
        return this;
    }

    /**
     * Задаёт слои фона; первый - верхний.
     */
    public TableStyle background(BackgroundLayer... layers) {
        this.backgroundLayers = List.of(layers);
        return this;
    }

    /**
     * Задаёт слои фона (копия списка).
     */
    public TableStyle backgroundLayers(@NonNull List<BackgroundLayer> layers) {
        this.backgroundLayers = List.copyOf(layers);
        return this;
    }

    // ============================================
    // Рамка
    // ============================================

    /**
     * Одинаковая рамка со всех сторон.
     */
    public TableStyle border(@NonNull BorderSide side) {
        this.borderTop = side;
        this.borderRight = side;
        this.borderBottom = side;
        this.borderLeft = side;
        return this;
    }

    /**
     * Сплошная рамка со всех сторон.
     */
    public TableStyle border(double width, Color color) {
        return border(BorderSide.solid(width, color));
    }

    /**
     * Одинаковое скругление всех углов.
     */
    public TableStyle borderRadius(double radius) {
        this.borderRadius = Corners.of(radius);
        return this;
    }

    /**
     * Скругление каждого угла.
     */
    public TableStyle borderRadius(@NonNull Corners corners) {
        this.borderRadius = corners;
        return this;
    }

    // ============================================
    // Отступы
    // ============================================

    /**
     * Одинаковый внутренний отступ со всех сторон.
     */
    public TableStyle padding(double all) {
        this.padding = Insets.of(all);
        return this;
    }

    /**
     * Внутренние отступы каждой стороны.
     */
    public TableStyle padding(@NonNull Insets padding) {
        this.padding = padding;
        return this;
    }

    /**
     * Одинаковый внешний отступ со всех сторон.
     */
    public TableStyle margin(double all) {
        this.margin = Insets.of(all);
        return this;
    }

    /**
     * Внешние отступы каждой стороны.
     */
    public TableStyle margin(@NonNull Insets margin) {
        this.margin = margin;
        return this;
    }

    // ============================================
    // Эффекты
    // ============================================

    /**
     * @return тени таблицы
     */
    public List<Shadow> boxShadow() {
        return this.boxShadow;
    }

    /**
     * Задаёт тени таблицы; первая - верхняя.
     */
    public TableStyle boxShadow(Shadow... shadows) {
        this.boxShadow = List.of(shadows);
        return this;
    }

    /**
     * Непрозрачность таблицы целиком 0..1.
     */
    public TableStyle opacity(double opacity) {
        if (opacity < 0 || opacity > 1) throw new IllegalArgumentException("Opacity must be in [0, 1]: " + opacity);
        this.opacity = opacity;
        return this;
    }

    // ============================================
    // Размеры
    // ============================================

    /**
     * Ширина таблицы в пикселях или процентах.
     */
    public TableStyle width(Size width) {
        this.width = width;
        return this;
    }

    /**
     * Ширина таблицы в пикселях.
     */
    public TableStyle width(double px) {
        this.width = Size.px(px);
        return this;
    }

    /**
     * Ширина таблицы в процентах от {@code RenderOptions#availableWidth}.
     */
    public TableStyle widthPercent(double percent) {
        this.width = Size.percent(percent);
        return this;
    }

    /**
     * Минимальная ширина таблицы в пикселях.
     */
    public TableStyle minWidth(double px) {
        this.minWidth = px;
        return this;
    }

    /**
     * Максимальная ширина таблицы в пикселях.
     */
    public TableStyle maxWidth(double px) {
        this.maxWidth = px;
        return this;
    }

    /**
     * Минимальная высота таблицы в пикселях.
     */
    public TableStyle height(double px) {
        this.height = px;
        return this;
    }

    // ============================================
    // Модель рамок
    // ============================================

    /**
     * Раздельная модель рамок с одинаковым расстоянием между ячейками.
     */
    public TableStyle separate(double spacing) {
        return separate(spacing, spacing);
    }

    /**
     * Раздельная модель рамок: у каждой ячейки своя рамка, между ячейками - промежутки.
     */
    public TableStyle separate(double horizontal, double vertical) {
        if (horizontal < 0 || vertical < 0) throw new IllegalArgumentException("Spacing can't be negative");
        this.borderCollapse = BorderCollapse.SEPARATE;
        this.horizontalSpacing = horizontal;
        this.verticalSpacing = vertical;
        return this;
    }

    /**
     * Модель схлопывающихся рамок: соседние ячейки делят общую рамку.
     */
    public TableStyle collapse() {
        this.borderCollapse = BorderCollapse.COLLAPSE;
        return this;
    }
}
