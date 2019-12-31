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

public class TransverseVelocityTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public TransverseVelocityTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(6, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Calculate tangential velocity", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 175));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(createLabel("Proper motion in RA (mas/yr): ", DEFAULT_FONT, JLabel.RIGHT));
            JTextField pmraField = createField("", DEFAULT_FONT);
            mainPanel.add(pmraField);

            mainPanel.add(createLabel("Proper motion in dec (mas/yr): ", DEFAULT_FONT, JLabel.RIGHT));
            JTextField pmdecField = createField("", DEFAULT_FONT);
            mainPanel.add(pmdecField);

            mainPanel.add(createLabel("Parallax (mas): ", DEFAULT_FONT, JLabel.RIGHT));
            JTextField parallaxField = createField("", DEFAULT_FONT);
            mainPanel.add(parallaxField);

            mainPanel.add(createLabel("or Distance (pc): ", DEFAULT_FONT, JLabel.RIGHT));
            JTextField distanceField = createField("", DEFAULT_FONT);
            mainPanel.add(distanceField);

            mainPanel.add(createLabel("Tangential velocity (km/s): ", DEFAULT_FONT, JLabel.RIGHT));
            JTextField resultField = createField("", DEFAULT_FONT);
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener((ActionEvent e) -> {
                try {
                    double transverseVelocity;
                    double parallax = toDouble(parallaxField.getText());
                    double distance = toDouble(distanceField.getText());
                    if (distance == 0) {
                        transverseVelocity = calculateTransverseVelocityFromParallax(
                                toDouble(pmraField.getText()),
                                toDouble(pmdecField.getText()),
                                parallax
                        );
                    } else {
                        transverseVelocity = calculateTransverseVelocityFromDistance(
                                toDouble(pmraField.getText()),
                                toDouble(pmdecField.getText()),
                                distance
                        );
                    }
                    resultField.setText(roundTo3DecNZ(transverseVelocity));
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
