package astro.tool.box.module.tab;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.Urls.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.CustomOverlay;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.NumberTriplet;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseRejectedEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaCatalogEntry;
import astro.tool.box.container.catalog.GaiaWDCatalogEntry;
import astro.tool.box.container.catalog.GenericCatalogEntry;
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
import astro.tool.box.enumeration.Unit;
import astro.tool.box.enumeration.WiseBand;
import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.module.Application;
import astro.tool.box.module.FlipbookComponent;
import astro.tool.box.module.GifSequencer;
import astro.tool.box.module.ImageContainer;
import astro.tool.box.module.InfoSheet;
import astro.tool.box.module.shape.Arrow;
import astro.tool.box.module.shape.Circle;
import astro.tool.box.module.shape.Cross;
import astro.tool.box.module.shape.CrossHair;
import astro.tool.box.module.shape.Diamond;
import astro.tool.box.module.shape.Drawable;
import astro.tool.box.module.shape.Square;
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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
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
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultCaret;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;

public class ImageViewerTab {

    public static final String TAB_NAME = "Image Viewer";
    public static final String EPOCH_LABEL = "Number of epochs: %d";
    public static final WiseBand WISE_BAND = WiseBand.W2;
    public static final Epoch EPOCH = Epoch.FIRST_LAST;
    public static final double OVERLAP_FACTOR = 0.9;
    public static final double SIZE_FACTOR = 2.75;
    public static final int NUMBER_OF_EPOCHS = 7;
    public static final int WINDOW_SPACING = 25;
    public static final int PANEL_HEIGHT = 260;
    public static final int PANEL_WIDTH = 220;
    public static final int SENSITIVITY = 1;
    public static final int RAW_CONTRAST = 1;
    public static final int LOW_CONTRAST = 50;
    public static final int HIGH_CONTRAST = 0;
    public static final int STRETCH = 100;
    public static final int SPEED = 300;
    public static final int ZOOM = 500;
    public static final int SIZE = 500;
    public static final String CHANGE_FOV_TEXT = "Current field of view: %d\" (*)";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CustomOverlaysTab customOverlaysTab;

    private final CatalogQueryFacade catalogQueryFacade;
    private final SpectralTypeLookupService mainSequenceSpectralTypeLookupService;
    private final SpectralTypeLookupService brownDwarfsSpectralTypeLookupService;
    private final DistanceLookupService distanceLookupService;
    private List<CatalogEntry> simbadEntries;
    private List<CatalogEntry> gaiaEntries;
    private List<CatalogEntry> gaiaTpmEntries;
    private List<CatalogEntry> allWiseEntries;
    private List<CatalogEntry> catWiseEntries;
    private List<CatalogEntry> catWiseTpmEntries;
    private List<CatalogEntry> catWiseRejectedEntries;
    private List<CatalogEntry> unWiseEntries;
    private List<CatalogEntry> panStarrsEntries;
    private List<CatalogEntry> sdssEntries;
    private List<CatalogEntry> twoMassEntries;
    private List<CatalogEntry> vhsEntries;
    private List<CatalogEntry> gaiaWDEntries;
    private List<CatalogEntry> spitzerEntries;
    private List<CatalogEntry> ssoEntries;

    private JPanel imagePanel;
    private JPanel rightPanel;
    private JPanel zooniversePanel1;
    private JPanel zooniversePanel2;
    private JCheckBox applyLimits;
    private JCheckBox saveContrast;
    private JCheckBox smoothImage;
    private JCheckBox invertColors;
    private JCheckBox borderEpoch;
    private JCheckBox staticDisplay;
    private JCheckBox simbadOverlay;
    private JCheckBox gaiaOverlay;
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
    private JCheckBox ssoOverlay;
    private JCheckBox ghostOverlay;
    private JCheckBox haloOverlay;
    private JCheckBox latentOverlay;
    private JCheckBox spikeOverlay;
    private JCheckBox gaiaProperMotion;
    private JCheckBox catWiseProperMotion;
    private JCheckBox useCustomOverlays;
    private JCheckBox dssImages;
    private JCheckBox sloanImages;
    private JCheckBox twoMassImages;
    private JCheckBox allwiseImages;
    private JCheckBox ps1Images;
    private JCheckBox createDataSheet;
    private JCheckBox skipBadImages;
    private JCheckBox skipSingleNodes;
    private JCheckBox markDifferences;
    private JCheckBox blinkMarkers;
    private JCheckBox hideMagnifier;
    private JCheckBox drawCrosshairs;
    private JCheckBox transposeProperMotion;
    private JComboBox wiseBands;
    private JComboBox epochs;
    private JSlider rawScaleSlider;
    private JSlider highScaleSlider;
    private JSlider lowScaleSlider;
    private JSlider stretchSlider;
    private JSlider minValueSlider;
    private JSlider maxValueSlider;
    private JSlider speedSlider;
    private JSlider zoomSlider;
    private JSlider epochSlider;
    private JSlider sensitivitySlider;
    private JTextField coordsField;
    private JTextField sizeField;
    private JTextField properMotionField;
    private JTextField differentSizeField;
    private JTextField transposeMotionField;
    private JTextArea crosshairCoords;
    private JTextArea downloadLog;
    private JRadioButton differentSizeButton;
    private JRadioButton showCatalogsButton;
    private JRadioButton showCirclesButton;
    private JLabel epochLabel;
    private JLabel changeFovLabel;
    private JLabel sensitivityLabel;
    private JTable collectionTable;
    private Timer timer;

    private BufferedImage wiseImage;
    private BufferedImage ps1Image;
    private BufferedImage sdssImage;
    private Map<String, ImageContainer> imagesW1;
    private Map<String, ImageContainer> imagesW2;
    private Map<String, Fits> images;
    private Map<String, CustomOverlay> customOverlays;
    private List<NumberPair> crosshairs;
    private FlipbookComponent[] flipbook;
    private ImageViewerTab imageViewer;

    private WiseBand wiseBand = WISE_BAND;
    private Epoch epoch = EPOCH;
    private int fieldOfView = 30;
    private int crosshairSize = 5;
    private int imageNumber;
    private int windowShift;
    private int quadrantCount;
    private int epochCount;
    private int epochCountW1;
    private int epochCountW2;
    private int numberOfEpochs;
    private int selectedEpochs;
    private int stretch = STRETCH;
    private int speed = SPEED;
    private int zoom = ZOOM;
    private int size = SIZE;

    private int maxSensitivity = LOW_CONTRAST / 10 * 2;
    private int sensitivity = SENSITIVITY;

    private int rawContrast = RAW_CONTRAST;
    private int lowContrast = LOW_CONTRAST;
    private int highContrast = HIGH_CONTRAST;
    private int lowContrastSaved = lowContrast;
    private int highContrastSaved = highContrast;

    private int minValue;
    private int maxValue;

    private double targetRa;
    private double targetDec;

    private double pixelX;
    private double pixelY;

    private int centerX;
    private int centerY;

    private int axisX;
    private int axisY;

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
    private boolean reloadImages;
    private boolean imageCutOff;
    private boolean timerStopped;
    private boolean hasException;
    private boolean panstarrsImages;
    private boolean sdssImages;

