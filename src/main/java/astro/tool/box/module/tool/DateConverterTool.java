package astro.tool.box.module.tool;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.enumeration.DateSystem;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class DateConverterTool {

    private final JFrame baseFrame;
    private final JPanel toolPanel;

    public DateConverterTool(JFrame baseFrame, JPanel toolPanel) {
        this.baseFrame = baseFrame;
        this.toolPanel = toolPanel;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new GridLayout(6, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Date converter", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(350, 175));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(createLabel("Date: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField dateField = createField("", PLAIN_FONT);
            mainPanel.add(dateField);

            mainPanel.add(createLabel("Calendar date format: ", PLAIN_FONT, JLabel.RIGHT));
            mainPanel.add(createLabel("2010-12-31 18:10:30", PLAIN_FONT));

            mainPanel.add(createLabel("", PLAIN_FONT, JLabel.RIGHT));
            mainPanel.add(createLabel("Time is not required.", PLAIN_FONT));

            mainPanel.add(createLabel("Convert to: ", PLAIN_FONT, JLabel.RIGHT));
            JComboBox<DateSystem> dateSystems = new JComboBox<>(new DateSystem[]{DateSystem.CALENDAR_DATE, DateSystem.MODIFIED_JULIAN_DATE});
            mainPanel.add(dateSystems);

            mainPanel.add(createLabel("Converted date: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField resultField = createField("", PLAIN_FONT);
            resultField.setEditable(false);
            mainPanel.add(resultField);

            mainPanel.add(new JLabel());
            JButton convertButton = new JButton("Convert");
            convertButton.addActionListener((ActionEvent e) -> {
                try {
                    DateSystem dateSystem = (DateSystem) dateSystems.getSelectedItem();
                    String result;
                    String input = dateField.getText().trim();
                    if (dateSystem.equals(DateSystem.CALENDAR_DATE)) {
                        result = convertMJDToDateTime(new BigDecimal(input)).format(DATE_TIME_FORMATTER);
                    } else {
                        LocalDateTime dateTime;
                        try {
                            dateTime = LocalDateTime.parse(input, DATE_TIME_FORMATTER);
                        } catch (DateTimeParseException ex) {
                            dateTime = LocalDateTime.parse(input + " 00:00:00", DATE_TIME_FORMATTER);
                        }
                        result = convertDateTimeToMJD(dateTime).toString();
                    }
                    resultField.setText(result);
                } catch (Exception ex) {
                    showErrorDialog(baseFrame, "Invalid input!");
                }
            });
            mainPanel.add(convertButton);
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

}
