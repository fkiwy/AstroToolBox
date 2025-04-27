package astro.tool.box.tab;

import static astro.tool.box.function.AstrometricFunctions.calculateAngularDistance;
import static astro.tool.box.function.AstrometricFunctions.calculatePositionFromProperMotion;
import static astro.tool.box.function.AstrometricFunctions.convertMJDToDateTime;
import static astro.tool.box.function.AstrometricFunctions.convertDateTimeToMJD;
import static astro.tool.box.function.NumericFunctions.roundTo2DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3Dec;
import static astro.tool.box.function.NumericFunctions.roundTo3DecLZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo7Dec;
import static astro.tool.box.function.NumericFunctions.roundTo7DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo7DecNZLZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.function.NumericFunctions.toInteger;
import static astro.tool.box.function.PhotometricFunctions.isAPossibleAGN;
import static astro.tool.box.function.PhotometricFunctions.isAPossibleWD;
import static astro.tool.box.function.StatisticFunctions.determineMedian;
import static astro.tool.box.main.ToolboxHelper.AGN_WARNING;
import static astro.tool.box.main.ToolboxHelper.BASE_FRAME_HEIGHT;
import static astro.tool.box.main.ToolboxHelper.BASE_FRAME_WIDTH;
import static astro.tool.box.main.ToolboxHelper.BUFFER_SIZE;
import static astro.tool.box.main.ToolboxHelper.INFO_ICON;
import static astro.tool.box.main.ToolboxHelper.PHOT_DIST_INFO;
import static astro.tool.box.main.ToolboxHelper.WD_WARNING;
import static astro.tool.box.main.ToolboxHelper.addEmptyCatalogElement;
import static astro.tool.box.main.ToolboxHelper.addFieldToPanel;
import static astro.tool.box.main.ToolboxHelper.addLabelToPanel;
import static astro.tool.box.main.ToolboxHelper.addTextToImage;
import static astro.tool.box.main.ToolboxHelper.alignResultColumns;
import static astro.tool.box.main.ToolboxHelper.collectObject;
import static astro.tool.box.main.ToolboxHelper.copyCoordsToClipboard;
import static astro.tool.box.main.ToolboxHelper.copyImage;
import static astro.tool.box.main.ToolboxHelper.copyObjectCoordinates;
import static astro.tool.box.main.ToolboxHelper.copyObjectInfo;
import static astro.tool.box.main.ToolboxHelper.copyObjectSummary;
import static astro.tool.box.main.ToolboxHelper.copyToClipboard;
import static astro.tool.box.main.ToolboxHelper.createEmptyBorder;
import static astro.tool.box.main.ToolboxHelper.createHeaderBox;
import static astro.tool.box.main.ToolboxHelper.createHeaderLabel;
import static astro.tool.box.main.ToolboxHelper.createHyperlink;
import static astro.tool.box.main.ToolboxHelper.createLabel;
import static astro.tool.box.main.ToolboxHelper.createMessageLabel;
import static astro.tool.box.main.ToolboxHelper.drawCenterShape;
import static astro.tool.box.main.ToolboxHelper.fillTygoForm;
import static astro.tool.box.main.ToolboxHelper.flipImage;
import static astro.tool.box.main.ToolboxHelper.getChildWindowAdapter;
import static astro.tool.box.main.ToolboxHelper.getCoordinates;
import static astro.tool.box.main.ToolboxHelper.getEpoch;
import static astro.tool.box.main.ToolboxHelper.getImageLabel;
import static astro.tool.box.main.ToolboxHelper.getMeanEpoch;
import static astro.tool.box.main.ToolboxHelper.getNearestZooniverseSubjects;
import static astro.tool.box.main.ToolboxHelper.getPs1Epoch;
import static astro.tool.box.main.ToolboxHelper.getPs1Epochs;
import static astro.tool.box.main.ToolboxHelper.getPs1FileNames;
import static astro.tool.box.main.ToolboxHelper.getToolBoxImage;
import static astro.tool.box.main.ToolboxHelper.getWiseTiles;
import static astro.tool.box.main.ToolboxHelper.html;
import static astro.tool.box.main.ToolboxHelper.isSameTarget;
import static astro.tool.box.main.ToolboxHelper.retrieveDesiImage;
import static astro.tool.box.main.ToolboxHelper.retrieveImage;
import static astro.tool.box.main.ToolboxHelper.retrieveNearInfraredImages;
import static astro.tool.box.main.ToolboxHelper.retrievePs1Image;
import static astro.tool.box.main.ToolboxHelper.rotateImage;
import static astro.tool.box.main.ToolboxHelper.showErrorDialog;
import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;
import static astro.tool.box.main.ToolboxHelper.showInfoDialog;
import static astro.tool.box.main.ToolboxHelper.writeErrorLog;
import static astro.tool.box.main.ToolboxHelper.zoomImage;
import static astro.tool.box.tab.SettingsTab.COMMENTS;
import static astro.tool.box.tab.SettingsTab.CUTOUT_SERVICE;
import static astro.tool.box.tab.SettingsTab.NEAREST_BYW_SUBJECTS;
import static astro.tool.box.tab.SettingsTab.PROP_PATH;
import static astro.tool.box.tab.SettingsTab.USER_SETTINGS;
import static astro.tool.box.tab.SettingsTab.getUserSetting;
import static astro.tool.box.util.Constants.ALLWISE_EPOCH;
import static astro.tool.box.util.Constants.CUTOUT_SERVICE_URL;
import static astro.tool.box.util.Constants.DATE_FORMATTER;
import static astro.tool.box.util.Constants.DESI_FILTERS;
import static astro.tool.box.util.Constants.DESI_LS_DR_LABEL;
import static astro.tool.box.util.Constants.DESI_LS_DR_PARAM;
import static astro.tool.box.util.Constants.DESI_LS_EPOCH;
import static astro.tool.box.util.Constants.LINE_BREAK;
import static astro.tool.box.util.Constants.LINE_SEP;
import static astro.tool.box.util.Constants.LINE_SEP_TEXT_AREA;
import static astro.tool.box.util.Constants.PIXEL_SCALE_DECAM;
import static astro.tool.box.util.Constants.PIXEL_SCALE_PS1;
import static astro.tool.box.util.Constants.PIXEL_SCALE_WISE;
import static astro.tool.box.util.Constants.SDSS_BASE_URL;
import static astro.tool.box.util.Constants.SDSS_LABEL;
import static astro.tool.box.util.Constants.SPITZER_EPOCH;
import static astro.tool.box.util.Constants.SPLIT_CHAR;
import static astro.tool.box.util.Constants.TAP_URL_PARAMS;
import static astro.tool.box.util.Constants.UHS_LABEL;
import static astro.tool.box.util.Constants.UHS_SURVEY_URL;
import static astro.tool.box.util.Constants.UKIDSS_LABEL;
import static astro.tool.box.util.Constants.UKIDSS_SURVEY_URL;
import static astro.tool.box.util.Constants.VHS_LABEL;
import static astro.tool.box.util.Constants.VHS_SURVEY_URL;
import static astro.tool.box.util.ConversionFactors.DEG_ARCSEC;
import static astro.tool.box.util.ConversionFactors.DEG_MAS;
import static astro.tool.box.util.ExternalResources.getAladinLiteUrl;
import static astro.tool.box.util.ExternalResources.getFinderChartUrl;
import static astro.tool.box.util.ExternalResources.getLegacySkyViewerUrl;
import static astro.tool.box.util.ExternalResources.getPanstarrsUrl;
import static astro.tool.box.util.ExternalResources.getSimbadUrl;
import static astro.tool.box.util.ExternalResources.getVizierUrl;
import static astro.tool.box.util.ExternalResources.getWiseViewUrl;
import static astro.tool.box.util.MiscUtils.encodeQuery;
import static astro.tool.box.util.ServiceHelper.createVizieRUrl;
import static astro.tool.box.util.ServiceHelper.establishHttpConnection;
import static astro.tool.box.util.ServiceHelper.readResponse;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.ceil;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static java.util.stream.Collectors.toList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultCaret;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.Artifact;
import astro.tool.box.catalog.CatWiseCatalogEntry;
import astro.tool.box.catalog.CatWiseRejectEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.DesCatalogEntry;
import astro.tool.box.catalog.Extinction;
import astro.tool.box.catalog.GaiaCmd;
import astro.tool.box.catalog.GaiaDR2CatalogEntry;
import astro.tool.box.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.catalog.GaiaWDCatalogEntry;
import astro.tool.box.catalog.GenericCatalogEntry;
import astro.tool.box.catalog.MocaCatalogEntry;
import astro.tool.box.catalog.NoirlabCatalogEntry;
import astro.tool.box.catalog.PanStarrsCatalogEntry;
import astro.tool.box.catalog.ProperMotionQuery;
import astro.tool.box.catalog.SdssCatalogEntry;
import astro.tool.box.catalog.SimbadCatalogEntry;
import astro.tool.box.catalog.SsoCatalogEntry;
import astro.tool.box.catalog.TessCatalogEntry;
import astro.tool.box.catalog.TwoMassCatalogEntry;
import astro.tool.box.catalog.UhsCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.catalog.WhiteDwarf;
import astro.tool.box.component.TextPrompt;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.Couple;
import astro.tool.box.container.CustomOverlay;
import astro.tool.box.container.Epoch;
import astro.tool.box.container.FlipbookComponent;
import astro.tool.box.container.ImageContainer;
import astro.tool.box.container.NirImage;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.Overlays;
import astro.tool.box.container.Tile;
import astro.tool.box.enumeration.ImageType;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.ObjectType;
import astro.tool.box.enumeration.Shape;
import astro.tool.box.enumeration.WiseBand;
import astro.tool.box.exception.ExtinctionException;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.DistanceLookupResult;
import astro.tool.box.lookup.LookupResult;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.lookup.SpectralTypeLookupEntry;
import astro.tool.box.main.Application;
import astro.tool.box.main.ImageSeriesPdf;
import astro.tool.box.panel.GaiaCmdPanel;
import astro.tool.box.panel.ReferencesPanel;
import astro.tool.box.panel.SedMsPanel;
import astro.tool.box.panel.SedWdPanel;
import astro.tool.box.panel.WiseCcdPanel;
import astro.tool.box.panel.WiseLcPanel;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.DistanceLookupService;
import astro.tool.box.service.DustExtinctionService;
import astro.tool.box.service.SpectralTypeLookupService;
import astro.tool.box.shape.Arrow;
import astro.tool.box.shape.Circle;
import astro.tool.box.shape.Cross;
import astro.tool.box.shape.CrossHair;
import astro.tool.box.shape.Diamond;
import astro.tool.box.shape.Disk;
import astro.tool.box.shape.Drawable;
import astro.tool.box.shape.Square;
import astro.tool.box.shape.Triangle;
import astro.tool.box.shape.XCross;
import astro.tool.box.util.CSVParser;
import astro.tool.box.util.Counter;
import astro.tool.box.util.FileTypeFilter;
import astro.tool.box.util.GifSequencer;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;

public class ImageViewerTab implements Tab {

	public static final String TAB_NAME = "Image Viewer";
	public static final WiseBand WISE_BAND = WiseBand.W1W2;
	public static final double OVERLAP_FACTOR = 0.9;
	public static final int NUMBER_OF_WISE_EPOCHS = 10;
	public static final int NUMBER_OF_UNWISE_EPOCHS = 8;
	public static final int WINDOW_SPACING = 25;
	public static final int CATALOG_PANEL_WIDTH = 700;
	public static final int PANEL_HEIGHT = 220;
	public static final int PANEL_WIDTH = 180;
	public static final int ROW_HEIGHT = 25;
	public static final int EPOCH_GAP = 6;
	public static final int SPEED = 200;
	public static final int ZOOM = 500;
	public static final int SIZE = 100;
	public static final int DIFFERENT_SIZE = 100;
	public static final int PROPER_MOTION = 100;
	public static final String OVERLAYS_KEY = "overlays";
	public static final String CHANGE_FOV_TEXT = "Current field of view: %d\" " + INFO_ICON;

	// Reference epochs:
	// AllWISE: 2010.559
	// CatWISE2020: 2015.405 -> CatWISE2020 - AllWISE = 4.846
	// Gaia DR2: 2015.5 -> Gaia DR2 - AllWISE = 4.941
	// Gaia DR3: 2016.0 -> Gaia DR3 - AllWISE = 5.441
	public static final double ALLWISE_REFERENCE_EPOCH = 2010.559;
	public static final double CATWISE_ALLWISE_EPOCH_DIFF = 4.846;
	public static final double GAIADR2_ALLWISE_EPOCH_DIFF = 4.941;
	public static final double GAIADR3_ALLWISE_EPOCH_DIFF = 5.441;

	private final JFrame baseFrame;
	private final JTabbedPane tabbedPane;

	private final CatalogQueryService catalogQueryService;
	private final SpectralTypeLookupService mainSequenceSpectralTypeLookupService;
	private final SpectralTypeLookupService brownDwarfsSpectralTypeLookupService;
	private final DistanceLookupService distanceLookupService;
	private final DustExtinctionService dustExtinctionService;
	private final List<SpectralTypeLookup> brownDwarfLookupEntries;
	private final Overlays overlays;
	private List<CatalogEntry> simbadEntries;
	private List<CatalogEntry> allWiseEntries;
	private List<CatalogEntry> catWiseEntries;
	private List<CatalogEntry> catWiseTpmEntries;
	private List<CatalogEntry> catWiseRejectEntries;
	private List<CatalogEntry> unWiseEntries;
	private List<CatalogEntry> gaiaEntries;
	private List<CatalogEntry> gaiaTpmEntries;
	private List<CatalogEntry> gaiaDR3Entries;
	private List<CatalogEntry> gaiaDR3TpmEntries;
	private List<CatalogEntry> noirlabEntries;
	private List<CatalogEntry> noirlabTpmEntries;
	private List<CatalogEntry> panStarrsEntries;
	private List<CatalogEntry> sdssEntries;
	private List<CatalogEntry> vhsEntries;
	private List<CatalogEntry> uhsEntries;
	private List<CatalogEntry> ukidssEntries;
	private List<CatalogEntry> ukidssTpmEntries;
	private List<CatalogEntry> twoMassEntries;
	private List<CatalogEntry> tessEntries;
	private List<CatalogEntry> desEntries;
	private List<CatalogEntry> gaiaWDEntries;
	private List<CatalogEntry> mocaEntries;
	private List<CatalogEntry> ssoEntries;

	private JPanel imagePanel;
	private JPanel rightPanel;
	private JPanel bywTopRow;
	private JPanel bywBottomRow;
	private JLabel panstarrsLabel;
	private JLabel aladinLiteLabel;
	private JLabel wiseViewLabel;
	private JLabel finderChartLabel;
	private JLabel legacyViewerLabel;
	private JLabel ukidssCutoutsLabel;
	private JLabel vhsCutoutsLabel;
	private JLabel simbadLabel;
	private JLabel vizierLabel;
	private JLabel changeFovLabel;
	private JButton changeFovButton;
	private JButton stopDownloadButton;
	private JRadioButton wiseviewCutouts;
	private JRadioButton unwiseCutouts;
	private JRadioButton desiCutouts;
	private JRadioButton ps1Cutouts;
	private JRadioButton showCatalogsButton;
	private JScrollPane rightScrollPanel;
	private JCheckBox differenceImaging;
	private JCheckBox skipIntermediateEpochs;
	private JCheckBox separateScanDirections;
	private JCheckBox resetContrast;
	private JCheckBox skipBadImages;
	private JCheckBox blurImages;
	private JCheckBox invertColors;
	private JCheckBox borderFirst;
	private JCheckBox staticView;
	private JCheckBox markTarget;
	private JCheckBox showCrosshairs;
	private JCheckBox simbadOverlay;
	private JCheckBox allWiseOverlay;
	private JCheckBox catWiseOverlay;
	private JCheckBox unWiseOverlay;
	private JCheckBox gaiaOverlay;
	private JCheckBox gaiaDR3Overlay;
	private JCheckBox noirlabOverlay;
	private JCheckBox panStarrsOverlay;
	private JCheckBox sdssOverlay;
	private JCheckBox spectrumOverlay;
	private JCheckBox vhsOverlay;
	private JCheckBox uhsOverlay;
	private JCheckBox ukidssOverlay;
	private JCheckBox twoMassOverlay;
	private JCheckBox tessOverlay;
	private JCheckBox desOverlay;
	private JCheckBox gaiaWDOverlay;
	private JCheckBox mocaOverlay;
	private JCheckBox ssoOverlay;
	private JCheckBox ghostOverlay;
	private JCheckBox haloOverlay;
	private JCheckBox latentOverlay;
	private JCheckBox spikeOverlay;
	private JCheckBox gaiaProperMotion;
	private JCheckBox gaiaDR3ProperMotion;
	private JCheckBox noirlabProperMotion;
	private JCheckBox catWiseProperMotion;
	private JCheckBox ukidssProperMotion;
	private JCheckBox showProperMotion;
	private JCheckBox useCustomOverlays;
	private JCheckBox dssImageSeries;
	private JCheckBox twoMassImageSeries;
	private JCheckBox sdssImageSeries;
	private JCheckBox spitzerImageSeries;
	private JCheckBox allwiseImageSeries;
	private JCheckBox ukidssImageSeries;
	private JCheckBox uhsImageSeries;
	private JCheckBox vhsImageSeries;
	private JCheckBox panstarrsImageSeries;
	private JCheckBox legacyImageSeries;
	private JCheckBox staticTimeSeries;
	private JCheckBox animatedTimeSeries;
	private JCheckBox imageSeriesPdf;
	private JCheckBox drawCrosshairs;
	private JComboBox wiseBands;
	private JSlider brightnessSlider;
	private JSlider contrastSlider;
	private JSlider speedSlider;
	private JSlider zoomSlider;
	private JSlider stackSlider;
	private JTextField coordsField;
	private JTextField sizeField;
	private JTextField properMotionField;
	private JTextField differentSizeField;
	private JTextField panstarrsField;
	private JTextField aladinLiteField;
	private JTextField wiseViewField;
	private JTextField finderChartField;
	private JTextArea crosshairCoords;
	private JTextArea downloadLog;
	private JTable collectionTable;
	private JTable currentTable;
	private Timer timer;

	private BufferedImage wiseImage;
	private BufferedImage desiImage;
	private BufferedImage ps1Image;
	private BufferedImage vhsImage;
	private BufferedImage uhsImage;
	private BufferedImage ukidssImage;
	private BufferedImage sdssImage;
	private BufferedImage dssImage;
	private BufferedImage processedDesiImage;
	private BufferedImage processedPs1Image;
	private BufferedImage processedVhsImage;
	private BufferedImage processedUhsImage;
	private BufferedImage processedUkidssImage;
	private BufferedImage processedSdssImage;
	private BufferedImage processedDssImage;
	private Map<String, ImageContainer> imagesW1 = new HashMap();
	private Map<String, ImageContainer> imagesW2 = new HashMap();
	private Map<String, ImageContainer> imagesW1All = new HashMap();
	private Map<String, ImageContainer> imagesW2All = new HashMap();
	private Map<String, ImageContainer> imagesW1Ends = new HashMap();
	private Map<String, ImageContainer> imagesW2Ends = new HashMap();
	private Map<String, CustomOverlay> customOverlays;
	private List<NumberPair> crosshairs = new ArrayList();
	private List<Fits> band1Images = new ArrayList();
	private List<Fits> band2Images = new ArrayList();
	private List<FlipbookComponent> flipbook = new ArrayList();
	private ImageViewerTab imageViewer;

	private Tile tile;
	private WiseBand wiseBand = WISE_BAND;
	private double pixelScale = PIXEL_SCALE_WISE;
	private int fieldOfView = 30;
	private int shapeSize = 5;
	private int stackSize = 1;
	private int imageNumber;
	private int imageCount;
	private int windowShift;
	private int quadrantCount;
	private int epochCount;
	private int brightness;
	private int contrast;
	private int minValue;
	private int maxValue;
	private int speed = SPEED;
	private int zoom = ZOOM;
	private int size = SIZE;

	private int year_ps1_y_i_g;
	private int year_vhs_k_h_j;
	private int year_uhs_k_j;
	private int year_ukidss_k_h_j;
	// private int year_sdss_z_g_u;
	private int year_dss_2ir_1r_1b;

	private double targetRa;
	private double targetDec;

	private double crval1;
	private double crval2;

	private double crpix1;
	private double crpix2;

	private int naxis1;
	private int naxis2;

	private int pointerX;
	private int pointerY;

	private int componentIndex;

	private int previousSize;
	private double previousRa;
	private double previousDec;

	private boolean loadImages;
	private boolean stopDownloadProcess;
	private boolean flipbookComplete;
	private boolean imageCutOff;
	private boolean timerStopped;
	private boolean hasException;
	private final boolean nearestBywSubjects;
	private boolean asyncDownloads;
	private boolean legacyImages;
	private boolean panstarrsImages;
	private boolean vhsImages;
	private boolean uhsImages;
	private boolean ukidssImages;
	private boolean sdssImages;
	private boolean dssImages;
	private boolean waitCursor = true;

