package astro.tool.box.tool;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.main.ModuleHelper.*;
import astro.tool.box.container.NumberPair;
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

public class ProperMotionsTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public ProperMotionsTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(7, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Proper motions calculator", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 200));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("Coordinates epoch 1 (deg): ", JLabel.RIGHT));
            JTextField fromCoordsField = new JTextField();
            mainPanel.add(fromCoordsField);

            mainPanel.add(new JLabel("Coordinates epoch 2 (deg): ", JLabel.RIGHT));
            JTextField toCoordsField = new JTextField();
            mainPanel.add(toCoordsField);

            mainPanel.add(new JLabel("Elapsed time unit: ", JLabel.RIGHT));
            JComboBox timeUnits = new JComboBox(new Unit[]{Unit.YEAR, Unit.DAY});
            mainPanel.add(timeUnits);

            mainPanel.add(new JLabel("Elapsed time: ", JLabel.RIGHT));
            JTextField elapsedTimeField = new JTextField();
            mainPanel.add(elapsedTimeField);

            mainPanel.add(new JLabel("Proper motions unit: ", JLabel.RIGHT));
            JComboBox resultUnits = new JComboBox(new Unit[]{Unit.ARCSEC, Unit.MAS});
            mainPanel.add(resultUnits);

            mainPanel.add(new JLabel("Proper motions (pmra, pmdec): ", JLabel.RIGHT));
            JTextField resultField = new JTextField();
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener((ActionEvent e) -> {
                try {
                    double elapsedTime = toDouble(elapsedTimeField.getText());
                    Unit timeUnit = (Unit) timeUnits.getSelectedItem();
                    int fromDays = 0, toDays;
                    if (timeUnit.equals(Unit.YEAR)) {
                        toDays = fromDoubleToInteger(elapsedTime * 365);
                    } else {
                        toDays = fromDoubleToInteger(elapsedTime);
                    }
                    Unit resultUnit = (Unit) resultUnits.getSelectedItem();
                    double conversionFactor;
                    if (resultUnit.equals(Unit.ARCSEC)) {
                        conversionFactor = DEG_ARCSEC;
                    } else {
                        conversionFactor = DEG_MAS;
                    }
                    NumberPair properMotions = calculateProperMotions(getCoordinates(fromCoordsField.getText()),
                            getCoordinates(toCoordsField.getText()),
                            fromDays,
                            toDays,
                            conversionFactor
                    );
                    String pmra;
                    String pmdec;
                    if (resultUnit.equals(Unit.ARCSEC)) {
                        pmra = roundTo6DecNZ(properMotions.getX());
                        pmdec = roundTo6DecNZ(properMotions.getY());
                    } else {
                        pmra = roundTo3DecNZ(properMotions.getX());
                        pmdec = roundTo3DecNZ(properMotions.getY());
                    }
                    resultField.setText(pmra + ", " + pmdec);
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
