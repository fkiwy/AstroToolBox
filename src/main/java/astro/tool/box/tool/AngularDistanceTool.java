package astro.tool.box.tool;

import static astro.tool.box.function.AstrometricFunctions.calculateAngularDistance;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo6DecNZ;
import static astro.tool.box.main.ToolboxHelper.getCoordinates;
import static astro.tool.box.main.ToolboxHelper.showErrorDialog;
import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;
import static astro.tool.box.util.ConversionFactors.DEG_ARCSEC;
import static astro.tool.box.util.ConversionFactors.DEG_MAS;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import astro.tool.box.enumeration.Unit;

public class AngularDistanceTool {

	private final JFrame baseFrame;
	private final JPanel toolPanel;

	public AngularDistanceTool(JFrame baseFrame, JPanel toolPanel) {
		this.baseFrame = baseFrame;
		this.toolPanel = toolPanel;
	}

	public void init() {
		try {
			JPanel mainPanel = new JPanel(new GridLayout(5, 2));
			mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"Angular distance calculator", TitledBorder.LEFT, TitledBorder.TOP));
			mainPanel.setPreferredSize(new Dimension(375, 150));

			JPanel containerPanel = new JPanel();
			containerPanel.add(mainPanel);
			toolPanel.add(containerPanel);

			mainPanel.add(new JLabel("Coordinates object 1 (deg): ", SwingConstants.RIGHT));
			JTextField fromCoordsField = new JTextField();
			mainPanel.add(fromCoordsField);

			mainPanel.add(new JLabel("Coordinates object 2 (deg): ", SwingConstants.RIGHT));
			JTextField toCoordsField = new JTextField();
			mainPanel.add(toCoordsField);

			mainPanel.add(new JLabel("Angular distance unit: ", SwingConstants.RIGHT));
			JComboBox resultUnits = new JComboBox(new Unit[] { Unit.ARCSEC, Unit.MAS });
			mainPanel.add(resultUnits);

			mainPanel.add(new JLabel("Angular distance: ", SwingConstants.RIGHT));
			JTextField resultField = new JTextField();
			resultField.setEditable(false);
			mainPanel.add(resultField);

			mainPanel.add(new JLabel());
			JButton calculateButton = new JButton("Calculate");
			calculateButton.addActionListener((ActionEvent e) -> {
				try {
					Unit resultUnit = (Unit) resultUnits.getSelectedItem();
					double conversionFactor;
					if (resultUnit.equals(Unit.ARCSEC)) {
						conversionFactor = DEG_ARCSEC;
					} else {
						conversionFactor = DEG_MAS;
					}
					double angularDistance = calculateAngularDistance(getCoordinates(fromCoordsField.getText()),
							getCoordinates(toCoordsField.getText()), conversionFactor);
					String result;
					if (resultUnit.equals(Unit.ARCSEC)) {
						result = roundTo6DecNZ(angularDistance);
					} else {
						result = roundTo3DecNZ(angularDistance);
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
