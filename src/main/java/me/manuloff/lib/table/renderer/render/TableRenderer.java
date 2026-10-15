package me.manuloff.lib.table.renderer.render;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.Cell;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.internal.render.TableScene;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Рендер таблиц в изображение или на произвольную графику.
 *
 * <pre>{@code
 * RenderedTable result = TableRenderer.render(table, RenderOptions.builder().scale(2).build());
 * }</pre>
 * Обычно удобнее {@link Table#render()} / {@link Table#render(RenderOptions)}.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class TableRenderer {

    private TableRenderer() {
    }

    /**
     * Рисует таблицу в изображение. Размер изображения - внешний прямоугольник таблицы (с отступами
     * и заголовком), расширенный на выступающие тени и контуры, умноженный на масштаб.
     */
    public static RenderedTable render(@NonNull Table table, @NonNull RenderOptions options) {
        TableScene scene = TableScene.of(table, options);
        Rectangle2D canvas = scene.canvas();
        double scale = options.getScale();

        int width = Math.max(1, (int) Math.ceil(canvas.getWidth() * scale - 1e-6));
        int height = Math.max(1, (int) Math.ceil(canvas.getHeight() * scale - 1e-6));
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();
        try {
            g.scale(scale, scale);
            if (options.getBackground() != null) {
                g.setColor(options.getBackground());
                g.fill(new Rectangle2D.Double(0, 0, canvas.getWidth(), canvas.getHeight()));
            }
            g.translate(-canvas.getX(), -canvas.getY());
            scene.paint(g, options);
        } finally {
            g.dispose();
        }

        // Геометрия - в координатах изображения (логические пиксели)
        double dx = -canvas.getX();
        double dy = -canvas.getY();
        IdentityHashMap<Cell, Rectangle2D> cells = new IdentityHashMap<>();
        for (Map.Entry<Cell, Rectangle2D> e : scene.cellRects().entrySet()) cells.put(e.getKey(), shift(e.getValue(), dx, dy));
        Rectangle2D caption = scene.captionRect();
        return new RenderedTable(image, canvas.getWidth(), canvas.getHeight(), scale, shift(scene.tableRect(), dx, dy),
                caption == null ? null : shift(caption, dx, dy), cells);
    }

    /**
     * Рисует таблицу на переданной графике, начиная с точки (0, 0) её текущей системы координат.
     * Подходит для векторного вывода через сторонние реализации {@link Graphics2D} (SVG, PDF).
     * Фон из параметров не заливается; масштаб из параметров влияет только на привязку к пикселям.
     * Тени и контуры могут выходить за пределы возвращаемого прямоугольника, если у таблицы нет внешнего отступа.
     *
     * @return размер таблицы вместе с внешним отступом и заголовком
     */
    public static Rectangle2D paint(@NonNull Table table, @NonNull Graphics2D g, @NonNull RenderOptions options) {
        TableScene scene = TableScene.of(table, options);
        scene.paint(g, options);
        return scene.outer();
    }

    private static Rectangle2D shift(Rectangle2D r, double dx, double dy) {
        return new Rectangle2D.Double(r.getX() + dx, r.getY() + dy, r.getWidth(), r.getHeight());
    }
}
