package astro.tool.box.module;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class Arrow {

    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private final int size;
    private final Color color;

    public Arrow(double x1, double y1, double x2, double y2, double size, Color color) {
        this.x1 = (int) round(x1);
        this.y1 = (int) round(y1);
        this.x2 = (int) round(x2);
        this.y2 = (int) round(y2);
        this.size = (int) round(size);
        this.color = color;
    }

    public void draw(Graphics graphics) {
        double theta = Math.atan2(y2 - y1, x2 - x1);
        double phi = Math.toRadians(20);
        double rho;
        graphics.setColor(color);
        rho = theta + phi;
        drawSide(graphics, rho);
        rho = theta - phi;
        drawSide(graphics, rho);
        graphics.drawLine(x1, y1, x2, y2);
    }

    private void drawSide(Graphics graphics, double rho) {
        int x = (int) round(x2 - size * cos(rho));
        int y = (int) round(y2 - size * sin(rho));
        graphics.drawLine(x2, y2, x, y);
    }

}
