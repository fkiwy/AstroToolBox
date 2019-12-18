package astro.tool.box.module;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.Graphics;

public class Circle {

    private final double x;
    private final double y;
    private final double diameter;
    private final Color color;

    public Circle(double x, double y, double diameter) {
        this.x = x - diameter / 2;
        this.y = y - diameter / 2;
        this.diameter = diameter;
        this.color = Color.BLACK;
    }

    public Circle(double x, double y, double diameter, Color color) {
        this.x = x - diameter / 2;
        this.y = y - diameter / 2;
        this.diameter = diameter;
        this.color = color;
    }

    public void draw(Graphics graphics) {
        graphics.setColor(color);
        graphics.drawOval((int) round(x), (int) round(y), (int) round(diameter), (int) round(diameter));
    }

}
