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
import astro.tool.box.container.ColorValue;
import astro.tool.box.container.CustomOverlay;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.NumberTriplet;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseRejectedEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaDR2CatalogEntry;
import astro.tool.box.container.catalog.GenericCatalogEntry;
import astro.tool.box.container.catalog.PanStarrsCatalogEntry;
import astro.tool.box.container.catalog.ProperMotionQuery;
import astro.tool.box.container.catalog.SDSSCatalogEntry;
import astro.tool.box.container.catalog.SSOCatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.container.lookup.BrownDwarfLookupEntry;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
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
import astro.tool.box.service.SpectralTypeLookupService;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Math.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;

public class ImageViewerTab {

    public static final String TAB_NAME = "Image Viewer";
    public static final WiseBand WISE_BAND = WiseBand.W2;
    public static final Epoch EPOCH = Epoch.FIRST_LAST;
    public static final double OVERLAP_FACTOR = 0.9;
    public static final double SIZE_FACTOR = 2.75;
    public static final int NUMBER_OF_EPOCHS = 6;
    public static final int WINDOW_SPACING = 25;
    public static final int MIN_VALUE = -2500;
    public static final int MAX_VALUE = 2500;
    public static final int STRETCH = 100;
    public static final int SPEED = 300;
    public static final int ZOOM = 500;
    public static final int SIZE = 500;
    public static final String CHANGE_FOV_TEXT = "(Spin wheel to change FoV: %d\")";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CustomOverlaysTab customOverlaysTab;

    private final CatalogQueryFacade catalogQueryFacade;
    private final SpectralTypeLookupService mainSequenceSpectralTypeLookupService;
    private final SpectralTypeLookupService brownDwarfsSpectralTypeLookupService;
    private List<CatalogEntry> simbadEntries;
    private List<CatalogEntry> gaiaDR2Entries;
    private List<CatalogEntry> gaiaDR2TpmEntries;
    private List<CatalogEntry> allWiseEntries;
    private List<CatalogEntry> catWiseEntries;
    private List<CatalogEntry> catWiseTpmEntries;
    private List<CatalogEntry> catWiseRejectedEntries;
    private List<CatalogEntry> panStarrsEntries;
    private List<CatalogEntry> sdssEntries;
    private List<CatalogEntry> ssoEntries;

    private JPanel imagePanel;
    private JPanel zooniversePanel1;
    private JPanel zooniversePanel2;
    private JCheckBox minMaxLimits;
    private JCheckBox stretchImage;
    private JCheckBox smoothImage;
    private JCheckBox keepContrast;
    private JCheckBox invertColors;
    private JCheckBox borderEpoch;
    private JCheckBox staticDisplay;
    private JCheckBox simbadOverlay;
    private JCheckBox gaiaDR2Overlay;
    private JCheckBox allWiseOverlay;
    private JCheckBox catWiseOverlay;
    private JCheckBox panStarrsOverlay;
    private JCheckBox sdssOverlay;
    private JCheckBox spectrumOverlay;
    private JCheckBox ssoOverlay;
    private JCheckBox ghostOverlay;
    private JCheckBox haloOverlay;
    private JCheckBox latentOverlay;
    private JCheckBox spikeOverlay;
    private JCheckBox gaiaDR2ProperMotion;
    private JCheckBox catWiseProperMotion;
    private JCheckBox transposeProperMotion;
    private JCheckBox useCustomOverlays;
    private JCheckBox skipFirstEpoch;
    private JCheckBox skipBadCoadds;
    //private JCheckBox smallBodyHelp;
    private JCheckBox hideMagnifier;
    private JCheckBox drawCrosshairs;
    private JCheckBox markDifferences;
    private JComboBox wiseBands;
    private JComboBox epochs;
    private JSlider highScaleSlider;
    private JSlider lowScaleSlider;
    private JSlider stretchSlider;
    private JSlider minValueSlider;
    private JSlider maxValueSlider;
    private JSlider speedSlider;
    private JSlider zoomSlider;
    private JSlider epochCountSlider;
    private JTextField coordsField;
    private JTextField sizeField;
    private JTextField transposeMotionField;
    private JTextField properMotionField;
    private JTextArea crosshairCoords;
    private JRadioButton showCatalogsButton;
    private JRadioButton showPanstarrsButton;
    private JRadioButton showAllwiseButton;
    private JRadioButton show2MassButton;
    private JLabel changeFovLabel;
    private JTable collectionTable;
    private Timer timer;

    private BufferedImage wiseImage;
    private BufferedImage ps1Image;
    private BufferedImage sdssImage;
    private Map<String, Fits> images;
    private Map<String, CustomOverlay> customOverlays;
    private List<NumberPair> crosshairs;
    private FlipbookComponent[] flipbook;
    private ImageViewerTab imageViewer;

    private WiseBand wiseBand = WISE_BAND;
    private Epoch epoch = EPOCH;
    private int fieldOfView = 15;
    private int crosshairSize = 5;
    private int imageNumber = 0;
    private int windowShift = 0;
    private int quadrantCount = 0;
    private int epochCount = NUMBER_OF_EPOCHS * 2;
    private int stretch = STRETCH;
    private int speed = SPEED;
    private int zoom = ZOOM;
    private int size = SIZE;

    private int lowContrast = getContrast();
    private int highContrast;
    private int lowContrastSaved = getContrast();
    private int highContrastSaved;

    private int minValue;
    private int maxValue;
    private int avgValue;

    private double targetRa;
    private double targetDec;

    private double pixelX;
    private double pixelY;

    //private double shiftX;
    //private double shiftY;
    private int centerX;
    private int centerY;

    private int axisX;
    private int axisY;

    private int previousSize;
    private double previousRa;
    private double previousDec;