	public ImageViewerTab(JFrame baseFrame, JTabbedPane tabbedPane) {
		this.baseFrame = baseFrame;
		this.tabbedPane = tabbedPane;
		catalogQueryService = new CatalogQueryService();
		dustExtinctionService = new DustExtinctionService();
		try (InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv")) {
			Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines();
			List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
				return new SpectralTypeLookupEntry(line.split(",", -1));
			}).collect(toList());
			mainSequenceSpectralTypeLookupService = new SpectralTypeLookupService(entries);
		} catch (IOException e) {
			showExceptionDialog(baseFrame, e);
			throw new RuntimeException(e);
		}
		try (InputStream input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv")) {
			Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines();
			brownDwarfLookupEntries = stream.skip(1).map(line -> {
				return new BrownDwarfLookupEntry(line.split(",", -1));
			}).collect(toList());
			brownDwarfsSpectralTypeLookupService = new SpectralTypeLookupService(brownDwarfLookupEntries);
			distanceLookupService = new DistanceLookupService(brownDwarfLookupEntries);
		} catch (IOException e) {
			showExceptionDialog(baseFrame, e);
			throw new RuntimeException(e);
		}
		overlays = new Overlays();
		overlays.deserialize(getUserSetting(OVERLAYS_KEY, overlays.serialize()));
		nearestBywSubjects = Boolean.parseBoolean(getUserSetting(NEAREST_BYW_SUBJECTS, "true"));
	}

	@Override
	public void init(boolean visible) {
		try {
			JPanel mainPanel = new JPanel(new BorderLayout());

			JPanel leftPanel = new JPanel();
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
			leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

			JTabbedPane controlTabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
			leftPanel.add(controlTabs);

			imagePanel = new JPanel();
			imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));

			JScrollPane imageScrollPanel = new JScrollPane(imagePanel);
			imageScrollPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, imageScrollPanel);
			mainPanel.add(splitPane, BorderLayout.CENTER);

			rightPanel = new JPanel();
			rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

			rightScrollPanel = new JScrollPane(rightPanel);
			rightScrollPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			rightScrollPanel.setPreferredSize(new Dimension(230, rightPanel.getHeight()));

			mainPanel.add(rightScrollPanel, BorderLayout.EAST);

			// ===================
			// Tab: Main controls
			// ===================
			int rows = 39;
			if (nearestBywSubjects) {
				rows += 3;
			}
			int controlPanelWidth = 255;
			int controlPanelHeight = 10 + ROW_HEIGHT * rows;

			JPanel mainControlPanel = new JPanel(new GridLayout(rows, 1));
			mainControlPanel.setPreferredSize(new Dimension(controlPanelWidth - 20, controlPanelHeight));
			mainControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

			JScrollPane mainScrollPanel = new JScrollPane(mainControlPanel);
			mainScrollPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));
			mainScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
			controlTabs.add("Controls", mainScrollPanel);

			mainControlPanel.add(new JLabel("Target coordinates:"));

			coordsField = new JTextField();
			mainControlPanel.add(coordsField);
			TextPrompt coordsFieldPrompt = new TextPrompt("Enter coordinates and press Enter");
			coordsFieldPrompt.applyTo(coordsField);
			coordsField.addActionListener((ActionEvent evt) -> {
				createFlipbook();
			});

			mainControlPanel.add(new JLabel("Field of view (arcsec):"));

			sizeField = new JTextField(String.valueOf(size));
			mainControlPanel.add(sizeField);
			sizeField.addActionListener((ActionEvent evt) -> {
				createFlipbook();
			});

			mainControlPanel.add(new JLabel("Band:"));

			wiseBands = new JComboBox(WiseBand.values());
			mainControlPanel.add(wiseBands);
			wiseBands.setSelectedItem(wiseBand);
			wiseBands.addActionListener((ActionEvent evt) -> {
				wiseBand = (WiseBand) wiseBands.getSelectedItem();
				loadImages = true;
				createFlipbook();
			});

			mainControlPanel.add(new JLabel("Brightness:"));

			brightnessSlider = new JSlider(1, 100, 1);
			mainControlPanel.add(brightnessSlider);
			brightnessSlider.addChangeListener((ChangeEvent e) -> {
				brightness = brightnessSlider.getValue();
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					return;
				}
				createFlipbook();
			});

			mainControlPanel.add(new JLabel("Contrast:"));

			contrastSlider = new JSlider(1, 100, 50);
			mainControlPanel.add(contrastSlider);
			contrastSlider.addChangeListener((ChangeEvent e) -> {
				contrast = contrastSlider.getValue();
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					return;
				}
				createFlipbook();
			});

			JLabel zoomLabel = new JLabel("Zoom: %d".formatted(zoom));
			mainControlPanel.add(zoomLabel);

			zoomSlider = new JSlider(100, 2000, ZOOM);
			mainControlPanel.add(zoomSlider);
			zoomSlider.addChangeListener((ChangeEvent e) -> {
				zoom = zoomSlider.getValue();
				zoomLabel.setText("Zoom: %d".formatted(zoom));
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					return;
				}
				processImages();
			});

			JLabel speedLabel = new JLabel("Blink interval: %d ms".formatted(speed));
			mainControlPanel.add(speedLabel);

			speedSlider = new JSlider(0, 2000, SPEED);
			mainControlPanel.add(speedSlider);
			speedSlider.addChangeListener((ChangeEvent e) -> {
				speed = speedSlider.getValue();
				speedLabel.setText("Blink interval: %d ms".formatted(speed));
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					return;
				}
				timer.setDelay(speed);
				processImages();
			});

			wiseviewCutouts = new JRadioButton(html("WISE cutouts (sep. scan) " + INFO_ICON), true);
			wiseviewCutouts.setToolTipText(
					"WISE cutouts are from http://byw.tools/wiseview and have separate scan directions,\nwhich can be activated by ticking the 'Separate scan directions' checkbox.");
			wiseviewCutouts.addActionListener((ActionEvent evt) -> {
				pixelScale = PIXEL_SCALE_WISE;
				previousSize = 0;
				createFlipbook();
			});

			unwiseCutouts = new JRadioButton(html("unWISE deep coadds " + INFO_ICON));
			unwiseCutouts.setToolTipText(
					"unWISE deep coadds are from https://unwise.me and do not have separate scan directions.\nSeveral epochs are stacked together so that high proper motion objects may look smeared.");
			unwiseCutouts.addActionListener((ActionEvent evt) -> {
				pixelScale = PIXEL_SCALE_WISE;
				previousSize = 0;
				createFlipbook();
			});

			String stackText = "Images per blink: %d";
			JLabel stackLabel = new JLabel(stackText.formatted(stackSize));
			mainControlPanel.add(stackLabel);

			stackSlider = new JSlider(1, getNumberOfWiseEpochs(), 1);
			mainControlPanel.add(stackSlider);
			stackSlider.addChangeListener((ChangeEvent e) -> {
				stackSize = stackSlider.getValue();
				stackLabel.setText(stackText.formatted(stackSize));
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					return;
				}
				if (skipIntermediateEpochs.isSelected()) {
					skipIntermediateEpochs.setSelected(false);
					loadImages = true;
				}
				createFlipbook();
			});

			skipIntermediateEpochs = new JCheckBox("Skip intermediate epochs", true);
			mainControlPanel.add(skipIntermediateEpochs);
			skipIntermediateEpochs.addActionListener((ActionEvent evt) -> {
				if (skipIntermediateEpochs.isSelected()) {
					if (skipBadImages.isSelected()) {
						skipBadImages.setSelected(false);
					}
					imagesW1.clear();
					imagesW2.clear();
					imagesW1.putAll(imagesW1Ends);
					imagesW2.putAll(imagesW2Ends);
					if (stackSlider.getValue() > 1) {
						ChangeListener actionListener = stackSlider.getChangeListeners()[0];
						stackSlider.removeChangeListener(actionListener);
						stackSlider.setValue(1);
						stackSlider.addChangeListener(actionListener);
						stackSize = 1;
						stackLabel.setText(stackText.formatted(stackSize));
					}
				} else {
					if (!imagesW1All.isEmpty()) {
						imagesW1.putAll(imagesW1All);
					}
					if (!imagesW2All.isEmpty()) {
						imagesW2.putAll(imagesW2All);
					}
				}
				loadImages = true;
				createFlipbook();
			});

			separateScanDirections = new JCheckBox("Separate scan directions");
			mainControlPanel.add(separateScanDirections);
			separateScanDirections.addActionListener((ActionEvent evt) -> {
				createFlipbook();
			});

			differenceImaging = new JCheckBox("Difference imaging");
			mainControlPanel.add(differenceImaging);
			differenceImaging.addActionListener((ActionEvent evt) -> {
				if (differenceImaging.isSelected()) {
					separateScanDirections.setSelected(true);
					blurImages.setSelected(true);
					brightnessSlider.setEnabled(false);
					separateScanDirections.setEnabled(false);
				} else {
					separateScanDirections.setSelected(false);
					blurImages.setSelected(false);
					brightnessSlider.setEnabled(true);
					separateScanDirections.setEnabled(true);
				}
				if (resetContrast.isSelected()) {
					resetContrastSlider();
				}
				createFlipbook();
			});

			resetContrast = new JCheckBox("Auto-reset brightness & contrast", true);
			mainControlPanel.add(resetContrast);
			resetContrast.addActionListener((ActionEvent evt) -> {
				if (resetContrast.isSelected()) {
					resetContrastSlider();
					createFlipbook();
				}
			});

			skipBadImages = new JCheckBox("Skip poor quality images");
			mainControlPanel.add(skipBadImages);
			skipBadImages.addActionListener((ActionEvent evt) -> {
				if (skipBadImages.isSelected() && skipIntermediateEpochs.isSelected()) {
					skipIntermediateEpochs.setSelected(false);
				}
				previousSize = -1;
				createFlipbook();
			});

			JPanel settingsPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(settingsPanel);

			blurImages = new JCheckBox("Blur images");
			settingsPanel.add(blurImages);
			blurImages.addActionListener((ActionEvent evt) -> {
				processImages();
			});

			invertColors = new JCheckBox("Invert colors");
			settingsPanel.add(invertColors);
			invertColors.addActionListener((ActionEvent evt) -> {
				processImages();
			});

			settingsPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(settingsPanel);

			borderFirst = new JCheckBox("Border 1st ep.");
			settingsPanel.add(borderFirst);

			staticView = new JCheckBox("Static view");
			settingsPanel.add(staticView);
			staticView.addActionListener((ActionEvent evt) -> {
				if (!flipbook.isEmpty()) {
					if (staticView.isSelected()) {
						createStaticBook();
					} else {
						createFlipbook();
					}
				}
			});

			settingsPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(settingsPanel);

			markTarget = new JCheckBox("Mark target");
			settingsPanel.add(markTarget);

			showCrosshairs = new JCheckBox(html("Crosshairs " + INFO_ICON));
			settingsPanel.add(showCrosshairs);
			showCrosshairs
					.setToolTipText("Click on object to copy coordinates to clipboard (overlays must be disabled)");

			JButton resetDefaultsButton = new JButton("Reset image defaults");
			mainControlPanel.add(resetDefaultsButton);
			resetDefaultsButton.addActionListener((ActionEvent evt) -> {
				if (differenceImaging.isSelected()) {
					blurImages.setSelected(true);
				} else {
					blurImages.setSelected(false);
				}
				resetContrastSlider();
				createFlipbook();
			});

			stopDownloadButton = new JButton("Stop download process");
			mainControlPanel.add(stopDownloadButton);
			stopDownloadButton.addActionListener((ActionEvent evt) -> {
				stopDownloadProcess = true;
				enableAll();
			});

			mainControlPanel.add(wiseviewCutouts);
			mainControlPanel.add(unwiseCutouts);

			desiCutouts = new JRadioButton(html("DECaLS cutouts " + INFO_ICON));
			mainControlPanel.add(desiCutouts);
			desiCutouts.setToolTipText(
					"DECaLS cutouts are from https://www.legacysurvey.org and should be used with caution for motion detection.\nThe imagery might partially be the same for some of the data releases (e.g. DR8 and DR9).");
			desiCutouts.addActionListener((ActionEvent evt) -> {
				pixelScale = PIXEL_SCALE_DECAM;
				previousSize = 0;
				createFlipbook();
			});

			ps1Cutouts = new JRadioButton(html("PS1 Warp images " + INFO_ICON));
			mainControlPanel.add(ps1Cutouts);
			ps1Cutouts.setToolTipText(
					"These are cutouts from the Pan-STARRS1 DR2 Warp images. For further details, see https://outerspace.stsci.edu/display/PANSTARRS/PS1+Warp+images");
			ps1Cutouts.addActionListener((ActionEvent evt) -> {
				pixelScale = PIXEL_SCALE_PS1;
				previousSize = 0;
				createFlipbook();
			});

			ButtonGroup cutoutGroup = new ButtonGroup();
			cutoutGroup.add(wiseviewCutouts);
			cutoutGroup.add(unwiseCutouts);
			cutoutGroup.add(desiCutouts);
			cutoutGroup.add(ps1Cutouts);

			if (nearestBywSubjects) {
				mainControlPanel.add(createHeaderLabel("Nearest BYW subjects"));

				bywTopRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
				mainControlPanel.add(bywTopRow);

				bywBottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
				mainControlPanel.add(bywBottomRow);
			}

			mainControlPanel.add(createHeaderLabel("External resources"));

			JPanel resourcesPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(resourcesPanel);
			panstarrsLabel = new JLabel("Pan-STARRS");
			resourcesPanel.add(panstarrsLabel);
			panstarrsField = new JTextField();
			resourcesPanel.add(panstarrsField);

			resourcesPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(resourcesPanel);
			aladinLiteLabel = new JLabel("Aladin Lite");
			resourcesPanel.add(aladinLiteLabel);
			aladinLiteField = new JTextField();
			resourcesPanel.add(aladinLiteField);

			resourcesPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(resourcesPanel);
			wiseViewLabel = new JLabel("WiseView");
			resourcesPanel.add(wiseViewLabel);
			wiseViewField = new JTextField();
			resourcesPanel.add(wiseViewField);

			resourcesPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(resourcesPanel);
			finderChartLabel = new JLabel("IRSA Finder Chart");
			resourcesPanel.add(finderChartLabel);
			finderChartField = new JTextField();
			resourcesPanel.add(finderChartField);

			resourcesPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(resourcesPanel);
			resourcesPanel.add(new JLabel());
			changeFovButton = new JButton("Change FoV (\")");
			changeFovButton.addActionListener((ActionEvent e) -> {
				try {
					int panstarrsFOV = toInteger(panstarrsField.getText());
					int aladinLiteFOV = toInteger(aladinLiteField.getText());
					int wiseViewFOV = toInteger(wiseViewField.getText());
					int finderChartFOV = toInteger(finderChartField.getText());
					int defaultFOV = toInteger(sizeField.getText());
					panstarrsFOV = panstarrsFOV == 0 ? defaultFOV : panstarrsFOV;
					aladinLiteFOV = aladinLiteFOV == 0 ? defaultFOV : aladinLiteFOV;
					wiseViewFOV = wiseViewFOV == 0 ? defaultFOV : wiseViewFOV;
					finderChartFOV = finderChartFOV == 0 ? defaultFOV : finderChartFOV;
					createHyperlink(panstarrsLabel,
							getPanstarrsUrl(targetRa, targetDec, panstarrsFOV, ImageType.STACK));
					createHyperlink(aladinLiteLabel, getAladinLiteUrl(targetRa, targetDec, aladinLiteFOV));
					createHyperlink(wiseViewLabel, getWiseViewUrl(targetRa, targetDec, wiseViewFOV,
							skipIntermediateEpochs.isSelected() ? 1 : 0, separateScanDirections.isSelected() ? 1 : 0,
							differenceImaging.isSelected() ? 1 : 0));
					createHyperlink(finderChartLabel, getFinderChartUrl(targetRa, targetDec, finderChartFOV));
				} catch (Exception ex) {
					showErrorDialog(baseFrame, "Invalid field of view!");
				}
			});
			resourcesPanel.add(changeFovButton);

			legacyViewerLabel = new JLabel("Legacy Sky Viewer");
			mainControlPanel.add(legacyViewerLabel);

			resourcesPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(resourcesPanel);

			ukidssCutoutsLabel = new JLabel("UKIDSS cutouts");
			resourcesPanel.add(ukidssCutoutsLabel);

			vhsCutoutsLabel = new JLabel("VHS cutouts");
			resourcesPanel.add(vhsCutoutsLabel);

			resourcesPanel = new JPanel(new GridLayout(1, 2));
			mainControlPanel.add(resourcesPanel);

			simbadLabel = new JLabel("SIMBAD");
			resourcesPanel.add(simbadLabel);

			vizierLabel = new JLabel("VizieR");
			resourcesPanel.add(vizierLabel);

			// ======================
			// Tab: Catalog overlays
			// ======================
			JPanel overlaysControlPanel = new JPanel(new GridLayout(rows, 1));
			overlaysControlPanel.setPreferredSize(new Dimension(controlPanelWidth - 20, controlPanelHeight));
			overlaysControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

			JScrollPane overlaysScrollPanel = new JScrollPane(overlaysControlPanel);
			overlaysScrollPanel.setPreferredSize(new Dimension(controlPanelWidth, 50));
			overlaysScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
			controlTabs.add("Overlays", overlaysScrollPanel);

			JLabel catalogOverlaysLabel = createHeaderLabel(html("Catalog overlays " + INFO_ICON));
			overlaysControlPanel.add(catalogOverlaysLabel);
			catalogOverlaysLabel.setToolTipText("Shortcuts: Alt+[underscored letter]");

			JPanel overlayPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(overlayPanel);

			simbadOverlay = new JCheckBox(html("<u>S</u>IMBAD"), overlays.isSimbad());
			simbadOverlay.setForeground(Color.RED);
			simbadOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(simbadOverlay);

			allWiseOverlay = new JCheckBox(html("<u>A</u>llWISE"), overlays.isAllwise());
			allWiseOverlay.setForeground(Color.GREEN.darker());
			allWiseOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(allWiseOverlay);

			overlayPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(overlayPanel);

			catWiseOverlay = new JCheckBox(html("<u>C</u>atWISE2020"), overlays.isCatwise());
			catWiseOverlay.setForeground(Color.MAGENTA);
			catWiseOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(catWiseOverlay);

			unWiseOverlay = new JCheckBox(html("<u>u</u>nWISE"), overlays.isUnwise());
			unWiseOverlay.setForeground(JColor.MINT.val);
			unWiseOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(unWiseOverlay);

			overlayPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(overlayPanel);

			gaiaOverlay = new JCheckBox("Gaia DR2", overlays.isGaiadr2());
			gaiaOverlay.setForeground(Color.CYAN.darker());
			gaiaOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(gaiaOverlay);

			gaiaDR3Overlay = new JCheckBox(html("<u>G</u>aia DR3"), overlays.isGaiadr3());
			gaiaDR3Overlay.setForeground(Color.CYAN.darker());
			gaiaDR3Overlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(gaiaDR3Overlay);

			overlayPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(overlayPanel);

			noirlabOverlay = new JCheckBox(html("<u>N</u>SC DR2"), overlays.isNoirlab());
			noirlabOverlay.setForeground(JColor.NAVY.val);
			noirlabOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(noirlabOverlay);

			panStarrsOverlay = new JCheckBox(html("<u>P</u>an-STARRS"), overlays.isPanstar());
			panStarrsOverlay.setForeground(JColor.BROWN.val);
			panStarrsOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(panStarrsOverlay);

			overlayPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(overlayPanel);

			sdssOverlay = new JCheckBox(html("S<u>D</u>SS DR17"), overlays.isSdss());
			sdssOverlay.setForeground(JColor.STEEL.val);
			sdssOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(sdssOverlay);

			spectrumOverlay = new JCheckBox("SDSS Spectra", overlays.isSpectra());
			spectrumOverlay.setForeground(JColor.OLIVE.val);
			spectrumOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(spectrumOverlay);

			overlayPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(overlayPanel);

			vhsOverlay = new JCheckBox(html("<u>V</u>HS DR5"), overlays.isVhs());
			vhsOverlay.setForeground(JColor.PINK.val);
			vhsOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(vhsOverlay);

			uhsOverlay = new JCheckBox(html("U<u>H</u>S DR2"), overlays.isUhs());
			uhsOverlay.setForeground(JColor.DARK_YELLOW.val);
			uhsOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(uhsOverlay);

			overlayPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(overlayPanel);

			ukidssOverlay = new JCheckBox(html("U<u>K</u>IDSS DR11"), overlays.isUkidss());
			ukidssOverlay.setForeground(JColor.BLOOD.val);
			ukidssOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(ukidssOverlay);

			twoMassOverlay = new JCheckBox(html("2<u>M</u>ASS"), overlays.isTwomass());
			twoMassOverlay.setForeground(JColor.ORANGE.val);
			twoMassOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(twoMassOverlay);

			overlayPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(overlayPanel);

			tessOverlay = new JCheckBox(html("<u>T</u>ESS"), overlays.isTess());
			tessOverlay.setForeground(JColor.LILAC.val);
			tessOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(tessOverlay);

			desOverlay = new JCheckBox(html("D<u>E</u>S DR2"), overlays.isDes());
			desOverlay.setForeground(JColor.SAND.val);
			desOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(desOverlay);

			overlayPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(overlayPanel);

			gaiaWDOverlay = new JCheckBox(html("Gaia EDR3 <u>W</u>D"), overlays.isGaiawd());
			gaiaWDOverlay.setForeground(JColor.PURPLE.val);
			gaiaWDOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(gaiaWDOverlay);

			mocaOverlay = new JCheckBox(html("M<u>O</u>CA DB " + INFO_ICON), overlays.isMoca());
			mocaOverlay.setForeground(JColor.DARK_ORANGE.val);
			mocaOverlay.setToolTipText(
					html("Montreal Open Clusters and Associations (MOCA) database (https://mocadb.ca/home)" + LINE_BREAK
							+ "Overlays created from the \"Summary of all objects\" table (https://mocadb.ca/schema/summary_all_objects)"));
			mocaOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlayPanel.add(mocaOverlay);

			ssoOverlay = new JCheckBox("Solar System Objects", overlays.isSso());
			ssoOverlay.setForeground(Color.BLUE);
			ssoOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlaysControlPanel.add(ssoOverlay);

			useCustomOverlays = new JCheckBox("Custom overlays:");
			overlaysControlPanel.add(useCustomOverlays);
			useCustomOverlays.addActionListener((ActionEvent evt) -> {
				if (customOverlays.isEmpty()) {
					showInfoDialog(baseFrame, "No custom overlays added yet.");
					useCustomOverlays.setSelected(false);
				} else {
					GridLayout layout = (GridLayout) overlaysControlPanel.getLayout();
					int numberOfRows = customOverlays.size();
					int rowsHeight = numberOfRows * ROW_HEIGHT;
					if (useCustomOverlays.isSelected()) {
						componentIndex = overlaysControlPanel.getComponentZOrder(useCustomOverlays) + 1;
						layout.setRows(layout.getRows() + numberOfRows);
						overlaysControlPanel.setPreferredSize(new Dimension(overlaysControlPanel.getWidth(),
								overlaysControlPanel.getHeight() + rowsHeight));
						customOverlays.values().forEach(customOverlay -> {
							JCheckBox checkBox = new JCheckBox(customOverlay.getName());
							checkBox.setForeground(customOverlay.getColor());
							checkBox.addActionListener((ActionEvent e) -> {
								processImages();
							});
							customOverlay.setCheckBox(checkBox);
							overlaysControlPanel.add(checkBox, componentIndex++);
						});
					} else {
						componentIndex = overlaysControlPanel.getComponentZOrder(useCustomOverlays) + numberOfRows;
						layout.setRows(layout.getRows() - numberOfRows);
						overlaysControlPanel.setPreferredSize(new Dimension(overlaysControlPanel.getWidth(),
								overlaysControlPanel.getHeight() - rowsHeight));
						customOverlays.values().forEach((customOverlay) -> {
							overlaysControlPanel.remove(componentIndex--);
							customOverlay.setCatalogEntries(null);
						});
						processImages();
					}
					overlaysControlPanel.updateUI();
					baseFrame.setVisible(true);
				}
			});

			JLabel pmOverlaysLabel = createHeaderLabel(html("Proper motion vectors " + INFO_ICON));
			overlaysControlPanel.add(pmOverlaysLabel);
			pmOverlaysLabel.setToolTipText("Shortcuts: Ctrl+Alt+[underscored letter]");

			JPanel properMotionPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(properMotionPanel);

			gaiaProperMotion = new JCheckBox("Gaia DR2", overlays.isPmgaiadr2());
			gaiaProperMotion.setForeground(Color.CYAN.darker());
			gaiaProperMotion.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			properMotionPanel.add(gaiaProperMotion);

			gaiaDR3ProperMotion = new JCheckBox(html("<u>G</u>aia DR3"), overlays.isPmgaiadr3());
			gaiaDR3ProperMotion.setForeground(Color.CYAN.darker());
			gaiaDR3ProperMotion.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			properMotionPanel.add(gaiaDR3ProperMotion);

			properMotionPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(properMotionPanel);

			noirlabProperMotion = new JCheckBox(html("<u>N</u>SC DR2"), overlays.isPmnoirlab());
			noirlabProperMotion.setForeground(JColor.NAVY.val);
			noirlabProperMotion.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			properMotionPanel.add(noirlabProperMotion);

			catWiseProperMotion = new JCheckBox(html("<u>C</u>atWISE2020"), overlays.isPmcatwise());
			catWiseProperMotion.setForeground(Color.MAGENTA);
			catWiseProperMotion.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			properMotionPanel.add(catWiseProperMotion);

			ukidssProperMotion = new JCheckBox(html("U<u>K</u>IDSS LAS"), overlays.isPmukidss());
			ukidssProperMotion.setForeground(JColor.BLOOD.val);
			ukidssProperMotion.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			overlaysControlPanel.add(ukidssProperMotion);

			properMotionPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(properMotionPanel);
			properMotionPanel.add(new JLabel("Total PM (mas/yr) >"));
			properMotionField = new JTextField(String.valueOf(PROPER_MOTION));
			properMotionPanel.add(properMotionField);
			properMotionField.addActionListener((ActionEvent evt) -> {
				gaiaTpmEntries = null;
				gaiaDR3TpmEntries = null;
				catWiseTpmEntries = null;
				noirlabTpmEntries = null;
				processImages();
			});

			showProperMotion = new JCheckBox("Show motion as moving dots");
			overlaysControlPanel.add(showProperMotion);
			showProperMotion.addActionListener((ActionEvent evt) -> {
				processImages();
			});

			JLabel artifactsLabel = createHeaderLabel(html("WISE artifacts " + INFO_ICON));
			overlaysControlPanel.add(artifactsLabel);
			artifactsLabel.setToolTipText(html("" + "Small shapes represent affected sources." + LINE_BREAK
					+ "Large shapes represent the actual artifacts."));

			JPanel artifactPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(artifactPanel);
			ghostOverlay = new JCheckBox("Ghosts", overlays.isGhosts());
			ghostOverlay.setForeground(Color.MAGENTA.darker());
			ghostOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			artifactPanel.add(ghostOverlay);
			haloOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;Halos&nbsp;</span>"),
					overlays.isHalos());
			haloOverlay.setForeground(Color.YELLOW);
			haloOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			artifactPanel.add(haloOverlay);

			artifactPanel = new JPanel(new GridLayout(1, 2));
			overlaysControlPanel.add(artifactPanel);
			latentOverlay = new JCheckBox("Latents", overlays.isLatents());
			latentOverlay.setForeground(Color.GREEN.darker());
			latentOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			artifactPanel.add(latentOverlay);
			spikeOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;Spikes&nbsp;</span>"),
					overlays.isSpikes());
			spikeOverlay.setForeground(Color.ORANGE);
			spikeOverlay.addActionListener((ActionEvent evt) -> {
				processImages();
			});
			artifactPanel.add(spikeOverlay);

			JLabel saveOverlaysMessage = createMessageLabel();
			Timer messageTimer = new Timer(3000, (ActionEvent e) -> {
				saveOverlaysMessage.setText("");
			});

			JButton saveButton = new JButton(html("Save selected overlays " + INFO_ICON));
			overlaysControlPanel.add(saveButton);
			saveButton.setToolTipText("Custom overlays not included!");
			saveButton.addActionListener((ActionEvent evt) -> {
				overlays.setSimbad(simbadOverlay.isSelected());
				overlays.setAllwise(allWiseOverlay.isSelected());
				overlays.setCatwise(catWiseOverlay.isSelected());
				overlays.setUnwise(unWiseOverlay.isSelected());
				overlays.setGaiadr2(gaiaOverlay.isSelected());
				overlays.setGaiadr3(gaiaDR3Overlay.isSelected());
				overlays.setNoirlab(noirlabOverlay.isSelected());
				overlays.setPanstar(panStarrsOverlay.isSelected());
				overlays.setSdss(sdssOverlay.isSelected());
				overlays.setSpectra(spectrumOverlay.isSelected());
				overlays.setVhs(vhsOverlay.isSelected());
				overlays.setUhs(uhsOverlay.isSelected());
				overlays.setUkidss(ukidssOverlay.isSelected());
				overlays.setTwomass(twoMassOverlay.isSelected());
				overlays.setTess(tessOverlay.isSelected());
				overlays.setDes(desOverlay.isSelected());
				overlays.setGaiawd(gaiaWDOverlay.isSelected());
				overlays.setMoca(mocaOverlay.isSelected());
				overlays.setSso(ssoOverlay.isSelected());
				overlays.setPmgaiadr2(gaiaProperMotion.isSelected());
				overlays.setPmgaiadr3(gaiaDR3ProperMotion.isSelected());
				overlays.setPmnoirlab(noirlabProperMotion.isSelected());
				overlays.setPmcatwise(catWiseProperMotion.isSelected());
				overlays.setPmukidss(ukidssProperMotion.isSelected());
				overlays.setGhosts(ghostOverlay.isSelected());
				overlays.setLatents(haloOverlay.isSelected());
				overlays.setHalos(latentOverlay.isSelected());
				overlays.setSpikes(spikeOverlay.isSelected());
				try (OutputStream output = new FileOutputStream(PROP_PATH)) {
					USER_SETTINGS.setProperty(OVERLAYS_KEY, overlays.serialize());
					USER_SETTINGS.store(output, COMMENTS);
					saveOverlaysMessage.setText("Overlays saved!");
					messageTimer.restart();
				} catch (IOException ex) {
				}
			});

			overlaysControlPanel.add(saveOverlaysMessage);

			// ====================
			// Tab: Mouse settings
			// ====================
			JPanel mouseControlPanel = new JPanel(new GridLayout(rows, 1));
			mouseControlPanel.setPreferredSize(new Dimension(controlPanelWidth - 20, controlPanelHeight));
			mouseControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

			JScrollPane mouseScrollPanel = new JScrollPane(mouseControlPanel);
			mouseScrollPanel.setPreferredSize(new Dimension(controlPanelWidth, 50));
			mouseScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
			controlTabs.add("Mouse", mouseScrollPanel);

			mouseControlPanel.add(createHeaderLabel("Mouse left click w/o overlays"));

			showCatalogsButton = new JRadioButton("Show catalog entries for object", true);
			mouseControlPanel.add(showCatalogsButton);

			JRadioButton recenterImagesButton = new JRadioButton("Recenter images on object", false);
			mouseControlPanel.add(recenterImagesButton);

			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(showCatalogsButton);
			buttonGroup.add(recenterImagesButton);

			mouseControlPanel.add(createHeaderLabel("Mouse wheel click"));

			mouseControlPanel.add(new JLabel("Select images to display:"));

			dssImageSeries = new JCheckBox("DSS 1Red, 1Blue, 2Red, 2Blue, 2IR", false);
			mouseControlPanel.add(dssImageSeries);
			dssImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			twoMassImageSeries = new JCheckBox("2MASS J, H & K bands", false);
			mouseControlPanel.add(twoMassImageSeries);
			twoMassImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			sdssImageSeries = new JCheckBox("SDSS u, g, r, i & z bands", false);
			mouseControlPanel.add(sdssImageSeries);
			sdssImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			spitzerImageSeries = new JCheckBox("Spitzer CH1, CH2, CH3, CH4, MIPS24", false);
			mouseControlPanel.add(spitzerImageSeries);
			spitzerImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			allwiseImageSeries = new JCheckBox("AllWISE W1, W2, W3 & W4 bands", true);
			mouseControlPanel.add(allwiseImageSeries);
			allwiseImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			ukidssImageSeries = new JCheckBox("UKIDSS Y, J, H & K bands", false);
			mouseControlPanel.add(ukidssImageSeries);
			ukidssImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			uhsImageSeries = new JCheckBox("UHS J & K bands", false);
			mouseControlPanel.add(uhsImageSeries);
			uhsImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			vhsImageSeries = new JCheckBox("VHS Y, J, H & K bands", false);
			mouseControlPanel.add(vhsImageSeries);
			vhsImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			panstarrsImageSeries = new JCheckBox("Pan-STARRS g, r, i, z & y bands", false);
			mouseControlPanel.add(panstarrsImageSeries);
			panstarrsImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			legacyImageSeries = new JCheckBox("DECaLS g, r & z bands", false);
			mouseControlPanel.add(legacyImageSeries);
			legacyImageSeries.addActionListener((ActionEvent evt) -> {
				imageSeriesPdf.setSelected(false);
			});

			staticTimeSeries = new JCheckBox("Time series - static", false);
			mouseControlPanel.add(staticTimeSeries);
			staticTimeSeries.addActionListener((ActionEvent evt) -> {
				if (staticTimeSeries.isSelected() || animatedTimeSeries.isSelected()) {
					dssImageSeries.setSelected(false);
					twoMassImageSeries.setSelected(false);
					sdssImageSeries.setSelected(false);
					spitzerImageSeries.setSelected(false);
					allwiseImageSeries.setSelected(false);
					ukidssImageSeries.setSelected(false);
					uhsImageSeries.setSelected(false);
					vhsImageSeries.setSelected(false);
					panstarrsImageSeries.setSelected(false);
					legacyImageSeries.setSelected(false);
					animatedTimeSeries.setSelected(false);
				}
				imageSeriesPdf.setSelected(false);
			});

			animatedTimeSeries = new JCheckBox("Time series - animated", false);
			mouseControlPanel.add(animatedTimeSeries);
			animatedTimeSeries.addActionListener((ActionEvent evt) -> {
				if (animatedTimeSeries.isSelected()) {
					dssImageSeries.setSelected(true);
					twoMassImageSeries.setSelected(true);
					sdssImageSeries.setSelected(true);
					spitzerImageSeries.setSelected(true);
					allwiseImageSeries.setSelected(true);
					ukidssImageSeries.setSelected(true);
					uhsImageSeries.setSelected(true);
					vhsImageSeries.setSelected(true);
					panstarrsImageSeries.setSelected(true);
					legacyImageSeries.setSelected(true);
					staticTimeSeries.setSelected(false);
				} else {
					dssImageSeries.setSelected(false);
					twoMassImageSeries.setSelected(false);
					sdssImageSeries.setSelected(false);
					spitzerImageSeries.setSelected(false);
					allwiseImageSeries.setSelected(false);
					ukidssImageSeries.setSelected(false);
					uhsImageSeries.setSelected(false);
					vhsImageSeries.setSelected(false);
					panstarrsImageSeries.setSelected(false);
					legacyImageSeries.setSelected(false);
				}
				imageSeriesPdf.setSelected(false);
			});

			imageSeriesPdf = new JCheckBox(html("Image series PDF " + INFO_ICON), false);
			mouseControlPanel.add(imageSeriesPdf);
			imageSeriesPdf.setToolTipText(html("" + "The creation of the PDF may take a few minutes." + LINE_BREAK
					+ "Do not continue working with AstroToolBox until the PDF is ready!"));
			imageSeriesPdf.addActionListener((ActionEvent evt) -> {
				if (imageSeriesPdf.isSelected()) {
					setImageViewer(this);
					dssImageSeries.setSelected(false);
					twoMassImageSeries.setSelected(false);
					sdssImageSeries.setSelected(false);
					spitzerImageSeries.setSelected(false);
					allwiseImageSeries.setSelected(false);
					ukidssImageSeries.setSelected(false);
					uhsImageSeries.setSelected(false);
					vhsImageSeries.setSelected(false);
					panstarrsImageSeries.setSelected(false);
					legacyImageSeries.setSelected(false);
					staticTimeSeries.setSelected(false);
					animatedTimeSeries.setSelected(false);
				}
			});

			changeFovLabel = new JLabel(html(CHANGE_FOV_TEXT.formatted(fieldOfView)));
			mouseControlPanel.add(changeFovLabel);
			changeFovLabel.setToolTipText("Spin wheel on flipbook images to change the size of the field of view.");

			mouseControlPanel.add(createHeaderLabel("Mouse right click"));

			mouseControlPanel.add(new JLabel("Show object in a different field of view"));

			JPanel differentSizePanel = new JPanel(new GridLayout(1, 2));
			mouseControlPanel.add(differentSizePanel);
			differentSizePanel.add(new JLabel("Enter FoV (arcsec):"));
			differentSizeField = new JTextField(String.valueOf(DIFFERENT_SIZE));
			differentSizePanel.add(differentSizeField);

			mouseControlPanel.add(new JLabel());

			drawCrosshairs = createHeaderBox(html("Draw crosshairs: " + INFO_ICON));
			mouseControlPanel.add(drawCrosshairs);
			drawCrosshairs.setToolTipText(html("" + "Tick the check box!" + LINE_BREAK
					+ "Push mouse wheel to draw a crosshair on a specific location." + LINE_BREAK
					+ "Spin mouse wheel to change the crosshair's size." + LINE_BREAK
					+ "Wheel-click the crosshair's center to delete it." + LINE_BREAK
					+ "The crosshair's coordinates appear in the text box below."));
			drawCrosshairs.addActionListener((ActionEvent evt) -> {
				if (!drawCrosshairs.isSelected()) {
					crosshairs.clear();
					crosshairCoords.setText("");
				}
			});

			crosshairCoords = new JTextArea();
			mouseControlPanel.add(new JScrollPane(crosshairCoords));
			crosshairCoords.setBackground(new JLabel().getBackground());

			// =====================
			// Tab: Player controls
			// =====================
			JPanel playerControlPanel = new JPanel(new GridLayout(rows, 1));
			playerControlPanel.setPreferredSize(new Dimension(controlPanelWidth - 20, controlPanelHeight));
			playerControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

			JScrollPane playerScrollPanel = new JScrollPane(playerControlPanel);
			playerScrollPanel.setPreferredSize(new Dimension(controlPanelWidth, 50));
			playerScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
			controlTabs.add("Player", playerScrollPanel);

			playerControlPanel.add(createHeaderLabel("Image player controls", SwingConstants.CENTER));

			playerControlPanel.add(new JLabel());

			JPanel playerControls = new JPanel(new GridLayout(1, 2));
			playerControlPanel.add(playerControls);

			JButton playButton = new JButton("Play");
			playerControls.add(playButton);
			playButton.addActionListener((ActionEvent evt) -> {
				timer.setRepeats(true);
				timer.start();
				timerStopped = false;
			});

			JButton stopButton = new JButton("Stop");
			playerControls.add(stopButton);
			stopButton.addActionListener((ActionEvent evt) -> {
				timer.stop();
				timerStopped = true;
			});

			playerControls = new JPanel(new GridLayout(1, 2));
			playerControlPanel.add(playerControls);

			JButton backwardButton = new JButton("Backward");
			playerControls.add(backwardButton);
			backwardButton.addActionListener((ActionEvent evt) -> {
				timer.stop();
				imageNumber -= 2;
				timer.setRepeats(false);
				timer.start();
			});

			JButton forwardButton = new JButton("Forward");
			playerControls.add(forwardButton);
			forwardButton.addActionListener((ActionEvent evt) -> {
				timer.stop();
				timer.setRepeats(false);
				timer.start();
			});

			playerControlPanel.add(new JLabel());

			JButton moveUpButton = new JButton("Move up");
			playerControlPanel.add(moveUpButton);
			moveUpButton.addActionListener((ActionEvent evt) -> {
				double newDec = targetDec + size * pixelScale * OVERLAP_FACTOR / DEG_ARCSEC;
				if (newDec > 90) {
					newDec = 90 - (newDec - 90);
					double newRa = targetRa + 180;
					targetRa = newRa > 360 ? newRa - 360 : newRa;
					showInfoDialog(baseFrame, "You're about to cross the North Celestial Pole." + LINE_SEP
							+ "If you want to move on in the current direction, use the 'Move down' button next!");
				}
				coordsField.setText(roundTo7DecNZLZ(targetRa) + " " + roundTo7DecNZLZ(newDec));
				createFlipbook();
			});

			JPanel navigationButtons = new JPanel(new GridLayout(1, 2));
			playerControlPanel.add(navigationButtons);

			JButton moveLeftButton = new JButton("Move left");
			navigationButtons.add(moveLeftButton);
			moveLeftButton.addActionListener((ActionEvent evt) -> {
				double distance = size * pixelScale * OVERLAP_FACTOR / DEG_ARCSEC;
				NumberPair coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec),
						new NumberPair(distance, 0));
				double newRa = coords.getX();
				newRa = newRa > 360 ? newRa - 360 : newRa;
				newRa = newRa > 360 ? 0 : newRa;
				coordsField.setText(roundTo7DecNZLZ(newRa) + " " + roundTo7DecNZLZ(targetDec));
				createFlipbook();
			});

			JButton moveRightButton = new JButton("Move right");
			navigationButtons.add(moveRightButton);
			moveRightButton.addActionListener((ActionEvent evt) -> {
				double distance = size * pixelScale * OVERLAP_FACTOR / DEG_ARCSEC;
				NumberPair coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec),
						new NumberPair(-distance, 0));
				double newRa = coords.getX();
				newRa = newRa < 0 ? newRa + 360 : newRa;
				newRa = newRa < 0 ? 0 : newRa;
				coordsField.setText(roundTo7DecNZLZ(newRa) + " " + roundTo7DecNZLZ(targetDec));
				createFlipbook();
			});

			JButton moveDownButton = new JButton("Move down");
			playerControlPanel.add(moveDownButton);
			moveDownButton.addActionListener((ActionEvent evt) -> {
				double newDec = targetDec - size * pixelScale * OVERLAP_FACTOR / DEG_ARCSEC;
				if (newDec < -90) {
					newDec = -90 + (abs(newDec) - 90);
					double newRa = targetRa + 180;
					targetRa = newRa > 360 ? newRa - 360 : newRa;
					showInfoDialog(baseFrame, "You're about to cross the South Celestial Pole." + LINE_SEP
							+ "If you want to move on in the current direction, use the 'Move up' button next!");
				}
				coordsField.setText(roundTo7DecNZLZ(targetRa) + " " + roundTo7DecNZLZ(newDec));
				createFlipbook();
			});

			playerControlPanel.add(new JLabel());

			JButton rotateButton = new JButton("Rotate by 90° clockwise: %d°".formatted(quadrantCount * 90));
			playerControlPanel.add(rotateButton);
			rotateButton.addActionListener((ActionEvent evt) -> {
				quadrantCount++;
				if (quadrantCount > 3) {
					quadrantCount = 0;
				}
				rotateButton.setText("Rotate by 90° clockwise: %d°".formatted(quadrantCount * 90));
				processImages();
			});

			playerControlPanel.add(new JLabel());

			JPanel saveControls = new JPanel(new GridLayout(1, 2));
			playerControlPanel.add(saveControls);

			JButton saveAsPngButton = new JButton("Save as PNG");
			saveControls.add(saveAsPngButton);
			saveAsPngButton.addActionListener((ActionEvent evt) -> {
				try {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(new FileTypeFilter(".png", ".png files"));
					int returnVal = fileChooser.showSaveDialog(playerControlPanel);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						file = new File(file.getPath() + ".png");
						ImageIO.write(wiseImage, "png", file);
					}
				} catch (HeadlessException | IOException ex) {
					showExceptionDialog(baseFrame, ex);
				}
			});

			JButton saveAsGifButton = new JButton("Save as GIF");
			saveControls.add(saveAsGifButton);
			saveAsGifButton.addActionListener((ActionEvent evt) -> {
				try {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(new FileTypeFilter(".gif", ".gif files"));
					int returnVal = fileChooser.showSaveDialog(playerControlPanel);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						file = new File(file.getPath() + ".gif");
						BufferedImage[] imageSet = new BufferedImage[flipbook.size()];
						for (int i = 0; i < flipbook.size(); i++) {
							FlipbookComponent component = flipbook.get(i);
							imageSet[i] = addCrosshairs(processImage(component, i));
						}
						if (imageSet.length > 0) {
							GifSequencer sequencer = new GifSequencer();
							sequencer.generateFromBI(imageSet, file, speed / 10, true);
						}
					}
				} catch (HeadlessException | IOException ex) {
					showExceptionDialog(baseFrame, ex);
				}
			});

			timer = new Timer(speed, (ActionEvent e) -> {
				try {
					if (flipbook.isEmpty()) {
						enableAll();
						return;
					}
					if (imageNumber < 0) {
						imageNumber = flipbook.size() - 1;
					}
					if (imageNumber > flipbook.size() - 1) {
						imageNumber = 0;
					}
					staticView.setSelected(false);

					FlipbookComponent component = flipbook.get(imageNumber);
					BufferedImage image = component.getImage();
					if (image == null) {
						return;
					}
					wiseImage = addCrosshairs(image);
					ImageIcon icon = new ImageIcon(wiseImage);
					String regularLabel = component.getTitle();
					JLabel regularImage = addTextToImage(new JLabel(icon), regularLabel);
					if (borderFirst.isSelected() && component.isFirstEpoch()) {
						regularImage.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
					} else {
						regularImage.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
					}

					imagePanel.removeAll();
					imagePanel.add(regularImage);

					// Initialize positions of magnified WISE image
					int width = 50;
					int height = 50;
					int imageWidth = wiseImage.getWidth();
					int imageHeight = wiseImage.getHeight();
					if (pointerX == 0 && pointerY == 0) {
						NumberPair pixelCoords = toPixelCoordinates(targetRa, targetDec);
						pointerX = (int) pixelCoords.getX();
						pointerY = (int) pixelCoords.getY();
					}
					int upperLeftX = pointerX - (width / 2);
					int upperLeftY = pointerY - (height / 2);
					int upperRightX = upperLeftX + width;
					int lowerLeftY = upperLeftY + height;

					// Correct positions of magnified WISE image
					upperLeftX = upperLeftX < 0 ? 0 : upperLeftX;
					upperLeftY = upperLeftY < 0 ? 0 : upperLeftY;
					if (upperRightX > imageWidth) {
						upperLeftX = upperLeftX - (upperRightX - imageWidth);
					}
					if (lowerLeftY > imageHeight) {
						upperLeftY = upperLeftY - (lowerLeftY - imageHeight);
					}

					// Create and display magnified WISE image
					rightPanel.removeAll();
					rightPanel.repaint();

					if (desiCutouts.isSelected()) {
						regularLabel = "DECaLS";
					} else if (ps1Cutouts.isSelected()) {
						regularLabel = "PS1";
					} else {
						regularLabel = "WISE";
					}

					addMagnifiedImage(regularLabel, wiseImage, upperLeftX, upperLeftY, width, height);

					List<Couple<String, NirImage>> surveyImages = new ArrayList();

					// DESI LS image
					JLabel desiLabel = null;
					if (processedDesiImage != null) {
						surveyImages.add(new Couple(getImageLabel("LS", DESI_LS_DR_LABEL),
								new NirImage(DESI_LS_EPOCH, processedDesiImage)));
					}

					// Pan-STARRS image
					JLabel ps1Label = null;
					if (processedPs1Image != null) {
						surveyImages.add(new Couple(getImageLabel("PS1", year_ps1_y_i_g),
								new NirImage(year_ps1_y_i_g, processedPs1Image)));
					}

					// VHS image
					JLabel vhsLabel = null;
					if (processedVhsImage != null) {
						surveyImages.add(new Couple(getImageLabel(VHS_LABEL, year_vhs_k_h_j),
								new NirImage(year_vhs_k_h_j, processedVhsImage)));
					}

					// UHS image
					JLabel uhsLabel = null;
					if (processedUhsImage != null) {
						surveyImages.add(new Couple(getImageLabel(UHS_LABEL, year_uhs_k_j),
								new NirImage(year_uhs_k_j, processedUhsImage)));
					}

					// UKIDSS image
					JLabel ukidssLabel = null;
					if (processedUkidssImage != null) {
						surveyImages.add(new Couple(getImageLabel(UKIDSS_LABEL, year_ukidss_k_h_j),
								new NirImage(year_ukidss_k_h_j, processedUkidssImage)));
					}

					// SDSS image
					if (processedSdssImage != null) {
						surveyImages.add(new Couple(SDSS_LABEL, new NirImage(2000, processedSdssImage)));
					}

					// DSS image
					if (processedDssImage != null) {
						surveyImages.add(new Couple(getImageLabel("DSS", year_dss_2ir_1r_1b),
								new NirImage(year_dss_2ir_1r_1b, processedDssImage)));
					}

					surveyImages.sort(Comparator.comparing(c -> 3000 - c.getB().getYear()));

					for (Couple<String, NirImage> couple : surveyImages) {
						String surveyLabel = couple.getA();
						BufferedImage surveyImage = couple.getB().getImage();

						// Create and display magnified image
						if (!imageCutOff) {
							addMagnifiedImage(surveyLabel, surveyImage, upperLeftX, upperLeftY, width, height);
						}

						// Display regular image
						JLabel imageLabel = addTextToImage(new JLabel(new ImageIcon(surveyImage)), surveyLabel);
						if (surveyLabel.contains("LS")) {
							desiLabel = imageLabel;
						} else if (surveyLabel.contains("PS1")) {
							ps1Label = imageLabel;
						} else if (surveyLabel.contains(VHS_LABEL)) {
							vhsLabel = imageLabel;
						} else if (surveyLabel.contains(UHS_LABEL)) {
							uhsLabel = imageLabel;
						} else if (surveyLabel.contains(UKIDSS_LABEL)) {
							ukidssLabel = imageLabel;
						}
						imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
						imagePanel.add(imageLabel);
					}

					baseFrame.repaint();
					imageNumber++;

					regularImage.addMouseListener(new MouseListener() {
						@Override
						public void mousePressed(MouseEvent evt) {
							int mouseX = evt.getX();
							int mouseY = evt.getY();
							NumberPair pointerCoords;
							if (quadrantCount > 0 && quadrantCount < 4) {
								NumberPair pixelCoords = undoRotationOfPixelCoords(mouseX, mouseY);
								mouseX = (int) pixelCoords.getX();
								mouseY = (int) pixelCoords.getY();
								pointerCoords = toWorldCoordinates((int) pixelCoords.getX(), (int) pixelCoords.getY());
							} else {
								pointerCoords = toWorldCoordinates(mouseX, mouseY);
							}
							double newRa = pointerCoords.getX();
							double newDec = pointerCoords.getY();
							if (SwingUtilities.isRightMouseButton(evt)) {
								CompletableFuture.supplyAsync(() -> openNewImageViewer(newRa, newDec));
							} else if (SwingUtilities.isMiddleMouseButton(evt)) {
								if (drawCrosshairs.isSelected()) {
									double crosshairX = mouseX * 1.0 / zoom;
									double crosshairY = mouseY * 1.0 / zoom;
									double radius = 0.01;
									boolean removed = false;
									ListIterator<NumberPair> iter = crosshairs.listIterator();
									while (iter.hasNext()) {
										NumberPair pixelCoords = iter.next();
										if (pixelCoords.getX() > crosshairX - radius
												&& pixelCoords.getX() < crosshairX + radius
												&& pixelCoords.getY() > crosshairY - radius
												&& pixelCoords.getY() < crosshairY + radius) {
											iter.remove();
											removed = true;
										}
									}
									if (!removed) {
										crosshairs.add(new NumberPair(crosshairX, crosshairY));
									}
									StringBuilder sb = new StringBuilder();
									for (int i = 0; i < crosshairs.size(); i++) {
										NumberPair crosshair = crosshairs.get(i);
										NumberPair c = toWorldCoordinates((int) round(crosshair.getX() * zoom),
												(int) round(crosshair.getY() * zoom));
										sb.append(i + 1).append(". ");
										sb.append(roundTo7Dec(c.getX()));
										sb.append(" ");
										sb.append(roundTo7Dec(c.getY()));
										sb.append(LINE_SEP_TEXT_AREA);
									}
									crosshairCoords.setText(sb.toString());
								} else {
									if (imageSeriesPdf.isSelected()) {
										CompletableFuture.supplyAsync(
												() -> new ImageSeriesPdf(newRa, newDec, fieldOfView, getImageViewer())
														.create(baseFrame));
									} else if (animatedTimeSeries.isSelected()) {
										if (imageCount == 0) {
											displayAnimatedTimeSeries(newRa, newDec, fieldOfView);
										}
									} else {
										CompletableFuture.supplyAsync(() -> {
											int numberOfPanels = 0;
											if (dssImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (twoMassImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (sdssImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (spitzerImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (allwiseImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (ukidssImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (uhsImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (vhsImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (panstarrsImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (legacyImageSeries.isSelected()) {
												numberOfPanels++;
											}
											if (staticTimeSeries.isSelected()) {
												numberOfPanels++;
											}
											Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
											int screenHeight = screenSize.height;
											int verticalSpacing;
											int totalPanelHeight = numberOfPanels * PANEL_HEIGHT;
											if (totalPanelHeight > screenHeight) {
												verticalSpacing = PANEL_HEIGHT
														- (totalPanelHeight - screenHeight) / (numberOfPanels);
											} else {
												verticalSpacing = PANEL_HEIGHT;
											}
											Counter counter = new Counter(verticalSpacing);
											if (dssImageSeries.isSelected()) {
												displayDssImages(newRa, newDec, fieldOfView, counter);
											}
											if (twoMassImageSeries.isSelected()) {
												display2MassImages(newRa, newDec, fieldOfView, counter);
											}
											if (sdssImageSeries.isSelected()) {
												displaySdssImages(newRa, newDec, fieldOfView, counter);
											}
											if (spitzerImageSeries.isSelected()) {
												displaySpitzerImages(newRa, newDec, fieldOfView, counter);
											}
											if (allwiseImageSeries.isSelected()) {
												displayAllwiseImages(newRa, newDec, fieldOfView, counter);
											}
											if (ukidssImageSeries.isSelected()) {
												displayUkidssImages(newRa, newDec, fieldOfView, counter);
											}
											if (uhsImageSeries.isSelected()) {
												displayUhsImages(newRa, newDec, fieldOfView, counter);
											}
											if (vhsImageSeries.isSelected()) {
												displayVhsImages(newRa, newDec, fieldOfView, counter);
											}
											if (panstarrsImageSeries.isSelected()) {
												displayPs1Images(newRa, newDec, fieldOfView, counter);
											}
											if (legacyImageSeries.isSelected()) {
												displayDesiImages(targetRa, targetDec, fieldOfView, counter);
											}
											if (staticTimeSeries.isSelected()) {
												displayStaticTimeSeries(newRa, newDec, fieldOfView, counter);
											}
											return null;
										});
									}
								}
							} else if (SwingUtilities.isLeftMouseButton(evt)) {
								int count = 0;
								if (simbadOverlay.isSelected() && simbadEntries != null) {
									showCatalogInfo(simbadEntries, mouseX, mouseY, Color.RED);
									count++;
								}
								if (allWiseOverlay.isSelected() && allWiseEntries != null) {
									showCatalogInfo(allWiseEntries, mouseX, mouseY, Color.GREEN.darker());
									count++;
								}
								if (catWiseOverlay.isSelected() && catWiseEntries != null) {
									showCatalogInfo(catWiseEntries, mouseX, mouseY, Color.MAGENTA);
									count++;
								}
								if (unWiseOverlay.isSelected() && unWiseEntries != null) {
									showCatalogInfo(unWiseEntries, mouseX, mouseY, JColor.MINT.val);
									count++;
								}
								if (gaiaOverlay.isSelected() && gaiaEntries != null) {
									showCatalogInfo(gaiaEntries, mouseX, mouseY, Color.CYAN.darker());
									count++;
								}
								if (gaiaDR3Overlay.isSelected() && gaiaDR3Entries != null) {
									showCatalogInfo(gaiaDR3Entries, mouseX, mouseY, Color.CYAN.darker());
									count++;
								}
								if (noirlabOverlay.isSelected() && noirlabEntries != null) {
									showCatalogInfo(noirlabEntries, mouseX, mouseY, JColor.NAVY.val);
									count++;
								}
								if (panStarrsOverlay.isSelected() && panStarrsEntries != null) {
									showCatalogInfo(panStarrsEntries, mouseX, mouseY, JColor.BROWN.val);
									count++;
								}
								if (sdssOverlay.isSelected() && sdssEntries != null) {
									showCatalogInfo(sdssEntries, mouseX, mouseY, JColor.STEEL.val);
									count++;
								}
								if (spectrumOverlay.isSelected() && sdssEntries != null) {
									showSpectrumInfo(sdssEntries, mouseX, mouseY);
									count++;
								}
								if (vhsOverlay.isSelected() && vhsEntries != null) {
									showCatalogInfo(vhsEntries, mouseX, mouseY, JColor.PINK.val);
									count++;
								}
								if (uhsOverlay.isSelected() && uhsEntries != null) {
									showCatalogInfo(uhsEntries, mouseX, mouseY, JColor.DARK_YELLOW.val);
									count++;
								}
								if (ukidssOverlay.isSelected() && ukidssEntries != null) {
									showCatalogInfo(ukidssEntries, mouseX, mouseY, JColor.BLOOD.val);
									count++;
								}
								if (twoMassOverlay.isSelected() && twoMassEntries != null) {
									showCatalogInfo(twoMassEntries, mouseX, mouseY, JColor.ORANGE.val);
									count++;
								}
								if (tessOverlay.isSelected() && tessEntries != null) {
									showCatalogInfo(tessEntries, mouseX, mouseY, JColor.LILAC.val);
									count++;
								}
								if (desOverlay.isSelected() && desEntries != null) {
									showCatalogInfo(desEntries, mouseX, mouseY, JColor.SAND.val);
									count++;
								}
								if (gaiaWDOverlay.isSelected() && gaiaWDEntries != null) {
									showCatalogInfo(gaiaWDEntries, mouseX, mouseY, JColor.PURPLE.val);
									count++;
								}
								if (mocaOverlay.isSelected() && mocaEntries != null) {
									showCatalogInfo(mocaEntries, mouseX, mouseY, JColor.DARK_ORANGE.val);
									count++;
								}
								if (ssoOverlay.isSelected() && ssoEntries != null) {
									showCatalogInfo(ssoEntries, mouseX, mouseY, Color.BLUE);
									count++;
								}
								if (useCustomOverlays.isSelected()) {
									for (CustomOverlay customOverlay : customOverlays.values()) {
										if (customOverlay.getCheckBox().isSelected()) {
											showCatalogInfo(customOverlay.getCatalogEntries(), mouseX, mouseY,
													customOverlay.getColor());
											count++;
										}
									}
								}
								if (gaiaProperMotion.isSelected() && gaiaTpmEntries != null) {
									showPMInfo(gaiaTpmEntries, mouseX, mouseY, Color.CYAN.darker());
									count++;
								}
								if (gaiaDR3ProperMotion.isSelected() && gaiaDR3TpmEntries != null) {
									showPMInfo(gaiaDR3TpmEntries, mouseX, mouseY, Color.CYAN.darker());
									count++;
								}
								if (noirlabProperMotion.isSelected() && noirlabTpmEntries != null) {
									showPMInfo(noirlabTpmEntries, mouseX, mouseY, JColor.NAVY.val);
									count++;
								}
								if (catWiseProperMotion.isSelected() && catWiseTpmEntries != null) {
									showPMInfo(catWiseTpmEntries, mouseX, mouseY, Color.MAGENTA);
									count++;
								}
								if (ukidssProperMotion.isSelected() && ukidssTpmEntries != null) {
									showPMInfo(ukidssTpmEntries, mouseX, mouseY, JColor.BLOOD.val);
									count++;
								}
								if (count == 0) {
									if (showCrosshairs.isSelected()) {
										copyCoordsToClipboard(newRa, newDec);
									} else if (showCatalogsButton.isSelected()) {
										CompletableFuture.supplyAsync(() -> openNewCatalogSearch(newRa, newDec));
									} else {
										coordsField.setText(roundTo7DecNZ(newRa) + " " + roundTo7DecNZ(newDec));
										createFlipbook();
									}
								}
							}
						}

						@Override
						public void mouseReleased(MouseEvent evt) {
						}

						@Override
						public void mouseEntered(MouseEvent evt) {
							pointerX = evt.getX();
							pointerY = evt.getY();
						}

						@Override
						public void mouseExited(MouseEvent evt) {
						}

						@Override
						public void mouseClicked(MouseEvent evt) {
						}
					});

					regularImage.addMouseWheelListener((MouseWheelEvent evt) -> {
						int notches = evt.getWheelRotation();
						if (markTarget.isSelected() || drawCrosshairs.isSelected() || showCrosshairs.isSelected()) {
							if (notches < 0) {
								shapeSize++;
							} else if (shapeSize > 0) {
								shapeSize--;
							}
						} else {
							if (notches < 0) {
								fieldOfView++;
							} else if (fieldOfView > 0) {
								fieldOfView--;
							}
							changeFovLabel.setText(html(CHANGE_FOV_TEXT.formatted(fieldOfView)));
						}
					});

					if (desiLabel != null) {
						desiLabel.addMouseListener(new MouseListener() {
							@Override
							public void mousePressed(MouseEvent evt) {
								try {
									Desktop.getDesktop().browse(
											new URI(getLegacySkyViewerUrl(targetRa, targetDec, DESI_LS_DR_PARAM)));
								} catch (IOException | URISyntaxException ex) {
									throw new RuntimeException(ex);
								}
							}

							@Override
							public void mouseReleased(MouseEvent evt) {
							}

							@Override
							public void mouseEntered(MouseEvent evt) {
							}

							@Override
							public void mouseExited(MouseEvent evt) {
							}

							@Override
							public void mouseClicked(MouseEvent evt) {
							}
						});
					}

					if (ps1Label != null) {
						ps1Label.addMouseListener(new MouseListener() {
							@Override
							public void mousePressed(MouseEvent evt) {
								try {
									Desktop.getDesktop().browse(new URI(getPanstarrsUrl(targetRa, targetDec,
											fieldOfView, ImageType.STACK_AND_WARP)));
								} catch (IOException | URISyntaxException ex) {
									throw new RuntimeException(ex);
								}
							}

							@Override
							public void mouseReleased(MouseEvent evt) {
							}

							@Override
							public void mouseEntered(MouseEvent evt) {
							}

							@Override
							public void mouseExited(MouseEvent evt) {
							}

							@Override
							public void mouseClicked(MouseEvent evt) {
							}
						});
					}

					if (vhsLabel != null) {
						vhsLabel.addMouseListener(new MouseListener() {
							@Override
							public void mousePressed(MouseEvent evt) {
								try {
									String imageSize = roundTo2DecNZ(size * pixelScale / 60f);
									Desktop.getDesktop().browse(new URI(VHS_SURVEY_URL.formatted(targetRa, targetDec,
											"all", imageSize, imageSize)));
								} catch (IOException | URISyntaxException ex) {
									throw new RuntimeException(ex);
								}
							}

							@Override
							public void mouseReleased(MouseEvent evt) {
							}

							@Override
							public void mouseEntered(MouseEvent evt) {
							}

							@Override
							public void mouseExited(MouseEvent evt) {
							}

							@Override
							public void mouseClicked(MouseEvent evt) {
							}
						});
					}

					if (uhsLabel != null) {
						uhsLabel.addMouseListener(new MouseListener() {
							@Override
							public void mousePressed(MouseEvent evt) {
								try {
									String imageSize = roundTo2DecNZ(size * pixelScale / 60f);
									Desktop.getDesktop().browse(new URI(UHS_SURVEY_URL.formatted(targetRa, targetDec,
											"all", imageSize, imageSize)));
								} catch (IOException | URISyntaxException ex) {
									throw new RuntimeException(ex);
								}
							}

							@Override
							public void mouseReleased(MouseEvent evt) {
							}

							@Override
							public void mouseEntered(MouseEvent evt) {
							}

							@Override
							public void mouseExited(MouseEvent evt) {
							}

							@Override
							public void mouseClicked(MouseEvent evt) {
							}
						});
					}

					if (ukidssLabel != null) {
						ukidssLabel.addMouseListener(new MouseListener() {
							@Override
							public void mousePressed(MouseEvent evt) {
								try {
									String imageSize = roundTo2DecNZ(size * pixelScale / 60f);
									Desktop.getDesktop().browse(new URI(UKIDSS_SURVEY_URL.formatted(targetRa, targetDec,
											"all", imageSize, imageSize)));
								} catch (IOException | URISyntaxException ex) {
									throw new RuntimeException(ex);
								}
							}

							@Override
							public void mouseReleased(MouseEvent evt) {
							}

							@Override
							public void mouseEntered(MouseEvent evt) {
							}

							@Override
							public void mouseExited(MouseEvent evt) {
							}

							@Override
							public void mouseClicked(MouseEvent evt) {
							}
						});
					}
				} catch (Exception ex) {
					showExceptionDialog(baseFrame, ex);
					hasException = true;
					timer.stop();
				}
			});

			tabbedPane.addChangeListener((ChangeEvent evt) -> {
				JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
				int index = sourceTabbedPane.getSelectedIndex();
				if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME) && !flipbook.isEmpty()) {
					if (!staticView.isSelected()) {
						createFlipbook();
						timer.restart();
					}
				} else {
					timer.stop();
				}
			});

			baseFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent evt) {
					timer.stop();
					if (imageViewer != null) {
						imageViewer.getTimer().restart();
					}
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
					if (!flipbook.isEmpty()) {
						timer.stop();
					}
				}

				@Override
				public void windowActivated(WindowEvent e) {
					if (!flipbook.isEmpty() && !staticView.isSelected() && !hasException && !timerStopped) {
						timer.restart();
					}
				}
			});

			// Define actions for function keys
			Action keyActionForAltS = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					simbadOverlay.setSelected(!simbadOverlay.isSelected());
					simbadOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltA = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					allWiseOverlay.setSelected(!allWiseOverlay.isSelected());
					allWiseOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltC = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					catWiseOverlay.setSelected(!catWiseOverlay.isSelected());
					catWiseOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltU = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					unWiseOverlay.setSelected(!unWiseOverlay.isSelected());
					unWiseOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltG = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gaiaDR3Overlay.setSelected(!gaiaDR3Overlay.isSelected());
					gaiaDR3Overlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltN = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					noirlabOverlay.setSelected(!noirlabOverlay.isSelected());
					noirlabOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltP = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					panStarrsOverlay.setSelected(!panStarrsOverlay.isSelected());
					panStarrsOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltD = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sdssOverlay.setSelected(!sdssOverlay.isSelected());
					sdssOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltV = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					vhsOverlay.setSelected(!vhsOverlay.isSelected());
					vhsOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltH = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					uhsOverlay.setSelected(!uhsOverlay.isSelected());
					uhsOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltK = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ukidssOverlay.setSelected(!ukidssOverlay.isSelected());
					ukidssOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltM = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					twoMassOverlay.setSelected(!twoMassOverlay.isSelected());
					twoMassOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltT = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tessOverlay.setSelected(!tessOverlay.isSelected());
					tessOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltE = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					desOverlay.setSelected(!desOverlay.isSelected());
					desOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltW = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gaiaWDOverlay.setSelected(!gaiaWDOverlay.isSelected());
					gaiaWDOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForAltO = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mocaOverlay.setSelected(!mocaOverlay.isSelected());
					mocaOverlay.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForCtrlAltG = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gaiaDR3ProperMotion.setSelected(!gaiaDR3ProperMotion.isSelected());
					gaiaDR3ProperMotion.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForCtrlAltN = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					noirlabProperMotion.setSelected(!noirlabProperMotion.isSelected());
					noirlabProperMotion.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForCtrlAltC = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					catWiseProperMotion.setSelected(!catWiseProperMotion.isSelected());
					catWiseProperMotion.getActionListeners()[0].actionPerformed(null);
				}
			};
			Action keyActionForCtrlAltK = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ukidssProperMotion.setSelected(!ukidssProperMotion.isSelected());
					ukidssProperMotion.getActionListeners()[0].actionPerformed(null);
				}
			};

			// Assign actions to function keys
			InputMap iMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			ActionMap aMap = mainPanel.getActionMap();

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK), "keyActionForAltS");
			aMap.put("keyActionForAltS", keyActionForAltS);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK), "keyActionForAltA");
			aMap.put("keyActionForAltA", keyActionForAltA);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK), "keyActionForAltC");
			aMap.put("keyActionForAltC", keyActionForAltC);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.ALT_MASK), "keyActionForAltU");
			aMap.put("keyActionForAltU", keyActionForAltU);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK), "keyActionForAltG");
			aMap.put("keyActionForAltG", keyActionForAltG);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK), "keyActionForAltN");
			aMap.put("keyActionForAltN", keyActionForAltN);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK), "keyActionForAltP");
			aMap.put("keyActionForAltP", keyActionForAltP);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK), "keyActionForAltD");
			aMap.put("keyActionForAltD", keyActionForAltD);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.ALT_MASK), "keyActionForAltV");
			aMap.put("keyActionForAltV", keyActionForAltV);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK), "keyActionForAltH");
			aMap.put("keyActionForAltH", keyActionForAltH);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.ALT_MASK), "keyActionForAltK");
			aMap.put("keyActionForAltK", keyActionForAltK);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK), "keyActionForAltM");
			aMap.put("keyActionForAltM", keyActionForAltM);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK), "keyActionForAltT");
			aMap.put("keyActionForAltT", keyActionForAltT);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK), "keyActionForAltE");
			aMap.put("keyActionForAltE", keyActionForAltE);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.ALT_MASK), "keyActionForAltW");
			aMap.put("keyActionForAltW", keyActionForAltW);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK), "keyActionForAltO");
			aMap.put("keyActionForAltO", keyActionForAltO);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK),
					"keyActionForCtrlAltG");
			aMap.put("keyActionForCtrlAltG", keyActionForCtrlAltG);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK),
					"keyActionForCtrlAltN");
			aMap.put("keyActionForCtrlAltN", keyActionForCtrlAltN);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK),
					"keyActionForCtrlAltC");
			aMap.put("keyActionForCtrlAltC", keyActionForCtrlAltC);

			iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK),
					"keyActionForCtrlAltK");
			aMap.put("keyActionForCtrlAltK", keyActionForCtrlAltK);

			if (visible) {
				tabbedPane.addTab(TAB_NAME, mainPanel);
			}
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
			hasException = true;
		}
	}

	private void addMagnifiedImage(String imageLabel, BufferedImage image, int upperLeftX, int upperLeftY, int width,
			int height) {
		try {
			BufferedImage magnifiedImage = image.getSubimage(upperLeftX, upperLeftY, width, height);
			magnifiedImage = zoomImage(magnifiedImage, 200);
			JLabel magnifiedLabel = addTextToImage(new JLabel(new ImageIcon(magnifiedImage)), imageLabel);
			magnifiedLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
			rightPanel.add(magnifiedLabel);
		} catch (RasterFormatException ex) {
			writeErrorLog(ex);
		}
	}

	private void resetContrastSlider() {
		ChangeListener changeListener;

		brightness = 1;
		changeListener = brightnessSlider.getChangeListeners()[0];
		brightnessSlider.removeChangeListener(changeListener);
		brightnessSlider.setValue(brightness);
		brightnessSlider.addChangeListener(changeListener);

		contrast = 50;
		changeListener = contrastSlider.getChangeListeners()[0];
		contrastSlider.removeChangeListener(changeListener);
		contrastSlider.setValue(contrast);
		contrastSlider.addChangeListener(changeListener);
	}

	private NumberPair undoRotationOfPixelCoords(int mouseX, int mouseY) {
		double anchorX = wiseImage.getWidth() / 2;
		double anchorY = wiseImage.getHeight() / 2;
		double angle = (4 - quadrantCount) * 90;
		double theta = toRadians(angle);
		Point2D ptSrc = new Point(mouseX, mouseY);
		Point2D ptDst = new Point();
		AffineTransform.getRotateInstance(theta, anchorX, anchorY).transform(ptSrc, ptDst);
		mouseX = (int) round(ptDst.getX());
		mouseY = (int) round(ptDst.getY());
		return new NumberPair(mouseX, mouseY);
	}

	private NumberPair toWorldCoordinates(double x, double y) {
		x = getUnzoomedValue(x);
		y = getUnzoomedValue(y);
		x += 0.5;
		y -= 0.5;
		y = naxis2 - y;
		x -= crpix1;
		y -= crpix2;
		double scale = DEG_ARCSEC / pixelScale;
		x = toRadians(x) / -scale;
		y = toRadians(y) / scale;
		double ra0 = toRadians(crval1);
		double dec0 = toRadians(crval2);
		double p = sqrt(x * x + y * y);
		double c = atan(p);
		double ra = ra0 + atan2(x * sin(c), p * cos(dec0) * cos(c) - y * sin(dec0) * sin(c));
		double dec = asin(cos(c) * sin(dec0) + (y * sin(c) * cos(dec0)) / p);
		ra = toDegrees(ra);
		dec = toDegrees(dec);
		// Correct ra if < 0
		if (ra < 0) {
			ra += 360;
		}
		return new NumberPair(ra, dec);
	}

	private NumberPair toPixelCoordinates(double ra, double dec) {
		ra = toRadians(ra);
		dec = toRadians(dec);
		double ra0 = toRadians(crval1);
		double dec0 = toRadians(crval2);
		double cosc = sin(dec0) * sin(dec) + cos(dec0) * cos(dec) * cos(ra - ra0);
		double x = (cos(dec) * sin(ra - ra0)) / cosc;
		double y = (cos(dec0) * sin(dec) - sin(dec0) * cos(dec) * cos(ra - ra0)) / cosc;
		double scale = DEG_ARCSEC / pixelScale;
		x = toDegrees(x) * -scale;
		y = toDegrees(y) * scale;
		x += crpix1;
		y += crpix2;
		y = naxis2 - y;
		x -= 0.5;
		y += 0.5;
		x = getZoomedValue(x);
		y = getZoomedValue(y);
		return new NumberPair(x, y);
	}

	private double getUnzoomedValue(double value) {
		return value * size / zoom;
	}

	private double getZoomedValue(double value) {
		return value * zoom / size;
	}

	public void createFlipbook() {
		if (asyncDownloads) {
			CompletableFuture.supplyAsync(() -> assembleFlipbook());
		} else {
			assembleFlipbook();
		}
	}

	public boolean assembleFlipbook() {
		try {
			timer.stop();
			timerStopped = true;
			stopDownloadProcess = false;
			String coords = coordsField.getText();
			if (coords.isEmpty()) {
				showErrorDialog(baseFrame, "Coordinates must not be empty!");
				return false;
			}
			String imageSize = sizeField.getText();
			if (imageSize.isEmpty()) {
				showErrorDialog(baseFrame, "Field of view must not be empty!");
				return false;
			}
			List<String> errorMessages = new ArrayList<>();
			try {
				NumberPair coordinates = getCoordinates(coords);
				targetRa = coordinates.getX();
				targetDec = coordinates.getY();
				if (targetRa < 0) {
					errorMessages.add("RA must not be smaller than 0 deg.");
				}
				if (targetRa > 360) {
					errorMessages.add("RA must not be greater than 360 deg.");
				}
				if (targetDec < -90) {
					errorMessages.add("Dec must not be smaller than -90 deg.");
				}
				if (targetDec > 90) {
					errorMessages.add("Dec must not be greater than 90 deg.");
				}
			} catch (Exception ex) {
				targetRa = 0;
				targetDec = 0;
				errorMessages.add("Invalid coordinates!");
			}
			try {
				size = (int) ceil(toInteger(sizeField.getText()) / pixelScale);
				if (desiCutouts.isSelected() || ps1Cutouts.isSelected()) {
					if (size > 1200) {
						errorMessages.add("Field of view must not be larger than 300 arcsec.");
					}
				} else {
					if (size > 1091) {
						errorMessages.add("Field of view must not be larger than 3000 arcsec.");
					}
				}
			} catch (Exception ex) {
				size = 0;
				errorMessages.add("Invalid field of view!");
			}
			if (!errorMessages.isEmpty()) {
				String errorMessage = String.join(LINE_SEP, errorMessages);
				showErrorDialog(baseFrame, errorMessage);
				return false;
			}
			baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			sizeField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			if (!isSameTarget(targetRa, targetDec, size, previousRa, previousDec, previousSize)) {
				skipIntermediateEpochs.setEnabled(false);
				separateScanDirections.setEnabled(false);
				differenceImaging.setEnabled(false);
				wiseviewCutouts.setEnabled(false);
				unwiseCutouts.setEnabled(false);
				desiCutouts.setEnabled(false);
				ps1Cutouts.setEnabled(false);

				int panstarrsFOV = toInteger(panstarrsField.getText());
				int aladinLiteFOV = toInteger(aladinLiteField.getText());
				int wiseViewFOV = toInteger(wiseViewField.getText());
				int finderChartFOV = toInteger(finderChartField.getText());
				int defaultFOV = toInteger(sizeField.getText());
				panstarrsFOV = panstarrsFOV == 0 ? defaultFOV : panstarrsFOV;
				aladinLiteFOV = aladinLiteFOV == 0 ? defaultFOV : aladinLiteFOV;
				wiseViewFOV = wiseViewFOV == 0 ? defaultFOV : wiseViewFOV;
				finderChartFOV = finderChartFOV == 0 ? defaultFOV : finderChartFOV;
				createHyperlink(panstarrsLabel, getPanstarrsUrl(targetRa, targetDec, panstarrsFOV, ImageType.STACK));
				createHyperlink(aladinLiteLabel, getAladinLiteUrl(targetRa, targetDec, aladinLiteFOV));
				createHyperlink(wiseViewLabel,
						getWiseViewUrl(targetRa, targetDec, wiseViewFOV, skipIntermediateEpochs.isSelected() ? 1 : 0,
								separateScanDirections.isSelected() ? 1 : 0, differenceImaging.isSelected() ? 1 : 0));
				createHyperlink(finderChartLabel, getFinderChartUrl(targetRa, targetDec, finderChartFOV));
				createHyperlink(legacyViewerLabel, getLegacySkyViewerUrl(targetRa, targetDec, "unwise-neo6"));
				String fovSize = roundTo2DecNZ(defaultFOV / 60f);
				createHyperlink(ukidssCutoutsLabel,
						UKIDSS_SURVEY_URL.formatted(targetRa, targetDec, "all", fovSize, fovSize));
				createHyperlink(vhsCutoutsLabel,
						VHS_SURVEY_URL.formatted(targetRa, targetDec, "all", fovSize, fovSize));
				createHyperlink(simbadLabel, getSimbadUrl(targetRa, targetDec, 30));
				createHyperlink(vizierLabel, getVizierUrl(targetRa, targetDec, 30, 50, false));

				loadImages = true;
				flipbookComplete = false;
				hasException = false;
				imageCutOff = false;
				imagesW1.clear();
				imagesW2.clear();
				imagesW1All.clear();
				imagesW2All.clear();
				imagesW1Ends.clear();
				imagesW2Ends.clear();
				crosshairs.clear();
				crosshairCoords.setText("");
				naxis1 = naxis2 = size;
				pointerX = pointerY = 0;
				windowShift = 0;
				year_ps1_y_i_g = 0;
				year_vhs_k_h_j = 0;
				year_uhs_k_j = 0;
				year_ukidss_k_h_j = 0;
				// year_sdss_z_g_u = 0;
				year_dss_2ir_1r_1b = 0;
				initCatalogEntries();
				if (resetContrast.isSelected()) {
					resetContrastSlider();
				}
				desiImage = null;
				processedDesiImage = null;
				if (legacyImages) {
					CompletableFuture.supplyAsync(() -> {
						desiImage = fetchDesiImage(targetRa, targetDec, size);
						processedDesiImage = zoomImage(rotateImage(desiImage, quadrantCount), zoom);
						return null;
					});
				}
				ps1Image = null;
				processedPs1Image = null;
				if (panstarrsImages) {
					CompletableFuture.supplyAsync(() -> {
						ps1Image = fetchPs1Image(targetRa, targetDec, size);
						processedPs1Image = zoomImage(rotateImage(ps1Image, quadrantCount), zoom);
						return null;
					});
				}
				vhsImage = null;
				processedVhsImage = null;
				if (vhsImages) {
					CompletableFuture.supplyAsync(() -> {
						vhsImage = fetchVhsImage(targetRa, targetDec, size);
						processedVhsImage = zoomImage(rotateImage(vhsImage, quadrantCount), zoom);
						return null;
					});
				}
				uhsImage = null;
				processedUhsImage = null;
				if (uhsImages) {
					CompletableFuture.supplyAsync(() -> {
						uhsImage = fetchUhsImage(targetRa, targetDec, size);
						processedUhsImage = zoomImage(rotateImage(uhsImage, quadrantCount), zoom);
						return null;
					});
				}
				ukidssImage = null;
				processedUkidssImage = null;
				if (ukidssImages) {
					CompletableFuture.supplyAsync(() -> {
						ukidssImage = fetchUkidssImage(targetRa, targetDec, size);
						processedUkidssImage = zoomImage(rotateImage(ukidssImage, quadrantCount), zoom);
						return null;
					});
				}
				sdssImage = null;
				processedSdssImage = null;
				if (sdssImages) {
					CompletableFuture.supplyAsync(() -> {
						sdssImage = fetchSdssImage(targetRa, targetDec, size);
						processedSdssImage = zoomImage(rotateImage(sdssImage, quadrantCount), zoom);
						return null;
					});
				}
				dssImage = null;
				processedDssImage = null;
				if (dssImages) {
					CompletableFuture.supplyAsync(() -> {
						dssImage = fetchDssImage(targetRa, targetDec, size);
						processedDssImage = zoomImage(rotateImage(dssImage, quadrantCount), zoom);
						return null;
					});
				}
				if (nearestBywSubjects) {
					bywTopRow.removeAll();
					bywBottomRow.removeAll();
					List<JLabel> subjects = getNearestZooniverseSubjects(targetRa, targetDec);
					int numberOfSubjects = subjects.size();
					if (numberOfSubjects == 0) {
						bywTopRow.add(new JLabel("None"));
					} else {
						for (int i = 0; i < 4 && i < numberOfSubjects; i++) {
							bywTopRow.add(subjects.get(i));
						}
						for (int i = 4; i < 8 && i < numberOfSubjects; i++) {
							bywBottomRow.add(subjects.get(i));
						}
					}
				}
				tile = getWiseTiles(targetRa, targetDec).getFirst();
			}

			previousSize = size;
			previousRa = targetRa;
			previousDec = targetDec;
			imageNumber = 0;

			if (loadImages) {
				epochCount = 0;
				band1Images.clear();
				band2Images.clear();
				imagePanel.removeAll();
				rightPanel.removeAll();
				if (asyncDownloads) {
					downloadLog = new JTextArea();
					downloadLog.setFont(new JLabel().getFont());
					downloadLog.setEditable(false);
					DefaultCaret caret = (DefaultCaret) downloadLog.getCaret();
					caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
					imagePanel.add(downloadLog);
					baseFrame.repaint();
				}
				writeLogEntry("Target: " + coordsField.getText() + " FoV: " + sizeField.getText() + "\"");
				List<Epoch> epochsW1;
				List<Epoch> epochsW2;
				if (unwiseCutouts.isSelected()) {
					epochsW1 = new ArrayList<>();
					epochsW2 = new ArrayList<>();
					double mjd = convertDateTimeToMJD(LocalDate.of(2010, 6, 1).atStartOfDay()).doubleValue();
					epochsW1.add(new Epoch(1, 0, 0, mjd));
					epochsW1.add(new Epoch(1, 1, 1, mjd));
					epochsW2.add(new Epoch(2, 0, 0, mjd));
					epochsW2.add(new Epoch(2, 1, 1, mjd));
					int year = 2013;
					for (int i = 2; i <= (NUMBER_OF_UNWISE_EPOCHS - 1) * 2 + 1; i++) {
						int forward = i % 2 == 0 ? 0 : 1;
						mjd = convertDateTimeToMJD(LocalDate.of(year + i / 2, 6, 1).atStartOfDay()).doubleValue();
						epochsW1.add(new Epoch(1, i, forward, mjd));
						epochsW2.add(new Epoch(2, i, forward, mjd));
					}
				} else {
					epochsW1 = tile.getEpochs().stream().filter(e -> (e.getBand() == 1)).collect(toList());
					epochsW2 = tile.getEpochs().stream().filter(e -> (e.getBand() == 2)).collect(toList());
				}
				epochsW1.sort(Comparator.comparingInt(Epoch::getEpoch).thenComparingInt(Epoch::getForward));
				epochsW2.sort(Comparator.comparingInt(Epoch::getEpoch).thenComparingInt(Epoch::getForward));
				if (skipIntermediateEpochs.isSelected()) {
					List<Epoch> tempEpochs;

					tempEpochs = new ArrayList();
					for (int i = 0; i < epochsW1.size(); i++) {
						if (epochsW1.get(i).getForward() == 0) {
							tempEpochs.add(epochsW1.get(i));
							break;
						}
					}
					for (int i = 0; i < epochsW1.size(); i++) {
						if (epochsW1.get(i).getForward() == 1) {
							tempEpochs.add(epochsW1.get(i));
							break;
						}
					}
					for (int i = epochsW1.size() - 1; i >= 0; i--) {
						if (epochsW1.get(i).getForward() == 0) {
							tempEpochs.add(epochsW1.get(i));
							break;
						}
					}
					for (int i = epochsW1.size() - 1; i >= 0; i--) {
						if (epochsW1.get(i).getForward() == 1) {
							tempEpochs.add(epochsW1.get(i));
							break;
						}
					}
					epochsW1 = tempEpochs;

					tempEpochs = new ArrayList();
					for (int i = 0; i < epochsW2.size(); i++) {
						if (epochsW2.get(i).getForward() == 0) {
							tempEpochs.add(epochsW2.get(i));
							break;
						}
					}
					for (int i = 0; i < epochsW2.size(); i++) {
						if (epochsW2.get(i).getForward() == 1) {
							tempEpochs.add(epochsW2.get(i));
							break;
						}
					}
					for (int i = epochsW2.size() - 1; i >= 0; i--) {
						if (epochsW2.get(i).getForward() == 0) {
							tempEpochs.add(epochsW2.get(i));
							break;
						}
					}
					for (int i = epochsW2.size() - 1; i >= 0; i--) {
						if (epochsW2.get(i).getForward() == 1) {
							tempEpochs.add(epochsW2.get(i));
							break;
						}
					}
					epochsW2 = tempEpochs;
				}
				switch (wiseBand) {
				case W1 -> downloadRequestedEpochs(null, WiseBand.W1.val, epochsW1, imagesW1);
				case W2 -> downloadRequestedEpochs(null, WiseBand.W2.val, epochsW2, imagesW2);
				case W1W2 -> {
					downloadRequestedEpochs(null, WiseBand.W1.val, epochsW1, imagesW1);
					if (stopDownloadProcess) {
						break;
					}
					downloadRequestedEpochs(null, WiseBand.W2.val, epochsW2, imagesW2);
				}
				}
				if (stopDownloadProcess) {
					writeLogEntry("Download process stopped.");
					return false;
				} else {
					writeLogEntry("Finished.");
				}
				if (asyncDownloads) {
					downloadLog.setCaretPosition(0);
				}
				if (epochCount < 2) {
					showInfoDialog(baseFrame, "No images found for the given coordinates.");
					hasException = true;
					return false;
				}
			}
			loadImages = false;

			List<Fits> band1Scan1Images = new ArrayList();
			List<Fits> band1Scan2Images = new ArrayList();
			for (Fits fits : band1Images) {
				ImageHDU hdu = (ImageHDU) fits.getHDU(0);
				long forward = hdu.getHeader().getLongValue("FORWARD");
				if (forward == 0) {
					band1Scan1Images.add(fits);
				} else {
					band1Scan2Images.add(fits);
				}
			}

			List<Fits> band2Scan1Images = new ArrayList();
			List<Fits> band2Scan2Images = new ArrayList();
			for (Fits fits : band2Images) {
				ImageHDU hdu = (ImageHDU) fits.getHDU(0);
				long forward = hdu.getHeader().getLongValue("FORWARD");
				if (forward == 0) {
					band2Scan1Images.add(fits);
				} else {
					band2Scan2Images.add(fits);
				}
			}

			boolean sep = separateScanDirections.isSelected();
			boolean diff = differenceImaging.isSelected();
			boolean desi = desiCutouts.isSelected();
			boolean ps1 = ps1Cutouts.isSelected();

			if (wiseviewCutouts.isSelected() || unwiseCutouts.isSelected() || ps1Cutouts.isSelected()) {
				band1Scan1Images = stackImages(band1Scan1Images, stackSize);
				band1Scan2Images = stackImages(band1Scan2Images, stackSize);
				band2Scan1Images = stackImages(band2Scan1Images, stackSize);
				band2Scan2Images = stackImages(band2Scan2Images, stackSize);
			}

			List<Fits> band1GroupedImages = new ArrayList();
			List<Fits> band2GroupedImages = new ArrayList();

			if (sep) {
				if (diff) {
					// Band W1 -> Scan ASC
					for (int i = 0; i < band1Scan1Images.size() - 1; i++) {
						band1GroupedImages.add(subtractImages(band1Scan1Images.get(0), band1Scan1Images.get(i + 1)));
					}
					// Band W1 -> Scan DESC
					for (int i = 0; i < band1Scan2Images.size() - 1; i++) {
						band1GroupedImages.add(subtractImages(band1Scan2Images.get(i + 1), band1Scan2Images.get(0)));
					}
					// Band W2 -> Scan ASC
					for (int i = 0; i < band2Scan1Images.size() - 1; i++) {
						band2GroupedImages.add(subtractImages(band2Scan1Images.get(0), band2Scan1Images.get(i + 1)));
					}
					// Band W2 -> Scan DESC
					for (int i = 0; i < band2Scan2Images.size() - 1; i++) {
						band2GroupedImages.add(subtractImages(band2Scan2Images.get(i + 1), band2Scan2Images.get(0)));
					}
				} else {
					// Band W1 -> Scan ASC
					for (int i = 0; i < band1Scan1Images.size(); i++) {
						band1GroupedImages.add(band1Scan1Images.get(i));
					}
					// Band W1 -> Scan DESC
					for (int i = 0; i < band1Scan2Images.size(); i++) {
						band1GroupedImages.add(band1Scan2Images.get(i));
					}
					// Band W2 -> Scan ASC
					for (int i = 0; i < band2Scan1Images.size(); i++) {
						band2GroupedImages.add(band2Scan1Images.get(i));
					}
					// Band W2 -> Scan DESC
					for (int i = 0; i < band2Scan2Images.size(); i++) {
						band2GroupedImages.add(band2Scan2Images.get(i));
					}
				}
			} else {
				// Band W1 -> Scan ASC+DESC
				for (int i = 0; i < band1Scan1Images.size() && i < band1Scan2Images.size(); i++) {
					band1GroupedImages.add(addImages(band1Scan1Images.get(i), band1Scan2Images.get(i)));
				}
				// Band W2 -> Scan ASC+DESC
				for (int i = 0; i < band2Scan1Images.size() && i < band2Scan2Images.size(); i++) {
					band2GroupedImages.add(addImages(band2Scan1Images.get(i), band2Scan2Images.get(i)));
				}
			}

			flipbook.clear();

			String band;
			switch (wiseBand) {
			case W1 -> {
				if (desi) {
					band = "DECaLS r";
				} else if (ps1) {
					band = "PS1 r";
				} else {
					band = "W1";
				}
				for (int i = 0; i < band1GroupedImages.size(); i++) {
					Fits fits = band1GroupedImages.get(i);
					flipbook.add(new FlipbookComponent(fits, null, band,
							desi ? getDataRelease(fits) : getMeanObsDate(fits), isFirstEpoch(fits)));
				}
			}
			case W2 -> {
				if (desi) {
					band = "DECaLS z";
				} else if (ps1) {
					band = "PS1 y";
				} else {
					band = "W2";
				}
				for (int i = 0; i < band2GroupedImages.size(); i++) {
					Fits fits = band2GroupedImages.get(i);
					flipbook.add(new FlipbookComponent(null, fits, band,
							desi ? getDataRelease(fits) : getMeanObsDate(fits), isFirstEpoch(fits)));
				}
			}
			case W1W2 -> {
				if (desi) {
					band = "DECaLS r+z";
				} else if (ps1) {
					band = "PS1 r+y";
				} else {
					band = "W1+W2";
				}
				int size1 = band1GroupedImages.size();
				int size2 = band2GroupedImages.size();
				for (int i = 0; i < min(size1, size2); i++) {
					Fits fits1 = band1GroupedImages.get(i);
					Fits fits2 = band2GroupedImages.get(i);
					flipbook.add(new FlipbookComponent(fits1, fits2, band,
							desi ? getDataRelease(fits1) : getMeanObsDate(fits1), isFirstEpoch(fits1)));
				}
			}
			}

			int count = flipbook.size();
			if (count > 0) {
				NumberPair refVal = getRefValues(flipbook.get(0));
				minValue = (int) refVal.getX();
				maxValue = (int) refVal.getY();
			}

			flipbookComplete = true;
			processImages();
			timer.restart();
			timerStopped = false;
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
			hasException = true;
		} finally {
			enableAll();
		}
		return true;
	}

	private boolean isFirstEpoch(Fits fits) throws Exception {
		ImageHDU hdu = (ImageHDU) fits.getHDU(0);
		long firstEpoch = hdu.getHeader().getLongValue("FEPOCH");
		return firstEpoch == 1;
	}

	private List<Fits> stackImages(List<Fits> images, int stackSize) {
		if (stackSize < 2) {
			return images;
		}
		try {
			List<Fits> list = new ArrayList();
			Fits fits = images.get(0);
			int j = 1;
			for (int i = 1; i < images.size(); i++) {
				if (j < stackSize) {
					fits = addImages(fits, images.get(i));
					j++;
				} else {
					list.add(average(fits, j));
					fits = images.get(i);
					j = 1;
				}
			}
			if (j > 1) {
				list.add(average(fits, j));
			}
			if (list.isEmpty()) {
				stackSize--;
				return stackImages(images, stackSize);
			}
			return list;
		} catch (Exception ex) {
			return images;
		}
	}

	private void enableAll() {
		skipIntermediateEpochs.setEnabled(true);
		if (!differenceImaging.isSelected()) {
			separateScanDirections.setEnabled(true);
		}
		differenceImaging.setEnabled(true);
		wiseviewCutouts.setEnabled(true);
		unwiseCutouts.setEnabled(true);
		desiCutouts.setEnabled(true);
		ps1Cutouts.setEnabled(true);
		if (waitCursor) {
			baseFrame.setCursor(Cursor.getDefaultCursor());
			coordsField.setCursor(Cursor.getDefaultCursor());
			sizeField.setCursor(Cursor.getDefaultCursor());
		}
	}

	private NumberPair getRefValues(FlipbookComponent component) throws Exception {
		Fits fits;
		fits = component.getFits2();
		if (fits != null) {
			ImageHDU hdu = (ImageHDU) fits.getHDU(0);
			ImageData imageData = hdu.getData();
			float[][] values = (float[][]) imageData.getData();
			NumberPair refValues = determineRefValues(values);
			double minVal = refValues.getX();
			double maxVal = refValues.getY();
			return new NumberPair(minVal, maxVal);
		}
		fits = component.getFits1();
		if (fits != null) {
			ImageHDU hdu = (ImageHDU) fits.getHDU(0);
			ImageData imageData = hdu.getData();
			float[][] values = (float[][]) imageData.getData();
			NumberPair refValues = determineRefValues(values);
			double minVal = refValues.getX();
			double maxVal = refValues.getY();
			return new NumberPair(minVal, maxVal);
		}
		return null;
	}

	private void processImages() {
		if (desiImage != null) {
			processedDesiImage = zoomImage(rotateImage(desiImage, quadrantCount), zoom);
		}
		if (ps1Image != null) {
			processedPs1Image = zoomImage(rotateImage(ps1Image, quadrantCount), zoom);
		}
		if (ukidssImage != null) {
			processedUkidssImage = zoomImage(rotateImage(ukidssImage, quadrantCount), zoom);
		}
		if (vhsImage != null) {
			processedVhsImage = zoomImage(rotateImage(vhsImage, quadrantCount), zoom);
		}
		if (sdssImage != null) {
			processedSdssImage = zoomImage(rotateImage(sdssImage, quadrantCount), zoom);
		}
		if (dssImage != null) {
			processedDssImage = zoomImage(rotateImage(dssImage, quadrantCount), zoom);
		}
		if (flipbook.isEmpty() || !flipbookComplete) {
			return;
		}
		timer.stop();
		for (int i = 0; i < flipbook.size(); i++) {
			FlipbookComponent component = flipbook.get(i);
			component.setImage(processImage(component, i));
		}
		timer.restart();
	}

	public void initCatalogEntries() {
		simbadEntries = null;
		allWiseEntries = null;
		catWiseEntries = null;
		catWiseRejectEntries = null;
		catWiseTpmEntries = null;
		unWiseEntries = null;
		gaiaEntries = null;
		gaiaDR3Entries = null;
		gaiaTpmEntries = null;
		gaiaDR3TpmEntries = null;
		noirlabEntries = null;
		noirlabTpmEntries = null;
		panStarrsEntries = null;
		sdssEntries = null;
		vhsEntries = null;
		uhsEntries = null;
		ukidssEntries = null;
		ukidssTpmEntries = null;
		twoMassEntries = null;
		tessEntries = null;
		desEntries = null;
		gaiaWDEntries = null;
		mocaEntries = null;
		ssoEntries = null;
		if (useCustomOverlays.isSelected()) {
			customOverlays.values().forEach((customOverlay) -> {
				customOverlay.setCatalogEntries(null);
			});
		}
	}

	private void createStaticBook() {
		timer.stop();
		JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT));
		for (int i = 0; i < flipbook.size(); i++) {
			FlipbookComponent component = flipbook.get(i);
			BufferedImage image = addCrosshairs(processImage(component, i));
			JScrollPane scrollPanel = new JScrollPane(
					addTextToImage(new JLabel(new ImageIcon(image)), component.getTitle()));
			grid.add(scrollPanel);
		}
		if (desiImage != null) {
			BufferedImage image = zoomImage(rotateImage(desiImage, quadrantCount), zoom);
			JScrollPane pane = new JScrollPane(
					addTextToImage(new JLabel(new ImageIcon(image)), getImageLabel("LS", DESI_LS_DR_LABEL)));
			grid.add(pane);
		}
		if (ps1Image != null) {
			BufferedImage image = zoomImage(rotateImage(ps1Image, quadrantCount), zoom);
			JScrollPane pane = new JScrollPane(
					addTextToImage(new JLabel(new ImageIcon(image)), getImageLabel("PS1", year_ps1_y_i_g)));
			grid.add(pane);
		}
		if (vhsImage != null) {
			BufferedImage image = zoomImage(rotateImage(vhsImage, quadrantCount), zoom);
			JScrollPane pane = new JScrollPane(
					addTextToImage(new JLabel(new ImageIcon(image)), getImageLabel(VHS_LABEL, year_vhs_k_h_j)));
			grid.add(pane);
		}
		if (uhsImage != null) {
			BufferedImage image = zoomImage(rotateImage(uhsImage, quadrantCount), zoom);
			JScrollPane pane = new JScrollPane(
					addTextToImage(new JLabel(new ImageIcon(image)), getImageLabel(UHS_LABEL, year_uhs_k_j)));
			grid.add(pane);
		}
		if (ukidssImage != null) {
			BufferedImage image = zoomImage(rotateImage(ukidssImage, quadrantCount), zoom);
			JScrollPane pane = new JScrollPane(
					addTextToImage(new JLabel(new ImageIcon(image)), getImageLabel(UKIDSS_LABEL, year_ukidss_k_h_j)));
			grid.add(pane);
		}
		if (sdssImage != null) {
			BufferedImage image = zoomImage(rotateImage(sdssImage, quadrantCount), zoom);
			JScrollPane pane = new JScrollPane(addTextToImage(new JLabel(new ImageIcon(image)), SDSS_LABEL));
			grid.add(pane);
		}
		if (dssImage != null) {
			BufferedImage image = zoomImage(rotateImage(dssImage, quadrantCount), zoom);
			JScrollPane pane = new JScrollPane(
					addTextToImage(new JLabel(new ImageIcon(image)), getImageLabel("DSS", year_dss_2ir_1r_1b)));
			grid.add(pane);
		}
		imagePanel.removeAll();
		imagePanel.setBorder(createEmptyBorder(""));
		imagePanel.add(grid);
		baseFrame.setVisible(true);
	}

	public BufferedImage processImage(FlipbookComponent component, int epoch) {
		BufferedImage image;
		if (wiseBand.equals(WiseBand.W1W2)) {
			image = createColorImage(component.getFits1(), component.getFits2());
		} else {
			image = createImage(component.getFits1() == null ? component.getFits2() : component.getFits1());
		}
		image = zoomImage(image, zoom);
		image = flipImage(image);
		addOverlaysAndPMVectors(image, epoch);
		return image;
	}

	private BufferedImage addCrosshairs(BufferedImage image) {
		// Copy the picture to draw shapes in real time
		if (markTarget.isSelected() || drawCrosshairs.isSelected() || showCrosshairs.isSelected()) {
			image = copyImage(image);
		}

		// Mark target coordinates
		if (markTarget.isSelected()) {
			NumberPair position = toPixelCoordinates(targetRa, targetDec);
			Circle circle = new Circle(position.getX(), position.getY(), shapeSize * zoom / 100, Color.RED);
			circle.draw(image.getGraphics());
			circle = new Circle(position.getX(), position.getY(), 1, Color.RED);
			circle.draw(image.getGraphics());
		}

		// Draw crosshairs
		if (drawCrosshairs.isSelected()) {
			for (int i = 0; i < crosshairs.size(); i++) {
				NumberPair crosshair = crosshairs.get(i);
				String label = String.valueOf(i + 1);
				CrossHair drawable = new CrossHair(crosshair.getX() * zoom, crosshair.getY() * zoom,
						shapeSize * zoom / 100, Color.RED, label);
				drawable.draw(image.getGraphics());
			}
		}

		// Rotate image by the given number of quadrants
		image = rotateImage(image, quadrantCount);

		// Show crosshairs with coordinates
		if (showCrosshairs.isSelected()) {
			NumberPair coordinates;
			if (quadrantCount > 0 && quadrantCount < 4) {
				NumberPair pixelCoords = undoRotationOfPixelCoords(pointerX, pointerY);
				coordinates = toWorldCoordinates((int) pixelCoords.getX(), (int) pixelCoords.getY());
			} else {
				coordinates = toWorldCoordinates(pointerX, pointerY);
			}
			String label = roundTo3DecNZ(coordinates.getX()) + " " + roundTo3DecNZ(coordinates.getY());
			CrossHair drawable = new CrossHair(pointerX, pointerY, shapeSize * zoom / 100, Color.RED, label);
			drawable.draw(image.getGraphics());
		}
		return image;
	}

	private void addOverlaysAndPMVectors(BufferedImage image, int epoch) {
		if (simbadOverlay.isSelected()) {
			if (simbadEntries == null) {
				simbadEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					simbadEntries = fetchCatalogEntries(new SimbadCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, simbadEntries, Color.RED, Shape.CIRCLE);
			}
		}
		if (allWiseOverlay.isSelected()) {
			if (allWiseEntries == null) {
				allWiseEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					allWiseEntries = fetchCatalogEntries(new AllWiseCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, allWiseEntries, Color.GREEN.darker(), Shape.CIRCLE);
			}
		}
		if (catWiseOverlay.isSelected()) {
			if (catWiseEntries == null) {
				catWiseEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					catWiseEntries = fetchCatalogEntries(new CatWiseCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, catWiseEntries, Color.MAGENTA, Shape.CIRCLE);
			}
		}
		if (unWiseOverlay.isSelected()) {
			if (unWiseEntries == null) {
				unWiseEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					unWiseEntries = fetchCatalogEntries(new UnWiseCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, unWiseEntries, JColor.MINT.val, Shape.CIRCLE);
			}
		}
		if (gaiaOverlay.isSelected()) {
			if (gaiaEntries == null) {
				gaiaEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					gaiaEntries = fetchCatalogEntries(new GaiaDR2CatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, gaiaEntries, Color.CYAN.darker(), Shape.CIRCLE);
			}
		}
		if (gaiaDR3Overlay.isSelected()) {
			if (gaiaDR3Entries == null) {
				gaiaDR3Entries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					gaiaDR3Entries = fetchCatalogEntries(new GaiaDR3CatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, gaiaDR3Entries, Color.CYAN.darker(), Shape.DIAMOND);
			}
		}
		if (noirlabOverlay.isSelected()) {
			if (noirlabEntries == null) {
				noirlabEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					noirlabEntries = fetchCatalogEntries(new NoirlabCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, noirlabEntries, JColor.NAVY.val, Shape.CIRCLE);
			}
		}
		if (panStarrsOverlay.isSelected()) {
			if (panStarrsEntries == null) {
				panStarrsEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					panStarrsEntries = fetchCatalogEntries(new PanStarrsCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, panStarrsEntries, JColor.BROWN.val, Shape.CIRCLE);
			}
		}
		if (sdssOverlay.isSelected()) {
			if (sdssEntries == null) {
				sdssEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					sdssEntries = fetchCatalogEntries(new SdssCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, sdssEntries, JColor.STEEL.val, Shape.CIRCLE);
			}
		}
		if (spectrumOverlay.isSelected()) {
			if (sdssEntries == null) {
				sdssEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					sdssEntries = fetchCatalogEntries(new SdssCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawSpectrumOverlay(image, sdssEntries);
			}
		}
		if (vhsOverlay.isSelected()) {
			if (vhsEntries == null) {
				vhsEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					vhsEntries = fetchCatalogEntries(new VhsCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, vhsEntries, JColor.PINK.val, Shape.CIRCLE);
			}
		}
		if (uhsOverlay.isSelected()) {
			if (uhsEntries == null) {
				uhsEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					uhsEntries = fetchCatalogEntries(new UhsCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, uhsEntries, JColor.DARK_YELLOW.val, Shape.CIRCLE);
			}
		}
		if (ukidssOverlay.isSelected()) {
			if (ukidssEntries == null) {
				ukidssEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					ukidssEntries = fetchCatalogEntries(new UkidssCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, ukidssEntries, JColor.BLOOD.val, Shape.CIRCLE);
			}
		}
		if (twoMassOverlay.isSelected()) {
			if (twoMassEntries == null) {
				twoMassEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					twoMassEntries = fetchCatalogEntries(new TwoMassCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, twoMassEntries, JColor.ORANGE.val, Shape.CIRCLE);
			}
		}
		if (tessOverlay.isSelected()) {
			if (tessEntries == null) {
				tessEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					tessEntries = fetchCatalogEntries(new TessCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, tessEntries, JColor.LILAC.val, Shape.CIRCLE);
			}
		}
		if (desOverlay.isSelected()) {
			if (desEntries == null) {
				desEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					desEntries = fetchCatalogEntries(new DesCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, desEntries, JColor.SAND.val, Shape.CIRCLE);
			}
		}
		if (gaiaWDOverlay.isSelected()) {
			if (gaiaWDEntries == null) {
				gaiaWDEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					gaiaWDEntries = fetchCatalogEntries(new GaiaWDCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, gaiaWDEntries, JColor.PURPLE.val, Shape.CIRCLE);
			}
		}
		if (mocaOverlay.isSelected()) {
			if (mocaEntries == null) {
				mocaEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					mocaEntries = fetchCatalogEntries(new MocaCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, mocaEntries, JColor.DARK_ORANGE.val, Shape.DIAMOND);
			}
		}
		if (ssoOverlay.isSelected()) {
			if (ssoEntries == null) {
				ssoEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					ssoEntries = fetchCatalogEntries(new SsoCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawOverlay(image, ssoEntries, Color.BLUE, Shape.CIRCLE);
			}
		}
		if (useCustomOverlays.isSelected()) {
			customOverlays.values().forEach((customOverlay) -> {
				JCheckBox checkBox = customOverlay.getCheckBox();
				if (checkBox != null && checkBox.isSelected()) {
					if (customOverlay.getCatalogEntries() == null) {
						customOverlay.setCatalogEntries(Collections.emptyList());
						CompletableFuture.supplyAsync(() -> {
							fetchGenericCatalogEntries(customOverlay);
							processImages();
							return null;
						});
					} else {
						drawOverlay(image, customOverlay.getCatalogEntries(), customOverlay.getColor(),
								customOverlay.getShape());
					}
				}
			});
		}
		if (gaiaProperMotion.isSelected()) {
			if (gaiaTpmEntries == null) {
				gaiaTpmEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					gaiaTpmEntries = fetchTpmCatalogEntries(new GaiaDR2CatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawPMVectors(image, gaiaTpmEntries, Color.CYAN.darker(), epoch);
			}
		}
		if (gaiaDR3ProperMotion.isSelected()) {
			if (gaiaDR3TpmEntries == null) {
				gaiaDR3TpmEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					gaiaDR3TpmEntries = fetchTpmCatalogEntries(new GaiaDR3CatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawPMVectors(image, gaiaDR3TpmEntries, Color.CYAN.darker(), epoch);
			}
		}
		if (noirlabProperMotion.isSelected()) {
			if (noirlabTpmEntries == null) {
				noirlabTpmEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					noirlabTpmEntries = fetchTpmCatalogEntries(new NoirlabCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawPMVectors(image, noirlabTpmEntries, JColor.NAVY.val, epoch);
			}
		}
		if (catWiseProperMotion.isSelected()) {
			if (catWiseTpmEntries == null) {
				catWiseTpmEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					catWiseTpmEntries = fetchTpmCatalogEntries(new CatWiseCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawPMVectors(image, catWiseTpmEntries, Color.MAGENTA, epoch);
			}
		}
		if (ukidssProperMotion.isSelected()) {
			if (ukidssTpmEntries == null) {
				ukidssTpmEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					ukidssTpmEntries = fetchTpmCatalogEntries(new UkidssCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawPMVectors(image, ukidssTpmEntries, JColor.BLOOD.val, epoch);
			}
		}
		if (ghostOverlay.isSelected() || haloOverlay.isSelected() || latentOverlay.isSelected()
				|| spikeOverlay.isSelected()) {
			if (catWiseEntries == null) {
				catWiseEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					catWiseEntries = fetchCatalogEntries(new CatWiseCatalogEntry());
					processImages();
					return null;
				});
			} else {
				drawArtifactOverlay(image, catWiseEntries);
			}
			if (catWiseRejectEntries == null) {
				catWiseRejectEntries = Collections.emptyList();
				CompletableFuture.supplyAsync(() -> {
					catWiseRejectEntries = fetchCatalogEntries(new CatWiseRejectEntry());
					processImages();
					return null;
				});
			} else {
				drawArtifactOverlay(image, catWiseRejectEntries);
			}
		}
	}

	private void downloadRequestedEpochs(Integer forward, int band, List<Epoch> epochs,
			Map<String, ImageContainer> images) throws Exception {
		if (epochs == null) {
			writeLogEntry("No images found for band " + band + ".");
			return;
		}
		if (desiCutouts.isSelected()) {
			retrieveDesiImages(band, images);
		} else if (ps1Cutouts.isSelected()) {
			retrievePs1Images(band, images);
		} else {
			for (Epoch epoch : epochs) {
				if (stopDownloadProcess) {
					return;
				}
				int requestedEpoch = epoch.getEpoch();
				String imageKey = band + "_" + requestedEpoch;
				ImageContainer container = images.get(imageKey);
				if (container != null) {
					writeLogEntry("band " + band + " | epoch " + requestedEpoch + " | cached");
					continue;
				}
				if (unwiseCutouts.isSelected()) {
					if (requestedEpoch % 2 > 0) {
						container = images.get(band + "_" + (requestedEpoch - 1));
						if (container != null) {
							Fits fits = new Fits();
							fits.addHDU(Fits.makeHDU(container.getImage().getHDU(0).getData().getData()));
							Header header = fits.getHDU(0).getHeader();
							header.addValue("FORWARD", epoch.getForward(), "Scan direction");
							header.addValue("MJDMEAN", epoch.getMjdmean(), "Mean MJD");
							images.put(imageKey, new ImageContainer(requestedEpoch, fits, false));
							writeLogEntry("band " + band + " | epoch " + requestedEpoch + " | "
									+ formatDate(epoch.getMjdmean()) + " | downloaded");
							continue;
						}
					}
				}
				Fits fits;
				ImageHDU hdu;
				try {
					fits = new Fits(getImageData(band, requestedEpoch));
					hdu = (ImageHDU) fits.getHDU(0);
					fits.close();
				} catch (IOException ex) {
					writeLogEntry("band " + band + " | epoch " + requestedEpoch + " | " + ex.getMessage());
					break;
				}
				double mjdmean;
				Header header = hdu.getHeader();
				if (wiseviewCutouts.isSelected()) {
					mjdmean = getMjdmean(header);
				} else {
					mjdmean = epoch.getMjdmean();
				}
				header.addValue("FORWARD", epoch.getForward(), "Scan direction");
				header.addValue("MJDMEAN", mjdmean, "Mean MJD");
				String meanObsDate = formatDate(mjdmean);
				if (skipBadImages.isSelected()) {
					ImageData imageData = hdu.getData();
					float[][] data = (float[][]) imageData.getData();
					double y = data.length;
					double x = y > 0 ? data[0].length : 0;
					int badPixels = 0;
					for (int i = 0; i < y; i++) {
						for (int j = 0; j < x; j++) {
							if (data[i][j] == 0) {
								badPixels++;
							}
						}
					}
					if (badPixels > x * y * 0.5) {
						writeLogEntry("band " + band + " | epoch " + requestedEpoch + " | " + meanObsDate
								+ " | skipped (poor quality image)");
						images.put(imageKey, new ImageContainer(requestedEpoch, fits, true));
						continue;
					}
				}
				images.put(imageKey, new ImageContainer(requestedEpoch, fits, false));
				writeLogEntry("band " + band + " | epoch " + requestedEpoch + " | " + meanObsDate + " | downloaded");
			}
		}
		if (images.isEmpty()) {
			return;
		}
		if (skipIntermediateEpochs.isSelected()) {
			imagesW1Ends.clear();
			imagesW2Ends.clear();
			imagesW1Ends.putAll(imagesW1);
			imagesW2Ends.putAll(imagesW2);
		} else {
			imagesW1All.clear();
			imagesW2All.clear();
			imagesW1All.putAll(imagesW1);
			imagesW2All.putAll(imagesW2);
		}
		List<ImageContainer> containers = images.values().stream().filter(v -> !v.isSkip())
				.sorted(Comparator.comparing(ImageContainer::getEpoch)).collect(toList());
		if (containers.isEmpty()) {
			return;
		}
		extractHeaderInfo(containers.get(0).getImage()); // Must be the first image in the list
		containers.stream().map(ImageContainer::getImage).forEach(i -> addImage(band, i));
		epochCount = containers.size();
	}

	private double getMjdmean(Header header) throws Exception {
		double mjdmin = header.getDoubleValue("MJDMIN");
		double mjdmax = header.getDoubleValue("MJDMAX");
		return (mjdmin + mjdmax) / 2;
	}

	private String formatDate(double mjd) {
		LocalDateTime obsDate = convertMJDToDateTime(new BigDecimal(Double.toString(mjd)));
		return obsDate.format(DATE_FORMATTER);
	}

	private String getMeanObsDate(Fits fits) throws Exception {
		ImageHDU hdu = (ImageHDU) fits.getHDU(0);
		Header header = hdu.getHeader();
		double mjdmean = header.getDoubleValue("MJDMEAN");
		return formatDate(mjdmean);
	}

	private String getDataRelease(Fits fits) throws Exception {
		ImageHDU hdu = (ImageHDU) fits.getHDU(0);
		Header header = hdu.getHeader();
		return header.getStringValue("SURVEY");
	}

	private void extractHeaderInfo(Fits fits) throws Exception {
		if (fits != null) {
			ImageHDU hdu = (ImageHDU) fits.getHDU(0);
			Header header = hdu.getHeader();
			header.addValue("FEPOCH", 1, "First epoch");
			if (header.getDoubleValue("NAXIS1") != header.getDoubleValue("NAXIS2")) {
				imageCutOff = true;
			}
			crval1 = header.getDoubleValue("CRVAL1");
			crval2 = header.getDoubleValue("CRVAL2");
			crpix1 = header.getDoubleValue("CRPIX1");
			crpix2 = header.getDoubleValue("CRPIX2");
			naxis1 = size;
			naxis2 = size;
		}
	}

	private void writeLogEntry(String log) {
		if (asyncDownloads) {
			downloadLog.append(log + LINE_SEP_TEXT_AREA);
		}
	}

	private InputStream getImageData(int band, int epoch) throws Exception {
		if (unwiseCutouts.isSelected()) {
			epoch /= 2;
			String unwiseEpoch;
			if (epoch == 0) {
				unwiseEpoch = "allwise";
			} else {
				unwiseEpoch = "neo" + epoch;
			}
			String unwiseURL = "https://unwise.me/cutout_fits?version=%s&ra=%f&dec=%f&size=%d&bands=%d&file_img_m=on"
					.formatted(unwiseEpoch, targetRa, targetDec, size, band);
			try (InputStream fi = establishHttpConnection(unwiseURL).getInputStream();
					InputStream bi = new BufferedInputStream(fi, BUFFER_SIZE);
					InputStream gzi = new GzipCompressorInputStream(bi);
					ArchiveInputStream ti = new TarArchiveInputStream(gzi)) {
				ArchiveEntry entry;
				Map<Long, byte[]> entries = new HashMap();
				while ((entry = ti.getNextEntry()) != null) {
					byte[] buf = new byte[(int) entry.getSize()];
					IOUtils.readFully(ti, buf);
					entries.put(entry.getSize(), buf);
				}
				List<Long> sizes = entries.keySet().stream().collect(toList());
				sizes.sort(Comparator.reverseOrder());
				long largest = sizes.get(0);
				return new ByteArrayInputStream(entries.get(largest));
			} catch (Exception e) {
				return getImageData(band, epoch - 1);
			}
		} else {
			String imageUrl = getUserSetting(CUTOUT_SERVICE, CUTOUT_SERVICE_URL) + "?ra=" + targetRa + "&dec="
					+ targetDec + "&size=" + size + "&band=" + band + "&epoch=" + epoch;
			HttpURLConnection connection = establishHttpConnection(imageUrl);
			return connection.getInputStream();
		}
	}

	private void retrieveDesiImages(int band, Map<String, ImageContainer> images) throws Exception {
		boolean epochDownloaded = downloadDesiCutouts(0, band, images, "decals-dr5");
		if (!epochDownloaded || !skipIntermediateEpochs.isSelected()) {
			epochDownloaded = downloadDesiCutouts(2, band, images, "decals-dr7");
		}
		if (!epochDownloaded || !skipIntermediateEpochs.isSelected()) {
			epochDownloaded = downloadDesiCutouts(4, band, images, "ls-dr8");
		}
		if (!epochDownloaded || !skipIntermediateEpochs.isSelected()) {
			downloadDesiCutouts(6, band, images, "ls-dr9");
		}
		downloadDesiCutouts(8, band, images, "ls-dr10");
	}

	private boolean downloadDesiCutouts(int requestedEpoch, int band, Map<String, ImageContainer> images, String survey)
			throws Exception {
		if (stopDownloadProcess) {
			return true;
		}
		String imageKey = band + "_" + requestedEpoch;
		ImageContainer container = images.get(imageKey);
		if (container != null) {
			writeLogEntry("band " + band + " | image " + requestedEpoch / 2 + " | cached");
			return true;
		}
		String selectedBand = band == 1 ? "r" : "z";
		String baseUrl = "https://www.legacysurvey.org/viewer/fits-cutout?ra=%f&dec=%f&pixscale=%f&layer=%s&size=%d&bands=%s";
		String imageUrl = baseUrl.formatted(targetRa, targetDec, PIXEL_SCALE_DECAM, survey, size, selectedBand);
		try {
			// Ascending scan
			HttpURLConnection connection = establishHttpConnection(imageUrl);
			Fits fits = new Fits(connection.getInputStream());
			Header header = fits.getHDU(0).getHeader();
			header.addValue("FORWARD", 0, "Scan direction");
			header.addValue("MJDMEAN", 55256.0, "Mean MJD");
			header.addValue("SURVEY", survey, "Data release");
			enhanceImage(fits, 1000);
			fits.close();
			images.put(imageKey, new ImageContainer(requestedEpoch, fits, false));
			writeLogEntry("band " + band + " | image " + requestedEpoch / 2 + " | " + survey + " | downloaded");
			requestedEpoch++;

			// Descending scan
			Fits fits2 = new Fits();
			fits2.addHDU(Fits.makeHDU(fits.getHDU(0).getData().getData()));
			header = fits2.getHDU(0).getHeader();
			header.addValue("FORWARD", 1, "Scan direction");
			header.addValue("MJDMEAN", 55256.0, "Mean MJD");
			header.addValue("SURVEY", survey, "Data release");
			imageKey = band + "_" + requestedEpoch;
			images.put(imageKey, new ImageContainer(requestedEpoch, fits2, false));

			return true;
		} catch (IOException | FitsException ex) {
			return false;
		}
	}

	private void retrievePs1Images(int band, Map<String, ImageContainer> images) throws Exception {
		List<String> fileNames = new ArrayList();
		String selectedBand = band == 1 ? "r" : "y";
		try {
			String downloadUrl = "http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=%s&type=warp&sep=comma"
					.formatted(targetRa, targetDec, selectedBand);
			String response = readResponse(establishHttpConnection(downloadUrl), "Pan-STARRS");
			try (Scanner scanner = new Scanner(response)) {
				String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
				int fileName = 0;
				for (int i = 0; i < columnNames.length; i++) {
					if (columnNames[i].equals("filename")) {
						fileName = i;
					}
				}
				while (scanner.hasNextLine()) {
					String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
					fileNames.add(columnValues[fileName]);
				}
			}
		} catch (IOException ex) {
			writeErrorLog(ex);
		}
		int i = 0;
		for (String fileName : fileNames) {
			downloadPs1Cutouts(i, band, images, fileName);
			i += 2;
		}
	}

	private void downloadPs1Cutouts(int requestedEpoch, int band, Map<String, ImageContainer> images, String fileName)
			throws Exception {
		if (stopDownloadProcess) {
			return;
		}
		String imageKey = band + "_" + requestedEpoch;
		ImageContainer container = images.get(imageKey);
		if (container != null) {
			writeLogEntry("band " + band + " | image " + requestedEpoch / 2 + " | cached");
			return;
		}
		String imageUrl = "http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?ra=%f&dec=%f&size=%d&red=%s&format=fits"
				.formatted(targetRa, targetDec, size, fileName);
		try {
			// Ascending scan
			HttpURLConnection connection = establishHttpConnection(imageUrl);
			Fits fits = new Fits(connection.getInputStream());
			Header header = fits.getHDU(0).getHeader();
			double mjdmean = header.getDoubleValue("MJD-OBS");
			String meanObsDate = formatDate(mjdmean);

			// Skip bad images
			ImageHDU hdu = (ImageHDU) fits.getHDU(0);
			ImageData imageData = hdu.getData();
			float[][] data = (float[][]) imageData.getData();
			double y = data.length;
			double x = y > 0 ? data[0].length : 0;
			int badPixels = 0;
			for (int i = 0; i < y; i++) {
				for (int j = 0; j < x; j++) {
					float value = data[i][j];
					if (isBadPixel(value)) {
						badPixels++;
					}
				}
			}
			double rate;
			if (skipBadImages.isSelected()) {
				rate = 0.1;
			} else {
				rate = 0.5;
			}
			if (badPixels > x * y * rate) {
				String reason = skipBadImages.isSelected() ? "(poor quality image)" : "(mostly blank image)";
				writeLogEntry("band " + band + " | epoch " + requestedEpoch / 2 + " | " + meanObsDate + " | skipped "
						+ reason);
				images.put(imageKey, new ImageContainer(requestedEpoch, fits, true));
				return;
			}
			// End

			header.addValue("FORWARD", 0, "Scan direction");
			header.addValue("MJDMEAN", mjdmean, "Mean MJD");
			fits.close();
			images.put(imageKey, new ImageContainer(requestedEpoch, fits, false));
			writeLogEntry("band " + band + " | image " + requestedEpoch / 2 + " | " + meanObsDate + " | downloaded");
			requestedEpoch++;

			// Descending scan
			Fits fits2 = new Fits();
			fits2.addHDU(Fits.makeHDU(fits.getHDU(0).getData().getData()));
			header = fits2.getHDU(0).getHeader();
			header.addValue("FORWARD", 1, "Scan direction");
			header.addValue("MJDMEAN", mjdmean, "Mean MJD");
			imageKey = band + "_" + requestedEpoch;
			images.put(imageKey, new ImageContainer(requestedEpoch, fits2, false));
		} catch (IOException | FitsException ex) {
		}
	}

	private boolean isBadPixel(float value) {
		return value == 0 || Float.toString(value).equals("NaN");
	}

	private BufferedImage createImage(Fits fits) {
		try {
			ImageHDU hdu = (ImageHDU) fits.getHDU(0);
			ImageData imageData = hdu.getData();
			float[][] values = (float[][]) imageData.getData();

			if (blurImages.isSelected()) {
				values = blur(values);
			}

			BufferedImage image = new BufferedImage(naxis1, naxis2, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = image.createGraphics();
			for (int i = 0; i < naxis2; i++) {
				for (int j = 0; j < naxis1; j++) {
					try {
						float value = processPixel(values[i][j]);
						graphics.setColor(new Color(value, value, value));
						graphics.fillRect(j, i, 1, 1);
					} catch (ArrayIndexOutOfBoundsException ex) {
					}
				}
			}

			return image;
		} catch (IOException | IndexOutOfBoundsException | FitsException ex) {
			throw new RuntimeException(ex);
		}
	}

	private BufferedImage createColorImage(Fits fits1, Fits fits2) {
		try {
			ImageHDU hdu = (ImageHDU) fits1.getHDU(0);
			ImageData imageData = hdu.getData();
			float[][] valuesW1 = (float[][]) imageData.getData();

			hdu = (ImageHDU) fits2.getHDU(0);
			imageData = hdu.getData();
			float[][] valuesW2 = (float[][]) imageData.getData();

			if (blurImages.isSelected()) {
				valuesW1 = blur(valuesW1);
				valuesW2 = blur(valuesW2);
			}

			BufferedImage image = new BufferedImage(naxis1, naxis2, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = image.createGraphics();
			for (int i = 0; i < naxis2; i++) {
				for (int j = 0; j < naxis1; j++) {
					try {
						float red = processPixel(valuesW1[i][j]);
						float blue = processPixel(valuesW2[i][j]);
						float green = (red + blue) / 2;
						Color color;
						if (invertColors.isSelected()) {
							color = new Color(blue, green, red);
						} else {
							color = new Color(red, green, blue);
						}
						graphics.setColor(color);
						graphics.fillRect(j, i, 1, 1);
					} catch (ArrayIndexOutOfBoundsException ex) {
					}
				}
			}

			return image;
		} catch (IOException | IndexOutOfBoundsException | FitsException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Fits addImages(Fits fits1, Fits fits2) throws Exception {
		ImageHDU hdu = (ImageHDU) fits1.getHDU(0);
		Header header = hdu.getHeader();
		String survey = header.getStringValue("SURVEY");
		double mjdmean1 = header.getDoubleValue("MJDMEAN");
		long firstEpoch1 = header.getLongValue("FEPOCH");
		ImageData imageData = hdu.getData();
		float[][] values1 = (float[][]) imageData.getData();

		hdu = (ImageHDU) fits2.getHDU(0);
		header = hdu.getHeader();
		double mjdmean2 = header.getDoubleValue("MJDMEAN");
		long firstEpoch2 = header.getLongValue("FEPOCH");
		imageData = hdu.getData();
		float[][] values2 = (float[][]) imageData.getData();

		float[][] addedValues = new float[naxis2][naxis1];
		for (int i = 0; i < naxis2; i++) {
			for (int j = 0; j < naxis1; j++) {
				try {
					float value1 = values1[i][j];
					float value2 = values2[i][j];
					value1 = isBadPixel(value1) ? value2 : value1;
					value2 = isBadPixel(value2) ? value1 : value2;
					addedValues[i][j] = value1 + value2;
				} catch (ArrayIndexOutOfBoundsException ex) {
				}
			}
		}

		Fits fits = new Fits();
		fits.addHDU(Fits.makeHDU(addedValues));
		hdu = (ImageHDU) fits.getHDU(0);
		header = hdu.getHeader();
		double mjdmean = (mjdmean1 + mjdmean2) / 2;
		header.addValue("MJDMEAN", mjdmean, "Mean MJD");
		header.addValue("SURVEY", survey, "Data release");
		header.addValue("FEPOCH", firstEpoch1 > 0 || firstEpoch2 > 0 ? 1 : 0, "First epoch");
		return fits;
	}

	private Fits subtractImages(Fits fits1, Fits fits2) throws Exception {
		ImageHDU hdu = (ImageHDU) fits1.getHDU(0);
		Header header = hdu.getHeader();
		String survey = header.getStringValue("SURVEY");
		double mjdmean1 = header.getDoubleValue("MJDMEAN");
		long firstEpoch1 = header.getLongValue("FEPOCH");
		ImageData imageData = hdu.getData();
		float[][] values1 = (float[][]) imageData.getData();

		hdu = (ImageHDU) fits2.getHDU(0);
		header = hdu.getHeader();
		double mjdmean2 = header.getDoubleValue("MJDMEAN");
		long firstEpoch2 = header.getLongValue("FEPOCH");
		imageData = hdu.getData();
		float[][] values2 = (float[][]) imageData.getData();

		float[][] subtractedValues = new float[naxis2][naxis1];
		for (int i = 0; i < naxis2; i++) {
			for (int j = 0; j < naxis1; j++) {
				try {
					subtractedValues[i][j] = values1[i][j] - values2[i][j];
				} catch (ArrayIndexOutOfBoundsException ex) {
				}
			}
		}

		Fits fits = new Fits();
		fits.addHDU(Fits.makeHDU(subtractedValues));
		hdu = (ImageHDU) fits.getHDU(0);
		header = hdu.getHeader();
		double mjdmean = (mjdmean1 + mjdmean2) / 2;
		header.addValue("MJDMEAN", mjdmean, "Mean MJD");
		header.addValue("SURVEY", survey, "Data release");
		header.addValue("FEPOCH", firstEpoch1 > 0 || firstEpoch2 > 0 ? 1 : 0, "First epoch");
		return fits;
	}

	private Fits average(Fits fits, int numberOfImages) throws Exception {
		ImageHDU hdu = (ImageHDU) fits.getHDU(0);
		Header header = hdu.getHeader();
		String survey = header.getStringValue("SURVEY");
		double mjdmean = header.getDoubleValue("MJDMEAN");
		long firstEpoch = header.getLongValue("FEPOCH");
		ImageData imageData = hdu.getData();
		float[][] values = (float[][]) imageData.getData();

		float[][] averagedValues = new float[naxis2][naxis1];
		for (int i = 0; i < naxis2; i++) {
			for (int j = 0; j < naxis1; j++) {
				try {
					averagedValues[i][j] = values[i][j] / numberOfImages;
				} catch (ArrayIndexOutOfBoundsException ex) {
				}
			}
		}

		fits = new Fits();
		fits.addHDU(Fits.makeHDU(averagedValues));
		hdu = (ImageHDU) fits.getHDU(0);
		header = hdu.getHeader();
		header.addValue("MJDMEAN", mjdmean, "Mean MJD");
		header.addValue("SURVEY", survey, "Data release");
		header.addValue("FEPOCH", firstEpoch, "First epoch");
		return fits;
	}

	private void enhanceImage(Fits fits, int enhanceFactor) throws Exception {
		ImageHDU imageHDU = (ImageHDU) fits.getHDU(0);
		ImageData imageData = imageHDU.getData();
		float[][] values = (float[][]) imageData.getData();

		for (int i = 0; i < naxis2; i++) {
			for (int j = 0; j < naxis1; j++) {
				try {
					values[i][j] = values[i][j] * enhanceFactor;
				} catch (ArrayIndexOutOfBoundsException ex) {
				}
			}
		}
	}

	private float[][] blur(float[][] values) {
		float[][] blurredValues = new float[naxis2][naxis1];
		for (int i = 0; i < naxis2; ++i) {
			for (int j = 0; j < naxis1; ++j) {
				int sum = 0, c = 0;
				for (int k = max(0, i - 1); k <= min(i + 1, naxis2 - 1); k++) {
					for (int u = max(0, j - 1); u <= min(j + 1, naxis1 - 1); u++) {
						sum += values[k][u];
						c++;
					}
				}
				blurredValues[i][j] = sum / c;
			}
		}
		return blurredValues;
	}

	private void addImage(int band, Fits fits) {
		if (band == 1) {
			band1Images.add(fits);
		}
		if (band == 2) {
			band2Images.add(fits);
		}
	}

	private float processPixel(float value) {
		value = normalize(value, minValue, maxValue);
		return invertColors.isSelected() ? value : 1 - value;
	}

	private float normalize(float value, float minVal, float maxVal) {
		value = max(value, minVal);
		value = min(value, maxVal);
		float lowerBound = 0, upperBound = 1;
		return (value - minVal) * ((upperBound - lowerBound) / (maxVal - minVal)) + lowerBound;
	}

	private NumberPair determineRefValues(float[][] values) {
		List<Double> imageData = new ArrayList<>();
		for (float[] row : values) {
			for (float value : row) {
				if (value != Float.POSITIVE_INFINITY && value != Float.NEGATIVE_INFINITY && value != Float.NaN) {
					imageData.add((double) value);
				}
			}
		}
		imageData.sort(Comparator.naturalOrder());
		double lowerBound;
		double upperBound;
		if (differenceImaging.isSelected()) {
			NumberPair limits = determineLimits(imageData, contrast / 10f, 100 - contrast / 10f);
			lowerBound = limits.getX();
			upperBound = limits.getY();
		} else {
			NumberPair limits = determineLimits(imageData, brightness, 1);
			lowerBound = limits.getX();
			limits = determineLimits(imageData, 1, 1);
			double min = limits.getX();
			double max = limits.getY();
			double dev = max - min;
			double med = determineMedian(imageData);
			upperBound = med + ((100 - contrast) / 10f) * dev;
		}
		return new NumberPair(lowerBound, upperBound);
	}

	public static NumberPair determineLimits(List<Double> values, double lowPercentile, double highPercentile) {
		int size = values.size();
		int half = size / 2;
		int min = (int) (half * lowPercentile / 100);
		int max = (int) (half * (100 - highPercentile) / 100);
		return new NumberPair(values.get(min), values.get((size - 1) - max));
	}

	private boolean openNewCatalogSearch(double targetRa, double targetDec) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		timer.stop();

		Application application = new Application();
		application.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		application.init();

		JTabbedPane pane = application.getTabbedPane();
		int tabIndex = pane.indexOfTab(CatalogQueryTab.TAB_NAME);
		if (tabIndex < 0) {
			showErrorDialog(baseFrame,
					"The Catalog Search tab has been removed. You can add it again from the Settings tab.");
			return false;
		}
		pane.setSelectedIndex(tabIndex);

		Point point = baseFrame.getLocation();
		application.getBaseFrame().setLocation((int) point.getX() + WINDOW_SPACING,
				(int) point.getY() + WINDOW_SPACING);

		CatalogQueryTab catalogQueryTab = application.getCatalogQueryTab();
		catalogQueryTab.getCoordsField().setText(roundTo7DecNZ(targetRa) + " " + roundTo7DecNZ(targetDec));
		catalogQueryTab.getRadiusField().setText("10");
		catalogQueryTab.getSearchButton().getActionListeners()[0].actionPerformed(null);

		baseFrame.setCursor(Cursor.getDefaultCursor());

		return true;
	}

	private boolean openNewImageViewer(double targetRa, double targetDec) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		timer.stop();

		Application application = new Application();
		application.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		application.init();

		JTabbedPane pane = application.getTabbedPane();
		int tabIndex = pane.indexOfTab(ImageViewerTab.TAB_NAME);
		if (tabIndex < 0) {
			showErrorDialog(baseFrame,
					"The Image Viewer tab has been removed. You can add it again from the Settings tab.");
			return false;
		}
		pane.setSelectedIndex(tabIndex);

		Point point = baseFrame.getLocation();
		application.getBaseFrame().setLocation((int) point.getX() + WINDOW_SPACING,
				(int) point.getY() + WINDOW_SPACING);

		ImageViewerTab imageViewerTab = application.getImageViewerTab();
		imageViewerTab.getCoordsField().setText(roundTo7DecNZ(targetRa) + " " + roundTo7DecNZ(targetDec));
		imageViewerTab.getSizeField().setText(differentSizeField.getText());
		if (unwiseCutouts.isSelected()) {
			imageViewerTab.setPixelScale(PIXEL_SCALE_WISE);
			imageViewerTab.getWiseCoadds().setSelected(true);
		}
		if (desiCutouts.isSelected()) {
			imageViewerTab.setPixelScale(PIXEL_SCALE_DECAM);
			imageViewerTab.getDesiCutouts().setSelected(true);
		}
		if (ps1Cutouts.isSelected()) {
			imageViewerTab.setPixelScale(PIXEL_SCALE_PS1);
			imageViewerTab.getPs1Cutouts().setSelected(true);
		}
		imageViewerTab.getWiseBands().setSelectedItem(wiseBand);
		imageViewerTab.setQuadrantCount(quadrantCount);
		imageViewerTab.getZoomSlider().setValue(ZOOM);
		imageViewerTab.setZoom(ZOOM);
		imageViewerTab.setImageViewer(this);

		baseFrame.setCursor(Cursor.getDefaultCursor());

		return true;
	}

	private BufferedImage fetchDesiImage(double targetRa, double targetDec, double size) {
		try {
			int imageSize = (int) round(size * pixelScale * 4);
			if (imageSize > 3000) {
				return null;
			}
			String imageUrl = "https://www.legacysurvey.org/viewer/jpeg-cutout?ra=%f&dec=%f&pixscale=%f&size=%d&bands=%s&layer=%s"
					.formatted(targetRa, targetDec, PIXEL_SCALE_DECAM, imageSize, DESI_FILTERS, DESI_LS_DR_PARAM);
			HttpURLConnection connection = establishHttpConnection(imageUrl);
			BufferedImage image;
			try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE)) {
				image = ImageIO.read(stream);
			}
			return isSameTarget(targetRa, targetDec, size, this.targetRa, this.targetDec, this.size) ? image : null;
		} catch (IOException ex) {
			return null;
		}
	}

	private BufferedImage fetchPs1Image(double targetRa, double targetDec, double size) {
		try {
			List<String> fileNames = new ArrayList<>();
			String imageUrl = "http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=giy&sep=comma"
					.formatted(targetRa, targetDec);
			String response = readResponse(establishHttpConnection(imageUrl), "Pan-STARRS");
			try (Scanner scanner = new Scanner(response)) {
				String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
				int fileName = 0;
				for (int i = 0; i < columnNames.length; i++) {
					if (columnNames[i].equals("filename")) {
						fileName = i;
						break;
					}
				}
				while (scanner.hasNextLine()) {
					String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
					fileNames.add(columnValues[fileName]);
				}
			}
			imageUrl = "http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?red=%s&green=%s&blue=%s&ra=%f&dec=%f&size=%d&output_size=%d&autoscale=99.8"
					.formatted(fileNames.get(2), fileNames.get(1), fileNames.get(0), targetRa, targetDec,
							(int) round(size * pixelScale * 4), 1024);
			HttpURLConnection connection = establishHttpConnection(imageUrl);
			BufferedImage image;
			try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE)) {
				image = ImageIO.read(stream);
			}
			Map<String, Double> years = getPs1Epochs(targetRa, targetDec);
			int year_g = years.get("g").intValue();
			int year_i = years.get("i").intValue();
			int year_y = years.get("y").intValue();
			year_ps1_y_i_g = getMeanEpoch(year_y, year_i, year_g);
			return isSameTarget(targetRa, targetDec, size, this.targetRa, this.targetDec, this.size) ? image : null;
		} catch (IOException ex) {
			return null;
		}
	}

	private BufferedImage fetchVhsImage(double targetRa, double targetDec, double size) {
		try {
			if (targetDec > 5) {
				return null;
			}
			Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size * pixelScale,
					VHS_SURVEY_URL, VHS_LABEL);
			NirImage nirImage = nirImages.get("K-H-J");
			if (nirImage == null) {
				nirImage = nirImages.get("K-J");
			}
			if (nirImage == null) {
				return null;
			}
			year_vhs_k_h_j = nirImage.getYear();
			return isSameTarget(targetRa, targetDec, size, this.targetRa, this.targetDec, this.size)
					? nirImage.getImage()
					: null;
		} catch (Exception ex) {
			return null;
		}
	}

	private BufferedImage fetchUhsImage(double targetRa, double targetDec, double size) {
		try {
			if (targetDec < -5) {
				return null;
			}
			Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size * pixelScale,
					UHS_SURVEY_URL, UHS_LABEL);
			NirImage nirImage = nirImages.get("K-H-J");
			if (nirImage == null) {
				nirImage = nirImages.get("K-J");
			}
			if (nirImage == null) {
				return null;
			}
			year_uhs_k_j = nirImage.getYear();
			return isSameTarget(targetRa, targetDec, size, this.targetRa, this.targetDec, this.size)
					? nirImage.getImage()
					: null;
		} catch (Exception ex) {
			return null;
		}
	}

	private BufferedImage fetchUkidssImage(double targetRa, double targetDec, double size) {
		try {
			if (targetDec < -5) {
				return null;
			}
			Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size * pixelScale,
					UKIDSS_SURVEY_URL, UKIDSS_LABEL);
			NirImage nirImage = nirImages.get("K-H-J");
			if (nirImage == null) {
				nirImage = nirImages.get("K-J");
			}
			if (nirImage == null) {
				return null;
			}
			year_ukidss_k_h_j = nirImage.getYear();
			return isSameTarget(targetRa, targetDec, size, this.targetRa, this.targetDec, this.size)
					? nirImage.getImage()
					: null;
		} catch (Exception ex) {
			return null;
		}
	}

	private BufferedImage fetchSdssImage(double targetRa, double targetDec, double size) {
		try {
			int resolution = 1024;
			String imageUrl = (SDSS_BASE_URL
					+ "/SkyserverWS/ImgCutout/getjpeg?ra=%f&dec=%f&width=%d&height=%d&scale=%f")
					.formatted(targetRa, targetDec, resolution, resolution, size * pixelScale / resolution);
			HttpURLConnection connection = establishHttpConnection(imageUrl);
			BufferedImage image;
			try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE)) {
				image = ImageIO.read(stream);
			}
			// BufferedImage image = retrieveImage(targetRa, targetDec, (int) round(size *
			// pixelScale), "sdss", "file_type=colorimage");
			// int year_u = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=u");
			// int year_g = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=g");
			// int year_z = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=z");
			// year_sdss_z_g_u = getMeanEpoch(year_z, year_g, year_u);
			return isSameTarget(targetRa, targetDec, size, this.targetRa, this.targetDec, this.size) ? image : null;
		} catch (IOException ex) {
			return null;
		}
	}

	private BufferedImage fetchDssImage(double targetRa, double targetDec, double size) {
		try {
			BufferedImage image = retrieveImage(targetRa, targetDec, (int) round(size * pixelScale), "dss",
					"file_type=colorimage");
			// int year_1b = getEpoch(targetRa, targetDec, size, "dss",
			// "dss_bands=poss1_blue");
			// int year_1r = getEpoch(targetRa, targetDec, size, "dss",
			// "dss_bands=poss1_red");
			int year_2ir = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir");
			// year_dss_2ir_1r_1b = getMeanEpoch(year_2ir, year_1r, year_1b);
			year_dss_2ir_1r_1b = year_2ir;
			return isSameTarget(targetRa, targetDec, size, this.targetRa, this.targetDec, this.size) ? image : null;
		} catch (Exception ex) {
			return null;
		}
	}

	private void displayDssImages(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			int year_1b = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss1_blue");
			int year_1r = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss1_red");
			int year_2b = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_blue");
			int year_2r = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_red");
			int year_2ir = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir");
			// int year_2ir_1r_1b = getMeanEpoch(year_2ir, year_1r, year_1b);
			int year_2ir_1r_1b = year_2ir;

			JPanel bandPanel = new JPanel(new GridLayout(1, 0));

			BufferedImage image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_blue&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DSS1 B", year_1b)));
			}
			image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_red&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DSS1 R", year_1r)));
			}
			image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_blue&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DSS2 B", year_2b)));
			}
			image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_red&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DSS2 R", year_2r)));
			}
			image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DSS IR", year_2ir)));
			}
			image = retrieveImage(targetRa, targetDec, size, "dss", "file_type=colorimage");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DSS IR-R-B", year_2ir_1r_1b)));
			}

			int componentCount = bandPanel.getComponentCount();
			if (componentCount == 0) {
				return;
			}

			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle("DSS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: "
					+ size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (HeadlessException | SecurityException ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void display2MassImages(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			int year_j = getEpoch(targetRa, targetDec, size, "2mass", "twomass_bands=j");
			int year_h = getEpoch(targetRa, targetDec, size, "2mass", "twomass_bands=h");
			int year_k = getEpoch(targetRa, targetDec, size, "2mass", "twomass_bands=k");
			int year_k_h_j = getMeanEpoch(year_k, year_h, year_j);

			JPanel bandPanel = new JPanel(new GridLayout(1, 0));

			BufferedImage image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=j&type=jpgurl");
			if (image != null) {

				bandPanel.add(buildImagePanel(image, getImageLabel("2MASS J", year_j)));
			}
			image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=h&type=jpgurl");
			if (image != null) {

				bandPanel.add(buildImagePanel(image, getImageLabel("2MASS H", year_h)));
			}
			image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
			if (image != null) {

				bandPanel.add(buildImagePanel(image, getImageLabel("2MASS K", year_k)));
			}
			image = retrieveImage(targetRa, targetDec, size, "2mass", "file_type=colorimage");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("2MASS K-H-J", year_k_h_j)));
			}

			int componentCount = bandPanel.getComponentCount();
			if (componentCount == 0) {
				return;
			}

			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle("2MASS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: "
					+ size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (HeadlessException | SecurityException ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displaySdssImages(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			int year_u = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=u");
			int year_g = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=g");
			int year_r = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=r");
			int year_i = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=i");
			int year_z = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=z");
			int year_z_g_u = getMeanEpoch(year_z, year_g, year_u);

			JPanel bandPanel = new JPanel(new GridLayout(1, 0));

			BufferedImage image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=u&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("SDSS u", year_u)));
			}
			image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=g&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("SDSS g", year_g)));
			}
			image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=r&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("SDSS r", year_r)));
			}
			image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=i&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("SDSS i", year_i)));
			}
			image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("SDSS z", year_z)));
			}
			image = retrieveImage(targetRa, targetDec, size, "sdss", "file_type=colorimage");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("SDSS z-g-u", year_z_g_u)));
			}

			int componentCount = bandPanel.getComponentCount();
			if (componentCount == 0) {
				return;
			}

			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle("SDSS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: "
					+ size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (HeadlessException | SecurityException ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displaySpitzerImages(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			int year_ch1 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC1");
			int year_ch2 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC2");
			int year_ch3 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC3");
			int year_ch4 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4");
			int year_mips24 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:MIPS24");
			int year_ch3_ch2_ch1 = getMeanEpoch(year_ch3, year_ch2, year_ch1);

			JPanel bandPanel = new JPanel(new GridLayout(1, 0));

			BufferedImage image = retrieveImage(targetRa, targetDec, size, "seip",
					"seip_bands=spitzer.seip_science:IRAC1&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("IRAC1", year_ch1)));
			}
			image = retrieveImage(targetRa, targetDec, size, "seip",
					"seip_bands=spitzer.seip_science:IRAC2&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("IRAC2", year_ch2)));
			}
			image = retrieveImage(targetRa, targetDec, size, "seip",
					"seip_bands=spitzer.seip_science:IRAC3&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("IRAC3", year_ch3)));
			}
			image = retrieveImage(targetRa, targetDec, size, "seip",
					"seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("IRAC4", year_ch4)));
			}
			image = retrieveImage(targetRa, targetDec, size, "seip",
					"seip_bands=spitzer.seip_science:MIPS24&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("MIPS24", year_mips24)));
			}
			image = retrieveImage(targetRa, targetDec, size, "seip", "file_type=colorimage");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("IRAC3-2-1", year_ch3_ch2_ch1)));
			}

			int componentCount = bandPanel.getComponentCount();
			if (componentCount == 0) {
				return;
			}

			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle("Spitzer (SEIP) - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec)
					+ " FoV: " + size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (HeadlessException | SecurityException ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displayAllwiseImages(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			int year_w1 = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=1");
			int year_w2 = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=2");
			int year_w3 = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=3");
			int year_w4 = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=4");
			int year_w4_w2_w1 = getMeanEpoch(year_w4, year_w2, year_w1);

			JPanel bandPanel = new JPanel(new GridLayout(1, 0));

			BufferedImage image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=1&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("WISE W1", year_w1)));
			}
			image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("WISE W2", year_w2)));
			}
			image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=3&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("WISE W3", year_w3)));
			}
			image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=4&type=jpgurl");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("WISE W4", year_w4)));
			}
			image = retrieveImage(targetRa, targetDec, size, "wise", "file_type=colorimage");
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("WISE W4-W2-W1", year_w4_w2_w1)));
			}

			int componentCount = bandPanel.getComponentCount();
			if (componentCount == 0) {
				return;
			}

			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle("AllWISE - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec)
					+ " FoV: " + size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (HeadlessException | SecurityException ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displayUkidssImages(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			if (targetDec < -5) {
				return;
			}
			Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size, UKIDSS_SURVEY_URL,
					UKIDSS_LABEL);
			if (nirImages.isEmpty()) {
				return;
			}
			JPanel bandPanel = new JPanel(new GridLayout(1, 0));
			nirImages.entrySet().forEach(entry -> {
				String band = entry.getKey();
				NirImage nirImage = entry.getValue();
				BufferedImage image = nirImage.getImage();
				int year = nirImage.getYear();
				bandPanel.add(buildImagePanel(image, getImageLabel(UKIDSS_LABEL + " " + band, year)));
			});
			int componentCount = bandPanel.getComponentCount();
			if (componentCount == 0) {
				return;
			}
			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle(UKIDSS_LABEL + " - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec)
					+ " FoV: " + size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displayUhsImages(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			if (targetDec < -5) {
				return;
			}
			Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size, UHS_SURVEY_URL,
					UHS_LABEL);
			if (nirImages.isEmpty()) {
				return;
			}
			JPanel bandPanel = new JPanel(new GridLayout(1, 0));
			nirImages.entrySet().forEach(entry -> {
				String band = entry.getKey();
				NirImage nirImage = entry.getValue();
				BufferedImage image = nirImage.getImage();
				int year = nirImage.getYear();
				bandPanel.add(buildImagePanel(image, getImageLabel(UHS_LABEL + " " + band, year)));
			});
			int componentCount = bandPanel.getComponentCount();
			if (componentCount == 0) {
				return;
			}
			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle(UHS_LABEL + " - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec)
					+ " FoV: " + size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displayVhsImages(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			if (targetDec > 5) {
				return;
			}
			Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size, VHS_SURVEY_URL,
					VHS_LABEL);
			if (nirImages.isEmpty()) {
				return;
			}
			JPanel bandPanel = new JPanel(new GridLayout(1, 0));
			nirImages.entrySet().forEach(entry -> {
				String band = entry.getKey();
				NirImage nirImage = entry.getValue();
				BufferedImage image = nirImage.getImage();
				int year = nirImage.getYear();
				bandPanel.add(buildImagePanel(image, getImageLabel(VHS_LABEL + " " + band, year)));
			});
			int componentCount = bandPanel.getComponentCount();
			if (componentCount == 0) {
				return;
			}
			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle(VHS_LABEL + " - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec)
					+ " FoV: " + size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displayPs1Images(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {

			Map<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
			if (imageInfos.isEmpty()) {
				return;
			}

			Map<String, Double> years = getPs1Epochs(targetRa, targetDec);
			int year_g = years.get("g").intValue();
			int year_r = years.get("r").intValue();
			int year_i = years.get("i").intValue();
			int year_z = years.get("z").intValue();
			int year_y = years.get("y").intValue();
			int year_y_i_g = getMeanEpoch(year_y, year_i, year_g);

			JPanel bandPanel = new JPanel(new GridLayout(1, 0));

			bandPanel.add(buildImagePanel(
					retrievePs1Image("red=%s".formatted(imageInfos.get("g")), targetRa, targetDec, size, true),
					getImageLabel("PS1 g", year_g)));
			bandPanel.add(buildImagePanel(
					retrievePs1Image("red=%s".formatted(imageInfos.get("r")), targetRa, targetDec, size, true),
					getImageLabel("PS1 r", year_r)));
			bandPanel.add(buildImagePanel(
					retrievePs1Image("red=%s".formatted(imageInfos.get("i")), targetRa, targetDec, size, true),
					getImageLabel("PS1 i", year_i)));
			bandPanel.add(buildImagePanel(
					retrievePs1Image("red=%s".formatted(imageInfos.get("z")), targetRa, targetDec, size, true),
					getImageLabel("PS1 z", year_z)));
			bandPanel.add(buildImagePanel(
					retrievePs1Image("red=%s".formatted(imageInfos.get("y")), targetRa, targetDec, size, true),
					getImageLabel("PS1 y", year_y)));
			bandPanel.add(buildImagePanel(
					retrievePs1Image("red=%s&green=%s&blue=%s".formatted(imageInfos.get("y"), imageInfos.get("i"),
							imageInfos.get("g")), targetRa, targetDec, size, false),
					getImageLabel("PS1 y-i-g", year_y_i_g)));

			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle("Pan-STARRS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec)
					+ " FoV: " + size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(6 * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (HeadlessException | SecurityException ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displayDesiImages(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			JPanel bandPanel = new JPanel(new GridLayout(1, 0));

			BufferedImage image = retrieveDesiImage(targetRa, targetDec, size, "g", true);
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DECaLS g", DESI_LS_DR_LABEL)));
			}
			image = retrieveDesiImage(targetRa, targetDec, size, "r", true);
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DECaLS r", DESI_LS_DR_LABEL)));
			}
			image = retrieveDesiImage(targetRa, targetDec, size, "z", true);
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DECaLS z", DESI_LS_DR_LABEL)));
			}
			image = retrieveDesiImage(targetRa, targetDec, size, DESI_FILTERS, false);
			if (image != null) {
				bandPanel.add(buildImagePanel(image, getImageLabel("DECaLS", DESI_LS_DR_LABEL)));
			}

			int componentCount = bandPanel.getComponentCount();
			if (componentCount == 0) {
				return;
			}

			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle("DECaLS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec)
					+ " FoV: " + size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (HeadlessException | SecurityException ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displayStaticTimeSeries(double targetRa, double targetDec, int size, Counter counter) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			JPanel bandPanel = new JPanel(new GridLayout(1, 0));

			List<Couple<String, NirImage>> timeSeries = new ArrayList<>();

			BufferedImage image = retrieveImage(targetRa, targetDec, size, "dss",
					"dss_bands=poss2ukstu_ir&type=jpgurl");
			if (image != null) {
				int year = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir");
				timeSeries.add(new Couple(getImageLabel("DSS IR", year), new NirImage(year, image)));
			}

			image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
			if (image != null) {
				int year = getEpoch(targetRa, targetDec, size, "2mass", "twomass_bands=k");
				timeSeries.add(new Couple(getImageLabel("2MASS K", year), new NirImage(year, image)));
			}

			image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
			if (image != null) {
				int year = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=z");
				timeSeries.add(new Couple(getImageLabel("SDSS z", year), new NirImage(year, image)));
			}

			image = retrieveImage(targetRa, targetDec, size, "seip",
					"seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
			if (image != null) {
				int year = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4");
				timeSeries.add(new Couple(getImageLabel("IRAC4", year), new NirImage(SPITZER_EPOCH, image)));
			}

			image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
			if (image != null) {
				int year = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=2");
				timeSeries.add(new Couple(getImageLabel("WISE W2", year), new NirImage(ALLWISE_EPOCH, image)));
			}

			if (targetDec > -5) {
				Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size,
						UKIDSS_SURVEY_URL, UKIDSS_LABEL);
				String band = "K";
				NirImage nirImage = nirImages.get(band);
				if (nirImage != null) {
					image = nirImage.getImage();
					if (image != null) {
						int year = nirImage.getYear();
						timeSeries.add(
								new Couple(getImageLabel(UKIDSS_LABEL + " " + band, year), new NirImage(year, image)));
					}
				}
			}

			if (targetDec > -5) {
				Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size, UHS_SURVEY_URL,
						UHS_LABEL);
				String band = "K";
				NirImage nirImage = nirImages.get(band);
				if (nirImage != null) {
					image = nirImage.getImage();
					if (image != null) {
						int year = nirImage.getYear();
						timeSeries.add(
								new Couple(getImageLabel(UHS_LABEL + " " + band, year), new NirImage(year, image)));
					}
				}
			}

			if (targetDec < 5) {
				Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size, VHS_SURVEY_URL,
						VHS_LABEL);
				String band = "K";
				NirImage nirImage = nirImages.get(band);
				if (nirImage != null) {
					image = nirImage.getImage();
					if (image != null) {
						int year = nirImage.getYear();
						timeSeries.add(
								new Couple(getImageLabel(VHS_LABEL + " " + band, year), new NirImage(year, image)));
					}
				}
			}

			Map<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
			if (!imageInfos.isEmpty()) {
				int year = getPs1Epoch(targetRa, targetDec, "z");
				image = retrievePs1Image("red=%s".formatted(imageInfos.get("z")), targetRa, targetDec, size, true);
				timeSeries.add(new Couple(getImageLabel("PS1 z", year), new NirImage(year, image)));
			}

			image = retrieveDesiImage(targetRa, targetDec, size, "z", true);
			if (image != null) {
				timeSeries.add(
						new Couple(getImageLabel("DECaLS z", DESI_LS_DR_LABEL), new NirImage(DESI_LS_EPOCH, image)));
			}

			int componentCount = timeSeries.size();
			if (componentCount == 0) {
				return;
			}

			timeSeries.sort(Comparator.comparing(c -> c.getB().getYear()));
			timeSeries.forEach(couple -> {
				bandPanel.add(buildImagePanel(couple.getB().getImage(), couple.getA()));
			});

			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle("Time series - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec)
					+ " FoV: " + size + "\"");
			imageFrame.add(bandPanel);
			imageFrame.setSize(componentCount * PANEL_WIDTH, PANEL_HEIGHT);
			imageFrame.setLocation(0, counter.value());
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);
			imageFrame.setVisible(true);
			counter.add();
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void displayAnimatedTimeSeries(double targetRa, double targetDec, int size) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			List<Couple<String, NirImage>> timeSeries = new ArrayList<>();

			BufferedImage image;
			if (dssImageSeries.isSelected()) {
				image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
				if (image != null) {
					int year = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir");
					timeSeries.add(new Couple(getImageLabel("DSS IR", year), new NirImage(year, image)));

				}
			}

			if (twoMassImageSeries.isSelected()) {
				image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
				if (image != null) {
					int year = getEpoch(targetRa, targetDec, size, "2mass", "twomass_bands=k");
					timeSeries.add(new Couple(getImageLabel("2MASS K", year), new NirImage(year, image)));

				}
			}

			if (sdssImageSeries.isSelected()) {
				image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
				if (image != null) {
					int year = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=z");
					timeSeries.add(new Couple(getImageLabel("SDSS z", year), new NirImage(year, image)));

				}
			}

			if (spitzerImageSeries.isSelected()) {
				image = retrieveImage(targetRa, targetDec, size, "seip",
						"seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
				if (image != null) {
					int year = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4");
					timeSeries.add(new Couple(getImageLabel("IRAC4", year), new NirImage(SPITZER_EPOCH, image)));
				}
			}

			if (allwiseImageSeries.isSelected()) {
				image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
				if (image != null) {
					int year = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=2");
					timeSeries.add(new Couple(getImageLabel("WISE W2", year), new NirImage(ALLWISE_EPOCH, image)));
				}
			}

			if (ukidssImageSeries.isSelected() && targetDec > -5) {
				Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size,
						UKIDSS_SURVEY_URL, UKIDSS_LABEL);
				String band = "K";
				NirImage nirImage = nirImages.get(band);
				if (nirImage != null) {
					image = nirImage.getImage();
					if (image != null) {
						int year = nirImage.getYear();
						timeSeries.add(
								new Couple(getImageLabel(UKIDSS_LABEL + " " + band, year), new NirImage(year, image)));
					}
				}
			}

			if (uhsImageSeries.isSelected() && targetDec > -5) {
				Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size, UHS_SURVEY_URL,
						UHS_LABEL);
				String band = "K";
				NirImage nirImage = nirImages.get(band);
				if (nirImage != null) {
					image = nirImage.getImage();
					if (image != null) {
						int year = nirImage.getYear();
						timeSeries.add(
								new Couple(getImageLabel(UHS_LABEL + " " + band, year), new NirImage(year, image)));
					}
				}
			}

			if (vhsImageSeries.isSelected() && targetDec < 5) {
				Map<String, NirImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size, VHS_SURVEY_URL,
						VHS_LABEL);
				String band = "K";
				NirImage nirImage = nirImages.get(band);
				if (nirImage != null) {
					image = nirImage.getImage();
					if (image != null) {
						int year = nirImage.getYear();
						timeSeries.add(
								new Couple(getImageLabel(VHS_LABEL + " " + band, year), new NirImage(year, image)));
					}
				}
			}

			if (panstarrsImageSeries.isSelected()) {
				Map<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
				if (!imageInfos.isEmpty()) {
					int year = getPs1Epoch(targetRa, targetDec, "z");
					image = retrievePs1Image("red=%s".formatted(imageInfos.get("z")), targetRa, targetDec, size, true);
					timeSeries.add(new Couple(getImageLabel("PS1 z", year), new NirImage(year, image)));
				}
			}

			if (legacyImageSeries.isSelected()) {
				image = retrieveDesiImage(targetRa, targetDec, size, "z", true);
				if (image != null) {
					timeSeries.add(new Couple(getImageLabel("DECaLS z", DESI_LS_DR_LABEL),
							new NirImage(DESI_LS_EPOCH, image)));
				}
			}

			int componentCount = timeSeries.size();
			if (componentCount == 0) {
				return;
			}

			timeSeries.sort(Comparator.comparing(c -> c.getB().getYear()));

			JPanel container = new JPanel();

			JPanel displayPanel = new JPanel();
			container.add(displayPanel);

			JButton saveAsGifButton = new JButton("Save as GIF");
			container.add(saveAsGifButton);
			saveAsGifButton.addActionListener((ActionEvent evt) -> {
				try {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(new FileTypeFilter(".gif", ".gif files"));
					int returnVal = fileChooser.showSaveDialog(container);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						file = new File(file.getPath() + ".gif");
						BufferedImage[] imageSet = new BufferedImage[timeSeries.size()];
						int i = 0;
						for (Couple<String, NirImage> nirImage : timeSeries) {
							BufferedImage imageBuffer = nirImage.getB().getImage();
							imageSet[i++] = drawCenterShape(imageBuffer);
						}
						if (imageSet.length > 0) {
							GifSequencer sequencer = new GifSequencer();
							sequencer.generateFromBI(imageSet, file, speed / 10, true);
						}
					}
				} catch (HeadlessException | IOException ex) {
					showExceptionDialog(baseFrame, ex);
				}
			});

			JFrame imageFrame = new JFrame();
			imageFrame.setIconImage(getToolBoxImage());
			imageFrame.setTitle("Time series - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec)
					+ " FoV: " + size + "\"");
			imageFrame.add(container);
			imageFrame.setSize(PANEL_WIDTH + 20, PANEL_HEIGHT + 50);
			imageFrame.setAlwaysOnTop(false);
			imageFrame.setResizable(false);

			Timer seriesTimer = new Timer(speed, (ActionEvent e) -> {
				if (imageCount > componentCount - 1) {
					imageCount = 0;
				}
				displayPanel.removeAll();
				Couple<String, NirImage> nirImage = timeSeries.get(imageCount);
				displayPanel.add(buildImagePanel(nirImage.getB().getImage(), nirImage.getA()));
				imageFrame.setVisible(true);
				imageCount++;
			});

			imageFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent evt) {
					seriesTimer.stop();
					imageCount = 0;
				}
			});

			seriesTimer.start();
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private JPanel buildImagePanel(BufferedImage image, String imageLabel) {
		JLabel label = addTextToImage(image, imageLabel);
		JPanel panel = new JPanel();
		panel.add(label);
		return panel;
	}

	private List<CatalogEntry> fetchCatalogEntries(CatalogEntry catalogQuery) {
		try {
			baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			catalogQuery.setRa(targetRa);
			catalogQuery.setDec(targetDec);
			catalogQuery.setSearchRadius(getFovDiagonal() / 2);
			List<CatalogEntry> resultEntries = new ArrayList<>();
			List<CatalogEntry> catalogEntries = catalogQueryService.getCatalogEntriesByCoords(catalogQuery);
			catalogEntries.forEach(catalogEntry -> {
				catalogEntry.setTargetRa(targetRa);
				catalogEntry.setTargetDec(targetDec);
				catalogEntry.loadCatalogElements();
				resultEntries.add(catalogEntry);
			});
			return resultEntries;
		} catch (IOException ex) {
			showExceptionDialog(baseFrame, ex);
			throw new RuntimeException(ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private List<CatalogEntry> fetchTpmCatalogEntries(ProperMotionQuery catalogQuery) {
		try {
			baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			properMotionField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			catalogQuery.setRa(targetRa);
			catalogQuery.setDec(targetDec);
			catalogQuery.setSearchRadius(getFovDiagonal() / 2);
			catalogQuery.setTpm(toDouble(properMotionField.getText()));
			List<CatalogEntry> resultEntries = new ArrayList<>();
			List<CatalogEntry> catalogEntries = catalogQueryService.getCatalogEntriesByCoordsAndTpm(catalogQuery);
			catalogEntries.forEach(catalogEntry -> {
				catalogEntry.setTargetRa(targetRa);
				catalogEntry.setTargetDec(targetDec);
				catalogEntry.loadCatalogElements();
				resultEntries.add(catalogEntry);
			});
			return resultEntries;
		} catch (IOException ex) {
			showExceptionDialog(baseFrame, ex);
			throw new RuntimeException(ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
			properMotionField.setCursor(Cursor.getDefaultCursor());
		}
	}

	private Object fetchGenericCatalogEntries(CustomOverlay customOverlay) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		List<CatalogEntry> catalogEntries = new ArrayList<>();
		String results = null;
		String queryUrl = null;
		boolean isCatalogSearch = false;
		if (!customOverlay.getTableName().isEmpty()) {
			isCatalogSearch = true;
			queryUrl = createVizieRUrl(targetRa, targetDec, getFovDiagonal() / 2 / DEG_ARCSEC,
					customOverlay.getTableName(), customOverlay.getRaColName(), customOverlay.getDecColName());
		}
		if (!customOverlay.getTapUrl().isEmpty()) {
			isCatalogSearch = true;
			String adqlQuery = customOverlay.getAdqlQuery().replace(":ra:", roundTo7DecNZ(targetRa))
					.replace(":dec:", roundTo7DecNZ(targetDec))
					.replace(":radius:", roundTo7DecNZ(getFovDiagonal() / 2 / DEG_ARCSEC));
			queryUrl = customOverlay.getTapUrl() + TAP_URL_PARAMS + encodeQuery(adqlQuery);
		}
		if (isCatalogSearch) {
			try {
				results = readResponse(establishHttpConnection(queryUrl), customOverlay.getName());
				if (results.isEmpty()) {
					baseFrame.setCursor(Cursor.getDefaultCursor());
					return null;
				}
			} catch (IOException ex) {
				showExceptionDialog(baseFrame, ex);
				baseFrame.setCursor(Cursor.getDefaultCursor());
				return null;
			}
		}
		try (Scanner scanner = (results == null) ? new Scanner(customOverlay.getFile()) : new Scanner(results)) {
			String[] columnNames = CSVParser.parseLine(scanner.nextLine());
			StringBuilder errors = new StringBuilder();
			int numberOfColumns = columnNames.length;
			int lastColumnIndex = numberOfColumns - 1;
			int raColumnIndex = customOverlay.getRaColumnIndex();
			if (raColumnIndex == 0 && !customOverlay.getRaColName().isEmpty()) {
				raColumnIndex = Arrays.asList(columnNames).indexOf(customOverlay.getRaColName());
			}
			int decColumnIndex = customOverlay.getDecColumnIndex();
			if (decColumnIndex == 0 && !customOverlay.getDecColName().isEmpty()) {
				decColumnIndex = Arrays.asList(columnNames).indexOf(customOverlay.getDecColName());
			}
			if (raColumnIndex > lastColumnIndex) {
				errors.append("RA position must not be greater than ").append(lastColumnIndex).append(".")
						.append(LINE_SEP);
			}
			if (decColumnIndex > lastColumnIndex) {
				errors.append("Dec position must not be greater than ").append(lastColumnIndex).append(".")
						.append(LINE_SEP);
			}
			if (errors.length() > 0) {
				showErrorDialog(baseFrame, errors.toString());
				return null;
			}
			while (scanner.hasNextLine()) {
				String[] columnValues = CSVParser.parseLine(scanner.nextLine());
				GenericCatalogEntry catalogEntry = new GenericCatalogEntry(columnNames, columnValues);
				catalogEntry.setRa(toDouble(columnValues[raColumnIndex]));
				catalogEntry.setDec(toDouble(columnValues[decColumnIndex]));
				/*
				 * NumberPair coords; double radius = size * pixelScale / 2 / DEG_ARCSEC;
				 * 
				 * coords = calculatePositionFromProperMotion(new NumberPair(targetRa,
				 * targetDec), new NumberPair(-radius, 0)); double rightBoundary =
				 * coords.getX();
				 * 
				 * coords = calculatePositionFromProperMotion(new NumberPair(targetRa,
				 * targetDec), new NumberPair(radius, 0)); double leftBoundary = coords.getX();
				 * 
				 * double bottomBoundary = targetDec - radius; double topBoundary = targetDec +
				 * radius;
				 * 
				 * double catalogRa = catalogEntry.getRa(); double catalogDec =
				 * catalogEntry.getDec();
				 * 
				 * if (isCatalogSearch || (catalogRa > rightBoundary && catalogRa < leftBoundary
				 * && catalogDec > bottomBoundary && catalogDec < topBoundary)) {
				 * catalogEntry.setTargetRa(targetRa); catalogEntry.setTargetDec(targetDec);
				 * catalogEntry.setCatalogName(customOverlay.getName());
				 * catalogEntry.loadCatalogElements(); catalogEntries.add(catalogEntry); }
				 */
				double catalogRa = catalogEntry.getRa();
				double catalogDec = catalogEntry.getDec();
				double radius = getFovDiagonal() / 2;

				double distance = calculateAngularDistance(new NumberPair(targetRa, targetDec),
						new NumberPair(catalogRa, catalogDec), DEG_ARCSEC);

				if (distance <= radius) {
					catalogEntry.setTargetRa(targetRa);
					catalogEntry.setTargetDec(targetDec);
					catalogEntry.setCatalogName(customOverlay.getName());
					catalogEntry.loadCatalogElements();
					catalogEntries.add(catalogEntry);
				}
			}
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			customOverlay.setCatalogEntries(catalogEntries);
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
		return null;
	}

	private void drawSpectrumOverlay(BufferedImage image, List<CatalogEntry> catalogEntries) {
		Graphics graphics = image.getGraphics();
		catalogEntries.forEach(catalogEntry -> {
			NumberPair position = toPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
			catalogEntry.setPixelRa(position.getX());
			catalogEntry.setPixelDec(position.getY());
			SdssCatalogEntry sdssCatalogEntry = (SdssCatalogEntry) catalogEntry;
			if (!sdssCatalogEntry.getSpecObjID().equals(new BigInteger("0"))) {
				Drawable toDraw = new Circle(position.getX(), position.getY(), getOverlaySize(), JColor.OLIVE.val);
				toDraw.draw(graphics);
			}
		});
	}

	private void showSpectrumInfo(List<CatalogEntry> catalogEntries, int x, int y) {
		catalogEntries.forEach(catalogEntry -> {
			double radius = getOverlaySize() / 2;
			SdssCatalogEntry sdssCatalogEntry = (SdssCatalogEntry) catalogEntry;
			if (!sdssCatalogEntry.getSpecObjID().equals(new BigInteger("0")) && catalogEntry.getPixelRa() > x - radius
					&& catalogEntry.getPixelRa() < x + radius && catalogEntry.getPixelDec() > y - radius
					&& catalogEntry.getPixelDec() < y + radius) {
				displaySdssSpectrum(catalogEntry);
			}
		});
	}

	private void displaySdssSpectrum(CatalogEntry catalogEntry) {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			SdssCatalogEntry SDSSCatalogEntry = (SdssCatalogEntry) catalogEntry;
			String spectrumUrl = SDSS_BASE_URL + "/en/get/specById.ashx?ID=" + SDSSCatalogEntry.getSpecObjID();
			HttpURLConnection connection = establishHttpConnection(spectrumUrl);
			BufferedImage spectrum;
			try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE)) {
				spectrum = ImageIO.read(stream);
			}
			if (spectrum != null) {
				JFrame spectrumFrame = new JFrame();
				spectrumFrame.setIconImage(getToolBoxImage());
				spectrumFrame.setTitle("SDSS spectrum for object: " + roundTo2DecNZ(catalogEntry.getRa()) + " "
						+ roundTo2DecNZ(catalogEntry.getDec()));
				spectrumFrame.add(new JLabel(new ImageIcon(spectrum)));
				spectrumFrame.setSize(1200, 900);
				spectrumFrame.setAlwaysOnTop(false);
				spectrumFrame.setResizable(false);
				spectrumFrame.setVisible(true);
			}
		} catch (HeadlessException | IOException | SecurityException ex) {
			showExceptionDialog(baseFrame, ex);
		} finally {
			baseFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void drawOverlay(BufferedImage image, List<CatalogEntry> catalogEntries, Color color, Shape shape) {
		Graphics graphics = image.getGraphics();
		catalogEntries.forEach(catalogEntry -> {
			NumberPair position = toPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
			catalogEntry.setPixelRa(position.getX());
			catalogEntry.setPixelDec(position.getY());
			Drawable toDraw;
			toDraw = switch (shape) {
			case CIRCLE -> new Circle(position.getX(), position.getY(), getOverlaySize(), color);
			case CROSS -> new Cross(position.getX(), position.getY(), getOverlaySize(), color);
			case XCROSS -> new XCross(position.getX(), position.getY(), getOverlaySize(), color);
			case SQUARE -> new Square(position.getX(), position.getY(), getOverlaySize(), color);
			case TRIANGLE -> new Triangle(position.getX(), position.getY(), getOverlaySize(), color);
			case DIAMOND -> new Diamond(position.getX(), position.getY(), getOverlaySize(), color);
			default -> new Circle(position.getX(), position.getY(), getOverlaySize(), color);
			};
			toDraw.draw(graphics);
		});
	}

	private void drawArtifactOverlay(BufferedImage image, List<CatalogEntry> catalogEntries) {
		Graphics graphics = image.getGraphics();
		catalogEntries.forEach(catalogEntry -> {
			NumberPair position = toPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
			catalogEntry.setPixelRa(position.getX());
			catalogEntry.setPixelDec(position.getY());
			Artifact artifact = (Artifact) catalogEntry;
			String ab_flags = artifact.getAb_flags();
			String cc_flags = artifact.getCc_flags();
			if (cc_flags.isEmpty()) {
				cc_flags = "0000";
			}
			switch (wiseBand) {
			case W1 -> {
				ab_flags = ab_flags.substring(0, 1);
				cc_flags = cc_flags.substring(0, 1);
			}
			case W2 -> {
				ab_flags = ab_flags.substring(1, 2);
				cc_flags = cc_flags.substring(1, 2);
			}
			default -> {
				ab_flags = ab_flags.substring(0, 2);
				cc_flags = cc_flags.substring(0, 2);
			}
			}
			String flags = ab_flags + cc_flags;
			if (ghostOverlay.isSelected()) {
				if (flags.contains("o")) {
					Drawable toDraw = new Diamond(position.getX(), position.getY(), getOverlaySize() / 2,
							Color.MAGENTA.darker());
					toDraw.draw(graphics);
				}
				if (flags.contains("O")) {
					Drawable toDraw = new Diamond(position.getX(), position.getY(), getOverlaySize(),
							Color.MAGENTA.darker());
					toDraw.draw(graphics);
				}
			}
			if (haloOverlay.isSelected()) {
				if (flags.contains("h")) {
					Drawable toDraw = new Square(position.getX(), position.getY(), getOverlaySize() / 2, Color.YELLOW);
					toDraw.draw(graphics);
				}
				if (flags.contains("H")) {
					Drawable toDraw = new Square(position.getX(), position.getY(), getOverlaySize(), Color.YELLOW);
					toDraw.draw(graphics);
				}
			}
			if (latentOverlay.isSelected()) {
				if (flags.contains("p")) {
					Drawable toDraw = new XCross(position.getX(), position.getY(), getOverlaySize() / 2,
							Color.GREEN.darker());
					toDraw.draw(graphics);
				}
				if (flags.contains("P")) {
					Drawable toDraw = new XCross(position.getX(), position.getY(), getOverlaySize(),
							Color.GREEN.darker());
					toDraw.draw(graphics);
				}
			}
			if (spikeOverlay.isSelected()) {
				if (flags.contains("d")) {
					Drawable toDraw = new Circle(position.getX(), position.getY(), getOverlaySize() / 2, Color.ORANGE);
					toDraw.draw(graphics);
				}
				if (flags.contains("D")) {
					Drawable toDraw = new Circle(position.getX(), position.getY(), getOverlaySize(), Color.ORANGE);
					toDraw.draw(graphics);
				}
			}
		});
	}

	private void drawPMVectors(BufferedImage image, List<CatalogEntry> catalogEntries, Color color,
			double flipbookIndex) { // flipbookIndex has to be a double!
		Graphics graphics = image.getGraphics();
		for (CatalogEntry catalogEntry : catalogEntries) {
			NumberPair position = toPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
			catalogEntry.setPixelRa(position.getX());
			catalogEntry.setPixelDec(position.getY());

			double ra = catalogEntry.getRa();
			double dec = catalogEntry.getDec();

			double pmRa = catalogEntry.getPmra();
			double pmDec = catalogEntry.getPmdec();

			double numberOfYears = 0;
			if (catalogEntry instanceof GaiaDR2CatalogEntry) {
				numberOfYears = GAIADR2_ALLWISE_EPOCH_DIFF;
			}
			if (catalogEntry instanceof GaiaDR3CatalogEntry) {
				numberOfYears = GAIADR3_ALLWISE_EPOCH_DIFF;
			}
			if (catalogEntry instanceof NoirlabCatalogEntry entry) {
				numberOfYears = entry.getMeanEpoch() - ALLWISE_REFERENCE_EPOCH;
			}
			if (catalogEntry instanceof CatWiseCatalogEntry entry) {
				ra = entry.getRa_pm();
				dec = entry.getDec_pm();
				numberOfYears = CATWISE_ALLWISE_EPOCH_DIFF;
			}
			if (catalogEntry instanceof UkidssCatalogEntry entry) {
				numberOfYears = entry.getMeanEpoch() - ALLWISE_REFERENCE_EPOCH;
			}

			if (showProperMotion.isSelected()) {
				double flipbookSize = flipbook.size() - 1;
				if (separateScanDirections.isSelected() && !skipIntermediateEpochs.isSelected()) {
					flipbookSize /= 2;
					if (flipbookIndex > flipbookSize) {
						flipbookIndex -= flipbookSize;
					}
				}
				double totalEpochs = (flipbookIndex / flipbookSize) * getNumberOfWiseEpochs() * 2;
				NumberPair newPosition = getNewPosition(ra, dec, pmRa, pmDec, numberOfYears, totalEpochs);
				NumberPair pixelCoords = toPixelCoordinates(newPosition.getX(), newPosition.getY());
				Disk disk = new Disk(pixelCoords.getX(), pixelCoords.getY(), getOverlaySize(2), color);
				disk.draw(image.getGraphics());
			} else {
				NumberPair fromCoords = calculatePositionFromProperMotion(new NumberPair(ra, dec),
						new NumberPair(-numberOfYears * pmRa / DEG_MAS, -numberOfYears * pmDec / DEG_MAS));
				double fromRa = fromCoords.getX();
				double fromDec = fromCoords.getY();

				NumberPair fromPoint = toPixelCoordinates(fromRa, fromDec);
				double fromX = fromPoint.getX();
				double fromY = fromPoint.getY();

				numberOfYears = getNumberOfWiseEpochs() + 2; // +2 years -> hibernation period

				NumberPair toCoords = calculatePositionFromProperMotion(new NumberPair(fromRa, fromDec),
						new NumberPair(numberOfYears * pmRa / DEG_MAS, numberOfYears * pmDec / DEG_MAS));
				double toRa = toCoords.getX();
				double toDec = toCoords.getY();

				NumberPair toPoint = toPixelCoordinates(toRa, toDec);
				double toX = toPoint.getX();
				double toY = toPoint.getY();

				Arrow arrow = new Arrow(fromX, fromY, toX, toY, getOverlaySize(), color);
				arrow.draw(graphics);
			}
		}
	}

	private NumberPair getNewPosition(double ra, double dec, double pmRa, double pmDec, double numberOfYears,
			double totalEpochs) {
		NumberPair fromCoords = calculatePositionFromProperMotion(new NumberPair(ra, dec),
				new NumberPair(-numberOfYears * pmRa / DEG_MAS, -numberOfYears * pmDec / DEG_MAS));
		double fromRa = fromCoords.getX();
		double fromDec = fromCoords.getY();

		NumberPair toCoords = calculatePositionFromProperMotion(new NumberPair(fromRa, fromDec),
				new NumberPair(totalEpochs * (pmRa / 2) / DEG_MAS, totalEpochs * (pmDec / 2) / DEG_MAS));
		double toRa = toCoords.getX();
		double toDec = toCoords.getY();

		return new NumberPair(toRa, toDec);
	}

	private void showPMInfo(List<CatalogEntry> catalogEntries, int x, int y, Color color) {
		catalogEntries.forEach(catalogEntry -> {
			double radius = getOverlaySize() / 2;
			if (catalogEntry.getPixelRa() > x - radius && catalogEntry.getPixelRa() < x + radius
					&& catalogEntry.getPixelDec() > y - radius && catalogEntry.getPixelDec() < y + radius) {
				displayCatalogPanel(catalogEntry, color, true);
			}
		});
	}

	private void showCatalogInfo(List<CatalogEntry> catalogEntries, int x, int y, Color color) {
		catalogEntries.forEach(catalogEntry -> {
			double radius = getOverlaySize() / 2;
			if (catalogEntry.getPixelRa() > x - radius && catalogEntry.getPixelRa() < x + radius
					&& catalogEntry.getPixelDec() > y - radius && catalogEntry.getPixelDec() < y + radius) {
				displayCatalogPanel(catalogEntry, color, true);
			}
		});
	}

	private void displayCatalogPanel(CatalogEntry catalogEntry, Color color, boolean addExtinctionCheckbox) {
		boolean simpleLayout = catalogEntry instanceof GenericCatalogEntry || catalogEntry instanceof SsoCatalogEntry;
		List<CatalogElement> catalogElements = catalogEntry.getCatalogElements();

		int elements = catalogElements.size();
		int rows = elements / 2;
		int remainder = elements % 2;
		rows += remainder;

		int maxRows;
		if (simpleLayout) {
			maxRows = rows > 30 ? rows : 30;
		} else {
			maxRows = rows > 20 ? rows : 20;
		}

		JPanel detailPanel = new JPanel(new GridLayout(0, 4));
		detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				catalogEntry.getCatalogName()
						+ " entry (Computed values are shown in green; (*) Further info: mouse pointer)",
				TitledBorder.LEFT, TitledBorder.TOP));

		catalogElements.forEach(element -> {
			addLabelToPanel(element, detailPanel);
			addFieldToPanel(element, detailPanel);
		});

		if (remainder == 1) {
			addEmptyCatalogElement(detailPanel);
		}
		for (int i = 0; i < maxRows - rows; i++) {
			addEmptyCatalogElement(detailPanel);
			addEmptyCatalogElement(detailPanel);
		}

		JScrollPane scrollPanel = new JScrollPane(detailPanel);
		scrollPanel.setBorder(BorderFactory.createEmptyBorder());
		scrollPanel.setMinimumSize(new Dimension(CATALOG_PANEL_WIDTH, 350));

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBorder(new LineBorder(color, 3));
		container.add(simpleLayout ? detailPanel : scrollPanel);

		if (!simpleLayout) {
			List<LookupResult> mainSequenceResults = mainSequenceSpectralTypeLookupService
					.lookup(catalogEntry.getColors(true));
			if (!mainSequenceResults.isEmpty()) {
				container.add(createMainSequenceSpectralTypePanel(mainSequenceResults, catalogEntry, color));
				if (catalogEntry instanceof AllWiseCatalogEntry entry) {
					if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
						JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
						messagePanel.add(createLabel(AGN_WARNING, JColor.RED));
						container.add(messagePanel);
					}
				}
				if (catalogEntry instanceof WhiteDwarf entry) {
					if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
						JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
						messagePanel.add(createLabel(WD_WARNING, JColor.RED));
						container.add(messagePanel);
					}
				}
			}
			List<LookupResult> brownDwarfsResults = brownDwarfsSpectralTypeLookupService
					.lookup(catalogEntry.getColors(true));
			if (!brownDwarfsResults.isEmpty()) {
				container.add(createBrownDwarfsSpectralTypePanel(brownDwarfsResults, catalogEntry, color));
			}
			if (mainSequenceResults.isEmpty() && brownDwarfsResults.isEmpty()) {
				container.add(createMainSequenceSpectralTypePanel(mainSequenceResults, catalogEntry, color));
				JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				messagePanel.add(createLabel("No colors available / No match", JColor.RED));
				container.add(messagePanel);
			}

			JPanel toolsPanel = new JPanel();
			toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.Y_AXIS));
			container.add(toolsPanel);

			JPanel collectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			toolsPanel.add(collectPanel);

			collectPanel.add(new JLabel("Object type:"));

			JComboBox objectTypes = new JComboBox(ObjectType.labels());
			collectPanel.add(objectTypes);

			JButton collectButton = new JButton("Add to collection");
			collectPanel.add(collectButton);
			Timer collectTimer = new Timer(3000, (ActionEvent e) -> {
				collectButton.setText("Add to collection");
			});
			collectButton.addActionListener((ActionEvent evt) -> {
				String selectedObjectType = (String) objectTypes.getSelectedItem();
				collectObject(selectedObjectType, catalogEntry, baseFrame, brownDwarfsSpectralTypeLookupService,
						collectionTable);
				collectButton.setText("Added!");
				collectTimer.restart();
			});

			if (catalogEntry instanceof SimbadCatalogEntry) {
				JButton referencesButton = new JButton("Object references");
				collectPanel.add(referencesButton);
				referencesButton.addActionListener((ActionEvent evt) -> {
					JFrame referencesFrame = new JFrame();
					referencesFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					referencesFrame.addWindowListener(getChildWindowAdapter(baseFrame));
					referencesFrame.setIconImage(getToolBoxImage());
					referencesFrame.setTitle("Measurements and references for " + catalogEntry.getSourceId() + " ("
							+ roundTo7DecNZ(catalogEntry.getRa()) + " " + roundTo7DecNZ(catalogEntry.getDec()) + ")");
					referencesFrame.add(new JScrollPane(new ReferencesPanel(catalogEntry, referencesFrame)));
					referencesFrame.setSize(BASE_FRAME_WIDTH, BASE_FRAME_HEIGHT);
					referencesFrame.setLocation(0, 0);
					referencesFrame.setAlwaysOnTop(false);
					referencesFrame.setResizable(true);
					referencesFrame.setVisible(true);
				});
			}

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			toolsPanel.add(buttonPanel);

			JButton copyCoordsButton = new JButton("Copy coords");
			buttonPanel.add(copyCoordsButton);
			Timer copyCoordsTimer = new Timer(3000, (ActionEvent e) -> {
				copyCoordsButton.setText("Copy coords");
			});
			copyCoordsButton.addActionListener((ActionEvent evt) -> {
				copyToClipboard(copyObjectCoordinates(catalogEntry));
				copyCoordsButton.setText("Copied to clipboard!");
				copyCoordsTimer.restart();
			});

			JButton copyInfoButton = new JButton("Copy summary");
			buttonPanel.add(copyInfoButton);
			Timer copyInfoTimer = new Timer(3000, (ActionEvent e) -> {
				copyInfoButton.setText("Copy summary");
			});
			copyInfoButton.addActionListener((ActionEvent evt) -> {
				copyToClipboard(copyObjectSummary(catalogEntry));
				copyInfoButton.setText("Copied to clipboard!");
				copyInfoTimer.restart();
			});

			JButton copyAllButton = new JButton("Copy all");
			buttonPanel.add(copyAllButton);
			Timer copyAllTimer = new Timer(3000, (ActionEvent e) -> {
				copyAllButton.setText("Copy all");
			});
			copyAllButton.addActionListener((ActionEvent evt) -> {
				copyToClipboard(
						copyObjectInfo(catalogEntry, mainSequenceResults, brownDwarfsResults, distanceLookupService));
				copyAllButton.setText("Copied to clipboard!");
				copyAllTimer.restart();
			});

			JButton fillFormButton = new JButton("TYGO form");
			buttonPanel.add(fillFormButton);
			fillFormButton.addActionListener((ActionEvent evt) -> {
				fillTygoForm(catalogEntry, catalogQueryService, baseFrame);

			});

			JButton createSedButton = new JButton("SED (MS)");
			buttonPanel.add(createSedButton);
			createSedButton.addActionListener((ActionEvent evt) -> {
				createSedButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frame.addWindowListener(getChildWindowAdapter(baseFrame));
				frame.setIconImage(getToolBoxImage());
				frame.setTitle("SED");
				frame.add(new SedMsPanel(brownDwarfLookupEntries, catalogQueryService, catalogEntry, baseFrame));
				frame.setSize(1000, 900);
				frame.setLocation(0, 0);
				frame.setAlwaysOnTop(false);
				frame.setResizable(true);
				frame.setVisible(true);
				createSedButton.setCursor(Cursor.getDefaultCursor());
			});

			JButton createWdSedButton = new JButton("SED (WD)");
			buttonPanel.add(createWdSedButton);
			createWdSedButton.addActionListener((ActionEvent evt) -> {
				createWdSedButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frame.addWindowListener(getChildWindowAdapter(baseFrame));
				frame.setIconImage(getToolBoxImage());
				frame.setTitle("WD SED");
				frame.add(new SedWdPanel(catalogQueryService, catalogEntry, baseFrame));
				frame.setSize(1000, 900);
				frame.setLocation(0, 0);
				frame.setAlwaysOnTop(false);
				frame.setResizable(true);
				frame.setVisible(true);
				createWdSedButton.setCursor(Cursor.getDefaultCursor());
			});

			JButton createCcdButton = new JButton("WISE CCD");
			collectPanel.add(createCcdButton);
			createCcdButton.addActionListener((ActionEvent evt) -> {
				try {
					createCcdButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					JFrame frame = new JFrame();
					frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					frame.addWindowListener(getChildWindowAdapter(baseFrame));
					frame.setIconImage(getToolBoxImage());
					frame.setTitle("WISE CCD");
					frame.add(new WiseCcdPanel(catalogQueryService, catalogEntry, baseFrame));
					frame.setSize(1000, 900);
					frame.setLocation(0, 0);
					frame.setAlwaysOnTop(false);
					frame.setResizable(true);
					frame.setVisible(true);
				} catch (HeadlessException | SecurityException ex) {
					showErrorDialog(baseFrame, ex.getMessage());
				} finally {
					createCcdButton.setCursor(Cursor.getDefaultCursor());
				}
			});

			JButton createLcButton = new JButton("WISE LC");
			collectPanel.add(createLcButton);
			createLcButton.addActionListener((ActionEvent evt) -> {
				try {
					createLcButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					JFrame frame = new JFrame();
					frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
					frame.addWindowListener(getChildWindowAdapter(baseFrame));
					frame.setIconImage(getToolBoxImage());
					frame.setTitle("WISE light curves");
					frame.add(new WiseLcPanel(catalogEntry, baseFrame));
					frame.setSize(1000, 900);
					frame.setLocation(0, 0);
					frame.setAlwaysOnTop(false);
					frame.setResizable(true);
					frame.setVisible(true);
				} catch (HeadlessException | SecurityException ex) {
					showErrorDialog(baseFrame, ex.getMessage());
				} finally {
					createLcButton.setCursor(Cursor.getDefaultCursor());
				}
			});

			if (catalogEntry instanceof GaiaCmd cmd) {
				JButton createCmdButton = new JButton("Gaia CMD");
				collectPanel.add(createCmdButton);
				createCmdButton.addActionListener((var evt) -> {
					try {
						createCmdButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						JFrame frame = new JFrame();
						frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
						frame.addWindowListener(getChildWindowAdapter(baseFrame));
						frame.setIconImage(getToolBoxImage());
						frame.setTitle("Gaia CMD");
						frame.add(new GaiaCmdPanel(cmd));
						frame.setSize(1000, 900);
						frame.setLocation(0, 0);
						frame.setAlwaysOnTop(false);
						frame.setResizable(true);
						frame.setVisible(true);
					} catch (HeadlessException | SecurityException ex) {
						showErrorDialog(baseFrame, ex.getMessage());
					} finally {
						createCmdButton.setCursor(Cursor.getDefaultCursor());
					}
				});
			}

			if (addExtinctionCheckbox && catalogEntry instanceof Extinction) {
				final Extinction selectedEntry = (Extinction) catalogEntry.copy();
				JPanel extinctionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				toolsPanel.add(extinctionPanel);
				JCheckBox dustExtinction = new JCheckBox(
						"Apply extinction correction for bands u, g, r, i, z, J, H, K, W1 & W2 (Schlafly & Finkbeiner, 2011)");
				extinctionPanel.add(dustExtinction);
				dustExtinction.addActionListener((ActionEvent evt) -> {
					if (dustExtinction.isSelected()) {
						baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						try {
							Map<String, Double> extinctionsByBand = dustExtinctionService
									.getExtinctionsByBand(selectedEntry.getRa(), selectedEntry.getDec(), 2.0);
							try {
								selectedEntry.applyExtinctionCorrection(extinctionsByBand);
								selectedEntry.loadCatalogElements();
								displayCatalogPanel(selectedEntry, color, false);
							} catch (ExtinctionException ex) {
								extinctionPanel.add(createLabel(
										"No extinction values for " + selectedEntry.getCatalogName() + " bands.",
										JColor.RED));
							}
						} catch (Exception ex) {
							showExceptionDialog(baseFrame, ex);
						} finally {
							baseFrame.setCursor(Cursor.getDefaultCursor());
						}
					}
				});
			}
		}

		JFrame detailsFrame = new JFrame();
		detailsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		detailsFrame.addWindowListener(getChildWindowAdapter(baseFrame));
		detailsFrame.setIconImage(getToolBoxImage());
		detailsFrame.setTitle("Object details");
		detailsFrame.add(simpleLayout ? new JScrollPane(container) : container);
		detailsFrame.setSize(CATALOG_PANEL_WIDTH, 700);
		detailsFrame.setLocation(windowShift, windowShift);
		detailsFrame.setAlwaysOnTop(false);
		detailsFrame.setResizable(true);
		detailsFrame.setVisible(true);
		windowShift += 10;
	}

	private JScrollPane createMainSequenceSpectralTypePanel(List<LookupResult> results, CatalogEntry catalogEntry,
			Color color) {
		List<String[]> spectralTypes = new ArrayList<>();
		results.forEach(entry -> {
			String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
			String spectralType = entry.getSpt() + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + ","
					+ roundTo3DecLZ(entry.getGap()) + "," + entry.getTeff() + "," + roundTo3Dec(entry.getRsun()) + ","
					+ roundTo3Dec(entry.getMsun());
			spectralTypes.add(spectralType.split(",", -1));
		});

		String titles = "spt,matched color,nearest color,offset,teff,radius (Rsun),mass (Msun)";
		String[] columns = titles.split(",", -1);
		Object[][] rows = new Object[][] {};
		JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns);
		alignResultColumns(spectralTypeTable, spectralTypes);
		spectralTypeTable.setAutoCreateRowSorter(true);
		spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumnModel columnModel = spectralTypeTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(50);
		columnModel.getColumn(1).setPreferredWidth(120);
		columnModel.getColumn(2).setPreferredWidth(75);
		columnModel.getColumn(3).setPreferredWidth(50);
		columnModel.getColumn(4).setPreferredWidth(50);
		columnModel.getColumn(5).setPreferredWidth(75);
		columnModel.getColumn(6).setPreferredWidth(75);

		spectralTypeTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (!e.getValueIsAdjusting()) {
				if (currentTable != null && currentTable != spectralTypeTable) {
					try {
						currentTable.clearSelection();
					} catch (Exception ex) {
					}
				}
				currentTable = spectralTypeTable;
				String spt = (String) spectralTypeTable.getValueAt(spectralTypeTable.getSelectedRow(), 0);
				List<DistanceLookupResult> distanceResults = distanceLookupService.lookup(spt, catalogEntry.getBands());
				createDistanceEstimatesPanel(distanceResults, spt, color);
			}
		});

		JScrollPane spectralTypePanel = new JScrollPane(spectralTypeTable);
		spectralTypePanel.setToolTipText(PHOT_DIST_INFO);
		spectralTypePanel.setMinimumSize(new Dimension(CATALOG_PANEL_WIDTH, 75));
		spectralTypePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				html("Main sequence spectral type estimates " + INFO_ICON), TitledBorder.LEFT, TitledBorder.TOP));

		return spectralTypePanel;
	}

	private JScrollPane createBrownDwarfsSpectralTypePanel(List<LookupResult> results, CatalogEntry catalogEntry,
			Color color) {
		List<String[]> spectralTypes = new ArrayList<>();
		results.forEach(entry -> {
			String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
			String spectralType = entry.getSpt() + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + ","
					+ roundTo3DecLZ(entry.getGap());
			spectralTypes.add(spectralType.split(",", -1));
		});

		String titles = "spt,matched color,nearest color,offset";
		String[] columns = titles.split(",", -1);
		Object[][] rows = new Object[][] {};
		JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns);
		alignResultColumns(spectralTypeTable, spectralTypes);
		spectralTypeTable.setAutoCreateRowSorter(true);
		spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumnModel columnModel = spectralTypeTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(50);
		columnModel.getColumn(1).setPreferredWidth(120);
		columnModel.getColumn(2).setPreferredWidth(75);
		columnModel.getColumn(3).setPreferredWidth(50);

		spectralTypeTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (!e.getValueIsAdjusting()) {
				if (currentTable != null && currentTable != spectralTypeTable) {
					try {
						currentTable.clearSelection();
					} catch (Exception ex) {
					}
				}
				currentTable = spectralTypeTable;
				String spt = (String) spectralTypeTable.getValueAt(spectralTypeTable.getSelectedRow(), 0);
				List<DistanceLookupResult> distanceResults = distanceLookupService.lookup(spt, catalogEntry.getBands());
				createDistanceEstimatesPanel(distanceResults, spt, color);
			}
		});

		JScrollPane spectralTypePanel = new JScrollPane(spectralTypeTable);
		spectralTypePanel.setToolTipText(PHOT_DIST_INFO);
		spectralTypePanel.setMinimumSize(new Dimension(CATALOG_PANEL_WIDTH, 75));
		spectralTypePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				html("M, L & T dwarfs spectral type estimates " + INFO_ICON), TitledBorder.LEFT, TitledBorder.TOP));

		return spectralTypePanel;
	}

	private void createDistanceEstimatesPanel(List<DistanceLookupResult> results, String spt, Color color) {
		List<String[]> distances = new ArrayList<>();
		results.forEach(entry -> {
			String matchedBand = entry.getBandKey().val + "=" + roundTo3DecNZ(entry.getBandValue());
			String distance = roundTo3Dec(entry.getDistance());
			if (entry.getDistanceError() > 0) {
				distance += "±" + roundTo3Dec(entry.getDistanceError());
			}
			String resutValues = distance + "," + matchedBand;
			distances.add(resutValues.split(",", -1));
		});

		String titles = "distance (pc),matched bands";
		String[] columns = titles.split(",", -1);
		Object[][] rows = new Object[][] {};
		JTable distanceTable = new JTable(distances.toArray(rows), columns);
		alignResultColumns(distanceTable, distances);
		distanceTable.setAutoCreateRowSorter(true);
		distanceTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumnModel columnModel = distanceTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(100);
		columnModel.getColumn(1).setPreferredWidth(100);

		JScrollPane distancePanel = new JScrollPane(distanceTable);
		distancePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				"Spectral type: " + spt, TitledBorder.LEFT, TitledBorder.TOP));

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBorder(new LineBorder(color, 3));
		container.add(distancePanel);

		JFrame detailsFrame = new JFrame();
		detailsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		detailsFrame.addWindowListener(getChildWindowAdapter(baseFrame));
		detailsFrame.setIconImage(getToolBoxImage());
		detailsFrame.setTitle("Photometric distance estimates");
		detailsFrame.add(container);
		detailsFrame.setSize(500, 300);
		detailsFrame.setLocation(windowShift, windowShift);
		detailsFrame.setAlwaysOnTop(true);
		detailsFrame.setResizable(true);
		detailsFrame.setVisible(true);
		windowShift += 10;
	}

	private double getFovDiagonal() {
		return size * pixelScale * sqrt(2);
	}

	private double getOverlaySize() {
		return getOverlaySize(1);
	}

	private double getOverlaySize(int scale) {
		double factor = desiCutouts.isSelected() || ps1Cutouts.isSelected() ? 0.25 : 0.15;
		double overlaySize = scale * factor * zoom * sqrt(size) / size;
		return max(5, min(overlaySize, 15));
	}

	private int getNumberOfWiseEpochs() {
		return wiseviewCutouts.isSelected() ? NUMBER_OF_WISE_EPOCHS : NUMBER_OF_UNWISE_EPOCHS;
	}

	public JCheckBox getBlurImages() {
		return blurImages;
	}

	public JCheckBox getUseCustomOverlays() {
		return useCustomOverlays;
	}

	public JComboBox getWiseBands() {
		return wiseBands;
	}

	public JTextField getCoordsField() {
		return coordsField;
	}

	public JTextField getSizeField() {
		return sizeField;
	}

	public JSlider getSpeedSlider() {
		return speedSlider;
	}

	public JSlider getZoomSlider() {
		return zoomSlider;
	}

	public JButton getStopDownloadButton() {
		return stopDownloadButton;
	}

	public JTextField getDifferentSizeField() {
		return differentSizeField;
	}

	public JTextField getProperMotionField() {
		return properMotionField;
	}

	public JRadioButton getWiseCoadds() {
		return unwiseCutouts;
	}

	public JRadioButton getDesiCutouts() {
		return desiCutouts;
	}

	public JRadioButton getPs1Cutouts() {
		return ps1Cutouts;
	}

	public JCheckBox getSkipIntermediateEpochs() {
		return skipIntermediateEpochs;
	}

	public JCheckBox getSimbadOverlay() {
		return simbadOverlay;
	}

	public JCheckBox getAllWiseOverlay() {
		return allWiseOverlay;
	}

	public JCheckBox getCatWiseOverlay() {
		return catWiseOverlay;
	}

	public JCheckBox getUnWiseOverlay() {
		return unWiseOverlay;
	}

	public JCheckBox getGaiaOverlay() {
		return gaiaOverlay;
	}

	public JCheckBox getGaiaDR3Overlay() {
		return gaiaDR3Overlay;
	}

	public JCheckBox getNoirlabOverlay() {
		return noirlabOverlay;
	}

	public JCheckBox getPanStarrsOverlay() {
		return panStarrsOverlay;
	}

	public JCheckBox getSdssOverlay() {
		return sdssOverlay;
	}

	public JCheckBox getVhsOverlay() {
		return vhsOverlay;
	}

	public JCheckBox getUhsOverlay() {
		return uhsOverlay;
	}

	public JCheckBox getUkidssOverlay() {
		return ukidssOverlay;
	}

	public JCheckBox getTwoMassOverlay() {
		return twoMassOverlay;
	}

	public JCheckBox getTessOverlay() {
		return tessOverlay;
	}

	public JCheckBox getDesOverlay() {
		return desOverlay;
	}

	public JCheckBox getGaiaWDOverlay() {
		return gaiaWDOverlay;
	}

	public JCheckBox getMocaOverlay() {
		return mocaOverlay;
	}

	public JTextField getPanstarrsField() {
		return panstarrsField;
	}

	public JTextField getAladinLiteField() {
		return aladinLiteField;
	}

	public JTextField getWiseViewField() {
		return wiseViewField;
	}

	public JTextField getFinderChartField() {
		return finderChartField;
	}

	public JButton getChangeFovButton() {
		return changeFovButton;
	}

	public Timer getTimer() {
		return timer;
	}

	public List<FlipbookComponent> getFlipbook() {
		return flipbook;
	}

	public ImageViewerTab getImageViewer() {
		return imageViewer;
	}

	public void setImageViewer(ImageViewerTab imageViewer) {
		this.imageViewer = imageViewer;
	}

	public void setCustomOverlays(Map<String, CustomOverlay> customOverlays) {
		this.customOverlays = customOverlays;
	}

	public void setCollectionTable(JTable collectionTable) {
		this.collectionTable = collectionTable;
	}

	public void setQuadrantCount(int quadrantCount) {
		this.quadrantCount = quadrantCount;
	}

	public void setWiseBand(WiseBand wiseBand) {
		this.wiseBand = wiseBand;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setPixelScale(double pixelScale) {
		this.pixelScale = pixelScale;
	}

	public void setAsyncDownloads(boolean asyncDownloads) {
		this.asyncDownloads = asyncDownloads;
	}

	public void setPanstarrsImages(boolean panstarrsImages) {
		this.panstarrsImages = panstarrsImages;
	}

	public void setVhsImages(boolean vhsImages) {
		this.vhsImages = vhsImages;
	}

	public void setUhsImages(boolean uhsImages) {
		this.uhsImages = uhsImages;
	}

	public void setUkidssImages(boolean ukidssImages) {
		this.ukidssImages = ukidssImages;
	}

	public void setLegacyImages(boolean legacyImages) {
		this.legacyImages = legacyImages;
	}

	public void setSdssImages(boolean sdssImages) {
		this.sdssImages = sdssImages;
	}

	public void setDssImages(boolean dssImages) {
		this.dssImages = dssImages;
	}

	public void setWaitCursor(boolean waitCursor) {
		this.waitCursor = waitCursor;
	}

}
