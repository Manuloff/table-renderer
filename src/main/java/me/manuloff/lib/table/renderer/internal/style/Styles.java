package me.manuloff.lib.table.renderer.internal.style;

import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.border.Corners;
import me.manuloff.lib.table.renderer.style.box.Overflow;
import me.manuloff.lib.table.renderer.style.text.BaselineShift;
import me.manuloff.lib.table.renderer.style.text.OverflowWrap;
import me.manuloff.lib.table.renderer.style.text.TextAlign;
import me.manuloff.lib.table.renderer.style.text.TextDecoration;
import me.manuloff.lib.table.renderer.style.text.TextTransform;
import me.manuloff.lib.table.renderer.style.text.VerticalAlign;
import me.manuloff.lib.table.renderer.style.text.WhiteSpace;
import me.manuloff.lib.table.renderer.style.text.WordBreak;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

/**
 * Значения по умолчанию и правила наследования стилей.
 * <p>
 * Итоговый ("вычисленный") стиль - обычный {@link Style}, в котором заданы все свойства, кроме
 * необязательных: background, outline, width, minWidth, maxWidth, height, maxHeight, lineHeight,
 * textStrokeColor, lineClamp.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Styles {

    private Styles() {
    }

    /**
     * @return новый стиль со всеми значениями по умолчанию
     */
    public static Style base() {
        Style s = new Style();
        resetBox(s);
        s.textAlign(TextAlign.LEFT)
                .verticalAlign(VerticalAlign.MIDDLE)
                .hidden(false)
                .fontFamily(List.of("SansSerif"))
                .fontSize(14d)
                .fontWeight(Style.NORMAL)
                .italic(false)
                .smallCaps(false)
                .kerning(true)
                .ligatures(true)
                .color(Color.BLACK)
                .letterSpacing(0)
                .wordSpacing(0)
                .textIndent(0)
                .textTransform(TextTransform.NONE)
                .textShadow(Collections.emptyList())
                .textStrokeWidth(0d)
                .whiteSpace(WhiteSpace.PRE_LINE)
                .wordBreak(WordBreak.NORMAL)
                .overflowWrap(OverflowWrap.NORMAL)
                .hyphens(true)
                .tabSize(8)
                .textOverflow("");
        return s;
    }

    /**
     * Стиль строчного фрагмента: текстовые свойства наследуются от родителя, свойства блока сбрасываются.
     */
    public static Style inline(Style parent, Style own) {
        Style s = parent.copy();
        resetBox(s);
        s.background((Color) null);
        s.outline(null);
        return s.apply(own);
    }

    /**
     * Сбрасывает ненаследуемые свойства блока к значениям по умолчанию.
     */
    private static void resetBox(Style s) {
        s.backgroundLayers(Collections.emptyList())
                .border(BorderSide.NONE)
                .borderRadius(Corners.ZERO)
                .padding(0)
                .boxShadow(Collections.emptyList())
                .outlineOffset(0)
                .opacity(1)
                .overflow(Overflow.VISIBLE)
                .textDecoration(TextDecoration.NONE)
                .baselineShift(BaselineShift.NONE)
                .baselineOffset(0);
    }
}
