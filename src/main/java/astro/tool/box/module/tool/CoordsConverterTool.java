package astro.tool.box.module.tool;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.CoordsSystem;
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

public class CoordsConverterTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public CoordsConverterTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(5, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Coordinates converter", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(350, 150));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(createLabel("Right ascension: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField raField = createField("", PLAIN_FONT);
            mainPanel.add(raField);

            mainPanel.add(createLabel("Declination: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField decField = createField("", PLAIN_FONT);
            mainPanel.add(decField);

            mainPanel.add(createLabel("Convert to: ", PLAIN_FONT, JLabel.RIGHT));
            JComboBox<CoordsSystem> coordsSystems = new JComboBox<>(new CoordsSystem[]{CoordsSystem.DECIMAL, CoordsSystem.SEXAGESIMAL});
            mainPanel.add(coordsSystems);

            mainPanel.add(createLabel("Converted coordinates: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField resultField = createField("", PLAIN_FONT);
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton convertButton = new JButton("Convert");
            convertButton.addActionListener((ActionEvent e) -> {
                try {
                    CoordsSystem coordsSystem = (CoordsSystem) coordsSystems.getSelectedItem();
                    String result;
                    if (coordsSystem.equals(CoordsSystem.DECIMAL)) {
                        NumberPair converted = convertToDecimalCoords(raField.getText(), decField.getText());
                        result = roundTo7DecNZ(converted.getX()) + " " + roundTo7DecNZ(converted.getY());
                    } else {
                        result = convertToSexagesimalCoords(toDouble(raField.getText()), toDouble(decField.getText()));
                    }
                    resultField.setText(result);
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
