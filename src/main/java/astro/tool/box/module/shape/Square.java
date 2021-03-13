package astro.tool.box.module.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

public class Square implements Drawable {

    private final double x;
    private final double y;
    private final int size;
    private final Color color;

    public Square(double x, double y, double size, Color color) {
        this.x = x - size / 2;
        this.y = y - size / 2;
        this.size = (int) size;
        this.color = color;
    }

    @Override
    public void draw(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(color);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        g2d.draw(new Rectangle2D.Double(x, y, size, size));
    }

}
