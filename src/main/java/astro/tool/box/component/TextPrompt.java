package astro.tool.box.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * The TextPrompt class will display a prompt over top of a text component when
 * the Document of the text field is empty. The Show property is used to
 * determine the visibility of the prompt.
 *
 * The Font and foreground Color of the prompt will default to those properties
 * of the parent text component. You are free to change the properties after
 * class construction.
 */
public final class TextPrompt extends JLabel implements FocusListener, DocumentListener {

	public enum Show {
		ALWAYS, FOCUS_GAINED, FOCUS_LOST;
	}

	private JTextComponent component;

	private final Show show;

	private Document document;

	private boolean showPromptOnce;

	private int focusLost;

	public TextPrompt(String text) {
		setText(text);
		this.show = Show.FOCUS_LOST;
	}

	public TextPrompt(String text, Show show) {
		setText(text);
		this.show = show;
	}

	public void applyTo(JTextComponent component) {
		this.component = component;
		document = component.getDocument();
		document.addDocumentListener(this);

		setFont(component.getFont());
		setBorder(new EmptyBorder(component.getInsets()));
		setHorizontalAlignment(SwingConstants.LEADING);

		component.addFocusListener(this);
		component.setLayout(new BorderLayout());
		component.add(this);
		checkPrompt();
	}

	private void checkPrompt() {
		if ((document.getLength() > 0) || (showPromptOnce && focusLost > 0)) {
			setForeground(component.getForeground());
			setVisible(false);
			return;
		}
		if (component.hasFocus()) {
			if (show == Show.ALWAYS || show == Show.FOCUS_GAINED) {
				setForeground(Color.GRAY);
				setVisible(true);
			} else {
				setForeground(component.getForeground());
				setVisible(false);
			}
		} else {
			if (show == Show.ALWAYS || show == Show.FOCUS_LOST) {
				setForeground(Color.GRAY);
				setVisible(true);
			} else {
				setForeground(component.getForeground());
				setVisible(false);
			}
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		checkPrompt();
	}

	@Override
	public void focusLost(FocusEvent e) {
		focusLost++;
		checkPrompt();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		checkPrompt();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		checkPrompt();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

}
