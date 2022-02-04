package astro.tool.box.main;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.main.ModuleHelper.*;
import static astro.tool.box.tab.SettingsTab.*;
import astro.tool.box.container.BatchResult;
import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.SimbadCatalogEntry;
import astro.tool.box.catalog.WhiteDwarf;
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
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class InfoSheet {

    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.DARK_GRAY);
    private static final Font LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font MEDIUM_FONT = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, BaseColor.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.BLACK);
    private static final Font SMALL_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, BaseColor.BLACK);
    private static final Font SMALL_GREEN_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.GREEN.darker());
    private static final Font SMALL_ORANGE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.ORANGE.darker());
    private static final Font SMALL_RED_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.RED.darker());
    private static final Font SMALL_WHITE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.WHITE);

    private final double targetRa;
    private final double targetDec;
    private final int size;
    private final ImageViewerTab imageViewerTab;

    private final Map<String, CatalogEntry> catalogInstances;

    private final CatalogQueryService catalogQueryService;
    private final SpectralTypeLookupService mainSequenceLookupService;
    private final SpectralTypeLookupService brownDwarfsLookupService;

    public InfoSheet(double targetRa, double targetDec, int size, ImageViewerTab imageViewerTab) {
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

            List<String> imageLabels = new ArrayList<>();
            List<BufferedImage> bufferedImages = new ArrayList<>();
            BufferedImage bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_blue&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("poss1_blue");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_red&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("poss1_red");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_blue&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("poss2ukstu_blue");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_red&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("poss2ukstu_red");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("poss2ukstu_ir");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "dss", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("dss2IR-dss1Red-dss1Blue");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable("DSS", imageLabels, bufferedImages, writer, document);

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=j&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("J");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=h&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("H");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("K");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("K-H-J");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable("2MASS", imageLabels, bufferedImages, writer, document);

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=u&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("u");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=g&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("g");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=r&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("r");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=i&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("i");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("z");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "sdss", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("z-g-u");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable("SDSS", imageLabels, bufferedImages, writer, document);

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
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:MIPS24&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("MIPS24");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "seip", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("3-color");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable("Spitzer (SEIP)", imageLabels, bufferedImages, writer, document);

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=1&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("W1");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("W2");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=3&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("W3");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=4&type=jpgurl");
            if (bufferedImage != null) {
                imageLabels.add("W4");
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "file_type=colorimage");
            if (bufferedImage != null) {
                imageLabels.add("W4-W2-W1");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable("AllWISE", imageLabels, bufferedImages, writer, document);

            SortedMap<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
            if (!imageInfos.isEmpty()) {
                imageLabels = new ArrayList<>();
                bufferedImages = new ArrayList<>();
                imageLabels.add("g");
                bufferedImages.add(retrievePs1Image(String.format("red=%s", imageInfos.get("g")), targetRa, targetDec, size));
                imageLabels.add("r");
                bufferedImages.add(retrievePs1Image(String.format("red=%s", imageInfos.get("r")), targetRa, targetDec, size));
                imageLabels.add("i");
                bufferedImages.add(retrievePs1Image(String.format("red=%s", imageInfos.get("i")), targetRa, targetDec, size));
                imageLabels.add("z");
                bufferedImages.add(retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size));
                imageLabels.add("y");
                bufferedImages.add(retrievePs1Image(String.format("red=%s", imageInfos.get("y")), targetRa, targetDec, size));
                imageLabels.add("y-i-g");
                bufferedImages.add(retrievePs1Image(String.format("red=%s&green=%s&blue=%s", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g")), targetRa, targetDec, size));

                createPdfTable("Pan-STARRS", imageLabels, bufferedImages, writer, document);
            }

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            bufferedImage = retrieveDecalsImage(targetRa, targetDec, size, "g");
            if (bufferedImage != null) {
                imageLabels.add("g");
                bufferedImage = convertToGray(bufferedImage);
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveDecalsImage(targetRa, targetDec, size, "r");
            if (bufferedImage != null) {
                imageLabels.add("r");
                bufferedImage = convertToGray(bufferedImage);
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveDecalsImage(targetRa, targetDec, size, "z");
            if (bufferedImage != null) {
                imageLabels.add("z");
                bufferedImage = convertToGray(bufferedImage);
                bufferedImages.add(bufferedImage);
            }
            bufferedImage = retrieveDecalsImage(targetRa, targetDec, size, "grz");
            if (bufferedImage != null) {
                imageLabels.add("g-r-z");
                bufferedImages.add(bufferedImage);
            }

            createPdfTable("DECaLS", imageLabels, bufferedImages, writer, document);

            imageLabels = new ArrayList<>();
            bufferedImages = new ArrayList<>();
            for (FlipbookComponent component : imageViewerTab.getFlipbook()) {
                imageLabels.add(component.getTitle());
                bufferedImages.add(imageViewerTab.processImage(component));
            }

            createPdfTable("NeoWISE", imageLabels, bufferedImages, writer, document);

            int searchRadius = size / 3;
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

            document.add(new Paragraph(" "));

            String mainHeader = "CATALOG ENTRIES (Search radius = " + roundTo1DecNZ(searchRadius) + "\")";
            document.add(createCatalogEntriesTable(mainSequenceLookupService, catalogEntries, "Main sequence spectral type evaluation (**)", mainHeader));
            document.add(createCatalogEntriesTable(brownDwarfsLookupService, catalogEntries, "M, L & T dwarfs spectral type evaluation (***)", null));

            PdfPTable table = new PdfPTable(3);
            table.setTotalWidth(new float[]{11, 40, 100});
            table.setLockedWidth(true);
            table.setSpacingBefore(10);
            table.setKeepTogether(true);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);

            table.addCell(createTableCell("(*)", SMALL_FONT));
            table.addCell(createTableCell("Match probability", SMALL_BOLD_FONT));
            table.addCell(createTableCell("Constraint", SMALL_BOLD_FONT));

            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(BaseColor.GREEN.darker());
            table.addCell(cell);
            table.addCell(createTableCell("High", SMALL_FONT));
            table.addCell(createTableCell("0 <= target distance < 3", SMALL_FONT));

            cell = new PdfPCell();
            cell.setBackgroundColor(BaseColor.ORANGE.darker());
            table.addCell(cell);
            table.addCell(createTableCell("Medium", SMALL_FONT));
            table.addCell(createTableCell("3 <= target distance < 6", SMALL_FONT));

            cell = new PdfPCell();
            cell.setBackgroundColor(BaseColor.RED.darker());
            table.addCell(cell);
            table.addCell(createTableCell("Low", SMALL_FONT));
            table.addCell(createTableCell("6 <= target distance", SMALL_FONT));

            document.add(table);

            document.add(new Paragraph("(**) Uses colors from: A Modern Mean Dwarf Stellar Color & Effective Temperature Sequence (Eric Mamajek)", SMALL_FONT));
            document.add(new Paragraph("(***) Uses colors from: Best et al. (2018), Carnero Rosell et al. (2019), Skrzypek et al. (2015), Skrzypek et al. (2016) and Kiman et al. (2019)", SMALL_FONT));

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
                    .setPmra(catalogEntry.getPmra())
                    .setPmdec(catalogEntry.getPmdec())
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
        addHeaderCell(table, "Target dist. (*)", Element.ALIGN_RIGHT);
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
            Font font;
            double targetDistance = batchResult.getTargetDistance();
            if (targetDistance < 3) {
                font = SMALL_GREEN_FONT;
            } else if (targetDistance >= 3 && targetDistance < 6) {
                font = SMALL_ORANGE_FONT;
            } else {
                font = SMALL_RED_FONT;
            }
            addCell(table, batchResult.getCatalogName(), Element.ALIGN_LEFT, i, SMALL_FONT);
            addCell(table, roundTo3Dec(batchResult.getTargetDistance()), Element.ALIGN_RIGHT, i, font);
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

    private void createPdfTable(String header, List<String> imageLabels, List<BufferedImage> bufferedImages, PdfWriter writer, Document document) throws Exception {
        int numberOfCells = imageLabels.size();

        if (numberOfCells == 0) {
            return;
        }

        float[] widths = new float[numberOfCells];
        for (int i = 0; i < numberOfCells; i++) {
            widths[i] = 75;
        }

        PdfPTable table = new PdfPTable(numberOfCells);
        table.setTotalWidth(widths);
        table.setLockedWidth(true);
        table.setKeepTogether(true);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell tableHeader = new PdfPCell(new Phrase(header, LARGE_FONT));
        tableHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableHeader.setColspan(numberOfCells);
        tableHeader.setBorderWidth(0);
        table.addCell(tableHeader);

        for (String imageLabel : imageLabels) {
            PdfPCell cell = new PdfPCell(new Phrase(imageLabel, SMALL_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderWidth(0);
            cell.setPadding(1);
            table.addCell(cell);
        }

        for (BufferedImage bi : bufferedImages) {
            bi = drawCenterShape(bi);
            Image image = Image.getInstance(writer, bi, 1);
            PdfPCell cell = new PdfPCell(image, true);
            cell.setBorderWidth(0);
            cell.setPadding(1);
            table.addCell(cell);
        }

        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, Object value, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(value.toString(), SMALL_WHITE_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderWidth(0.5f);
        cell.setPadding(2);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, Object value, int alignment, int rowIndex, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value.toString(), font));
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(rowIndex % 2 == 0 ? BaseColor.WHITE : BaseColor.LIGHT_GRAY);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderWidth(0.5f);
        cell.setPadding(2);
        table.addCell(cell);
    }

    private PdfPCell createTableCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorderWidth(0);
        cell.setPadding(2);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        return cell;
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
