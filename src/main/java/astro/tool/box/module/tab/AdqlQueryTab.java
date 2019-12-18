package astro.tool.box.module.tab;

import static astro.tool.box.module.ServiceProviderUtils.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaDR2CatalogEntry;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.JobStatus;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.json.JSONArray;
import org.json.JSONObject;

public class AdqlQueryTab {

    public static final String TAB_NAME = "ADQL Query";
    private static final String IRSA_TABLES = "IRSA tables";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;

    private JPanel centerPanel;
    private JPanel catalogPanel;
    private JTextField statusField;
    private JTextField elapsedTime;
    private TableRowSorter<TableModel> catalogTableSorter;
    private TableRowSorter<TableModel> catalogColumnSorter;

    private Timer checkQueryStatus;
    private Instant startTime;

    private File file;
    private String jobId;
    private String jobStatus;
    private String queryResults;
    private String previousTableName;

    private boolean isSyntaxChecked;

    public AdqlQueryTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
    }

    public void init() {
        try {
            JPanel mainPanel = new JPanel(new BorderLayout());

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            JTextArea textEditor = new JTextArea();
            textEditor.setBorder(new EmptyBorder(5, 5, 5, 5));
            textEditor.setFont(MONO_FONT);
            textEditor.setEditable(true);
            addUndoManager(textEditor);

            ChangeListener changeListener = (ChangeEvent changeEvent) -> {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    String query = textEditor.getText();
                    if (query.isEmpty() || query.contains("Find all comovers")) {
                        CatalogEntry selectedEntry = catalogQueryTab.getSelectedEntry();
                        if (selectedEntry != null && selectedEntry instanceof GaiaDR2CatalogEntry) {
                            String comoverQuery = createComoverQuery();
                            comoverQuery = comoverQuery.replace("[RA]", roundTo7DecNZ(selectedEntry.getRa()));
                            comoverQuery = comoverQuery.replace("[DE]", roundTo7DecNZ(selectedEntry.getDec()));
                            comoverQuery = comoverQuery.replace("[PMRA]", roundTo3DecNZ(selectedEntry.getPmra()));
                            comoverQuery = comoverQuery.replace("[PMDE]", roundTo3DecNZ(selectedEntry.getPmdec()));
                            textEditor.setText(comoverQuery);
                        }
                    }
                }
            };
            tabbedPane.addChangeListener(changeListener);

            JScrollPane scrollEditor = new JScrollPane(textEditor);
            scrollEditor.setPreferredSize(new Dimension(scrollEditor.getWidth(), 250));
            scrollEditor.setBorder(createEtchedBorder("ADQL query"));
            centerPanel.add(scrollEditor);

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser fileChooser = new JFileChooser();

            JButton importButton = new JButton("Import query");
            topPanel.add(importButton);
            importButton.addActionListener((ActionEvent evt) -> {
                int returnVal = fileChooser.showOpenDialog(topPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    try {
                        List<String> lines = Files.readAllLines(file.toPath());
                        String content = String.join(LINE_SEP_TEXT_AREA, lines);
                        textEditor.setText(content);
                        scrollEditor.setBorder(createEtchedBorder("Current file: " + file.getName()));
                    } catch (IOException ex) {
                        showExceptionDialog(baseFrame, ex);
                    }
                }
            });

            String saveMessage = "File has been saved!";
            JLabel message = createLabel("", PLAIN_FONT, JColor.DARKER_GREEN.val);
            Timer timer = new Timer(3000, (ActionEvent e) -> {
                message.setText("");
            });

            JButton saveButton = new JButton("Save query");
            topPanel.add(saveButton);
            saveButton.addActionListener((ActionEvent evt) -> {
                if (file == null) {
                    int returnVal = fileChooser.showSaveDialog(topPanel);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file = fileChooser.getSelectedFile();
                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write(textEditor.getText());
                            scrollEditor.setBorder(createEtchedBorder("Current file: " + file.getName()));
                            message.setText(saveMessage);
                            timer.restart();
                        } catch (IOException ex) {
                            showExceptionDialog(baseFrame, ex);
                        }
                    }
                    return;
                }
                if (!showConfirmDialog(baseFrame, "Confirm save action for file " + file.getName())) {
                    return;
                }
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(textEditor.getText());
                    message.setText(saveMessage);
                    timer.restart();
                } catch (IOException ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            JButton saveAsButton = new JButton("Save As...");
            topPanel.add(saveAsButton);
            saveAsButton.addActionListener((ActionEvent evt) -> {
                int returnVal = fileChooser.showSaveDialog(topPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(textEditor.getText());
                        scrollEditor.setBorder(createEtchedBorder("Current file: " + file.getName()));
                        message.setText(saveMessage);
                        timer.restart();
                    } catch (IOException ex) {
                        showExceptionDialog(baseFrame, ex);
                    }
                }
            });

            JButton runButton = new JButton("Run query");
            topPanel.add(runButton);
            runButton.addActionListener((ActionEvent evt) -> {
                if (jobStatus != null && (jobStatus.equals(JobStatus.QUEUED.toString()) || jobStatus.equals(JobStatus.EXECUTING.toString()))) {
                    showErrorDialog(baseFrame, "Query is still running!");
                    return;
                }
                String query = textEditor.getText();
                if (query.isEmpty()) {
                    showErrorDialog(baseFrame, "No query to run!");
                    return;
                }
                runButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                removeResultPanel();
                jobStatus = JobStatus.QUEUED.toString();
                statusField.setText(jobStatus);
                statusField.setBackground(getStatusColor(jobStatus).val);
                queryResults = null;
                jobId = null;
                String encodedQuery = omitQueryComments(query).replaceAll(LINE_SEP_TEXT_AREA, " ")
                        .replaceAll(" +", "%20")
                        .replaceAll("\\+", "%2B")
                        .replaceAll(";", "");
                try {
                    String response;
                    // Validate query
                    try {
                        response = readResponse(establishHttpConnection(createValidatorUrl(encodedQuery)));
                        isSyntaxChecked = true;
                        JSONObject obj = new JSONObject(response);
                        String validation = obj.getString("validation");
                        if (!validation.equals("ok")) {
                            JSONArray arr = obj.getJSONArray("errors");
                            String errorMessage = arr.getJSONObject(0).getString("message");
                            showQueryErrorMessage(errorMessage);
                            initStatus();
                            return;
                        }
                    } catch (Exception ex) {
                    }

                    // Execute query
                    startClock();
                    response = readResponse(establishHttpConnection(createAsynchQueryUrl(encodedQuery)));
                    String[] parts = response.split("<uws:jobId>");
                    parts = parts[1].split("</uws:jobId>");
                    jobId = parts[0];
                } catch (IOException ex) {
                    stopClock();
                    initStatus();
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    runButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            topPanel.add(new JLabel("Status:"));

            statusField = new JTextField(10);
            topPanel.add(statusField);
            statusField.setEditable(false);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            checkQueryStatus = new Timer(10000, (ActionEvent e) -> {
                if (jobId == null) {
                    stopClock();
                    showErrorDialog(baseFrame, "No query submitted!");
                    return;
                }
                try {
                    jobStatus = readResponse(establishHttpConnection(createStatusUrl(jobId)));
                    statusField.setText(jobStatus);
                    statusField.setBackground(getStatusColor(jobStatus).val);
                    if (jobStatus.equals(JobStatus.ERROR.toString())) {
                        stopClock();
                        showQueryErrorMessage(getQueryErrorMessage());
                    }
                    if (jobStatus.equals(JobStatus.ABORT.toString()) || jobStatus.equals(JobStatus.COMPLETED.toString())) {
                        stopClock();
                    }
                } catch (IOException ex) {
                    stopClock();
                    initStatus();
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    Duration duration = Duration.between(startTime, Instant.now());
                    LocalTime time = LocalTime.ofSecondOfDay(duration.getSeconds());
                    elapsedTime.setText(time.format(timeFormatter));
                }
            });

            topPanel.add(new JLabel("Elapsed time:"));

            elapsedTime = new JTextField(6);
            elapsedTime.setEditable(false);
            topPanel.add(elapsedTime);

            JButton fetchButton = new JButton("Fetch results");
            topPanel.add(fetchButton);
            fetchButton.addActionListener((ActionEvent evt) -> {
                if (jobId == null) {
                    showErrorDialog(baseFrame, "No query submitted!");
                    return;
                }
                fetchButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                removeResultPanel();
                try {
                    jobStatus = readResponse(establishHttpConnection(createStatusUrl(jobId)));
                    statusField.setText(jobStatus);
                    statusField.setBackground(getStatusColor(jobStatus).val);

                    if (jobStatus.equals(JobStatus.QUEUED.toString())) {
                        showInfoDialog(baseFrame, "Query is still queued!");
                    } else if (jobStatus.equals(JobStatus.EXECUTING.toString())) {
                        showInfoDialog(baseFrame, "Query is still running!");
                    } else if (jobStatus.equals(JobStatus.COMPLETED.toString())) {
                        queryResults = readResponse(establishHttpConnection(createResultUrl(jobId)));
                        centerPanel.add(readQueryResult(new TableRowSorter<>(), queryResults, "Query results"));
                        baseFrame.setVisible(true);
                    } else if (jobStatus.equals(JobStatus.ERROR.toString())) {
                        showQueryErrorMessage(getQueryErrorMessage());
                        showErrorDialog(baseFrame, "Query error!");
                    } else if (jobStatus.equals(JobStatus.ABORT.toString())) {
                        showInfoDialog(baseFrame, "Query was aborted!");
                    }
                } catch (IOException ex) {
                    initStatus();
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    fetchButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            JButton exportButton = new JButton("Export results");
            topPanel.add(exportButton);
            exportButton.addActionListener((ActionEvent evt) -> {
                if (queryResults == null || queryResults.isEmpty()) {
                    showErrorDialog(baseFrame, "Nothing to export yet!");
                } else {
                    try {
                        File tmpFile = File.createTempFile("AstroToolBox_", ".csv");
                        try (FileWriter writer = new FileWriter(tmpFile)) {
                            writer.write(queryResults);
                        }
                        Desktop.getDesktop().open(tmpFile);
                    } catch (IOException ex) {
                        showExceptionDialog(baseFrame, ex);
                    }

                }
            });

            JButton browseButton = new JButton("Browse IRSA tables");
            topPanel.add(browseButton);
            browseButton.addActionListener((ActionEvent evt) -> {
                browseButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                removeResultPanel();
                String query = "SELECT * FROM TAP_SCHEMA.tables";
                String encodedQuery = query.replaceAll(" +", "%20");
                try {
                    String result = readResponse(establishHttpConnection(createSynchQueryUrl(encodedQuery)));
                    catalogPanel = new JPanel(new GridLayout(1, 2));
                    centerPanel.add(catalogPanel);

                    JPanel catalogTablePanel = new JPanel();
                    catalogPanel.add(catalogTablePanel);
                    catalogTablePanel.setLayout(new BoxLayout(catalogTablePanel, BoxLayout.Y_AXIS));

                    catalogTableSorter = new TableRowSorter<>();
                    catalogTablePanel.add(readQueryResult(catalogTableSorter, result, IRSA_TABLES));

                    JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    catalogTablePanel.add(filterPanel);
                    filterPanel.add(new JLabel("Table filter:"));
                    JTextField filterField = new JTextField(30);
                    filterPanel.add(filterField);
                    filterField.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void changedUpdate(DocumentEvent e) {
                        }

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            createCatalogTableFilter(filterField.getText());
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            createCatalogTableFilter(filterField.getText());
                        }
                    });

                    baseFrame.setVisible(true);
                } catch (IOException ex) {
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    browseButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            topPanel.add(message);

            tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void startClock() {
        checkQueryStatus.restart();
        startTime = Instant.now();
        elapsedTime.setText("");
    }

    private void stopClock() {
        checkQueryStatus.stop();
    }

    private JScrollPane readQueryResult(TableRowSorter<TableModel> sorter, String queryResult, String panelName) {
        try (Scanner scanner = new Scanner(queryResult)) {
            String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
            int numberOfColumns = columnNames.length;
            int rowNumber = 0;
            List<String[]> rows = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String[] columnValues = scanner.nextLine().split(SPLIT_CHAR, numberOfColumns);
                String[] values = concatArrays(new String[]{String.valueOf(++rowNumber)}, columnValues);
                for (int i = 0; i < values.length; i++) {
                    if (isDecimal(values[i])) {
                        values[i] = roundTo4Dec(toDouble(values[i]));
                    }
                }
                rows.add(values);
            }
            String[] names = concatArrays(new String[]{"row#"}, columnNames);
            return displayQueryResults(sorter, names, rows, panelName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private JScrollPane displayQueryResults(TableRowSorter<TableModel> sorter, String[] columnNames, List<String[]> rows, String panelName) {
        Object[][] data = new Object[][]{};
        DefaultTableModel defaultTableModel = new DefaultTableModel(rows.toArray(data), columnNames);
        JTable resultTable = new JTable(defaultTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        alignResultColumns(resultTable, rows);
        addComparatorsToTableSorter(sorter, defaultTableModel, rows);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setRowSorter(sorter);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (IRSA_TABLES.equals(panelName)) {
            resultTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = resultTable.getSelectedRow();
                    String tableName;
                    try {
                        tableName = (String) resultTable.getValueAt(selectedRow, 3);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        return;
                    }
                    if (tableName.equals(previousTableName)) {
                        return;
                    }
                    previousTableName = tableName;
                    resultTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    removeColumnPanel();
                    String query = "SELECT * FROM TAP_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "'";
                    String encodedQuery = query.replaceAll(" +", "%20");
                    try {
                        String result = readResponse(establishHttpConnection(createSynchQueryUrl(encodedQuery)));

                        JPanel catalogColumnPanel = new JPanel();
                        catalogPanel.add(catalogColumnPanel);
                        catalogColumnPanel.setLayout(new BoxLayout(catalogColumnPanel, BoxLayout.Y_AXIS));

                        catalogColumnSorter = new TableRowSorter<>();
                        catalogColumnPanel.add(readQueryResult(catalogColumnSorter, result, "Table columns"));

                        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        catalogColumnPanel.add(filterPanel);
                        filterPanel.add(new JLabel("Column filter:"));
                        JTextField filterField = new JTextField(30);
                        filterPanel.add(filterField);
                        filterField.getDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void changedUpdate(DocumentEvent e) {
                            }

                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                createCatalogColumnFilter(filterField.getText());
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                createCatalogColumnFilter(filterField.getText());
                            }
                        });

                        baseFrame.setVisible(true);
                    } catch (IOException ex) {
                        showExceptionDialog(baseFrame, ex);
                    } finally {
                        resultTable.setCursor(Cursor.getDefaultCursor());
                    }
                }
            });
        }
        resizeColumnWidth(resultTable);

        JScrollPane resultScrollPanel = new JScrollPane(rows.isEmpty() ? new JLabel("Query completed without result.") : resultTable);
        resultScrollPanel.setBorder(createEtchedBorder(panelName));
        return resultScrollPanel;
    }

    private String createComoverQuery() {
        StringBuilder query = new StringBuilder();
        addRow(query, "-- Find all comovers in Gaia DR2 within a radius of one degree, having proper motions within +/- 10% of the target's ones");
        addRow(query, "SELECT ra AS RA,");
        addRow(query, "       dec AS dec,");
        addRow(query, "       source_id AS source_id,");
        addRow(query, "       parallax AS plx,");
        addRow(query, "       pmra AS pmRA,");
        addRow(query, "       pmdec AS pmdec,");
        addRow(query, "       phot_g_mean_mag AS G,");
        addRow(query, "       phot_bp_mean_mag AS BP,");
        addRow(query, "       phot_rp_mean_mag AS RP,");
        addRow(query, "       phot_g_mean_mag - phot_rp_mean_mag AS \"G-RP\",");
        addRow(query, "       bp_rp AS \"BP-RP\",");
        addRow(query, "       radial_velocity AS RV,");
        addRow(query, "       teff_val AS Teff,");
        addRow(query, "       radius_val AS Radius,");
        addRow(query, "       lum_val AS Lum");
        addRow(query, "FROM   gaia_dr2_source");
        addRow(query, "WHERE  1=CONTAINS(POINT('ICRS', ra, dec), CIRCLE('ICRS', [RA], [DE], 1))");
        addRow(query, "AND   (pmra  BETWEEN [PMRA] - ABS([PMRA]) * 0.1 AND [PMRA] + ABS([PMRA]) * 0.1");
        addRow(query, "AND    pmdec BETWEEN [PMDE] - ABS([PMDE]) * 0.1 AND [PMDE] + ABS([PMDE]) * 0.1)");
        return query.toString();
    }

    private String omitQueryComments(String query) {
        String[] lines = query.split(LINE_SEP_TEXT_AREA);
        List<String> results = new ArrayList<>();
        for (String line : lines) {
            if (!line.startsWith("--")) {
                results.add(line);
            }
        }
        return String.join(LINE_SEP_TEXT_AREA, results);
    }

    private void addRow(StringBuilder query, String row) {
        query.append(row).append(LINE_SEP_TEXT_AREA);
    }

    private String createSynchQueryUrl(String query) {
        return IRSA_TAP_URL + "/sync?query=" + query + "&format=csv";
    }

    private String createAsynchQueryUrl(String query) {
        return IRSA_TAP_URL + "/async?query=" + query + "&format=csv&phase=RUN";
    }

    private String createStatusUrl(String jobId) {
        return IRSA_TAP_URL + "/async/" + jobId + "/phase";
    }

    private String createResultUrl(String jobId) {
        return IRSA_TAP_URL + "/async/" + jobId + "/results/result";
    }

    private String createValidatorUrl(String query) {
        return "https://cdsportal.u-strasbg.fr/adqltuto/adqlvalidate?query=" + query;
    }

    private void removeResultPanel() {
        int count = centerPanel.getComponentCount();
        if (count > 1) {
            centerPanel.remove(1);
        }
    }

    private void removeColumnPanel() {
        int count = catalogPanel.getComponentCount();
        if (count > 1) {
            catalogPanel.remove(1);
        }
    }

    private void initStatus() {
        jobStatus = null;
        statusField.setText("");
        statusField.setBackground(null);
        jobId = null;
    }

    private void createCatalogTableFilter(String filterText) {
        try {
            RowFilter filter = RowFilter.regexFilter(filterText);
            catalogTableSorter.setRowFilter(filter);
        } catch (PatternSyntaxException ex) {
        }
    }

    private void createCatalogColumnFilter(String filterText) {
        try {
            RowFilter filter = RowFilter.regexFilter(filterText);
            catalogColumnSorter.setRowFilter(filter);
        } catch (PatternSyntaxException ex) {
        }
    }

    private JColor getStatusColor(String jobStatus) {
        JColor color;
        if (jobStatus.equals(JobStatus.QUEUED.toString())) {
            color = JColor.LIGHT_YELLOW;
        } else if (jobStatus.equals(JobStatus.EXECUTING.toString())) {
            color = JColor.LIGHT_BLUE;
        } else if (jobStatus.equals(JobStatus.COMPLETED.toString())) {
            color = JColor.LIGHT_GREEN;
        } else if (jobStatus.equals(JobStatus.ERROR.toString())) {
            color = JColor.LIGHT_RED;
        } else if (jobStatus.equals(JobStatus.ABORT.toString())) {
            color = JColor.LIGHT_RED;
        } else {
            color = JColor.LIGHT_YELLOW;
        }
        return color;
    }

    private void addUndoManager(JTextArea textEditor) {
        final UndoManager manger = new UndoManager();
        Document document = textEditor.getDocument();

        // Listen for undo and redo events
        document.addUndoableEditListener((UndoableEditEvent evt) -> {
            manger.addEdit(evt.getEdit());
        });

        // Create an undo action and add it to the text component
        textEditor.getActionMap().put("Undo", new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (manger.canUndo()) {
                        manger.undo();
                    }
                } catch (CannotUndoException e) {
                }
            }
        });

        // Bind the undo action to ctl-Z
        textEditor.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

        // Create a redo action and add it to the text component
        textEditor.getActionMap().put("Redo", new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (manger.canRedo()) {
                        manger.redo();
                    }
                } catch (CannotRedoException e) {
                }
            }
        });

        // Bind the redo action to ctl-Y
        textEditor.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }

    private String getQueryErrorMessage() {
        StringBuilder message = new StringBuilder();
        if (isSyntaxChecked) {
            addRow(message, "There seems to be some kind of error not related to ADQL syntax.");
            addRow(message, "Check the table and column names, they might be misspelled.");
            addRow(message, "Use the 'Browse IRSA tables' button to do so.");
            addRow(message, "If the error persists, please send a bug report including your query to " + HELP_EMAIL);
        } else {
            addRow(message, "Syntax check service not available. Error unknown!");
            addRow(message, "Review the ADQL syntax and the table and column names.");
        }
        return message.toString();
    }

    private void showQueryErrorMessage(String message) {
        JTextArea errorMessage = new JTextArea(message);
        errorMessage.setBorder(new EmptyBorder(5, 5, 5, 5));
        errorMessage.setForeground(JColor.DARK_RED.val);
        errorMessage.setFont(MONO_FONT);
        errorMessage.setEditable(false);

        JScrollPane scrollPanel = new JScrollPane(errorMessage);
        scrollPanel.setBorder(createEtchedBorder("Error(s)"));
        centerPanel.add(scrollPanel);
        baseFrame.setVisible(true);
    }

}
