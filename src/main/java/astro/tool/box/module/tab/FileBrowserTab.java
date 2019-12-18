package astro.tool.box.module.tab;

import astro.tool.box.enumeration.JColor;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.util.FileTypeFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class FileBrowserTab {

    public static final String TAB_NAME = "File Browser";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;
    private final ImageViewerTab imageViewerTab;

    private JPanel centerPanel;
    private JTable resultTable;
    private JTextField raColumnPosition;
    private JTextField decColumnPosition;

    private File file;

    private int raColumnIndex;
    private int decColumnIndex;

    public FileBrowserTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab, ImageViewerTab imageViewerTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        this.imageViewerTab = imageViewerTab;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new BorderLayout());

            JPanel topPanel = new JPanel(new GridLayout(1, 1));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(filePanel);

            filePanel.add(new JLabel("RA position:"));

            raColumnPosition = createField("", PLAIN_FONT, 2);
            filePanel.add(raColumnPosition);

            filePanel.add(new JLabel("dec position:"));

            decColumnPosition = createField("", PLAIN_FONT, 2);
            filePanel.add(decColumnPosition);

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileTypeFilter(".csv", ".csv files"));

            JButton importButton = new JButton("Import csv file with header");
            filePanel.add(importButton);
            importButton.addActionListener((ActionEvent evt) -> {
                int returnVal = fileChooser.showOpenDialog(filePanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    if (raColumnPosition.getText().isEmpty() || decColumnPosition.getText().isEmpty()) {
                        raColumnIndex = 0;
                        decColumnIndex = 0;
                    } else {
                        StringBuilder errors = new StringBuilder();
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
                    }
                    file = fileChooser.getSelectedFile();
                    removeAndRecreateCenterPanel(mainPanel);
                    readFileContents("");
                    baseFrame.setVisible(true);
                }
            });

            filePanel.add(new JLabel("Columns to add:"));

            JTextField addColumnsField = createField("", PLAIN_FONT, 15);
            filePanel.add(addColumnsField);

            JLabel message = createLabel("", PLAIN_FONT, JColor.DARKER_GREEN.val);
            Timer timer = new Timer(3000, (ActionEvent e) -> {
                message.setText("");
            });

            JButton reloadButton = new JButton("Reload file");
            filePanel.add(reloadButton);
            reloadButton.addActionListener((ActionEvent evt) -> {
                if (file == null) {
                    showErrorDialog(baseFrame, "No file imported yet!");
                    return;
                }
                if (!showConfirmDialog(baseFrame, "Confirm reload action for file " + file.getName())) {
                    return;
                }
                removeAndRecreateCenterPanel(mainPanel);
                readFileContents(addColumnsField.getText());
                addColumnsField.setText("");
                message.setText("File has been reloaded!");
                timer.restart();
                baseFrame.setVisible(true);
            });

            JButton saveButton = new JButton("Save file");
            filePanel.add(saveButton);
            saveButton.addActionListener((ActionEvent evt) -> {
                if (file == null) {
                    showErrorDialog(baseFrame, "No file imported yet!");
                    return;
                }
                if (!showConfirmDialog(baseFrame, "Confirm save action for file " + file.getName())) {
                    return;
                }
                StringBuilder fileContent = new StringBuilder();
                TableModel model = resultTable.getModel();
                int columnCount = model.getColumnCount();
                for (int i = 1; i < columnCount; i++) {
                    String columnName = model.getColumnName(i);
                    appendCellValue(fileContent, i, columnCount, columnName);
                }
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int y = 1; y < columnCount; y++) {
                        String cellValue = (String) model.getValueAt(i, y);
                        appendCellValue(fileContent, y, columnCount, cellValue);
                    }
                }
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(fileContent.toString());
                    message.setText("File has been saved!");
                    timer.restart();
                } catch (IOException ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            filePanel.add(message);

            baseFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evt) {
                    if (file != null) {
                        String message = "File " + file.getName() + " is still open in the File Browser."
                                + LINE_SEP + "It may contain unsaved changes!"
                                + LINE_SEP + "Do you really want to close the program?";
                        if (showConfirmDialog(baseFrame, message)) {
                            System.exit(0);
                        } else {
                            baseFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        }
                    }
                }
            });

            tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void appendCellValue(StringBuilder fileContent, int columnIndex, int columnCount, String cellValue) {
        if (cellValue != null) {
            fileContent.append(cellValue.trim());
        }
        if (columnIndex < columnCount - 1) {
            fileContent.append(",");
        } else {
            fileContent.append(LINE_SEP);
        }
    }

    private void readFileContents(String columnsToAdd) {
        try (Scanner scanner = new Scanner(file)) {
            String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);

            StringBuilder errors = new StringBuilder();
            int numberOfColumns = columnNames.length;
            int lastColumnIndex = numberOfColumns - 1;
            if (raColumnIndex > lastColumnIndex) {
                errors.append("RA position must not be greater than ").append(lastColumnIndex).append(".").append(LINE_SEP);
            }
            if (decColumnIndex > lastColumnIndex) {
                errors.append("Dec position must not be greater than ").append(lastColumnIndex).append(".").append(LINE_SEP);
            }
            if (errors.length() > 0) {
                showErrorDialog(baseFrame, errors.toString());
                return;
            }

            String[] newNames = columnsToAdd.split(SPLIT_CHAR);
            int columnCount = newNames.length;
            if (columnCount == 1 && newNames[0].isEmpty()) {
                columnCount = 0;
            }

            String valuesToAppend = "";
            for (int i = 1; i < columnCount; i++) {
                valuesToAppend += ",";
            }
            String[] newValues = valuesToAppend.split(SPLIT_CHAR, columnCount);

            int rowNumber = 0;
            List<String[]> rows = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String[] columnValues = scanner.nextLine().split(SPLIT_CHAR, numberOfColumns);
                /*
                if (raColumnIndex > 0 || decColumnIndex > 0) {
                    String raValue = columnValues[raColumnIndex];
                    String decValue = columnValues[decColumnIndex];
                    if (!isNumeric(raValue) || !isNumeric(decValue)) {
                        showErrorDialog(baseFrame, "RA and dec must be numeric!");
                        return;
                    }
                }*/
                String[] values = concatArrays(new String[]{String.valueOf(++rowNumber)}, columnValues);
                if (columnCount > 0) {
                    values = concatArrays(values, newValues);
                }
                rows.add(values);
            }

            String[] names = concatArrays(new String[]{"row#"}, columnNames);
            if (columnCount > 0) {
                names = concatArrays(names, newNames);
            }

            displayQueryResults(names, rows);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void displayQueryResults(String[] columnNames, List<String[]> rows) {
        Object[][] data = new Object[][]{};
        DefaultTableModel defaultTableModel = new DefaultTableModel(rows.toArray(data), columnNames);
        resultTable = new JTable(defaultTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        alignResultColumns(resultTable, rows);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setRowSorter(createResultTableSorter(defaultTableModel, rows));
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (raColumnIndex > 0 || decColumnIndex > 0) {
            resultTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = resultTable.getSelectedRow();
                    String ra = (String) resultTable.getValueAt(selectedRow, raColumnIndex + 1);
                    String dec = (String) resultTable.getValueAt(selectedRow, decColumnIndex + 1);
                    String coords = ra + " " + dec;
                    imageViewerTab.getCoordsField().setText(coords);
                    catalogQueryTab.getCoordsField().setText(coords);
                    catalogQueryTab.getSearchLabel().setText("");
                    catalogQueryTab.removeAndRecreateCenterPanel();
                    catalogQueryTab.removeAndRecreateBottomPanel();
                }
            });
        }
        resizeColumnWidth(resultTable);

        JScrollPane resultScrollPanel = new JScrollPane(resultTable);
        resultScrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), file.getName(), TitledBorder.LEFT, TitledBorder.TOP
        ));
        centerPanel.add(resultScrollPanel);
    }

    private void removeAndRecreateCenterPanel(JPanel mainPanel) {
        if (centerPanel != null) {
            mainPanel.remove(centerPanel);
        }
        centerPanel = new JPanel(new GridLayout(1, 1));
        mainPanel.add(centerPanel);
    }

}
