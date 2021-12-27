package astro.tool.box.module.shape;

import static java.lang.Math.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(color);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        g2d.drawLine(x - size, y, x + size, y);
        g2d.drawLine(x, y - size, x, y + size);
        g2d.setFont(new Font("default", Font.PLAIN, size - 2));
        int width = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, x - width / 2 + 5, y - size);
    }

}
