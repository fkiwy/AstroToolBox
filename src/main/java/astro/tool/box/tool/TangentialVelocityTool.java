package astro.tool.box.tool;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.main.ModuleHelper.*;
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

public class TangentialVelocityTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public TangentialVelocityTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(6, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Tangential velocity calculator", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 175));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("Proper motion in RA (mas/yr): ", JLabel.RIGHT));
            JTextField pmraField = new JTextField();
            mainPanel.add(pmraField);

            mainPanel.add(new JLabel("Proper motion in dec (mas/yr): ", JLabel.RIGHT));
            JTextField pmdecField = new JTextField();
            mainPanel.add(pmdecField);

            mainPanel.add(new JLabel("Parallax (mas): ", JLabel.RIGHT));
            JTextField parallaxField = new JTextField();
            mainPanel.add(parallaxField);

            mainPanel.add(new JLabel("or Distance (pc): ", JLabel.RIGHT));
            JTextField distanceField = new JTextField();
            mainPanel.add(distanceField);

            mainPanel.add(new JLabel("Tangential velocity (km/s): ", JLabel.RIGHT));
            JTextField resultField = new JTextField();
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener((ActionEvent e) -> {
                try {
                    double tangentialVelocity;
                    double parallax = toDouble(parallaxField.getText());
                    double distance = toDouble(distanceField.getText());
                    if (distance == 0) {
                        tangentialVelocity = calculateTangentialVelocityFromParallax(
                                toDouble(pmraField.getText()),
                                toDouble(pmdecField.getText()),
                                parallax
                        );
                    } else {
                        tangentialVelocity = calculateTangentialVelocityFromDistance(
                                toDouble(pmraField.getText()),
                                toDouble(pmdecField.getText()),
                                distance
                        );
                    }
                    resultField.setText(roundTo3DecNZ(tangentialVelocity));
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
