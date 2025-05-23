package astro.tool.box.panel;

import static astro.tool.box.main.ToolboxHelper.alignResultColumns;
import static astro.tool.box.main.ToolboxHelper.bold;
import static astro.tool.box.main.ToolboxHelper.createHyperlink;
import static astro.tool.box.main.ToolboxHelper.resizeColumnWidth;
import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;
import static astro.tool.box.util.Constants.ENCODING;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
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
import javax.swing.table.TableColumn;

import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.main.ToolboxHelper;
import astro.tool.box.service.SimbadQueryService;

public class ReferencesPanel extends JPanel {

	private final SimbadQueryService simbadQueryService = new SimbadQueryService();

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

				JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				container.add(searchPanel);

				JPanel catalogsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				container.add(catalogsPanel);

				String mainIdentifier = catalogEntry.getSourceId();

				// Object types
				List<String[]> results = simbadQueryService.getObjectTypes(mainIdentifier);

				String[] columns = new String[] { "Object type", "Description" };
				JScrollPane resultPanel = new JScrollPane(createResultTable(results, columns, 0));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Object types")));

				measurementsPanel.add(resultPanel);

				// Spectral types
				results = simbadQueryService.getObjectSpectralTypes(mainIdentifier);

				columns = new String[] { "Spectral type", "Bibcode" };
				resultPanel = new JScrollPane(createResultTable(results, columns, columns.length));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Spectral types")));

				measurementsPanel.add(resultPanel);

				// Parallaxes
				results = simbadQueryService.getObjectParallaxes(mainIdentifier);

				columns = new String[] { "Parallax", "Error", "Bibcode" };
				resultPanel = new JScrollPane(createResultTable(results, columns, columns.length));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Parallaxes")));

				measurementsPanel.add(resultPanel);

				// Distances
				results = simbadQueryService.getObjectDistances(mainIdentifier);

				columns = new String[] { "Distance", "Quality", "Unit", "Minus error", "Plus error", "Method",
						"Bibcode" };
				resultPanel = new JScrollPane(createResultTable(results, columns, columns.length));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Distances")));

				measurementsPanel.add(resultPanel);

				// Velocities
				results = simbadQueryService.getObjectVelocities(mainIdentifier);

				columns = new String[] { "Type", "Velocity", "Error", "Quality", "Number of meas.", "Nature of meas.",
						"Quality", "Wavelength", "Resolution", "Obs. date", "Remarks", "Origin", "Bibcode" };
				resultPanel = new JScrollPane(createResultTable(results, columns, columns.length));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Velocities")));

				measurementsPanel.add(resultPanel);

				// Proper motions
				results = simbadQueryService.getObjectProperMotions(mainIdentifier);

				columns = new String[] { "PM R.A.", "R.A. error", "PM DEC.", "DEC. error", "Coord. system", "Bibcode" };
				resultPanel = new JScrollPane(createResultTable(results, columns, columns.length));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Proper motions")));

				measurementsPanel2.add(resultPanel);

				// Fluxes
				results = simbadQueryService.getObjectFluxes(mainIdentifier);

				columns = new String[] { "Filter", "Flux", "Error", "Quality", "Description", "Unit", "Bibcode" };
				resultPanel = new JScrollPane(createResultTable(results, columns, columns.length));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Fluxes")));

				measurementsPanel2.add(resultPanel);

				// Variabilities
				results = simbadQueryService.getObjectVariabilities(mainIdentifier);

				columns = new String[] { "Type", "Upper limit flag", "Max. brightness", "Uncertainty flag",
						"Magnitude type", "Lower limit flag", "Min. brightness", "Uncertainty flag",
						"Lower limit flag period", "Period", "Uncertainty flag period", "Epoch", "Uncertainty epoch",
						"Raising time", "Uncertainty raising time", "Bibcode" };
				resultPanel = new JScrollPane(createResultTable(results, columns, columns.length));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Variabilities")));

				measurementsPanel2.add(resultPanel);

				// Rotations
				results = simbadQueryService.getObjectRotations(mainIdentifier);

				columns = new String[] { "Upper value Vsini", "Vsini", "Error", "Number of meas.", "Quality",
						"Bibcode" };
				resultPanel = new JScrollPane(createResultTable(results, columns, columns.length));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Rotations")));

				measurementsPanel2.add(resultPanel);

				// Metallicities
				results = simbadQueryService.getObjectMetallicities(mainIdentifier);

