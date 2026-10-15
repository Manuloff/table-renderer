package me.manuloff.lib.table.renderer.internal.text;

import lombok.Value;
import me.manuloff.lib.table.renderer.style.Style;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Подбор шрифтов по стилю: семейство с запасными вариантами, насыщенность, начертание, кернинг и лигатуры.
 * Экземпляр кэширует созданные шрифты и метрики и используется в пределах одного рендера.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class FontResolver {

    /**
     * Контекст измерения текста. Дробные метрики включены, чтобы раскладка не зависела от масштаба рендера.
     */
    public static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    private final List<String> fallbackFamilies;
    private final Map<FontKey, List<Font>> chains = new HashMap<>();
    private final Map<FontKey, Font> fonts = new HashMap<>();
    private final Map<Font, TextMetrics> metrics = new HashMap<>();

    /**
     * @param fallbackFamilies семейства, в которых ищутся символы, отсутствующие в шрифтах стиля
     */
    public FontResolver(List<String> fallbackFamilies) {
        this.fallbackFamilies = fallbackFamilies;
    }

    /**
     * @return шрифты стиля в порядке приоритета: семейства стиля, затем запасные. Список не пуст.
     */
    public List<Font> chain(Style style) {
        FontKey key = new FontKey(style.fontFamily(), (float) (double) style.fontSize(), style.fontWeight(),
                Boolean.TRUE.equals(style.isItalic()), Boolean.TRUE.equals(style.kerning()),
                Boolean.TRUE.equals(style.ligatures()));
        return this.chains.computeIfAbsent(key, this::buildChain);
    }

    /**
     * @return метрики шрифта
     */
    public TextMetrics metrics(Font font) {
        return this.metrics.computeIfAbsent(font, TextMetrics::new);
    }

    private List<Font> buildChain(FontKey key) {
        List<Font> chain = new ArrayList<>();
        for (String name : key.families) addFamily(chain, name, key);
        for (String name : this.fallbackFamilies) addFamily(chain, name, key);
        if (chain.isEmpty()) chain.add(create(Font.DIALOG, key));
        return chain;
    }

    private void addFamily(List<Font> chain, String name, FontKey key) {
        String family = FontFamilies.resolve(name);
        if (family != null) chain.add(create(family, key));
    }

    private Font create(String family, FontKey key) {
        FontKey fontKey = new FontKey(List.of(family), key.size, key.weight, key.italic, key.kerning, key.ligatures);
        return this.fonts.computeIfAbsent(fontKey, k -> {
            String actual = family;
            boolean bold = key.weight >= 600;
            String variant = FontFamilies.weightVariant(family, key.weight);
            if (variant != null) {
                actual = variant;
                bold = false;
            }

            Map<TextAttribute, Object> attrs = new HashMap<>();
            attrs.put(TextAttribute.FAMILY, actual);
            attrs.put(TextAttribute.SIZE, key.size);
            attrs.put(TextAttribute.WEIGHT, bold ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
            attrs.put(TextAttribute.POSTURE, key.italic ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
            if (key.kerning) attrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
            if (key.ligatures) attrs.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
            return new Font(attrs);
        });
    }

    /**
     * Ключ кэша: параметры, от которых зависит шрифт.
     */
    @Value
    private static class FontKey {
        List<String> families;
        float size;
        int weight;
        boolean italic;
        boolean kerning;
        boolean ligatures;
    }
}
