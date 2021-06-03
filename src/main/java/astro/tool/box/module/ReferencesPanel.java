package astro.tool.box.module;

import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.service.SimbadQueryService;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
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
        CompletableFuture.supplyAsync(() -> {
            try {
                referencesFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                add(container);

                JPanel measurementsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                container.add(measurementsPanel);

                JPanel measurementsPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                container.add(measurementsPanel2);

                JPanel referencesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                container.add(referencesPanel);

                JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                container.add(detailsPanel);

                JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                container.add(linksPanel);

                JPanel catalogsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                container.add(catalogsPanel);

                String mainIdentifier = catalogEntry.getSourceId();

                // Object types
                List<String[]> results = simbadQueryService.getObjectTypes(mainIdentifier);

                String[] columns = new String[]{"Object type", "Description"};
                JScrollPane resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Object types"));

                measurementsPanel.add(resultPanel);

                // Spectral types
                results = simbadQueryService.getObjectSpectralTypes(mainIdentifier);

                columns = new String[]{"Spectral type", "Bibcode"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Spectral types"));

                measurementsPanel.add(resultPanel);

                // Parallaxes
                results = simbadQueryService.getObjectParallaxes(mainIdentifier);

                columns = new String[]{"Parallax", "Error", "Bibcode"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Parallaxes"));

                measurementsPanel.add(resultPanel);

                // Distances
                results = simbadQueryService.getObjectDistances(mainIdentifier);

                columns = new String[]{"Distance", "Quality", "Unit", "Minus error", "Plus error", "Method", "Bibcode"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Distances"));

                measurementsPanel.add(resultPanel);

                // Velocities
                results = simbadQueryService.getObjectVelocities(mainIdentifier);

                columns = new String[]{"Type", "Velocity", "Error", "Quality", "Number of meas.", "Nature of meas.", "Quality", "Wavelength", "Resolution", "Obs. date", "Remarks", "Origin", "Bibcode"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Velocities"));

                measurementsPanel.add(resultPanel);

                // Proper motions
                results = simbadQueryService.getObjectProperMotions(mainIdentifier);

                columns = new String[]{"PM R.A.", "R.A. error", "PM DEC.", "DEC. error", "Coord. system", "Bibcode"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Proper motions"));

                measurementsPanel2.add(resultPanel);

                // Fluxes
                results = simbadQueryService.getObjectFluxes(mainIdentifier);

                columns = new String[]{"Filter", "Flux", "Error", "Quality", "Description", "Unit", "Bibcode"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Fluxes"));

                measurementsPanel2.add(resultPanel);

                // Variabilities
                results = simbadQueryService.getObjectVariabilities(mainIdentifier);

                columns = new String[]{"Type", "Upper limit flag", "Max. brightness", "Uncertainty flag", "Magnitude type", "Lower limit flag", "Min. brightness", "Uncertainty flag", "Lower limit flag period", "Period", "Uncertainty flag period", "Epoch", "Uncertainty epoch", "Raising time", "Uncertainty raising time", "Bibcode"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Variabilities"));

                measurementsPanel2.add(resultPanel);

                // Rotations
                results = simbadQueryService.getObjectRotations(mainIdentifier);

                columns = new String[]{"Upper value Vsini", "Vsini", "Error", "Number of meas.", "Quality", "Bibcode"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Rotations"));

                measurementsPanel2.add(resultPanel);

                // Metallicities
                results = simbadQueryService.getObjectMetallicities(mainIdentifier);

                columns = new String[]{"Teff", "Log g", "Fe H", "Fe H flag", "Comparison star", "Star in the Cayrel et al.", "Bibcode"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Metallicities"));

                measurementsPanel2.add(resultPanel);

                // Object identifiers
                results = simbadQueryService.getObjectIdentifiers(mainIdentifier);

                columns = new String[]{"Identifier"};
                resultPanel = new JScrollPane(createResultTable(results, columns));
                resultPanel.setPreferredSize(new Dimension(300, 200));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Object identifiers"));

                referencesPanel.add(resultPanel);

                // Object references
                results = simbadQueryService.getObjectReferences(mainIdentifier);

                columns = new String[]{"Year", "Journal", "Volume", "Title", "Bibcode", "Ref"};
                JComponent component = createResultTable(results, columns);
                if (component instanceof JTable) {
                    JTable resultTable = (JTable) component;
                    resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    resultTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
                        if (!e.getValueIsAdjusting()) {
                            String bibRef = (String) resultTable.getValueAt(resultTable.getSelectedRow(), 5);
                            populateDetailsPanel(Integer.valueOf(bibRef), detailsPanel);

                            String bibCode = (String) resultTable.getValueAt(resultTable.getSelectedRow(), 4);
                            populateLinksPanel(bibCode, linksPanel);

                            populateCatalogsPanel(bibCode, catalogsPanel);

                            referencesFrame.setVisible(true);
                        }
                    });
                    component = resultTable;
                }

                resultPanel = new JScrollPane(component);
                resultPanel.setPreferredSize(new Dimension(1500, 200));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Object references"));

                referencesPanel.add(resultPanel);

                referencesFrame.setVisible(true);
            } catch (IOException ex) {
                showExceptionDialog(null, ex);
            }
            referencesFrame.setCursor(Cursor.getDefaultCursor());
            return null;
        });
    }

    private void populateDetailsPanel(Integer bibRef, JPanel detailsPanel) {
        try {
            detailsPanel.removeAll();

            // Authors
            List<String[]> results = simbadQueryService.getAuthors(bibRef);

            String[] columns = new String[]{"Author"};
            JScrollPane resultPanel = new JScrollPane(createResultTable(results, columns));
            resultPanel.setPreferredSize(new Dimension(300, 200));
            resultPanel.setBorder(ModuleHelper.createEtchedBorder("Authors"));

            detailsPanel.add(resultPanel);

            // Abstract
            String result = simbadQueryService.getAbstract(bibRef);

            JTextArea textArea = new JTextArea(result);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            resultPanel = new JScrollPane(textArea);
            resultPanel.setPreferredSize(new Dimension(900, 200));
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

    private void populateCatalogsPanel(String bibCode, JPanel catalogsPanel) {
        try {
            catalogsPanel.removeAll();

            // VizieR catalogs
            List<String> catalogs = simbadQueryService.getVizierCatalogs(bibCode);
            if (catalogs.isEmpty()) {
                return;
            }

            catalogsPanel.add(new JLabel("VizieR catalogs:"));
            catalogs.forEach(catalog -> {
                catalogsPanel.add(createHyperlink(catalog, String.format("http://vizier.u-strasbg.fr/viz-bin/VizieR?-source=%s", catalog)));
            });
        } catch (IOException ex) {
            showExceptionDialog(null, ex);
        }
    }

    private JComponent createResultTable(List<String[]> results, String[] columns) {
        if (results.isEmpty()) {
            return new JLabel("N/A");
        }
        Object[][] rows = new Object[][]{};
        JTable resultTable = new JTable(results.toArray(rows), columns);
        alignResultColumns(resultTable, results);
        resizeColumnWidth(resultTable, 1000);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return resultTable;
    }

}
