package astro.tool.box.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.Extinction;
import astro.tool.box.container.NumberPair;
import astro.tool.box.catalog.GaiaCmd;
import astro.tool.box.catalog.SimbadCatalogEntry;
import astro.tool.box.catalog.WhiteDwarf;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.lookup.SpectralTypeLookupEntry;
import astro.tool.box.lookup.LookupResult;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.ObjectType;
import astro.tool.box.exception.ExtinctionException;
import astro.tool.box.lookup.DistanceLookupResult;
import astro.tool.box.panel.WiseCcdPanel;
import astro.tool.box.panel.GaiaCmdPanel;
import astro.tool.box.panel.ReferencesPanel;
import astro.tool.box.panel.SedMsPanel;
import astro.tool.box.panel.SedWdPanel;
import astro.tool.box.panel.WiseLcPanel;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.DistanceLookupService;
import astro.tool.box.service.DustExtinctionService;
import astro.tool.box.service.SpectralTypeLookupService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class CatalogQueryTab implements Tab {

    public static final String TAB_NAME = "Catalog Search";

    private static final int BOTTOM_PANEL_HEIGHT = 375;

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    private final Map<String, CatalogEntry> catalogInstances;
    private final CatalogQueryService catalogQueryService;

    private final SpectralTypeLookupService mainSequenceSpectralTypeLookupService;
    private final SpectralTypeLookupService brownDwarfsSpectralTypeLookupService;
    private final DistanceLookupService distanceLookupService;
    private final DustExtinctionService dustExtinctionService;
    private final List<SpectralTypeLookup> brownDwarfLookupEntries;

    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    private JLabel searchLabel;
    private JButton searchButton;
    private JTextField coordsField;
    private JTextField radiusField;
    private JTable collectionTable;
    private JTable currentTable;

    private CatalogEntry selectedEntry;

    private double targetRa;
    private double targetDec;
    private double searchRadius;
    private int windowShift;
    private boolean copyCoordsToClipboard;

    public CatalogQueryTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        catalogInstances = getCatalogInstances();
        catalogQueryService = new CatalogQueryService();
        dustExtinctionService = new DustExtinctionService();
        try (InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv")) {
        	Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines();
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(",", -1));
            }).collect(Collectors.toList());
            mainSequenceSpectralTypeLookupService = new SpectralTypeLookupService(entries);
        } catch (IOException e) {
        	showExceptionDialog(baseFrame, e);
        	throw new RuntimeException(e);
		}
        try (InputStream input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv");) {
        	Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines();
            brownDwarfLookupEntries = stream.skip(1).map(line -> {
                return new BrownDwarfLookupEntry(line.split(",", -1));
            }).collect(Collectors.toList());
            brownDwarfsSpectralTypeLookupService = new SpectralTypeLookupService(brownDwarfLookupEntries);
            distanceLookupService = new DistanceLookupService(brownDwarfLookupEntries);
        } catch (IOException e) {
        	showExceptionDialog(baseFrame, e);
        	throw new RuntimeException(e);
		}
    }

    @Override
    public void init(boolean visible) {
        try {
            mainPanel = new JPanel(new BorderLayout());

            if (visible) {
                tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));
            }

            topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.setPreferredSize(new Dimension(1000, 60));

            JLabel coordsLabel = new JLabel("Coordinates:");
            topPanel.add(coordsLabel);

            coordsField = new JTextField(25);
            topPanel.add(coordsField);
            coordsField.addActionListener((ActionEvent e) -> {
                searchCatalogs();
            });

            JLabel radiusLabel = new JLabel("Search radius (arcsec):");
            topPanel.add(radiusLabel);

            radiusField = new JTextField(5);
            topPanel.add(radiusField);
            radiusField.addActionListener((ActionEvent e) -> {
                searchCatalogs();
            });

            JLabel catalogLabel = new JLabel("Catalogs:");
            topPanel.add(catalogLabel);

            JCheckBox catalog;
            for (String catalogKey : catalogInstances.keySet()) {
                catalog = new JCheckBox(catalogKey);
                catalog.setSelected(true);
                topPanel.add(catalog);
            }

            searchButton = new JButton("Search");
            topPanel.add(searchButton);
            searchButton.addActionListener((ActionEvent e) -> {
                searchCatalogs();
            });

            searchLabel = new JLabel();
            topPanel.add(searchLabel);

            baseFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent componentEvent) {
                    String coords = coordsField.getText();
                    if (!coords.isEmpty() && selectedEntry != null) {
                        removeAndRecreateBottomPanel();
                        displayCatalogDetails(selectedEntry);
                        displaySpectralTypes(selectedEntry, true);
                    }
                }
            });

            mainPanel.add(topPanel, BorderLayout.PAGE_START);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void searchCatalogs() {
        try {
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
                searchRadius = Double.parseDouble(radius);
                if (searchRadius > 300) {
                    errorMessages.add("Radius must not be larger than 300 arcsec.");
                }
            } catch (NumberFormatException ex) {
                searchRadius = 0;
                errorMessages.add("Invalid radius!");
            }
            List<String> selectedCatalogs = new ArrayList<>();
            for (Component component : topPanel.getComponents()) {
                if (component instanceof JCheckBox catalogBox) {
                    if (catalogBox.isSelected()) {
                        selectedCatalogs.add(catalogBox.getText());
                    }
                }
            }
            if (selectedCatalogs.isEmpty()) {
                errorMessages.add("No catalog selected!");
            }
            if (!errorMessages.isEmpty()) {
                searchLabel.setText("");
                String message = String.join(LINE_SEP, errorMessages);
                showErrorDialog(baseFrame, message);
            } else {
                selectedEntry = null;
                if (copyCoordsToClipboard) {
                    copyCoordsToClipboard(targetRa, targetDec);
                }
                removeAndRecreateCenterPanel();
                removeAndRecreateBottomPanel();

                CompletableFuture.supplyAsync(() -> {
                    baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    radiusField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    try {
                        int count = 0;
                        StringBuilder resultsPerCatalog = new StringBuilder();
                        Iterator<String> iter = selectedCatalogs.listIterator();
                        while (iter.hasNext()) {
                            CatalogEntry catalogQuery = catalogInstances.get(iter.next());
                            catalogQuery.setRa(targetRa);
                            catalogQuery.setDec(targetDec);
                            catalogQuery.setSearchRadius(searchRadius);
                            int results = queryCatalog(catalogQuery);
                            count += results;
                            resultsPerCatalog.append(catalogQuery.getCatalogName()).append(": ").append(results);
                            if (iter.hasNext()) {
                                resultsPerCatalog.append("; ");
                            }
                        }
                        String searchLabelText = "RA=" + targetRa + "° dec=" + targetDec + "° radius=" + searchRadius + " arcsec";
                        if (count > 0) {
                            searchLabel.setText(count + " result(s) for " + searchLabelText + " (" + resultsPerCatalog + ")");
                        } else {
                            searchLabel.setText("No results for " + searchLabelText);
                        }
                        baseFrame.setVisible(true);
                    } catch (IOException ex) {
                        showExceptionDialog(baseFrame, ex);
                    } finally {
                        baseFrame.setCursor(Cursor.getDefaultCursor());
                        coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                        radiusField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    }
                    return null;
                });
            }
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private int queryCatalog(CatalogEntry catalogQuery) throws IOException {
        List<CatalogEntry> catalogEntries = catalogQueryService.getCatalogEntriesByCoords(catalogQuery);
        catalogEntries.forEach(catalogEntry -> {
            catalogEntry.setTargetRa(catalogQuery.getRa());
            catalogEntry.setTargetDec(catalogQuery.getDec());
            catalogEntry.loadCatalogElements();
        });
        if (!catalogEntries.isEmpty()) {
            displayCatalogResults(catalogEntries);
            baseFrame.setVisible(true);
        }
        return catalogEntries.size();
    }

    private void displayCatalogResults(List<CatalogEntry> catalogEntries) {
        selectedEntry = null;
        List<Object[]> list = new ArrayList<>();
        catalogEntries.forEach(entry -> {
            list.add(entry.getColumnValues());
        });
        CatalogEntry catalogEntry = catalogEntries.get(0);
        Object[] columns = catalogEntry.getColumnTitles();
        Object[][] rows = new Object[][]{};
        DefaultTableModel defaultTableModel = new DefaultTableModel(list.toArray(rows), columns);
        JTable catalogTable = new JTable(defaultTableModel);
        alignCatalogColumns(catalogTable, catalogEntry);
        catalogTable.setAutoCreateRowSorter(true);
        catalogTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        catalogTable.setRowSorter(createCatalogTableSorter(defaultTableModel, catalogEntry));
        catalogTable.getRowSorter().toggleSortOrder(0);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                if (currentTable != null && currentTable != catalogTable) {
                    try {
                        currentTable.clearSelection();
                    } catch (Exception ex) {
                    }
                }
                currentTable = catalogTable;
                String sourceId = (String) catalogTable.getValueAt(catalogTable.getSelectedRow(), 1);
                CatalogEntry selected = catalogEntries.stream()
                        .filter(entry -> entry.getSourceId().equals(sourceId))
                        .findFirst().get();
                if (selected != null) {
                    selectedEntry = selected;
                    removeAndRecreateBottomPanel();
                    if (copyCoordsToClipboard) {
                        copyCoordsToClipboard(selected.getRa(), selected.getDec());
                    }
                    displayCatalogDetails(selected);
                    displaySpectralTypes(selected, true);
                    baseFrame.setVisible(true);
                }
            }
        });
        resizeColumnWidth(catalogTable);

        // Save table data as CSV file
        JButton saveButton = new JButton("Save as CSV file");
        saveButton.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save CSV File");
            // Set file extension filter
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
            fileChooser.setFileFilter(filter);
            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                // Append .csv extension if not already present
                if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
                }
                try (FileWriter csvWriter = new FileWriter(fileToSave)) {
                    // Write column headers
                    for (int i = 0; i < catalogTable.getColumnCount(); i++) {
                        csvWriter.append(catalogTable.getColumnName(i));
                        if (i < catalogTable.getColumnCount() - 1) {
                            csvWriter.append(',');
                        } else {
                            csvWriter.append('\n');
                        }
                    }
                    // Write table data
                    for (int row = 0; row < catalogTable.getRowCount(); row++) {
                        for (int col = 0; col < catalogTable.getColumnCount(); col++) {
                            csvWriter.append(catalogTable.getValueAt(row, col).toString());
                            if (col < catalogTable.getColumnCount() - 1) {
                                csvWriter.append(',');
                            } else {
                                csvWriter.append('\n');
                            }
                        }
                    }
                } catch (Exception ex) {
                    writeErrorLog(ex);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(saveButton);

        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(catalogEntry.getCatalogColor(), 3), catalogEntry.getCatalogName() + " results", TitledBorder.LEFT, TitledBorder.TOP
        ));
        container.add(new JScrollPane(catalogTable), BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.add(container);
    }

    private void displayCatalogDetails(CatalogEntry selectedEntry) {
        List<CatalogElement> catalogElements = selectedEntry.getCatalogElements();

        int size = catalogElements.size();
        int rows = size / 2;
        int remainder = size % 2;
        rows += remainder;

        int maxRows = rows > 20 ? rows : 20;

        JPanel detailPanel = new JPanel(new GridLayout(0, 4));
        detailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), selectedEntry.getCatalogName() + " entry (Computed values are shown in green; (*) Further info: mouse pointer)", TitledBorder.LEFT, TitledBorder.TOP
        ));

        catalogElements.forEach(element -> {
            addLabelToPanel(element, detailPanel);
            addFieldToPanel(element, detailPanel);
        });

        if (remainder == 1) {
            addEmptyCatalogElement(detailPanel);
        }
        for (int i = 0; i < maxRows - rows; i++) {
            addEmptyCatalogElement(detailPanel);
            addEmptyCatalogElement(detailPanel);
        }

        JScrollPane scrollPanel = new JScrollPane(detailPanel);
        scrollPanel.setBorder(new LineBorder(selectedEntry.getCatalogColor(), 3));
        scrollPanel.setPreferredSize(new Dimension(650, BOTTOM_PANEL_HEIGHT));
        bottomPanel.add(scrollPanel);
    }

    private void displaySpectralTypes(CatalogEntry catalogEntry, boolean addExtinctionCheckbox) {
        try {
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setBorder(new LineBorder(selectedEntry.getCatalogColor(), 3));
            container.setPreferredSize(new Dimension(650, BOTTOM_PANEL_HEIGHT));

            List<LookupResult> mainSequenceResults = mainSequenceSpectralTypeLookupService.lookup(catalogEntry.getColors(true));
            if (!mainSequenceResults.isEmpty()) {
                container.add(createMainSequenceSpectralTypePanel(mainSequenceResults, catalogEntry));
                if (catalogEntry instanceof AllWiseCatalogEntry entry) {
                    if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        messagePanel.add(createLabel(AGN_WARNING, JColor.RED));
                        container.add(messagePanel);
                    }
                }
                if (catalogEntry instanceof WhiteDwarf entry) {
                    if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        messagePanel.add(createLabel(WD_WARNING, JColor.RED));
                        container.add(messagePanel);
                    }
                }
            }
            List<LookupResult> brownDwarfsResults = brownDwarfsSpectralTypeLookupService.lookup(catalogEntry.getColors(true));
            if (!brownDwarfsResults.isEmpty()) {
                container.add(createBrownDwarfsSpectralTypePanel(brownDwarfsResults, catalogEntry));
            }
            if (mainSequenceResults.isEmpty() && brownDwarfsResults.isEmpty()) {
                container.add(createMainSequenceSpectralTypePanel(mainSequenceResults, catalogEntry));
                JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                messagePanel.add(createLabel("No colors available / No match", JColor.RED));
                container.add(messagePanel);
            }

            JPanel toolsPanel = new JPanel();
            toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.Y_AXIS));
            container.add(toolsPanel);

            JPanel collectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            toolsPanel.add(collectPanel);

            collectPanel.add(new JLabel("Object type:"));

            JComboBox objectTypes = new JComboBox(ObjectType.labels());
            collectPanel.add(objectTypes);

            JButton collectButton = new JButton("Add to collection");
            collectPanel.add(collectButton);
            Timer collectTimer = new Timer(3000, (ActionEvent e) -> {
                collectButton.setText("Add to collection");
            });
            collectButton.addActionListener((ActionEvent evt) -> {
                String selectedObjectType = (String) objectTypes.getSelectedItem();
                collectObject(selectedObjectType, catalogEntry, baseFrame, mainSequenceSpectralTypeLookupService, collectionTable);
                collectButton.setText("Added!");
                collectTimer.restart();
            });

            if (catalogEntry instanceof SimbadCatalogEntry) {
                JButton referencesButton = new JButton("Object references");
                collectPanel.add(referencesButton);
                referencesButton.addActionListener((ActionEvent evt) -> {
                    JFrame referencesFrame = new JFrame();
                    referencesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    referencesFrame.addWindowListener(getChildWindowAdapter(baseFrame));
                    referencesFrame.setIconImage(getToolBoxImage());
                    referencesFrame.setTitle("Measurements and references for "
                            + catalogEntry.getSourceId() + " ("
                            + roundTo7DecNZ(catalogEntry.getRa()) + " "
                            + roundTo7DecNZ(catalogEntry.getDec()) + ")");
                    referencesFrame.add(new JScrollPane(new ReferencesPanel(catalogEntry, referencesFrame)));
                    referencesFrame.setSize(BASE_FRAME_WIDTH, BASE_FRAME_HEIGHT);
                    referencesFrame.setLocation(0, 0);
                    referencesFrame.setAlwaysOnTop(false);
                    referencesFrame.setResizable(true);
                    referencesFrame.setVisible(true);
                });
            }

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            toolsPanel.add(buttonPanel);

            JButton copyCoordsButton = new JButton("Copy coords");
            buttonPanel.add(copyCoordsButton);
            Timer copyCoordsTimer = new Timer(3000, (ActionEvent e) -> {
                copyCoordsButton.setText("Copy coords");
            });
            copyCoordsButton.addActionListener((ActionEvent evt) -> {
                copyToClipboard(copyObjectCoordinates(catalogEntry));
                copyCoordsButton.setText("Copied to clipboard!");
                copyCoordsTimer.restart();
            });

            JButton copyInfoButton = new JButton("Copy summary");
            buttonPanel.add(copyInfoButton);
            Timer copyInfoTimer = new Timer(3000, (ActionEvent e) -> {
                copyInfoButton.setText("Copy summary");
            });
            copyInfoButton.addActionListener((ActionEvent evt) -> {
                copyToClipboard(copyObjectSummary(catalogEntry));
                copyInfoButton.setText("Copied to clipboard!");
                copyInfoTimer.restart();
            });

            JButton copyAllButton = new JButton("Copy all");
            buttonPanel.add(copyAllButton);
            Timer copyAllTimer = new Timer(3000, (ActionEvent e) -> {
                copyAllButton.setText("Copy all");
            });
            copyAllButton.addActionListener((ActionEvent evt) -> {
                copyToClipboard(copyObjectInfo(catalogEntry, mainSequenceResults, brownDwarfsResults, distanceLookupService));
                copyAllButton.setText("Copied to clipboard!");
                copyAllTimer.restart();
            });

            JButton fillFormButton = new JButton("TYGO form");
            buttonPanel.add(fillFormButton);
            fillFormButton.addActionListener((ActionEvent evt) -> {
                fillTygoForm(catalogEntry, catalogQueryService, baseFrame);
            });

            JButton createSedButton = new JButton("SED (MS)");
            buttonPanel.add(createSedButton);
            createSedButton.addActionListener((ActionEvent evt) -> {
                createSedButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.addWindowListener(getChildWindowAdapter(baseFrame));
                frame.setIconImage(getToolBoxImage());
                frame.setTitle("SED");
                frame.add(new SedMsPanel(brownDwarfLookupEntries, catalogQueryService, catalogEntry, baseFrame));
                frame.setSize(1000, 900);
                frame.setLocation(0, 0);
                frame.setAlwaysOnTop(false);
                frame.setResizable(true);
                frame.setVisible(true);
                createSedButton.setCursor(Cursor.getDefaultCursor());
            });

            JButton createWdSedButton = new JButton("SED (WD)");
            buttonPanel.add(createWdSedButton);
            createWdSedButton.addActionListener((ActionEvent evt) -> {
                createWdSedButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.addWindowListener(getChildWindowAdapter(baseFrame));
                frame.setIconImage(getToolBoxImage());
                frame.setTitle("WD SED");
                frame.add(new SedWdPanel(catalogQueryService, catalogEntry, baseFrame));
                frame.setSize(1000, 900);
                frame.setLocation(0, 0);
                frame.setAlwaysOnTop(false);
                frame.setResizable(true);
                frame.setVisible(true);
                createWdSedButton.setCursor(Cursor.getDefaultCursor());
            });

            JButton createCcdButton = new JButton("WISE CCD");
            collectPanel.add(createCcdButton);
            createCcdButton.addActionListener((ActionEvent evt) -> {
                try {
                    createCcdButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.addWindowListener(getChildWindowAdapter(baseFrame));
                    frame.setIconImage(getToolBoxImage());
                    frame.setTitle("WISE CCD");
                    frame.add(new WiseCcdPanel(catalogQueryService, catalogEntry, baseFrame));
                    frame.setSize(1000, 900);
                    frame.setLocation(0, 0);
                    frame.setAlwaysOnTop(false);
                    frame.setResizable(true);
                    frame.setVisible(true);
                } catch (HeadlessException | SecurityException ex) {
                    showErrorDialog(baseFrame, ex.getMessage());
                } finally {
                    createCcdButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            JButton createLcButton = new JButton("WISE LC");
            collectPanel.add(createLcButton);
            createLcButton.addActionListener((ActionEvent evt) -> {
                try {
                    createLcButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.addWindowListener(getChildWindowAdapter(baseFrame));
                    frame.setIconImage(getToolBoxImage());
                    frame.setTitle("WISE light curves");
                    frame.add(new WiseLcPanel(catalogEntry, baseFrame));
                    frame.setSize(1000, 900);
                    frame.setLocation(0, 0);
                    frame.setAlwaysOnTop(false);
                    frame.setResizable(true);
                    frame.setVisible(true);
                } catch (HeadlessException | SecurityException ex) {
                    showErrorDialog(baseFrame, ex.getMessage());
                } finally {
                    createLcButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            if (catalogEntry instanceof GaiaCmd cmd) {
                JButton createCmdButton = new JButton("Gaia CMD");
                collectPanel.add(createCmdButton);
                createCmdButton.addActionListener((ActionEvent evt) -> {
                    try {
                        createCmdButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        JFrame frame = new JFrame();
                        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        frame.addWindowListener(getChildWindowAdapter(baseFrame));
                        frame.setIconImage(getToolBoxImage());
                        frame.setTitle("Gaia CMD");
                        frame.add(new GaiaCmdPanel(cmd));
                        frame.setSize(1000, 900);
                        frame.setLocation(0, 0);
                        frame.setAlwaysOnTop(false);
                        frame.setResizable(true);
                        frame.setVisible(true);
                    } catch (HeadlessException | SecurityException ex) {
                        showErrorDialog(baseFrame, ex.getMessage());
                    } finally {
                        createCmdButton.setCursor(Cursor.getDefaultCursor());
                    }
                });
            }

            if (addExtinctionCheckbox && catalogEntry instanceof Extinction) {
                final Extinction entry = (Extinction) catalogEntry.copy();
                JPanel extinctionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                toolsPanel.add(extinctionPanel);
                JCheckBox dustExtinction = new JCheckBox("Apply extinction correction for bands u, g, r, i, z, J, H, K, W1 & W2 (Schlafly & Finkbeiner, 2011)");
                extinctionPanel.add(dustExtinction);
                dustExtinction.addActionListener((ActionEvent evt) -> {
                    if (dustExtinction.isSelected()) {
                        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        try {
                            Map<String, Double> extinctionsByBand = dustExtinctionService.getExtinctionsByBand(entry.getRa(), entry.getDec(), 2.0);
                            try {
                                entry.applyExtinctionCorrection(extinctionsByBand);
                                entry.loadCatalogElements();
                                bottomPanel.remove(container);
                                displaySpectralTypes(entry, false);
                            } catch (ExtinctionException ex) {
                                extinctionPanel.add(createLabel("No extinction values for " + entry.getCatalogName() + " bands.", JColor.RED));
                            }
                        } catch (Exception ex) {
                            showExceptionDialog(baseFrame, ex);
                        } finally {
                            baseFrame.setCursor(Cursor.getDefaultCursor());
                        }
                    }
                });
            }

            bottomPanel.add(container);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private JScrollPane createMainSequenceSpectralTypePanel(List<LookupResult> results, CatalogEntry catalogEntry) {
        List<String[]> spectralTypes = new ArrayList<>();
        results.forEach(entry -> {
            String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
            String spectralType = entry.getSpt() + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + "," + roundTo3DecLZ(entry.getGap()) + ","
                    + entry.getTeff() + "," + roundTo3Dec(entry.getRsun()) + "," + roundTo3Dec(entry.getMsun());
            spectralTypes.add(spectralType.split(",", -1));
        });

        String titles = "spt,matched color,nearest color,offset,teff,radius (Rsun),mass (Msun)";
        String[] columns = titles.split(",", -1);
        Object[][] rows = new Object[][]{};
        JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns);
        alignResultColumns(spectralTypeTable, spectralTypes);
        spectralTypeTable.setAutoCreateRowSorter(true);
        spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = spectralTypeTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(120);
        columnModel.getColumn(2).setPreferredWidth(75);
        columnModel.getColumn(3).setPreferredWidth(50);
        columnModel.getColumn(4).setPreferredWidth(50);
        columnModel.getColumn(5).setPreferredWidth(75);
        columnModel.getColumn(6).setPreferredWidth(75);

        spectralTypeTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                if (currentTable != null && currentTable != spectralTypeTable) {
                    try {
                        currentTable.clearSelection();
                    } catch (Exception ex) {
                    }
                }
                currentTable = spectralTypeTable;
                String spt = (String) spectralTypeTable.getValueAt(spectralTypeTable.getSelectedRow(), 0);
                List<DistanceLookupResult> distanceResults = distanceLookupService.lookup(spt, catalogEntry.getBands());
                createDistanceEstimatesPanel(distanceResults, spt, catalogEntry.getCatalogColor());
            }
        });

        JScrollPane spectralTypePanel = new JScrollPane(spectralTypeTable);
        spectralTypePanel.setToolTipText(PHOT_DIST_INFO);
        spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), html("Main sequence spectral type estimates " + INFO_ICON), TitledBorder.LEFT, TitledBorder.TOP
        ));

        return spectralTypePanel;
    }

    private JScrollPane createBrownDwarfsSpectralTypePanel(List<LookupResult> results, CatalogEntry catalogEntry) {
        List<String[]> spectralTypes = new ArrayList<>();
        results.forEach(entry -> {
            String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
            String spectralType = entry.getSpt() + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + "," + roundTo3DecLZ(entry.getGap());
            spectralTypes.add(spectralType.split(",", -1));
        });

        String titles = "spt,matched color,nearest color,offset";
        String[] columns = titles.split(",", -1);
        Object[][] rows = new Object[][]{};
        JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns);
        alignResultColumns(spectralTypeTable, spectralTypes);
        spectralTypeTable.setAutoCreateRowSorter(true);
        spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = spectralTypeTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(120);
        columnModel.getColumn(2).setPreferredWidth(75);
        columnModel.getColumn(3).setPreferredWidth(50);

        spectralTypeTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                if (currentTable != null && currentTable != spectralTypeTable) {
                    try {
                        currentTable.clearSelection();
                    } catch (Exception ex) {
                    }
                }
                currentTable = spectralTypeTable;
                String spt = (String) spectralTypeTable.getValueAt(spectralTypeTable.getSelectedRow(), 0);
                List<DistanceLookupResult> distanceResults = distanceLookupService.lookup(spt, catalogEntry.getBands());
                createDistanceEstimatesPanel(distanceResults, spt, catalogEntry.getCatalogColor());
            }
        });

        JScrollPane spectralTypePanel = new JScrollPane(spectralTypeTable);
        spectralTypePanel.setToolTipText(PHOT_DIST_INFO);
        spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), html("M, L & T dwarfs spectral type estimates " + INFO_ICON), TitledBorder.LEFT, TitledBorder.TOP
        ));

        return spectralTypePanel;
    }

    private void createDistanceEstimatesPanel(List<DistanceLookupResult> results, String spt, Color color) {
        List<String[]> distances = new ArrayList<>();
        results.forEach(entry -> {
            String matchedBand = entry.getBandKey().val + "=" + roundTo3DecNZ(entry.getBandValue());
            String distance = roundTo3Dec(entry.getDistance());
            if (entry.getDistanceError() > 0) {
                distance += "±" + roundTo3Dec(entry.getDistanceError());
            }
            String resutValues = distance + "," + matchedBand;
            distances.add(resutValues.split(",", -1));
        });

        String titles = "distance (pc),matched bands";
        String[] columns = titles.split(",", -1);
        Object[][] rows = new Object[][]{};
        JTable distanceTable = new JTable(distances.toArray(rows), columns);
        alignResultColumns(distanceTable, distances);
        distanceTable.setAutoCreateRowSorter(true);
        distanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = distanceTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(100);

        JScrollPane distancePanel = new JScrollPane(distanceTable);
        distancePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Spectral type: " + spt, TitledBorder.LEFT, TitledBorder.TOP
        ));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new LineBorder(color, 3));
        container.add(distancePanel);

        JFrame detailsFrame = new JFrame();
        detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        detailsFrame.addWindowListener(getChildWindowAdapter(baseFrame));
        detailsFrame.setIconImage(getToolBoxImage());
        detailsFrame.setTitle("Photometric distance estimates");
        detailsFrame.add(container);
        detailsFrame.setSize(500, 300);
        detailsFrame.setLocation(windowShift, windowShift);
        detailsFrame.setAlwaysOnTop(true);
        detailsFrame.setResizable(true);
        detailsFrame.setVisible(true);
        windowShift += 10;
    }

    public void removeAndRecreateCenterPanel() {
        if (centerPanel != null) {
            mainPanel.remove(centerPanel);
        }
        centerPanel = new JPanel(new GridLayout(2, 0));
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setPreferredSize(new Dimension(centerPanel.getWidth(), 250));
    }

    public void removeAndRecreateBottomPanel() {
        if (bottomPanel != null) {
            mainPanel.remove(bottomPanel);
        }
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.add(bottomPanel, BorderLayout.PAGE_END);
    }

    public JPanel getTopPanel() {
        return topPanel;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JTextField getCoordsField() {
        return coordsField;
    }

    public JTextField getRadiusField() {
        return radiusField;
    }

    public JLabel getSearchLabel() {
        return searchLabel;
    }

    public void setCollectionTable(JTable collectionTable) {
        this.collectionTable = collectionTable;
    }

    public void setCopyCoordsToClipboard(boolean copyCoordsToClipboard) {
        this.copyCoordsToClipboard = copyCoordsToClipboard;
    }

}
