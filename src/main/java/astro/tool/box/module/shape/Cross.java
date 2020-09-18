package astro.tool.box.module.shape;

import static java.lang.Math.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        g2d.drawLine(x - size, y, x + size, y);
        g2d.drawLine(x, y - size, x, y + size);
    }

}
