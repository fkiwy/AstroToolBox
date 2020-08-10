package astro.tool.box.module.tool;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class LinearDistanceTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public LinearDistanceTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(6, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Calculate linear distance", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 175));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("From coordinates (deg): ", JLabel.RIGHT));
            JTextField fromCoordsField = new JTextField();
            mainPanel.add(fromCoordsField);

            mainPanel.add(new JLabel("To coordinates (deg): ", JLabel.RIGHT));
            JTextField toCoordsField = new JTextField();
            mainPanel.add(toCoordsField);

            mainPanel.add(new JLabel("From parallax (mas): ", JLabel.RIGHT));
            JTextField fromParallaxField = new JTextField();
            mainPanel.add(fromParallaxField);

            mainPanel.add(new JLabel("To parallax (mas): ", JLabel.RIGHT));
            JTextField toParallaxField = new JTextField();
            mainPanel.add(toParallaxField);

            mainPanel.add(new JLabel("Linear distance (pc): ", JLabel.RIGHT));
            JTextField resultField = new JTextField();
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener((ActionEvent e) -> {
                try {
                    double linearDistance = calculateLinearDistance(getCoordinates(fromCoordsField.getText()),
                            getCoordinates(toCoordsField.getText()),
                            toDouble(fromParallaxField.getText()),
                            toDouble(toParallaxField.getText())
                    );
                    resultField.setText(roundTo6DecNZ(linearDistance));
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
