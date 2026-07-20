package astro.tool.box.shape;

import java.awt.*;

import static java.lang.Math.round;

public class XCross implements Drawable {

	private final int x;
	private final int y;
	private final int size;
	private final Color color;

	public XCross(double x, double y, double size, Color color) {
		size = 3 * size / 4;
		this.x = (int) round(x - size / 2);
		this.y = (int) round(y - size / 2);
		this.size = (int) size;
		this.color = color;
	}

	@Override
	public void draw(Graphics graphics) {
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setColor(color);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(STROKE_WIDTH));
		g2d.drawLine(x, y, x + size, y + size);
		g2d.drawLine(x + size, y, x, y + size);
	}

}
