package me.manuloff.lib.table.renderer.internal.text;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Справочник установленных семейств шрифтов: поиск без учёта регистра, общие имена CSS
 * ({@code sans-serif}, {@code serif}, {@code monospace}) и семейства-начертания для промежуточной насыщенности.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class FontFamilies {

    private static volatile Map<String, String> families;

    private FontFamilies() {
    }

    /**
     * Сбрасывает кэш семейств (после регистрации новых шрифтов).
     */
    public static void invalidate() {
        families = null;
    }

    /**
     * @param name имя семейства, логическое имя Java или общее имя CSS
     * @return имя установленного семейства или null, если такого нет
     */
    public static String resolve(String name) {
        String key = name.trim().toLowerCase(Locale.ROOT);
        switch (key) {
            case "sans-serif":
                return Font.SANS_SERIF;
            case "serif":
                return Font.SERIF;
            case "monospace":
                return Font.MONOSPACED;
            case "system-ui":
                return Font.DIALOG;
            default:
                return all().get(key);
        }
    }

    /**
     * Ищет установленное семейство-начертание для насыщенности, которую нельзя получить обычным жирным
     * начертанием: "Segoe UI Semibold", "Arial Black" и т.п.
     *
     * @return имя семейства-начертания или null
     */
    public static String weightVariant(String family, int weight) {
        for (String suffix : suffixes(weight)) {
            String name = all().get((family + " " + suffix).toLowerCase(Locale.ROOT));
            if (name != null) return name;
        }
        return null;
    }

    private static String[] suffixes(int weight) {
        if (weight <= 150) return new String[]{"Thin", "Hairline", "ExtraLight", "Light"};
        if (weight <= 250) return new String[]{"ExtraLight", "Extra Light", "UltraLight", "Light"};
        if (weight <= 350) return new String[]{"Light", "Semilight", "SemiLight"};
        if (weight >= 450 && weight < 550) return new String[]{"Medium"};
        if (weight >= 550 && weight < 650) return new String[]{"Semibold", "SemiBold", "Demibold", "DemiBold", "Semi Bold"};
        if (weight >= 750 && weight < 850) return new String[]{"ExtraBold", "Extra Bold", "UltraBold", "Heavy"};
        if (weight >= 850) return new String[]{"Black", "Heavy", "ExtraBold"};
        return new String[0];
    }

    private static Map<String, String> all() {
        Map<String, String> map = families;
        if (map == null) {
            map = new HashMap<>();
            for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ROOT)) {
                map.put(name.toLowerCase(Locale.ROOT), name);
            }
            for (String logical : new String[]{Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED}) {
                map.put(logical.toLowerCase(Locale.ROOT), logical);
            }
            families = map;
        }
        return map;
    }
}
