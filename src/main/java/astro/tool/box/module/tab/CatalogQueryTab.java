package astro.tool.box.module.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.Urls.*;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.catalog.WhiteDwarf;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.enumeration.FileType;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.LookupTable;
import astro.tool.box.enumeration.ObjectType;
import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.SpectralTypeLookupService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class CatalogQueryTab {

    public static final String TAB_NAME = "Catalog Search";

    private static final int BOTTOM_PANEL_HEIGHT = 375;

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    private final Map<String, CatalogEntry> catalogInstances;
    private final CatalogQueryFacade catalogQueryFacade;

    private final SpectralTypeLookupService spectralTypeLookupService;

    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    private JLabel searchLabel;
    private JButton searchButton;
    private JTextField coordsField;
    private JTextField radiusField;
    private JTextField panstarrsField;
    private JTextField aladinLiteField;
    private JTextField wiseViewField;
    private JTextField finderChartField;
    private JTable collectionTable;
    private JTable currentTable;

    private CatalogEntry selectedEntry;

    private boolean copyCoordsToClipboard;

    private int panstarrsFOV;
    private int aladinLiteFOV;
    private int wiseViewFOV;
    private int finderChartFOV;

    private double targetRa;
    private double targetDec;
    private double searchRadius;

    public CatalogQueryTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        catalogInstances = getCatalogInstances();
        catalogQueryFacade = new CatalogQueryService();
        InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(",", -1));
            }).collect(Collectors.toList());
            spectralTypeLookupService = new SpectralTypeLookupService(entries);
        }
    }

    public void init() {
        try {
            mainPanel = new JPanel(new BorderLayout());
            tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));

            topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.setPreferredSize(new Dimension(1000, 60));

            JLabel coordsLabel = new JLabel("Coordinates:");
            topPanel.add(coordsLabel);

            coordsField = new JTextField(25);
            topPanel.add(coordsField);

            JLabel radiusLabel = new JLabel("Search radius (arcsec):");
            topPanel.add(radiusLabel);

            radiusField = new JTextField(5);
            topPanel.add(radiusField);

            JLabel catalogLabel = new JLabel("Catalogs:");
            topPanel.add(catalogLabel);

            JCheckBox catalog;
            for (String catalogKey : catalogInstances.keySet()) {
                catalog = new JCheckBox(catalogKey);
                catalog.setSelected(true);
                topPanel.add(catalog);
            }

            searchButton = new JButton("Search");
            baseFrame.getRootPane().setDefaultButton(searchButton);
            searchButton.requestFocus();
            searchButton.addActionListener((ActionEvent e) -> {
                try {
                    baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    radiusField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
                        if (searchRadius > 300) {
                            errorMessages.add("Radius must not be larger than 300 arcsec.");
                        }
                    } catch (Exception ex) {
                        searchRadius = 0;
                        errorMessages.add("Invalid radius!");
                    }
                    List<String> selectedCatalogs = new ArrayList<>();
                    for (Component component : topPanel.getComponents()) {
                        if (component instanceof JCheckBox) {
                            JCheckBox catalogBox = (JCheckBox) component;
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
                        displayLinks(targetRa, targetDec, searchRadius);
                        String searchLabelText = "RA=" + targetRa + "° dec=" + targetDec + "° radius=" + searchRadius + " arcsec";
                        if (count > 0) {
                            searchLabel.setText(count + " result(s) for " + searchLabelText + " (" + resultsPerCatalog + ")");
                        } else {
                            searchLabel.setText("No results for " + searchLabelText);
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
            topPanel.add(searchButton);

            searchLabel = new JLabel();
            topPanel.add(searchLabel);

            tabbedPane.addChangeListener((ChangeEvent evt) -> {
                JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    baseFrame.getRootPane().setDefaultButton(searchButton);
                } else {
                    baseFrame.getRootPane().setDefaultButton(null);
                }
            });

            baseFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent componentEvent) {
                    String coords = coordsField.getText();
                    if (!coords.isEmpty() && selectedEntry != null) {
                        removeAndRecreateBottomPanel();
                        displayLinks(targetRa, targetDec, searchRadius);
                        displayCatalogDetails(selectedEntry);
                        displaySpectralTypes(selectedEntry);
                    }
                }
            });

            mainPanel.add(topPanel, BorderLayout.PAGE_START);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private int queryCatalog(CatalogEntry catalogQuery) throws IOException {
        List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
        catalogEntries.forEach(catalogEntry -> {
            catalogEntry.setTargetRa(catalogQuery.getRa());
            catalogEntry.setTargetDec(catalogQuery.getDec());
            catalogEntry.loadCatalogElements();
        });
        if (!catalogEntries.isEmpty()) {
            displayCatalogResults(catalogEntries, catalogQuery.getSearchRadius());
        }
        return catalogEntries.size();
    }

    private void displayCatalogResults(List<CatalogEntry> catalogEntries, double degRadius) {
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
                CatalogEntry selected = catalogEntries.stream().filter(entry -> {
                    return entry.getSourceId().equals(sourceId);
                }).findFirst().get();
                if (selected != null) {
                    selectedEntry = selected;
                    removeAndRecreateBottomPanel();
                    if (copyCoordsToClipboard) {
                        copyCoordsToClipboard(selected.getRa(), selected.getDec());
                    }
                    displayLinks(selected.getRa(), selected.getDec(), degRadius);
                    displayCatalogDetails(selected);
                    displaySpectralTypes(selected);
                    baseFrame.setVisible(true);
                }
            }
        });
        resizeColumnWidth(catalogTable);

        JScrollPane catalogScrollPanel = new JScrollPane(catalogTable);
        catalogScrollPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(catalogEntry.getCatalogColor(), 3), catalogEntry.getCatalogName() + " results", TitledBorder.LEFT, TitledBorder.TOP
        ));
        centerPanel.add(catalogScrollPanel);
    }

    private void displayLinks(double degRA, double degDE, double degRadius) {
        JPanel linkPanel = new JPanel(new GridLayout(17, 2));
        linkPanel.setPreferredSize(new Dimension(250, BOTTOM_PANEL_HEIGHT));
        linkPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(Color.LIGHT_GRAY, 3), "External resources", TitledBorder.LEFT, TitledBorder.TOP
        ));

        linkPanel.add(new JLabel("Image viewers:"));
        linkPanel.add(new JLabel("FoV (arcsec)"));
        panstarrsField = new JTextField(String.valueOf(panstarrsFOV));
        aladinLiteField = new JTextField(String.valueOf(aladinLiteFOV));
        wiseViewField = new JTextField(String.valueOf(wiseViewFOV));
        finderChartField = new JTextField(String.valueOf(finderChartFOV));
        if (degDE >= -31) {
            linkPanel.add(createHyperlink("Pan-STARRS", getPanstarrsUrl(degRA, degDE, panstarrsFOV, FileType.STACK)));
            linkPanel.add(panstarrsField);
        }
        linkPanel.add(createHyperlink("Aladin Lite", getAladinLiteUrl(degRA, degDE, aladinLiteFOV)));
        linkPanel.add(aladinLiteField);
        linkPanel.add(createHyperlink("WiseView", getWiseViewUrl(degRA, degDE, wiseViewFOV)));
        linkPanel.add(wiseViewField);
        linkPanel.add(createHyperlink("IRSA Finder Chart", getFinderChartUrl(degRA, degDE, finderChartFOV)));
        linkPanel.add(finderChartField);
        linkPanel.add(createHyperlink("Legacy Sky Viewer", getLegacySkyViewerUrl(degRA, degDE, "unwise-neo6")));
        JButton saveButton = new JButton("Change FoV");
        saveButton.addActionListener((ActionEvent e) -> {
            try {
                panstarrsFOV = toInteger(panstarrsField.getText());
                aladinLiteFOV = toInteger(aladinLiteField.getText());
                wiseViewFOV = toInteger(wiseViewField.getText());
                finderChartFOV = toInteger(finderChartField.getText());
                bottomPanel.remove(linkPanel);
                displayLinks(degRA, degDE, degRadius);
                baseFrame.setVisible(true);
            } catch (Exception ex) {
                showErrorDialog(baseFrame, "Invalid field of view!");
            }
        });
        linkPanel.add(saveButton);

        linkPanel.add(new JLabel());
        linkPanel.add(new JLabel());
        linkPanel.add(new JLabel("Databases:"));
        linkPanel.add(createHyperlink("IRSA Data Discovery", getDataDiscoveryUrl()));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("SIMBAD", getSimbadUrl(degRA, degDE, degRadius)));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("VizieR", getVizierUrl(degRA, degDE, degRadius)));

        linkPanel.add(new JLabel());
        linkPanel.add(new JLabel());
        linkPanel.add(new JLabel("VizieR catalogs:"));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("AllWISE", getSpecificCatalogsUrl("II/328/allwise", degRA, degDE, degRadius)));
        linkPanel.add(createHyperlink("CatWISE2020", getSpecificCatalogsUrl("II/365/catwise", degRA, degDE, degRadius)));
        linkPanel.add(createHyperlink("unWISE", getSpecificCatalogsUrl("II/363/unwise", degRA, degDE, degRadius)));
        linkPanel.add(createHyperlink("2MASS", getSpecificCatalogsUrl("II/246/out", degRA, degDE, degRadius)));
        linkPanel.add(createHyperlink("Gaia eDR3", getSpecificCatalogsUrl("I/350/gaiaedr3", degRA, degDE, degRadius)));
        linkPanel.add(createHyperlink("Gaia Distances", getSpecificCatalogsUrl("I/347/gaia2dis", degRA, degDE, degRadius)));
        linkPanel.add(createHyperlink("Gaia WD Candidates", getSpecificCatalogsUrl("J/MNRAS/482/4570/gaia2wd", degRA, degDE, degRadius)));
        linkPanel.add(createHyperlink("Pan-STARRS DR1", getSpecificCatalogsUrl("II/349/ps1", degRA, degDE, degRadius)));
        linkPanel.add(createHyperlink("SDSS DR12", getSpecificCatalogsUrl("V/147/sdss12", degRA, degDE, degRadius)));
        linkPanel.add(createHyperlink("VHS DR5", getSpecificCatalogsUrl("II/367/vhs_dr5", degRA, degDE, degRadius)));

        bottomPanel.add(linkPanel);
        bottomPanel.setComponentZOrder(linkPanel, 0);
    }

    private void displayCatalogDetails(CatalogEntry selectedEntry) {
        List<CatalogElement> catalogElements = selectedEntry.getCatalogElements();

        int size = catalogElements.size();
        int rows = size / 2;
        int remainder = size % 2;
        rows += remainder;

        int maxRows = rows > 19 ? rows : 19;

        JPanel detailPanel = new JPanel(new GridLayout(maxRows, 4));
        detailPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(selectedEntry.getCatalogColor(), 3),
                selectedEntry.getCatalogName() + " entry (Computed values are shown in green; (*) Further info: mouse pointer)",
                TitledBorder.LEFT,
                TitledBorder.TOP
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
        scrollPanel.setPreferredSize(new Dimension(650, BOTTOM_PANEL_HEIGHT));
        bottomPanel.add(scrollPanel);
    }

    private void displaySpectralTypes(CatalogEntry catalogEntry) {
        try {
            List<LookupResult> results = spectralTypeLookupService.lookup(catalogEntry.getColors(true));

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
            columnModel.getColumn(1).setPreferredWidth(100);
            columnModel.getColumn(2).setPreferredWidth(75);
            columnModel.getColumn(3).setPreferredWidth(50);
            columnModel.getColumn(4).setPreferredWidth(50);
            columnModel.getColumn(5).setPreferredWidth(75);
            columnModel.getColumn(6).setPreferredWidth(75);

            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(Color.LIGHT_GRAY, 3), "Spectral type evaluation", TitledBorder.LEFT, TitledBorder.TOP
            ));
            container.setPreferredSize(new Dimension(500, BOTTOM_PANEL_HEIGHT));
            container.add(new JScrollPane(spectralTypeTable));

            JPanel remarks = new JPanel(new GridLayout(0, 1));
            remarks.setPreferredSize(new Dimension(remarks.getWidth(), 100));
            container.add(remarks);

            if (spectralTypes.isEmpty()) {
                remarks.add(createLabel("No colors available / No match", JColor.RED));
            }
            if (catalogEntry instanceof AllWiseCatalogEntry) {
                AllWiseCatalogEntry entry = (AllWiseCatalogEntry) catalogEntry;
                if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                    remarks.add(createLabel(AGN_WARNING, JColor.RED));
                }
            }
            if (catalogEntry instanceof WhiteDwarf) {
                WhiteDwarf entry = (WhiteDwarf) catalogEntry;
                if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                    remarks.add(createLabel(WD_WARNING, JColor.RED));
                }
            }

            remarks.add(new JLabel("This feature uses Eric Mamajek's spectral type lookup table (version: 2021.03.02):"));
            String hyperlink = "http://www.pas.rochester.edu/~emamajek/EEM_dwarf_UBVIJHK_colors_Teff.txt";
            remarks.add(createHyperlink("A Modern Mean Dwarf Stellar Color & Effective Temperature Sequence", hyperlink));
            remarks.add(new JLabel("The table is also available in the " + LookupTab.TAB_NAME + " tab: " + LookupTable.MAIN_SEQUENCE.name()));

            JPanel toolsPanel = new JPanel();
            toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.Y_AXIS));
            toolsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
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
                collectObject(selectedObjectType, catalogEntry, baseFrame, spectralTypeLookupService, collectionTable);
                collectButton.setText("Added!");
                collectTimer.restart();
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            toolsPanel.add(buttonPanel);

            JButton copyCoordsButton = new JButton("Copy coords");
            buttonPanel.add(copyCoordsButton);
            Timer copyCoordsTimer = new Timer(3000, (ActionEvent e) -> {
                copyCoordsButton.setText("Copy coords");
            });
            copyCoordsButton.addActionListener((ActionEvent evt) -> {
                copyToClipboard(copyObjectCoordinates(catalogEntry));
                copyCoordsButton.setText("Copied!");
                copyCoordsTimer.restart();
            });

            JButton copyInfoButton = new JButton("Copy digest");
            buttonPanel.add(copyInfoButton);
            Timer copyInfoTimer = new Timer(3000, (ActionEvent e) -> {
                copyInfoButton.setText("Copy digest");
            });
            copyInfoButton.addActionListener((ActionEvent evt) -> {
                copyToClipboard(copyObjectDigest(catalogEntry));
                copyInfoButton.setText("Copied!");
                copyInfoTimer.restart();
            });

            JButton copyAllButton = new JButton("Copy all");
            buttonPanel.add(copyAllButton);
            Timer copyAllTimer = new Timer(3000, (ActionEvent e) -> {
                copyAllButton.setText("Copy all");
            });
            copyAllButton.addActionListener((ActionEvent evt) -> {
                copyToClipboard(copyObjectInfo(catalogEntry, results, null, null));
                copyAllButton.setText("Copied!");
                copyAllTimer.restart();
            });

            JButton fillFormButton = new JButton("TYGO form");
            buttonPanel.add(fillFormButton);
            fillFormButton.addActionListener((ActionEvent evt) -> {
                fillTygoForm(catalogEntry, catalogQueryFacade, baseFrame);

            });

            bottomPanel.add(container);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
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

    public JTextField getPanstarrsField() {
        return panstarrsField;
    }

    public JTextField getAladinLiteField() {
        return aladinLiteField;
    }

    public JTextField getWiseViewField() {
        return wiseViewField;
    }

    public JTextField getFinderChartField() {
        return finderChartField;
    }

    public JLabel getSearchLabel() {
        return searchLabel;
    }

    public CatalogEntry getSelectedEntry() {
        return selectedEntry;
    }

    public void setCollectionTable(JTable collectionTable) {
        this.collectionTable = collectionTable;
    }

    public void setCopyCoordsToClipboard(boolean copyCoordsToClipboard) {
        this.copyCoordsToClipboard = copyCoordsToClipboard;
    }

    public void setPanstarrsFOV(int panstarrsFOV) {
        this.panstarrsFOV = panstarrsFOV;
    }

    public void setAladinLiteFOV(int aladinLiteFOV) {
        this.aladinLiteFOV = aladinLiteFOV;
    }

    public void setWiseViewFOV(int wiseViewFOV) {
        this.wiseViewFOV = wiseViewFOV;
    }

    public void setFinderChartFOV(int finderChartFOV) {
        this.finderChartFOV = finderChartFOV;
    }

}
