package astro.tool.box.component;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A directory picker composed of a path field and a button that opens a
 * directory-only file chooser.
 */
public class DirectoryPicker extends JPanel {

	private final JTextField pathField;
	private final JFileChooser fileChooser;

	public DirectoryPicker(String initialPath) {
		super(new BorderLayout(4, 0));
		pathField = new JTextField(initialPath, 20);
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
		}
	}
}
