package astro.tool.box.module.tab;

import static astro.tool.box.util.Constants.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.ColorValue;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

            JPanel colorInput = new JPanel(new GridLayout(9, 2));
            colorInput.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Manual input", TitledBorder.LEFT, TitledBorder.TOP
            ));
            colorInput.setPreferredSize(new Dimension(200, 250));

            JPanel inputPanel = new JPanel();
            inputPanel.setPreferredSize(new Dimension(200, 610));
            inputPanel.add(colorInput);
            spectralTypeLookup.add(inputPanel);

            colorInput.add(createLabel("Gmag: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField gField = createField("", PLAIN_FONT);
            colorInput.add(gField);

            colorInput.add(createLabel("BPmag: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField bpField = createField("", PLAIN_FONT);
            colorInput.add(bpField);

            colorInput.add(createLabel("RPmag: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField rpField = createField("", PLAIN_FONT);
            colorInput.add(rpField);

            colorInput.add(createLabel("Bmag: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField bField = createField("", PLAIN_FONT);
            colorInput.add(bField);

            colorInput.add(createLabel("Vmag: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField vField = createField("", PLAIN_FONT);
            colorInput.add(vField);

            colorInput.add(createLabel("Jmag: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField jField = createField("", PLAIN_FONT);
            colorInput.add(jField);

            colorInput.add(createLabel("gmag: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField g_Field = createField("", PLAIN_FONT);
            colorInput.add(g_Field);

            colorInput.add(createLabel("rmag: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField r_Field = createField("", PLAIN_FONT);
            colorInput.add(r_Field);

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
                    colors.put(Color.r_J, toDouble(r_Field.getText()) - toDouble(jField.getText()));
                    lookupWhiteDwarfsByColor(lookupResult, colors);
                    baseFrame.setVisible(true);
                } catch (Exception ex) {
                    showErrorDialog(baseFrame, "Invalid color input!");
                }
            });
            colorInput.add(lookupButton);

            ChangeListener changeListener = (ChangeEvent changeEvent) -> {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    lookupResult.removeAll();
                    CatalogEntry selectedEntry = catalogQueryTab.getSelectedEntry();
                    if (selectedEntry == null) {
                        lookupResult.add(createLabel("No catalog entry selected in the " + CatalogQueryTab.TAB_NAME + " tab!", PLAIN_FONT, JColor.DARK_RED.val));
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
                        lookupResult.add(createLabel(catalogEntry.toString(), PLAIN_FONT));
                        if (selectedEntry instanceof AllWiseCatalogEntry) {
                            AllWiseCatalogEntry entry = (AllWiseCatalogEntry) selectedEntry;
                            if (isAPossibleAgn(entry.getW1_W2(), entry.getW2_W3())) {
                                String warning = "W2-W3=" + roundTo3DecNZ(entry.getW2_W3()) + " (> 2.5) " + AGN_WARNING;
                                lookupResult.add(createLabel(warning, PLAIN_FONT, JColor.DARK_RED.val));
                            }
                        }
                    }
                    lookupWhiteDwarfsByColor(lookupResult, selectedEntry.getColors());
                }
            };

            tabbedPane.addChangeListener(changeListener);
            tabbedPane.addTab(TAB_NAME, spectralTypeLookup);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void lookupWhiteDwarfsByColor(JPanel lookupResult, Map<Color, Double> colors) {
        Map<SpectralTypeLookupResult, Set<ColorValue>> whiteDwarfPureHResults = whiteDwarfPureHLookupService.lookup(colors);
        displaySpectralTypes(whiteDwarfPureHResults, lookupResult, "WD type: Pure H");
        Map<SpectralTypeLookupResult, Set<ColorValue>> whiteDwarfPureHeResults = whiteDwarfPureHeLookupService.lookup(colors);
        displaySpectralTypes(whiteDwarfPureHeResults, lookupResult, "WD type: Pure He");
        Map<SpectralTypeLookupResult, Set<ColorValue>> whiteDwarfMixResults = whiteDwarfMixLookupService.lookup(colors);
        displaySpectralTypes(whiteDwarfMixResults, lookupResult, "WD type: Mix He/H=0.1");

        lookupResult.add(createLabel("White dwarfs lookup tables are available in the " + LookupTab.TAB_NAME + " tab under", SMALL_FONT));
        lookupResult.add(createLabel(LookupTable.WHITE_DWARFS_PURE_H + ", " + LookupTable.WHITE_DWARFS_PURE_HE + " and " + LookupTable.WHITE_DWARFS_MIX + ".", SMALL_FONT));
        lookupResult.add(createLabel("Lookup is performed with the following colors, if available: G-RP, BP-RP, B-V, V-J, g-r and r-J.", SMALL_FONT));
    }

    private void displaySpectralTypes(Map<SpectralTypeLookupResult, Set<ColorValue>> results, JPanel lookupResult, String panelTitle) {
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
            String spectralType = key.getTeff() + "," + matchedColors;
            spectralTypes.add(spectralType.split(",", 2));
        });

        String titles = "teff,matched colors";
        String[] columns = titles.split(",", 2);
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
                ? new JScrollPane(createLabel("No colors available / No match", PLAIN_FONT, JColor.DARK_RED.val))
                : new JScrollPane(spectralTypeTable);
        spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), panelTitle, TitledBorder.LEFT, TitledBorder.TOP
        ));
        lookupResult.add(spectralTypePanel);
    }

}
