package astro.tool.box.tab;

import static astro.tool.box.function.NumericFunctions.toInteger;
import static astro.tool.box.main.ToolboxHelper.USER_HOME;
import static astro.tool.box.main.ToolboxHelper.html;
import static astro.tool.box.main.ToolboxHelper.showConfirmDialog;
import static astro.tool.box.main.ToolboxHelper.showErrorDialog;
import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;
import static astro.tool.box.util.Constants.LINE_SEP;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;

import astro.tool.box.component.TextPrompt;
import astro.tool.box.container.CustomOverlay;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.Shape;
import astro.tool.box.util.FileTypeFilter;

public class CustomOverlaysTab implements Tab {

	public static final String TAB_NAME = "Custom Overlays";
	private static final String OVERLAYS_FILE_NAME = "/AstroToolBoxOverlays.txt";
	private static final String OVERLAYS_PATH = USER_HOME + OVERLAYS_FILE_NAME;

	private final Map<String, CustomOverlay> customOverlays = new LinkedHashMap<>();

	private final JFrame baseFrame;
	private final JTabbedPane tabbedPane;
	private final ImageViewerTab imageViewerTab;

	private boolean allOverlaysCreated;

	public CustomOverlaysTab(JFrame baseFrame, JTabbedPane tabbedPane, ImageViewerTab imageViewerTab) {
		this.baseFrame = baseFrame;
		this.tabbedPane = tabbedPane;
		this.imageViewerTab = imageViewerTab;
	}

	@Override
	public void init(boolean visible) {
		try {
			try (Scanner scanner = new Scanner(new File(OVERLAYS_PATH))) {
				while (scanner.hasNextLine()) {
					CustomOverlay customOverlay = new CustomOverlay();
					customOverlay.deserialize(scanner.nextLine());
					customOverlays.put(customOverlay.getName(), customOverlay);
				}
			} catch (Exception ex) {
				if (ex instanceof FileNotFoundException) {
				} else {
					showExceptionDialog(baseFrame, ex);
					return;
				}
			}
			imageViewerTab.setCustomOverlays(customOverlays);
			List<CustomOverlay> overlays = new ArrayList<>();
			customOverlays.values().forEach(overlay -> {
				overlays.add(overlay);
			});

			JPanel container = new JPanel(new BorderLayout());

			if (visible) {
				tabbedPane.addTab(TAB_NAME, container);
			}

			GridLayout layout = new GridLayout(overlays.size() + 50, 1);
			JPanel overlayPanel = new JPanel(layout);
			container.add(new JScrollPane(overlayPanel), BorderLayout.CENTER);

			JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			overlayPanel.add(topPanel);

			JButton addButton = new JButton("Create new overlay");
			topPanel.add(addButton);
			addButton.addActionListener((ActionEvent evt) -> {
				layout.setRows(layout.getRows() + 1);
				addOverlayRow(overlayPanel, new CustomOverlay());
				baseFrame.setVisible(true);
			});

			JLabel topRowLabel = new JLabel(
					html("""
							Overlays can be created by specifying either a local CSV file (<span style='background:#CCFFCC'>green fields</span>), \
							a VizieR catalog (<span style='background:#FFFFCC'>yellow fields</span>), \
							or a TAP access URL (<span style='background:#FFEBCC'>orange fields</span> plus <span style='background:#FFFFCC'>RA & Dec column names</span>). \
							Don't forget to save your overlays using the 'Save' button at the end of the overlay row.\
							"""));
			topPanel.add(topRowLabel);

			tabbedPane.addChangeListener((ChangeEvent evt) -> {
				baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
				int index = sourceTabbedPane.getSelectedIndex();
				if (!allOverlaysCreated && sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
					for (CustomOverlay overlay : overlays) {
						addOverlayRow(overlayPanel, overlay);
					}
					allOverlaysCreated = true;
				}
				baseFrame.setCursor(Cursor.getDefaultCursor());
			});
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		}
	}

	private void addOverlayRow(JPanel overlayPanel, CustomOverlay customOverlay) {
		JPanel overlayRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (customOverlay.getName() == null) {
			overlayPanel.add(overlayRow, 1);
		} else {
			overlayPanel.add(overlayRow);
		}

		String overlayName = customOverlay.getName();
		String tableName = customOverlay.getTableName();
		String tapUrl = customOverlay.getTapUrl();

		JTextField overlayNameField = new JTextField(18);
		overlayRow.add(overlayNameField);
		TextPrompt overlayNamePrompt = new TextPrompt("Overlay name");
		overlayNamePrompt.applyTo(overlayNameField);
		overlayNameField.setText(overlayName);
		overlayNameField.setEditable((overlayName == null));

		JButton chooseColorButton = new JButton("Choose color");
		overlayRow.add(chooseColorButton);
		Color overlayColor = customOverlay.getColor();
		chooseColorButton.setBackground(overlayColor);
		chooseColorButton.addActionListener((ActionEvent evt) -> {
			Color chosenColor = JColorChooser.showDialog(null, "Choose an overlay color", overlayColor);
			chosenColor = chosenColor == null ? overlayColor : chosenColor;
			customOverlay.setColor(chosenColor);
			chooseColorButton.setBackground(chosenColor);
		});

		JComboBox overlayShapes = new JComboBox(Shape.values());
		overlayRow.add(overlayShapes);
		Shape shape = customOverlay.getShape();
		overlayShapes.setSelectedItem(shape == null ? Shape.CIRCLE : shape);

		JTextField fileNameField = new JTextField(22);
		fileNameField.setBackground(JColor.LIGHT_GREEN.val);
		TextPrompt fileNamePrompt = new TextPrompt("Select CSV file using \"Select file\" button");
		fileNamePrompt.applyTo(fileNameField);
		File file = customOverlay.getFile();
		fileNameField.setText(file == null ? "" : file.getName());
		fileNameField.setEditable(false);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileTypeFilter(".csv", ".csv files"));

