package astro.tool.box.tab;

import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import astro.tool.box.tool.AbsoluteMagnitudeTool;
import astro.tool.box.tool.AngularDistanceTool;
import astro.tool.box.tool.CoordsConverterTool;
import astro.tool.box.tool.DateConverterTool;
import astro.tool.box.tool.LinearDistanceTool;
import astro.tool.box.tool.ParallacticDistanceTool;
import astro.tool.box.tool.PhotometricDistanceTool;
import astro.tool.box.tool.ProperMotionsTool;
import astro.tool.box.tool.TangentialVelocityTool;
import astro.tool.box.tool.TotalProperMotionTool;
import astro.tool.box.tool.TotalVelocityTool;
import astro.tool.box.tool.UnitConverterTool;

public class ToolTab implements Tab {

	public static final String TAB_NAME = "Calculators & Converters";

	private final JFrame baseFrame;
	private final JTabbedPane tabbedPane;

	public ToolTab(JFrame baseFrame, JTabbedPane tabbedPane) {
		this.baseFrame = baseFrame;
		this.tabbedPane = tabbedPane;
	}

	@Override
	public void init(boolean visible) {
		try {
			JPanel toolPanel = new JPanel(new GridLayout(4, 3));
			AngularDistanceTool angularDistanceTool = new AngularDistanceTool(baseFrame, toolPanel);
			angularDistanceTool.init();
			LinearDistanceTool linearDistanceTool = new LinearDistanceTool(baseFrame, toolPanel);
			linearDistanceTool.init();
			ParallacticDistanceTool parallacticDistanceTool = new ParallacticDistanceTool(baseFrame, toolPanel);
			parallacticDistanceTool.init();
			PhotometricDistanceTool photometricDistanceTool = new PhotometricDistanceTool(baseFrame, toolPanel);
			photometricDistanceTool.init();
			AbsoluteMagnitudeTool absoluteMagnitudeTool = new AbsoluteMagnitudeTool(baseFrame, toolPanel);
			absoluteMagnitudeTool.init();
			ProperMotionsTool properMotionsTool = new ProperMotionsTool(baseFrame, toolPanel);
			properMotionsTool.init();
			TotalProperMotionTool totalProperMotionTool = new TotalProperMotionTool(baseFrame, toolPanel);
			totalProperMotionTool.init();
			TangentialVelocityTool tangentialVelocityTool = new TangentialVelocityTool(baseFrame, toolPanel);
			tangentialVelocityTool.init();
			TotalVelocityTool totalVelocityTool = new TotalVelocityTool(baseFrame, toolPanel);
			totalVelocityTool.init();
			UnitConverterTool unitConverterTool = new UnitConverterTool(baseFrame, toolPanel);
			unitConverterTool.init();
			CoordsConverterTool coordsConverterTool = new CoordsConverterTool(baseFrame, toolPanel);
			coordsConverterTool.init();
			DateConverterTool dateConverterTool = new DateConverterTool(baseFrame, toolPanel);
			dateConverterTool.init();

			if (visible) {
				tabbedPane.addTab(TAB_NAME, new JScrollPane(toolPanel));
			}
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		}
	}

}
