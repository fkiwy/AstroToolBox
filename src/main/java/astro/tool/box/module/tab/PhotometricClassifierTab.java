package astro.tool.box.module.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.getSelectedCatalogs;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.BatchResult;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.container.catalog.WhiteDwarf;
import astro.tool.box.container.lookup.BrownDwarfLookupEntry;
import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.SpectralTypeLookupService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class PhotometricClassifierTab {

    private static final String TAB_NAME = "Photometric Classifier";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;
    private final ImageViewerTab imageViewerTab;

    private final Map<String, CatalogEntry> catalogInstances;
    private final CatalogQueryFacade catalogQueryFacade;

    private final SpectralTypeLookupService mainSequenceLookupService;
    private final SpectralTypeLookupService brownDwarfsLookupService;

    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    private JButton searchButton;
    private JTextField coordsField;
    private JTextField radiusField;

    private double targetRa;
    private double targetDec;
    private double searchRadius;

    private Map<String, Integer> sptOccurrencesAltogether;
    private Map<String, Integer> sptOccurrencesMainSequence;
    private Map<String, Integer> sptOccurrencesBrownDwarfs;
    private Map<String, Integer> sptOccurrencesSimbad;

    public PhotometricClassifierTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab, ImageViewerTab imageViewerTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        this.imageViewerTab = imageViewerTab;
        catalogInstances = getCatalogInstances();
        catalogQueryFacade = new CatalogQueryService();
        InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(SPLIT_CHAR, SpectralTypeLookupEntry.NUMBER_OF_COLUMNS));
            }).collect(Collectors.toList());
            mainSequenceLookupService = new SpectralTypeLookupService(entries);
        }
        input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new BrownDwarfLookupEntry(line.split(SPLIT_CHAR, BrownDwarfLookupEntry.NUMBER_OF_COLUMNS));
            }).collect(Collectors.toList());
            brownDwarfsLookupService = new SpectralTypeLookupService(entries);
        }
    }

    public void init() {
        try {
            mainPanel = new JPanel(new BorderLayout());
            tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));

            topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            centerPanel = new JPanel(new GridLayout(2, 1));
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

            JLabel coordsLabel = new JLabel("Coordinates:");
            topPanel.add(coordsLabel);

            coordsField = new JTextField(25);
            topPanel.add(coordsField);

            JLabel radiusLabel = new JLabel("Search radius (arcsec):");
            topPanel.add(radiusLabel);

            radiusField = new JTextField(5);
            topPanel.add(radiusField);
            radiusField.setText("5");

            searchButton = new JButton("Search");
            topPanel.add(searchButton);
            searchButton.addActionListener((ActionEvent e) -> {
                try {
                    baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    radiusField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    if (centerPanel.getComponentCount() > 0) {
                        centerPanel.removeAll();
                    }
                    if (bottomPanel.getComponentCount() > 0) {
                        bottomPanel.removeAll();
                    }
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
                        if (searchRadius > 100) {
                            errorMessages.add("Radius must not be larger than 100 arcsec.");
                        }
                    } catch (Exception ex) {
                        searchRadius = 0;
                        errorMessages.add("Invalid radius!");
                    }
                    if (!errorMessages.isEmpty()) {
                        String message = String.join(LINE_SEP, errorMessages);
                        showErrorDialog(baseFrame, message);
                    } else {
                        List<CatalogEntry> catalogEntries = new ArrayList<>();
                        List<String> selectedCatalogs = getSelectedCatalogs(catalogInstances);
                        for (CatalogEntry catalogEntry : catalogInstances.values()) {
                            if (selectedCatalogs.contains(catalogEntry.getCatalogName())) {
                                catalogEntry.setRa(targetRa);
                                catalogEntry.setDec(targetDec);
                                catalogEntry.setSearchRadius(searchRadius);
                                List<CatalogEntry> results = performQuery(catalogEntry);
                                if (results != null) {
                                    catalogEntries.addAll(results);
                                }
                            }
                        }
                        sptOccurrencesAltogether = new HashMap();
                        sptOccurrencesMainSequence = new HashMap();
                        sptOccurrencesBrownDwarfs = new HashMap();
                        sptOccurrencesSimbad = new HashMap();
                        List<BatchResult> batchResults;
                        batchResults = performSpectralTypeLookup(mainSequenceLookupService, catalogEntries, sptOccurrencesMainSequence);
                        displayQueryResults(batchResults, "Main sequence spectral type evaluation", JColor.DARK_GREEN.val);
                        batchResults = performSpectralTypeLookup(brownDwarfsLookupService, catalogEntries, sptOccurrencesBrownDwarfs);
                        displayQueryResults(batchResults, "Brown dwarfs spectral type evaluation", JColor.BROWN.val);
                        displayClassification(sptOccurrencesAltogether, "Photometric classification: Altogether", Color.RED);
                        displayClassification(sptOccurrencesMainSequence, "Photometric classification: Main sequence", JColor.DARK_GREEN.val);
                        displayClassification(sptOccurrencesBrownDwarfs, "Photometric classification: Brown dwarfs", JColor.BROWN.val);
                        if (!sptOccurrencesSimbad.isEmpty()) {
                            displayClassification(sptOccurrencesSimbad, "SIMBAD object type", Color.LIGHT_GRAY);
                        }
                        baseFrame.setVisible(true);
                    }
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    baseFrame.setCursor(Cursor.getDefaultCursor());
                    coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    radiusField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                }
            });

            coordsField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
            radiusField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private List<CatalogEntry> performQuery(CatalogEntry catalogQuery) throws IOException {
        List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
        catalogEntries.forEach(catalogEntry -> {
            catalogEntry.setTargetRa(catalogQuery.getRa());
            catalogEntry.setTargetDec(catalogQuery.getDec());
        });
        if (!catalogEntries.isEmpty()) {
            catalogEntries.sort(Comparator.comparingDouble(CatalogEntry::getTargetDistance));
            return catalogEntries;
        }
        return null;
    }

    private List<BatchResult> performSpectralTypeLookup(SpectralTypeLookupService spectralTypeLookupService, List<CatalogEntry> catalogEntries, Map<String, Integer> sptOccurrences) throws Exception {
        List<BatchResult> batchResults = new ArrayList<>();
        int rowNumber = 1;
        for (CatalogEntry catalogEntry : catalogEntries) {
            List<LookupResult> results = spectralTypeLookupService.lookup(catalogEntry.getColors(true));
            List<String> spectralTypes = new ArrayList<>();
            results.forEach(entry -> {
                String spectralType = entry.getSpt();
                addOccurrence(spectralType.replace("V", ""), sptOccurrences);
                String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
                spectralType += ": " + matchedColor + "; ";
                spectralTypes.add(spectralType);
            });
            if (catalogEntry instanceof SimbadCatalogEntry) {
                SimbadCatalogEntry simbadEntry = (SimbadCatalogEntry) catalogEntry;
                StringBuilder simbadType = new StringBuilder();
                simbadType.append(simbadEntry.getObjectType());
                String spectralType = simbadEntry.getSpectralType();
                if (!spectralType.isEmpty()) {
                    simbadType.append(": ").append(spectralType);
                }
                sptOccurrencesSimbad.put(simbadType.toString(), 1);
                simbadType.append("; ");
                spectralTypes.add(0, simbadType.toString());
            }
            if (catalogEntry instanceof AllWiseCatalogEntry) {
                AllWiseCatalogEntry entry = (AllWiseCatalogEntry) catalogEntry;
                if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                    String spectralType = AGN_WARNING;
                    addOccurrence(spectralType, sptOccurrences);
                    spectralTypes.add(spectralType);
                }
            }
            if (catalogEntry instanceof WhiteDwarf) {
                WhiteDwarf entry = (WhiteDwarf) catalogEntry;
                if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                    String spectralType = WD_WARNING;
                    addOccurrence(spectralType, sptOccurrences);
                    spectralTypes.add(spectralType);
                }
            }
            BatchResult batchResult = new BatchResult.Builder()
                    .setRowNumber(rowNumber++)
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
        }
        return batchResults;
    }

    private void displayQueryResults(List<BatchResult> batchResults, String title, Color borderColor) {
        List<Object[]> list = new ArrayList<>();
        batchResults.forEach(entry -> {
            list.add(entry.getColumnValues());
        });
        BatchResult result = batchResults.get(0);
        Object[] columns = result.getColumnTitles();
        Object[][] rows = new Object[][]{};
        DefaultTableModel defaultTableModel = new DefaultTableModel(list.toArray(rows), columns);
        JTable resultTable = new JTable(defaultTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
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
                    String coords = selected.getRa() + " " + selected.getDec();
                    imageViewerTab.getCoordsField().setText(coords);
                    catalogQueryTab.getCoordsField().setText(coords);
                    catalogQueryTab.getRadiusField().setText(roundTo2DecNZ(searchRadius));
                    catalogQueryTab.getSearchLabel().setText("");
                    catalogQueryTab.removeAndRecreateCenterPanel();
                    catalogQueryTab.removeAndRecreateBottomPanel();
                }
            }
        });
        resizeColumnWidth(resultTable, 300);

        JScrollPane resultScrollPanel = new JScrollPane(resultTable);
        resultScrollPanel.setPreferredSize(new Dimension(resultScrollPanel.getWidth(), resultScrollPanel.getHeight()));
        resultScrollPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(borderColor, 2), title, TitledBorder.LEFT, TitledBorder.TOP
        ));
        centerPanel.add(resultScrollPanel);
    }

    private void displayClassification(Map<String, Integer> sptOccurrences, String title, Color borderColor) {
        List<String[]> spectralTypes = new ArrayList();
        sptOccurrences.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEach(entry -> {
            spectralTypes.add(new String[]{entry.getValue().toString(), entry.getKey()});
        });

        String titles = "occurrences,spectral type";
        String[] columns = titles.split(",", 2);
        Object[][] rows = new Object[][]{};
        DefaultTableModel defaultTableModel = new DefaultTableModel(spectralTypes.toArray(rows), columns);
        JTable resultTable = new JTable(defaultTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        alignResultColumns(resultTable, spectralTypes);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setRowSorter(createResultTableSorter(defaultTableModel, spectralTypes));
        TableColumnModel columnModel = resultTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(75);
        columnModel.getColumn(1).setPreferredWidth(195);

        JScrollPane resultScrollPanel = spectralTypes.isEmpty()
                ? new JScrollPane(createLabel("No colors available / No match", JColor.RED))
                : new JScrollPane(resultTable);
        resultScrollPanel.setPreferredSize(new Dimension(300, 250));
        resultScrollPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(borderColor, 2), title, TitledBorder.LEFT, TitledBorder.TOP
        ));
        bottomPanel.add(resultScrollPanel);
    }

    private void addOccurrence(String spectralType, Map<String, Integer> sptOccurrences) {
        Integer occurrences = sptOccurrences.get(spectralType);
        sptOccurrences.put(spectralType, occurrences == null ? 1 : occurrences + 1);
        occurrences = sptOccurrencesAltogether.get(spectralType);
        sptOccurrencesAltogether.put(spectralType, occurrences == null ? 1 : occurrences + 1);
    }

}