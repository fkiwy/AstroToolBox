package astro.tool.box.module;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.Urls.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.CollectedObject;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaCatalogEntry;
import astro.tool.box.container.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.container.catalog.GaiaWDCatalogEntry;
import astro.tool.box.container.catalog.NoirlabCatalogEntry;
import astro.tool.box.container.catalog.PanStarrsCatalogEntry;
import astro.tool.box.container.catalog.SdssCatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.container.catalog.TessCatalogEntry;
import astro.tool.box.container.catalog.TwoMassCatalogEntry;
import astro.tool.box.container.catalog.UnWiseCatalogEntry;
import astro.tool.box.container.catalog.VhsCatalogEntry;
import astro.tool.box.container.catalog.WhiteDwarf;
import astro.tool.box.container.lookup.DistanceLookupResult;
import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.function.AstrometricFunctions;
import astro.tool.box.enumeration.BasicDataType;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.service.DistanceLookupService;
import astro.tool.box.service.NameResolverService;
import astro.tool.box.service.SpectralTypeLookupService;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.RowFilter;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.json.JSONArray;
import org.json.JSONObject;

public class ModuleHelper {

    public static final String PGM_NAME = "AstroToolBox";
    public static final String PGM_VERSION = "2.3.5";
    public static final String CONFIG_FILE_URL = "https://drive.google.com/uc?export=download&id=1RYT_nJA7oO6HgoFkLpq0CWqspXCgcp3I";
    public static final String DOWNLOAD_URL = "https://drive.google.com/file/d/";

    public static final String USER_HOME = System.getProperty("user.home");
    public static final String AGN_WARNING = "Possible AGN!";
    public static final String WD_WARNING = "Possible white dwarf!";

    private static final String ERROR_FILE_NAME = "/AstroToolBoxError.txt";
    private static final String ERROR_FILE_PATH = USER_HOME + ERROR_FILE_NAME;

    public static Image getToolBoxImage() {
        ImageIcon icon = new ImageIcon(ModuleHelper.class.getResource("/icons/toolbox.png"));
        return icon.getImage();
    }

    public static Map<String, CatalogEntry> getCatalogInstances() {
        Map<String, CatalogEntry> catalogInstances = new LinkedHashMap<>();

        // Plug in catalogs here
        SimbadCatalogEntry simbadCatalogEntry = new SimbadCatalogEntry();
        catalogInstances.put(simbadCatalogEntry.getCatalogName(), simbadCatalogEntry);
        AllWiseCatalogEntry allWiseCatalogEntry = new AllWiseCatalogEntry();
        catalogInstances.put(allWiseCatalogEntry.getCatalogName(), allWiseCatalogEntry);
        CatWiseCatalogEntry catWiseCatalogEntry = new CatWiseCatalogEntry();
        catalogInstances.put(catWiseCatalogEntry.getCatalogName(), catWiseCatalogEntry);
        UnWiseCatalogEntry unWiseCatalogEntry = new UnWiseCatalogEntry();
        catalogInstances.put(unWiseCatalogEntry.getCatalogName(), unWiseCatalogEntry);
        GaiaCatalogEntry gaiaCatalogEntry = new GaiaCatalogEntry();
        catalogInstances.put(gaiaCatalogEntry.getCatalogName(), gaiaCatalogEntry);
        GaiaDR3CatalogEntry gaiaDR3CatalogEntry = new GaiaDR3CatalogEntry();
        catalogInstances.put(gaiaDR3CatalogEntry.getCatalogName(), gaiaDR3CatalogEntry);
        NoirlabCatalogEntry noirlabCatalogEntry = new NoirlabCatalogEntry();
        catalogInstances.put(noirlabCatalogEntry.getCatalogName(), noirlabCatalogEntry);
        PanStarrsCatalogEntry panStarrsCatalogEntry = new PanStarrsCatalogEntry();
        catalogInstances.put(panStarrsCatalogEntry.getCatalogName(), panStarrsCatalogEntry);
        SdssCatalogEntry sdssCatalogEntry = new SdssCatalogEntry();
        catalogInstances.put(sdssCatalogEntry.getCatalogName(), sdssCatalogEntry);
        VhsCatalogEntry vhsCatalogEntry = new VhsCatalogEntry();
        catalogInstances.put(vhsCatalogEntry.getCatalogName(), vhsCatalogEntry);
        GaiaWDCatalogEntry gaiaWDCatalogEntry = new GaiaWDCatalogEntry();
        catalogInstances.put(gaiaWDCatalogEntry.getCatalogName(), gaiaWDCatalogEntry);
        TwoMassCatalogEntry twoMassCatalogEntry = new TwoMassCatalogEntry();
        catalogInstances.put(twoMassCatalogEntry.getCatalogName(), twoMassCatalogEntry);
        TessCatalogEntry tessCatalogEntry = new TessCatalogEntry();
        catalogInstances.put(tessCatalogEntry.getCatalogName(), tessCatalogEntry);

        return catalogInstances;
    }

