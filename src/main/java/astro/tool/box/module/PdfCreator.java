package astro.tool.box.module;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.BatchResult;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaDR2CatalogEntry;
import astro.tool.box.container.catalog.PanStarrsCatalogEntry;
import astro.tool.box.container.catalog.SDSSCatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.container.catalog.VHSCatalogEntry;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.facade.CatalogQueryFacade;
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
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;

public class PdfCreator {

    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.DARK_GRAY);
    private static final Font LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.BLACK);
    private static final Font SMALL_WHITE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.WHITE);

    private final double targetRa;
    private final double targetDec;
    private final int size;

    private final Map<String, CatalogEntry> catalogInstances;

    private final CatalogQueryFacade catalogQueryFacade;
    private final SpectralTypeLookupService spectralTypeLookupService;

    public PdfCreator(double targetRa, double targetDec, int size) {
        this.targetRa = targetRa;
        this.targetDec = targetDec;
        this.size = size;

        // Plug in catalogs here
        catalogInstances = new LinkedHashMap<>();
        SimbadCatalogEntry simbadCatalogEntry = new SimbadCatalogEntry();
        catalogInstances.put(simbadCatalogEntry.getCatalogName(), simbadCatalogEntry);
        GaiaDR2CatalogEntry gaiaDR2CatalogEntry = new GaiaDR2CatalogEntry();
        catalogInstances.put(gaiaDR2CatalogEntry.getCatalogName(), gaiaDR2CatalogEntry);
        AllWiseCatalogEntry allWiseCatalogEntry = new AllWiseCatalogEntry();
        catalogInstances.put(allWiseCatalogEntry.getCatalogName(), allWiseCatalogEntry);
        CatWiseCatalogEntry catWiseCatalogEntry = new CatWiseCatalogEntry();
        catalogInstances.put(catWiseCatalogEntry.getCatalogName(), catWiseCatalogEntry);
        PanStarrsCatalogEntry panStarrsCatalogEntry = new PanStarrsCatalogEntry();
        catalogInstances.put(panStarrsCatalogEntry.getCatalogName(), panStarrsCatalogEntry);
        SDSSCatalogEntry sdssCatalogEntry = new SDSSCatalogEntry();
        catalogInstances.put(sdssCatalogEntry.getCatalogName(), sdssCatalogEntry);
        VHSCatalogEntry vhsCatalogEntry = new VHSCatalogEntry();
        catalogInstances.put(vhsCatalogEntry.getCatalogName(), vhsCatalogEntry);

        catalogQueryFacade = new CatalogQueryService();
        InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(SPLIT_CHAR, 30));
            }).collect(Collectors.toList());
            spectralTypeLookupService = new SpectralTypeLookupService(entries);
        }
    }

    public Boolean create(JFrame baseFrame) {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            File tmpFile = File.createTempFile("Target_" + roundTo2DecNZ(targetRa) + addPlusSign(roundDouble(targetDec, PATTERN_2DEC_NZ)) + "_", ".pdf");

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tmpFile));

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
                imageLabels.add("y");
                bufferedImages.add(retrievePs1Image(String.format("red=%s&green=%s&blue=%s", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g")), targetRa, targetDec, size));

                createPdfTable("Pan-STARRS", imageLabels, bufferedImages, writer, document);
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("CATALOG ENTRIES", LARGE_FONT));

            List<BatchResult> batchResults = new ArrayList<>();
            for (CatalogEntry catalogEntry : catalogInstances.values()) {
                catalogEntry.setRa(targetRa);
                catalogEntry.setDec(targetDec);
                catalogEntry.setSearchRadius(5);
                catalogEntry = performQuery(catalogEntry);
                if (catalogEntry == null) {
                    continue;
                }
                List<String> spectralTypes = lookupSpectralTypes(catalogEntry.getColors(), spectralTypeLookupService, true);
                if (catalogEntry instanceof SimbadCatalogEntry) {
                    SimbadCatalogEntry simbadEntry = (SimbadCatalogEntry) catalogEntry;
                    StringBuilder simbadType = new StringBuilder();
                    //if (includeColors.isSelected()) {
                    simbadType.append("[");
                    //}
                    simbadType.append(simbadEntry.getObjectType());
                    if (!simbadEntry.getSpectralType().isEmpty()) {
                        simbadType.append(" ").append(simbadEntry.getSpectralType());
                    }
                    //if (includeColors.isSelected()) {
                    simbadType.append("]");
                    //}
                    spectralTypes.add(0, simbadType.toString());
                }
                if (catalogEntry instanceof AllWiseCatalogEntry) {
                    AllWiseCatalogEntry entry = (AllWiseCatalogEntry) catalogEntry;
                    if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
                        spectralTypes.add("[" + AGN_WARNING + "]");
                    }
                }
                if (catalogEntry instanceof GaiaDR2CatalogEntry) {
                    GaiaDR2CatalogEntry entry = (GaiaDR2CatalogEntry) catalogEntry;
                    if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
                        spectralTypes.add("[" + WD_WARNING + "]");
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

            PdfPTable table = new PdfPTable(10);
            table.setTotalWidth(new float[]{50, 30, 40, 40, 80, 30, 30, 30, 100, 100});
            table.setLockedWidth(true);
            table.setSpacingBefore(10);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);

            addHeaderCell(table, "Catalog", Element.ALIGN_LEFT);
            addHeaderCell(table, "Target distance (max. 5\")", Element.ALIGN_RIGHT);
            addHeaderCell(table, "RA", Element.ALIGN_LEFT);
            addHeaderCell(table, "dec", Element.ALIGN_LEFT);
            addHeaderCell(table, "Source id", Element.ALIGN_LEFT);
            addHeaderCell(table, "Plx", Element.ALIGN_RIGHT);
            addHeaderCell(table, "pmRA", Element.ALIGN_RIGHT);
            addHeaderCell(table, "pmdec", Element.ALIGN_RIGHT);
            addHeaderCell(table, "Magnitudes", Element.ALIGN_LEFT);
            addHeaderCell(table, "Spectral types", Element.ALIGN_LEFT);

            for (int i = 0; i < batchResults.size(); i++) {
                BatchResult batchResult = batchResults.get(i);
                addCell(table, batchResult.getCatalogName(), Element.ALIGN_LEFT, i);
                addCell(table, roundTo3Dec(batchResult.getTargetDistance()), Element.ALIGN_RIGHT, i);
                addCell(table, roundTo6DecNZ(batchResult.getRa()), Element.ALIGN_LEFT, i);
                addCell(table, roundTo6DecNZ(batchResult.getDec()), Element.ALIGN_LEFT, i);
                addCell(table, batchResult.getSourceId(), Element.ALIGN_LEFT, i);
                addCell(table, roundTo3Dec(batchResult.getPlx()), Element.ALIGN_RIGHT, i);
                addCell(table, roundTo3Dec(batchResult.getPmra()), Element.ALIGN_RIGHT, i);
                addCell(table, roundTo3Dec(batchResult.getPmdec()), Element.ALIGN_RIGHT, i);
                addCell(table, batchResult.getMagnitudes(), Element.ALIGN_LEFT, i);
                addCell(table, batchResult.joinSpetralTypes(), Element.ALIGN_LEFT, i);
            }

            document.add(table);

            document.close();

            Desktop.getDesktop().open(tmpFile);

        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        } finally {
            baseFrame.setCursor(Cursor.getDefaultCursor());
        }

        return true;
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

    private void addCell(PdfPTable table, Object value, int alignment, int rowIndex) {
        PdfPCell cell = new PdfPCell(new Phrase(value.toString(), SMALL_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setBackgroundColor(rowIndex % 2 == 0 ? BaseColor.WHITE : BaseColor.LIGHT_GRAY);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderWidth(0.5f);
        cell.setPadding(2);
        table.addCell(cell);
    }

    private CatalogEntry performQuery(CatalogEntry catalogQuery) throws IOException {
        List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
        catalogEntries.forEach(catalogEntry -> {
            catalogEntry.setTargetRa(catalogQuery.getRa());
            catalogEntry.setTargetDec(catalogQuery.getDec());
        });
        if (!catalogEntries.isEmpty()) {
            catalogEntries.sort(Comparator.comparing(entry -> entry.getTargetDistance()));
            return catalogEntries.get(0);
        }
        return null;
    }

}
