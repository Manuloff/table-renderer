package me.manuloff.lib.table.renderer.internal.paint.effect;

/**
 * Быстрое приближение гауссова размытия тремя проходами усредняющего фильтра.
 *
 * @author Manuloff
 * @since 2.0
 */
final class Blur {

    private Blur() {
    }

    /**
     * Размывает одноканальное изображение на месте.
     *
     * @param data  значения 0..255, построчно
     * @param sigma стандартное отклонение в пикселях
     */
    static void gaussian(int[] data, int width, int height, double sigma) {
        if (sigma < 0.3) return;
        int[] sizes = boxSizes(sigma, 3);
        int[] tmp = new int[data.length];
        for (int size : sizes) {
            int r = (size - 1) / 2;
            boxHorizontal(data, tmp, width, height, r);
            boxVertical(tmp, data, width, height, r);
        }
    }

    /**
     * Размеры окон для n проходов, дающих в сумме гауссово размытие с заданной сигмой.
     */
    private static int[] boxSizes(double sigma, int n) {
        double ideal = Math.sqrt(12 * sigma * sigma / n + 1);
        int wl = (int) Math.floor(ideal);
        if (wl % 2 == 0) wl--;
        int wu = wl + 2;
        double mIdeal = (12 * sigma * sigma - n * wl * wl - 4 * n * wl - 3 * n) / (-4.0 * wl - 4);
        long m = Math.round(mIdeal);
        int[] sizes = new int[n];
        for (int i = 0; i < n; i++) sizes[i] = i < m ? wl : wu;
        return sizes;
    }

    private static void boxHorizontal(int[] src, int[] dst, int w, int h, int r) {
        if (r <= 0) {
            System.arraycopy(src, 0, dst, 0, src.length);
            return;
        }
        int window = 2 * r + 1;
        for (int y = 0; y < h; y++) {
            int row = y * w;
            int sum = 0;
            for (int x = -r; x <= r; x++) sum += (x >= 0 && x < w) ? src[row + x] : 0;
            for (int x = 0; x < w; x++) {
                dst[row + x] = (sum + window / 2) / window;
                int out = x - r;
                int in = x + r + 1;
                if (out >= 0) sum -= src[row + out];
                if (in < w) sum += src[row + in];
            }
        }
    }

    private static void boxVertical(int[] src, int[] dst, int w, int h, int r) {
        if (r <= 0) {
            System.arraycopy(src, 0, dst, 0, src.length);
            return;
        }
        int window = 2 * r + 1;
        for (int x = 0; x < w; x++) {
            int sum = 0;
            for (int y = -r; y <= r; y++) sum += (y >= 0 && y < h) ? src[y * w + x] : 0;
            for (int y = 0; y < h; y++) {
                dst[y * w + x] = (sum + window / 2) / window;
                int out = y - r;
                int in = y + r + 1;
                if (out >= 0) sum -= src[out * w + x];
                if (in < h) sum += src[in * w + x];
            }
        }
    }
}
