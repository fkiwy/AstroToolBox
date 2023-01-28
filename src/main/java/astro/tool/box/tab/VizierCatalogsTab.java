package astro.tool.box.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceHelper.*;
import astro.tool.box.container.NumberPair;
import static astro.tool.box.util.ExternalResources.getVizierUrl;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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

    private static final String TAB_NAME = "VizieR Catalogs";
    private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    private JPanel mainPanel;
    private JPanel firstRow;
    private JPanel secondRow;
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
    private boolean allColumns;

    private double prevTargetRa;
    private double prevTargetDec;
    private double prevSearchRadius;
    private int prevNumberOfRows;
    private boolean prevAllColumns;

    private int position;
    private int matchesFound;

    private boolean firstTable = true;
    private boolean titleAdded = false;

    public VizierCatalogsTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
    }

    public void init() {
        try {
            mainPanel = new JPanel(new BorderLayout());
            tabbedPane.addTab(TAB_NAME, mainPanel);

            JPanel layout = new JPanel(new GridLayout(2, 1));
            mainPanel.add(layout, BorderLayout.PAGE_START);

            firstRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            layout.add(firstRow);

            secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            layout.add(secondRow);

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
            firstRow.add(coordsLabel);

            coordsField = new JTextField(25);
            firstRow.add(coordsField);

            JLabel radiusLabel = new JLabel("Search radius (arcsec):");
            firstRow.add(radiusLabel);

            radiusField = new JTextField(5);
            firstRow.add(radiusField);
            radiusField.setText("5");

            JLabel rowsLabel = new JLabel("Number of rows per table:");
            firstRow.add(rowsLabel);

            rowsField = new JTextField(5);
            firstRow.add(rowsField);
            rowsField.setText("50");

            JCheckBox allColumnsCheckBox = new JCheckBox("Include all columns");

            JPanel vizierLinkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            searchButton = new JButton("Search");
            firstRow.add(searchButton);
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
                    allColumns = allColumnsCheckBox.isSelected();
                    if (targetRa == prevTargetRa && targetDec == prevTargetDec && searchRadius == prevSearchRadius && numberOfRows == prevNumberOfRows && allColumns == prevAllColumns) {
                        return;
                    }
                    position = 0;
                    matchesFound = 0;
                    firstTable = true;
                    titleAdded = false;
                    catalogArea.setText(null);
                    prevTargetRa = targetRa;
                    prevTargetDec = targetDec;
                    prevSearchRadius = searchRadius;
                    prevNumberOfRows = numberOfRows;
                    prevAllColumns = allColumns;
                    vizierLinkPanel.removeAll();
                    if (!errorMessages.isEmpty()) {
                        String message = String.join(LINE_SEP, errorMessages);
                        showErrorDialog(baseFrame, message);
                    } else {
                        CompletableFuture.supplyAsync(() -> {
                            try {
                                setWaitCursor();
                                String outAll = allColumnsCheckBox.isSelected() ? "&-out.all" : "";
                                String url = "http://vizier.u-strasbg.fr/viz-bin/asu-txt?-c=%s%s&-c.rs=%f&-out.max=%d&-sort=_r&-out.meta=hu&-oc.form=d&-out.add=_r&-out.form=mini%s";
                                url = String.format(url, Double.toString(targetRa), addPlusSign(targetDec), searchRadius, numberOfRows, outAll);
                                HttpURLConnection connection = establishHttpConnection(url);
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                                    reader.lines().forEach(line -> {
                                        line = line.replaceAll("\\s+$", "");
                                        if (line.startsWith("#RESOURCE")) {
                                            titleAdded = false;
                                        }
                                        if (line.startsWith("#Title:") && !titleAdded) {
                                            titleAdded = true;
                                            if (firstTable) {
                                                firstTable = false;
                                            } else {
                                                catalogArea.append(LINE_SEP_TEXT_AREA + LINE_SEP_TEXT_AREA);
                                            }
                                            String title = line.replace("#Title: ", "");
                                            String headerDeco = createHeaderDeco(title);
                                            catalogArea.append(headerDeco + LINE_SEP_TEXT_AREA);
                                            catalogArea.append("===== " + title + " =====" + LINE_SEP_TEXT_AREA);
                                            catalogArea.append(headerDeco + LINE_SEP_TEXT_AREA);
                                        } else if (!line.startsWith("#") && !line.isEmpty()) {
                                            catalogArea.append(line + LINE_SEP_TEXT_AREA);
                                        }
                                    });
                                }
                                catalogArea.append(LINE_SEP_TEXT_AREA);
                                catalogArea.append("##### END #####");

                                JLabel vizierLink = createHyperlink("Open in web browser", getVizierUrl(targetRa, targetDec, searchRadius, numberOfRows, allColumnsCheckBox.isSelected()));
                                vizierLinkPanel.add(vizierLink);

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

            firstRow.add(allColumnsCheckBox);

            JLabel findLabel = new JLabel("Find in search results:");
            secondRow.add(findLabel);

            findField = new JTextField(10);
            secondRow.add(findField);

            findButton = new JButton("Find");
            secondRow.add(findButton);
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

            secondRow.add(vizierLinkPanel);
            coordsField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
            radiusField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
            rowsField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
            findField.addActionListener((ActionEvent evt) -> {
                showWarnDialog(baseFrame, "Use the Find button, please!");
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

    private String createHeaderDeco(String title) {
        int lengthOfTitle = title.length();
        String headerDeco = "";
        for (int i = 0; i < lengthOfTitle + 12; i++) {
            headerDeco += "=";
        }
        return headerDeco;
    }

}
