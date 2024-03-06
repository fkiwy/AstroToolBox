package astro.tool.box.tab;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceHelper.*;
import static astro.tool.box.util.MiscUtils.*;
import static astro.tool.box.tab.SettingsTab.*;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.JobStatus;
import astro.tool.box.enumeration.TapProvider;
import astro.tool.box.util.CSVParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
import java.util.Arrays;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AdqlQueryTab implements Tab {

    public static final String TAB_NAME = "ADQL Query";
    public static final String QUERY_SERVICE = "TAP service";
    private static final String AVAILABLE_TABLES = "Available tables";
    private static final String ADQL_TAP_PROVIDER = "adqlTapProvider";
    private static final String DEFAULT_TAP_PROVIDER = TapProvider.VIZIER.name();
    private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    private JPanel centerPanel;
    private JPanel catalogPanel;
    private JTextField statusField;
    private JTextField elapsedTime;
    private JComboBox tapProvider;
    private JComboBox jobIds;
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

    public AdqlQueryTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void init(boolean visible) {
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
                    } catch (IOException ex) {
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
                //if (jobStatus != null && (jobStatus.equals(JobStatus.PENDING.toString()) || jobStatus.equals(JobStatus.QUEUED.toString()) || jobStatus.equals(JobStatus.EXECUTING.toString()))) {
                //    showErrorDialog(baseFrame, "Query is still running!");
                //    return;
                //}
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
                if (!TapProvider.NOIRLAB.equals(getTapProvider())) {
                    try {
                        response = readResponse(establishHttpConnection(createValidatorUrl(encodeQuery(query))), "Query validator");
                        if (!response.isEmpty()) {
                            JsonElement jelement = JsonParser.parseString​(response).getAsJsonObject();
                            JsonObject jobject = jelement.getAsJsonObject();
                            String validation = jobject.get("validation").getAsString();
                            if (!validation.equals("ok")) {
                                JsonArray jarray = jobject.getAsJsonArray("errors");
                                jobject = jarray.get(0).getAsJsonObject();
                                String errorMessage = jobject.get("message").getAsString();
                                showErrorDialog(baseFrame, errorMessage);
                                initStatus();
                                return;
                            }
                        }
                    } catch (JsonSyntaxException | IOException ex) {
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
                            addJobId(jobId, getTapProvider());
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
                    jobStatus = doGet(createStatusUrl(jobId));
                    statusField.setText(jobStatus);
                    statusField.setBackground(getStatusColor(jobStatus).val);
                    if (jobStatus.equals(JobStatus.ERROR.toString()) || jobStatus.equals(JobStatus.ABORTED.toString()) || jobStatus.equals(JobStatus.COMPLETED.toString())) {
                        stopClock();
                    }
                    Duration duration = Duration.between(startTime, Instant.now());
                    LocalTime time = LocalTime.ofSecondOfDay(duration.getSeconds());
                    elapsedTime.setText(time.format(timeFormatter));
                } catch (IOException ex) {
                    stopClock();
                    initStatus();
                }
            });

            firstRow.add(new JLabel("Elapsed time:"));

            elapsedTime = new JTextField(6);
            elapsedTime.setEditable(false);
            firstRow.add(elapsedTime);

            JButton displayButton = new JButton("Display result");
            firstRow.add(displayButton);
            displayButton.addActionListener((ActionEvent evt) -> {
                if (jobId == null) {
                    showInfoDialog(baseFrame, "No query submitted!");
                    return;
                }
                displayButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                removeResultPanel();
                try {
                    jobStatus = doGet(createStatusUrl(jobId));
                    statusField.setText(jobStatus);
                    statusField.setBackground(getStatusColor(jobStatus).val);
                    if (jobStatus.equals(JobStatus.PENDING.toString())) {
                        showInfoDialog(baseFrame, "Query is still pending!");
                    } else if (jobStatus.equals(JobStatus.QUEUED.toString())) {
                        showInfoDialog(baseFrame, "Query is still queued!");
                    } else if (jobStatus.equals(JobStatus.EXECUTING.toString())) {
                        showInfoDialog(baseFrame, "Query is still running!");
                    } else if (jobStatus.equals(JobStatus.COMPLETED.toString())) {
                        queryResults = doGet(createResultUrl(jobId));
                        centerPanel.add(readQueryResult(new TableRowSorter<>(), queryResults, "Query results"));
                        baseFrame.setVisible(true);
                    } else if (jobStatus.equals(JobStatus.ERROR.toString())) {
                        String response = doGet(createErrorUrl(jobId));
                        String errorMessage = getErrorMessage(response);
                        showScrollableErrorDialog(baseFrame, errorMessage.isEmpty() ? response : errorMessage);
                    } else if (jobStatus.equals(JobStatus.ABORTED.toString())) {
                        showInfoDialog(baseFrame, "Query was aborted!");
                    } else {
                        displayNoResultAvailable();
                    }
                } catch (Exception ex) {
                    initStatus();
                    showInfoDialog(baseFrame, "No result to display!");
                } finally {
                    displayButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            JButton downloadButton = new JButton("Download result");
            firstRow.add(downloadButton);
            downloadButton.addActionListener((ActionEvent evt) -> {
                if (jobId == null) {
                    showInfoDialog(baseFrame, "No query submitted!");
                    return;
                }
                downloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                removeResultPanel();
                try {
                    jobStatus = doGet(createStatusUrl(jobId));
                    statusField.setText(jobStatus);
                    statusField.setBackground(getStatusColor(jobStatus).val);
                    if (jobStatus.equals(JobStatus.PENDING.toString())) {
                        showInfoDialog(baseFrame, "Query is still pending!");
                    } else if (jobStatus.equals(JobStatus.QUEUED.toString())) {
                        showInfoDialog(baseFrame, "Query is still queued!");
                    } else if (jobStatus.equals(JobStatus.EXECUTING.toString())) {
                        showInfoDialog(baseFrame, "Query is still running!");
                    } else if (jobStatus.equals(JobStatus.COMPLETED.toString())) {
                        queryResults = doGet(createResultUrl(jobId));
                        File tmpFile = File.createTempFile("AstroToolBox_", ".txt");
                        try (FileWriter writer = new FileWriter(tmpFile)) {
                            writer.write(queryResults);
                        }
                        Desktop.getDesktop().open(tmpFile);
                    } else if (jobStatus.equals(JobStatus.ERROR.toString())) {
                        String response = doGet(createErrorUrl(jobId));
                        String errorMessage = getErrorMessage(response);
                        showScrollableErrorDialog(baseFrame, errorMessage.isEmpty() ? response : errorMessage);
                    } else if (jobStatus.equals(JobStatus.ABORTED.toString())) {
                        showInfoDialog(baseFrame, "Query was aborted!");
                    } else {
                        displayNoResultAvailable();
                    }
                } catch (Exception ex) {
                    initStatus();
                    showInfoDialog(baseFrame, "No result to download!");
                } finally {
                    downloadButton.setCursor(Cursor.getDefaultCursor());
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
                    removeJobId(jobId, getTapProvider());
                    showInfoDialog(baseFrame, "Query aborted!");
                } catch (IOException ex) {
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
                    removeJobId(jobId, getTapProvider());
                    showInfoDialog(baseFrame, "Query deleted!");
                } catch (IOException ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            firstRow.add(message);

            secondRow.add(new JLabel("TAP provider:"));

            tapProvider = new JComboBox(TapProvider.values());
            secondRow.add(tapProvider);
            tapProvider.setSelectedItem(TapProvider.valueOf(getUserSetting(ADQL_TAP_PROVIDER, DEFAULT_TAP_PROVIDER)));
            tapProvider.addActionListener((ActionEvent evt) -> {
                refreshJobIdList();
                setUserSetting(ADQL_TAP_PROVIDER, getTapProvider().name());
                saveSettings();
            });

            secondRow.add(new JLabel("Job ids for selected TAP provider:"));

            String[] ids = retrieveJobIds(getTapProvider());
            jobIds = new JComboBox(ids);
            secondRow.add(jobIds);
            jobIds.addActionListener((ActionEvent evt) -> {
                jobId = (String) jobIds.getSelectedItem();
            });

            // Must also be initialized when the GUI is built
            jobId = (String) jobIds.getSelectedItem();

            JButton removeButton = new JButton("Remove job ids");
            secondRow.add(removeButton);
            removeButton.addActionListener((ActionEvent evt) -> {
                if (showConfirmDialog(baseFrame, "Do you really want to remove all job ids?")) {
                    removeAllJobIds(getTapProvider());
                }
            });

            JButton resumeButton = new JButton("Resume query");
            secondRow.add(resumeButton);
            resumeButton.addActionListener((ActionEvent evt) -> {
                jobId = (String) jobIds.getSelectedItem();
                if (jobId != null && !jobId.isEmpty()) {
                    statusField.setText("Resuming ...");
                    statusField.setBackground(JColor.LIGHT_NAVY.val);
                    startClock();
                }
            });

            JButton browseButton = new JButton("Browse tables");
            secondRow.add(browseButton);
            browseButton.addActionListener((ActionEvent evt) -> {
                browseButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                removeResultPanel();
                String query = "select schema_name, table_name, table_type, description from tap_schema.tables where schema_name != 'mydb' order by table_name";
                String encodedQuery = query.replaceAll(" +", "%20");
                try {
                    String result = readResponse(establishHttpConnection(createSynchQueryUrl(encodedQuery)), QUERY_SERVICE);
                    if (TapProvider.ESAC.equals(getTapProvider())) {
                        // Transform mutliple lines to single lines
                        result = result.replace("\n\"\r\n", "").replace("\n\"", "[br]").replace("\r\n", " ").replace("[br]", "\n\"");
                    }

                    catalogPanel = new JPanel(new GridLayout(1, 2));
                    centerPanel.add(catalogPanel);

                    JPanel catalogTablePanel = new JPanel();
                    catalogPanel.add(catalogTablePanel);
                    catalogTablePanel.setLayout(new BoxLayout(catalogTablePanel, BoxLayout.Y_AXIS));

                    catalogTableSorter = new TableRowSorter<>();
                    catalogTablePanel.add(readQueryResult(catalogTableSorter, result, AVAILABLE_TABLES));

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
                } catch (IOException ex) {
                    showExceptionDialog(baseFrame, ex);
                } finally {
                    browseButton.setCursor(Cursor.getDefaultCursor());
                }
            });

            if (visible) {
                tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));
            }
        } catch (ParserConfigurationException ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void displayNoResultAvailable() {
        statusField.setText(null);
        statusField.setBackground(elapsedTime.getBackground());
        showInfoDialog(baseFrame, "No result available!");
        writeMessageLog("No ADQL result available. Reason: " + jobStatus);
    }

    private void addJobId(String id, TapProvider provider) {
        List<String> ids = retrieveJobIdsAsList(provider);
        if (!ids.contains(id)) {
            ids.add(0, id);
            saveJobIds(ids, provider);
        }
    }

    private void removeJobId(String id, TapProvider provider) {
        List<String> ids = retrieveJobIdsAsList(provider);
        if (ids.contains(id)) {
            ids.remove(id);
            saveJobIds(ids, provider);
        }
    }

    private void removeAllJobIds(TapProvider provider) {
        saveJobIds(null, provider);
    }

    private void saveJobIds(List<String> ids, TapProvider provider) {
        String idList;
        if (ids == null || ids.isEmpty()) {
            idList = "";
        } else {
            idList = String.join(",", ids);
        }
        setUserSetting(provider.name(), idList);
        saveSettings();
        refreshJobIdList();
    }

    private String[] retrieveJobIds(TapProvider provider) {
        String ids = getUserSetting(provider.name(), "");
        if (ids.isEmpty()) {
            return new String[0];
        } else {
            return ids.split(",", -1);
        }
    }

    private List<String> retrieveJobIdsAsList(TapProvider provider) {
        List<String> ids = new ArrayList();
        ids.addAll(Arrays.asList(retrieveJobIds(provider)));
        return ids;
    }

    private void refreshJobIdList() {
        jobIds.removeAllItems();
        String[] ids = retrieveJobIds(getTapProvider());
        if (ids.length > 0) {
            for (String id : ids) {
                jobIds.addItem(id);
            }
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
        if (AVAILABLE_TABLES.equals(panelName)) {
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
        return "http://cdsportal.u-strasbg.fr/adqltuto/adqlvalidate?query=" + query;
    }

    private String getTapProviderUrl() {
        return getTapProvider().val;
    }

    private String getJobIdentifier(String response) throws Exception {
        String id = parseXml(response, "jobId");
        if (id.isEmpty()) {
            id = parseXml(response, "uws:jobId");
        }
        return id;
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
        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post)) {
            writeMessageLog(post.getURI().toString());
            writeMessageLog(params.toString());
            writeMessageLog(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            return EntityUtils.toString(response.getEntity());
        }
    }

    private String doGet(String url) throws UnsupportedEncodingException, IOException {
        HttpGet get = new HttpGet(url);
        try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(get)) {
            //writeMessageLog(get.getURI().toString());
            //writeMessageLog(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            return EntityUtils.toString(response.getEntity());
        }
    }

}
