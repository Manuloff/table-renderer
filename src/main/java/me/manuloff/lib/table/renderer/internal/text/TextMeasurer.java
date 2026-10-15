package me.manuloff.lib.table.renderer.internal.text;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Измерение и отрисовка строк с межбуквенным и межсловным интервалом. Кэширует ширины строк.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class TextMeasurer {

    private final Map<Font, Map<String, Double>> cache = new HashMap<>();

    /**
     * Ширина строки без интервалов.
     */
    public double width(Font font, String text) {
        if (text.isEmpty()) return 0;
        return this.cache.computeIfAbsent(font, f -> new HashMap<>())
                .computeIfAbsent(text, t -> font.getStringBounds(t, FontResolver.FRC).getWidth());
    }

    /**
     * Ширина строки с интервалами: letterSpacing добавляется после каждого символа, wordSpacing - после пробела.
     */
    public double width(Font font, String text, double letterSpacing, double wordSpacing) {
        if (letterSpacing == 0 && wordSpacing == 0) return width(font, text);
        // Те же части, что и при отрисовке, чтобы ширина точно совпадала с нарисованным
        if (letterSpacing == 0) {
            double w = 0;
            int start = 0;
            for (int i = 0; i <= text.length(); i++) {
                if (i == text.length() || text.charAt(i) == ' ') {
                    w += width(font, text.substring(start, i));
                    if (i < text.length()) w += width(font, " ") + wordSpacing;
                    start = i + 1;
                }
            }
            return w;
        }
        double w = 0;
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            w += width(font, new String(Character.toChars(cp))) + letterSpacing;
            if (cp == ' ') w += wordSpacing;
            i += Character.charCount(cp);
        }
        return w;
    }

    /**
     * Рисует строку текущим цветом/заливкой графики.
     */
    public void draw(Graphics2D g, Font font, String text, double x, double y, double letterSpacing, double wordSpacing) {
        g.setFont(font);
        if (letterSpacing == 0 && wordSpacing == 0) {
            g.drawString(text, (float) x, (float) y);
            return;
        }
        forEachPiece(font, text, x, letterSpacing, wordSpacing, (piece, px) -> g.drawString(piece, (float) px, (float) y));
    }

    /**
     * @return контур строки (для обводки и теней)
     */
    public Shape outline(Font font, String text, double x, double y, double letterSpacing, double wordSpacing) {
        Path2D.Double path = new Path2D.Double();
        forEachPiece(font, text, x, letterSpacing, wordSpacing, (piece, px) -> {
            if (piece.isEmpty() || piece.trim().isEmpty()) return;
            TextLayout layout = new TextLayout(piece, font, FontResolver.FRC);
            path.append(layout.getOutline(AffineTransform.getTranslateInstance(px, y)), false);
        });
        return path;
    }

    private interface PieceConsumer {
        void accept(String piece, double x);
    }

    /**
     * Делит строку на части, которые рисуются отдельно: по символам при letterSpacing, по словам при wordSpacing.
     */
    private void forEachPiece(Font font, String text, double x, double letterSpacing, double wordSpacing,
                              PieceConsumer consumer) {
        if (letterSpacing == 0 && wordSpacing == 0) {
            consumer.accept(text, x);
            return;
        }
        if (letterSpacing == 0) {
            int start = 0;
            double px = x;
            for (int i = 0; i <= text.length(); i++) {
                if (i == text.length() || text.charAt(i) == ' ') {
                    String word = text.substring(start, i);
                    if (!word.isEmpty()) consumer.accept(word, px);
                    px += width(font, word);
                    if (i < text.length()) px += width(font, " ") + wordSpacing;
                    start = i + 1;
                }
            }
            return;
        }
        double px = x;
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            String ch = new String(Character.toChars(cp));
            consumer.accept(ch, px);
            px += width(font, ch) + letterSpacing;
            if (cp == ' ') px += wordSpacing;
            i += Character.charCount(cp);
        }
    }
}
