package astro.tool.box.panel;

import static astro.tool.box.function.NumericFunctions.PATTERN_2DEC_NZ;
import static astro.tool.box.function.NumericFunctions.addPlusSign;
import static astro.tool.box.function.NumericFunctions.roundDouble;
import static astro.tool.box.function.NumericFunctions.roundTo2DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecSN;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.function.NumericFunctions.toInteger;
import static astro.tool.box.function.PhotometricFunctions.convertMagnitudeToFluxDensity;
import static astro.tool.box.function.PhotometricFunctions.convertMagnitudeToFluxJansky;
import static astro.tool.box.function.PhotometricFunctions.convertMagnitudeToFluxLambda;
import static astro.tool.box.function.StatisticFunctions.calculateMean;
import static astro.tool.box.function.StatisticFunctions.determineMedian;
import static astro.tool.box.main.ToolboxHelper.createHyperlink;
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
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.DesCatalogEntry;
import astro.tool.box.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.catalog.NoirlabCatalogEntry;
import astro.tool.box.catalog.PanStarrsCatalogEntry;
import astro.tool.box.catalog.TwoMassCatalogEntry;
import astro.tool.box.catalog.UhsCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.container.SedBestMatch;
import astro.tool.box.container.SedFluxes;
import astro.tool.box.container.SedReferences;
import astro.tool.box.container.WhiteDwarfEntry;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Sed;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.util.CSVParser;

public final class SedWdPanel extends JPanel {

	private static final String FONT_NAME = "Tahoma";

	private final List<WhiteDwarfEntry> whiteDwarfEntries;
	private final CatalogQueryService catalogQueryService;
	private final JFrame baseFrame;

	private final JTextField photSearchRadius;
	private final JButton removeButton;
	private final JCheckBox bestMatch;
	private final JCheckBox overplotTemplates;

	private final JCheckBox gaiaPhot;
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

	private Map<Band, SedReferences> sedReferences;
	private Map<Band, SedFluxes> sedFluxes;
	private Map<Band, Double> sedPhotometry;
	private Map<Band, String> sedCatalogs;
	private StringBuilder sedDataPoints;

	private boolean useGaiaPhotometry;

