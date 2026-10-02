# table-renderer

Java-библиотека для программной отрисовки таблиц в растровое изображение (`BufferedImage`).  
Автор: [Manuloff](https://github.com/Manuloff).

Библиотека описывает таблицу как набор ячеек, строк и колонок (с `rowspan` / `colspan` и иерархией стилей) и рисует её через Java 2D. Результат можно сохранить, например, в PNG.

## Зачем это нужно

Когда таблицу нужно не вывести в HTML или консоль, а получить картинку — для превью, отчёта, расписания, сообщения в чат или любого другого места, где удобнее показать готовое изображение. Модель близка к HTML-таблице: ячейки, объединение, строки/колонки, отступы, рамки, выравнивание текста.

## Возможности

Реализовано в текущем коде пакета `me.manuloff.lib.table.renderer`:

- Сборка таблицы через `Table`, `Cell`, `Row`, `Column` и текстовые фрагменты `Text`
- Объединение ячеек: `getCell(row, col, rowspan, colspan)`
- Несколько фрагментов текста в одной ячейке, в том числе с собственным стилем и явным переносом строки (`Text.NEW_LINE`)
- Рендеринг в `RenderedTable`: логические `width` / `height` и `BufferedImage` (обычно сохраняют через `ImageIO` как PNG)
- Иерархия стилей: таблица → строка → колонка → группа строк → ячейка; у ячейки отдельно учитываются состояния hover / focus / active
- Оформление ячейки и таблицы: фон, padding, рамка, `border-collapse` / `border-spacing`, скругление углов
- Стили рамки: `SOLID`, `DASHED`, `DOTTED`, `DOUBLE`, `NONE`; стороны рамки можно задать по отдельности
- Оформление текста: шрифт, размер, цвет, жирный / курсив / подчёркивание / зачёркивание, межстрочный интервал
- Выравнивание текста по девяти позициям (`Alignment`)
- Перенос текста по словам (`wordWrap`)
- Фиксированная ширина/высота таблицы, колонки, строки и ячейки; у ячейки также `min` / `max`
- Пресеты качества отрисовки `RenderQuality` (масштаб изображения и сглаживание)
- Группы строк `thead` / `tbody` / `tfoot` — используются при расчёте эффективного стиля ячейки и показаны в `src/test/java/.../Test.java`
- Тень таблицы (`BoxShadow`) — рисуется рендерером, если стиль её задаёт

Подробности и ограничения конкретных свойств — в [документации по стилям](docs/styles.md).

## Требования

- **Java 11** (`maven.compiler.source` / `target` = 11)
- **Maven** для сборки
- **Lombok** 1.18.34 — зависимость с scope `provided` (нужна при компиляции этой библиотеки; в runtime готового JAR она не требуется)

Рендеринг использует `java.awt` (Java 2D).

## Установка

Библиотека не публикуется в Maven Central. Подключение — из локального репозитория Maven после установки из исходников.

Соберите и установите артефакт:

```bash
mvn install
```

Затем добавьте зависимость с координатами из `pom.xml`:

```xml
<dependency>
    <groupId>me.manuloff.lib</groupId>
    <artifactId>table-renderer</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

В проекте-потребителе Lombok не обязателен: публичный API уже скомпилирован. Для сборки самого `table-renderer` аннотации Lombok (`@Data`, `@Accessors(fluent = true)` и др.) должны быть доступны компилятору.

## Быстрый старт

Минимальный пример — как `test01_BasicTable` в `src/test/java/me/manuloff/lib/table/renderer/Test.java`:

```java
import me.manuloff.lib.table.renderer.Table;
import me.manuloff.lib.table.renderer.style.Alignment;
import me.manuloff.lib.table.renderer.style.BorderCollapse;
import me.manuloff.lib.table.renderer.style.Padding;
import me.manuloff.lib.table.renderer.style.RenderQuality;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class QuickStart {
    public static void main(String[] args) throws IOException {
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

        BufferedImage image = table.render().getImage();
        ImageIO.write(image, "png", new File("table.png"));
    }
}
```

`table.render()` возвращает `RenderedTable`. Размер `BufferedImage` равен логическому размеру таблицы, умноженному на масштаб выбранного `RenderQuality` (у `MAXIMUM` масштаб `2.0`).

## Документация

- [Обзор документации](docs/README.md)
- [Архитектура и основные понятия](docs/architecture.md)
- [Стили и значения по умолчанию](docs/styles.md)
- [Примеры типичных сценариев](docs/examples.md)

Исходный набор сценариев — класс `src/test/java/me/manuloff/lib/table/renderer/Test.java`.

## Статус и лицензия

Репозиторий приватный. Лицензия пока не определена: не копируйте и не распространяйте код без явного разрешения автора.
