package me.manuloff.lib.table.renderer.theme;

import me.manuloff.lib.table.renderer.Section;
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.rule.Match;
import me.manuloff.lib.table.renderer.style.Style;
import me.manuloff.lib.table.renderer.style.TableStyle;
import me.manuloff.lib.table.renderer.style.background.Gradient;
import me.manuloff.lib.table.renderer.style.border.BorderSide;
import me.manuloff.lib.table.renderer.style.box.Shadow;
import me.manuloff.lib.table.renderer.style.table.BorderCollapse;

import java.awt.Color;
import java.util.Collections;

/**
 * Готовые темы оформления.
 * <p>
 * Тема сбрасывает оформление блока таблицы (фон, рамку, скругление, тень, отступы, модель рамок),
 * стили ячеек таблицы, секций и заголовка, после чего задаёт свои. Размеры таблицы, стили строк, колонок,
 * отдельных ячеек и пользовательские условные правила не затрагиваются.
 *
 * <pre>{@code
 * table.theme(Themes.MODERN);
 * }</pre>
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Themes {

    private Themes() {
    }

    /**
     * Без оформления: нет рамок, отступов и фона.
     */
    public static final Theme PLAIN = Themes::reset;

    /**
     * Оформление по умолчанию: тонкая серая сетка, жирная шапка на светло-сером фоне.
     */
    public static final Theme DEFAULT = table -> {
        reset(table);
        table.cellStyle().padding(6, 10).border(BorderSide.solid(1, new Color(0xC8C8C8)));
        table.head().style().bold().background(new Color(0xF2F2F2));
    };

    /**
     * Классическая чёрная сетка.
     */
    public static final Theme GRID = table -> {
        reset(table);
        table.cellStyle().padding(4, 8).border(BorderSide.solid(1, Color.BLACK));
        table.head().style().bold().background(new Color(0xE6E6E6));
        table.foot().style().bold();
    };

    /**
     * Минимализм: только горизонтальные линии и выделенная шапка.
     */
    public static final Theme MINIMAL = table -> {
        reset(table);
        Color text = new Color(0x24292F);
        table.cellStyle().padding(8, 14).borderBottom(BorderSide.solid(1, new Color(0xE3E5E8))).color(text);
        table.head().style().bold().color(new Color(0x57606A)).borderBottom(BorderSide.solid(2, text));
        table.foot().style().bold().borderTop(BorderSide.solid(2, text)).borderBottom(BorderSide.NONE);
    };

    /**
     * "Зебра": чередование цвета строк тела и тёмная шапка.
     */
    public static final Theme STRIPED = table -> {
        reset(table);
        Color dark = new Color(0x343A40);
        table.style().background(Color.WHITE);
        table.cellStyle().padding(7, 12).color(new Color(0x212529));
        table.head().style().bold().color(Color.WHITE).background(dark);
        table.foot().style().bold().borderTop(BorderSide.solid(2, dark));
        table.themeRule(Match.body().and(Match.evenRows()), s -> s.background(new Color(0xF2F4F6)));
    };

    /**
     * Тёмная тема.
     */
    public static final Theme DARK = table -> {
        reset(table);
        Color background = new Color(0x22252C);
        Color line = new Color(0x3A3F4B);
        Color text = new Color(0xD7DAE0);
        table.style().background(background).border(BorderSide.solid(1, line));
        table.cellStyle().padding(8, 12).color(text).border(BorderSide.solid(1, line));
        table.head().style().bold().color(Color.WHITE).background(new Color(0x2C313C));
        table.foot().style().bold().background(new Color(0x2C313C));
        table.caption().style().background(background).color(text).padding(10, 0)
                .border(BorderSide.solid(1, line)).borderBottom(BorderSide.NONE);
    };

    /**
     * Карточка: скруглённые углы, тень, градиентная шапка, внешний отступ для тени.
     */
    public static final Theme MODERN = table -> {
        reset(table);
        Color line = new Color(0xE8EAF0);
        table.style().separate(0).borderRadius(12).background(Color.WHITE)
                .border(BorderSide.solid(1, line))
                .boxShadow(Shadow.of(0, 6, 18, new Color(20, 30, 60, 40)))
                .margin(20);
        table.cellStyle().padding(10, 16).color(new Color(0x2D3142)).borderBottom(BorderSide.solid(1, line));
        table.head().style().fontWeight(Style.SEMI_BOLD).color(Color.WHITE).borderBottom(BorderSide.NONE)
                .background(Gradient.horizontal(new Color(0x5B6CFF), new Color(0x8A5BFF)));
        table.foot().style().bold().background(new Color(0xF7F8FB)).borderBottom(BorderSide.NONE);
        table.themeRule(Match.body().and(Match.lastRow()), s -> s.borderBottom(BorderSide.NONE));
    };

    /**
     * Сбрасывает оформление, которое задают темы.
     */
    private static void reset(Table table) {
        TableStyle ts = table.style();
        ts.background((Color) null)
                .backgroundLayers(Collections.emptyList())
                .border(BorderSide.NONE)
                .borderRadius(0)
                .padding(0)
                .margin(0)
                .boxShadow(new Shadow[0])
                .outline(null)
                .borderCollapse(BorderCollapse.COLLAPSE)
                .horizontalSpacing(0)
                .verticalSpacing(0);
        table.cellStyle().clear();
        table.head().style().clear();
        table.foot().style().clear();
        for (Section body : table.bodies()) body.style().clear();
        if (table.hasCaption()) table.caption().style().clear();
    }
}
