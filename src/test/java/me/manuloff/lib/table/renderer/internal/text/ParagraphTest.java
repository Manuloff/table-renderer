package me.manuloff.lib.table.renderer.internal.text;

import me.manuloff.lib.table.renderer.content.RichText;
import me.manuloff.lib.table.renderer.internal.style.Styles;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.text.WhiteSpace;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParagraphTest {

    private final FontResolver fonts = new FontResolver(List.of("Dialog"));
    private final TextMeasurer measurer = new TextMeasurer();

    private Paragraph paragraph(RichText text, Consumer<Style> style) {
        Style s = Styles.base();
        style.accept(s);
        return Paragraph.build(text, s, this.fonts, this.measurer, Locale.ROOT);
    }

    private static String lineText(TextLine line) {
        StringBuilder sb = new StringBuilder();
        for (TextFragment f : line.texts) sb.append(f.text);
        return sb.toString();
    }

    @Test
    void minAndMaxContent() {
        Paragraph p = paragraph(RichText.of("aaa bbbbbb c"), s -> {
        });
        assertTrue(p.minContentWidth() < p.maxContentWidth());
        TextBlock wide = p.layout(p.maxContentWidth());
        assertEquals(1, wide.lines.size());
        TextBlock narrow = p.layout(p.minContentWidth());
        assertEquals(3, narrow.lines.size());
        assertEquals("bbbbbb", lineText(narrow.lines.get(1)));
    }

    @Test
    void whitespaceCollapsing() {
        Paragraph p = paragraph(RichText.of("  a   b  \n c "), s -> s.whiteSpace(WhiteSpace.NORMAL));
        TextBlock b = p.layout(1000);
        assertEquals(1, b.lines.size());
        assertEquals("a b c", lineText(b.lines.get(0)));
    }

    @Test
    void preLineKeepsNewlines() {
        Paragraph p = paragraph(RichText.of("a  b\nc"), s -> s.whiteSpace(WhiteSpace.PRE_LINE));
        TextBlock b = p.layout(1000);
        assertEquals(2, b.lines.size());
        assertEquals("a b", lineText(b.lines.get(0)));
        assertEquals("c", lineText(b.lines.get(1)));
    }

    @Test
    void preKeepsSpaces() {
        Paragraph p = paragraph(RichText.of("a  b"), s -> s.whiteSpace(WhiteSpace.PRE));
        assertEquals("a  b", lineText(p.layout(1).lines.get(0)));
    }

    @Test
    void nowrapStaysOnOneLine() {
        Paragraph p = paragraph(RichText.of("one two three four"), Style::nowrap);
        assertEquals(p.minContentWidth(), p.maxContentWidth(), 1e-6);
        assertEquals(1, p.layout(10).lines.size());
    }

    @Test
    void lineBreaks() {
        Paragraph p = paragraph(new RichText().text("a").br().br().text("b"), s -> {
        });
        assertEquals(3, p.layout(1000).lines.size());
    }

    @Test
    void lineClampAddsEllipsis() {
        Paragraph p = paragraph(RichText.of("word ".repeat(40)), s -> s.lineClamp(2).textOverflow(Style.ELLIPSIS));
        TextBlock b = p.layout(100);
        assertEquals(2, b.lines.size());
        assertTrue(lineText(b.lines.get(1)).endsWith(Style.ELLIPSIS));
    }

    @Test
    void ellipsisTruncatesToWidth() {
        Paragraph p = paragraph(RichText.of("a very long line of text that does not fit"), Style::ellipsis);
        TextBlock b = p.layout(80);
        assertEquals(1, b.lines.size());
        assertTrue(lineText(b.lines.get(0)).endsWith(Style.ELLIPSIS));
        assertTrue(b.lines.get(0).width <= 80.5);
    }

    @Test
    void overflowWrapBreaksLongWord() {
        String word = "x".repeat(50);
        Paragraph normal = paragraph(RichText.of(word), s -> {
        });
        assertEquals(1, normal.layout(50).lines.size());
        Paragraph wrap = paragraph(RichText.of(word), s -> s.overflowWrap(me.manuloff.lib.table.renderer.style.text.OverflowWrap.ANYWHERE));
        assertTrue(wrap.layout(50).lines.size() > 1);
        assertTrue(wrap.minContentWidth() < normal.minContentWidth());
    }

    @Test
    void softHyphenAddsHyphen() {
        Paragraph p = paragraph(RichText.of("ab­cd­ef­gh"), s -> {
        });
        TextBlock b = p.layout(p.maxContentWidth() / 2);
        assertTrue(b.lines.size() > 1);
        assertTrue(lineText(b.lines.get(0)).endsWith("-"));
        assertEquals("abcdefgh", lineText(p.layout(1000).lines.get(0)));
    }

    @Test
    void textTransform() {
        Paragraph p = paragraph(RichText.of("hello world"), s -> s.textTransform(me.manuloff.lib.table.renderer.style.text.TextTransform.CAPITALIZE));
        assertEquals("Hello World", lineText(p.layout(1000).lines.get(0)));
    }

    @Test
    void spanEdgesAddWidth() {
        Paragraph plain = paragraph(new RichText().text("abc"), s -> {
        });
        Paragraph padded = paragraph(new RichText().text("abc", s -> s.padding(0, 10)), s -> {
        });
        assertEquals(plain.maxContentWidth() + 20, padded.maxContentWidth(), 0.01);
    }

    @Test
    void lineHeightFactor() {
        Paragraph p = paragraph(new RichText().text("a").br().text("b"), s -> s.lineHeight(2).fontSize(10));
        assertEquals(40, p.layout(100).height(), 0.01);
    }

    @Test
    void alignment() {
        Paragraph p = paragraph(RichText.of("abc"), s -> s.textAlign(me.manuloff.lib.table.renderer.style.text.TextAlign.RIGHT));
        TextBlock b = p.layout(200);
        assertEquals(200, b.lines.get(0).offset + b.lines.get(0).width, 0.01);
    }
}
