package me.manuloff.lib.table.renderer;

import lombok.Getter;
import lombok.NonNull;
import me.manuloff.lib.table.renderer.style.Alignment;
import me.manuloff.lib.table.renderer.style.Padding;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.TextStyle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Manuloff
 * @since 14:23 21.01.2025
 */
@Getter
public final class RenderedTable {

	private static final double SCALE = 2;

	private final Table table;

	private final int width;
	private final int height;

	private final BufferedImage image;

	RenderedTable(@NonNull Table table) {
		this.table = table;

		Map<Integer, Integer> colWidths = new HashMap<>();
		Map<Integer, Integer> rowHeights = new HashMap<>();

		Graphics2D g2d = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();

		for (Cell cell : this.table.getCells()) {
			TextStyle textStyle = this.resolveTextStyle(cell);
			TableStyle tableStyle = this.resolveTableStyle(cell);

			Dimension dim = estimateTextSize(g2d, cell.getTexts(), textStyle, tableStyle);

			Padding padding = tableStyle.padding();

			int paddedW = dim.width + padding.left() + padding.right();
			int paddedH = dim.height + padding.top() + padding.bottom();

			// распределяем по колонкам и строкам
			for (int c = 0; c < cell.getColspan(); c++) {
				colWidths.merge(cell.getCol() + c, paddedW / cell.getColspan(), Math::max);
			}

			for (int r = 0; r < cell.getRowspan(); r++) {
				rowHeights.merge(cell.getRow() + r, paddedH / cell.getRowspan(), Math::max);
			}
		}

		g2d.dispose();

		this.width = colWidths.values().stream().mapToInt(Integer::intValue).sum();
		this.height = rowHeights.values().stream().mapToInt(Integer::intValue).sum();

		this.image = new BufferedImage((int) (this.width * SCALE), (int) (this.height * SCALE), BufferedImage.TYPE_INT_ARGB);

		// 3. Отрисовка
		Graphics2D g = this.image.createGraphics();
		g.scale(SCALE, SCALE);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);

		int[] colXs = new int[colWidths.size() + 1];
		int x = 0, i = 0;
		for (int w : colWidths.values()) {
			colXs[i++] = x;
			x += w;
		}
		colXs[i] = x;

		int[] rowYs = new int[rowHeights.size() + 1];
		int y = 0, j = 0;
		for (int h : rowHeights.values()) {
			rowYs[j++] = y;
			y += h;
		}
		rowYs[j] = y;

		// 4. Проходим по ячейкам
		for (Cell cell : this.table.getCells()) {
			int x1 = colXs[cell.getCol()];
			int x2 = colXs[cell.getCol() + cell.getColspan()];
			int y1 = rowYs[cell.getRow()];
			int y2 = rowYs[cell.getRow() + cell.getRowspan()];

			Rectangle rect = new Rectangle(x1, y1, x2 - x1, y2 - y1);
			this.drawCell(g, cell, rect);
		}

