package astro.tool.box.module.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.Urls.*;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.ColorValue;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.catalog.GaiaDR2CatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.LookupTable;
import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.SpectralTypeLookupService;
import java.awt.BorderLayout;
import java.awt.Component;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class CatalogQueryTab {

    public static final String TAB_NAME = "Catalog Search";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

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

    private final CatalogQueryFacade catalogQueryFacade;
    private final SpectralTypeLookupService spectralTypeLookupService;

    private final Map<String, CatalogEntry> catalogInstances;
    private final Map<Integer, List<CatalogEntry>> catalogResults;

    private AllWiseCatalogEntry selectedAllWiseEntry;
    private CatWiseCatalogEntry selectedCatWiseEntry;
    private CatalogEntry selectedEntry;

    private boolean copyCoordsToClipboard;

    private int panstarrsFOV;
    private int aladinLiteFOV;
    private int wiseViewFOV;
    private int finderChartFOV;

    private double targetRa;
    private double targetDec;

    public CatalogQueryTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        catalogQueryFacade = new CatalogQueryService();
        InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(SPLIT_CHAR, 30));
            }).collect(Collectors.toList());
            spectralTypeLookupService = new SpectralTypeLookupService(entries);
        }
        catalogInstances = new LinkedHashMap<>();
        catalogResults = new HashMap<>();
    }

    public void init() {
        try {
            // Plug in catalogs here
            SimbadCatalogEntry simbadCatalogEntry = new SimbadCatalogEntry();
            catalogInstances.put(simbadCatalogEntry.getCatalogName(), simbadCatalogEntry);
            GaiaDR2CatalogEntry gaiaDR2CatalogEntry = new GaiaDR2CatalogEntry();
            catalogInstances.put(gaiaDR2CatalogEntry.getCatalogName(), gaiaDR2CatalogEntry);
            AllWiseCatalogEntry allWiseCatalogEntry = new AllWiseCatalogEntry();
            catalogInstances.put(allWiseCatalogEntry.getCatalogName(), allWiseCatalogEntry);
            CatWiseCatalogEntry catWiseCatalogEntry = new CatWiseCatalogEntry();
            catalogInstances.put(catWiseCatalogEntry.getCatalogName(), catWiseCatalogEntry);

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

            JCheckBox checkbox;
            for (String catalogKey : catalogInstances.keySet()) {
                checkbox = new JCheckBox(catalogKey);
                checkbox.setSelected(true);
                topPanel.add(checkbox);
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
                    double searchRadius;
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
                        searchRadius = Double.valueOf(0);
                        errorMessages.add("Invalid radius!");
                    }
                    List<String> selectedCatalogs = new ArrayList<>();
                    for (Component component : topPanel.getComponents()) {
                        if (component instanceof JCheckBox) {
                            JCheckBox choice = (JCheckBox) component;
                            if (choice.isSelected()) {
                                selectedCatalogs.add(choice.getText());
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
                        if (copyCoordsToClipboard) {
                            copyCoordsToClipboard(targetRa, targetDec);
                        }
                        removeAndRecreateCenterPanel();
                        removeAndRecreateBottomPanel();

                        int count = 0;
                        int catalogNumber = 0;
                        StringBuilder resultsPerCatalog = new StringBuilder();
                        Iterator<String> iter = selectedCatalogs.listIterator();
                        while (iter.hasNext()) {
                            CatalogEntry catalogQuery = catalogInstances.get(iter.next());
                            catalogQuery.setRa(targetRa);
                            catalogQuery.setDec(targetDec);
                            catalogQuery.setSearchRadius(searchRadius);
                            catalogQuery.setCatalogNumber(catalogNumber++);
                            int results = queryCatalog(catalogQuery);
                            count += results;
                            if (results == 0 && catalogNumber > 0) {
                                catalogNumber--;
                            }
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

            mainPanel.add(topPanel, BorderLayout.PAGE_START);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private int queryCatalog(CatalogEntry catalogQuery) throws IOException {
        selectedAllWiseEntry = null;
        selectedCatWiseEntry = null;
        List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
        catalogEntries.forEach(catalogEntry -> {
            catalogEntry.setTargetRa(catalogQuery.getRa());
            catalogEntry.setTargetDec(catalogQuery.getDec());
            catalogEntry.setCatalogNumber(catalogQuery.getCatalogNumber());
            catalogEntry.loadCatalogElements();
        });
        if (!catalogEntries.isEmpty()) {
            catalogResults.put(catalogQuery.getCatalogNumber(), catalogEntries);
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
        JTable catalogTable = new JTable(defaultTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        alignCatalogColumns(catalogTable, catalogEntry);
        catalogTable.setAutoCreateRowSorter(true);
        catalogTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        catalogTable.setRowSorter(createCatalogTableSorter(defaultTableModel, catalogEntry));
        catalogTable.getRowSorter().toggleSortOrder(0);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                Component[] components = centerPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    if (i != catalogEntry.getCatalogNumber()) {
                        Component component = components[i];
                        if (component != null) {
                            centerPanel.remove(component);
                            displayCatalogResults(catalogResults.get(i), degRadius);
                        }
                    }
                }
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
                    //displayProperMotions(selected);
                    displaySpectralTypes(selected.getColors());
                    baseFrame.setVisible(true);
                }
            }
        });
        resizeColumnWidth(catalogTable);

        JScrollPane catalogScrollPanel = new JScrollPane(catalogTable);
        catalogScrollPanel.setBackground(catalogEntry.getCatalogColor());
        catalogScrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), catalogEntry.getCatalogName() + " results", TitledBorder.LEFT, TitledBorder.TOP
        ));
        centerPanel.add(catalogScrollPanel, catalogEntry.getCatalogNumber());
    }

    private void displayLinks(double degRA, double degDE, double degRadius) {
        JPanel linkPanel = new JPanel(new GridLayout(18, 2));
        linkPanel.setPreferredSize(new Dimension(250, 375));
        linkPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "External resources", TitledBorder.LEFT, TitledBorder.TOP
        ));

        linkPanel.add(new JLabel("Image viewers:"));
        linkPanel.add(new JLabel("FoV (arcsec)"));
        panstarrsField = new JTextField(String.valueOf(panstarrsFOV));
        aladinLiteField = new JTextField(String.valueOf(aladinLiteFOV));
        wiseViewField = new JTextField(String.valueOf(wiseViewFOV));
        finderChartField = new JTextField(String.valueOf(finderChartFOV));
        if (degDE >= -30) {
            linkPanel.add(createHyperlink("PanSTARRS", getPanstarrsUrl(degRA, degDE, panstarrsFOV)));
            linkPanel.add(panstarrsField);
        }
        linkPanel.add(createHyperlink("Aladin Lite", getAladinLiteUrl(degRA, degDE, aladinLiteFOV)));
        linkPanel.add(aladinLiteField);
        linkPanel.add(createHyperlink("WiseView", getWiseViewUrl(degRA, degDE, wiseViewFOV)));
        linkPanel.add(wiseViewField);
        linkPanel.add(createHyperlink("IRSA Finder Chart", getFinderChartUrl(degRA, degDE, finderChartFOV)));
        linkPanel.add(finderChartField);

        linkPanel.add(new JLabel());
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
        linkPanel.add(createHyperlink("IRSA Data Discov.", getDataDiscoveryUrl()));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("SIMBAD", getSimbadUrl(degRA, degDE, degRadius)));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("VizieR", getVizierUrl(degRA, degDE, degRadius)));

        linkPanel.add(new JLabel());
        linkPanel.add(new JLabel());
        linkPanel.add(new JLabel("Single catalogs:"));
        linkPanel.add(createHyperlink("AllWISE", getSpecificCatalogsUrl("II/328/allwise", degRA, degDE, degRadius)));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("2MASS", getSpecificCatalogsUrl("II/246/out", degRA, degDE, degRadius)));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("Gaia DR2", getSpecificCatalogsUrl("I/345/gaia2", degRA, degDE, degRadius)));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("Gaia Distances", getSpecificCatalogsUrl("I/347/gaia2dis", degRA, degDE, degRadius)));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("Gaia WD Candidates", getSpecificCatalogsUrl("J/MNRAS/482/4570/gaia2wd", degRA, degDE, degRadius)));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("PanSTARRS DR1", getSpecificCatalogsUrl("II/349/ps1", degRA, degDE, degRadius)));
        linkPanel.add(new JLabel());
        linkPanel.add(createHyperlink("SDSS DR12", getSpecificCatalogsUrl("V/147/sdss12", degRA, degDE, degRadius)));

        bottomPanel.add(linkPanel);
        bottomPanel.setComponentZOrder(linkPanel, 0);
    }

    private void displayCatalogDetails(CatalogEntry selectedEntry) {
        int maxRows = 19;
        JPanel detailPanel = new JPanel(new GridLayout(maxRows, 4));
        detailPanel.setPreferredSize(new Dimension(650, 375));
        detailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), selectedEntry.getCatalogName() + " entry (computed values are shown in green)", TitledBorder.LEFT, TitledBorder.TOP
        ));

        List<CatalogElement> catalogElements = selectedEntry.getCatalogElements();
        catalogElements.forEach(element -> {
            addLabelToPanel(element, detailPanel);
            addFieldToPanel(element, detailPanel);
        });

        int size = catalogElements.size();
        int rows = size / 2;
        int remainder = size % 2;
        rows += remainder;
        if (remainder == 1) {
            addEmptyCatalogElement(detailPanel);
        }
        for (int i = 0; i < maxRows - rows; i++) {
            addEmptyCatalogElement(detailPanel);
            addEmptyCatalogElement(detailPanel);
        }

        bottomPanel.add(detailPanel);
    }

    /*private void displayProperMotions(CatalogEntry selectedEntry) {
        if (selectedEntry instanceof AllWiseCatalogEntry) {
            selectedAllWiseEntry = (AllWiseCatalogEntry) selectedEntry;
        } else if (selectedEntry instanceof CatWiseCatalogEntry) {
            selectedCatWiseEntry = (CatWiseCatalogEntry) selectedEntry;
        }
        if (selectedAllWiseEntry != null && selectedCatWiseEntry != null) {
            NumberPair properMotions = calculateProperMotions(
                    new NumberPair(selectedAllWiseEntry.getRa_pm(), selectedAllWiseEntry.getDec_pm()),
                    new NumberPair(selectedCatWiseEntry.getRa_pm(), selectedCatWiseEntry.getDec_pm()),
                    55400,
                    56700,
                    DEG_MAS
            );
            System.out.println("Apparent motions: " + properMotions);
        }
    }*/
    //
    private void displaySpectralTypes(Map<Color, Double> colors) {
        try {
            Map<SpectralTypeLookupResult, Set<ColorValue>> results = spectralTypeLookupService.lookup(colors);

            List<Object[]> spectralTypes = new ArrayList<>();
            results.entrySet().forEach(entry -> {
                SpectralTypeLookupResult key = entry.getKey();
                Set<ColorValue> values = entry.getValue();
                StringBuilder matchedColors = new StringBuilder();
                Iterator<ColorValue> colorIterator = values.iterator();
                while (colorIterator.hasNext()) {
                    ColorValue colorValue = colorIterator.next();
                    matchedColors.append(colorValue.getColor().val).append("=").append(roundTo3DecNZ(colorValue.getValue()));
                    if (colorIterator.hasNext()) {
                        matchedColors.append(", ");
                    }
                }
                String spectralType = key.getSpt() + "," + key.getTeff() + "," + key.getRsun() + "," + key.getMsun() + "," + matchedColors
                        + "," + key.getNearest() + "," + roundTo3DecLZ(key.getGap());
                spectralTypes.add(spectralType.split(",", 7));
            });

            String titles = "spt,teff,sol rad,sol mass,matched colors,nearest color,gap to nearest color";
            String[] columns = titles.split(",", 7);
            Object[][] rows = new Object[][]{};
            JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };
            spectralTypeTable.setAutoCreateRowSorter(true);
            spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            spectralTypeTable.setCellSelectionEnabled(false);
            resizeColumnWidth(spectralTypeTable);

            JScrollPane spectralTypePanel = spectralTypes.isEmpty()
                    ? new JScrollPane(createLabel("No colors available / No match", JColor.DARK_RED))
                    : new JScrollPane(spectralTypeTable);

            JPanel spectralTypeInfo = new JPanel(new GridLayout(2, 1));
            spectralTypeInfo.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Spectral type lookup", TitledBorder.LEFT, TitledBorder.TOP
            ));
            spectralTypeInfo.setPreferredSize(new Dimension(425, 375));
            spectralTypeInfo.add(spectralTypePanel);

            JPanel spectralTypeNote = new JPanel();
            spectralTypeNote.setLayout(new BoxLayout(spectralTypeNote, BoxLayout.Y_AXIS));

            spectralTypeNote.add(new JLabel("Note that for some colors, results may be contradictory, as they may fit"));
            spectralTypeNote.add(new JLabel("to early type as well to late type stars."));
            spectralTypeNote.add(new JLabel("The more colors match, the better the results, in general."));
            spectralTypeNote.add(new JLabel("Be aware that this feature only returns approximate results."));
            spectralTypeNote.add(new JLabel(" "));
            spectralTypeNote.add(new JLabel("The feature uses Eric Mamajek's spectral type lookup table:"));
            String hyperlink = "http://www.pas.rochester.edu/~emamajek/EEM_dwarf_UBVIJHK_colors_Teff.txt";
            spectralTypeNote.add(createHyperlink("A Modern Mean Dwarf Stellar Color & Effective Temperature Sequence", hyperlink));
            spectralTypeNote.add(new JLabel("Version in use: 2019.3.22"));
            spectralTypeNote.add(new JLabel("The table is also available in the " + LookupTab.TAB_NAME + " tab: " + LookupTable.MAIN_SEQUENCE.name()));

            spectralTypeInfo.add(spectralTypeNote);

            bottomPanel.add(spectralTypeInfo);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    public void removeAndRecreateCenterPanel() {
        if (centerPanel != null) {
            mainPanel.remove(centerPanel);
        }
        centerPanel = new JPanel(new GridLayout(2, 2));
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

    private void alignCatalogColumns(JTable table, CatalogEntry entry) {
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        List<CatalogElement> elements = entry.getCatalogElements();
        for (int i = 0; i < elements.size(); i++) {
            Alignment alignment = elements.get(i).getAlignment();
            table.getColumnModel().getColumn(i).setCellRenderer(alignment.equals(Alignment.LEFT) ? leftRenderer : rightRenderer);
        }
    }

    private TableRowSorter createCatalogTableSorter(DefaultTableModel defaultTableModel, CatalogEntry entry) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(defaultTableModel);
        List<CatalogElement> elements = entry.getCatalogElements();
        for (int i = 0; i < elements.size(); i++) {
            sorter.setComparator(i, elements.get(i).getComparator());
        }
        return sorter;
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

    public double getTargetRa() {
        return targetRa;
    }

    public double getTargetDec() {
        return targetDec;
    }

}
