package astro.tool.box.module.tab;

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
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class CustomOverlaysTab {

    private static final String TAB_NAME = "Custom Overlays";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    private final Map<String, CustomOverlay> customOverlays;

    public CustomOverlaysTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        customOverlays = new HashMap<>();
    }

    public void init() {
        try {
            int overlayCount = 10;
            JPanel table = new JPanel(new GridLayout(overlayCount, 1));

            for (int i = 0; i < overlayCount; i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                table.add(row);

                CustomOverlay customOverlay = new CustomOverlay();

                row.add(new JLabel("Overlay name:"));

                JTextField overlayNameField = new JTextField(20);
                row.add(overlayNameField);

                JButton chooseColorButton = new JButton("Overlay color");
                row.add(chooseColorButton);
                chooseColorButton.addActionListener((ActionEvent evt) -> {
                    Color chosenColor = JColorChooser.showDialog(null, "Choose an overlay color", null);
                    customOverlay.setColor(chosenColor);
                });

                row.add(new JLabel("RA position:"));

                JTextField raColumnPosition = new JTextField(2);
                row.add(raColumnPosition);

                row.add(new JLabel("dec position:"));

                JTextField decColumnPosition = new JTextField(2);
                row.add(decColumnPosition);

                JTextField overlayFileName = new JTextField(30);
                overlayFileName.setEditable(false);

                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileTypeFilter(".csv", ".csv files"));

                JButton importFileButton = new JButton("Import overlay csv file");
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
                        File file = fileChooser.getSelectedFile();
                        overlayFileName.setText(file.getName());
                        customOverlay.setFile(file);
                        customOverlay.setRaColumnIndex(raColumnIndex);
                        customOverlay.setDecColumnIndex(decColumnIndex);
                        customOverlay.setName(overlayNameField.getText());
                        overlayNameField.setEditable(false);
                        customOverlays.put(customOverlay.getName(), customOverlay);
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

            tabbedPane.addTab(TAB_NAME, new JScrollPane(table));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    public Map<String, CustomOverlay> getCustomOverlays() {
        return customOverlays;
    }

}
