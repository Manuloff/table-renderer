package me.manuloff.lib.table.renderer.style;

/**
 * Пресет качества рендеринга.
 * Влияет на использование Anti-Aliasing и других графических настроек.
 *
 * @author Manuloff
 * @since 1.0
 */
public enum RenderQuality {

    /**
     * Минимальное качество: быстрая отрисовка без сглаживания
     */
    LOW(1.0, false, false, false),

    /**
     * Среднее качество: базовое сглаживание текста
     */
    MEDIUM(1.5, true, false, false),

    /**
     * Высокое качество: полное сглаживание
     */
    HIGH(2.0, true, true, false),

    /**
     * Максимальное качество: всё включено
     */
    MAXIMUM(2.0, true, true, true);

    private final double scale;
    private final boolean antialiasing;
    private final boolean fractionalMetrics;
    private final boolean highQuality;

    RenderQuality(double scale, boolean antialiasing, boolean fractionalMetrics, boolean highQuality) {
        this.scale = scale;
        this.antialiasing = antialiasing;
        this.fractionalMetrics = fractionalMetrics;
        this.highQuality = highQuality;
    }

    /**
     * @return множитель размера итогового {@code BufferedImage} относительно логического размера таблицы
     */
    public double getScale() {
        return scale;
    }

    /**
     * @return включено ли сглаживание фигур и текста
     */
    public boolean isAntialiasing() {
        return antialiasing;
    }

    /**
     * @return использовать ли дробные метрики шрифта
     */
    public boolean isFractionalMetrics() {
        return fractionalMetrics;
    }

    /**
     * @return включать ли набор hints максимального качества отрисовки
     */
    public boolean isHighQuality() {
        return highQuality;
    }
}
