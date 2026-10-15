package me.manuloff.lib.table.renderer.render;

import lombok.NonNull;
import me.manuloff.lib.table.renderer.internal.text.FontFamilies;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Регистрация собственных шрифтов (TTF/OTF). После регистрации шрифт доступен в
 * {@link me.manuloff.lib.table.renderer.style.Style#fontFamily(String...)} по имени семейства.
 *
 * <pre>{@code
 * String family = Fonts.register(Path.of("fonts/Inter-Regular.ttf"));
 * Fonts.register(Path.of("fonts/Inter-Bold.ttf"));
 * table.cellStyle(s -> s.fontFamily(family));
 * }</pre>
 *
 * @author Manuloff
 * @since 2.0
 */
public final class Fonts {

    private Fonts() {
    }

    /**
     * @return имя семейства зарегистрированного шрифта
     */
    public static String register(@NonNull Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            return register(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return имя семейства зарегистрированного шрифта
     */
    public static String register(@NonNull InputStream in) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            FontFamilies.invalidate();
            return font.getFamily();
        } catch (FontFormatException e) {
            throw new IllegalArgumentException("Invalid font file", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return true, если семейство шрифтов установлено в системе или зарегистрировано
     */
    public static boolean isAvailable(@NonNull String family) {
        for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (name.equalsIgnoreCase(family)) return true;
        }
        return false;
    }
}
