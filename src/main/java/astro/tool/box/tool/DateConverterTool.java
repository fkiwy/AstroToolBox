package astro.tool.box.tool;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.main.ModuleHelper.*;
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
            JPanel mainPanel = new JPanel(new GridLayout(7, 2));
            mainPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Date converter", TitledBorder.LEFT, TitledBorder.TOP
            ));
            mainPanel.setPreferredSize(new Dimension(375, 200));

            JPanel containerPanel = new JPanel();
            containerPanel.add(mainPanel);
            toolPanel.add(containerPanel);

            mainPanel.add(new JLabel("Date to convert: ", JLabel.RIGHT));
            JTextField dateToConvert = new JTextField();
            mainPanel.add(dateToConvert);

            mainPanel.add(new JLabel("Calendar date format: ", JLabel.RIGHT));
            mainPanel.add(new JLabel("yyyy-MM-dd HH:mm:ss"));

            mainPanel.add(new JLabel("", JLabel.RIGHT));
            mainPanel.add(new JLabel("Time is not required."));

            mainPanel.add(new JLabel("Convert from: ", JLabel.RIGHT));
            JComboBox systemsToConvertFrom = new JComboBox(new DateSystem[]{DateSystem.CALENDAR_DATE, DateSystem.MODIFIED_JULIAN_DATE});
            mainPanel.add(systemsToConvertFrom);

            mainPanel.add(new JLabel("To: ", JLabel.RIGHT));
            JComboBox systemsToConvertTo = new JComboBox(new DateSystem[]{DateSystem.CALENDAR_DATE, DateSystem.MODIFIED_JULIAN_DATE});
            systemsToConvertTo.setSelectedItem(DateSystem.MODIFIED_JULIAN_DATE);
            mainPanel.add(systemsToConvertTo);

            systemsToConvertFrom.addActionListener((ActionEvent evt) -> {
                DateSystem dateSystem = (DateSystem) systemsToConvertFrom.getSelectedItem();
                if (dateSystem.equals(DateSystem.CALENDAR_DATE)) {
                    systemsToConvertTo.setSelectedItem(DateSystem.MODIFIED_JULIAN_DATE);
                } else {
                    systemsToConvertTo.setSelectedItem(DateSystem.CALENDAR_DATE);
                }
            });

            systemsToConvertTo.addActionListener((ActionEvent evt) -> {
                DateSystem dateSystem = (DateSystem) systemsToConvertTo.getSelectedItem();
                if (dateSystem.equals(DateSystem.CALENDAR_DATE)) {
                    systemsToConvertFrom.setSelectedItem(DateSystem.MODIFIED_JULIAN_DATE);
                } else {
                    systemsToConvertFrom.setSelectedItem(DateSystem.CALENDAR_DATE);
                }
            });

            mainPanel.add(new JLabel("Converted date: ", JLabel.RIGHT));
            JTextField convertedDate = new JTextField();
            convertedDate.setEditable(false);
            mainPanel.add(convertedDate);

            mainPanel.add(new JLabel());
            JButton convertButton = new JButton("Convert");
            convertButton.addActionListener((ActionEvent e) -> {
                try {
                    String converted;
                    DateSystem dateSystem = (DateSystem) systemsToConvertTo.getSelectedItem();
                    String input = dateToConvert.getText().trim();
                    if (dateSystem.equals(DateSystem.CALENDAR_DATE)) {
                        converted = convertMJDToDateTime(new BigDecimal(input)).format(DATE_TIME_FORMATTER);
                    } else {
                        LocalDateTime dateTime;
                        try {
                            dateTime = LocalDateTime.parse(input, DATE_TIME_FORMATTER);
                        } catch (DateTimeParseException ex) {
                            dateTime = LocalDateTime.parse(input + " 00:00:00", DATE_TIME_FORMATTER);
                        }
                        converted = convertDateTimeToMJD(dateTime).toString();
                    }
                    convertedDate.setText(converted);
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
