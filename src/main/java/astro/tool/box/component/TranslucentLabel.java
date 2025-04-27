package astro.tool.box.component;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.JLabel;

public class TranslucentLabel extends JLabel {

	public TranslucentLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public TranslucentLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public TranslucentLabel(String text) {
		super(text);
	}

	public TranslucentLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	public TranslucentLabel(Icon image) {
		super(image);
	}

	public TranslucentLabel() {
		super();
	}

	@Override
	public boolean isOpaque() {
		return false;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, getWidth(), getHeight());
		super.paintComponent(g2d);
		g2d.dispose();
	}
}
