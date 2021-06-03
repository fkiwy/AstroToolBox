package astro.tool.box.module;

import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.service.SimbadQueryService;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
                List<String[]> objectTypes = Collections.EMPTY_LIST; //simbadQueryService.getObjectTypes(mainIdentifier);

                String[] columns = new String[]{"Object type", "Description"};
                Object[][] rows = new Object[][]{};

                JTable objectTypesTable = new JTable(objectTypes.toArray(rows), columns);
                alignResultColumns(objectTypesTable, objectTypes);
                resizeColumnWidth(objectTypesTable, 1000);
                objectTypesTable.setAutoCreateRowSorter(true);
                objectTypesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                JScrollPane resultPanel = new JScrollPane(objectTypesTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Object types"));

                measurementsPanel.add(resultPanel);

                // Spectral types
                List<String[]> spectralTypes = simbadQueryService.getObjectSpectralTypes(mainIdentifier);

                columns = new String[]{"Spectral type", "Bibcode"};
                rows = new Object[][]{};

                JTable spectalTypesTable = new JTable(spectralTypes.toArray(rows), columns);
                alignResultColumns(spectalTypesTable, spectralTypes);
                resizeColumnWidth(spectalTypesTable, 1000);
                spectalTypesTable.setAutoCreateRowSorter(true);
                spectalTypesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                resultPanel = new JScrollPane(spectalTypesTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Spectral types"));

                measurementsPanel.add(resultPanel);

                // Parallaxes
                List<String[]> parallaxes = simbadQueryService.getObjectParallaxes(mainIdentifier);

                columns = new String[]{"Parallax", "Error", "Bibcode"};
                rows = new Object[][]{};

                JTable parallaxesTable = new JTable(parallaxes.toArray(rows), columns);
                alignResultColumns(parallaxesTable, parallaxes);
                resizeColumnWidth(parallaxesTable, 1000);
                parallaxesTable.setAutoCreateRowSorter(true);
                parallaxesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                resultPanel = new JScrollPane(parallaxesTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Parallaxes"));

                measurementsPanel.add(resultPanel);

                // Distances
                List<String[]> distances = simbadQueryService.getObjectDistances(mainIdentifier);

                columns = new String[]{"Distance", "Quality", "Unit", "Minus error", "Plus error", "Method", "Bibcode"};
                rows = new Object[][]{};

                JTable distancesTable = new JTable(distances.toArray(rows), columns);
                alignResultColumns(distancesTable, distances);
                resizeColumnWidth(distancesTable, 1000);
                distancesTable.setAutoCreateRowSorter(true);
                distancesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                resultPanel = new JScrollPane(distancesTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Distances"));

                measurementsPanel.add(resultPanel);

                // Velocities
                List<String[]> velocities = simbadQueryService.getObjectVelocities(mainIdentifier);

                columns = new String[]{"Type", "Velocity", "Error", "Quality", "Number of meas.", "Nature of meas.", "Quality", "Wavelength", "Resolution", "Obs. date", "Remarks", "Origin", "Bibcode"};
                rows = new Object[][]{};

                JTable velocitiesTable = new JTable(velocities.toArray(rows), columns);
                alignResultColumns(velocitiesTable, velocities);
                resizeColumnWidth(velocitiesTable, 1000);
                velocitiesTable.setAutoCreateRowSorter(true);
                velocitiesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                resultPanel = new JScrollPane(velocitiesTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Velocities"));

                measurementsPanel.add(resultPanel);

                // Proper motions
                List<String[]> properMotions = simbadQueryService.getObjectProperMotions(mainIdentifier);

                columns = new String[]{"PM RA", "PM DE", "RA error", "DE error", "Coord. system", "Bibcode"};
                rows = new Object[][]{};

                JTable properMotionsTable = new JTable(properMotions.toArray(rows), columns);
                alignResultColumns(properMotionsTable, properMotions);
                resizeColumnWidth(properMotionsTable, 1000);
                properMotionsTable.setAutoCreateRowSorter(true);
                properMotionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                resultPanel = new JScrollPane(properMotionsTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Proper motions"));

                measurementsPanel2.add(resultPanel);

                // Fluxes
                List<String[]> fluxes = simbadQueryService.getObjectFluxes(mainIdentifier);

                columns = new String[]{"Filter", "Flux", "Error", "Quality", "Description", "Unit", "Bibcode"};
                rows = new Object[][]{};

                JTable fluxesTable = new JTable(fluxes.toArray(rows), columns);
                alignResultColumns(fluxesTable, fluxes);
                resizeColumnWidth(fluxesTable, 1000);
                fluxesTable.setAutoCreateRowSorter(true);
                fluxesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                resultPanel = new JScrollPane(fluxesTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Fluxes"));

                measurementsPanel2.add(resultPanel);

                // Variabilities
                List<String[]> variabilities = simbadQueryService.getObjectVariabilities(mainIdentifier);

                columns = new String[]{"Type", "Upper limit flag", "Max. brightness", "Uncertainty flag", "Magnitude type", "Lower limit flag", "Min. brightness", "Uncertainty flag", "Lower limit flag period", "Period", "Uncertainty flag period", "Epoch", "Uncertainty epoch", "Raising time", "Uncertainty raising time", "Bibcode"};
                rows = new Object[][]{};

                JTable variabilitiesTable = new JTable(variabilities.toArray(rows), columns);
                alignResultColumns(variabilitiesTable, variabilities);
                resizeColumnWidth(variabilitiesTable, 1000);
                variabilitiesTable.setAutoCreateRowSorter(true);
                variabilitiesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                resultPanel = new JScrollPane(variabilitiesTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Variabilities"));

                measurementsPanel2.add(resultPanel);

                // Rotations
                List<String[]> rotations = simbadQueryService.getObjectRotations(mainIdentifier);

                columns = new String[]{"Upper value Vsini", "Vsini", "Error", "Number of meas.", "Quality", "Bibcode"};
                rows = new Object[][]{};

                JTable rotationsTable = new JTable(rotations.toArray(rows), columns);
                alignResultColumns(rotationsTable, rotations);
                resizeColumnWidth(rotationsTable, 1000);
                rotationsTable.setAutoCreateRowSorter(true);
                rotationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                resultPanel = new JScrollPane(rotationsTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Rotations"));

                measurementsPanel2.add(resultPanel);

                // Metallicities
                List<String[]> metallicities = simbadQueryService.getObjectMetallicities(mainIdentifier);

                columns = new String[]{"Teff", "Log g", "Fe H", "Fe H flag", "Comparison star", "Star in the Cayrel et al.", "Bibcode"};
                rows = new Object[][]{};

                JTable metallicitiesTable = new JTable(metallicities.toArray(rows), columns);
                alignResultColumns(metallicitiesTable, metallicities);
                resizeColumnWidth(metallicitiesTable, 1000);
                metallicitiesTable.setAutoCreateRowSorter(true);
                metallicitiesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                resultPanel = new JScrollPane(metallicitiesTable);
                resultPanel.setPreferredSize(new Dimension(300, 150));
                resultPanel.setBorder(ModuleHelper.createEtchedBorder("Metallicities"));

                measurementsPanel2.add(resultPanel);

                // Object identifiers
                List<String[]> identifiers = simbadQueryService.getObjectIdentifiers(mainIdentifier);

                columns = new String[]{"Identifier"};
                rows = new Object[][]{};

                JTable identifiersTable = new JTable(identifiers.toArray(rows), columns);
                alignResultColumns(identifiersTable, identifiers);
                resizeColumnWidth(identifiersTable, 1000);
                identifiersTable.setAutoCreateRowSorter(true);
                identifiersTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                identifiersTable.setTableHeader(null);

                resultPanel = new JScrollPane(identifiersTable);
                resultPanel.setPreferredSize(new Dimension(300, 200));
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

                        populateCatalogsPanel(bibCode, catalogsPanel);

                        referencesFrame.setVisible(true);
                    }
                });

                resultPanel = new JScrollPane(referencesTable);
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
            resultPanel.setPreferredSize(new Dimension(300, 200));
            resultPanel.setBorder(ModuleHelper.createEtchedBorder("Authors"));

            detailsPanel.add(resultPanel);

            // Abstract
            String result = simbadQueryService.getAbstract(bibRef);

            JTextArea textArea = new JTextArea(result);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            resultPanel = new JScrollPane(textArea);
            resultPanel.setPreferredSize(new Dimension(850, 200));
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

}
