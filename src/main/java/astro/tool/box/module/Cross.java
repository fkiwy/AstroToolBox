package astro.tool.box.module;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class Cross implements Drawable {

    private final int x;
    private final int y;
    private final int size;
    private final Color color;

    public Cross(double x, double y, double size, Color color) {
        this.x = (int) round(x);
        this.y = (int) round(y);
        this.size = (int) round(size / 2);
        this.color = color;
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(color);
        graphics.drawLine(x - size, y, x + size, y);
        graphics.drawLine(x, y - size, x, y + size);
    }

}
