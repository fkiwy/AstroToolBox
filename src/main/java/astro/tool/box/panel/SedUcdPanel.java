package astro.tool.box.panel;

import static astro.tool.box.function.NumericFunctions.PATTERN_2DEC_NZ;
import static astro.tool.box.function.NumericFunctions.addPlusSign;
import static astro.tool.box.function.NumericFunctions.roundDouble;
import static astro.tool.box.function.NumericFunctions.roundTo2DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecSN;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.function.PhotometricFunctions.calculatePhotometricDistance;
import static astro.tool.box.function.PhotometricFunctions.convertMagnitudeToFluxDensity;
import static astro.tool.box.function.PhotometricFunctions.convertMagnitudeToFluxJansky;
import static astro.tool.box.function.PhotometricFunctions.convertMagnitudeToFluxLambda;
import static astro.tool.box.function.StatisticFunctions.calculateMean;
import static astro.tool.box.function.StatisticFunctions.determineMedian;
import static astro.tool.box.function.StatisticFunctions.medianAbsoluteDeviation;
import static astro.tool.box.main.ToolboxHelper.createPDF;
import static astro.tool.box.main.ToolboxHelper.getInfoIcon;
import static astro.tool.box.main.ToolboxHelper.html;
import static astro.tool.box.main.ToolboxHelper.retrieveCatalogEntry;
import static astro.tool.box.main.ToolboxHelper.showScrollableDialog;
import static astro.tool.box.main.ToolboxHelper.writeErrorLog;
import static astro.tool.box.util.Constants.LINE_BREAK;
import static astro.tool.box.util.Constants.LINE_SEP;
import static java.lang.Math.abs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.block.LabelBlock;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.DesCatalogEntry;
import astro.tool.box.catalog.NoirlabCatalogEntry;
import astro.tool.box.catalog.PanStarrsCatalogEntry;
import astro.tool.box.catalog.TwoMassCatalogEntry;
import astro.tool.box.catalog.UhsCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.SedBestMatch;
import astro.tool.box.container.SedFluxes;
import astro.tool.box.container.SedReferences;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Sed;
import astro.tool.box.enumeration.SpectralType;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.service.CatalogQueryService;

public class SedUcdPanel extends JPanel {

	private static final String FONT_NAME = "Tahoma";

	private final List<SpectralTypeLookup> brownDwarfLookupEntries;
	private final CatalogQueryService catalogQueryService;
	private final JFrame baseFrame;

	private final JTextField photSearchRadius;
	private final JComboBox spectralTypes;
	private final JButton createButton;
	private final JButton removeButton;
	private final JCheckBox bestMatch;
	private final JCheckBox overplotTemplates;

	private final JCheckBox panStarrsPhot;
	private final JCheckBox noirlabPhot;
	private final JCheckBox desPhot;
	private final JCheckBox twoMassPhot;
	private final JCheckBox ukidssPhot;
	private final JCheckBox uhsPhot;
	private final JCheckBox vhsPhot;
	private final JCheckBox allwisePhot;
	private final JCheckBox catwisePhot;
	private final JCheckBox unwisePhot;

	private JFreeChart chart;
	private ChartPanel chartPanel;

	private Map<Band, SedReferences> sedReferences;
	private Map<Band, SedFluxes> sedFluxes;
	private Map<Band, NumberPair> sedPhotometry;
	private Map<Band, String> sedCatalogs;
	private StringBuilder sedDataPoints;

	private final double flux_error = Double.NaN;
	private double medianPhotDist;
	private double stdPhotDist;
	private List<Band> photDistBands;

