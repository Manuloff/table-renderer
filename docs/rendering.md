# Рендер

## Параметры

<!-- code: render-options -->
```java
RenderOptions options = RenderOptions.builder()
        .scale(2)                          // изображение вдвое крупнее, раскладка та же
        .background(Color.WHITE)           // фон холста (по умолчанию прозрачный)
        .availableWidth(600d)              // ширина "окна": длинный текст будет переноситься
        .fallbackFonts(List.of("Segoe UI Emoji", "Dialog"))
        .locale(Locale.forLanguageTag("ru"))
        .build();
RenderedTable result = table.render(options);
```
<!-- /code -->

| Параметр          | По умолчанию                       | Описание                                                       |
|-------------------|------------------------------------|----------------------------------------------------------------|
| `scale`           | 1                                  | Масштаб: 2 — изображение вдвое больше при той же раскладке (HiDPI) |
| `background`      | прозрачный                         | Фон всего изображения                                          |
| `availableWidth`  | не ограничена                      | Доступная ширина: ограничивает автоматическую ширину таблицы и служит базой для процентной |
| `antialiasing`    | true                               | Сглаживание фигур и текста                                     |
| `fallbackFonts`   | Dialog, Segoe UI Symbol, Segoe UI Emoji, Noto Sans, DejaVu Sans | Шрифты для символов, которых нет в шрифтах стиля |
| `locale`          | системная                          | Правила переноса строк и смены регистра                        |

Размер изображения — таблица вместе с заголовком и внешним отступом (`TableStyle.margin`), расширенная
так, чтобы поместились тени и контуры, и умноженная на масштаб.

## Результат

<!-- code: render-output -->
```java
BufferedImage image = result.image();           // готовое изображение
result.write(Path.of("table.png"));             // формат по расширению: png, jpg, bmp, gif
result.write("png", out);                        // в поток (например, ответ HTTP)
byte[] png = result.toPng();                     // в массив байтов (например, для отправки в мессенджер)
```
<!-- /code -->

Для форматов без прозрачности (JPEG, BMP) фон заливается белым.

## Геометрия

`RenderedTable` знает, где нарисован каждый элемент (в логических пикселях, без учёта масштаба) —
это пригодится для карт ссылок или обработки кликов по изображению.

<!-- code: render-geometry -->
```java
RenderedTable result = table.render();
Cell cell = table.body().row(0).cell(1);
Rectangle2D bounds = result.cellBounds(cell);    // где нарисована ячейка
Cell clicked = result.cellAt(120, 45);           // какая ячейка в точке
```
<!-- /code -->

Также доступны `tableBounds()`, `captionBounds()`, `width()`, `height()`, `scale()`.

## Вывод на Graphics2D

Таблицу можно нарисовать на любую графику — например, на векторную для SVG или PDF:

<!-- code: render-graphics -->
```java
// Например, SVGGraphics2D из Apache Batik или PdfBoxGraphics2D
Rectangle2D size = TableRenderer.paint(table, graphics, RenderOptions.DEFAULT);
```
<!-- /code -->

Таблица рисуется от точки (0, 0) текущей системы координат графики, фон не заливается.

## Шрифты

<!-- code: render-fonts -->
```java
String family = Fonts.register(Path.of("fonts/Inter-Regular.ttf"));
Fonts.register(Path.of("fonts/Inter-Bold.ttf"));    // начертания того же семейства
table.cellStyle(s -> s.fontFamily(family, "SansSerif"));
```
<!-- /code -->

`Fonts.register` возвращает имя семейства шрифта; `Fonts.isAvailable(name)` проверяет, установлен ли шрифт.
Если шрифт стиля не найден, используется следующий в списке семейств, затем запасные шрифты.

## Производительность и потоки

- Рендер не изменяет таблицу: одну таблицу можно рисовать много раз и из разных потоков
  (если её не изменяют в это время).
- Таблица на 2000 строк × 5 колонок раскладывается и рисуется примерно за полсекунды.
- Тени с размытием и прозрачность рисуются через промежуточный растр — на больших площадях это заметно дороже.
- На сервере без графической среды запускайте JVM с `-Djava.awt.headless=true`.
