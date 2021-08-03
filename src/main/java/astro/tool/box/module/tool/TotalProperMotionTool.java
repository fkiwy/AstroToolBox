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
                    BorderFactory.createEtchedBorder(), "Total proper motion calculator", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 125));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("Proper motion in RA: ", JLabel.RIGHT));
            JTextField pmraField = new JTextField();
            mainPanel.add(pmraField);

            mainPanel.add(new JLabel("Proper motion in dec: ", JLabel.RIGHT));
            JTextField pmdecField = new JTextField();
            mainPanel.add(pmdecField);

            mainPanel.add(new JLabel("Total proper motion: ", JLabel.RIGHT));
            JTextField resultField = new JTextField();
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
