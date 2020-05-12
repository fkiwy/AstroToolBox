package astro.tool.box.module;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
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
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class PdfCreator {

    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.DARK_GRAY);
    private static final Font LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.BLACK);

    private final double targetRa;
    private final double targetDec;
    private final int size;

    public PdfCreator(double targetRa, double targetDec, int size) {
        this.targetRa = targetRa;
        this.targetDec = targetDec;
        this.size = size;
    }

    public void create() throws Exception {
        File tmpFile = File.createTempFile("DataSheet_" + roundTo2DecNZ(targetRa) + addPlusSign(roundDouble(targetDec, PATTERN_2DEC_NZ)) + "_", ".pdf");

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tmpFile));

        document.open();

        Chunk chunk = new Chunk("Data sheet for object: " + roundTo6DecNZ(targetRa) + " " + addPlusSign(roundDouble(targetDec, PATTERN_6DEC_NZ)) + " FoV: " + size + "\"", HEADER_FONT);
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
            imageLabels.add("j");
            bufferedImages.add(bufferedImage);
        }
        bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=h&type=jpgurl");
        if (bufferedImage != null) {
            imageLabels.add("h");
            bufferedImages.add(bufferedImage);
        }
        bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
        if (bufferedImage != null) {
            imageLabels.add("k");
            bufferedImages.add(bufferedImage);
        }
        bufferedImage = retrieveImage(targetRa, targetDec, size, "2mass", "file_type=colorimage");
        if (bufferedImage != null) {
            imageLabels.add("k-h-j");
            bufferedImages.add(bufferedImage);
        }

        createPdfTable("2MASS", imageLabels, bufferedImages, writer, document);

        imageLabels = new ArrayList<>();
        bufferedImages = new ArrayList<>();
        bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=1&type=jpgurl");
        if (bufferedImage != null) {
            imageLabels.add("w1");
            bufferedImages.add(bufferedImage);
        }
        bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
        if (bufferedImage != null) {
            imageLabels.add("w2");
            bufferedImages.add(bufferedImage);
        }
        bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=3&type=jpgurl");
        if (bufferedImage != null) {
            imageLabels.add("w3");
            bufferedImages.add(bufferedImage);
        }
        bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=4&type=jpgurl");
        if (bufferedImage != null) {
            imageLabels.add("w4");
            bufferedImages.add(bufferedImage);
        }
        bufferedImage = retrieveImage(targetRa, targetDec, size, "wise", "file_type=colorimage");
        if (bufferedImage != null) {
            imageLabels.add("w4-w2-w1");
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

        document.close();

        Desktop.getDesktop().open(tmpFile);
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

}
