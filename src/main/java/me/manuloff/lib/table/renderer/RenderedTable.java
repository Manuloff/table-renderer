package me.manuloff.lib.table.renderer;

import lombok.Getter;
import me.manuloff.lib.table.renderer.style.*;


import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Рендерер таблицы в изображение.
 *
 * @author Manuloff
 * @since 1.0
 */
@Getter
public final class RenderedTable {

    private final Table table;
    private final int width;
    private final int height;
    private final BufferedImage image;

    public BufferedImage getImage() {
        return this.image;
    }

    RenderedTable(Table table) {
        this.table = table;

        RenderQuality quality = table.renderQuality();
        double scale = quality.getScale();

        // 1. Расчёт размеров колонок и строк
        Map<Integer, Integer> colWidths = calculateColumnWidths();
        Map<Integer, Integer> rowHeights = calculateRowHeights();

        // 2. Применяем фиксированные размеры таблицы
        this.width = applyTableWidth(colWidths);
        this.height = applyTableHeight(rowHeights);

        // 3. Создаём изображение
        int imageWidth = (int) Math.ceil(this.width * scale);
        int imageHeight = (int) Math.ceil(this.height * scale);
        this.image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        // 4. Рендеринг
        Graphics2D g = this.image.createGraphics();
        g.scale(scale, scale);

        configureGraphics(g, quality);

        // 5. Отрисовка таблицы
        if (table.tableStyle().boxShadow() != null && table.tableStyle().boxShadow().hasShadows()) {
            drawTableWithShadow(g, colWidths, rowHeights);
        } else {
            drawTable(g, colWidths, rowHeights);
        }

        g.dispose();
    }

    private void configureGraphics(Graphics2D g, RenderQuality quality) {
        // Anti-aliasing
        if (quality.isAntialiasing()) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        // Качество рендеринга
        if (quality.isHighQuality()) {
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        }

        // Дробные метрики
        if (quality.isFractionalMetrics()) {
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
    }

    // =====================
    // Расчёт ширины колонок
    // =====================

    private Map<Integer, Integer> calculateColumnWidths() {
        Map<Integer, Integer> colWidths = new HashMap<>();

        // AUTO layout - считаем по содержимому
        Graphics2D g = createTempGraphics();

        int colCount = table.getColumnCount();
        for (int col = 0; col < colCount; col++) {
            int maxWidth = 0;

            // Проверяем фиксированную ширину колонки
            Column column = table.getColumn(col);
            if (column.fixedWidth() != null) {
                colWidths.put(col, column.fixedWidth());
                continue;
            }

            for (Cell cell : table.cells()) {
                if (cell.getCol() == col) {
                    int cellWidth = estimateCellWidth(g, cell);
                    maxWidth = Math.max(maxWidth, cellWidth);
                }
            }

            colWidths.put(col, Math.max(maxWidth, 10));
        }

        g.dispose();
        return colWidths;
    }

    private int estimateCellWidth(Graphics2D g, Cell cell) {
        TableStyle ts = resolveTableStyle(cell);
        TextStyle txtStyle = resolveTextStyle(cell);

        int contentWidth = estimateTextWidth(g, cell.getTexts(), txtStyle);
        Padding padding = ts.padding() != null ? ts.padding() : new Padding(1);

        int width = contentWidth + padding.left() + padding.right();

        // Применяем ограничения ячейки
        if (cell.minWidth() != null) {
            width = Math.max(width, cell.minWidth());
        }
        if (cell.maxWidth() != null) {
            width = Math.min(width, cell.maxWidth());
        }

        // Учитываем colspan
        int perCol = width / cell.getColspan();

        return perCol;
    }

    // =====================
    // Расчёт высоты строк
    // =====================

    private Map<Integer, Integer> calculateRowHeights() {
        Map<Integer, Integer> rowHeights = new HashMap<>();
        Graphics2D g = createTempGraphics();

        int rowCount = table.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            // Проверяем фиксированную высоту строки
            Row tableRow = table.getRow(row);
            if (tableRow.fixedHeight() != null) {
                rowHeights.put(row, tableRow.fixedHeight());
                continue;
            }

            int maxHeight = 0;

            for (Cell cell : table.cells()) {
                if (cell.getRow() == row) {
                    int cellHeight = estimateCellHeight(g, cell);
                    maxHeight = Math.max(maxHeight, cellHeight);
                }
            }

            rowHeights.put(row, Math.max(maxHeight, 10));
        }

        g.dispose();
        return rowHeights;
    }

