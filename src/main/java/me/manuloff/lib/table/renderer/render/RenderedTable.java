package me.manuloff.lib.table.renderer.render;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.Cell;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Результат рендера: изображение и геометрия элементов таблицы.
 * Координаты геометрии - в логических пикселях (без учёта {@code RenderOptions.getScale()}).
 *
 * @author Manuloff
 * @since 2.0
 */
public final class RenderedTable {

    private final BufferedImage image;
    private final double width;
    private final double height;
    private final double scale;
    private final Rectangle2D tableBounds;
    private final Rectangle2D captionBounds;
    private final Map<Cell, Rectangle2D> cellBounds;

    RenderedTable(BufferedImage image, double width, double height, double scale, Rectangle2D tableBounds,
                  Rectangle2D captionBounds, IdentityHashMap<Cell, Rectangle2D> cellBounds) {
        this.image = image;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.tableBounds = tableBounds;
        this.captionBounds = captionBounds;
        this.cellBounds = Collections.unmodifiableMap(cellBounds);
    }

    /**
     * @return изображение (размер - логический размер, умноженный на масштаб)
     */
    public BufferedImage image() {
        return this.image;
    }

    /**
     * @return ширина в логических пикселях
     */
    public double width() {
        return this.width;
    }

    /**
     * @return высота в логических пикселях
     */
    public double height() {
        return this.height;
    }

    /**
     * @return масштаб рендера
     */
    public double scale() {
        return this.scale;
    }

    /**
     * @return прямоугольник таблицы (по внешнему краю рамки)
     */
    public Rectangle2D tableBounds() {
        return (Rectangle2D) this.tableBounds.clone();
    }

    /**
     * @return прямоугольник заголовка или null, если его нет
     */
    public Rectangle2D captionBounds() {
        return this.captionBounds == null ? null : (Rectangle2D) this.captionBounds.clone();
    }

    /**
     * @return прямоугольник ячейки (по внешнему краю рамки) или null, если ячейка не отрисована
     */
    public Rectangle2D cellBounds(@NonNull Cell cell) {
        Rectangle2D r = this.cellBounds.get(cell);
        return r == null ? null : (Rectangle2D) r.clone();
    }

    /**
     * @return ячейка в точке (логические пиксели) или null
     */
    public Cell cellAt(double x, double y) {
        for (Map.Entry<Cell, Rectangle2D> e : this.cellBounds.entrySet()) {
            if (e.getValue().contains(x, y)) return e.getKey();
        }
        return null;
    }

    // === Сохранение ===

    /**
     * Записывает изображение в формате PNG, JPEG, BMP или GIF.
     * Для форматов без прозрачности фон заливается белым.
     */
    public void write(@NonNull String format, @NonNull OutputStream out) {
        try {
            BufferedImage img = this.image;
            String f = format.toLowerCase(Locale.ROOT);
            if (f.equals("jpg") || f.equals("jpeg") || f.equals("bmp")) {
                img = new BufferedImage(this.image.getWidth(), this.image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, img.getWidth(), img.getHeight());
                g.drawImage(this.image, 0, 0, null);
                g.dispose();
            }
            if (!ImageIO.write(img, f, out)) {
                throw new IllegalArgumentException("Unsupported image format: " + format);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Записывает изображение в файл; формат определяется по расширению (по умолчанию PNG).
     */
    public void write(@NonNull Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String format = dot < 0 ? "png" : name.substring(dot + 1);
        try (OutputStream out = Files.newOutputStream(path)) {
            write(format, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Записывает изображение в файл; формат определяется по расширению (по умолчанию PNG).
     */
    public void write(@NonNull File file) {
        write(file.toPath());
    }

    /**
     * Записывает изображение в PNG независимо от расширения файла.
     */
    public void writePng(@NonNull Path path) {
        try (OutputStream out = Files.newOutputStream(path)) {
            write("png", out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return изображение в формате PNG
     */
    public byte[] toPng() {
        return toBytes("png");
    }

    /**
     * @return изображение в указанном формате (png, jpg, bmp, gif)
     */
    public byte[] toBytes(@NonNull String format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(format, out);
        return out.toByteArray();
    }
}