		JButton importFileButton = new JButton("Select file");
		overlayRow.add(importFileButton);
		importFileButton.addActionListener((ActionEvent evt) -> {
			int returnVal = fileChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				customOverlay.setFile(selectedFile);
				fileNameField.setText(selectedFile.getName());
			}
		});

		overlayRow.add(fileNameField);

		JTextField raPositionField = new JTextField(12);
		overlayRow.add(raPositionField);
		raPositionField.setBackground(JColor.LIGHT_GREEN.val);
		TextPrompt raPositionPrompt = new TextPrompt("RA column number");
		raPositionPrompt.applyTo(raPositionField);
		raPositionField.setText(overlayName == null || !tableName.isEmpty() || !tapUrl.isEmpty() ? ""
				: Integer.toString(customOverlay.getRaColumnIndex() + 1));

		JTextField decPositionField = new JTextField(12);
		overlayRow.add(decPositionField);
		decPositionField.setBackground(JColor.LIGHT_GREEN.val);
		TextPrompt decPositionPrompt = new TextPrompt("Dec column number");
		decPositionPrompt.applyTo(decPositionField);
		decPositionField.setText(overlayName == null || !tableName.isEmpty() || !tapUrl.isEmpty() ? ""
				: Integer.toString(customOverlay.getDecColumnIndex() + 1));

		JTextField tableNameField = new JTextField(12);
		overlayRow.add(tableNameField);
		tableNameField.setBackground(JColor.LIGHT_YELLOW.val);
		TextPrompt tableNamePrompt = new TextPrompt("VizieR catalog name");
		tableNamePrompt.applyTo(tableNameField);
		tableNameField.setText(overlayName == null ? "" : customOverlay.getTableName());

		JTextField raColNameField = new JTextField(11);
		overlayRow.add(raColNameField);
		raColNameField.setBackground(JColor.LIGHT_YELLOW.val);
		TextPrompt raColNamePrompt = new TextPrompt("RA column name");
		raColNamePrompt.applyTo(raColNameField);
		raColNameField.setText(overlayName == null ? "" : customOverlay.getRaColName());

		JTextField decColNameField = new JTextField(11);
		overlayRow.add(decColNameField);
		decColNameField.setBackground(JColor.LIGHT_YELLOW.val);
		TextPrompt decColNamePrompt = new TextPrompt("Dec column name");
		decColNamePrompt.applyTo(decColNameField);
		decColNameField.setText(overlayName == null ? "" : customOverlay.getDecColName());

		JTextField tapUrlField = new JTextField(10);
		overlayRow.add(tapUrlField);
		tapUrlField.setBackground(JColor.LIGHT_ORANGE.val);
		TextPrompt tapUrlPrompt = new TextPrompt("TAP access URL");
		tapUrlPrompt.applyTo(tapUrlField);
		tapUrlField.setText(overlayName == null ? "" : customOverlay.getTapUrl());

		JTextField adqlQueryField = new JTextField(20);
		overlayRow.add(adqlQueryField);
		adqlQueryField.setBackground(JColor.LIGHT_ORANGE.val);
		TextPrompt adqlQueryPrompt = new TextPrompt("ADQL query");
		adqlQueryPrompt.applyTo(adqlQueryField);
		adqlQueryField.setText(overlayName == null ? "" : customOverlay.getAdqlQuery());

		JButton saveButton = new JButton("Save");
		overlayRow.add(saveButton);
		Timer saveButtonTimer = new Timer(3000, (ActionEvent e) -> {
			saveButton.setText("Save");
		});
		saveButton.setToolTipText(overlayNameField.getText());
		saveButton.addActionListener((ActionEvent evt) -> {
			int raColumnIndex = 0;
			int decColumnIndex = 0;
			StringBuilder errors = new StringBuilder();
			if (overlayNameField.getText().isEmpty()) {
				errors.append("Overlay name must be specified.").append(LINE_SEP);
			}
			if (customOverlay.getColor() == null) {
				errors.append("Overlay color must be specified.").append(LINE_SEP);
			}
			if (customOverlay.getFile() == null && tableNameField.getText().isEmpty()
					&& tapUrlField.getText().isEmpty()) {
				errors.append("Either a CSV file or a Catalog table name or a TAP access URL must be specified.")
						.append(LINE_SEP);
			}
			if (tableNameField.getText().isEmpty() && tapUrlField.getText().isEmpty()) {
				if (raPositionField.getText().isEmpty()) {
					errors.append("RA position must be specified.").append(LINE_SEP);
				} else {
					try {
						raColumnIndex = toInteger(raPositionField.getText()) - 1;
						if (raColumnIndex < 0) {
							errors.append("RA position must be greater than 0.").append(LINE_SEP);
						}
					} catch (Exception ex) {
						errors.append("Invalid RA position!").append(LINE_SEP);
					}
				}
				if (decPositionField.getText().isEmpty()) {
					errors.append("Dec position must be specified.").append(LINE_SEP);
				} else {
					try {
						decColumnIndex = toInteger(decPositionField.getText()) - 1;
						if (decColumnIndex < 0) {
							errors.append("Dec position must be greater than 0.").append(LINE_SEP);
						}
					} catch (Exception ex) {
						errors.append("Invalid dec position!").append(LINE_SEP);
					}
				}
			} else {
				if (raColNameField.getText().isEmpty()) {
					errors.append("RA column name must be specified.").append(LINE_SEP);
				}
				if (decColNameField.getText().isEmpty()) {
					errors.append("Dec column name must be specified.").append(LINE_SEP);
				}
				if (!tapUrlField.getText().isEmpty()) {
					String adqlQuery = adqlQueryField.getText();
					if (adqlQuery.isEmpty()) {
						errors.append("ADQL query must be specified.").append(LINE_SEP);
					} else {

						boolean hasRa = adqlQuery.contains(":ra:");
						boolean hasDec = adqlQuery.contains(":dec:");
						boolean hasRadius = adqlQuery.contains(":radius:");
						if (!hasRa || !hasDec || !hasRadius) {
							errors.append(
									"Your ADQL must contain the following 3 keywords, colons included! :ra:, :dec:, :radius: (e.g. CIRCLE('ICRS', :ra:, :dec:, :radius:)).")
									.append(LINE_SEP);
						}
					}
				}
			}
			if (errors.length() > 0) {
				showErrorDialog(baseFrame, errors.toString());
				return;
			}
			String name = overlayNameField.getText();
			customOverlay.setName(name);
			customOverlay.setShape((Shape) overlayShapes.getSelectedItem());
			customOverlay.setRaColumnIndex(raColumnIndex);
			customOverlay.setDecColumnIndex(decColumnIndex);
			customOverlay.setTableName(tableNameField.getText().trim());
			customOverlay.setRaColName(raColNameField.getText().trim());
			customOverlay.setDecColName(decColNameField.getText().trim());
			customOverlay.setTapUrl(tapUrlField.getText().trim());
			customOverlay.setAdqlQuery(adqlQueryField.getText().trim());
			fireCustomOverlaysListener();
			customOverlays.put(name, customOverlay);
			overlayNameField.setEditable(false);
			saveOverlayDefinitions();

			saveButton.setText(html("<b>Saved</b>"));
			saveButtonTimer.restart();
		});

		JButton deleteButton = new JButton("Delete");
		overlayRow.add(deleteButton);
		Timer deleteButtonTimer = new Timer(3000, (ActionEvent e) -> {
			deleteButton.setText("Delete");
		});
		deleteButton.setToolTipText(overlayNameField.getText());
		deleteButton.addActionListener((ActionEvent evt) -> {
			String name = customOverlay.getName();
			if (name == null || !showConfirmDialog(baseFrame, "Do you really want to delete overlay " + name + "?")) {
				return;
			}
			fireCustomOverlaysListener();
			customOverlays.remove(name);
			saveOverlayDefinitions();
			overlayNameField.setText("");
			overlayNameField.setEditable(true);
			chooseColorButton.setBackground(null);
			overlayShapes.setSelectedItem(Shape.CIRCLE);
			raPositionField.setText("");
			decPositionField.setText("");
			fileNameField.setText("");
			tableNameField.setText("");
			raColNameField.setText("");
			decColNameField.setText("");
			tapUrlField.setText("");
			adqlQueryField.setText("");
			customOverlay.init();
			overlayPanel.updateUI();

			deleteButton.setText(html("<b>Deleted</b>"));
			deleteButtonTimer.restart();
		});
	}

	private void saveOverlayDefinitions() {
		StringBuilder data = new StringBuilder();
		customOverlays.values().forEach(customOverlay -> {
			data.append(customOverlay.serialize()).append(LINE_SEP);
		});
		try (FileWriter writer = new FileWriter(OVERLAYS_PATH)) {
			writer.write(data.toString());

		} catch (IOException ex) {
			showExceptionDialog(baseFrame, ex);
		}
	}

	private void fireCustomOverlaysListener() {
		JCheckBox useCustomOverlays = imageViewerTab.getUseCustomOverlays();
		if (useCustomOverlays.isSelected()) {
			useCustomOverlays.setSelected(false);
			useCustomOverlays.getActionListeners()[0].actionPerformed(null);
		}
	}

}
