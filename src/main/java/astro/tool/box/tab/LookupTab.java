package astro.tool.box.tab;

import static astro.tool.box.function.NumericFunctions.isInteger;
import static astro.tool.box.function.NumericFunctions.isNumeric;
import static astro.tool.box.main.ToolboxHelper.resizeColumnWidth;
import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;
import static astro.tool.box.util.Constants.MAMAJEK_VERSION;
import static astro.tool.box.util.Constants.SPLIT_CHAR;

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
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import astro.tool.box.component.FixedTable;
import astro.tool.box.enumeration.LookupTable;

public class LookupTab implements Tab {

	public static final String TAB_NAME = "Photometric Relations";

	private final JFrame baseFrame;
	private final JTabbedPane tabbedPane;

	private JPanel centerPanel;
	private JTable resultTable;

	public LookupTab(JFrame baseFrame, JTabbedPane tabbedPane) {
		this.baseFrame = baseFrame;
		this.tabbedPane = tabbedPane;
	}

	@Override
	public void init(boolean visible) {
		try {
			JPanel mainPanel = new JPanel(new BorderLayout());

			JPanel topPanel = new JPanel(new GridLayout(1, 1));
			mainPanel.add(topPanel, BorderLayout.PAGE_START);

			JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			topPanel.add(filePanel);

			filePanel.add(new JLabel("Relations table:"));

			JComboBox lookupTables = new JComboBox(LookupTable.values());
			filePanel.add(lookupTables);
			lookupTables.addActionListener((ActionEvent evt) -> {
				String fileName = null;
				String tableName = null;
				switch ((LookupTable) lookupTables.getSelectedItem()) {
				case MAIN_SEQUENCE -> {
					fileName = "/SpectralTypeLookupTable.csv";
					tableName = "Main sequence stars - A Modern Mean Dwarf Stellar Color & Effective Temperature Sequence (Eric Mamajek, version %s)"
							.formatted(MAMAJEK_VERSION);
				}
				case MLT_DWARFS -> {
					fileName = "/BrownDwarfLookupTable.csv";
					tableName = "M, L & T dwarfs - Skrzypek et al. (2015), Skrzypek et al. (2016), Deacon et al. (2016), Best et al. (2018), Carnero Rosell et al. (2019) and Kiman et al. (2019)";
				}
				case WHITE_DWARFS -> {
					fileName = "/WhiteDwarfLookupTable.csv";
					tableName = "White dwarfs - Evolutionary cooling sequences from http://www.astro.umontreal.ca/~bergeron/CoolingModels (Bédard et al., 2020)";
				}
				}
				removeAndRecreateCenterPanel(mainPanel);
				InputStream input = getClass().getResourceAsStream(fileName);
				readFileContents(input, tableName);
				baseFrame.setVisible(true);
			});
			lookupTables.setSelectedItem(LookupTable.MAIN_SEQUENCE);

			if (visible) {
				tabbedPane.addTab(TAB_NAME, mainPanel);
			}
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		}
	}

	private void readFileContents(InputStream input, String tableName) {
		try (Scanner scanner = new Scanner(input)) {
			String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
			List<String[]> rows = new ArrayList<>();
			while (scanner.hasNextLine()) {
				String[] columnValues = scanner.nextLine().split(",", -1);
				rows.add(columnValues);
			}
			displayQueryResults(columnNames, rows, tableName);
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		}
	}

	private void displayQueryResults(String[] columnNames, List<String[]> rows, String tableName) {
		Object[][] data = new Object[][] {};
		DefaultTableModel defaultTableModel = new DefaultTableModel(rows.toArray(data), columnNames);
		resultTable = new JTable(defaultTableModel);
		alignResultColumns(resultTable, rows);
		resultTable.setAutoCreateRowSorter(true);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		resultTable.setRowSorter(createResultTableSorter(defaultTableModel, rows));
		resizeColumnWidth(resultTable);

		JScrollPane resultScrollPanel = new JScrollPane(resultTable);
		resultScrollPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), tableName,
				TitledBorder.LEFT, TitledBorder.TOP));

		FixedTable fixedTable = new FixedTable(1, resultScrollPanel);
		resultScrollPanel.getRowHeader().addChangeListener(fixedTable);

		centerPanel.add(resultScrollPanel);
	}

	private void alignResultColumns(JTable table, List<String[]> rows) {
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
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
				 * Comparator comparator; String columnValue = row[i]; if
				 * (!columnValue.isEmpty()) { if (isInteger(columnValue)) { comparator =
				 * getLongComparator(); } else if (isNumeric(columnValue)) { comparator =
				 * getDoubleComparator(); } else { comparator = getStringComparator(); }
				 * sorter.setComparator(i, comparator); }
				 */
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
