# Примеры

Примеры повторяют публичный API из исходников и из `src/test/java/me/manuloff/lib/table/renderer/Test.java`. Импорты везде одни и те же по смыслу: `Table`, типы из `style.*`, `Color`, `ImageIO`, `BufferedImage`.

Сохранение результата:

```java
BufferedImage image = table.render().getImage();
ImageIO.write(image, "png", new File("table.png"));
```

## Базовая таблица

```java
Table table = new Table();

table.tableStyle()
        .backgroundColor(Color.WHITE)
        .padding(new Padding(8, 12))
        .border(1, new Color(0xCCCCCC))
        .borderCollapse(BorderCollapse.COLLAPSE);

table.textStyle()
        .fontName("Arial")
        .fontSize(14)
        .textColor(Color.BLACK)
        .textAlignment(Alignment.CENTER)
        .wordWrap(false);

table.renderQuality(RenderQuality.MAXIMUM);

for (int row = 0; row < 5; row++) {
    for (int col = 0; col < 4; col++) {
        table.getCell(row, col).setText("Cell [" + row + "," + col + "]");
    }
}
```

## Объединение ячеек

`getCell(row, col, rowspan, colspan)` создаёт ячейку, которая занимает несколько позиций сетки. Повторное обращение к любой занятой позиции вернёт ту же ячейку.

```java
Table table = new Table();
table.tableStyle().border(1, Color.BLACK);
table.textStyle().textAlignment(Alignment.CENTER);

table.getCell(0, 0, 1, 2).setText("Две колонки");
table.getCell(0, 2).setText("Одна");

table.getCell(1, 0, 2, 1).setText("Две строки");
table.getCell(1, 1).setText("A");
table.getCell(1, 2).setText("B");
table.getCell(2, 1).setText("C");
table.getCell(2, 2).setText("D");
```

Несколько фрагментов и явный перевод строки:

```java
table.getCell(1, 1)
        .appendText(new Text("МатАН").bold())
        .appendNewLine()
        .appendText("Лекции")
        .appendNewLine()
        .appendText(new Text("301/3").color(new Color(0x1976d2)));
```

## Выравнивание

Горизонталь и вертикаль задаются одним `Alignment`. Чтобы вертикаль была заметна, строке нужна высота больше текста:

```java
table.getRow(0).fixedHeight(80);
table.textStyle().fontName("Arial").fontSize(12);

table.getCell(0, 0).setText("TOP LEFT");
table.getCell(0, 0).textStyle().textAlignment(Alignment.TOP_LEFT);

table.getCell(0, 1).setText("CENTER");
table.getCell(0, 1).textStyle().textAlignment(Alignment.CENTER);

table.getCell(0, 2).setText("BOTTOM RIGHT");
table.getCell(0, 2).textStyle().textAlignment(Alignment.BOTTOM_RIGHT);
```

В тестах также вызывается `textStyle().verticalAlign(VerticalAlign.TOP | MIDDLE | BOTTOM)`. Поле есть в `TextStyle`; позиция блока при отрисовке берётся из `textAlignment`.

## Рамки

Общая сплошная рамка и разные стили на ячейках:

```java
table.tableStyle()
        .backgroundColor(Color.WHITE)
        .borderCollapse(BorderCollapse.COLLAPSE);

table.getCell(0, 0).setText("SOLID");
table.getCell(0, 0).tableStyle()
        .border(Border.all(new Border.BorderSide(BorderStyle.SOLID, 2, Color.RED)));

table.getCell(0, 1).setText("DASHED");
table.getCell(0, 1).tableStyle()
        .border(Border.all(new Border.BorderSide(BorderStyle.DASHED, 2, Color.BLUE)));

table.getCell(0, 2).setText("DOTTED");
table.getCell(0, 2).tableStyle()
        .border(Border.all(new Border.BorderSide(BorderStyle.DOTTED, 2, Color.GREEN)));

table.getCell(0, 3).setText("DOUBLE");
table.getCell(0, 3).tableStyle()
        .border(Border.all(new Border.BorderSide(BorderStyle.DOUBLE, 3, Color.MAGENTA)));
```

Разные стороны одной ячейки:

```java
Border.BorderSide top = new Border.BorderSide(BorderStyle.SOLID, 3, Color.RED);
Border.BorderSide right = new Border.BorderSide(BorderStyle.SOLID, 3, Color.ORANGE);
Border.BorderSide bottom = new Border.BorderSide(BorderStyle.SOLID, 3, Color.BLUE);
Border.BorderSide left = new Border.BorderSide(BorderStyle.SOLID, 3, Color.GREEN);

table.getCell(1, 1).setText("Custom\nBorders");
table.getCell(1, 1).tableStyle().border(new Border(top, right, bottom, left));
```

## Фиксированные размеры

