package astro.tool.box.module;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class Triangle implements Drawable {

    private final int x;
    private final int y;
    private final int size;
    private final Color color;

    public Triangle(double x, double y, double size, Color color) {
        this.x = (int) round(x - size / 2);
        this.y = (int) round(y + size / 2);
        this.size = (int) round(size);
        this.color = color;
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(color);
        graphics.drawLine(x, y - 1, x + size / 2, y - size);
        graphics.drawLine(x + size / 2, y - size, x + size, y);
        graphics.drawLine(x, y, x + size, y);
    }

}
