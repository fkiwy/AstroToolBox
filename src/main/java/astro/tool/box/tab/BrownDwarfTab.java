package astro.tool.box.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.WhiteDwarf;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.DistanceLookupResult;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.lookup.LookupResult;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.LookupTable;
import astro.tool.box.exception.NoExtinctionValuesException;
import astro.tool.box.service.DistanceLookupService;
import astro.tool.box.service.DustExtinctionService;
import astro.tool.box.service.SpectralTypeLookupService;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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

    public static final String TAB_NAME = "M, L & T Dwarfs";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;

    private final SpectralTypeLookupService spectralTypeLookupService;
    private final DistanceLookupService distanceLookupService;
    private final DustExtinctionService dustExtinctionService;

    private JCheckBox dustExtinction;
    private CatalogEntry selectedEntry;

    public BrownDwarfTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        InputStream input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new BrownDwarfLookupEntry(line.split(",", -1));
            }).collect(Collectors.toList());
            spectralTypeLookupService = new SpectralTypeLookupService(entries);
            distanceLookupService = new DistanceLookupService(entries);
            dustExtinctionService = new DustExtinctionService();
        }
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JPanel containerPanel = new JPanel();
            containerPanel.setLayout(new BorderLayout());
            mainPanel.add(containerPanel);

            JPanel spectralTypePanel = new JPanel();
            spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Spectral type evaluation", TitledBorder.LEFT, TitledBorder.TOP
            ));
            spectralTypePanel.setLayout(new BoxLayout(spectralTypePanel, BoxLayout.Y_AXIS));
            spectralTypePanel.setPreferredSize(new Dimension(500, 375));
            containerPanel.add(spectralTypePanel, BorderLayout.PAGE_START);

            JPanel distancePanel = new JPanel();
            distancePanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Distance evaluation", TitledBorder.LEFT, TitledBorder.TOP
            ));
            distancePanel.setLayout(new BoxLayout(distancePanel, BoxLayout.Y_AXIS));
            distancePanel.setPreferredSize(new Dimension(500, 325));
            containerPanel.add(distancePanel, BorderLayout.CENTER);

            JPanel extinctionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            extinctionPanel.setPreferredSize(new Dimension(500, 50));
            containerPanel.add(extinctionPanel, BorderLayout.PAGE_END);

            dustExtinction = new JCheckBox("Consider Galactic dust reddening & extinction for bands u, g, r, i, z, J, H, K, W1 & W2");
            extinctionPanel.add(dustExtinction);

            dustExtinction.addActionListener((ActionEvent evt) -> {
                performLookup(spectralTypePanel, distancePanel);
            });

            tabbedPane.addChangeListener((ChangeEvent evt) -> {
                JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    performLookup(spectralTypePanel, distancePanel);
                }
            });

            tabbedPane.addTab(TAB_NAME, mainPanel);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void performLookup(JPanel spectralTypePanel, JPanel distancePanel) {
        spectralTypePanel.removeAll();
        distancePanel.removeAll();
        distancePanel.repaint();
        selectedEntry = catalogQueryTab.getSelectedEntry();
        if (selectedEntry == null) {
            spectralTypePanel.add(createLabel("No catalog entry selected in the " + CatalogQueryTab.TAB_NAME + " tab!", JColor.RED));
        } else {
            distancePanel.add(createLabel("No spectral type selected in the table above!", JColor.RED));
            JPanel entryPanel = new JPanel(new GridLayout(0, 1));
            spectralTypePanel.add(entryPanel);
            String catalogEntry = "for " + selectedEntry.getCatalogName() + ": source id = " + selectedEntry.getSourceId()
                    + " RA = " + roundTo7DecNZ(selectedEntry.getRa()) + " dec = " + roundTo7DecNZ(selectedEntry.getDec());
            entryPanel.add(new JLabel(catalogEntry));
            if (selectedEntry instanceof AllWiseCatalogEntry) {
                AllWiseCatalogEntry entry = (AllWiseCatalogEntry) selectedEntry;
                if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                    entryPanel.add(createLabel(AGN_WARNING, JColor.RED));
                }
            }
            if (selectedEntry instanceof WhiteDwarf) {
                WhiteDwarf entry = (WhiteDwarf) selectedEntry;
                if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                    entryPanel.add(createLabel(WD_WARNING, JColor.RED));
                }
            }
            if (dustExtinction.isSelected()) {
                baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    Map<String, Double> extinctionsByBand = dustExtinctionService.getExtinctionsByBand(selectedEntry.getRa(), selectedEntry.getDec(), 2.0);
                    selectedEntry = selectedEntry.copy();
                    try {
                        selectedEntry.applyExtinctionCorrection(extinctionsByBand);
                    } catch (NoExtinctionValuesException ex) {
                        entryPanel.add(createLabel("No extinction values for " + selectedEntry.getCatalogName() + " bands.", JColor.RED));
                    }
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    baseFrame.setCursor(Cursor.getDefaultCursor());
                }
            }
            List<LookupResult> results = spectralTypeLookupService.lookup(selectedEntry.getColors(true));
            displaySpectralTypes(results, spectralTypePanel, distancePanel);
        }
    }

    private void displaySpectralTypes(List<LookupResult> results, JPanel spectralTypePanel, JPanel distancePanel) {
        List<String[]> resultRows = new ArrayList<>();
        results.forEach(entry -> {
            String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
            String resultValues = entry.getSpt() + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + "," + roundTo3DecLZ(entry.getGap());
            resultRows.add(resultValues.split(",", -1));
        });

        String titles = "spt,matched color,nearest color,offset";
        String[] columns = titles.split(",", -1);
        Object[][] rows = new Object[][]{};
        JTable resultTable = new JTable(resultRows.toArray(rows), columns);
        alignResultColumns(resultTable, resultRows);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableColumnModel columnModel = resultTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(100);

        resultTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                String spt = (String) resultTable.getValueAt(resultTable.getSelectedRow(), 0);

                distancePanel.removeAll();

                JPanel entryPanel = new JPanel(new GridLayout(2, 1));
                distancePanel.add(entryPanel);
                String catalogEntry = "for spectral type " + spt;
                entryPanel.add(new JLabel(catalogEntry));

                List<DistanceLookupResult> distanceResults = distanceLookupService.lookup(spt, selectedEntry.getBands());
                displayDistances(distanceResults, distancePanel);
            }
        });

        spectralTypePanel.add(new JScrollPane(resultTable));
        if (resultRows.isEmpty()) {
            JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            messagePanel.add(createLabel("No colors available / No match", JColor.RED));
            spectralTypePanel.add(messagePanel);
        }

        JPanel remarks = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remarks.setPreferredSize(new Dimension(500, 200));
        spectralTypePanel.add(remarks);
        remarks.add(new JLabel("M, L & T dwarfs lookup table is available in the " + LookupTab.TAB_NAME + " tab: " + LookupTable.MLT_DWARFS));
        remarks.add(new JLabel("Lookup is performed with the following colors, if available, W1-W2, K-W1, J-K,"));
        remarks.add(new JLabel("g-r, r-i, i-z, i-y, z-y, G-RP, BP-RP, BP-G, M_BP, M_G, M_RP and by using the relations"));
        remarks.add(new JLabel("described in Best et al. (2018), Carnero Rosell et al. (2019), Skrzypek et al. (2015),"));
        remarks.add(new JLabel("Skrzypek et al. (2016) and Kiman et al. (2019)"));
        baseFrame.setVisible(true);
    }

    private void displayDistances(List<DistanceLookupResult> results, JPanel distancePanel) {
        List<String[]> resultRows = new ArrayList<>();
        results.forEach(entry -> {
            String matchedBand = entry.getBandKey().val + "=" + roundTo3DecNZ(entry.getBandValue());
            String distance = roundTo3Dec(entry.getDistance());
            if (entry.getDistanceError() > 0) {
                distance += "±" + roundTo3Dec(entry.getDistanceError());
            }
            String resutValues = distance + "," + matchedBand;
            resultRows.add(resutValues.split(",", -1));
        });

        String titles = "distance (pc),matched bands";
        String[] columns = titles.split(",", -1);
        Object[][] rows = new Object[][]{};
        JTable resultTable = new JTable(resultRows.toArray(rows), columns);
        alignResultColumns(resultTable, resultRows);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = resultTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(100);

        distancePanel.add(new JScrollPane(resultTable));
        if (resultRows.isEmpty()) {
            JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            messagePanel.add(createLabel("No bands available / No match", JColor.RED));
            distancePanel.add(messagePanel);
        }

        JPanel remarks = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remarks.setPreferredSize(new Dimension(500, 150));
        distancePanel.add(remarks);
        remarks.add(new JLabel("Distance evaluation is performed using distance modulus for the following bands,"));
        remarks.add(new JLabel("if available: W1, W2, W3, J, H, K, g, r, i, z, y, G, RP"));
        remarks.add(new JLabel("Absolute magnitudes are from the M, L & T dwarfs lookup table."));
        baseFrame.setVisible(true);
    }

}