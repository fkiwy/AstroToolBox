package astro.tool.box.module.tab;

import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.module.tool.TotalProperMotionTool;
import astro.tool.box.module.tool.ParallaxDistanceTool;
import astro.tool.box.module.tool.ProperMotionsTool;
import astro.tool.box.module.tool.UnitConverterTool;
import astro.tool.box.module.tool.TransverseVelocityTool;
import astro.tool.box.module.tool.AbsoluteMagnitudeTool;
import astro.tool.box.module.tool.CoordsConverterTool;
import astro.tool.box.module.tool.TotalVelocityTool;
import astro.tool.box.module.tool.AngularDistanceTool;
import astro.tool.box.module.tool.DateConverterTool;
import astro.tool.box.module.tool.PhotometricDistanceTool;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class ToolTab {

    private static final String TAB_NAME = "Calculators & Converters";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    public ToolTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
    }

    public void init() {
        try {
            JPanel toolPanel = new JPanel(new GridLayout(4, 3));
            ProperMotionsTool properMotionsTool = new ProperMotionsTool(baseFrame, toolPanel);
            properMotionsTool.init();
            AngularDistanceTool angularDistanceTool = new AngularDistanceTool(baseFrame, toolPanel);
            angularDistanceTool.init();
            TotalProperMotionTool totalProperMotionTool = new TotalProperMotionTool(baseFrame, toolPanel);
            totalProperMotionTool.init();
            TransverseVelocityTool transverseVelocityTool = new TransverseVelocityTool(baseFrame, toolPanel);
            transverseVelocityTool.init();
            TotalVelocityTool totalVelocityTool = new TotalVelocityTool(baseFrame, toolPanel);
            totalVelocityTool.init();
            ParallaxDistanceTool parallaxlDistanceTool = new ParallaxDistanceTool(baseFrame, toolPanel);
            parallaxlDistanceTool.init();
            PhotometricDistanceTool photometricDistanceTool = new PhotometricDistanceTool(baseFrame, toolPanel);
            photometricDistanceTool.init();
            AbsoluteMagnitudeTool absoluteMagnitudeTool = new AbsoluteMagnitudeTool(baseFrame, toolPanel);
            absoluteMagnitudeTool.init();
            UnitConverterTool unitConverterTool = new UnitConverterTool(baseFrame, toolPanel);
            unitConverterTool.init();
            CoordsConverterTool coordsConverterTool = new CoordsConverterTool(baseFrame, toolPanel);
            coordsConverterTool.init();
            DateConverterTool dateConverterTool = new DateConverterTool(baseFrame, toolPanel);
            dateConverterTool.init();
            tabbedPane.addTab(TAB_NAME, new JScrollPane(toolPanel));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

}
