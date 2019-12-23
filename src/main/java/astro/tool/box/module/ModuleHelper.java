package astro.tool.box.module;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.function.AstrometricFunctions;
import astro.tool.box.enumeration.BasicDataType;
import astro.tool.box.enumeration.JColor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class ModuleHelper {

    public static final String PGM_NAME = "AstroToolBox";
    public static final String PGM_VERSION = "v1.1b";

    public static final String USER_HOME = System.getProperty("user.home");
    public static final String HELP_EMAIL = "AstroToolSet@gmail.com";
    public static final String AGN_WARNING = "Possible AGN?";

    public static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    public static final Font PLAIN_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font PLAIN_BOLD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    public static final Font SMALL_BOLD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    private static final String ERROR_FILE_NAME = "/AstroToolBoxError.txt";
    private static final String ERROR_FILE_PATH = USER_HOME + ERROR_FILE_NAME;

    public static Image getToolBoxImage() {
        ImageIcon icon = new ImageIcon(ModuleHelper.class.getResource("/icons/toolbox.png"));
        return icon.getImage();
    }

    public static JLabel createHyperlink(String label, String uri) {
        JLabel hyperlink = new JLabel(label);
        hyperlink.setFont(SMALL_FONT);
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
                hyperlink.setText("<html><a href=''>" + label + "</a></html>");
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
        try {
            Files.write(Paths.get(ERROR_FILE_PATH), getStackTrace(ex).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
        }
        JOptionPane.showMessageDialog(baseFrame, createMessagePanel(getStackTrace(ex)), "Error", JOptionPane.ERROR_MESSAGE);
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

    public static JTextField createField(Object text, Font font) {
        return createField(text, font, 0);
    }

    public static JTextField createField(Object text, Font font, int cols) {
        JTextField field = cols == 0 ? new JTextField() : new JTextField(cols);
        field.setText(text.toString());
        field.setFont(font);
        field.setEditable(true);
        return field;
    }

    public static JLabel createLabel(Object text, Font font) {
        JLabel label = new JLabel(text.toString());
        label.setFont(font);
        return label;
    }

    public static JLabel createLabel(Object text, Font font, int alignment) {
        JLabel label = new JLabel(text.toString(), alignment);
        label.setFont(font);
        return label;
    }

    public static JLabel createLabel(Object text, Font font, Color color) {
        JLabel label = new JLabel(text.toString());
        label.setForeground(color);
        label.setFont(font);
        return label;
    }

    public static String underLine(String text) {
        return "<html><u>" + text + "</u></html>";
    }

    public static Border createEtchedBorder(String boderTitle) {
        return createEtchedBorder(boderTitle, null, null);
    }

    public static Border createEtchedBorder(String boderTitle, Font titleFont) {
        return createEtchedBorder(boderTitle, titleFont, null);
    }

    public static Border createEtchedBorder(String boderTitle, Font titleFont, Color titleColor) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), boderTitle, TitledBorder.LEFT, TitledBorder.TOP, titleFont, titleColor
        );
    }

    public static Border createEmptyBorder(String boderTitle, Font titleFont) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(), boderTitle, TitledBorder.LEFT, TitledBorder.TOP, titleFont
        );
    }

    public static NumberPair getCoordinates(String coords) {
        String[] parts = splitCoordinates(coords);
        double ra = toDouble(parts[0].trim());
        double dec = toDouble(parts[1].trim());
        return new NumberPair(ra, dec);
    }

    public static String[] splitCoordinates(String coords) {
        coords = convertToDecimalCoords(coords);
        return coords.trim().replaceAll("[,;]", " ").split("\\s+");
    }

    public static String convertToDecimalCoords(String coords) {
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
        addFieldToPanel(new CatalogElement(), detailPanel);
    }

    public static void addLabelToPanel(CatalogElement element, JPanel panel) {
        String name = element.getName();
        JLabel label = new JLabel(name == null ? "" : name + " = ", JLabel.RIGHT);
        if (element.isOnFocus()) {
            label.setOpaque(true);
            label.setBackground(JColor.WHITE.val);
        }
        if (element.isComputed()) {
            label.setForeground(JColor.DARKER_GREEN.val);
        }
        if (element.isFaulty()) {
            label.setForeground(JColor.DARK_RED.val);
        }
        panel.add(label);
    }

    public static void addFieldToPanel(CatalogElement element, JPanel panel) {
        String value = element.getValue();
        JTextField field = new JTextField(value == null ? "" : value);
        if (element.isOnFocus()) {
            field.setBackground(JColor.WHITE.val);
        }
        if (element.isComputed()) {
            field.setForeground(JColor.DARKER_GREEN.val);
        }
        if (element.isFaulty()) {
            field.setForeground(JColor.DARK_RED.val);
        }
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setEditable(false);
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
