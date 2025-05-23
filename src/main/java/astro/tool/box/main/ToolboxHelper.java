package astro.tool.box.main;

import static astro.tool.box.function.AstrometricFunctions.convertMJDToDate;
import static astro.tool.box.function.NumericFunctions.isNumeric;
import static astro.tool.box.function.NumericFunctions.roundTo2DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo4DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo7DecNZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.function.PhotometricFunctions.isAPossibleAGN;
import static astro.tool.box.function.PhotometricFunctions.isAPossibleWD;
import static astro.tool.box.tab.SettingsTab.OBJECT_COLLECTION_PATH;
import static astro.tool.box.tab.SettingsTab.getUserSetting;
import static astro.tool.box.util.Comparators.getDoubleComparator;
import static astro.tool.box.util.Comparators.getIntegerComparator;
import static astro.tool.box.util.Comparators.getStringComparator;
import static astro.tool.box.util.Constants.DESI_LS_DR_PARAM;
import static astro.tool.box.util.Constants.LINE_SEP;
import static astro.tool.box.util.Constants.PIXEL_SCALE_DECAM;
import static astro.tool.box.util.Constants.SPLIT_CHAR;
import static astro.tool.box.util.Constants.UHS_LABEL;
import static astro.tool.box.util.Constants.UKIDSS_LABEL;
import static astro.tool.box.util.ExternalResources.getTygoFormUrl;
import static astro.tool.box.util.ServiceHelper.establishHttpConnection;
import static astro.tool.box.util.ServiceHelper.readResponse;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.jfree.chart.JFreeChart;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.DesCatalogEntry;
import astro.tool.box.catalog.GaiaDR2CatalogEntry;
import astro.tool.box.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.catalog.GaiaWDCatalogEntry;
import astro.tool.box.catalog.MocaCatalogEntry;
import astro.tool.box.catalog.NoirlabCatalogEntry;
import astro.tool.box.catalog.PanStarrsCatalogEntry;
import astro.tool.box.catalog.SdssCatalogEntry;
import astro.tool.box.catalog.SimbadCatalogEntry;
import astro.tool.box.catalog.TessCatalogEntry;
import astro.tool.box.catalog.TwoMassCatalogEntry;
import astro.tool.box.catalog.UhsCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.catalog.WhiteDwarf;
import astro.tool.box.component.TranslucentLabel;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.CollectedObject;
import astro.tool.box.container.Couple;
import astro.tool.box.container.MjdEpoch;
import astro.tool.box.container.NirImage;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.Tiles;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.BasicDataType;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.function.AstrometricFunctions;
import astro.tool.box.lookup.DistanceLookupResult;
import astro.tool.box.lookup.LookupResult;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.DistanceLookupService;
import astro.tool.box.service.NameResolverService;
import astro.tool.box.service.SpectralTypeLookupService;
import astro.tool.box.shape.Circle;
import astro.tool.box.shape.Drawable;
import astro.tool.box.util.FileTypeFilter;
import astro.tool.box.util.GifSequencer;

public class ToolboxHelper {

	public static final String PGM_NAME = "AstroToolBox";
	public static final String PGM_VERSION = "4.4.0";
	public static final String RELEASES_URL = "https://fkiwy.github.io/AstroToolBox/releases/";

	public static final String USER_HOME = System.getProperty("user.home");
	public static final String AGN_WARNING = "Possible AGN!";
	public static final String WD_WARNING = "Possible white dwarf!";
	public static final String INFO_ICON = "<span style='color:red'>&#9432;</span>";
	public static final String PHOT_DIST_INFO = "Clicking on a table row displays photometric distance estimates for the specified spectral type.";

	private static final String ERROR_FILE_NAME = "/AstroToolBoxError.txt";
	private static final String ERROR_FILE_PATH = USER_HOME + ERROR_FILE_NAME;

	public static int BASE_FRAME_WIDTH = 1275;
	public static int BASE_FRAME_HEIGHT = 875;

	public static int BUFFER_SIZE = 8 * 1024;

	public static Image getToolBoxImage() {
		ImageIcon icon = new ImageIcon(ToolboxHelper.class.getResource("/icons/toolbox.png"));
		return icon.getImage();
	}

	public static ImageIcon getInfoIcon() {
		return new ImageIcon(ToolboxHelper.class.getResource("/icons/info.png"));
	}

