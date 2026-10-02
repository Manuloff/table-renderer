# Архитектура и основные понятия

Библиотека делится на **модель** (то, что описывает таблицу) и **рендерер** (то, что рисует её в `BufferedImage`).

```
Table
 ├── cells : List<Cell>
 ├── rows  : Map<Integer, Row>
 ├── columns : Map<Integer, Column>
 ├── tableStyle / textStyle
 ├── renderQuality, fixedWidth, fixedHeight
 └── render() ──► RenderedTable
                    ├── width, height   (логические пиксели)
                    └── image           (BufferedImage, с учётом scale)
```

Все перечисленные типы находятся в пакете `me.manuloff.lib.table.renderer`, стили — в `me.manuloff.lib.table.renderer.style`.

## Table

`Table` — корневой объект. Ячейки не создаются заранее сеткой: они появляются при обращении к `getCell`.

| Метод / свойство | Назначение |
| --- | --- |
| `getCell(row, col)` | Ячейка `1×1` в позиции `(row, col)`. Если в этой позиции уже есть ячейка (в том числе из-за span), возвращается она. |
| `getCell(row, col, rowspan, colspan)` | То же, но с объединением. `rowspan` и `colspan` должны быть ≥ 1, индексы — ≥ 0. |
| `getRow(row)` / `getColumn(col)` | Строка или колонка; создаётся при первом обращении. |
| `getRowCount()` / `getColumnCount()` | Число строк и колонок: максимальный занятый индекс + 1 с учётом span. Для пустой таблицы — `0`. |
| `tableStyle()` / `textStyle()` | Глобальные стили. Есть перегрузки с `Consumer`, возвращающие `this`. |
| `renderQuality()` | Пресет качества, по умолчанию `RenderQuality.HIGH`. |
| `fixedWidth()` / `fixedHeight()` | Фиксированный логический размер таблицы или `null` (авто по содержимому). |
| `render()` | Считает раскладку и возвращает `RenderedTable`. |

Индексация с нуля: строка `0` — первая сверху, колонка `0` — первая слева.

Поиск существующей ячейки: если запрошенная позиция попадает внутрь уже созданной ячейки (включая её `rowspan` / `colspan`), возвращается эта ячейка, новая не создаётся.

## Cell

Ячейка хранит позицию, размер в сетке, тексты и свои стили.

| Поле / метод | Назначение |
| --- | --- |
| `row`, `col`, `rowspan`, `colspan` | Позиция и объединение. Задаются в конструкторе и больше не меняются. |
| `setText(String)` / `setText(Text)` | Заменить содержимое одним фрагментом. |
| `setTexts(List<Text>)` | Заменить содержимое списком фрагментов. |
| `appendText(String)` / `appendText(Text)` | Добавить фрагмент. |
| `appendNewLine()` | Добавить маркер `Text.NEW_LINE`. |
| `tableStyle()` / `textStyle()` | Стиль этой ячейки. |
| `hoverStyle()` / `focusStyle()` / `activeStyle()` | Переопределения `TableStyle` для состояний. |
| `hovered(boolean)` / `focused(boolean)` / `active(boolean)` | Включить состояние. Приоритет при наложении: active, затем focus, затем hover. |
| `fixedWidth` / `fixedHeight` / `minWidth` / `maxWidth` / `minHeight` / `maxHeight` | Ограничения размера ячейки (пиксели). |
| `getEffectiveTableStyle()` | Итоговый `TableStyle` после иерархии и состояний. |
| `getEffectiveTextStyle()` | Итоговый `TextStyle` после иерархии. |

Рендерер рисует только те ячейки, которые есть в `table.cells()`. «Пустые» позиции сетки, к которым ни разу не вызывали `getCell`, не создаются и не рисуются.

## Row и Column

`Row` и `Column` — контейнеры стиля и фиксированного размера для всех ячеек с данным индексом.

- `Row.fixedHeight(Integer)` — фиксированная высота строки; `null` означает авто по содержимому.
- `Column.fixedWidth(Integer)` — фиксированная ширина колонки; `null` — авто.
- У обоих есть `tableStyle()` и `textStyle()`, в том числе с `Consumer`.
- `getCells()` возвращает ячейки таблицы, у которых `row` или `col` совпадает с индексом (ячейки, попавшие в строку/колонку только за счёт span, в этот список не входят).

