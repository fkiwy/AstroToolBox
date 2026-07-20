package astro.tool.box.shape;

import java.awt.*;

import static java.lang.Math.round;

public class Circle implements Drawable {

	private final int x;
	private final int y;
	private final int size;
	private final Color color;
	private final float strokeWidth;

	public Circle(double x, double y, double size, Color color) {
		this(x, y, size, color, STROKE_WIDTH);
	}

	public Circle(double x, double y, double size, Color color, float strokeWidth) {
		this.x = (int) round(x - size / 2);
		this.y = (int) round(y - size / 2);
		this.size = (int) size;
		this.color = color;
		this.strokeWidth = strokeWidth;
	}

	@Override
	public void draw(Graphics graphics) {
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setColor(color);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(strokeWidth));
		g2d.drawOval(x, y, size, size);
	}

}
