package astro.tool.box.module.shape;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class CrossHair implements Drawable {

    private final int x;
    private final int y;
    private final int size;
    private final Color color;
    private final String label;

    public CrossHair(double x, double y, double size, Color color, String label) {
        this.x = (int) round(x);
        this.y = (int) round(y);
        this.size = (int) round(size / 2);
        this.color = color;
        this.label = label;
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(color);
        graphics.drawLine(x - size, y, x + size, y);
        graphics.drawLine(x, y - size, x, y + size);
        graphics.drawString(label, x, y - size);
    }

}
