package astro.tool.box.tool;

import static astro.tool.box.function.AstrometricFunctions.calculateTotalVelocity;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
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
                    BorderFactory.createEtchedBorder(), "Total velocity calculator", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 125));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("Tangential velocity (km/s): ", SwingConstants.RIGHT));
            JTextField tangVelocityField = new JTextField();
            mainPanel.add(tangVelocityField);

            mainPanel.add(new JLabel("Radial velocity (km/s): ", SwingConstants.RIGHT));
            JTextField radVelocityField = new JTextField();
            mainPanel.add(radVelocityField);

            mainPanel.add(new JLabel("Total velocity (km/s): ", SwingConstants.RIGHT));
            JTextField resultField = new JTextField();
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
