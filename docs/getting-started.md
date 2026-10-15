# Начало работы

## Первая таблица

<!-- example: quick-start -->
```java
Table table = new Table();
table.caption("Продажи за квартал");
table.header("Товар", "Январь", "Февраль", "Март");
table.addRow("Яблоки", 120, 135, 150);
table.addRow("Груши", 80, 95, 70);
table.addRow("Сливы", 45, 50, 62);
table.footer("Итого", 245, 280, 282);

// Числа - по правому краю
table.rule(Match.column(1, 2, 3), Style::right);
```

<img src="images/quick-start.png" alt="quick-start" width="314">
<!-- /example -->

Что здесь происходит:

- `new Table()` создаёт пустую таблицу с оформлением по умолчанию (тонкая серая сетка, жирная шапка);
- `caption(...)` — заголовок над таблицей;
- `header(...)`, `addRow(...)`, `footer(...)` добавляют строки в шапку, тело и подвал;
- `rule(...)` — условное правило: стиль применяется к ячейкам, удовлетворяющим условию
  (здесь — к колонкам с индексами 1–3).

Значения ячеек могут быть любыми объектами: строки выводятся как есть, остальное — через
`String.valueOf` или [свой форматтер](structure.md#значения-и-форматирование).

## Оформление

Каждый уровень таблицы настраивается лямбдой со стилем:

```java
table.style(s -> s.borderRadius(8).margin(16));            // блок таблицы
table.cellStyle(s -> s.padding(8, 12).fontSize(13));       // все ячейки
table.head().style(s -> s.background(new Color(0xEEF2FF))); // шапка
table.column(1).style(s -> s.right());                     // колонка
row.style(s -> s.bold());                                   // строка
cell.style(s -> s.color(Color.RED));                       // ячейка
```

Или целиком — темой:

<!-- example: quick-start-themed -->
```java
table.theme(Themes.MODERN);
```

<img src="images/quick-start-themed.png" alt="quick-start-themed" width="413">
<!-- /example -->

Подробнее: [стили](styles.md), [правила и темы](rules-and-themes.md).

## Сохранение

<!-- code: quick-start-save -->
```java
table.render().write(Path.of("table.png"));

// Чётче на экранах высокой плотности и с белым фоном
table.render(RenderOptions.builder().scale(2).background(Color.WHITE).build())
        .write(Path.of("table@2x.png"));
```
<!-- /code -->

`render()` возвращает [`RenderedTable`](rendering.md): изображение, его размеры и координаты ячеек.

## Основные понятия

| Понятие        | Класс                         | Описание                                                          |
|----------------|-------------------------------|-------------------------------------------------------------------|
| Таблица        | `Table`                       | Корневой объект: секции, колонки, заголовок, стили, правила       |
| Секция         | `Section`                     | Шапка (`head()`), тело (`body()`, `addBody()`), подвал (`foot()`) |
| Строка, ячейка | `Row`, `Cell`                 | Ячейка хранит значение или форматированный текст                  |
| Колонка        | `Column`                      | Ширина и стиль ячеек колонки                                      |
| Стиль          | `Style`, `TableStyle`         | Оформление ячеек и текста; оформление блока таблицы               |
| Текст          | `RichText`, `Span`            | Содержимое ячейки с фрагментами разного стиля                     |
| Правило        | `CellMatcher`, `Match`        | Условие выбора ячеек для оформления                               |
| Тема           | `Theme`, `Themes`             | Набор стилей, применяемый к таблице целиком                       |
| Рендер         | `RenderOptions`, `RenderedTable` | Параметры рендера и результат                                  |

Классы сгруппированы по пакетам: модель — в `me.manuloff.lib.table.renderer`, остальное — в подпакетах
`style`, `content`, `rule`, `theme`, `builder`, `render`. Пакет `internal` — реализация, не используйте его
напрямую.
