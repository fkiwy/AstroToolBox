package astro.tool.box.shape;

import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Cross implements Drawable {

	private final int x;
	private final int y;
	private final int size;
	private final Color color;
	private final float strokeWidth;

	public Cross(double x, double y, double size, Color color) {
		this(x, y, size, color, STROKE_WIDTH);
	}

	public Cross(double x, double y, double size, Color color, float strokeWidth) {
		this.x = (int) round(x);
		this.y = (int) round(y);
		this.size = (int) round(size / 2);
		this.color = color;
		this.strokeWidth = strokeWidth;
	}

	@Override
	public void draw(Graphics graphics) {
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setColor(color);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(strokeWidth));
		g2d.drawLine(x - size, y, x + size, y);
		g2d.drawLine(x, y - size, x, y + size);
	}

}
