package astro.tool.box.tab;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.function.StatisticFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.tab.SettingsTab.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceHelper.*;
import static astro.tool.box.util.ExternalResources.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.Couple;
import astro.tool.box.container.CustomOverlay;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.Overlays;
import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.Artifact;
import astro.tool.box.catalog.CatWiseCatalogEntry;
import astro.tool.box.catalog.CatWiseRejectEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.DesCatalogEntry;
import astro.tool.box.catalog.GaiaCatalogEntry;
import astro.tool.box.catalog.GaiaCmd;
import astro.tool.box.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.catalog.GaiaWDCatalogEntry;
import astro.tool.box.catalog.GenericCatalogEntry;
import astro.tool.box.catalog.NoirlabCatalogEntry;
import astro.tool.box.catalog.PanStarrsCatalogEntry;
import astro.tool.box.catalog.ProperMotionQuery;
import astro.tool.box.catalog.SdssCatalogEntry;
import astro.tool.box.catalog.SsoCatalogEntry;
import astro.tool.box.catalog.SimbadCatalogEntry;
import astro.tool.box.catalog.TessCatalogEntry;
import astro.tool.box.catalog.TwoMassCatalogEntry;
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.catalog.WhiteDwarf;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.lookup.SpectralTypeLookupEntry;
import astro.tool.box.lookup.LookupResult;
import astro.tool.box.enumeration.Epoch;
import astro.tool.box.enumeration.FileType;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.ObjectType;
import astro.tool.box.enumeration.Shape;
import astro.tool.box.enumeration.WiseBand;
import astro.tool.box.main.Application;
import astro.tool.box.panel.CmdPanel;
import astro.tool.box.container.FlipbookComponent;
import astro.tool.box.main.GifSequencer;
import astro.tool.box.container.ImageContainer;
import astro.tool.box.main.InfoSheet;
import astro.tool.box.panel.ReferencesPanel;
import astro.tool.box.panel.SedPanel;
import astro.tool.box.panel.WdSedPanel;
import astro.tool.box.shape.Arrow;
import astro.tool.box.shape.Circle;
import astro.tool.box.shape.Cross;
import astro.tool.box.shape.CrossHair;
import astro.tool.box.shape.Diamond;
import astro.tool.box.shape.Disk;
import astro.tool.box.shape.Drawable;
import astro.tool.box.shape.Square;
import astro.tool.box.shape.Text;
import astro.tool.box.shape.Triangle;
import astro.tool.box.shape.XCross;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.DistanceLookupService;
import astro.tool.box.service.SpectralTypeLookupService;
import astro.tool.box.util.CSVParser;
import astro.tool.box.util.Counter;
import astro.tool.box.util.FileTypeFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
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
import static java.lang.Math.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.Month;
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
import java.util.stream.Collectors;
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
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultCaret;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class ImageViewerTab {

    public static final String TAB_NAME = "Image Viewer";
    public static final String EPOCH_LABEL = "Survey epochs: %d";
    public static final WiseBand WISE_BAND = WiseBand.W2;
    public static final Epoch EPOCH = Epoch.FIRST_LAST;
    public static final String AUTO_RANGE = "AUTO";
    public static final double OVERLAP_FACTOR = 0.9;
    public static final int NUMBER_OF_EPOCHS = 8;
    public static final int NUMBER_OF_UNWISE_EPOCHS = 8;
    public static final int WINDOW_SPACING = 25;
    public static final int PANEL_HEIGHT = 270;
    public static final int PANEL_WIDTH = 230;
    public static final int ROW_HEIGHT = 25;
    public static final int EPOCH_GAP = 5;
    public static final int SPEED = 300;
    public static final int ZOOM = 500;
    public static final int SIZE = 500;
    public static final int DIFFERENT_SIZE = 100;
    public static final int PROPER_MOTION = 100;
    public static final String OVERLAYS_KEY = "overlays";
    public static final String CHANGE_FOV_TEXT = "Current field of view: %d\" (*)";
    public static final String NO_OBJECT_FOUND = "Proper motion checker:\nNo object found at the given coordinates in a search radius of 5 arcsec.";

    //Reference epochs:
    //allwise: 2010.559
    //catwise: 2014.118 -> catwise - allwise = 3.559 (CatWISE Preliminary)
    //catwise: 2015.405 -> catwise - allwise = 4.846 (CatWISE2020)
    //gaiadr2: 2015.5   -> gaiadr2 - allwise = 4.941
    //gaiadr3: 2016.0   -> gaiadr3 - allwise = 5.441
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
    private final List<SpectralTypeLookup> brownDwarfLookupEntries;
    private final Overlays overlays;
    private List<CatalogEntry> simbadEntries;
    private List<CatalogEntry> gaiaEntries;
    private List<CatalogEntry> gaiaTpmEntries;
    private List<CatalogEntry> gaiaDR3Entries;
    private List<CatalogEntry> gaiaDR3TpmEntries;
    private List<CatalogEntry> allWiseEntries;
    private List<CatalogEntry> catWiseEntries;
    private List<CatalogEntry> catWiseTpmEntries;
    private List<CatalogEntry> catWiseRejectEntries;
    private List<CatalogEntry> unWiseEntries;
    private List<CatalogEntry> panStarrsEntries;
    private List<CatalogEntry> sdssEntries;
    private List<CatalogEntry> twoMassEntries;
    private List<CatalogEntry> vhsEntries;
    private List<CatalogEntry> gaiaWDEntries;
    private List<CatalogEntry> noirlabEntries;
    private List<CatalogEntry> noirlabTpmEntries;
    private List<CatalogEntry> tessEntries;
    private List<CatalogEntry> desEntries;
    private List<CatalogEntry> ssoEntries;

    private JPanel imagePanel;
    private JPanel rightPanel;
    private JLabel changeFovLabel;
    private JLabel epochLabel;
    private JPanel zooniversePanel1;
    private JPanel zooniversePanel2;
    private JScrollPane rightScrollPanel;
    private JRadioButton wiseviewCutouts;
    private JRadioButton unwiseCutouts;
    private JRadioButton decalsCutouts;
    private JRadioButton showCatalogsButton;
    private JCheckBox blurImages;
    private JCheckBox invertColors;
    private JCheckBox borderFirst;
    private JCheckBox staticView;
    private JCheckBox markTarget;
    private JCheckBox showCrosshairs;
    private JCheckBox simbadOverlay;
    private JCheckBox gaiaOverlay;
    private JCheckBox gaiaDR3Overlay;
    private JCheckBox allWiseOverlay;
    private JCheckBox catWiseOverlay;
    private JCheckBox unWiseOverlay;
    private JCheckBox panStarrsOverlay;
    private JCheckBox sdssOverlay;
    private JCheckBox spectrumOverlay;
    private JCheckBox twoMassOverlay;
    private JCheckBox vhsOverlay;
    private JCheckBox gaiaWDOverlay;
    private JCheckBox noirlabOverlay;
    private JCheckBox tessOverlay;
    private JCheckBox desOverlay;
    private JCheckBox ssoOverlay;
    private JCheckBox ghostOverlay;
    private JCheckBox haloOverlay;
    private JCheckBox latentOverlay;
    private JCheckBox spikeOverlay;
    private JCheckBox gaiaProperMotion;
    private JCheckBox gaiaDR3ProperMotion;
    private JCheckBox catWiseProperMotion;
    private JCheckBox noirlabProperMotion;
    private JCheckBox showProperMotion;
    private JCheckBox showBrownDwarfsOnly;
    private JCheckBox displaySpectralTypes;
    private JCheckBox useCustomOverlays;
    private JCheckBox allSkyImages;
    private JCheckBox twoMassImages;
    private JCheckBox sloanImages;
    private JCheckBox spitzerImages;
    private JCheckBox allwiseImages;
    private JCheckBox ps1Images;
    private JCheckBox decalsImages;
    private JCheckBox staticTimeSeries;
    private JCheckBox animatedTimeSeries;
    private JCheckBox createDataSheet;
    private JCheckBox drawCrosshairs;
    private JComboBox wiseBands;
    private JComboBox epochs;
    private JComboBox ranges;
    private JSlider minValSlider;
    private JSlider maxValSlider;
    private JSlider speedSlider;
    private JSlider zoomSlider;
    private JSlider epochSlider;
    private JTextField coordsField;
    private JTextField sizeField;
    private JTextField properMotionField;
    private JTextField differentSizeField;
    private JTextArea crosshairCoords;
    private JTextArea downloadLog;
    private JTable collectionTable;
    private Timer timer;

    private BufferedImage wiseImage;
    private BufferedImage decalsImage;
    private BufferedImage ps1Image;
    private BufferedImage ukidssImage;
    private BufferedImage vhsImage;
    private BufferedImage sdssImage;
    private BufferedImage dssImage;
    private BufferedImage processedDecalsImage;
    private BufferedImage processedPs1Image;
    private BufferedImage processedUkidssImage;
    private BufferedImage processedVhsImage;
    private BufferedImage processedSdssImage;
    private BufferedImage processedDssImage;
    private Map<String, ImageContainer> imagesW1 = new HashMap<>();
    private Map<String, ImageContainer> imagesW2 = new HashMap<>();
    private Map<String, Fits> images;
    private Map<String, CustomOverlay> customOverlays;
    private List<Integer> requestedEpochs;
    private List<NumberPair> crosshairs;
    private FlipbookComponent[] flipbook;
    private ImageViewerTab imageViewer;

    private WiseBand wiseBand = WISE_BAND;
    private Epoch epoch = EPOCH;
    private String range = AUTO_RANGE;
    private double pixelScale = PIXEL_SCALE_WISE;
    private int fieldOfView = 30;
    private int shapeSize = 5;
    private int imageNumber;
    private int imageCount;
    private int windowShift;
    private int quadrantCount;
    private int epochCount;
    private int epochCountW1;
    private int epochCountW2;
    private int numberOfEpochs;
    private int selectedEpochs;
    private int minValue;
    private int maxValue;
    private int speed = SPEED;
    private int zoom = ZOOM;
    private int size = SIZE;

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
    private boolean bandW1Loaded;
    private boolean bandW2Loaded;
    private boolean allEpochsW1Loaded;
    private boolean allEpochsW2Loaded;
    private boolean moreImagesAvailable;
    private boolean flipbookComplete;
    private boolean reloadImages;
    private boolean imageCutOff;
    private boolean timerStopped;
    private boolean hasException;
    private boolean asyncDownloads;
    private boolean legacyImages;
    private boolean panstarrsImages;
    private boolean ukidssImages;
    private boolean vhsImages;
    private boolean sdssImages;
    private boolean dssImages;
    private boolean waitCursor = true;

    public static final List<String> MLTY_DWARFS = new ArrayList<>();

    static {
        for (int i = 0; i < 10; i++) {
            add("M", i);
        }
        for (int i = 0; i < 10; i++) {
            add("L", i);
        }
        for (int i = 0; i < 10; i++) {
            add("T", i);
        }
        for (int i = 0; i < 10; i++) {
            add("Y", i);
        }
    }

    static void add(String spt, int i) {
        MLTY_DWARFS.add(spt + i);
        MLTY_DWARFS.add(spt + i + ".5");
        MLTY_DWARFS.add(spt + i + "V");
        MLTY_DWARFS.add(spt + i + ".5V");
    }

    public ImageViewerTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        catalogQueryService = new CatalogQueryService();
        InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(",", -1));
            }).collect(Collectors.toList());
            mainSequenceSpectralTypeLookupService = new SpectralTypeLookupService(entries);
        }
        input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            brownDwarfLookupEntries = stream.skip(1).map(line -> {
                return new BrownDwarfLookupEntry(line.split(",", -1));
            }).collect(Collectors.toList());
            brownDwarfsSpectralTypeLookupService = new SpectralTypeLookupService(brownDwarfLookupEntries);
            distanceLookupService = new DistanceLookupService(brownDwarfLookupEntries);
        }
        overlays = new Overlays();
        overlays.deserialize(getUserSetting(OVERLAYS_KEY, overlays.serialize()));
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new BorderLayout());

            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JTabbedPane controlTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
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
            rightScrollPanel.setPreferredSize(new Dimension(225, rightPanel.getHeight()));

            mainPanel.add(rightScrollPanel, BorderLayout.EAST);

            //===================
            // Tab: Main controls
            //===================
            int rows = 33;
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
                WiseBand previousBand = wiseBand;
                wiseBand = (WiseBand) wiseBands.getSelectedItem();
                if (WiseBand.W1.equals(previousBand) && !WiseBand.W1.equals(wiseBand)) {
                    if (bandW2Loaded) {
                        if (!Epoch.isFirstLast(epoch) && !allEpochsW2Loaded) {
                            loadImages = true;
                        }
                    } else {
                        loadImages = true;
                    }
                    if (loadImages && allEpochsW1Loaded) {
                        imagesW1.clear();
                    }
                }
                if (WiseBand.W2.equals(previousBand) && !WiseBand.W2.equals(wiseBand)) {
                    if (bandW1Loaded) {
                        if (!Epoch.isFirstLast(epoch) && !allEpochsW1Loaded) {
                            loadImages = true;
                        }
                    } else {
                        loadImages = true;
                    }
                    if (loadImages && allEpochsW2Loaded) {
                        imagesW2.clear();
                    }
                }
                createFlipbook();
            });

            mainControlPanel.add(new JLabel("Epoch:"));

            epochs = new JComboBox(Epoch.values());
            mainControlPanel.add(epochs);
            epochs.setSelectedItem(epoch);
            epochs.addActionListener((ActionEvent evt) -> {
                Epoch previousEpoch = epoch;
                epoch = (Epoch) epochs.getSelectedItem();
                if (Epoch.isFirstLast(previousEpoch) && !Epoch.isFirstLast(epoch)) {
                    epochCountW1 = 0;
                    epochCountW2 = 0;
                    if (!allEpochsW1Loaded || !allEpochsW2Loaded) {
                        loadImages = true;
                    }
                }
                if (Epoch.isSubtracted(epoch)) {
                    if (!Epoch.isSubtracted(previousEpoch)) {
                        blurImages.setSelected(true);
                    }
                } else {
                    if (Epoch.isSubtracted(previousEpoch)) {
                        blurImages.setSelected(false);
                    }
                }
                createFlipbook();
            });

            mainControlPanel.add(new JLabel("Pixel value range (%):"));

            ranges = new JComboBox(new Object[]{
                AUTO_RANGE,
                "100",
                "99.9",
                "99.8",
                "99.7",
                "99.6",
                "99.5",
                "99.4",
                "99.3",
                "99.2",
                "99.1",
                "99",
                "98",
                "97",
                "96",
                "95",
                "94",
                "93",
                "92",
                "91",
                "90"
            });
            mainControlPanel.add(ranges);
            ranges.setSelectedItem(range);
            ranges.addActionListener((ActionEvent evt) -> {
                range = (String) ranges.getSelectedItem();
                createFlipbook();
            });

            mainControlPanel.add(new JLabel("Brightness:"));

            minValSlider = new JSlider();
            mainControlPanel.add(minValSlider);
            minValSlider.addChangeListener((ChangeEvent e) -> {
                minValue = minValSlider.getValue();
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                processImages();
            });

            mainControlPanel.add(new JLabel("Contrast:"));

            maxValSlider = new JSlider();
            mainControlPanel.add(maxValSlider);
            maxValSlider.setInverted(true);
            maxValSlider.addChangeListener((ChangeEvent e) -> {
                maxValue = maxValSlider.getValue();
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                processImages();
            });

            JLabel speedLabel = new JLabel(String.format("Speed: %d ms", speed));
            mainControlPanel.add(speedLabel);

            speedSlider = new JSlider(0, 2000, SPEED);
            mainControlPanel.add(speedSlider);
            speedSlider.addChangeListener((ChangeEvent e) -> {
                speed = speedSlider.getValue();
                speedLabel.setText(String.format("Speed: %d ms", speed));
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                timer.setDelay(speed);
                processImages();
            });

            JLabel zoomLabel = new JLabel(String.format("Zoom: %d", zoom));
            mainControlPanel.add(zoomLabel);

            zoomSlider = new JSlider(0, 2000, ZOOM);
            mainControlPanel.add(zoomSlider);
            zoomSlider.addChangeListener((ChangeEvent e) -> {
                zoom = zoomSlider.getValue();
                zoom = zoom < 100 ? 100 : zoom;
                zoomLabel.setText(String.format("Zoom: %d", zoom));
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                processImages();
            });

            epochLabel = new JLabel(String.format(EPOCH_LABEL, selectedEpochs));
            mainControlPanel.add(epochLabel);

            epochSlider = new JSlider(JSlider.HORIZONTAL, 2, NUMBER_OF_EPOCHS, NUMBER_OF_EPOCHS);
            mainControlPanel.add(epochSlider);
            epochSlider.setMajorTickSpacing(1);
            epochSlider.setPaintTicks(true);
            epochSlider.addChangeListener((ChangeEvent e) -> {
                selectedEpochs = epochSlider.getValue();
                epochLabel.setText(String.format(EPOCH_LABEL, selectedEpochs));
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                reloadImages = true;
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
                if (flipbook != null) {
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

            showCrosshairs = new JCheckBox(html("Crosshairs (*)"));
            settingsPanel.add(showCrosshairs);
            showCrosshairs.setToolTipText("Click on object to copy coordinates to clipboard (overlays must be disabled)");

            JButton resetDefaultsButton = new JButton("Reset image processing defaults");
            mainControlPanel.add(resetDefaultsButton);
            resetDefaultsButton.addActionListener((ActionEvent evt) -> {
                if (Epoch.isSubtracted(epoch)) {
                    blurImages.setSelected(true);
                } else {
                    blurImages.setSelected(false);
                }
                ranges.setSelectedItem(AUTO_RANGE);
            });

            mainControlPanel.add(new JLabel());

            mainControlPanel.add(createHeaderLabel("Cutout service:"));

            wiseviewCutouts = new JRadioButton(html("WiseView (*)"), true);
            mainControlPanel.add(wiseviewCutouts);
            wiseviewCutouts.setToolTipText("WiseView cutouts are from http://byw.tools/wiseview");
            wiseviewCutouts.addActionListener((ActionEvent evt) -> {
                resetEpochSlider(NUMBER_OF_EPOCHS);
                pixelScale = PIXEL_SCALE_WISE;
                previousRa = 0;
                previousDec = 0;
                ranges.setSelectedItem(AUTO_RANGE);
            });

            unwiseCutouts = new JRadioButton(html("unWISE coadds (*)"));
            mainControlPanel.add(unwiseCutouts);
            unwiseCutouts.setToolTipText("unWISE coadds are from http://unwise.me \nNo separate scan directions. High proper motion objects may look smeared.");
            unwiseCutouts.addActionListener((ActionEvent evt) -> {
                resetEpochSlider(NUMBER_OF_UNWISE_EPOCHS);
                pixelScale = PIXEL_SCALE_WISE;
                previousRa = 0;
                previousDec = 0;
                ranges.setSelectedItem(AUTO_RANGE);
            });

            decalsCutouts = new JRadioButton(html("DESI Legacy Survey (*)"));
            mainControlPanel.add(decalsCutouts);
            decalsCutouts.setToolTipText("DESI LS cutouts are from https://www.legacysurvey.org \nNot reliable for motion detection. Epochs can be to close together. \nW1 represents the r-band, W2 the z-band.");
            decalsCutouts.addActionListener((ActionEvent evt) -> {
                pixelScale = PIXEL_SCALE_DECAM;
                previousRa = 0;
                previousDec = 0;
                ranges.setSelectedItem(AUTO_RANGE);
            });

            ButtonGroup cutoutGroup = new ButtonGroup();
            cutoutGroup.add(wiseviewCutouts);
            cutoutGroup.add(unwiseCutouts);
            cutoutGroup.add(decalsCutouts);

            mainControlPanel.add(new JLabel(html("(*) Shows a tooltip when hovered")));

            mainControlPanel.add(createHeaderLabel("Nearest BYWP9 subjects:"));

            zooniversePanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainControlPanel.add(zooniversePanel1);

            zooniversePanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainControlPanel.add(zooniversePanel2);

            //======================
            // Tab: Catalog overlays
            //======================
            JPanel overlaysControlPanel = new JPanel(new GridLayout(rows, 1));
            overlaysControlPanel.setPreferredSize(new Dimension(controlPanelWidth - 20, controlPanelHeight));
            overlaysControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JScrollPane overlaysScrollPanel = new JScrollPane(overlaysControlPanel);
            overlaysScrollPanel.setPreferredSize(new Dimension(controlPanelWidth, 50));
            overlaysScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            controlTabs.add("Overlays", overlaysScrollPanel);

            JLabel catalogOverlaysLabel = createHeaderLabel(html("Catalog overlays: (*)"));
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
            gaiaDR3Overlay = new JCheckBox(html("<u>G</u>aia eDR3"), overlays.isGaiadr3());
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
            sdssOverlay = new JCheckBox(html("S<u>D</u>SS DR16"), overlays.isSdss());
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
            gaiaWDOverlay = new JCheckBox(html("Gaia eDR3 <u>W</u>D"), overlays.isGaiawd());
            gaiaWDOverlay.setForeground(JColor.PURPLE.val);
            gaiaWDOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(gaiaWDOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            overlaysControlPanel.add(overlayPanel);
            twoMassOverlay = new JCheckBox(html("2<u>M</u>ASS"), overlays.isTwomass());
            twoMassOverlay.setForeground(JColor.ORANGE.val);
            twoMassOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(twoMassOverlay);
            tessOverlay = new JCheckBox(html("<u>T</u>ESS"), overlays.isTess());
            tessOverlay.setForeground(JColor.LILAC.val);
            tessOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(tessOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            overlaysControlPanel.add(overlayPanel);
            desOverlay = new JCheckBox(html("D<u>E</u>S DR1"), overlays.isDes());
            desOverlay.setForeground(JColor.SAND.val);
            desOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(desOverlay);
            ssoOverlay = new JCheckBox("Solar Sys. Obj.", overlays.isSso());
            ssoOverlay.setForeground(Color.BLUE);
            ssoOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(ssoOverlay);

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
                        overlaysControlPanel.setPreferredSize(new Dimension(overlaysControlPanel.getWidth(), overlaysControlPanel.getHeight() + rowsHeight));
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
                        overlaysControlPanel.setPreferredSize(new Dimension(overlaysControlPanel.getWidth(), overlaysControlPanel.getHeight() - rowsHeight));
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

            JLabel pmOverlaysLabel = createHeaderLabel(html("Proper motion vectors: (*)"));
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
            gaiaDR3ProperMotion = new JCheckBox(html("<u>G</u>aia eDR3"), overlays.isPmgaiadr3());
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

            JLabel artifactsLabel = createHeaderLabel(html("WISE artifacts: (*)"));
            overlaysControlPanel.add(artifactsLabel);
            artifactsLabel.setToolTipText(html(""
                    + "Small shapes represent affected sources." + LINE_BREAK
                    + "Large shapes represent the actual artifacts.")
            );

            JPanel artifactPanel = new JPanel(new GridLayout(1, 2));
            overlaysControlPanel.add(artifactPanel);
            ghostOverlay = new JCheckBox("Ghosts", overlays.isGhosts());
            ghostOverlay.setForeground(Color.MAGENTA.darker());
            ghostOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            artifactPanel.add(ghostOverlay);
            haloOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;Halos&nbsp;</span>"), overlays.isHalos());
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
            spikeOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;Spikes&nbsp;</span>"), overlays.isSpikes());
            spikeOverlay.setForeground(Color.ORANGE);
            spikeOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            artifactPanel.add(spikeOverlay);

            JLabel featuresLabel = createHeaderLabel(html("Experimental features: (*)"));
            overlaysControlPanel.add(featuresLabel);
            featuresLabel.setToolTipText("Spectral type estimates are based on single colors and may not be accurate!");

            displaySpectralTypes = new JCheckBox("Display estimated spectral types", overlays.isEstspt());
            overlaysControlPanel.add(displaySpectralTypes);
            displaySpectralTypes.addActionListener((ActionEvent evt) -> {
                if (displaySpectralTypes.isSelected() && !isCatalogOverlaySelected()) {
                    gaiaOverlay.setSelected(true);
                }
                initCatalogEntries();
                processImages();
            });

            showBrownDwarfsOnly = new JCheckBox("Show potential M, L & T dwarfs only", overlays.isPotbd());
            overlaysControlPanel.add(showBrownDwarfsOnly);
            showBrownDwarfsOnly.addActionListener((ActionEvent evt) -> {
                if (showBrownDwarfsOnly.isSelected() && !isCatalogOverlaySelected()) {
                    gaiaOverlay.setSelected(true);
                }
                initCatalogEntries();
                processImages();
            });

            JLabel saveOverlaysMessage = createMessageLabel();
            Timer messageTimer = new Timer(3000, (ActionEvent e) -> {
                saveOverlaysMessage.setText("");
            });

            JButton saveButton = new JButton(html("Save selected overlays (*)"));
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
                overlays.setGaiawd(gaiaWDOverlay.isSelected());
                overlays.setTwomass(twoMassOverlay.isSelected());
                overlays.setTess(tessOverlay.isSelected());
                overlays.setDes(desOverlay.isSelected());
                overlays.setSso(ssoOverlay.isSelected());
                overlays.setPmgaiadr2(gaiaProperMotion.isSelected());
                overlays.setPmgaiadr3(gaiaDR3ProperMotion.isSelected());
                overlays.setPmnoirlab(noirlabProperMotion.isSelected());
                overlays.setPmcatwise(catWiseProperMotion.isSelected());
                overlays.setGhosts(ghostOverlay.isSelected());
                overlays.setLatents(haloOverlay.isSelected());
                overlays.setHalos(latentOverlay.isSelected());
                overlays.setSpikes(spikeOverlay.isSelected());
                overlays.setEstspt(displaySpectralTypes.isSelected());
                overlays.setPotbd(showBrownDwarfsOnly.isSelected());
                try (OutputStream output = new FileOutputStream(PROP_PATH)) {
                    USER_SETTINGS.setProperty(OVERLAYS_KEY, overlays.serialize());
                    USER_SETTINGS.store(output, COMMENTS);
                    saveOverlaysMessage.setText("Overlays saved!");
                    messageTimer.restart();
                } catch (IOException ex) {
                }
            });

            overlaysControlPanel.add(new JLabel(html("(*) Shows a tooltip when hovered")));

            overlaysControlPanel.add(saveOverlaysMessage);

            //====================
            // Tab: Mouse settings
            //====================
            JPanel mouseControlPanel = new JPanel(new GridLayout(rows, 1));
            mouseControlPanel.setPreferredSize(new Dimension(controlPanelWidth - 20, controlPanelHeight));
            mouseControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JScrollPane mouseScrollPanel = new JScrollPane(mouseControlPanel);
            mouseScrollPanel.setPreferredSize(new Dimension(controlPanelWidth, 50));
            mouseScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            controlTabs.add("Mouse", mouseScrollPanel);

            mouseControlPanel.add(createHeaderLabel("Mouse left click w/o overlays:"));

            showCatalogsButton = new JRadioButton("Show catalog entries for object", true);
            mouseControlPanel.add(showCatalogsButton);

            JRadioButton recenterImagesButton = new JRadioButton("Recenter images on object", false);
            mouseControlPanel.add(recenterImagesButton);

            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(showCatalogsButton);
            buttonGroup.add(recenterImagesButton);

            mouseControlPanel.add(createHeaderLabel("Mouse wheel click:"));

            mouseControlPanel.add(new JLabel("Select images to display:"));

            allSkyImages = new JCheckBox("DSS 1Red, 1Blue, 2Red, 2Blue, 2IR", false);
            mouseControlPanel.add(allSkyImages);
            allSkyImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            twoMassImages = new JCheckBox("2MASS J, H & K bands", false);
            mouseControlPanel.add(twoMassImages);
            twoMassImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            sloanImages = new JCheckBox("SDSS u, g, r, i & z bands", false);
            mouseControlPanel.add(sloanImages);
            sloanImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            spitzerImages = new JCheckBox("Spitzer CH1, CH2, CH3, CH4, MIPS24", false);
            mouseControlPanel.add(spitzerImages);
            spitzerImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            allwiseImages = new JCheckBox("AllWISE W1, W2, W3 & W4 bands", true);
            mouseControlPanel.add(allwiseImages);
            allwiseImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            ps1Images = new JCheckBox("Pan-STARRS g, r, i, z & y bands", false);
            mouseControlPanel.add(ps1Images);
            ps1Images.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            decalsImages = new JCheckBox("DESI LS g, r & z bands", false);
            mouseControlPanel.add(decalsImages);
            decalsImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            staticTimeSeries = new JCheckBox("Time series - static", false);
            mouseControlPanel.add(staticTimeSeries);
            staticTimeSeries.addActionListener((ActionEvent evt) -> {
                if (staticTimeSeries.isSelected() || animatedTimeSeries.isSelected()) {
                    allSkyImages.setSelected(false);
                    twoMassImages.setSelected(false);
                    sloanImages.setSelected(false);
                    spitzerImages.setSelected(false);
                    allwiseImages.setSelected(false);
                    ps1Images.setSelected(false);
                    decalsImages.setSelected(false);
                    animatedTimeSeries.setSelected(false);
                }
                createDataSheet.setSelected(false);
            });

            animatedTimeSeries = new JCheckBox("Time series - animated", false);
            mouseControlPanel.add(animatedTimeSeries);
            animatedTimeSeries.addActionListener((ActionEvent evt) -> {
                if (animatedTimeSeries.isSelected()) {
                    allSkyImages.setSelected(true);
                    twoMassImages.setSelected(true);
                    sloanImages.setSelected(true);
                    spitzerImages.setSelected(true);
                    allwiseImages.setSelected(true);
                    ps1Images.setSelected(true);
                    decalsImages.setSelected(true);
                    staticTimeSeries.setSelected(false);
                } else {
                    allSkyImages.setSelected(false);
                    twoMassImages.setSelected(false);
                    sloanImages.setSelected(false);
                    spitzerImages.setSelected(false);
                    allwiseImages.setSelected(false);
                    ps1Images.setSelected(false);
                    decalsImages.setSelected(false);
                }
                createDataSheet.setSelected(false);
            });

            createDataSheet = new JCheckBox("Object info sheet", false);
            mouseControlPanel.add(createDataSheet);
            createDataSheet.addActionListener((ActionEvent evt) -> {
                if (createDataSheet.isSelected()) {
                    setImageViewer(this);
                    allSkyImages.setSelected(false);
                    twoMassImages.setSelected(false);
                    sloanImages.setSelected(false);
                    spitzerImages.setSelected(false);
                    allwiseImages.setSelected(false);
                    ps1Images.setSelected(false);
                    decalsImages.setSelected(false);
                    staticTimeSeries.setSelected(false);
                    animatedTimeSeries.setSelected(false);
                }
            });

            changeFovLabel = new JLabel(html(String.format(CHANGE_FOV_TEXT, fieldOfView)));
            mouseControlPanel.add(changeFovLabel);
            changeFovLabel.setToolTipText("Spin wheel on flipbook images to change the size of the field of view");

            mouseControlPanel.add(createHeaderLabel("Mouse right click:"));

            mouseControlPanel.add(new JLabel("Show object in a different field of view"));

            JPanel differentSizePanel = new JPanel(new GridLayout(1, 2));
            mouseControlPanel.add(differentSizePanel);
            differentSizePanel.add(new JLabel("Enter FoV (arcsec):"));
            differentSizeField = new JTextField(String.valueOf(DIFFERENT_SIZE));
            differentSizePanel.add(differentSizeField);

            mouseControlPanel.add(new JLabel());

            drawCrosshairs = createHeaderBox(html("Draw crosshairs: (*)"));
            mouseControlPanel.add(drawCrosshairs);
            drawCrosshairs.setToolTipText(html(""
                    + "Tick the check box!" + LINE_BREAK
                    + "Push mouse wheel to draw a crosshair on a specific location." + LINE_BREAK
                    + "Spin mouse wheel to change the crosshair's size." + LINE_BREAK
                    + "Wheel-click the crosshair's center to delete it." + LINE_BREAK
                    + "The crosshair's coordinates appear in the text box below.")
            );
            drawCrosshairs.addActionListener((ActionEvent evt) -> {
                if (!drawCrosshairs.isSelected()) {
                    crosshairs.clear();
                    crosshairCoords.setText("");
                }
            });

            crosshairCoords = new JTextArea();
            mouseControlPanel.add(new JScrollPane(crosshairCoords));
            crosshairCoords.setBackground(new JLabel().getBackground());

            mouseControlPanel.add(new JLabel(html("(*) Shows a tooltip when hovered")));

            //=====================
            // Tab: Player controls
            //=====================
            JPanel playerControlPanel = new JPanel(new GridLayout(rows, 1));
            playerControlPanel.setPreferredSize(new Dimension(controlPanelWidth - 20, controlPanelHeight));
            playerControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JScrollPane playerScrollPanel = new JScrollPane(playerControlPanel);
            playerScrollPanel.setPreferredSize(new Dimension(controlPanelWidth, 50));
            playerScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            controlTabs.add("Player", playerScrollPanel);

            playerControlPanel.add(createHeaderLabel("Image player controls", JLabel.CENTER));

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
                    showInfoDialog(baseFrame, "You're about to cross the North Celestial Pole." + LINE_SEP + "If you want to move on in the current direction, use the 'Move down' button next!");
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
                NumberPair coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec), new NumberPair(distance, 0));
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
                NumberPair coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec), new NumberPair(-distance, 0));
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
                    showInfoDialog(baseFrame, "You're about to cross the South Celestial Pole." + LINE_SEP + "If you want to move on in the current direction, use the 'Move up' button next!");
                }
                coordsField.setText(roundTo7DecNZLZ(targetRa) + " " + roundTo7DecNZLZ(newDec));
                createFlipbook();
            });

            playerControlPanel.add(new JLabel());

            JButton rotateButton = new JButton(String.format("Rotate by 90° clockwise: %d°", quadrantCount * 90));
            playerControlPanel.add(rotateButton);
            rotateButton.addActionListener((ActionEvent evt) -> {
                quadrantCount++;
                if (quadrantCount > 3) {
                    quadrantCount = 0;
                }
                rotateButton.setText(String.format("Rotate by 90° clockwise: %d°", quadrantCount * 90));
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
                } catch (Exception ex) {
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
                        BufferedImage[] imageSet = new BufferedImage[flipbook.length];
                        int i = 0;
                        for (FlipbookComponent component : flipbook) {
                            imageSet[i++] = addCrosshairs(processImage(component), component);
                        }
                        if (imageSet.length > 0) {
                            GifSequencer sequencer = new GifSequencer();
                            sequencer.generateFromBI(imageSet, file, speed / 10, true);
                        }
                    }
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            timer = new Timer(speed, (ActionEvent e) -> {
                try {
                    if (flipbook == null) {
                        return;
                    }
                    if (imageNumber < 0) {
                        imageNumber = flipbook.length - 1;
                    }
                    if (imageNumber > flipbook.length - 1) {
                        imageNumber = 0;
                    }
                    staticView.setSelected(false);

                    FlipbookComponent component = flipbook[imageNumber];
                    wiseImage = addCrosshairs(component.getImage(), component);
                    ImageIcon icon = new ImageIcon(wiseImage);
                    JLabel imageLabel = new JLabel(icon);
                    if (borderFirst.isSelected() && component.isFirstEpoch()) {
                        imageLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                    } else {
                        imageLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                    }

                    imagePanel.removeAll();
                    imagePanel.add(new JLabel(" " + component.getTitle()));
                    imagePanel.add(imageLabel);

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
                    addMagnifiedImage("WISE", wiseImage, upperLeftX, upperLeftY, width, height);

                    // Display DECaLS images
                    JLabel decalsLabel = null;
                    if (processedDecalsImage != null) {
                        // Create and display magnified DECaLS image
                        if (!imageCutOff) {
                            addMagnifiedImage("DESI LS", processedDecalsImage, upperLeftX, upperLeftY, width, height);
                        }

                        // Display regular DECaLS image
                        imagePanel.add(new JLabel(" DESI LS"));
                        decalsLabel = new JLabel(new ImageIcon(processedDecalsImage));
                        decalsLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
                        imagePanel.add(decalsLabel);
                    }

                    // Display Pan-STARRS images
                    JLabel ps1Label = null;
                    if (processedPs1Image != null) {
                        // Create and display magnified Pan-STARRS image
                        if (!imageCutOff) {
                            addMagnifiedImage("Pan-STARRS", processedPs1Image, upperLeftX, upperLeftY, width, height);
                        }

                        // Display regular Pan-STARRS image
                        imagePanel.add(new JLabel(" Pan-STARRS"));
                        ps1Label = new JLabel(new ImageIcon(processedPs1Image));
                        ps1Label.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
                        imagePanel.add(ps1Label);
                    }

                    // Display UKIDSS images
                    JLabel ukidssLabel = null;
                    if (processedUkidssImage != null) {
                        // Create and display magnified UKIDSS image
                        if (!imageCutOff) {
                            addMagnifiedImage("UKIDSS", processedUkidssImage, upperLeftX, upperLeftY, width, height);
                        }

                        // Display regular UKIDSS image
                        imagePanel.add(new JLabel(" UKIDSS"));
                        ukidssLabel = new JLabel(new ImageIcon(processedUkidssImage));
                        ukidssLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
                        imagePanel.add(ukidssLabel);
                    }

                    // Display VHS images
                    JLabel vhsLabel = null;
                    if (processedVhsImage != null) {
                        // Create and display magnified VHS image
                        if (!imageCutOff) {
                            addMagnifiedImage("VHS", processedVhsImage, upperLeftX, upperLeftY, width, height);
                        }

                        // Display regular VHS image
                        imagePanel.add(new JLabel(" VHS"));
                        vhsLabel = new JLabel(new ImageIcon(processedVhsImage));
                        vhsLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
                        imagePanel.add(vhsLabel);
                    }

                    // Display SDSS images
                    if (processedSdssImage != null) {
                        // Create and display magnified SDSS image
                        if (!imageCutOff) {
                            addMagnifiedImage("SDSS", processedSdssImage, upperLeftX, upperLeftY, width, height);
                        }

                        // Display regular SDSS image
                        imagePanel.add(new JLabel(" SDSS"));
                        JLabel sdssLabel = new JLabel(new ImageIcon(processedSdssImage));
                        sdssLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
                        imagePanel.add(sdssLabel);
                    }

                    // Display DSS images
                    if (processedDssImage != null) {
                        // Create and display magnified DSS image
                        if (!imageCutOff) {
                            addMagnifiedImage("DSS", processedDssImage, upperLeftX, upperLeftY, width, height);
                        }

                        // Display regular DSS image
                        imagePanel.add(new JLabel(" DSS"));
                        JLabel dssLabel = new JLabel(new ImageIcon(processedDssImage));
                        dssLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
                        imagePanel.add(dssLabel);
                    }

                    baseFrame.repaint();
                    imageNumber++;

                    imageLabel.addMouseListener(new MouseListener() {
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
                            switch (evt.getButton()) {
                                case MouseEvent.BUTTON3:
                                    CompletableFuture.supplyAsync(() -> openNewImageViewer(newRa, newDec));
                                    break;
                                case MouseEvent.BUTTON2:
                                    if (drawCrosshairs.isSelected()) {
                                        double crosshairX = mouseX * 1.0 / zoom;
                                        double crosshairY = mouseY * 1.0 / zoom;
                                        double radius = 0.01;
                                        boolean removed = false;
                                        ListIterator<NumberPair> iter = crosshairs.listIterator();
                                        while (iter.hasNext()) {
                                            NumberPair pixelCoords = iter.next();
                                            if (pixelCoords.getX() > crosshairX - radius && pixelCoords.getX() < crosshairX + radius
                                                    && pixelCoords.getY() > crosshairY - radius && pixelCoords.getY() < crosshairY + radius) {
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
                                            NumberPair c = toWorldCoordinates(
                                                    (int) round(crosshair.getX() * zoom),
                                                    (int) round(crosshair.getY() * zoom)
                                            );
                                            sb.append(i + 1).append(". ");
                                            sb.append(roundTo7Dec(c.getX()));
                                            sb.append(" ");
                                            sb.append(roundTo7Dec(c.getY()));
                                            sb.append(LINE_SEP_TEXT_AREA);
                                        }
                                        crosshairCoords.setText(sb.toString());
                                    } else {
                                        if (createDataSheet.isSelected()) {
                                            CompletableFuture.supplyAsync(() -> new InfoSheet(newRa, newDec, fieldOfView, getImageViewer()).create(baseFrame));
                                        } else if (animatedTimeSeries.isSelected()) {
                                            if (imageCount == 0) {
                                                displayAnimatedTimeSeries(newRa, newDec, fieldOfView);
                                            }
                                        } else {
                                            CompletableFuture.supplyAsync(() -> {
                                                int numberOfPanels = 0;
                                                if (allSkyImages.isSelected()) {
                                                    numberOfPanels++;
                                                }
                                                if (twoMassImages.isSelected()) {
                                                    numberOfPanels++;
                                                }
                                                if (sloanImages.isSelected()) {
                                                    numberOfPanels++;
                                                }
                                                if (spitzerImages.isSelected()) {
                                                    numberOfPanels++;
                                                }
                                                if (allwiseImages.isSelected()) {
                                                    numberOfPanels++;
                                                }
                                                if (ps1Images.isSelected()) {
                                                    numberOfPanels++;
                                                }
                                                if (decalsImages.isSelected()) {
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
                                                    verticalSpacing = PANEL_HEIGHT - (totalPanelHeight - screenHeight) / (numberOfPanels);
                                                } else {
                                                    verticalSpacing = PANEL_HEIGHT;
                                                }
                                                Counter counter = new Counter(verticalSpacing);
                                                if (allSkyImages.isSelected()) {
                                                    displayDssImages(newRa, newDec, fieldOfView, counter);
                                                }
                                                if (twoMassImages.isSelected()) {
                                                    display2MassImages(newRa, newDec, fieldOfView, counter);
                                                }
                                                if (sloanImages.isSelected()) {
                                                    displaySdssImages(newRa, newDec, fieldOfView, counter);
                                                }
                                                if (spitzerImages.isSelected()) {
                                                    displaySpitzerImages(newRa, newDec, fieldOfView, counter);
                                                }
                                                if (allwiseImages.isSelected()) {
                                                    displayAllwiseImages(newRa, newDec, fieldOfView, counter);
                                                }
                                                if (ps1Images.isSelected()) {
                                                    displayPs1Images(newRa, newDec, fieldOfView, counter);
                                                }
                                                if (decalsImages.isSelected()) {
                                                    displayDecalsImages(targetRa, targetDec, fieldOfView, counter);
                                                }
                                                if (staticTimeSeries.isSelected()) {
                                                    displayStaticTimeSeries(newRa, newDec, fieldOfView, counter);
                                                }
                                                return null;
                                            });
                                        }
                                    }
                                    break;
                                default:
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
                                    if (gaiaWDOverlay.isSelected() && gaiaWDEntries != null) {
                                        showCatalogInfo(gaiaWDEntries, mouseX, mouseY, JColor.PURPLE.val);
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
                                    if (ssoOverlay.isSelected() && ssoEntries != null) {
                                        showCatalogInfo(ssoEntries, mouseX, mouseY, Color.BLUE);
                                        count++;
                                    }
                                    if (useCustomOverlays.isSelected()) {
                                        for (CustomOverlay customOverlay : customOverlays.values()) {
                                            if (customOverlay.getCheckBox().isSelected()) {
                                                showCatalogInfo(customOverlay.getCatalogEntries(), mouseX, mouseY, customOverlay.getColor());
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
                                    break;

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

                    imageLabel.addMouseWheelListener((MouseWheelEvent evt) -> {
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
                            changeFovLabel.setText(html(String.format(CHANGE_FOV_TEXT, fieldOfView)));
                        }
                    });

                    if (ps1Label != null) {
                        ps1Label.addMouseListener(new MouseListener() {
                            @Override
                            public void mousePressed(MouseEvent evt) {
                                try {
                                    Desktop.getDesktop().browse(new URI(getPanstarrsUrl(targetRa, targetDec, fieldOfView, FileType.STACK)));
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

                    if (decalsLabel != null) {
                        decalsLabel.addMouseListener(new MouseListener() {
                            @Override
                            public void mousePressed(MouseEvent evt) {
                                try {
                                    Desktop.getDesktop().browse(new URI(getLegacySkyViewerUrl(targetRa, targetDec, CURRENT_DESI_DR)));
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
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME) && flipbook != null) {
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
                    if (flipbook != null) {
                        timer.stop();
                    }
                }

                @Override
                public void windowActivated(WindowEvent e) {
                    if (flipbook != null && !staticView.isSelected() && !hasException && !timerStopped) {
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

            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK), "keyActionForAltM");
            aMap.put("keyActionForAltM", keyActionForAltM);

            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK), "keyActionForAltT");
            aMap.put("keyActionForAltT", keyActionForAltT);

            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK), "keyActionForAltE");
            aMap.put("keyActionForAltE", keyActionForAltE);

            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.ALT_MASK), "keyActionForAltW");
            aMap.put("keyActionForAltW", keyActionForAltW);

            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK), "keyActionForCtrlAltG");
            aMap.put("keyActionForCtrlAltG", keyActionForCtrlAltG);

            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK), "keyActionForCtrlAltN");
            aMap.put("keyActionForCtrlAltN", keyActionForCtrlAltN);

            iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK), "keyActionForCtrlAltC");
            aMap.put("keyActionForCtrlAltC", keyActionForCtrlAltC);

            tabbedPane.addTab(TAB_NAME, mainPanel);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
            hasException = true;
        }
    }

    private void addMagnifiedImage(String imageLabel, BufferedImage image, int upperLeftX, int upperLeftY, int width, int height) {
        try {
            rightPanel.add(new JLabel(imageLabel));
            BufferedImage magnifiedDecalsImage = image.getSubimage(upperLeftX, upperLeftY, width, height);
            magnifiedDecalsImage = zoomImage(magnifiedDecalsImage, 200);
            rightPanel.add(new JLabel(new ImageIcon(magnifiedDecalsImage)));
        } catch (RasterFormatException ex) {
            writeErrorLog(ex);
        }
    }

    private boolean isCatalogOverlaySelected() {
        int count = 0;
        if (simbadOverlay.isSelected()) {
            count++;
        }
        if (allWiseOverlay.isSelected()) {
            count++;
        }
        if (catWiseOverlay.isSelected()) {
            count++;
        }
        if (unWiseOverlay.isSelected()) {
            count++;
        }
        if (gaiaOverlay.isSelected()) {
            count++;
        }
        if (gaiaDR3Overlay.isSelected()) {
            count++;
        }
        if (noirlabOverlay.isSelected()) {
            count++;
        }
        if (panStarrsOverlay.isSelected()) {
            count++;
        }
        if (sdssOverlay.isSelected()) {
            count++;
        }
        if (vhsOverlay.isSelected()) {
            count++;
        }
        if (gaiaWDOverlay.isSelected()) {
            count++;
        }
        if (twoMassOverlay.isSelected()) {
            count++;
        }
        if (tessOverlay.isSelected()) {
            count++;
        }
        if (desOverlay.isSelected()) {
            count++;
        }
        if (gaiaProperMotion.isSelected()) {
            count++;
        }
        if (gaiaDR3ProperMotion.isSelected()) {
            count++;
        }
        if (noirlabProperMotion.isSelected()) {
            count++;
        }
        if (catWiseProperMotion.isSelected()) {
            count++;
        }
        return count > 0;
    }

    private void resetEpochSlider(int numberOfEpochs) {
        epochLabel.setText(String.format(EPOCH_LABEL, numberOfEpochs));
        ChangeListener changeListener = epochSlider.getChangeListeners()[0];
        epochSlider.removeChangeListener(changeListener);
        epochSlider.setMaximum(numberOfEpochs);
        epochSlider.setValue(numberOfEpochs);
        epochSlider.addChangeListener(changeListener);
        selectedEpochs = numberOfEpochs;
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
                if (decalsCutouts.isSelected()) {
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

            if (size != previousSize || targetRa != previousRa || targetDec != previousDec) {
                loadImages = true;
                bandW1Loaded = false;
                bandW2Loaded = false;
                allEpochsW1Loaded = false;
                allEpochsW2Loaded = false;
                moreImagesAvailable = false;
                flipbookComplete = false;
                imagesW1 = new HashMap<>();
                imagesW2 = new HashMap<>();
                images = new HashMap<>();
                crosshairs = new ArrayList<>();
                crosshairCoords.setText("");
                hasException = false;
                naxis1 = naxis2 = size;
                pointerX = pointerY = 0;
                windowShift = 0;
                epochCountW1 = 0;
                epochCountW2 = 0;
                imageCutOff = false;
                initCatalogEntries();
                decalsImage = null;
                processedDecalsImage = null;
                if (legacyImages) {
                    CompletableFuture.supplyAsync(() -> {
                        decalsImage = fetchDecalsImage(targetRa, targetDec, size);
                        processedDecalsImage = zoomImage(rotateImage(decalsImage, quadrantCount), zoom);
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
                ukidssImage = null;
                processedUkidssImage = null;
                if (ukidssImages) {
                    CompletableFuture.supplyAsync(() -> {
                        ukidssImage = fetchUkidssImage(targetRa, targetDec, size);
                        processedUkidssImage = zoomImage(rotateImage(ukidssImage, quadrantCount), zoom);
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
                zooniversePanel1.removeAll();
                zooniversePanel2.removeAll();
                List<JLabel> subjects = getNearestZooniverseSubjects(targetRa, targetDec);
                int numberOfSubjects = subjects.size();
                if (numberOfSubjects == 0) {
                    zooniversePanel1.add(new JLabel("None"));
                } else {
                    for (int i = 0; i < 4 && i < numberOfSubjects; i++) {
                        zooniversePanel1.add(subjects.get(i));
                    }
                    for (int i = 4; i < 8 && i < numberOfSubjects; i++) {
                        zooniversePanel2.add(subjects.get(i));
                    }
                }
                if (wiseviewCutouts.isSelected()) {
                    try {
                        InputStream stream = getImageData(1, numberOfEpochs + 1);
                        stream.close();
                        moreImagesAvailable = true;
                    } catch (IOException e) {
                    }
                }
            }
            previousSize = size;
            previousRa = targetRa;
            previousDec = targetDec;
            imageNumber = 0;

            if (loadImages || reloadImages) {
                int totalEpochs = selectedEpochs * 2;
                if (unwiseCutouts.isSelected()) {
                    epochCount = totalEpochs;
                }
                requestedEpochs = new ArrayList<>();
                if (moreImagesAvailable) {
                    for (int i = 0; i < 100; i++) {
                        requestedEpochs.add(i);
                    }
                } else {
                    if (Epoch.isFirstLast(epoch)) {
                        if (reloadImages) {
                            imagesW1.clear();
                            imagesW2.clear();
                        }
                        requestedEpochs.add(0);
                        requestedEpochs.add(1);
                        requestedEpochs.add(totalEpochs - 2);
                        requestedEpochs.add(totalEpochs - 1);
                    } else {
                        for (int i = 0; i < totalEpochs; i++) {
                            requestedEpochs.add(i);
                        }
                    }
                }
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
                switch (wiseBand) {
                    case W1:
                        downloadRequestedEpochs(WiseBand.W1.val, requestedEpochs, imagesW1);
                        epochCountW1 = epochCount;
                        break;
                    case W2:
                        downloadRequestedEpochs(WiseBand.W2.val, requestedEpochs, imagesW2);
                        epochCountW2 = epochCount;
                        break;
                    case W1W2:
                        downloadRequestedEpochs(WiseBand.W1.val, requestedEpochs, imagesW1);
                        epochCountW1 = epochCount;
                        downloadRequestedEpochs(WiseBand.W2.val, requestedEpochs, imagesW2);
                        epochCountW2 = epochCount;
                        break;
                }
                writeLogEntry("Ready.");
                if (asyncDownloads) {
                    downloadLog.setCaretPosition(0);
                }
                if (images.size() < 2) {
                    showInfoDialog(baseFrame, "No images found for the given coordinates.");
                    hasException = true;
                    return false;
                }
                if (epochCountW1 > 0 && epochCountW2 > 0) {
                    epochCount = min(epochCountW1, epochCountW2);
                }
                epochCount = epochCount % 2 == 0 ? epochCount : epochCount - 1;
                if (!Epoch.isFirstLast(epoch) || moreImagesAvailable) {
                    epochCount = totalEpochs < epochCount ? totalEpochs : epochCount;
                }
            }
            if (WiseBand.W1.equals(wiseBand) || WiseBand.W1W2.equals(wiseBand)) {
                bandW1Loaded = true;
                if (!Epoch.isFirstLast(epoch)) {
                    allEpochsW1Loaded = true;
                }
            }
            if (WiseBand.W2.equals(wiseBand) || WiseBand.W1W2.equals(wiseBand)) {
                bandW2Loaded = true;
                if (!Epoch.isFirstLast(epoch)) {
                    allEpochsW2Loaded = true;
                }
            }
            loadImages = false;
            reloadImages = false;

            Fits fits;
            switch (epoch) {
                case ALL:
                    flipbook = new FlipbookComponent[epochCount];
                    for (int i = 0; i < epochCount; i++) {
                        flipbook[i] = new FlipbookComponent(wiseBand.val, i, i > 1 ? i + EPOCH_GAP : i);
                    }
                    break;
                case ASCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 0; i < epochCount; i += 2) {
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i, i > 0 ? i + EPOCH_GAP : i);
                    }
                    break;
                case DESCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 1; i < epochCount; i += 2) {
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i, i > 1 ? i + EPOCH_GAP : i);
                    }
                    break;
                case ASCENDING_DESCENDING:
                    flipbook = new FlipbookComponent[epochCount];
                    int k = 0;
                    for (int i = 0; i < epochCount; i += 2) {
                        flipbook[k] = new FlipbookComponent(wiseBand.val, i, i > 0 ? i + EPOCH_GAP : i);
                        k++;
                    }
                    for (int i = 1; i < epochCount; i += 2) {
                        flipbook[k] = new FlipbookComponent(wiseBand.val, i, i > 1 ? i + EPOCH_GAP : i);
                        k++;
                    }
                    break;
                case ASCENDING_DESCENDING_SUBTRACTED:
                    flipbook = new FlipbookComponent[epochCount - 4];
                    k = 0;
                    for (int i = 2; i < epochCount - 2; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W1.val, 0);
                            addImage(WiseBand.W1.val, 600 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W2.val, 0);
                            addImage(WiseBand.W2.val, 600 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W1.val, i, WiseBand.W1.val, i + 2);
                            addImage(WiseBand.W1.val, 700 + i, takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W2.val, i, WiseBand.W2.val, i + 2);
                            addImage(WiseBand.W2.val, 700 + i, takeAverage(fits, 2));
                        }
                        differenceImaging(600 + i, 700 + i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 700 + i, true, i > 2 ? i + EPOCH_GAP + 2 : i - 2);
                        k++;
                    }
                    for (int i = 3; i < epochCount - 2; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W1.val, 1);
                            addImage(WiseBand.W1.val, 600 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W2.val, 1);
                            addImage(WiseBand.W2.val, 600 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W1.val, i, WiseBand.W1.val, i + 2);
                            addImage(WiseBand.W1.val, 700 + i, takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W2.val, i, WiseBand.W2.val, i + 2);
                            addImage(WiseBand.W2.val, 700 + i, takeAverage(fits, 2));
                        }
                        differenceImaging(600 + i, 700 + i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 600 + i, true, i > 3 ? i + EPOCH_GAP + 2 : i - 2);
                        k++;
                    }
                    break;
                case YEAR:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 0; i < epochCount; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W1.val, i, WiseBand.W1.val, i + 1);
                            addImage(WiseBand.W1.val, 501 + (i / 2), takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W2.val, i, WiseBand.W2.val, i + 1);
                            addImage(WiseBand.W2.val, 501 + (i / 2), takeAverage(fits, 2));
                        }
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, 501 + (i / 2), true, i > 1 ? i + EPOCH_GAP + 1 : i);
                    }
                    break;
                case FIRST_LAST:
                case FIRST_LAST_SUBTRACTED:
                    flipbook = new FlipbookComponent[2];
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W1.val, 0, WiseBand.W1.val, 1);
                        addImage(WiseBand.W1.val, 100, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W2.val, 0, WiseBand.W2.val, 1);
                        addImage(WiseBand.W2.val, 100, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W1.val, epochCount - 2, WiseBand.W1.val, epochCount - 1);
                        addImage(WiseBand.W1.val, 200, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W2.val, epochCount - 2, WiseBand.W2.val, epochCount - 1);
                        addImage(WiseBand.W2.val, 200, takeAverage(fits, 2));
                    }
                    if (epoch.equals(Epoch.FIRST_LAST_SUBTRACTED)) {
                        differenceImaging(100, 200);
                    }
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 100, true, 0);
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 200, true, selectedEpochs * 2 + EPOCH_GAP - 1);
                    break;
                case FIRST_LAST_PARALLAX:
                    flipbook = new FlipbookComponent[2];
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W1.val, 0, WiseBand.W1.val, epochCount - 2);
                        addImage(WiseBand.W1.val, 300, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W2.val, 0, WiseBand.W2.val, epochCount - 2);
                        addImage(WiseBand.W2.val, 300, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W1.val, 1, WiseBand.W1.val, epochCount - 1);
                        addImage(WiseBand.W1.val, 400, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W2.val, 1, WiseBand.W2.val, epochCount - 1);
                        addImage(WiseBand.W2.val, 400, takeAverage(fits, 2));
                    }
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 300, true, 0);
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 400, true, selectedEpochs * 2 + EPOCH_GAP - 1);
                    break;
            }

            List<Double> minValues = new ArrayList<>();
            List<Double> maxValues = new ArrayList<>();
            for (FlipbookComponent component : flipbook) {
                NumberPair refVal = getRefValues(component);
                minValues.add(refVal.getX());
                maxValues.add(refVal.getY());
            }
            int count = flipbook.length;
            minValues.sort(Comparator.naturalOrder());
            maxValues.sort(Comparator.naturalOrder());
            double minVal = minValues.get(count - 1);
            double maxVal = maxValues.get(count - 1);
            setMinMaxVal((int) minVal, (int) maxVal);

            flipbookComplete = true;
            processImages();
            timer.restart();
            timerStopped = false;
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
            hasException = true;
        } finally {
            if (waitCursor) {
                baseFrame.setCursor(Cursor.getDefaultCursor());
                coordsField.setCursor(Cursor.getDefaultCursor());
                sizeField.setCursor(Cursor.getDefaultCursor());
            }
        }
        return true;
    }

    private NumberPair getRefValues(FlipbookComponent component) throws Exception {
        Fits fits;
        int divisor = 0;
        int minValW1 = 0;
        int maxValW1 = 0;
        fits = getImage(WiseBand.W1.val, component.getEpoch());
        if (fits != null) {
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values = (float[][]) imageData.getData();
            NumberPair refValues = determineRefValues(values);
            minValW1 = (int) refValues.getX();
            maxValW1 = (int) refValues.getY();
            divisor++;
        }
        int minValW2 = 0;
        int maxValW2 = 0;
        fits = getImage(WiseBand.W2.val, component.getEpoch());
        if (fits != null) {
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values = (float[][]) imageData.getData();
            NumberPair refValues = determineRefValues(values);
            minValW2 = (int) refValues.getX();
            maxValW2 = (int) refValues.getY();
            divisor++;
        }
        int minVal = (minValW1 + minValW2) / divisor;
        int maxVal = (maxValW1 + maxValW2) / divisor;
        return new NumberPair(minVal, maxVal);
    }

    private NumberPair getNewPosition(double ra, double dec, double pmRa, double pmDec, double numberOfYears, int totalEpochs) {
        NumberPair fromCoords = calculatePositionFromProperMotion(new NumberPair(ra, dec), new NumberPair(-numberOfYears * pmRa / DEG_MAS, -numberOfYears * pmDec / DEG_MAS));
        double fromRa = fromCoords.getX();
        double fromDec = fromCoords.getY();

        NumberPair toCoords = calculatePositionFromProperMotion(new NumberPair(fromRa, fromDec), new NumberPair(totalEpochs * (pmRa / 2) / DEG_MAS, totalEpochs * (pmDec / 2) / DEG_MAS));
        double toRa = toCoords.getX();
        double toDec = toCoords.getY();

        return new NumberPair(toRa, toDec);
    }

    private void processImages() {
        if (decalsImage != null) {
            processedDecalsImage = zoomImage(rotateImage(decalsImage, quadrantCount), zoom);
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
        if (flipbook == null || !flipbookComplete) {
            return;
        }
        timer.stop();
        for (FlipbookComponent component : flipbook) {
            component.setEpochCount(selectedEpochs);
            component.setImage(processImage(component));
        }
        timer.restart();
    }

    public void initCatalogEntries() {
        simbadEntries = null;
        gaiaEntries = null;
        gaiaDR3Entries = null;
        gaiaTpmEntries = null;
        gaiaDR3TpmEntries = null;
        allWiseEntries = null;
        catWiseEntries = null;
        catWiseRejectEntries = null;
        catWiseTpmEntries = null;
        unWiseEntries = null;
        panStarrsEntries = null;
        sdssEntries = null;
        twoMassEntries = null;
        vhsEntries = null;
        gaiaWDEntries = null;
        noirlabEntries = null;
        noirlabTpmEntries = null;
        tessEntries = null;
        desEntries = null;
        ssoEntries = null;
        if (useCustomOverlays.isSelected()) {
            customOverlays.values().forEach((customOverlay) -> {
                customOverlay.setCatalogEntries(null);
            });
        }
    }

    private void createStaticBook() {
        timer.stop();
        JPanel grid = new JPanel(new GridLayout(4, 4));
        for (FlipbookComponent component : flipbook) {
            component.setEpochCount(selectedEpochs);
            BufferedImage image = addCrosshairs(processImage(component), component);
            JScrollPane scrollPanel = new JScrollPane(new JLabel(new ImageIcon(image)));
            scrollPanel.setBorder(createEtchedBorder(component.getTitle()));
            grid.add(scrollPanel);
        }
        if (decalsImage != null) {
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(zoomImage(rotateImage(decalsImage, quadrantCount), zoom))));
            pane.setBorder(createEtchedBorder("DESI LS"));
            grid.add(pane);
        }
        if (ps1Image != null) {
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(zoomImage(rotateImage(ps1Image, quadrantCount), zoom))));
            pane.setBorder(createEtchedBorder("Pan-STARRS"));
            grid.add(pane);
        }
        if (ukidssImage != null) {
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(zoomImage(rotateImage(ukidssImage, quadrantCount), zoom))));
            pane.setBorder(createEtchedBorder("UKIDSS"));
            grid.add(pane);
        }
        if (vhsImage != null) {
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(zoomImage(rotateImage(vhsImage, quadrantCount), zoom))));
            pane.setBorder(createEtchedBorder("VHS"));
            grid.add(pane);
        }
        if (sdssImage != null) {
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(zoomImage(rotateImage(sdssImage, quadrantCount), zoom))));
            pane.setBorder(createEtchedBorder("SDSS"));
            grid.add(pane);
        }
        if (dssImage != null) {
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(zoomImage(rotateImage(dssImage, quadrantCount), zoom))));
            pane.setBorder(createEtchedBorder("DSS"));
            grid.add(pane);
        }
        imagePanel.removeAll();
        imagePanel.setBorder(createEmptyBorder(""));
        imagePanel.add(grid);
        baseFrame.setVisible(true);
    }

    public BufferedImage processImage(FlipbookComponent component) {
        BufferedImage image;
        if (wiseBand.equals(WiseBand.W1W2)) {
            image = createComposite(component.getEpoch());
        } else {
            image = createImage(component.getBand(), component.getEpoch());
        }
        image = zoomImage(image, zoom);
        image = flipImage(image);
        addOverlaysAndPMVectors(image, component.getTotalEpochs());
        return image;
    }

    private BufferedImage addCrosshairs(BufferedImage image, FlipbookComponent component) {
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
                CrossHair drawable = new CrossHair(crosshair.getX() * zoom, crosshair.getY() * zoom, shapeSize * zoom / 100, Color.RED, label);
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

    private void addOverlaysAndPMVectors(BufferedImage image, int totalEpochs) {
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
                    gaiaEntries = fetchCatalogEntries(new GaiaCatalogEntry());
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
                        drawOverlay(image, customOverlay.getCatalogEntries(), customOverlay.getColor(), customOverlay.getShape());
                    }
                }
            });
        }
        if (gaiaProperMotion.isSelected()) {
            if (gaiaTpmEntries == null) {
                gaiaTpmEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> {
                    gaiaTpmEntries = fetchTpmCatalogEntries(new GaiaCatalogEntry());
                    processImages();
                    return null;
                });
            } else {
                drawPMVectors(image, gaiaTpmEntries, Color.CYAN.darker(), totalEpochs);
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
                drawPMVectors(image, gaiaDR3TpmEntries, Color.CYAN.darker(), totalEpochs);
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
                drawPMVectors(image, noirlabTpmEntries, JColor.NAVY.val, totalEpochs);
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
                drawPMVectors(image, catWiseTpmEntries, Color.MAGENTA, totalEpochs);
            }
        }
        if (ghostOverlay.isSelected() || haloOverlay.isSelected() || latentOverlay.isSelected() || spikeOverlay.isSelected()) {
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

    private void downloadRequestedEpochs(int band, List<Integer> requestedEpochs, Map<String, ImageContainer> images) throws Exception {
        if (requestedEpochs == null) {
            writeLogEntry("No images found for band " + band + ".");
            return;
        }
        writeLogEntry("Downloading ...");
        if (decalsCutouts.isSelected()) {
            retrieveDecalsImages(band, images);
        } else {
            for (int i = 0; i < requestedEpochs.size(); i++) {
                int requestedEpoch = requestedEpochs.get(i);
                String imageKey = band + "_" + requestedEpoch;
                ImageContainer container = images.get(imageKey);
                if (container != null) {
                    writeLogEntry("band " + band + " | image " + requestedEpoch + " > already downloaded");
                    continue;
                }
                if (unwiseCutouts.isSelected()) {
                    if (requestedEpoch % 2 > 0) {
                        container = images.get(band + "_" + (requestedEpoch - 1));
                        if (container != null) {
                            LocalDateTime obsDate = container.getDate().plusMonths(6);
                            images.put(imageKey, new ImageContainer(requestedEpoch, obsDate, container.getImage()));
                            writeLogEntry("band " + band + " | image " + requestedEpoch + " | " + obsDate.format(DATE_FORMATTER) + " > downloaded");
                            continue;
                        }
                    }
                }
                Fits fits;
                try {
                    fits = new Fits(getImageData(band, requestedEpoch));
                } catch (IOException ex) {
                    if (requestedEpochs.size() == 4) {
                        writeLogEntry("band " + band + " | image " + requestedEpoch + " > not found, looking for surrogates");
                        downloadRequestedEpochs(band, provideAlternativeEpochs(requestedEpoch, requestedEpochs), images);
                        return;
                    } else {
                        break;
                    }
                }
                ImageHDU hdu;
                try {
                    hdu = (ImageHDU) fits.getHDU(0);
                    fits.close();
                } catch (FitsException ex) {
                    if (requestedEpochs.size() == 4) {
                        writeLogEntry("band " + band + " | image " + requestedEpoch + " > unreadable, looking for surrogates");
                        downloadRequestedEpochs(band, provideAlternativeEpochs(requestedEpoch, requestedEpochs), images);
                        return;
                    } else {
                        writeLogEntry("band " + band + " | image " + requestedEpoch + " > unreadable");
                        continue;
                    }
                }
                Header header = hdu.getHeader();
                double minObsEpoch = header.getDoubleValue("MJDMIN");
                LocalDateTime obsDate;
                if (unwiseCutouts.isSelected()) {
                    int wiseEpoch = requestedEpoch / 2;
                    int year = wiseEpoch == 0 ? 2010 : 2013 + wiseEpoch;
                    obsDate = LocalDateTime.of(year, Month.MARCH, 1, 0, 0);
                } else {
                    obsDate = convertMJDToDateTime(new BigDecimal(Double.toString(minObsEpoch)));
                }
                images.put(imageKey, new ImageContainer(requestedEpoch, obsDate, fits));
                writeLogEntry("band " + band + " | image " + requestedEpoch + " | " + obsDate.format(DATE_FORMATTER) + " > downloaded");
            }
        }
        if (images.isEmpty()) {
            return;
        }
        writeLogEntry("Grouping ...");
        List<ImageContainer> sortedList = images.values().stream()
                .filter(container -> container.getImage() != null)
                .sorted(Comparator.comparing(ImageContainer::getDate))
                .collect(Collectors.toList());
        List<List<ImageContainer>> groupedList = new ArrayList<>();
        List<ImageContainer> group = new ArrayList<>();
        ImageContainer containerSaved = sortedList.get(0);
        LocalDateTime date = containerSaved.getDate();
        int prevYear = date.getYear();
        int prevMonth = date.getMonthValue();
        int prevNode = 1;
        int node1 = 0;
        int node2 = 0;
        boolean nodeChange = false;
        for (ImageContainer container : sortedList) {
            date = container.getDate();
            int year = date.getYear();
            int month = date.getMonthValue();
            int node;
            if (year != prevYear) {
                node = 1;
                nodeChange = false;
            } else if (month - prevMonth > 4 && !nodeChange) {
                node = prevNode == 1 ? 2 : 1;
                nodeChange = true;
            } else {
                node = prevNode;
            }
            if (year == prevYear && node == prevNode) {
                group.add(container);
            } else {
                groupedList.add(group);
                group = new ArrayList<>();
                group.add(container);
            }
            if (year == prevYear) {
                if (node == 1) {
                    node1++;
                }
                if (node == 2) {
                    node2++;
                }
            } else {
                if (!Epoch.isFirstLast(epoch) && (node1 == 0 || node2 == 0)) {
                    writeLogEntry("year " + prevYear + " | node " + prevNode + " > skipped (single node)");
                    groupedList.remove(groupedList.size() - 1);
                }
                node1 = 0;
                node2 = 0;
                if (node == 1) {
                    node1++;
                }
                if (node == 2) {
                    node2++;
                }
            }
            prevYear = year;
            prevMonth = month;
            prevNode = node;
            writeLogEntry("year " + year + " | node " + node);
        }
        if (!Epoch.isFirstLast(epoch) && (node1 == 0 || node2 == 0)) {
            writeLogEntry("year " + prevYear + " | node " + prevNode + " > skipped (single node)");
        } else {
            groupedList.add(group);
        }
        writeLogEntry("Stacking ...");
        epochCount = 0;
        for (List<ImageContainer> imageGroup : groupedList) {
            ImageContainer container = imageGroup.get(0);
            LocalDateTime time = container.getDate();
            int year = time.getYear();
            int month = time.getMonthValue();
            Fits fits = container.getImage();
            extractHeaderInfo(fits);
            int groupSize = imageGroup.size();
            for (int i = 1; i < groupSize; i++) {
                fits = stackImages(fits, imageGroup.get(i).getImage());
            }
            addImage(band, epochCount, groupSize > 1 ? takeAverage(fits, groupSize) : fits);
            writeLogEntry("band " + band + " | image " + epochCount + " | year " + year + " | month " + month);
            epochCount++;
        }
    }

    private void extractHeaderInfo(Fits fits) throws Exception {
        ImageHDU hdu = (ImageHDU) fits.getHDU(0);
        Header header = hdu.getHeader();
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

    private void writeLogEntry(String log) {
        if (asyncDownloads) {
            downloadLog.append(log + LINE_SEP_TEXT_AREA);
        }
        //System.out.println(log);
    }

    private List<Integer> provideAlternativeEpochs(int requestedEpoch, List<Integer> requestedEpochs) {
        if (requestedEpoch == selectedEpochs) {
            return null;
        }
        List<Integer> alternativeEpochs = new ArrayList<>();
        if (requestedEpoch < selectedEpochs) {
            alternativeEpochs.add(requestedEpochs.get(0) + 1);
            alternativeEpochs.add(requestedEpochs.get(1) + 1);
            alternativeEpochs.add(requestedEpochs.get(2));
            alternativeEpochs.add(requestedEpochs.get(3));
        } else {
            alternativeEpochs.add(requestedEpochs.get(0));
            alternativeEpochs.add(requestedEpochs.get(1));
            alternativeEpochs.add(requestedEpochs.get(2) - 1);
            alternativeEpochs.add(requestedEpochs.get(3) - 1);
        }
        return alternativeEpochs;
    }

    private Fits stackImages(Fits fits1, Fits fits2) {
        try {
            ImageHDU imageHDU = (ImageHDU) fits1.getHDU(0);
            ImageData imageData = (ImageData) imageHDU.getData();
            float[][] values1 = (float[][]) imageData.getData();

            imageHDU = (ImageHDU) fits2.getHDU(0);
            imageData = (ImageData) imageHDU.getData();
            float[][] values2 = (float[][]) imageData.getData();

            float[][] addedValues = new float[naxis2][naxis1];
            for (int i = 0; i < naxis2; i++) {
                for (int j = 0; j < naxis1; j++) {
                    try {
                        float value1 = values1[i][j];
                        float value2 = values2[i][j];
                        value1 = value1 == 0 ? value2 : value1;
                        value2 = value2 == 0 ? value1 : value2;
                        addedValues[i][j] = value1 + value2;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                    }
                }
            }

            Fits result = new Fits();
            result.addHDU(FitsFactory.hduFactory(addedValues));
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
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
            String unwiseURL = String.format("http://unwise.me/cutout_fits?version=%s&ra=%f&dec=%f&size=%d&bands=%d&file_img_m=on", unwiseEpoch, targetRa, targetDec, size, band);
            try (InputStream fi = establishHttpConnection(unwiseURL).getInputStream();
                    InputStream bi = new BufferedInputStream(fi);
                    InputStream gzi = new GzipCompressorInputStream(bi);
                    ArchiveInputStream ti = new TarArchiveInputStream(gzi)) {
                ArchiveEntry entry;
                Map<Long, byte[]> entries = new HashMap();
                while ((entry = ti.getNextEntry()) != null) {
                    byte[] buf = new byte[(int) entry.getSize()];
                    IOUtils.readFully(ti, buf);
                    entries.put(entry.getSize(), buf);
                }
                List<Long> sizes = entries.keySet().stream().collect(Collectors.toList());
                sizes.sort(Comparator.reverseOrder());
                long largest = sizes.get(0);
                return new ByteArrayInputStream(entries.get(largest));
            }
        } else {
            String imageUrl = getUserSetting(CUTOUT_SERVICE, CUTOUT_SERVICE_URL) + "?ra=" + targetRa + "&dec=" + targetDec + "&size=" + size + "&band=" + band + "&epoch=" + epoch;
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            return connection.getInputStream();
        }
    }

    private void retrieveDecalsImages(int band, Map<String, ImageContainer> images) throws Exception {
        boolean firstEpochDownloaded = downloadDecalsCutouts(0, band, images, "decals-dr5", 2016);
        if (!firstEpochDownloaded || !Epoch.isFirstLast(epoch)) {
            downloadDecalsCutouts(2, band, images, "decals-dr7", 2018);
            downloadDecalsCutouts(4, band, images, "ls-dr8", 2019);
        }
        downloadDecalsCutouts(6, band, images, "ls-dr9", 2020);
    }

    private boolean downloadDecalsCutouts(int requestedEpoch, int band, Map<String, ImageContainer> images, String survey, int year) throws Exception {
        String imageKey = band + "_" + requestedEpoch;
        ImageContainer container = images.get(imageKey);
        if (container != null) {
            writeLogEntry("band " + band + " | image " + requestedEpoch + " > already downloaded");
            requestedEpoch++;
            writeLogEntry("band " + band + " | image " + requestedEpoch + " > already downloaded");
            requestedEpoch++;
            return true;
        }
        String selectedBand = band == 1 ? "r" : "z";
        String baseUrl = "https://www.legacysurvey.org/viewer/fits-cutout?ra=%f&dec=%f&pixscale=%f&layer=%s&size=%d&bands=%s";
        String imageUrl = String.format(baseUrl, targetRa, targetDec, PIXEL_SCALE_DECAM, survey, size, selectedBand);
        try {
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            Fits fits = new Fits(connection.getInputStream());
            enhanceImage(fits, 1000);
            fits.close();
            LocalDateTime obsDate = LocalDateTime.of(year, Month.MARCH, 1, 0, 0);
            images.put(imageKey, new ImageContainer(requestedEpoch, obsDate, fits));
            writeLogEntry("band " + band + " | image " + requestedEpoch + " | " + survey + " > downloaded");
            requestedEpoch++;
            imageKey = band + "_" + requestedEpoch;
            obsDate = LocalDateTime.of(year, Month.SEPTEMBER, 1, 0, 0);
            images.put(imageKey, new ImageContainer(requestedEpoch, obsDate, fits));
            writeLogEntry("band " + band + " | image " + requestedEpoch + " | " + survey + " > downloaded");
            requestedEpoch++;
            return true;
        } catch (IOException | FitsException ex) {
            return false;
        }
    }

    private BufferedImage createImage(int band, int epoch) {
        try {
            Fits fits = getImage(band, epoch);
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private BufferedImage createComposite(int epoch) {
        try {
            Fits fits = getImage(WiseBand.W1.val, epoch);
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] valuesW1 = (float[][]) imageData.getData();

            fits = getImage(WiseBand.W2.val, epoch);
            hdu = (ImageHDU) fits.getHDU(0);
            imageData = (ImageData) hdu.getData();
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Fits addImages(int band1, int epoch1, int band2, int epoch2) throws Exception {
        Fits fits = getImage(band1, epoch1);
        ImageHDU imageHDU = (ImageHDU) fits.getHDU(0);
        ImageData imageData = (ImageData) imageHDU.getData();
        float[][] values1 = (float[][]) imageData.getData();

        fits = getImage(band2, epoch2);
        imageHDU = (ImageHDU) fits.getHDU(0);
        imageData = (ImageData) imageHDU.getData();
        float[][] values2 = (float[][]) imageData.getData();

        float[][] addedValues = new float[naxis2][naxis1];
        for (int i = 0; i < naxis2; i++) {
            for (int j = 0; j < naxis1; j++) {
                try {
                    float value1 = values1[i][j];
                    float value2 = values2[i][j];
                    value1 = value1 == 0 ? value2 : value1;
                    value2 = value2 == 0 ? value1 : value2;
                    addedValues[i][j] = value1 + value2;
                } catch (ArrayIndexOutOfBoundsException ex) {
                }
            }
        }

        Fits result = new Fits();
        result.addHDU(FitsFactory.hduFactory(addedValues));
        return result;
    }

    private Fits subtractImages(int band1, int epoch1, int band2, int epoch2) throws Exception {
        Fits fits = getImage(band1, epoch1);
        ImageHDU hdu = (ImageHDU) fits.getHDU(0);
        ImageData imageData = (ImageData) hdu.getData();
        float[][] values1 = (float[][]) imageData.getData();

        fits = getImage(band2, epoch2);
        hdu = (ImageHDU) fits.getHDU(0);
        imageData = (ImageData) hdu.getData();
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

        Fits result = new Fits();
        result.addHDU(FitsFactory.hduFactory(subtractedValues));
        return result;
    }

    private Fits takeAverage(Fits fits, int numberOfImages) throws Exception {
        ImageHDU imageHDU = (ImageHDU) fits.getHDU(0);
        ImageData imageData = (ImageData) imageHDU.getData();
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

        Fits result = new Fits();
        result.addHDU(FitsFactory.hduFactory(averagedValues));
        return result;
    }

    private void enhanceImage(Fits fits, int enhanceFactor) throws Exception {
        ImageHDU imageHDU = (ImageHDU) fits.getHDU(0);
        ImageData imageData = (ImageData) imageHDU.getData();
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

    private void differenceImaging(int epoch1, int epoch2) throws Exception {
        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
            Fits fits1 = subtractImages(WiseBand.W1.val, epoch1, WiseBand.W1.val, epoch2);
            Fits fits2 = subtractImages(WiseBand.W1.val, epoch2, WiseBand.W1.val, epoch1);
            addImage(WiseBand.W1.val, epoch1, fits1);
            addImage(WiseBand.W1.val, epoch2, fits2);
        }
        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
            Fits fits1 = subtractImages(WiseBand.W2.val, epoch1, WiseBand.W2.val, epoch2);
            Fits fits2 = subtractImages(WiseBand.W2.val, epoch2, WiseBand.W2.val, epoch1);
            addImage(WiseBand.W2.val, epoch1, fits1);
            addImage(WiseBand.W2.val, epoch2, fits2);
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

    private Fits getImage(int band, int epoch) {
        return images.get(band + "_" + epoch);
    }

    private void addImage(int band, int epoch, Fits fits) {
        images.put(band + "_" + epoch, fits);
    }

    private float processPixel(float value) {
        value = normalize(value, min(minValue, maxValue), max(minValue, maxValue));
        return invertColors.isSelected() ? value : 1 - value;
    }

    private float normalize(float value, float minVal, float maxVal) {
        if (value < minVal) {
            value = minVal;
        }
        if (value > maxVal) {
            value = maxVal;
        }
        float newMinVal = 0, newMaxVal = 1;
        return (value - minVal) * ((newMaxVal - newMinVal) / (maxVal - minVal)) + newMinVal;
    }

    private void setMinMaxVal(int min, int max) {
        minValSlider.setMinimum(min - max);
        minValSlider.setMaximum(max);
        minValSlider.setValue(min);
        maxValSlider.setMinimum(min);
        maxValSlider.setMaximum(max + max);
        maxValSlider.setValue(max);
    }

    private NumberPair determineRefValues(float[][] values) {
        List<Double> data = new ArrayList<>();
        for (float[] row : values) {
            for (float value : row) {
                if (value != Float.POSITIVE_INFINITY && value != Float.NEGATIVE_INFINITY && value != Float.NaN) {
                    data.add((double) value);
                }
            }
        }
        Collections.sort(data);
        List<Double> data1;
        List<Double> data2;
        int dataSize = data.size() - 1;
        data1 = data.subList(0, dataSize / 2);
        if (dataSize % 2 == 0) {
            data2 = data.subList(dataSize / 2, dataSize);
        } else {
            data2 = data.subList(dataSize / 2 + 1, dataSize);
        }
        double lowerBound;
        double upperBound;
        if (AUTO_RANGE.equals(range)) {
            double q1 = determineMedian(data1);
            double q3 = determineMedian(data2);
            double iqr = q3 - q1;
            double fov = toDouble(sizeField.getText());
            double scale = fov > 1000 ? fov / 1000 : 1;
            if (Epoch.isSubtracted(epoch)) {
                lowerBound = q1 - 5 * iqr * scale;
                upperBound = q3 + 5 * iqr * scale;
            } else {
                double minVal = data.get(0);
                double maxVal = data.get(data.size() - 1);
                lowerBound = q1 - iqr * scale;
                lowerBound = minVal < -1000 ? lowerBound : minVal;
                upperBound = q3 + 10 * iqr * scale;
                upperBound = upperBound > maxVal ? maxVal : upperBound;
            }
        } else {
            double percent = Double.valueOf(range);
            if (Epoch.isSubtracted(epoch) || decalsCutouts.isSelected()) {
                Collections.sort(data1, Comparator.reverseOrder());
                int size1 = data1.size() - 1;
                lowerBound = data1.get((int) (size1 * percent / 100));
            } else {
                lowerBound = data.get(0);
            }
            int size2 = data2.size() - 1;
            upperBound = data2.get((int) (size2 * percent / 100));
        }
        return new NumberPair(lowerBound, upperBound);
    }

    private boolean openNewCatalogSearch(double targetRa, double targetDec) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        timer.stop();

        Application application = new Application();
        application.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        application.init();

        Point point = baseFrame.getLocation();
        application.getBaseFrame().setLocation((int) point.getX() + WINDOW_SPACING, (int) point.getY() + WINDOW_SPACING);

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
        application.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        application.init();
        application.getTabbedPane().setSelectedIndex(3);

        Point point = baseFrame.getLocation();
        application.getBaseFrame().setLocation((int) point.getX() + WINDOW_SPACING, (int) point.getY() + WINDOW_SPACING);

        ImageViewerTab imageViewerTab = application.getImageViewerTab();
        imageViewerTab.getCoordsField().setText(roundTo7DecNZ(targetRa) + " " + roundTo7DecNZ(targetDec));
        imageViewerTab.getSizeField().setText(differentSizeField.getText());
        if (unwiseCutouts.isSelected()) {
            imageViewerTab.resetEpochSlider(NUMBER_OF_UNWISE_EPOCHS);
            imageViewerTab.setPixelScale(PIXEL_SCALE_WISE);
            imageViewerTab.getUnwiseCutouts().setSelected(true);
        }
        if (decalsCutouts.isSelected()) {
            imageViewerTab.setPixelScale(PIXEL_SCALE_DECAM);
            imageViewerTab.getDecalsCutouts().setSelected(true);
        }
        imageViewerTab.getWiseBands().setSelectedItem(wiseBand);
        imageViewerTab.setQuadrantCount(quadrantCount);
        imageViewerTab.getZoomSlider().setValue(ZOOM);
        imageViewerTab.setZoom(ZOOM);
        imageViewerTab.setImageViewer(this);

        baseFrame.setCursor(Cursor.getDefaultCursor());

        return true;
    }

    private BufferedImage fetchDecalsImage(double targetRa, double targetDec, double size) {
        try {
            int imageSize = (int) round(size * pixelScale * 4);
            if (imageSize > 3000) {
                return null;
            }
            String imageUrl = String.format("https://www.legacysurvey.org/viewer/jpeg-cutout?ra=%f&dec=%f&pixscale=%f&size=%d&bands=grz&layer=%s", targetRa, targetDec, PIXEL_SCALE_DECAM, imageSize, CURRENT_DESI_DR);
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedImage image;
            try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {
                image = ImageIO.read(stream);
            }
            return image;
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage fetchPs1Image(double targetRa, double targetDec, double size) {
        try {
            List<String> fileNames = new ArrayList<>();
            String imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=giy&sep=comma", targetRa, targetDec);
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
            imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?red=%s&green=%s&blue=%s&ra=%f&dec=%f&size=%d&output_size=%d&autoscale=99.8", fileNames.get(2), fileNames.get(1), fileNames.get(0), targetRa, targetDec, (int) round(size * pixelScale * 4), 1024);
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedImage image;
            try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {
                image = ImageIO.read(stream);
            }
            return image;
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage fetchUkidssImage(double targetRa, double targetDec, double size) {
        try {
            if (targetDec < -5) {
                return null;
            }
            String surveyLabel = "UKIDSS";
            Map<String, BufferedImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size, UKIDSS_SURVEY_URL, surveyLabel);
            BufferedImage nirImage = nirImages.get("K-H-J");
            if (nirImage == null) {
                nirImage = nirImages.get("K-J");
            }
            return nirImage;
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage fetchVhsImage(double targetRa, double targetDec, double size) {
        try {
            if (targetDec > 5) {
                return null;
            }
            String surveyLabel = "VHS";
            Map<String, BufferedImage> nirImages = retrieveNearInfraredImages(targetRa, targetDec, size, VHS_SURVEY_URL, surveyLabel);
            BufferedImage nirImage = nirImages.get("K-H-J");
            if (nirImage == null) {
                nirImage = nirImages.get("K-J");
            }
            return nirImage;
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage fetchSdssImage(double targetRa, double targetDec, double size) {
        try {
            int resolution = 1024;
            String imageUrl = String.format(SDSS_BASE_URL + "/SkyserverWS/ImgCutout/getjpeg?ra=%f&dec=%f&width=%d&height=%d&scale=%f", targetRa, targetDec, resolution, resolution, size * pixelScale / resolution);
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedImage image;
            try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {
                image = ImageIO.read(stream);
            }
            return image;
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage fetchDssImage(double targetRa, double targetDec, double size) {
        try {
            BufferedImage image = retrieveImage(targetRa, targetDec, (int) round(size * pixelScale), "dss", "file_type=colorimage");
            return image;
        } catch (Exception ex) {
            return null;
        }
    }

    private void displayDssImages(double targetRa, double targetDec, int size, Counter counter) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JPanel bandPanel = new JPanel(new GridLayout(1, 0));

            BufferedImage image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_blue&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "poss1_blue"));
            }
            image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_red&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "poss1_red"));
            }
            image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_blue&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "poss2ukstu_blue"));
            }
            image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_red&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "poss2ukstu_red"));
            }
            image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "poss2ukstu_ir"));
            }
            image = retrieveImage(targetRa, targetDec, size, "dss", "file_type=colorimage");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "dss2IR-dss1Red-dss1Blue"));
            }

            int componentCount = bandPanel.getComponentCount();
            if (componentCount == 0) {
                return;
            }

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("DSS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
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

    private void display2MassImages(double targetRa, double targetDec, int size, Counter counter) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JPanel bandPanel = new JPanel(new GridLayout(1, 0));

            BufferedImage image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=j&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "J"));
            }
            image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=h&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "H"));
            }
            image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "K"));
            }
            image = retrieveImage(targetRa, targetDec, size, "2mass", "file_type=colorimage");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "K-H-J"));
            }

            int componentCount = bandPanel.getComponentCount();
            if (componentCount == 0) {
                return;
            }

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("2MASS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
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

    private void displaySdssImages(double targetRa, double targetDec, int size, Counter counter) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JPanel bandPanel = new JPanel(new GridLayout(1, 0));

            BufferedImage image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=u&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "u"));
            }
            image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=g&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "g"));
            }
            image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=r&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "r"));
            }
            image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=i&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "i"));
            }
            image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "z"));
            }
            image = retrieveImage(targetRa, targetDec, size, "sdss", "file_type=colorimage");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "z-g-u"));
            }

            int componentCount = bandPanel.getComponentCount();
            if (componentCount == 0) {
                return;
            }

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("SDSS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
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

    private void displaySpitzerImages(double targetRa, double targetDec, int size, Counter counter) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JPanel bandPanel = new JPanel(new GridLayout(1, 0));

            BufferedImage image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC1&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "IRAC1"));
            }
            image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC2&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "IRAC2"));
            }
            image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC3&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "IRAC3"));
            }
            image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "IRAC4"));
            }
            image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:MIPS24&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "MIPS24"));
            }
            image = retrieveImage(targetRa, targetDec, size, "seip", "file_type=colorimage");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "3-color"));
            }

            int componentCount = bandPanel.getComponentCount();
            if (componentCount == 0) {
                return;
            }

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("Spitzer (SEIP) - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
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

    private void displayAllwiseImages(double targetRa, double targetDec, int size, Counter counter) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JPanel bandPanel = new JPanel(new GridLayout(1, 0));

            BufferedImage image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=1&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "W1"));
            }
            image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "W2"));
            }
            image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=3&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "W3"));
            }
            image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=4&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "W4"));
            }
            image = retrieveImage(targetRa, targetDec, size, "wise", "file_type=colorimage");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "W4-W2-W1"));
            }

            int componentCount = bandPanel.getComponentCount();
            if (componentCount == 0) {
                return;
            }

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("AllWISE - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
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
            // Fetch file names for Pan-STARRS filters
            Map<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
            if (imageInfos.isEmpty()) {
                return;
            }

            // Fetch images for Pan-STARRS filters
            JPanel bandPanel = new JPanel(new GridLayout(1, 0));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("g")), targetRa, targetDec, size, true), "g"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("r")), targetRa, targetDec, size, true), "r"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("i")), targetRa, targetDec, size, true), "i"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size, true), "z"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("y")), targetRa, targetDec, size, true), "y"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s&green=%s&blue=%s", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g")), targetRa, targetDec, size, false), "y-i-g"));

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("Pan-STARRS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
            imageFrame.add(bandPanel);
            imageFrame.setSize(6 * PANEL_WIDTH, PANEL_HEIGHT);
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

    private void displayDecalsImages(double targetRa, double targetDec, int size, Counter counter) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JPanel bandPanel = new JPanel(new GridLayout(1, 0));

            BufferedImage image = retrieveDecalsImage(targetRa, targetDec, size, "g", true);
            if (image != null) {
                image = convertToGray(image);
                bandPanel.add(buildImagePanel(image, "g"));
            }
            image = retrieveDecalsImage(targetRa, targetDec, size, "r", true);
            if (image != null) {
                image = convertToGray(image);
                bandPanel.add(buildImagePanel(image, "r"));
            }
            image = retrieveDecalsImage(targetRa, targetDec, size, "z", true);
            if (image != null) {
                image = convertToGray(image);
                bandPanel.add(buildImagePanel(image, "z"));
            }
            image = retrieveDecalsImage(targetRa, targetDec, size, "grz", false);
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "g-r-z"));
            }

            int componentCount = bandPanel.getComponentCount();
            if (componentCount == 0) {
                return;
            }

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("DESI LS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
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

    private void displayStaticTimeSeries(double targetRa, double targetDec, int size, Counter counter) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JPanel bandPanel = new JPanel(new GridLayout(1, 0));

            BufferedImage image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "DSS2 - IR"));
            }
            image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "2MASS - K"));
            }
            image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "SDSS - z"));
            }
            image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "Spitzer - CH4"));
            }
            image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "WISE - W2"));
            }
            Map<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
            if (!imageInfos.isEmpty()) {
                image = retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size, true);
                bandPanel.add(buildImagePanel(image, "PS1 - z"));
            }
            image = retrieveDecalsImage(targetRa, targetDec, size, "z", true);
            if (image != null) {
                image = convertToGray(image);
                bandPanel.add(buildImagePanel(image, "DESI LS - z"));
            }

            int componentCount = bandPanel.getComponentCount();
            if (componentCount == 0) {
                return;
            }

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("Time series - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
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
            BufferedImage image;
            List<Couple<String, BufferedImage>> imageList = new ArrayList<>();
            if (allSkyImages.isSelected()) {
                image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
                if (image != null) {
                    imageList.add(new Couple("DSS2 - IR", image));
                }
            }
            if (twoMassImages.isSelected()) {
                image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
                if (image != null) {
                    imageList.add(new Couple("2MASS - K", image));
                }
            }
            if (sloanImages.isSelected()) {
                image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
                if (image != null) {
                    imageList.add(new Couple("SDSS - z", image));
                }
            }
            if (spitzerImages.isSelected()) {
                image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
                if (image != null) {
                    imageList.add(new Couple("Spitzer - CH4", image));
                }
            }
            if (allwiseImages.isSelected()) {
                image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
                if (image != null) {
                    imageList.add(new Couple("WISE - W2", image));
                }
            }
            if (ps1Images.isSelected()) {
                Map<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
                if (!imageInfos.isEmpty()) {
                    image = retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size, true);
                    imageList.add(new Couple("PS1 - z", image));
                }
            }
            if (decalsImages.isSelected()) {
                image = retrieveDecalsImage(targetRa, targetDec, size, "z", true);
                if (image != null) {
                    image = convertToGray(image);
                    imageList.add(new Couple("DESI LS - z", image));
                }
            }

            int componentCount = imageList.size();
            if (componentCount == 0) {
                return;
            }

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
                        BufferedImage[] imageSet = new BufferedImage[imageList.size()];
                        int i = 0;
                        for (Couple<String, BufferedImage> imageData : imageList) {
                            BufferedImage imageBuffer = imageData.getB();
                            imageSet[i++] = drawCenterShape(imageBuffer);
                        }
                        if (imageSet.length > 0) {
                            GifSequencer sequencer = new GifSequencer();
                            sequencer.generateFromBI(imageSet, file, speed / 10, true);
                        }
                    }
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("Time series - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
            imageFrame.add(container);
            imageFrame.setSize(PANEL_WIDTH + 20, PANEL_HEIGHT + 50);
            imageFrame.setAlwaysOnTop(false);
            imageFrame.setResizable(false);

            Timer timeSeries = new Timer(speed, (ActionEvent e) -> {
                if (imageCount > componentCount - 1) {
                    imageCount = 0;
                }
                displayPanel.removeAll();
                Couple<String, BufferedImage> imageData = imageList.get(imageCount);
                displayPanel.add(buildImagePanel(imageData.getB(), imageData.getA()));
                imageFrame.setVisible(true);
                imageCount++;
            });

            imageFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evt) {
                    timeSeries.stop();
                    imageCount = 0;
                }
            });

            timeSeries.start();
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
    }

    private JPanel buildImagePanel(BufferedImage image, String imageHeader) {
        JPanel panel = new JPanel();
        panel.setBorder(createEtchedBorder(imageHeader));
        panel.add(new JLabel(new ImageIcon(drawCenterShape(image))));
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
                if (showBrownDwarfsOnly.isSelected() || displaySpectralTypes.isSelected()) {
                    setSpectralType(catalogEntry);
                }
                if (showBrownDwarfsOnly.isSelected()) {
                    if (isBrownDwarf(catalogEntry)) {
                        resultEntries.add(catalogEntry);
                    }
                } else {
                    resultEntries.add(catalogEntry);
                }
            });
            return resultEntries;
        } catch (Exception ex) {
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
                if (showBrownDwarfsOnly.isSelected() || displaySpectralTypes.isSelected()) {
                    setSpectralType(catalogEntry);
                }
                if (showBrownDwarfsOnly.isSelected()) {
                    if (isBrownDwarf(catalogEntry)) {
                        resultEntries.add(catalogEntry);
                    }
                } else {
                    resultEntries.add(catalogEntry);
                }
            });
            return resultEntries;
        } catch (Exception ex) {
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
        boolean isCatalogSearch = false;
        if (!customOverlay.getTableName().isEmpty()) {
            isCatalogSearch = true;
            String vizieRUrl = createVizieRUrl(targetRa, targetDec, getFovDiagonal() / 2 / DEG_ARCSEC,
                    customOverlay.getTableName(), customOverlay.getRaColName(), customOverlay.getDecColName());
            try {
                results = readResponse(establishHttpConnection(vizieRUrl), "VizieR");
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
        Scanner scanner = null;
        try {
            if (results == null) {
                scanner = new Scanner(customOverlay.getFile());
            } else {
                scanner = new Scanner(results);
            }
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
                errors.append("RA position must not be greater than ").append(lastColumnIndex).append(".").append(LINE_SEP);
            }
            if (decColumnIndex > lastColumnIndex) {
                errors.append("Dec position must not be greater than ").append(lastColumnIndex).append(".").append(LINE_SEP);
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

                NumberPair coords;
                double radius = size * pixelScale / 2 / DEG_ARCSEC;

                coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec), new NumberPair(-radius, 0));
                double rightBoundary = coords.getX();

                coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec), new NumberPair(radius, 0));
                double leftBoundary = coords.getX();

                double bottomBoundary = targetDec - radius;
                double topBoundary = targetDec + radius;

                double catalogRa = catalogEntry.getRa();
                double catalogDec = catalogEntry.getDec();

                if (isCatalogSearch
                        || (catalogRa > rightBoundary && catalogRa < leftBoundary
                        && catalogDec > bottomBoundary && catalogDec < topBoundary)) {
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
            if (scanner != null) {
                scanner.close();
            }
            customOverlay.setCatalogEntries(catalogEntries);
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
        return null;
    }

    private void setSpectralType(CatalogEntry catalogEntry) {
        List<LookupResult> results = mainSequenceSpectralTypeLookupService.lookup(catalogEntry.getColors(true));
        if (results.isEmpty()) {
            results = brownDwarfsSpectralTypeLookupService.lookup(catalogEntry.getColors(true));
        }
        if (results.isEmpty()) {
            catalogEntry.setSpt("N/A");
        } else {
            results.sort(Comparator.comparingDouble(LookupResult::getGap));
            catalogEntry.setSpt(results.get(0).getSpt());
        }
    }

    private boolean isBrownDwarf(CatalogEntry catalogEntry) {
        return MLTY_DWARFS.contains(catalogEntry.getSpt());
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
            if (!sdssCatalogEntry.getSpecObjID().equals(new BigInteger("0"))
                    && catalogEntry.getPixelRa() > x - radius && catalogEntry.getPixelRa() < x + radius
                    && catalogEntry.getPixelDec() > y - radius && catalogEntry.getPixelDec() < y + radius) {
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
            try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {
                spectrum = ImageIO.read(stream);
            }
            if (spectrum != null) {
                JFrame spectrumFrame = new JFrame();
                spectrumFrame.setIconImage(getToolBoxImage());
                spectrumFrame.setTitle("SDSS spectrum for object: " + roundTo2DecNZ(catalogEntry.getRa()) + " " + roundTo2DecNZ(catalogEntry.getDec()));
                spectrumFrame.add(new JLabel(new ImageIcon(spectrum)));
                spectrumFrame.setSize(1200, 900);
                spectrumFrame.setAlwaysOnTop(false);
                spectrumFrame.setResizable(false);
                spectrumFrame.setVisible(true);
            }
        } catch (Exception ex) {
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
            if (displaySpectralTypes.isSelected()) {
                toDraw = new Text(position.getX(), position.getY(), getOverlaySize(), color, catalogEntry.getSpt());
            } else {
                switch (shape) {
                    case CIRCLE:
                        toDraw = new Circle(position.getX(), position.getY(), getOverlaySize(), color);
                        break;
                    case CROSS:
                        toDraw = new Cross(position.getX(), position.getY(), getOverlaySize(), color);
                        break;
                    case XCROSS:
                        toDraw = new XCross(position.getX(), position.getY(), getOverlaySize(), color);
                        break;
                    case SQUARE:
                        toDraw = new Square(position.getX(), position.getY(), getOverlaySize(), color);
                        break;
                    case TRIANGLE:
                        toDraw = new Triangle(position.getX(), position.getY(), getOverlaySize(), color);
                        break;
                    case DIAMOND:
                        toDraw = new Diamond(position.getX(), position.getY(), getOverlaySize(), color);
                        break;
                    default:
                        toDraw = new Circle(position.getX(), position.getY(), getOverlaySize(), color);
                        break;
                }
            }
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
                case W1:
                    ab_flags = ab_flags.substring(0, 1);
                    cc_flags = cc_flags.substring(0, 1);
                    break;
                case W2:
                    ab_flags = ab_flags.substring(1, 2);
                    cc_flags = cc_flags.substring(1, 2);
                    break;
                default:
                    ab_flags = ab_flags.substring(0, 2);
                    cc_flags = cc_flags.substring(0, 2);
                    break;
            }
            String flags = ab_flags + cc_flags;
            if (ghostOverlay.isSelected()) {
                if (flags.contains("o")) {
                    Drawable toDraw = new Diamond(position.getX(), position.getY(), getOverlaySize() / 2, Color.MAGENTA.darker());
                    toDraw.draw(graphics);
                }
                if (flags.contains("O")) {
                    Drawable toDraw = new Diamond(position.getX(), position.getY(), getOverlaySize(), Color.MAGENTA.darker());
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
                    Drawable toDraw = new XCross(position.getX(), position.getY(), getOverlaySize() / 2, Color.GREEN.darker());
                    toDraw.draw(graphics);
                }
                if (flags.contains("P")) {
                    Drawable toDraw = new XCross(position.getX(), position.getY(), getOverlaySize(), Color.GREEN.darker());
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

    private void drawPMVectors(BufferedImage image, List<CatalogEntry> catalogEntries, Color color, int totalEpochs) {
        Graphics graphics = image.getGraphics();
        catalogEntries.forEach(catalogEntry -> {
            NumberPair position = toPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
            catalogEntry.setPixelRa(position.getX());
            catalogEntry.setPixelDec(position.getY());

            double ra = catalogEntry.getRa();
            double dec = catalogEntry.getDec();

            double pmRa = catalogEntry.getPmra();
            double pmDec = catalogEntry.getPmdec();

            double numberOfYears = 0;
            if (catalogEntry instanceof GaiaCatalogEntry) {
                numberOfYears = GAIADR2_ALLWISE_EPOCH_DIFF;
            }
            if (catalogEntry instanceof GaiaDR3CatalogEntry) {
                numberOfYears = GAIADR3_ALLWISE_EPOCH_DIFF;
            }
            if (catalogEntry instanceof NoirlabCatalogEntry) {
                numberOfYears = ((NoirlabCatalogEntry) catalogEntry).getMeanEpoch() - ALLWISE_REFERENCE_EPOCH;
            }
            if (catalogEntry instanceof CatWiseCatalogEntry) {
                ra = ((CatWiseCatalogEntry) catalogEntry).getRa_pm();
                dec = ((CatWiseCatalogEntry) catalogEntry).getDec_pm();
                numberOfYears = CATWISE_ALLWISE_EPOCH_DIFF;
            }

            if (showProperMotion.isSelected()) {
                NumberPair pixelCoords;
                NumberPair newPosition = getNewPosition(ra, dec, pmRa, pmDec, numberOfYears, totalEpochs);
                pixelCoords = toPixelCoordinates(newPosition.getX(), newPosition.getY());
                Disk disk = new Disk(pixelCoords.getX(), pixelCoords.getY(), getOverlaySize(200), color);
                disk.draw(image.getGraphics());
            } else {
                NumberPair fromCoords = calculatePositionFromProperMotion(new NumberPair(ra, dec), new NumberPair(-numberOfYears * pmRa / DEG_MAS, -numberOfYears * pmDec / DEG_MAS));
                double fromRa = fromCoords.getX();
                double fromDec = fromCoords.getY();

                NumberPair fromPoint = toPixelCoordinates(fromRa, fromDec);
                double fromX = fromPoint.getX();
                double fromY = fromPoint.getY();

                numberOfYears = selectedEpochs + 2; // +2 years -> hibernation period

                NumberPair toCoords = calculatePositionFromProperMotion(new NumberPair(fromRa, fromDec), new NumberPair(numberOfYears * pmRa / DEG_MAS, numberOfYears * pmDec / DEG_MAS));
                double toRa = toCoords.getX();
                double toDec = toCoords.getY();

                NumberPair toPoint = toPixelCoordinates(toRa, toDec);
                double toX = toPoint.getX();
                double toY = toPoint.getY();

                Drawable toDraw;
                if (displaySpectralTypes.isSelected()) {
                    toDraw = new Text(position.getX(), position.getY(), getOverlaySize(), color, catalogEntry.getSpt());
                } else {
                    toDraw = new Arrow(fromX, fromY, toX, toY, getOverlaySize(), color);
                }
                toDraw.draw(graphics);
            }
        });
    }

    private void showPMInfo(List<CatalogEntry> catalogEntries, int x, int y, Color color) {
        catalogEntries.forEach(catalogEntry -> {
            double radius = getOverlaySize() / 2;
            if (catalogEntry.getPixelRa() > x - radius && catalogEntry.getPixelRa() < x + radius
                    && catalogEntry.getPixelDec() > y - radius && catalogEntry.getPixelDec() < y + radius) {
                displayCatalogPanel(catalogEntry, color);
            }
        });
    }

    private void showCatalogInfo(List<CatalogEntry> catalogEntries, int x, int y, Color color) {
        catalogEntries.forEach(catalogEntry -> {
            double radius = getOverlaySize() / 2;
            if (catalogEntry.getPixelRa() > x - radius && catalogEntry.getPixelRa() < x + radius
                    && catalogEntry.getPixelDec() > y - radius && catalogEntry.getPixelDec() < y + radius) {
                displayCatalogPanel(catalogEntry, color);
            }
        });
    }

    private void displayCatalogPanel(CatalogEntry catalogEntry, Color color) {
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
            maxRows = rows > 19 ? rows : 19;
        }

        JPanel detailPanel = new JPanel(new GridLayout(maxRows, 4));
        detailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), catalogEntry.getCatalogName() + " entry (Computed values are shown in green; (*) Further info: mouse pointer)", TitledBorder.LEFT, TitledBorder.TOP
        ));

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
        scrollPanel.setPreferredSize(new Dimension(650, 330));
        scrollPanel.setMinimumSize(new Dimension(650, 330));
        scrollPanel.setMaximumSize(new Dimension(650, 330));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new LineBorder(color, 3));
        container.add(simpleLayout ? detailPanel : scrollPanel);

        if (!simpleLayout) {
            List<LookupResult> mainSequenceResults = mainSequenceSpectralTypeLookupService.lookup(catalogEntry.getColors(true));
            if (!mainSequenceResults.isEmpty()) {
                container.add(createMainSequenceSpectralTypePanel(mainSequenceResults));
                if (catalogEntry instanceof AllWiseCatalogEntry) {
                    AllWiseCatalogEntry entry = (AllWiseCatalogEntry) catalogEntry;
                    if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        messagePanel.add(createLabel(AGN_WARNING, JColor.RED));
                        container.add(messagePanel);
                    }
                }
                if (catalogEntry instanceof WhiteDwarf) {
                    WhiteDwarf entry = (WhiteDwarf) catalogEntry;
                    if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        messagePanel.add(createLabel(WD_WARNING, JColor.RED));
                        container.add(messagePanel);
                    }
                }
            }
            List<LookupResult> brownDwarfsResults = brownDwarfsSpectralTypeLookupService.lookup(catalogEntry.getColors(true));
            if (!brownDwarfsResults.isEmpty()) {
                container.add(createBrownDwarfsSpectralTypePanel(brownDwarfsResults));
            }
            if (mainSequenceResults.isEmpty() && brownDwarfsResults.isEmpty()) {
                container.add(createMainSequenceSpectralTypePanel(mainSequenceResults));
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
                collectObject(selectedObjectType, catalogEntry, baseFrame, brownDwarfsSpectralTypeLookupService, collectionTable);
                collectButton.setText("Added!");
                collectTimer.restart();
            });

            if (catalogEntry instanceof SimbadCatalogEntry) {
                JButton referencesButton = new JButton("Object references");
                collectPanel.add(referencesButton);
                referencesButton.addActionListener((ActionEvent evt) -> {
                    JFrame referencesFrame = new JFrame();
                    referencesFrame.addWindowListener(getChildWindowAdapter(baseFrame));
                    referencesFrame.setIconImage(getToolBoxImage());
                    referencesFrame.setTitle("Measurements and references for "
                            + catalogEntry.getSourceId() + " ("
                            + roundTo7DecNZ(catalogEntry.getRa()) + " "
                            + roundTo7DecNZ(catalogEntry.getDec()) + ")");
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
                copyCoordsButton.setText("Copied!");
                copyCoordsTimer.restart();
            });

            JButton copyInfoButton = new JButton("Copy digest");
            buttonPanel.add(copyInfoButton);
            Timer copyInfoTimer = new Timer(3000, (ActionEvent e) -> {
                copyInfoButton.setText("Copy digest");
            });
            copyInfoButton.addActionListener((ActionEvent evt) -> {
                copyToClipboard(copyObjectDigest(catalogEntry));
                copyInfoButton.setText("Copied!");
                copyInfoTimer.restart();
            });

            JButton copyAllButton = new JButton("Copy all");
            buttonPanel.add(copyAllButton);
            Timer copyAllTimer = new Timer(3000, (ActionEvent e) -> {
                copyAllButton.setText("Copy all");
            });
            copyAllButton.addActionListener((ActionEvent evt) -> {
                copyToClipboard(copyObjectInfo(catalogEntry, mainSequenceResults, brownDwarfsResults, distanceLookupService));
                copyAllButton.setText("Copied!");
                copyAllTimer.restart();
            });

            JButton fillFormButton = new JButton("TYGO form");
            buttonPanel.add(fillFormButton);
            fillFormButton.addActionListener((ActionEvent evt) -> {
                fillTygoForm(catalogEntry, catalogQueryService, baseFrame);

            });

            JButton createSedButton = new JButton("SED");
            buttonPanel.add(createSedButton);
            createSedButton.addActionListener((ActionEvent evt) -> {
                JFrame sedFrame = new JFrame();
                sedFrame.addWindowListener(getChildWindowAdapter(baseFrame));
                sedFrame.setIconImage(getToolBoxImage());
                sedFrame.setTitle("SED");
                sedFrame.add(new SedPanel(brownDwarfLookupEntries, catalogQueryService, catalogEntry, baseFrame));
                sedFrame.setSize(1000, 900);
                sedFrame.setLocation(0, 0);
                sedFrame.setAlwaysOnTop(false);
                sedFrame.setResizable(true);
                sedFrame.setVisible(true);
            });

            JButton createWdSedButton = new JButton("WD SED");
            buttonPanel.add(createWdSedButton);
            createWdSedButton.addActionListener((ActionEvent evt) -> {
                JFrame sedFrame = new JFrame();
                sedFrame.addWindowListener(getChildWindowAdapter(baseFrame));
                sedFrame.setIconImage(getToolBoxImage());
                sedFrame.setTitle("WD SED");
                sedFrame.add(new WdSedPanel(catalogQueryService, catalogEntry, baseFrame));
                sedFrame.setSize(1000, 900);
                sedFrame.setLocation(0, 0);
                sedFrame.setAlwaysOnTop(false);
                sedFrame.setResizable(true);
                sedFrame.setVisible(true);
            });

            if (catalogEntry instanceof GaiaCmd) {
                JButton createCmdButton = new JButton("CMD");
                buttonPanel.add(createCmdButton);
                createCmdButton.addActionListener((ActionEvent evt) -> {
                    try {
                        JFrame sedFrame = new JFrame();
                        sedFrame.addWindowListener(getChildWindowAdapter(baseFrame));
                        sedFrame.setIconImage(getToolBoxImage());
                        sedFrame.setTitle("CMD");
                        sedFrame.add(new CmdPanel((GaiaCmd) catalogEntry));
                        sedFrame.setSize(1000, 900);
                        sedFrame.setLocation(0, 0);
                        sedFrame.setAlwaysOnTop(false);
                        sedFrame.setResizable(true);
                        sedFrame.setVisible(true);
                    } catch (Exception ex) {
                        showErrorDialog(baseFrame, ex.getMessage());
                    }
                });
            }
        }

        JFrame detailsFrame = new JFrame();
        detailsFrame.addWindowListener(getChildWindowAdapter(baseFrame));
        detailsFrame.setIconImage(getToolBoxImage());
        detailsFrame.setTitle("Object details");
        detailsFrame.add(simpleLayout ? new JScrollPane(container) : container);
        detailsFrame.setSize(650, 650);
        detailsFrame.setLocation(windowShift, windowShift);
        detailsFrame.setAlwaysOnTop(false);
        detailsFrame.setResizable(true);
        detailsFrame.setVisible(true);
        windowShift += 10;
    }

    private JScrollPane createMainSequenceSpectralTypePanel(List<LookupResult> results) {
        List<String[]> spectralTypes = new ArrayList<>();
        results.forEach(entry -> {
            String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
            String spectralType = entry.getSpt() + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + "," + roundTo3DecLZ(entry.getGap()) + ","
                    + entry.getTeff() + "," + roundTo3Dec(entry.getRsun()) + "," + roundTo3Dec(entry.getMsun());
            spectralTypes.add(spectralType.split(",", -1));
        });

        String titles = "spt,matched color,nearest color,offset,teff,radius (Rsun),mass (Msun)";
        String[] columns = titles.split(",", -1);
        Object[][] rows = new Object[][]{};
        JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns);
        alignResultColumns(spectralTypeTable, spectralTypes);
        spectralTypeTable.setAutoCreateRowSorter(true);
        spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = spectralTypeTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(75);
        columnModel.getColumn(3).setPreferredWidth(50);
        columnModel.getColumn(4).setPreferredWidth(50);
        columnModel.getColumn(5).setPreferredWidth(75);
        columnModel.getColumn(6).setPreferredWidth(75);

        JScrollPane spectralTypePanel = new JScrollPane(spectralTypeTable);
        spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Main sequence spectral type evaluation", TitledBorder.LEFT, TitledBorder.TOP
        ));

        return spectralTypePanel;
    }

    private JScrollPane createBrownDwarfsSpectralTypePanel(List<LookupResult> results) {
        List<String[]> spectralTypes = new ArrayList<>();
        results.forEach(entry -> {
            String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
            String spectralType = entry.getSpt() + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + "," + roundTo3DecLZ(entry.getGap());
            spectralTypes.add(spectralType.split(",", -1));
        });

        String titles = "spt,matched color,nearest color,offset";
        String[] columns = titles.split(",", -1);
        Object[][] rows = new Object[][]{};
        JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns);
        alignResultColumns(spectralTypeTable, spectralTypes);
        spectralTypeTable.setAutoCreateRowSorter(true);
        spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = spectralTypeTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(75);
        columnModel.getColumn(3).setPreferredWidth(50);

        JScrollPane spectralTypePanel = new JScrollPane(spectralTypeTable);
        spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "M, L & T dwarfs spectral type evaluation", TitledBorder.LEFT, TitledBorder.TOP
        ));

        return spectralTypePanel;
    }

    private double getFovDiagonal() {
        return size * pixelScale * sqrt(2);
    }

    private double getOverlaySize() {
        return getOverlaySize(100);
    }

    private double getOverlaySize(int val) {
        int x = decalsCutouts.isSelected() ? 1500 : 300;
        double overlaySize = zoom / val + x / size;
        return min(overlaySize, 15);
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

    public JComboBox getEpochs() {
        return epochs;
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

    public JTextField getDifferentSizeField() {
        return differentSizeField;
    }

    public JTextField getProperMotionField() {
        return properMotionField;
    }

    public JSlider getEpochSlider() {
        return epochSlider;
    }

    public JLabel getEpochLabel() {
        return epochLabel;
    }

    public JRadioButton getUnwiseCutouts() {
        return unwiseCutouts;
    }

    public JRadioButton getDecalsCutouts() {
        return decalsCutouts;
    }

    public JCheckBox getSimbadOverlay() {
        return simbadOverlay;
    }

    public JCheckBox getGaiaOverlay() {
        return gaiaOverlay;
    }

    public JCheckBox getGaiaDR3Overlay() {
        return gaiaDR3Overlay;
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

    public JCheckBox getPanStarrsOverlay() {
        return panStarrsOverlay;
    }

    public JCheckBox getSdssOverlay() {
        return sdssOverlay;
    }

    public JCheckBox getTwoMassOverlay() {
        return twoMassOverlay;
    }

    public JCheckBox getVhsOverlay() {
        return vhsOverlay;
    }

    public JCheckBox getGaiaWDOverlay() {
        return gaiaWDOverlay;
    }

    public JCheckBox getNoirlabOverlay() {
        return noirlabOverlay;
    }

    public JCheckBox getTessOverlay() {
        return tessOverlay;
    }

    public JCheckBox getDesOverlay() {
        return desOverlay;
    }

    public Timer getTimer() {
        return timer;
    }

    public FlipbookComponent[] getFlipbook() {
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

    public void setNumberOfEpochs(int numberOfEpochs) {
        this.numberOfEpochs = numberOfEpochs;
        this.selectedEpochs = numberOfEpochs / 2;
    }

    public void setWiseBand(WiseBand wiseBand) {
        this.wiseBand = wiseBand;
    }

    public void setEpoch(Epoch epoch) {
        this.epoch = epoch;
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

    public void setUkidssImages(boolean ukidssImages) {
        this.ukidssImages = ukidssImages;
    }

    public void setVhsImages(boolean vhsImages) {
        this.vhsImages = vhsImages;
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