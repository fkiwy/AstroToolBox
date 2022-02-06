package astro.tool.box.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.CustomOverlay;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.Shape;
import astro.tool.box.main.TextPrompt;
import astro.tool.box.util.FileTypeFilter;
import java.awt.BorderLayout;
import java.awt.Color;
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

public class CustomOverlaysTab {

    private static final String TAB_NAME = "Custom Overlays";
    private static final String OVERLAYS_FILE_NAME = "/AstroToolBoxOverlays.txt";
    private static final String OVERLAYS_PATH = USER_HOME + OVERLAYS_FILE_NAME;

    private final Map<String, CustomOverlay> customOverlays = new LinkedHashMap<>();

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final ImageViewerTab imageViewerTab;

    public CustomOverlaysTab(JFrame baseFrame, JTabbedPane tabbedPane, ImageViewerTab imageViewerTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.imageViewerTab = imageViewerTab;
    }

    public void init() {
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
            tabbedPane.addTab(TAB_NAME, container);

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            container.add(topPanel, BorderLayout.PAGE_START);
            JLabel topRowLabel = new JLabel(html("You can either use a local CSV file <span style='background:#CCFFCC'>(green fields)</span> or a VizieR catalog <span style='background:#FFFFCC'>(yellow fields)</span> to create an overlay:"));
            topPanel.add(topRowLabel);

            JButton addButton = new JButton("Create new overlay");
            topPanel.add(addButton);

            int overlayCount = overlays.size();

            JPanel overlayPanel = new JPanel(new GridLayout(overlayCount + 30, 1));
            container.add(new JScrollPane(overlayPanel), BorderLayout.CENTER);

            addButton.addActionListener((ActionEvent evt) -> {
                addOverlayRow(overlayPanel, new CustomOverlay());
                baseFrame.setVisible(true);
            });

            for (int i = 0; i < overlayCount; i++) {
                addOverlayRow(overlayPanel, overlays.get(i));
            }
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void addOverlayRow(JPanel overlayPanel, CustomOverlay customOverlay) {
        JPanel overlayRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        overlayPanel.add(overlayRow);

        String overlayName = customOverlay.getName();
        String tableName = customOverlay.getTableName();

        JTextField overlayNameField = new JTextField(20);
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

        JTextField fileNameField = new JTextField(25);
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

        JTextField raPositionField = new JTextField(10);
        overlayRow.add(raPositionField);
        raPositionField.setBackground(JColor.LIGHT_GREEN.val);
        TextPrompt raPositionPrompt = new TextPrompt("RA column #");
        raPositionPrompt.applyTo(raPositionField);
        raPositionField.setText(overlayName == null || !tableName.isEmpty() ? "" : Integer.toString(customOverlay.getRaColumnIndex() + 1));

        JTextField decPositionField = new JTextField(10);
        overlayRow.add(decPositionField);
        decPositionField.setBackground(JColor.LIGHT_GREEN.val);
        TextPrompt decPositionPrompt = new TextPrompt("Dec column #");
        decPositionPrompt.applyTo(decPositionField);
        decPositionField.setText(overlayName == null || !tableName.isEmpty() ? "" : Integer.toString(customOverlay.getDecColumnIndex() + 1));

        JTextField tableNameField = new JTextField(15);
        overlayRow.add(tableNameField);
        tableNameField.setBackground(JColor.LIGHT_YELLOW.val);
        TextPrompt tableNamePrompt = new TextPrompt("Catalog table name");
        tableNamePrompt.applyTo(tableNameField);
        tableNameField.setText(overlayName == null ? "" : customOverlay.getTableName());

        JTextField raColNameField = new JTextField(15);
        overlayRow.add(raColNameField);
        raColNameField.setBackground(JColor.LIGHT_YELLOW.val);
        TextPrompt raColNamePrompt = new TextPrompt("RA column name");
        raColNamePrompt.applyTo(raColNameField);
        raColNameField.setText(overlayName == null ? "" : customOverlay.getRaColName());

        JTextField decColNameField = new JTextField(15);
        overlayRow.add(decColNameField);
        decColNameField.setBackground(JColor.LIGHT_YELLOW.val);
        TextPrompt decColNamePrompt = new TextPrompt("Dec column name");
        decColNamePrompt.applyTo(decColNameField);
        decColNameField.setText(overlayName == null ? "" : customOverlay.getDecColName());

        JLabel message = createMessageLabel();
        Timer timer = new Timer(3000, (ActionEvent e) -> {
            message.setText("");
        });

        JButton saveOverlayButton = new JButton("Save");
        overlayRow.add(saveOverlayButton);
        saveOverlayButton.addActionListener((ActionEvent evt) -> {
            int raColumnIndex = 0;
            int decColumnIndex = 0;
            StringBuilder errors = new StringBuilder();
            if (overlayNameField.getText().isEmpty()) {
                errors.append("Overlay name must not be empty.").append(LINE_SEP);
            }
            if (customOverlay.getColor() == null) {
                errors.append("Overlay color must be specified.").append(LINE_SEP);
            }
            if (customOverlay.getFile() == null && tableNameField.getText().isEmpty()) {
                errors.append("CSV file or VizieR catalog must be specified.").append(LINE_SEP);
            }
            if (customOverlay.getFile() != null && !tableNameField.getText().isEmpty()) {
                errors.append("Only either a CSV file or a VizieR catalog may be specified.").append(LINE_SEP);
            }
            if (tableNameField.getText().isEmpty()) {
                if (raPositionField.getText().isEmpty()) {
                    errors.append("RA position must not be empty.").append(LINE_SEP);
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
                    errors.append("Dec position must not be empty.").append(LINE_SEP);
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
                    errors.append("RA column name must not be empty.").append(LINE_SEP);
                }
                if (decColNameField.getText().isEmpty()) {
                    errors.append("Dec column name must not be empty.").append(LINE_SEP);
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
            fireCustomOverlaysListener();
            customOverlays.put(name, customOverlay);
            overlayNameField.setEditable(false);
            saveOverlayDefinitions();

            message.setText("Saved!");
            timer.restart();
        });

        JButton deleteOverlayButton = new JButton("Delete");
        overlayRow.add(deleteOverlayButton);
        deleteOverlayButton.addActionListener((ActionEvent evt) -> {
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
            customOverlay.init();
            overlayPanel.updateUI();

            message.setText("Deleted!");
            timer.restart();
        });

        overlayRow.add(message);
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