    public ImageViewerTab(JFrame baseFrame, JTabbedPane tabbedPane, CustomOverlaysTab customOverlaysTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.customOverlaysTab = customOverlaysTab;
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

            int controlPanelWidth = 250;
            int controlPanelHeight = 2075;

            JPanel controlPanel = new JPanel(new GridLayout(86, 1));
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
                    applyLimits.setSelected(true);
                    smoothImage.setSelected(true);
                    setContrast(LOW_CONTRAST, HIGH_CONTRAST);
                } else if (Epoch.isSubtracted(previousEpoch)) {
                    smoothImage.setSelected(false);
                    setContrast(lowContrastSaved, highContrastSaved);
                }
                createFlipbook();
            });

            JPanel whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel highScaleLabel = new JLabel(String.format("Contrast high scale: %d", highContrast));
            whitePanel.add(highScaleLabel);

            highScaleSlider = new JSlider(0, 1000, HIGH_CONTRAST);
            controlPanel.add(highScaleSlider);
            highScaleSlider.setBackground(Color.WHITE);
            highScaleSlider.addChangeListener((ChangeEvent e) -> {
                int savedValue = highContrast;
                highContrast = highScaleSlider.getValue();
                highScaleLabel.setText(String.format("Contrast high scale: %d", highContrast));
                setMaxSensitivity();
                if (!Epoch.isSubtracted(epoch)) {
                    highContrastSaved = highContrast;
                }
                if (markDifferences.isSelected() && flipbook != null && savedValue != highContrast) {
                    detectDifferences();
                }
            });

            JPanel grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            JLabel lowScaleLabel = new JLabel(String.format("Contrast low scale: %d", lowContrast));
            grayPanel.add(lowScaleLabel);

            lowScaleSlider = new JSlider(0, 100, LOW_CONTRAST);
            controlPanel.add(lowScaleSlider);
            lowScaleSlider.setBackground(Color.LIGHT_GRAY);
            lowScaleSlider.addChangeListener((ChangeEvent e) -> {
                int savedValue = lowContrast;
                lowContrast = lowScaleSlider.getValue();
                lowScaleLabel.setText(String.format("Contrast low scale: %d", lowContrast));
                setMaxSensitivity();
                if (!Epoch.isSubtracted(epoch)) {
                    lowContrastSaved = lowContrast;
                }
                if (lowContrast == 0) {
                    applyLimits.setSelected(false);
                    setContrast(10, HIGH_CONTRAST);
                    createFlipbook();
                }
                if (markDifferences.isSelected() && flipbook != null && savedValue != lowContrast) {
                    detectDifferences();
                }
            });

            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel rawScaleLabel = new JLabel(String.format("Raw image contrast: %d", rawContrast));
            whitePanel.add(rawScaleLabel);

            rawScaleSlider = new JSlider(1, 10, RAW_CONTRAST);
            controlPanel.add(rawScaleSlider);
            rawScaleSlider.setBackground(Color.WHITE);
            rawScaleSlider.addChangeListener((ChangeEvent e) -> {
                int savedValue = rawContrast;
                rawContrast = rawScaleSlider.getValue();
                rawScaleLabel.setText(String.format("Raw image contrast: %d", rawContrast));
                setMaxSensitivity();
                if (markDifferences.isSelected() && flipbook != null && savedValue != rawContrast) {
                    detectDifferences();
                }
            });

            grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            JLabel minValueLabel = new JLabel(String.format("Min pixel value: %d", minValue));
            grayPanel.add(minValueLabel);

            minValueSlider = new JSlider();
            controlPanel.add(minValueSlider);
            minValueSlider.setBackground(Color.LIGHT_GRAY);
            minValueSlider.addChangeListener((ChangeEvent e) -> {
                minValue = minValueSlider.getValue();
                minValueLabel.setText(String.format("Min pixel value: %d", minValue));
            });

            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel maxValueLabel = new JLabel(String.format("Max pixel value: %d", maxValue));
            whitePanel.add(maxValueLabel);

            maxValueSlider = new JSlider();
            controlPanel.add(maxValueSlider);
            maxValueSlider.setBackground(Color.WHITE);
            maxValueSlider.addChangeListener((ChangeEvent e) -> {
                maxValue = maxValueSlider.getValue();
                maxValueLabel.setText(String.format("Max pixel value: %d", maxValue));
            });

            grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            JLabel stretchLabel = new JLabel(String.format("Stretch control: %s", roundTo2Dec(stretch / 100f)));
            grayPanel.add(stretchLabel);

            stretchSlider = new JSlider(0, 100, STRETCH);
            controlPanel.add(stretchSlider);
            stretchSlider.setBackground(Color.LIGHT_GRAY);
            stretchSlider.addChangeListener((ChangeEvent e) -> {
                stretch = stretchSlider.getValue();
                stretchLabel.setText(String.format("Stretch control: %s", roundTo2Dec(stretch / 100f)));
            });

            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel speedLabel = new JLabel(String.format("Speed: %d ms", speed));
            whitePanel.add(speedLabel);

            speedSlider = new JSlider(0, 2000, SPEED);
            controlPanel.add(speedSlider);
            speedSlider.setBackground(Color.WHITE);
            speedSlider.addChangeListener((ChangeEvent e) -> {
                speed = speedSlider.getValue();
                timer.setDelay(speed);
                speedLabel.setText(String.format("Speed: %d ms", speed));
            });

            grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            JLabel zoomLabel = new JLabel(String.format("Zoom: %d", zoom));
            grayPanel.add(zoomLabel);

            zoomSlider = new JSlider(0, 2000, ZOOM);
            controlPanel.add(zoomSlider);
            zoomSlider.setBackground(Color.LIGHT_GRAY);
            zoomSlider.addChangeListener((ChangeEvent e) -> {
                zoom = zoomSlider.getValue();
                zoom = zoom < 100 ? 100 : zoom;
                zoomLabel.setText(String.format("Zoom: %d", zoom));
            });

            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            epochLabel = new JLabel(String.format(EPOCH_LABEL, selectedEpochs));
            whitePanel.add(epochLabel);

            epochSlider = new JSlider(2, NUMBER_OF_EPOCHS, NUMBER_OF_EPOCHS);
            controlPanel.add(epochSlider);
            epochSlider.setBackground(Color.WHITE);
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

            JPanel gridPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(gridPanel);
            applyLimits = new JCheckBox("Apply limits", true);
            gridPanel.add(applyLimits);
            applyLimits.addActionListener((ActionEvent evt) -> {
                setContrast(LOW_CONTRAST, HIGH_CONTRAST);
                createFlipbook();
            });
            smoothImage = new JCheckBox("Smoothing");
            gridPanel.add(smoothImage);

            gridPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(gridPanel);
            saveContrast = new JCheckBox("Keep contrast");
            gridPanel.add(saveContrast);
            saveContrast.addActionListener((ActionEvent evt) -> {
                if (saveContrast.isSelected() && !Epoch.isSubtracted(epoch)) {
                    lowContrastSaved = lowContrast;
                    highContrastSaved = highContrast;
                }
            });
            invertColors = new JCheckBox("Invert colors");
            gridPanel.add(invertColors);

            gridPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(gridPanel);
            borderEpoch = new JCheckBox("Border first");
            gridPanel.add(borderEpoch);
            staticDisplay = new JCheckBox("Static view");
            gridPanel.add(staticDisplay);
            staticDisplay.addActionListener((ActionEvent evt) -> {
                if (flipbook != null) {
                    if (staticDisplay.isSelected()) {
                        createStaticBook();
                    } else {
                        createFlipbook();
                    }
                }
            });

            JButton resetDefaultsButton = new JButton("Image processing defaults");
            controlPanel.add(resetDefaultsButton);
            resetDefaultsButton.addActionListener((ActionEvent evt) -> {
                applyLimits.setSelected(true);
                if (Epoch.isSubtracted(epoch)) {
                    smoothImage.setSelected(true);
                } else {
                    smoothImage.setSelected(false);
                }
                stretchSlider.setValue(stretch = STRETCH);
                rawScaleSlider.setValue(rawContrast = RAW_CONTRAST);
                setContrast(LOW_CONTRAST, HIGH_CONTRAST);
                createFlipbook();
            });

            controlPanel.add(new JLabel(underline("Overlays:")));

            JPanel overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            simbadOverlay = new JCheckBox(SimbadCatalogEntry.CATALOG_NAME);
            simbadOverlay.setForeground(Color.RED);
            overlayPanel.add(simbadOverlay);
            gaiaOverlay = new JCheckBox(GaiaCatalogEntry.CATALOG_NAME);
            gaiaOverlay.setForeground(Color.CYAN.darker());
            overlayPanel.add(gaiaOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            allWiseOverlay = new JCheckBox(AllWiseCatalogEntry.CATALOG_NAME);
            allWiseOverlay.setForeground(Color.GREEN.darker());
            overlayPanel.add(allWiseOverlay);
            catWiseOverlay = new JCheckBox(CatWiseCatalogEntry.CATALOG_NAME);
            catWiseOverlay.setForeground(Color.MAGENTA);
            overlayPanel.add(catWiseOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            unWiseOverlay = new JCheckBox(UnWiseCatalogEntry.CATALOG_NAME);
            unWiseOverlay.setForeground(JColor.MINT.val);
            overlayPanel.add(unWiseOverlay);
            panStarrsOverlay = new JCheckBox(PanStarrsCatalogEntry.CATALOG_NAME);
            panStarrsOverlay.setForeground(JColor.BROWN.val);
            overlayPanel.add(panStarrsOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            sdssOverlay = new JCheckBox(SDSSCatalogEntry.CATALOG_NAME);
            sdssOverlay.setForeground(JColor.STEEL.val);
            overlayPanel.add(sdssOverlay);
            spectrumOverlay = new JCheckBox("SDSS spectra");
            spectrumOverlay.setForeground(JColor.OLIVE.val);
            overlayPanel.add(spectrumOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            gaiaWDOverlay = new JCheckBox(GaiaWDCatalogEntry.CATALOG_SHORT_NAME);
            gaiaWDOverlay.setForeground(JColor.PURPLE.val);
            overlayPanel.add(gaiaWDOverlay);
            vhsOverlay = new JCheckBox(VHSCatalogEntry.CATALOG_NAME);
            vhsOverlay.setForeground(JColor.PINK.val);
            overlayPanel.add(vhsOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            twoMassOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;" + TwoMassCatalogEntry.CATALOG_NAME + "&nbsp;</span>"));
            twoMassOverlay.setForeground(JColor.ORANGE.val);
            overlayPanel.add(twoMassOverlay);
            spitzerOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;" + SpitzerCatalogEntry.CATALOG_SHORT_NAME + "&nbsp;</span>"));
            spitzerOverlay.setForeground(JColor.YELLOW.val);
            overlayPanel.add(spitzerOverlay);

            ssoOverlay = new JCheckBox(SSOCatalogEntry.CATALOG_NAME);
            ssoOverlay.setForeground(Color.BLUE);
            controlPanel.add(ssoOverlay);

            controlPanel.add(new JLabel(underline("PM vectors:")));

            JPanel properMotionPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(properMotionPanel);
            gaiaProperMotion = new JCheckBox(GaiaCatalogEntry.CATALOG_NAME);
            gaiaProperMotion.setForeground(Color.CYAN.darker());
            properMotionPanel.add(gaiaProperMotion);
            catWiseProperMotion = new JCheckBox(CatWiseCatalogEntry.CATALOG_NAME);
            catWiseProperMotion.setForeground(Color.MAGENTA);
            properMotionPanel.add(catWiseProperMotion);

            properMotionPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(properMotionPanel);
            properMotionPanel.add(new JLabel("Total PM (mas/yr) >"));
            properMotionField = new JTextField(String.valueOf(100));
            properMotionPanel.add(properMotionField);
            properMotionField.addActionListener((ActionEvent evt) -> {
                gaiaTpmEntries = null;
                catWiseTpmEntries = null;
            });

            controlPanel.add(new JLabel(underline("Sources affected by WISE artifacts:")));

            JPanel artifactPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(artifactPanel);
            ghostOverlay = new JCheckBox("Ghosts");
            ghostOverlay.setForeground(Color.MAGENTA.darker());
            artifactPanel.add(ghostOverlay);
            haloOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;Halos&nbsp;</span>"));
            haloOverlay.setForeground(Color.YELLOW);
            artifactPanel.add(haloOverlay);

            artifactPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(artifactPanel);
            latentOverlay = new JCheckBox("Latents");
            latentOverlay.setForeground(Color.GREEN.darker());
            artifactPanel.add(latentOverlay);
            spikeOverlay = new JCheckBox(html("<span style='background:black'>&nbsp;Spikes&nbsp;</span>"));
            spikeOverlay.setForeground(Color.ORANGE);
            artifactPanel.add(spikeOverlay);

            controlPanel.add(new JLabel(underline("Mouse left click w/o overlays:")));

            showCatalogsButton = new JRadioButton("Show catalog entries for object", true);
            controlPanel.add(showCatalogsButton);

            JRadioButton recenterImagesButton = new JRadioButton("Recenter images on object", false);
            controlPanel.add(recenterImagesButton);

            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(showCatalogsButton);
            buttonGroup.add(recenterImagesButton);

            controlPanel.add(new JLabel(underline("Mouse wheel click:")));

            controlPanel.add(new JLabel("Select images to display:"));

            dssImages = new JCheckBox("DSS 1Red, 1Blue, 2Red, 2Blue, 2IR", false);
            controlPanel.add(dssImages);
            dssImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            sloanImages = new JCheckBox("SDSS u, g, r, i & z bands", false);
            controlPanel.add(sloanImages);
            sloanImages.addActionListener((ActionEvent evt) -> {
                createDataSheet.setSelected(false);
            });

            twoMassImages = new JCheckBox("2MASS J, H & K bands", false);
            controlPanel.add(twoMassImages);
            twoMassImages.addActionListener((ActionEvent evt) -> {
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

            createDataSheet = new JCheckBox("Create object info sheet", false);
            controlPanel.add(createDataSheet);
            createDataSheet.addActionListener((ActionEvent evt) -> {
                setImageViewer(this);
                if (createDataSheet.isSelected()) {
                    dssImages.setSelected(false);
                    sloanImages.setSelected(false);
                    twoMassImages.setSelected(false);
                    allwiseImages.setSelected(false);
                    ps1Images.setSelected(false);
                }
            });

            changeFovLabel = new JLabel(String.format(CHANGE_FOV_TEXT, fieldOfView));
            controlPanel.add(changeFovLabel);

            JLabel fovLabel = new JLabel("(*) Spin wheel upon WISE images to change FoV");
            Font font = fovLabel.getFont();
            font = font.deriveFont(9f);
            fovLabel.setFont(font);
            controlPanel.add(fovLabel);

            controlPanel.add(new JLabel(underline("Mouse right click:")));

            differentSizeButton = new JRadioButton("Show object in different FoV", true);
            controlPanel.add(differentSizeButton);

            buttonGroup = new ButtonGroup();
            buttonGroup.add(differentSizeButton);

            JPanel differentSizePanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(differentSizePanel);
            differentSizePanel.add(new JLabel("Enter FoV (arcsec)"));
            differentSizeField = new JTextField(String.valueOf(100));
            differentSizePanel.add(differentSizeField);

            controlPanel.add(new JLabel(underline("Nearest Zooniverse subjects:")));

            zooniversePanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(zooniversePanel1);

            zooniversePanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(zooniversePanel2);

            controlPanel.add(new JLabel(underline("Advanced controls:")));

            JPanel differencesPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(differencesPanel);
            differencesPanel.setBackground(Color.WHITE);

            markDifferences = new JCheckBox("Mark differences");
            differencesPanel.add(markDifferences);
            markDifferences.setBackground(Color.WHITE);
            markDifferences.addActionListener((ActionEvent evt) -> {
                if (markDifferences.isSelected() && flipbook != null) {
                    detectDifferences();
                }
            });

            blinkMarkers = new JCheckBox("Blink");
            differencesPanel.add(blinkMarkers);
            blinkMarkers.setBackground(Color.WHITE);
            blinkMarkers.addActionListener((ActionEvent evt) -> {
                if (markDifferences.isSelected() && flipbook != null) {
                    detectDifferences();
                }
            });

            differencesPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(differencesPanel);
            differencesPanel.setBackground(Color.WHITE);

            showCirclesButton = new JRadioButton("Mark with circles", false);
            differencesPanel.add(showCirclesButton);
            showCirclesButton.setBackground(Color.WHITE);
            showCirclesButton.addActionListener((ActionEvent evt) -> {
                if (markDifferences.isSelected() && flipbook != null) {
                    detectDifferences();
                }
            });

            JRadioButton showDotsButton = new JRadioButton("Mark with dots", true);
            differencesPanel.add(showDotsButton);
            showDotsButton.setBackground(Color.WHITE);
            showDotsButton.addActionListener((ActionEvent evt) -> {
                if (markDifferences.isSelected() && flipbook != null) {
                    detectDifferences();
                }
            });

            buttonGroup = new ButtonGroup();
            buttonGroup.add(showCirclesButton);
            buttonGroup.add(showDotsButton);

            differencesPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(differencesPanel);
            differencesPanel.setBackground(Color.WHITE);

            sensitivityLabel = new JLabel(String.format("Sensitivity: %d/%d", maxSensitivity + 1 - sensitivity, maxSensitivity));
            differencesPanel.add(sensitivityLabel);

            sensitivitySlider = new JSlider(1, maxSensitivity, SENSITIVITY);
            differencesPanel.add(sensitivitySlider);
            sensitivitySlider.setBackground(Color.WHITE);
            sensitivitySlider.addChangeListener((ChangeEvent e) -> {
                int savedValue = sensitivity;
                sensitivity = sensitivitySlider.getValue();
                sensitivityLabel.setText(String.format("Sensitivity: %d/%d", maxSensitivity + 1 - sensitivity, maxSensitivity));
                if (markDifferences.isSelected() && flipbook != null && savedValue != sensitivity) {
                    detectDifferences();
                }
            });

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

            JLabel crosshairLabel = new JLabel("(*) Wheel click on desired position / Spin wheel to");
            crosshairLabel.setFont(font);
            controlPanel.add(crosshairLabel);

            crosshairLabel = new JLabel("change the size / Wheel click cross center to delete");
            crosshairLabel.setFont(font);
            controlPanel.add(crosshairLabel);

            controlPanel.add(new JLabel("Crosshairs coordinates:"));

            crosshairCoords = new JTextArea();
            controlPanel.add(new JScrollPane(crosshairCoords));
            crosshairCoords.setFont(font);
            crosshairCoords.setEditable(false);

            controlPanel.add(new JLabel(underline("Image player controls:")));

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
                        createAnimatedGif(file);
                    }
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            controlPanel.add(new JLabel(underline("Navigation buttons:")));

            JPanel navigationButtons = new JPanel(new GridLayout(1, 2));
            controlPanel.add(navigationButtons);

            JButton moveLeftButton = new JButton("Move left");
            navigationButtons.add(moveLeftButton);
            moveLeftButton.addActionListener((ActionEvent evt) -> {
                double distance = size * SIZE_FACTOR * OVERLAP_FACTOR / DEG_ARCSEC;
                double newRa = targetRa + distance / cos(toRadians(targetDec));
                newRa = newRa > 360 ? newRa - 360 : newRa;
                coordsField.setText(roundTo7DecNZ(newRa) + " " + targetDec);
                createFlipbook();
            });

            JButton moveRightButton = new JButton("Move right");
            navigationButtons.add(moveRightButton);
            moveRightButton.addActionListener((ActionEvent evt) -> {
                double distance = size * SIZE_FACTOR * OVERLAP_FACTOR / DEG_ARCSEC;
                double newRa = targetRa - distance / cos(toRadians(targetDec));
                newRa = newRa < 0 ? newRa + 360 : newRa;
                coordsField.setText(roundTo7DecNZ(newRa) + " " + targetDec);
                createFlipbook();
            });

            navigationButtons = new JPanel(new GridLayout(1, 2));
            controlPanel.add(navigationButtons);

            JButton moveUpButton = new JButton("Move up");
            navigationButtons.add(moveUpButton);
            moveUpButton.addActionListener((ActionEvent evt) -> {
                double newDec = targetDec + size * SIZE_FACTOR * OVERLAP_FACTOR / DEG_ARCSEC;
                newDec = newDec > 90 ? 90 : newDec;
                coordsField.setText(targetRa + " " + roundTo7DecNZ(newDec));
                createFlipbook();
            });

            JButton moveDownButton = new JButton("Move down");
            navigationButtons.add(moveDownButton);
            moveDownButton.addActionListener((ActionEvent evt) -> {
                double newDec = targetDec - size * SIZE_FACTOR * OVERLAP_FACTOR / DEG_ARCSEC;
                newDec = newDec < -90 ? -90 : newDec;
                coordsField.setText(targetRa + " " + roundTo7DecNZ(newDec));
                createFlipbook();
            });

            transposeProperMotion = new JCheckBox(underline("Transpose proper motion:"));
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
            transposeMotionField.addActionListener((ActionEvent evt) -> {
                if (transposeProperMotion.isSelected() && !transposeMotionField.getText().isEmpty()) {
                    imagesW1.clear();
                    imagesW2.clear();
                    reloadImages = true;
                    createFlipbook();
                }
            });

            useCustomOverlays = new JCheckBox(underline("Custom overlays:"));
            controlPanel.add(useCustomOverlays);
            customOverlays = customOverlaysTab.getCustomOverlays();
            useCustomOverlays.addActionListener((ActionEvent evt) -> {
                if (customOverlays.isEmpty()) {
                    showInfoDialog(baseFrame, "No custom overlays have been added yet.");
                    useCustomOverlays.setSelected(false);
                } else {
                    GridLayout layout = (GridLayout) controlPanel.getLayout();
                    if (useCustomOverlays.isSelected()) {
                        layout.setRows(layout.getRows() + customOverlays.size());
                        customOverlays.values().forEach(customOverlay -> {
                            JCheckBox overlayCheckBox = new JCheckBox(customOverlay.getName());
                            overlayCheckBox.setForeground(customOverlay.getColor());
                            customOverlay.setCheckBox(overlayCheckBox);
                            controlPanel.add(overlayCheckBox);
                        });
                    } else {
                        layout.setRows(layout.getRows() - customOverlays.size());
                        customOverlays.values().forEach((customOverlay) -> {
                            JCheckBox checkBox = customOverlay.getCheckBox();
                            if (checkBox != null) {
                                controlPanel.remove(checkBox);
                            }
                        });
                    }
                    controlPanel.updateUI();
                    baseFrame.setVisible(true);
                }
            });

            timer = new Timer(speed, (ActionEvent e) -> {
                try {
                    staticDisplay.setSelected(false);
                    if (imageNumber > flipbook.length - 1) {
                        imageNumber = 0;
                    }
                    imagePanel.removeAll();

                    FlipbookComponent component = flipbook[imageNumber];
                    component.setEpochCount(selectedEpochs);
                    imagePanel.setBorder(createEtchedBorder(component.getTitle()));
                    wiseImage = processImage(component);

                    ImageIcon icon = new ImageIcon(wiseImage);
                    JLabel imageLabel = new JLabel(icon);
                    if (borderEpoch.isSelected() && component.isFirstEpoch()) {
                        imageLabel.setBorder(BorderFactory.createDashedBorder(null, 5, 5));
                    } else {
                        imageLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                    }

                    imagePanel.add(imageLabel);

                    // Initialize positions of magnified WISE image
                    int width = 50;
                    int height = 50;
                    int imageWidth = wiseImage.getWidth();
                    int imageHeight = wiseImage.getHeight();
                    if (centerX == 0 && centerY == 0) {
                        centerX = imageWidth / 2;
                        centerY = imageHeight / 2;
                    }
                    int upperLeftX = centerX - (width / 2);
                    int upperLeftY = centerY - (height / 2);
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
                    if (ps1Image != null) {
                        BufferedImage processedPs1Image = zoom(rotate(ps1Image, quadrantCount), zoom);

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
                    if (sdssImage != null) {
                        BufferedImage processedSdssImage = zoom(rotate(sdssImage, quadrantCount), zoom);

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
                            // Undo rotation of pixel coordinates in case of image rotation
                            if (quadrantCount > 0 && quadrantCount < 4) {
                                double anchorX = wiseImage.getWidth() / 2;
                                double anchorY = wiseImage.getHeight() / 2;
                                double angle = (4 - quadrantCount) * 90;
                                double theta = toRadians(angle);
                                Point2D ptSrc = new Point(mouseX, mouseY);
                                Point2D ptDst = new Point();
                                AffineTransform.getRotateInstance(theta, anchorX, anchorY).transform(ptSrc, ptDst);
                                mouseX = (int) round(ptDst.getX());
                                mouseY = (int) round(ptDst.getY());
                            }
                            NumberPair coords = getObjectCoordinates(mouseX, mouseY);
                            double newRa = coords.getX();
                            double newDec = coords.getY();
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
                                            NumberPair c = getObjectCoordinates(
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
                                        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                                        int screenHeight = screenSize.height;
                                        int verticalSpacing = screenHeight / 6;
                                        Counter counter = new Counter(verticalSpacing);
                                        if (dssImages.isSelected()) {
                                            displayDssImages(newRa, newDec, fieldOfView, counter);
                                        }
                                        if (sloanImages.isSelected()) {
                                            displaySdssImages(newRa, newDec, fieldOfView, counter);
                                        }
                                        if (twoMassImages.isSelected()) {
                                            display2MassImages(newRa, newDec, fieldOfView, counter);
                                        }
                                        if (allwiseImages.isSelected()) {
                                            displayAllwiseImages(newRa, newDec, fieldOfView, counter);
                                        }
                                        if (ps1Images.isSelected()) {
                                            displayPs1Images(newRa, newDec, fieldOfView, counter);
                                        }
                                        if (createDataSheet.isSelected()) {
                                            CompletableFuture.supplyAsync(() -> new InfoSheet(newRa, newDec, fieldOfView, getImageViewer()).create(baseFrame));
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
                                    if (gaiaProperMotion.isSelected() && gaiaTpmEntries != null) {
                                        showPMInfo(gaiaTpmEntries, mouseX, mouseY, Color.CYAN.darker());
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
                                        if (showCatalogsButton.isSelected()) {
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
                            centerX = evt.getX();
                            centerY = evt.getY();
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
                        if (drawCrosshairs.isSelected()) {
                            if (notches < 0) {
                                crosshairSize++;
                            } else if (crosshairSize > 0) {
                                crosshairSize--;
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
                    if (!staticDisplay.isSelected()) {
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
                    if (flipbook != null && !staticDisplay.isSelected() && !hasException && !timerStopped) {
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

    private NumberPair getObjectCoordinates(int x, int y) {
        double diffX = getScaledValue(pixelX) - x;
        double diffY = getScaledValue(pixelY) - y;
        double conversionFactor = getConversionFactor();
        diffX *= conversionFactor;
        diffY *= conversionFactor;
        double posY = targetDec + diffY;
        double posX = targetRa + diffX / cos(toRadians((targetDec + posY) / 2));
        // Correct RA if < 0 or > 360
        posX = posX < 0 ? posX + 360 : posX;
        posX = posX > 360 ? posX - 360 : posX;
        return new NumberPair(posX, posY);
    }

    private NumberPair getPixelCoordinates(double ra, double dec) {
        // Correct RA if difference between targetRa and ra > 300
        double correctedRa = targetRa;
        if (abs(targetRa - ra) > 300) {
            if (targetRa > ra) {
                ra = 360 + ra;
            } else {
                correctedRa = 360 + targetRa;
            }
        }
        double diffX = (correctedRa - ra) * cos(toRadians((targetDec + dec) / 2));
        double diffY = targetDec - dec;
        double conversionFactor = getConversionFactor();
        diffX /= -conversionFactor;
        diffY /= -conversionFactor;
        double posX = getScaledValue(pixelX) - diffX;
        double posY = getScaledValue(pixelY) - diffY;
        return new NumberPair(posX, posY);
    }

    private double getConversionFactor() {
        return SIZE_FACTOR * size / zoom / DEG_ARCSEC;
    }

    private void createFlipbook() {
        CompletableFuture.supplyAsync(() -> assembleFlipbook());
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
                size = (int) round(toInteger(sizeField.getText()) / SIZE_FACTOR);
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
                imagesW1 = new HashMap<>();
                imagesW2 = new HashMap<>();
                images = new HashMap<>();
                crosshairs = new ArrayList<>();
                crosshairCoords.setText("");
                hasException = false;
                centerX = centerY = 0;
                axisX = axisY = size;
                windowShift = 0;
                epochCountW1 = 0;
                epochCountW2 = 0;
                imageCutOff = false;
                simbadEntries = null;
                gaiaEntries = null;
                gaiaTpmEntries = null;
                allWiseEntries = null;
                catWiseEntries = null;
                catWiseTpmEntries = null;
                catWiseRejectedEntries = null;
                unWiseEntries = null;
                panStarrsEntries = null;
                sdssEntries = null;
                twoMassEntries = null;
                vhsEntries = null;
                gaiaWDEntries = null;
                spitzerEntries = null;
                ssoEntries = null;
                if (useCustomOverlays.isSelected()) {
                    customOverlays.values().forEach((customOverlay) -> {
                        customOverlay.setCatalogEntries(null);
                    });
                }
                ps1Image = null;
                if (panstarrsImages) {
                    CompletableFuture.supplyAsync(() -> ps1Image = fetchPs1Image(targetRa, targetDec, size));
                }
                sdssImage = null;
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
                applyLimits.setSelected(true);
                stretchSlider.setValue(stretch = STRETCH);
                rawScaleSlider.setValue(rawContrast = RAW_CONTRAST);
                if (!saveContrast.isSelected()) {
                    setContrast(LOW_CONTRAST, HIGH_CONTRAST);
                }
                try {
                    getImageData(1, numberOfEpochs + 3);
                    moreImagesAvailable = true;
                } catch (FileNotFoundException ex) {
                    try {
                        getImageData(1, numberOfEpochs);
                        oneMoreImageAvailable = true;
                    } catch (FileNotFoundException ex2) {
                    }
                }
            }
            previousSize = size;
            previousRa = targetRa;
            previousDec = targetDec;
            imageNumber = 0;

            if (loadImages || reloadImages) {
                int totalEpochs = selectedEpochs * 2 + (oneMoreImageAvailable ? 1 : 0);
                List<Integer> requestedEpochs = new ArrayList<>();
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
                downloadLog = new JTextArea();
                downloadLog.setFont(new JLabel().getFont());
                downloadLog.setEditable(false);
                DefaultCaret caret = (DefaultCaret) downloadLog.getCaret();
                caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                imagePanel.add(downloadLog);
                baseFrame.setVisible(true);
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
                downloadLog.setCaretPosition(0);
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
                        flipbook[i] = new FlipbookComponent(wiseBand.val, i);
                    }
                    break;
                case ASCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 0; i < epochCount; i += 2) {
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i);
                    }
                    break;
                case DESCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 1; i < epochCount; i += 2) {
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i);
                    }
                    break;
                case ASCENDING_DESCENDING:
                    flipbook = new FlipbookComponent[epochCount];
                    k = 0;
                    for (int i = 0; i < epochCount; i += 2) {
                        flipbook[k] = new FlipbookComponent(wiseBand.val, i);
                        k++;
                    }
                    for (int i = 1; i < epochCount; i += 2) {
                        flipbook[k] = new FlipbookComponent(wiseBand.val, i);
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
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 900 + i, true);
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
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 800 + i, true);
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
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 900 + i, true);
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
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 800 + i, true);
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
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 600, true);
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 700, true);
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
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, 101 + (i / 2), true);
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
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 100, true);
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 300, true);
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
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 100, true);
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 200, true);
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
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 400, true);
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 500, true);
                    break;
            }

            FlipbookComponent component = flipbook[0];
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
            setMinMaxValues(minVal, maxVal, avgVal);

            if (markDifferences.isSelected()) {
                detectDifferences();
            }

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

    private void createStaticBook() {
        timer.stop();
        JPanel grid = new JPanel(new GridLayout(4, 4));
        for (FlipbookComponent component : flipbook) {
            component.setEpochCount(selectedEpochs);
            BufferedImage image = processImage(component);
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

    private void createAnimatedGif(File file) throws IOException {
        timer.stop();
        BufferedImage[] imageSet = new BufferedImage[flipbook.length];
        int i = 0;
        for (FlipbookComponent component : flipbook) {
            imageSet[i++] = processImage(component);
        }
        if (imageSet.length > 0) {
            GifSequencer sequencer = new GifSequencer();
            sequencer.generateFromBI(imageSet, file, speed / 10, true);
        }
    }

    public BufferedImage processImage(FlipbookComponent component) {
        BufferedImage image;
        if (wiseBand.equals(WiseBand.W1W2)) {
            image = createComposite(component.getEpoch());
        } else {
            image = createImage(component.getBand(), component.getEpoch());
        }
        image = zoom(image, zoom);
        if (markDifferences.isSelected()) {
            for (NumberTriplet diffPixel : component.getDiffPixels()) {
                Circle circle = new Circle(getScaledValue(diffPixel.getX()), getScaledValue(diffPixel.getY()), getScaledValue(diffPixel.getZ()), Color.RED);
                circle.draw(image.getGraphics());
            }
        }
        image = flip(image);
        addOverlaysAndPMVectors(image);
        if (drawCrosshairs.isSelected()) {
            for (int i = 0; i < crosshairs.size(); i++) {
                NumberPair crosshair = crosshairs.get(i);
                CrossHair drawable = new CrossHair(crosshair.getX() * zoom, crosshair.getY() * zoom, zoom * crosshairSize / 100, Color.RED, i + 1);
                drawable.draw(image.getGraphics());
            }
        }
        return rotate(image, quadrantCount);
    }

    private void addOverlaysAndPMVectors(BufferedImage image) {
        if (simbadOverlay.isSelected()) {
            if (simbadEntries == null) {
                simbadEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> simbadEntries = fetchCatalogEntries(new SimbadCatalogEntry()));
            } else {
                drawOverlay(image, simbadEntries, Color.RED, Shape.CIRCLE);
            }
        }
        if (gaiaOverlay.isSelected()) {
            if (gaiaEntries == null) {
                gaiaEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> gaiaEntries = fetchCatalogEntries(new GaiaCatalogEntry()));
            } else {
                drawOverlay(image, gaiaEntries, Color.CYAN.darker(), Shape.CIRCLE);
            }
        }
        if (allWiseOverlay.isSelected()) {
            if (allWiseEntries == null) {
                allWiseEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> allWiseEntries = fetchCatalogEntries(new AllWiseCatalogEntry()));
            } else {
                drawOverlay(image, allWiseEntries, Color.GREEN.darker(), Shape.CIRCLE);
            }
        }
        if (catWiseOverlay.isSelected()) {
            if (catWiseEntries == null) {
                catWiseEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> catWiseEntries = fetchCatalogEntries(new CatWiseCatalogEntry()));
            } else {
                drawOverlay(image, catWiseEntries, Color.MAGENTA, Shape.CIRCLE);
            }
        }
        if (unWiseOverlay.isSelected()) {
            if (unWiseEntries == null) {
                unWiseEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> unWiseEntries = fetchCatalogEntries(new UnWiseCatalogEntry()));
            } else {
                drawOverlay(image, unWiseEntries, JColor.MINT.val, Shape.CIRCLE);
            }
        }
        if (panStarrsOverlay.isSelected()) {
            if (panStarrsEntries == null) {
                panStarrsEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> panStarrsEntries = fetchCatalogEntries(new PanStarrsCatalogEntry()));
            } else {
                drawOverlay(image, panStarrsEntries, JColor.BROWN.val, Shape.CIRCLE);
            }
        }
        if (sdssOverlay.isSelected()) {
            if (sdssEntries == null) {
                sdssEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> sdssEntries = fetchCatalogEntries(new SDSSCatalogEntry()));
            } else {
                drawOverlay(image, sdssEntries, JColor.STEEL.val, Shape.CIRCLE);
            }
        }
        if (spectrumOverlay.isSelected()) {
            if (sdssEntries == null) {
                sdssEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> sdssEntries = fetchCatalogEntries(new SDSSCatalogEntry()));
            } else {
                drawSectrumOverlay(image, sdssEntries);
            }
        }
        if (twoMassOverlay.isSelected()) {
            if (twoMassEntries == null) {
                twoMassEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> twoMassEntries = fetchCatalogEntries(new TwoMassCatalogEntry()));
            } else {
                drawOverlay(image, twoMassEntries, JColor.ORANGE.val, Shape.CIRCLE);
            }
        }
        if (vhsOverlay.isSelected()) {
            if (vhsEntries == null) {
                vhsEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> vhsEntries = fetchCatalogEntries(new VHSCatalogEntry()));
            } else {
                drawOverlay(image, vhsEntries, JColor.PINK.val, Shape.CIRCLE);
            }
        }
        if (gaiaWDOverlay.isSelected()) {
            if (gaiaWDEntries == null) {
                gaiaWDEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> gaiaWDEntries = fetchCatalogEntries(new GaiaWDCatalogEntry()));
            } else {
                drawOverlay(image, gaiaWDEntries, JColor.PURPLE.val, Shape.CIRCLE);
            }
        }
        if (spitzerOverlay.isSelected()) {
            if (spitzerEntries == null) {
                spitzerEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> spitzerEntries = fetchCatalogEntries(new SpitzerCatalogEntry()));
            } else {
                drawOverlay(image, spitzerEntries, JColor.YELLOW.val, Shape.CIRCLE);
            }
        }
        if (ssoOverlay.isSelected()) {
            if (ssoEntries == null) {
                ssoEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> ssoEntries = fetchCatalogEntries(new SSOCatalogEntry()));
            } else {
                drawOverlay(image, ssoEntries, Color.BLUE, Shape.CIRCLE);
            }
        }
        if (ghostOverlay.isSelected() || haloOverlay.isSelected() || latentOverlay.isSelected() || spikeOverlay.isSelected()) {
            if (catWiseEntries == null) {
                catWiseEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> catWiseEntries = fetchCatalogEntries(new CatWiseCatalogEntry()));
            } else {
                drawArtifactOverlay(image, catWiseEntries);
            }
            if (catWiseRejectedEntries == null) {
                catWiseRejectedEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> catWiseRejectedEntries = fetchCatalogEntries(new CatWiseRejectedEntry()));
            } else {
                drawArtifactOverlay(image, catWiseRejectedEntries);
            }
        }
        if (useCustomOverlays.isSelected()) {
            customOverlays.values().forEach((customOverlay) -> {
                if (customOverlay.getCheckBox().isSelected()) {
                    if (customOverlay.getCatalogEntries() == null) {
                        customOverlay.setCatalogEntries(Collections.emptyList());
                        CompletableFuture.supplyAsync(() -> fetchGenericCatalogEntries(customOverlay));
                    } else {
                        drawOverlay(image, customOverlay.getCatalogEntries(), customOverlay.getColor(), customOverlay.getShape());
                    }
                }
            });
        }
        if (gaiaProperMotion.isSelected()) {
            if (gaiaTpmEntries == null) {
                gaiaTpmEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> gaiaTpmEntries = fetchTpmCatalogEntries(new GaiaCatalogEntry()));
            } else {
                drawPMVectors(image, gaiaTpmEntries, Color.CYAN.darker());
            }
        }
        if (catWiseProperMotion.isSelected()) {
            if (catWiseTpmEntries == null) {
                catWiseTpmEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> catWiseTpmEntries = fetchTpmCatalogEntries(new CatWiseCatalogEntry()));
            } else {
                drawPMVectors(image, catWiseTpmEntries, Color.MAGENTA);
            }
        }
    }

    private double getScaledValue(double value) {
        return zoom * value / size;
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
            double naxis1 = header.getDoubleValue("NAXIS1");
            double naxis2 = header.getDoubleValue("NAXIS2");
            double minObsEpoch = header.getDoubleValue("MJDMIN");
            LocalDateTime obsDate = convertMJDToDateTime(new BigDecimal(Double.toString(minObsEpoch)));
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values = (float[][]) imageData.getData();
            // Skip images with too many zero values
            if (naxis2 > 0) {
                naxis1 = values[0].length;
            }
            int zeroValues = 0;
            for (int j = 0; j < naxis2; j++) {
                for (int k = 0; k < naxis1; k++) {
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
                double maxAllowed = naxis1 * naxis2 / 20;
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
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            Header header = hdu.getHeader();
            double crpix1 = header.getDoubleValue("CRPIX1");
            double crpix2 = header.getDoubleValue("CRPIX2");
            double naxis1 = header.getDoubleValue("NAXIS1");
            double naxis2 = header.getDoubleValue("NAXIS2");
            if (naxis1 != naxis2) {
                imageCutOff = true;
            }
            pixelX = crpix1;
            pixelY = size - crpix2;
            axisX = size;
            axisY = size;
            for (int i = 1; i < imageGroup.size(); i++) {
                fits = stackImages(fits, imageGroup.get(i).getImage());
            }
            int imageCount = imageGroup.size();
            addImage(band, epochCount, imageCount > 1 ? takeAverage(fits, imageCount) : fits);
            writeLogEntry("band " + band + " | image " + epochCount + " | year " + year + " | month " + month);
            epochCount++;
        }
    }

    private void writeLogEntry(String log) {
        downloadLog.append(log + LINE_SEP_TEXT_AREA);
        baseFrame.setVisible(true);
        //System.out.println(log);
    }

    private List<Integer> provideAlternativeEpochs(int requestedEpoch, List<Integer> requestedEpochs) {
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

            float[][] addedValues = new float[axisY][axisX];
            for (int i = 0; i < axisY; i++) {
                for (int j = 0; j < axisX; j++) {
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
        String imageUrl;
        if (transposeProperMotion.isSelected() && !transposeMotionField.getText().isEmpty()) {
            NumberPair properMotion = getCoordinates(transposeMotionField.getText());
            double pmra = properMotion.getX();
            double pmdec = properMotion.getY();
            int totalEpochs = epoch > 1 ? epoch + numberOfEpochs / 2 : epoch;
            double pmraOfOneEpoch = (pmra / 2) / DEG_MAS;
            double pmdecOfOneEpoch = (pmdec / 2) / DEG_MAS;
            double pmraOfEpochs = totalEpochs * pmraOfOneEpoch;
            double pmdecOfEpochs = totalEpochs * pmdecOfOneEpoch;
            double ra = targetRa + pmraOfEpochs / cos(toRadians(targetDec));
            double dec = targetDec + pmdecOfEpochs;
            imageUrl = createImageUrl(ra, dec, size, band, epoch);
        } else {
            imageUrl = createImageUrl(targetRa, targetDec, size, band, epoch);
        }
        HttpURLConnection connection = establishHttpConnection(imageUrl);
        return connection.getInputStream();
    }

    private String createImageUrl(double targetRa, double targetDec, int size, int band, int epoch) throws MalformedURLException {
        return WISE_VIEW_URL + "?ra=" + targetRa + "&dec=" + targetDec + "&size=" + size + "&band=" + band + "&epoch=" + epoch;
    }

    private void detectDifferences() {
        // 92.1944649 18.1008679
        for (int i = 0; i < flipbook.length; i++) {
            FlipbookComponent component1 = flipbook[i];
            FlipbookComponent component2 = flipbook[i + 1 == flipbook.length ? 0 : i + 1];
            int band = component1.getBand();
            int epoch1 = component1.getEpoch();
            int epoch2 = component2.getEpoch();
            List<NumberTriplet> diffPixels = new ArrayList<>();
            if (band == 1 || band == 12) {
                detectDifferencesPerBand(1, epoch1, epoch2, diffPixels);
            }
            if (band == 2 || band == 12) {
                detectDifferencesPerBand(2, epoch1, epoch2, diffPixels);
            }
            component2.setDiffPixels(diffPixels);
        }
    }

    private void detectDifferencesPerBand(int band, int epoch1, int epoch2, List<NumberTriplet> diffPixels) {
        try {
            Fits fits = getImage(band, epoch1);
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values1 = (float[][]) imageData.getData();

            fits = getImage(band, epoch2);
            hdu = (ImageHDU) fits.getHDU(0);
            imageData = (ImageData) hdu.getData();
            float[][] values2 = (float[][]) imageData.getData();

            List<Integer> xPixels = new ArrayList<>();
            List<Integer> yPixels = new ArrayList<>();
            List<NumberTriplet> pixels = new ArrayList<>();
            for (int i = 0; i < axisY; i += 10) {
                for (int j = 0; j < axisX; j += 10) {
                    for (int k = max(0, i - 5); k <= min(i + 5, axisY); k++) {
                        for (int u = max(0, j - 5); u <= min(j + 5, axisX); u++) {
                            try {
                                float value1 = processPixel(values1[k][u]);
                                float value2 = processPixel(values2[k][u]);
                                float max = max(value1, value2);
                                float min = min(value1, value2);
                                boolean isSelectable = false;
                                if (blinkMarkers.isSelected()) {
                                    if (max - min > min && value1 == max) {
                                        isSelectable = true;
                                    }
                                } else {
                                    if (max - min > min) {
                                        isSelectable = true;
                                    }
                                }
                                if (isSelectable) {
                                    if (showCirclesButton.isSelected()) {
                                        xPixels.add(u);
                                        yPixels.add(k);
                                    } else {
                                        pixels.add(new NumberTriplet(u, k, 1));
                                    }
                                }
                            } catch (ArrayIndexOutOfBoundsException ex) {
                            }
                        }
                    }
                    if (showCirclesButton.isSelected()) {
                        if (xPixels.size() > sensitivity) {
                            IntSummaryStatistics xStats = xPixels.stream().mapToInt((x) -> x).summaryStatistics();
                            IntSummaryStatistics yStats = yPixels.stream().mapToInt((y) -> y).summaryStatistics();

                            double xCenter = xStats.getAverage();
                            double yCenter = yStats.getAverage();

                            //int xDiff = xStats.getMax() - xStats.getMin();
                            //int yDiff = yStats.getMax() - yStats.getMin();
                            //int diameter = max(xDiff, yDiff);
                            int diameter = 10;

                            diffPixels.add(new NumberTriplet(xCenter, yCenter, diameter));
                        }
                        xPixels.clear();
                        yPixels.clear();
                    } else {
                        if (pixels.size() > sensitivity) {
                            diffPixels.addAll(pixels);
                        }
                        pixels.clear();
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private BufferedImage createImage(int band, int epoch) {
        try {
            Fits fits = getImage(band, epoch);
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values = (float[][]) imageData.getData();

            if (smoothImage.isSelected()) {
                values = smooth(values);
            }

            BufferedImage image = new BufferedImage(axisX, axisY, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            for (int i = 0; i < axisY; i++) {
                for (int j = 0; j < axisX; j++) {
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

            if (smoothImage.isSelected()) {
                valuesW1 = smooth(valuesW1);
                valuesW2 = smooth(valuesW2);
            }

            BufferedImage image = new BufferedImage(axisX, axisY, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            for (int i = 0; i < axisY; i++) {
                for (int j = 0; j < axisX; j++) {
                    try {
                        float red = processPixel(valuesW1[i][j]);
                        float blue = processPixel(valuesW2[i][j]);
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

            float[][] addedValues = new float[axisY][axisX];
            for (int i = 0; i < axisY; i++) {
                for (int j = 0; j < axisX; j++) {
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

            float[][] subtractedValues = new float[axisY][axisX];
            for (int i = 0; i < axisY; i++) {
                for (int j = 0; j < axisX; j++) {
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

            float[][] averagedValues = new float[axisY][axisX];
            for (int i = 0; i < axisY; i++) {
                for (int j = 0; j < axisX; j++) {
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

    public float[][] smooth(float[][] values) {
        float[][] smoothedValues = new float[axisY][axisX];
        for (int i = 0; i < axisY; ++i) {
            for (int j = 0; j < axisX; ++j) {
                int sum = 0, c = 0;
                for (int k = max(0, i - 1); k <= min(i + 1, axisY - 1); k++) {
                    for (int u = max(0, j - 1); u <= min(j + 1, axisX - 1); u++) {
                        sum += values[k][u];
                        c++;
                    }
                }
                smoothedValues[i][j] = sum / c;
            }
        }
        return smoothedValues;
    }

    private BufferedImage flip(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    private BufferedImage rotate(BufferedImage image, int numberOfQuadrants) {
        AffineTransform tx = AffineTransform.getQuadrantRotateInstance(numberOfQuadrants, image.getWidth() / 2, image.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

    private BufferedImage zoom(BufferedImage image, int zoom) {
        zoom = zoom == 0 ? 1 : zoom;
        Image scaled = image.getScaledInstance((axisX > axisY ? 1 : -1) * zoom, (axisX > axisY ? -1 : 1) * zoom, Image.SCALE_DEFAULT);
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

    private float processPixel(float value) {
        value *= rawContrast;
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
        float a = stretch / 100f;
        return asinh(value / a) / asinh(1 / a);
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

    private void setMaxSensitivity() {
        maxSensitivity = (lowContrast + highContrast) / 10 * 2;
        sensitivityLabel.setText(String.format("Sensitivity: %d/%d", maxSensitivity + 1 - sensitivity, maxSensitivity));
        sensitivitySlider.setMaximum(maxSensitivity);
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

    private void setMinMaxValues(int minVal, int maxVal, int avgVal) {
        //System.out.println("minVal=" + minVal + " maxVal=" + maxVal + " avgVal=" + avgVal);

        if (applyLimits.isSelected()) {
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
                minVal = -maxVal / 20;
            }
        }

        // Set minimum slider values
        minValueSlider.setMinimum(minVal);
        minValueSlider.setMaximum(maxVal);
        minValueSlider.setValue(minVal);

        // Set maximum slider values
        maxValueSlider.setMinimum(minVal);
        maxValueSlider.setMaximum(maxVal);
        maxValueSlider.setValue(maxVal);

        // Set minimum & maximum values
        minValue = minVal;
        maxValue = maxVal;
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
        imageViewerTab.getWiseBands().setSelectedItem(wiseBand);
        imageViewerTab.getZoomSlider().setValue(ZOOM);
        imageViewerTab.setZoom(ZOOM);
        imageViewerTab.setQuadrantCount(quadrantCount);
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
            imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?red=%s&green=%s&blue=%s&ra=%f&dec=%f&size=%d&output_size=%d", fileNames.get(2), fileNames.get(1), fileNames.get(0), targetRa, targetDec, (int) round(size * SIZE_FACTOR * 4), 1024);
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
            return ImageIO.read(stream);
        } catch (Exception ex) {
            return null;
        }
    }

    private BufferedImage fetchSdssImage(double targetRa, double targetDec, double size) {
        try {
            int resolution = 1000;
            String imageUrl = String.format(SDSS_BASE_URL + "/SkyserverWS/ImgCutout/getjpeg?ra=%f&dec=%f&width=%d&height=%d&scale=%f", targetRa, targetDec, resolution, resolution, size * SIZE_FACTOR / resolution);
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
            return ImageIO.read(stream);
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
            imageFrame.getContentPane().add(bandPanel);
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
            imageFrame.getContentPane().add(bandPanel);
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
            imageFrame.getContentPane().add(bandPanel);
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
            imageFrame.getContentPane().add(bandPanel);
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
            imageFrame.getContentPane().add(bandPanel);
            imageFrame.setSize(1320, PANEL_HEIGHT);
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

    private JPanel buildImagePanel(BufferedImage image, String imageHeader) {
        JPanel panel = new JPanel();
        panel.setBorder(createEtchedBorder(imageHeader));
        image = zoom(image, 200);
        double x = image.getWidth() / 2;
        double y = image.getHeight() / 2;
        Graphics g = image.getGraphics();
        Circle circle = new Circle(x, y, 10, Color.MAGENTA);
        circle.draw(g);
        panel.add(new JLabel(new ImageIcon(image)));
        return panel;
    }

    private List<CatalogEntry> fetchCatalogEntries(CatalogEntry catalogQuery) {
        try {
            baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            catalogQuery.setRa(targetRa);
            catalogQuery.setDec(targetDec);
            catalogQuery.setSearchRadius(getFovDiagonal() / 2);
            List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
            catalogEntries.forEach(catalogEntry -> {
                catalogEntry.setTargetRa(targetRa);
                catalogEntry.setTargetDec(targetDec);
                catalogEntry.loadCatalogElements();
            });
            return catalogEntries;
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
            List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoordsAndTpm(catalogQuery);
            catalogEntries.forEach(catalogEntry -> {
                catalogEntry.setTargetRa(targetRa);
                catalogEntry.setTargetDec(targetDec);
                catalogEntry.loadCatalogElements();
            });
            return catalogEntries;
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
        try (Scanner scanner = new Scanner(customOverlay.getFile())) {
            String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
            StringBuilder errors = new StringBuilder();
            int numberOfColumns = columnNames.length;
            int lastColumnIndex = numberOfColumns - 1;
            int raColumnIndex = customOverlay.getRaColumnIndex();
            int decColumnIndex = customOverlay.getDecColumnIndex();
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
                double radius = convertToUnit(getFovDiagonal() / 2, Unit.ARCSEC, Unit.DEGREE);
                if (catalogEntry.getRa() > targetRa - radius && catalogEntry.getRa() < targetRa + radius
                        && catalogEntry.getDec() > targetDec - radius && catalogEntry.getDec() < targetDec + radius) {
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

    private void drawSectrumOverlay(BufferedImage image, List<CatalogEntry> catalogEntries) {
        Graphics graphics = image.getGraphics();
        catalogEntries.forEach(catalogEntry -> {
            NumberPair position = getPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
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
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
            BufferedImage spectrum = ImageIO.read(stream);
            if (spectrum != null) {
                JFrame imageFrame = new JFrame();
                imageFrame.setIconImage(getToolBoxImage());
                imageFrame.setTitle("SDSS spectrum for object: " + roundTo2DecNZ(catalogEntry.getRa()) + " " + roundTo2DecNZ(catalogEntry.getDec()));
                imageFrame.getContentPane().add(new JLabel(new ImageIcon(spectrum)));
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
            NumberPair position = getPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
            catalogEntry.setPixelRa(position.getX());
            catalogEntry.setPixelDec(position.getY());
            Drawable toDraw;
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
            toDraw.draw(graphics);
        });
    }

    private void drawArtifactOverlay(BufferedImage image, List<CatalogEntry> catalogEntries) {
        Graphics graphics = image.getGraphics();
        catalogEntries.forEach(catalogEntry -> {
            NumberPair position = getPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
            catalogEntry.setPixelRa(position.getX());
            catalogEntry.setPixelDec(position.getY());
            String ab_flags = "";
            String cc_flags = "";
            if (catalogEntry instanceof CatWiseCatalogEntry) {
                CatWiseCatalogEntry catWiseCatalog = (CatWiseCatalogEntry) catalogEntry;
                ab_flags = catWiseCatalog.getAb_flags();
                cc_flags = catWiseCatalog.getCc_flags();
            }
            if (catalogEntry instanceof CatWiseRejectedEntry) {
                CatWiseRejectedEntry catWiseRejected = (CatWiseRejectedEntry) catalogEntry;
                ab_flags = catWiseRejected.getAb_flags();
                cc_flags = catWiseRejected.getCc_flags();
            }
            if (cc_flags.length() > 1) {
                cc_flags = cc_flags.substring(0, 2);
            } else if (cc_flags.length() > 0) {
                cc_flags = cc_flags.substring(0, 1);
            }
            ab_flags = ab_flags.toUpperCase();
            cc_flags = cc_flags.toUpperCase();
            if (ghostOverlay.isSelected() && (ab_flags.contains("O") || cc_flags.contains("O"))) {
                Drawable toDraw = new Diamond(position.getX(), position.getY(), getOverlaySize(), Color.MAGENTA.darker());
                toDraw.draw(graphics);
            }
            if (haloOverlay.isSelected() && (ab_flags.contains("H") || cc_flags.contains("H"))) {
                Drawable toDraw = new Square(position.getX(), position.getY(), getOverlaySize(), Color.YELLOW);
                toDraw.draw(graphics);
            }
            if (latentOverlay.isSelected() && (ab_flags.contains("P") || cc_flags.contains("P"))) {
                Drawable toDraw = new XCross(position.getX(), position.getY(), getOverlaySize(), Color.GREEN.darker(), 2);
                toDraw.draw(graphics);
            }
            if (spikeOverlay.isSelected() && (ab_flags.contains("D") || cc_flags.contains("D"))) {
                Drawable toDraw = new Circle(position.getX(), position.getY(), getOverlaySize(), Color.ORANGE);
                toDraw.draw(graphics);
            }
        });
    }

    private void drawPMVectors(BufferedImage image, List<CatalogEntry> catalogEntries, Color color) {
        Graphics graphics = image.getGraphics();
        catalogEntries.forEach(catalogEntry -> {
            NumberPair position = getPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
            catalogEntry.setPixelRa(position.getX());
            catalogEntry.setPixelDec(position.getY());

            double pmRa = catalogEntry.getPmra();
            double pmDec = catalogEntry.getPmdec();

            double ra = 0;
            double dec = 0;
            int numberOfYears = 0;
            if (catalogEntry instanceof GaiaCatalogEntry) {
                ra = catalogEntry.getRa();
                dec = catalogEntry.getDec();
                numberOfYears = 5;
            }
            if (catalogEntry instanceof CatWiseCatalogEntry) {
                ra = ((CatWiseCatalogEntry) catalogEntry).getRa_pm();
                dec = ((CatWiseCatalogEntry) catalogEntry).getDec_pm();
                numberOfYears = 4;
            }

            ra = ra - (numberOfYears * pmRa / DEG_MAS) / cos(toRadians(dec));
            dec = dec - numberOfYears * pmDec / DEG_MAS;

            NumberPair pixelCoords = getPixelCoordinates(ra, dec);
            double x = pixelCoords.getX();
            double y = pixelCoords.getY();

            numberOfYears = (selectedEpochs) + 3; // 3 -> 2011, 2012 & 2013
            double newRa = ra + (numberOfYears * pmRa / DEG_MAS) / cos(toRadians(dec));
            double newDec = dec + numberOfYears * pmDec / DEG_MAS;

            pixelCoords = getPixelCoordinates(newRa, newDec);
            double newX = pixelCoords.getX();
            double newY = pixelCoords.getY();

            Arrow arrow = new Arrow(x, y, newX, newY, getOverlaySize(), color, 2);
            arrow.draw(graphics);
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
        int maxRows = simpleLayout ? 30 : 19;
        JPanel detailPanel = new JPanel(new GridLayout(maxRows, 4));
        detailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), catalogEntry.getCatalogName() + " entry (Computed values are shown in green; (*) Further info: mouse pointer)", TitledBorder.LEFT, TitledBorder.TOP
        ));

        List<CatalogElement> catalogElements = catalogEntry.getCatalogElements();
        catalogElements.forEach(element -> {
            addLabelToPanel(element, detailPanel);
            addFieldToPanel(element, detailPanel);
        });

        int elements = catalogElements.size();
        int rows = elements / 2;
        int remainder = elements % 2;
        rows += remainder;
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
        catalogFrame.add(container);
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

    private double getFovDiagonal() {
        return size * SIZE_FACTOR * sqrt(2);
    }

    private double getOverlaySize() {
        return 5 + zoom / 100;
    }

    public JCheckBox getSmoothImage() {
        return smoothImage;
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

    public void setPanstarrsImages(boolean panstarrsImages) {
        this.panstarrsImages = panstarrsImages;
    }

    public void setSdssImages(boolean sdssImages) {
        this.sdssImages = sdssImages;
    }

}
