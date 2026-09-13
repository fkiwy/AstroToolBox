package astro.tool.box.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * A directory picker composed of a path field and a button that opens a
 * directory-only file chooser.
 */
public class DirectoryPicker extends JPanel {

	private final JTextField pathField;
	private final JFileChooser fileChooser;
	private Consumer<String> directorySelectionListener = ignored -> {
	};

	public DirectoryPicker(String initialPath) {
		super(new BorderLayout(4, 0));
		pathField = new JTextField(initialPath, 20);
		pathField.addActionListener(event -> notifyDirectorySelection());
		pathField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent event) {
				notifyDirectorySelection();
			}
		});
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select FITS cutouts directory");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);

		JButton browse = new JButton("Browse...");
		browse.addActionListener(event -> chooseDirectory());

		add(pathField, BorderLayout.CENTER);
		add(browse, BorderLayout.EAST);
	}

	public String getSelectedDirectoryPath() {
		return pathField.getText();
	}

	public void setSelectedDirectoryPath(String path) {
		pathField.setText(path);
	}

	/**
	 * Receives a path after the user chooses a directory or confirms an edited
	 * path. Programmatic path updates do not invoke the listener.
	 */
	public void setDirectorySelectionListener(Consumer<String> listener) {
		directorySelectionListener = listener == null ? ignored -> {
		} : listener;
	}

	private void chooseDirectory() {
		String selectedPath = getSelectedDirectoryPath().trim();
		if (!selectedPath.isEmpty()) {
			try {
				Path path = Path.of(selectedPath).toAbsolutePath();
				Path initialDirectory = Files.isDirectory(path) ? path : path.getParent();
				if (initialDirectory != null) fileChooser.setCurrentDirectory(initialDirectory.toFile());
			} catch (Exception ignored) {
				// Keep the chooser's existing directory for an invalid typed path.
			}
		}

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			setSelectedDirectoryPath(fileChooser.getSelectedFile().toPath()
					.toAbsolutePath().normalize().toString());
			notifyDirectorySelection();
		}
	}

	private void notifyDirectorySelection() {
		directorySelectionListener.accept(getSelectedDirectoryPath().trim());
	}
}
