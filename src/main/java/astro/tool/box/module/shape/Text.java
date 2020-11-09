package astro.tool.box.module.shape;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

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
        graphics.setColor(color);
        graphics.setFont(new Font("default", Font.BOLD, size + 3));
        graphics.drawString(label, x, y);
    }

}
