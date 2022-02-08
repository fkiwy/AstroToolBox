package astro.tool.box.main;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;

class WatermarkedCell implements PdfPCellEvent {

    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.BLACK);

    String watermark;

    public WatermarkedCell(String watermark) {
        this.watermark = watermark;
    }

    @Override
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
        canvas.saveState();
        PdfGState state = new PdfGState();
        state.setFillOpacity(0.75f);
        canvas.setGState(state);
        Chunk text = new Chunk(watermark, SMALL_FONT);
        text.setBackground(BaseColor.WHITE, 1, 0.5f, 1.2f, 1.5f);
        ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                new Phrase(text),
                position.getLeft() + 5,
                position.getTop() - 8, 0);
        canvas.restoreState();
    }

}
