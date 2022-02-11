package astro.tool.box.main;

import astro.tool.box.container.FlipbookComponent;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.tab.SettingsTab.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.BatchResult;
import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.SimbadCatalogEntry;
import astro.tool.box.catalog.WhiteDwarf;
import astro.tool.box.container.Couple;
import astro.tool.box.container.NirImage;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.lookup.SpectralTypeLookupEntry;
import astro.tool.box.enumeration.Epoch;
import astro.tool.box.tab.ImageViewerTab;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.SpectralTypeLookupService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class ImageSeriesPdf {

    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.DARK_GRAY);
    private static final Font LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font MEDIUM_FONT = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, BaseColor.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.BLACK);
    private static final Font SMALL_WHITE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.WHITE);

    private final double targetRa;
    private final double targetDec;
    private final int size;

    private final ImageViewerTab imageViewerTab;

    private final Map<String, CatalogEntry> catalogInstances;

    private final CatalogQueryService catalogQueryService;
    private final SpectralTypeLookupService mainSequenceLookupService;
    private final SpectralTypeLookupService brownDwarfsLookupService;

    public ImageSeriesPdf(double targetRa, double targetDec, int size, ImageViewerTab imageViewerTab) {
        this.targetRa = targetRa;
        this.targetDec = targetDec;
        this.size = size;
        this.imageViewerTab = imageViewerTab;
        catalogInstances = getCatalogInstances();
        catalogQueryService = new CatalogQueryService();
        InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(",", -1));
            }).collect(Collectors.toList());
            mainSequenceLookupService = new SpectralTypeLookupService(entries);
        }
        input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new BrownDwarfLookupEntry(line.split(",", -1));
            }).collect(Collectors.toList());
            brownDwarfsLookupService = new SpectralTypeLookupService(entries);
        }
    }

    public Boolean create(JFrame baseFrame) {
        imageViewerTab.setWaitCursor(false);
        JTextField coordsField = imageViewerTab.getCoordsField();
        ActionListener actionListener = coordsField.getActionListeners()[0];
        coordsField.removeActionListener(actionListener);
        coordsField.setText(roundTo7DecNZ(targetRa) + " " + roundTo7DecNZ(targetDec));
        coordsField.addActionListener(actionListener);
        JTextField sizeField = imageViewerTab.getSizeField();
        actionListener = sizeField.getActionListeners()[0];
        sizeField.removeActionListener(actionListener);
        sizeField.setText(String.valueOf(size));
        sizeField.addActionListener(actionListener);
        try {
            imageViewerTab.getZoomSlider().setValue(250);
            imageViewerTab.getEpochs().setSelectedItem(Epoch.YEAR);

            baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            File tmpFile = File.createTempFile("Target_" + roundTo2DecNZ(targetRa) + addPlusSign(roundDouble(targetDec, PATTERN_2DEC_NZ)) + "_", ".pdf");

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tmpFile));

            DocumentFooter event = new DocumentFooter();
            writer.setPageEvent(event);

            document.open();

            Chunk chunk = new Chunk("Target: " + roundTo6DecNZ(targetRa) + " " + addPlusSign(roundDouble(targetDec, PATTERN_6DEC_NZ)) + " FoV: " + size + "\"", HEADER_FONT);
            document.add(chunk);

            document.add(new Paragraph(" "));

            List<Couple<String, NirImage>> timeSeries = new ArrayList<>();

            List<String> imageLabels = new ArrayList<>();
            List<BufferedImage> bufferedImages = new ArrayList<>();
            BufferedImage bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_blue&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("DSS1 B");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_red&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("DSS1 R");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_blue&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("DSS2 B");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_red&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("DSS2 R");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("DSS IR");
                bufferedImages.add(bufferedImage);
                timeSeries.add(new Couple("DSS IR", new NirImage(1980, bufferedImage)));
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("DSS IR-R-B");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable(imageLabels, bufferedImages, writer, document);

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=j&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("2MASS J");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=h&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("2MASS H");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("2MASS K");
                bufferedImages.add(bufferedImage);
                timeSeries.add(new Couple("2MASS K", new NirImage(1999, bufferedImage)));
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("2MASS K-H-J");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable(imageLabels, bufferedImages, writer, document);

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=u&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("SDSS u");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=g&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("SDSS g");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=r&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("SDSS r");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=i&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("SDSS i");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("SDSS z");
                bufferedImages.add(bufferedImage);
                timeSeries.add(new Couple("SDSS z", new NirImage(2003, bufferedImage)));
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("SDSS z-g-u");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable(imageLabels, bufferedImages, writer, document);

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            bufferedImage = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC1&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("IRAC1");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC2&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("IRAC2");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC3&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("IRAC3");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("IRAC4");
                bufferedImages.add(bufferedImage);
                timeSeries.add(new Couple("IRAC4", new NirImage(2005, bufferedImage)));
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:MIPS24&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("MIPS24");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "seip", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("IRAC color");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable(imageLabels, bufferedImages, writer, document);

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=1&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("WISE W1");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("WISE W2");
                bufferedImages.add(bufferedImage);
                timeSeries.add(new Couple("WISE W2", new NirImage(2010, bufferedImage)));
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=3&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("WISE W3");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=4&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("WISE W4");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("WISE W4-W2-W1");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable(imageLabels, bufferedImages, writer, document);

            if (targetDec > -5) {
                imageLabels = new ArrayList<>();
                bufferedImages = new ArrayList<>();
                Map<String, NirImage> images = retrieveNearInfraredImages(targetRa, targetDec, size, UKIDSS_SURVEY_URL, UKIDSS_LABEL);
                if (!images.isEmpty()) {
                    for (Entry<String, NirImage> entry : images.entrySet()) {
                        String band = entry.getKey();
                        NirImage nirImage = entry.getValue();
                        bufferedImage = nirImage.getImage();
                        if (bufferedImage != null) {
                            String imageLabel = UKIDSS_LABEL + " " + band;
                            imageLabels.add(imageLabel);
                            bufferedImages.add(bufferedImage);
                            if (band.equals("K")) {
                                timeSeries.add(new Couple(imageLabel, new NirImage(nirImage.getYear(), bufferedImage)));
                            }
                        }
                    }
                    createPdfTable(imageLabels, bufferedImages, writer, document);
                }
            }

            if (targetDec < 5) {
                imageLabels = new ArrayList<>();
                bufferedImages = new ArrayList<>();
                Map<String, NirImage> images = retrieveNearInfraredImages(targetRa, targetDec, size, VHS_SURVEY_URL, VHS_LABEL);
                if (!images.isEmpty()) {
                    for (Entry<String, NirImage> entry : images.entrySet()) {
                        String band = entry.getKey();
                        NirImage nirImage = entry.getValue();
                        bufferedImage = nirImage.getImage();
                        if (bufferedImage != null) {
                            String imageLabel = VHS_LABEL + " " + band;
                            imageLabels.add(imageLabel);
                            bufferedImages.add(bufferedImage);
                            if (band.equals("K")) {
                                timeSeries.add(new Couple(imageLabel, new NirImage(nirImage.getYear(), bufferedImage)));
                            }
                        }
                    }
                    createPdfTable(imageLabels, bufferedImages, writer, document);
                }
            }

            Map<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
            if (!imageInfos.isEmpty()) {
                imageLabels = new ArrayList<>();
                bufferedImages = new ArrayList<>();
                imageLabels.add("PS1 g");
                bufferedImages.add(retrievePs1Image(String.format("red=%s", imageInfos.get("g")), targetRa, targetDec, size, true));
                imageLabels.add("PS1 r");
                bufferedImages.add(retrievePs1Image(String.format("red=%s", imageInfos.get("r")), targetRa, targetDec, size, true));
                imageLabels.add("PS1 i");
                bufferedImages.add(retrievePs1Image(String.format("red=%s", imageInfos.get("i")), targetRa, targetDec, size, true));
                imageLabels.add("PS1 z");
                bufferedImages.add(bufferedImage = retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size, true));
                timeSeries.add(new Couple("PS1 z", new NirImage(2012, bufferedImage)));
                imageLabels.add("PS1 y");
                bufferedImages.add(retrievePs1Image(String.format("red=%s", imageInfos.get("y")), targetRa, targetDec, size, true));
                imageLabels.add("PS1 y-i-g");
                bufferedImages.add(retrievePs1Image(String.format("red=%s&green=%s&blue=%s", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g")), targetRa, targetDec, size, false));

                createPdfTable(imageLabels, bufferedImages, writer, document);
            }

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            bufferedImage = retrieveDecalsImage(targetRa, targetDec, size, "g", true);
            if (bufferedImage != null) {
                imageLabels.add("DESI LS g");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveDecalsImage(targetRa, targetDec, size, "r", true);
            if (bufferedImage != null) {
                imageLabels.add("DESI LS r");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveDecalsImage(targetRa, targetDec, size, "z", true);
            if (bufferedImage != null) {
                imageLabels.add("DESI LS z");
                bufferedImages.add(bufferedImage);
                timeSeries.add(new Couple("DESI LS z", new NirImage(2020, bufferedImage)));
            }
            bufferedImage = retrieveDecalsImage(targetRa, targetDec, size, "grz", false);
            if (bufferedImage != null) {
                imageLabels.add("DESI LS g-r-z");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable(imageLabels, bufferedImages, writer, document);

            // Cross survey time series
            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            timeSeries.sort(Comparator.comparing(c -> c.getB().getYear()));
            for (Couple<String, NirImage> couple : timeSeries) {
                imageLabels.add(couple.getA());
                bufferedImages.add(couple.getB().getImage());
            }

            createPdfTable(imageLabels, bufferedImages, writer, document);

            // WISE time series
            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            FlipbookComponent[] flipbook = imageViewerTab.getFlipbook();
            int length = flipbook.length;
            length = length < 8 ? length : 8;
            for (int i = 0; i < length; i++) {
                FlipbookComponent component = flipbook[i];
                imageLabels.add(component.getTitle());
                bufferedImages.add(imageViewerTab.processImage(component));
            }

            createPdfTable(imageLabels, bufferedImages, writer, document);

            int searchRadius = 10;
            List<CatalogEntry> catalogEntries = new ArrayList<>();
            List<String> selectedCatalogs = getSelectedCatalogs(catalogInstances);
            for (CatalogEntry catalogEntry : catalogInstances.values()) {
                if (selectedCatalogs.contains(catalogEntry.getCatalogName())) {
                    catalogEntry.setRa(targetRa);
                    catalogEntry.setDec(targetDec);
                    catalogEntry.setSearchRadius(searchRadius);
                    List<CatalogEntry> results = performQuery(catalogEntry);
                    if (results != null) {
                        catalogEntries.addAll(results);
                    }
                }
            }

            String mainHeader = "CATALOG ENTRIES (Search radius = " + roundTo1DecNZ(searchRadius) + "\")";

            document.add(createCatalogEntriesTable(mainSequenceLookupService, catalogEntries, "Main sequence spectral type estimates (*)", mainHeader));
            document.add(new Paragraph("(*) Uses the color - spectral type relations from Eric Mamajek's Modern Mean Dwarf Stellar Color & Effective Temperature Sequence", SMALL_FONT));

            document.add(createCatalogEntriesTable(brownDwarfsLookupService, catalogEntries, "M, L & T dwarfs spectral type estimates (*)", null));
            document.add(new Paragraph("(*) Uses the color - spectral type relations from Best et al. (2018), Carnero Rosell et al. (2019), Skrzypek et al. (2015), Skrzypek et al. (2016) and Kiman et al. (2019)", SMALL_FONT));

            document.close();

            Desktop.getDesktop().open(tmpFile);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            imageViewerTab.setWaitCursor(true);
            baseFrame.setCursor(Cursor.getDefaultCursor());
            coordsField.setCursor(Cursor.getDefaultCursor());
            sizeField.setCursor(Cursor.getDefaultCursor());
        }

        return true;
    }

    private PdfPTable createCatalogEntriesTable(SpectralTypeLookupService spectralTypeLookupService, List<CatalogEntry> catalogEntries, String header, String mainHeader) throws Exception {
        List<BatchResult> batchResults = new ArrayList<>();
        for (CatalogEntry catalogEntry : catalogEntries) {
            List<String> spectralTypes = lookupSpectralTypes(catalogEntry.getColors(true), spectralTypeLookupService, true);
            if (catalogEntry instanceof SimbadCatalogEntry) {
                SimbadCatalogEntry simbadEntry = (SimbadCatalogEntry) catalogEntry;
                StringBuilder simbadType = new StringBuilder();
                simbadType.append(simbadEntry.getObjectType());
                if (!simbadEntry.getSpectralType().isEmpty()) {
                    simbadType.append(" ").append(simbadEntry.getSpectralType());
                }
                simbadType.append("; ");
                spectralTypes.add(0, simbadType.toString());
            }
            if (catalogEntry instanceof AllWiseCatalogEntry) {
                AllWiseCatalogEntry entry = (AllWiseCatalogEntry) catalogEntry;
                if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                    spectralTypes.add(AGN_WARNING);
                }
            }
            if (catalogEntry instanceof WhiteDwarf) {
                WhiteDwarf entry = (WhiteDwarf) catalogEntry;
                if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                    spectralTypes.add(WD_WARNING);
                }
            }
            BatchResult batchResult = new BatchResult.Builder()
                    .setCatalogName(catalogEntry.getCatalogName())
                    .setTargetRa(targetRa)
                    .setTargetDec(targetDec)
                    .setTargetDistance(catalogEntry.getTargetDistance())
                    .setRa(catalogEntry.getRa())
                    .setDec(catalogEntry.getDec())
                    .setSourceId(catalogEntry.getSourceId() + " ")
                    .setPlx(catalogEntry.getPlx())
                    .setPmra(catalogEntry.getTotalProperMotion() > 100000 ? 0 : catalogEntry.getPmra())
                    .setPmdec(catalogEntry.getTotalProperMotion() > 100000 ? 0 : catalogEntry.getPmdec())
                    .setMagnitudes(catalogEntry.getMagnitudes())
                    .setSpectralTypes(spectralTypes).build();
            batchResults.add(batchResult);
        }

        int numberOfCols = 10;
        PdfPTable table = new PdfPTable(numberOfCols);
        table.setTotalWidth(new float[]{50, 30, 40, 40, 80, 30, 35, 35, 100, 100});
        table.setLockedWidth(true);
        table.setSpacingBefore(10);
        table.setKeepTogether(true);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell tableHeader;
        if (mainHeader != null) {
            tableHeader = new PdfPCell(new Phrase(mainHeader, LARGE_FONT));
            tableHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableHeader.setColspan(numberOfCols);
            tableHeader.setBorderWidth(0);
            tableHeader.setPaddingBottom(10);
            table.addCell(tableHeader);
        }

        tableHeader = new PdfPCell(new Phrase(header, MEDIUM_FONT));
        tableHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableHeader.setColspan(numberOfCols);
        tableHeader.setBorderWidth(0);
        tableHeader.setPaddingBottom(5);
        table.addCell(tableHeader);

        addHeaderCell(table, "Catalog", Element.ALIGN_LEFT);
        addHeaderCell(table, "Target dist. (\")", Element.ALIGN_RIGHT);
        addHeaderCell(table, "RA", Element.ALIGN_LEFT);
        addHeaderCell(table, "dec", Element.ALIGN_LEFT);
        addHeaderCell(table, "Source id", Element.ALIGN_LEFT);
        addHeaderCell(table, "Plx (mas)", Element.ALIGN_RIGHT);
        addHeaderCell(table, "pmRA (mas/yr)", Element.ALIGN_RIGHT);
        addHeaderCell(table, "pmdec (mas/yr)", Element.ALIGN_RIGHT);
        addHeaderCell(table, "Magnitudes", Element.ALIGN_LEFT);
        addHeaderCell(table, "Spectral types", Element.ALIGN_LEFT);

        for (int i = 0; i < batchResults.size(); i++) {
            BatchResult batchResult = batchResults.get(i);
            addCell(table, batchResult.getCatalogName(), Element.ALIGN_LEFT, i, SMALL_FONT);
            addCell(table, roundTo3Dec(batchResult.getTargetDistance()), Element.ALIGN_RIGHT, i, SMALL_FONT);
            addCell(table, roundTo6DecNZ(batchResult.getRa()), Element.ALIGN_LEFT, i, SMALL_FONT);
            addCell(table, roundTo6DecNZ(batchResult.getDec()), Element.ALIGN_LEFT, i, SMALL_FONT);
            addCell(table, batchResult.getSourceId(), Element.ALIGN_LEFT, i, SMALL_FONT);
            addCell(table, roundTo3Dec(batchResult.getPlx()), Element.ALIGN_RIGHT, i, SMALL_FONT);
            addCell(table, roundTo3Dec(batchResult.getPmra()), Element.ALIGN_RIGHT, i, SMALL_FONT);
            addCell(table, roundTo3Dec(batchResult.getPmdec()), Element.ALIGN_RIGHT, i, SMALL_FONT);
            addCell(table, batchResult.getMagnitudes(), Element.ALIGN_LEFT, i, SMALL_FONT);
            addCell(table, batchResult.joinSpetralTypes(), Element.ALIGN_LEFT, i, SMALL_FONT);
        }

        return table;
    }

    private void createPdfTable(List<String> imageLabels, List<BufferedImage> bufferedImages, PdfWriter writer, Document document) throws Exception {
        int numberOfImages = bufferedImages.size();

        if (numberOfImages == 0) {
            return;
        }

        int maxCellsPerRow = 6;
        int cellsPerRow = numberOfImages;
        int totalCells = numberOfImages;

        if (numberOfImages > maxCellsPerRow) {
            cellsPerRow = maxCellsPerRow;
            int remainder = numberOfImages % maxCellsPerRow;
            int numberOfRows = numberOfImages / maxCellsPerRow;
            numberOfRows = remainder > 0 ? numberOfRows + 1 : numberOfRows;
            totalCells = numberOfRows * maxCellsPerRow;
        }

        float[] widths = new float[cellsPerRow];
        for (int i = 0; i < cellsPerRow; i++) {
            widths[i] = 90;
        }

        PdfPTable table = new PdfPTable(cellsPerRow);
        table.setTotalWidth(widths);
        table.setLockedWidth(true);
        table.setSpacingAfter(5);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        for (int i = 0; i < totalCells; i++) {
            if (i < numberOfImages) {
                String label = imageLabels.get(i);
                BufferedImage bi = drawCenterShape(bufferedImages.get(i));
                Image image = Image.getInstance(writer, bi, 1);
                PdfPCell cell = new PdfPCell(image, true);
                cell.setCellEvent(new WatermarkedCell(label));
                cell.setBorderWidth(0);
                cell.setPadding(1);
                table.addCell(cell);
            } else {
                PdfPCell cell = new PdfPCell();
                cell.setBorderWidth(0);
                cell.setPadding(1);
                table.addCell(cell);
            }
        }

        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, Object value, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value.toString(), SMALL_WHITE_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderWidth(0);
        cell.setPadding(2);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, Object value, int alignment, int rowIndex, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value.toString(), font));
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(rowIndex % 2 == 0 ? BaseColor.WHITE : BaseColor.LIGHT_GRAY);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderWidth(0);
        cell.setPadding(2);
        table.addCell(cell);
    }

    private List<CatalogEntry> performQuery(CatalogEntry catalogQuery) throws IOException {
        List<CatalogEntry> catalogEntries = catalogQueryService.getCatalogEntriesByCoords(catalogQuery);
        catalogEntries.forEach(catalogEntry -> {
            catalogEntry.setTargetRa(catalogQuery.getRa());
            catalogEntry.setTargetDec(catalogQuery.getDec());
        });
        if (!catalogEntries.isEmpty()) {
            catalogEntries.sort(Comparator.comparingDouble(CatalogEntry::getTargetDistance));
            return catalogEntries;
        }
        return null;
    }

    class DocumentFooter extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase(PGM_NAME + " " + PGM_VERSION + " - Page " + writer.getPageNumber(), SMALL_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer, (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 10, 0);
        }
    }

}
