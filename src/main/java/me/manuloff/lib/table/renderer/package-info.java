/**
 * Модель таблицы: {@link me.manuloff.lib.table.renderer.Table} и её части - секции, строки, ячейки,
 * колонки, заголовок. Отсюда начинается работа с библиотекой:
 * <pre>{@code
 * Table table = new Table();
 * table.header("Товар", "Цена");
 * table.addRow("Яблоки", 120);
 * table.render().write(Path.of("table.png"));
 * }</pre>
 * Связанные пакеты: {@code style} - оформление, {@code content} - форматированный текст,
 * {@code rule} - условные правила, {@code theme} - темы, {@code builder} - таблица из коллекции,
 * {@code render} - параметры и результат рендера.
 */
package me.manuloff.lib.table.renderer;
