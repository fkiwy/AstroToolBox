package astro.tool.box.shape;

import static java.lang.Math.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Arrow implements Drawable {

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
        this.size = size + 2;
        this.color = color;
    }

    @Override
    public void draw(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics;
        double xDiff = x1 - x2;
        double vectorAngle;
        if (xDiff == 0) {
            vectorAngle = PI / 2;
        } else {
            vectorAngle = atan((y1 - y2) / xDiff) + (x1 < x2 ? PI : 0);
        }
        double arrowAngle = PI / 9;
        double xLeft = size * cos(vectorAngle - arrowAngle);
        double yLeft = size * sin(vectorAngle - arrowAngle);
        double xRight = size * cos(vectorAngle + arrowAngle);
        double yRight = size * sin(vectorAngle + arrowAngle);
        //double xPlus = (size / 2) * cos(vectorAngle);
        //double yPlus = (size / 2) * sin(vectorAngle);
        g2d.setColor(color);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(STROKE_WIDTH));
        g2d.drawLine((int) round(x2), (int) round(y2), (int) round(x2 + xLeft), (int) round(y2 + yLeft));
        g2d.drawLine((int) round(x2), (int) round(y2), (int) round(x2 + xRight), (int) round(y2 + yRight));
        g2d.drawLine((int) round(x1), (int) round(y1), (int) round(x2 /*+ xPlus*/), (int) round(y2 /*+ yPlus*/));
    }

}
