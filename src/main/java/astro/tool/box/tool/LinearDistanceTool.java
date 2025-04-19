package astro.tool.box.tool;

import static astro.tool.box.function.AstrometricFunctions.calculateLinearDistance;
import static astro.tool.box.function.NumericFunctions.roundTo6DecNZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.main.ToolboxHelper.getCoordinates;
import static astro.tool.box.main.ToolboxHelper.showErrorDialog;
import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
                    BorderFactory.createEtchedBorder(), "Linear distance calculator", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 175));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("Coordinates object 1 (deg): ", SwingConstants.RIGHT));
            JTextField fromCoordsField = new JTextField();
            mainPanel.add(fromCoordsField);

            mainPanel.add(new JLabel("Coordinates object 2 (deg): ", SwingConstants.RIGHT));
            JTextField toCoordsField = new JTextField();
            mainPanel.add(toCoordsField);

            mainPanel.add(new JLabel("Parallax object 1 (mas): ", SwingConstants.RIGHT));
            JTextField fromParallaxField = new JTextField();
            mainPanel.add(fromParallaxField);

            mainPanel.add(new JLabel("Parallax object 2 (mas): ", SwingConstants.RIGHT));
            JTextField toParallaxField = new JTextField();
            mainPanel.add(toParallaxField);

            mainPanel.add(new JLabel("Linear distance (pc): ", SwingConstants.RIGHT));
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
