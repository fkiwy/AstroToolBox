package astro.tool.box.module.shape;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class Diamond implements Drawable {

    private final int x;
    private final int y;
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private final Color color;

    public Diamond(double x, double y, double size, Color color) {
        this.x = (int) round(x);
        this.y = (int) round(y);
        int z = (int) round(size / 2);
        this.x1 = this.x - z;
        this.x2 = this.x + z;
        this.y1 = this.y - z;
        this.y2 = this.y + z;
        this.color = color;
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(color);
        graphics.drawLine(x1, y, x, y1);
        graphics.drawLine(x, y1, x2, y);
        graphics.drawLine(x2, y, x, y2);
        graphics.drawLine(x, y2, x1, y);
    }

}
