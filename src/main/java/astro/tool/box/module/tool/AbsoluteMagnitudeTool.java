package astro.tool.box.module.tool;

import static astro.tool.box.function.PhotometricFunctions.*;
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

public class AbsoluteMagnitudeTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public AbsoluteMagnitudeTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(5, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Calculate absolute magnitude", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 150));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("Apparent magnitude: ", JLabel.RIGHT));
            JTextField apparentMagField = new JTextField("");
            mainPanel.add(apparentMagField);

            mainPanel.add(new JLabel("Parallax (mas): ", JLabel.RIGHT));
            JTextField parallaxField = new JTextField("");
            mainPanel.add(parallaxField);

            mainPanel.add(new JLabel("or Distance (pc): ", JLabel.RIGHT));
            JTextField distanceField = new JTextField("");
            mainPanel.add(distanceField);

            mainPanel.add(new JLabel("Absolute magnitude: ", JLabel.RIGHT));
            JTextField resultField = new JTextField("");
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener((ActionEvent e) -> {
                try {
                    double absoluteMagnitude;
                    double parallax = toDouble(parallaxField.getText());
                    double distance = toDouble(distanceField.getText());
                    if (distance == 0) {
                        absoluteMagnitude = calculateAbsoluteMagnitudeFromParallax(
                                toDouble(apparentMagField.getText()),
                                parallax
                        );
                    } else {
                        absoluteMagnitude = calculateAbsoluteMagnitudeFromDistance(
                                toDouble(apparentMagField.getText()),
                                distance
                        );
                    }
                    resultField.setText(roundTo3DecNZ(absoluteMagnitude));
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
