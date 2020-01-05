package astro.tool.box.module.shape;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class Square implements Drawable {

    private final int x;
    private final int y;
    private final int size;
    private final Color color;

    public Square(double x, double y, double size, Color color) {
        this.x = (int) round(x - size / 2);
        this.y = (int) round(y - size / 2);
        this.size = (int) round(size);
        this.color = color;
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(color);
        graphics.drawRect(x, y, size, size);
    }

}
