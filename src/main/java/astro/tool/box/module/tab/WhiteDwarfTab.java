package astro.tool.box.module.tab;

import static astro.tool.box.util.Constants.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaWDCatalogEntry;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.container.lookup.WhiteDwarfLookupEntry;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.LookupTable;
import astro.tool.box.service.EffectiveTemperatureLookupService;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class WhiteDwarfTab {

    public static final String TAB_NAME = "White Dwarfs";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;

    private final EffectiveTemperatureLookupService whiteDwarfPureHLookupService;
    private final EffectiveTemperatureLookupService whiteDwarfPureHeLookupService;

    private CatalogEntry selectedEntry;

    public WhiteDwarfTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        InputStream input;
        input = getClass().getResourceAsStream("/WhiteDwarfPureHLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new WhiteDwarfLookupEntry(line.split(SPLIT_CHAR, 18));
            }).collect(Collectors.toList());
            whiteDwarfPureHLookupService = new EffectiveTemperatureLookupService(entries);
        }
        input = getClass().getResourceAsStream("/WhiteDwarfPureHeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new WhiteDwarfLookupEntry(line.split(SPLIT_CHAR, 18));
            }).collect(Collectors.toList());
            whiteDwarfPureHeLookupService = new EffectiveTemperatureLookupService(entries);
        }
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JPanel resultPanel = new JPanel();
            resultPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Effective temperature evaluation", TitledBorder.LEFT, TitledBorder.TOP
            ));
            resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
            resultPanel.setPreferredSize(new Dimension(600, 700));
            mainPanel.add(resultPanel);

            tabbedPane.addChangeListener((ChangeEvent evt) -> {
                JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    resultPanel.removeAll();
                    selectedEntry = catalogQueryTab.getSelectedEntry();
                    if (selectedEntry == null) {
                        resultPanel.add(createLabel("No catalog entry selected in the " + CatalogQueryTab.TAB_NAME + " tab!", JColor.RED));
                        return;
                    } else {
                        JPanel entryPanel = new JPanel(new GridLayout(0, 1));
                        resultPanel.add(entryPanel);
                        String catalogEntry = "for " + selectedEntry.getCatalogName() + ": source id = " + selectedEntry.getSourceId()
                                + " RA = " + roundTo7DecNZ(selectedEntry.getRa()) + " dec = " + roundTo7DecNZ(selectedEntry.getDec());
                        entryPanel.add(new JLabel(catalogEntry));
                    }
                    performLookup(resultPanel, selectedEntry.getColors(false));
                }
            });

            tabbedPane.addTab(TAB_NAME, mainPanel);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void performLookup(JPanel resultPanel, Map<Color, Double> colors) {
        double teffH = 0, teffHe = 0, loggH = 0, loggHe = 0, massH = 0, massHe = 0;
        if (selectedEntry instanceof GaiaWDCatalogEntry) {
            GaiaWDCatalogEntry entry = (GaiaWDCatalogEntry) selectedEntry;
            teffH = entry.getTeffH();
            teffHe = entry.getTeffHe();
            loggH = entry.getLoggH();
            loggHe = entry.getLoggHe();
            massH = entry.getMassH();
            massHe = entry.getMassHe();
        }

        List<LookupResult> whiteDwarfPureHResults = whiteDwarfPureHLookupService.lookup(colors, teffH, loggH, massH);
        displayTemperatures(whiteDwarfPureHResults, resultPanel, String.format("Pure <span style='color:red'>H</span> - %s: teff H = <span style='color:#1E90FF'>%s</span>; mass H = <span style='color:#1E90FF'>%s</span>; logg H = <span style='color:#1E90FF'>%s</span>", GaiaWDCatalogEntry.CATALOG_NAME, roundTo3DecNZ(teffH), roundTo3DecNZ(massH), roundTo3DecNZ(loggH)));
        List<LookupResult> whiteDwarfPureHeResults = whiteDwarfPureHeLookupService.lookup(colors, teffHe, loggHe, massHe);
        displayTemperatures(whiteDwarfPureHeResults, resultPanel, String.format("Pure <span style='color:red'>He</span> - %s: teff He = <span style='color:#1E90FF'>%s</span>; mass He = <span style='color:#1E90FF'>%s</span>; logg He = <span style='color:#1E90FF'>%s</span>", GaiaWDCatalogEntry.CATALOG_NAME, roundTo3DecNZ(teffHe), roundTo3DecNZ(massHe), roundTo3DecNZ(loggHe)));

        JPanel remarks = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remarks.setPreferredSize(new Dimension(600, 150));
        resultPanel.add(remarks);
        remarks.add(new JLabel("White dwarfs lookup tables are available in the " + LookupTab.TAB_NAME + " tab:"));
        remarks.add(new JLabel(LookupTable.WHITE_DWARFS_PURE_H + " & " + LookupTable.WHITE_DWARFS_PURE_HE + " (*)"));
        remarks.add(new JLabel("Lookup is performed with the following colors, if available: G-RP, BP-RP, B-V, V-J, g-r, r-i, i-z"));
        String hyperlink = "https://vizier.u-strasbg.fr/viz-bin/VizieR?-source=J/A%2BA/565/A11";
        remarks.add(createHyperlink("(*) Gaia photometry for white dwarfs (Carrasco+, 2014)", hyperlink));
    }

    private void displayTemperatures(List<LookupResult> results, JPanel resultPanel, String panelTitle) {
        List<String[]> resultRows = new ArrayList<>();
        results.forEach(entry -> {
            String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
            String resultValues = entry.getTeff() + "," + roundTo3Dec(entry.getMsun()) + "," + roundTo1Dec(entry.getLogG()) + "," + entry.getAge() + ","
                    + matchedColor + "," + roundTo3Dec(entry.getNearest()) + "," + roundTo3DecLZ(entry.getGap());
            resultRows.add(resultValues.split(",", 7));
        });

        String titles = "teff,mass (Msun),logg,age,matched color,nearest color,offset";
        String[] columns = titles.split(",", 7);
        Object[][] rows = new Object[][]{};
        DefaultTableModel defaultTableModel = new DefaultTableModel(resultRows.toArray(rows), columns);
        JTable resultTable = new JTable(defaultTableModel);
        alignResultColumns(resultTable, resultRows);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setRowSorter(createResultTableSorter(defaultTableModel, resultRows));
        resultTable.getRowSorter().toggleSortOrder(0);
        TableColumnModel columnModel = resultTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(50);
        columnModel.getColumn(3).setPreferredWidth(100);
        columnModel.getColumn(4).setPreferredWidth(100);
        columnModel.getColumn(5).setPreferredWidth(75);
        columnModel.getColumn(6).setPreferredWidth(50);

        JScrollPane scrollPanel = resultRows.isEmpty()
                ? new JScrollPane(createLabel("No colors available / No match", JColor.RED))
                : new JScrollPane(resultTable);
        scrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), html(panelTitle), TitledBorder.LEFT, TitledBorder.TOP
        ));
        resultPanel.add(scrollPanel);
    }

}