	public static Map<String, CatalogEntry> getCatalogInstances() {
		Map<String, CatalogEntry> catalogInstances = new LinkedHashMap();

		// Plug in catalogs here
		SimbadCatalogEntry simbadCatalogEntry = new SimbadCatalogEntry();
		catalogInstances.put(simbadCatalogEntry.getCatalogName(), simbadCatalogEntry);
		AllWiseCatalogEntry allWiseCatalogEntry = new AllWiseCatalogEntry();
		catalogInstances.put(allWiseCatalogEntry.getCatalogName(), allWiseCatalogEntry);
		CatWiseCatalogEntry catWiseCatalogEntry = new CatWiseCatalogEntry();
		catalogInstances.put(catWiseCatalogEntry.getCatalogName(), catWiseCatalogEntry);
		UnWiseCatalogEntry unWiseCatalogEntry = new UnWiseCatalogEntry();
		catalogInstances.put(unWiseCatalogEntry.getCatalogName(), unWiseCatalogEntry);
		GaiaDR2CatalogEntry gaiaCatalogEntry = new GaiaDR2CatalogEntry();
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
		UhsCatalogEntry uhsCatalogEntry = new UhsCatalogEntry();
		catalogInstances.put(uhsCatalogEntry.getCatalogName(), uhsCatalogEntry);
		UkidssCatalogEntry ukidssCatalogEntry = new UkidssCatalogEntry();
		catalogInstances.put(ukidssCatalogEntry.getCatalogName(), ukidssCatalogEntry);
		TwoMassCatalogEntry twoMassCatalogEntry = new TwoMassCatalogEntry();
		catalogInstances.put(twoMassCatalogEntry.getCatalogName(), twoMassCatalogEntry);
		TessCatalogEntry tessCatalogEntry = new TessCatalogEntry();
		catalogInstances.put(tessCatalogEntry.getCatalogName(), tessCatalogEntry);
		DesCatalogEntry desCatalogEntry = new DesCatalogEntry();
		catalogInstances.put(desCatalogEntry.getCatalogName(), desCatalogEntry);
		GaiaWDCatalogEntry gaiaWDCatalogEntry = new GaiaWDCatalogEntry();
		catalogInstances.put(gaiaWDCatalogEntry.getCatalogName(), gaiaWDCatalogEntry);
		MocaCatalogEntry mocaCatalogEntry = new MocaCatalogEntry();
		catalogInstances.put(mocaCatalogEntry.getCatalogName(), mocaCatalogEntry);

		return catalogInstances;
	}

	public static JLabel createHyperlink(String label, String uri) {
		return createHyperlink(new JLabel(label), uri);
	}