    private boolean imageCutOff;
    private boolean disableOverlays;
    private boolean hasException;
    private boolean timerStopped;

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
                return new BrownDwarfLookupEntry(line.split(SPLIT_CHAR, 21));
            }).collect(Collectors.toList());
            brownDwarfsSpectralTypeLookupService = new SpectralTypeLookupService(entries);
        }
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new BorderLayout());

            JPanel leftPanel = new JPanel();
            mainPanel.add(leftPanel, BorderLayout.LINE_START);
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBorder(new EmptyBorder(0, 5, 5, 20));

            imagePanel = new JPanel();
            imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));

            JScrollPane imageScrollPanel = new JScrollPane(imagePanel);
            mainPanel.add(imageScrollPanel, BorderLayout.CENTER);
            imageScrollPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JPanel rightPanel = new JPanel();
            mainPanel.add(rightPanel, BorderLayout.LINE_END);
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBorder(new EmptyBorder(20, 0, 5, 5));

            int controlPanelWidth = 250;
            int controlPanelHeight = 1775;

            JPanel controlPanel = new JPanel(new GridLayout(73, 1));
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
                wiseBands.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                wiseBand = (WiseBand) wiseBands.getSelectedItem();
                initMinMaxValues();
                createFlipbook();
                wiseBands.setCursor(Cursor.getDefaultCursor());
            });

            controlPanel.add(new JLabel("Epochs:"));

            epochs = new JComboBox<>(Epoch.values());
            controlPanel.add(epochs);
            epochs.setMaximumRowCount(Epoch.values().length);
            epochs.setSelectedItem(epoch);
            epochs.addActionListener((ActionEvent evt) -> {
                epochs.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Epoch previousEpoch = epoch;
                epoch = (Epoch) epochs.getSelectedItem();
                //if (epochs.getSelectedItem().equals(Epoch.ALL)) {
                //    smallBodyHelp.setEnabled(true);
                //} else {
                //    smallBodyHelp.setSelected(false);
                //    smallBodyHelp.setEnabled(false);
                //}
                initMinMaxValues();
                if (Epoch.isSubtracted(epoch)) {
                    smoothImage.setSelected(true);
                } else if (Epoch.isSubtracted(previousEpoch)) {
                    smoothImage.setSelected(false);
                }
                if (Epoch.isSubtracted(epoch)) {
                    setSubtractedContrast();
                } else {
                    setContrast(lowContrastSaved, highContrastSaved);
                }
                createFlipbook();
                epochs.setCursor(Cursor.getDefaultCursor());
            });

            JPanel whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel highScaleLabel = new JLabel(String.format("Contrast high scale: %d", highContrast));
            whitePanel.add(highScaleLabel);

            highScaleSlider = new JSlider(0, 1000, highContrast);
            controlPanel.add(highScaleSlider);
            highScaleSlider.setBackground(Color.WHITE);
            highScaleSlider.addChangeListener((ChangeEvent e) -> {
                highContrast = highScaleSlider.getValue();
                highScaleLabel.setText(String.format("Contrast high scale: %d", highContrast));
                if (markDifferences.isSelected() && flipbook != null) {
                    detectDifferences();
                }
                if (Epoch.isSubtracted(epoch)) {
                    initMinMaxValues();
                } else {
                    highContrastSaved = highContrast;
                }
            });

            JPanel grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            JLabel lowScaleLabel = new JLabel(String.format("Contrast low scale: %d", lowContrast));
            grayPanel.add(lowScaleLabel);

            lowScaleSlider = new JSlider(0, 100, lowContrast);
            controlPanel.add(lowScaleSlider);
            lowScaleSlider.setBackground(Color.LIGHT_GRAY);
            lowScaleSlider.addChangeListener((ChangeEvent e) -> {
                lowContrast = lowScaleSlider.getValue();
                lowScaleLabel.setText(String.format("Contrast low scale: %d", lowContrast));
                if (markDifferences.isSelected() && flipbook != null) {
                    detectDifferences();
                }
                if (Epoch.isSubtracted(epoch)) {
                    initMinMaxValues();
                } else {
                    lowContrastSaved = lowContrast;
                }
            });

            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel minValueLabel = new JLabel(String.format("Min pixel value: %d", minValue));
            whitePanel.add(minValueLabel);

            minValueSlider = new JSlider();
            controlPanel.add(minValueSlider);
            minValueSlider.setBackground(Color.WHITE);
            minValueSlider.addChangeListener((ChangeEvent e) -> {
                minValue = minValueSlider.getValue();
                minValueLabel.setText(String.format("Min pixel value: %d", minValue));
            });

            grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            JLabel maxValueLabel = new JLabel(String.format("Max pixel value: %d", maxValue));
            grayPanel.add(maxValueLabel);

            maxValueSlider = new JSlider();
            controlPanel.add(maxValueSlider);
            maxValueSlider.setBackground(Color.LIGHT_GRAY);
            maxValueSlider.addChangeListener((ChangeEvent e) -> {
                maxValue = maxValueSlider.getValue();
                maxValueLabel.setText(String.format("Max pixel value: %d", maxValue));
            });

            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel stretchLabel = new JLabel(String.format("Stretch control: %s", roundTo2Dec(stretch / 100f)));
            whitePanel.add(stretchLabel);

            stretchSlider = new JSlider(0, 100, stretch);
            controlPanel.add(stretchSlider);
            stretchSlider.setBackground(Color.WHITE);
            stretchSlider.addChangeListener((ChangeEvent e) -> {
                stretch = stretchSlider.getValue();
                stretchLabel.setText(String.format("Stretch control: %s", roundTo2Dec(stretch / 100f)));
            });

            grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            JLabel speedLabel = new JLabel(String.format("Speed: %d ms", speed));
            grayPanel.add(speedLabel);

            speedSlider = new JSlider(0, 2000, speed);
            controlPanel.add(speedSlider);
            speedSlider.setBackground(Color.LIGHT_GRAY);
            speedSlider.addChangeListener((ChangeEvent e) -> {
                speed = speedSlider.getValue();
                timer.setDelay(speed);
                speedLabel.setText(String.format("Speed: %d ms", speed));
            });

            whitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(whitePanel);
            whitePanel.setBackground(Color.WHITE);

            JLabel zoomLabel = new JLabel(String.format("Zoom: %d", zoom));
            whitePanel.add(zoomLabel);

            zoomSlider = new JSlider(0, 2000, zoom);
            controlPanel.add(zoomSlider);
            zoomSlider.setBackground(Color.WHITE);
            zoomSlider.addChangeListener((ChangeEvent e) -> {
                zoom = zoomSlider.getValue();
                zoom = zoom < 100 ? 100 : zoom;
                zoomLabel.setText(String.format("Zoom: %d", zoom));
            });

            grayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(grayPanel);
            grayPanel.setBackground(Color.LIGHT_GRAY);

            JLabel epochCountLabel = new JLabel(String.format("Number of epochs: %d", epochCount / 2));
            grayPanel.add(epochCountLabel);

            epochCountSlider = new JSlider(2, NUMBER_OF_EPOCHS, NUMBER_OF_EPOCHS);
            controlPanel.add(epochCountSlider);
            epochCountSlider.setBackground(Color.LIGHT_GRAY);
            epochCountSlider.addChangeListener((ChangeEvent e) -> {
                epochCount = epochCountSlider.getValue() * 2;
                epochCountLabel.setText(String.format("Number of epochs: %d", epochCount / 2));
                //initMinMaxValues();
                createFlipbook();
            });

            minMaxLimits = new JCheckBox("Set min/max limits", true);
            controlPanel.add(minMaxLimits);
            minMaxLimits.addActionListener((ActionEvent evt) -> {
                initMinMaxValues();
            });

            stretchImage = new JCheckBox("Stretch images", true);
            controlPanel.add(stretchImage);

            smoothImage = new JCheckBox("Smooth images");
            controlPanel.add(smoothImage);

            keepContrast = new JCheckBox("Keep contrast settings");
            controlPanel.add(keepContrast);

            invertColors = new JCheckBox("Invert colors");
            controlPanel.add(invertColors);

            borderEpoch = new JCheckBox("Border first epoch");
            controlPanel.add(borderEpoch);

            staticDisplay = new JCheckBox("Static display");
            controlPanel.add(staticDisplay);
            staticDisplay.addActionListener((ActionEvent evt) -> {
                if (staticDisplay.isSelected() && flipbook != null) {
                    createStaticBook();
                } else {
                    createFlipbook();
                }
            });

            JButton resetDefaultsButton = new JButton("Image processing defaults");
            controlPanel.add(resetDefaultsButton);
            resetDefaultsButton.addActionListener((ActionEvent evt) -> {
                //minMaxLimits.setSelected(true);
                stretchImage.setSelected(true);
                stretchSlider.setValue(stretch = STRETCH);
                if (Epoch.isSubtracted(epoch)) {
                    setSubtractedContrast();
                } else {
                    setContrast(getContrast(), 0);
                }
                initMinMaxValues();
                createFlipbook();
            });

            controlPanel.add(new JLabel(underline("Overlays:")));

            JPanel overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            simbadOverlay = new JCheckBox("SIMBAD");
            simbadOverlay.setForeground(Color.RED);
            overlayPanel.add(simbadOverlay);
            gaiaDR2Overlay = new JCheckBox("Gaia DR2");
            gaiaDR2Overlay.setForeground(Color.CYAN.darker());
            overlayPanel.add(gaiaDR2Overlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            allWiseOverlay = new JCheckBox("AllWISE");
            allWiseOverlay.setForeground(Color.GREEN.darker());
            overlayPanel.add(allWiseOverlay);
            catWiseOverlay = new JCheckBox("CatWISE");
            catWiseOverlay.setForeground(Color.MAGENTA);
            overlayPanel.add(catWiseOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            panStarrsOverlay = new JCheckBox("Pan-STARRS");
            panStarrsOverlay.setForeground(JColor.BROWN.val);
            overlayPanel.add(panStarrsOverlay);
            sdssOverlay = new JCheckBox("SDSS DR16");
            sdssOverlay.setForeground(JColor.STEEL.val);
            overlayPanel.add(sdssOverlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            spectrumOverlay = new JCheckBox("SDSS spectra");
            spectrumOverlay.setForeground(JColor.OLIVE.val);
            overlayPanel.add(spectrumOverlay);
            ssoOverlay = new JCheckBox("Solar Sys. Obj.");
            ssoOverlay.setForeground(Color.BLUE);
            overlayPanel.add(ssoOverlay);

            controlPanel.add(new JLabel(underline("PM vectors:")));

            JPanel properMotionPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(properMotionPanel);
            gaiaDR2ProperMotion = new JCheckBox("Gaia DR2");
            gaiaDR2ProperMotion.setForeground(Color.CYAN.darker());
            properMotionPanel.add(gaiaDR2ProperMotion);
            catWiseProperMotion = new JCheckBox("CatWISE");
            catWiseProperMotion.setForeground(Color.MAGENTA);
            properMotionPanel.add(catWiseProperMotion);

            properMotionPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(properMotionPanel);
            properMotionPanel.add(new JLabel("Total PM (mas/yr) >"));
            properMotionField = new JTextField(String.valueOf(100));
            properMotionPanel.add(properMotionField);
            properMotionField.addActionListener((ActionEvent evt) -> {
                gaiaDR2TpmEntries = null;
                catWiseTpmEntries = null;
            });
            /*
            properMotionField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    gaiaDR2TpmEntries = null;
                    catWiseTpmEntries = null;
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    gaiaDR2TpmEntries = null;
                    catWiseTpmEntries = null;
                }
            });*/

            controlPanel.add(new JLabel(underline("Sources affected by WISE artifacts:")));

            JPanel artifactPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(artifactPanel);
            ghostOverlay = new JCheckBox("Ghosts");
            ghostOverlay.setForeground(Color.MAGENTA.darker());
            artifactPanel.add(ghostOverlay);
            haloOverlay = new JCheckBox("<html><span style='background:black'>&nbsp;Halos&nbsp;</span></html>");
            haloOverlay.setForeground(Color.YELLOW);
            artifactPanel.add(haloOverlay);

            artifactPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(artifactPanel);
            latentOverlay = new JCheckBox("Latents");
            latentOverlay.setForeground(Color.GREEN.darker());
            artifactPanel.add(latentOverlay);
            spikeOverlay = new JCheckBox("<html><span style='background:black'>&nbsp;Spikes&nbsp;</span></html>");
            spikeOverlay.setForeground(Color.ORANGE);
            artifactPanel.add(spikeOverlay);

            controlPanel.add(new JLabel(underline("Mouse left click w/o overlays:")));

            showCatalogsButton = new JRadioButton("Show catalogs", true);
            controlPanel.add(showCatalogsButton);

            JRadioButton recenterImagesButton = new JRadioButton("Recenter images", false);
            controlPanel.add(recenterImagesButton);

            ButtonGroup radioGroup = new ButtonGroup();
            radioGroup.add(showCatalogsButton);
            radioGroup.add(recenterImagesButton);

            controlPanel.add(new JLabel(underline("Mouse wheel click:")));

            changeFovLabel = new JLabel(String.format(CHANGE_FOV_TEXT, fieldOfView));
            controlPanel.add(changeFovLabel);

            showPanstarrsButton = new JRadioButton("Zoomed Pan-STARRS image", true);
            controlPanel.add(showPanstarrsButton);
            showPanstarrsButton.addActionListener((ActionEvent evt) -> {
                fieldOfView = 15;
                changeFovLabel.setText(String.format(CHANGE_FOV_TEXT, fieldOfView));
            });

            showAllwiseButton = new JRadioButton("AllWISE W1, W2, W3, W4 images", false);
            controlPanel.add(showAllwiseButton);
            showAllwiseButton.addActionListener((ActionEvent evt) -> {
                fieldOfView = 30;
                changeFovLabel.setText(String.format(CHANGE_FOV_TEXT, fieldOfView));
            });

            show2MassButton = new JRadioButton("2MASS J, H, K images", false);
            controlPanel.add(show2MassButton);
            show2MassButton.addActionListener((ActionEvent evt) -> {
                fieldOfView = 30;
                changeFovLabel.setText(String.format(CHANGE_FOV_TEXT, fieldOfView));
            });

            radioGroup = new ButtonGroup();
            radioGroup.add(showPanstarrsButton);
            radioGroup.add(showAllwiseButton);
            radioGroup.add(show2MassButton);

            controlPanel.add(new JLabel(underline("Nearest Zooniverse Subjects:")));

            zooniversePanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(zooniversePanel1);

            zooniversePanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlPanel.add(zooniversePanel2);

            controlPanel.add(new JLabel(underline("Advanced controls:")));

            skipFirstEpoch = new JCheckBox("Skip first epoch (year 2010)");
            controlPanel.add(skipFirstEpoch);
            skipFirstEpoch.addActionListener((ActionEvent evt) -> {
                if (skipFirstEpoch.isSelected()) {
                    epochCount -= 2;
                } else {
                    epochCount += 2;
                }
                if (images != null) {
                    skipFirstEpoch.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    images.clear();
                    initMinMaxValues();
                    createFlipbook();
                    skipFirstEpoch.setCursor(Cursor.getDefaultCursor());
                }
            });

            skipBadCoadds = new JCheckBox("Skip low weighted coadds");
            controlPanel.add(skipBadCoadds);
            skipBadCoadds.addActionListener((ActionEvent evt) -> {
                if (images != null) {
                    skipBadCoadds.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    images.clear();
                    initMinMaxValues();
                    createFlipbook();
                    skipBadCoadds.setCursor(Cursor.getDefaultCursor());
                }
            });

            //smallBodyHelp = new JCheckBox("Small body help (Epochs: ALL)");
            //controlPanel.add(smallBodyHelp);
            //smallBodyHelp.setEnabled(false);
            hideMagnifier = new JCheckBox("Hide magnifier panel");
            controlPanel.add(hideMagnifier);
            hideMagnifier.addActionListener((ActionEvent evt) -> {
                if (hideMagnifier.isSelected()) {
                    rightPanel.setVisible(false);
                } else {
                    rightPanel.setVisible(true);
                }
            });

            markDifferences = new JCheckBox("Mark differences");
            controlPanel.add(markDifferences);
            markDifferences.addActionListener((ActionEvent evt) -> {
                if (markDifferences.isSelected() && flipbook != null) {
                    detectDifferences();
                }
            });

            drawCrosshairs = new JCheckBox("Draw crosshairs (wheel click & spin)");
            controlPanel.add(drawCrosshairs);
            drawCrosshairs.addActionListener((ActionEvent evt) -> {
                if (!drawCrosshairs.isSelected()) {
                    crosshairs.clear();
                    crosshairCoords.setText("");
                }
            });

            controlPanel.add(new JLabel("Crosshairs coordinates:"));

            crosshairCoords = new JTextArea();
            controlPanel.add(new JScrollPane(crosshairCoords));
            Font font = coordsField.getFont();
            crosshairCoords.setFont(font.deriveFont(font.getSize() - 2.0f));
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

            //
            /*controlPanel.add(new JLabel(underLine("Image alignment controls:")));

            int delay = 100;

            JPanel alignmentControls = new JPanel(new GridLayout(1, 3));
            controlPanel.add(alignmentControls);

            JButton shiftLeft = new JButton(html("Shift &larr;"));
            alignmentControls.add(shiftLeft);

            JButton shiftRight = new JButton(html("Shift &rarr;"));
            alignmentControls.add(shiftRight);

            JTextField shiftXValue = new JTextField(roundTo1Dec(shiftX));
            alignmentControls.add(shiftXValue);
            shiftXValue.setHorizontalAlignment(JTextField.CENTER);
            shiftXValue.setEditable(false);

            shiftLeft.addMouseListener(new MouseAdapter() {
                Timer timer = new Timer(delay, (ActionEvent e) -> {
                    shiftX -= 0.1;
                    shiftXValue.setText(roundTo1Dec(shiftX));
                });

                @Override
                public void mousePressed(MouseEvent e) {
                    timer.start();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    timer.stop();
                }
            });

            shiftRight.addMouseListener(new MouseAdapter() {
                Timer timer = new Timer(delay, (ActionEvent e) -> {
                    shiftX += 0.1;
                    shiftXValue.setText(roundTo1Dec(shiftX));
                });

                @Override
                public void mousePressed(MouseEvent e) {
                    timer.start();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    timer.stop();
                }
            });

            alignmentControls = new JPanel(new GridLayout(1, 2));
            controlPanel.add(alignmentControls);

            JButton shiftUp = new JButton(html("Shift &uarr;"));
            alignmentControls.add(shiftUp);

            JButton shiftDown = new JButton(html("Shift &darr;"));
            alignmentControls.add(shiftDown);

            JTextField shiftYValue = new JTextField(roundTo1Dec(shiftY));
            alignmentControls.add(shiftYValue);
            shiftYValue.setHorizontalAlignment(JTextField.CENTER);
            shiftYValue.setEditable(false);

            shiftUp.addMouseListener(new MouseAdapter() {
                Timer timer = new Timer(delay, (ActionEvent e) -> {
                    shiftY -= 0.1;
                    shiftYValue.setText(roundTo1Dec(shiftY));
                });

                @Override
                public void mousePressed(MouseEvent e) {
                    timer.start();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    timer.stop();
                }
            });

            shiftDown.addMouseListener(new MouseAdapter() {
                Timer timer = new Timer(delay, (ActionEvent e) -> {
                    shiftY += 0.1;
                    shiftYValue.setText(roundTo1Dec(shiftY));
                });

                @Override
                public void mousePressed(MouseEvent e) {
                    timer.start();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    timer.stop();
                }
            });*/
            //
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
                    transposeProperMotion.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    images.clear();
                    createFlipbook();
                    transposeProperMotion.setCursor(Cursor.getDefaultCursor());
                }
            });

            transposeMotionField = new JTextField();
            controlPanel.add(transposeMotionField);
            transposeMotionField.addActionListener((ActionEvent evt) -> {
                if (transposeProperMotion.isSelected() && !transposeMotionField.getText().isEmpty()) {
                    transposeMotionField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    images.clear();
                    createFlipbook();
                    transposeMotionField.setCursor(Cursor.getDefaultCursor());
                }
            });

            useCustomOverlays = new JCheckBox(underline("Work with custom overlays:"));
            controlPanel.add(useCustomOverlays);
            customOverlays = customOverlaysTab.getCustomOverlays();
            useCustomOverlays.addActionListener((ActionEvent evt) -> {
                if (customOverlays.isEmpty()) {
                    showInfoDialog(baseFrame, "There are no custom overlays.");
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
                    component.setEpochCount(epochCount / 2);
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
                                    displayRecenteredWiseImages(newRa, newDec);
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
                                        if (showPanstarrsButton.isSelected()) {
                                            CompletableFuture.supplyAsync(() -> displayPs1Images(newRa, newDec, fieldOfView));
                                        } else if (showAllwiseButton.isSelected()) {
                                            CompletableFuture.supplyAsync(() -> displayAllwiseAtlasImages(newRa, newDec, fieldOfView));
                                        } else if (show2MassButton.isSelected()) {
                                            CompletableFuture.supplyAsync(() -> display2MassAllSkyImages(newRa, newDec, fieldOfView));
                                        }
                                    }
                                    break;
                                default:
                                    //if (smallBodyHelp.isSelected()) {
                                    //    displaySmallBodyPanel(newRa, newDec, component.getMinObsEpoch(), component.getMaxObsEpoch());
                                    //} else {
                                    int overlays = 0;
                                    if (simbadOverlay.isSelected() && simbadEntries != null) {
                                        showCatalogInfo(simbadEntries, mouseX, mouseY, Color.RED);
                                        overlays++;
                                    }
                                    if (gaiaDR2Overlay.isSelected() && gaiaDR2Entries != null) {
                                        showCatalogInfo(gaiaDR2Entries, mouseX, mouseY, Color.CYAN.darker());
                                        overlays++;
                                    }
                                    if (gaiaDR2ProperMotion.isSelected() && gaiaDR2TpmEntries != null) {
                                        showPMInfo(gaiaDR2TpmEntries, mouseX, mouseY, Color.CYAN.darker());
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
                                            displayCatalogSearchResults(newRa, newDec);
                                        } else {
                                            coordsField.setText(roundTo7DecNZ(newRa) + " " + roundTo7DecNZ(newDec));
                                            createFlipbook();
                                        }
                                    }
                                    //}
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
        try {
            timer.stop();
            String coords = coordsField.getText();
            if (coords.isEmpty()) {
                showErrorDialog(baseFrame, "Coordinates must not be empty!");
                return;
            }
            String imageSize = sizeField.getText();
            if (imageSize.isEmpty()) {
                showErrorDialog(baseFrame, "Field of view must not be empty!");
                return;
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
                return;
            }
            baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            if (size != previousSize || targetRa != previousRa || targetDec != previousDec) {
                images = new HashMap<>();
                crosshairs = new ArrayList<>();
                crosshairCoords.setText("");
                hasException = false;
                if (!keepContrast.isSelected() && !markDifferences.isSelected()) {
                    lowContrastSaved = getContrast();
                    highContrastSaved = 0;
                }
                setContrast(lowContrastSaved, highContrastSaved);
                initMinMaxValues();
                //shiftX = shiftY = 0;
                centerX = centerY = 0;
                axisX = axisY = 0;
                windowShift = 0;
                imageCutOff = false;
                disableOverlays = false;
                simbadOverlay.setEnabled(true);
                gaiaDR2Overlay.setEnabled(true);
                allWiseOverlay.setEnabled(true);
                catWiseOverlay.setEnabled(true);
                panStarrsOverlay.setEnabled(true);
                sdssOverlay.setEnabled(true);
                spectrumOverlay.setEnabled(true);
                ssoOverlay.setEnabled(true);
                ghostOverlay.setEnabled(true);
                haloOverlay.setEnabled(true);
                latentOverlay.setEnabled(true);
                spikeOverlay.setEnabled(true);
                gaiaDR2ProperMotion.setEnabled(true);
                catWiseProperMotion.setEnabled(true);
                simbadEntries = null;
                gaiaDR2Entries = null;
                gaiaDR2TpmEntries = null;
                allWiseEntries = null;
                catWiseEntries = null;
                catWiseTpmEntries = null;
                catWiseRejectedEntries = null;
                panStarrsEntries = null;
                sdssEntries = null;
                ssoEntries = null;
                if (useCustomOverlays.isSelected()) {
                    customOverlays.values().forEach((customOverlay) -> {
                        customOverlay.getCheckBox().setEnabled(true);
                        customOverlay.setCatalogEntries(null);
                    });
                }
                ps1Image = null;
                sdssImage = null;
                CompletableFuture.supplyAsync(() -> ps1Image = fetchPs1Image(targetRa, targetDec, size));
                CompletableFuture.supplyAsync(() -> sdssImage = fetchSdssImage(targetRa, targetDec, size));
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
            }
            previousSize = size;
            previousRa = targetRa;
            previousDec = targetDec;
            imageNumber = 0;
            avgValue = 0;

            Fits fits;
            int k;
            switch (epoch) {
                case ALL:
                    flipbook = new FlipbookComponent[epochCount];
                    for (int i = 0; i < epochCount; i++) {
                        NumberPair obsEpochs = loadImage(wiseBand.val, i);
                        flipbook[i] = new FlipbookComponent(wiseBand.val, i, obsEpochs.getX(), obsEpochs.getY());
                    }
                    break;
                case ASCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 0; i < epochCount; i += 2) {
                        NumberPair obsEpochs = loadImage(wiseBand.val, i);
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i, obsEpochs.getX(), obsEpochs.getY());
                    }
                    break;
                case DESCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];
                    for (int i = 1; i < epochCount; i += 2) {
                        NumberPair obsEpochs = loadImage(wiseBand.val, i);
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i, obsEpochs.getX(), obsEpochs.getY());
                    }
                    break;
                case ASCENDING_DESCENDING:
                    flipbook = new FlipbookComponent[epochCount];
                    k = 0;
                    for (int i = 0; i < epochCount; i += 2) {
                        NumberPair obsEpochs = loadImage(wiseBand.val, i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, i, obsEpochs.getX(), obsEpochs.getY());
                        k++;
                    }
                    for (int i = 1; i < epochCount; i += 2) {
                        NumberPair obsEpochs = loadImage(wiseBand.val, i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, i, obsEpochs.getX(), obsEpochs.getY());
                        k++;
                    }
                    break;
                case ASCENDING_DESCENDING_SUBTRACTED:
                    flipbook = new FlipbookComponent[epochCount - 2];
                    k = 0;
                    for (int i = 2; i < epochCount; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W1.val, 0);
                            fits = getImage(WiseBand.W1.val, 0);
                            addImage(WiseBand.W1.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W2.val, 0);
                            fits = getImage(WiseBand.W2.val, 0);
                            addImage(WiseBand.W2.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W1.val, i);
                            fits = getImage(WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 900 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W2.val, i);
                            fits = getImage(WiseBand.W2.val, i);
                            addImage(WiseBand.W2.val, 900 + i, fits);
                        }
                        differenceImaging(800 + i, 900 + i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 800 + i, true);
                        k++;
                    }
                    for (int i = 3; i < epochCount; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W1.val, 1);
                            fits = getImage(WiseBand.W1.val, 1);
                            addImage(WiseBand.W1.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W2.val, 1);
                            fits = getImage(WiseBand.W2.val, 1);
                            addImage(WiseBand.W2.val, 800 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W1.val, i);
                            fits = getImage(WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 900 + i, fits);
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W2.val, i);
                            fits = getImage(WiseBand.W2.val, i);
                            addImage(WiseBand.W2.val, 900 + i, fits);
                        }
                        differenceImaging(800 + i, 900 + i);
                        flipbook[k] = new FlipbookComponent(wiseBand.val, 900 + i, true);
                        k++;
                    }
                    break;
                case ASCENDING_DESCENDING_PARALLAX:
                    flipbook = new FlipbookComponent[2];
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W1.val, 0);
                        loadImage(WiseBand.W1.val, 2);
                        fits = addImages(WiseBand.W1.val, 0, WiseBand.W1.val, 2);
                        addImage(WiseBand.W1.val, 600, fits);
                        for (int i = 4; i < epochCount; i += 2) {
                            loadImage(WiseBand.W1.val, i);
                            fits = addImages(WiseBand.W1.val, 600, WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 600, fits);
                        }
                        addImage(WiseBand.W1.val, 600, takeAverage(fits, epochCount / 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W2.val, 0);
                        loadImage(WiseBand.W2.val, 2);
                        fits = addImages(WiseBand.W2.val, 0, WiseBand.W2.val, 2);
                        addImage(WiseBand.W2.val, 600, fits);
                        for (int i = 4; i < epochCount; i += 2) {
                            loadImage(WiseBand.W2.val, i);
                            fits = addImages(WiseBand.W2.val, 600, WiseBand.W2.val, i);
                            addImage(WiseBand.W2.val, 600, fits);
                        }
                        addImage(WiseBand.W2.val, 600, takeAverage(fits, epochCount / 2));
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W1.val, 1);
                        loadImage(WiseBand.W1.val, 3);
                        fits = addImages(WiseBand.W1.val, 1, WiseBand.W1.val, 3);
                        addImage(WiseBand.W1.val, 700, fits);
                        for (int i = 5; i < epochCount; i += 2) {
                            loadImage(WiseBand.W1.val, i);
                            fits = addImages(WiseBand.W1.val, 700, WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 700, fits);
                        }
                        addImage(WiseBand.W1.val, 700, takeAverage(fits, epochCount / 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W2.val, 1);
                        loadImage(WiseBand.W2.val, 3);
                        fits = addImages(WiseBand.W2.val, 1, WiseBand.W2.val, 3);
                        addImage(WiseBand.W2.val, 700, fits);
                        for (int i = 5; i < epochCount; i += 2) {
                            loadImage(WiseBand.W2.val, i);
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
                            loadImage(WiseBand.W1.val, i);
                            loadImage(WiseBand.W1.val, i + 1);
                            fits = addImages(WiseBand.W1.val, i, WiseBand.W1.val, i + 1);
                            addImage(WiseBand.W1.val, 101 + (i / 2), takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W2.val, i);
                            loadImage(WiseBand.W2.val, i + 1);
                            fits = addImages(WiseBand.W2.val, i, WiseBand.W2.val, i + 1);
                            addImage(WiseBand.W2.val, 101 + (i / 2), takeAverage(fits, 2));
                        }
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, 101 + (i / 2), true);
                    }
                    break;
                /*
                case YEAR_SUBTRACTED:
                    flipbook = new FlipbookComponent[epochCount / 2 - 1];
                    k = 0;
                    for (int i = 2; i < epochCount; i += 2) {
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W1.val, 0);
                            loadImage(WiseBand.W1.val, 1);
                            fits = addImages(WiseBand.W1.val, 0, WiseBand.W1.val, 1);
                            addImage(WiseBand.W1.val, 1000 + k, takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W2.val, 0);
                            loadImage(WiseBand.W2.val, 1);
                            fits = addImages(WiseBand.W2.val, 0, WiseBand.W2.val, 1);
                            addImage(WiseBand.W2.val, 1000 + k, takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W1.val, i);
                            loadImage(WiseBand.W1.val, i + 1);
                            fits = addImages(WiseBand.W1.val, i, WiseBand.W1.val, i + 1);
                            addImage(WiseBand.W1.val, 1100 + k, takeAverage(fits, 2));
                        }
                        if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                            loadImage(WiseBand.W2.val, i);
                            loadImage(WiseBand.W2.val, i + 1);
                            fits = addImages(WiseBand.W2.val, i, WiseBand.W2.val, i + 1);
                            addImage(WiseBand.W2.val, 1100 + k, takeAverage(fits, 2));
                        }
                        differenceImaging(1000 + k, 1100 + k);
                        if (k == 0) {
                            flipbook[k] = new FlipbookComponent(wiseBand.val, 1100 + k, true);
                        } else {
                            flipbook[k] = new FlipbookComponent(wiseBand.val, 1000 + k, true);
                        }
                        k++;
                    }
                    break;
                 */
                case FIRST_REMAINING:
                case FIRST_REMAINING_SUBTRACTED:
                    flipbook = new FlipbookComponent[2];
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W1.val, 0);
                        loadImage(WiseBand.W1.val, 1);
                        fits = addImages(WiseBand.W1.val, 0, WiseBand.W1.val, 1);
                        addImage(WiseBand.W1.val, 100, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W2.val, 0);
                        loadImage(WiseBand.W2.val, 1);
                        fits = addImages(WiseBand.W2.val, 0, WiseBand.W2.val, 1);
                        addImage(WiseBand.W2.val, 100, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W1.val, 2);
                        loadImage(WiseBand.W1.val, 3);
                        fits = addImages(WiseBand.W1.val, 2, WiseBand.W1.val, 3);
                        addImage(WiseBand.W1.val, 300, fits);
                        for (int i = 4; i < epochCount; i++) {
                            loadImage(WiseBand.W1.val, i);
                            fits = addImages(WiseBand.W1.val, 300, WiseBand.W1.val, i);
                            addImage(WiseBand.W1.val, 300, fits);
                        }
                        addImage(WiseBand.W1.val, 300, takeAverage(fits, epochCount - 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W2.val, 2);
                        loadImage(WiseBand.W2.val, 3);
                        fits = addImages(WiseBand.W2.val, 2, WiseBand.W2.val, 3);
                        addImage(WiseBand.W2.val, 300, fits);
                        for (int i = 4; i < epochCount; i++) {
                            loadImage(WiseBand.W2.val, i);
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
                        loadImage(WiseBand.W1.val, 0);
                        loadImage(WiseBand.W1.val, 1);
                        fits = addImages(WiseBand.W1.val, 0, WiseBand.W1.val, 1);
                        addImage(WiseBand.W1.val, 100, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W2.val, 0);
                        loadImage(WiseBand.W2.val, 1);
                        fits = addImages(WiseBand.W2.val, 0, WiseBand.W2.val, 1);
                        addImage(WiseBand.W2.val, 100, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W1.val, epochCount - 2);
                        loadImage(WiseBand.W1.val, epochCount - 1);
                        fits = addImages(WiseBand.W1.val, epochCount - 2, WiseBand.W1.val, epochCount - 1);
                        addImage(WiseBand.W1.val, 200, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W2.val, epochCount - 2);
                        loadImage(WiseBand.W2.val, epochCount - 1);
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
                        loadImage(WiseBand.W1.val, 0);
                        loadImage(WiseBand.W1.val, epochCount - 2);
                        fits = addImages(WiseBand.W1.val, 0, WiseBand.W1.val, epochCount - 2);
                        addImage(WiseBand.W1.val, 400, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W2.val, 0);
                        loadImage(WiseBand.W2.val, epochCount - 2);
                        fits = addImages(WiseBand.W2.val, 0, WiseBand.W2.val, epochCount - 2);
                        addImage(WiseBand.W2.val, 400, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W1) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W1.val, 1);
                        loadImage(WiseBand.W1.val, epochCount - 1);
                        fits = addImages(WiseBand.W1.val, 1, WiseBand.W1.val, epochCount - 1);
                        addImage(WiseBand.W1.val, 500, takeAverage(fits, 2));
                    }
                    if (wiseBand.equals(WiseBand.W2) || wiseBand.equals(WiseBand.W1W2)) {
                        loadImage(WiseBand.W2.val, 1);
                        loadImage(WiseBand.W2.val, epochCount - 1);
                        fits = addImages(WiseBand.W2.val, 1, WiseBand.W2.val, epochCount - 1);
                        addImage(WiseBand.W2.val, 500, takeAverage(fits, 2));
                    }
                    flipbook[0] = new FlipbookComponent(wiseBand.val, 400, true);
                    flipbook[1] = new FlipbookComponent(wiseBand.val, 500, true);
                    break;
            }
            if (Epoch.isSubtracted(epoch)) {
                setSubtractedContrast();
            }
            if (markDifferences.isSelected()) {
                detectDifferences();
            }
            timer.restart();
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
            hasException = true;
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void createStaticBook() {
        timer.stop();
        JPanel grid = new JPanel(new GridLayout(4, 4));
        for (FlipbookComponent component : flipbook) {
            component.setEpochCount(epochCount / 2);
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
            sequencer.generateFromBI(imageSet, file, 50, true);
        }
    }

    private BufferedImage processImage(FlipbookComponent component) {
        BufferedImage image;
        if (wiseBand.equals(WiseBand.W1W2)) {
            image = createComposite(component.getEpoch());
        } else {
            image = createImage(component.getBand(), component.getEpoch());
        }
        image = zoom(image, zoom);
        if (markDifferences.isSelected()) {
            for (NumberPair diffPixel : component.getDiffPixels()) {
                Circle circle = new Circle(getScaledValue(diffPixel.getX()), getScaledValue(diffPixel.getY()), getScaledValue(1), Color.RED);
                circle.draw(image.getGraphics());
            }
        }
        image = flip(image);
        //if (imageNumber == 0) {
        //    image = shift(image);
        //}
        if (!disableOverlays) {
            addOverlaysAndPMVectors(image);
        }
        if (drawCrosshairs.isSelected()) {
            for (int i = 0; i < crosshairs.size(); i++) {
                NumberPair crosshair = crosshairs.get(i);
                CrossHair drawable = new CrossHair(crosshair.getX() * zoom, crosshair.getY() * zoom, zoom * crosshairSize / 100, Color.RED, i + 1);
                drawable.draw(image.getGraphics());
            }
        }
        image = rotate(image, quadrantCount);
        return image;
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
        if (gaiaDR2Overlay.isSelected()) {
            if (gaiaDR2Entries == null) {
                gaiaDR2Entries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> gaiaDR2Entries = fetchCatalogEntries(new GaiaDR2CatalogEntry()));
            } else {
                drawOverlay(image, gaiaDR2Entries, Color.CYAN.darker(), Shape.CIRCLE);
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
        if (gaiaDR2ProperMotion.isSelected()) {
            if (gaiaDR2TpmEntries == null) {
                gaiaDR2TpmEntries = Collections.emptyList();
                CompletableFuture.supplyAsync(() -> gaiaDR2TpmEntries = fetchTpmCatalogEntries(new GaiaDR2CatalogEntry()));
            } else {
                drawPMVectors(image, gaiaDR2TpmEntries, Color.CYAN.darker());
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

    private NumberPair loadImage(int band_, int epoch) throws Exception {
        String str = Integer.toString(band_);
        int[] bands = new int[str.length()];
        for (int i = 0; i < str.length(); i++) {
            bands[i] = str.charAt(i) - '0';
        }
        Fits fits = null;
        for (int band : bands) {
            fits = getImage(band, epoch);
            if (fits != null) {
                continue;
            }
            try {
                fits = new Fits(getImageData(band, epoch));
                if (skipBadCoadds.isSelected()) {
                    ImageHDU weightHDU = (ImageHDU) fits.getHDU(1);
                    ImageData weightData = (ImageData) weightHDU.getData();
                    short[][] weights = (short[][]) weightData.getData();
                    if (getNumberOfWeightsBelowLimit(weights, 5) > 50) {
                        fits = getPreviousImage(band, epoch);
                    }
                }
                ImageHDU imageHDU = (ImageHDU) fits.getHDU(0);
                ImageData imageData = (ImageData) imageHDU.getData();
                float[][] values = (float[][]) imageData.getData();

                // Replace an image with too many zero values by a preceding image
                axisY = values.length;
                if (axisY > 0) {
                    axisX = values[0].length;
                }
                int zeroValues = 0;
                for (int i = 0; i < axisY; i++) {
                    for (int j = 0; j < axisX; j++) {
                        try {
                            if (values[i][j] == 0) {
                                zeroValues++;
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                    }
                }
                if (zeroValues > 1000) {
                    fits = getPreviousImage(band, epoch);
                }

                // Un/Check the "Set min/max limits" check box automatically
                minMaxLimits.setSelected(false);
                NumberTriplet minMaxValues = getMinMaxValues(values);
                int avgVal = (int) minMaxValues.getZ();
                if (avgValue == 0) {
                    avgValue = avgVal;
                }
                if (avgValue > 500) {
                    minMaxLimits.setSelected(false);
                } else {
                    minMaxLimits.setSelected(true);
                }
            } catch (Exception ex) {
                if (ex instanceof NumberFormatException) {
                    throw ex;
                }
                fits = getPreviousImage(band, epoch);
            }
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            Header header = hdu.getHeader();
            double crpix1 = header.getDoubleValue("CRPIX1");
            double crpix2 = header.getDoubleValue("CRPIX2");
            double naxis1 = header.getDoubleValue("NAXIS1");
            double naxis2 = header.getDoubleValue("NAXIS2");
            if (size > naxis1 && size > naxis2 && !disableOverlays) {
                String message = "Some features of the Image Viewer have been disabled because the current field of view exceeds the requested WISE tile.";
                showWarnDialog(baseFrame, message);
                disableOverlays = true;
                simbadOverlay.setEnabled(false);
                gaiaDR2Overlay.setEnabled(false);
                allWiseOverlay.setEnabled(false);
                catWiseOverlay.setEnabled(false);
                panStarrsOverlay.setEnabled(false);
                sdssOverlay.setEnabled(false);
                spectrumOverlay.setEnabled(false);
                ssoOverlay.setEnabled(false);
                ghostOverlay.setEnabled(false);
                haloOverlay.setEnabled(false);
                latentOverlay.setEnabled(false);
                spikeOverlay.setEnabled(false);
                gaiaDR2ProperMotion.setEnabled(false);
                catWiseProperMotion.setEnabled(false);
                if (useCustomOverlays.isSelected()) {
                    customOverlays.values().forEach((customOverlay) -> {
                        customOverlay.getCheckBox().setEnabled(false);
                    });
                }
            }
            if (naxis1 != naxis2) {
                imageCutOff = true;
            }
            pixelX = crpix1;
            pixelY = naxis2 - crpix2;
            axisX = (int) round(naxis1);
            axisY = (int) round(naxis2);
            addImage(band, epoch, fits);
        }
        return getMinMaxObsEpoch(fits);
    }

    private NumberPair getMinMaxObsEpoch(Fits fits) throws Exception {
        ImageHDU hdu = (ImageHDU) fits.getHDU(0);
        Header header = hdu.getHeader();
        double minObsEpoch = header.getDoubleValue("MJDMIN");
        double maxObsEpoch = header.getDoubleValue("MJDMAX");
        return new NumberPair(minObsEpoch, maxObsEpoch);
    }

    private Fits getPreviousImage(int band, int epoch) throws FitsException {
        Fits fits;
        try {
            int previousEpoch;
            switch (epoch) {
                case 0:
                case 1:
                    previousEpoch = epoch + 2;
                    break;
                default:
                    previousEpoch = epoch - 2;
            }
            fits = getImage(band, previousEpoch);
            if (fits == null) {
                fits = new Fits(getImageData(band, previousEpoch));
            }
        } catch (Exception ex) {
            float[][] values = new float[axisY][axisX];
            short[][] weights = new short[axisY][axisX];
            for (int i = 0; i < axisY; i++) {
                for (int j = 0; j < axisX; j++) {
                    values[i][j] = 0;
                    weights[i][j] = 0;
                }
            }
            fits = new Fits();
            fits.addHDU(FitsFactory.hduFactory(values));
            fits.addHDU(FitsFactory.hduFactory(weights));
        }
        return fits;
    }

    private InputStream getImageData(int band, int epoch) throws Exception {
        String imageUrl;
        if (skipFirstEpoch.isSelected()) {
            epoch += 2;
        }
        if (transposeProperMotion.isSelected() && !transposeMotionField.getText().isEmpty()) {
            NumberPair properMotion = getCoordinates(transposeMotionField.getText());
            double pmra = properMotion.getX();
            double pmdec = properMotion.getY();
            int numberOfEpochs = epoch > 1 ? epoch + 6 : epoch;
            double pmraOfOneEpoch = (pmra / 2) / DEG_MAS;
            double pmdecOfOneEpoch = (pmdec / 2) / DEG_MAS;
            double pmraOfEpochs = numberOfEpochs * pmraOfOneEpoch;
            double pmdecOfEpochs = numberOfEpochs * pmdecOfOneEpoch;
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
        return WISE_VIEW_URL + "?ra=" + targetRa + "&dec=" + targetDec + "&size=" + size + "&band=" + band + "&epoch=" + epoch + (skipBadCoadds.isSelected() ? "&covmap=true" : "");
    }

    private void detectDifferences() {
        for (int i = 0; i < flipbook.length; i++) {
            FlipbookComponent component1 = flipbook[i];
            FlipbookComponent component2 = flipbook[i + 1 == flipbook.length ? 0 : i + 1];
            int band = component1.getBand();
            int epoch1 = component1.getEpoch();
            int epoch2 = component2.getEpoch();
            List<NumberPair> diffPixels = new ArrayList<>();
            if (band == 1 || band == 12) {
                detectDifferencesPerBand(1, epoch1, epoch2, diffPixels);
            }
            if (band == 2 || band == 12) {
                detectDifferencesPerBand(2, epoch1, epoch2, diffPixels);
            }
            component2.setDiffPixels(diffPixels);
        }
    }

    private void detectDifferencesPerBand(int band, int epoch1, int epoch2, List<NumberPair> diffPixels) {
        try {
            Fits fits = getImage(band, epoch1);
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values1 = (float[][]) imageData.getData();

            fits = getImage(band, epoch2);
            hdu = (ImageHDU) fits.getHDU(0);
            imageData = (ImageData) hdu.getData();
            float[][] values2 = (float[][]) imageData.getData();

            if (minValue == 0 && maxValue == 0) {
                NumberTriplet minMaxValues = getMinMaxValues(values1);
                int minVal = (int) minMaxValues.getX();
                int maxVal = (int) minMaxValues.getY();
                int avgVal = (int) minMaxValues.getZ();
                setMinMaxValues(minVal, maxVal, avgVal);
            }

            for (int i = 0; i < axisY; i++) {
                for (int j = 0; j < axisX; j++) {
                    float value1 = processPixel(values1[i][j]);
                    float value2 = processPixel(values2[i][j]);
                    float max = max(value1, value2);
                    float min = min(value1, value2);
                    //if (max - min > (max + min) / 2) {
                    if (max - min > (max + min) / 2 && value1 == max) {
                        diffPixels.add(new NumberPair(j, i));
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

            if (minValue == 0 && maxValue == 0) {
                NumberTriplet minMaxValues = getMinMaxValues(values);
                int minVal = (int) minMaxValues.getX();
                int maxVal = (int) minMaxValues.getY();
                int avgVal = (int) minMaxValues.getZ();
                setMinMaxValues(minVal, maxVal, avgVal);
            }

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
            Fits fits = getImage(1, epoch);
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] valuesW1 = (float[][]) imageData.getData();

            fits = getImage(2, epoch);
            hdu = (ImageHDU) fits.getHDU(0);
            imageData = (ImageData) hdu.getData();
            float[][] valuesW2 = (float[][]) imageData.getData();

            if (smoothImage.isSelected()) {
                valuesW1 = smooth(valuesW1);
                valuesW2 = smooth(valuesW2);
            }

            if (minValue == 0 && maxValue == 0) {
                NumberTriplet minMaxValues1 = getMinMaxValues(valuesW1);
                int minVal1 = (int) minMaxValues1.getX();
                int maxVal1 = (int) minMaxValues1.getY();
                int avgVal1 = (int) minMaxValues1.getZ();

                NumberTriplet minMaxValues2 = getMinMaxValues(valuesW2);
                int minVal2 = (int) minMaxValues2.getX();
                int maxVal2 = (int) minMaxValues2.getY();
                int avgVal2 = (int) minMaxValues2.getZ();

                int minVal = min(minVal1, minVal2);
                int maxVal = max(maxVal1, maxVal2);
                int avgVal = (avgVal1 + avgVal2) / 2;
                setMinMaxValues(minVal, maxVal, avgVal);
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

    /*private BufferedImage shift(BufferedImage image) {
        AffineTransform tx = AffineTransform.getTranslateInstance(shiftX, shiftY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB));
    }*/
    //
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
        value = normalize(value, minValue, maxValue);
        if (stretchImage.isSelected()) {
            value = stretch(value);
        }
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

    private int getContrast() {
        return size < 30 ? 25 : 50;
    }

    private void setSubtractedContrast() {
        if (minMaxLimits.isSelected()) {
            setContrast(getContrast(), 0);
        } else {
            setContrast(getContrast(), 500);
        }
    }

    private void setContrast(int low, int high) {
        lowScaleSlider.setValue(lowContrast = low);
        highScaleSlider.setValue(highContrast = high);
    }

    private int getNumberOfWeightsBelowLimit(short[][] weights, int limit) {
        int x = weights.length;
        int y = weights[0].length;
        int numberOfWeightsBelowLimit = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                short weight = weights[i][j];
                if (weight < limit) {
                    numberOfWeightsBelowLimit++;
                }
            }
        }
        return numberOfWeightsBelowLimit;
    }

    private NumberTriplet getMinMaxValues(float[][] values) {
        int x = values.length;
        int y = values[0].length;
        float minVal = clipToBoundaries(values[0][0]);
        float maxVal = clipToBoundaries(values[0][0]);
        int sum = 0;
        int nbr = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                float value = values[i][j];
                if (value == Float.POSITIVE_INFINITY || value == Float.NEGATIVE_INFINITY || value == Float.NaN) {
                    continue;
                }
                if (minMaxLimits.isSelected()) {
                    value = clipToBoundaries(value);
                }
                if (value < minVal) {
                    minVal = value;
                }
                if (value > maxVal) {
                    maxVal = value;
                }
                sum += abs(value);
                nbr++;
            }
        }
        float avgVal = sum / nbr;
        return new NumberTriplet(minVal, maxVal, avgVal);
    }

    private float clipToBoundaries(float value) {
        return max(MIN_VALUE, min(MAX_VALUE, value));
    }

    private void initMinMaxValues() {
        minValue = maxValue = 0;
    }

    private void setMinMaxValues(int minVal, int maxVal, int avgVal) {
        int presetMinVal;
        int presetMaxVal;
        if (Epoch.isSubtracted(epoch)) {
            presetMinVal = -avgVal * size / ((lowContrast + highContrast) / (minMaxLimits.isSelected() ? 2 : 5));
            presetMinVal = presetMinVal < minVal ? minVal : presetMinVal;
        } else {
            presetMinVal = minVal <= MIN_VALUE ? -avgVal : minVal;
        }
        presetMaxVal = avgVal * size;
        presetMaxVal = presetMaxVal > maxVal ? maxVal : presetMaxVal;

        minValueSlider.setMinimum(minVal);
        minValueSlider.setMaximum(maxVal);
        minValueSlider.setValue(presetMinVal);

        maxValueSlider.setMinimum(minVal);
        maxValueSlider.setMaximum(maxVal);
        maxValueSlider.setValue(presetMaxVal);

        minValue = presetMinVal;
        maxValue = presetMaxVal;
    }

    private void displayCatalogSearchResults(double targetRa, double targetDec) {
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
    }

    private void displayRecenteredWiseImages(double targetRa, double targetDec) {
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
        imageViewerTab.getSizeField().setText("100");
        imageViewerTab.getWiseBands().setSelectedItem(wiseBand);
        //imageViewerTab.getEpochs().setSelectedItem(epoch);
        imageViewerTab.getZoomSlider().setValue(ZOOM);
        imageViewerTab.setZoom(ZOOM);
        imageViewerTab.setQuadrantCount(quadrantCount);
        imageViewerTab.setImageViewer(this);
        imageViewerTab.createFlipbook();

        baseFrame.setCursor(Cursor.getDefaultCursor());
    }

    private BufferedImage fetchPs1Image(double targetRa, double targetDec, double size) {
        try {
            List<String> fileNames = new ArrayList<>();
            String imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=giy&sep=comma", targetRa, targetDec);
            String response = readResponse(establishHttpConnection(imageUrl));
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

    private Object displayPs1Images(double targetRa, double targetDec, int size) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            // Fetch file name for each Pan-STARRS filter
            SortedMap<String, String> imageInfos = new TreeMap<>();
            String imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/ps1filenames.py?RA=%f&DEC=%f&filters=grizy&sep=comma", targetRa, targetDec);
            String response = readResponse(establishHttpConnection(imageUrl));
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
                    imageInfos.put(columnValues[filter], columnValues[fileName]);
                }
            }

            imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?red=%s&green=%s&blue=%s&ra=%f&dec=%f&size=%d&output_size=%d", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g"), targetRa, targetDec, (int) round(size * 4), 256);
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());

            JPanel ps1Panel = new JPanel(new GridLayout(1, 5));
            ps1Panel.add(buildImagePanel(producePs1Image(imageInfos.get("g"), targetRa, targetDec, size), "g"));
            ps1Panel.add(buildImagePanel(producePs1Image(imageInfos.get("r"), targetRa, targetDec, size), "r"));
            ps1Panel.add(buildImagePanel(producePs1Image(imageInfos.get("i"), targetRa, targetDec, size), "i"));
            ps1Panel.add(buildImagePanel(producePs1Image(imageInfos.get("z"), targetRa, targetDec, size), "z"));
            ps1Panel.add(buildImagePanel(producePs1Image(imageInfos.get("y"), targetRa, targetDec, size), "y"));
            ps1Panel.add(buildImagePanel(ImageIO.read(stream), "y-i-g"));

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("Pan-STARRS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
            imageFrame.getContentPane().add(ps1Panel);
            imageFrame.setSize(1320, 260);
            imageFrame.setAlwaysOnTop(true);
            imageFrame.setResizable(false);
            imageFrame.setVisible(true);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
        return null;
    }

    private BufferedImage producePs1Image(String fileName, double targetRa, double targetDec, int size) throws IOException {
        String imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?red=%s&ra=%f&dec=%f&size=%d&output_size=%d", fileName, targetRa, targetDec, (int) round(size * 4), 256);
        HttpURLConnection connection = establishHttpConnection(imageUrl);
        BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
        return ImageIO.read(stream);
    }

    private Object displayAllwiseAtlasImages(double targetRa, double targetDec, int fieldOfView) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            // Fetch coadd id for each WISE band
            SortedMap<Integer, String> imageInfos = new TreeMap<>();
            String imageUrl = String.format("https://irsa.ipac.caltech.edu/ibe/search/wise/allwise/p3am_cdd?POS=%f,%f&ct=csv&mcen", targetRa, targetDec);
            String response = readResponse(establishHttpConnection(imageUrl));
            try (Scanner scanner = new Scanner(response)) {
                String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
                int band = 0;
                int coadd_id = 0;
                for (int i = 0; i < columnNames.length; i++) {
                    if (columnNames[i].equals("band")) {
                        band = i;
                    }
                    if (columnNames[i].equals("coadd_id")) {
                        coadd_id = i;
                    }
                }
                while (scanner.hasNextLine()) {
                    String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
                    imageInfos.put(new Integer(columnValues[band]), columnValues[coadd_id]);
                }
            }

            // Fetch cutout for each WISE band
            int length = fieldOfView;
            SortedMap<Integer, Fits> fitsFiles = new TreeMap<>();
            for (Map.Entry<Integer, String> entry : imageInfos.entrySet()) {
                int band = entry.getKey();
                String coadd_id = entry.getValue();
                String url = String.format("https://irsa.ipac.caltech.edu/ibe/data/wise/allwise/p3am_cdd/%s/%s/%s/%s-w%d-int-3.fits?center=%f,%f&size=%darcsec", coadd_id.substring(0, 2), coadd_id.substring(0, 4), coadd_id, coadd_id, band, targetRa, targetDec, fieldOfView);
                HttpURLConnection connection = establishHttpConnection(url);
                Fits fits = new Fits(connection.getInputStream());
                ImageHDU hdu = (ImageHDU) fits.getHDU(0);
                Header header = hdu.getHeader();
                length = (int) round(header.getDoubleValue("NAXIS1"));
                ImageData imageData = (ImageData) hdu.getData();
                float[][] values = (float[][]) imageData.getData();
                NumberTriplet minMaxValues = getMinMaxValues(values);
                float minVal = (float) minMaxValues.getX();
                float maxVal = (float) minMaxValues.getY();
                float[][] processedValues = new float[length][length];
                for (int i = 0; i < length; i++) {
                    for (int j = 0; j < length; j++) {
                        try {
                            processedValues[i][j] = min(1, normalize(values[i][j], minVal, maxVal) * 1);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                    }
                }
                Fits result = new Fits();
                result.addHDU(FitsFactory.hduFactory(processedValues));
                fitsFiles.put(band, result);
            }

            // Produce grayscale RGB image for each WISE band
            List<BufferedImage> atlasImages = new ArrayList<>();
            for (Map.Entry<Integer, Fits> entry : fitsFiles.entrySet()) {
                atlasImages.add(produceGrayscaleImage(entry.getValue(), length));
            }

            // Produce colored RGB image
            Fits fits = fitsFiles.get(1);
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values1 = (float[][]) imageData.getData();
            fits = fitsFiles.get(2);
            hdu = (ImageHDU) fits.getHDU(0);
            imageData = (ImageData) hdu.getData();
            float[][] values2 = (float[][]) imageData.getData();
            fits = fitsFiles.get(4);
            hdu = (ImageHDU) fits.getHDU(0);
            imageData = (ImageData) hdu.getData();
            float[][] values4 = (float[][]) imageData.getData();
            BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length; j++) {
                    try {
                        float w1 = values1[i][j];
                        float w2 = values2[i][j];
                        float w4 = values4[i][j];
                        graphics.setColor(new Color(w4, w2, w1));
                        graphics.fillRect(j, i, 1, 1);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                    }
                }
            }
            atlasImages.add(image);

            // Display Atlas images
            JPanel atlasPanel = new JPanel(new GridLayout(1, 5));
            int band = 1;
            for (BufferedImage atlasImage : atlasImages) {
                String imageHeader = band < 5 ? "W" + band++ : "W4-W2-W1";
                atlasPanel.add(buildImagePanel(flip(atlasImage), imageHeader));
            }
            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("AllWISE - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + fieldOfView + "\"");
            imageFrame.getContentPane().add(atlasPanel);
            imageFrame.setSize(1100, 260);
            imageFrame.setAlwaysOnTop(true);
            imageFrame.setResizable(false);
            imageFrame.setVisible(true);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
        return null;
    }

    private Object display2MassAllSkyImages(double targetRa, double targetDec, int fieldOfView) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            // Fetch file name for each 2Mass filter
            SortedMap<String, String[]> imageInfos = new TreeMap<>();
            String imageUrl = String.format("https://irsa.ipac.caltech.edu/ibe/search/twomass/allsky/allsky?POS=%f,%f&ct=csv&mcen", targetRa, targetDec);
            String response = readResponse(establishHttpConnection(imageUrl));
            try (Scanner scanner = new Scanner(response)) {
                String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
                int filter = 0;
                int ordate = 0;
                int hemisphere = 0;
                int scanno = 0;
                int fname = 0;
                for (int i = 0; i < columnNames.length; i++) {
                    switch (columnNames[i]) {
                        case "filter":
                            filter = i;
                            break;
                        case "ordate":
                            ordate = i;
                            break;
                        case "hemisphere":
                            hemisphere = i;
                            break;
                        case "scanno":
                            scanno = i;
                            break;
                        case "fname":
                            fname = i;
                            break;
                        default:
                            break;
                    }
                }
                while (scanner.hasNextLine()) {
                    String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
                    imageInfos.put(columnValues[filter], new String[]{columnValues[ordate], columnValues[hemisphere], columnValues[scanno], columnValues[fname]});
                }
            }

            // Fetch cutout for each 2Mass filter
            int length = fieldOfView;
            SortedMap<String, Fits> fitsFiles = new TreeMap<>();
            for (Map.Entry<String, String[]> entry : imageInfos.entrySet()) {
                String filter = entry.getKey();
                String[] params = entry.getValue();
                String ordate = params[0];
                String hemisphere = params[1];
                String scanno = params[2];
                if (Integer.valueOf(scanno) < 100) {
                    scanno = "0" + scanno;
                }
                String fname = params[3];
                String url = String.format("https://irsa.ipac.caltech.edu/ibe/data/twomass/allsky/allsky/%s%s/s%s/image/%s?center=%f,%f&size=%darcsec", ordate, hemisphere, scanno, fname, targetRa, targetDec, fieldOfView);
                HttpURLConnection connection = establishHttpConnection(url);
                Fits fits = new Fits(connection.getInputStream());
                ImageHDU hdu = (ImageHDU) fits.getHDU(0);
                Header header = hdu.getHeader();
                length = (int) round(header.getDoubleValue("NAXIS1"));
                ImageData imageData = (ImageData) hdu.getData();
                float[][] values = (float[][]) imageData.getData();
                NumberTriplet minMaxValues = getMinMaxValues(values);
                float minVal = (float) minMaxValues.getX();
                float maxVal = (float) minMaxValues.getY();
                float[][] processedValues = new float[length][length];
                for (int i = 0; i < length; i++) {
                    for (int j = 0; j < length; j++) {
                        try {
                            processedValues[i][j] = min(1, normalize(values[i][j], minVal, maxVal) * 2);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                    }
                }
                Fits result = new Fits();
                result.addHDU(FitsFactory.hduFactory(processedValues));
                fitsFiles.put(filter, result);
            }

            // Produce grayscale RGB image for each 2Mass filter
            List<BufferedImage> allSkyImages = new ArrayList<>();
            allSkyImages.add(produceGrayscaleImage(fitsFiles.get("j"), length));
            allSkyImages.add(produceGrayscaleImage(fitsFiles.get("h"), length));
            allSkyImages.add(produceGrayscaleImage(fitsFiles.get("k"), length));

            // Produce colored RGB image
            Fits fits = fitsFiles.get("j");
            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) hdu.getData();
            float[][] values1 = (float[][]) imageData.getData();
            fits = fitsFiles.get("h");
            hdu = (ImageHDU) fits.getHDU(0);
            imageData = (ImageData) hdu.getData();
            float[][] values2 = (float[][]) imageData.getData();
            fits = fitsFiles.get("k");
            hdu = (ImageHDU) fits.getHDU(0);
            imageData = (ImageData) hdu.getData();
            float[][] values4 = (float[][]) imageData.getData();
            BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length; j++) {
                    try {
                        float jFilter = values1[i][j];
                        float hFilter = values2[i][j];
                        float kFilter = values4[i][j];
                        graphics.setColor(new Color(kFilter, hFilter, jFilter));
                        graphics.fillRect(j, i, 1, 1);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                    }
                }
            }
            allSkyImages.add(image);

            // Display All-Sky images
            JPanel atlasPanel = new JPanel(new GridLayout(1, 4));
            atlasPanel.add(buildImagePanel(flip(allSkyImages.get(0)), "J"));
            atlasPanel.add(buildImagePanel(flip(allSkyImages.get(1)), "H"));
            atlasPanel.add(buildImagePanel(flip(allSkyImages.get(2)), "K"));
            atlasPanel.add(buildImagePanel(flip(allSkyImages.get(3)), "K-H-J"));

            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("2MASS - Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + fieldOfView + "\"");
            imageFrame.getContentPane().add(atlasPanel);
            imageFrame.setSize(880, 260);
            imageFrame.setAlwaysOnTop(true);
            imageFrame.setResizable(false);
            imageFrame.setVisible(true);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
        return null;
    }

    private BufferedImage produceGrayscaleImage(Fits fits, int length) throws Exception {
        ImageHDU hdu = (ImageHDU) fits.getHDU(0);
        ImageData imageData = (ImageData) hdu.getData();
        float[][] values = (float[][]) imageData.getData();
        BufferedImage image = new BufferedImage(length, length, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                try {
                    float value = 1 - values[i][j];
                    graphics.setColor(new Color(value, value, value));
                    graphics.fillRect(j, i, 1, 1);
                } catch (ArrayIndexOutOfBoundsException ex) {
                }
            }
        }
        return image;
    }

    private JPanel buildImagePanel(BufferedImage atlasImage, String imageHeader) {
        JPanel panel = new JPanel();
        panel.setBorder(createEtchedBorder(imageHeader));
        atlasImage = zoom(atlasImage, 200);
        double x = atlasImage.getWidth() / 2;
        double y = atlasImage.getHeight() / 2;
        Graphics g = atlasImage.getGraphics();
        Circle circle = new Circle(x, y, 10, Color.MAGENTA);
        circle.draw(g);
        panel.add(new JLabel(new ImageIcon(atlasImage)));
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
                Drawable toDraw = new XCross(position.getX(), position.getY(), getOverlaySize(), Color.GREEN.darker());
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

            //double tpm = calculateTotalProperMotion(pmRa, pmDec);
            //double pmLimit = toDouble(properMotionField.getText());
            //if (tpm > pmLimit) {
            double ra = 0;
            double dec = 0;
            int numberOfYears = 0;
            if (catalogEntry instanceof GaiaDR2CatalogEntry) {
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

            numberOfYears = (epochCount / 2) + 3; // 3 -> 2011, 2012 & 2013
            double newRa = ra + (numberOfYears * pmRa / DEG_MAS) / cos(toRadians(dec));
            double newDec = dec + numberOfYears * pmDec / DEG_MAS;

            pixelCoords = getPixelCoordinates(newRa, newDec);
            double newX = pixelCoords.getX();
            double newY = pixelCoords.getY();

            Arrow arrow = new Arrow(x, y, newX, newY, getOverlaySize(), color);
            arrow.draw(graphics);
            //}
        });
    }

    private void showPMInfo(List<CatalogEntry> catalogEntries, int x, int y, Color color) {
        catalogEntries.forEach(catalogEntry -> {
            double pmRa = catalogEntry.getPmra();
            double pmDec = catalogEntry.getPmdec();
            //double tpm = calculateTotalProperMotion(pmRa, pmDec);
            //double pmLimit = toDouble(properMotionField.getText());
            double radius = getOverlaySize() / 2;
            if (/*tpm > pmLimit
                    &&*/catalogEntry.getPixelRa() > x - radius && catalogEntry.getPixelRa() < x + radius
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
                BorderFactory.createEtchedBorder(), catalogEntry.getCatalogName() + " entry (computed values are shown in green)", TitledBorder.LEFT, TitledBorder.TOP
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
            container.add(createMainSequenceSpectralTypePanel(catalogEntry));
            if (catalogEntry instanceof AllWiseCatalogEntry) {
                AllWiseCatalogEntry entry = (AllWiseCatalogEntry) catalogEntry;
                if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                    JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    messagePanel.add(createLabel(AGN_WARNING, JColor.DARK_RED));
                    container.add(messagePanel);
                }
            }
            if (catalogEntry instanceof GaiaDR2CatalogEntry) {
                GaiaDR2CatalogEntry entry = (GaiaDR2CatalogEntry) catalogEntry;
                if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                    JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    messagePanel.add(createLabel(WD_WARNING, JColor.DARK_RED));
                    container.add(messagePanel);
                }
            }
            container.add(createBrownDwarfsSpectralTypePanel(catalogEntry));

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

    private JScrollPane createMainSequenceSpectralTypePanel(CatalogEntry catalogEntry) {
        try {
            Map<SpectralTypeLookupResult, Set<ColorValue>> results = mainSequenceSpectralTypeLookupService.lookup(catalogEntry.getColors());

            List<String[]> spectralTypes = new ArrayList<>();
            results.entrySet().forEach(entry -> {
                SpectralTypeLookupResult key = entry.getKey();
                Set<ColorValue> values = entry.getValue();
                StringBuilder matchedColors = new StringBuilder();
                Iterator<ColorValue> colorIterator = values.iterator();
                while (colorIterator.hasNext()) {
                    ColorValue colorValue = colorIterator.next();
                    matchedColors.append(colorValue.getColor().val).append("=").append(roundTo3DecNZ(colorValue.getValue()));
                    if (colorIterator.hasNext()) {
                        matchedColors.append(", ");
                    }
                }
                String spectralType = key.getSpt() + "," + key.getTeff() + "," + roundTo3Dec(key.getRsun()) + "," + roundTo3Dec(key.getMsun())
                        + "," + matchedColors + "," + roundTo3Dec(key.getNearest()) + "," + roundTo3DecLZ(key.getGap());
                spectralTypes.add(spectralType.split(",", 7));
            });

            String titles = "spt,teff,sol rad,sol mass,matched colors,nearest color,gap to nearest color";
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
            spectralTypeTable.setCellSelectionEnabled(false);
            resizeColumnWidth(spectralTypeTable);

            JScrollPane spectralTypePanel = spectralTypes.isEmpty()
                    ? new JScrollPane(createLabel("No colors available / No match", JColor.DARK_RED))
                    : new JScrollPane(spectralTypeTable);
            spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Main sequence spectral type evaluation", TitledBorder.LEFT, TitledBorder.TOP
            ));

            return spectralTypePanel;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private JScrollPane createBrownDwarfsSpectralTypePanel(CatalogEntry catalogEntry) {
        try {
            Map<SpectralTypeLookupResult, Set<ColorValue>> results = brownDwarfsSpectralTypeLookupService.lookup(catalogEntry.getColors());

            List<String[]> spectralTypes = new ArrayList<>();
            results.entrySet().forEach(entry -> {
                SpectralTypeLookupResult key = entry.getKey();
                Set<ColorValue> values = entry.getValue();
                StringBuilder matchedColors = new StringBuilder();
                Iterator<ColorValue> colorIterator = values.iterator();
                while (colorIterator.hasNext()) {
                    ColorValue colorValue = colorIterator.next();
                    matchedColors.append(colorValue.getColor().val).append("=").append(roundTo3DecNZ(colorValue.getValue()));
                    if (colorIterator.hasNext()) {
                        matchedColors.append(", ");
                    }
                }
                String spectralType = key.getSpt() + "," + matchedColors + "," + roundTo3Dec(key.getNearest()) + "," + roundTo3DecLZ(key.getGap());
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
            spectralTypeTable.setCellSelectionEnabled(false);
            resizeColumnWidth(spectralTypeTable);

            JScrollPane spectralTypePanel = spectralTypes.isEmpty()
                    ? new JScrollPane(createLabel("No colors available / No match", JColor.DARK_RED))
                    : new JScrollPane(spectralTypeTable);
            spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "M-L-T-Y dwarfs spectral type evaluation", TitledBorder.LEFT, TitledBorder.TOP
            ));

            return spectralTypePanel;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /*
    private void displaySmallBodyPanel(double targetRa, double targetDec, double minObsEpoch, double maxObsEpoch) {
        JPanel detailPanel = new JPanel(new GridLayout(10, 2));
        detailPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        StringPair sexagesimalCoords = convertToSexagesimalCoords(targetRa, targetDec);
        String objectRa = sexagesimalCoords.getS1().replace(" ", ":").split("\\.")[0];
        String objectDec = sexagesimalCoords.getS2().split("\\.")[0];

        detailPanel.add(new JLabel("Min observation time (*): ", JLabel.RIGHT));
        detailPanel.add(new JTextField(convertMJDToDateTime(new BigDecimal(Double.toString(minObsEpoch))).format(DATE_TIME_FORMATTER) + " (" + minObsEpoch + ")"));

        detailPanel.add(new JLabel("Max observation time (*): ", JLabel.RIGHT));
        detailPanel.add(new JTextField(convertMJDToDateTime(new BigDecimal(Double.toString(maxObsEpoch))).format(DATE_TIME_FORMATTER) + " (" + maxObsEpoch + ")"));

        detailPanel.add(new JLabel("Some observatories in the North: ", JLabel.RIGHT));
        detailPanel.add(new JTextField("T05, T08, F51, F52, 675, 703, Wise"));

        detailPanel.add(new JLabel("Some observatories in the South: ", JLabel.RIGHT));
        detailPanel.add(new JTextField("413, Antofagasta, Arica, Johannesburg, Pretoria"));

        detailPanel.add(new JLabel("Center of the search region in RA: ", JLabel.RIGHT));
        detailPanel.add(new JTextField(objectRa + " (" + roundTo7DecNZ(targetRa) + ")"));

        detailPanel.add(new JLabel("Center of the search region in dec: ", JLabel.RIGHT));
        detailPanel.add(new JTextField(objectDec + " (" + roundTo7DecNZ(targetDec) + ")"));

        detailPanel.add(new JLabel("Width of search region in RA: ", JLabel.RIGHT));
        detailPanel.add(new JTextField("w0:05"));

        detailPanel.add(new JLabel("Width of search region in dec: ", JLabel.RIGHT));
        detailPanel.add(new JTextField("w0 05"));

        detailPanel.add(new JLabel("Visual magnitude limit: ", JLabel.RIGHT));
        detailPanel.add(new JTextField("25"));

        detailPanel.add(new JLabel("Link: ", JLabel.RIGHT));
        detailPanel.add(createHyperlink("JPL SB Identification", "https://ssd.jpl.nasa.gov/sbfind.cgi"));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(detailPanel);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createEtchedBorder());
        container.add(infoPanel);

        infoPanel.add(new JLabel("(*) These are the observation times of the first and last single exposures that went into the coadd the"));
        infoPanel.add(new JLabel("small body is located in. You have to find the single exposure between these 2 dates in which the object"));
        infoPanel.add(new JLabel("shows up. Use the"));
        infoPanel.add(createHyperlink("WISE image service", "https://irsa.ipac.caltech.edu/applications/wise"));
        infoPanel.add(new JLabel("to do so."));
        infoPanel.add(new JLabel("Enter the observation time of that single exposure into JPL's SB Identification tool."));

        JFrame smallBodyFrame = new JFrame();
        smallBodyFrame.setIconImage(getToolBoxImage());
        smallBodyFrame.setTitle("Data to enter into JPL's Small Body Identification tool");
        smallBodyFrame.add(container);
        smallBodyFrame.setSize(600, 400);
        smallBodyFrame.setAlwaysOnTop(true);
        smallBodyFrame.setResizable(false);
        smallBodyFrame.setVisible(true);
    }*/
    //
    private double getFovDiagonal() {
        return size * SIZE_FACTOR * sqrt(2);
    }

    private double getOverlaySize() {
        return 5 + zoom / 100;
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

    public Timer getTimer() {
        return timer;
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

}
