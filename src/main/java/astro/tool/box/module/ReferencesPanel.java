package astro.tool.box.module;

import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.service.SimbadQueryService;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

public class ReferencesPanel extends JPanel {
    
    final private SimbadQueryService simbadQueryService = new SimbadQueryService();
    
    public ReferencesPanel(CatalogEntry catalogEntry, JFrame referencesFrame) {
        try {
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            add(container);
            
            JPanel referencesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            container.add(referencesPanel);
            
            JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            container.add(detailsPanel);
            
            JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            container.add(linksPanel);
            
            String mainIdentifier = catalogEntry.getSourceId();

            // Object identifiers
            List<String[]> identifiers = simbadQueryService.getObjectIdentifiers(mainIdentifier);
            
            String[] columns = new String[]{"Identifier"};
            Object[][] rows = new Object[][]{};
            
            JTable identifiersTable = new JTable(identifiers.toArray(rows), columns);
            alignResultColumns(identifiersTable, identifiers);
            resizeColumnWidth(identifiersTable, 1000);
            identifiersTable.setAutoCreateRowSorter(true);
            identifiersTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            identifiersTable.setTableHeader(null);
            
            JScrollPane resultPanel = new JScrollPane(identifiersTable);
            resultPanel.setPreferredSize(new Dimension(300, 300));
            resultPanel.setBorder(ModuleHelper.createEtchedBorder("Object identifiers"));
            
            referencesPanel.add(resultPanel);

            // Object references
            List<String[]> references = simbadQueryService.getObjectReferences(mainIdentifier);
            
            columns = new String[]{"Year", "Journal", "Volume", "Title", "Bibcode", "Ref"};
            rows = new Object[][]{};
            
            JTable referencesTable = new JTable(references.toArray(rows), columns);
            alignResultColumns(referencesTable, references);
            resizeColumnWidth(referencesTable, 1000);
            referencesTable.setAutoCreateRowSorter(true);
            referencesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            referencesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            referencesTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
                if (!e.getValueIsAdjusting()) {
                    String bibRef = (String) referencesTable.getValueAt(referencesTable.getSelectedRow(), 5);
                    populateDetailsPanel(Integer.valueOf(bibRef), detailsPanel);
                    
                    String bibCode = (String) referencesTable.getValueAt(referencesTable.getSelectedRow(), 4);
                    populateLinksPanel(bibCode, linksPanel);
                    
                    referencesFrame.setVisible(true);
                }
            });
            
            resultPanel = new JScrollPane(referencesTable);
            resultPanel.setPreferredSize(new Dimension(880, 300));
            resultPanel.setBorder(ModuleHelper.createEtchedBorder("Object references"));
            
            referencesPanel.add(resultPanel);
            
        } catch (IOException ex) {
            showExceptionDialog(null, ex);
        }
    }
    
    private void populateDetailsPanel(Integer bibRef, JPanel detailsPanel) {
        try {
            detailsPanel.removeAll();

            // Authors
            List<String[]> authors = simbadQueryService.getAuthors(bibRef);
            
            String[] columns = new String[]{"Author"};
            Object[][] rows = new Object[][]{};
            
            JTable authorsTable = new JTable(authors.toArray(rows), columns);
            alignResultColumns(authorsTable, authors);
            resizeColumnWidth(authorsTable, 1000);
            authorsTable.setAutoCreateRowSorter(true);
            authorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            authorsTable.setTableHeader(null);
            
            JScrollPane resultPanel = new JScrollPane(authorsTable);
            resultPanel.setPreferredSize(new Dimension(300, 300));
            resultPanel.setBorder(ModuleHelper.createEtchedBorder("Authors"));
            
            detailsPanel.add(resultPanel);

            // Abstract
            String result = simbadQueryService.getAbstract(bibRef);
            
            JTextArea textArea = new JTextArea(result);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            
            resultPanel = new JScrollPane(textArea);
            resultPanel.setPreferredSize(new Dimension(880, 300));
            resultPanel.setBorder(ModuleHelper.createEtchedBorder("Abstract"));
            
            detailsPanel.add(resultPanel);
        } catch (IOException ex) {
            showExceptionDialog(null, ex);
        }
    }
    
    private void populateLinksPanel(String bibCode, JPanel linksPanel) {
        linksPanel.removeAll();
        linksPanel.add(new JLabel("Download from"));
        linksPanel.add(createHyperlink("ArXiv", String.format("https://ui.adsabs.harvard.edu/link_gateway/%s/EPRINT_PDF", bibCode)));
        linksPanel.add(new JLabel("or"));
        linksPanel.add(createHyperlink("Publisher", String.format("https://ui.adsabs.harvard.edu/link_gateway/%s/PUB_PDF", bibCode)));
    }
    
}
