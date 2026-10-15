package me.manuloff.lib.table.renderer.internal.paint.border;

import me.manuloff.lib.table.renderer.internal.layout.CollapsedBorders;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.border.BorderStyle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Отрисовка общих рамок модели схлопывания. Рамка рисуется по центру линии сетки; соседние одинаковые
 * отрезки объединяются (непрерывный пунктир), отрезки удлиняются на половину толщины перпендикулярных рамок,
 * чтобы закрыть углы. Более сильные рамки рисуются позже и перекрывают слабые на стыках.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class CollapsedBorderPainter {

    /**
     * Отрезок рамки: для горизонтальных - y линии и x начала/конца, для вертикальных - наоборот.
     */
    private static final class Segment {
        final BorderSide side;
        final boolean horizontal;
        final double line;
        final double from;
        final double to;

        Segment(BorderSide side, boolean horizontal, double line, double from, double to) {
            this.side = side;
            this.horizontal = horizontal;
            this.line = line;
            this.from = from;
            this.to = to;
        }
    }

    private CollapsedBorderPainter() {
    }

    /**
     * @param xs        x линий колонок (cols + 1)
     * @param ys        y линий строк (rows + 1)
     * @param skipOuter не рисовать внешние рамки (их рисует скруглённый контур таблицы)
     */
    public static void paint(Graphics2D g, CollapsedBorders borders, double[] xs, double[] ys, boolean skipOuter) {
        List<Segment> segments = new ArrayList<>();
        collectHorizontal(borders, xs, ys, skipOuter, segments);
        collectVertical(borders, xs, ys, skipOuter, segments);
        segments.sort(Comparator.<Segment>comparingDouble(s -> s.side.getWidth())
                .thenComparingInt(s -> CollapsedBorders.rank(s.side.getStyle())));
        for (Segment s : segments) paintSegment(g, s);
    }

    private static void collectHorizontal(CollapsedBorders b, double[] xs, double[] ys, boolean skipOuter,
                                          List<Segment> out) {
        for (int r = 0; r <= b.rows(); r++) {
            if (skipOuter && (r == 0 || r == b.rows())) continue;
            int c = 0;
            while (c < b.cols()) {
                BorderSide side = b.horizontal(r, c);
                if (side == null || !side.isVisible()) {
                    c++;
                    continue;
                }
                int end = c + 1;
                while (end < b.cols() && side.equals(b.horizontal(r, end))) end++;
                double from = xs[c] - verticalJoint(b, r, c) / 2;
                double to = xs[end] + verticalJoint(b, r, end) / 2;
                out.add(new Segment(side, true, ys[r], from, to));
                c = end;
            }
        }
    }

    private static void collectVertical(CollapsedBorders b, double[] xs, double[] ys, boolean skipOuter,
                                        List<Segment> out) {
        for (int c = 0; c <= b.cols(); c++) {
            if (skipOuter && (c == 0 || c == b.cols())) continue;
            int r = 0;
            while (r < b.rows()) {
                BorderSide side = b.vertical(r, c);
                if (side == null || !side.isVisible()) {
                    r++;
                    continue;
                }
                int end = r + 1;
                while (end < b.rows() && side.equals(b.vertical(end, c))) end++;
                double from = ys[r] - horizontalJoint(b, r, c) / 2;
                double to = ys[end] + horizontalJoint(b, end, c) / 2;
                out.add(new Segment(side, false, xs[c], from, to));
                r = end;
            }
        }
    }

    /**
     * @return толщина вертикальных рамок в узле (линия строк {@code rowLine}, линия колонок {@code colLine})
     */
    private static double verticalJoint(CollapsedBorders b, int rowLine, int colLine) {
        double w = 0;
        if (rowLine > 0) w = Math.max(w, b.verticalWidth(rowLine - 1, colLine));
        if (rowLine < b.rows()) w = Math.max(w, b.verticalWidth(rowLine, colLine));
        return w;
    }

    /**
     * @return толщина горизонтальных рамок в узле (линия строк {@code rowLine}, линия колонок {@code colLine})
     */
    private static double horizontalJoint(CollapsedBorders b, int rowLine, int colLine) {
        double w = 0;
        if (colLine > 0) w = Math.max(w, b.horizontalWidth(rowLine, colLine - 1));
        if (colLine < b.cols()) w = Math.max(w, b.horizontalWidth(rowLine, colLine));
        return w;
    }

    private static void paintSegment(Graphics2D g, Segment s) {
        BorderSide side = s.side;
        double w = side.getWidth();
        double length = s.to - s.from;
        if (length <= 0) return;
        Rectangle2D.Double rect = s.horizontal
                ? new Rectangle2D.Double(s.from, s.line - w / 2, length, w)
                : new Rectangle2D.Double(s.line - w / 2, s.from, w, length);
        Color c = side.getColor();

        switch (side.getStyle()) {
            case DOUBLE:
                g.setColor(c);
                g.fill(part(rect, s.horizontal, 0, 1.0 / 3));
                g.fill(part(rect, s.horizontal, 2.0 / 3, 1));
                break;
            case DASHED:
            case DOTTED:
                g.setColor(c);
                g.setStroke(BorderStrokes.dashed(side.getStyle(), w));
                g.draw(s.horizontal
                        ? new Line2D.Double(rect.x, s.line, rect.getMaxX(), s.line)
                        : new Line2D.Double(s.line, rect.y, s.line, rect.getMaxY()));
                g.setStroke(new BasicStroke());
                break;
            case GROOVE:
            case RIDGE:
            case INSET:
            case OUTSET: {
                // В модели схлопывания inset рисуется как ridge, outset - как groove
                boolean groove = side.getStyle() == BorderStyle.GROOVE || side.getStyle() == BorderStyle.OUTSET;
                g.setColor(groove ? BorderStrokes.darker(c) : BorderStrokes.lighter(c));
                g.fill(part(rect, s.horizontal, 0, 0.5));
                g.setColor(groove ? BorderStrokes.lighter(c) : BorderStrokes.darker(c));
                g.fill(part(rect, s.horizontal, 0.5, 1));
                break;
            }
            default:
                g.setColor(c);
                g.fill(rect);
                break;
        }
    }

    /**
     * @return полоса отрезка между долями толщины {@code from} и {@code to}
     */
    private static Rectangle2D part(Rectangle2D.Double r, boolean horizontal, double from, double to) {
        return horizontal
                ? new Rectangle2D.Double(r.x, r.y + r.height * from, r.width, r.height * (to - from))
                : new Rectangle2D.Double(r.x + r.width * from, r.y, r.width * (to - from), r.height);
    }
}
