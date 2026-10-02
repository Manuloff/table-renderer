# Стили

Стили живут в пакете `me.manuloff.lib.table.renderer.style`. Почти все свойства — fluent-методы Lombok: `style.fontSize(14)` записывает значение и возвращает тот же объект.

`applyTo(target)` копирует в цель только не-`null` поля. Так работает иерархия: частный стиль перезаписывает общий, не затирая остальные поля значениями «не задано».

## TableStyle

Стиль фона, рамок и геометрии таблицы / строки / колонки / ячейки.

Значения, которые конструктор выставляет сразу (initializer):

| Свойство | Тип | По умолчанию |
| --- | --- | --- |
| `padding` | `Padding` | `new Padding(10)` — по 10 px со всех сторон |
| `border` | `Border` | `Border.DEFAULT` |
| `borderCollapse` | `BorderCollapse` | `BorderCollapse.COLLAPSE` |
| `borderSpacing` | `Integer` | `0` |
| `borderRadius` | `Float` | `0f` |
| `backgroundColor` | `Color` | `null` (фон не заливается) |
| `width`, `height`, `minWidth`, `maxWidth`, `minHeight`, `maxHeight` | `Integer` | `null` |
| `borderWidth`, `borderColor` | `Integer` / `Color` | `null` |

Константа `TableStyle.DEFAULT` — это `new TableStyle()`, то есть тот же набор значений.

### Что реально использует рендерер

Из `RenderedTable`:

- `backgroundColor` — заливка таблицы и ячейки; при `borderRadius > 0` рисуется скруглённый прямоугольник.
- `padding` — внутренние отступы ячейки. Если после разрешения стиля padding вдруг `null`, рендерер подставляет `new Padding(1)`.
- `border` — четыре стороны рамки. Если `null`, берётся `Border.NONE`.
- `borderCollapse` — `COLLAPSE` (по умолчанию в рендерере, если поле `null`) или `SEPARATE`.
- `borderSpacing` — зазор между ячейками только в режиме `SEPARATE`.
- `borderRadius` — скругление фона таблицы и ячейки.
- `boxShadow` — если у стиля таблицы задана тень с слоями, рендерер рисует её до ячеек (`BoxShadow.hasShadows()`, слои `ShadowLayer`).

Поля `width` / `height` / `min*` / `max*` и `borderWidth` / `borderColor` хранятся в стиле и участвуют в `applyTo` / `clone`, но раскладку рендерер берёт из `Table.fixedWidth` / `fixedHeight`, размеров `Row` / `Column` / `Cell` и объекта `border`, а не из этих полей.

### Удобные методы рамки

```java
table.tableStyle().border(1, new Color(0xCCCCCC));
table.tableStyle().border(BorderStyle.DASHED, 2, Color.BLUE);
```

Оба метода ставят всем четырём сторонам один и тот же `Border.BorderSide`.

### Border, BorderStyle, BorderCollapse

`BorderStyle`:

| Значение | Рендерер |
| --- | --- |
| `NONE` | Сторона не рисуется (`isVisible()` должно быть ложно) |
| `SOLID` | Сплошная линия |
| `DASHED` | Штрихи (`width * 3` / `width * 2`) |
| `DOTTED` | Точки |
| `DOUBLE` | Двойная линия (несколько проходов stroke) |

`type` и `filled` у enum на отрисовку не влияют: ветка выбирается по самому значению enum.

`BorderCollapse`:

- `COLLAPSE` — общие границы, нижняя/правая сторона ячейки скрывается при наличии соседа.
- `SEPARATE` — ячейки с зазором `borderSpacing`.

`Border` задаёт стороны по отдельности. По использованию в `TableStyle`, `RenderedTable` и тестах:

```java
// одинаковая сторона со всех сторон
cell.tableStyle().border(Border.all(
        new Border.BorderSide(BorderStyle.SOLID, 2, Color.RED)));

// четыре разные стороны: top, right, bottom, left
cell.tableStyle().border(new Border(topSide, rightSide, bottomSide, leftSide));

// точечная замена сторон
cell.tableStyle().border(
        new Border()
                .withTop(new Border.BorderSide(BorderStyle.SOLID, 3, Color.RED))
                .withBottom(new Border.BorderSide(BorderStyle.SOLID, 1, Color.GRAY)));

cell.tableStyle().border(Border.all(new Border.BorderSide(BorderStyle.NONE, 0, null)));
```

У стороны: конструктор `(BorderStyle, width, Color)`, методы `style()`, `width()`, `color()`, `isVisible()`.  
У рамки: `top()`, `right()`, `bottom()`, `left()`, константы `Border.NONE` и `Border.DEFAULT`.

### BoxShadow

Конструирование из тестов и чтение в рендерере:

```java
BoxShadow shadow = new BoxShadow()
        .add(0, 4, 8, 0, new Color(0, 0, 0, 80))
        .add(0, 8, 16, 0, new Color(0, 0, 0, 40));

table.tableStyle().boxShadow(shadow);
```

Аргументы `add`: смещение X, смещение Y, blur, spread, цвет.  
Слой (`BoxShadow.ShadowLayer`): `xOffset()`, `yOffset()`, `blurRadius()`, `spreadRadius()`, `color()`.

## TextStyle

Стиль текста таблицы, строки, колонки, группы, ячейки и отдельного `Text`.

Значения initializer'а:

| Свойство | Тип | По умолчанию |
| --- | --- | --- |
| `fontName` | `String` | `"Arial"` |
| `fontStyle` | `Integer` | `Font.PLAIN` |
| `fontSize` | `Integer` | `12` |
| `textColor` | `Color` | `Color.BLACK` |
| `textAlignment` | `Alignment` | `Alignment.TOP_LEFT` |
| `lineHeight` | `Float` | `1.0f` |
| `verticalAlign` | `VerticalAlign` | `VerticalAlign.TOP` |
| `wordWrap` | `Boolean` | `false` |
| `overflow` | `Boolean` | `false` |
| `textOverflow` | `String` | `"ellipsis"` |
| `bold` | `Boolean` | `false` |
| `italic` | `Boolean` | `false` |
| `underline` | `Boolean` | `false` |
| `strikethrough` | `Boolean` | `false` |

`TextStyle.DEFAULT` — `new TextStyle()`.

Есть конструктор с основными параметрами:  
`TextStyle(fontName, fontStyle, fontSize, textColor, textAlignment, lineHeight)` — он вызывает `this()` (подставляет defaults), затем перезаписывает переданные поля.

### Что использует рендерер

- `fontName`, `fontSize`, `getEffectiveFontStyle()` — создание `java.awt.Font`.
- `textColor` — цвет строки; если `null`, берётся чёрный.
- `textAlignment` — позиция блока текста и выравнивание отдельных строк (`TOP_CENTER` / `CENTER` / `BOTTOM_CENTER` и правые аналоги центрируют или прижимают каждую строку).
- `wordWrap` — перенос по пробелам; если `null`, рендерер считает, что перенос включён. Значение по умолчанию в самом `TextStyle` при этом `false`.
- `lineHeight` — множитель высоты строки (`fm.getHeight() * lineHeight`, при `null` — `1.0`).
- `underline` / `strikethrough` — линия под текстом или через него.

`getEffectiveFontStyle()`:

- `bold && italic` → `Font.BOLD | Font.ITALIC`
- только `bold` → `Font.BOLD`
- только `italic` → `Font.ITALIC`
- иначе `fontStyle`, либо `Font.PLAIN`, если он `null`

Поля `verticalAlign`, `overflow` и `textOverflow` хранятся и копируются в `applyTo` / `clone`. Раскладка текста в `RenderedTable` ориентируется на `textAlignment` и `wordWrap`, а не на эти три поля.

### Удобные методы

| Метод | Действие |
| --- | --- |
| `bold()` / `italic()` / `boldItalic()` | Ставят соответствующие флаги в `true` |
| `bold(true)` / `italic(true)` / `underline(true)` / `strikethrough(true)` | Fluent-сеттеры Lombok |
| `noWrap()` | `wordWrap = false` |
| `wrap()` | `wordWrap = true` |

`VerticalAlign` в тестах: `TOP`, `MIDDLE`, `BOTTOM`.

## Alignment

Девять позиций. У каждой есть горизонталь (`TextAlign`: `LEFT`, `CENTER`, `RIGHT`) и вертикаль (`TextVAlign`: `TOP`, `CENTER`, `BOTTOM`).

| Константа | Горизонталь | Вертикаль |
| --- | --- | --- |
| `TOP_LEFT` | left | top |
| `TOP_CENTER` | center | top |
| `TOP_RIGHT` | right | top |
| `CENTER_LEFT` | left | center |
| `CENTER` | center | center |
| `CENTER_RIGHT` | right | center |
| `BOTTOM_LEFT` | left | bottom |
| `BOTTOM_CENTER` | center | bottom |
| `BOTTOM_RIGHT` | right | bottom |

Статические сокращения (всегда с вертикалью **top**):

- `Alignment.left()` → `TOP_LEFT`
- `Alignment.center()` → `TOP_CENTER`
- `Alignment.right()` → `TOP_RIGHT`

`Alignment.CENTER` и `Alignment.center()` — разные значения.

## Padding

Неизменяемые отступы. Отрицательные значения запрещены (`IllegalArgumentException`).

| Конструктор | Смысл |
| --- | --- |
| `Padding(top, bottom, left, right)` | Четыре стороны |
| `Padding(vertical, horizontal)` | `top = bottom = vertical`, `left = right = horizontal` |
| `Padding(padding)` | Одинаково со всех сторон |

Константы: `Padding.DEFAULT` = `(1, 1, 1, 1)`, `Padding.ZERO` = `(0, 0, 0, 0)`.  
У `TableStyle` при создании всё же ставится `new Padding(10)`, а не `Padding.DEFAULT`.

Методы: `top()` / `bottom()` / `left()` / `right()`, `copy()`, `verticalSum()`, `horizontalSum()`.

## RenderQuality

Пресет для `Table.renderQuality(...)`. По умолчанию у таблицы — `HIGH`.

| Пресет | `scale` | Anti-aliasing | Fractional metrics | High-quality hints |
| --- | --- | --- | --- | --- |
| `LOW` | 1.0 | нет | нет | нет |
| `MEDIUM` | 1.5 | да | нет | нет |
| `HIGH` | 2.0 | да | да | нет |
| `MAXIMUM` | 2.0 | да | да | да |

`scale` увеличивает размер `BufferedImage` относительно логических `width` / `height`.  
`HIGH` и `MAXIMUM` дают картинку в два раза шире и выше логического размера.

High-quality hints (`MAXIMUM`): `KEY_RENDERING`, `KEY_STROKE_CONTROL`, `KEY_INTERPOLATION`, `KEY_ALPHA_INTERPOLATION`, `KEY_COLOR_RENDERING`.