	public SedUcdPanel(List<SpectralTypeLookup> brownDwarfLookupEntries, CatalogQueryService catalogQueryService,
			CatalogEntry catalogEntry, JFrame baseFrame) {
		this.brownDwarfLookupEntries = brownDwarfLookupEntries;
		this.catalogQueryService = catalogQueryService;
		this.baseFrame = baseFrame;

		photSearchRadius = new JTextField("5", 3);
		spectralTypes = new JComboBox(SpectralType.values());
		createButton = new JButton("Create SED");
		removeButton = new JButton("Remove templates");
		bestMatch = new JCheckBox("Best match", true);
		overplotTemplates = new JCheckBox("Overplot templates", true);

		panStarrsPhot = new JCheckBox("Pan-STARRS  ", true);
		noirlabPhot = new JCheckBox("NSC  ", false);
		desPhot = new JCheckBox("DES  ", false);
		twoMassPhot = new JCheckBox("2MASS  ", true);
		ukidssPhot = new JCheckBox("UKIDSS  ", false);
		uhsPhot = new JCheckBox("UHS  ", false);
		vhsPhot = new JCheckBox("VHS  ", false);
		allwisePhot = new JCheckBox("AllWISE  ", true);
		catwisePhot = new JCheckBox("CatWISE  ", false);
		unwisePhot = new JCheckBox("unWISE  ", false);

		YIntervalSeriesCollection collection = createSed(catalogEntry, null, true);

		JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(commandPanel);

		commandPanel.add(new JLabel("Search radius (\")"));
		commandPanel.add(photSearchRadius);

		commandPanel.add(panStarrsPhot);
		panStarrsPhot.addActionListener((ActionEvent e) -> {
			noirlabPhot.setSelected(false);
			desPhot.setSelected(false);
		});

		commandPanel.add(noirlabPhot);
		noirlabPhot.addActionListener((ActionEvent e) -> {
			panStarrsPhot.setSelected(false);
			desPhot.setSelected(false);
		});

		commandPanel.add(desPhot);
		desPhot.addActionListener((ActionEvent e) -> {
			panStarrsPhot.setSelected(false);
			noirlabPhot.setSelected(false);
		});

		commandPanel.add(twoMassPhot);
		twoMassPhot.addActionListener((ActionEvent e) -> {
			ukidssPhot.setSelected(false);
			uhsPhot.setSelected(false);
			vhsPhot.setSelected(false);
		});

		commandPanel.add(ukidssPhot);
		ukidssPhot.addActionListener((ActionEvent e) -> {
			twoMassPhot.setSelected(false);
			uhsPhot.setSelected(false);
			vhsPhot.setSelected(false);
		});

		commandPanel.add(uhsPhot);
		uhsPhot.addActionListener((ActionEvent e) -> {
			twoMassPhot.setSelected(false);
			ukidssPhot.setSelected(false);
			vhsPhot.setSelected(false);
		});

		commandPanel.add(vhsPhot);
		vhsPhot.addActionListener((ActionEvent e) -> {
			twoMassPhot.setSelected(false);
			ukidssPhot.setSelected(false);
			uhsPhot.setSelected(false);
		});

		commandPanel.add(allwisePhot);
		allwisePhot.addActionListener((ActionEvent e) -> {
			catwisePhot.setSelected(false);
			unwisePhot.setSelected(false);
		});

		commandPanel.add(catwisePhot);
		catwisePhot.addActionListener((ActionEvent e) -> {
			allwisePhot.setSelected(false);
			unwisePhot.setSelected(false);
		});

		commandPanel.add(unwisePhot);
		unwisePhot.addActionListener((ActionEvent e) -> {
			allwisePhot.setSelected(false);
			catwisePhot.setSelected(false);
		});

		commandPanel.add(createButton);
		createButton.setFont(createButton.getFont().deriveFont(Font.BOLD, 14f));
		createButton.setMargin(new Insets(2, 10, 2, 10));
		createButton.addActionListener((ActionEvent e) -> {
			spectralTypes.setSelectedItem(SpectralType.SELECT);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
			XYPlot plot = chart.getXYPlot();
			plot.getRenderer().setSeriesToolTipGenerator(0, addToolTips());
		});

		commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(commandPanel);

		commandPanel.add(new JLabel("SED templates: ", SwingConstants.RIGHT));
		commandPanel.add(spectralTypes);
		spectralTypes.addActionListener((ActionEvent e) -> {
			addReferenceSeds(sedPhotometry, collection);
			setSeriesShape(chart);
		});

		commandPanel.add(removeButton);
		removeButton.addActionListener((ActionEvent e) -> {
			spectralTypes.setSelectedItem(SpectralType.SELECT);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, false);
			setSeriesShape(chart);
		});

