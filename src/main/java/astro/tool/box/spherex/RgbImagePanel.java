package astro.tool.box.spherex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Interactive SPHEREx RGB panel. A click is converted from displayed image
 * coordinates to the celestial coordinates of the common, stacked WCS.
 */
public class RgbImagePanel extends JPanel {

	@FunctionalInterface
	public interface CoordinateClickListener {
		void coordinatesSelected(double raDeg, double decDeg);
	}

	private BufferedImage image;
	private ImagePlotter.RgbComposite composite;
	private double selectedImageX = Double.NaN;
	private double selectedImageY = Double.NaN;
	private CoordinateClickListener listener;

	public RgbImagePanel() {
		setBackground(Color.WHITE);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleClick(e.getX(), e.getY());
			}
		});
	}

	public void setCoordinateClickListener(CoordinateClickListener listener) {
		this.listener = listener;
	}

	public void setComposite(ImagePlotter.RgbComposite composite) {
		this.composite = composite;
		this.image = composite == null ? null : composite.image();
		selectedImageX = Double.NaN;
		selectedImageY = Double.NaN;
		revalidate();
		repaint();
	}

	/**
	 * Marks a celestial position on the composite without treating it as a
	 * mouse click. This lets the initially requested extraction position remain
	 * visible when the RGB image is first displayed.
	 */
	public void setSelectedCoordinates(double raDeg, double decDeg) {
		if (image == null || composite == null) return;

		double deltaRa = raDeg - composite.crval1();
		if (deltaRa > 180.0) deltaRa -= 360.0;
		if (deltaRa < -180.0) deltaRa += 360.0;

		double xiDeg = deltaRa * Math.cos(Math.toRadians(composite.crval2()));
		double etaDeg = decDeg - composite.crval2();
		double determinant = composite.cd11() * composite.cd22()
				- composite.cd12() * composite.cd21();
		if (Math.abs(determinant) < 1.0e-12) return;

		double dx = (composite.cd22() * xiDeg - composite.cd12() * etaDeg) / determinant;
		double dy = (-composite.cd21() * xiDeg + composite.cd11() * etaDeg) / determinant;

		// Invert the pixel conventions used by handleClick().
		double x = dx + composite.crpix1() - 0.5;
		double y = dy + composite.crpix2();
		if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) return;

		selectedImageX = x;
		selectedImageY = y;
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500, 500);
	}

	private Rectangle imageBounds() {

		if (image == null) {
			return new Rectangle();
		}

		/*
		 * Scale the RGB image to fill the available panel area while
		 * preserving its aspect ratio.
		 *
		 * Unlike the previous implementation, allow enlargement above
		 * the native image resolution.
		 */
		double scale =
				Math.min(
						(double) getWidth() / image.getWidth(),
						(double) getHeight() / image.getHeight()
				);

		int drawWidth =
				Math.max(
						1,
						(int) Math.round(
								image.getWidth() * scale
						)
				);

		int drawHeight =
				Math.max(
						1,
						(int) Math.round(
								image.getHeight() * scale
						)
				);

		return new Rectangle(
				(getWidth() - drawWidth) / 2,
				(getHeight() - drawHeight) / 2,
				drawWidth,
				drawHeight
		);
	}

	private void handleClick(int mouseX, int mouseY) {
		if (image == null || composite == null) return;

		Rectangle bounds = imageBounds();
		if (!bounds.contains(mouseX, mouseY)) return;

		double x = (mouseX - bounds.x) * image.getWidth() / (double) bounds.width;
		double y = (mouseY - bounds.y) * image.getHeight() / (double) bounds.height;

		selectedImageX = x;
		selectedImageY = y;
		repaint();

		// Convert Java 0-based pixel center coordinates to FITS 1-based pixels.
		double dx = (x + 0.5) - composite.crpix1();
		double dy = (y + 0.0) - composite.crpix2();

		// Small-field tangent-plane approximation using the actual CD matrix.
		double xiDeg = composite.cd11() * dx + composite.cd12() * dy;
		double etaDeg = composite.cd21() * dx + composite.cd22() * dy;

		double dec = composite.crval2() + etaDeg;
		double cosDec = Math.cos(Math.toRadians(composite.crval2()));
		if (Math.abs(cosDec) < 1.0e-12) return;

		double ra = composite.crval1() + xiDeg / cosDec;
		ra %= 360.0;
		if (ra < 0.0) ra += 360.0;

		if (listener != null) listener.coordinatesSelected(ra, dec);
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		if (image == null) return;

		Graphics2D g2 = (Graphics2D) graphics.create();
		try {
			Rectangle bounds = imageBounds();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);

			if (Double.isFinite(selectedImageX) && Double.isFinite(selectedImageY)) {
				double x = bounds.x + selectedImageX * bounds.width / image.getWidth();
				double y = bounds.y + selectedImageY * bounds.height / image.getHeight();

				g2.setColor(Color.MAGENTA);
				g2.setStroke(new BasicStroke(2.0f));
				int r = 9;
				g2.drawLine((int) Math.round(x - r), (int) Math.round(y),
						(int) Math.round(x + r), (int) Math.round(y));
				g2.drawLine((int) Math.round(x), (int) Math.round(y - r),
						(int) Math.round(x), (int) Math.round(y + r));
			}
		} finally {
			g2.dispose();
		}
	}
}
