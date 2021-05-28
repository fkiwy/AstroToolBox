package astro.tool.box.module.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.Utils.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaCatalogEntry;
import astro.tool.box.container.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.JobStatus;
import astro.tool.box.enumeration.TapProvider;
import astro.tool.box.util.CSVParser;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AdqlQueryTab {

    public static final String TAB_NAME = "ADQL Query";
    public static final String QUERY_SERVICE = "TAP service";
    private static final String IRSA_TABLES = "Available tables";
    private static final String JOB_ID = "jobId";
    private static final String ASYNC_TAP_PROVIDER = "asyncTapProvider";
    private static final TapProvider DEFAULT_TAP_PROVIDER = TapProvider.VIZIER;
    private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;

    private JPanel centerPanel;
    private JPanel catalogPanel;
    private JTextField statusField;
    private JTextField elapsedTime;
    private JComboBox tapProvider;
    private TableRowSorter<TableModel> catalogTableSorter;
    private TableRowSorter<TableModel> catalogColumnSorter;

    private Timer checkQueryStatus;
    private Instant startTime;

    private File file;
    private String jobId;
    private String jobStatus;
    private String queryResults;
    private String previousTableName;

    private DocumentBuilder builder;

    public AdqlQueryTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
    }

    public void init() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();

            JPanel mainPanel = new JPanel(new BorderLayout());

            JPanel topPanel = new JPanel(new GridLayout(2, 1));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(firstRow);

            JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(secondRow);

            JTextArea textEditor = new JTextArea();
            textEditor.setBorder(new EmptyBorder(5, 5, 5, 5));
            textEditor.setFont(MONO_FONT);
            textEditor.setEditable(true);
            addUndoManager(textEditor);

            JScrollPane scrollEditor = new JScrollPane(textEditor);
            scrollEditor.setPreferredSize(new Dimension(scrollEditor.getWidth(), 200));
            scrollEditor.setBorder(createEtchedBorder("ADQL query"));

            centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollEditor, centerPanel);
            mainPanel.add(splitPane, BorderLayout.CENTER);

            JFileChooser fileChooser = new JFileChooser();

            JButton importButton = new JButton("Import query");
            firstRow.add(importButton);
            importButton.addActionListener((ActionEvent evt) -> {
                int returnVal = fileChooser.showOpenDialog(firstRow);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    try {
                        List<String> lines = Files.readAllLines(file.toPath());
                        String content = String.join(LINE_SEP_TEXT_AREA, lines);
                        textEditor.setText(content);
                        scrollEditor.setBorder(createEtchedBorder("Current file: " + file.getName()));
                    } catch (Exception ex) {
                        showExceptionDialog(baseFrame, ex);
                    }
                }
            });

            String saveMessage = "File saved!";
            JLabel message = createMessageLabel();
            Timer timer = new Timer(3000, (ActionEvent e) -> {
                message.setText("");
            });

            JButton saveButton = new JButton("Save query");
            firstRow.add(saveButton);
            saveButton.addActionListener((ActionEvent evt) -> {
                if (file == null) {
                    int returnVal = fileChooser.showSaveDialog(firstRow);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file = fileChooser.getSelectedFile();
                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write(textEditor.getText());
                            scrollEditor.setBorder(createEtchedBorder("Current file: " + file.getName()));
                            message.setText(saveMessage);
                            timer.restart();
                        } catch (Exception ex) {
                            showExceptionDialog(baseFrame, ex);
                        }
                    }
                    return;
                }
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(textEditor.getText());
                    message.setText(saveMessage);
                    timer.restart();
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            JButton saveAsButton = new JButton("Save As...");
            firstRow.add(saveAsButton);
            saveAsButton.addActionListener((ActionEvent evt) -> {
                int returnVal = fileChooser.showSaveDialog(firstRow);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(textEditor.getText());
                        scrollEditor.setBorder(createEtchedBorder("Current file: " + file.getName()));
                        message.setText(saveMessage);
                        timer.restart();
                    } catch (Exception ex) {
                        showExceptionDialog(baseFrame, ex);
                    }
                }
            });

            JButton runButton = new JButton("Run query");
            firstRow.add(runButton);
            runButton.addActionListener((ActionEvent evt) -> {
                if (jobStatus != null && (jobStatus.equals(JobStatus.PENDING.toString()) || jobStatus.equals(JobStatus.QUEUED.toString()) || jobStatus.equals(JobStatus.EXECUTING.toString()))) {
                    showErrorDialog(baseFrame, "Query is still running!");
                    return;
                }
                String query = textEditor.getText();
                if (query.isEmpty()) {
                    showErrorDialog(baseFrame, "No query to run!");
                    return;
                }
                removeResultPanel();
                jobStatus = JobStatus.PENDING.toString();
                statusField.setText(jobStatus);
                statusField.setBackground(getStatusColor(jobStatus).val);
                queryResults = null;
                jobId = null;
                String response;
                // Validate query
                if (!TapProvider.NOAO.equals(getTapProvider())) {
                    try {
                        response = readResponse(establishHttpConnection(createValidatorUrl(encodeQuery(query))), "Query validator");
                        if (!response.isEmpty()) {
                            JSONObject obj = new JSONObject(response);
                            String validation = obj.getString("validation");
                            if (!validation.equals("ok")) {
                                JSONArray arr = obj.getJSONArray("errors");
                                String errorMessage = arr.getJSONObject(0).getString("message");
                                showErrorDialog(baseFrame, errorMessage);
                                initStatus();
                                return;
                            }
                        }
                    } catch (Exception ex) {
                    }
                }
                // Execute query
                startClock();
                try {
                    runButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("request", "doQuery"));
                    params.add(new BasicNameValuePair("lang", "ADQL"));
                    params.add(new BasicNameValuePair("format", "csv"));
                    params.add(new BasicNameValuePair("query", omitQueryComments(query)));
                    response = doPost(createAsynchQueryUrl(), params);
                    if (!response.isEmpty()) {
                        try {
                            jobId = getJobIdentifier(response);
                            params = new ArrayList<>();
                            params.add(new BasicNameValuePair("PHASE", "RUN"));
                            response = doPost(createStatusUrl(jobId), params);
                            SettingsTab.setUserSetting(JOB_ID, jobId);
                            SettingsTab.setUserSetting(ASYNC_TAP_PROVIDER, getTapProvider().name());
                            SettingsTab.saveSettings();
                        } catch (Exception ex) {
                            stopClock();
                            initStatus();
                            showErrorDialog(baseFrame, ex.getMessage());
                        }
                        String errorMessage = getErrorMessage(response);
                        if (!errorMessage.isEmpty()) {
                            showScrollableErrorDialog(baseFrame, errorMessage);
                        }
                    }
                } catch (Exception ex) {
                    stopClock();
                    initStatus();
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    runButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            firstRow.add(new JLabel("Status:"));

            statusField = new JTextField(10);
            firstRow.add(statusField);
            statusField.setEditable(false);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            checkQueryStatus = new Timer(10000, (ActionEvent e) -> {
                if (jobId == null) {
                    stopClock();
                    showErrorDialog(baseFrame, "No query submitted!");
                    return;
                }
                try {
                    jobStatus = readResponse(establishHttpConnection(createStatusUrl(jobId)), QUERY_SERVICE);
                    statusField.setText(jobStatus);
                    statusField.setBackground(getStatusColor(jobStatus).val);
                    if (jobStatus.equals(JobStatus.ERROR.toString()) || jobStatus.equals(JobStatus.ABORTED.toString()) || jobStatus.equals(JobStatus.COMPLETED.toString())) {
                        stopClock();
                    }
                    Duration duration = Duration.between(startTime, Instant.now());
                    LocalTime time = LocalTime.ofSecondOfDay(duration.getSeconds());
                    elapsedTime.setText(time.format(timeFormatter));
                } catch (Exception ex) {
                    stopClock();
                    initStatus();
                }
            });

            firstRow.add(new JLabel("Elapsed time:"));

            elapsedTime = new JTextField(6);
            elapsedTime.setEditable(false);
            firstRow.add(elapsedTime);

            JButton fetchButton = new JButton("Fetch results");
            firstRow.add(fetchButton);
            fetchButton.addActionListener((ActionEvent evt) -> {
                if (jobId == null) {
                    showInfoDialog(baseFrame, "No query submitted!");
                    return;
                }
                fetchButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                removeResultPanel();
                try {
                    jobStatus = readResponse(establishHttpConnection(createStatusUrl(jobId)), QUERY_SERVICE);
                    statusField.setText(jobStatus);
                    statusField.setBackground(getStatusColor(jobStatus).val);
                    if (jobStatus.equals(JobStatus.PENDING.toString())) {
                        showInfoDialog(baseFrame, "Query is still pending!");
                    } else if (jobStatus.equals(JobStatus.QUEUED.toString())) {
                        showInfoDialog(baseFrame, "Query is still queued!");
                    } else if (jobStatus.equals(JobStatus.EXECUTING.toString())) {
                        showInfoDialog(baseFrame, "Query is still running!");
                    } else if (jobStatus.equals(JobStatus.COMPLETED.toString())) {
                        queryResults = readResponse(establishHttpConnection(createResultUrl(jobId)), QUERY_SERVICE);
                        centerPanel.add(readQueryResult(new TableRowSorter<>(), queryResults, "Query results"));
                        baseFrame.setVisible(true);
                    } else if (jobStatus.equals(JobStatus.ERROR.toString())) {
                        String response = readResponse(establishHttpConnection(createErrorUrl(jobId)), QUERY_SERVICE);
                        String errorMessage = getErrorMessage(response);
                        showScrollableErrorDialog(baseFrame, errorMessage.isEmpty() ? response : errorMessage);
                    } else if (jobStatus.equals(JobStatus.ABORTED.toString())) {
                        showInfoDialog(baseFrame, "Query was aborted!");
                    }
                } catch (Exception ex) {
                    initStatus();
                    showInfoDialog(baseFrame, "No results to fetch!");
                } finally {
                    fetchButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            JButton exportButton = new JButton("Export results");
            firstRow.add(exportButton);
            exportButton.addActionListener((ActionEvent evt) -> {
                if (queryResults == null || queryResults.isEmpty()) {
                    showInfoDialog(baseFrame, "No results to export!");
                } else {
                    try {
                        File tmpFile = File.createTempFile("AstroToolBox_", ".csv");
                        try (FileWriter writer = new FileWriter(tmpFile)) {
                            writer.write(queryResults);
                        }
                        Desktop.getDesktop().open(tmpFile);
                    } catch (Exception ex) {
                        showExceptionDialog(baseFrame, ex);
                    }

                }
            });

            JButton abortButton = new JButton("Abort query");
            firstRow.add(abortButton);
            abortButton.addActionListener((ActionEvent evt) -> {
                if (TapProvider.IRSA.equals(getTapProvider())) {
                    showInfoDialog(baseFrame, "IRSA does not allow to abort queries.");
                    return;
                } else {
                    if (!showConfirmDialog(baseFrame, "Do you really want to abort this query?")) {
                        return;
                    }
                }
                try {
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("PHASE", "ABORT"));
                    doPost(createStatusUrl(jobId), params);
                    SettingsTab.setUserSetting(JOB_ID, "");
                    SettingsTab.saveSettings();
                    showInfoDialog(baseFrame, "Query aborted!");
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            JButton deleteButton = new JButton("Delete query");
            firstRow.add(deleteButton);
            deleteButton.addActionListener((ActionEvent evt) -> {
                if (TapProvider.IRSA.equals(getTapProvider())) {
                    showInfoDialog(baseFrame, "IRSA does not allow to delete queries.");
                    return;
                } else {
                    if (!showConfirmDialog(baseFrame, "Do you really want to delete this query?")) {
                        return;
                    }
                }
                try {
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("ACTION", "DELETE"));
                    doPost(createDeleteUrl(jobId), params);
                    SettingsTab.setUserSetting(JOB_ID, "");
                    SettingsTab.saveSettings();
                    showInfoDialog(baseFrame, "Query deleted!");
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            firstRow.add(message);

            secondRow.add(new JLabel("TAP provider:"));

            tapProvider = new JComboBox(TapProvider.values());
            secondRow.add(tapProvider);
            tapProvider.setSelectedItem(DEFAULT_TAP_PROVIDER);

            JButton browseButton = new JButton("Browse tables");
            secondRow.add(browseButton);
            browseButton.addActionListener((ActionEvent evt) -> {
                browseButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                removeResultPanel();
                String query = "select schema_name, table_name, table_type, description from tap_schema.tables where schema_name != 'mydb' order by table_name";
                String encodedQuery = query.replaceAll(" +", "%20");
                try {
                    String result = readResponse(establishHttpConnection(createSynchQueryUrl(encodedQuery)), QUERY_SERVICE);
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
                            catalogTableSorter.setRowFilter(getCustomRowFilter(filterField.getText()));
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            catalogTableSorter.setRowFilter(getCustomRowFilter(filterField.getText()));
                        }
                    });

                    baseFrame.setVisible(true);
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    browseButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            tabbedPane.addChangeListener((ChangeEvent evt) -> {
                JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    String query = textEditor.getText();
                    if (query.isEmpty() || query.contains("Find all comovers")) {
                        CatalogEntry selectedEntry = catalogQueryTab.getSelectedEntry();
                        if (selectedEntry != null && (selectedEntry instanceof GaiaCatalogEntry || selectedEntry instanceof GaiaDR3CatalogEntry)) {
                            tapProvider.setSelectedItem(TapProvider.IRSA);
                            String comoverQuery = createComoverQuery();
                            comoverQuery = comoverQuery.replace("[RA]", roundTo7DecNZ(selectedEntry.getRa()));
                            comoverQuery = comoverQuery.replace("[DE]", roundTo7DecNZ(selectedEntry.getDec()));
                            comoverQuery = comoverQuery.replace("[PMRA]", roundTo3DecNZ(selectedEntry.getPmra()));
                            comoverQuery = comoverQuery.replace("[PMDE]", roundTo3DecNZ(selectedEntry.getPmdec()));
                            textEditor.setText(comoverQuery);
                        }
                    }
                }
            });

            jobId = SettingsTab.getUserSetting(JOB_ID, "");
            if (!jobId.isEmpty()) {
                String provider = SettingsTab.getUserSetting(ASYNC_TAP_PROVIDER, DEFAULT_TAP_PROVIDER.name());
                tapProvider.setSelectedItem(TapProvider.valueOf(provider));
                statusField.setText("Resuming ...");
                startClock();
            }

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
            String[] columnNames = CSVParser.parseLine(scanner.nextLine());
            int rowNumber = 0;
            List<String[]> rows = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String[] columnValues = CSVParser.parseLine(scanner.nextLine());
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
        JTable resultTable = new JTable(defaultTableModel);
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
                        tableName = (String) resultTable.getValueAt(selectedRow, 2);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        return;
                    }
                    if (tableName.equals(previousTableName)) {
                        return;
                    }
                    previousTableName = tableName;
                    resultTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    removeColumnPanel();
                    String query = "select * from tap_schema.columns where table_name = '" + tableName + "' order by column_name";
                    String encodedQuery = query.replaceAll(" +", "%20");
                    try {
                        String result = readResponse(establishHttpConnection(createSynchQueryUrl(encodedQuery)), QUERY_SERVICE);

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
                                catalogColumnSorter.setRowFilter(getCustomRowFilter(filterField.getText()));
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                catalogColumnSorter.setRowFilter(getCustomRowFilter(filterField.getText()));
                            }
                        });

                        baseFrame.setVisible(true);
                    } catch (Exception ex) {
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
        addRow(query, "-- Find all comovers in Gaia within a radius of one degree, having proper motions within +/- 10% of the target's ones");
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

    private String createSynchQueryUrl(String query) {
        return getTapProviderUrl() + "/sync?request=doQuery&lang=ADQL&format=csv&query=" + query;
    }

    private String createAsynchQueryUrl() {
        return getTapProviderUrl() + "/async";
    }

    private String createStatusUrl(String jobId) {
        return getTapProviderUrl() + "/async/" + jobId + "/phase";
    }

    private String createDeleteUrl(String jobId) {
        return getTapProviderUrl() + "/async/" + jobId;
    }

    private String createResultUrl(String jobId) {
        return getTapProviderUrl() + "/async/" + jobId + "/results/result";
    }

    private String createErrorUrl(String jobId) {
        return getTapProviderUrl() + "/async/" + jobId + "/error";
    }

    private String createValidatorUrl(String query) {
        return "https://cdsportal.u-strasbg.fr/adqltuto/adqlvalidate?query=" + query;
    }

    private String getTapProviderUrl() {
        switch (getTapProvider()) {
            case IRSA:
                return IRSA_TAP_URL;
            case VIZIER:
                return VIZIER_BASE_URL;
            case NOAO:
                return NOAO_BASE_URL;
            default:
                return null;
        }
    }

    private String getJobIdentifier(String response) throws Exception {
        switch (getTapProvider()) {
            case IRSA:
            case NOAO:
                return parseXml(response, "uws:jobId");
            case VIZIER:
                return parseXml(response, "jobId");
            default:
                return null;
        }
    }

    private String getErrorMessage(String response) throws Exception {
        return parseXml(response, "message");
    }

    private TapProvider getTapProvider() {
        return (TapProvider) tapProvider.getSelectedItem();
    }

    private void removeResultPanel() {
        centerPanel.removeAll();
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

    private JColor getStatusColor(String jobStatus) {
        JColor color;
        if (jobStatus.equals(JobStatus.PENDING.toString())) {
            color = JColor.LIGHT_YELLOW;
        } else if (jobStatus.equals(JobStatus.QUEUED.toString())) {
            color = JColor.LIGHT_YELLOW;
        } else if (jobStatus.equals(JobStatus.EXECUTING.toString())) {
            color = JColor.LIGHT_BLUE;
        } else if (jobStatus.equals(JobStatus.COMPLETED.toString())) {
            color = JColor.LIGHT_GREEN;
        } else if (jobStatus.equals(JobStatus.ERROR.toString())) {
            color = JColor.LIGHT_RED;
        } else if (jobStatus.equals(JobStatus.ABORTED.toString())) {
            color = JColor.LIGHT_ORANGE;
        } else {
            color = JColor.LIGHT_YELLOW;
        }
        return color;
    }

    private String parseXml(String xml, String tag) throws Exception {
        try {
            InputSource input = new InputSource(new StringReader(xml));
            org.w3c.dom.Document document = builder.parse(input);
            Node node = document.getElementsByTagName(tag).item(0);
            return node == null ? "" : node.getTextContent();
        } catch (IOException | DOMException | SAXException ex) {
            return "";
        }
    }

    private String doPost(String url, List<NameValuePair> params) throws UnsupportedEncodingException, IOException {
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(params));
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {
            writeMessageLog(post.getURI().toString());
            writeMessageLog(params.toString());
            writeMessageLog(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            return EntityUtils.toString(response.getEntity());
        }
    }

}
