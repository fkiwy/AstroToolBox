package astro.tool.box.module;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class XCross implements Drawable {

    private final int x;
    private final int y;
    private final int size;
    private final Color color;

    public XCross(double x, double y, double size, Color color) {
        size = 3 * size / 4;
        this.x = (int) round(x - size / 2);
        this.y = (int) round(y - size / 2);
        this.size = (int) round(size);
        this.color = color;
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(color);
        graphics.drawLine(x, y, x + size, y + size);
        graphics.drawLine(x + size, y, x, y + size);
    }

}