    public static JLabel createHyperlink(String label, String uri) {
        JLabel hyperlink = new JLabel(label);
        hyperlink.setForeground(JColor.LINK_BLUE.val);
        hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hyperlink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(uri));
                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hyperlink.setText(html("<a href=''>" + label + "</a>"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hyperlink.setText(label);
            }
        });
        return hyperlink;
    }

    public static void showInfoDialog(JFrame baseFrame, String message) {
        JOptionPane.showMessageDialog(baseFrame, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarnDialog(JFrame baseFrame, String message) {
        JOptionPane.showMessageDialog(baseFrame, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void showErrorDialog(JFrame baseFrame, String message) {
        JOptionPane.showMessageDialog(baseFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showScrollableErrorDialog(JFrame baseFrame, String message) {
        JOptionPane.showMessageDialog(baseFrame, createMessagePanel(message), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showExceptionDialog(JFrame baseFrame, Exception error) {
        writeErrorLog(error);
        JOptionPane.showMessageDialog(baseFrame, createMessagePanel(formatError(error)), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void writeErrorLog(Exception error) {
        writeLogEntry(formatError(error));
    }

    public static void writeMessageLog(String message) {
        writeLogEntry(formatMessage(message));
    }

    private static void writeLogEntry(String entry) {
        try {
            Files.write(Paths.get(ERROR_FILE_PATH), entry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
        }
    }

    private static JScrollPane createMessagePanel(String message) {
        JTextPane textPane = new JTextPane();
        textPane.setText(message);
        textPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(BorderFactory.createEtchedBorder());
        scrollPane.setPreferredSize(new Dimension(700, 300));
        return scrollPane;
    }

    public static boolean showConfirmDialog(JFrame baseFrame, String message) {
        int option = JOptionPane.showConfirmDialog(baseFrame, message, "Confimation", JOptionPane.OK_CANCEL_OPTION);
        return option == JOptionPane.YES_OPTION;
    }

    public static String bold(String text) {
        return html("<b>" + text + "</b>");
    }

    public static String red(String text) {
        return html("<span style=color:red>" + text + "</span>");
    }

    public static String html(String text) {
        return "<html>" + text + "</html>";
    }

    public static JLabel createHeaderLabel(String text) {
        JLabel header = new JLabel(text);
        header.setBorder(new EmptyBorder(0, 5, 0, 0));
        header.setBackground(Color.GRAY.brighter());
        header.setForeground(Color.BLACK);
        header.setOpaque(true);
        return header;
    }

    public static JCheckBox createHeaderBox(String text) {
        JCheckBox box = new JCheckBox(text);
        box.setBackground(Color.GRAY.brighter());
        box.setForeground(Color.BLACK);
        return box;
    }

    public static JLabel createMessageLabel() {
        return createLabel("", JColor.DARK_GREEN);
    }

    public static JLabel createLabel(Object text, JColor color) {
        JLabel label = new JLabel(text.toString());
        label.setForeground(color.val);
        return label;
    }

    public static Border createEtchedBorder(String boderTitle) {
        return createEtchedBorder(boderTitle, null);
    }

    public static Border createEtchedBorder(String boderTitle, Color titleColor) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), boderTitle, TitledBorder.LEFT, TitledBorder.TOP, null, titleColor
        );
    }

    public static Border createEmptyBorder(String boderTitle) {
        return createEmptyBorder(boderTitle, null);
    }

    public static Border createEmptyBorder(String boderTitle, Color titleColor) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(), boderTitle, TitledBorder.LEFT, TitledBorder.TOP, null, titleColor
        );
    }

    public static NumberPair getCoordinates(String coords) {
        coords = coords.replace('−', '-');
        Pattern pattern = Pattern.compile(".*[a-zA-Z]+.*");
        Matcher matcher = pattern.matcher(coords);
        if (matcher.matches()) {
            try {
                NameResolverService nameResolverService = new NameResolverService();
                coords = nameResolverService.getCoordinatesByName(coords);
            } catch (Exception ex) {
                coords = coords.replaceAll("[^\\d .-]", "");
            }
        }
        String[] parts = splitCoordinates(coords);
        double ra = toDouble(parts[0].trim());
        double dec = toDouble(parts[1].trim());
        return new NumberPair(ra, dec);
    }

    private static String[] splitCoordinates(String coords) {
        coords = convertToDecimalCoords(coords);
        return coords.trim().replaceAll("[,;]", " ").split("\\s+");
    }

    private static String convertToDecimalCoords(String coords) {
        String[] parts = coords.replaceAll("[:hdms°'\"]", " ").split("\\s+");
        if (parts.length == 6) {
            String ra = "";
            for (int i = 0; i < 3; i++) {
                ra += parts[i] + " ";
            }
            String dec = "";
            for (int i = 3; i < 6; i++) {
                dec += parts[i] + " ";
            }
            NumberPair decCoords = AstrometricFunctions.convertToDecimalCoords(ra, dec);
            return roundTo7DecNZ(decCoords.getX()) + " " + roundTo7DecNZ(decCoords.getY());
        }
        return coords;
    }

    public static void copyCoordsToClipboard(double degRA, double degDE) {
        String coordsToCopy = roundTo7DecNZ(degRA) + " " + roundTo7DecNZ(degDE);
        copyToClipboard(coordsToCopy);
    }

    public static void copyToClipboard(String toCopy) {
        StringSelection stringSelection = new StringSelection(toCopy);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public static void addEmptyCatalogElement(JPanel detailPanel) {
        addLabelToPanel(new CatalogElement(), detailPanel);
        addLabelToPanel(new CatalogElement(), detailPanel);
    }

    public static void addLabelToPanel(CatalogElement element, JPanel panel) {
        String name = element.getName();
        JLabel label = new JLabel(name == null ? "" : name + " = ", JLabel.RIGHT);
        //if (element.isOnFocus()) {
        //    label.setOpaque(true);
        //    label.setBackground(JColor.WHITE.val);
        //}
        //if (element.isComputed()) {
        //    label.setForeground(JColor.DARK_GREEN.val);
        //}
        //if (element.isFaulty()) {
        //    label.setForeground(JColor.RED.val);
        //}
        if (element.getToolTip() != null) {
            label.setToolTipText(html(element.getToolTip()));
        }
        panel.add(label);
    }

    public static void addFieldToPanel(CatalogElement element, JPanel panel) {
        String value = element.getValue();
        boolean hasToolTip = element.getToolTip() != null;
        JTextField field = new JTextField(value == null ? "" : value + (hasToolTip ? " (*)" : ""));
        //if (element.isOnFocus()) {
        //    field.setBackground(JColor.WHITE.val);
        //} else {
        field.setBackground(new JLabel().getBackground());
        //}
        if (element.isComputed()) {
            field.setForeground(JColor.DARK_GREEN.val);
        }
        if (element.isFaulty()) {
            field.setForeground(JColor.RED.val);
        }
        field.setCaretPosition(0);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setEditable(true);
        if (hasToolTip) {
            field.setToolTipText(html(element.getToolTip()));
        }
        panel.add(field);
    }

    public static void alignCatalogColumns(JTable table, CatalogEntry entry) {
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

    public static TableRowSorter createCatalogTableSorter(DefaultTableModel defaultTableModel, CatalogEntry entry) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(defaultTableModel);
        List<CatalogElement> elements = entry.getCatalogElements();
        for (int i = 0; i < elements.size(); i++) {
            sorter.setComparator(i, elements.get(i).getComparator());
        }
        return sorter;
    }

    public static void alignResultColumns(JTable table, List<String[]> rows) {
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        Map<Integer, BasicDataType> types = determineBasicTypes(rows);
        for (int i = 0; i < types.size(); i++) {
            DefaultTableCellRenderer cellRenderer;
            if (BasicDataType.NUMERIC.equals(types.get(i))) {
                cellRenderer = rightRenderer;
            } else {
                cellRenderer = leftRenderer;
            }
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    public static TableRowSorter createResultTableSorter(DefaultTableModel defaultTableModel, List<String[]> rows) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>();
        addComparatorsToTableSorter(sorter, defaultTableModel, rows);
        return sorter;
    }

    public static void addComparatorsToTableSorter(TableRowSorter<TableModel> sorter, DefaultTableModel defaultTableModel, List<String[]> rows) {
        sorter.setModel(defaultTableModel);
        Map<Integer, BasicDataType> types = determineBasicTypes(rows);
        for (int i = 0; i < types.size(); i++) {
            Comparator comparator;
            if (BasicDataType.NUMERIC.equals(types.get(i))) {
                comparator = getDoubleComparator();
            } else {
                comparator = getStringComparator();
            }
            sorter.setComparator(i, comparator);
        }
    }

    private static Map<Integer, BasicDataType> determineBasicTypes(List<String[]> rows) {
        Map<Integer, BasicDataType> types = new HashMap<>();
        rows.forEach((row) -> {
            for (int i = 0; i < row.length; i++) {
                String columnValue = row[i];
                if (!columnValue.isEmpty()) {
                    BasicDataType type = types.get(i);
                    if (type == null) {
                        type = BasicDataType.NONE;
                    }
                    if (isNumeric(columnValue)) {
                        if (!type.equals(BasicDataType.ALPHA_NUMERIC)) {
                            type = BasicDataType.NUMERIC;
                        }
                    } else {
                        if (type.equals(BasicDataType.NUMERIC)) {
                            type = BasicDataType.ALPHA_NUMERIC;
                        }
                    }
                    types.put(i, type);
                }
            }
        });
        return types;
    }

    public static void resizeColumnWidth(JTable table) {
        resizeColumnWidth(table, 300);
    }

    public static void resizeColumnWidth(JTable table, int maxColWidth) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 0; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component component = table.prepareRenderer(renderer, row, column);
                width = Math.max(component.getPreferredSize().width + 1, width);
            }
            if (maxColWidth > 0 && width > maxColWidth) {
                width = maxColWidth; // Max width
            }
            columnModel.getColumn(column).setPreferredWidth(width + 20);
        }
    }

    public static RowFilter getCustomRowFilter(String filterText) {
        return new RowFilter<Object, Object>() {
            @Override
            public boolean include(RowFilter.Entry<? extends Object, ? extends Object> entry) {
                for (int i = entry.getValueCount() - 1; i >= 0; i--) {
                    if (entry.getStringValue(i).toUpperCase().contains(filterText.toUpperCase())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static void alignResultColumns(JTable table) {
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        int i = 0;
        table.getColumnModel().getColumn(i++).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(leftRenderer);
        table.getColumnModel().getColumn(i++).setCellRenderer(leftRenderer);
    }

    public static TableRowSorter createResultTableSorter(DefaultTableModel defaultTableModel) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(defaultTableModel);
        int i = 0;
        sorter.setComparator(i++, getIntegerComparator());
        sorter.setComparator(i++, getIntegerComparator());
        sorter.setComparator(i++, getStringComparator());
        sorter.setComparator(i++, getDoubleComparator());
        sorter.setComparator(i++, getDoubleComparator());
        sorter.setComparator(i++, getDoubleComparator());
        sorter.setComparator(i++, getDoubleComparator());
        sorter.setComparator(i++, getDoubleComparator());
        sorter.setComparator(i++, getStringComparator());
        sorter.setComparator(i++, getDoubleComparator());
        sorter.setComparator(i++, getDoubleComparator());
        sorter.setComparator(i++, getDoubleComparator());
        sorter.setComparator(i++, getStringComparator());
        sorter.setComparator(i++, getStringComparator());
        return sorter;
    }

    public static List<String> lookupSpectralTypes(Map<astro.tool.box.enumeration.Color, Double> colors, SpectralTypeLookupService spectralTypeLookupService, boolean includeColors) {
        List<LookupResult> results = spectralTypeLookupService.lookup(colors);
        List<String> spectralTypes = new ArrayList<>();
        results.forEach(entry -> {
            String spectralType = entry.getSpt();
            if (includeColors) {
                String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
                spectralType += ": " + matchedColor;
            }
            spectralType += "; ";
            spectralTypes.add(spectralType);
        });
        return spectralTypes;
    }

    public static void collectObject(String objectType, CatalogEntry catalogEntry, JFrame baseFrame, SpectralTypeLookupService spectralTypeLookupService, JTable collectionTable) {
        // Collect data
        List<String> spectralTypes = lookupSpectralTypes(catalogEntry.getColors(true), spectralTypeLookupService, true);
        if (catalogEntry instanceof SimbadCatalogEntry) {
            SimbadCatalogEntry simbadEntry = (SimbadCatalogEntry) catalogEntry;
            StringBuilder simbadType = new StringBuilder();
            simbadType.append(simbadEntry.getObjectType());
            if (!simbadEntry.getSpectralType().isEmpty()) {
                simbadType.append(" ").append(simbadEntry.getSpectralType());
            }
            simbadType.append("; ");
            spectralTypes.add(0, simbadType.toString());
        }
        if (catalogEntry instanceof AllWiseCatalogEntry) {
            AllWiseCatalogEntry allWiseEntry = (AllWiseCatalogEntry) catalogEntry;
            if (isAPossibleAGN(allWiseEntry.getW1_W2(), allWiseEntry.getW2_W3())) {
                spectralTypes.add(AGN_WARNING);
            }
        }
        if (catalogEntry instanceof WhiteDwarf) {
            WhiteDwarf entry = (WhiteDwarf) catalogEntry;
            if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                spectralTypes.add(WD_WARNING);
            }
        }
        CollectedObject collectedObject = new CollectedObject.Builder()
                .setDiscoveryDate(LocalDateTime.now())
                .setObjectType(objectType)
                .setCatalogName(catalogEntry.getCatalogName())
                .setRa(catalogEntry.getRa())
                .setDec(catalogEntry.getDec())
                .setSourceId(catalogEntry.getSourceId() + " ")
                .setPlx(catalogEntry.getPlx())
                .setPmra(catalogEntry.getPmra())
                .setPmdec(catalogEntry.getPmdec())
                .setSpectralTypes(spectralTypes)
                .setNotes("").build();

        // Save object
        String objectCollectionPath = getUserSetting(OBJECT_COLLECTION_PATH);
        if (objectCollectionPath == null || objectCollectionPath.isEmpty()) {
            showErrorDialog(baseFrame, "Specify file location of object collection in the Settings tab.");
            return;
        }

        boolean newFile = false;
        File objectCollection = new File(objectCollectionPath);
        if (!objectCollection.exists()) {
            try {
                objectCollection.createNewFile();
                newFile = true;
            } catch (IOException ex) {
                showExceptionDialog(baseFrame, ex);
                return;
            }
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(objectCollection, true))) {
            if (newFile) {
                pw.println(collectedObject.getTitles());
            }
            pw.println(collectedObject.getValues());
        } catch (IOException ex) {
            showExceptionDialog(baseFrame, ex);
            return;
        }

        if (collectionTable != null) {
            DefaultTableModel tableModel = (DefaultTableModel) collectionTable.getModel();
            tableModel.addRow(concatArrays(new String[]{""}, collectedObject.getColumnValues()));
        }
    }

    public static String copyObjectCoordinates(CatalogEntry catalogEntry) {
        StringBuilder toCopy = new StringBuilder();
        toCopy.append(roundTo7DecNZ(catalogEntry.getRa()));
        toCopy.append(" ");
        toCopy.append(roundTo7DecNZ(catalogEntry.getDec()));
        return toCopy.toString();
    }

    public static String copyObjectDigest(CatalogEntry catalogEntry) {
        StringBuilder toCopy = new StringBuilder();
        toCopy.append(catalogEntry.getCatalogName()).append(": ").append(catalogEntry.getSourceId());
        toCopy.append(LINE_SEP);
        toCopy.append("ra=").append(roundTo7DecNZ(catalogEntry.getRa()));
        toCopy.append(" ");
        toCopy.append("dec=").append(roundTo7DecNZ(catalogEntry.getDec()));
        toCopy.append(LINE_SEP);
        if (catalogEntry.getPlx() != 0) {
            toCopy.append("plx=").append(roundTo3DecNZ(catalogEntry.getPlx())).append(" mas");
            toCopy.append(LINE_SEP);
        }
        if (catalogEntry.getParallacticDistance() != 0) {
            toCopy.append("dist=").append(roundTo3DecNZ(catalogEntry.getParallacticDistance())).append(" pc");
            toCopy.append(LINE_SEP);
        }
        if (catalogEntry.getPmra() != 0 || catalogEntry.getPmdec() != 0) {
            toCopy.append("pmra=").append(roundTo3DecNZ(catalogEntry.getPmra()));
            toCopy.append(" ");
            toCopy.append("pmdec=").append(roundTo3DecNZ(catalogEntry.getPmdec()));
            toCopy.append(LINE_SEP);
            toCopy.append("tpm=").append(roundTo3DecNZ(catalogEntry.getTotalProperMotion())).append(" mas/yr");
            toCopy.append(LINE_SEP);
        }
        toCopy.append(catalogEntry.getMagnitudes());
        toCopy.append(LINE_SEP);
        Map<astro.tool.box.enumeration.Color, Double> colors = catalogEntry.getColors(false);
        colors.entrySet().forEach(entry -> {
            double value = entry.getValue();
            if (value != 0) {
                String label = entry.getKey().val;
                toCopy.append(label).append("=").append(roundTo3DecNZ(value));
                toCopy.append(LINE_SEP);
            }
        });
        return toCopy.toString();
    }

    public static String copyObjectInfo(CatalogEntry catalogEntry, List<LookupResult> mainSequenceResults, List<LookupResult> brownDwarfsResults, DistanceLookupService distanceLookupService) {
        StringBuilder toCopy = new StringBuilder();
        toCopy.append(catalogEntry.getEntryData());
        toCopy.append(LINE_SEP).append(LINE_SEP).append("Spectral type evaluation:");
        if (mainSequenceResults != null) {
            toCopy.append(LINE_SEP).append("* Main sequence table:");
            mainSequenceResults.forEach(entry -> {
                toCopy.append(LINE_SEP).append("  + ").append(entry.getColorKey().val).append(" = ").append(roundTo3DecNZ(entry.getColorValue())).append(" -> ").append(entry.getSpt());
            });
        }
        if (brownDwarfsResults != null) {
            toCopy.append(LINE_SEP).append("* Brown dwarfs only:");
            brownDwarfsResults.forEach(entry -> {
                toCopy.append(LINE_SEP).append("  + ").append(entry.getColorKey().val).append(" = ").append(roundTo3DecNZ(entry.getColorValue())).append(" -> ").append(entry.getSpt());
                List<DistanceLookupResult> distanceResults = distanceLookupService.lookup(entry.getSpt(), catalogEntry.getBands());
                toCopy.append(LINE_SEP).append("      Distance evaluation for ").append(entry.getSpt()).append(":");
                distanceResults.forEach(result -> {
                    toCopy.append(LINE_SEP).append("      - ").append(result.getBandKey().val).append(" = ").append(roundTo3DecNZ(result.getBandValue())).append(" -> ").append(roundTo3DecNZ(result.getDistance())).append(" pc");
                });
            });
        }
        return toCopy.toString();
    }

    public static void fillTygoForm(CatalogEntry catalogEntry, CatalogQueryFacade catalogQueryFacade, JFrame baseFrame) {
        StringBuilder params = new StringBuilder();
        // Citizen scientist name
        String userName = getUserSetting("userName", "");
        if (!userName.isEmpty()) {
            params.append("entry.472808084=").append(userName);
        }
        // Enter your email
        String userEmail = getUserSetting("userEmail", "");
        if (!userEmail.isEmpty()) {
            params.append("&entry.1241683426=").append(userEmail);
        }
        // Exact Allwise RA
        params.append("&entry.1014230382=").append(roundTo7DecNZ(catalogEntry.getRa()));
        // Exact Allwise Decimal DEC
        params.append("&entry.504539104=").append(roundTo7DecNZ(catalogEntry.getDec()));
        // Notes
        if (!AllWiseCatalogEntry.class.isInstance(catalogEntry)) {
            params.append("&entry.690953267=").append("Coordinates are from ").append(catalogEntry.getCatalogName());
        }
        // GAIA data
        GaiaDR3CatalogEntry gaiaEntry = new GaiaDR3CatalogEntry();
        gaiaEntry.setRa(catalogEntry.getRa());
        gaiaEntry.setDec(catalogEntry.getDec());
        gaiaEntry.setSearchRadius(5);
        gaiaEntry = (GaiaDR3CatalogEntry) retrieveCatalogEntry(gaiaEntry, catalogQueryFacade, baseFrame);
        if (gaiaEntry != null) {
            // GAIA DR2 pmRA + e_pmRA (mas/y)
            if (gaiaEntry.getPmra() != 0) {
                params.append("&entry.905761395=").append(roundTo3DecNZ(gaiaEntry.getPmra())).append(" ").append(roundTo3DecNZ(gaiaEntry.getPmra_err()));
            }
            // GAIA DR2 pmDE + e_pmDE
            if (gaiaEntry.getPmdec() != 0) {
                params.append("&entry.965290776=").append(roundTo3DecNZ(gaiaEntry.getPmdec())).append(" ").append(roundTo3DecNZ(gaiaEntry.getPmdec_err()));
            }
            // GAIA RV + e_RV
            if (gaiaEntry.getRadvel() != 0) {
                params.append("&entry.702334724=").append(roundTo3DecNZ(gaiaEntry.getRadvel())).append(" ").append(roundTo3DecNZ(gaiaEntry.getRadvel_err()));
            }
            // GAIA DR2 Parallax + e_
            if (gaiaEntry.getPlx() != 0) {
                params.append("&entry.1383168065=").append(roundTo4DecNZ(gaiaEntry.getPlx())).append(" ").append(roundTo4DecNZ(gaiaEntry.getPlx_err()));
            }
            // GAIA ID
            params.append("&entry.1411207241=").append(gaiaEntry.getSourceId());
        }
        try {
            Desktop.getDesktop().browse(new URI(getTygoFormUrl() + params.toString().replace(" ", "%20")));
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static CatalogEntry retrieveCatalogEntry(CatalogEntry catalogQuery, CatalogQueryFacade catalogQueryFacade, JFrame baseFrame) {
        try {
            List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
            catalogEntries.forEach(catalogEntry -> {
                catalogEntry.setTargetRa(catalogQuery.getRa());
                catalogEntry.setTargetDec(catalogQuery.getDec());
            });
            if (!catalogEntries.isEmpty()) {
                catalogEntries.sort(Comparator.comparingDouble(CatalogEntry::getTargetDistance));
                return catalogEntries.get(0);
            }
        } catch (IOException ex) {
            showExceptionDialog(baseFrame, ex);
        }
        return null;
    }

    public static BufferedImage zoom(BufferedImage image, int zoom) {
        zoom = zoom == 0 ? 1 : zoom;
        Image scaledImage = image.getScaledInstance(zoom, zoom, Image.SCALE_DEFAULT);
        BufferedImage zoomedImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = zoomedImage.createGraphics();
        graphics.drawImage(scaledImage, 0, 0, null);
        graphics.dispose();
        return zoomedImage;
    }

    public static BufferedImage convertToGray(BufferedImage colorImage) {
        BufferedImage grayImage = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics graphics = grayImage.getGraphics();
        graphics.drawImage(colorImage, 0, 0, null);
        graphics.dispose();
        return grayImage;
    }

    public static List<JLabel> getNearestZooniverseSubjects(double degRA, double degDE) {
        List<JLabel> subjects = new ArrayList<>();
        try {
            String url = String.format("http://byw.tools/xref?ra=%f&dec=%f", degRA, degDE);
            String response = readResponse(establishHttpConnection(url), "Zooniverse");
            if (!response.isEmpty()) {
                JSONObject obj = new JSONObject(response);
                JSONArray ids = obj.getJSONArray("ids");
                for (Object id : ids) {
                    subjects.add(createHyperlink(id.toString(), "https://www.zooniverse.org/projects/marckuchner/backyard-worlds-planet-9/talk/subjects/" + id));
                }
            }
        } catch (Exception ex) {
        }
        return subjects;
    }

    public static BufferedImage retrieveImage(double targetRa, double targetDec, int size, String survey, String band) {
        BufferedImage bi;
        String imageUrl = String.format("https://irsa.ipac.caltech.edu/applications/finderchart/servlet/api?mode=getImage&RA=%f&DEC=%f&subsetsize=%s&thumbnail_size=large&survey=%s&%s", targetRa, targetDec, roundTo2DecNZ(size / 60f), survey, band);
        try {
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
            bi = ImageIO.read(stream);
        } catch (IOException ex) {
            bi = null;
        }
        return bi;
    }

    public static SortedMap<String, String> getPs1FileNames(double targetRa, double targetDec) throws IOException {
        SortedMap<String, String> fileNames = new TreeMap<>();
        String imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=grizy&sep=comma", targetRa, targetDec);
        String response = readResponse(establishHttpConnection(imageUrl), "Pan-STARRS");
        try (Scanner scanner = new Scanner(response)) {
            String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
            int filter = 0;
            int fileName = 0;
            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i].equals("filter")) {
                    filter = i;
                }
                if (columnNames[i].equals("filename")) {
                    fileName = i;
                }
            }
            while (scanner.hasNextLine()) {
                String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
                fileNames.put(columnValues[filter], columnValues[fileName]);
            }
        }
        return fileNames;
    }

    public static BufferedImage retrievePs1Image(String fileNames, double targetRa, double targetDec, int size) {
        BufferedImage bi;
        String imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?%s&ra=%f&dec=%f&size=%d&output_size=%d&autoscale=99.8", fileNames, targetRa, targetDec, size * 4, 256);
        try {
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
            bi = ImageIO.read(stream);
        } catch (IOException ex) {
            bi = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        }
        return bi;
    }

    public static BufferedImage retrieveDecalsImage(double targetRa, double targetDec, int size, String band) {
        return retrieveDecalsImage(targetRa, targetDec, size, band, "ls-dr9");
    }

    public static BufferedImage retrieveDecalsImage(double targetRa, double targetDec, int size, String band, String layer) {
        BufferedImage bi;
        String imageUrl = String.format("https://www.legacysurvey.org/viewer/jpeg-cutout?ra=%f&dec=%f&pixscale=0.27&layer=%s&size=%d&bands=%s", targetRa, targetDec, layer, size * 4, band);
        try {
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
            bi = ImageIO.read(stream);
            bi = zoom(bi, 256);
        } catch (IOException ex) {
            bi = null;
        }
        return bi;
    }

    public static String[] concatArrays(String[] arg1, String[] arg2) {
        int length = arg1.length + arg2.length;
        String[] array = new String[length];
        System.arraycopy(arg1, 0, array, 0, arg1.length);
        System.arraycopy(arg2, 0, array, arg1.length, arg2.length);
        return array;
    }

    public static String formatError(Exception error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print(LocalDateTime.now().toString() + " ");
        error.printStackTrace(pw);
        return sw.toString();
    }

    public static String formatMessage(String message) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print(LocalDateTime.now().toString() + " ");
        pw.println(message);
        return sw.toString();
    }

}
