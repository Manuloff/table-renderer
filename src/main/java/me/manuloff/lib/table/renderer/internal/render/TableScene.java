package me.manuloff.lib.table.renderer.internal.render;

import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.internal.layout.CaptionBox;
import me.manuloff.lib.table.renderer.internal.layout.CellBox;
import me.manuloff.lib.table.renderer.internal.layout.LaidOutTable;
import me.manuloff.lib.table.renderer.internal.layout.TableLayoutEngine;
import me.manuloff.lib.table.renderer.internal.paint.BoxPainter;
import me.manuloff.lib.table.renderer.internal.text.FontResolver;
import me.manuloff.lib.table.renderer.internal.text.TextMeasurer;
import me.manuloff.lib.table.renderer.internal.text.TextPainter;
import me.manuloff.lib.table.renderer.render.RenderOptions;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.box.Insets;
import me.manuloff.lib.table.renderer.style.table.CaptionSide;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Разложенная таблица, размещённая на холсте: внешние отступы, заголовок над или под таблицей
 * и холст, вмещающий выступающие тени и контуры.
 * <p>
 * Координаты сцены: (0, 0) - левый верхний угол внешнего прямоугольника (с отступами).
 * Холст ({@link #canvas()}) может начинаться левее/выше, если тени выходят за отступы.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class TableScene {

    private final LaidOutTable layout;
    private final TablePainter tablePainter;
    private final CaptionPainter captionPainter;
    private final Rectangle2D tableRect;
    private final Rectangle2D captionRect;
    private final Rectangle2D outer;
    private final Rectangle2D canvas;

    private TableScene(Table table, RenderOptions options) {
        FontResolver fonts = new FontResolver(options.getFallbackFonts());
        TextMeasurer measurer = new TextMeasurer();
        TextPainter text = new TextPainter(measurer);
        this.layout = TableLayoutEngine.layout(table, options, fonts, measurer);
        this.tablePainter = new TablePainter(this.layout, text);

        CaptionBox caption = this.layout.caption();
        this.captionPainter = caption == null ? null : new CaptionPainter(caption, text);

        TableStyle ts = table.style();
        Insets margin = ts.margin();
        double w = this.layout.width();
        double h = this.layout.height();
        double ch = caption == null ? 0 : caption.height();
        boolean captionOnTop = ts.captionSide() == CaptionSide.TOP;

        double tableY = margin.getTop() + (captionOnTop ? ch : 0);
        this.tableRect = new Rectangle2D.Double(margin.getLeft(), tableY, w, h);
        this.captionRect = caption == null ? null : new Rectangle2D.Double(margin.getLeft(),
                captionOnTop ? margin.getTop() : tableY + h, w, ch);
        this.outer = new Rectangle2D.Double(0, 0, margin.horizontal() + w, margin.vertical() + h + ch);
        this.canvas = computeCanvas();
    }

    /**
     * Раскладывает таблицу и размещает её на холсте.
     */
    public static TableScene of(Table table, RenderOptions options) {
        return new TableScene(table, options);
    }

    /**
     * Холст: внешний прямоугольник вместе с выступающими тенями и контурами, выровненный по целым пикселям.
     */
    private Rectangle2D computeCanvas() {
        Rectangle2D c = (Rectangle2D) this.outer.clone();
        Rectangle2D ink = TablePainter.inkBounds(this.layout);
        c.add(new Rectangle2D.Double(ink.getX() + this.tableRect.getX(), ink.getY() + this.tableRect.getY(),
                ink.getWidth(), ink.getHeight()));
        if (this.captionRect != null) c.add(BoxPainter.expand(this.captionRect, this.captionPainter.inkOverflow()));
        double x0 = Math.floor(c.getX());
        double y0 = Math.floor(c.getY());
        return new Rectangle2D.Double(x0, y0, Math.ceil(c.getMaxX()) - x0, Math.ceil(c.getMaxY()) - y0);
    }

    // ============================================
    // Геометрия
    // ============================================

    /**
     * @return внешний прямоугольник: таблица, заголовок и внешние отступы
     */
    public Rectangle2D outer() {
        return (Rectangle2D) this.outer.clone();
    }

    /**
     * @return холст: внешний прямоугольник с выступающими тенями и контурами
     */
    public Rectangle2D canvas() {
        return (Rectangle2D) this.canvas.clone();
    }

    /**
     * @return прямоугольник блока таблицы
     */
    public Rectangle2D tableRect() {
        return (Rectangle2D) this.tableRect.clone();
    }

    /**
     * @return прямоугольник заголовка или null
     */
    public Rectangle2D captionRect() {
        return this.captionRect == null ? null : (Rectangle2D) this.captionRect.clone();
    }

    /**
     * @return прямоугольники ячеек в координатах сцены
     */
    public Map<Cell, Rectangle2D> cellRects() {
        Map<Cell, Rectangle2D> map = new IdentityHashMap<>();
        for (CellBox cb : this.layout.cells()) {
            Rectangle2D b = cb.bounds();
            map.put(cb.cell(), new Rectangle2D.Double(b.getX() + this.tableRect.getX(), b.getY() + this.tableRect.getY(),
                    b.getWidth(), b.getHeight()));
        }
        return map;
    }

    // ============================================
    // Отрисовка
    // ============================================

    /**
     * Рисует сцену в координатах сцены.
     */
    public void paint(Graphics2D g, RenderOptions options) {
        Graphics2D sg = (Graphics2D) g.create();
        try {
            configure(sg, options);
            if (this.captionRect != null) this.captionPainter.paint(sg, this.captionRect);
            sg.translate(this.tableRect.getX(), this.tableRect.getY());
            this.tablePainter.paint(sg);
        } finally {
            sg.dispose();
        }
    }

    private static void configure(Graphics2D g, RenderOptions options) {
        boolean aa = options.isAntialiasing();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                aa ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                aa ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        // Дробные метрики обязательны: раскладка текста посчитана с ними
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}