Объекты строк и колонок создаются лениво. Даже чтение `table.getRow(i)` создаёт запись в карте строк.

## Text

`Text` — фрагмент содержимого ячейки со своим `TextStyle`.

- `new Text("...")` — обычный фрагмент.
- `Text.NEW_LINE` — маркер новой строки (`"\n"`). Рендерер обрабатывает его отдельно от обычного текста.
- Удобные методы: `bold()`, `italic()`, `color(Color)`, `size(int)`, `font(String)`.
- Стиль фрагмента накладывается на эффективный стиль ячейки в момент раскладки текста (`applyTo`).

В одной ячейке может быть несколько фрагментов: заголовок жирным, подпись другим цветом и т.д.

## RenderedTable

`RenderedTable` создаётся только из `Table.render()`. Конструктор пакетный.

| Свойство | Смысл |
| --- | --- |
| `table` | Исходная таблица. |
| `width` / `height` | Логический размер в пикселях (до масштаба качества). |
| `getImage()` | `BufferedImage` типа `TYPE_INT_ARGB`. Ширина и высота картинки = `ceil(логика × scale)`. |

Порядок работы рендерера:

1. Посчитать ширины колонок (фиксированная ширина колонки либо оценка по тексту + padding, с `min` / `max` ячейки и делением на `colspan`).
2. Посчитать высоты строк (аналогично, с `rowspan`).
3. Если заданы `table.fixedWidth()` / `fixedHeight()` и текущая сумма меньше целевой — распределить остаток по колонкам/строкам.
4. Создать изображение с учётом `RenderQuality.getScale()`.
5. Настроить `Graphics2D` (сглаживание, fractional metrics, hints качества).
6. Нарисовать фон таблицы, при необходимости тень, затем ячейки (фон, рамки, текст).

При `border-collapse: collapse` нижняя и правая стороны рамки ячейки не рисуются, если справа или снизу есть соседняя ячейка. При `separate` учитывается `borderSpacing`.

## Иерархия стилей

Эффективный стиль ячейки собирается наложением не-`null` полей (`applyTo`): более частный уровень перезаписывает более общий.

**TableStyle** (`getEffectiveTableStyle`):

1. `table.tableStyle()`
2. `table.getRow(row).tableStyle()`
3. `table.getColumn(col).tableStyle()`
4. Стиль группы строк (`thead` / `tbody` / `tfoot`), если строка в неё входит
5. `cell.tableStyle()`
6. При активных флагах: `activeStyle`, затем `focusStyle`, затем `hoverStyle`

**TextStyle** (`getEffectiveTextStyle`):

1. `table.textStyle()`
2. стиль строки
3. стиль колонки
4. стиль группы строк
5. `cell.textStyle()`

Дополнительно каждый `Text` применяет свой `textStyle` поверх эффективного стиля ячейки.

## Группы строк

`Cell` при расчёте стиля просматривает `table.thead()`, `table.tbody()`, `table.tfoot()`. У группы есть `getRowIndices()`, `tableStyle()` и `textStyle()`.

В `src/test/java/me/manuloff/lib/table/renderer/Test.java` группы настраиваются так:

```java
table.autoConfigureRowGroups(1, 1); // одна строка заголовка, одна — подвала

table.thead().tableStyle().backgroundColor(new Color(0x3F51B5));
table.thead().textStyle().textColor(Color.WHITE).bold(true);

table.tbody().tableStyle().backgroundColor(Color.WHITE);

table.tfoot().tableStyle().backgroundColor(new Color(0xFFC107));
table.tfoot().textStyle().textColor(Color.BLACK).bold(true);
```

`autoConfigureRowGroups(headerRows, footerRows)` в тестах относит первые `headerRows` строк к `thead`, последние `footerRows` — к `tfoot`, остальные — к `tbody`.

## Lombok и форма вызовов

Модель и стили используют `@Data` и `@Accessors(fluent = true)`:

```java
table.renderQuality(RenderQuality.HIGH);
table.tableStyle().backgroundColor(Color.WHITE).padding(new Padding(8));
table.textStyle(style -> style.fontSize(14).bold(true));
```

Геттер и сеттер выглядят одинаково: без аргумента — чтение, с аргументом — запись и возврат `this` (для стилей) либо самого объекта.
