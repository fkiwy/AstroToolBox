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

public class TotalProperMotionTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public TotalProperMotionTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(4, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Calculate total proper motion", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 125));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(createLabel("Proper motion in RA: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField pmraField = createField("", PLAIN_FONT);
            mainPanel.add(pmraField);

            mainPanel.add(createLabel("Proper motion in dec: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField pmdecField = createField("", PLAIN_FONT);
            mainPanel.add(pmdecField);

            mainPanel.add(createLabel("Total proper motion: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField resultField = createField("", PLAIN_FONT);
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton calculateButton = new JButton("Calculate");
            calculateButton.addActionListener((ActionEvent e) -> {
                try {
                    double totalProperMotion = calculateTotalProperMotion(
                            toDouble(pmraField.getText()),
                            toDouble(pmdecField.getText())
                    );
                    resultField.setText(roundTo3DecNZ(totalProperMotion));
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
