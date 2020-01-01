package astro.tool.box.module;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class Arrow {

    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;
    private final double size;
    private final Color color;

    public Arrow(double x1, double y1, double x2, double y2, double size, Color color) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.size = size;
        this.color = color;
    }

    public void draw(Graphics graphics) {
        double xDiff = x1 - x2;
        double vectorAngle;
        if (xDiff == 0) {
            vectorAngle = PI / 2;
        } else {
            vectorAngle = atan((y1 - y2) / xDiff) + (x1 < x2 ? PI : 0);
        }
        double arrowAngle = PI / 7;
        double xLeft = size * cos(vectorAngle - arrowAngle);
        double yLeft = size * sin(vectorAngle - arrowAngle);
        double xRight = size * cos(vectorAngle + arrowAngle);
        double yRight = size * sin(vectorAngle + arrowAngle);
        //double xPlus = (size / 2) * cos(vectorAngle);
        //double yPlus = (size / 2) * sin(vectorAngle);
        graphics.setColor(color);
        graphics.drawLine((int) round(x2), (int) round(y2), (int) round(x2 + xLeft), (int) round(y2 + yLeft));
        graphics.drawLine((int) round(x2), (int) round(y2), (int) round(x2 + xRight), (int) round(y2 + yRight));
        graphics.drawLine((int) round(x1), (int) round(y1), (int) round(x2 /*+ xPlus*/), (int) round(y2 /*+ yPlus*/));
    }

}
