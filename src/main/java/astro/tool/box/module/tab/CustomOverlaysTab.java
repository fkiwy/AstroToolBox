package astro.tool.box.module.tab;

import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.Shape;
import static astro.tool.box.function.NumericFunctions.toInteger;
import astro.tool.box.module.CustomOverlay;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.LINE_SEP;
import astro.tool.box.util.FileTypeFilter;
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
import javax.swing.UIManager;

public class CustomOverlaysTab {

    private static final String TAB_NAME = "Custom Overlays";
    private static final String OVERLAYS_FILE_NAME = "/AstroToolBoxOverlays.txt";
    private static final String OVERLAYS_PATH = USER_HOME + OVERLAYS_FILE_NAME;

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    private final Map<String, CustomOverlay> customOverlays;

    public CustomOverlaysTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        customOverlays = new LinkedHashMap<>();
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

            List<CustomOverlay> overlays = new ArrayList<>();
            customOverlays.values().forEach(overlay -> {
                overlays.add(overlay);
            });

            int overlayCount = 20;
            JPanel table = new JPanel(new GridLayout(overlayCount, 1));

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

                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileTypeFilter(".csv", ".csv files"));

                JButton importFileButton = new JButton("Select file");
                row.add(importFileButton);
                importFileButton.addActionListener((ActionEvent evt) -> {
                    int returnVal = fileChooser.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        customOverlay.setFile(selectedFile);
                        overlayFileName.setText(selectedFile.getName());
                    }
                });

                row.add(overlayFileName);

                JLabel message = createLabel("", DEFAULT_FONT, JColor.DARKER_GREEN.val);
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
                        errors.append("Overlay name must be specified.").append(LINE_SEP);
                    }
                    if (customOverlay.getColor() == null) {
                        errors.append("Overlay color must be specified.").append(LINE_SEP);
                    }
                    if (raColumnPosition.getText().isEmpty()) {
                        errors.append("RA position must be specified.").append(LINE_SEP);
                    }
                    if (raColumnPosition.getText().isEmpty() || decColumnPosition.getText().isEmpty()) {
                        errors.append("Dec position must be specified.").append(LINE_SEP);
                    }
                    if (customOverlay.getFile() == null) {
                        errors.append("Overlay file must be specified.").append(LINE_SEP);
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
                    customOverlays.put(name, customOverlay);
                    overlayNameField.setEditable(false);
                    saveOverlayDefinitions();

                    message.setText("Saved!");
                    timer.restart();
                });

                JButton removeOverlayButton = new JButton("Delete");
                row.add(removeOverlayButton);
                removeOverlayButton.addActionListener((ActionEvent evt) -> {
                    String name = customOverlay.getName();
                    if (name == null || !showConfirmDialog(baseFrame, "Confirm delete action for overlay " + name)) {
                        return;
                    }
                    customOverlays.remove(name);
                    saveOverlayDefinitions();
                    overlayNameField.setText("");
                    overlayNameField.setEditable(true);
                    colorField.setBackground(null);
                    overlayShapes.setSelectedItem(Shape.CIRCLE);
                    raColumnPosition.setText("");
                    decColumnPosition.setText("");
                    overlayFileName.setText("");
                    customOverlay.init();
                    table.updateUI();

                    message.setText("Deleted!");
                    timer.restart();
                });

                row.add(message);
            }

            tabbedPane.addTab(TAB_NAME, new JScrollPane(table));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
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

    public Map<String, CustomOverlay> getCustomOverlays() {
        return customOverlays;
    }

}