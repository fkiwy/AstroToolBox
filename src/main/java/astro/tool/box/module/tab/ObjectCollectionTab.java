package astro.tool.box.module.tab;

import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.enumeration.JColor;
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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class ObjectCollectionTab {

    public static final String TAB_NAME = "Object Collection";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;
    private final ImageViewerTab imageViewerTab;
    private final TableRowSorter<TableModel> objectCollectionSorter;

    private JPanel centerPanel;
    private JTable resultTable;
    private JTextField searchField;
    private JCheckBox copyCoords;

    private File file;

    public ObjectCollectionTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab, ImageViewerTab imageViewerTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        this.imageViewerTab = imageViewerTab;
        objectCollectionSorter = new TableRowSorter<>();
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new BorderLayout());

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

            topPanel.add(new JLabel("Columns to add:"));

            JTextField addColumnsField = new JTextField(15);
            topPanel.add(addColumnsField);

            JLabel topPanelMessage = createLabel("", JColor.DARKER_GREEN);
            JLabel bottomPanelMessage = createLabel("", JColor.DARKER_GREEN);
            Timer timer = new Timer(3000, (ActionEvent e) -> {
                topPanelMessage.setText("");
                bottomPanelMessage.setText("");
            });

            JButton reloadButton = new JButton("Reload file");
            topPanel.add(reloadButton);
            reloadButton.addActionListener((ActionEvent evt) -> {
                if (file == null) {
                    showErrorDialog(baseFrame, "Object collection does not exist yet!");
                    return;
                }
                String confirmMessage = "Any unsaved changes will be lost!" + LINE_SEP
                        + "Do you really want to reload file " + file.getName() + "?";
                if (!showConfirmDialog(baseFrame, confirmMessage)) {
                    return;
                }
                removeAndRecreateCenterPanel(mainPanel);
                readFileContents(addColumnsField.getText());
                addColumnsField.setText("");
                topPanelMessage.setText("File has been reloaded!");
                timer.restart();
                baseFrame.setVisible(true);
            });

            JButton saveButton = new JButton("Save file");
            topPanel.add(saveButton);
            saveButton.addActionListener((ActionEvent evt) -> {
                if (file == null) {
                    showErrorDialog(baseFrame, "Object collection does not exist yet!");
                    return;
                }
                boolean hasFileBeenSaved = saveFile();
                if (hasFileBeenSaved) {
                    topPanelMessage.setText("File has been saved!");
                    timer.restart();
                }
            });

            topPanel.add(topPanelMessage);

            JButton addButton = new JButton("Add row");
            bottomPanel.add(addButton);
            addButton.addActionListener((ActionEvent evt) -> {
                if (file == null) {
                    showErrorDialog(baseFrame, "Object collection does not exist yet!");
                    return;
                }
                searchField.setText("");
                DefaultTableModel tableModel = (DefaultTableModel) resultTable.getModel();
                tableModel.addRow((Object[]) null);
                bottomPanelMessage.setText("Row has been added!");
                timer.restart();
            });

            JButton deleteButton = new JButton("Delete selected row");
            bottomPanel.add(deleteButton);
            deleteButton.addActionListener((ActionEvent evt) -> {
                if (file == null) {
                    showErrorDialog(baseFrame, "Object collection does not exist yet!");
                    return;
                }
                if (resultTable.getSelectedRow() == -1) {
                    showErrorDialog(baseFrame, "No row selected yet!");
                    return;
                }
                int selectedRow = resultTable.getSelectedRow();
                String confirmMessage = "Do you really want to delete row # " + resultTable.getValueAt(selectedRow, 0);
                if (!showConfirmDialog(baseFrame, confirmMessage)) {
                    return;
                }
                DefaultTableModel tableModel = (DefaultTableModel) resultTable.getModel();
                int rowToDelete = resultTable.convertRowIndexToModel(selectedRow);
                tableModel.removeRow(rowToDelete);
                boolean hasFileBeenSaved = saveFile();
                if (hasFileBeenSaved) {
                    bottomPanelMessage.setText("Row has been deleted!");
                    timer.restart();
                }
            });

            bottomPanel.add(bottomPanelMessage);

            bottomPanel.add(new JLabel("Search:"));
            searchField = new JTextField(30);
            bottomPanel.add(searchField);
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    objectCollectionSorter.setRowFilter(getCustomRowFilter(searchField.getText()));
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    objectCollectionSorter.setRowFilter(getCustomRowFilter(searchField.getText()));
                }
            });

            copyCoords = new JCheckBox("Copy selected coordinates to the Image Viewer or Catalog Search tab");
            bottomPanel.add(copyCoords);

            tabbedPane.addChangeListener((ChangeEvent evt) -> {
                JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    if (file == null) {
                        String objectCollectionPath = getUserSetting(OBJECT_COLLECTION_PATH);
                        if (objectCollectionPath == null || objectCollectionPath.isEmpty()) {
                            return;
                        }
                        File objectCollectionFile = new File(objectCollectionPath);
                        if (!objectCollectionFile.exists()) {
                            return;
                        }
                        file = objectCollectionFile;
                    } else {
                        saveFile();
                    }
                    removeAndRecreateCenterPanel(mainPanel);
                    readFileContents(addColumnsField.getText());
                    baseFrame.setVisible(true);
                }
            });

            baseFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evt) {
                    saveFile();
                }
            });

            tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private boolean saveFile() {
        if (file != null) {
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
                return true;
            } catch (IOException ex) {
                showExceptionDialog(baseFrame, ex);
            }
        }
        return false;
    }

    private void appendCellValue(StringBuilder fileContent, int columnIndex, int columnCount, String cellValue) {
        if (cellValue != null) {
            fileContent.append(cellValue);
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
                String[] columnValues = scanner.nextLine().split(SPLIT_CHAR, columnNames.length);
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
        addComparatorsToTableSorter(objectCollectionSorter, defaultTableModel, rows);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setRowSorter(objectCollectionSorter);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (copyCoords.isSelected()) {
                int selectedRow = resultTable.getSelectedRow();
                if (!e.getValueIsAdjusting() && selectedRow > -1 && selectedRow < resultTable.getRowCount()) {
                    String ra = (String) resultTable.getValueAt(selectedRow, 4);
                    String dec = (String) resultTable.getValueAt(selectedRow, 5);
                    String coords = ra + " " + dec;
                    imageViewerTab.getCoordsField().setText(coords);
                    catalogQueryTab.getCoordsField().setText(coords);
                    catalogQueryTab.getSearchLabel().setText("");
                    catalogQueryTab.removeAndRecreateCenterPanel();
                    catalogQueryTab.removeAndRecreateBottomPanel();
                }
            }
        });
        resizeColumnWidth(resultTable);

        imageViewerTab.setCollectionTable(resultTable);
        catalogQueryTab.setCollectionTable(resultTable);

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
