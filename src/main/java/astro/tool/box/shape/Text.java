package astro.tool.box.shape;

import static java.lang.Math.round;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Text implements Drawable {

    private final int x;
    private final int y;
    private final int size;
    private final Color color;
    private final String label;

    public Text(double x, double y, double size, Color color, String label) {
        this.x = (int) round(x - size / 2);
        this.y = (int) round(y + size / 2);
        this.size = (int) size;
        this.color = color;
        this.label = label;
    }

    @Override
    public void draw(Graphics graphics) {
        if (label == null) {
            return;
        }
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(color);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("default", Font.BOLD, size + 2));
        g2d.drawString(label, x, y);
    }

}
