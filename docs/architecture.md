# Устройство библиотеки

Страница для тех, кто дорабатывает библиотеку.

## Пакеты

```
me.manuloff.lib.table.renderer
├── (корень)         модель: Table, Section, Row, Cell, Column, Caption
├── content          форматированный текст: RichText, Span, InlineImage, ...
├── style            Style (ячейки и текст), TableStyle (блок таблицы)
│   ├── box          Insets, Size, Shadow, Overflow
│   ├── border       BorderSide, BorderStyle, Corners
│   ├── background   Gradient и его виды, ColorStop, ImageLayer, ObjectFit
│   ├── text         выравнивание, оформление, переносы
│   └── table        BorderCollapse, TableLayout, CaptionSide, EmptyCells
├── rule             CellMatcher, CellContext, Match, StyleRule
├── theme            Theme, Themes
├── builder          TableBuilder, ColumnSpec
├── render           TableRenderer, RenderOptions, RenderedTable, Fonts
└── internal         реализация (не публичный API)
    ├── style        значения по умолчанию и наследование стилей
    ├── layout       раскладка таблицы
    ├── text         текстовый движок
    ├── render       порядок отрисовки, холст
    └── paint        примитивы: фоны, рамки, тени, эффекты
        ├── background
        ├── border
        └── effect
```

Публичные пакеты зависят только друг от друга; `internal` используют только `render.TableRenderer`
и `render.Fonts`.

## Конвейер рендера

```
Table ──► TableScene (internal.render)
           │
           ├─► TableLayoutEngine (internal.layout)
           │     1. TableGrid           - ячейки по сетке с учётом colspan/rowspan
           │     2. CellStyleResolver   - каскад стилей, содержимое ячейки
           │     3. Paragraph           - подготовка текста (internal.text)
           │     4. CollapsedBorders    - общие рамки и толщины для раскладки
           │     5. ColumnWidths        - ширина колонок (auto / fixed)
           │     6. RowHeights          - текст по ширине, высота строк, базовые линии
           │     7. линии сетки, геометрия ячеек, вертикальное выравнивание
           │     8. CaptionBox          - заголовок по ширине таблицы
           │     └─► LaidOutTable
           │
           └─► TablePainter / CellPainter / CaptionPainter
                 таблица: тени, фон, рамка → ячейки: фоны групп, тени, фон, рамка
                 → общие рамки → текст ячеек → контуры
```

### Текстовый движок (`internal.text`)

```
RichText ─► ParagraphTextBuilder ─► ParagraphText ─► LineBreaker ─► LineBuilder ─► TextBlock ─► TextPainter
            стили фрагментов,       строка, блоки,    места переноса,  фрагменты строк,  строки        текст, тени,
            пробелы, регистр,       прогоны шрифтов,  min/max ширина,  высота, базовая               линии, бейджи,
            запасные шрифты         измерение         разбиение        линия, обрезка                изображения
```

`Paragraph` — фасад: `build` → `minContentWidth` / `maxContentWidth` → `layout(width)`.

### Примитивы отрисовки (`internal.paint`)

- `BoxPainter` + `BoxDecor` — оформление любого прямоугольного блока (ячейки, таблицы, бейджа, изображения):
  внешние тени → фон → внутренние тени → рамка; контур отдельно.
- `background` — `BackgroundPainter` (цвет и слои), `GradientPaints` (градиенты по правилам CSS), `ConicPaint`.
- `border` — `BorderPainter` (рамки блоков всех стилей со скруглениями), `CollapsedBorderPainter` (общие рамки),
  `BorderStrokes` (штрихи, оттенки).
- `effect` — `BlurPainter` (заливка с размытием для теней), `OpacityLayer` (групповая прозрачность).

## Как добавить свойство стиля

1. Поле в `Style` (или `TableStyle`) с javadoc: что делает и значение по умолчанию.
   Для числовых полей добавьте явный метод записи с примитивом (`fontSize(double)`), иначе Lombok создаст
   метод только для `Double`.
2. Значение по умолчанию — в `internal.style.Styles.base()`. Если свойство не должно наследоваться
   фрагментами текста — сбросьте его в `Styles.inline`.
3. Используйте свойство в раскладке (`internal.layout` / `internal.text`) и/или отрисовке (`internal.paint` /
   `internal.render`).
4. Добавьте тест и пример в документацию.

## Документация и примеры

Примеры кода в `docs/` и `README.md` не пишутся вручную — они подставляются из исходников
`src/test/java/.../docs/*Examples.java`:

- участок кода размечается комментариями `// region имя` … `// endregion`;
- метод с аннотацией `@Example("имя")` возвращает таблицу, её картинка сохраняется в `docs/images/имя.png`;
- в markdown места отмечаются `<!-- example: имя --><!-- /example -->` (код и картинка),
  `<!-- code: ... -->` (только код), `<!-- image: ... -->` (только картинка).

`DocsTest` проверяет, что все примеры рисуются и документация соответствует коду. После изменений:

```shell
mvn test -Dtest=DocsTest -Ddocs.update=true
```

## Тесты

| Тест               | Что проверяет                                                         |
|--------------------|-----------------------------------------------------------------------|
| `TableRenderTest`  | Раскладка и отрисовка через публичный API: размеры, объединения, правила, рамки, темы |
| `ParagraphTest`    | Текстовый движок: пробелы, переносы, обрезка, выравнивание            |
| `DocsTest`         | Примеры документации и её актуальность                                |
