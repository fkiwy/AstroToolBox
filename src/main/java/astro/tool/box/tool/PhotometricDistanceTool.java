package astro.tool.box.tool;

import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.function.PhotometricFunctions.calculatePhotometricDistance;
import static astro.tool.box.function.PhotometricFunctions.calculatePhotometricDistanceError;
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

public class PhotometricDistanceTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public PhotometricDistanceTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(4, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Photometric distance calculator", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 125));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("Apparent magnitude: ", SwingConstants.RIGHT));
            JTextField apparentMagnitudeField = new JTextField();
            mainPanel.add(apparentMagnitudeField);

            mainPanel.add(new JLabel("Absolute magnitude: ", SwingConstants.RIGHT));
            JTextField absoluteMagnitudeField = new JTextField();
            mainPanel.add(absoluteMagnitudeField);

            mainPanel.add(new JLabel("Distance (pc): ", SwingConstants.RIGHT));
            JTextField resultField = new JTextField();
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener((ActionEvent e) -> {
                try {
                    double apparentMagnitude;
                    double apparentMagnitudeError = 0;
                    double absoluteMagnitude;
                    double absoluteMagnitudeError = 0;
                    if (apparentMagnitudeField.getText().contains(" ")) {
                        String[] apparentMagnitudeData = apparentMagnitudeField.getText().split(" ");
                        apparentMagnitude = toDouble(apparentMagnitudeData[0]);
                        apparentMagnitudeError = toDouble(apparentMagnitudeData[1]);
                    } else {
                        apparentMagnitude = toDouble(apparentMagnitudeField.getText());
                    }
                    if (absoluteMagnitudeField.getText().contains(" ")) {
                        String[] absoluteMagnitudeData = absoluteMagnitudeField.getText().split(" ");
                        absoluteMagnitude = toDouble(absoluteMagnitudeData[0]);
                        absoluteMagnitudeError = toDouble(absoluteMagnitudeData[1]);
                    } else {
                        absoluteMagnitude = toDouble(absoluteMagnitudeField.getText());
                    }
                    double distance = calculatePhotometricDistance(apparentMagnitude, absoluteMagnitude);
                    double distanceError = calculatePhotometricDistanceError(apparentMagnitude, apparentMagnitudeError, absoluteMagnitude, absoluteMagnitudeError);
                    String result = roundTo3DecNZ(distance);
                    if (distanceError != 0) {
                        result += "±" + roundTo3DecNZ(distanceError);
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
