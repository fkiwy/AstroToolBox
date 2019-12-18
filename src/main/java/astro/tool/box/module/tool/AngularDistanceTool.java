package astro.tool.box.module.tool;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.enumeration.Unit;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class AngularDistanceTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public AngularDistanceTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(5, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Calculate angular distance", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(350, 150));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(createLabel("From coordinates (deg): ", PLAIN_FONT, JLabel.RIGHT));
            JTextField fromCoordsField = createField("", PLAIN_FONT);
            mainPanel.add(fromCoordsField);

            mainPanel.add(createLabel("To coordinates (deg): ", PLAIN_FONT, JLabel.RIGHT));
            JTextField toCoordsField = createField("", PLAIN_FONT);
            mainPanel.add(toCoordsField);

            mainPanel.add(createLabel("Angular distance unit: ", PLAIN_FONT, JLabel.RIGHT));
            JComboBox<Unit> resultUnits = new JComboBox<>(new Unit[]{Unit.ARCSEC, Unit.MAS});
            mainPanel.add(resultUnits);

            mainPanel.add(createLabel("Angular distance: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField resultField = createField("", PLAIN_FONT);
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener((ActionEvent e) -> {
                try {
                    Unit resultUnit = (Unit) resultUnits.getSelectedItem();
                    double conversionFactor;
                    if (resultUnit.equals(Unit.ARCSEC)) {
                        conversionFactor = DEG_ARCSEC;
                    } else {
                        conversionFactor = DEG_MAS;
                    }
                    double angularDistance = calculateAngularDistance(getCoordinates(fromCoordsField.getText()),
                            getCoordinates(toCoordsField.getText()),
                            conversionFactor
                    );
                    String result;
                    if (resultUnit.equals(Unit.ARCSEC)) {
                        result = roundTo6DecNZ(angularDistance);
                    } else {
                        result = roundTo3DecNZ(angularDistance);
                    }
                    resultField.setText(result);
                } catch (Exception ex) {
                    showErrorDialog(baseFrame, "Invalid input!");
                }
            });
            mainPanel.add(calculateButton);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

}
