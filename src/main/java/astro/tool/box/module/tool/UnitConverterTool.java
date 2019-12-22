package astro.tool.box.module.tool;

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
            mainPanel.setPreferredSize(new Dimension(350, 150));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(createLabel("Value: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField toConvertField = createField("", PLAIN_FONT);
            mainPanel.add(toConvertField);

            mainPanel.add(createLabel("Convert from: ", PLAIN_FONT, JLabel.RIGHT));
            JComboBox<Unit> toConvertUnits = new JComboBox<>(new Unit[]{Unit.DEGREE, Unit.ARCSEC, Unit.MAS});
            mainPanel.add(toConvertUnits);

            mainPanel.add(createLabel("To: ", PLAIN_FONT, JLabel.RIGHT));
            JComboBox<Unit> convertedUnits = new JComboBox<>(new Unit[]{Unit.DEGREE, Unit.ARCSEC, Unit.MAS});
            mainPanel.add(convertedUnits);

            mainPanel.add(createLabel("Converted value: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField convertedField = createField("", PLAIN_FONT);
            convertedField.setEditable(false);
            mainPanel.add(convertedField);

            mainPanel.add(new JLabel());
            JButton convertButton = new JButton("Convert");
            convertButton.addActionListener((ActionEvent e) -> {
                try {
                    double converted = convertToUnit(
                            toDouble(toConvertField.getText()),
                            (Unit) toConvertUnits.getSelectedItem(),
                            (Unit) convertedUnits.getSelectedItem()
                    );
                    convertedField.setText(roundTo9DecNZ(converted));
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
