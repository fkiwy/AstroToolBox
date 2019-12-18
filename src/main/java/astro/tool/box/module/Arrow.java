package astro.tool.box.module;

import java.awt.Color;
import static java.lang.Math.*;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

public class Arrow {

    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;
    private final double arrowSize;
    private final Color color;

    public Arrow(double startX, double startY, double endX, double endY, double arrowSize) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.arrowSize = arrowSize;
        this.color = Color.BLACK;
    }

    public Arrow(double startX, double startY, double endX, double endY, double arrowSize, Color color) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.arrowSize = arrowSize;
        this.color = color;
    }
    
    public void draw(Graphics2D graphics) {
        double diffX = startX - endX;
        double angle;
        if (diffX == 0) {
            angle = PI / 2;
        } else {
            angle = atan((startY - endY) / diffX) + (startX < endX ? PI : 0);
        }
        double arrowAngle = PI / 5;
        double x1 = arrowSize * cos(angle - arrowAngle);
        double y1 = arrowSize * sin(angle - arrowAngle);
        double x2 = arrowSize * cos(angle + arrowAngle);
        double y2 = arrowSize * sin(angle + arrowAngle);
        double cx = (arrowSize / 2.0f) * cos(angle);
        double cy = (arrowSize / 2.0f) * sin(angle);
        GeneralPath polygon = new GeneralPath();
        polygon.moveTo(endX, endY);
        polygon.lineTo(endX + x1, endY + y1);
        polygon.lineTo(endX + x2, endY + y2);
        polygon.closePath();
        graphics.setColor(color);
        graphics.draw(polygon);
        graphics.drawLine((int) round(startX), (int) round(startY), (int) round(endX + cx), (int) round(endY + cy));
    }

}
