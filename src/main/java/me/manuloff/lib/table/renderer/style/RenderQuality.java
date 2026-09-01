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

    public double getScale() {
        return scale;
    }

    public boolean isAntialiasing() {
        return antialiasing;
    }

    public boolean isFractionalMetrics() {
        return fractionalMetrics;
    }

    public boolean isHighQuality() {
        return highQuality;
    }
}
