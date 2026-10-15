package me.manuloff.lib.table.renderer.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Пример для документации: статический метод без параметров, возвращающий таблицу.
 * Код примера - участок метода между {@code // region <имя>} и {@code // endregion};
 * картинка сохраняется в {@code docs/images/<имя>.png}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Example {

    /**
     * Имя примера (совпадает с именем участка кода).
     */
    String value();
}
