package astro.tool.box.module.shape;

import static java.lang.Math.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        g2d.drawLine(x1, y, x, y1);
        g2d.drawLine(x, y1, x2, y);
        g2d.drawLine(x2, y, x, y2);
        g2d.drawLine(x, y2, x1, y);
    }

}