    private int estimateCellHeight(Graphics2D g, Cell cell) {
        TableStyle ts = resolveTableStyle(cell);
        TextStyle txtStyle = resolveTextStyle(cell);

        // Получаем ширину колонки для расчёта переноса
        int availableWidth = getColumnAvailableWidth(cell.getCol(), cell.getColspan());
        Padding padding = ts.padding() != null ? ts.padding() : new Padding(1);

        int contentHeight = estimateTextHeight(g, cell.getTexts(), txtStyle, availableWidth - padding.left() - padding.right());

        int height = contentHeight + padding.top() + padding.bottom();

        // Применяем ограничения
        if (cell.minHeight() != null) {
            height = Math.max(height, cell.minHeight());
        }
        if (cell.maxHeight() != null) {
            height = Math.min(height, cell.maxHeight());
        }

        // Учитываем rowspan
        int perRow = height / cell.getRowspan();

        return perRow;
    }

    private int getColumnAvailableWidth(int col, int colspan) {
        // Это упрощённый расчёт - в реальности нужно знать точные ширины
        return 100 * colspan; // Заглушка
    }

    // =====================
    // Применение размеров таблицы
    // =====================

    private int applyTableWidth(Map<Integer, Integer> colWidths) {
        if (table.fixedWidth() != null) {
            int currentWidth = colWidths.values().stream().mapToInt(Integer::intValue).sum();
            if (currentWidth < table.fixedWidth()) {
                int diff = table.fixedWidth() - currentWidth;
                int extraPerCol = diff / colWidths.size();
                for (Integer key : colWidths.keySet()) {
                    colWidths.merge(key, extraPerCol, Integer::sum);
                }
            }
            return table.fixedWidth();
        }
        return colWidths.values().stream().mapToInt(Integer::intValue).sum();
    }

    private int applyTableHeight(Map<Integer, Integer> rowHeights) {
        if (table.fixedHeight() != null) {
            int currentHeight = rowHeights.values().stream().mapToInt(Integer::intValue).sum();
            if (currentHeight < table.fixedHeight()) {
                int diff = table.fixedHeight() - currentHeight;
                int extraPerRow = diff / rowHeights.size();
                for (Integer key : rowHeights.keySet()) {
                    rowHeights.merge(key, extraPerRow, Integer::sum);
                }
            }
            return table.fixedHeight();
        }
        return rowHeights.values().stream().mapToInt(Integer::intValue).sum();
    }

    // =====================
    // Отрисовка таблицы
    // =====================

    private void drawTable(Graphics2D g, Map<Integer, Integer> colWidths, Map<Integer, Integer> rowHeights) {
        TableStyle ts = table.tableStyle();
        BorderCollapse collapse = ts.borderCollapse() != null ? ts.borderCollapse() : BorderCollapse.COLLAPSE;

        // Фон таблицы
        if (ts.backgroundColor() != null) {
            g.setColor(ts.backgroundColor());
            Float radius = ts.borderRadius();
            if (radius != null && radius > 0) {
                g.fill(new RoundRectangle2D.Float(0, 0, this.width, this.height, radius * 2, radius * 2));
            } else {
                g.fillRect(0, 0, this.width, this.height);
            }
        }

        // Box-shadow таблицы
        BoxShadow shadow = ts.boxShadow();
        if (shadow != null && shadow.hasShadows()) {
            drawBoxShadow(g, shadow, this.width, this.height);
        }

        // Вычисляем позиции
        int[] colXs = calculatePositions(colWidths);
        int[] rowYs = calculatePositions(rowHeights);

        // Border-spacing для SEPARATE режима
        int spacing = 0;
        if (collapse == BorderCollapse.SEPARATE && ts.borderSpacing() != null) {
            spacing = ts.borderSpacing();
        }

        // Отрисовка ячеек
        for (Cell cell : table.cells()) {
            int x1 = colXs[cell.getCol()] + (collapse == BorderCollapse.SEPARATE ? spacing / 2 : 0);
            int x2 = colXs[cell.getCol() + cell.getColspan()] - (collapse == BorderCollapse.SEPARATE ? spacing / 2 : 0);
            int y1 = rowYs[cell.getRow()] + (collapse == BorderCollapse.SEPARATE ? spacing / 2 : 0);
            int y2 = rowYs[cell.getRow() + cell.getRowspan()] - (collapse == BorderCollapse.SEPARATE ? spacing / 2 : 0);

            Rectangle rect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
            drawCell(g, cell, rect, collapse);
        }
    }

