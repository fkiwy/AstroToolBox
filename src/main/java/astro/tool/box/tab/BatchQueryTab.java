package astro.tool.box.tab;

import static astro.tool.box.function.NumericFunctions.isNumeric;
import static astro.tool.box.function.NumericFunctions.roundTo2DecNZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.function.NumericFunctions.toInteger;
import static astro.tool.box.function.PhotometricFunctions.isAPossibleAGN;
import static astro.tool.box.function.PhotometricFunctions.isAPossibleWD;
import static astro.tool.box.main.ToolboxHelper.AGN_WARNING;
import static astro.tool.box.main.ToolboxHelper.WD_WARNING;
import static astro.tool.box.main.ToolboxHelper.alignResultColumns;
import static astro.tool.box.main.ToolboxHelper.createResultTableSorter;
import static astro.tool.box.main.ToolboxHelper.getCatalogInstances;
import static astro.tool.box.main.ToolboxHelper.lookupSpectralTypes;
import static astro.tool.box.main.ToolboxHelper.resizeColumnWidth;
import static astro.tool.box.main.ToolboxHelper.showErrorDialog;
import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;
import static astro.tool.box.util.Constants.LINE_SEP;
import static astro.tool.box.util.Constants.SPLIT_CHAR;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.SimbadCatalogEntry;
import astro.tool.box.catalog.WhiteDwarf;
import astro.tool.box.container.BatchResult;
import astro.tool.box.enumeration.AsynchResult;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.LookupTable;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.lookup.SpectralTypeLookupEntry;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.SpectralTypeLookupService;
import astro.tool.box.util.FileTypeFilter;

public class BatchQueryTab implements Tab {

    public static final String TAB_NAME = "Batch Search";
    private static final int MAX_SEARCH_RADIUS = 10;
    private static final int MAX_INPUT_ROWS = 50000;

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;
    private final ImageViewerTab imageViewerTab;

    private JPanel bottomRow;
    private JPanel centerPanel;
    private JTextField echoField;
    private JCheckBox includeColors;
    private JComboBox lookupTables;
    private JProgressBar progressBar;
    private JButton cancelButton;

    private File file;
    private List<String> selectedCatalogs;
    private List<BatchResult> batchResults;

    private final CatalogQueryService catalogQueryService;
    private SpectralTypeLookupService spectralTypeLookupService;

    private final Map<String, CatalogEntry> catalogInstances;

    private boolean isProcessing;
    private boolean toCancel;

    private int raColumnIndex;
    private int decColumnIndex;
    private double searchRadius;

