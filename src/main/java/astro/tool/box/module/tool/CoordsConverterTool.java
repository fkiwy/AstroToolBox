package astro.tool.box.module.tool;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.StringPair;
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
            mainPanel.setPreferredSize(new Dimension(375, 150));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(createLabel("Coordinates to convert: ", DEFAULT_FONT, JLabel.RIGHT));
            JTextField coordsToConvert = createField("", DEFAULT_FONT);
            mainPanel.add(coordsToConvert);

            mainPanel.add(createLabel("Convert from: ", DEFAULT_FONT, JLabel.RIGHT));
            JComboBox<CoordsSystem> systemsToConvertFrom = new JComboBox<>(new CoordsSystem[]{CoordsSystem.DECIMAL, CoordsSystem.SEXAGESIMAL});
            mainPanel.add(systemsToConvertFrom);

            mainPanel.add(createLabel("To: ", DEFAULT_FONT, JLabel.RIGHT));
            JComboBox<CoordsSystem> systemsToConvertTo = new JComboBox<>(new CoordsSystem[]{CoordsSystem.DECIMAL, CoordsSystem.SEXAGESIMAL});
            systemsToConvertTo.setSelectedItem(CoordsSystem.SEXAGESIMAL);
            mainPanel.add(systemsToConvertTo);

            systemsToConvertFrom.addActionListener((ActionEvent evt) -> {
                CoordsSystem coordsSystem = (CoordsSystem) systemsToConvertFrom.getSelectedItem();
                if (coordsSystem.equals(CoordsSystem.DECIMAL)) {
                    systemsToConvertTo.setSelectedItem(CoordsSystem.SEXAGESIMAL);
                } else {
                    systemsToConvertTo.setSelectedItem(CoordsSystem.DECIMAL);
                }
            });

            systemsToConvertTo.addActionListener((ActionEvent evt) -> {
                CoordsSystem coordsSystem = (CoordsSystem) systemsToConvertTo.getSelectedItem();
                if (coordsSystem.equals(CoordsSystem.DECIMAL)) {
                    systemsToConvertFrom.setSelectedItem(CoordsSystem.SEXAGESIMAL);
                } else {
                    systemsToConvertFrom.setSelectedItem(CoordsSystem.DECIMAL);
                }
            });

            mainPanel.add(createLabel("Converted coordinates: ", DEFAULT_FONT, JLabel.RIGHT));
            JTextField convertedCoords = createField("", DEFAULT_FONT);
            convertedCoords.setEditable(false);
            mainPanel.add(convertedCoords);

            mainPanel.add(new JLabel());
            JButton convertButton = new JButton("Convert");
            convertButton.addActionListener((ActionEvent e) -> {
                try {
                    String converted;
                    CoordsSystem coordsSystem = (CoordsSystem) systemsToConvertTo.getSelectedItem();
                    NumberPair coordinates = getCoordinates(coordsToConvert.getText());
                    if (coordsSystem.equals(CoordsSystem.DECIMAL)) {
                        converted = coordinates.getX() + " " + coordinates.getY();
                    } else {
                        StringPair strings = convertToSexagesimalCoords(coordinates.getX(), coordinates.getY());
                        converted = strings.getS1() + " " + strings.getS2();
                    }
                    convertedCoords.setText(converted);
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