    private void drawTableWithShadow(Graphics2D g, Map<Integer, Integer> colWidths, Map<Integer, Integer> rowHeights) {
        // Сначала рисуем тень на отдельном слое
        BoxShadow shadow = table.tableStyle().boxShadow();

        // Создаём промежуточное изображение для тени
        BufferedImage shadowImage = new BufferedImage(this.width + 20, this.height + 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D shadowG = shadowImage.createGraphics();

        // Рисуем тень
        drawBoxShadow(shadowG, shadow, this.width, this.height);

        shadowG.dispose();

        // Рисуем тень на основное изображение
        g.drawImage(shadowImage, 10, 10, null);

        // Сдвигаем координаты для отрисовки таблицы
        Graphics2D shiftedG = (Graphics2D) g.create();
        shiftedG.translate(10, 10);

        // Фон таблицы
        TableStyle ts = table.tableStyle();
        if (ts.backgroundColor() != null) {
            shiftedG.setColor(ts.backgroundColor());
            Float radius = ts.borderRadius();
            if (radius != null && radius > 0) {
                shiftedG.fill(new RoundRectangle2D.Float(0, 0, this.width, this.height, radius * 2, radius * 2));
            } else {
                shiftedG.fillRect(0, 0, this.width, this.height);
            }
        }

        // Остальная отрисовка (без повторного рисования shadow)
        BorderCollapse collapse = ts.borderCollapse() != null ? ts.borderCollapse() : BorderCollapse.COLLAPSE;

        int[] colXs = calculatePositions(colWidths);
        int[] rowYs = calculatePositions(rowHeights);

        int spacing = 0;
        if (collapse == BorderCollapse.SEPARATE && ts.borderSpacing() != null) {
            spacing = ts.borderSpacing();
        }

        for (Cell cell : table.cells()) {
            int x1 = colXs[cell.getCol()] + (collapse == BorderCollapse.SEPARATE ? spacing / 2 : 0);
            int x2 = colXs[cell.getCol() + cell.getColspan()] - (collapse == BorderCollapse.SEPARATE ? spacing / 2 : 0);
            int y1 = rowYs[cell.getRow()] + (collapse == BorderCollapse.SEPARATE ? spacing / 2 : 0);
            int y2 = rowYs[cell.getRow() + cell.getRowspan()] - (collapse == BorderCollapse.SEPARATE ? spacing / 2 : 0);

            Rectangle rect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
            drawCell(shiftedG, cell, rect, collapse);
        }

        shiftedG.dispose();
    }

    private void drawBoxShadow(Graphics2D g, BoxShadow shadow, int width, int height) {
        for (BoxShadow.ShadowLayer layer : shadow.shadows()) {
            float x = layer.xOffset();
            float y = layer.yOffset();
            float blur = layer.blurRadius();
            float spread = layer.spreadRadius();
            Color color = layer.color();

            int shadowWidth = (int) (width + spread * 2 + blur * 2);
            int shadowHeight = (int) (height + spread * 2 + blur * 2);
            int shadowX = (int) (-spread - blur + x);
            int shadowY = (int) (-spread - blur + y);

            // Создаём градиент для размытой тени
            float alpha = (float) (color.getAlpha() / 255.0);
            Color shadowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 50));

            if (blur > 0) {
                // Размытая тень
                for (int i = (int) blur; i > 0; i -= 2) {
                    float ratio = 1.0f - (i / blur);
                    int alphaVal = (int) (alpha * 255 * ratio);
                    Color currentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaVal);
                    g.setColor(currentColor);
                    g.fill(new RoundRectangle2D.Float(
                            shadowX + blur - i, shadowY + blur - i,
                            shadowWidth - 2 * (blur - i), shadowHeight - 2 * (blur - i),
                            4, 4
                    ));
                }
            }

