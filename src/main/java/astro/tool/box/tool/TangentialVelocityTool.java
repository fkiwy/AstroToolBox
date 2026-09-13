package astro.tool.box.tool;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

import static astro.tool.box.function.AstrometricFunctions.calculateTangentialVelocityFromDistance;
import static astro.tool.box.function.AstrometricFunctions.calculateTangentialVelocityFromParallax;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.main.ToolboxHelper.showErrorDialog;
import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;

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
			mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"Tangential velocity calculator", TitledBorder.LEFT, TitledBorder.TOP));
			mainPanel.setPreferredSize(new Dimension(375, 175));

			JPanel containerPanel = new JPanel();
			containerPanel.add(mainPanel);
			toolPanel.add(containerPanel);

			mainPanel.add(new JLabel("Proper motion in RA (mas/yr): ", SwingConstants.RIGHT));
			JTextField pmraField = new JTextField();
			mainPanel.add(pmraField);

			mainPanel.add(new JLabel("Proper motion in dec (mas/yr): ", SwingConstants.RIGHT));
			JTextField pmdecField = new JTextField();
			mainPanel.add(pmdecField);

			mainPanel.add(new JLabel("Parallax (mas): ", SwingConstants.RIGHT));
			JTextField parallaxField = new JTextField();
			mainPanel.add(parallaxField);

			mainPanel.add(new JLabel("or Distance (pc): ", SwingConstants.RIGHT));
			JTextField distanceField = new JTextField();
			mainPanel.add(distanceField);

			mainPanel.add(new JLabel("Tangential velocity (km/s): ", SwingConstants.RIGHT));
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
						tangentialVelocity = calculateTangentialVelocityFromParallax(toDouble(pmraField.getText()),
								toDouble(pmdecField.getText()), parallax);
					} else {
						tangentialVelocity = calculateTangentialVelocityFromDistance(toDouble(pmraField.getText()),
								toDouble(pmdecField.getText()), distance);
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
