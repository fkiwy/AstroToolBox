package astro.tool.box.module.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.CustomOverlay;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.Shape;
import astro.tool.box.module.TextPrompt;
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

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final ImageViewerTab imageViewerTab;

    public static final Map<String, CustomOverlay> CUSTOM_OVERLAYS = new LinkedHashMap<>();

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
                    CUSTOM_OVERLAYS.put(customOverlay.getName(), customOverlay);
                }
            } catch (Exception ex) {
                if (ex instanceof FileNotFoundException) {
                } else {
                    showExceptionDialog(baseFrame, ex);
                    return;
                }
            }

            List<CustomOverlay> overlays = new ArrayList<>();
            CUSTOM_OVERLAYS.values().forEach(overlay -> {
                overlays.add(overlay);
            });

            JPanel container = new JPanel(new BorderLayout());
            tabbedPane.addTab(TAB_NAME, container);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            container.add(topRow, BorderLayout.PAGE_START);
            JLabel topRowLabel = new JLabel("You can either use a local CSV file (dark gray button) or a VizieR catalog (yellow fields) to produce an overlay:");
            topRowLabel.setForeground(JColor.DARK_RED.val);
            topRow.add(topRowLabel);

            int overlayCount = 50;
            JPanel table = new JPanel(new GridLayout(overlayCount, 1));
            container.add(new JScrollPane(table), BorderLayout.CENTER);

            for (int i = 0; i < overlayCount; i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                table.add(row);

                CustomOverlay customOverlay;
                if (i < overlays.size()) {
                    customOverlay = overlays.get(i);
                } else {
                    customOverlay = new CustomOverlay();
                }

                row.add(new JLabel("Overlay name:"));

                JTextField overlayNameField = new JTextField(20);
                row.add(overlayNameField);
                String overlayName = customOverlay.getName();
                overlayNameField.setText(overlayName);
                overlayNameField.setEditable((overlayName == null));

                JTextField colorField = new JTextField(1);
                Color color = customOverlay.getColor();
                colorField.setBackground(color);
                colorField.setEditable(false);

                JButton chooseColorButton = new JButton("Choose color");
                row.add(chooseColorButton);
                chooseColorButton.addActionListener((ActionEvent evt) -> {
                    Color chosenColor = JColorChooser.showDialog(null, "Choose an overlay color", color);
                    chosenColor = chosenColor == null ? color : chosenColor;
                    customOverlay.setColor(chosenColor);
                    colorField.setBackground(chosenColor);
                });

                row.add(colorField);

                row.add(new JLabel("Shape:"));

                JComboBox<Shape> overlayShapes = new JComboBox<>(Shape.values());
                row.add(overlayShapes);
                Shape shape = customOverlay.getShape();
                overlayShapes.setSelectedItem(shape == null ? Shape.CIRCLE : shape);

                row.add(new JLabel("RA position:"));

                JTextField raColumnPosition = new JTextField(2);
                row.add(raColumnPosition);
                raColumnPosition.setText(overlayName == null ? "" : Integer.toString(customOverlay.getRaColumnIndex() + 1));

                row.add(new JLabel("dec position:"));

                JTextField decColumnPosition = new JTextField(2);
                row.add(decColumnPosition);
                decColumnPosition.setText(overlayName == null ? "" : Integer.toString(customOverlay.getDecColumnIndex() + 1));

                JTextField overlayFileName = new JTextField(30);
                File file = customOverlay.getFile();
                overlayFileName.setText(file == null ? "" : file.getName());
                overlayFileName.setEditable(false);

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileTypeFilter(".csv", ".csv files"));

                JButton importFileButton = new JButton("Select file");
                row.add(importFileButton);
                importFileButton.setBorderPainted(false);
                importFileButton.setBackground(JColor.GRAY.val);
                importFileButton.setForeground(JColor.WHITE.val);
                importFileButton.addActionListener((ActionEvent evt) -> {
                    int returnVal = fileChooser.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        customOverlay.setFile(selectedFile);
                        overlayFileName.setText(selectedFile.getName());
                    }
                });

                row.add(overlayFileName);

                JTextField tableName = new JTextField(15);
                row.add(tableName);
                tableName.setBackground(JColor.LIGHT_YELLOW.val);
                TextPrompt tableNamePrompt = new TextPrompt("Catalog table name");
                tableNamePrompt.applyTo(tableName);
                tableName.setText(overlayName == null ? "" : customOverlay.getTableName());

                JTextField raColName = new JTextField(15);
                row.add(raColName);
                raColName.setBackground(JColor.LIGHT_YELLOW.val);
                TextPrompt raColNamePrompt = new TextPrompt("RA column name");
                raColNamePrompt.applyTo(raColName);
                raColName.setText(overlayName == null ? "" : customOverlay.getRaColName());

                JTextField decColName = new JTextField(15);
                row.add(decColName);
                decColName.setBackground(JColor.LIGHT_YELLOW.val);
                TextPrompt decColNamePrompt = new TextPrompt("Dec column name");
                decColNamePrompt.applyTo(decColName);
                decColName.setText(overlayName == null ? "" : customOverlay.getDecColName());

                JLabel message = createLabel("", JColor.DARKER_GREEN);
                Timer timer = new Timer(3000, (ActionEvent e) -> {
                    message.setText("");
                });

                JButton saveOverlayButton = new JButton("Save");
                row.add(saveOverlayButton);
                saveOverlayButton.addActionListener((ActionEvent evt) -> {
                    int raColumnIndex = 0;
                    int decColumnIndex = 0;
                    StringBuilder errors = new StringBuilder();
                    if (overlayNameField.getText().isEmpty()) {
                        errors.append("Overlay name must not be empty.").append(LINE_SEP);
                    }
                    if (customOverlay.getColor() == null) {
                        errors.append("Overlay color must not be empty.").append(LINE_SEP);
                    }
                    if (raColumnPosition.getText().isEmpty()) {
                        errors.append("RA position must not be empty.").append(LINE_SEP);
                    }
                    if (raColumnPosition.getText().isEmpty() || decColumnPosition.getText().isEmpty()) {
                        errors.append("Dec position must not be empty.").append(LINE_SEP);
                    }
                    if (customOverlay.getFile() == null && tableName.getText().isEmpty()) {
                        errors.append("Either a CSV file or a VizieR catalog must be specified.").append(LINE_SEP);
                    }
                    if (!tableName.getText().isEmpty()) {
                        if (raColName.getText().isEmpty()) {
                            errors.append("RA column name must not be empty.").append(LINE_SEP);
                        }
                        if (decColName.getText().isEmpty()) {
                            errors.append("Dec column name must not be empty.").append(LINE_SEP);
                        }
                    }
                    if (errors.length() > 0) {
                        showErrorDialog(baseFrame, errors.toString());
                        return;
                    }
                    try {
                        raColumnIndex = toInteger(raColumnPosition.getText()) - 1;
                        if (raColumnIndex < 0) {
                            errors.append("RA position must be greater than 0.").append(LINE_SEP);
                        }
                    } catch (Exception ex) {
                        errors.append("Invalid RA position!").append(LINE_SEP);
                    }
                    try {
                        decColumnIndex = toInteger(decColumnPosition.getText()) - 1;
                        if (decColumnIndex < 0) {
                            errors.append("Dec position must be greater than 0.").append(LINE_SEP);
                        }
                    } catch (Exception ex) {
                        errors.append("Invalid dec position!").append(LINE_SEP);
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
                    customOverlay.setTableName(tableName.getText().trim());
                    customOverlay.setRaColName(raColName.getText().trim());
                    customOverlay.setDecColName(decColName.getText().trim());
                    fireCustomOverlaysListener();
                    CUSTOM_OVERLAYS.put(name, customOverlay);
                    overlayNameField.setEditable(false);
                    saveOverlayDefinitions();

                    message.setText("Saved!");
                    timer.restart();
                });

                JButton removeOverlayButton = new JButton("Delete");
                row.add(removeOverlayButton);
                removeOverlayButton.addActionListener((ActionEvent evt) -> {
                    String name = customOverlay.getName();
                    if (name == null || !showConfirmDialog(baseFrame, "Do you really want to delete overlay " + name + "?")) {
                        return;
                    }
                    fireCustomOverlaysListener();
                    CUSTOM_OVERLAYS.remove(name);
                    saveOverlayDefinitions();
                    overlayNameField.setText("");
                    overlayNameField.setEditable(true);
                    colorField.setBackground(null);
                    overlayShapes.setSelectedItem(Shape.CIRCLE);
                    raColumnPosition.setText("");
                    decColumnPosition.setText("");
                    overlayFileName.setText("");
                    tableName.setText("");
                    raColName.setText("");
                    decColName.setText("");
                    customOverlay.init();
                    table.updateUI();

                    message.setText("Deleted!");
                    timer.restart();
                });

                row.add(message);
            }
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void saveOverlayDefinitions() {
        StringBuilder data = new StringBuilder();
        CUSTOM_OVERLAYS.values().forEach(customOverlay -> {
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
        useCustomOverlays.setSelected(false);
        useCustomOverlays.getActionListeners()[0].actionPerformed(null);
    }

}
