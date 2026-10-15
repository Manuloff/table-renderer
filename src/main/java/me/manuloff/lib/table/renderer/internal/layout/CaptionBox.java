package me.manuloff.lib.table.renderer.internal.layout;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.manuloff.lib.table.renderer.Caption;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.internal.style.Styles;
import me.manuloff.lib.table.renderer.internal.text.FontResolver;
import me.manuloff.lib.table.renderer.internal.text.Paragraph;
import me.manuloff.lib.table.renderer.internal.text.TextBlock;
import me.manuloff.lib.table.renderer.internal.text.TextMeasurer;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.text.TextAlign;

import java.util.Locale;

/**
 * Заголовок таблицы: вычисленный стиль, текст и высота при ширине таблицы.
 * Текстовые свойства наследуются от стиля ячеек таблицы, по умолчанию текст по центру с отступами 6px сверху и снизу.
 *
 * @author Manuloff
 * @since 2.0
 */
@Getter
@Accessors(fluent = true)
public final class CaptionBox {

    private final Style style;
    private final Paragraph paragraph;
    private TextBlock block;
    private double height;

    private CaptionBox(Style style, Paragraph paragraph) {
        this.style = style;
        this.paragraph = paragraph;
    }

    /**
     * @return заголовок или null, если у таблицы его нет
     */
    static CaptionBox of(Table table, FontResolver fonts, TextMeasurer measurer, Locale locale) {
        if (!table.hasCaption()) return null;
        Caption caption = table.caption();
        Style inherited = Styles.base().apply(table.cellStyle());
        Style own = new Style().textAlign(TextAlign.CENTER).padding(6, 0).apply(caption.style());
        Style style = Styles.inline(inherited, own);
        return new CaptionBox(style, Paragraph.build(caption.content(), style, fonts, measurer, locale));
    }

    /**
     * @return минимальная ширина заголовка (с отступами и рамкой)
     */
    double minWidth() {
        return this.paragraph.minContentWidth() + horizontalInsets();
    }

    /**
     * Раскладывает текст по ширине таблицы.
     */
    void layout(double width) {
        Style s = this.style;
        this.block = this.paragraph.layout(Math.max(0, width - horizontalInsets()));
        double h = this.block.height() + insetTop() + s.paddingBottom() + s.borderBottom().effectiveWidth();
        if (s.height() != null) h = Math.max(h, s.height());
        this.height = h;
    }

    /**
     * @return отступ и рамка слева
     */
    public double insetLeft() {
        return this.style.paddingLeft() + this.style.borderLeft().effectiveWidth();
    }

    /**
     * @return отступ и рамка сверху
     */
    public double insetTop() {
        return this.style.paddingTop() + this.style.borderTop().effectiveWidth();
    }

    private double horizontalInsets() {
        Style s = this.style;
        return insetLeft() + s.paddingRight() + s.borderRight().effectiveWidth();
    }
}