Таблица на 600 логических пикселей по ширине (если сумма колонок меньше — остаток распределяется):

```java
table.fixedWidth(600);
```

Колонки и строки:

```java
table.getColumn(0).fixedWidth(50);
table.getColumn(1).fixedWidth(200);
table.getColumn(2).fixedWidth(100);

table.getRow(0).fixedHeight(60);
table.getRow(1).fixedHeight(40);
```

Ограничения ячейки:

```java
table.getCell(0, 0).setText("minW=150").minWidth(150);
table.getCell(0, 1).setText("Long text").maxWidth(80);
table.getCell(1, 0).setText("minH=60").minHeight(60);
table.getCell(1, 1).setText("maxH=30").maxHeight(30);
table.getCell(2, 0).setText("fixed 100x60").fixedWidth(100).fixedHeight(60);
```

## Перенос текста

При узкой колонке:

```java
table.getColumn(0).fixedWidth(100);

table.getCell(0, 0).setText("This is a long text that should wrap to multiple lines");
table.getCell(0, 0).textStyle().wordWrap(true);

table.getCell(1, 0).setText("Long text without wrap - goes beyond boundaries");
table.getCell(1, 0).textStyle().wordWrap(false);
```

Эквиваленты: `textStyle().wrap()` и `textStyle().noWrap()`.

## Начертание и цвет

```java
table.getCell(0, 0).setText("Bold Text").textStyle().bold(true);
table.getCell(0, 1).setText("Italic Text").textStyle().italic(true);
table.getCell(0, 2).setText("Bold Italic").textStyle().bold(true).italic(true);
table.getCell(1, 0).setText("Underlined").textStyle().underline(true);
table.getCell(1, 1).setText("Strikethrough").textStyle().strikethrough(true);
table.getCell(1, 2).setText("Red Text").textStyle().textColor(Color.RED);

table.getCell(2, 0).setText("Line height 1.5\nSecond line\nThird line");
table.getCell(2, 0).textStyle().lineHeight(1.5f);
table.getCell(2, 0).fixedHeight(100);
```

Стиль на всю колонку:

```java
table.getColumn(0).textStyle().bold();
table.getColumn(1).textStyle().italic();
table.getColumn(2).textStyle().underline(true);
```

## Стиль строки-заголовка

```java
table.getRow(0).tableStyle().backgroundColor(new Color(0x1976D2));
table.getRow(0).textStyle().textColor(Color.WHITE).bold(true);
```

## Скругление и тень

```java
BoxShadow shadow = new BoxShadow()
        .add(0, 4, 8, 0, new Color(0, 0, 0, 80))
        .add(0, 8, 16, 0, new Color(0, 0, 0, 40));

table.tableStyle()
        .backgroundColor(Color.WHITE)
        .boxShadow(shadow)
        .border(1, new Color(0xCCCCCC))
        .borderRadius(8f);
```

## Состояния ячейки

Состояния не вычисляются сами: их выставляет вызывающий код, после чего рендерер берёт `hoverStyle` / `focusStyle` / `activeStyle`.

```java
table.getCell(1, 0).setText("Hover Me").hovered(true);
table.getCell(1, 0).hoverStyle().backgroundColor(new Color(0xE3F2FD));
table.getCell(1, 0).hoverStyle().border(
        Border.all(new Border.BorderSide(BorderStyle.SOLID, 2, new Color(0x2196F3))));

table.getCell(1, 1).setText("Focused").focused(true);
table.getCell(1, 1).focusStyle().backgroundColor(new Color(0xFFF8E1));

table.getCell(1, 2).setText("Active").active(true);
table.getCell(1, 2).activeStyle().backgroundColor(new Color(0xFFEBEE));
```

## Группы строк

```java
for (int row = 0; row < 7; row++) {
    for (int col = 0; col < 3; col++) {
        table.getCell(row, col).setText("Row " + row + ", Col " + col);
    }
}

table.autoConfigureRowGroups(1, 1);

table.thead().tableStyle().backgroundColor(new Color(0x3F51B5));
table.thead().textStyle().textColor(Color.WHITE).bold(true);

table.tbody().tableStyle().backgroundColor(Color.WHITE);

table.tfoot().tableStyle().backgroundColor(new Color(0xFFC107));
table.tfoot().textStyle().textColor(Color.BLACK).bold(true);
```

## Качество рендеринга

```java
table.renderQuality(RenderQuality.LOW);      // scale 1.0, без сглаживания
table.renderQuality(RenderQuality.MEDIUM);   // scale 1.5
table.renderQuality(RenderQuality.HIGH);     // scale 2.0 — значение по умолчанию
table.renderQuality(RenderQuality.MAXIMUM);  // scale 2.0 и quality-hints
```

Размер файла PNG растёт вместе с `scale`: при `HIGH` / `MAXIMUM` картинка в два раза больше логического размера таблицы.
