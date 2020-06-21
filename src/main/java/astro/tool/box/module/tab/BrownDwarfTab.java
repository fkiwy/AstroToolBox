package astro.tool.box.module.tab;

import static astro.tool.box.util.Constants.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaCatalogEntry;
import astro.tool.box.container.lookup.BrownDwarfLookupEntry;
import astro.tool.box.container.lookup.DistanceLookupResult;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.LookupTable;
import astro.tool.box.service.DistanceLookupService;
import astro.tool.box.service.SpectralTypeLookupService;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumnModel;

public class BrownDwarfTab {

    public static final String TAB_NAME = "M-L-T-Y Dwarfs";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;

    private final SpectralTypeLookupService spectralTypeLookupService;
    private final DistanceLookupService distanceLookupService;

    private JPanel spectralTypeLookup;
    private CatalogEntry selectedEntry;

    public BrownDwarfTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        InputStream input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new BrownDwarfLookupEntry(line.split(SPLIT_CHAR, 22));
            }).collect(Collectors.toList());
            spectralTypeLookupService = new SpectralTypeLookupService(entries);
            distanceLookupService = new DistanceLookupService(entries);
        }
    }

    public void init() {
        try {
            spectralTypeLookup = new JPanel(new FlowLayout(FlowLayout.LEFT));
            spectralTypeLookup.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Spectral type lookup for M, L, T & Y dwarfs", TitledBorder.LEFT, TitledBorder.TOP
            ));

            JPanel lookupResult = new JPanel();
            lookupResult.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Spectral type evaluation", TitledBorder.LEFT, TitledBorder.TOP
            ));
            lookupResult.setLayout(new BoxLayout(lookupResult, BoxLayout.Y_AXIS));
            lookupResult.setPreferredSize(new Dimension(500, 300));
            spectralTypeLookup.add(lookupResult);

            tabbedPane.addChangeListener((ChangeEvent evt) -> {
                removeDistanceLookupResult();
                JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    lookupResult.removeAll();
                    selectedEntry = catalogQueryTab.getSelectedEntry();
                    if (selectedEntry == null) {
                        lookupResult.add(createLabel("No catalog entry selected in the " + CatalogQueryTab.TAB_NAME + " tab!", JColor.DARK_RED));
                        return;
                    } else {
                        JPanel entryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        lookupResult.add(entryPanel);
                        String catalogEntry = "for " + selectedEntry.getCatalogName() + ": source id = " + selectedEntry.getSourceId()
                                + " RA = " + roundTo7DecNZ(selectedEntry.getRa()) + " dec = " + roundTo7DecNZ(selectedEntry.getDec());
                        entryPanel.add(new JLabel(catalogEntry));
                        if (selectedEntry instanceof AllWiseCatalogEntry) {
                            AllWiseCatalogEntry entry = (AllWiseCatalogEntry) selectedEntry;
                            if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                                entryPanel.add(createLabel(AGN_WARNING, JColor.DARK_RED));
                            }
                        }
                        if (selectedEntry instanceof GaiaCatalogEntry) {
                            GaiaCatalogEntry entry = (GaiaCatalogEntry) selectedEntry;
                            if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                                entryPanel.add(createLabel(WD_WARNING, JColor.DARK_RED));
                            }
                        }
                    }
                    List<LookupResult> results = spectralTypeLookupService.lookup(selectedEntry.getColors());
                    displaySpectralTypes(results, lookupResult);
                }
            });

            tabbedPane.addTab(TAB_NAME, spectralTypeLookup);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void displaySpectralTypes(List<LookupResult> results, JPanel lookupResult) {
        List<String[]> resultRows = new ArrayList<>();
        results.forEach(entry -> {
            String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
            String resultValues = entry.getSpt() + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + "," + roundTo3DecLZ(entry.getGap());
            resultRows.add(resultValues.split(",", 4));
        });

        String titles = "spt,matched colors,nearest color,gap to nearest color";
        String[] columns = titles.split(",", 4);
        Object[][] rows = new Object[][]{};
        JTable spectralTypeTable = new JTable(resultRows.toArray(rows), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        alignResultColumns(spectralTypeTable, resultRows);
        spectralTypeTable.setAutoCreateRowSorter(true);
        spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        spectralTypeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumnModel columnModel = spectralTypeTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(100);

        spectralTypeTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                String spt = (String) spectralTypeTable.getValueAt(spectralTypeTable.getSelectedRow(), 0);

                JPanel distanceLookupResult = new JPanel();
                distanceLookupResult.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "Distance evaluation", TitledBorder.LEFT, TitledBorder.TOP
                ));
                distanceLookupResult.setLayout(new BoxLayout(distanceLookupResult, BoxLayout.Y_AXIS));
                distanceLookupResult.setPreferredSize(new Dimension(500, 300));

                JPanel entryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                distanceLookupResult.add(entryPanel);
                String catalogEntry = "for spectral type " + spt;
                entryPanel.add(new JLabel(catalogEntry));

                removeDistanceLookupResult();
                spectralTypeLookup.add(distanceLookupResult);

                List<DistanceLookupResult> distanceResults = distanceLookupService.lookup(spt, selectedEntry.getBands());
                displayDistances(distanceResults, distanceLookupResult);
                baseFrame.setVisible(true);
            }
        });

        JScrollPane spectralTypePanel = resultRows.isEmpty()
                ? new JScrollPane(createLabel("No colors available / No match", JColor.DARK_RED))
                : new JScrollPane(spectralTypeTable);
        spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder()
        ));
        lookupResult.add(spectralTypePanel);

        JPanel remarks = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remarks.setPreferredSize(new Dimension(100, 200));
        lookupResult.add(remarks);
        remarks.add(new JLabel("M, L, T & Y dwarfs lookup table is available in the " + LookupTab.TAB_NAME + " tab: " + LookupTable.MLTY_DWARFS));
        remarks.add(new JLabel("Lookup is performed with the following colors, if available:"));
        remarks.add(new JLabel("W1-W2, CH1-CH2, J-W2, J-K, g-r, r-i and absolute Gmag"));
    }

    private void displayDistances(List<DistanceLookupResult> results, JPanel lookupResult) {
        List<String[]> resultRows = new ArrayList<>();
        results.forEach(entry -> {
            String matchedBand = entry.getBandKey().val + "=" + roundTo3DecNZ(entry.getBandValue());
            String resutValues = roundTo3Dec(entry.getDistance()) + "," + matchedBand;
            resultRows.add(resutValues.split(",", 2));
        });

        String titles = "distance (pc),matched bands";
        String[] columns = titles.split(",", 2);
        Object[][] rows = new Object[][]{};
        JTable distanceTable = new JTable(resultRows.toArray(rows), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        alignResultColumns(distanceTable, resultRows);
        distanceTable.setAutoCreateRowSorter(true);
        distanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = distanceTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(100);

        JScrollPane distancePanel = resultRows.isEmpty()
                ? new JScrollPane(createLabel("No bands available / No match", JColor.DARK_RED))
                : new JScrollPane(distanceTable);
        distancePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder()
        ));
        lookupResult.add(distancePanel);

        JPanel remarks = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remarks.setPreferredSize(new Dimension(100, 200));
        lookupResult.add(remarks);
        remarks.add(new JLabel("Distance evaluation is performed using distance modulus for the following bands,"));
        remarks.add(new JLabel("if available: r, i, z, y, J, H, K, W1, W2 and G"));
        remarks.add(new JLabel("Absolute magnitudes are from M, L, T & Y dwarfs lookup table."));
    }

    private void removeDistanceLookupResult() {
        int count = spectralTypeLookup.getComponentCount();
        if (count > 1) {
            spectralTypeLookup.remove(count - 1);
        }
    }

}