	public static JLabel createHyperlink(JLabel label, String uri) {
		label.setForeground(JColor.LINK_BLUE.val);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if (label.getMouseListeners().length > 0) {
			label.removeMouseListener(label.getMouseListeners()[0]);
		}
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(uri));
				} catch (IOException | URISyntaxException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		return label;
	}

	public static void showScrollableDialog(JFrame baseFrame, String title, String message) {
		JOptionPane.showMessageDialog(baseFrame, createMessagePanel(message), title, JOptionPane.PLAIN_MESSAGE);
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
		JOptionPane.showMessageDialog(baseFrame, createMessagePanel(formatError(error)), "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public static void writeErrorLog(Exception error) {
		writeLogEntry(formatError(error));
	}

	public static void writeMessageLog(String message) {
		writeLogEntry(formatMessage(message));
	}

	private static void writeLogEntry(String entry) {
		try {
			Files.write(Paths.get(ERROR_FILE_PATH), entry.getBytes(), StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
		}
	}

	private static JScrollPane createMessagePanel(String message) {
		JTextPane textPane = new JTextPane();
		textPane.setText(message);
		textPane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setBorder(BorderFactory.createEtchedBorder());
		scrollPane.setPreferredSize(new Dimension(700, 500));
		return scrollPane;
	}

	public static boolean showConfirmDialog(JFrame baseFrame, String message) {
		int option = JOptionPane.showConfirmDialog(baseFrame, message, "Confimation", JOptionPane.OK_CANCEL_OPTION);
		return option == JOptionPane.YES_OPTION;
	}

	public static String red(String text) {
		return html("<span style='color:red'>" + text + "</span>");
	}

	public static String bold(String text) {
		return html("<b>" + text + "</b>");
	}

	public static String html(String text) {
		return "<html>" + text + "</html>";
	}

	public static JLabel createHeaderLabel(String text) {
		return createHeaderLabel(text, SwingConstants.LEFT);
	}

	public static JLabel createHeaderLabel(String text, int alignment) {
		JLabel header = new JLabel(text);
		header.setBorder(new EmptyBorder(0, 5, 0, 0));
		header.setBackground(Color.GRAY.brighter());
		header.setForeground(Color.BLACK);
		header.setOpaque(true);
		header.setHorizontalAlignment(alignment);
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
		return BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), boderTitle, TitledBorder.LEFT,
				TitledBorder.TOP, null, titleColor);
	}

	public static Border createEmptyBorder(String boderTitle) {
		return createEmptyBorder(boderTitle, null);
	}

	public static Border createEmptyBorder(String boderTitle, Color titleColor) {
		return BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), boderTitle, TitledBorder.LEFT,
				TitledBorder.TOP, null, titleColor);
	}

	public static boolean isSameTarget(double targetRa, double targetDec, double size, double previousRa,
			double previousDec, double previousSize) {
		return targetRa == previousRa && targetDec == previousDec && size == previousSize;
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
		JLabel label = new JLabel(name == null ? "" : name + ": ", SwingConstants.RIGHT);
		// if (element.isOnFocus()) {
		// label.setOpaque(true);
		// label.setBackground(JColor.WHITE.val);
		// }
		// if (element.isComputed()) {
		// label.setForeground(JColor.DARK_GREEN.val);
		// }
		// if (element.isFaulty()) {
		// label.setForeground(JColor.RED.val);
		// }
		if (element.getToolTip() != null) {
			label.setToolTipText(html(element.getToolTip()));
		}
		panel.add(label);
	}

	public static void addFieldToPanel(CatalogElement element, JPanel panel) {
		String value = element.getValue();
		boolean hasToolTip = element.getToolTip() != null;
		JTextField field = new JTextField(value == null ? "" : value + (hasToolTip ? " (*)" : ""));
		// if (element.isOnFocus()) {
		// field.setBackground(JColor.WHITE.val);
		// } else {
		field.setBackground(new JLabel().getBackground());
		// }
		if (element.isComputed()) {
			field.setForeground(JColor.DARK_GREEN.val);
		}
		if (element.isFaulty()) {
			field.setForeground(JColor.RED.val);
		}
		field.setCaretPosition(0);
		field.setBorder(BorderFactory.createEmptyBorder());
		field.setEditable(false);
		if (hasToolTip) {
			field.setToolTipText(html(element.getToolTip()));
		}
		field.setPreferredSize(new Dimension(100, field.getPreferredSize().height));
		panel.add(field);
	}

	public static void alignCatalogColumns(JTable table, CatalogEntry entry) {
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		List<CatalogElement> elements = entry.getCatalogElements();
		for (int i = 0; i < elements.size(); i++) {
			Alignment alignment = elements.get(i).getAlignment();
			table.getColumnModel().getColumn(i)
					.setCellRenderer(alignment.equals(Alignment.LEFT) ? leftRenderer : rightRenderer);
		}
	}

	public static TableRowSorter createCatalogTableSorter(DefaultTableModel defaultTableModel, CatalogEntry entry) {
		TableRowSorter<TableModel> sorter = new TableRowSorter(defaultTableModel);
		List<CatalogElement> elements = entry.getCatalogElements();
		for (int i = 0; i < elements.size(); i++) {
			sorter.setComparator(i, elements.get(i).getComparator());
		}
		return sorter;
	}

	public static void alignResultColumns(JTable table, List<String[]> rows) {
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
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
		TableRowSorter<TableModel> sorter = new TableRowSorter();
		addComparatorsToTableSorter(sorter, defaultTableModel, rows);
		return sorter;
	}

	public static void addComparatorsToTableSorter(TableRowSorter<TableModel> sorter,
			DefaultTableModel defaultTableModel, List<String[]> rows) {
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
		Map<Integer, BasicDataType> types = new HashMap();
		rows.forEach((row) -> {
			for (int i = 0; i < row.length; i++) {
				String columnValue = row[i];
				if (!columnValue.isEmpty()) {
					BasicDataType type = types.get(i);
					if (type == null) {
						type = BasicDataType.NONE;
					}
					if (type.equals(BasicDataType.ALPHA_NUMERIC)) {
						continue;
					}
					if (isNumeric(columnValue)) {
						type = BasicDataType.NUMERIC;
					} else {
						type = BasicDataType.ALPHA_NUMERIC;
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
		return new RowFilter<>() {
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
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
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
		TableRowSorter<TableModel> sorter = new TableRowSorter(defaultTableModel);
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

	public static List<String> lookupSpectralTypes(Map<astro.tool.box.enumeration.Color, Double> colors,
			SpectralTypeLookupService spectralTypeLookupService, boolean includeColors) {
		List<LookupResult> results = spectralTypeLookupService.lookup(colors);
		List<String> spectralTypes = new ArrayList();
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

	public static void collectObject(String objectType, CatalogEntry catalogEntry, JFrame baseFrame,
			SpectralTypeLookupService spectralTypeLookupService, JTable collectionTable) {
		// Collect data
		List<String> spectralTypes = lookupSpectralTypes(catalogEntry.getColors(true), spectralTypeLookupService, true);
		if (catalogEntry instanceof SimbadCatalogEntry simbadEntry) {
			StringBuilder simbadType = new StringBuilder();
			simbadType.append(simbadEntry.getObjectType());
			if (!simbadEntry.getSpectralType().isEmpty()) {
				simbadType.append(" ").append(simbadEntry.getSpectralType());
			}
			simbadType.append("; ");
			spectralTypes.add(0, simbadType.toString());
		}
		if (catalogEntry instanceof AllWiseCatalogEntry allWiseEntry) {
			if (isAPossibleAGN(allWiseEntry.getW1_W2(), allWiseEntry.getW2_W3())) {
				spectralTypes.add(AGN_WARNING);
			}
		}
		if (catalogEntry instanceof WhiteDwarf entry) {
			if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
				spectralTypes.add(WD_WARNING);
			}
		}
		CollectedObject collectedObject = new CollectedObject.Builder().setDiscoveryDate(LocalDateTime.now())
				.setObjectType(objectType).setCatalogName(catalogEntry.getCatalogName()).setRa(catalogEntry.getRa())
				.setDec(catalogEntry.getDec()).setSourceId(catalogEntry.getSourceId() + " ")
				.setPlx(catalogEntry.getPlx()).setPmra(catalogEntry.getPmra()).setPmdec(catalogEntry.getPmdec())
				.setSpectralTypes(spectralTypes).setNotes("").build();

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
			tableModel.addRow(concatArrays(new String[] { "" }, collectedObject.getColumnValues()));
		}
	}

	public static String copyObjectCoordinates(CatalogEntry catalogEntry) {
		StringBuilder toCopy = new StringBuilder();
		toCopy.append(roundTo7DecNZ(catalogEntry.getRa()));
		toCopy.append(" ");
		toCopy.append(roundTo7DecNZ(catalogEntry.getDec()));
		return toCopy.toString();
	}

	public static String copyObjectSummary(CatalogEntry catalogEntry) {
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
				if (!label.contains("err")) {
					toCopy.append(label).append("=").append(roundTo3DecNZ(value));
					toCopy.append(LINE_SEP);
				}
			}
		});
		return toCopy.toString();
	}

	public static String copyObjectInfo(CatalogEntry catalogEntry, List<LookupResult> mainSequenceResults,
			List<LookupResult> brownDwarfsResults, DistanceLookupService distanceLookupService) {
		StringBuilder toCopy = new StringBuilder();
		toCopy.append(catalogEntry.getEntryData());
		toCopy.append(LINE_SEP).append(LINE_SEP).append("Spectral type estimates:");
		if (mainSequenceResults != null) {
			toCopy.append(LINE_SEP).append("* Main sequence table:");
			mainSequenceResults.forEach(entry -> {
				toCopy.append(LINE_SEP).append("  + ").append(entry.getColorKey().val).append(" = ")
						.append(roundTo3DecNZ(entry.getColorValue())).append(" -> ").append(entry.getSpt());
			});
		}
		if (brownDwarfsResults != null) {
			toCopy.append(LINE_SEP).append("* M, L & T dwarfs only:");
			brownDwarfsResults.forEach(entry -> {
				toCopy.append(LINE_SEP).append("  + ").append(entry.getColorKey().val).append(" = ")
						.append(roundTo3DecNZ(entry.getColorValue())).append(" -> ").append(entry.getSpt());
				List<DistanceLookupResult> distanceResults = distanceLookupService.lookup(entry.getSpt(),
						catalogEntry.getBands());
				toCopy.append(LINE_SEP).append("      Distance estimates for ").append(entry.getSpt()).append(":");
				distanceResults.forEach(result -> {
					toCopy.append(LINE_SEP).append("      - ").append(result.getBandKey().val).append(" = ")
							.append(roundTo3DecNZ(result.getBandValue())).append(" -> ")
							.append(roundTo3DecNZ(result.getDistance())).append(" pc");
				});
			});
		}
		return toCopy.toString();
	}

	public static void fillTygoForm(CatalogEntry catalogEntry, CatalogQueryService catalogQueryService,
			JFrame baseFrame) {
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
		gaiaEntry = (GaiaDR3CatalogEntry) retrieveCatalogEntry(gaiaEntry, catalogQueryService, baseFrame);
		if (gaiaEntry != null) {
			// GAIA DR2 pmRA + e_pmRA (mas/y)
			if (gaiaEntry.getPmra() != 0) {
				params.append("&entry.905761395=").append(roundTo3DecNZ(gaiaEntry.getPmra())).append(" ")
						.append(roundTo3DecNZ(gaiaEntry.getPmraErr()));
			}
			// GAIA DR2 pmDE + e_pmDE
			if (gaiaEntry.getPmdec() != 0) {
				params.append("&entry.965290776=").append(roundTo3DecNZ(gaiaEntry.getPmdec())).append(" ")
						.append(roundTo3DecNZ(gaiaEntry.getPmdecErr()));
			}
			// GAIA RV + e_RV
			if (gaiaEntry.getRadvel() != 0) {
				params.append("&entry.702334724=").append(roundTo3DecNZ(gaiaEntry.getRadvel())).append(" ")
						.append(roundTo3DecNZ(gaiaEntry.getRadvelErr()));
			}
			// GAIA DR2 Parallax + e_
			if (gaiaEntry.getPlx() != 0) {
				params.append("&entry.1383168065=").append(roundTo4DecNZ(gaiaEntry.getPlx())).append(" ")
						.append(roundTo4DecNZ(gaiaEntry.getPlxErr()));
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

	public static CatalogEntry retrieveCatalogEntry(CatalogEntry catalogQuery, CatalogQueryService catalogQueryService,
			JFrame baseFrame) {
		try {
			List<CatalogEntry> catalogEntries = catalogQueryService.getCatalogEntriesByCoords(catalogQuery);
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

	public static Tiles getWiseTiles(double degRA, double degDE) throws IOException {
		String url = "http://byw.tools/tiles?ra=%f&dec=%f".formatted(degRA, degDE);
		String response = readResponse(establishHttpConnection(url), "WiseView");
		return new Gson().fromJson(response, Tiles.class);
	}

	public static List<JLabel> getNearestZooniverseSubjects(double degRA, double degDE) {
		List<JLabel> subjects = new ArrayList();
		try {
			String url = "http://byw.tools/xref?ra=%f&dec=%f".formatted(degRA, degDE);
			String response = readResponse(establishHttpConnection(url), "Zooniverse");
			if (!response.isEmpty()) {
				JsonElement jelement = JsonParser.parseString(response).getAsJsonObject();
				JsonObject jobject = jelement.getAsJsonObject();
				JsonArray jarray = jobject.getAsJsonArray("ids");
				for (JsonElement element : jarray) {
					String id = element.getAsString();
					subjects.add(createHyperlink(id,
							"https://www.zooniverse.org/projects/marckuchner/backyard-worlds-planet-9/talk/subjects/"
									+ id));
				}
			}
		} catch (JsonSyntaxException | IOException ex) {
		}
		return subjects;
	}

	public static String getImageLabel(String text, int epoch) {
		return text + (epoch > 0 ? " " + epoch : "");
	}

	public static String getImageLabel(String text, String epoch) {
		return text + " " + epoch;
	}

	public static int getMeanEpoch(int... epochs) {
		int sum = 0;
		int i = 0;
		for (int epoch : epochs) {
			if (epoch != 0) {
				sum += epoch;
				i++;
			}
		}
		return i == 0 ? i : sum / i;
	}

	public static int getEpoch(double targetRa, double targetDec, double size, String survey, String band) {
		try {
			String downloadUrl = "https://irsa.ipac.caltech.edu/applications/finderchart/servlet/api?RA=%f&DEC=%f&subsetsize=%s&survey=%s&%s"
					.formatted(targetRa, targetDec, roundTo2DecNZ(size / 60f), survey, band);
			String response = readResponse(establishHttpConnection(downloadUrl), "IRSA");
			try (Scanner scanner = new Scanner(response)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (line.contains("obsdate")) {
						String[] parts = line.split("<obsdate>");
						parts = parts[1].split("-");
						return Integer.parseInt(parts[0]);
					}
				}
			}
		} catch (IOException | NumberFormatException ex) {
		}
		return 0;
	}

	public static BufferedImage retrieveImage(double targetRa, double targetDec, int size, String survey, String band) {
		BufferedImage bi;
		String imageUrl = "https://irsa.ipac.caltech.edu/applications/finderchart/servlet/api?mode=getImage&RA=%f&DEC=%f&subsetsize=%s&thumbnail_size=small&survey=%s&%s"
				.formatted(targetRa, targetDec, roundTo2DecNZ(size / 60f), survey, band);
		try {
			HttpURLConnection connection = establishHttpConnection(imageUrl);
			BufferedInputStream stream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
			bi = ImageIO.read(stream);
			if (!band.contains("colorimage")) {
				invertColors(bi);
			}
		} catch (IOException ex) {
			bi = null;
		}
		return bi;
	}

	public static void invertColors(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgba = image.getRGB(x, y);
				int a = (rgba >> 24) & 0xff;
				int r = (rgba >> 16) & 0xff;
				int g = (rgba >> 8) & 0xff;
				int b = rgba & 0xff;

				// Invert each color component
				r = 255 - r;
				g = 255 - g;
				b = 255 - b;

				// Reconstruct the pixel with the inverted colors
				rgba = (a << 24) | (r << 16) | (g << 8) | b;
				image.setRGB(x, y, rgba);
			}
		}
	}

	public static int getPs1Epoch(double targetRa, double targetDec, String filters) {
		try {
			String downloadUrl = "http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=%s&type=warp&sep=comma"
					.formatted(targetRa, targetDec, filters);
			String response = readResponse(establishHttpConnection(downloadUrl), "Pan-STARRS");
			try (Scanner scanner = new Scanner(response)) {
				String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
				int mjd = 0;
				for (int i = 0; i < columnNames.length; i++) {
					if (columnNames[i].equals("mjd")) {
						mjd = i;
						break;
					}
				}
				int i = 0;
				double epoch = 0;
				while (scanner.hasNextLine()) {
					String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
					epoch += toDouble(columnValues[mjd]);
					i++;
				}
				return convertMJDToDate(epoch / i).get(ChronoField.YEAR);
			}
		} catch (IOException ex) {
		}
		return 0;
	}

	public static Map<String, Double> getPs1Epochs(double targetRa, double targetDec) {
		try {
			String downloadUrl = "http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=grizy&type=warp&sep=comma"
					.formatted(targetRa, targetDec);
			String response = readResponse(establishHttpConnection(downloadUrl), "Pan-STARRS");
			try (Scanner scanner = new Scanner(response)) {
				String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
				int filter = 0;
				int mjd = 0;
				for (int i = 0; i < columnNames.length; i++) {
					if (columnNames[i].equals("filter")) {
						filter = i;
					}
					if (columnNames[i].equals("mjd")) {
						mjd = i;
					}
				}
				List<MjdEpoch> epochs = new ArrayList();
				while (scanner.hasNextLine()) {
					String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
					String band = columnValues[filter];
					double epoch = toDouble(columnValues[mjd]);
					epochs.add(new MjdEpoch(band, convertMJDToDate(epoch).get(ChronoField.YEAR)));

				}
				return epochs.stream()
						.collect(Collectors.groupingBy(MjdEpoch::getBand, Collectors.averagingInt(MjdEpoch::getEpoch)));

			}
		} catch (IOException ex) {
		}
		return new HashMap();
	}

	public static Map<String, String> getPs1FileNames(double targetRa, double targetDec) {
		Map<String, String> fileNames = new LinkedHashMap();
		try {
			String downloadUrl = "http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=grizy&sep=comma"
					.formatted(targetRa, targetDec);
			String response = readResponse(establishHttpConnection(downloadUrl), "Pan-STARRS");
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
		} catch (IOException ex) {
			writeErrorLog(ex);
		}
		return fileNames;
	}

	public static BufferedImage retrievePs1Image(String fileNames, double targetRa, double targetDec, int size,
			boolean invert) {
		BufferedImage bi;
		String imageUrl = "http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?%s&ra=%f&dec=%f&size=%d&output_size=%d&autoscale=95.0&invert=%s"
				.formatted(fileNames, targetRa, targetDec, size * 4, 256, invert);
		try {
			HttpURLConnection connection = establishHttpConnection(imageUrl);
			BufferedInputStream stream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
			bi = ImageIO.read(stream);
		} catch (IOException ex) {
			bi = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		}
		return bi;
	}

	public static BufferedImage retrieveDesiImage(double targetRa, double targetDec, int size, String band,
			boolean invert) {
		return retrieveDesiImage(targetRa, targetDec, size, band, invert, DESI_LS_DR_PARAM);
	}

	public static BufferedImage retrieveDesiImage(double targetRa, double targetDec, int size, String band,
			boolean invert, String layer) {
		BufferedImage image;
		if (band == null) {
			band = "";
		}
		if (!band.isEmpty()) {
			band = "&bands=" + band;
		}
		String imageUrl = "https://www.legacysurvey.org/viewer/jpeg-cutout?ra=%f&dec=%f&pixscale=%f&layer=%s&size=%d%s"
				.formatted(targetRa, targetDec, PIXEL_SCALE_DECAM, layer, size * 4, band);
		try {
			HttpURLConnection connection = establishHttpConnection(imageUrl);
			BufferedInputStream stream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
			image = ImageIO.read(stream);
			if (invert) {
				image = convertToGrayImage(image);
				image = invertImage(image);
			}
			image = zoomImage(image, 256);
		} catch (IOException ex) {
			image = null;
		}
		return image;
	}

	public static Map<String, NirImage> retrieveNearInfraredImages(double targetRa, double targetDec, double size,
			String surveyUrl, String surveyLabel) throws Exception {
		String imageSize = roundTo2DecNZ(size / 60f);
		List<NirImage> nirImages = new ArrayList();
		String[] filterIds = new String[] { "2", "3", "4", "5" };
		for (String filterId : filterIds) {
			String downloadUrl = surveyUrl.formatted(targetRa, targetDec, filterId, imageSize, imageSize);
			String response = readResponse(establishHttpConnection(downloadUrl), surveyLabel);
			int i = 0;
			String imageUrl = "";
			String extNo = "";
			String year = "";
			try (Scanner scanner = new Scanner(response)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (line.contains("href")) {
						String[] parts = line.split("href=\"");
						parts = parts[1].split("\"");
						imageUrl = parts[0].replace("getImage", "getJImage");
						parts = line.split("extNo=");
						parts = parts[1].split("&");
						extNo = parts[0];
						i = 1;
					}
					if (i == 7) {
						try {
							String[] parts = line.split("<td nowrap>");
							parts = parts[1].split("-");
							year = parts[0];
						} catch (Exception ex) {
							year = "2010";
						}
						break;
					}
					if (i > 0) {
						i++;
					}
				}
			}
			if (!imageUrl.isEmpty()) {
				nirImages.add(new NirImage(filterId, extNo, Integer.parseInt(year), imageUrl));
			}
		}
		Map<String, NirImage> images = new LinkedHashMap();
		if (nirImages.isEmpty()) {
			return images;
		}
		for (NirImage nirImage : nirImages) {
			String band = getBand(nirImage.getFilderId());
			String extNo = nirImage.getExtNo();
			String imageUrl = nirImage.getImageUrl();
			try {
				HttpURLConnection connection = establishHttpConnection(imageUrl);
				BufferedInputStream stream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
				BufferedImage image = ImageIO.read(stream);
				int width = image.getWidth();
				int height = image.getHeight();
				int offset = 2;
				if (width > height + offset || width < height - offset) {
					return new LinkedHashMap();
				}
				if (surveyLabel.equals(UHS_LABEL) || surveyLabel.equals(UKIDSS_LABEL)) {
					// Rotate image
					switch (extNo) {
					case "1" -> image = rotateImage(image, 1);
					case "2" -> {
					}
					case "3" -> image = rotateImage(image, 3);
					case "4" -> image = rotateImage(image, 2);
					}
					// No rotation necessary
				}
				// Flip image
				image = flipImage(image);
				nirImage.setImage(image);
				images.put(band, nirImage);
			} catch (IOException ex) {
			}
		}
		NirImage nir1 = images.get("K");
		NirImage nir2 = images.get("H");
		NirImage nir3 = images.get("J");
		if (nir1 != null && nir2 != null && nir3 != null) {
			BufferedImage i1 = nir1.getImage();
			BufferedImage i2 = nir2.getImage();
			BufferedImage i3 = nir3.getImage();
			int width = i3.getWidth();
			int height = i3.getHeight();
			i1 = resizeImage(i1, width, height);
			i2 = resizeImage(i2, width, height);
			int y1 = nir1.getYear();
			int y2 = nir2.getYear();
			int y3 = nir3.getYear();
			BufferedImage colorImage = createColorImage(invertImage(i1), invertImage(i2), invertImage(i3));
			NirImage nirImage = new NirImage(getMeanEpoch(y1, y2, y3), colorImage);
			images.put("K-H-J", nirImage);
		} else if (nir1 != null && nir3 != null) {
			BufferedImage i1 = nir1.getImage();
			BufferedImage i3 = nir3.getImage();
			int width = i3.getWidth();
			int height = i3.getHeight();
			i1 = resizeImage(i1, width, height);
			int y1 = nir1.getYear();
			int y3 = nir3.getYear();
			BufferedImage colorImage = createColorImage(invertImage(i1), invertImage(i3));
			NirImage nirImage = new NirImage(getMeanEpoch(y1, y3), colorImage);
			images.put("K-J", nirImage);
		}
		return images;
	}

	private static String getBand(String filterId) {
		return switch (filterId) {
		case "2" -> "Y";
		case "3" -> "J";
		case "4" -> "H";
		case "5" -> "K";
		default -> "?";
		};
	}

	public static BufferedImage copyImage(BufferedImage bufferImage) {
		ColorModel colorModel = bufferImage.getColorModel();
		WritableRaster raster = bufferImage.copyData(null);
		boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
		return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
	}

	public static BufferedImage zoomImage(BufferedImage image, int zoom) {
		zoom = zoom == 0 ? 1 : zoom;
		return resizeImage(image, zoom, zoom);
	}

	private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
		Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
		BufferedImage zoomedImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null),
				BufferedImage.TYPE_INT_RGB);
		Graphics graphics = zoomedImage.createGraphics();
		graphics.drawImage(scaledImage, 0, 0, null);
		graphics.dispose();
		return zoomedImage;
	}

	public static BufferedImage invertImage(BufferedImage image) {
		BufferedImage invertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				Color c = new Color(rgb, true);
				c = new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
				invertedImage.setRGB(x, y, c.getRGB());
			}
		}
		return invertedImage;
	}

	public static BufferedImage flipImage(BufferedImage image) {
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -image.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return op.filter(image, null);
	}

	public static BufferedImage rotateImage(BufferedImage image, int numberOfQuadrants) {
		if (numberOfQuadrants == 0) {
			return image;
		}
		AffineTransform tx = AffineTransform.getQuadrantRotateInstance(numberOfQuadrants, image.getWidth() / 2,
				image.getHeight() / 2);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		return op.filter(image, null);
	}

	public static BufferedImage convertToGray(BufferedImage colorImage) {
		BufferedImage grayImage = new BufferedImage(colorImage.getWidth(), colorImage.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);
		Graphics graphics = grayImage.getGraphics();
		graphics.drawImage(colorImage, 0, 0, null);
		graphics.dispose();
		return grayImage;
	}

	public static BufferedImage convertToGrayImage(BufferedImage colorImage) {
		int width = colorImage.getWidth();
		int height = colorImage.getHeight();

		// Create a new BufferedImage with the same dimensions and grayscale color space
		BufferedImage grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		// Iterate through each pixel of the color image and set the grayscale value
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Get the RGB components of the pixel
				Color color = new Color(colorImage.getRGB(x, y));
				int red = color.getRed();
				int green = color.getGreen();
				int blue = color.getBlue();

				// Calculate the grayscale value as the average of RGB components
				// int grayscaleValue = (red + green + blue) / 3;
				int grayscaleValue = Math.min(red + green + blue, 255);

				// Set the grayscale value to the pixel in the new BufferedImage
				grayscaleImage.setRGB(x, y, new Color(grayscaleValue, grayscaleValue, grayscaleValue).getRGB());
			}
		}

		return grayscaleImage;
	}

	public static BufferedImage enhanceContrast(BufferedImage grayscaleImage, double contrastFactor) {
		int width = grayscaleImage.getWidth();
		int height = grayscaleImage.getHeight();

		// Create a new BufferedImage with enhanced contrast
		BufferedImage contrastEnhancedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		// Apply the contrast enhancement with the custom contrast factor to each pixel
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixelValue = new Color(grayscaleImage.getRGB(x, y)).getRed();
				int contrastEnhancedValue = (int) (contrastFactor * pixelValue);
				contrastEnhancedValue = Math.min(Math.max(contrastEnhancedValue, 0), 255);
				contrastEnhancedImage.setRGB(x, y,
						new Color(contrastEnhancedValue, contrastEnhancedValue, contrastEnhancedValue).getRGB());
			}
		}

		return contrastEnhancedImage;
	}

	public static BufferedImage createColorImage(BufferedImage i1, BufferedImage i2) {
		BufferedImage colorImage = new BufferedImage(i1.getWidth(), i1.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < colorImage.getWidth(); x++) {
			for (int y = 0; y < colorImage.getHeight(); y++) {
				try {
					int rgb1 = i1.getRGB(x, y);
					int rgb2 = i2.getRGB(x, y);
					Color c1 = new Color(rgb1, true);
					Color c2 = new Color(rgb2, true);
					Color color = new Color(c1.getRed(), (c1.getRed() + c2.getRed()) / 2, c2.getRed());
					colorImage.setRGB(x, y, color.getRGB());
				} catch (ArrayIndexOutOfBoundsException ex) {
				}
			}
		}
		return colorImage;
	}

	public static BufferedImage createColorImage(BufferedImage i1, BufferedImage i2, BufferedImage i3) {
		BufferedImage colorImage = new BufferedImage(i1.getWidth(), i1.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < colorImage.getWidth(); x++) {
			for (int y = 0; y < colorImage.getHeight(); y++) {
				try {
					int rgb1 = i1.getRGB(x, y);
					int rgb2 = i2.getRGB(x, y);
					int rgb3 = i3.getRGB(x, y);
					Color c1 = new Color(rgb1, true);
					Color c2 = new Color(rgb2, true);
					Color c3 = new Color(rgb3, true);
					Color color = new Color(c1.getRed(), c2.getRed(), c3.getRed());
					colorImage.setRGB(x, y, color.getRGB());
				} catch (ArrayIndexOutOfBoundsException ex) {
				}
			}
		}
		return colorImage;
	}

	public static BufferedImage drawCenterShape(BufferedImage image) {
		image = zoomImage(image, 175);
		double x = image.getWidth() / 2;
		double y = image.getHeight() / 2;
		Graphics g = image.getGraphics();
		Drawable drawable = new Circle(x, y, 30, Color.RED);
		drawable.draw(g);
		return image;
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

	public static JLabel addTextToImage(BufferedImage image, String text) {
		return addTextToImage(new JLabel(new ImageIcon(drawCenterShape(image))), text);
	}

	public static JLabel addTextToImage(JLabel background, String text) {
		background.setLayout(new BoxLayout(background, BoxLayout.Y_AXIS));
		JLabel label = new TranslucentLabel(text);
		label.setFont(label.getFont().deriveFont(10f));
		label.setBackground(new Color(255, 255, 255, 200));
		label.setBorder(new EmptyBorder(0, 3, 0, 3));
		label.setForeground(Color.BLACK);
		background.add(label);
		return background;
	}

	public static void saveAnimatedGif(List<Couple<String, BufferedImage>> imageList, Component container)
			throws Exception {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileTypeFilter(".gif", ".gif files"));
		int returnVal = fileChooser.showSaveDialog(container);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			file = new File(file.getPath() + ".gif");
			BufferedImage[] imageSet = new BufferedImage[imageList.size()];
			int i = 0;
			for (Couple<String, BufferedImage> imageData : imageList) {
				BufferedImage imageBuffer = imageData.getB();
				imageSet[i++] = drawCenterShape(imageBuffer);
			}
			if (imageSet.length > 0) {
				GifSequencer sequencer = new GifSequencer();
				sequencer.generateFromBI(imageSet, file, 500 / 10, true);
			}
		}
	}

	public static void createPDF(JFreeChart chart, File tmpFile, int width, int height) throws Exception {
		Rectangle pagesize = new Rectangle(width, height);
		com.itextpdf.text.Document document = new com.itextpdf.text.Document(pagesize, 50, 50, 50, 50);
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tmpFile));
		document.open();
		PdfContentByte contentByte = writer.getDirectContent();
		PdfTemplate template = contentByte.createTemplate(width, height);
		Graphics2D graphics = new PdfGraphics2D(contentByte, width, height);
		Rectangle2D rectangle = new Rectangle2D.Double(0, 0, width, height);
		chart.draw(graphics, rectangle);
		graphics.dispose();
		contentByte.addTemplate(template, 0, 0);
		document.close();
	}

	public static void addUndoManager(JTextArea textArea) {
		final UndoManager manger = new UndoManager();
		Document document = textArea.getDocument();

		// Listen for undo and redo events
		document.addUndoableEditListener((UndoableEditEvent evt) -> {
			manger.addEdit(evt.getEdit());
		});

		// Create an undo action and add it to the text component
		textArea.getActionMap().put("Undo", new AbstractAction("Undo") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					if (manger.canUndo()) {
						manger.undo();
					}
				} catch (CannotUndoException e) {
				}
			}
		});

		// Bind the undo action to ctl-Z
		textArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

		// Create a redo action and add it to the text component
		textArea.getActionMap().put("Redo", new AbstractAction("Redo") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					if (manger.canRedo()) {
						manger.redo();
					}
				} catch (CannotRedoException e) {
				}
			}
		});

		// Bind the redo action to ctl-Y
		textArea.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
	}

	public static WindowAdapter getChildWindowAdapter(JFrame baseFrame) {
		return new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				baseFrame.setFocusableWindowState(true);
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				baseFrame.setFocusableWindowState(true);
			}

			@Override
			public void windowIconified(WindowEvent e) {
				baseFrame.setFocusableWindowState(true);
				baseFrame.toFront();
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				baseFrame.setFocusableWindowState(true);
			}

			@Override
			public void windowOpened(WindowEvent evt) {
				baseFrame.setFocusableWindowState(false);
			}

			@Override
			public void windowActivated(WindowEvent e) {
				baseFrame.setFocusableWindowState(false);
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				baseFrame.setFocusableWindowState(false);
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				baseFrame.setFocusableWindowState(false);
			}
		};
	}

}
