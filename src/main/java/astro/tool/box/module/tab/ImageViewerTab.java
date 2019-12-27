package astro.tool.box.module.tab;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.module.ServiceProviderUtils.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Urls.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.ColorValue;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.NumberTriplet;
import astro.tool.box.container.StringPair;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaDR2CatalogEntry;
import astro.tool.box.container.catalog.GenericCatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.container.lookup.BrownDwarfLookupEntry;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
import astro.tool.box.enumeration.Epoch;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.Unit;
import astro.tool.box.enumeration.WiseBand;
import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.module.Application;
import astro.tool.box.module.Circle;
import astro.tool.box.module.FlipbookComponent;
import astro.tool.box.module.Arrow;
import astro.tool.box.module.CustomOverlay;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.SpectralTypeLookupService;
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
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
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
    public static final double SIZE_FACTOR = 2.75;
    public static final int NUMBER_OF_EPOCHS = 6;
    public static final int WINDOW_SPACING = 25;
    public static final int MIN_VALUE = -5000;
    public static final int MAX_VALUE = 5000;
    public static final int STRETCH = 100;
    public static final int SPEED = 300;
    public static final int ZOOM = 500;
    public static final int SIZE = 500;

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CustomOverlaysTab customOverlaysTab;

    private final CatalogQueryFacade catalogQueryFacade;
    private final SpectralTypeLookupService mainSequenceSpectralTypeLookupService;
    private final SpectralTypeLookupService brownDwarfsSpectralTypeLookupService;
    private List<CatalogEntry> simbadEntries;
    private List<CatalogEntry> gaiaDR2Entries;
    private List<CatalogEntry> allWiseEntries;
    private List<CatalogEntry> catWiseEntries;

    private JPanel imagePanel;
    private JCheckBox minMaxLimits;
    private JCheckBox stretchImage;
    private JCheckBox invertColors;
    private JCheckBox borderEpoch;
    private JCheckBox staticDisplay;
    private JCheckBox simbadOverlay;
    private JCheckBox gaiaDR2Overlay;
    private JCheckBox allWiseOverlay;
    private JCheckBox catWiseOverlay;
    private JCheckBox gaiaDR2ProperMotion;
    private JCheckBox catWiseProperMotion;
    private JCheckBox useCustomOverlays;
    private JCheckBox useCoverageMaps;
    private JCheckBox skipBadCoadds;
    private JCheckBox smallBodyHelp;
    private JCheckBox hideMagnifier;
    private JCheckBox drawCircle;
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
    private JTextField properMotionField;
    private Timer timer;

    private BufferedImage wiseImage;
    private BufferedImage ps1Image;
    private Map<String, Fits> images;
    private Map<String, CustomOverlay> customOverlays;
    private List<NumberPair> circles;
    private FlipbookComponent[] flipbook;
    private ImageViewerTab imageViewer;

    private WiseBand wiseBand = WISE_BAND;
    private Epoch epoch = EPOCH;
    private int fieldOfView = 15;
    private int circleSize = 10;
    private int imageNumber = 0;
    private int windowShift = 0;
    private int quadrantCount = 0;
    private int epochCount = NUMBER_OF_EPOCHS * 2;
    private int stretch = STRETCH;
    private int speed = SPEED;
    private int zoom = ZOOM;
    private int size = SIZE;

    private int highContrast;
    private int lowContrast;

    private int minValue;
    private int maxValue;

    private double targetRa;
    private double targetDec;

    private double pixelX;
    private double pixelY;

    private int centerX;
    private int centerY;

    private int previousSize;
    private double previousRa;
    private double previousDec;

    private boolean imageCutOff;
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

            int controlPanelWidth = 240;
            int controlPanelHeight = 1175;

            JPanel controlPanel = new JPanel(new GridLayout(48, 1));
            controlPanel.setPreferredSize(new Dimension(controlPanelWidth - 20, controlPanelHeight));
            controlPanel.setBorder(new EmptyBorder(0, 5, 0, 10));

            JScrollPane controlScrollPanel = new JScrollPane(controlPanel);
            controlScrollPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));
            controlScrollPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            leftPanel.add(controlScrollPanel);

            controlPanel.add(new JLabel("Coordinates:"));

            coordsField = createField("133.787 -7.245150", PLAIN_FONT);
            controlPanel.add(coordsField);
            coordsField.addActionListener((ActionEvent evt) -> {
                coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                createFlipbook();
                coordsField.setCursor(Cursor.getDefaultCursor());
            });

            controlPanel.add(new JLabel("Field of view (arcsec):"));

            sizeField = createField(String.valueOf(size), PLAIN_FONT);
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
                initMinMaxValues();
                createFlipbook();
                wiseBands.setCursor(Cursor.getDefaultCursor());
            });

            controlPanel.add(new JLabel("Epochs:"));

            epochs = new JComboBox<>(Epoch.values());
            controlPanel.add(epochs);
            epochs.setSelectedItem(epoch);
            epochs.addActionListener((ActionEvent evt) -> {
                epochs.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (epochs.getSelectedItem().equals(Epoch.ALL)) {
                    smallBodyHelp.setEnabled(true);
                } else {
                    smallBodyHelp.setSelected(false);
                    smallBodyHelp.setEnabled(false);
                }
                initMinMaxValues();
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
                createFlipbook();
            });

            stretchImage = new JCheckBox("Apply image stretching", true);
            controlPanel.add(stretchImage);

            invertColors = new JCheckBox("Invert colors");
            controlPanel.add(invertColors);

            borderEpoch = new JCheckBox("Border first epoch");
            controlPanel.add(borderEpoch);

            staticDisplay = new JCheckBox("Static display");
            controlPanel.add(staticDisplay);
            staticDisplay.addActionListener((ActionEvent evt) -> {
                if (staticDisplay.isSelected()) {
                    createStaticBook();
                } else {
                    createFlipbook();
                }
            });

            JButton resetDefaultsButton = new JButton("Image processing defaults");
            controlPanel.add(resetDefaultsButton);
            resetDefaultsButton.addActionListener((ActionEvent evt) -> {
                minMaxLimits.setSelected(true);
                stretchImage.setSelected(true);
                stretchSlider.setValue(stretch = STRETCH);
                setContrast(getContrast());
                initMinMaxValues();
                createFlipbook();
            });

            controlPanel.add(new JLabel(underLine("Overlays:")));

            JPanel overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            simbadOverlay = new JCheckBox("Simbad");
            simbadOverlay.setForeground(Color.RED);
            overlayPanel.add(simbadOverlay);
            gaiaDR2Overlay = new JCheckBox("Gaia DR2");
            gaiaDR2Overlay.setForeground(Color.CYAN.darker());
            overlayPanel.add(gaiaDR2Overlay);

            overlayPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(overlayPanel);
            allWiseOverlay = new JCheckBox("AllWise");
            allWiseOverlay.setForeground(Color.GREEN.darker());
            overlayPanel.add(allWiseOverlay);
            catWiseOverlay = new JCheckBox("CatWise");
            catWiseOverlay.setForeground(Color.MAGENTA);
            overlayPanel.add(catWiseOverlay);

            controlPanel.add(new JLabel(underLine("PM vectors:")));

            JPanel properMotionPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(properMotionPanel);
            gaiaDR2ProperMotion = new JCheckBox("Gaia DR2");
            gaiaDR2ProperMotion.setForeground(Color.CYAN.darker());
            properMotionPanel.add(gaiaDR2ProperMotion);
            catWiseProperMotion = new JCheckBox("CatWise");
            catWiseProperMotion.setForeground(Color.MAGENTA);
            properMotionPanel.add(catWiseProperMotion);

            properMotionPanel = new JPanel(new GridLayout(1, 2));
            controlPanel.add(properMotionPanel);
            properMotionPanel.add(new JLabel("Total PM (mas/yr) >"));
            properMotionField = createField(100, PLAIN_FONT);
            properMotionPanel.add(properMotionField);

            controlPanel.add(new JLabel(underLine("Advanced controls:")));

            useCoverageMaps = new JCheckBox("Use coverage maps");
            controlPanel.add(useCoverageMaps);
            useCoverageMaps.addActionListener((ActionEvent evt) -> {
                useCoverageMaps.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                images.clear();
                setContrast(getContrast());
                initMinMaxValues();
                createFlipbook();
                useCoverageMaps.setCursor(Cursor.getDefaultCursor());
            });

            skipBadCoadds = new JCheckBox("Skip low weighted coadds");
            controlPanel.add(skipBadCoadds);
            skipBadCoadds.addActionListener((ActionEvent evt) -> {
                skipBadCoadds.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                images.clear();
                initMinMaxValues();
                createFlipbook();
                skipBadCoadds.setCursor(Cursor.getDefaultCursor());
            });

            smallBodyHelp = new JCheckBox("Show small body help (Epochs: ALL)");
            controlPanel.add(smallBodyHelp);
            smallBodyHelp.setEnabled(false);

            hideMagnifier = new JCheckBox("Hide magnifier panel");
            controlPanel.add(hideMagnifier);
            hideMagnifier.addActionListener((ActionEvent evt) -> {
                if (hideMagnifier.isSelected()) {
                    rightPanel.setVisible(false);
                } else {
                    rightPanel.setVisible(true);
                }
            });

            drawCircle = new JCheckBox("Draw circle (mouse wheel click & spin)");
            controlPanel.add(drawCircle);

            controlPanel.add(new JLabel(underLine("Image player controls:")));

            JPanel timerControls = new JPanel(new GridLayout(1, 2));
            controlPanel.add(timerControls);

            JButton playButton = new JButton("Play");
            timerControls.add(playButton);
            playButton.addActionListener((ActionEvent evt) -> {
                timer.setRepeats(true);
                timer.start();
                timerStopped = false;
            });

            JButton stopButton = new JButton("Stop");
            timerControls.add(stopButton);
            stopButton.addActionListener((ActionEvent evt) -> {
                timer.stop();
                timerStopped = true;
            });

            timerControls = new JPanel(new GridLayout(1, 2));
            controlPanel.add(timerControls);

            JButton backwardButton = new JButton("Backward");
            timerControls.add(backwardButton);
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
            timerControls.add(forwardButton);
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

            JButton saveAsButton = new JButton("Save image");
            controlPanel.add(saveAsButton);
            saveAsButton.addActionListener((ActionEvent evt) -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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

            useCustomOverlays = new JCheckBox("Works with custom overlays");
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
                            controlPanel.remove(customOverlay.getCheckBox());
                            controlPanel.updateUI();
                        });
                    }
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
                    imagePanel.setBorder(createEtchedBorder(component.getTitle(), PLAIN_FONT));

                    // Create and display WISE images
                    if (wiseBand.equals(WiseBand.W1W2)) {
                        wiseImage = createComposite(component.getEpoch());
                    } else {
                        wiseImage = createImage(component.getBand(), component.getEpoch());
                    }
                    wiseImage = flipVertically(wiseImage);
                    wiseImage = zoom(wiseImage, zoom);

                    addOverlaysAndPMVectors(wiseImage);
                    wiseImage = rotate(wiseImage, quadrantCount);

                    if (drawCircle.isSelected()) {
                        for (NumberPair circle : circles) {
                            drawCircle(wiseImage, (int) round(circle.getX() * zoom), (int) round(circle.getY() * zoom), circleSize * 2, Color.RED);
                        }
                    }

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
                    if (centerX == 0 && centerY == 0) {
                        //centerX = (int) round(getScaledValue(pixelX));
                        //centerY = (int) round(getScaledValue(pixelY));
                        centerX = wiseImage.getWidth() / 2;
                        centerY = wiseImage.getHeight() / 2;
                    }
                    int imageWidth = wiseImage.getWidth();
                    int imageHeight = wiseImage.getHeight();
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
                        BufferedImage magnifiedWiseImage = wiseImage.getSubimage(upperLeftX, upperLeftY, width, height);
                        magnifiedWiseImage = zoom(magnifiedWiseImage, 200);
                        rightPanel.add(new JLabel(new ImageIcon(magnifiedWiseImage)));
                    }

                    // Display PanSTARRS images
                    JLabel ps1Label = null;
                    if (ps1Image != null) {
                        BufferedImage processedPs1Image = zoom(rotate(ps1Image, quadrantCount), zoom);

                        // Create and display magnified PanSTARRS image
                        if (!hideMagnifier.isSelected()) {
                            BufferedImage magnifiedPs1Image = processedPs1Image.getSubimage(upperLeftX, upperLeftY, width, height);
                            magnifiedPs1Image = zoom(magnifiedPs1Image, 200);
                            rightPanel.add(new JLabel(new ImageIcon(magnifiedPs1Image)));
                        }

                        // Display regular PanSTARRS image
                        ps1Label = new JLabel(new ImageIcon(processedPs1Image));
                        ps1Label.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                        imagePanel.add(ps1Label);
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
                                double theta = Math.toRadians(angle);
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
                                    if (drawCircle.isSelected()) {
                                        double circleX = evt.getX() * 1.0 / zoom;
                                        double circleY = evt.getY() * 1.0 / zoom;
                                        circles.add(new NumberPair(circleX, circleY));
                                    } else {
                                        displayZoomedPs1Image(newRa, newDec, fieldOfView);
                                    }
                                    break;
                                default:
                                    if (smallBodyHelp.isSelected()) {
                                        displaySmallBodyPanel(newRa, newDec, component.getMinObsEpoch(), component.getMaxObsEpoch());
                                    } else {
                                        int overlays = 0;
                                        if (simbadOverlay.isSelected() && simbadEntries != null) {
                                            showCatalogInfo(simbadEntries, mouseX, mouseY);
                                            overlays++;
                                        }
                                        if (gaiaDR2Overlay.isSelected() && gaiaDR2Entries != null) {
                                            showCatalogInfo(gaiaDR2Entries, mouseX, mouseY);
                                            overlays++;
                                        }
                                        if (allWiseOverlay.isSelected() && allWiseEntries != null) {
                                            showCatalogInfo(allWiseEntries, mouseX, mouseY);
                                            overlays++;
                                        }
                                        if (catWiseOverlay.isSelected() && catWiseEntries != null) {
                                            showCatalogInfo(catWiseEntries, mouseX, mouseY);
                                            overlays++;
                                        }
                                        if (overlays == 0) {
                                            displayCatalogSearchResults(newRa, newDec);
                                        }
                                    }
                                    //coordsField.setText(roundTo7DecNZ(newRa) + " " + roundTo7DecNZ(newDec));
                                    //createFlipbook();
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
                        if (drawCircle.isSelected()) {
                            if (notches < 0) {
                                circleSize++;
                            } else if (circleSize > 0) {
                                circleSize--;
                            }
                        } else {
                            if (notches < 0) {
                                fieldOfView++;
                            } else if (fieldOfView > 0) {
                                fieldOfView--;
                            }
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
        double posX = targetRa + diffX / cos(toRadians(targetDec));
        double posY = targetDec + diffY;
        return new NumberPair(posX, posY);
    }

    private NumberPair getPixelCoordinates(double ra, double dec) {
        double diffX = (targetRa - ra) * cos(toRadians(targetDec));
        double diffY = targetDec - dec;
        double conversionFactor = getConversionFactor();
        diffX /= -conversionFactor;
        diffY /= -conversionFactor;
        double posX = getScaledValue(pixelX) - (diffX);
        double posY = getScaledValue(pixelY) - (diffY);
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
            NumberPair coordinates = getCoordinates(coords);
            List<String> errorMessages = new ArrayList<>();
            try {
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

            wiseBand = (WiseBand) wiseBands.getSelectedItem();
            epoch = (Epoch) epochs.getSelectedItem();

            if (size != previousSize || targetRa != previousRa || targetDec != previousDec) {
                images = new HashMap<>();
                circles = new ArrayList<>();
                hasException = false;
                setContrast(getContrast());
                initMinMaxValues();
                centerX = centerY = 0;
                windowShift = 0;
                imageCutOff = false;
                simbadOverlay.setEnabled(true);
                gaiaDR2Overlay.setEnabled(true);
                allWiseOverlay.setEnabled(true);
                catWiseOverlay.setEnabled(true);
                gaiaDR2ProperMotion.setEnabled(true);
                catWiseProperMotion.setEnabled(true);
                simbadEntries = gaiaDR2Entries = allWiseEntries = catWiseEntries = null;
                customOverlays.values().forEach((customOverlay) -> {
                    customOverlay.setCatalogEntries(null);
                });
                ps1Image = fetchPs1Image(targetRa, targetDec, size, 1024);
            }
            previousSize = size;
            previousRa = targetRa;
            previousDec = targetDec;
            imageNumber = 0;

            switch (epoch) {
                case ALL:
                    flipbook = new FlipbookComponent[epochCount];

                    for (int i = 0; i < epochCount; i++) {
                        NumberPair obsEpochs = loadImage(wiseBand.val, i);
                        flipbook[i] = new FlipbookComponent(wiseBand.val, i, obsEpochs.getX(), obsEpochs.getY());
                    }

                    break;
                case ALL_ASCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];

                    for (int i = 0; i < epochCount; i += 2) {
                        NumberPair obsEpochs = loadImage(wiseBand.val, i);
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i, obsEpochs.getX(), obsEpochs.getY());
                    }

                    break;
                case ALL_DESCENDING:
                    flipbook = new FlipbookComponent[epochCount / 2];

                    for (int i = 1; i < epochCount; i += 2) {
                        NumberPair obsEpochs = loadImage(wiseBand.val, i);
                        flipbook[i / 2] = new FlipbookComponent(wiseBand.val, i, obsEpochs.getX(), obsEpochs.getY());
                    }

                    break;
                case YEAR:
                    Fits fits;
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
                case FIRST_REMAINING_PARALLAX:
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
        JPanel grid = new JPanel(new GridLayout(3, 5));
        for (FlipbookComponent component : flipbook) {
            component.setEpochCount(epochCount / 2);
            BufferedImage image;
            if (wiseBand.equals(WiseBand.W1W2)) {
                image = createComposite(component.getEpoch());
            } else {
                image = createImage(component.getBand(), component.getEpoch());
            }
            image = flipVertically(image);
            image = zoom(image, zoom);

            addOverlaysAndPMVectors(image);
            image = rotate(image, quadrantCount);

            if (drawCircle.isSelected()) {
                for (NumberPair circle : circles) {
                    drawCircle(image, (int) round(circle.getX() * zoom), (int) round(circle.getY() * zoom), circleSize * 2, Color.RED);
                }
            }

            Graphics graphics = image.getGraphics();
            Circle circle = new Circle(getScaledValue(pixelX), getScaledValue(pixelY), 10 + zoom / 10, Color.BLACK);
            circle.draw(graphics);

            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(image)));
            pane.setBorder(createEtchedBorder(component.getTitle(), PLAIN_FONT));
            grid.add(pane);
        }
        if (ps1Image != null) {
            JScrollPane pane = new JScrollPane(new JLabel(new ImageIcon(zoom(rotate(ps1Image, quadrantCount), zoom))));
            pane.setBorder(createEtchedBorder("PanSTARRS-1 stack y/i/g", PLAIN_FONT));
            grid.add(pane);
        }
        imagePanel.removeAll();
        imagePanel.setBorder(createEmptyBorder("", PLAIN_FONT));
        imagePanel.add(grid);
        baseFrame.setVisible(true);
    }

    private void addOverlaysAndPMVectors(BufferedImage image) {
        if (simbadOverlay.isSelected()) {
            fetchSimbadCatalogEntries();
            drawOverlay(image, simbadEntries, Color.RED);
        }
        if (gaiaDR2Overlay.isSelected()) {
            fetchGaiaDR2CatalogEntries();
            drawOverlay(image, gaiaDR2Entries, Color.CYAN.darker());
        }
        if (allWiseOverlay.isSelected()) {
            fetchAllWiseCatalogEntries();
            drawOverlay(image, allWiseEntries, Color.GREEN.darker());
        }
        if (catWiseOverlay.isSelected()) {
            fetchCatWiseCatalogEntries();
            drawOverlay(image, catWiseEntries, Color.MAGENTA);
        }

        if (useCustomOverlays.isSelected()) {
            customOverlays.values().forEach((customOverlay) -> {
                if (customOverlay.getCheckBox().isSelected()) {
                    fetchGenericCatalogEntries(customOverlay);
                    drawOverlay(image, customOverlay.getCatalogEntries(), customOverlay.getColor());
                }
            });
        }

        if (gaiaDR2ProperMotion.isSelected()) {
            fetchGaiaDR2CatalogEntries();
            drawPMVectors(image, gaiaDR2Entries, Color.CYAN.darker());
        }
        if (catWiseProperMotion.isSelected()) {
            fetchCatWiseCatalogEntries();
            drawPMVectors(image, catWiseEntries, Color.MAGENTA);
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
            } catch (Exception ex) {
                fits = getPreviousImage(band, epoch);
            }

            ImageHDU hdu = (ImageHDU) fits.getHDU(0);
            Header header = hdu.getHeader();
            double naxis1 = header.getDoubleValue("NAXIS1");
            double naxis2 = header.getDoubleValue("NAXIS2");
            if (naxis1 != naxis2 && !imageCutOff) {
                String message = "Image has been cut off. No centering possible. Overlays deactivated. Choose a smaller field of view!";
                showInfoDialog(baseFrame, message);
                imageCutOff = true;
                simbadOverlay.setSelected(false);
                gaiaDR2Overlay.setSelected(false);
                allWiseOverlay.setSelected(false);
                catWiseOverlay.setSelected(false);
                gaiaDR2ProperMotion.setSelected(false);
                catWiseProperMotion.setSelected(false);
                simbadOverlay.setEnabled(false);
                gaiaDR2Overlay.setEnabled(false);
                allWiseOverlay.setEnabled(false);
                catWiseOverlay.setEnabled(false);
                gaiaDR2ProperMotion.setEnabled(false);
                catWiseProperMotion.setEnabled(false);
            }
            double crpix1 = header.getDoubleValue("CRPIX1");
            double crpix2 = header.getDoubleValue("CRPIX2");
            pixelX = crpix1;
            pixelY = naxis2 - crpix2;

            addImage(band, epoch, useCoverageMaps.isSelected() ? applyWeights(fits) : fits);
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
            int previousEpoch = epoch - 1;
            fits = getImage(band, previousEpoch);
            if (fits == null) {
                fits = new Fits(getImageData(band, previousEpoch));
            }
        } catch (Exception ex) {
            float[][] values = new float[size][size];
            short[][] weights = new short[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
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
        HttpURLConnection connection = establishHttpConnection(createImageUrl(band, epoch));
        return connection.getInputStream();
    }

    private String createImageUrl(int band, int epoch) throws MalformedURLException {
        return WISE_VIEW_URL + "?ra=" + targetRa + "&dec=" + targetDec + "&size=" + size + "&band=" + band + "&epoch=" + epoch + (useCoverageMaps.isSelected() || skipBadCoadds.isSelected() ? "&covmap=true" : "");
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

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    try {
                        float value = processPixel(values[i][j]);
                        graphics.setColor(new Color(value, value, value));
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        graphics.setColor(new Color(1f, 1f, 1f));
                    }
                    graphics.fillRect(j, i, 1, 1);
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

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    try {
                        float red = processPixel(valuesW1[i][j]);
                        float blue = processPixel(valuesW2[i][j]);
                        float green = (red + blue) / 2;
                        graphics.setColor(new Color(red, green, blue));
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        graphics.setColor(new Color(1f, 1f, 1f));
                    }
                    graphics.fillRect(j, i, 1, 1);
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

            float[][] addedValues = new float[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
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

            float[][] subtractedValues = new float[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
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

            float[][] averagedValues = new float[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
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

    private Fits applyWeights(Fits fits) {
        try {
            ImageHDU imageHDU = (ImageHDU) fits.getHDU(0);
            ImageData imageData = (ImageData) imageHDU.getData();
            float[][] values = (float[][]) imageData.getData();

            imageHDU = (ImageHDU) fits.getHDU(1);
            imageData = (ImageData) imageHDU.getData();
            short[][] weights = (short[][]) imageData.getData();

            float[][] weightedValues = new float[size][size];
            short[][] refactoredWeights = new short[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    try {
                        weightedValues[i][j] = values[i][j] * weights[i][j];
                        refactoredWeights[i][j] = 1;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                    }
                }
            }

            Fits result = new Fits();
            result.addHDU(FitsFactory.hduFactory(weightedValues));
            result.addHDU(FitsFactory.hduFactory(refactoredWeights));
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

    private BufferedImage flipVertically(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    public BufferedImage rotate(BufferedImage image, int numberOfQuadrants) {
        AffineTransform tx = AffineTransform.getQuadrantRotateInstance(numberOfQuadrants, image.getWidth() / 2, image.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

    private BufferedImage zoom(BufferedImage image, int zoom) {
        zoom = zoom == 0 ? 1 : zoom;
        Image pic = image.getScaledInstance(zoom, zoom, Image.SCALE_DEFAULT);
        image = new BufferedImage(zoom, zoom, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(pic, 0, 0, null);
        g2d.dispose();
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

    private float normalize(float value, int minVal, int maxVal) {
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
        return (float) Math.log(x + Math.sqrt(x * x + 1.0));
    }

    private int getContrast() {
        return size < 30 || useCoverageMaps.isSelected() ? 25 : 50;
    }

    private void setContrast(int contrast) {
        lowScaleSlider.setValue(lowContrast = contrast);
        highScaleSlider.setValue(highContrast = 0);
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
        if (epoch.equals(Epoch.FIRST_LAST_SUBTRACTED) || epoch.equals(Epoch.FIRST_REMAINING_SUBTRACTED)) {
            presetMinVal = -avgVal * size / 10;
            presetMinVal = presetMinVal < minVal ? minVal : presetMinVal;
            presetMaxVal = maxVal;
        } else {
            presetMinVal = minVal <= MIN_VALUE ? -avgVal : minVal;
            presetMaxVal = avgVal * size;
            presetMaxVal = presetMaxVal > maxVal ? maxVal : presetMaxVal;
        }

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
        imageViewerTab.getSizeField().setText(sizeField.getText());
        imageViewerTab.getWiseBands().setSelectedItem(wiseBand);
        imageViewerTab.getEpochs().setSelectedItem(epoch);
        imageViewerTab.setQuadrantCount(quadrantCount);
        imageViewerTab.setImageViewer(this);
        imageViewerTab.createFlipbook();

        baseFrame.setCursor(Cursor.getDefaultCursor());
    }

    private void displayZoomedPs1Image(double targetRa, double targetDec, int size) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        BufferedImage ps1ImageZoomed = fetchPs1Image(targetRa, targetDec, size, 256);
        if (ps1ImageZoomed != null) {
            JFrame imageFrame = new JFrame();
            imageFrame.setIconImage(getToolBoxImage());
            imageFrame.setTitle("Target: " + roundTo2DecNZ(targetRa) + " " + roundTo2DecNZ(targetDec) + " FoV: " + size + "\"");
            imageFrame.getContentPane().add(new JLabel(new ImageIcon(ps1ImageZoomed)));
            imageFrame.setSize(350, 350);
            imageFrame.setAlwaysOnTop(true);
            imageFrame.setResizable(false);
            imageFrame.setVisible(true);
        }
        baseFrame.setCursor(Cursor.getDefaultCursor());
    }

    private BufferedImage fetchPs1Image(double targetRa, double targetDec, int size, int resolution) {
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
            imageUrl = String.format("http://ps1images.stsci.edu/cgi-bin/fitscut.cgi?red=%s&green=%s&blue=%s&x=%f&y=%f&size=%d&wcs=1&asinh=true&autoscale=98.00&output_size=%d", fileNames.get(2), fileNames.get(1), fileNames.get(0), targetRa, targetDec, (int) (size * SIZE_FACTOR * 4), resolution);
            HttpURLConnection connection = establishHttpConnection(imageUrl);
            BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
            return ImageIO.read(stream);
        } catch (Exception ex) {
            return null;
        }
    }

    private void fetchSimbadCatalogEntries() {
        try {
            if (simbadEntries == null) {
                baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                CatalogEntry catalogQuery = new SimbadCatalogEntry();
                catalogQuery.setRa(targetRa);
                catalogQuery.setDec(targetDec);
                catalogQuery.setSearchRadius(getFovDiagonal() / 2);
                simbadEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
                simbadEntries.forEach(catalogEntry -> {
                    catalogEntry.setTargetRa(targetRa);
                    catalogEntry.setTargetDec(targetDec);
                    catalogEntry.loadCatalogElements();
                });
            }
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void fetchGaiaDR2CatalogEntries() {
        try {
            if (gaiaDR2Entries == null) {
                baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                CatalogEntry catalogQuery = new GaiaDR2CatalogEntry();
                catalogQuery.setRa(targetRa);
                catalogQuery.setDec(targetDec);
                catalogQuery.setSearchRadius(getFovDiagonal() / 2);
                gaiaDR2Entries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
                gaiaDR2Entries.forEach(catalogEntry -> {
                    catalogEntry.setTargetRa(targetRa);
                    catalogEntry.setTargetDec(targetDec);
                    catalogEntry.loadCatalogElements();
                });
            }
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void fetchAllWiseCatalogEntries() {
        try {
            if (allWiseEntries == null) {
                baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                CatalogEntry catalogQuery = new AllWiseCatalogEntry();
                catalogQuery.setRa(targetRa);
                catalogQuery.setDec(targetDec);
                catalogQuery.setSearchRadius(getFovDiagonal() / 2);
                allWiseEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
                allWiseEntries.forEach(catalogEntry -> {
                    catalogEntry.setTargetRa(targetRa);
                    catalogEntry.setTargetDec(targetDec);
                    catalogEntry.loadCatalogElements();
                });
            }
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void fetchCatWiseCatalogEntries() {
        try {
            if (catWiseEntries == null) {
                baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                CatalogEntry catalogQuery = new CatWiseCatalogEntry();
                catalogQuery.setRa(targetRa);
                catalogQuery.setDec(targetDec);
                catalogQuery.setSearchRadius(getFovDiagonal() / 2);
                catWiseEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
                catWiseEntries.forEach(catalogEntry -> {
                    catalogEntry.setTargetRa(targetRa);
                    catalogEntry.setTargetDec(targetDec);
                    catalogEntry.loadCatalogElements();
                });
            }
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void fetchGenericCatalogEntries(CustomOverlay customOverlay) {
        List<CatalogEntry> catalogEntries = customOverlay.getCatalogEntries();
        if (catalogEntries == null) {
            baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            catalogEntries = new ArrayList<>();
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
                    return;
                }
                while (scanner.hasNextLine()) {
                    String[] columnValues = scanner.nextLine().split(SPLIT_CHAR, numberOfColumns);
                    CatalogEntry catalogEntry = new GenericCatalogEntry(columnNames, columnValues);
                    catalogEntry.setRa(toDouble(columnValues[raColumnIndex]));
                    catalogEntry.setDec(toDouble(columnValues[decColumnIndex]));
                    double radius = convertToUnit(getFovDiagonal() / 2, Unit.ARCSEC, Unit.DEGREE);
                    System.out.println(catalogEntry);
                    if (catalogEntry.getRa() > targetRa - radius && catalogEntry.getRa() < targetRa + radius
                            && catalogEntry.getDec() > targetDec - radius && catalogEntry.getDec() < targetDec + radius) {
                        System.out.println("----------------->" + catalogEntry);
                        catalogEntry.setTargetRa(targetRa);
                        catalogEntry.setTargetDec(targetDec);
                        catalogEntry.loadCatalogElements();
                        catalogEntries.add(catalogEntry);
                        break;
                    }
                }
            } catch (Exception ex) {
                showExceptionDialog(baseFrame, ex);
            } finally {
                customOverlay.setCatalogEntries(catalogEntries);
                baseFrame.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void drawOverlay(BufferedImage image, List<CatalogEntry> catalogEntries, Color color) {
        Graphics graphics = image.getGraphics();
        catalogEntries.forEach(catalogEntry -> {
            NumberPair position = getPixelCoordinates(catalogEntry.getRa(), catalogEntry.getDec());
            catalogEntry.setPixelRa(position.getX());
            catalogEntry.setPixelDec(position.getY());
            Circle circle = new Circle(position.getX(), position.getY(), getOverlaySize(), color);
            circle.draw(graphics);
        });
    }

    private void drawPMVectors(BufferedImage image, List<CatalogEntry> catalogEntries, Color color) {
        Graphics graphics = image.getGraphics();
        catalogEntries.forEach(catalogEntry -> {
            double pmRa = catalogEntry.getPmra();
            double pmDec = catalogEntry.getPmdec();

            double tpm = calculateTotalProperMotion(pmRa, pmDec);
            double pmLimit = toDouble(properMotionField.getText());

            if (tpm > pmLimit) {
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

                NumberPair position = getPixelCoordinates(ra, dec);
                double x = position.getX();
                double y = position.getY();

                numberOfYears = (epochCount / 2) + 3; // 3 -> 2011, 2012 & 2013
                double newRa = ra + (numberOfYears * pmRa / DEG_MAS) / cos(toRadians(dec));
                double newDec = dec + numberOfYears * pmDec / DEG_MAS;

                position = getPixelCoordinates(newRa, newDec);
                double newX = position.getX();
                double newY = position.getY();

                Arrow arrow = new Arrow(x, y, newX, newY, getOverlaySize(), color);
                arrow.draw(graphics);
            }
        });
    }

    private void drawCircle(BufferedImage image, int x, int y, int size, Color color) {
        Graphics graphics = image.getGraphics();
        Circle circle = new Circle(x, y, size, color);
        circle.draw(graphics);
    }

    private void showCatalogInfo(List<CatalogEntry> catalogEntries, int x, int y) {
        catalogEntries.forEach(catalogEntry -> {
            double radius = getOverlaySize() / 2;
            if (catalogEntry.getPixelRa() > x - radius && catalogEntry.getPixelRa() < x + radius
                    && catalogEntry.getPixelDec() > y - radius && catalogEntry.getPixelDec() < y + radius) {
                displayCatalogPanel(catalogEntry);
            }
        });
    }

    private void displayCatalogPanel(CatalogEntry catalogEntry) {
        int maxRows = 19;
        JPanel detailPanel = new JPanel(new GridLayout(maxRows, 4));
        detailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Catalog entry (computed values are shown in green)", TitledBorder.LEFT, TitledBorder.TOP
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
        container.add(detailPanel);
        container.add(createMainSequenceSpectralTypePanel(catalogEntry));
        if (catalogEntry instanceof AllWiseCatalogEntry) {
            AllWiseCatalogEntry entry = (AllWiseCatalogEntry) catalogEntry;
            if (isAPossibleAgn(entry.getW1_W2(), entry.getW2_W3())) {
                String warning = "W2-W3=" + roundTo3DecNZ(entry.getW2_W3()) + " (> 2.5) " + AGN_WARNING;
                container.add(createLabel(warning, PLAIN_FONT, JColor.DARK_RED.val));
            }
        }
        container.add(createBrownDwarfsSpectralTypePanel(catalogEntry));

        JFrame catalogFrame = new JFrame();
        catalogFrame.setIconImage(getToolBoxImage());
        catalogFrame.setTitle(catalogEntry.getCatalogName());
        catalogFrame.add(container);
        catalogFrame.setSize(650, 550);
        catalogFrame.setLocation(windowShift, windowShift);
        catalogFrame.setAlwaysOnTop(true);
        catalogFrame.setResizable(false);
        catalogFrame.setVisible(true);
        windowShift += 10;
    }

    private JScrollPane createMainSequenceSpectralTypePanel(CatalogEntry catalogEntry) {
        try {
            Map<SpectralTypeLookupResult, Set<ColorValue>> results = mainSequenceSpectralTypeLookupService.lookup(catalogEntry.getColors());

            List<Object[]> spectralTypes = new ArrayList<>();
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
                String spectralType = key.getSpt() + "," + key.getTeff() + "," + key.getRsun() + "," + key.getMsun() + "," + matchedColors;
                spectralTypes.add(spectralType.split(",", 5));
            });

            String titles = "spt,teff,sol rad,sol mass,matched colors";
            String[] columns = titles.split(",", 5);
            Object[][] rows = new Object[][]{};
            JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };
            spectralTypeTable.setAutoCreateRowSorter(true);
            spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            spectralTypeTable.setCellSelectionEnabled(false);
            resizeColumnWidth(spectralTypeTable);

            JScrollPane spectralTypePanel = spectralTypes.isEmpty()
                    ? new JScrollPane(createLabel("No colors available / No match", PLAIN_FONT, JColor.DARK_RED.val))
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

            List<Object[]> spectralTypes = new ArrayList<>();
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
                String spectralType = key.getSpt() + "," + matchedColors;
                spectralTypes.add(spectralType.split(",", 2));
            });

            String titles = "spt,matched colors";
            String[] columns = titles.split(",", 2);
            Object[][] rows = new Object[][]{};
            JTable spectralTypeTable = new JTable(spectralTypes.toArray(rows), columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };
            spectralTypeTable.setAutoCreateRowSorter(true);
            spectralTypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            spectralTypeTable.setCellSelectionEnabled(false);
            resizeColumnWidth(spectralTypeTable);

            JScrollPane spectralTypePanel = spectralTypes.isEmpty()
                    ? new JScrollPane(createLabel("No colors available / No match", PLAIN_FONT, JColor.DARK_RED.val))
                    : new JScrollPane(spectralTypeTable);
            spectralTypePanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "M-L-T-Y dwarfs spectral type evaluation", TitledBorder.LEFT, TitledBorder.TOP
            ));

            return spectralTypePanel;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void displaySmallBodyPanel(double targetRa, double targetDec, double minObsEpoch, double maxObsEpoch) {
        JPanel detailPanel = new JPanel(new GridLayout(10, 2));
        detailPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        StringPair sexagesimalCoords = convertToSexagesimalCoords(targetRa, targetDec);
        String objectRa = sexagesimalCoords.getS1().replace(" ", ":").split("\\.")[0];
        String objectDec = sexagesimalCoords.getS2().split("\\.")[0];

        detailPanel.add(createLabel("Min observation time (*): ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createField(convertMJDToDateTime(new BigDecimal(Double.toString(minObsEpoch))).format(DATE_TIME_FORMATTER) + " (" + minObsEpoch + ")", PLAIN_FONT));

        detailPanel.add(createLabel("Max observation time (*): ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createField(convertMJDToDateTime(new BigDecimal(Double.toString(maxObsEpoch))).format(DATE_TIME_FORMATTER) + " (" + maxObsEpoch + ")", PLAIN_FONT));

        detailPanel.add(createLabel("Some observatories in the North: ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createField("T05, T08, F51, F52, 675, 703, Wise", PLAIN_FONT));

        detailPanel.add(createLabel("Some observatories in the South: ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createField("413, Antofagasta, Arica, Johannesburg, Pretoria", PLAIN_FONT));

        detailPanel.add(createLabel("Center of the search region in RA: ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createField(objectRa + " (" + roundTo7DecNZ(targetRa) + ")", PLAIN_FONT));

        detailPanel.add(createLabel("Center of the search region in dec: ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createField(objectDec + " (" + roundTo7DecNZ(targetDec) + ")", PLAIN_FONT));

        detailPanel.add(createLabel("Width of search region in RA: ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createField("w0:05", PLAIN_FONT));

        detailPanel.add(createLabel("Width of search region in dec: ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createField("w0 05", PLAIN_FONT));

        detailPanel.add(createLabel("Visual magnitude limit: ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createField("25", PLAIN_FONT));

        detailPanel.add(createLabel("Link: ", PLAIN_FONT, JLabel.RIGHT));
        detailPanel.add(createHyperlink("JPL SB Identification", "https://ssd.jpl.nasa.gov/sbfind.cgi", PLAIN_FONT));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(detailPanel);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
        container.add(infoPanel);

        infoPanel.add(createLabel("(*) These are the observation times of the first and last single exposures that went into the coadd the", PLAIN_FONT));
        infoPanel.add(createLabel("small body is located in. You have to find the single exposure between these 2 dates in which the object", PLAIN_FONT));
        infoPanel.add(createLabel("shows up. Use the", PLAIN_FONT));
        infoPanel.add(createHyperlink("WISE image service", "https://irsa.ipac.caltech.edu/applications/wise", PLAIN_FONT));
        infoPanel.add(createLabel("to do so.", PLAIN_FONT));
        infoPanel.add(createLabel("Enter the observation time of that single exposure into JPL's SB Identification tool.", PLAIN_FONT));

        JFrame smallBodyFrame = new JFrame();
        smallBodyFrame.setIconImage(getToolBoxImage());
        smallBodyFrame.setTitle("Data to enter into JPL's Small Body Identification tool");
        smallBodyFrame.add(container);
        smallBodyFrame.setSize(600, 420);
        smallBodyFrame.setAlwaysOnTop(true);
        smallBodyFrame.setResizable(false);
        smallBodyFrame.setVisible(true);
    }

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
