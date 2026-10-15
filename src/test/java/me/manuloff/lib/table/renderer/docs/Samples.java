package me.manuloff.lib.table.renderer.docs;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Данные и картинки для примеров документации.
 */
final class Samples {

    private Samples() {
    }

    /**
     * Сотрудник для примеров построения таблицы из коллекции.
     */
    static final class Employee {
        private final String name;
        private final String position;
        private final int age;
        private final double salary;
        private final boolean active;

        Employee(String name, String position, int age, double salary, boolean active) {
            this.name = name;
            this.position = position;
            this.age = age;
            this.salary = salary;
            this.active = active;
        }

        String getName() {
            return this.name;
        }

        String getPosition() {
            return this.position;
        }

        int getAge() {
            return this.age;
        }

        double getSalary() {
            return this.salary;
        }

        boolean isActive() {
            return this.active;
        }
    }

    static List<Employee> employees() {
        return List.of(
                new Employee("Иван Петров", "Разработчик", 29, 185000, true),
                new Employee("Мария Смирнова", "Дизайнер", 34, 160000, true),
                new Employee("Алексей Козлов", "Тестировщик", 41, 120000, false),
                new Employee("Ольга Новикова", "Руководитель проекта", 38, 240000, true));
    }

    /**
     * Флаг из трёх полос.
     */
    static BufferedImage flag(int a, int b, int c, boolean vertical) {
        BufferedImage img = new BufferedImage(60, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        int[] colors = {a, b, c};
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(colors[i]));
            if (vertical) {
                g.fillRect(i * 20, 0, 20, 40);
            } else {
                g.fillRect(0, i * 40 / 3, 60, 40 / 3 + 1);
            }
        }
        g.dispose();
        return img;
    }

    /**
     * "Фотография": небо, солнце, холм.
     */
    static BufferedImage photo(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, new Color(0x60A5FA), 0, h, new Color(0xFDE68A)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(0xF59E0B));
        g.fillOval(w * 2 / 3, h / 6, h / 4, h / 4);
        g.setColor(new Color(0x166534));
        g.fillPolygon(new int[]{0, w / 3, w * 2 / 3, w}, new int[]{h, h / 2, h * 3 / 4, h}, 4);
        g.dispose();
        return img;
    }

    /**
     * Шахматная клетка для плитки.
     */
    static BufferedImage checker(int size, Color a, Color b) {
        BufferedImage img = new BufferedImage(size * 2, size * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(a);
        g.fillRect(0, 0, size * 2, size * 2);
        g.setColor(b);
        g.fillRect(0, 0, size, size);
        g.fillRect(size, size, size, size);
        g.dispose();
        return img;
    }
}
