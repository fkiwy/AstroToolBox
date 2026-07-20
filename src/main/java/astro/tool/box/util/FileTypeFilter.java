package astro.tool.box.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileTypeFilter extends FileFilter {

	private final String extension;
	private final String description;

	public FileTypeFilter(String extension, String description) {
		this.extension = extension;
		this.description = description;
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}
		return file.getName().toLowerCase().endsWith(extension);
	}

	@Override
	public String getDescription() {
		return description + " (*%s)".formatted(extension);
	}

}
