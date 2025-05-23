package astro.tool.box.tab;

import static astro.tool.box.function.NumericFunctions.roundTo2DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3Dec;
import static astro.tool.box.function.NumericFunctions.roundTo7DecNZLZ;
import static astro.tool.box.function.PhotometricFunctions.isAPossibleAGN;
import static astro.tool.box.function.PhotometricFunctions.isAPossibleWD;
import static astro.tool.box.main.ToolboxHelper.AGN_WARNING;
import static astro.tool.box.main.ToolboxHelper.WD_WARNING;
import static astro.tool.box.main.ToolboxHelper.alignResultColumns;
import static astro.tool.box.main.ToolboxHelper.createLabel;
import static astro.tool.box.main.ToolboxHelper.createResultTableSorter;
import static astro.tool.box.main.ToolboxHelper.getCatalogInstances;
import static astro.tool.box.main.ToolboxHelper.getCoordinates;
import static astro.tool.box.main.ToolboxHelper.isSameTarget;
import static astro.tool.box.main.ToolboxHelper.resizeColumnWidth;
import static astro.tool.box.main.ToolboxHelper.showErrorDialog;
import static astro.tool.box.main.ToolboxHelper.showExceptionDialog;
import static astro.tool.box.tab.SettingsTab.getSelectedCatalogs;
import static astro.tool.box.util.Comparators.getDoubleComparator;
import static astro.tool.box.util.Constants.LINE_SEP;
import static astro.tool.box.util.MiscUtils.SPECTRAL_TYPES;
import static astro.tool.box.util.MiscUtils.addToArray;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.GaiaDR2CatalogEntry;
import astro.tool.box.catalog.GaiaWDCatalogEntry;
import astro.tool.box.catalog.SimbadCatalogEntry;
import astro.tool.box.catalog.TessCatalogEntry;
import astro.tool.box.catalog.WhiteDwarf;
import astro.tool.box.container.ClassificationResult;
import astro.tool.box.container.ClassifierData;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.SpectralType;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.LookupResult;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.lookup.SpectralTypeLookupEntry;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.service.SpectralTypeLookupService;

public class PhotometricClassifierTab implements Tab {

	public static final String TAB_NAME = "Photometric Classifier";

	private final JFrame baseFrame;
	private final JTabbedPane tabbedPane;
	private final CatalogQueryTab catalogQueryTab;
	private final ImageViewerTab imageViewerTab;

	private final Map<String, CatalogEntry> catalogInstances;
	private final CatalogQueryService catalogQueryService;

	private final SpectralTypeLookupService mainSequenceLookupService;
	private final SpectralTypeLookupService brownDwarfsLookupService;

	private JPanel mainPanel;
	private JPanel topPanel;
	private JPanel centerPanel;
	private JPanel bottomPanel;
	private JButton searchButton;
	private JTextField coordsField;
	private JTextField radiusField;
	private JTable mainSequenceResultTable;
	private JTable brownDwarfsResultTable;

	private double targetRa;
	private double targetDec;
	private double searchRadius;

	private double prevTargetRa;
	private double prevTargetDec;
	private double prevSearchRadius;

	private Set<String> matchedColors;

	private Map<String, Integer> sptOccurrencesMainSequence;
	private Map<String, Integer> sptOccurrencesBrownDwarfs;
	private Map<String, Integer> sptOccurrencesSimbad;

	private List<CatalogEntry> catalogEntries;
	private List<ClassifierData> classifierListMainSequence;
	private List<ClassifierData> classifierListBrownDwarfs;

