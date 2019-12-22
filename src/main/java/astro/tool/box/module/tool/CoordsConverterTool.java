package astro.tool.box.module.tool;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
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
            JPanel mainPanel = new JPanel(new GridLayout(4, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Coordinates converter", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(350, 125));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(createLabel("Coordinates: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField coordsField = createField("", PLAIN_FONT);
            mainPanel.add(coordsField);

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
                    String[] parts = splitCoordinates(coordsField.getText());
                    double degRA = Double.valueOf(parts[0].trim());
                    double degDE = Double.valueOf(parts[1].trim());
                    if (coordsSystem.equals(CoordsSystem.DECIMAL)) {
                        result = degRA + " " + degDE;
                    } else {
                        result = convertToSexagesimalCoords(degRA, degDE);
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
