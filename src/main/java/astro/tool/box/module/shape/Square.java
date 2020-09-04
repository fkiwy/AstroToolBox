package astro.tool.box.module.shape;

import static java.lang.Math.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        g2d.drawRect(x, y, size, size);
    }

}