		commandPanel.add(bestMatch);
		bestMatch.addActionListener((ActionEvent e) -> {
			spectralTypes.setSelectedItem(SpectralType.SELECT);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(overplotTemplates);
		overplotTemplates.addActionListener((ActionEvent e) -> {
			spectralTypes.setSelectedItem(SpectralType.SELECT);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		JButton createButton = new JButton("Create PDF");
		commandPanel.add(createButton);
		createButton.addActionListener((ActionEvent e) -> {
			try {
				File tmpFile = File.createTempFile("Target_" + roundTo2DecNZ(catalogEntry.getRa())
						+ addPlusSign(roundDouble(catalogEntry.getDec(), PATTERN_2DEC_NZ)) + "_", ".pdf");
				createPDF(chart, tmpFile, 1000, 800);
				Desktop.getDesktop().open(tmpFile);
			} catch (Exception ex) {
				writeErrorLog(ex);
			}
		});

		JButton dataButton = new JButton("Get SED data points");
		commandPanel.add(dataButton);
		dataButton.addActionListener((ActionEvent e) -> {
			showScrollableDialog(baseFrame, "SED data points", sedDataPoints.toString());
		});

		String info = "Holding the mouse pointer over a data point on your object's SED (black line), shows the corresponding magnitude, wavelength and flux density."
				+ LINE_BREAK
				+ "Right-clicking on the chart, opens a context menu with additional functions like printing and saving.";

		JLabel infoLabel = new JLabel("Tooltip");
		infoLabel.setToolTipText(html(info));
		commandPanel.add(infoLabel);

		JLabel toolTip = new JLabel(getInfoIcon());
		toolTip.setToolTipText(html(info));
		commandPanel.add(toolTip);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	private YIntervalSeriesCollection createSed(CatalogEntry catalogEntry, YIntervalSeriesCollection collection,
			boolean addReferenceSeds) {
		photSearchRadius.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		createButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		bestMatch.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		overplotTemplates.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		panStarrsPhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		noirlabPhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		desPhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		twoMassPhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		ukidssPhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		uhsPhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		vhsPhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		allwisePhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		catwisePhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		unwisePhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		sedReferences = new HashMap();
		sedFluxes = new HashMap();
		sedPhotometry = new HashMap();
		sedCatalogs = new HashMap();
		sedDataPoints = new StringBuilder();

		medianPhotDist = 0;
		stdPhotDist = 0;
		photDistBands = Collections.emptyList();

		double searchRadius = toDouble(photSearchRadius.getText());
		searchRadius = searchRadius < 1 ? 1 : searchRadius;
		StringBuilder seriesLabel = new StringBuilder();

		if (panStarrsPhot.isSelected()) {
			PanStarrsCatalogEntry panStarrsEntry = new PanStarrsCatalogEntry();
			panStarrsEntry.setRa(catalogEntry.getRa());
			panStarrsEntry.setDec(catalogEntry.getDec());
			panStarrsEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(panStarrsEntry, catalogQueryService, baseFrame);
			if (retrievedEntry == null) {
				panStarrsPhot.setSelected(false);
				noirlabPhot.setSelected(true);
			} else {
				panStarrsEntry = (PanStarrsCatalogEntry) retrievedEntry;
				seriesLabel.append(panStarrsEntry.getCatalogName()).append(": ").append(panStarrsEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.g, panStarrsEntry.getCatalogName());
				sedCatalogs.put(Band.r, panStarrsEntry.getCatalogName());
				sedCatalogs.put(Band.i, panStarrsEntry.getCatalogName());
				sedCatalogs.put(Band.z, panStarrsEntry.getCatalogName());
				sedCatalogs.put(Band.y, panStarrsEntry.getCatalogName());
				addPanStarrsReferences();
				sedPhotometry.put(Band.g, panStarrsEntry.get_g_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(panStarrsEntry.get_g_mag(), panStarrsEntry.get_g_err()));
				sedPhotometry.put(Band.r, panStarrsEntry.get_r_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(panStarrsEntry.get_r_mag(), panStarrsEntry.get_r_err()));
				sedPhotometry.put(Band.i, panStarrsEntry.get_i_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(panStarrsEntry.get_i_mag(), panStarrsEntry.get_i_err()));
				sedPhotometry.put(Band.z, panStarrsEntry.get_z_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(panStarrsEntry.get_z_mag(), panStarrsEntry.get_z_err()));
				sedPhotometry.put(Band.y, panStarrsEntry.get_y_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(panStarrsEntry.get_y_mag(), panStarrsEntry.get_y_err()));
			}
		}

		if (noirlabPhot.isSelected()) {
			NoirlabCatalogEntry noirlabEntry = new NoirlabCatalogEntry();
			noirlabEntry.setRa(catalogEntry.getRa());
			noirlabEntry.setDec(catalogEntry.getDec());
			noirlabEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(noirlabEntry, catalogQueryService, baseFrame);
			if (retrievedEntry == null) {
				noirlabPhot.setSelected(false);
				desPhot.setSelected(true);
			} else {
				noirlabEntry = (NoirlabCatalogEntry) retrievedEntry;
				seriesLabel.append(noirlabEntry.getCatalogName()).append(": ").append(noirlabEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.g, noirlabEntry.getCatalogName());
				sedCatalogs.put(Band.r, noirlabEntry.getCatalogName());
				sedCatalogs.put(Band.i, noirlabEntry.getCatalogName());
				sedCatalogs.put(Band.z, noirlabEntry.getCatalogName());
				sedCatalogs.put(Band.y, noirlabEntry.getCatalogName());
				addDecamReferences();
				sedPhotometry.put(Band.g, new NumberPair(noirlabEntry.get_g_mag(), noirlabEntry.get_g_err()));
				sedPhotometry.put(Band.r, new NumberPair(noirlabEntry.get_r_mag(), noirlabEntry.get_r_err()));
				sedPhotometry.put(Band.i, new NumberPair(noirlabEntry.get_i_mag(), noirlabEntry.get_i_err()));
				sedPhotometry.put(Band.z, new NumberPair(noirlabEntry.get_z_mag(), noirlabEntry.get_z_err()));
				sedPhotometry.put(Band.y, new NumberPair(noirlabEntry.get_y_mag(), noirlabEntry.get_y_err()));
			}
		}

		if (desPhot.isSelected()) {
			DesCatalogEntry desEntry = new DesCatalogEntry();
			desEntry.setRa(catalogEntry.getRa());
			desEntry.setDec(catalogEntry.getDec());
			desEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(desEntry, catalogQueryService, baseFrame);
			if (retrievedEntry == null) {
				desPhot.setSelected(false);
			} else {
				desEntry = (DesCatalogEntry) retrievedEntry;
				seriesLabel.append(desEntry.getCatalogName()).append(": ").append(desEntry.getSourceId()).append(" ");
				sedCatalogs.put(Band.g, desEntry.getCatalogName());
				sedCatalogs.put(Band.r, desEntry.getCatalogName());
				sedCatalogs.put(Band.i, desEntry.getCatalogName());
				sedCatalogs.put(Band.z, desEntry.getCatalogName());
				sedCatalogs.put(Band.y, desEntry.getCatalogName());
				addDecamReferences();
				sedPhotometry.put(Band.g, desEntry.get_g_caut() > 3 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(desEntry.get_g_mag(), desEntry.get_g_err()));
				sedPhotometry.put(Band.r, desEntry.get_r_caut() > 3 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(desEntry.get_r_mag(), desEntry.get_r_err()));
				sedPhotometry.put(Band.i, desEntry.get_i_caut() > 3 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(desEntry.get_i_mag(), desEntry.get_i_err()));
				sedPhotometry.put(Band.z, desEntry.get_z_caut() > 3 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(desEntry.get_z_mag(), desEntry.get_z_err()));
				sedPhotometry.put(Band.y, desEntry.get_y_caut() > 3 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(desEntry.get_y_mag(), desEntry.get_y_err()));
			}
		}

		add2MassReferences();
		sedPhotometry.put(Band.J, new NumberPair(flux_error, flux_error));
		sedPhotometry.put(Band.H, new NumberPair(flux_error, flux_error));
		sedPhotometry.put(Band.K, new NumberPair(flux_error, flux_error));

		if (twoMassPhot.isSelected()) {
			TwoMassCatalogEntry twoMassEntry = new TwoMassCatalogEntry();
			twoMassEntry.setRa(catalogEntry.getRa());
			twoMassEntry.setDec(catalogEntry.getDec());
			twoMassEntry.setSearchRadius(searchRadius * 2);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(twoMassEntry, catalogQueryService, baseFrame);
			if (retrievedEntry == null) {
				twoMassPhot.setSelected(false);
				ukidssPhot.setSelected(true);
			} else {
				twoMassEntry = (TwoMassCatalogEntry) retrievedEntry;
				seriesLabel.append(twoMassEntry.getCatalogName()).append(": ").append(twoMassEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.J, twoMassEntry.getCatalogName());
				sedCatalogs.put(Band.H, twoMassEntry.getCatalogName());
				sedCatalogs.put(Band.K, twoMassEntry.getCatalogName());
				add2MassReferences();
				sedPhotometry.put(Band.J, twoMassEntry.getJ_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(twoMassEntry.getJmag(), twoMassEntry.getJ_err()));
				sedPhotometry.put(Band.H, twoMassEntry.getH_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(twoMassEntry.getHmag(), twoMassEntry.getH_err()));
				sedPhotometry.put(Band.K, twoMassEntry.getK_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(twoMassEntry.getKmag(), twoMassEntry.getK_err()));
			}
		}

		if (ukidssPhot.isSelected()) {
			UkidssCatalogEntry ukidssEntry;
			if (catalogEntry.getDec() > -5) {
				ukidssEntry = new UkidssCatalogEntry();
				ukidssEntry.setRa(catalogEntry.getRa());
				ukidssEntry.setDec(catalogEntry.getDec());
				ukidssEntry.setSearchRadius(searchRadius);
				CatalogEntry retrievedEntry = retrieveCatalogEntry(ukidssEntry, catalogQueryService, baseFrame);
				if (retrievedEntry == null) {
					ukidssPhot.setSelected(false);
					uhsPhot.setSelected(true);
				} else {
					ukidssEntry = (UkidssCatalogEntry) retrievedEntry;
					if (ukidssEntry.getJmag() == 0 && ukidssEntry.getHmag() == 0 && ukidssEntry.getKmag() == 0) {
						ukidssPhot.setSelected(false);
						uhsPhot.setSelected(true);
					} else {
						seriesLabel.append(ukidssEntry.getCatalogName()).append(": ").append(ukidssEntry.getSourceId())
								.append(" ");
						sedCatalogs.put(Band.J, ukidssEntry.getCatalogName());
						sedCatalogs.put(Band.H, ukidssEntry.getCatalogName());
						sedCatalogs.put(Band.K, ukidssEntry.getCatalogName());
						addUkidssReferences();
						sedPhotometry.put(Band.J, new NumberPair(ukidssEntry.getJmag(), ukidssEntry.getJ_err()));
						sedPhotometry.put(Band.H, new NumberPair(ukidssEntry.getHmag(), ukidssEntry.getH_err()));
						sedPhotometry.put(Band.K, new NumberPair(ukidssEntry.getKmag(), ukidssEntry.getK_err()));
					}
				}
			} else {
				ukidssPhot.setSelected(false);
				vhsPhot.setSelected(true);
			}
		}

		if (uhsPhot.isSelected()) {
			UhsCatalogEntry uhsEntry;
			if (catalogEntry.getDec() > -5) {
				uhsEntry = new UhsCatalogEntry();
				uhsEntry.setRa(catalogEntry.getRa());
				uhsEntry.setDec(catalogEntry.getDec());
				uhsEntry.setSearchRadius(searchRadius);
				CatalogEntry retrievedEntry = retrieveCatalogEntry(uhsEntry, catalogQueryService, baseFrame);
				if (retrievedEntry == null) {
					uhsPhot.setSelected(false);
					vhsPhot.setSelected(true);
				} else {
					uhsEntry = (UhsCatalogEntry) retrievedEntry;
					seriesLabel.append(uhsEntry.getCatalogName()).append(": ").append(uhsEntry.getSourceId())
							.append(" ");
					sedCatalogs.put(Band.J, uhsEntry.getCatalogName());
					sedCatalogs.put(Band.H, uhsEntry.getCatalogName());
					sedCatalogs.put(Band.K, uhsEntry.getCatalogName());
					addUkidssReferences();
					sedPhotometry.put(Band.J, new NumberPair(uhsEntry.getJmag(), uhsEntry.getJ_err()));
					sedPhotometry.put(Band.H, new NumberPair(uhsEntry.getHmag(), uhsEntry.getH_err()));
					sedPhotometry.put(Band.K, new NumberPair(uhsEntry.getKmag(), uhsEntry.getK_err()));
				}
			} else {
				uhsPhot.setSelected(false);
				vhsPhot.setSelected(true);
			}
		}

		if (vhsPhot.isSelected()) {
			VhsCatalogEntry vhsEntry;
			if (catalogEntry.getDec() < 5) {
				vhsEntry = new VhsCatalogEntry();
				vhsEntry.setRa(catalogEntry.getRa());
				vhsEntry.setDec(catalogEntry.getDec());
				vhsEntry.setSearchRadius(searchRadius);
				CatalogEntry retrievedEntry = retrieveCatalogEntry(vhsEntry, catalogQueryService, baseFrame);
				if (retrievedEntry == null) {
					vhsPhot.setSelected(false);
				} else {
					vhsEntry = (VhsCatalogEntry) retrievedEntry;
					seriesLabel.append(vhsEntry.getCatalogName()).append(": ").append(vhsEntry.getSourceId())
							.append(" ");
					sedCatalogs.put(Band.J, vhsEntry.getCatalogName());
					sedCatalogs.put(Band.H, vhsEntry.getCatalogName());
					sedCatalogs.put(Band.K, vhsEntry.getCatalogName());
					addVhsReferences();
					sedPhotometry.put(Band.J, new NumberPair(vhsEntry.getJmag(), vhsEntry.getJ_err()));
					sedPhotometry.put(Band.H, new NumberPair(vhsEntry.getHmag(), vhsEntry.getH_err()));
					sedPhotometry.put(Band.K, new NumberPair(vhsEntry.getKmag(), vhsEntry.getK_err()));
				}
			} else {
				vhsPhot.setSelected(false);
			}
		}

		if (allwisePhot.isSelected()) {
			AllWiseCatalogEntry allWiseEntry = new AllWiseCatalogEntry();
			allWiseEntry.setRa(catalogEntry.getRa());
			allWiseEntry.setDec(catalogEntry.getDec());
			allWiseEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(allWiseEntry, catalogQueryService, baseFrame);
			if (retrievedEntry == null) {
				allwisePhot.setSelected(false);
				catwisePhot.setSelected(true);
			} else {
				allWiseEntry = (AllWiseCatalogEntry) retrievedEntry;
				seriesLabel.append(allWiseEntry.getCatalogName()).append(": ").append(allWiseEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.W1, allWiseEntry.getCatalogName());
				sedCatalogs.put(Band.W2, allWiseEntry.getCatalogName());
				sedCatalogs.put(Band.W3, allWiseEntry.getCatalogName());
				addWiseReferences();
				sedPhotometry.put(Band.W1, allWiseEntry.getW1_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(allWiseEntry.getW1mag(), allWiseEntry.getW1_err()));
				sedPhotometry.put(Band.W2, allWiseEntry.getW2_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(allWiseEntry.getW2mag(), allWiseEntry.getW2_err()));
				sedPhotometry.put(Band.W3, allWiseEntry.getW3_err() == 0 ? new NumberPair(flux_error, flux_error)
						: new NumberPair(allWiseEntry.getW3mag(), allWiseEntry.getW3_err()));
			}
		}

		if (catwisePhot.isSelected()) {
			CatWiseCatalogEntry catWiseEntry = new CatWiseCatalogEntry();
			catWiseEntry.setRa(catalogEntry.getRa());
			catWiseEntry.setDec(catalogEntry.getDec());
			catWiseEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(catWiseEntry, catalogQueryService, baseFrame);
			if (retrievedEntry == null) {
				catwisePhot.setSelected(false);
				unwisePhot.setSelected(true);
			} else {
				catWiseEntry = (CatWiseCatalogEntry) retrievedEntry;
				seriesLabel.append(catWiseEntry.getCatalogName()).append(": ").append(catWiseEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.W1, catWiseEntry.getCatalogName());
				sedCatalogs.put(Band.W2, catWiseEntry.getCatalogName());
				addWiseReferences();
				sedPhotometry.put(Band.W1, new NumberPair(catWiseEntry.getW1mag(), catWiseEntry.getW1_err()));
				sedPhotometry.put(Band.W2, new NumberPair(catWiseEntry.getW2mag(), catWiseEntry.getW2_err()));
			}
		}

		if (unwisePhot.isSelected()) {
			UnWiseCatalogEntry unWiseEntry = new UnWiseCatalogEntry();
			unWiseEntry.setRa(catalogEntry.getRa());
			unWiseEntry.setDec(catalogEntry.getDec());
			unWiseEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(unWiseEntry, catalogQueryService, baseFrame);
			if (retrievedEntry == null) {
				unwisePhot.setSelected(false);
			} else {
				unWiseEntry = (UnWiseCatalogEntry) retrievedEntry;
				seriesLabel.append(unWiseEntry.getCatalogName()).append(": ").append(unWiseEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.W1, unWiseEntry.getCatalogName());
				sedCatalogs.put(Band.W2, unWiseEntry.getCatalogName());
				addWiseReferences();
				sedPhotometry.put(Band.W1, new NumberPair(unWiseEntry.getW1mag(), flux_error));
				sedPhotometry.put(Band.W2, new NumberPair(unWiseEntry.getW2mag(), flux_error));
			}
		}

		Band.getSedBands().forEach(band -> {
			NumberPair pair = sedPhotometry.get(band);
			if (pair != null) {
				double photometry = pair.x() == 0 ? Double.NaN : pair.x();
				double error = pair.y() == 0 ? Double.NaN : pair.y();
				SedReferences references = sedReferences.get(band);
				sedFluxes.put(band,
						new SedFluxes(photometry, error,
								convertMagnitudeToFluxDensity(photometry, references.zeropoint(),
										references.wavelenth()),
								convertMagnitudeToFluxJansky(photometry, references.zeropoint()),
								convertMagnitudeToFluxLambda(photometry, references.zeropoint(),
										references.wavelenth())));
			}
		});

		YIntervalSeries series = new YIntervalSeries(seriesLabel.toString());

		sedDataPoints.append(seriesLabel).append(LINE_SEP);
		Band.getSedBands().forEach(band -> {
			NumberPair pair = sedPhotometry.get(band);
			if (pair != null) {
				SedReferences references = sedReferences.get(band);
				SedFluxes fluxes = sedFluxes.get(band);
				double flux = fluxes.fluxLambda();
				double factor = Math.pow(10.0, 0.4 * fluxes.magError());
				double lower = flux / factor;
				double upper = flux * factor;
				double wavelength = references.wavelenth();
				series.add(wavelength, flux, lower, upper);
				sedDataPoints.append("(").append(wavelength).append(",").append(flux).append(")").append(LINE_SEP);

			}
		});

		if (collection == null) {
			collection = new YIntervalSeriesCollection();
		} else {
			collection.removeAllSeries();
		}

		collection.addSeries(series);

		if (addReferenceSeds) {
			addReferenceSeds(sedPhotometry, collection);
		}

		chart = createChart(collection);

		if (chartPanel != null) {
			remove(chartPanel);
		}

		chartPanel = new ChartPanel(chart) {
			@Override
			public void mouseDragged(MouseEvent e) {
			}
		};
		chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		chartPanel.setPreferredSize(new Dimension(1000, 850));
		chartPanel.setBackground(Color.WHITE);
		add(chartPanel, 0);

		// Add median photometric distance to legend
		LegendTitle legend = chart.getLegend();
		chart.removeLegend();
		BlockContainer container = new BlockContainer(new ColumnArrangement());
		container.add(legend);
		if (medianPhotDist > 0) {
			String bandsLabel = photDistBands.stream().map(b -> b.val).collect(Collectors.joining(", "));
			LabelBlock label = new LabelBlock(
					String.format("Median photometric distance (%s) = %.2f pc, Median absolute deviation = %.2f pc",
							bandsLabel, medianPhotDist, stdPhotDist));
			label.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
			label.setPaint(Color.DARK_GRAY);
			container.add(label);
		}
		CompositeTitle composite = new CompositeTitle(container);
		composite.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(composite);

		revalidate();
		repaint();

		photSearchRadius.setCursor(Cursor.getDefaultCursor());
		createButton.setCursor(Cursor.getDefaultCursor());
		removeButton.setCursor(Cursor.getDefaultCursor());
		bestMatch.setCursor(Cursor.getDefaultCursor());
		overplotTemplates.setCursor(Cursor.getDefaultCursor());

		panStarrsPhot.setCursor(Cursor.getDefaultCursor());
		noirlabPhot.setCursor(Cursor.getDefaultCursor());
		desPhot.setCursor(Cursor.getDefaultCursor());
		twoMassPhot.setCursor(Cursor.getDefaultCursor());
		ukidssPhot.setCursor(Cursor.getDefaultCursor());
		uhsPhot.setCursor(Cursor.getDefaultCursor());
		vhsPhot.setCursor(Cursor.getDefaultCursor());
		allwisePhot.setCursor(Cursor.getDefaultCursor());
		catwisePhot.setCursor(Cursor.getDefaultCursor());
		unwisePhot.setCursor(Cursor.getDefaultCursor());

		return collection;
	}

	private void addPanStarrsReferences() {
		sedReferences.put(Band.g, new SedReferences(Sed.PS1_G.zeropoint, Sed.PS1_G.wavelenth));
		sedReferences.put(Band.r, new SedReferences(Sed.PS1_R.zeropoint, Sed.PS1_R.wavelenth));
		sedReferences.put(Band.i, new SedReferences(Sed.PS1_I.zeropoint, Sed.PS1_I.wavelenth));
		sedReferences.put(Band.z, new SedReferences(Sed.PS1_Z.zeropoint, Sed.PS1_Z.wavelenth));
		sedReferences.put(Band.y, new SedReferences(Sed.PS1_Y.zeropoint, Sed.PS1_Y.wavelenth));
	}

	private void addDecamReferences() {
		sedReferences.put(Band.g, new SedReferences(Sed.DECAM_G.zeropoint, Sed.DECAM_G.wavelenth));
		sedReferences.put(Band.r, new SedReferences(Sed.DECAM_R.zeropoint, Sed.DECAM_R.wavelenth));
		sedReferences.put(Band.i, new SedReferences(Sed.DECAM_I.zeropoint, Sed.DECAM_I.wavelenth));
		sedReferences.put(Band.z, new SedReferences(Sed.DECAM_Z.zeropoint, Sed.DECAM_Z.wavelenth));
		sedReferences.put(Band.y, new SedReferences(Sed.DECAM_Y.zeropoint, Sed.DECAM_Y.wavelenth));
	}

	private void add2MassReferences() {
		sedReferences.put(Band.J, new SedReferences(Sed.MASS_J.zeropoint, Sed.MASS_J.wavelenth));
		sedReferences.put(Band.H, new SedReferences(Sed.MASS_H.zeropoint, Sed.MASS_H.wavelenth));
		sedReferences.put(Band.K, new SedReferences(Sed.MASS_K.zeropoint, Sed.MASS_K.wavelenth));
	}

	private void addUkidssReferences() {
		sedReferences.put(Band.J, new SedReferences(Sed.UKIDSS_J.zeropoint, Sed.UKIDSS_J.wavelenth));
		sedReferences.put(Band.H, new SedReferences(Sed.UKIDSS_H.zeropoint, Sed.UKIDSS_H.wavelenth));
		sedReferences.put(Band.K, new SedReferences(Sed.UKIDSS_K.zeropoint, Sed.UKIDSS_K.wavelenth));
	}

	private void addVhsReferences() {
		sedReferences.put(Band.J, new SedReferences(Sed.VHS_J.zeropoint, Sed.VHS_J.wavelenth));
		sedReferences.put(Band.H, new SedReferences(Sed.VHS_H.zeropoint, Sed.VHS_H.wavelenth));
		sedReferences.put(Band.K, new SedReferences(Sed.VHS_K.zeropoint, Sed.VHS_K.wavelenth));
	}

	private void addWiseReferences() {
		sedReferences.put(Band.W1, new SedReferences(Sed.WISE_W1.zeropoint, Sed.WISE_W1.wavelenth));
		sedReferences.put(Band.W2, new SedReferences(Sed.WISE_W2.zeropoint, Sed.WISE_W2.wavelenth));
		sedReferences.put(Band.W3, new SedReferences(Sed.WISE_W3.zeropoint, Sed.WISE_W3.wavelenth));
	}

	private void addReferenceSeds(Map<Band, NumberPair> sedPhotometry, YIntervalSeriesCollection collection) {
		SpectralType selectedType = (SpectralType) spectralTypes.getSelectedItem();
		List<SedBestMatch> matches = new ArrayList();
		for (SpectralTypeLookup lookupEntry : brownDwarfLookupEntries) {
			BrownDwarfLookupEntry entry = (BrownDwarfLookupEntry) lookupEntry;
			Map<Band, Double> bands = entry.getMagnitudes();
			String spectralType = entry.getSpt();
			List<Double> diffMags = new ArrayList();
			List<Double> photDistances = new ArrayList();
			List<Band> photBands = new ArrayList();
			Band.getSedBands().forEach(band -> {
				if (sedPhotometry.get(band) != null) {
					Double observed = sedPhotometry.get(band).x();
					Double template = bands.get(band);
					if (!observed.equals(Double.NaN) && observed != 0 && !template.equals(Double.NaN)
							&& template != 0) {
						double diffMag = observed - template;
						diffMags.add(diffMag);
						double photDistance = calculatePhotometricDistance(observed, template);
						photDistances.add(photDistance);
						photBands.add(band);
					}
				}
			});
			double medianDiffMag = determineMedian(diffMags);

			double medianPhotDistance = determineMedian(photDistances);
			double stdPhotDistance = 1.4826 * medianAbsoluteDeviation(photDistances);
			// The factor 1.4826 makes the MAD comparable to the standard deviation under a
			// normal distribution.

			if (selectedType.equals(SpectralType.SELECT)) {
				int totalMags = diffMags.size();
				if (totalMags < 4) {
					continue;
				}
				int selectedMags = 0;
				List<Double> correctedDiffMags = new ArrayList();
				for (Double diffMag : diffMags) {
					double correctedDiffMag = abs(diffMag - medianDiffMag);
					correctedDiffMags.add(correctedDiffMag);
					if (correctedDiffMag < 0.3) {
						selectedMags++;
					}
				}
				if (selectedMags < totalMags - 2) {
					continue;
				}
				double meanDiffMag = calculateMean(correctedDiffMags);
				matches.add(new SedBestMatch(spectralType, medianDiffMag, meanDiffMag, medianPhotDistance,
						stdPhotDistance, photBands));
			} else if (selectedType.equals(SpectralType.valueOf(spectralType))) {
				createReferenceSed(spectralType, collection, medianDiffMag);
				return;
			}
		}
		if (!matches.isEmpty()) {
			matches.sort(Comparator.comparing(SedBestMatch::meanDiffMag));
			SedBestMatch bestMatched = matches.get(0);
			medianPhotDist = bestMatched.medianPhotDist();
			stdPhotDist = bestMatched.stdPhotDist();
			photDistBands = bestMatched.photBands();
			int j = bestMatch.isSelected() ? 1 : 3;
			for (int i = 0; i < j && i < matches.size(); i++) {
				SedBestMatch match = matches.get(i);
				createReferenceSed(match.spt(), collection, match.medianDiffMag());
			}
		}
	}

	private void createReferenceSed(String spectralType, YIntervalSeriesCollection collection, double medianDiffMag) {
		Map<Band, Double> magnitudes = provideReferenceMagnitudes(spectralType);
		if (magnitudes == null) {
			return;
		}
		if (!overplotTemplates.isSelected()) {
			medianDiffMag = 0;
		}
		YIntervalSeries series = new YIntervalSeries(spectralType);
		series.add(Sed.PS1_G.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.g) + medianDiffMag,
				Sed.PS1_G.zeropoint, Sed.PS1_G.wavelenth), flux_error, flux_error);
		series.add(Sed.PS1_R.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.r) + medianDiffMag,
				Sed.PS1_R.zeropoint, Sed.PS1_R.wavelenth), flux_error, flux_error);
		series.add(Sed.PS1_I.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.i) + medianDiffMag,
				Sed.PS1_I.zeropoint, Sed.PS1_I.wavelenth), flux_error, flux_error);
		series.add(Sed.PS1_Z.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.z) + medianDiffMag,
				Sed.PS1_Z.zeropoint, Sed.PS1_Z.wavelenth), flux_error, flux_error);
		series.add(Sed.PS1_Y.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.y) + medianDiffMag,
				Sed.PS1_Y.zeropoint, Sed.PS1_Y.wavelenth), flux_error, flux_error);
		series.add(Sed.MASS_J.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.J) + medianDiffMag,
				Sed.MASS_J.zeropoint, Sed.MASS_J.wavelenth), flux_error, flux_error);
		series.add(Sed.MASS_H.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.H) + medianDiffMag,
				Sed.MASS_H.zeropoint, Sed.MASS_H.wavelenth), flux_error, flux_error);
		series.add(Sed.MASS_K.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.K) + medianDiffMag,
				Sed.MASS_K.zeropoint, Sed.MASS_K.wavelenth), flux_error, flux_error);
		series.add(Sed.WISE_W1.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.W1) + medianDiffMag,
				Sed.WISE_W1.zeropoint, Sed.WISE_W1.wavelenth), flux_error, flux_error);
		series.add(Sed.WISE_W2.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.W2) + medianDiffMag,
				Sed.WISE_W2.zeropoint, Sed.WISE_W2.wavelenth), flux_error, flux_error);
		series.add(Sed.WISE_W3.wavelenth, convertMagnitudeToFluxLambda(magnitudes.get(Band.W3) + medianDiffMag,
				Sed.WISE_W3.zeropoint, Sed.WISE_W3.wavelenth), flux_error, flux_error);

		sedDataPoints.append(LINE_SEP).append(spectralType).append(":").append(LINE_SEP);
		for (int i = 0; i < series.getItemCount(); i++) {
			if (!Double.isNaN(series.getYValue(i))) {
				sedDataPoints.append("(").append(series.getX(i)).append(",").append(series.getYValue(i)).append(")")
						.append(LINE_SEP);
			}
		}

		try {
			collection.addSeries(series);
		} catch (IllegalArgumentException ex) {
		}
	}

	private JFreeChart createChart(YIntervalSeriesCollection collection) {
		JFreeChart chart = ChartFactory.createXYLineChart("Spectral Energy Distribution", "", "", collection);
		chart.setPadding(new RectangleInsets(10, 10, 10, 10));
		XYPlot plot = chart.getXYPlot();

		LogAxis xAxis = new LogAxis("Wavelength (μm)");
		xAxis.setAutoRangeMinimumSize(0.1);
		xAxis.setTickUnit(new NumberTickUnit(0.2));
		// xAxis.setNumberFormatOverride(new DecimalFormat("#.#"));
		plot.setDomainAxis(xAxis);

		LogAxis yAxis = new LogAxis("Flux (W/m²/μm)");
		yAxis.setAutoRangeMinimumSize(1E-18);
		yAxis.setTickUnit(new NumberTickUnit(0.5));
		// yAxis.setNumberFormatOverride(new DecimalFormat("0E0"));
		plot.setRangeAxis(yAxis);

		Font tickLabelFont = new Font(FONT_NAME, Font.PLAIN, 18);
		xAxis.setTickLabelFont(tickLabelFont);
		yAxis.setTickLabelFont(tickLabelFont);
		Font labelFont = new Font(FONT_NAME, Font.PLAIN, 18);
		xAxis.setLabelFont(labelFont);
		yAxis.setLabelFont(labelFont);

		// Data point shape of object to estimate
		double size = 6.0;
		double delta = size / 2.0;
		Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);

		XYErrorRenderer renderer = new XYErrorRenderer();
		renderer.setDrawXError(false);
		renderer.setDrawYError(true);
		renderer.setDefaultLinesVisible(true);
		renderer.setSeriesShape(0, shape);
		renderer.setSeriesPaint(0, Color.BLACK);
		renderer.setSeriesStroke(0, new BasicStroke(2));
		renderer.setSeriesToolTipGenerator(0, addToolTips());
		plot.setRenderer(renderer);

		// Data point shape of templates
		setSeriesShape(chart);

		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlineStroke(new BasicStroke());
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlineStroke(new BasicStroke());

		Font titleFont = new Font(FONT_NAME, Font.PLAIN, 22);
		chart.getTitle().setFont(titleFont);

		Font legendFont = new Font(FONT_NAME, Font.PLAIN, 18);
		chart.getLegend().setFrame(BlockBorder.NONE);
		chart.getLegend().setItemFont(legendFont);

		return chart;
	}

	private void setSeriesShape(JFreeChart chart) {
		XYPlot plot = chart.getXYPlot();
		XYItemRenderer renderer = plot.getRenderer();
		double size = 8.0;
		double delta = size / 2.0;
		Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);
		for (int i = 1; i < plot.getSeriesCount(); i++) {
			renderer.setSeriesShape(i, shape);
		}
	}

	private CustomXYToolTipGenerator addToolTips() {
		List<String> toolTips = new ArrayList();
		Band.getSedBands().forEach(band -> {
			SedFluxes fluxes = sedFluxes.get(band);
			if (fluxes != null) {
				toolTips.add(html(sedCatalogs.get(band) + " " + band.val + "=" + roundTo3DecNZ(fluxes.magnitude())
						+ " ± " + roundTo3DecNZ(fluxes.magError()) + " mag<br>" + "λ="
						+ sedReferences.get(band).wavelenth() + " μm<br>" + "F(ν)="
						+ roundTo3DecSN(fluxes.fluxJansky()) + " Jy<br>" + "λF(λ)="
						+ roundTo3DecSN(fluxes.fluxDensity()) + " W/m²<br>" + "F(λ)="
						+ roundTo3DecSN(fluxes.fluxLambda()) + " W/m²/μm"));
			}
		});
		CustomXYToolTipGenerator generator = new CustomXYToolTipGenerator();
		generator.addToolTipSeries(toolTips);
		return generator;
	}

	private Map<Band, Double> provideReferenceMagnitudes(String spt) {
		for (SpectralTypeLookup lookupEntry : brownDwarfLookupEntries) {
			BrownDwarfLookupEntry entry = (BrownDwarfLookupEntry) lookupEntry;
			if (entry.getSpt().equals(spt)) {
				Map<Band, Double> magnitudes = new HashMap<>(entry.getMagnitudes());
				magnitudes.replaceAll((band, value) -> value == null || value == 0 ? Double.NaN : value);
				return magnitudes;
			}
		}
		return null;
	}

}