		g.dispose();
	}

	private void drawCell(@NonNull Graphics2D g,
						  @NonNull Cell cell,
						  @NonNull Rectangle rect) {

		TableStyle ts = this.resolveTableStyle(cell);

		// Фон
		g.setColor(ts.backgroundColor());
		g.fillRect(rect.x, rect.y, rect.width, rect.height);

		// Барьер
		if (ts.borderWidth() > 0) {
			g.setColor(ts.borderColor());
			g.setStroke(new BasicStroke(ts.borderWidth()));
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}

		this.drawCellText(g, cell, rect, ts, this.resolveTextStyle(cell));
	}

	private void drawCellText(@NonNull Graphics2D g,
							  @NonNull Cell cell,
							  @NonNull Rectangle rect,
							  @NonNull TableStyle ts,
							  @NonNull TextStyle cellTextStyle) {

		Padding padding = ts.padding();

		int innerX = rect.x + padding.left();
		int innerY = rect.y + padding.top();
		int innerW = Math.max(0, rect.width - padding.left() - padding.right());
		int innerH = Math.max(0, rect.height - padding.top() - padding.bottom());

		boolean debug = innerX == 195;

		if (debug) {
			System.out.println();
			System.out.println("rect = " + rect);
			System.out.println("text = " + String.join("", cell.getTexts().stream().map(Text::getText).collect(Collectors.toSet())));

			System.out.println("innerX = " + innerX);
			System.out.println("innerY = " + innerY);
			System.out.println("innerW = " + innerW);
			System.out.println("innerH = " + innerH);
		}

		if (innerW == 0 || innerH == 0) return;
		if (cell.getTexts().isEmpty()) return;

		// Разбиваем на логические строки с учетом \n и разных стилей Text
		TextLayout textLayout = layoutLines(g, cell.getTexts(), cellTextStyle);

		Alignment a = cellTextStyle.textAlignment();

		int topY;
		switch (a) {
			case TOP_LEFT:
			case TOP_CENTER:
			case TOP_RIGHT:
				topY = innerY;
				break;
			case CENTER_LEFT:
			case CENTER:
			case CENTER_RIGHT:
				topY = innerY + (innerH - textLayout.blockHeight) / 2;
				break;
			default:
				topY = innerY + innerH - textLayout.blockHeight;
		}

		int yCursor = topY;
		for (Line line : textLayout.lines) {
			int startX;
			switch (a) {
				case TOP_CENTER:
				case CENTER:
				case BOTTOM_CENTER:
					startX = innerX + (innerW - line.width) / 2;
					break;
				case TOP_RIGHT:
				case CENTER_RIGHT:
				case BOTTOM_RIGHT:
					startX = innerX + innerW - line.width;
					break;
				default: // LEFT-варианты
					startX = innerX;
			}

			int baseline = yCursor + line.baseline;

			int xCursor = startX;
			for (Run run : line.runs) {
				g.setFont(run.font);

				g.setColor(run.style.textColor());
				g.drawString(run.text, xCursor, baseline);

				xCursor += run.width;
			}

			yCursor += line.height;
		}
	}

	/**
	 * Используется для расчета некого бокса, внутри которого будет помещен текст
	 *
	 * @param g Временная графика
	 * @param texts Тексты, расположенные в ячейке
	 * @param cellTextStyle - текстовый стиль, примененный к ячейке
	 * @return размеры тексты
	 */
	private static Dimension estimateTextSize(
			@NonNull Graphics2D g,
			@NonNull List<Text> texts,
			@NonNull TextStyle cellTextStyle,
			@NonNull TableStyle tableStyle
	) {
		int width = 0;
		int height = 0;

		// текущая ширина строки
		int lineWidth = 0;
		int lineHeight = 0;

		for (Text t : texts) {
			// итоговый стиль = стиль ячейки + стиль текста
			TextStyle style = cellTextStyle.clone();
			t.textStyle().applyTo(style);

			Font font = new Font(style.fontName(), style.fontStyle(), style.fontSize());
			FontMetrics fm = g.getFontMetrics(font);

			String raw = t.getText();
			String[] parts = raw.split("\n", -1);

			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];

				int w = fm.stringWidth(part);
				int h = (int) Math.ceil(fm.getHeight() * style.lineHeight());

				lineWidth += w;
				lineHeight = Math.max(lineHeight, h);

				// перенос строки?
				if (i < parts.length - 1) {
					width = Math.max(width, lineWidth);
					height += lineHeight;

					// сбрасываем текущую строку
					lineWidth = 0;
					lineHeight = 0;
				}
			}
		}

		// дописываем последнюю строку
		width = Math.max(width, lineWidth);
		height += lineHeight;

		return new Dimension(
				Math.max(tableStyle.minCellWidth(), width),
				Math.max(tableStyle.minCellHeight(), height)
		);
	}

	/**
	 * Позволяет получить стиль таблицы для указанной ячейки с учетом всех родителей
	 * @param cell Ячейка, для которой нужно получить стиль таблицы
	 * @return Стиль таблицы с учетом всех её родственников
	 */
	@NonNull
	private TableStyle resolveTableStyle(@NonNull Cell cell) {
		TableStyle style = this.table.tableStyle().clone();
		this.table.getRow(cell.getRow()).tableStyle().applyTo(style);
		this.table.getColumn(cell.getCol()).tableStyle().applyTo(style);
		cell.tableStyle().applyTo(style);

		return style;
	}

	/**
	 * Позволяет получить стиль текста для указанной ячейки с учетом всех родителей
	 * @param cell Ячейка, для которой нужно получить стиль текста
	 * @return Стиль текста с учетом всех её родственников
	 */
	@NonNull
	private TextStyle resolveTextStyle(@NonNull Cell cell) {
		TextStyle style = this.table.textStyle().clone();
		this.table.getRow(cell.getRow()).textStyle().applyTo(style);
		this.table.getColumn(cell.getCol()).textStyle().applyTo(style);
		cell.textStyle().applyTo(style);

		return style;
	}

	private static TextLayout layoutLines(
			@NonNull Graphics2D g2d,
			@NonNull List<Text> texts,
			@NonNull TextStyle cellTextStyle
	) {
		TextLayout out = new TextLayout();
		Line line = new Line();

		for (Text t : texts) {
			TextStyle eff = cellTextStyle.clone();
			t.textStyle().applyTo(eff);

			String raw = t == Text.NEW_LINE ? "\n" : t.getText();
			String[] paragraphs = raw.split("\n", -1);

			for (int p = 0; p < paragraphs.length; p++) {
				String segment = paragraphs[p];

				if (p > 0) {
					finishLine(out, line);
					line = new Line();
				}

				if (segment.isEmpty()) {
					continue;
				}

				// Создаём run целиком и кладём в линию
				Run run = new Run(segment, eff, g2d);
				line.runs.add(run);
				line.width += run.width;
				int fmH = run.fm.getHeight();
				float lh = run.style.lineHeight();
				int scaledH = (int) Math.ceil(fmH * lh);
				int ascScaled = (int) Math.round(scaledH * (run.fm.getAscent() / (double) fmH));
				line.height = Math.max(line.height, scaledH);
				line.baseline = Math.max(line.baseline, ascScaled);
			}
		}

		finishLine(out, line);

		return out;
	}

	private static void finishLine(TextLayout out, Line line) {
		out.lines.add(line);
		out.blockWidth = Math.max(out.blockWidth, line.width);
		out.blockHeight += line.height;
	}

	// Режем длинный run (одно «слово») на несколько кусков, добавляя "-" в конец переноса
	private static void splitRunAndPlaceWithHyphenation(
			Run original,
			Line line,
			Graphics2D g2d,
			int maxWidth,
			BiConsumer<Line, Run> addRun,
			Consumer<Line> finishLine
	) {
		String rest = original.text;
		final String hyphen = "-";
		final int hyphenW = original.fm.stringWidth(hyphen);

		while (!rest.isEmpty()) {
			int remaining = maxWidth - line.width;
			if (remaining <= 0) {
				// нет места — перенос строки
				finishLine.accept(line);
				line = new Line();
				continue;
			}

			// сколько символов влезет, с учётом возможного дефиса в конце
			int fit = fitChars(original.fm, rest, remaining);
			if (fit == rest.length()) {
				// всё влезло — без дефиса
				addRun.accept(line, new Run(rest, original.style, g2d));
				rest = "";
			} else {
				// не влезло — пробуем оставить место под дефис
				int fitWithHyphen = fitChars(original.fm, rest, Math.max(0, remaining - hyphenW));
				if (fitWithHyphen <= 0) {
					// Совсем не влезает ни одного символа — перенос строки и пробуем снова
					finishLine.accept(line);
					line = new Line();
					continue;
				}
				String head = rest.substring(0, fitWithHyphen) + hyphen;
				addRun.accept(line, new Run(head, original.style, g2d));
				// перенос строки и продолжаем
				finishLine.accept(line);
				line = new Line();
				rest = rest.substring(fitWithHyphen);
			}
		}
	}

	// Бинарный поиск: сколько первых символов строки s помещается в width пикселей
	private static int fitChars(FontMetrics fm, String s, int width) {
		int lo = 0, hi = s.length();
		while (lo < hi) {
			int mid = (lo + hi + 1) >>> 1;
			int w = fm.stringWidth(s.substring(0, mid));
			if (w <= width) lo = mid; else hi = mid - 1;
		}
		return lo;
	}

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
			return this.runs.isEmpty();
		}
	}

	static final class Run {
		final String text;
		final TextStyle style;
		final Font font;
		final FontMetrics fm;
		final int width;

		Run(@NonNull String text, @NonNull TextStyle style, @NonNull Graphics2D g) {
			this.text = text;
			this.style = style;
			this.font = new Font(style.fontName(), style.fontStyle(), style.fontSize());
			this.fm = g.getFontMetrics(font);
			this.width = fm.stringWidth(text);
		}
	}
}