    public BatchQueryTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab, ImageViewerTab imageViewerTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        this.imageViewerTab = imageViewerTab;
        catalogInstances = getCatalogInstances();
        catalogQueryService = new CatalogQueryService();
    }

    @Override
    public void init(boolean visible) {
        try {
            JPanel mainPanel = new JPanel(new BorderLayout());

            JPanel topPanel = new JPanel(new GridLayout(4, 1));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(topRow);

            JPanel centerRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(centerRow);

            bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(bottomRow);

            JPanel echoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(echoPanel);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileTypeFilter(".csv", ".csv files"));

            JTextField fileNameField = new JTextField(50);
            fileNameField.setEditable(false);

            JButton importButton = new JButton("Import csv file with header");
            topRow.add(importButton);
            importButton.addActionListener((ActionEvent evt) -> {
                int returnVal = fileChooser.showOpenDialog(topRow);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    fileNameField.setText(file.getName());
                }
            });

            topRow.add(new JLabel("Imported file:"));
            topRow.add(fileNameField);

            centerRow.add(new JLabel("RA position:"));

            JTextField raColumnPosition = new JTextField(2);
            centerRow.add(raColumnPosition);

            centerRow.add(new JLabel("dec position:"));

            JTextField decColumnPosition = new JTextField(2);
            centerRow.add(decColumnPosition);

            centerRow.add(new JLabel("Search radius:"));

            JTextField radiusField = new JTextField("5", 3);
            centerRow.add(radiusField);

            centerRow.add(new JLabel("Include colors:"));

            includeColors = new JCheckBox();
            centerRow.add(includeColors);

            centerRow.add(new JLabel("Relations table:"));

            lookupTables = new JComboBox(new LookupTable[]{LookupTable.MAIN_SEQUENCE, LookupTable.MLT_DWARFS});
            centerRow.add(lookupTables);

            JButton queryButton = new JButton("Start query");
            centerRow.add(queryButton);
            queryButton.addActionListener((ActionEvent evt) -> {
                if (isProcessing) {
                    showErrorDialog(baseFrame, "There's still a query being processed!");
                    return;
                }

                if (file == null) {
                    showErrorDialog(baseFrame, "No file selected!");
                    return;
                }

                if (!file.getName().endsWith(".csv")) {
                    showErrorDialog(baseFrame, "The selected file is not a .csv file!");
                    return;
                }

                int rows = 0;
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNextLine()) {
                        scanner.nextLine();
                        rows++;
                    }
                } catch (FileNotFoundException ex) {
                }
                if (rows > MAX_INPUT_ROWS) {
                    showErrorDialog(baseFrame, "The selected file contains " + rows + " rows! Max. allowed: " + MAX_INPUT_ROWS);
                    return;
                }

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

                try {
                    searchRadius = toDouble(radiusField.getText());
                    if (searchRadius > MAX_SEARCH_RADIUS) {
                        errors.append("Radius must not be larger than ").append(MAX_SEARCH_RADIUS).append(" arcsec.").append(LINE_SEP);
                    }
                } catch (Exception ex) {
                    errors.append("Invalid radius!").append(LINE_SEP);
                }

                if (errors.length() > 0) {
                    showErrorDialog(baseFrame, errors.toString());
                    return;
                }

                selectedCatalogs = new ArrayList<>();
                for (Component component : bottomRow.getComponents()) {
                    if (component instanceof JCheckBox catalogBox) {
                        if (catalogBox.isSelected()) {
                            selectedCatalogs.add(catalogBox.getText());
                        }
                    }
                }
                if (selectedCatalogs.isEmpty()) {
                    showErrorDialog(baseFrame, "No catalog selected!");
                    return;
                }

                batchResults = null;
                if (centerPanel != null) {
                    mainPanel.remove(centerPanel);
                }
                centerPanel = new JPanel(new GridLayout(1, 1));
                mainPanel.add(centerPanel);

                if (progressBar != null) {
                    centerRow.remove(progressBar);
                }
                progressBar = new JProgressBar(0, rows);
                centerRow.add(progressBar);
                progressBar.setStringPainted(true);

                if (cancelButton != null) {
                    centerRow.remove(cancelButton);
                }
                cancelButton = new JButton("Cancel query");
                centerRow.add(cancelButton);
                cancelButton.addActionListener((ActionEvent e) -> {
                    toCancel = true;
                });

                baseFrame.setVisible(true);

                CompletableFuture.supplyAsync(() -> queryCatalogs());
            });

            bottomRow.add(new JLabel("Catalogs:"));

            JCheckBox catalog;
            for (String catalogKey : catalogInstances.keySet()) {
                catalog = new JCheckBox(catalogKey);
                catalog.setSelected(true);
                bottomRow.add(catalog);
            }

            echoPanel.add(new JLabel("Status:"));

            echoField = new JTextField(90);
            echoPanel.add(echoField);
            echoField.setEditable(false);

            JButton exportButton = new JButton("Export results");
            echoPanel.add(exportButton);
            exportButton.addActionListener((ActionEvent evt) -> {
                if (batchResults == null || batchResults.isEmpty()) {
                    showErrorDialog(baseFrame, "Nothing to export yet!");
                } else {
                    String content = batchResults.stream().map(result -> result.getValues()).collect(Collectors.joining(LINE_SEP));
                    content = batchResults.get(0).getTitles() + LINE_SEP + content;
                    try {
                        File tmpFile = File.createTempFile("AstroToolBox_", ".csv");
                        try (FileWriter writer = new FileWriter(tmpFile)) {
                            writer.write(content);
                        }
                        Desktop.getDesktop().open(tmpFile);
                    } catch (IOException ex) {
                        showExceptionDialog(baseFrame, ex);
                    }

                }
            });

            if (visible) {
                tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));
            }
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private Future<AsynchResult> queryCatalogs() {
        toCancel = false;
        isProcessing = true;
        echoField.setText("Query is running ...");
        echoField.setBackground(JColor.LIGHT_BLUE.val);
        CompletableFuture<AsynchResult> future = new CompletableFuture();
        batchResults = new ArrayList<>();
        int rowNumber = 1;
        int objectNumber = 1;

        LookupTable selectedTable = (LookupTable) lookupTables.getSelectedItem();
		switch (selectedTable) {
		case MAIN_SEQUENCE -> {
			try (InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv")) {
				Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines();
				List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
					return new SpectralTypeLookupEntry(line.split(",", -1));
				}).collect(Collectors.toList());
				spectralTypeLookupService = new SpectralTypeLookupService(entries);
			} catch (IOException e) {
				showExceptionDialog(baseFrame, e);
				throw new RuntimeException(e);
			}
		}
		case MLT_DWARFS -> {
			try (InputStream input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv")) {
				Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines();
				List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
					return new BrownDwarfLookupEntry(line.split(",", -1));
				}).collect(Collectors.toList());
				spectralTypeLookupService = new SpectralTypeLookupService(entries);
			} catch (IOException e) {
				showExceptionDialog(baseFrame, e);
				throw new RuntimeException(e);
			}
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + selectedTable);
		}

        try (Scanner scanner = new Scanner(file)) {
            String[] columns = scanner.nextLine().split(SPLIT_CHAR);
            StringBuilder errors = new StringBuilder();
            int numberOfColumns = columns.length;
            int lastColumnIndex = numberOfColumns - 1;
            if (raColumnIndex > lastColumnIndex) {
                errors.append("RA position must not be greater than ").append(lastColumnIndex).append(".").append(LINE_SEP);
            }
            if (decColumnIndex > lastColumnIndex) {
                errors.append("Dec position must not be greater than ").append(lastColumnIndex).append(".").append(LINE_SEP);
            }
            if (errors.length() > 0) {
                handleError(future, errors.toString());
                return future;
            }
            while (scanner.hasNextLine()) {
                if (toCancel) {
                    echoField.setText("Query cancelled!");
                    echoField.setBackground(JColor.LIGHT_YELLOW.val);
                    future.complete(AsynchResult.CANCELLED);
                    isProcessing = false;
                    return future;
                }
                columns = scanner.nextLine().split(",", -1);
                String raValue = columns[raColumnIndex];
                String decValue = columns[decColumnIndex];
                if (!isNumeric(raValue) || !isNumeric(decValue)) {
                    handleError(future, "RA and dec must be numeric!");
                    return future;
                }
                double targetRa = toDouble(raValue);
                double targetDec = toDouble(decValue);
                int resultCount = 0;
                Iterator<String> iter = selectedCatalogs.listIterator();
                while (iter.hasNext()) {
                    CatalogEntry catalogEntry = catalogInstances.get(iter.next());
                    catalogEntry.setRa(targetRa);
                    catalogEntry.setDec(targetDec);
                    catalogEntry.setSearchRadius(searchRadius);
                    catalogEntry = performQuery(catalogEntry);
                    if (catalogEntry == null) {
                        continue;
                    }
                    List<String> spectralTypes = lookupSpectralTypes(catalogEntry.getColors(true), spectralTypeLookupService, includeColors.isSelected());
                    if (catalogEntry instanceof SimbadCatalogEntry simbadEntry) {
                        StringBuilder simbadType = new StringBuilder();
                        simbadType.append(simbadEntry.getObjectType());
                        if (!simbadEntry.getSpectralType().isEmpty()) {
                            simbadType.append(" ").append(simbadEntry.getSpectralType());
                        }
                        simbadType.append("; ");
                        spectralTypes.add(0, simbadType.toString());
                    }
                    if (catalogEntry instanceof AllWiseCatalogEntry entry) {
                        if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                            spectralTypes.add(AGN_WARNING);
                        }
                    }
                    if (catalogEntry instanceof WhiteDwarf entry) {
                        if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                            spectralTypes.add(WD_WARNING);
                        }
                    }
                    BatchResult batchResult = new BatchResult.Builder()
                            .setRowNumber(rowNumber)
                            .setObjectNumber(objectNumber)
                            .setCatalogName(catalogEntry.getCatalogName())
                            .setTargetRa(targetRa)
                            .setTargetDec(targetDec)
                            .setTargetDistance(catalogEntry.getTargetDistance())
                            .setRa(catalogEntry.getRa())
                            .setDec(catalogEntry.getDec())
                            .setSourceId(catalogEntry.getSourceId() + " ")
                            .setPlx(catalogEntry.getPlx())
                            .setPmra(catalogEntry.getPmra())
                            .setPmdec(catalogEntry.getPmdec())
                            .setMagnitudes(catalogEntry.getMagnitudes())
                            .setSpectralTypes(spectralTypes).build();
                    batchResults.add(batchResult);
                    resultCount++;
                    rowNumber++;
                }
                if (resultCount == 0) {
                    batchResults.add(new BatchResult(rowNumber, objectNumber, targetRa, targetDec));
                    rowNumber++;
                }
                //batchResults.add(new BatchResult(rowNumber, 0, 0, 0));
                //rowNumber++;
                progressBar.setValue(++objectNumber);

            }
        } catch (Exception ex) {
            handleError(future, ex.toString());
            return future;
        }

        displayQueryResults();

        echoField.setText("Query completed successfully!");
        echoField.setBackground(JColor.LIGHT_GREEN.val);
        future.complete(AsynchResult.SUCCESS);

        baseFrame.setVisible(true);
        isProcessing = false;
        return future;
    }

    private void handleError(CompletableFuture<AsynchResult> future, String message) {
        echoField.setText(message);
        echoField.setBackground(JColor.LIGHT_RED.val);
        future.complete(AsynchResult.ERROR);
        isProcessing = false;
    }

    private CatalogEntry performQuery(CatalogEntry catalogQuery) throws IOException {
        List<CatalogEntry> catalogEntries = catalogQueryService.getCatalogEntriesByCoords(catalogQuery);
        catalogEntries.forEach(catalogEntry -> {
            catalogEntry.setTargetRa(catalogQuery.getRa());
            catalogEntry.setTargetDec(catalogQuery.getDec());
        });
        if (!catalogEntries.isEmpty()) {
            catalogEntries.sort(Comparator.comparingDouble(CatalogEntry::getTargetDistance));
            return catalogEntries.get(0);
        }
        return null;
    }

    private void displayQueryResults() {
        List<Object[]> list = new ArrayList<>();
        batchResults.forEach(entry -> {
            list.add(entry.getColumnValues());
        });
        BatchResult result = batchResults.get(0);
        Object[] columns = result.getColumnTitles();
        Object[][] rows = new Object[][]{};
        DefaultTableModel defaultTableModel = new DefaultTableModel(list.toArray(rows), columns);
        JTable resultTable = new JTable(defaultTableModel);
        alignResultColumns(resultTable);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setRowSorter(createResultTableSorter(defaultTableModel));
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                int rowNumber = Integer.parseInt((String) resultTable.getValueAt(resultTable.getSelectedRow(), 0));
                BatchResult selected = batchResults.stream().filter(entry -> {
                    return entry.getRowNumber() == rowNumber;
                }).findFirst().get();
                if (selected != null) {
                    String coords = selected.getTargetRa() + " " + selected.getTargetDec();
                    imageViewerTab.getCoordsField().setText(coords);
                    catalogQueryTab.getCoordsField().setText(coords);
                    catalogQueryTab.getRadiusField().setText(roundTo2DecNZ(searchRadius));
                    catalogQueryTab.getSearchLabel().setText("");
                    catalogQueryTab.removeAndRecreateCenterPanel();
                    catalogQueryTab.removeAndRecreateBottomPanel();
                }
            }
        });
        resizeColumnWidth(resultTable);

        JScrollPane resultScrollPanel = new JScrollPane(resultTable);
        resultScrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Batch query results", TitledBorder.LEFT, TitledBorder.TOP
        ));
        centerPanel.add(resultScrollPanel);
    }

    public JPanel getBottomRow() {
        return bottomRow;
    }

}