	public PhotometricClassifierTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab,
			ImageViewerTab imageViewerTab) {
		this.baseFrame = baseFrame;
		this.tabbedPane = tabbedPane;
		this.catalogQueryTab = catalogQueryTab;
		this.imageViewerTab = imageViewerTab;
		catalogInstances = getCatalogInstances();

		// Catalogs to be removed from the classification
		catalogInstances.remove(GaiaDR2CatalogEntry.CATALOG_NAME);
		catalogInstances.remove(GaiaWDCatalogEntry.CATALOG_NAME);
		catalogInstances.remove(TessCatalogEntry.CATALOG_NAME);

		catalogQueryService = new CatalogQueryService();
		try (InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv")) {
			Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines();
			List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
				return new SpectralTypeLookupEntry(line.split(",", -1));
			}).collect(Collectors.toList());
			mainSequenceLookupService = new SpectralTypeLookupService(entries);
		} catch (IOException e) {
			showExceptionDialog(baseFrame, e);
			throw new RuntimeException(e);
		}
		try (InputStream input = getClass().getResourceAsStream("/BrownDwarfLookupTable.csv");) {
			Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines();
			List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
				return new BrownDwarfLookupEntry(line.split(",", -1));
			}).collect(Collectors.toList());
			brownDwarfsLookupService = new SpectralTypeLookupService(entries);
		} catch (IOException e) {
			showExceptionDialog(baseFrame, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void init(boolean visible) {
		try {
			mainPanel = new JPanel(new BorderLayout());

			if (visible) {
				tabbedPane.addTab(TAB_NAME, new JScrollPane(mainPanel));
			}

			topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			mainPanel.add(topPanel, BorderLayout.PAGE_START);

			centerPanel = new JPanel(new GridLayout(2, 1));
			mainPanel.add(centerPanel, BorderLayout.CENTER);

			bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

			JLabel coordsLabel = new JLabel("Coordinates:");
			topPanel.add(coordsLabel);

			coordsField = new JTextField(25);
			topPanel.add(coordsField);

			JLabel radiusLabel = new JLabel("Search radius (arcsec):");
			topPanel.add(radiusLabel);

			radiusField = new JTextField(5);
			topPanel.add(radiusField);
			radiusField.setText("5");

			searchButton = new JButton("Classify");
			topPanel.add(searchButton);
			searchButton.addActionListener((ActionEvent e) -> {
				try {
					bottomPanel.removeAll();
					baseFrame.setVisible(true);
					String coords = coordsField.getText();
					if (coords.isEmpty()) {
						showErrorDialog(baseFrame, "Coordinates must not be empty!");
						return;
					}
					String radius = radiusField.getText();
					if (radius.isEmpty()) {
						showErrorDialog(baseFrame, "Search radius must not be empty!");
						return;
					}
					List<String> errorMessages = new ArrayList<>();
					try {
						NumberPair coordinates = getCoordinates(coords);
						targetRa = coordinates.getX();
						targetDec = coordinates.getY();
						if (targetRa < 0) {
							errorMessages.add("RA must not be smaller than 0 deg.");
						}
						if (targetRa > 360) {
							errorMessages.add("RA must not be greater than 360 deg.");
						}
						if (targetDec < -90) {
							errorMessages.add("Dec must not be smaller than -90 deg.");
						}
						if (targetDec > 90) {
							errorMessages.add("Dec must not be greater than 90 deg.");
						}
					} catch (Exception ex) {
						targetRa = 0;
						targetDec = 0;
						errorMessages.add("Invalid coordinates!");
					}
					try {
						searchRadius = Double.parseDouble(radius);
						if (searchRadius < 0) {
							errorMessages.add("Radius must not be smaller than 0.");
						}
						if (searchRadius > 100) {
							errorMessages.add("Radius must not be larger than 100 arcsec.");
						}
					} catch (NumberFormatException ex) {
						searchRadius = 0;
						errorMessages.add("Invalid radius!");
					}
					if (!errorMessages.isEmpty()) {
						String message = String.join(LINE_SEP, errorMessages);
						showErrorDialog(baseFrame, message);
					} else {
						if (!isSameTarget(targetRa, targetDec, searchRadius, prevTargetRa, prevTargetDec,
								prevSearchRadius)) {
							centerPanel.removeAll();
							baseFrame.setVisible(true);
						}
						CompletableFuture.supplyAsync(() -> {
							try {
								setWaitCursor();
								matchedColors = new HashSet();
								sptOccurrencesMainSequence = new HashMap();
								sptOccurrencesBrownDwarfs = new HashMap();
								sptOccurrencesSimbad = new HashMap();
								classifierListMainSequence = new ArrayList();
								classifierListBrownDwarfs = new ArrayList();
								if (isSameTarget(targetRa, targetDec, searchRadius, prevTargetRa, prevTargetDec,
										prevSearchRadius)) {
									performSpectralTypeLookup(mainSequenceLookupService, catalogEntries,
											sptOccurrencesMainSequence, classifierListMainSequence,
											mainSequenceResultTable);
									performSpectralTypeLookup(brownDwarfsLookupService, catalogEntries,
											sptOccurrencesBrownDwarfs, classifierListBrownDwarfs,
											brownDwarfsResultTable);
								} else {
									catalogEntries = new ArrayList<>();
									List<String> selectedCatalogs = getSelectedCatalogs(catalogInstances);
									for (CatalogEntry catalogEntry : catalogInstances.values()) {
										if (selectedCatalogs.contains(catalogEntry.getCatalogName())) {
											catalogEntry.setRa(targetRa);
											catalogEntry.setDec(targetDec);
											catalogEntry.setSearchRadius(searchRadius);
											List<CatalogEntry> results = performQuery(catalogEntry);
											if (results != null) {
												catalogEntries.addAll(results);
											}
										}
									}
									prevTargetRa = targetRa;
									prevTargetDec = targetDec;
									prevSearchRadius = searchRadius;
									List<ClassificationResult> classificationResults;
									classificationResults = performSpectralTypeLookup(mainSequenceLookupService,
											catalogEntries, sptOccurrencesMainSequence, classifierListMainSequence,
											null);
									mainSequenceResultTable = displayQueryResults(classificationResults,
											"Main sequence spectral type estimates", JColor.DARK_GREEN.val);
									classificationResults = performSpectralTypeLookup(brownDwarfsLookupService,
											catalogEntries, sptOccurrencesBrownDwarfs, classifierListBrownDwarfs, null);
									brownDwarfsResultTable = displayQueryResults(classificationResults,
											"M, L & T dwarfs spectral type estimates", JColor.BROWN.val);
								}
								displayClassification(sptOccurrencesMainSequence,
										"Photometric classification: Main sequence", JColor.DARK_GREEN.val);
								displayClassification(sptOccurrencesBrownDwarfs,
										"Photometric classification: M, L & T dwarfs", JColor.BROWN.val);
								displayClassifierData(classifierListMainSequence,
										"Colors used for classification: Main sequence", JColor.DARK_GREEN.val);
								displayClassifierData(classifierListBrownDwarfs,
										"Colors used for classification: M, L & T dwarfs", JColor.BROWN.val);
								if (!sptOccurrencesSimbad.isEmpty()) {
									displayClassification(sptOccurrencesSimbad, "SIMBAD object type", Color.RED);
								}
								baseFrame.setVisible(true);
							} catch (Exception ex) {
								showExceptionDialog(baseFrame, ex);
							} finally {
								setDefaultCursor();
							}
							return null;
						});
					}
				} catch (Exception ex) {
					showExceptionDialog(baseFrame, ex);
				}
			});

			coordsField.addActionListener((ActionEvent evt) -> {
				searchButton.getActionListeners()[0].actionPerformed(evt);
			});
			radiusField.addActionListener((ActionEvent evt) -> {
				searchButton.getActionListeners()[0].actionPerformed(evt);
			});
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		}
	}

	private void setWaitCursor() {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		radiusField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	private void setDefaultCursor() {
		baseFrame.setCursor(Cursor.getDefaultCursor());
		coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		radiusField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}

	private List<CatalogEntry> performQuery(CatalogEntry catalogQuery) throws IOException {
		List<CatalogEntry> entries = catalogQueryService.getCatalogEntriesByCoords(catalogQuery);
		entries.forEach(catalogEntry -> {
			catalogEntry.setTargetRa(catalogQuery.getRa());
			catalogEntry.setTargetDec(catalogQuery.getDec());
		});
		if (!entries.isEmpty()) {
			entries.sort(Comparator.comparingDouble(CatalogEntry::getTargetDistance));
			return entries;
		}
		return null;
	}

	private List<ClassificationResult> performSpectralTypeLookup(SpectralTypeLookupService spectralTypeLookupService,
			List<CatalogEntry> catalogEntries, Map<String, Integer> sptOccurrences, List<ClassifierData> classifierList,
			JTable resultTable) throws Exception {
		List<ClassificationResult> classificationResults = new ArrayList<>();
		for (CatalogEntry catalogEntry : catalogEntries) {
			String catalogName = catalogEntry.getCatalogName();
			String sourceId = catalogEntry.getSourceId();
			List<LookupResult> results = spectralTypeLookupService.lookup(catalogEntry.getColors(true));
			List<String> spectralTypes = new ArrayList<>();
			results.forEach(entry -> {
				String colorKey = entry.getColorKey().val;
				String colorValue = roundTo3Dec(entry.getColorValue());
				String nearest = roundTo3Dec(entry.getNearest());
				String matchedColor = colorKey + "=" + colorValue;
				String spectralType = entry.getSpt();
				addOccurrence(new ClassifierData(catalogName, colorKey, colorValue, spectralType, nearest, sourceId),
						sptOccurrences, classifierList, resultTable);
				spectralType += ": " + matchedColor + "; ";
				spectralTypes.add(spectralType);
			});
			if (catalogEntry instanceof SimbadCatalogEntry simbadEntry) {
				StringBuilder simbadType = new StringBuilder();
				simbadType.append(simbadEntry.getObjectType());
				String spectralType = simbadEntry.getSpectralType();
				if (!spectralType.isEmpty()) {
					simbadType.append(": ").append(spectralType);
				}
				sptOccurrencesSimbad.put(simbadType.toString(), 1);
				simbadType.append("; ");
				spectralTypes.add(0, simbadType.toString());
			}
			if (catalogEntry instanceof AllWiseCatalogEntry entry) {
				if (isAPossibleAGN(entry.getW1_W2(), entry.getW2_W3())) {
					String spectralType = AGN_WARNING;
					addOccurrence(new ClassifierData(catalogName, spectralType, sourceId), sptOccurrences,
							classifierList, resultTable);
					spectralTypes.add(spectralType);
				}
			}
			if (catalogEntry instanceof WhiteDwarf entry) {
				if (isAPossibleWD(entry.getAbsoluteGmag(), entry.getBP_RP())) {
					String spectralType = WD_WARNING;
					addOccurrence(new ClassifierData(catalogName, spectralType, sourceId), sptOccurrences,
							classifierList, resultTable);
					spectralTypes.add(spectralType);
				}
			}
			ClassificationResult classificationResult = new ClassificationResult.Builder()
					.setCatalogName(catalogEntry.getCatalogName()).setTargetRa(targetRa).setTargetDec(targetDec)
					.setTargetDistance(catalogEntry.getTargetDistance()).setRa(catalogEntry.getRa())
					.setDec(catalogEntry.getDec()).setSourceId(catalogEntry.getSourceId() + " ")
					.setPlx(catalogEntry.getPlx()).setPmra(catalogEntry.getPmra()).setPmdec(catalogEntry.getPmdec())
					.setMagnitudes(catalogEntry.getMagnitudes()).setSpectralTypes(spectralTypes).build();
			classificationResults.add(classificationResult);
		}
		return classificationResults;
	}

	private JTable displayQueryResults(List<ClassificationResult> classificationResults, String title,
			Color borderColor) {
		List<Object[]> resultRows = new ArrayList<>();
		classificationResults.forEach(entry -> {
			resultRows.add(addToArray(new Boolean[] { Boolean.FALSE }, entry.getColumnValues()));
		});
		ClassificationResult result = classificationResults.get(0);
		Object[] columns = addToArray(new String[] { "Remove from classification" }, result.getColumnTitles());
		Object[][] array = new Object[][] {};
		Object[][] rows = resultRows.toArray(array);
		DefaultTableModel defaultTableModel = new DefaultTableModel(rows, columns);
		JTable resultTable = new JTable(defaultTableModel) {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return rows[0][columnIndex].getClass();
			}
		};
		resultTable.setAutoCreateRowSorter(true);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (!e.getValueIsAdjusting()) {
				String sourceId = (String) resultTable.getValueAt(resultTable.getSelectedRow(), 7);
				ClassificationResult selected = classificationResults.stream().filter(entry -> {
					return entry.getSourceId().equals(sourceId);
				}).findFirst().get();
				if (selected != null) {
					String coords = roundTo7DecNZLZ(selected.getRa()) + " " + roundTo7DecNZLZ(selected.getDec());
					imageViewerTab.getCoordsField().setText(coords);
					catalogQueryTab.getCoordsField().setText(coords);
					catalogQueryTab.getRadiusField().setText(roundTo2DecNZ(searchRadius));
					catalogQueryTab.getSearchLabel().setText("");
					catalogQueryTab.removeAndRecreateCenterPanel();
					catalogQueryTab.removeAndRecreateBottomPanel();
				}
			}
		});
		resizeColumnWidth(resultTable, 300);

		TableColumnModel columnModel = resultTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(150);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		columnModel.getColumn(4).setCellRenderer(rightRenderer);
		columnModel.getColumn(8).setCellRenderer(rightRenderer);
		columnModel.getColumn(9).setCellRenderer(rightRenderer);
		columnModel.getColumn(10).setCellRenderer(rightRenderer);

		TableRowSorter<TableModel> sorter = new TableRowSorter<>(defaultTableModel);
		sorter.setComparator(4, getDoubleComparator());
		sorter.setComparator(8, getDoubleComparator());
		sorter.setComparator(9, getDoubleComparator());
		sorter.setComparator(10, getDoubleComparator());

		JScrollPane resultScrollPanel = new JScrollPane(resultTable);
		resultScrollPanel.setPreferredSize(new Dimension(resultScrollPanel.getWidth(), resultScrollPanel.getHeight()));
		resultScrollPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(borderColor, 2), title,
				TitledBorder.LEFT, TitledBorder.TOP));
		centerPanel.add(resultScrollPanel);
		return resultTable;
	}

	private void displayClassification(Map<String, Integer> sptOccurrences, String title, Color borderColor) {
		List<SpectralType> spectralTypes = new ArrayList();
		sptOccurrences.entrySet().forEach(entry -> {
			Double sptNum = SPECTRAL_TYPES.get(entry.getKey());
			spectralTypes.add(new SpectralType(entry.getKey(), entry.getValue(), sptNum == null ? -1 : sptNum));
		});
		spectralTypes.sort(Comparator.comparing(SpectralType::getOccurrences, Comparator.reverseOrder())
				.thenComparing(SpectralType::getSptNum));

		List<String[]> occurrences = new ArrayList();
		spectralTypes.forEach(spectralType -> {
			occurrences.add(new String[] { spectralType.getOccurrences().toString(), spectralType.getSpt() });
		});

		String titles = "occurrences,spectral type";
		String[] columns = titles.split(",", -1);
		Object[][] rows = new Object[][] {};
		DefaultTableModel defaultTableModel = new DefaultTableModel(occurrences.toArray(rows), columns);
		JTable resultTable = new JTable(defaultTableModel);
		alignResultColumns(resultTable, occurrences);
		resultTable.setAutoCreateRowSorter(true);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		resultTable.setRowSorter(createResultTableSorter(defaultTableModel, occurrences));
		TableColumnModel columnModel = resultTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(75);
		columnModel.getColumn(1).setPreferredWidth(195);

		JScrollPane resultScrollPanel = occurrences.isEmpty()
				? new JScrollPane(createLabel("No colors available / No match", JColor.RED))
				: new JScrollPane(resultTable);
		resultScrollPanel.setPreferredSize(new Dimension(300, 300));
		resultScrollPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(borderColor, 2), title,
				TitledBorder.LEFT, TitledBorder.TOP));
		bottomPanel.add(resultScrollPanel);
	}

	private void displayClassifierData(List<ClassifierData> classifierList, String title, Color borderColor) {
		classifierList.forEach(entry -> {
			Double sptNum = SPECTRAL_TYPES.get(entry.getSpectralType());
			entry.setSptNum(sptNum == null ? -1 : sptNum);
		});
		classifierList.sort(Comparator.comparing(ClassifierData::getCatalog, Comparator.naturalOrder())
				.thenComparing(ClassifierData::getSptNum));

		List<String[]> occurrences = new ArrayList();
		classifierList.forEach(classifierData -> {
			occurrences.add(new String[] { classifierData.getCatalog(), classifierData.getSpectralType(),
					classifierData.getColorKey(), classifierData.getColorValue(), classifierData.getReferenceColor(),
					classifierData.getSourceId() });
		});

		String titles = "catalog,spectral type,color,value,reference value,source id";
		String[] columns = titles.split(",", -1);
		Object[][] rows = new Object[][] {};
		DefaultTableModel defaultTableModel = new DefaultTableModel(occurrences.toArray(rows), columns);
		JTable resultTable = new JTable(defaultTableModel);
		alignResultColumns(resultTable, occurrences);
		resultTable.setAutoCreateRowSorter(true);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		resultTable.setRowSorter(createResultTableSorter(defaultTableModel, occurrences));
		TableColumnModel columnModel = resultTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(120);
		columnModel.getColumn(2).setPreferredWidth(80);
		columnModel.getColumn(4).setPreferredWidth(100);
		columnModel.getColumn(5).setPreferredWidth(150);

		JScrollPane resultScrollPanel = occurrences.isEmpty()
				? new JScrollPane(createLabel("No data available", JColor.RED))
				: new JScrollPane(resultTable);
		resultScrollPanel.setPreferredSize(new Dimension(400, 300));
		resultScrollPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(borderColor, 2), title,
				TitledBorder.LEFT, TitledBorder.TOP));
		bottomPanel.add(resultScrollPanel);
	}

	private void addOccurrence(ClassifierData classifierData, Map<String, Integer> sptOccurrences,
			List<ClassifierData> classifierList, JTable resultTable) {
		if (resultTable != null) {
			for (int i = 0; i < resultTable.getRowCount(); i++) {
				String sourceId = (String) resultTable.getValueAt(i, 7);
				Boolean remove = (Boolean) resultTable.getValueAt(i, 0);
				if (classifierData.getSourceId().equals(sourceId.trim()) && remove) {
					return;
				}
			}
		}
		String matchedColor = classifierData.getColorKey() + classifierData.getColorValue()
				+ classifierData.getSpectralType();
		String spectralType = classifierData.getSpectralType();
		if (matchedColors.contains(matchedColor)) {
			return;
		}
		Integer occurrences = sptOccurrences.get(spectralType);
		sptOccurrences.put(spectralType, occurrences == null ? 1 : occurrences + 1);
		matchedColors.add(matchedColor);
		classifierList.add(classifierData);
	}

}
