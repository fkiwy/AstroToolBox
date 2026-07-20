package astro.tool.box.main;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

class WatermarkedCell implements PdfPCellEvent {

	private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 5, BaseColor.BLACK);

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
		ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(text), position.getLeft() + 2,
				position.getTop() - 6, 0);
		canvas.restoreState();
	}

}
