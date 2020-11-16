package astro.tool.box.module;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.CollectedObject;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaCatalogEntry;
import astro.tool.box.container.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.container.catalog.GaiaWDCatalogEntry;
import astro.tool.box.container.catalog.PanStarrsCatalogEntry;
import astro.tool.box.container.catalog.SDSSCatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.container.catalog.SpitzerCatalogEntry;
import astro.tool.box.container.catalog.TwoMassCatalogEntry;
import astro.tool.box.container.catalog.UnWiseCatalogEntry;
import astro.tool.box.container.catalog.VHSCatalogEntry;
import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.function.AstrometricFunctions;
import astro.tool.box.enumeration.BasicDataType;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.service.NameResolverService;
import astro.tool.box.service.SpectralTypeLookupService;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
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
import static java.lang.Math.round;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.border.Border;
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
    public static final String PGM_VERSION = "v2.2.3";

    public static final String USER_HOME = System.getProperty("user.home");
    public static final String AGN_WARNING = "Possible AGN!";
    public static final String WD_WARNING = "Possible white dwarf!";

    private static final String ERROR_FILE_NAME = "/AstroToolBoxError.txt";
    private static final String ERROR_FILE_PATH = USER_HOME + ERROR_FILE_NAME;

    public static final LocalDate GAIA_DR3_RELEASE_DATE = LocalDate.of(2020, Month.DECEMBER, 3);

    public static Image getToolBoxImage() {
        ImageIcon icon = new ImageIcon(ModuleHelper.class.getResource("/icons/toolbox.png"));
        return icon.getImage();
    }

    public static Map<String, CatalogEntry> getCatalogInstances() {
        Map<String, CatalogEntry> catalogInstances = new LinkedHashMap<>();

        // Plug in catalogs here
        SimbadCatalogEntry simbadCatalogEntry = new SimbadCatalogEntry();
        catalogInstances.put(simbadCatalogEntry.getCatalogName(), simbadCatalogEntry);
        GaiaCatalogEntry gaiaCatalogEntry = new GaiaCatalogEntry();
        catalogInstances.put(gaiaCatalogEntry.getCatalogName(), gaiaCatalogEntry);
        if (LocalDate.now().isAfter(GAIA_DR3_RELEASE_DATE)) {
            GaiaDR3CatalogEntry gaiaDR3CatalogEntry = new GaiaDR3CatalogEntry();
            catalogInstances.put(gaiaDR3CatalogEntry.getCatalogName(), gaiaDR3CatalogEntry);
        }
        AllWiseCatalogEntry allWiseCatalogEntry = new AllWiseCatalogEntry();
        catalogInstances.put(allWiseCatalogEntry.getCatalogName(), allWiseCatalogEntry);
        CatWiseCatalogEntry catWiseCatalogEntry = new CatWiseCatalogEntry();
        catalogInstances.put(catWiseCatalogEntry.getCatalogName(), catWiseCatalogEntry);
        UnWiseCatalogEntry unWiseCatalogEntry = new UnWiseCatalogEntry();
        catalogInstances.put(unWiseCatalogEntry.getCatalogName(), unWiseCatalogEntry);
        PanStarrsCatalogEntry panStarrsCatalogEntry = new PanStarrsCatalogEntry();
        catalogInstances.put(panStarrsCatalogEntry.getCatalogName(), panStarrsCatalogEntry);
        SDSSCatalogEntry sdssCatalogEntry = new SDSSCatalogEntry();
        catalogInstances.put(sdssCatalogEntry.getCatalogName(), sdssCatalogEntry);
        TwoMassCatalogEntry twoMassCatalogEntry = new TwoMassCatalogEntry();
        catalogInstances.put(twoMassCatalogEntry.getCatalogName(), twoMassCatalogEntry);
        VHSCatalogEntry vhsCatalogEntry = new VHSCatalogEntry();
        catalogInstances.put(vhsCatalogEntry.getCatalogName(), vhsCatalogEntry);
        GaiaWDCatalogEntry gaiaWDCatalogEntry = new GaiaWDCatalogEntry();
        catalogInstances.put(gaiaWDCatalogEntry.getCatalogName(), gaiaWDCatalogEntry);
        SpitzerCatalogEntry spitzerCatalogEntry = new SpitzerCatalogEntry();
        catalogInstances.put(spitzerCatalogEntry.getCatalogName(), spitzerCatalogEntry);

        return catalogInstances;
    }

    public static JLabel createHyperlink(String label, String uri) {
        JLabel hyperlink = new JLabel(label);
        hyperlink.setForeground(JColor.DARK_BLUE.val);
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

    public static void showExceptionDialog(JFrame baseFrame, Exception ex) {
        writeErrorLog(ex);
        JOptionPane.showMessageDialog(baseFrame, createMessagePanel(getStackTrace(ex)), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void writeErrorLog(Exception ex) {
        try {
            Files.write(Paths.get(ERROR_FILE_PATH), getStackTrace(ex).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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

    public static String header(String text) {
        return html("<span style='background:#BEBEBE;color:#880000'>&nbsp;<u>" + text + "</u>&nbsp;</span>");
    }

    public static String html(String text) {
        return "<html>" + text + "</html>";
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
        return BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(), boderTitle, TitledBorder.LEFT, TitledBorder.TOP
        );
    }

    public static NumberPair getCoordinates(String coords) {
        coords = coords.replace('−', '-');
        Pattern pattern = Pattern.compile(".*[a-zA-Z]+.*");
        Matcher matcher = pattern.matcher(coords);
        if (matcher.matches()) {
            NameResolverService nameResolverService = new NameResolverService();
            try {
                coords = nameResolverService.getCoordinatesByName(coords);
            } catch (Exception ex) {
                showErrorDialog(null, ex.getMessage());
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
        StringSelection stringSelection = new StringSelection(coordsToCopy);
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
        if (element.isComputed()) {
            label.setForeground(JColor.DARKER_GREEN.val);
        }
        if (element.isFaulty()) {
            label.setForeground(JColor.DARK_RED.val);
        }
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
            field.setForeground(JColor.DARKER_GREEN.val);
        }
        if (element.isFaulty()) {
            field.setForeground(JColor.DARK_RED.val);
        }
        field.setCaretPosition(0);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setEditable(true);
        if (hasToolTip) {
            field.setToolTipText(html(element.getToolTip()));
        }
        panel.add(field);
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
        TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 0; // Min width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component component = table.prepareRenderer(renderer, row, column);
                width = Math.max(component.getPreferredSize().width + 1, width);
            }
            if (width > 300) {
                width = 300; // Max width
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

    public static void collectObject(String objectType, CatalogEntry catalogEntry, JLabel message, Timer messageTimer, JFrame baseFrame, SpectralTypeLookupService spectralTypeLookupService, JTable collectionTable) {
        // Collect data
        List<String> spectralTypes = lookupSpectralTypes(catalogEntry.getColors(), spectralTypeLookupService, true);
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
        if (catalogEntry instanceof GaiaCatalogEntry) {
            GaiaCatalogEntry entry = (GaiaCatalogEntry) catalogEntry;
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

        message.setText("Object has been added to collection!");
        messageTimer.restart();
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

    public static BufferedImage retrieveImage(double targetRa, double targetDec, int size, String survey, String band) throws IOException {
        BufferedImage bi;
        String imageUrl = String.format("https://irsa.ipac.caltech.edu/applications/finderchart/servlet/api?mode=getImage&RA=%s&DEC=%s&subsetsize=%s&thumbnail_size=large&survey=%s&%s", roundTo6DecNZ(targetRa), roundTo6DecNZ(targetDec), roundTo2DecNZ(size / 60f), survey, band);
        try {
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
            bi = ImageIO.read(stream);
        } catch (IOException ex) {
            bi = null;
        }
        return bi;
    }

    public static BufferedImage retrievePs1Image(String fileNames, double targetRa, double targetDec, int size) throws IOException {
        BufferedImage bi;
        String imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?%s&ra=%f&dec=%f&size=%d&output_size=%d", fileNames, targetRa, targetDec, (int) round(size * 4), 256);
        try {
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
            bi = ImageIO.read(stream);
        } catch (IOException ex) {
            bi = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
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

    public static String[] concatArrays(String[] arg1, String[] arg2) {
        int length = arg1.length + arg2.length;
        String[] array = new String[length];
        System.arraycopy(arg1, 0, array, 0, arg1.length);
        System.arraycopy(arg2, 0, array, arg1.length, arg2.length);
        return array;
    }

    public static String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print(LocalDateTime.now().toString() + " ");
        ex.printStackTrace(pw);
        return sw.toString();
    }

}
