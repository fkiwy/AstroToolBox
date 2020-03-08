package astro.tool.box.module.shape;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class CrossHair implements Drawable {

    private final int x;
    private final int y;
    private final int size;
    private final Color color;
    private final String number;

    public CrossHair(double x, double y, double size, Color color, int number) {
        this.x = (int) round(x);
        this.y = (int) round(y);
        this.size = (int) round(size / 2);
        this.color = color;
        this.number = String.valueOf(number);
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(color);
        graphics.drawLine(x - size, y, x + size, y);
        graphics.drawLine(x, y - size, x, y + size);
        graphics.drawString(number, x, y - size);
    }

}
