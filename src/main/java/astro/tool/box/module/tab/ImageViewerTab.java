package astro.tool.box.module.tab;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.Urls.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.Couple;
import astro.tool.box.container.CustomOverlay;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.NumberTriplet;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseRejectEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaCatalogEntry;
import astro.tool.box.container.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.container.catalog.GaiaWDCatalogEntry;
import astro.tool.box.container.catalog.GenericCatalogEntry;
import astro.tool.box.container.catalog.NoirlabCatalogEntry;
import astro.tool.box.container.catalog.PanStarrsCatalogEntry;
import astro.tool.box.container.catalog.ProperMotionQuery;
import astro.tool.box.container.catalog.SDSSCatalogEntry;
import astro.tool.box.container.catalog.SSOCatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.container.catalog.SpitzerCatalogEntry;
import astro.tool.box.container.catalog.TwoMassCatalogEntry;
import astro.tool.box.container.catalog.UnWiseCatalogEntry;
import astro.tool.box.container.catalog.VHSCatalogEntry;
import astro.tool.box.container.lookup.BrownDwarfLookupEntry;
import astro.tool.box.container.lookup.DistanceLookupResult;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.enumeration.Epoch;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.ObjectType;
import astro.tool.box.enumeration.Shape;
import astro.tool.box.enumeration.WiseBand;
import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.module.Application;
import astro.tool.box.module.FlipbookComponent;
import astro.tool.box.module.GifSequencer;
import astro.tool.box.module.ImageContainer;
import astro.tool.box.module.InfoSheet;
import astro.tool.box.module.TextPrompt;
import astro.tool.box.module.shape.Arrow;
import astro.tool.box.module.shape.Circle;
import astro.tool.box.module.shape.Cross;
import astro.tool.box.module.shape.CrossHair;
import astro.tool.box.module.shape.Diamond;
import astro.tool.box.module.shape.Drawable;
import astro.tool.box.module.shape.Square;
import astro.tool.box.module.shape.Text;
import astro.tool.box.module.shape.Triangle;
import astro.tool.box.module.shape.XCross;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.DistanceLookupService;
import astro.tool.box.service.SpectralTypeLookupService;
import astro.tool.box.util.Counter;
import astro.tool.box.util.FileTypeFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Math.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
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
    public static final String FITS_DIR = USER_HOME + "/.fits";
    public static final String EPOCH_LABEL = "Number of epochs: %d";
    public static final String HIGH_SCALE_LABEL = "Contrast - high scale: %d";
    public static final String LOW_SCALE_LABEL = "Contrast - low scale: %d";
    public static final String SUB_SCALE_LABEL = "Contrast - subtracted mode: %d";
    public static final WiseBand WISE_BAND = WiseBand.W2;
    public static final Epoch EPOCH = Epoch.FIRST_LAST;
    public static final double OVERLAP_FACTOR = 0.9;
    public static final double PIXEL_SIZE = 2.75;
    public static final int NUMBER_OF_EPOCHS = 7;
    public static final int WINDOW_SPACING = 25;
    public static final int PANEL_HEIGHT = 270;
    public static final int PANEL_WIDTH = 230;
    public static final int ROW_HEIGHT = 25;
    public static final int HIGH_CONTRAST = 0;
    public static final int LOW_CONTRAST = 50;
    public static final int SUB_CONTRAST = 1;
    public static final int EPOCH_GAP = 5;
    public static final int SPEED = 300;
    public static final int ZOOM = 500;
    public static final int SIZE = 500;
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
    public static final double NOIRLAB_ALLWISE_EPOCH_DIFF = GAIADR2_ALLWISE_EPOCH_DIFF;

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    private final CatalogQueryFacade catalogQueryFacade;
    private final SpectralTypeLookupService mainSequenceSpectralTypeLookupService;
    private final SpectralTypeLookupService brownDwarfsSpectralTypeLookupService;
    private final DistanceLookupService distanceLookupService;
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
    private List<CatalogEntry> spitzerEntries;
    private List<CatalogEntry> noirlabEntries;
    private List<CatalogEntry> noirlabTpmEntries;
    private List<CatalogEntry> ssoEntries;

    private JPanel imagePanel;
    private JPanel rightPanel;
    private JLabel changeFovLabel;
    private JLabel highScaleLabel;
    private JLabel lowScaleLabel;
    private JLabel subScaleLabel;
    private JLabel epochLabel;
    private JPanel zooniversePanel1;
    private JPanel zooniversePanel2;
    private JCheckBox unwiseCutouts;
    private JCheckBox autoContrast;
    private JCheckBox keepContrast;
    private JCheckBox blurImages;
    private JCheckBox invertColors;
    private JCheckBox borderFirst;
    private JCheckBox staticView;
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
    private JCheckBox spitzerOverlay;
    private JCheckBox noirlabOverlay;
    private JCheckBox showBrownDwarfsOnly;
    private JCheckBox displaySpectralTypes;
    private JCheckBox ssoOverlay;
    private JCheckBox ghostOverlay;
    private JCheckBox haloOverlay;
    private JCheckBox latentOverlay;
    private JCheckBox spikeOverlay;
    private JCheckBox gaiaProperMotion;
    private JCheckBox gaiaDR3ProperMotion;
    private JCheckBox catWiseProperMotion;
    private JCheckBox noirlabProperMotion;
    private JCheckBox useCustomOverlays;
    private JCheckBox dssImages;
    private JCheckBox twoMassImages;
    private JCheckBox sloanImages;
    private JCheckBox allwiseImages;
    private JCheckBox ps1Images;
    private JCheckBox staticTimeSeries;
    private JCheckBox animatedTimeSeries;
    private JCheckBox createDataSheet;
    private JCheckBox skipBadImages;
    private JCheckBox skipSingleNodes;
    private JCheckBox optimizeContrast;
    private JCheckBox hideMagnifier;
    private JCheckBox drawCrosshairs;
    private JCheckBox checkProperMotion;
    private JCheckBox useAboveCoords;
    private JCheckBox useGaiaPM;
    private JCheckBox useCatwisePM;
    private JCheckBox useNoirlabPM;
    private JCheckBox transposeProperMotion;
    private JComboBox wiseBands;
    private JComboBox epochs;
    private JSlider highScaleSlider;
    private JSlider lowScaleSlider;
    private JSlider subScaleSlider;
    private JSlider speedSlider;
    private JSlider zoomSlider;
    private JSlider epochSlider;
    private JTextField coordsField;
    private JTextField sizeField;
    private JTextField properMotionField;
    private JTextField differentSizeField;
    private JTextField checkObjectCoordsField;
    private JTextField checkObjectMotionField;
    private JTextField transposeMotionField;
    private JTextArea crosshairCoords;
    private JTextArea downloadLog;
    private JRadioButton differentSizeButton;
    private JRadioButton showCatalogsButton;
    private JTable collectionTable;
    private Timer timer;

    private BufferedImage wiseImage;
    private BufferedImage ps1Image;
    private BufferedImage sdssImage;
    private BufferedImage processedPs1Image;
    private BufferedImage processedSdssImage;
    private Map<String, ImageContainer> imagesW1 = new HashMap<>();
    private Map<String, ImageContainer> imagesW2 = new HashMap<>();
    private Map<String, Fits> images;
    private Map<String, CustomOverlay> customOverlays;
    private List<Integer> requestedEpochs;
    private List<NumberPair> crosshairs;
    private FlipbookComponent[] flipbook;
    private ImageViewerTab imageViewer;
    private CatalogEntry pmCatalogEntry;

    private WiseBand wiseBand = WISE_BAND;
    private Epoch epoch = EPOCH;
    //private double rotationAngle;
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
    private int speed = SPEED;
    private int zoom = ZOOM;
    private int size = SIZE;

    private int highContrast = HIGH_CONTRAST;
    private int lowContrast = LOW_CONTRAST;
    private int subContrast = SUB_CONTRAST;

    private int highContrastSaved = highContrast;
    private int lowContrastSaved = lowContrast;
    private int subContrastSaved = subContrast;

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
    private boolean oneMoreImageAvailable;
    private boolean flipbookComplete;
    private boolean reloadImages;
    private boolean imageCutOff;
    private boolean timerStopped;
    private boolean hasException;
    private boolean asyncDownloads;
    private boolean panstarrsImages;
    private boolean sdssImages;

    public static final List<String> BROWN_DWARFS = new ArrayList<>();

    static {
        for (int i = 5; i < 10; i++) {
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
        BROWN_DWARFS.add(spt + i);
        BROWN_DWARFS.add(spt + i + ".5");
        BROWN_DWARFS.add(spt + i + "V");
        BROWN_DWARFS.add(spt + i + ".5V");
    }

    public ImageViewerTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        catalogQueryFacade = new CatalogQueryService();
        InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(SPLIT_CHAR, 30));
            }).collect(Collectors.toList());
            mainSequenceSpectralTypeLookupService = new SpectralTypeLookupService(entries);
        }
        input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new BrownDwarfLookupEntry(line.split(SPLIT_CHAR, 22));
            }).collect(Collectors.toList());
            brownDwarfsSpectralTypeLookupService = new SpectralTypeLookupService(entries);
            distanceLookupService = new DistanceLookupService(entries);
        }
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new BorderLayout());

            JPanel leftPanel = new JPanel();
            mainPanel.add(leftPanel, BorderLayout.WEST);
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBorder(new EmptyBorder(0, 5, 5, 20));

            imagePanel = new JPanel();
            imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));

            JScrollPane imageScrollPanel = new JScrollPane(imagePanel);
            mainPanel.add(imageScrollPanel, BorderLayout.CENTER);
            imageScrollPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            rightPanel = new JPanel();
            mainPanel.add(rightPanel, BorderLayout.EAST);
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBorder(new EmptyBorder(20, 0, 5, 5));

            int rows = 96;
            int controlPanelWidth = 250;
            int controlPanelHeight = 10 + ROW_HEIGHT * rows;

            JPanel controlPanel = new JPanel(new GridLayout(rows, 1));
            controlPanel.setPreferredSize(new Dimension(controlPanelWidth - 20, controlPanelHeight));
            controlPanel.setBorder(new EmptyBorder(0, 5, 0, 10));

            JScrollPane controlScrollPanel = new JScrollPane(controlPanel);
            controlScrollPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));
            controlScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            leftPanel.add(controlScrollPanel);

            controlPanel.add(new JLabel("Coordinates:"));

            coordsField = new JTextField();
            controlPanel.add(coordsField);
            coordsField.addActionListener((ActionEvent evt) -> {
                coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                createFlipbook();
                coordsField.setCursor(Cursor.getDefaultCursor());
            });

            controlPanel.add(new JLabel("Field of view (arcsec):"));

            sizeField = new JTextField(String.valueOf(size));
            controlPanel.add(sizeField);
            sizeField.addActionListener((ActionEvent evt) -> {
                sizeField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                createFlipbook();
                sizeField.setCursor(Cursor.getDefaultCursor());
            });

            controlPanel.add(new JLabel("Bands:"));

            wiseBands = new JComboBox<>(WiseBand.values());
            controlPanel.add(wiseBands);
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

            controlPanel.add(new JLabel("Epochs:"));

            epochs = new JComboBox<>(Epoch.values());
            controlPanel.add(epochs);
            epochs.setMaximumRowCount(Epoch.values().length);
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
                    autoContrast.setSelected(true);
                    optimizeContrast.setSelected(false);
                    blurImages.setSelected(true);
                    setContrast(LOW_CONTRAST, HIGH_CONTRAST);
                    setSubContrast(subContrastSaved);
                } else if (Epoch.isSubtracted(previousEpoch)) {
                    optimizeContrast.setSelected(true);
                    blurImages.setSelected(false);
                    setContrast(lowContrastSaved, highContrastSaved);
                    setSubContrast(SUB_CONTRAST);
                }
                createFlipbook();
            });

            JPanel whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            highScaleLabel = new JLabel(String.format(HIGH_SCALE_LABEL, highContrast));
            whitePanel.add(highScaleLabel);

            highScaleSlider = new JSlider(0, 1000, HIGH_CONTRAST);
            controlPanel.add(highScaleSlider);
            highScaleSlider.setBackground(Color.WHITE);
            highScaleSlider.addChangeListener((ChangeEvent e) -> {
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                highContrast = highScaleSlider.getValue();
                highScaleLabel.setText(String.format(HIGH_SCALE_LABEL, highContrast));
                if (!Epoch.isSubtracted(epoch)) {
                    highContrastSaved = highContrast;
                }
                if (lowContrast + highContrast == 0) {
                    setContrast(10, HIGH_CONTRAST);
                }
                processImages();
            });

            JPanel grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            lowScaleLabel = new JLabel(String.format(LOW_SCALE_LABEL, lowContrast));
            grayPanel.add(lowScaleLabel);

            lowScaleSlider = new JSlider(0, 100, LOW_CONTRAST);
            controlPanel.add(lowScaleSlider);
            lowScaleSlider.setBackground(Color.LIGHT_GRAY);
            lowScaleSlider.addChangeListener((ChangeEvent e) -> {
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                lowContrast = lowScaleSlider.getValue();
                lowScaleLabel.setText(String.format(LOW_SCALE_LABEL, lowContrast));
                if (!Epoch.isSubtracted(epoch)) {
                    lowContrastSaved = lowContrast;
                }
                if (lowContrast + highContrast == 0) {
                    autoContrast.setSelected(false);
                    setContrast(10, HIGH_CONTRAST);
                    createFlipbook();
                } else {
                    processImages();
                }
            });

            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            subScaleLabel = new JLabel(String.format(SUB_SCALE_LABEL, subContrast));
            whitePanel.add(subScaleLabel);

            subScaleSlider = new JSlider(1, 10, SUB_CONTRAST);
            controlPanel.add(subScaleSlider);
            subScaleSlider.setBackground(Color.WHITE);
            subScaleSlider.addChangeListener((ChangeEvent e) -> {
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                subContrast = subScaleSlider.getValue();
                subScaleLabel.setText(String.format(SUB_SCALE_LABEL, subContrast));
                if (Epoch.isSubtracted(epoch)) {
                    subContrastSaved = subContrast;
                }
                processImages();
            });

            grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            JLabel speedLabel = new JLabel(String.format("Playback speed: %d ms", speed));
            grayPanel.add(speedLabel);

            speedSlider = new JSlider(0, 2000, SPEED);
            controlPanel.add(speedSlider);
            speedSlider.setBackground(Color.LIGHT_GRAY);
            speedSlider.addChangeListener((ChangeEvent e) -> {
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                speed = speedSlider.getValue();
                timer.setDelay(speed);
                speedLabel.setText(String.format("Playback speed: %d ms", speed));
                processImages();
            });

            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel zoomLabel = new JLabel(String.format("Image zoom: %d", zoom));
            whitePanel.add(zoomLabel);

            zoomSlider = new JSlider(0, 2000, ZOOM);
            controlPanel.add(zoomSlider);
            zoomSlider.setBackground(Color.WHITE);
            zoomSlider.addChangeListener((ChangeEvent e) -> {
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                zoom = zoomSlider.getValue();
                zoom = zoom < 100 ? 100 : zoom;
                zoomLabel.setText(String.format("Image zoom: %d", zoom));
                processImages();
            });

            grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            epochLabel = new JLabel(String.format(EPOCH_LABEL, selectedEpochs));
            grayPanel.add(epochLabel);

            epochSlider = new JSlider(2, NUMBER_OF_EPOCHS, NUMBER_OF_EPOCHS);
            controlPanel.add(epochSlider);
            epochSlider.setBackground(Color.LIGHT_GRAY);
            epochSlider.addChangeListener((ChangeEvent e) -> {
                JSlider source = (JSlider) e.getSource();
                if (source.getValueIsAdjusting()) {
                    return;
                }
                selectedEpochs = epochSlider.getValue();
                epochLabel.setText(String.format(EPOCH_LABEL, selectedEpochs));
                reloadImages = true;
                createFlipbook();
            });

            unwiseCutouts = new JCheckBox("Use unwise.me cutouts (ASC=DESC!)");
            controlPanel.add(unwiseCutouts);
            unwiseCutouts.addActionListener((ActionEvent evt) -> {
                imagesW1.clear();
                imagesW2.clear();
                reloadImages = true;
                createFlipbook();
            });

            JPanel settingsPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(settingsPanel);

            autoContrast = new JCheckBox("Auto-contrast", true);
            settingsPanel.add(autoContrast);
            autoContrast.addActionListener((ActionEvent evt) -> {
                setContrast(lowContrastSaved, highContrastSaved);
                setSubContrast(subContrastSaved);
                createFlipbook();
            });

            keepContrast = new JCheckBox("Keep contrast");
            settingsPanel.add(keepContrast);
            keepContrast.addActionListener((ActionEvent evt) -> {
                if (keepContrast.isSelected()) {
                    if (Epoch.isSubtracted(epoch)) {
                        subContrastSaved = subContrast;
                    } else {
                        highContrastSaved = highContrast;
                        lowContrastSaved = lowContrast;
                    }
                }
            });

            settingsPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(settingsPanel);

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
            controlPanel.add(settingsPanel);

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

            showCrosshairs = new JCheckBox("Show crosshairs with coords (*)");
            controlPanel.add(showCrosshairs);

            JLabel copyCoordsLabel = new JLabel("(*) Click object to copy coords to clipboard");
            Font font = copyCoordsLabel.getFont();
            font = font.deriveFont(9f);
            copyCoordsLabel.setFont(font);
            controlPanel.add(copyCoordsLabel);

            JButton resetDefaultsButton = new JButton("Image processing defaults");
            controlPanel.add(resetDefaultsButton);
            resetDefaultsButton.addActionListener((ActionEvent evt) -> {
                autoContrast.setSelected(true);
                if (Epoch.isSubtracted(epoch)) {
                    blurImages.setSelected(true);
                } else {
                    blurImages.setSelected(false);
                }
                setContrast(LOW_CONTRAST, HIGH_CONTRAST);
                setSubContrast(SUB_CONTRAST);
                createFlipbook();
            });

            controlPanel.add(new JLabel(header("Overlays:")));

            JPanel overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            simbadOverlay = new JCheckBox(SimbadCatalogEntry.CATALOG_NAME);
            simbadOverlay.setForeground(Color.RED);
            simbadOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(simbadOverlay);
            gaiaOverlay = new JCheckBox(GaiaCatalogEntry.CATALOG_NAME);
            gaiaOverlay.setForeground(Color.CYAN.darker());
            gaiaOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(gaiaOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            allWiseOverlay = new JCheckBox(AllWiseCatalogEntry.CATALOG_NAME);
            allWiseOverlay.setForeground(Color.GREEN.darker());
            allWiseOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(allWiseOverlay);
            catWiseOverlay = new JCheckBox(CatWiseCatalogEntry.CATALOG_NAME);
            catWiseOverlay.setForeground(Color.MAGENTA);
            catWiseOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(catWiseOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            unWiseOverlay = new JCheckBox(UnWiseCatalogEntry.CATALOG_NAME);
            unWiseOverlay.setForeground(JColor.MINT.val);
            unWiseOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(unWiseOverlay);
            panStarrsOverlay = new JCheckBox(PanStarrsCatalogEntry.CATALOG_NAME);
            panStarrsOverlay.setForeground(JColor.BROWN.val);
            panStarrsOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(panStarrsOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            sdssOverlay = new JCheckBox(SDSSCatalogEntry.CATALOG_NAME);
            sdssOverlay.setForeground(JColor.STEEL.val);
            sdssOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(sdssOverlay);
            spectrumOverlay = new JCheckBox("SDSS spectra");
            spectrumOverlay.setForeground(JColor.OLIVE.val);
            spectrumOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(spectrumOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            gaiaWDOverlay = new JCheckBox(GaiaWDCatalogEntry.CATALOG_NAME);
            gaiaWDOverlay.setForeground(JColor.PURPLE.val);
            gaiaWDOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(gaiaWDOverlay);
            vhsOverlay = new JCheckBox(VHSCatalogEntry.CATALOG_NAME);
            vhsOverlay.setForeground(JColor.PINK.val);
            vhsOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(vhsOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            twoMassOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;" + TwoMassCatalogEntry.CATALOG_NAME + "&nbsp;</span>"));
            twoMassOverlay.setForeground(JColor.ORANGE.val);
            twoMassOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(twoMassOverlay);
            spitzerOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;" + SpitzerCatalogEntry.CATALOG_NAME + "&nbsp;</span>"));
            spitzerOverlay.setForeground(JColor.YELLOW.val);
            spitzerOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(spitzerOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            gaiaDR3Overlay = new JCheckBox(GaiaDR3CatalogEntry.CATALOG_NAME);
            gaiaDR3Overlay.setForeground(Color.CYAN.darker());
            gaiaDR3Overlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            if (LocalDate.now().isAfter(GAIA_DR3_RELEASE_DATE)) {
                gaiaDR3Overlay.setEnabled(true);
            } else {
                gaiaDR3Overlay.setEnabled(false);
            }
            overlayPanel.add(gaiaDR3Overlay);
            noirlabOverlay = new JCheckBox(NoirlabCatalogEntry.CATALOG_NAME);
            noirlabOverlay.setForeground(JColor.NAVY.val);
            noirlabOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            overlayPanel.add(noirlabOverlay);

            ssoOverlay = new JCheckBox(SSOCatalogEntry.CATALOG_NAME);
            ssoOverlay.setForeground(Color.BLUE);
            ssoOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            controlPanel.add(ssoOverlay);

            useCustomOverlays = new JCheckBox("Custom overlays:");
            controlPanel.add(useCustomOverlays);
            useCustomOverlays.setBackground(Color.WHITE);
            useCustomOverlays.addActionListener((ActionEvent evt) -> {
                if (customOverlays.isEmpty()) {
                    showInfoDialog(baseFrame, "No custom overlays have been added yet.");
                    useCustomOverlays.setSelected(false);
                } else {
                    GridLayout layout = (GridLayout) controlPanel.getLayout();
                    int numberOfRows = customOverlays.size();
                    int rowsHeight = numberOfRows * ROW_HEIGHT;
                    if (useCustomOverlays.isSelected()) {
                        componentIndex = controlPanel.getComponentZOrder(useCustomOverlays) + 1;
                        layout.setRows(layout.getRows() + numberOfRows);
                        controlPanel.setPreferredSize(new Dimension(controlPanel.getWidth(), controlPanel.getHeight() + rowsHeight));
                        customOverlays.values().forEach(customOverlay -> {
                            JCheckBox checkBox = new JCheckBox(customOverlay.getName());
                            checkBox.setForeground(customOverlay.getColor());
                            checkBox.setBackground(Color.WHITE);
                            checkBox.addActionListener((ActionEvent e) -> {
                                processImages();
                            });
                            customOverlay.setCheckBox(checkBox);
                            controlPanel.add(checkBox, componentIndex++);
                        });
                    } else {
                        componentIndex = controlPanel.getComponentZOrder(useCustomOverlays) + numberOfRows;
                        layout.setRows(layout.getRows() - numberOfRows);
                        controlPanel.setPreferredSize(new Dimension(controlPanel.getWidth(), controlPanel.getHeight() - rowsHeight));
                        customOverlays.values().forEach((customOverlay) -> {
                            controlPanel.remove(componentIndex--);
                            customOverlay.setCatalogEntries(null);
                        });
                        processImages();
                    }
                    controlPanel.updateUI();
                    baseFrame.setVisible(true);
                }
            });

            controlPanel.add(new JLabel(header("PM vectors:")));

            JPanel properMotionPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(properMotionPanel);
            gaiaProperMotion = new JCheckBox(GaiaCatalogEntry.CATALOG_NAME);
            gaiaProperMotion.setForeground(Color.CYAN.darker());
            gaiaProperMotion.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            properMotionPanel.add(gaiaProperMotion);
            catWiseProperMotion = new JCheckBox(CatWiseCatalogEntry.CATALOG_NAME);
            catWiseProperMotion.setForeground(Color.MAGENTA);
            catWiseProperMotion.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            properMotionPanel.add(catWiseProperMotion);

            properMotionPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(properMotionPanel);
            gaiaDR3ProperMotion = new JCheckBox(GaiaDR3CatalogEntry.CATALOG_NAME);
            gaiaDR3ProperMotion.setForeground(Color.CYAN.darker());
            gaiaDR3ProperMotion.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            if (LocalDate.now().isAfter(GAIA_DR3_RELEASE_DATE)) {
                gaiaDR3ProperMotion.setEnabled(true);
            } else {
                gaiaDR3ProperMotion.setEnabled(false);
            }
            properMotionPanel.add(gaiaDR3ProperMotion);
            noirlabProperMotion = new JCheckBox(NoirlabCatalogEntry.CATALOG_NAME);
            noirlabProperMotion.setForeground(JColor.NAVY.val);
            noirlabProperMotion.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            properMotionPanel.add(noirlabProperMotion);

            properMotionPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(properMotionPanel);
            properMotionPanel.add(new JLabel("Total PM (mas/yr) >"));
            properMotionField = new JTextField(String.valueOf(100));
            properMotionPanel.add(properMotionField);
            properMotionField.addActionListener((ActionEvent evt) -> {
                gaiaTpmEntries = null;
                gaiaDR3TpmEntries = null;
                catWiseTpmEntries = null;
                noirlabTpmEntries = null;
                processImages();
            });

            controlPanel.add(new JLabel(header("WISE artifacts (*):")));

            JPanel artifactPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(artifactPanel);
            ghostOverlay = new JCheckBox("Ghosts");
            ghostOverlay.setForeground(Color.MAGENTA.darker());
            ghostOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            artifactPanel.add(ghostOverlay);
            haloOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;Halos&nbsp;</span>"));
            haloOverlay.setForeground(Color.YELLOW);
            haloOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            artifactPanel.add(haloOverlay);

            artifactPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(artifactPanel);
            latentOverlay = new JCheckBox("Latents");
            latentOverlay.setForeground(Color.GREEN.darker());
            latentOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            artifactPanel.add(latentOverlay);
            spikeOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;Spikes&nbsp;</span>"));
            spikeOverlay.setForeground(Color.ORANGE);
            spikeOverlay.addActionListener((ActionEvent evt) -> {
                processImages();
            });
            artifactPanel.add(spikeOverlay);

            JLabel artifactLabel = new JLabel(html("(*) Small shapes represent affected sources." + LINE_BREAK + "Large shapes represent the actual artifacts."));
            artifactLabel.setFont(font);
            controlPanel.add(artifactLabel);

            controlPanel.add(new JLabel(header("Mouse left click w/o overlays:")));

            showCatalogsButton = new JRadioButton("Show catalog entries for object", true);
            controlPanel.add(showCatalogsButton);

            JRadioButton recenterImagesButton = new JRadioButton("Recenter images on object", false);
            controlPanel.add(recenterImagesButton);

            ButtonGroup groupOne = new ButtonGroup();
            groupOne.add(showCatalogsButton);
            groupOne.add(recenterImagesButton);

            controlPanel.add(new JLabel(header("Mouse wheel click:")));

            controlPanel.add(new JLabel("Select images to display:"));

            dssImages = new JCheckBox("DSS 1Red, 1Blue, 2Red, 2Blue, 2IR", false);
            controlPanel.add(dssImages);
            dssImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            twoMassImages = new JCheckBox("2MASS J, H & K bands", false);
            controlPanel.add(twoMassImages);
            twoMassImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            sloanImages = new JCheckBox("SDSS u, g, r, i & z bands", false);
            controlPanel.add(sloanImages);
            sloanImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            allwiseImages = new JCheckBox("AllWISE W1, W2, W3 & W4 bands", true);
            controlPanel.add(allwiseImages);
            allwiseImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            ps1Images = new JCheckBox("Pan-STARRS g, r, i, z & y bands", false);
            controlPanel.add(ps1Images);
            ps1Images.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            staticTimeSeries = new JCheckBox("Time series - static", false);
            controlPanel.add(staticTimeSeries);
            staticTimeSeries.addActionListener((ActionEvent evt) -> {
                if (animatedTimeSeries.isSelected()) {
                    dssImages.setSelected(false);
                    twoMassImages.setSelected(false);
                    sloanImages.setSelected(false);
                    allwiseImages.setSelected(false);
                    ps1Images.setSelected(false);
                    animatedTimeSeries.setSelected(false);
                }
                createDataSheet.setSelected(false);
            });

            animatedTimeSeries = new JCheckBox("Time series - animated", false);
            controlPanel.add(animatedTimeSeries);
            animatedTimeSeries.addActionListener((ActionEvent evt) -> {
                if (animatedTimeSeries.isSelected()) {
                    dssImages.setSelected(true);
                    twoMassImages.setSelected(true);
                    sloanImages.setSelected(true);
                    allwiseImages.setSelected(true);
                    ps1Images.setSelected(true);
                    staticTimeSeries.setSelected(false);
                } else {
                    dssImages.setSelected(false);
                    twoMassImages.setSelected(false);
                    sloanImages.setSelected(false);
                    allwiseImages.setSelected(false);
                    ps1Images.setSelected(false);
                }
                createDataSheet.setSelected(false);
            });

            createDataSheet = new JCheckBox("Object info sheet", false);
            controlPanel.add(createDataSheet);
            createDataSheet.addActionListener((ActionEvent evt) -> {
                if (createDataSheet.isSelected()) {
                    setImageViewer(this);
                    dssImages.setSelected(false);
                    twoMassImages.setSelected(false);
                    sloanImages.setSelected(false);
                    allwiseImages.setSelected(false);
                    ps1Images.setSelected(false);
                    staticTimeSeries.setSelected(false);
                    animatedTimeSeries.setSelected(false);
                }
            });

            changeFovLabel = new JLabel(String.format(CHANGE_FOV_TEXT, fieldOfView));
            controlPanel.add(changeFovLabel);

            JLabel fovLabel = new JLabel("(*) Spin wheel on WISE images to change FoV");
            fovLabel.setFont(font);
            controlPanel.add(fovLabel);

            controlPanel.add(new JLabel(header("Mouse right click:")));

            differentSizeButton = new JRadioButton("Show object in different FoV", true);
            controlPanel.add(differentSizeButton);

            ButtonGroup groupTwo = new ButtonGroup();
            groupTwo.add(differentSizeButton);

            JPanel differentSizePanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(differentSizePanel);
            differentSizePanel.add(new JLabel("Enter FoV (arcsec)"));
            differentSizeField = new JTextField(String.valueOf(100));
            differentSizePanel.add(differentSizeField);

            controlPanel.add(new JLabel(header("Nearest Zooniverse subjects:")));

            zooniversePanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(zooniversePanel1);

            zooniversePanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(zooniversePanel2);

            controlPanel.add(new JLabel(header("Advanced controls:")));

            /*==================================================================
            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel overlayLabel = new JLabel(String.format("Correct overlay rotation by: %s°", roundTo1DecLZ(rotationAngle)));
            whitePanel.add(overlayLabel);

            JSlider overlaySlider = new JSlider(-50, 50, 0);
            controlPanel.add(overlaySlider);
            overlaySlider.setBackground(Color.WHITE);
            overlaySlider.addChangeListener((ChangeEvent e) -> {
                rotationAngle = overlaySlider.getValue();
                rotationAngle /= 10;
                overlayLabel.setText(String.format("Correct overlay rotation by: %s°", roundTo1DecLZ(rotationAngle)));
                processImages();
            });
            ==================================================================*/
            skipBadImages = new JCheckBox("Skip bad quality images", true);
            controlPanel.add(skipBadImages);
            skipBadImages.addActionListener((ActionEvent evt) -> {
                if (!skipBadImages.isSelected()) {
                    showWarnDialog(baseFrame, "Unchecking this may decrease image quality and lead to poorer motion detection!");
                }
                imagesW1.clear();
                imagesW2.clear();
                reloadImages = true;
                createFlipbook();
            });

            skipSingleNodes = new JCheckBox("Skip single nodes", true);
            controlPanel.add(skipSingleNodes);
            skipSingleNodes.addActionListener((ActionEvent evt) -> {
                if (!skipSingleNodes.isSelected()) {
                    showWarnDialog(baseFrame, "Unchecking this may affect image ordering and lead to poorer motion detection, especially in subtracted modes!");
                }
                imagesW1.clear();
                imagesW2.clear();
                reloadImages = true;
                createFlipbook();
            });

            optimizeContrast = new JCheckBox("Optimize contrast", true);
            controlPanel.add(optimizeContrast);
            optimizeContrast.addActionListener((ActionEvent evt) -> {
                createFlipbook();
            });

            hideMagnifier = new JCheckBox("Hide magnifier panel");
            controlPanel.add(hideMagnifier);
            hideMagnifier.addActionListener((ActionEvent evt) -> {
                if (hideMagnifier.isSelected()) {
                    rightPanel.setVisible(false);
                } else {
                    rightPanel.setVisible(true);
                }
            });

            drawCrosshairs = new JCheckBox("Draw crosshairs (*)");
            controlPanel.add(drawCrosshairs);
            drawCrosshairs.addActionListener((ActionEvent evt) -> {
                if (!drawCrosshairs.isSelected()) {
                    crosshairs.clear();
                    crosshairCoords.setText("");
                }
            });

            JLabel crosshairLabel = new JLabel(html("(*) Wheel click on object / Spin wheel to change" + LINE_BREAK + "the size / Wheel click on cross center to delete"));
            crosshairLabel.setFont(font);
            controlPanel.add(crosshairLabel);

            controlPanel.add(new JLabel("Crosshairs coordinates:"));

            crosshairCoords = new JTextArea();
            controlPanel.add(new JScrollPane(crosshairCoords));
            crosshairCoords.setFont(font);
            crosshairCoords.setEditable(false);
            crosshairCoords.setBackground(new JLabel().getBackground());

            controlPanel.add(new JLabel(header("Image player controls:")));

            JPanel playerControls = new JPanel(new GridLayout(1, 2));
            controlPanel.add(playerControls);

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
            controlPanel.add(playerControls);

            JButton backwardButton = new JButton("Backward");
            playerControls.add(backwardButton);
            backwardButton.addActionListener((ActionEvent evt) -> {
                timer.stop();
                imageNumber -= 2;
                if (imageNumber < 0) {
                    imageNumber = flipbook.length - 1;
                }
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

            JButton rotateButton = new JButton(String.format("Rotate by 90° clockwise: %d°", quadrantCount * 90));
            controlPanel.add(rotateButton);
            rotateButton.addActionListener((ActionEvent evt) -> {
                quadrantCount++;
                if (quadrantCount > 3) {
                    quadrantCount = 0;
                }
                rotateButton.setText(String.format("Rotate by 90° clockwise: %d°", quadrantCount * 90));
                processImages();
            });

            JPanel saveControls = new JPanel(new GridLayout(1, 2));
            controlPanel.add(saveControls);

            JButton saveAsPngButton = new JButton("Save as PNG");
            saveControls.add(saveAsPngButton);
            saveAsPngButton.addActionListener((ActionEvent evt) -> {
                try {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileFilter(new FileTypeFilter(".png", ".png files"));
                    int returnVal = fileChooser.showSaveDialog(controlPanel);
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
                    int returnVal = fileChooser.showSaveDialog(controlPanel);
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

            controlPanel.add(new JLabel(header("Navigation buttons:")));

            JPanel navigationButtons = new JPanel(new GridLayout(1, 2));
            controlPanel.add(navigationButtons);

            JButton moveLeftButton = new JButton("Move left");
            navigationButtons.add(moveLeftButton);
            moveLeftButton.addActionListener((ActionEvent evt) -> {
                double distance = size * PIXEL_SIZE * OVERLAP_FACTOR / DEG_ARCSEC;
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
                double distance = size * PIXEL_SIZE * OVERLAP_FACTOR / DEG_ARCSEC;
                NumberPair coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec), new NumberPair(-distance, 0));
                double newRa = coords.getX();
                newRa = newRa < 0 ? newRa + 360 : newRa;
                newRa = newRa < 0 ? 0 : newRa;
                coordsField.setText(roundTo7DecNZLZ(newRa) + " " + roundTo7DecNZLZ(targetDec));
                createFlipbook();
            });

            navigationButtons = new JPanel(new GridLayout(1, 2));
            controlPanel.add(navigationButtons);

            JButton moveUpButton = new JButton("Move up");
            navigationButtons.add(moveUpButton);
            moveUpButton.addActionListener((ActionEvent evt) -> {
                double newDec = targetDec + size * PIXEL_SIZE * OVERLAP_FACTOR / DEG_ARCSEC;
                if (newDec > 90) {
                    newDec = 90 - (newDec - 90);
                    double newRa = targetRa + 180;
                    targetRa = newRa > 360 ? newRa - 360 : newRa;
                    showInfoDialog(baseFrame, "You're about to cross the North Celestial Pole." + LINE_SEP + "If you want to move on in the current direction, use the 'Move down' button next!");
                }
                coordsField.setText(roundTo7DecNZLZ(targetRa) + " " + roundTo7DecNZLZ(newDec));
                createFlipbook();
            });

            JButton moveDownButton = new JButton("Move down");
            navigationButtons.add(moveDownButton);
            moveDownButton.addActionListener((ActionEvent evt) -> {
                double newDec = targetDec - size * PIXEL_SIZE * OVERLAP_FACTOR / DEG_ARCSEC;
                if (newDec < -90) {
                    newDec = -90 + (abs(newDec) - 90);
                    double newRa = targetRa + 180;
                    targetRa = newRa > 360 ? newRa - 360 : newRa;
                    showInfoDialog(baseFrame, "You're about to cross the South Celestial Pole." + LINE_SEP + "If you want to move on in the current direction, use the 'Move up' button next!");
                }
                coordsField.setText(roundTo7DecNZLZ(targetRa) + " " + roundTo7DecNZLZ(newDec));
                createFlipbook();
            });

            checkProperMotion = new JCheckBox(header("Check proper motion:"));
            controlPanel.add(checkProperMotion);
            checkProperMotion.addActionListener((ActionEvent evt) -> {
                displayMotionChecker();
            });

            checkObjectCoordsField = new JTextField();
            controlPanel.add(checkObjectCoordsField);
            TextPrompt checkObjectCoordsPrompt = new TextPrompt("RA & Dec of object to check");
            checkObjectCoordsPrompt.applyTo(checkObjectCoordsField);
            checkObjectCoordsField.addActionListener((ActionEvent evt) -> {
                useAboveCoords.setSelected(false);
                displayMotionChecker();
            });

            useAboveCoords = new JCheckBox("Or use above coordinates");
            controlPanel.add(useAboveCoords);
            useAboveCoords.addActionListener((ActionEvent evt) -> {
                if (useAboveCoords.isSelected() && !coordsField.getText().isEmpty()) {
                    checkObjectCoordsField.setText(coordsField.getText());
                }
            });

            checkObjectMotionField = new JTextField();
            controlPanel.add(checkObjectMotionField);
            TextPrompt checkObjectMotionPrompt = new TextPrompt("pmRA & pmDec of object to check");
            checkObjectMotionPrompt.applyTo(checkObjectMotionField);
            checkObjectMotionField.addActionListener((ActionEvent evt) -> {
                useGaiaPM.setSelected(false);
                useCatwisePM.setSelected(false);
                useNoirlabPM.setSelected(false);
                displayMotionChecker();
            });

            JLabel pmLabel = new JLabel("  Or use proper motions from:");
            controlPanel.add(pmLabel);

            JPanel checkerPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(checkerPanel);

            useGaiaPM = new JCheckBox(GaiaCatalogEntry.CATALOG_NAME);
            checkerPanel.add(useGaiaPM);

            useCatwisePM = new JCheckBox(CatWiseCatalogEntry.CATALOG_NAME);
            checkerPanel.add(useCatwisePM);

            checkerPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(checkerPanel);

            useNoirlabPM = new JCheckBox(NoirlabCatalogEntry.CATALOG_NAME);
            checkerPanel.add(useNoirlabPM);

            useGaiaPM.addActionListener((ActionEvent evt) -> {
                if (useGaiaPM.isSelected() && !checkObjectCoordsField.getText().isEmpty()) {
                    useCatwisePM.setSelected(false);
                    useNoirlabPM.setSelected(false);
                    applyProperMotion(new GaiaCatalogEntry());
                }
                displayMotionChecker();
            });

            useCatwisePM.addActionListener((ActionEvent evt) -> {
                if (useCatwisePM.isSelected() && !checkObjectCoordsField.getText().isEmpty()) {
                    useGaiaPM.setSelected(false);
                    useNoirlabPM.setSelected(false);
                    applyProperMotion(new CatWiseCatalogEntry());
                }
                displayMotionChecker();
            });

            useNoirlabPM.addActionListener((ActionEvent evt) -> {
                if (useNoirlabPM.isSelected() && !checkObjectCoordsField.getText().isEmpty()) {
                    useGaiaPM.setSelected(false);
                    useCatwisePM.setSelected(false);
                    applyProperMotion(new NoirlabCatalogEntry());
                }
                displayMotionChecker();
            });

            transposeProperMotion = new JCheckBox(header("Transpose proper motion:"));
            controlPanel.add(transposeProperMotion);
            transposeProperMotion.addActionListener((ActionEvent evt) -> {
                if (!transposeMotionField.getText().isEmpty()) {
                    imagesW1.clear();
                    imagesW2.clear();
                    reloadImages = true;
                    createFlipbook();
                }
            });

            transposeMotionField = new JTextField();
            controlPanel.add(transposeMotionField);
            TextPrompt transposeMotionPrompt = new TextPrompt("pmRA & pmDec to transpose");
            transposeMotionPrompt.applyTo(transposeMotionField);
            transposeMotionField.addActionListener((ActionEvent evt) -> {
                if (transposeProperMotion.isSelected() && !transposeMotionField.getText().isEmpty()) {
                    imagesW1.clear();
                    imagesW2.clear();
                    reloadImages = true;
                    createFlipbook();
                }
            });

            controlPanel.add(new JLabel(header("Experimental features (*):")));

            displaySpectralTypes = new JCheckBox("Display estimated spectral types");
            controlPanel.add(displaySpectralTypes);
            displaySpectralTypes.addActionListener((ActionEvent evt) -> {
                if (displaySpectralTypes.isSelected() && !isCatalogOverlaySelected()) {
                    gaiaOverlay.setSelected(true);
                }
                initCatalogEntries();
                processImages();
            });

            showBrownDwarfsOnly = new JCheckBox("Show potential brown dwarfs only");
            controlPanel.add(showBrownDwarfsOnly);
            showBrownDwarfsOnly.addActionListener((ActionEvent evt) -> {
                if (showBrownDwarfsOnly.isSelected() && !isCatalogOverlaySelected()) {
                    gaiaOverlay.setSelected(true);
                }
                initCatalogEntries();
                processImages();
            });

            JLabel warning = new JLabel(html("(*) Warning: Spectral type estimates are based" + LINE_BREAK + "on single colors only and may be wrong."));
            warning.setForeground(Color.RED);
            warning.setFont(font);
            controlPanel.add(warning);

            timer = new Timer(speed, (ActionEvent e) -> {
                try {
                    staticView.setSelected(false);
                    if (imageNumber > flipbook.length - 1) {
                        imageNumber = 0;
                    }

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
                    imagePanel.setBorder(createEtchedBorder(component.getTitle()));
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
                    if (!hideMagnifier.isSelected()) {
                        rightPanel.removeAll();
                        rightPanel.repaint();
                        BufferedImage magnifiedWiseImage = wiseImage.getSubimage(upperLeftX, upperLeftY, width, height);
                        magnifiedWiseImage = zoom(magnifiedWiseImage, 200);
                        rightPanel.add(new JLabel(new ImageIcon(magnifiedWiseImage)));
                    }

                    // Display Pan-STARRS images
                    JLabel ps1Label = null;
                    if (processedPs1Image != null) {
                        // Create and display magnified Pan-STARRS image
                        if (!hideMagnifier.isSelected() && !imageCutOff) {
                            BufferedImage magnifiedPs1Image = processedPs1Image.getSubimage(upperLeftX, upperLeftY, width, height);
                            magnifiedPs1Image = zoom(magnifiedPs1Image, 200);
                            rightPanel.add(new JLabel(new ImageIcon(magnifiedPs1Image)));
                        }

                        // Display regular Pan-STARRS image
                        ps1Label = new JLabel(new ImageIcon(processedPs1Image));
                        ps1Label.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                        imagePanel.add(ps1Label);
                    }

                    // Display SDSS images
                    if (processedSdssImage != null) {
                        // Create and display magnified SDSS image
                        if (!hideMagnifier.isSelected() && !imageCutOff) {
                            BufferedImage magnifiedSdssImage = processedSdssImage.getSubimage(upperLeftX, upperLeftY, width, height);
                            magnifiedSdssImage = zoom(magnifiedSdssImage, 200);
                            rightPanel.add(new JLabel(new ImageIcon(magnifiedSdssImage)));
                        }

                        // Display regular SDSS image
                        JLabel sdssLabel = new JLabel(new ImageIcon(processedSdssImage));
                        sdssLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                        imagePanel.add(sdssLabel);
                    }

                    baseFrame.setVisible(true);
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
                                    if (differentSizeButton.isSelected()) {
                                        CompletableFuture.supplyAsync(() -> openNewImageViewer(newRa, newDec));
                                    }
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
                                            int numberOfPanels = 0;
                                            if (dssImages.isSelected()) {
                                                numberOfPanels++;
                                            }
                                            if (twoMassImages.isSelected()) {
                                                numberOfPanels++;
                                            }
                                            if (sloanImages.isSelected()) {
                                                numberOfPanels++;
                                            }
                                            if (allwiseImages.isSelected()) {
                                                numberOfPanels++;
                                            }
                                            if (ps1Images.isSelected()) {
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
                                            if (dssImages.isSelected()) {
                                                displayDssImages(newRa, newDec, fieldOfView, counter);
                                            }
                                            if (twoMassImages.isSelected()) {
                                                display2MassImages(newRa, newDec, fieldOfView, counter);
                                            }
                                            if (sloanImages.isSelected()) {
                                                displaySdssImages(newRa, newDec, fieldOfView, counter);
                                            }
                                            if (allwiseImages.isSelected()) {
                                                displayAllwiseImages(newRa, newDec, fieldOfView, counter);
                                            }
                                            if (ps1Images.isSelected()) {
                                                displayPs1Images(newRa, newDec, fieldOfView, counter);
                                            }
                                            if (staticTimeSeries.isSelected()) {
                                                displayStaticTimeSeries(newRa, newDec, fieldOfView, counter);
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    int overlays = 0;
                                    if (simbadOverlay.isSelected() && simbadEntries != null) {
                                        showCatalogInfo(simbadEntries, mouseX, mouseY, Color.RED);
                                        overlays++;
                                    }
                                    if (gaiaOverlay.isSelected() && gaiaEntries != null) {
                                        showCatalogInfo(gaiaEntries, mouseX, mouseY, Color.CYAN.darker());
                                        overlays++;
                                    }
                                    if (gaiaDR3Overlay.isSelected() && gaiaDR3Entries != null) {
                                        showCatalogInfo(gaiaDR3Entries, mouseX, mouseY, Color.CYAN.darker());
                                        overlays++;
                                    }
                                    if (gaiaProperMotion.isSelected() && gaiaTpmEntries != null) {
                                        showPMInfo(gaiaTpmEntries, mouseX, mouseY, Color.CYAN.darker());
                                        overlays++;
                                    }
                                    if (gaiaDR3ProperMotion.isSelected() && gaiaDR3TpmEntries != null) {
                                        showPMInfo(gaiaDR3TpmEntries, mouseX, mouseY, Color.CYAN.darker());
                                        overlays++;
                                    }
                                    if (allWiseOverlay.isSelected() && allWiseEntries != null) {
                                        showCatalogInfo(allWiseEntries, mouseX, mouseY, Color.GREEN.darker());
                                        overlays++;
                                    }
                                    if (catWiseOverlay.isSelected() && catWiseEntries != null) {
                                        showCatalogInfo(catWiseEntries, mouseX, mouseY, Color.MAGENTA);
                                        overlays++;
                                    }
                                    if (catWiseProperMotion.isSelected() && catWiseTpmEntries != null) {
                                        showPMInfo(catWiseTpmEntries, mouseX, mouseY, Color.MAGENTA);
                                        overlays++;
                                    }
                                    if (unWiseOverlay.isSelected() && unWiseEntries != null) {
                                        showCatalogInfo(unWiseEntries, mouseX, mouseY, JColor.MINT.val);
                                        overlays++;
                                    }
                                    if (panStarrsOverlay.isSelected() && panStarrsEntries != null) {
                                        showCatalogInfo(panStarrsEntries, mouseX, mouseY, JColor.BROWN.val);
                                        overlays++;
                                    }
                                    if (sdssOverlay.isSelected() && sdssEntries != null) {
                                        showCatalogInfo(sdssEntries, mouseX, mouseY, JColor.STEEL.val);
                                        overlays++;
                                    }
                                    if (spectrumOverlay.isSelected() && sdssEntries != null) {
                                        showSpectrumInfo(sdssEntries, mouseX, mouseY);
                                        overlays++;
                                    }
                                    if (twoMassOverlay.isSelected() && twoMassEntries != null) {
                                        showCatalogInfo(twoMassEntries, mouseX, mouseY, JColor.ORANGE.val);
                                        overlays++;
                                    }
                                    if (vhsOverlay.isSelected() && vhsEntries != null) {
                                        showCatalogInfo(vhsEntries, mouseX, mouseY, JColor.PINK.val);
                                        overlays++;
                                    }
                                    if (gaiaWDOverlay.isSelected() && gaiaWDEntries != null) {
                                        showCatalogInfo(gaiaWDEntries, mouseX, mouseY, JColor.PURPLE.val);
                                        overlays++;
                                    }
                                    if (spitzerOverlay.isSelected() && spitzerEntries != null) {
                                        showCatalogInfo(spitzerEntries, mouseX, mouseY, JColor.YELLOW.val);
                                        overlays++;
                                    }
                                    if (noirlabOverlay.isSelected() && noirlabEntries != null) {
                                        showCatalogInfo(noirlabEntries, mouseX, mouseY, JColor.NAVY.val);
                                        overlays++;
                                    }
                                    if (noirlabProperMotion.isSelected() && noirlabTpmEntries != null) {
                                        showPMInfo(noirlabTpmEntries, mouseX, mouseY, JColor.NAVY.val);
                                        overlays++;
                                    }
                                    if (ssoOverlay.isSelected() && ssoEntries != null) {
                                        showCatalogInfo(ssoEntries, mouseX, mouseY, Color.BLUE);
                                        overlays++;
                                    }
                                    if (useCustomOverlays.isSelected()) {
                                        for (CustomOverlay customOverlay : customOverlays.values()) {
                                            if (customOverlay.getCheckBox().isSelected()) {
                                                showCatalogInfo(customOverlay.getCatalogEntries(), mouseX, mouseY, customOverlay.getColor());
                                                overlays++;
                                            }
                                        }
                                    }
                                    if (overlays == 0) {
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
                        if (checkProperMotion.isSelected() || drawCrosshairs.isSelected() || showCrosshairs.isSelected()) {
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
                            changeFovLabel.setText(String.format(CHANGE_FOV_TEXT, fieldOfView));
                        }
                    });

                    if (ps1Label != null) {
                        ps1Label.addMouseListener(new MouseListener() {
                            @Override
                            public void mousePressed(MouseEvent evt) {
                                try {
                                    Desktop.getDesktop().browse(new URI(getPanstarrsUrl(targetRa, targetDec, fieldOfView)));
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

            tabbedPane.addTab(TAB_NAME, mainPanel);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
            hasException = true;
        }
    }

    private boolean isCatalogOverlaySelected() {
        int overlays = 0;
        if (simbadOverlay.isSelected()) {
            overlays++;
        }
        if (gaiaOverlay.isSelected()) {
            overlays++;
        }
        if (gaiaDR3Overlay.isSelected()) {
            overlays++;
        }
        if (gaiaProperMotion.isSelected()) {
            overlays++;
        }
        if (gaiaDR3ProperMotion.isSelected()) {
            overlays++;
        }
        if (allWiseOverlay.isSelected()) {
            overlays++;
        }
        if (catWiseOverlay.isSelected()) {
            overlays++;
        }
        if (catWiseProperMotion.isSelected()) {
            overlays++;
        }
        if (unWiseOverlay.isSelected()) {
            overlays++;
        }
        if (panStarrsOverlay.isSelected()) {
            overlays++;
        }
        if (sdssOverlay.isSelected()) {
            overlays++;
        }
        if (twoMassOverlay.isSelected()) {
            overlays++;
        }
        if (vhsOverlay.isSelected()) {
            overlays++;
        }
        if (gaiaWDOverlay.isSelected()) {
            overlays++;
        }
        if (spitzerOverlay.isSelected()) {
            overlays++;
        }
        if (noirlabOverlay.isSelected()) {
            overlays++;
        }
        if (noirlabProperMotion.isSelected()) {
            overlays++;
        }
        return overlays > 0;
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
        double scale = DEG_ARCSEC / PIXEL_SIZE;
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
        double scale = DEG_ARCSEC / PIXEL_SIZE;
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

    /*==========================================================================
    private NumberPair rotatePoint(double cx, double cy, double angle, NumberPair p) {
        double s = sin(toRadians(angle));
        double c = cos(toRadians(angle));
        double x = p.getX();
        double y = p.getY();

        // translate point back to origin
        x -= cx;
        y -= cy;

        // rotate point
        double px = x * c - y * s;
        double py = x * s + y * c;

        // translate point back
        x = px + cx;
        y = py + cy;
        return new NumberPair(x, y);
    }
    ==========================================================================*/
    private void createFlipbook() {
        if (asyncDownloads) {
            CompletableFuture.supplyAsync(() -> assembleFlipbook());
        } else {
            assembleFlipbook();
        }
    }

    private boolean assembleFlipbook() {
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
                size = (int) round(toInteger(sizeField.getText()) / PIXEL_SIZE);
                if (size > 1091) {
                    errorMessages.add("Field of view must not be larger than 3000 arcsec.");
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

            if (size != previousSize || targetRa != previousRa || targetDec != previousDec) {
                loadImages = true;
                bandW1Loaded = false;
                bandW2Loaded = false;
                allEpochsW1Loaded = false;
                allEpochsW2Loaded = false;
                moreImagesAvailable = false;
                oneMoreImageAvailable = false;
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
                pmCatalogEntry = null;
                ps1Image = null;
                processedPs1Image = null;
                if (panstarrsImages) {
                    CompletableFuture.supplyAsync(() -> ps1Image = fetchPs1Image(targetRa, targetDec, size));
                }
                sdssImage = null;
                processedSdssImage = null;
                if (sdssImages) {
                    CompletableFuture.supplyAsync(() -> sdssImage = fetchSdssImage(targetRa, targetDec, size));
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
                autoContrast.setSelected(true);
                if (!keepContrast.isSelected()) {
                    initContrast();
                }
                if (checkProperMotion.isSelected()) {
                    if (useAboveCoords.isSelected() && !coordsField.getText().isEmpty()) {
                        checkObjectCoordsField.setText(coordsField.getText());
                    }
                    if (useGaiaPM.isSelected() && !checkObjectCoordsField.getText().isEmpty()) {
                        applyProperMotion(new GaiaCatalogEntry());
                    }
                    if (useCatwisePM.isSelected() && !checkObjectCoordsField.getText().isEmpty()) {
                        applyProperMotion(new CatWiseCatalogEntry());
                    }
                }
                if (!unwiseCutouts.isSelected()) {
                    try {
                        InputStream stream = getImageData(1, numberOfEpochs + 5);
                        stream.close();
                        moreImagesAvailable = true;
                    } catch (FileNotFoundException ex) {
                        try {
                            InputStream stream = getImageData(1, numberOfEpochs);
                            stream.close();
                            oneMoreImageAvailable = true;
                        } catch (FileNotFoundException ex2) {
                        }
                    }
                }
            }
            previousSize = size;
            previousRa = targetRa;
            previousDec = targetDec;
            imageNumber = 0;

            if (loadImages || reloadImages) {
                int totalEpochs = selectedEpochs * 2 + (oneMoreImageAvailable ? 1 : 0);
                if (unwiseCutouts.isSelected()) {
                    epochCount = totalEpochs;
                }
                requestedEpochs = new ArrayList<>();
                if (Epoch.isFirstLast(epoch) && !moreImagesAvailable) {
                    if (reloadImages) {
                        imagesW1.clear();
                        imagesW2.clear();
                    }
                    requestedEpochs.add(0);
                    requestedEpochs.add(1);
                    requestedEpochs.add(totalEpochs - 2);
                    requestedEpochs.add(totalEpochs - 1);
                } else {
                    if (moreImagesAvailable) {
                        for (int i = 0; i < 100; i++) {
                            requestedEpochs.add(i);
                        }
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
                    baseFrame.setVisible(true);
                }
                writeLogEntry("Target: " + coordsField.getText() + " FoV: " + sizeField.getText() + "\"");
                switch (wiseBand) {
                    case W1:
                        if (unwiseCutouts.isSelected()) {
                            downloadUnwiseEpochs(WiseBand.W1.val, requestedEpochs, imagesW1);
                        } else {
                            downloadRequestedEpochs(WiseBand.W1.val, requestedEpochs, imagesW1);
                        }
                        epochCountW1 = epochCount;
                        break;
                    case W2:
                        if (unwiseCutouts.isSelected()) {
                            downloadUnwiseEpochs(WiseBand.W2.val, requestedEpochs, imagesW2);
                        } else {
                            downloadRequestedEpochs(WiseBand.W2.val, requestedEpochs, imagesW2);
                        }
                        epochCountW2 = epochCount;
                        break;
                    case W1W2:
                        if (unwiseCutouts.isSelected()) {
                            downloadUnwiseEpochs(WiseBand.W1.val, requestedEpochs, imagesW1);
                        } else {
                            downloadRequestedEpochs(WiseBand.W1.val, requestedEpochs, imagesW1);
                        }
                        epochCountW1 = epochCount;
                        if (unwiseCutouts.isSelected()) {
                            downloadUnwiseEpochs(WiseBand.W2.val, requestedEpochs, imagesW2);
                        } else {
                            downloadRequestedEpochs(WiseBand.W2.val, requestedEpochs, imagesW2);
                        }
                        epochCountW2 = epochCount;
                        break;
                }
                writeLogEntry("Ready.");
                if (asyncDownloads) {
                    downloadLog.setCaretPosition(0);
                }
                if (images.isEmpty()) {
                    showInfoDialog(baseFrame, "No decent images found for the specified coordinates and FoV.");
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
            loadImages = false;
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
            reloadImages = false;

            Fits fits;
            int k;
            switch (epoch) {
                case ALL:
                    flipbook = new FlipbookComponent[epochCount];
                    for (int i = 0; i < epochCount; i++) {
                        flipbook[i] = new FlipbookComponent(wiseBand.val, i, getEpochCoordinates(i > 1 ? i + EPOCH_GAP : i));
                    }
                    break;
                case ASCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 0; i < epochCount; i += 2) {
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i, getEpochCoordinates(i > 0 ? i + EPOCH_GAP : i));
                    }
                    break;
                case DESCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 1; i < epochCount; i += 2) {
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i, getEpochCoordinates(i > 1 ? i + EPOCH_GAP : i));
                    }
                    break;
                case ASCENDING_DESCENDING:
                    flipbook = new FlipbookComponent[epochCount];
                    k = 0;
                    for (int i = 0; i < epochCount; i += 2) {
                        flipbook[k] = new FlipbookComponent(wiseBand.val, i, getEpochCoordinates(i > 0 ? i + EPOCH_GAP : i));
                        k++;
                    }
                    for (int i = 1; i < epochCount; i += 2) {
                        flipbook[k] = new FlipbookComponent(wiseBand.val, i, getEpochCoordinates(i > 1 ? i + EPOCH_GAP : i));
                        k++;
                    }
                    break;
                case ASCENDING_DESCENDING_SUBTRACTED:
                    flipbook = new FlipbookComponent[epochCount - 2];
                    k = 0;
                    for (int i = 2; i < epochCount; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W1.val, 0);
                            addImage(WiseBand.W1.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W2.val, 0);
                            addImage(WiseBand.W2.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 900 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W2.val, i);
                            addImage(WiseBand.W2.val, 900 + i, fits);
                        }
                        differenceImaging(800 + i, 900 + i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 900 + i, true, getEpochCoordinates(i > 2 ? i + EPOCH_GAP : i - 2));
                        k++;
                    }
                    for (int i = 3; i < epochCount; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W1.val, 1);
                            addImage(WiseBand.W1.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W2.val, 1);
                            addImage(WiseBand.W2.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 900 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W2.val, i);
                            addImage(WiseBand.W2.val, 900 + i, fits);
                        }
                        differenceImaging(800 + i, 900 + i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 800 + i, true, getEpochCoordinates(i > 3 ? i + EPOCH_GAP : i - 2));
                        k++;
                    }
                    break;
                case ASCENDING_DESCENDING_NOISE_REDUCED:
                    flipbook = new FlipbookComponent[epochCount - 4];
                    k = 0;
                    for (int i = 2; i < epochCount - 2; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W1.val, 0);
                            addImage(WiseBand.W1.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W2.val, 0);
                            addImage(WiseBand.W2.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W1.val, i, WiseBand.W1.val, i + 2);
                            addImage(WiseBand.W1.val, 900 + i, takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W2.val, i, WiseBand.W2.val, i + 2);
                            addImage(WiseBand.W2.val, 900 + i, takeAverage(fits, 2));
                        }
                        differenceImaging(800 + i, 900 + i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 900 + i, true, getEpochCoordinates(i > 2 ? i + EPOCH_GAP + 2 : i - 2));
                        k++;
                    }
                    for (int i = 3; i < epochCount - 2; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W1.val, 1);
                            addImage(WiseBand.W1.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = getImage(WiseBand.W2.val, 1);
                            addImage(WiseBand.W2.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W1.val, i, WiseBand.W1.val, i + 2);
                            addImage(WiseBand.W1.val, 900 + i, takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W2.val, i, WiseBand.W2.val, i + 2);
                            addImage(WiseBand.W2.val, 900 + i, takeAverage(fits, 2));
                        }
                        differenceImaging(800 + i, 900 + i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 800 + i, true, getEpochCoordinates(i > 3 ? i + EPOCH_GAP + 2 : i - 2));
                        k++;
                    }
                    break;
                case ASCENDING_DESCENDING_PARALLAX:
                    flipbook = new FlipbookComponent[2];
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W1.val, 0, WiseBand.W1.val, 2);
                        addImage(WiseBand.W1.val, 600, fits);
                        for (int i = 4; i < epochCount; i += 2) {
                            fits = addImages(WiseBand.W1.val, 600, WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 600, fits);
                        }
                        addImage(WiseBand.W1.val, 600, takeAverage(fits, epochCount / 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W2.val, 0, WiseBand.W2.val, 2);
                        addImage(WiseBand.W2.val, 600, fits);
                        for (int i = 4; i < epochCount; i += 2) {
                            fits = addImages(WiseBand.W2.val, 600, WiseBand.W2.val, i);
                            addImage(WiseBand.W2.val, 600, fits);
                        }
                        addImage(WiseBand.W2.val, 600, takeAverage(fits, epochCount / 2));
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W1.val, 1, WiseBand.W1.val, 3);
                        addImage(WiseBand.W1.val, 700, fits);
                        for (int i = 5; i < epochCount; i += 2) {
                            fits = addImages(WiseBand.W1.val, 700, WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 700, fits);
                        }
                        addImage(WiseBand.W1.val, 700, takeAverage(fits, epochCount / 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W2.val, 1, WiseBand.W2.val, 3);
                        addImage(WiseBand.W2.val, 700, fits);
                        for (int i = 5; i < epochCount; i += 2) {
                            fits = addImages(WiseBand.W2.val, 700, WiseBand.W2.val, i);
                            addImage(WiseBand.W2.val, 700, fits);
                        }
                        addImage(WiseBand.W2.val, 700, takeAverage(fits, epochCount / 2));
                    }
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 600, true, getEpochCoordinates(0));
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 700, true, getEpochCoordinates(selectedEpochs * 2 + EPOCH_GAP - 1));
                    break;
                case YEAR:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 0; i < epochCount; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W1.val, i, WiseBand.W1.val, i + 1);
                            addImage(WiseBand.W1.val, 101 + (i / 2), takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            fits = addImages(WiseBand.W2.val, i, WiseBand.W2.val, i + 1);
                            addImage(WiseBand.W2.val, 101 + (i / 2), takeAverage(fits, 2));
                        }
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, 101 + (i / 2), true, getEpochCoordinates(i > 1 ? i + EPOCH_GAP + 1 : i));
                    }
                    break;
                case FIRST_REMAINING:
                case FIRST_REMAINING_SUBTRACTED:
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
                        fits = addImages(WiseBand.W1.val, 2, WiseBand.W1.val, 3);
                        addImage(WiseBand.W1.val, 300, fits);
                        for (int i = 4; i < epochCount; i++) {
                            fits = addImages(WiseBand.W1.val, 300, WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 300, fits);
                        }
                        addImage(WiseBand.W1.val, 300, takeAverage(fits, epochCount - 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W2.val, 2, WiseBand.W2.val, 3);
                        addImage(WiseBand.W2.val, 300, fits);
                        for (int i = 4; i < epochCount; i++) {
                            fits = addImages(WiseBand.W2.val, 300, WiseBand.W2.val, i);
                            addImage(WiseBand.W2.val, 300, fits);
                        }
                        addImage(WiseBand.W2.val, 300, takeAverage(fits, epochCount - 2));
                    }
                    if (epoch.equals(Epoch.FIRST_REMAINING_SUBTRACTED)) {
                        differenceImaging(100, 300);
                    }
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 100, true, getEpochCoordinates(0));
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 300, true, getEpochCoordinates(selectedEpochs * 2 + EPOCH_GAP - 1));
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
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 100, true, getEpochCoordinates(0));
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 200, true, getEpochCoordinates(selectedEpochs * 2 + EPOCH_GAP - 1));
                    break;
                case FIRST_LAST_PARALLAX:
                    flipbook = new FlipbookComponent[2];
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W1.val, 0, WiseBand.W1.val, epochCount - 2);
                        addImage(WiseBand.W1.val, 400, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W2.val, 0, WiseBand.W2.val, epochCount - 2);
                        addImage(WiseBand.W2.val, 400, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W1.val, 1, WiseBand.W1.val, epochCount - 1);
                        addImage(WiseBand.W1.val, 500, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = addImages(WiseBand.W2.val, 1, WiseBand.W2.val, epochCount - 1);
                        addImage(WiseBand.W2.val, 500, takeAverage(fits, 2));
                    }
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 400, true, getEpochCoordinates(0));
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 500, true, getEpochCoordinates(selectedEpochs * 2 + EPOCH_GAP - 1));
                    break;
                case FIRST_LAST_ASCENDING:
                    flipbook = new FlipbookComponent[2];
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = getImage(WiseBand.W1.val, 0);
                        addImage(WiseBand.W1.val, 1100, fits);
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = getImage(WiseBand.W2.val, 0);
                        addImage(WiseBand.W2.val, 1100, fits);
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = getImage(WiseBand.W1.val, epochCount - 2);
                        addImage(WiseBand.W1.val, 1200, fits);
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = getImage(WiseBand.W2.val, epochCount - 2);
                        addImage(WiseBand.W2.val, 1200, fits);
                    }
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 1100, true, getEpochCoordinates(0));
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 1200, true, getEpochCoordinates(selectedEpochs * 2 + EPOCH_GAP - 2));
                    break;
                case FIRST_LAST_DESCENDING:
                    flipbook = new FlipbookComponent[2];
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = getImage(WiseBand.W1.val, 1);
                        addImage(WiseBand.W1.val, 1300, fits);
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = getImage(WiseBand.W2.val, 1);
                        addImage(WiseBand.W2.val, 1300, fits);
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = getImage(WiseBand.W1.val, epochCount - 1);
                        addImage(WiseBand.W1.val, 1400, fits);
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        fits = getImage(WiseBand.W2.val, epochCount - 1);
                        addImage(WiseBand.W2.val, 1400, fits);
                    }
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 1300, true, getEpochCoordinates(1));
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 1400, true, getEpochCoordinates(selectedEpochs * 2 + EPOCH_GAP - 1));
                    break;
            }

            if (optimizeContrast.isSelected()) {
                for (FlipbookComponent component : flipbook) {
                    setRefValues(component);
                }
                List<Double> minValues = new ArrayList<>();
                List<Double> maxValues = new ArrayList<>();
                for (FlipbookComponent component : flipbook) {
                    NumberPair refVal = component.getRefValues();
                    minValues.add(refVal.getX());
                    maxValues.add(refVal.getY());
                }
                int count = flipbook.length;
                minValues.sort(Comparator.naturalOrder());
                maxValues.sort(Comparator.naturalOrder());
                double highestMinVal = minValues.get(count - 1);
                double highestMaxVal = maxValues.get(count - 1);
                NumberPair refValues = new NumberPair(highestMinVal, highestMaxVal);
                for (FlipbookComponent component : flipbook) {
                    component.setRefValues(refValues);
                }
            } else {
                FlipbookComponent firstComponent = flipbook[0];
                setRefValues(firstComponent);
                for (FlipbookComponent component : flipbook) {
                    component.setRefValues(firstComponent.getRefValues());
                }
            }

            flipbookComplete = true;
            processImages();
            timer.restart();
            timerStopped = false;
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
            hasException = true;
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
        return true;
    }

    private NumberPair getEpochCoordinates(int totalEpochs) {
        if (!checkProperMotion.isSelected() || checkObjectCoordsField.getText().isEmpty() || checkObjectMotionField.getText().isEmpty()) {
            return new NumberPair(0, 0);
        }

        NumberPair objectCoords = getCoordinates(checkObjectCoordsField.getText());
        double ra = objectCoords.getX();
        double dec = objectCoords.getY();

        NumberPair objectMotion = getCoordinates(checkObjectMotionField.getText());
        double pmRa = objectMotion.getX();
        double pmDec = objectMotion.getY();

        double numberOfYears = 0;
        if (pmCatalogEntry != null) {
            if (useGaiaPM.isSelected()) {
                ra = pmCatalogEntry.getRa();
                dec = pmCatalogEntry.getDec();
                numberOfYears = GAIADR2_ALLWISE_EPOCH_DIFF;
            }
            if (useCatwisePM.isSelected()) {
                ra = ((CatWiseCatalogEntry) pmCatalogEntry).getRa_pm();
                dec = ((CatWiseCatalogEntry) pmCatalogEntry).getDec_pm();
                numberOfYears = CATWISE_ALLWISE_EPOCH_DIFF;
            }
            if (useNoirlabPM.isSelected()) {
                ra = pmCatalogEntry.getRa();
                dec = pmCatalogEntry.getDec();
                numberOfYears = ((NoirlabCatalogEntry) pmCatalogEntry).getMeanEpoch() - ALLWISE_REFERENCE_EPOCH;
            }
        }

        NumberPair fromCoords = calculatePositionFromProperMotion(new NumberPair(ra, dec), new NumberPair(-numberOfYears * pmRa / DEG_MAS, -numberOfYears * pmDec / DEG_MAS));
        double fromRa = fromCoords.getX();
        double fromDec = fromCoords.getY();

        NumberPair toCoords = calculatePositionFromProperMotion(new NumberPair(fromRa, fromDec), new NumberPair(totalEpochs * (pmRa / 2) / DEG_MAS, totalEpochs * (pmDec / 2) / DEG_MAS));
        double toRa = toCoords.getX();
        double toDec = toCoords.getY();

        return new NumberPair(toRa, toDec);
    }

    private void setRefValues(FlipbookComponent component) throws Exception {
        Fits fits;
        int divisor = 0;
        int minValW1 = 0;
        int maxValW1 = 0;
        int avgValW1 = 0;
        fits = getImage(WiseBand.W1.val, component.getEpoch());
        if (fits != null) {
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values = (float[][]) imageData.getData();
            NumberTriplet refValues = getRefValues(values);
            minValW1 = (int) refValues.getX();
            maxValW1 = (int) refValues.getY();
            avgValW1 = (int) refValues.getZ();
            divisor++;
        }
        int minValW2 = 0;
        int maxValW2 = 0;
        int avgValW2 = 0;
        fits = getImage(WiseBand.W2.val, component.getEpoch());
        if (fits != null) {
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values = (float[][]) imageData.getData();
            NumberTriplet refValues = getRefValues(values);
            minValW2 = (int) refValues.getX();
            maxValW2 = (int) refValues.getY();
            avgValW2 = (int) refValues.getZ();
            divisor++;
        }
        int minVal = (minValW1 + minValW2) / divisor;
        int maxVal = (maxValW1 + maxValW2) / divisor;
        int avgVal = (avgValW1 + avgValW2) / divisor;
        NumberPair refValues = getMinMaxValues(minVal, maxVal, avgVal);
        component.setRefValues(refValues);
    }

    private void processImages() {
        if (ps1Image != null) {
            processedPs1Image = zoom(rotate(ps1Image, quadrantCount), zoom);
        }
        if (sdssImage != null) {
            processedSdssImage = zoom(rotate(sdssImage, quadrantCount), zoom);
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

    private void initCatalogEntries() {
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
        spitzerEntries = null;
        noirlabEntries = null;
        noirlabTpmEntries = null;
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
        if (ps1Image != null) {
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(zoom(rotate(ps1Image, quadrantCount), zoom))));
            pane.setBorder(createEtchedBorder("Pan-STARRS stack y/i/g"));
            grid.add(pane);
        }
        if (sdssImage != null) {
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(zoom(rotate(sdssImage, quadrantCount), zoom))));
            pane.setBorder(createEtchedBorder("Sloan Digital Sky Survey (SDSS)"));
            grid.add(pane);
        }
        imagePanel.removeAll();
        imagePanel.setBorder(createEmptyBorder(""));
        imagePanel.add(grid);
        baseFrame.setVisible(true);
    }

    public BufferedImage processImage(FlipbookComponent component) {
        BufferedImage image;
        int minValue = (int) component.getRefValues().getX();
        int maxValue = (int) component.getRefValues().getY();
        if (wiseBand.equals(WiseBand.W1W2)) {
            image = createComposite(component.getEpoch(), minValue, maxValue);
        } else {
            image = createImage(component.getBand(), component.getEpoch(), minValue, maxValue);
        }
        image = flip(zoom(image, zoom));
        addOverlaysAndPMVectors(image);
        return image;
    }

    private BufferedImage addCrosshairs(BufferedImage image, FlipbookComponent component) {
        // Copy the picture to draw shapes in real time
        if (checkProperMotion.isSelected() || drawCrosshairs.isSelected() || showCrosshairs.isSelected()) {
            image = copy(image);
        }

        // Draw a circle around the object to check if proper motions are consistent
        if (checkProperMotion.isSelected()) {
            NumberPair epochCoordinates = component.getEpochCoordinates();
            NumberPair position = toPixelCoordinates(epochCoordinates.getX(), epochCoordinates.getY());
            Circle circle = new Circle(position.getX(), position.getY(), shapeSize * zoom / 200, Color.RED);
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

        // Rotate image by the specified number of quadrants
        image = rotate(image, quadrantCount);

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

    private void addOverlaysAndPMVectors(BufferedImage image) {
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
                    sdssEntries = fetchCatalogEntries(new SDSSCatalogEntry());
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
                    sdssEntries = fetchCatalogEntries(new SDSSCatalogEntry());
                    processImages();
                    return null;
                });
            } else {
                drawSectrumOverlay(image, sdssEntries);
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
        if (vhsOverlay.isSelected()) {
            if (vhsEntries == null) {
                vhsEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> {
                    vhsEntries = fetchCatalogEntries(new VHSCatalogEntry());
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
        if (spitzerOverlay.isSelected()) {
            if (spitzerEntries == null) {
                spitzerEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> {
                    spitzerEntries = fetchCatalogEntries(new SpitzerCatalogEntry());
                    processImages();
                    return null;
                });
            } else {
                drawOverlay(image, spitzerEntries, JColor.YELLOW.val, Shape.CIRCLE);
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
        if (ssoOverlay.isSelected()) {
            if (ssoEntries == null) {
                ssoEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> {
                    ssoEntries = fetchCatalogEntries(new SSOCatalogEntry());
                    processImages();
                    return null;
                });
            } else {
                drawOverlay(image, ssoEntries, Color.BLUE, Shape.CIRCLE);
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
                drawPMVectors(image, gaiaTpmEntries, Color.CYAN.darker());
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
                drawPMVectors(image, gaiaDR3TpmEntries, Color.CYAN.darker());
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
                drawPMVectors(image, catWiseTpmEntries, Color.MAGENTA);
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
                drawPMVectors(image, noirlabTpmEntries, JColor.NAVY.val);
            }
        }
    }

    private void downloadUnwiseEpochs(int band, List<Integer> requestedEpochs, Map<String, ImageContainer> images) throws Exception {
        writeLogEntry("Downloading ...");
        for (int i = 0; i < requestedEpochs.size(); i++) {
            int requestedEpoch = requestedEpochs.get(i);
            String imageKey = band + "_" + requestedEpoch;
            ImageContainer container = images.get(imageKey);
            if (container != null) {
                writeLogEntry("band " + band + " | image " + requestedEpoch + " > already downloaded");
                continue;
            }
            try {
                Fits fits = new Fits(getImageData(band, requestedEpoch));
                extractHeaderInfo(fits);
                addImage(band, requestedEpoch, fits);
                writeLogEntry("band " + band + " | image " + requestedEpoch);
                images.put(imageKey, new ImageContainer(requestedEpoch, null, fits));
            } catch (FileNotFoundException ex) {
                if (requestedEpochs.size() == 4) {
                    writeLogEntry("band " + band + " | image " + requestedEpoch + " > not found, looking for surrogates");
                    downloadUnwiseEpochs(band, provideAlternativeEpochs(requestedEpoch, requestedEpochs), images);
                    return;
                } else {
                    break;
                }
            }
        }
    }

    private void downloadRequestedEpochs(int band, List<Integer> requestedEpochs, Map<String, ImageContainer> images) throws Exception {
        writeLogEntry("Downloading ...");
        for (int i = 0; i < requestedEpochs.size(); i++) {
            int requestedEpoch = requestedEpochs.get(i);
            String imageKey = band + "_" + requestedEpoch;
            ImageContainer container = images.get(imageKey);
            if (container != null) {
                writeLogEntry("band " + band + " | image " + requestedEpoch + " > already downloaded");
                continue;
            }
            Fits fits;
            try {
                fits = new Fits(getImageData(band, requestedEpoch));
            } catch (FileNotFoundException ex) {
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
                    writeLogEntry("band " + band + " | image " + requestedEpoch + " > unable to read, looking for surrogates");
                    downloadRequestedEpochs(band, provideAlternativeEpochs(requestedEpoch, requestedEpochs), images);
                    return;
                } else {
                    writeLogEntry("band " + band + " | image " + requestedEpoch + " > unable to read");
                    continue;
                }
            }
            Header header = hdu.getHeader();
            double xAxis = header.getDoubleValue("NAXIS1");
            double yAxis = header.getDoubleValue("NAXIS2");
            double minObsEpoch = header.getDoubleValue("MJDMIN");
            LocalDateTime obsDate = convertMJDToDateTime(new BigDecimal(Double.toString(minObsEpoch)));
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values = (float[][]) imageData.getData();
            // Skip images with too many zero values
            if (yAxis > 0) {
                xAxis = values[0].length;
            }
            int zeroValues = 0;
            for (int j = 0; j < yAxis; j++) {
                for (int k = 0; k < xAxis; k++) {
                    try {
                        if (values[j][k] == 0) {
                            zeroValues++;
                        }
                    } catch (ArrayIndexOutOfBoundsException ex) {
                    }
                }
            }
            String imageDate = obsDate.format(DATE_FORMATTER);
            if (skipBadImages.isSelected()) {
                double maxAllowed = xAxis * yAxis / 20;
                if (zeroValues > maxAllowed) {
                    if (requestedEpochs.size() == 4) {
                        writeLogEntry("band " + band + " | image " + requestedEpoch + " | " + imageDate + " > skipped (bad image quality), looking for surrogates");
                        downloadRequestedEpochs(band, provideAlternativeEpochs(requestedEpoch, requestedEpochs), images);
                        return;
                    } else {
                        writeLogEntry("band " + band + " | image " + requestedEpoch + " | " + imageDate + " > skipped (bad image quality)");
                        continue;
                    }
                }
            }
            images.put(imageKey, new ImageContainer(requestedEpoch, obsDate, fits));
            writeLogEntry("band " + band + " | image " + requestedEpoch + " | " + imageDate + " > downloaded");
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
            containerSaved = container;
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
                if (skipSingleNodes.isSelected() && (node1 == 0 || node2 == 0)) {
                    if (requestedEpochs.size() == 4) {
                        images.clear();
                        int requestedEpoch = container.getEpoch();
                        writeLogEntry("year " + prevYear + " | node " + prevNode + " > skipped (single node), looking for surrogates");
                        downloadRequestedEpochs(band, provideAlternativeEpochs(requestedEpoch, requestedEpochs), images);
                        return;
                    } else {
                        writeLogEntry("year " + prevYear + " | node " + prevNode + " > skipped (single node)");
                        groupedList.remove(groupedList.size() - 1);
                    }
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
        if (skipSingleNodes.isSelected() && (node1 == 0 || node2 == 0)) {
            if (requestedEpochs.size() == 4) {
                images.clear();
                int requestedEpoch = containerSaved.getEpoch();
                writeLogEntry("year " + prevYear + " | node " + prevNode + " > skipped (single node), looking for surrogates");
                downloadRequestedEpochs(band, provideAlternativeEpochs(requestedEpoch, requestedEpochs), images);
                return;
            } else {
                writeLogEntry("year " + prevYear + " | node " + prevNode + " > skipped (single node)");
            }
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
            for (int i = 1; i < imageGroup.size(); i++) {
                fits = stackImages(fits, imageGroup.get(i).getImage());
            }
            int groupSize = imageGroup.size();
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
            skipBadImages.setSelected(false);
            skipSingleNodes.setSelected(false);
            return this.requestedEpochs;
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
                        addedValues[i][j] = values1[i][j] + values2[i][j];
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
        double ra;
        double dec;
        if (transposeProperMotion.isSelected() && !transposeMotionField.getText().isEmpty()) {
            NumberPair properMotion = getCoordinates(transposeMotionField.getText());
            double pmRa = properMotion.getX();
            double pmDec = properMotion.getY();
            int totalEpochs;
            if (epoch > 1) {
                totalEpochs = epoch + numberOfEpochs / 2;
            } else {
                totalEpochs = epoch;
            }
            NumberPair coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec), new NumberPair(totalEpochs * (pmRa / 2) / DEG_MAS, totalEpochs * (pmDec / 2) / DEG_MAS));
            ra = coords.getX();
            dec = coords.getY();
        } else {
            ra = targetRa;
            dec = targetDec;
        }
        if (unwiseCutouts.isSelected()) {
            epoch /= 2;
            String unwiseEpoch;
            if (epoch == 0) {
                unwiseEpoch = "allwise";
            } else {
                unwiseEpoch = "neo" + epoch;
            }
            String unwiseURL = String.format("http://unwise.me/cutout_fits?version=%s&ra=%f&dec=%f&size=%d&bands=%d&file_img_m=on", unwiseEpoch, ra, dec, size, band);
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
            String imageUrl = createImageUrl(ra, dec, size, band, epoch);
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            return connection.getInputStream();
        }
    }

    private String createImageUrl(double targetRa, double targetDec, int size, int band, int epoch) throws MalformedURLException {
        return getUserSetting(CUTOUT_SERVICE) + "?ra=" + targetRa + "&dec=" + targetDec + "&size=" + size + "&band=" + band + "&epoch=" + epoch;
    }

    private BufferedImage createImage(int band, int epoch, int minValue, int maxValue) {
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
                        float value = processPixel(values[i][j], minValue, maxValue);
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

    private BufferedImage createComposite(int epoch, int minValue, int maxValue) {
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
                        float red = processPixel(valuesW1[i][j], minValue, maxValue);
                        float blue = processPixel(valuesW2[i][j], minValue, maxValue);
                        float green = (red + blue) / 2;
                        graphics.setColor(new Color(red, green, blue));
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

    private Fits addImages(int band1, int epoch1, int band2, int epoch2) {
        try {
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
                        addedValues[i][j] = values1[i][j] + values2[i][j];
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

    private Fits subtractImages(int band1, int epoch1, int band2, int epoch2) {
        try {
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Fits takeAverage(Fits fits, int numberOfImages) {
        try {
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void differenceImaging(int epoch1, int epoch2) {
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

    public float[][] blur(float[][] values) {
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

    private BufferedImage copy(BufferedImage bufferImage) {
        ColorModel colorModel = bufferImage.getColorModel();
        WritableRaster raster = bufferImage.copyData(null);
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

    private BufferedImage flip(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    private BufferedImage rotate(BufferedImage image, int numberOfQuadrants) {
        if (numberOfQuadrants == 0) {
            return image;
        }
        AffineTransform tx = AffineTransform.getQuadrantRotateInstance(numberOfQuadrants, image.getWidth() / 2, image.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

    private BufferedImage zoom(BufferedImage image, int zoom) {
        zoom = zoom == 0 ? 1 : zoom;
        Image scaled = image.getScaledInstance((naxis1 > naxis2 ? 1 : -1) * zoom, (naxis1 > naxis2 ? -1 : 1) * zoom, Image.SCALE_DEFAULT);
        image = new BufferedImage(scaled.getWidth(null), scaled.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(scaled, 0, 0, null);
        graphics.dispose();
        return image;
    }

    private Fits getImage(int band, int epoch) {
        return images.get(band + "_" + epoch);
    }

    private void addImage(int band, int epoch, Fits fits) {
        images.put(band + "_" + epoch, fits);
    }

    private float processPixel(float value, int minValue, int maxValue) {
        value *= subContrast;
        value = normalize(value, minValue, maxValue);
        value = stretch(value);
        value = contrast(value);
        value = min(1, value);
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

    private float stretch(float value) {
        return asinh(value / 1) / asinh(1 / 1);
    }

    private float contrast(float value) {
        return value * (highContrast + lowContrast) * 0.1f;
    }

    private float asinh(float x) {
        return (float) log(x + sqrt(x * x + 1.0));
    }

    private void setContrast(int low, int high) {
        if (!Epoch.isSubtracted(epoch)) {
            lowContrastSaved = low;
            highContrastSaved = high;
        }
        lowScaleSlider.setValue(lowContrast = low);
        highScaleSlider.setValue(highContrast = high);
    }

    private void setSubContrast(int val) {
        if (Epoch.isSubtracted(epoch)) {
            subContrastSaved = val;
        }
        subScaleSlider.setValue(subContrast = val);
    }

    private void initContrast() {
        subContrastSaved = SUB_CONTRAST;
        lowContrastSaved = LOW_CONTRAST;
        highContrastSaved = HIGH_CONTRAST;

        ChangeListener listener = subScaleSlider.getChangeListeners()[0];
        subScaleSlider.removeChangeListener(listener);
        subScaleSlider.setValue(subContrast = subContrastSaved);
        subScaleSlider.addChangeListener(listener);

        listener = lowScaleSlider.getChangeListeners()[0];
        lowScaleSlider.removeChangeListener(listener);
        lowScaleSlider.setValue(lowContrast = lowContrastSaved);
        lowScaleSlider.addChangeListener(listener);

        listener = highScaleSlider.getChangeListeners()[0];
        highScaleSlider.removeChangeListener(listener);
        highScaleSlider.setValue(highContrast = highContrastSaved);
        highScaleSlider.addChangeListener(listener);

        subScaleLabel.setText(String.format(SUB_SCALE_LABEL, subContrast));
        lowScaleLabel.setText(String.format(LOW_SCALE_LABEL, lowContrast));
        highScaleLabel.setText(String.format(HIGH_SCALE_LABEL, highContrast));
    }

    private NumberTriplet getRefValues(float[][] values) {
        int total = 0, count = 0;
        List<Float> numbers = new ArrayList<>();
        for (float[] row : values) {
            for (float value : row) {
                if (value != Float.POSITIVE_INFINITY && value != Float.NEGATIVE_INFINITY && value != Float.NaN) {
                    numbers.add(value);
                    total += abs(value);
                    count++;
                }
            }
        }
        numbers.sort(Comparator.naturalOrder());
        float minVal = numbers.get(0);
        float maxVal = numbers.get(numbers.size() - 1);
        float avgVal = total / count;
        return new NumberTriplet(minVal, maxVal, avgVal);
    }

    private NumberPair getMinMaxValues(int minVal, int maxVal, int avgVal) {
        //System.out.println("minVal=" + minVal + " maxVal=" + maxVal + " avgVal=" + avgVal);
        if (autoContrast.isSelected()) {
            if (maxVal < 500) {
                maxVal = 500;
            } else {
                int maxLimit;
                if (avgVal > 15000) {
                    maxLimit = 50000;
                } else if (avgVal > 1500) {
                    maxLimit = 25000;
                } else if (avgVal > 500) {
                    maxLimit = 10000;
                } else if (avgVal > 300) {
                    maxLimit = 5000;
                } else if (avgVal > 200) {
                    maxLimit = 3000;
                } else if (avgVal > 10) {
                    maxLimit = 2000;
                } else {
                    maxLimit = 1000;
                }
                if (maxVal > maxLimit) {
                    maxVal = maxLimit;
                }
            }
            if (Epoch.isSubtracted(epoch)) {
                minVal = -maxVal / 10;
            } else if (minVal < -3000) {
                minVal = -maxVal / 30;
            }
        }
        return new NumberPair(minVal, maxVal);
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
        imageViewerTab.getUnwiseCutouts().setSelected(unwiseCutouts.isSelected());
        imageViewerTab.getSizeField().setText(differentSizeField.getText());
        imageViewerTab.getWiseBands().setSelectedItem(wiseBand);
        imageViewerTab.setQuadrantCount(quadrantCount);
        imageViewerTab.getZoomSlider().setValue(ZOOM);
        imageViewerTab.setZoom(ZOOM);
        imageViewerTab.setImageViewer(this);

        baseFrame.setCursor(Cursor.getDefaultCursor());

        return true;
    }

    private BufferedImage fetchPs1Image(double targetRa, double targetDec, double size) {
        try {
            List<String> fileNames = new ArrayList<>();
            String imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=giy&sep=comma", targetRa, targetDec);
            String response = readResponse(establishHttpConnection(imageUrl), "Pan-STARRS");
            try (Scanner scanner = new Scanner(response)) {
                String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
                int j = 0;
                for (int i = 0; i < columnNames.length; i++) {
                    if (columnNames[i].equals("filename")) {
                        j = i;
                        break;
                    }
                }
                while (scanner.hasNextLine()) {
                    String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
                    fileNames.add(columnValues[j]);
                }
            }
            imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?red=%s&green=%s&blue=%s&ra=%f&dec=%f&size=%d&output_size=%d&autoscale=99.8", fileNames.get(2), fileNames.get(1), fileNames.get(0), targetRa, targetDec, (int) round(size * PIXEL_SIZE * 4), 1024);
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

    private BufferedImage fetchSdssImage(double targetRa, double targetDec, double size) {
        try {
            int resolution = 1000;
            String imageUrl = String.format(SDSS_BASE_URL + "/SkyserverWS/ImgCutout/getjpeg?ra=%f&dec=%f&width=%d&height=%d&scale=%f", targetRa, targetDec, resolution, resolution, size * PIXEL_SIZE / resolution);
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

    private void displayDssImages(double targetRa, double targetDec, int size, Counter counter) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            JPanel bandPanel = new JPanel(new GridLayout(1, 6));

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
            imageFrame.setLocation(0, counter.getTotal());
            imageFrame.setAlwaysOnTop(true);
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
            JPanel bandPanel = new JPanel(new GridLayout(1, 4));

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
            imageFrame.setLocation(0, counter.getTotal());
            imageFrame.setAlwaysOnTop(true);
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
            JPanel bandPanel = new JPanel(new GridLayout(1, 5));

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
            imageFrame.setLocation(0, counter.getTotal());
            imageFrame.setAlwaysOnTop(true);
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
            JPanel bandPanel = new JPanel(new GridLayout(1, 4));

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
            imageFrame.setLocation(0, counter.getTotal());
            imageFrame.setAlwaysOnTop(true);
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
            SortedMap<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
            if (imageInfos.isEmpty()) {
                return;
            }

            // Fetch images for Pan-STARRS filters
            JPanel bandPanel = new JPanel(new GridLayout(1, 6));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("g")), targetRa, targetDec, size), "g"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("r")), targetRa, targetDec, size), "r"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("i")), targetRa, targetDec, size), "i"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size), "z"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("y")), targetRa, targetDec, size), "y"));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s&green=%s&blue=%s", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g")), targetRa, targetDec, size), "y-i-g"));

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("Pan-STARRS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
            imageFrame.add(bandPanel);
            imageFrame.setSize(6 * PANEL_WIDTH, PANEL_HEIGHT);
            imageFrame.setLocation(0, counter.getTotal());
            imageFrame.setAlwaysOnTop(true);
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
            JPanel bandPanel = new JPanel(new GridLayout(1, 5));

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
            image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
            if (image != null) {
                bandPanel.add(buildImagePanel(image, "WISE - W2"));
            }
            SortedMap<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
            if (!imageInfos.isEmpty()) {
                image = retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size);
                bandPanel.add(buildImagePanel(image, "PS1 - z"));
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
            imageFrame.setLocation(0, counter.getTotal());
            imageFrame.setAlwaysOnTop(true);
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
            if (dssImages.isSelected()) {
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
            if (allwiseImages.isSelected()) {
                image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
                if (image != null) {
                    imageList.add(new Couple("WISE - W2", image));
                }
            }
            if (ps1Images.isSelected()) {
                SortedMap<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
                if (!imageInfos.isEmpty()) {
                    image = retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size);
                    imageList.add(new Couple("PS1 - z", image));
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
                            imageSet[i++] = drawCenterCircle(imageBuffer);
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
            imageFrame.setAlwaysOnTop(true);
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
        panel.add(new JLabel(new ImageIcon(drawCenterCircle(image))));
        return panel;
    }

    private BufferedImage drawCenterCircle(BufferedImage image) {
        image = zoom(image, 200);
        double x = image.getWidth() / 2;
        double y = image.getHeight() / 2;
        Graphics g = image.getGraphics();
        Circle circle = new Circle(x, y, 10, Color.MAGENTA);
        circle.draw(g);
        return image;
    }

    private List<CatalogEntry> fetchCatalogEntries(CatalogEntry catalogQuery) {
        try {
            baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            catalogQuery.setRa(targetRa);
            catalogQuery.setDec(targetDec);
            catalogQuery.setSearchRadius(getFovDiagonal() / 2);
            List<CatalogEntry> resultEntries = new ArrayList<>();
            List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
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
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
        return null;
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
            List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoordsAndTpm(catalogQuery);
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
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
            properMotionField.setCursor(Cursor.getDefaultCursor());
        }
        return null;
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
            String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
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
                String[] columnValues = scanner.nextLine().split(SPLIT_CHAR, numberOfColumns);
                GenericCatalogEntry catalogEntry = new GenericCatalogEntry(columnNames, columnValues);
                catalogEntry.setRa(toDouble(columnValues[raColumnIndex]));
                catalogEntry.setDec(toDouble(columnValues[decColumnIndex]));

                NumberPair coords;
                double radius = size * PIXEL_SIZE / 2 / DEG_ARCSEC;

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
        List<LookupResult> results = mainSequenceSpectralTypeLookupService.lookup(catalogEntry.getColors());
        if (results.isEmpty()) {
            results = brownDwarfsSpectralTypeLookupService.lookup(catalogEntry.getColors());
        }
        if (results.isEmpty()) {
            catalogEntry.setSpt("N/A");
            return;
        }
        for (LookupResult result : results) {
            catalogEntry.setSpt(result.getSpt());
            break;
        }
    }

    private boolean isBrownDwarf(CatalogEntry catalogEntry) {
        return BROWN_DWARFS.contains(catalogEntry.getSpt());
    }

    private void drawSectrumOverlay(BufferedImage image, List<CatalogEntry> catalogEntries) {
        Graphics graphics = image.getGraphics();
        catalogEntries.forEach(catalogEntry -> {
            NumberPair position = toPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
            catalogEntry.setPixelRa(position.getX());
            catalogEntry.setPixelDec(position.getY());
            SDSSCatalogEntry sdssCatalogEntry = (SDSSCatalogEntry) catalogEntry;
            if (!sdssCatalogEntry.getSpecObjID().equals(new BigInteger("0"))) {
                Drawable toDraw = new Circle(position.getX(), position.getY(), getOverlaySize(), JColor.OLIVE.val);
                toDraw.draw(graphics);
            }
        });
    }

    private void showSpectrumInfo(List<CatalogEntry> catalogEntries, int x, int y) {
        catalogEntries.forEach(catalogEntry -> {
            double radius = getOverlaySize() / 2;
            SDSSCatalogEntry sdssCatalogEntry = (SDSSCatalogEntry) catalogEntry;
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
            SDSSCatalogEntry SDSSCatalogEntry = (SDSSCatalogEntry) catalogEntry;
            String spectrumUrl = SDSS_BASE_URL + "/en/get/specById.ashx?ID=" + SDSSCatalogEntry.getSpecObjID();
            HttpURLConnection connection = establishHttpConnection(spectrumUrl);
            BufferedImage spectrum;
            try (BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {
                spectrum = ImageIO.read(stream);
            }
            if (spectrum != null) {
                JFrame imageFrame = new JFrame();
                imageFrame.setIconImage(getToolBoxImage());
                imageFrame.setTitle("SDSS spectrum for object: " + roundTo2DecNZ(catalogEntry.getRa()) + " " + roundTo2DecNZ(catalogEntry.getDec()));
                imageFrame.add(new JLabel(new ImageIcon(spectrum)));
                imageFrame.setSize(1200, 900);
                imageFrame.setAlwaysOnTop(true);
                imageFrame.setResizable(true);
                imageFrame.setVisible(true);
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
            CatWiseCatalogEntry catWiseCatalog = (CatWiseCatalogEntry) catalogEntry;
            String ab_flags = catWiseCatalog.getAb_flags();
            String cc_flags = catWiseCatalog.getCc_flags();
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

    private void drawPMVectors(BufferedImage image, List<CatalogEntry> catalogEntries, Color color) {
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
            if (catalogEntry instanceof CatWiseCatalogEntry) {
                ra = ((CatWiseCatalogEntry) catalogEntry).getRa_pm();
                dec = ((CatWiseCatalogEntry) catalogEntry).getDec_pm();
                numberOfYears = CATWISE_ALLWISE_EPOCH_DIFF;
            }
            if (catalogEntry instanceof NoirlabCatalogEntry) {
                numberOfYears = ((NoirlabCatalogEntry) catalogEntry).getMeanEpoch() - ALLWISE_REFERENCE_EPOCH;
            }

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
        boolean simpleLayout = catalogEntry instanceof GenericCatalogEntry || catalogEntry instanceof SSOCatalogEntry;
        List<CatalogElement> catalogElements = catalogEntry.getCatalogElements();

        int elements = catalogElements.size();
        int rows = elements / 2;
        int remainder = elements % 2;
        rows += remainder;

        int maxRows;
        if (simpleLayout) {
            maxRows = rows > 30 ? rows : 30;
        } else {
            maxRows = 19;
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

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(new EmptyBorder(3, 3, 3, 3));
        container.setBackground(color);
        container.add(detailPanel);

        if (!simpleLayout) {
            List<LookupResult> mainSequenceResults = mainSequenceSpectralTypeLookupService.lookup(catalogEntry.getColors());
            container.add(createMainSequenceSpectralTypePanel(mainSequenceResults));
            if (catalogEntry instanceof AllWiseCatalogEntry) {
                AllWiseCatalogEntry entry = (AllWiseCatalogEntry) catalogEntry;
                if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                    JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    messagePanel.add(createLabel(AGN_WARNING, JColor.DARK_RED));
                    container.add(messagePanel);
                }
            }
            if (catalogEntry instanceof GaiaCatalogEntry) {
                GaiaCatalogEntry entry = (GaiaCatalogEntry) catalogEntry;
                if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                    JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    messagePanel.add(createLabel(WD_WARNING, JColor.DARK_RED));
                    container.add(messagePanel);
                }
            }
            List<LookupResult> brownDwarfsResults = brownDwarfsSpectralTypeLookupService.lookup(catalogEntry.getColors());
            container.add(createBrownDwarfsSpectralTypePanel(brownDwarfsResults));

            JPanel collectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            container.add(collectPanel);

            JLabel message = createLabel("", JColor.DARKER_GREEN);
            Timer messageTimer = new Timer(3000, (ActionEvent e) -> {
                message.setText("");
            });

            collectPanel.add(new JLabel("Object type:"));

            JComboBox objectTypes = new JComboBox<>(ObjectType.labels());
            collectPanel.add(objectTypes);

            JButton collectButton = new JButton("Add to object collection");
            collectPanel.add(collectButton);
            collectButton.addActionListener((ActionEvent evt) -> {
                String selectedObjectType = (String) objectTypes.getSelectedItem();
                collectObject(selectedObjectType, catalogEntry, message, messageTimer, baseFrame, mainSequenceSpectralTypeLookupService, collectionTable);
            });

            JButton copyButton = new JButton("Copy");
            collectPanel.add(copyButton);
            copyButton.addActionListener((ActionEvent evt) -> {
                StringBuilder toCopytoClipboard = new StringBuilder();
                toCopytoClipboard.append(catalogEntry.getEntryData());
                toCopytoClipboard.append(LINE_SEP).append(LINE_SEP).append("Spectral type evaluation:");
                toCopytoClipboard.append(LINE_SEP).append("* Main sequence table:");
                mainSequenceResults.forEach(entry -> {
                    toCopytoClipboard.append(LINE_SEP).append("  + ").append(entry.getColorKey().val).append(" = ").append(roundTo3DecNZ(entry.getColorValue())).append(" -> ").append(entry.getSpt());
                });
                toCopytoClipboard.append(LINE_SEP).append("* M-L-T-Y dwarfs only:");
                brownDwarfsResults.forEach(entry -> {
                    toCopytoClipboard.append(LINE_SEP).append("  + ").append(entry.getColorKey().val).append(" = ").append(roundTo3DecNZ(entry.getColorValue())).append(" -> ").append(entry.getSpt());
                    List<DistanceLookupResult> distanceResults = distanceLookupService.lookup(entry.getSpt(), catalogEntry.getBands());
                    toCopytoClipboard.append(LINE_SEP).append("      Distance evaluation for ").append(entry.getSpt()).append(":");
                    distanceResults.forEach(result -> {
                        toCopytoClipboard.append(LINE_SEP).append("      - ").append(result.getBandKey().val).append(" = ").append(roundTo3DecNZ(result.getBandValue())).append(" -> ").append(roundTo3DecNZ(result.getDistance())).append(" pc");
                    });
                });
                StringSelection stringSelection = new StringSelection(toCopytoClipboard.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

                message.setText("Copied to clipboard!");
                messageTimer.restart();
            });

            collectPanel.add(message);
        }

        JFrame catalogFrame = new JFrame();
        catalogFrame.setIconImage(getToolBoxImage());
        catalogFrame.setTitle("Object details");
        catalogFrame.add(simpleLayout ? new JScrollPane(container) : container);
        catalogFrame.setSize(650, 550);
        catalogFrame.setLocation(windowShift, windowShift);
        catalogFrame.setAlwaysOnTop(true);
        catalogFrame.setResizable(true);
        catalogFrame.setVisible(true);
        windowShift += 10;
    }

    private JScrollPane createMainSequenceSpectralTypePanel(List<LookupResult> results) {
        List<String[]> spectralTypes = new ArrayList<>();
        results.forEach(entry -> {
            String matchedColor = entry.getColorKey().val + "=" + roundTo3DecNZ(entry.getColorValue());
            String spectralType = entry.getSpt() + "," + entry.getTeff() + "," + roundTo3Dec(entry.getRsun()) + "," + roundTo3Dec(entry.getMsun())
                    + "," + matchedColor + "," + roundTo3Dec(entry.getNearest()) + "," + roundTo3DecLZ(entry.getGap());
            spectralTypes.add(spectralType.split(",", 7));
        });

        String titles = "spt,teff,radius (Rsun),mass (Msun),matched colors,nearest color,gap to nearest color";
        String[] columns = titles.split(",", 7);
        Object[][] rows = new Object[][]{};
        JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        alignResultColumns(spectralTypeTable, spectralTypes);
        spectralTypeTable.setAutoCreateRowSorter(true);
        spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = spectralTypeTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(50);
        columnModel.getColumn(2).setPreferredWidth(55);
        columnModel.getColumn(3).setPreferredWidth(50);
        columnModel.getColumn(4).setPreferredWidth(100);
        columnModel.getColumn(5).setPreferredWidth(100);
        columnModel.getColumn(6).setPreferredWidth(100);

        JScrollPane spectralTypePanel = spectralTypes.isEmpty()
                ? new JScrollPane(createLabel("No colors available / No match", JColor.DARK_RED))
                : new JScrollPane(spectralTypeTable);
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
            spectralTypes.add(spectralType.split(",", 4));
        });

        String titles = "spt,matched colors,nearest color,gap to nearest color";
        String[] columns = titles.split(",", 4);
        Object[][] rows = new Object[][]{};
        JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        alignResultColumns(spectralTypeTable, spectralTypes);
        spectralTypeTable.setAutoCreateRowSorter(true);
        spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = spectralTypeTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(100);

        JScrollPane spectralTypePanel = spectralTypes.isEmpty()
                ? new JScrollPane(createLabel("No colors available / No match", JColor.DARK_RED))
                : new JScrollPane(spectralTypeTable);
        spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "M-L-T-Y dwarfs spectral type evaluation", TitledBorder.LEFT, TitledBorder.TOP
        ));

        return spectralTypePanel;
    }

    private void displayMotionChecker() {
        if (checkProperMotion.isSelected() && !checkObjectCoordsField.getText().isEmpty() && !checkObjectMotionField.getText().isEmpty()) {
            createFlipbook();
        }
    }

    private void applyProperMotion(CatalogEntry catalogEntry) {
        NumberPair objectCoords = getCoordinates(checkObjectCoordsField.getText());
        catalogEntry.setRa(objectCoords.getX());
        catalogEntry.setDec(objectCoords.getY());
        catalogEntry.setSearchRadius(5);
        pmCatalogEntry = retrieveCatalogEntry(catalogEntry);
        if (pmCatalogEntry == null) {
            showInfoDialog(baseFrame, NO_OBJECT_FOUND);
            checkObjectMotionField.setText(null);
        } else {
            checkObjectMotionField.setText(roundTo3DecNZ(pmCatalogEntry.getPmra()) + " " + roundTo3DecNZ(pmCatalogEntry.getPmdec()));
        }
    }

    private CatalogEntry retrieveCatalogEntry(CatalogEntry catalogEntry) {
        try {
            List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogEntry);
            if (!catalogEntries.isEmpty()) {
                catalogEntries.sort(Comparator.comparing(CatalogEntry::getTargetDistance));
                return catalogEntries.get(0);
            }
        } catch (IOException ex) {
            showExceptionDialog(baseFrame, ex);
        }
        return null;
    }

    private double getFovDiagonal() {
        return size * PIXEL_SIZE * sqrt(2);
    }

    private double getOverlaySize() {
        return zoom / 100 + 300 / size;
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

    public JSlider getEpochSlider() {
        return epochSlider;
    }

    public JLabel getEpochLabel() {
        return epochLabel;
    }

    public JCheckBox getUnwiseCutouts() {
        return unwiseCutouts;
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

    public void setAsyncDownloads(boolean asyncDownloads) {
        this.asyncDownloads = asyncDownloads;
    }

    public void setPanstarrsImages(boolean panstarrsImages) {
        this.panstarrsImages = panstarrsImages;
    }

    public void setSdssImages(boolean sdssImages) {
        this.sdssImages = sdssImages;
    }

}
