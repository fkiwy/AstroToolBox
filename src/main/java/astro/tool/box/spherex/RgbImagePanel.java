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

		// Convert Java 0-based pixel centre coordinates to FITS 1-based pixels.
		double dx = (x + 1.0) - composite.crpix1();
		double dy = (y + 1.0) - composite.crpix2();

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

				g2.setColor(Color.YELLOW);
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
