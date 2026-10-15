package me.manuloff.lib.table.renderer.docs;

import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.render.RenderOptions;
import me.manuloff.lib.table.renderer.render.RenderedTable;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Документация с примерами.
 * <p>
 * В markdown-файлах ({@code README.md}, {@code docs/*.md}) места для примеров отмечаются так:
 * <pre>
 * &lt;!-- example: имя --&gt; ... &lt;!-- /example --&gt;   код и картинка
 * &lt;!-- code: имя --&gt; ... &lt;!-- /code --&gt;         только код
 * &lt;!-- image: имя --&gt; ... &lt;!-- /image --&gt;       только картинка
 * </pre>
 * Код берётся из участков {@code // region имя} в исходниках примеров, картинки - из методов с {@link Example}.
 * <p>
 * Обычный запуск тестов проверяет, что все примеры рисуются и документация актуальна.
 * Обновить документацию и картинки: {@code mvn test -Dtest=DocsTest -Ddocs.update=true}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocsTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path DOCS = ROOT.resolve("docs");
    private static final Path IMAGES = DOCS.resolve("images");
    private static final Path SOURCES = ROOT.resolve("src/test/java/me/manuloff/lib/table/renderer/docs");

    private static final List<Class<?>> EXAMPLES = List.of(
            GettingStartedExamples.class, StructureExamples.class, ContentExamples.class, StyleExamples.class,
            BorderExamples.class, BackgroundExamples.class, TextExamples.class, RuleExamples.class,
            BuilderExamples.class, RenderingExamples.class);

    /**
     * Масштаб картинок: вдвое больше логического размера - чётко на экранах высокой плотности.
     */
    private static final double SCALE = 2;
    private static final int PADDING = 16;

    private static final Pattern MARKER = Pattern.compile(
            "<!-- (example|code|image): ([\\w-]+) -->.*?<!-- /\\1 -->", Pattern.DOTALL);
    private static final Pattern REGION = Pattern.compile(
            "^([ \\t]*)// region ([\\w-]+)\\R(.*?)^[ \\t]*// endregion", Pattern.DOTALL | Pattern.MULTILINE);

    private static boolean update() {
        return Boolean.getBoolean("docs.update");
    }

    @Test
    @Order(1)
    void examplesRender() throws Exception {
        Map<String, Table> tables = examples();
        assertTrue(tables.size() > 10);
        for (Map.Entry<String, Table> e : tables.entrySet()) {
            RenderedTable r = e.getValue().render(RenderOptions.builder().scale(SCALE).build());
            assertTrue(r.image().getWidth() > 1, e.getKey());
            if (update()) writeImage(e.getKey(), r);
        }
    }

    @Test
    @Order(2)
    void docsAreUpToDate() throws IOException {
        Map<String, String> regions = regions();
        List<Path> pages = new ArrayList<>();
        pages.add(ROOT.resolve("README.md"));
        try (Stream<Path> files = Files.list(DOCS)) {
            pages.addAll(files.filter(p -> p.toString().endsWith(".md")).sorted().collect(Collectors.toList()));
        }

        List<String> stale = new ArrayList<>();
        for (Path page : pages) {
            String text = Files.readString(page, StandardCharsets.UTF_8);
            String imagePrefix = page.getParent().equals(DOCS) ? "images/" : "docs/images/";
            String expected = render(text, regions, imagePrefix, page);
            if (!expected.equals(text)) {
                if (update()) {
                    Files.writeString(page, expected, StandardCharsets.UTF_8);
                } else {
                    stale.add(ROOT.relativize(page).toString());
                }
            }
        }
        if (!stale.isEmpty()) {
            fail("Документация устарела: " + stale + ". Обновите: mvn test -Dtest=DocsTest -Ddocs.update=true");
        }
    }

    // ============================================
    // Подстановка примеров
    // ============================================

    private static String render(String text, Map<String, String> regions, String imagePrefix, Path page) {
        Matcher m = MARKER.matcher(text);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String kind = m.group(1);
            String name = m.group(2);
            if (kind.equals("image")) {
                // Одной строкой, чтобы картинку можно было поставить в ячейку markdown-таблицы
                String inline = "<!-- image: " + name + " -->" + imageTag(name, imagePrefix) + "<!-- /image -->";
                m.appendReplacement(out, Matcher.quoteReplacement(inline));
                continue;
            }
            String code = regions.get(name);
            if (code == null) fail("Нет участка кода '" + name + "' (" + page.getFileName() + ")");
            StringBuilder block = new StringBuilder("<!-- ").append(kind).append(": ").append(name).append(" -->\n");
            block.append("```java\n").append(code).append("```\n");
            if (kind.equals("example")) block.append('\n').append(imageTag(name, imagePrefix)).append('\n');
            block.append("<!-- /").append(kind).append(" -->");
            m.appendReplacement(out, Matcher.quoteReplacement(block.toString()));
        }
        m.appendTail(out);
        return out.toString();
    }

    /**
     * Картинка с логической шириной, чтобы двукратное изображение отображалось чётко и в нужном размере.
     */
    private static String imageTag(String name, String prefix) {
        Path file = IMAGES.resolve(name + ".png");
        int width = 0;
        try {
            if (Files.exists(file)) width = (int) Math.round(ImageIO.read(file.toFile()).getWidth() / SCALE);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return "<img src=\"" + prefix + name + ".png\" alt=\"" + name + "\"" + (width > 0 ? " width=\"" + width + "\"" : "") + ">";
    }

    /**
     * Участки кода из исходников примеров (с убранным общим отступом).
     */
    private static Map<String, String> regions() throws IOException {
        Map<String, String> regions = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(SOURCES)) {
            for (Path file : files.filter(p -> p.toString().endsWith("Examples.java")).sorted().collect(Collectors.toList())) {
                Matcher m = REGION.matcher(Files.readString(file, StandardCharsets.UTF_8));
                while (m.find()) {
                    String indent = m.group(1);
                    StringBuilder code = new StringBuilder();
                    for (String line : m.group(3).split("\\R", -1)) {
                        code.append(line.startsWith(indent) ? line.substring(indent.length()) : line.trim()).append('\n');
                    }
                    String result = code.toString().replaceAll("\\n+$", "\n");
                    if (regions.put(m.group(2), result) != null) fail("Повтор участка кода: " + m.group(2));
                }
            }
        }
        return regions;
    }

    // ============================================
    // Примеры и картинки
    // ============================================

    private static Map<String, Table> examples() throws Exception {
        Map<String, Table> tables = new LinkedHashMap<>();
        for (Class<?> type : EXAMPLES) {
            for (Method method : type.getDeclaredMethods()) {
                Example example = method.getAnnotation(Example.class);
                if (example == null) continue;
                assertTrue(Modifier.isStatic(method.getModifiers()), method.toString());
                method.setAccessible(true);
                Table table = (Table) method.invoke(null);
                if (tables.put(example.value(), table) != null) fail("Повтор примера: " + example.value());
            }
        }
        return tables;
    }

    private static void writeImage(String name, RenderedTable r) throws IOException {
        Files.createDirectories(IMAGES);
        BufferedImage src = r.image();
        int pad = (int) (PADDING * SCALE);
        BufferedImage out = new BufferedImage(src.getWidth() + 2 * pad, src.getHeight() + 2 * pad, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.drawImage(src, pad, pad, null);
        g.dispose();
        ImageIO.write(out, "png", IMAGES.resolve(name + ".png").toFile());
        assertEquals(true, Files.exists(IMAGES.resolve(name + ".png")));
    }
}
