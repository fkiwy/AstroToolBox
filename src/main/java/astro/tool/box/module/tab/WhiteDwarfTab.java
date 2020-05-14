package astro.tool.box.module.tab;

import static astro.tool.box.util.Constants.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
import astro.tool.box.container.lookup.WhiteDwarfLookupEntry;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.LookupTable;
import astro.tool.box.service.SpectralTypeLookupService;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;

public class WhiteDwarfTab {

    public static final String TAB_NAME = "White Dwarfs";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;

    private final SpectralTypeLookupService whiteDwarfPureHLookupService;
    private final SpectralTypeLookupService whiteDwarfPureHeLookupService;
    private final SpectralTypeLookupService whiteDwarfMixLookupService;

    public WhiteDwarfTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        InputStream input;

        input = getClass().getResourceAsStream("/WhiteDwarfPureHLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new WhiteDwarfLookupEntry(line.split(SPLIT_CHAR, 15));
            }).collect(Collectors.toList());
            whiteDwarfPureHLookupService = new SpectralTypeLookupService(entries);
        }

        input = getClass().getResourceAsStream("/WhiteDwarfPureHeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new WhiteDwarfLookupEntry(line.split(SPLIT_CHAR, 15));
            }).collect(Collectors.toList());
            whiteDwarfPureHeLookupService = new SpectralTypeLookupService(entries);
        }

        input = getClass().getResourceAsStream("/WhiteDwarfMixLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new WhiteDwarfLookupEntry(line.split(SPLIT_CHAR, 15));
            }).collect(Collectors.toList());
            whiteDwarfMixLookupService = new SpectralTypeLookupService(entries);
        }
    }

    public void init() {
        try {
            JPanel spectralTypeLookup = new JPanel(new FlowLayout(FlowLayout.LEFT));
            spectralTypeLookup.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Effective temperature lookup for white dwarfs", TitledBorder.LEFT, TitledBorder.TOP
            ));

            JPanel lookupResult = new JPanel();
            lookupResult.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Effective temperatures", TitledBorder.LEFT, TitledBorder.TOP
            ));
            lookupResult.setLayout(new BoxLayout(lookupResult, BoxLayout.Y_AXIS));
            lookupResult.setPreferredSize(new Dimension(500, 600));
            spectralTypeLookup.add(lookupResult);

            JPanel colorInput = new JPanel(new GridLayout(10, 2));
            colorInput.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Manual input", TitledBorder.LEFT, TitledBorder.TOP
            ));
            colorInput.setPreferredSize(new Dimension(200, 275));

            JPanel inputPanel = new JPanel();
            inputPanel.setPreferredSize(new Dimension(200, 610));
            inputPanel.add(colorInput);
            spectralTypeLookup.add(inputPanel);

            colorInput.add(new JLabel("Gmag: ", JLabel.RIGHT));
            JTextField gField = new JTextField();
            colorInput.add(gField);

            colorInput.add(new JLabel("BPmag: ", JLabel.RIGHT));
            JTextField bpField = new JTextField();
            colorInput.add(bpField);

            colorInput.add(new JLabel("RPmag: ", JLabel.RIGHT));
            JTextField rpField = new JTextField();
            colorInput.add(rpField);

            colorInput.add(new JLabel("Bmag: ", JLabel.RIGHT));
            JTextField bField = new JTextField();
            colorInput.add(bField);

            colorInput.add(new JLabel("Vmag: ", JLabel.RIGHT));
            JTextField vField = new JTextField();
            colorInput.add(vField);

            colorInput.add(new JLabel("Jmag: ", JLabel.RIGHT));
            JTextField jField = new JTextField();
            colorInput.add(jField);

            colorInput.add(new JLabel("g_mag: ", JLabel.RIGHT));
            JTextField g_Field = new JTextField();
            colorInput.add(g_Field);

            colorInput.add(new JLabel("r_mag: ", JLabel.RIGHT));
            JTextField r_Field = new JTextField();
            colorInput.add(r_Field);

            colorInput.add(new JLabel("i_mag: ", JLabel.RIGHT));
            JTextField i_Field = new JTextField();
            colorInput.add(i_Field);

            colorInput.add(new JLabel());
            JButton lookupButton = new JButton("Lookup");
            lookupButton.addActionListener((ActionEvent e) -> {
                try {
                    lookupResult.removeAll();
                    Map<Color, Double> colors = new LinkedHashMap<>();
                    colors.put(Color.G_RP, toDouble(gField.getText()) - toDouble(rpField.getText()));
                    colors.put(Color.BP_RP, toDouble(bpField.getText()) - toDouble(rpField.getText()));
                    colors.put(Color.B_V, toDouble(bField.getText()) - toDouble(vField.getText()));
                    colors.put(Color.V_J, toDouble(vField.getText()) - toDouble(jField.getText()));
                    colors.put(Color.g_r, toDouble(g_Field.getText()) - toDouble(r_Field.getText()));
                    colors.put(Color.r_i, toDouble(r_Field.getText()) - toDouble(i_Field.getText()));
                    colors.put(Color.r_J, toDouble(r_Field.getText()) - toDouble(jField.getText()));
                    lookupWhiteDwarfsByColor(lookupResult, colors);
                    baseFrame.setVisible(true);
                } catch (Exception ex) {
                    showErrorDialog(baseFrame, "Invalid color input!");
                }
            });
            colorInput.add(lookupButton);

            tabbedPane.addChangeListener((ChangeEvent evt) -> {
                JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    lookupResult.removeAll();
                    CatalogEntry selectedEntry = catalogQueryTab.getSelectedEntry();
                    if (selectedEntry == null) {
                        lookupResult.add(createLabel("No catalog entry selected in the " + CatalogQueryTab.TAB_NAME + " tab!", JColor.DARK_RED));
                        return;
                    } else {
                        StringBuilder catalogEntry = new StringBuilder("for ")
                                .append(selectedEntry.getCatalogName())
                                .append(": sourceId = ")
                                .append(selectedEntry.getSourceId())
                                .append(" RA = ")
                                .append(selectedEntry.getRa())
                                .append(" dec = ")
                                .append(selectedEntry.getDec());
                        lookupResult.add(new JLabel(catalogEntry.toString()));
                        if (selectedEntry instanceof AllWiseCatalogEntry) {
                            AllWiseCatalogEntry entry = (AllWiseCatalogEntry) selectedEntry;
                            if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                                lookupResult.add(createLabel(AGN_WARNING, JColor.DARK_RED));
                            }
                        }
                    }
                    lookupWhiteDwarfsByColor(lookupResult, selectedEntry.getColors());
                }
            });

            tabbedPane.addTab(TAB_NAME, spectralTypeLookup);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void lookupWhiteDwarfsByColor(JPanel lookupResult, Map<Color, Double> colors) {
        List<SpectralTypeLookupResult> whiteDwarfPureHResults = whiteDwarfPureHLookupService.lookup(colors);
        displaySpectralTypes(whiteDwarfPureHResults, lookupResult, "WD type: Pure H");
        List<SpectralTypeLookupResult> whiteDwarfPureHeResults = whiteDwarfPureHeLookupService.lookup(colors);
        displaySpectralTypes(whiteDwarfPureHeResults, lookupResult, "WD type: Pure He");
        List<SpectralTypeLookupResult> whiteDwarfMixResults = whiteDwarfMixLookupService.lookup(colors);
        displaySpectralTypes(whiteDwarfMixResults, lookupResult, "WD type: Mix He/H=0.1");

        lookupResult.add(new JLabel("White dwarfs lookup tables are available in the " + LookupTab.TAB_NAME + " tab:"));
        lookupResult.add(new JLabel(LookupTable.WHITE_DWARFS_PURE_H + ", " + LookupTable.WHITE_DWARFS_PURE_HE + ", " + LookupTable.WHITE_DWARFS_MIX));
        lookupResult.add(new JLabel("Lookup is performed with the following colors, if available:"));
        lookupResult.add(new JLabel("G-RP, BP-RP, B-V, V-J, g-r, r-i and r-J"));
    }

    private void displaySpectralTypes(List<SpectralTypeLookupResult> results, JPanel lookupResult, String panelTitle) {
        List<String[]> spectralTypes = new ArrayList<>();
        results.forEach(entry -> {
            String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
            String spectralType = entry.getTeff() + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + "," + roundTo3DecLZ(entry.getGap());
            spectralTypes.add(spectralType.split(",", 4));
        });

        String titles = "teff,matched colors,nearest color,gap to nearest color";
        String[] columns = titles.split(",", 4);
        Object[][] rows = new Object[][]{};
        JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        alignResultColumns(spectralTypeTable, spectralTypes);
        spectralTypeTable.setAutoCreateRowSorter(true);
        spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        spectralTypeTable.setCellSelectionEnabled(false);
        resizeColumnWidth(spectralTypeTable);

        JScrollPane spectralTypePanel = spectralTypes.isEmpty()
                ? new JScrollPane(createLabel("No colors available / No match", JColor.DARK_RED))
                : new JScrollPane(spectralTypeTable);
        spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), panelTitle, TitledBorder.LEFT, TitledBorder.TOP
        ));
        lookupResult.add(spectralTypePanel);
    }

}