	public SedWdPanel(CatalogQueryService catalogQueryService, CatalogEntry catalogEntry, JFrame baseFrame) {
		whiteDwarfEntries = new ArrayList();
		createWhiteDwarfSedEntries();

		this.catalogQueryService = catalogQueryService;
		this.baseFrame = baseFrame;

		photSearchRadius = new JTextField("5", 3);
		removeButton = new JButton("Remove templates");
		bestMatch = new JCheckBox("Best match", true);
		overplotTemplates = new JCheckBox("Overplot templates", true);

		gaiaPhot = new JCheckBox("Gaia", true);
		panStarrsPhot = new JCheckBox("Pan-STARRS", false);
		noirlabPhot = new JCheckBox("NSC", false);
		desPhot = new JCheckBox("DES", false);
		twoMassPhot = new JCheckBox("2MASS", true);
		ukidssPhot = new JCheckBox("UKIDSS", false);
		uhsPhot = new JCheckBox("UHS", false);
		vhsPhot = new JCheckBox("VHS", false);
		allwisePhot = new JCheckBox("AllWISE", true);
		catwisePhot = new JCheckBox("CatWISE", false);
		unwisePhot = new JCheckBox("unWISE", false);

		XYSeriesCollection collection = createSed(catalogEntry, null, true);
		JFreeChart chart = createChart(collection);

		ChartPanel chartPanel = new ChartPanel(chart) {
			@Override
			public void mouseDragged(MouseEvent e) {
			}
		};
		chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		chartPanel.setPreferredSize(new Dimension(1000, 850));
		chartPanel.setBackground(Color.WHITE);
		add(chartPanel);

		JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(commandPanel);

		commandPanel.add(new JLabel("Photometry search radius"));
		commandPanel.add(photSearchRadius);
		photSearchRadius.addActionListener((ActionEvent e) -> {
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
			XYPlot plot = chart.getXYPlot();
			plot.getRenderer().setSeriesToolTipGenerator(0, addToolTips());
		});

		commandPanel.add(removeButton);
		removeButton.addActionListener((ActionEvent e) -> {
			collection.removeAllSeries();
			createSed(catalogEntry, collection, false);
			setSeriesShape(chart);
		});

		commandPanel.add(bestMatch);
		bestMatch.addActionListener((ActionEvent e) -> {
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(overplotTemplates);
		overplotTemplates.addActionListener((ActionEvent e) -> {
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(commandPanel);

		commandPanel.add(gaiaPhot);
		gaiaPhot.addActionListener((ActionEvent e) -> {
			panStarrsPhot.setSelected(false);
			noirlabPhot.setSelected(false);
			desPhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(panStarrsPhot);
		panStarrsPhot.addActionListener((ActionEvent e) -> {
			gaiaPhot.setSelected(false);
			noirlabPhot.setSelected(false);
			desPhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(noirlabPhot);
		noirlabPhot.addActionListener((ActionEvent e) -> {
			gaiaPhot.setSelected(false);
			panStarrsPhot.setSelected(false);
			desPhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(desPhot);
		desPhot.addActionListener((ActionEvent e) -> {
			gaiaPhot.setSelected(false);
			panStarrsPhot.setSelected(false);
			noirlabPhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(twoMassPhot);
		twoMassPhot.addActionListener((ActionEvent e) -> {
			ukidssPhot.setSelected(false);
			uhsPhot.setSelected(false);
			vhsPhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(ukidssPhot);
		ukidssPhot.addActionListener((ActionEvent e) -> {
			twoMassPhot.setSelected(false);
			uhsPhot.setSelected(false);
			vhsPhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(uhsPhot);
		uhsPhot.addActionListener((ActionEvent e) -> {
			twoMassPhot.setSelected(false);
			ukidssPhot.setSelected(false);
			vhsPhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(vhsPhot);
		vhsPhot.addActionListener((ActionEvent e) -> {
			twoMassPhot.setSelected(false);
			ukidssPhot.setSelected(false);
			uhsPhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(allwisePhot);
		allwisePhot.addActionListener((ActionEvent e) -> {
			catwisePhot.setSelected(false);
			unwisePhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(catwisePhot);
		catwisePhot.addActionListener((ActionEvent e) -> {
			allwisePhot.setSelected(false);
			unwisePhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel.add(unwisePhot);
		unwisePhot.addActionListener((ActionEvent e) -> {
			allwisePhot.setSelected(false);
			catwisePhot.setSelected(false);
			collection.removeAllSeries();
			createSed(catalogEntry, collection, true);
			setSeriesShape(chart);
		});

		commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(commandPanel);

		JButton createButton = new JButton("Create PDF");
		commandPanel.add(createButton);
		createButton.addActionListener((ActionEvent e) -> {
			try {
				File tmpFile = File.createTempFile("Target_" + roundTo2DecNZ(catalogEntry.getRa())
						+ addPlusSign(roundDouble(catalogEntry.getDec(), PATTERN_2DEC_NZ)) + "_", ".pdf");
				createPDF(chart, tmpFile, 800, 700);
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

		String info = "Holding the mouse pointer over a data point on your object's SED (black line), shows the corresponding filter and wavelength."
				+ LINE_BREAK
				+ "Right-clicking on the chart, opens a context menu with additional functions like printing and saving.";

		JLabel infoLabel = new JLabel("Tooltip");
		infoLabel.setToolTipText(html(info));
		commandPanel.add(infoLabel);

		JLabel toolTip = new JLabel(getInfoIcon());
		toolTip.setToolTipText(html(info));
		commandPanel.add(toolTip);

		commandPanel.add(new JLabel("This feature uses the"));
		commandPanel.add(createHyperlink("Montreal cooling sequences.",
				"http://www.astro.umontreal.ca/~bergeron/CoolingModels"));

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	private XYSeriesCollection createSed(CatalogEntry catalogEntry, XYSeriesCollection collection,
			boolean addReferenceSeds) {
		photSearchRadius.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		bestMatch.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		overplotTemplates.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		gaiaPhot.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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

		double searchRadius = toDouble(photSearchRadius.getText());
		searchRadius = searchRadius < 1 ? 1 : searchRadius;
		StringBuilder seriesLabel = new StringBuilder();

		useGaiaPhotometry = false;
		if (gaiaPhot.isSelected()) {
			GaiaDR3CatalogEntry gaiaEntry = new GaiaDR3CatalogEntry();
			gaiaEntry.setRa(catalogEntry.getRa());
			gaiaEntry.setDec(catalogEntry.getDec());
			gaiaEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(gaiaEntry, catalogQueryService, baseFrame);
			if (retrievedEntry != null) {
				useGaiaPhotometry = true;
				gaiaEntry = (GaiaDR3CatalogEntry) retrievedEntry;
				seriesLabel.append(gaiaEntry.getCatalogName()).append(": ").append(gaiaEntry.getSourceId()).append(" ");
				sedCatalogs.put(Band.BP, gaiaEntry.getCatalogName());
				sedCatalogs.put(Band.G, gaiaEntry.getCatalogName());
				sedCatalogs.put(Band.RP, gaiaEntry.getCatalogName());
				addGaiaReferences();
				sedPhotometry.put(Band.BP, gaiaEntry.getBPmag());
				sedPhotometry.put(Band.G, gaiaEntry.getGmag());
				sedPhotometry.put(Band.RP, gaiaEntry.getRPmag());
			}
		}

		if (panStarrsPhot.isSelected()) {
			PanStarrsCatalogEntry panStarrsEntry = new PanStarrsCatalogEntry();
			panStarrsEntry.setRa(catalogEntry.getRa());
			panStarrsEntry.setDec(catalogEntry.getDec());
			panStarrsEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(panStarrsEntry, catalogQueryService, baseFrame);
			if (retrievedEntry != null) {
				panStarrsEntry = (PanStarrsCatalogEntry) retrievedEntry;
				seriesLabel.append(panStarrsEntry.getCatalogName()).append(": ").append(panStarrsEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.g, panStarrsEntry.getCatalogName());
				sedCatalogs.put(Band.r, panStarrsEntry.getCatalogName());
				sedCatalogs.put(Band.i, panStarrsEntry.getCatalogName());
				sedCatalogs.put(Band.z, panStarrsEntry.getCatalogName());
				sedCatalogs.put(Band.y, panStarrsEntry.getCatalogName());
				addPanStarrsReferences();
				sedPhotometry.put(Band.g, panStarrsEntry.get_g_err() == 0 ? 0 : panStarrsEntry.get_g_mag());
				sedPhotometry.put(Band.r, panStarrsEntry.get_r_err() == 0 ? 0 : panStarrsEntry.get_r_mag());
				sedPhotometry.put(Band.i, panStarrsEntry.get_i_err() == 0 ? 0 : panStarrsEntry.get_i_mag());
				sedPhotometry.put(Band.z, panStarrsEntry.get_z_err() == 0 ? 0 : panStarrsEntry.get_z_mag());
				sedPhotometry.put(Band.y, panStarrsEntry.get_y_err() == 0 ? 0 : panStarrsEntry.get_y_mag());
			}
		}

		if (noirlabPhot.isSelected()) {
			NoirlabCatalogEntry noirlabEntry = new NoirlabCatalogEntry();
			noirlabEntry.setRa(catalogEntry.getRa());
			noirlabEntry.setDec(catalogEntry.getDec());
			noirlabEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(noirlabEntry, catalogQueryService, baseFrame);
			if (retrievedEntry != null) {
				noirlabEntry = (NoirlabCatalogEntry) retrievedEntry;
				seriesLabel.append(noirlabEntry.getCatalogName()).append(": ").append(noirlabEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.g, noirlabEntry.getCatalogName());
				sedCatalogs.put(Band.r, noirlabEntry.getCatalogName());
				sedCatalogs.put(Band.i, noirlabEntry.getCatalogName());
				sedCatalogs.put(Band.z, noirlabEntry.getCatalogName());
				sedCatalogs.put(Band.y, noirlabEntry.getCatalogName());
				addDecamReferences();
				sedPhotometry.put(Band.g, noirlabEntry.get_g_mag());
				sedPhotometry.put(Band.r, noirlabEntry.get_r_mag());
				sedPhotometry.put(Band.i, noirlabEntry.get_i_mag());
				sedPhotometry.put(Band.z, noirlabEntry.get_z_mag());
				sedPhotometry.put(Band.y, noirlabEntry.get_y_mag());
			}
		}

		if (desPhot.isSelected()) {
			DesCatalogEntry desEntry = new DesCatalogEntry();
			desEntry.setRa(catalogEntry.getRa());
			desEntry.setDec(catalogEntry.getDec());
			desEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(desEntry, catalogQueryService, baseFrame);
			if (retrievedEntry != null) {
				desEntry = (DesCatalogEntry) retrievedEntry;
				seriesLabel.append(desEntry.getCatalogName()).append(": ").append(desEntry.getSourceId()).append(" ");
				sedCatalogs.put(Band.g, desEntry.getCatalogName());
				sedCatalogs.put(Band.r, desEntry.getCatalogName());
				sedCatalogs.put(Band.i, desEntry.getCatalogName());
				sedCatalogs.put(Band.z, desEntry.getCatalogName());
				sedCatalogs.put(Band.y, desEntry.getCatalogName());
				addDecamReferences();
				sedPhotometry.put(Band.g, desEntry.get_g_caut() > 3 ? 0 : desEntry.get_g_mag());
				sedPhotometry.put(Band.r, desEntry.get_r_caut() > 3 ? 0 : desEntry.get_r_mag());
				sedPhotometry.put(Band.i, desEntry.get_i_caut() > 3 ? 0 : desEntry.get_i_mag());
				sedPhotometry.put(Band.z, desEntry.get_z_caut() > 3 ? 0 : desEntry.get_z_mag());
				sedPhotometry.put(Band.y, desEntry.get_y_caut() > 3 ? 0 : desEntry.get_y_mag());
			}
		}

		add2MassReferences();
		sedPhotometry.put(Band.J, 0d);
		sedPhotometry.put(Band.H, 0d);
		sedPhotometry.put(Band.K, 0d);

		if (twoMassPhot.isSelected()) {
			TwoMassCatalogEntry twoMassEntry = new TwoMassCatalogEntry();
			twoMassEntry.setRa(catalogEntry.getRa());
			twoMassEntry.setDec(catalogEntry.getDec());
			twoMassEntry.setSearchRadius(searchRadius * 2);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(twoMassEntry, catalogQueryService, baseFrame);
			if (retrievedEntry != null) {
				twoMassEntry = (TwoMassCatalogEntry) retrievedEntry;
				seriesLabel.append(twoMassEntry.getCatalogName()).append(": ").append(twoMassEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.J, twoMassEntry.getCatalogName());
				sedCatalogs.put(Band.H, twoMassEntry.getCatalogName());
				sedCatalogs.put(Band.K, twoMassEntry.getCatalogName());
				add2MassReferences();
				sedPhotometry.put(Band.J, twoMassEntry.getJ_err() == 0 ? 0 : twoMassEntry.getJmag());
				sedPhotometry.put(Band.H, twoMassEntry.getH_err() == 0 ? 0 : twoMassEntry.getHmag());
				sedPhotometry.put(Band.K, twoMassEntry.getK_err() == 0 ? 0 : twoMassEntry.getKmag());
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
				if (retrievedEntry != null) {
					ukidssEntry = (UkidssCatalogEntry) retrievedEntry;
					seriesLabel.append(ukidssEntry.getCatalogName()).append(": ").append(ukidssEntry.getSourceId())
							.append(" ");
					sedCatalogs.put(Band.J, ukidssEntry.getCatalogName());
					sedCatalogs.put(Band.H, ukidssEntry.getCatalogName());
					sedCatalogs.put(Band.K, ukidssEntry.getCatalogName());
					addUkidssReferences();
					sedPhotometry.put(Band.J, ukidssEntry.getJmag());
					sedPhotometry.put(Band.H, ukidssEntry.getHmag());
					sedPhotometry.put(Band.K, ukidssEntry.getKmag());
				}
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
				if (retrievedEntry != null) {
					uhsEntry = (UhsCatalogEntry) retrievedEntry;
					seriesLabel.append(uhsEntry.getCatalogName()).append(": ").append(uhsEntry.getSourceId())
							.append(" ");
					sedCatalogs.put(Band.J, uhsEntry.getCatalogName());
					sedCatalogs.put(Band.H, uhsEntry.getCatalogName());
					sedCatalogs.put(Band.K, uhsEntry.getCatalogName());
					addUkidssReferences();
					sedPhotometry.put(Band.J, uhsEntry.getJmag());
					sedPhotometry.put(Band.H, uhsEntry.getHmag());
					sedPhotometry.put(Band.K, uhsEntry.getKmag());
				}
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
				if (retrievedEntry != null) {
					vhsEntry = (VhsCatalogEntry) retrievedEntry;
					seriesLabel.append(vhsEntry.getCatalogName()).append(": ").append(vhsEntry.getSourceId())
							.append(" ");
					sedCatalogs.put(Band.J, vhsEntry.getCatalogName());
					sedCatalogs.put(Band.H, vhsEntry.getCatalogName());
					sedCatalogs.put(Band.K, vhsEntry.getCatalogName());
					addVhsReferences();
					sedPhotometry.put(Band.J, vhsEntry.getJmag());
					sedPhotometry.put(Band.H, vhsEntry.getHmag());
					sedPhotometry.put(Band.K, vhsEntry.getKmag());

				}
			}
		}

		if (allwisePhot.isSelected()) {
			AllWiseCatalogEntry allWiseEntry = new AllWiseCatalogEntry();
			allWiseEntry.setRa(catalogEntry.getRa());
			allWiseEntry.setDec(catalogEntry.getDec());
			allWiseEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(allWiseEntry, catalogQueryService, baseFrame);
			if (retrievedEntry != null) {
				allWiseEntry = (AllWiseCatalogEntry) retrievedEntry;
				seriesLabel.append(allWiseEntry.getCatalogName()).append(": ").append(allWiseEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.W1, allWiseEntry.getCatalogName());
				sedCatalogs.put(Band.W2, allWiseEntry.getCatalogName());
				sedCatalogs.put(Band.W3, allWiseEntry.getCatalogName());
				addWiseReferences();
				sedPhotometry.put(Band.W1, allWiseEntry.getW1_err() == 0 ? 0 : allWiseEntry.getW1mag());
				sedPhotometry.put(Band.W2, allWiseEntry.getW2_err() == 0 ? 0 : allWiseEntry.getW2mag());
				sedPhotometry.put(Band.W3, allWiseEntry.getW3_err() == 0 ? 0 : allWiseEntry.getW3mag());
			}
		}

		if (catwisePhot.isSelected()) {
			CatWiseCatalogEntry catWiseEntry = new CatWiseCatalogEntry();
			catWiseEntry.setRa(catalogEntry.getRa());
			catWiseEntry.setDec(catalogEntry.getDec());
			catWiseEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(catWiseEntry, catalogQueryService, baseFrame);
			if (retrievedEntry != null) {
				catWiseEntry = (CatWiseCatalogEntry) retrievedEntry;
				seriesLabel.append(catWiseEntry.getCatalogName()).append(": ").append(catWiseEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.W1, catWiseEntry.getCatalogName());
				sedCatalogs.put(Band.W2, catWiseEntry.getCatalogName());
				addWiseReferences();
				sedPhotometry.put(Band.W1, catWiseEntry.getW1mag());
				sedPhotometry.put(Band.W2, catWiseEntry.getW2mag());
			}
		}

		if (unwisePhot.isSelected()) {
			UnWiseCatalogEntry unWiseEntry = new UnWiseCatalogEntry();
			unWiseEntry.setRa(catalogEntry.getRa());
			unWiseEntry.setDec(catalogEntry.getDec());
			unWiseEntry.setSearchRadius(searchRadius);
			CatalogEntry retrievedEntry = retrieveCatalogEntry(unWiseEntry, catalogQueryService, baseFrame);
			if (retrievedEntry != null) {
				unWiseEntry = (UnWiseCatalogEntry) retrievedEntry;
				seriesLabel.append(unWiseEntry.getCatalogName()).append(": ").append(unWiseEntry.getSourceId())
						.append(" ");
				sedCatalogs.put(Band.W1, unWiseEntry.getCatalogName());
				sedCatalogs.put(Band.W2, unWiseEntry.getCatalogName());
				addWiseReferences();
				sedPhotometry.put(Band.W1, unWiseEntry.getW1mag());
				sedPhotometry.put(Band.W2, unWiseEntry.getW2mag());
			}
		}

		List<Band> sedBands = useGaiaPhotometry ? Band.getWdSedBands() : Band.getSedBands();

		sedBands.forEach(band -> {
			Double photometry = sedPhotometry.get(band);
			if (photometry != null) {
				SedReferences references = sedReferences.get(band);
				sedFluxes.put(band,
						new SedFluxes(photometry,
								convertMagnitudeToFluxDensity(photometry, references.getZeropoint(),
										references.getWavelenth()),
								convertMagnitudeToFluxJansky(photometry, references.getZeropoint()),
								convertMagnitudeToFluxLambda(photometry, references.getZeropoint(),
										references.getWavelenth())));
			}
		});

		XYSeries series = new XYSeries(seriesLabel.toString());

		sedDataPoints.append(seriesLabel.toString()).append(LINE_SEP);
		sedBands.forEach(band -> {
			Double photometry = sedPhotometry.get(band);
			if (photometry != null) {
				SedReferences references = sedReferences.get(band);
				SedFluxes fluxes = sedFluxes.get(band);
				series.add(references.getWavelenth(), photometry == 0 ? null : fluxes.getFluxLambda());
				if (photometry != 0) {
					sedDataPoints.append("(").append(references.getWavelenth()).append(",")
							.append(fluxes.getFluxLambda()).append(")").append(LINE_SEP);
				}
			}
		});

		if (collection == null) {
			collection = new XYSeriesCollection();
			collection.addSeries(series);
		} else {
			List<XYSeries> savedSeries = new ArrayList();
			savedSeries.addAll(collection.getSeries());
			collection.removeAllSeries();
			collection.addSeries(series);
			for (int y = 1; y < savedSeries.size(); y++) {
				collection.addSeries(savedSeries.get(y));
			}
		}

		if (addReferenceSeds) {
			addReferenceSeds(sedPhotometry, collection);
		}

		photSearchRadius.setCursor(Cursor.getDefaultCursor());
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

	private void addGaiaReferences() {
		sedReferences.put(Band.BP, new SedReferences(Sed.GAIA_BP.zeropoint, Sed.GAIA_BP.wavelenth));
		sedReferences.put(Band.G, new SedReferences(Sed.GAIA_G.zeropoint, Sed.GAIA_G.wavelenth));
		sedReferences.put(Band.RP, new SedReferences(Sed.GAIA_RP.zeropoint, Sed.GAIA_RP.wavelenth));
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

	private void addReferenceSeds(Map<Band, Double> sedPhotometry, XYSeriesCollection collection) {
		List<SedBestMatch> matches = new ArrayList();
		for (WhiteDwarfEntry entry : whiteDwarfEntries) {
			Map<Band, Double> bands = entry.getBands();
			String spectralType = entry.getInfo();
			List<Double> diffMags = new ArrayList();
			List<Band> sedBands = useGaiaPhotometry ? Band.getWdSedBands() : Band.getSedBands();
			sedBands.forEach(band -> {
				if (sedPhotometry.get(band) != null && sedPhotometry.get(band) != 0 && bands.get(band) != 0) {
					double diffMag = sedPhotometry.get(band) - bands.get(band);
					diffMags.add(diffMag);
				}
			});
			double medianDiffMag = determineMedian(diffMags);
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
			matches.add(new SedBestMatch(spectralType, medianDiffMag, meanDiffMag));
		}
		if (!matches.isEmpty()) {
			matches.sort(Comparator.comparing(SedBestMatch::getMeanDiffMag));
			int j = bestMatch.isSelected() ? 1 : 3;
			for (int i = 0; i < j && i < matches.size(); i++) {
				SedBestMatch match = matches.get(i);
				createReferenceSed(match.getSpt(), collection, match.getMedianDiffMag());
			}
		}
	}

	private void createReferenceSed(String spectralType, XYSeriesCollection collection, double medianDiffMag) {
		Map<Band, Double> magnitudes = provideReferenceMagnitudes(spectralType);
		if (magnitudes == null) {
			return;
		}
		if (!overplotTemplates.isSelected()) {
			medianDiffMag = 0;
		}
		XYSeries series = new XYSeries(spectralType);
		if (useGaiaPhotometry) {
			series.add(Sed.GAIA_BP.wavelenth,
					magnitudes.get(Band.BP) == 0 ? null
							: convertMagnitudeToFluxLambda(magnitudes.get(Band.BP) + medianDiffMag,
									Sed.GAIA_BP.zeropoint, Sed.GAIA_BP.wavelenth));
			series.add(Sed.GAIA_G.wavelenth,
					magnitudes.get(Band.G) == 0 ? null
							: convertMagnitudeToFluxLambda(magnitudes.get(Band.G) + medianDiffMag, Sed.GAIA_G.zeropoint,
									Sed.GAIA_G.wavelenth));
			series.add(Sed.GAIA_RP.wavelenth,
					magnitudes.get(Band.RP) == 0 ? null
							: convertMagnitudeToFluxLambda(magnitudes.get(Band.RP) + medianDiffMag,
									Sed.GAIA_RP.zeropoint, Sed.GAIA_RP.wavelenth));
		} else {
			series.add(Sed.PS1_G.wavelenth,
					magnitudes.get(Band.g) == 0 ? null
							: convertMagnitudeToFluxLambda(magnitudes.get(Band.g) + medianDiffMag, Sed.PS1_G.zeropoint,
									Sed.PS1_G.wavelenth));
			series.add(Sed.PS1_R.wavelenth,
					magnitudes.get(Band.r) == 0 ? null
							: convertMagnitudeToFluxLambda(magnitudes.get(Band.r) + medianDiffMag, Sed.PS1_R.zeropoint,
									Sed.PS1_R.wavelenth));
			series.add(Sed.PS1_I.wavelenth,
					magnitudes.get(Band.i) == 0 ? null
							: convertMagnitudeToFluxLambda(magnitudes.get(Band.i) + medianDiffMag, Sed.PS1_I.zeropoint,
									Sed.PS1_I.wavelenth));
			series.add(Sed.PS1_Z.wavelenth,
					magnitudes.get(Band.z) == 0 ? null
							: convertMagnitudeToFluxLambda(magnitudes.get(Band.z) + medianDiffMag, Sed.PS1_Z.zeropoint,
									Sed.PS1_Z.wavelenth));
			series.add(Sed.PS1_Y.wavelenth,
					magnitudes.get(Band.y) == 0 ? null
							: convertMagnitudeToFluxLambda(magnitudes.get(Band.y) + medianDiffMag, Sed.PS1_Y.zeropoint,
									Sed.PS1_Y.wavelenth));
		}
		series.add(Sed.MASS_J.wavelenth,
				magnitudes.get(Band.J) == 0 ? null
						: convertMagnitudeToFluxLambda(magnitudes.get(Band.J) + medianDiffMag, Sed.MASS_J.zeropoint,
								Sed.MASS_J.wavelenth));
		series.add(Sed.MASS_H.wavelenth,
				magnitudes.get(Band.H) == 0 ? null
						: convertMagnitudeToFluxLambda(magnitudes.get(Band.H) + medianDiffMag, Sed.MASS_H.zeropoint,
								Sed.MASS_H.wavelenth));
		series.add(Sed.MASS_K.wavelenth,
				magnitudes.get(Band.K) == 0 ? null
						: convertMagnitudeToFluxLambda(magnitudes.get(Band.K) + medianDiffMag, Sed.MASS_K.zeropoint,
								Sed.MASS_K.wavelenth));
		series.add(Sed.WISE_W1.wavelenth,
				magnitudes.get(Band.W1) == 0 ? null
						: convertMagnitudeToFluxLambda(magnitudes.get(Band.W1) + medianDiffMag, Sed.WISE_W1.zeropoint,
								Sed.WISE_W1.wavelenth));
		series.add(Sed.WISE_W2.wavelenth,
				magnitudes.get(Band.W2) == 0 ? null
						: convertMagnitudeToFluxLambda(magnitudes.get(Band.W2) + medianDiffMag, Sed.WISE_W2.zeropoint,
								Sed.WISE_W2.wavelenth));
		series.add(Sed.WISE_W3.wavelenth,
				magnitudes.get(Band.W3) == 0 ? null
						: convertMagnitudeToFluxLambda(magnitudes.get(Band.W3) + medianDiffMag, Sed.WISE_W3.zeropoint,
								Sed.WISE_W3.wavelenth));

		sedDataPoints.append(LINE_SEP).append(spectralType).append(":").append(LINE_SEP);
		for (Object item : series.getItems()) {
			XYDataItem dataItem = (XYDataItem) item;
			if (!Double.isNaN(dataItem.getYValue())) {
				sedDataPoints.append("(").append(dataItem.getXValue()).append(",").append(dataItem.getYValue())
						.append(")").append(LINE_SEP);
			}
		}

		try {
			collection.addSeries(series);
		} catch (IllegalArgumentException ex) {
		}
	}

	private JFreeChart createChart(XYSeriesCollection collection) {
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

		// XYSplineRenderer renderer = new XYSplineRenderer(100);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
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

		Font legendFont = new Font(FONT_NAME, Font.PLAIN, 18);
		chart.getLegend().setFrame(BlockBorder.NONE);
		chart.getLegend().setItemFont(legendFont);

		Font titleFont = new Font(FONT_NAME, Font.PLAIN, 22);
		chart.getTitle().setFont(titleFont);

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
		List<Band> sedBands = useGaiaPhotometry ? Band.getWdSedBands() : Band.getSedBands();
		sedBands.forEach(band -> {
			SedFluxes fluxes = sedFluxes.get(band);
			if (fluxes != null) {
				toolTips.add(html(sedCatalogs.get(band) + " " + band.val + "=" + roundTo3DecNZ(fluxes.getMagnitude())
						+ " mag<br>" + "λ=" + sedReferences.get(band).getWavelenth() + " μm<br>" + "F(ν)="
						+ roundTo3DecSN(fluxes.getFluxJansky()) + " Jy<br>" + "λF(λ)="
						+ roundTo3DecSN(fluxes.getFluxDensity()) + " W/m²<br>" + "F(λ)="
						+ roundTo3DecSN(fluxes.getFluxLambda()) + " W/m²/μm"));
			}
		});
		CustomXYToolTipGenerator generator = new CustomXYToolTipGenerator();
		generator.addToolTipSeries(toolTips);
		return generator;
	}

	private Map<Band, Double> provideReferenceMagnitudes(String spt) {
		Map<Band, Double> absoluteMagnitudes = null;
		for (WhiteDwarfEntry entry : whiteDwarfEntries) {
			if (entry.getInfo().equals(spt)) {
				absoluteMagnitudes = entry.getBands();
			}
		}
		return absoluteMagnitudes;
	}

	public void createWhiteDwarfSedEntries() {
		InputStream input = getClass().getResourceAsStream("/WhiteDwarfLookupTable.csv");
		try (Scanner fileScanner = new Scanner(input)) {
			String headerLine = fileScanner.nextLine();
			String[] headers = CSVParser.parseLine(headerLine);
			Map<String, Integer> columns = new HashMap<>();
			for (int i = 0; i < headers.length; i++) {
				columns.put(headers[i], i);
			}
			while (fileScanner.hasNextLine()) {
				String bodyLine = fileScanner.nextLine();
				String[] values = CSVParser.parseLine(bodyLine);
				String type = values[columns.get("Type")];
				int teff = toInteger(values[columns.get("Teff")]);
				double logG = toDouble(values[columns.get("log g")]);
				double mass = toDouble(values[columns.get("M/Mo")]);
				String age = values[columns.get("Age")];
				double Gmag = toDouble(values[columns.get("G3")]);
				double BPmag = toDouble(values[columns.get("G3_BP")]);
				double RPmag = toDouble(values[columns.get("G3_RP")]);
				double g_mag = toDouble(values[columns.get("PS1_g")]);
				double r_mag = toDouble(values[columns.get("PS1_r")]);
				double i_mag = toDouble(values[columns.get("PS1_i")]);
				double z_mag = toDouble(values[columns.get("PS1_z")]);
				double y_mag = toDouble(values[columns.get("PS1_y")]);
				double Jmag = toDouble(values[columns.get("2MASS_J")]);
				double Hmag = toDouble(values[columns.get("2MASS_H")]);
				double Kmag = toDouble(values[columns.get("2MASS_Ks")]);
				double W1mag = toDouble(values[columns.get("W1")]);
				double W2mag = toDouble(values[columns.get("W2")]);
				double W3mag = toDouble(values[columns.get("W3")]);
				double W4mag = toDouble(values[columns.get("W4")]);
				Map<Band, Double> bands = new HashMap();
				bands.put(Band.G, Gmag);
				bands.put(Band.BP, BPmag);
				bands.put(Band.RP, RPmag);
				bands.put(Band.g, g_mag);
				bands.put(Band.r, r_mag);
				bands.put(Band.i, i_mag);
				bands.put(Band.z, z_mag);
				bands.put(Band.y, y_mag);
				bands.put(Band.J, Jmag);
				bands.put(Band.H, Hmag);
				bands.put(Band.K, Kmag);
				bands.put(Band.W1, W1mag);
				bands.put(Band.W2, W2mag);
				bands.put(Band.W3, W3mag);
				bands.put(Band.W4, W4mag);
				whiteDwarfEntries.add(new WhiteDwarfEntry(type, teff, logG, mass, age, bands));
			}
		}
	}

}