				columns = new String[] { "Teff", "Log g", "Fe H", "Fe H flag", "Comparison star",
						"Star in the Cayrel et al.", "Bibcode" };
				resultPanel = new JScrollPane(createResultTable(results, columns, columns.length));
				resultPanel.setPreferredSize(new Dimension(300, 150));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Metallicities")));

				measurementsPanel2.add(resultPanel);

				// Object identifiers
				List<String[]> identifiers = simbadQueryService.getObjectIdentifiers(mainIdentifier);

				columns = new String[] { "Identifier" };
				resultPanel = new JScrollPane(createResultTable(identifiers, columns, 0));
				resultPanel.setPreferredSize(new Dimension(300, 400));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Object identifiers")));

				referencesPanel.add(resultPanel);

				// Object references
				results = simbadQueryService.getObjectReferences(mainIdentifier);

				columns = new String[] { "Year", "Journal", "Volume", "Title", "Bibcode", "Ref" };
				JComponent component = createResultTable(results, columns, columns.length - 1);
				if (component instanceof JTable resultTable) {
					resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					resultTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
						if (!e.getValueIsAdjusting()) {
							String bibRef = (String) resultTable.getValueAt(resultTable.getSelectedRow(), 5);
							populateDetailsPanel(Integer.valueOf(bibRef), detailsPanel);

							String bibCode = (String) resultTable.getValueAt(resultTable.getSelectedRow(), 4);
							populateCatalogsPanel(bibCode, catalogsPanel);

							referencesFrame.setVisible(true);
						}
					});
					component = resultTable;
				}

				resultPanel = new JScrollPane(component);
				resultPanel.setPreferredSize(new Dimension(1500, 400));
				resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Object references")));

				referencesPanel.add(resultPanel);

				// Google search links
				searchPanel.add(new JLabel("Google search with object identifiers:"));
				try {
					for (String[] identifier : identifiers) {
						searchPanel.add(createHyperlink(identifier[0], "http://www.google.com/search?q="
								+ URLEncoder.encode("\"" + identifier[0] + "\"", ENCODING)));
					}
				} catch (UnsupportedEncodingException ex) {
					showExceptionDialog(null, ex);
				}

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

			String[] columns = new String[] { "Author" };
			JScrollPane resultPanel = new JScrollPane(createResultTable(results, columns, 0));
			resultPanel.setPreferredSize(new Dimension(300, 200));
			resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Authors")));

			detailsPanel.add(resultPanel);

			// Abstract
			String result = simbadQueryService.getAbstract(bibRef);

			JTextArea textArea = new JTextArea(result);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);

			resultPanel = new JScrollPane(textArea);
			resultPanel.setPreferredSize(new Dimension(900, 200));
			resultPanel.setBorder(ToolboxHelper.createEtchedBorder(bold("Abstract")));

			detailsPanel.add(resultPanel);
		} catch (IOException ex) {
			showExceptionDialog(null, ex);
		}
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
				catalogsPanel.add(createHyperlink(catalog,
						"http://vizier.u-strasbg.fr/viz-bin/VizieR?-source=%s".formatted(catalog)));
			});
		} catch (IOException ex) {
			showExceptionDialog(null, ex);
		}
	}

	private JComponent createResultTable(List<String[]> results, String[] columns, int linkColumn) {
		if (results.isEmpty()) {
			return new JLabel("N/A");
		}
		Object[][] rows = new Object[][] {};
		JTable resultTable = new JTable(results.toArray(rows), columns);
		alignResultColumns(resultTable, results);
		resizeColumnWidth(resultTable, 1000);
		resultTable.setAutoCreateRowSorter(true);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		if (linkColumn > 0) {
			TableColumn bibcodeCol = resultTable.getColumnModel().getColumn(linkColumn - 1);
			bibcodeCol.setCellRenderer(
					(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) -> {
						JLabel bibcodeLabel = new JLabel((String) value);
						bibcodeLabel.setForeground(JColor.LINK_BLUE.val);
						bibcodeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						return bibcodeLabel;
					});
			resultTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = resultTable.getSelectedRow();
					int col = resultTable.getSelectedColumn();
					if (col == linkColumn - 1) {
						String bibCode = (String) resultTable.getValueAt(row, col);
						try {
							URI uri = new URI("https://ui.adsabs.harvard.edu/abs/%s".formatted(bibCode));
							Desktop.getDesktop().browse(uri);
						} catch (IOException | URISyntaxException ex) {
							showExceptionDialog(null, ex);
						}
					}
				}
			});
		}
		return resultTable;
	}

}
