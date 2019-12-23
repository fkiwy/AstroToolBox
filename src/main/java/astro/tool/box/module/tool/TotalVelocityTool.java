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

public class TotalVelocityTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public TotalVelocityTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(4, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Calculate total velocity", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 125));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(createLabel("Tangential velocity (km/s): ", PLAIN_FONT, JLabel.RIGHT));
            JTextField tangVelocityField = createField("", PLAIN_FONT);
            mainPanel.add(tangVelocityField);

            mainPanel.add(createLabel("Radial velocity (km/s): ", PLAIN_FONT, JLabel.RIGHT));
            JTextField radVelocityField = createField("", PLAIN_FONT);
            mainPanel.add(radVelocityField);

            mainPanel.add(createLabel("Total velocity (km/s): ", PLAIN_FONT, JLabel.RIGHT));
            JTextField resultField = createField("", PLAIN_FONT);
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener((ActionEvent e) -> {
                try {
                    double totalVelocity = calculateTotalVelocity(
                            toDouble(tangVelocityField.getText()),
                            toDouble(radVelocityField.getText())
                    );
                    resultField.setText(roundTo3DecNZ(totalVelocity));
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
