package astro.tool.box.module;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class Circle {

    private final int x;
    private final int y;
    private final int size;
    private final Color color;

    public Circle(double x, double y, double size, Color color) {
        this.x = (int) round(x - size / 2);
        this.y = (int) round(y - size / 2);
        this.size = (int) round(size);
        this.color = color;
    }

    public void draw(Graphics graphics) {
        graphics.setColor(color);
        graphics.drawOval(x, y, size, size);
    }

}
