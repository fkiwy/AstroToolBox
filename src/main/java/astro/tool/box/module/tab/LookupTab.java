package astro.tool.box.module.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.enumeration.LookupTable;
import astro.tool.box.module.FixedTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class LookupTab {

    public static final String TAB_NAME = "Lookup Tables";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    private JPanel centerPanel;
    private JTable resultTable;

    public LookupTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new BorderLayout());

            JPanel topPanel = new JPanel(new GridLayout(1, 1));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(filePanel);

            filePanel.add(new JLabel("Lookup table:"));

            JComboBox lookupTables = new JComboBox<>(LookupTable.values());
            filePanel.add(lookupTables);
            lookupTables.addActionListener((ActionEvent evt) -> {
                String fileName;
                String tableName;
                switch ((LookupTable) lookupTables.getSelectedItem()) {
                    case MAIN_SEQUENCE:
                        fileName = "/SpectralTypeLookupTable.csv";
                        tableName = "Main sequence lookup table (A Modern Mean Dwarf Stellar Color & Effective Temperature Sequence by Eric Mamajek)";
                        break;
                    case MLTY_DWARFS:
                        fileName = "/BrownDwarfLookupTable.csv";
                        tableName = "M, L, T & Y dwarfs lookup table";
                        break;
                    case WHITE_DWARFS_PURE_H:
                        fileName = "/WhiteDwarfPureHLookupTable.csv";
                        tableName = "White dwarfs pure H lookup table (Gaia photometry for white dwarfs by J. M. Carrasco)";
                        break;
                    case WHITE_DWARFS_PURE_HE:
                        fileName = "/WhiteDwarfPureHeLookupTable.csv";
                        tableName = "White dwarfs pure He lookup table (Gaia photometry for white dwarfs by J. M. Carrasco)";
                        break;
                    case WHITE_DWARFS_DA:
                        fileName = "/WhiteDwarfDALookupTable.csv";
                        tableName = "White dwarfs DA lookup table (Synthetic Colors and Evolutionary Sequences of Hydrogen- and Helium-Atmosphere White Dwarfs by Pierre Bergeron)";
                        break;
                    case WHITE_DWARFS_DB:
                        fileName = "/WhiteDwarfDBLookupTable.csv";
                        tableName = "White dwarfs DB lookup table (Synthetic Colors and Evolutionary Sequences of Hydrogen- and Helium-Atmosphere White Dwarfs by Pierre Bergeron)";
                        break;
                    default:
                        fileName = "";
                        tableName = "";
                        break;
                }
                removeAndRecreateCenterPanel(mainPanel);
                InputStream input = getClass().getResourceAsStream(fileName);
                readFileContents(input, tableName);
                baseFrame.setVisible(true);
            });
            lookupTables.setSelectedItem(LookupTable.MAIN_SEQUENCE);

            tabbedPane.addTab(TAB_NAME, mainPanel);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void readFileContents(InputStream input, String tableName) {
        try (Scanner scanner = new Scanner(input)) {
            String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
            int numberOfColumns = columnNames.length;
            List<String[]> rows = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String[] columnValues = scanner.nextLine().split(SPLIT_CHAR, numberOfColumns);
                rows.add(columnValues);
            }
            displayQueryResults(columnNames, rows, tableName);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void displayQueryResults(String[] columnNames, List<String[]> rows, String tableName) {
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
        resizeColumnWidth(resultTable);

        JScrollPane resultScrollPanel = new JScrollPane(resultTable);
        resultScrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), tableName, TitledBorder.LEFT, TitledBorder.TOP
        ));

        FixedTable fixedTable = new FixedTable(1, resultScrollPanel);
        resultScrollPanel.getRowHeader().addChangeListener(fixedTable);

        centerPanel.add(resultScrollPanel);
    }

    private void alignResultColumns(JTable table, List<String[]> rows) {
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rows.forEach((row) -> {
            for (int i = 0; i < row.length; i++) {
                DefaultTableCellRenderer cellRenderer;
                String columnValue = row[i];
                if (!columnValue.isEmpty()) {
                    if (isInteger(columnValue) || isNumeric(columnValue)) {
                        cellRenderer = rightRenderer;
                    } else {
                        cellRenderer = leftRenderer;
                    }
                    table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
                }
            }
        });
    }

    private TableRowSorter createResultTableSorter(DefaultTableModel defaultTableModel, List<String[]> rows) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(defaultTableModel);
        rows.forEach((row) -> {
            for (int i = 0; i < row.length; i++) {
                /*
                Comparator comparator;
                String columnValue = row[i];
                if (!columnValue.isEmpty()) {
                    if (isInteger(columnValue)) {
                        comparator = getLongComparator();
                    } else if (isNumeric(columnValue)) {
                        comparator = getDoubleComparator();
                    } else {
                        comparator = getStringComparator();
                    }
                    sorter.setComparator(i, comparator);
                }*/
                sorter.setSortable(i, false);
            }
        });
        return sorter;
    }

    private void removeAndRecreateCenterPanel(JPanel mainPanel) {
        if (centerPanel != null) {
            mainPanel.remove(centerPanel);
        }
        centerPanel = new JPanel(new GridLayout(1, 1));
        mainPanel.add(centerPanel);
    }

}