            // Основная тень
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            g.fill(new RoundRectangle2D.Float(shadowX, shadowY, shadowWidth, shadowHeight, 4, 4));
        }
    }

    private void drawCell(Graphics2D g, Cell cell, Rectangle rect, BorderCollapse collapse) {
        TableStyle ts = cell.getEffectiveTableStyle();
        TextStyle txtStyle = cell.getEffectiveTextStyle();

        // Фон ячейки
        if (ts.backgroundColor() != null) {
            g.setColor(ts.backgroundColor());
            Float radius = ts.borderRadius();
            if (radius != null && radius > 0) {
                float r = Math.min(radius, Math.min(rect.width / 2f, rect.height / 2f));
                g.fill(new RoundRectangle2D.Float(rect.x, rect.y, rect.width, rect.height, r * 2, r * 2));
            } else {
                g.fillRect(rect.x, rect.y, rect.width, rect.height);
            }
        }

        // Границы
        drawBorders(g, cell, rect, ts, collapse);

        // Текст
        drawCellText(g, cell, rect, ts, txtStyle);
    }

    private void drawBorders(Graphics2D g, Cell cell, Rectangle rect, TableStyle ts, BorderCollapse collapse) {
        Border border = ts.border();
        if (border == null) {
            border = Border.NONE;
        }

        // Если border-collapse = COLLAPSE, рисуем только часть границы
        boolean drawTop = true, drawRight = true, drawBottom = true, drawLeft = true;

        if (collapse == BorderCollapse.COLLAPSE) {
            // Проверяем соседние ячейки
            drawBottom = !hasCellBelow(cell);
            drawRight = !hasCellRight(cell);
        }

        // Верхняя граница
        if (drawTop) {
            drawBorderSide(g, rect.x, rect.y, rect.width, 0, border.top());
        }

        // Правая граница
        if (drawRight) {
            drawBorderSide(g, rect.x + rect.width, rect.y, rect.height, 90, border.right());
        }

        // Нижняя граница
        if (drawBottom) {
            drawBorderSide(g, rect.x, rect.y + rect.height, rect.width, 180, border.bottom());
        }

        // Левая граница
        if (drawLeft) {
            drawBorderSide(g, rect.x, rect.y, rect.height, 270, border.left());
        }
    }

    private void drawBorderSide(Graphics2D g, int x, int y, int length, int angle, Border.BorderSide side) {
        if (!side.isVisible()) return;

        g.setColor(side.color());
        g.setStroke(new BasicStroke(side.width()));

        switch (side.style()) {
            case SOLID:
                g.drawLine(x, y, x + (angle == 0 || angle == 180 ? length : 0),
                        y + (angle == 90 || angle == 270 ? length : 0));
                break;

            case DASHED:
                g.setStroke(new BasicStroke(side.width(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,
                        new float[]{side.width() * 3, side.width() * 2}, 0));
                g.drawLine(x, y, x + (angle == 0 || angle == 180 ? length : 0),
                        y + (angle == 90 || angle == 270 ? length : 0));
                break;

            case DOTTED:
                g.setStroke(new BasicStroke(side.width(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1, new float[]{1, side.width() * 2}, 0));
                g.drawLine(x, y, x + (angle == 0 || angle == 180 ? length : 0),
                        y + (angle == 90 || angle == 270 ? length : 0));
                break;

            case DOUBLE:
                // Рисуем три линии для двойной границы
                float offset = side.width() / 3f;
                for (int i = 0; i < 3; i++) {
                    g.setStroke(new BasicStroke(side.width() / 3f));
                    g.drawLine(x, y, x + (angle == 0 || angle == 180 ? length : 0),
                            y + (angle == 90 || angle == 270 ? length : 0));
                }
                break;

            case NONE:
            default:
                break;
        }
    }

    private boolean hasCellBelow(Cell cell) {
        int nextRow = cell.getRow() + cell.getRowspan();
        for (Cell c : table.cells()) {
            if (c.getRow() == nextRow && c.getCol() == cell.getCol()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCellRight(Cell cell) {
        int nextCol = cell.getCol() + cell.getColspan();
        for (Cell c : table.cells()) {
            if (c.getRow() == cell.getRow() && c.getCol() == nextCol) {
                return true;
            }
        }
        return false;
    }

    // =====================
    // Отрисовка текста
    // =====================

    private void drawCellText(Graphics2D g, Cell cell, Rectangle rect, TableStyle ts, TextStyle txtStyle) {
        if (cell.getTexts().isEmpty()) return;

        Padding padding = ts.padding() != null ? ts.padding() : new Padding(1);

        int innerX = rect.x + padding.left();
        int innerY = rect.y + padding.top();
        int innerW = Math.max(0, rect.width - padding.left() - padding.right());
        int innerH = Math.max(0, rect.height - padding.top() - padding.bottom());

        if (innerW == 0 || innerH == 0) return;

        // Расчёт позиции текста с учётом выравнивания
        TextLayout textLayout = layoutText(g, cell.getTexts(), txtStyle, innerW);

        int textX, textY;

        Alignment alignment = txtStyle.textAlignment();

        // Горизонтальное выравнивание
		switch (alignment.getHorizontal()) {
            case LEFT:
                textX = innerX;
                break;
			case CENTER:
                textX = innerX + (innerW - textLayout.blockWidth) / 2;
				break;
			case RIGHT:
                textX = innerX + innerW - textLayout.blockWidth;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + alignment.getHorizontal());
		}

        // Вертикальное выравнивание
		switch (alignment.getVertical()) {
			case TOP:
                textY = innerY;
				break;
			case CENTER:
                textY = innerY + (innerH - textLayout.blockHeight) / 2;
				break;
			case BOTTOM:
                textY = innerY + innerH - textLayout.blockHeight;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + alignment.getVertical());
		}

        // Отрисовка строк текста
        int yCursor = textY;
        for (Line line : textLayout.lines) {
            int xCursor = textX;

            // Для CENTER и RIGHT выравниваем каждую строку
            switch (alignment) {
                case TOP_CENTER:
                case CENTER:
                case BOTTOM_CENTER:
                    xCursor = innerX + (innerW - line.width) / 2;
                    break;
                case TOP_RIGHT:
                case CENTER_RIGHT:
                case BOTTOM_RIGHT:
                    xCursor = innerX + innerW - line.width;
                    break;
            }

            int baseline = yCursor + line.baseline;

            for (Run run : line.runs) {
                g.setFont(run.font);

                // Цвет текста
                Color textColor = run.style.textColor() != null ? run.style.textColor() : Color.BLACK;
                g.setColor(textColor);

                // Рисуем текст
                if (run.style.underline() != null && run.style.underline()) {
                    // Рисуем текст с подчёркиванием
                    g.drawString(run.text, xCursor, baseline);

                    // Подчёркивание
                    FontMetrics fm = g.getFontMetrics(run.font);
                    int underlineY = baseline + 2;
                    g.setStroke(new BasicStroke(1));
                    g.drawLine(xCursor, underlineY, xCursor + run.width, underlineY);
                } else if (run.style.strikethrough() != null && run.style.strikethrough()) {
                    g.drawString(run.text, xCursor, baseline);

                    // Зачёркивание
                    FontMetrics fm = g.getFontMetrics(run.font);
                    int strikeY = baseline - fm.getAscent() / 2;
                    g.setStroke(new BasicStroke(1));
                    g.drawLine(xCursor, strikeY, xCursor + run.width, strikeY);
                } else {
                    g.drawString(run.text, xCursor, baseline);
                }

                xCursor += run.width;
            }

            yCursor += line.height;
        }
    }

    // =====================
    // Текстовый layout
    // =====================

    private TextLayout layoutText(Graphics2D g, List<Text> texts, TextStyle baseStyle, int maxWidth) {
        TextLayout layout = new TextLayout();
        Line currentLine = new Line();

        boolean wordWrap = baseStyle.wordWrap() != null ? baseStyle.wordWrap() : true;

        for (Text t : texts) {
            TextStyle effectiveStyle = baseStyle.clone();
            t.textStyle().applyTo(effectiveStyle);

            String raw = (t == Text.NEW_LINE) ? "\n" : t.getText();

            if (raw.equals("\n")) {
                if (!currentLine.isEmpty()) {
                    finishLine(layout, currentLine);
                    currentLine = new Line();
                }
                continue;
            }

            if (wordWrap) {
                wrapText(g, raw, effectiveStyle, maxWidth, layout, currentLine);
            } else {
                // Без переноса - добавляем как есть
                Run run = new Run(raw, effectiveStyle, g);
                currentLine.runs.add(run);
                currentLine.width += run.width;
                currentLine.height = Math.max(currentLine.height, run.height);
                currentLine.baseline = Math.max(currentLine.baseline, run.baseline);
            }
        }

        if (!currentLine.isEmpty()) {
            finishLine(layout, currentLine);
        }

        return layout;
    }

    private void wrapText(Graphics2D g, String text, TextStyle style, int maxWidth,
                          TextLayout layout, Line currentLine) {
        Font font = new Font(style.fontName(), style.getEffectiveFontStyle(), style.fontSize());
        FontMetrics fm = g.getFontMetrics(font);

        String[] words = text.split(" ");
        StringBuilder currentWord = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            currentWord.append(word);

            int wordWidth = fm.stringWidth(currentWord.toString());

            if (currentLine.width + wordWidth > maxWidth && currentLine.width > 0) {
                // Переносим на новую строку
                finishLine(layout, currentLine);
                currentLine = new Line();
            }

            Run run = new Run(currentWord.toString(), style, g);
            currentLine.runs.add(run);
            currentLine.width += run.width;
            currentLine.height = Math.max(currentLine.height, run.height);
            currentLine.baseline = Math.max(currentLine.baseline, run.baseline);

            // Добавляем пробел если не последнее слово
            if (i < words.length - 1) {
                currentWord.append(" ");
            } else {
                currentWord.setLength(0);
            }
        }
    }

    private void finishLine(TextLayout layout, Line line) {
        layout.lines.add(line);
        layout.blockWidth = Math.max(layout.blockWidth, line.width);
        layout.blockHeight += line.height;
    }

    private int estimateTextWidth(Graphics2D g, List<Text> texts, TextStyle baseStyle) {
        int maxWidth = 0;
        int currentWidth = 0;

        for (Text t : texts) {
            TextStyle effectiveStyle = baseStyle.clone();
            t.textStyle().applyTo(effectiveStyle);

            Font font = new Font(effectiveStyle.fontName(), effectiveStyle.getEffectiveFontStyle(), effectiveStyle.fontSize());
            FontMetrics fm = g.getFontMetrics(font);

            String raw = (t == Text.NEW_LINE) ? "" : t.getText();
            int w = fm.stringWidth(raw);

            if (raw.isEmpty() || raw.equals("\n")) {
                maxWidth = Math.max(maxWidth, currentWidth);
                currentWidth = 0;
            } else {
                currentWidth += w;
                maxWidth = Math.max(maxWidth, currentWidth);
            }
        }

        return maxWidth;
    }

    private int estimateTextHeight(Graphics2D g, List<Text> texts, TextStyle baseStyle, int maxWidth) {
        TextLayout layout = layoutText(g, texts, baseStyle, maxWidth);
        return layout.blockHeight;
    }

    // =====================
    // Вспомогательные методы
    // =====================

    private Graphics2D createTempGraphics() {
        BufferedImage temp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        return temp.createGraphics();
    }

    private int[] calculatePositions(Map<Integer, Integer> sizes) {
        int[] positions = new int[sizes.size() + 1];
        int pos = 0;
        for (int i = 0; i < sizes.size(); i++) {
            positions[i] = pos;
            pos += sizes.get(i);
        }
        positions[sizes.size()] = pos;
        return positions;
    }

    private TableStyle resolveTableStyle(Cell cell) {
        return cell.getEffectiveTableStyle();
    }

    private TextStyle resolveTextStyle(Cell cell) {
        return cell.getEffectiveTextStyle();
    }

    // =====================
    // Внутренние классы
    // =====================

    static final class TextLayout {
        final List<Line> lines = new ArrayList<>();
        int blockWidth;
        int blockHeight;
    }

    static final class Line {
        final List<Run> runs = new ArrayList<>();
        int width;
        int height;
        int baseline;

        boolean isEmpty() {
            return runs.isEmpty();
        }
    }

    static final class Run {
        final String text;
        final TextStyle style;
        final Font font;
        final int width;
        final int height;
        final int baseline;

        Run(String text, TextStyle style, Graphics2D g) {
            this.text = text;
            this.style = style;
            this.font = new Font(style.fontName(), style.getEffectiveFontStyle(), style.fontSize());
            FontMetrics fm = g.getFontMetrics(font);
            this.width = fm.stringWidth(text);
            this.height = (int) Math.ceil(fm.getHeight() * (style.lineHeight() != null ? style.lineHeight() : 1.0f));
            this.baseline = (int) Math.round(this.height * (fm.getAscent() / (double) fm.getHeight()));
        }
    }
}
