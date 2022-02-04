package astro.tool.box.tool;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.main.ModuleHelper.*;
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

public class UnitConverterTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public UnitConverterTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(5, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Unit converter", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 150));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("Value to convert: ", JLabel.RIGHT));
            JTextField valueToConvert = new JTextField();
            mainPanel.add(valueToConvert);

            mainPanel.add(new JLabel("Convert from: ", JLabel.RIGHT));
            JComboBox unitsToConvertFrom = new JComboBox(new Unit[]{Unit.DEGREE, Unit.ARCSEC, Unit.MAS});
            mainPanel.add(unitsToConvertFrom);

            mainPanel.add(new JLabel("To: ", JLabel.RIGHT));
            JComboBox unitsToConvertTo = new JComboBox(new Unit[]{Unit.DEGREE, Unit.ARCSEC, Unit.MAS});
            unitsToConvertTo.setSelectedItem(Unit.MAS);
            mainPanel.add(unitsToConvertTo);

            mainPanel.add(new JLabel("Converted value: ", JLabel.RIGHT));
            JTextField convertedValue = new JTextField();
            convertedValue.setEditable(false);
            mainPanel.add(convertedValue);

            mainPanel.add(new JLabel());
            JButton convertButton = new JButton("Convert");
            convertButton.addActionListener((ActionEvent e) -> {
                try {
                    double converted = convertToUnit(toDouble(valueToConvert.getText()),
                            (Unit) unitsToConvertFrom.getSelectedItem(),
                            (Unit) unitsToConvertTo.getSelectedItem()
                    );
                    convertedValue.setText(roundTo9DecNZ(converted));
                } catch (Exception ex) {
                    showErrorDialog(baseFrame, "Invalid input!");
                }
            });
            mainPanel.add(convertButton);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

}
