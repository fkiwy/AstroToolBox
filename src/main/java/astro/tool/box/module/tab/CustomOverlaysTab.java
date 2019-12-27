package astro.tool.box.module.tab;

import astro.tool.box.enumeration.JColor;
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

            int overlayCount = 10;
            JPanel table = new JPanel(new GridLayout(overlayCount + 1, 1));

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
                overlayNameField.setText(customOverlay.getName());

                JTextField colorField = new JTextField(2);
                row.add(colorField);
                colorField.setBackground(customOverlay.getColor());

                JButton chooseColorButton = new JButton("Overlay color");
                row.add(chooseColorButton);
                chooseColorButton.addActionListener((ActionEvent evt) -> {
                    Color chosenColor = JColorChooser.showDialog(null, "Choose an overlay color", null);
                    customOverlay.setColor(chosenColor);
                    colorField.setBackground(chosenColor);
                });

                row.add(new JLabel("RA position:"));

                JTextField raColumnPosition = new JTextField(2);
                row.add(raColumnPosition);
                raColumnPosition.setText(Integer.toString(customOverlay.getRaColumnIndex()));

                row.add(new JLabel("dec position:"));

                JTextField decColumnPosition = new JTextField(2);
                row.add(decColumnPosition);
                decColumnPosition.setText(Integer.toString(customOverlay.getDecColumnIndex()));

                JTextField overlayFileName = new JTextField(30);
                overlayFileName.setEditable(false);
                File file = customOverlay.getFile();
                overlayFileName.setText(file == null ? "" : file.getName());

                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileTypeFilter(".csv", ".csv files"));

                JButton importFileButton = new JButton("Specify file location");
                row.add(importFileButton);
                importFileButton.addActionListener((ActionEvent evt) -> {
                    int returnVal = fileChooser.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
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
                        String overlayName = overlayNameField.getText();
                        File selectedFile = fileChooser.getSelectedFile();

                        customOverlay.setName(overlayName);
                        customOverlay.setRaColumnIndex(raColumnIndex);
                        customOverlay.setDecColumnIndex(decColumnIndex);
                        customOverlay.setFile(selectedFile);
                        customOverlays.put(overlayName, customOverlay);

                        overlayNameField.setEditable(false);
                        overlayFileName.setText(selectedFile.getName());
                    }
                });

                row.add(new JLabel("Overlay file:"));
                row.add(overlayFileName);

                JButton removeOverlayButton = new JButton("Remove");
                row.add(removeOverlayButton);
                removeOverlayButton.addActionListener((ActionEvent evt) -> {
                    customOverlays.remove(customOverlay.getName());
                    table.remove(row);
                    table.updateUI();
                });
            }

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            table.add(row);

            String saveMessage = "Overlay definitions have been saved!";
            JLabel message = createLabel("", PLAIN_FONT, JColor.DARKER_GREEN.val);
            Timer timer = new Timer(3000, (ActionEvent e) -> {
                message.setText("");
            });

            JButton removeOverlayButton = new JButton("Save overlay definitions");
            row.add(removeOverlayButton);
            removeOverlayButton.addActionListener((ActionEvent evt) -> {
                StringBuilder data = new StringBuilder();
                customOverlays.values().forEach(customOverlay -> {
                    data.append(customOverlay.serialize()).append(LINE_SEP);
                });
                try (FileWriter writer = new FileWriter(OVERLAYS_PATH)) {
                    writer.write(data.toString());
                    message.setText(saveMessage);
                    timer.restart();
                } catch (IOException ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            row.add(message);

            tabbedPane.addTab(TAB_NAME, new JScrollPane(table));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    public Map<String, CustomOverlay> getCustomOverlays() {
        return customOverlays;
    }

}
