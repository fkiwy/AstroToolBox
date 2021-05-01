package astro.tool.box.module.tab;

import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Epoch;
import astro.tool.box.enumeration.Shape;
import static astro.tool.box.function.NumericFunctions.roundTo7DecNZ;
import astro.tool.box.module.FlipbookComponent;
import astro.tool.box.module.shape.Circle;
import astro.tool.box.module.shape.Cross;
import astro.tool.box.module.shape.Drawable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class TimeSeriesTab {

    private static final String TAB_NAME = "Time Series";
    private static final int MAX_IMAGES = 6;

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final ImageViewerTab imageViewerTab;

    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    private JButton searchButton;
    private JTextField coordsField;
    private JTextField fovField;

    private double targetRa;
    private double targetDec;
    private int fieldOfView;

    public TimeSeriesTab(JFrame baseFrame, JTabbedPane tabbedPane, ImageViewerTab imageViewerTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.imageViewerTab = imageViewerTab;
    }

    public void init() {
        try {
            mainPanel = new JPanel(new BorderLayout());
            tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));

            topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            centerPanel = new JPanel(new GridLayout(0, 1));
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

            JLabel coordsLabel = new JLabel("Coordinates:");
            topPanel.add(coordsLabel);

            coordsField = new JTextField(25);
            topPanel.add(coordsField);

            JLabel fovLabel = new JLabel("Field of view (arcsec):");
            topPanel.add(fovLabel);

            fovField = new JTextField(5);
            topPanel.add(fovField);
            fovField.setText("30");

            searchButton = new JButton("Search");
            topPanel.add(searchButton);
            searchButton.addActionListener((ActionEvent e) -> {
                try {
                    baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    fovField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    if (centerPanel.getComponentCount() > 0) {
                        centerPanel.removeAll();
                    }
                    if (bottomPanel.getComponentCount() > 0) {
                        bottomPanel.removeAll();
                    }
                    String coords = coordsField.getText();
                    if (coords.isEmpty()) {
                        showErrorDialog(baseFrame, "Coordinates must not be empty!");
                        return;
                    }
                    String fov = fovField.getText();
                    if (fov.isEmpty()) {
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
                        fieldOfView = Integer.valueOf(fov);
                        if (fieldOfView > 300) {
                            errorMessages.add("Field of view must not be larger than 300 arcsec.");
                        }
                    } catch (Exception ex) {
                        fieldOfView = 0;
                        errorMessages.add("Invalid field of view!");
                    }
                    if (!errorMessages.isEmpty()) {
                        String message = String.join(LINE_SEP, errorMessages);
                        showErrorDialog(baseFrame, message);
                    } else {
                        displayDssImages(targetRa, targetDec, fieldOfView);
                        display2MassImages(targetRa, targetDec, fieldOfView);
                        displaySdssImages(targetRa, targetDec, fieldOfView);
                        displayAllwiseImages(targetRa, targetDec, fieldOfView);
                        displayPs1Images(targetRa, targetDec, fieldOfView);
                        displayDecalsImages(targetRa, targetDec, fieldOfView);
                        displayTimeSeries(targetRa, targetDec, fieldOfView);
                        displayWiseTimeSeries(targetRa, targetDec, fieldOfView);
                        baseFrame.setVisible(true);
                    }
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    baseFrame.setCursor(Cursor.getDefaultCursor());
                    coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    fovField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                }
            });

            coordsField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
            fovField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void displayDssImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new GridLayout(1, MAX_IMAGES));
        bandPanel.setBorder(createEmptyBorder("DSS"));

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

        if (bandPanel.getComponentCount() > 0) {
            addFillerPanel(bandPanel);
            centerPanel.add(bandPanel);
        }
    }

    private void display2MassImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new GridLayout(1, MAX_IMAGES));
        bandPanel.setBorder(createEmptyBorder("2MASS"));

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

        if (bandPanel.getComponentCount() > 0) {
            addFillerPanel(bandPanel);
            centerPanel.add(bandPanel);
        }
    }

    private void displaySdssImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new GridLayout(1, MAX_IMAGES));
        bandPanel.setBorder(createEmptyBorder("SDSS"));

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

        if (bandPanel.getComponentCount() > 0) {
            addFillerPanel(bandPanel);
            centerPanel.add(bandPanel);
        }
    }

    private void displayAllwiseImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new GridLayout(1, MAX_IMAGES));
        bandPanel.setBorder(createEmptyBorder("AllWISE"));

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

        if (bandPanel.getComponentCount() > 0) {
            addFillerPanel(bandPanel);
            centerPanel.add(bandPanel);
        }
    }

    private void displayPs1Images(double targetRa, double targetDec, int size) throws Exception {
        // Fetch file names for Pan-STARRS filters
        SortedMap<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
        if (imageInfos.isEmpty()) {
            return;
        }

        // Fetch images for Pan-STARRS filters
        JPanel bandPanel = new JPanel(new GridLayout(1, MAX_IMAGES));
        bandPanel.setBorder(createEmptyBorder("Pan-STARRS"));

        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("g")), targetRa, targetDec, size), "g"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("r")), targetRa, targetDec, size), "r"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("i")), targetRa, targetDec, size), "i"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size), "z"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("y")), targetRa, targetDec, size), "y"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s&green=%s&blue=%s", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g")), targetRa, targetDec, size), "y-i-g"));

        if (bandPanel.getComponentCount() > 0) {
            addFillerPanel(bandPanel);
            centerPanel.add(bandPanel);
        }
    }

    private void displayDecalsImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new GridLayout(1, MAX_IMAGES));
        bandPanel.setBorder(createEmptyBorder("DECaLS"));

        BufferedImage image = retrieveDecalsImage(targetRa, targetDec, size, "g");
        if (image != null) {
            image = convertToGray(image);
            bandPanel.add(buildImagePanel(image, "g"));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "r");
        if (image != null) {
            image = convertToGray(image);
            bandPanel.add(buildImagePanel(image, "r"));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "z");
        if (image != null) {
            image = convertToGray(image);
            bandPanel.add(buildImagePanel(image, "z"));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "grz");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "g-r-z"));
        }

        if (bandPanel.getComponentCount() > 0) {
            addFillerPanel(bandPanel);
            centerPanel.add(bandPanel);
        }
    }

    private void displayTimeSeries(double targetRa, double targetDec, int size) throws Exception {
        JPanel bandPanel = new JPanel(new GridLayout(1, MAX_IMAGES));
        bandPanel.setBorder(createEmptyBorder("Time series"));

        BufferedImage image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DSS2 - IR", Shape.CROSS));
        }
        image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "2MASS - K", Shape.CROSS));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "SDSS - z", Shape.CROSS));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "WISE - W2", Shape.CROSS));
        }
        SortedMap<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
        if (!imageInfos.isEmpty()) {
            image = retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size);
            bandPanel.add(buildImagePanel(image, "PS1 - z", Shape.CROSS));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "z");
        if (image != null) {
            image = convertToGray(image);
            bandPanel.add(buildImagePanel(image, "DECaLS - z", Shape.CROSS));
        }

        if (bandPanel.getComponentCount() > 0) {
            addFillerPanel(bandPanel);
            centerPanel.add(bandPanel);
        }
    }

    private void displayWiseTimeSeries(double targetRa, double targetDec, int size) throws Exception {
        JTextField coordinateField = imageViewerTab.getCoordsField();
        ActionListener actionListener = coordinateField.getActionListeners()[0];
        coordinateField.removeActionListener(actionListener);
        coordinateField.setText(roundTo7DecNZ(targetRa) + " " + roundTo7DecNZ(targetDec));
        coordinateField.addActionListener(actionListener);
        JTextField sizeField = imageViewerTab.getSizeField();
        actionListener = sizeField.getActionListeners()[0];
        sizeField.removeActionListener(actionListener);
        sizeField.setText(String.valueOf(size));
        sizeField.addActionListener(actionListener);
        imageViewerTab.getZoomSlider().setValue(250);
        imageViewerTab.getEpochs().setSelectedItem(Epoch.YEAR);
        imageViewerTab.assembleFlipbook();

        JPanel bandPanel = new JPanel(new GridLayout(1, 0));
        bandPanel.setBorder(createEmptyBorder("WISE time series"));

        for (FlipbookComponent component : imageViewerTab.getFlipbook()) {
            BufferedImage image = imageViewerTab.processImage(component);
            bandPanel.add(buildImagePanel(image, component.getTitle(), Shape.CROSS));
        }

        if (bandPanel.getComponentCount() > 0) {
            addFillerPanel(bandPanel);
            centerPanel.add(bandPanel);
        }
    }

    private JPanel buildImagePanel(BufferedImage image, String imageHeader) {
        return buildImagePanel(image, imageHeader, Shape.CIRCLE);
    }

    private JPanel buildImagePanel(BufferedImage image, String imageHeader, Shape shape) {
        JPanel panel = new JPanel();
        panel.setBorder(createEtchedBorder(imageHeader));
        panel.add(new JLabel(new ImageIcon(drawCenterShape(image, shape))));
        return panel;
    }

    private BufferedImage drawCenterShape(BufferedImage image, Shape shape) {
        image = zoom(image, 200);
        double x = image.getWidth() / 2;
        double y = image.getHeight() / 2;
        Graphics g = image.getGraphics();
        Drawable drawable;
        switch (shape) {
            case CROSS:
                drawable = new Cross(x, y, 50, Color.MAGENTA);
                break;
            default:
                drawable = new Circle(x, y, 10, Color.MAGENTA);
                break;
        }
        drawable.draw(g);
        return image;
    }

    private void addFillerPanel(JPanel bandPanel) {
        for (int i = bandPanel.getComponentCount(); i < MAX_IMAGES + 1; i++) {
            bandPanel.add(new JPanel());
        }
    }

}
