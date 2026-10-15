package me.manuloff.lib.table.renderer.content;

/**
 * Принудительный перенос строки.
 *
 * @author Manuloff
 * @since 2.0
 */
public final class LineBreak implements Inline {

    /**
     * Единственный экземпляр.
     */
    public static final LineBreak INSTANCE = new LineBreak();

    private LineBreak() {
    }

    @Override
    public String toString() {
        return "LineBreak";
    }
}
