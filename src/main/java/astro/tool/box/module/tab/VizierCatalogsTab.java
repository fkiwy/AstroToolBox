package astro.tool.box.module.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import astro.tool.box.container.NumberPair;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

public class VizierCatalogsTab {

    private static final String TAB_NAME = "VizieR catalogs";
    private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JButton searchButton;
    private JButton findButton;
    private JTextField coordsField;
    private JTextField radiusField;
    private JTextField rowsField;
    private JTextField findField;
    private JTextArea catalogArea;

    private double targetRa;
    private double targetDec;
    private double searchRadius;
    private int numberOfRows;

    private int position;
    private int matchesFound;

    public VizierCatalogsTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
    }

    public void init() {
        try {
            mainPanel = new JPanel(new BorderLayout());
            tabbedPane.addTab(TAB_NAME, mainPanel);

            topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            catalogArea = new JTextArea();
            catalogArea.setBorder(new EmptyBorder(5, 5, 5, 5));
            catalogArea.setFont(MONO_FONT);
            catalogArea.setEditable(true);
            DefaultCaret caret = (DefaultCaret) catalogArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            JScrollPane scrollPanel = new JScrollPane(catalogArea);
            scrollPanel.setBorder(createEtchedBorder("Search results"));
            centerPanel.add(scrollPanel);

            JLabel coordsLabel = new JLabel("Coordinates:");
            topPanel.add(coordsLabel);

            coordsField = new JTextField(25);
            topPanel.add(coordsField);

            JLabel radiusLabel = new JLabel("Search radius (arcsec):");
            topPanel.add(radiusLabel);

            radiusField = new JTextField(5);
            topPanel.add(radiusField);
            radiusField.setText("5");

            JLabel rowsLabel = new JLabel("Number of rows per table:");
            topPanel.add(rowsLabel);

            rowsField = new JTextField(5);
            topPanel.add(rowsField);
            rowsField.setText("50");

            JCheckBox allColumns = new JCheckBox("Include all columns");

            searchButton = new JButton("Search");
            topPanel.add(searchButton);
            searchButton.addActionListener((ActionEvent e) -> {
                try {
                    baseFrame.setVisible(true);
                    String coords = coordsField.getText();
                    if (coords.isEmpty()) {
                        showErrorDialog(baseFrame, "Coordinates must not be empty!");
                        return;
                    }
                    String radius = radiusField.getText();
                    if (radius.isEmpty()) {
                        showErrorDialog(baseFrame, "Search radius must not be empty!");
                        return;
                    }
                    String rows = rowsField.getText();
                    if (rows.isEmpty()) {
                        showErrorDialog(baseFrame, "Number of rows must not be empty!");
                        return;
                    }
                    position = 0;
                    matchesFound = 0;
                    catalogArea.setText(null);
                    List<String> errorMessages = new ArrayList<>();
                    try {
                        NumberPair coordinates = getCoordinates(coords);
                        targetRa = coordinates.getX();
                        targetDec = coordinates.getY();
                        if (targetRa < 0) {
                            errorMessages.add("RA must not be smaller than 0 deg.");
                        }
                        if (targetRa > 360) {
                            errorMessages.add("RA must not be greater than 360 deg.");
                        }
                        if (targetDec < -90) {
                            errorMessages.add("Dec must not be smaller than -90 deg.");
                        }
                        if (targetDec > 90) {
                            errorMessages.add("Dec must not be greater than 90 deg.");
                        }
                    } catch (Exception ex) {
                        targetRa = 0;
                        targetDec = 0;
                        errorMessages.add("Invalid coordinates!");
                    }
                    try {
                        searchRadius = Double.valueOf(radius);
                        if (searchRadius > 300) {
                            errorMessages.add("Radius must not be larger than 300 arcsec.");
                        }
                    } catch (Exception ex) {
                        searchRadius = 0;
                        errorMessages.add("Invalid radius!");
                    }
                    try {
                        numberOfRows = Integer.valueOf(rows);
                        if (numberOfRows > 500) {
                            errorMessages.add("Number of rows must not be greater than 500.");
                        } else if (numberOfRows < 1) {
                            errorMessages.add("Number of rows must not be less than 1.");
                        }
                    } catch (Exception ex) {
                        numberOfRows = 0;
                        errorMessages.add("Invalid number of rows!");
                    }
                    if (!errorMessages.isEmpty()) {
                        String message = String.join(LINE_SEP, errorMessages);
                        showErrorDialog(baseFrame, message);
                    } else {
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                setWaitCursor();

                                String outAll = allColumns.isSelected() ? "&-out.all" : "";
                                String url = "http://vizier.u-strasbg.fr/viz-bin/asu-txt?-c=%s%s&-c.rs=%f&-out.max=%d&-sort=_r&-out.meta=hu&-oc.form=d&-out.form=mini%s";
                                url = String.format(url, Double.toString(targetRa), addPlusSign(targetDec), searchRadius, numberOfRows, outAll);

                                HttpURLConnection connection = establishHttpConnection(url);
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                    reader.lines().forEach(line -> {
                                        if (line.startsWith("#Title:")) {
                                            line = line.replace("#Title:", "==========>");
                                        }
                                        if (!line.startsWith("#")) {
                                            catalogArea.append(line + LINE_SEP_TEXT_AREA);
                                        }
                                    });
                                    catalogArea.append("==========> END");
                                }

                                baseFrame.setVisible(true);
                            } catch (IOException ex) {
                                showExceptionDialog(baseFrame, ex);
                            } finally {
                                setDefaultCursor();
                            }
                            return null;
                        });
                    }
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            topPanel.add(allColumns);

            JLabel findLabel = new JLabel("-  Find in search results:");
            topPanel.add(findLabel);

            findField = new JTextField(10);
            topPanel.add(findField);

            findButton = new JButton("Find");
            topPanel.add(findButton);
            findButton.addActionListener((ActionEvent e) -> {
                String stringToFind = findField.getText().toLowerCase();
                if (stringToFind != null && stringToFind.length() > 0) {
                    Document document = catalogArea.getDocument();
                    int findLength = stringToFind.length();
                    try {
                        boolean found = false;
                        if (position + findLength > document.getLength()) {
                            position = 0;
                        }
                        while (position + findLength <= document.getLength()) {
                            String match = document.getText(position, findLength).toLowerCase();
                            if (match.equals(stringToFind)) {
                                found = true;
                                break;
                            }
                            position++;
                        }
                        if (found) {
                            matchesFound++;
                            catalogArea.requestFocusInWindow();
                            Rectangle viewRect = catalogArea.modelToView(position + findLength);
                            catalogArea.scrollRectToVisible(viewRect);
                            catalogArea.setCaretPosition(position + findLength);
                            catalogArea.moveCaretPosition(position);
                            position += findLength;
                        } else {
                            String message;
                            if (matchesFound == 0) {
                                message = "No match found for";
                            } else {
                                message = "No more matches found for";
                                matchesFound = 0;
                            }
                            showInfoDialog(baseFrame, message + " '" + stringToFind + "'");
                            position = 0;
                        }
                    } catch (BadLocationException ex) {
                        showErrorDialog(baseFrame, ex.getMessage());
                    }
                }
            });

            coordsField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
            radiusField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
            findField.addActionListener((ActionEvent evt) -> {
                showInfoDialog(baseFrame, "Use the Find button, please!");
            });
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void setWaitCursor() {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        radiusField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        findField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        catalogArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void setDefaultCursor() {
        baseFrame.setCursor(Cursor.getDefaultCursor());
        coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        radiusField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        findField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        catalogArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

}
