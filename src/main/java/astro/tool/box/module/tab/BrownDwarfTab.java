package astro.tool.box.module.tab;

import static astro.tool.box.util.Constants.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.lookup.BrownDwarfLookupEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.ColorValue;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
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

public class BrownDwarfTab {

    public static final String TAB_NAME = "M-L-T-Y Dwarfs";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;

    private final SpectralTypeLookupService spectralTypeLookupService;

    public BrownDwarfTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        InputStream input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new BrownDwarfLookupEntry(line.split(SPLIT_CHAR, 21));
            }).collect(Collectors.toList());
            spectralTypeLookupService = new SpectralTypeLookupService(entries);
        }
    }

    public void init() {
        try {
            JPanel spectralTypeLookup = new JPanel(new FlowLayout(FlowLayout.LEFT));
            spectralTypeLookup.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Spectral type lookup for M, L, T & Y dwarfs", TitledBorder.LEFT, TitledBorder.TOP
            ));

            JPanel lookupResult = new JPanel();
            lookupResult.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Spectral types", TitledBorder.LEFT, TitledBorder.TOP
            ));
            lookupResult.setLayout(new BoxLayout(lookupResult, BoxLayout.Y_AXIS));
            lookupResult.setPreferredSize(new Dimension(500, 300));
            spectralTypeLookup.add(lookupResult);

            JPanel colorInput = new JPanel(new GridLayout(6, 2));
            colorInput.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Manual input", TitledBorder.LEFT, TitledBorder.TOP
            ));
            colorInput.setPreferredSize(new Dimension(200, 175));

            JPanel inputPanel = new JPanel();
            inputPanel.setPreferredSize(new Dimension(200, 310));
            inputPanel.add(colorInput);
            spectralTypeLookup.add(inputPanel);

            colorInput.add(new JLabel("W1mag: ", JLabel.RIGHT));
            JTextField w1Field = new JTextField("");
            colorInput.add(w1Field);

            colorInput.add(new JLabel("W2mag: ", JLabel.RIGHT));
            JTextField w2Field = new JTextField("");
            colorInput.add(w2Field);

            colorInput.add(new JLabel("Jmag: ", JLabel.RIGHT));
            JTextField jField = new JTextField("");
            colorInput.add(jField);

            colorInput.add(new JLabel("Kmag: ", JLabel.RIGHT));
            JTextField kField = new JTextField("");
            colorInput.add(kField);

            colorInput.add(new JLabel("M Gmag: ", JLabel.RIGHT));
            JTextField m_gField = new JTextField("");
            colorInput.add(m_gField);

            colorInput.add(new JLabel());
            JButton lookupButton = new JButton("Lookup");
            lookupButton.addActionListener((ActionEvent e) -> {
                try {
                    lookupResult.removeAll();
                    Map<Color, Double> colors = new LinkedHashMap<>();
                    colors.put(Color.W1_W2, toDouble(w1Field.getText()) - toDouble(w2Field.getText()));
                    colors.put(Color.J_W2, toDouble(jField.getText()) - toDouble(w2Field.getText()));
                    colors.put(Color.J_K, toDouble(jField.getText()) - toDouble(kField.getText()));
                    colors.put(Color.M_G, toDouble(m_gField.getText()));
                    Map<SpectralTypeLookupResult, Set<ColorValue>> results = spectralTypeLookupService.lookup(colors);
                    displaySpectralTypes(results, lookupResult);
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
                            if (isAPossibleAgn(entry.getW1_W2(), entry.getW2_W3())) {
                                String warning = "W2-W3=" + roundTo3DecNZ(entry.getW2_W3()) + " (> 2.5) " + AGN_WARNING;
                                lookupResult.add(createLabel(warning, JColor.DARK_RED));
                            }
                        }
                    }
                    Map<SpectralTypeLookupResult, Set<ColorValue>> results = spectralTypeLookupService.lookup(selectedEntry.getColors());
                    displaySpectralTypes(results, lookupResult);
                }
            });

            tabbedPane.addTab(TAB_NAME, spectralTypeLookup);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void displaySpectralTypes(Map<SpectralTypeLookupResult, Set<ColorValue>> results, JPanel lookupResult) {
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
            String spectralType = key.getSpt() + "," + matchedColors;
            spectralTypes.add(spectralType.split(",", 2));
        });

        String titles = "spt,matched colors";
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
                ? new JScrollPane(createLabel("No colors available / No match", JColor.DARK_RED))
                : new JScrollPane(spectralTypeTable);
        lookupResult.add(spectralTypePanel);

        lookupResult.add(new JLabel("M, L, T & Y dwarfs lookup table is available in the " + LookupTab.TAB_NAME + " tab: " + LookupTable.MLTY_DWARFS));
        lookupResult.add(new JLabel("Lookup is performed with the following colors, if available:"));
        lookupResult.add(new JLabel("W1-W2, J-W2, J-K and absolute Gmag"));
    }

}
