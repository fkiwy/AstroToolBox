package astro.tool.box.tab;

import astro.tool.box.catalog.*;
import astro.tool.box.container.Couple;
import astro.tool.box.container.FlipbookComponent;
import astro.tool.box.container.NirImage;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.ImageType;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.util.Counter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.roundTo3DecLZ;
import static astro.tool.box.function.NumericFunctions.roundTo7DecNZ;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.tab.SettingsTab.getSelectedCatalogs;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.DEG_MAS;
import static astro.tool.box.util.ExternalResources.getLegacySingleExposuresUrl;
import static astro.tool.box.util.ExternalResources.getPanstarrsUrl;
import static java.lang.Math.sqrt;

public class ImageSeriesTab implements Tab {

	public static final String TAB_NAME = "Image Series";
	private static final long SURVEY_TIMEOUT_SECONDS = 20;
	private static final ExecutorService IMAGE_DOWNLOAD_EXECUTOR = Executors.newFixedThreadPool(4,
			new ImageDownloadThreadFactory());
	// A stalled WFA request must not occupy a worker needed by the other archives.
	private static final ExecutorService NIR_DOWNLOAD_EXECUTOR = Executors.newFixedThreadPool(2,
			new ImageDownloadThreadFactory());

	private final JFrame baseFrame;
	private final JTabbedPane tabbedPane;
	private final ImageViewerTab imageViewerTab;

	private final Map<String, CatalogEntry> catalogInstances;
	private final CatalogQueryService catalogQueryService;

	private List<Couple<String, NirImage>> timeSeries;
	private List<Couple<String, BufferedImage>> desiImages;
	private List<Couple<String, BufferedImage>> wiseImages;

	private Timer timeSeriesTimer;
	private Timer desiTimeSeriesTimer;
	private Timer wiseTimeSeriesTimer;

	private JPanel mainPanel;
	private JPanel topPanel;
	private JPanel centerPanel;
	private JScrollPane scrollPanel;
	private JTabbedPane bottomPanel;
	private JButton searchButton;
	private JTextField coordsField;
	private JTextField fovField;
	private JTable currentTable;

	private TwoMassCatalogEntry twoMassEntry;
	private AllWiseCatalogEntry allWiseEntry;
	private SdssCatalogEntry sdssEntry;
	private PanStarrsCatalogEntry panStarrsEntry;
	private GaiaDR3CatalogEntry gaiaDR3Entry;
	private CatWiseCatalogEntry catWiseEntry;
	private NoirlabCatalogEntry noirlabEntry;
	private UkidssCatalogEntry ukidssEntry;

	private double targetRa;
	private double targetDec;
	private int fieldOfView;

	private double prevTargetRa;
	private double prevTargetDec;
	private int prevFieldOfView;
	private boolean legacyImages = true;
	private boolean panstarrsImages = true;
	private boolean vhsImages = true;
	private boolean uhsImages = true;
	private boolean ukidssImages = true;
	private boolean sdssImages = true;
	private boolean dssImages = true;
	private boolean twoMassImages = true;
	private boolean spitzerImages = true;
	private boolean wiseImagesEnabled = true;
	private boolean wiseTimeSeries = true;

	public ImageSeriesTab(JFrame baseFrame, JTabbedPane tabbedPane, ImageViewerTab imageViewerTab) {
		this.baseFrame = baseFrame;
		this.tabbedPane = tabbedPane;
		this.imageViewerTab = imageViewerTab;
		catalogInstances = getCatalogInstances();
		catalogQueryService = new CatalogQueryService();
	}

	@Override
	public void init(boolean visible) {
		try {
			mainPanel = new JPanel(new BorderLayout());

			if (visible) {
				tabbedPane.addTab(TAB_NAME, mainPanel);
			}

			topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			mainPanel.add(topPanel, BorderLayout.PAGE_START);

			centerPanel = new JPanel();
			centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
			scrollPanel = new JScrollPane(centerPanel);
			mainPanel.add(scrollPanel, BorderLayout.CENTER);

			bottomPanel = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
			bottomPanel.setPreferredSize(new Dimension(bottomPanel.getWidth(), 200));
			mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

			JLabel coordsLabel = new JLabel("Coordinates:");
			topPanel.add(coordsLabel);

			coordsField = new JTextField(25);
			topPanel.add(coordsField);

			JLabel fovLabel = new JLabel("Field of view (arcsec):");
			topPanel.add(fovLabel);

			fovField = new JTextField(5);
			topPanel.add(fovField);
			fovField.setText("30");

			searchButton = new JButton("Search");
			topPanel.add(searchButton);
			searchButton.addActionListener((ActionEvent e) -> {
				try {
					String coords = coordsField.getText();
					if (coords.isEmpty()) {
						showErrorDialog(baseFrame, "Coordinates must not be empty!");
						return;
					}
					String fov = fovField.getText();
					if (fov.isEmpty()) {
						showErrorDialog(baseFrame, "Field of view must not be empty!");
						return;
					}
					List<String> errorMessages = new ArrayList<>();
					try {
						NumberPair coordinates = getCoordinates(coords);
						targetRa = coordinates.x();
						targetDec = coordinates.y();
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
						fieldOfView = Integer.parseInt(fov);
						if (fieldOfView < 10) {
							errorMessages.add("Field of view must not be smaller than 10 arcsec.");
						}
						if (fieldOfView > 300) {
							errorMessages.add("Field of view must not be larger than 300 arcsec.");
						}
					} catch (NumberFormatException ex) {
						fieldOfView = 0;
						errorMessages.add("Invalid field of view!");
					}
					if (targetRa == prevTargetRa && targetDec == prevTargetDec && fieldOfView == prevFieldOfView) {
						return;
					}
					timeSeries = new ArrayList<>();
					desiImages = new ArrayList<>();
					wiseImages = new ArrayList<>();
					timeSeriesTimer = null;
					desiTimeSeriesTimer = null;
					wiseTimeSeriesTimer = null;
					if (centerPanel.getComponentCount() > 0) {
						centerPanel.removeAll();
					}
					if (bottomPanel.getComponentCount() > 0) {
						bottomPanel.removeAll();
					}
					twoMassEntry = null;
					allWiseEntry = null;
					sdssEntry = null;
					panStarrsEntry = null;
					gaiaDR3Entry = null;
					catWiseEntry = null;
					noirlabEntry = null;
					ukidssEntry = null;
					prevTargetRa = targetRa;
					prevTargetDec = targetDec;
					prevFieldOfView = fieldOfView;
					stopTimers();
					if (!errorMessages.isEmpty()) {
						String message = String.join(LINE_SEP, errorMessages);
						showErrorDialog(baseFrame, message);
					} else {
						imageViewerTab.setWaitCursor(false);
						JTextField coordinateField = imageViewerTab.getCoordsField();
						ActionListener actionListener = coordinateField.getActionListeners()[0];
						coordinateField.removeActionListener(actionListener);
						coordinateField.setText(roundTo7DecNZ(targetRa) + " " + roundTo7DecNZ(targetDec));
						coordinateField.addActionListener(actionListener);
						JTextField sizeField = imageViewerTab.getSizeField();
						actionListener = sizeField.getActionListeners()[0];
						sizeField.removeActionListener(actionListener);
						sizeField.setText(String.valueOf(fieldOfView));
						sizeField.addActionListener(actionListener);
						imageViewerTab.getZoomSlider().setValue(250);
						imageViewerTab.getSkipIntermediateEpochs().setSelected(false);
						imageViewerTab.createFlipbook();

						CompletableFuture.supplyAsync(() -> {
							try {
								setWaitCursor();
								displayImages(targetRa, targetDec, fieldOfView);
								baseFrame.setVisible(true);
							} catch (Exception ex) {
								showExceptionDialog(baseFrame, ex);
							} finally {
								imageViewerTab.setWaitCursor(true);
								setDefaultCursor();
							}
							return null;
						});

						CompletableFuture.supplyAsync(() -> {
							try {
								List<String> selectedCatalogs = getSelectedCatalogs(catalogInstances);
								for (CatalogEntry catalogEntry : catalogInstances.values()) {
									if (selectedCatalogs.contains(catalogEntry.getCatalogName())) {
										double searchRadius = fieldOfView * sqrt(2) / 2; // diagonal of the fov divided
										// by 2
										catalogEntry.setRa(targetRa);
										catalogEntry.setDec(targetDec);
										catalogEntry.setSearchRadius(searchRadius);
										List<CatalogEntry> results;
										results = performQuery(catalogEntry);
										if (results != null) {
											displayCatalogResults(results);
										}
									}
								}
								List<String[]> resultRows = new ArrayList<>();
								if (twoMassEntry != null && allWiseEntry != null) {
									long days = Duration.between(twoMassEntry.getObsDate(),
											convertMJDToDateTime(new BigDecimal("55400"))).toDays();
									NumberPair properMotions = calculateProperMotions(
											new NumberPair(twoMassEntry.getRa(), twoMassEntry.getDec()),
											new NumberPair(allWiseEntry.getRa_pm(), allWiseEntry.getDec_pm()), 0,
											(int) days, DEG_MAS);
									double pmRa = properMotions.x();
									double pmDec = properMotions.y();
									double tpm = calculateTotalProperMotion(pmRa, pmDec);
									resultRows.add(new String[]{
											"Calculated from " + TwoMassCatalogEntry.CATALOG_NAME + " and "
													+ AllWiseCatalogEntry.CATALOG_NAME + " coordinates",
											twoMassEntry.getSourceId(), roundTo3DecLZ(twoMassEntry.getTargetDistance()),
											allWiseEntry.getSourceId(), roundTo3DecLZ(allWiseEntry.getTargetDistance()),
											roundTo3DecLZ(tpm), roundTo3DecLZ(pmRa), roundTo3DecLZ(pmDec), "N/A",
											"N/A"});
								}
								if (sdssEntry != null && panStarrsEntry != null) {
									long days = Duration.between(sdssEntry.getObsDate(), panStarrsEntry.getObsDate())
											.toDays();
									NumberPair properMotions = calculateProperMotions(
											new NumberPair(sdssEntry.getRa(), sdssEntry.getDec()),
											new NumberPair(panStarrsEntry.getRa(), panStarrsEntry.getDec()), 0,
											(int) days, DEG_MAS);
									double pmRa = properMotions.x();
									double pmDec = properMotions.y();
									double tpm = calculateTotalProperMotion(pmRa, pmDec);
									resultRows.add(new String[]{
											"Calculated from " + SdssCatalogEntry.CATALOG_NAME + " and "
													+ PanStarrsCatalogEntry.CATALOG_NAME + " coordinates",
											sdssEntry.getSourceId(), roundTo3DecLZ(sdssEntry.getTargetDistance()),
											panStarrsEntry.getSourceId(),
											roundTo3DecLZ(panStarrsEntry.getTargetDistance()), roundTo3DecLZ(tpm),
											roundTo3DecLZ(pmRa), roundTo3DecLZ(pmDec), "N/A", "N/A"});
								}
								addProperMotionEntry(gaiaDR3Entry, resultRows);
								addProperMotionEntry(catWiseEntry, resultRows);
								if (noirlabEntry != null && noirlabEntry.getNdet() >= 5
										&& noirlabEntry.getDelta_mjd() >= 180) {
									addProperMotionEntry(noirlabEntry, resultRows);
								}
								addProperMotionEntry(ukidssEntry, resultRows);
								if (!resultRows.isEmpty()) {
									String[] columns = new String[]{"Proper motion origin", "source 1",
											"dist. from target (arcsec)", "source 2", "dist. from target (arcsec)",
											"tpm (mas/yr)", "pmRA (mas/yr)", "pmDE (mas/yr)", "pmRA error",
											"pmDE error"};
									Object[][] rows = new Object[][]{};
									JTable resultTable = new JTable(resultRows.toArray(rows), columns);
									resultTable.setAutoCreateRowSorter(true);
									resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
									resultTable.getSelectionModel()
											.addListSelectionListener((ListSelectionEvent event) -> {
												if (!event.getValueIsAdjusting()) {
													String label = (String) resultTable
															.getValueAt(resultTable.getSelectedRow(), 0);
													if (label.isEmpty()) {
														return;
													}
													deselectedCatalogOverlay(imageViewerTab);
													if (label.contains(TwoMassCatalogEntry.CATALOG_NAME)) {
														activateSelectedCatalogOverlay(imageViewerTab, twoMassEntry);
														activateSelectedCatalogOverlay(imageViewerTab, allWiseEntry);
													} else if (label.contains(SdssCatalogEntry.CATALOG_NAME)) {
														activateSelectedCatalogOverlay(imageViewerTab, sdssEntry);
														activateSelectedCatalogOverlay(imageViewerTab, panStarrsEntry);
													} else if (label.contains(GaiaDR3CatalogEntry.CATALOG_NAME)) {
														activateSelectedCatalogOverlay(imageViewerTab, gaiaDR3Entry);
													} else if (label.contains(CatWiseCatalogEntry.CATALOG_NAME)) {
														activateSelectedCatalogOverlay(imageViewerTab, catWiseEntry);
													} else if (label.contains(NoirlabCatalogEntry.CATALOG_NAME)) {
														activateSelectedCatalogOverlay(imageViewerTab, noirlabEntry);
													} else if (label.contains(UkidssCatalogEntry.CATALOG_NAME)) {
														activateSelectedCatalogOverlay(imageViewerTab, ukidssEntry);
													}
													tabbedPane.setSelectedIndex(0);
												}
											});
									TableColumnModel columnModel = resultTable.getColumnModel();
									DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
									leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
									DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
									rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
									// Column width
									columnModel.getColumn(0).setPreferredWidth(325);
									columnModel.getColumn(1).setPreferredWidth(150);
									columnModel.getColumn(2).setPreferredWidth(100);
									columnModel.getColumn(3).setPreferredWidth(150);
									columnModel.getColumn(4).setPreferredWidth(100);
									columnModel.getColumn(5).setPreferredWidth(100);
									columnModel.getColumn(6).setPreferredWidth(100);
									columnModel.getColumn(7).setPreferredWidth(100);
									columnModel.getColumn(8).setPreferredWidth(100);
									columnModel.getColumn(9).setPreferredWidth(100);
									// Column alignment
									columnModel.getColumn(0).setCellRenderer(leftRenderer);
									columnModel.getColumn(1).setCellRenderer(leftRenderer);
									columnModel.getColumn(2).setCellRenderer(rightRenderer);
									columnModel.getColumn(3).setCellRenderer(leftRenderer);
									columnModel.getColumn(4).setCellRenderer(rightRenderer);
									columnModel.getColumn(5).setCellRenderer(rightRenderer);
									columnModel.getColumn(6).setCellRenderer(rightRenderer);
									columnModel.getColumn(7).setCellRenderer(rightRenderer);
									columnModel.getColumn(8).setCellRenderer(rightRenderer);
									columnModel.getColumn(9).setCellRenderer(rightRenderer);
									JPanel container = new JPanel();
									container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
									container.add(new JScrollPane(resultTable));
									JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
									messagePanel.add(new JLabel(red("Warning:")));
									messagePanel.add(new JLabel(
											"The entries listed above do not necessarily belong to the same object."));
									messagePanel.add(new JLabel(
											"Clicking on an entry will take you to the Image Viewer with the appropriate overlays enabled."));
									container.add(messagePanel);
									bottomPanel.addTab("Proper motions", container);
									bottomPanel.setSelectedIndex(bottomPanel.getTabCount() - 1);
								}
								baseFrame.setVisible(true);
							} catch (IOException ex) {
								showExceptionDialog(baseFrame, ex);
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
			fovField.addActionListener((ActionEvent evt) -> {
				searchButton.getActionListeners()[0].actionPerformed(evt);
			});
			tabbedPane.addChangeListener((ChangeEvent evt) -> {
				JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
				int index = sourceTabbedPane.getSelectedIndex();
				if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
					restartTimers();
				} else {
					stopTimers();
				}
			});
		} catch (Exception ex) {
			showExceptionDialog(baseFrame, ex);
		}
	}

	private void restartTimers() {
		if (timeSeriesTimer != null) {
			timeSeriesTimer.restart();
		}
		if (desiTimeSeriesTimer != null) {
			desiTimeSeriesTimer.restart();
		}
		if (wiseTimeSeriesTimer != null) {
			wiseTimeSeriesTimer.restart();
		}
	}

	private void stopTimers() {
		if (timeSeriesTimer != null) {
			timeSeriesTimer.stop();
		}
		if (desiTimeSeriesTimer != null) {
			desiTimeSeriesTimer.stop();
		}
		if (wiseTimeSeriesTimer != null) {
			wiseTimeSeriesTimer.stop();
		}
	}

	private void setWaitCursor() {
		baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		fovField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	private void setDefaultCursor() {
		baseFrame.setCursor(Cursor.getDefaultCursor());
		coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		fovField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}

	private void addProperMotionEntry(ProperMotionCatalog entry, List<String[]> resultRows) {
		if (entry != null && entry.getTotalProperMotion() > 0) {
			double tpm = entry.getTotalProperMotion();
			double pmRa = entry.getPmra();
			double pmDec = entry.getPmdec();
			double pmRaErr = entry.getPmraErr();
			double pmDecErr = entry.getPmdecErr();
			resultRows.add(new String[]{entry.getCatalogName(), entry.getSourceId(),
					roundTo3DecLZ(entry.getTargetDistance()), "N/A", "N/A", roundTo3DecLZ(tpm), roundTo3DecLZ(pmRa),
					roundTo3DecLZ(pmDec), roundTo3DecLZ(pmRaErr), roundTo3DecLZ(pmDecErr)});
		}
	}

	/**
	 * Downloads are deliberately parallel only at survey granularity.  A survey can
	 * make several requests itself, but keeping those requests together prevents us
	 * from flooding the remote archive servers.
	 */
	private void displayImages(double targetRa, double targetDec, int size) throws Exception {
		List<CompletableFuture<SurveyResult>> downloads = new ArrayList<>();
		if (dssImages) addDownload(downloads, downloadIrsaSurvey("DSS", targetRa, targetDec, size,
				new String[][]{{"DSS1 B", "poss1_blue"}, {"DSS1 R", "poss1_red"},
						{"DSS2 B", "poss2ukstu_blue"}, {"DSS2 R", "poss2ukstu_red"},
						{"DSS IR", "poss2ukstu_ir"}, {"DSS IR-R-B", "colorimage"}}, "dss", "DSS IR"));
		if (twoMassImages) addDownload(downloads, downloadIrsaSurvey("2MASS", targetRa, targetDec, size,
				new String[][]{{"2MASS J", "j"}, {"2MASS H", "h"}, {"2MASS K", "k"},
						{"2MASS K-H-J", "colorimage"}}, "2mass", "2MASS K"));
		if (sdssImages) addDownload(downloads, downloadIrsaSurvey("SDSS", targetRa, targetDec, size,
				new String[][]{{"SDSS u", "u"}, {"SDSS g", "g"}, {"SDSS r", "r"},
						{"SDSS i", "i"}, {"SDSS z", "z"}, {"SDSS z-g-u", "colorimage"}}, "sdss", "SDSS z"));
		if (spitzerImages) addDownload(downloads, downloadIrsaSurvey("Spitzer", targetRa, targetDec, size,
				new String[][]{{"IRAC1", "spitzer.seip_science:IRAC1"}, {"IRAC2", "spitzer.seip_science:IRAC2"},
						{"IRAC3", "spitzer.seip_science:IRAC3"}, {"IRAC4", "spitzer.seip_science:IRAC4"},
						{"MIPS24", "spitzer.seip_science:MIPS24"}, {"IRAC3-2-1", "colorimage"}}, "seip", "IRAC4"));
		if (wiseImagesEnabled) addDownload(downloads, downloadIrsaSurvey("WISE", targetRa, targetDec, size,
				new String[][]{{"WISE W1", "1"}, {"WISE W2", "2"}, {"WISE W3", "3"},
						{"WISE W4", "4"}, {"WISE W4-W2-W1", "colorimage"}}, "wise", "WISE W2"));
		if (ukidssImages && targetDec > -5) addDownload(downloads, downloadNirSurvey(UKIDSS_LABEL, UKIDSS_SURVEY_URL, targetRa, targetDec, size));
		if (uhsImages && targetDec > -5) addDownload(downloads, downloadNirSurvey(UHS_LABEL, UHS_SURVEY_URL, targetRa, targetDec, size));
		if (vhsImages && targetDec < 5) addDownload(downloads, downloadNirSurvey(VHS_LABEL, VHS_SURVEY_URL, targetRa, targetDec, size));
		if (panstarrsImages) addDownload(downloads, downloadPs1Survey(targetRa, targetDec, size));
		if (legacyImages) {
			addDownload(downloads, downloadDesiSurvey(targetRa, targetDec, size));
			addDownload(downloads, downloadDesiHistory(targetRa, targetDec, size));
		}

		CompletableFuture.allOf(downloads.toArray(CompletableFuture[]::new)).join();
		List<SurveyResult> results = downloads.stream().map(CompletableFuture::join).toList();
		SwingUtilities.invokeAndWait(() -> {
			// A later search may have started while these requests were in flight.
			if (targetRa == this.targetRa && targetDec == this.targetDec && size == fieldOfView) {
				displayDownloadedSurveys(results);
			}
		});
	}

	private void addDownload(List<CompletableFuture<SurveyResult>> downloads, CompletableFuture<SurveyResult> download) {
		downloads.add(download.completeOnTimeout(new SurveyResult("", List.of(), null), SURVEY_TIMEOUT_SECONDS,
				TimeUnit.SECONDS));
	}

	private CompletableFuture<SurveyResult> downloadIrsaSurvey(String name, double ra, double dec, int size,
			String[][] bands, String survey, String timeSeriesLabel) {
		return CompletableFuture.supplyAsync(() -> {
			List<SurveyImage> images = new ArrayList<>();
			for (String[] band : bands) {
				String value = band[1];
				String archiveBand = value.equals("colorimage") ? "file_type=colorimage" : surveyBand(survey, value);
				int year = value.equals("colorimage") ? 0 : getEpoch(ra, dec, size, survey, archiveBand);
				BufferedImage image = retrieveImage(ra, dec, size, survey,
						value.equals("colorimage") ? archiveBand : archiveBand + "&type=jpgurl");
				if (image != null) images.add(new SurveyImage(band[0], year, image, band[0].equals(timeSeriesLabel)));
			}
			return new SurveyResult(name, images, null);
		}, IMAGE_DOWNLOAD_EXECUTOR).exceptionally(ex -> new SurveyResult(name, List.of(), null));
	}

	private String surveyBand(String survey, String band) {
		return switch (survey) {
			case "dss" -> "dss_bands=" + band;
			case "2mass" -> "twomass_bands=" + band;
			case "sdss" -> "sdss_bands=" + band;
			case "wise" -> "wise_bands=" + band;
			case "seip" -> "seip_bands=" + band;
			default -> band;
		};
	}

	private CompletableFuture<SurveyResult> downloadNirSurvey(String name, String url, double ra, double dec, int size) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				List<SurveyImage> images = new ArrayList<>();
				for (Entry<String, NirImage> entry : retrieveNearInfraredImages(ra, dec, size, url, name).entrySet()) {
					NirImage image = entry.getValue();
					images.add(new SurveyImage(name + " " + entry.getKey(), image.getYear(), image.getImage(), entry.getKey().equals("K")));
				}
				return new SurveyResult(name, images, null);
			} catch (Exception ex) { return new SurveyResult(name, List.of(), null); }
		}, NIR_DOWNLOAD_EXECUTOR);
	}

	private CompletableFuture<SurveyResult> downloadPs1Survey(double ra, double dec, int size) {
		return CompletableFuture.supplyAsync(() -> {
			Map<String, String> files = getPs1FileNames(ra, dec);
			Map<String, Double> years = getPs1Epochs(ra, dec);
			List<SurveyImage> images = new ArrayList<>();
			for (String band : List.of("g", "r", "i", "z", "y")) {
				BufferedImage image = retrievePs1Image("red=%s".formatted(files.get(band)), ra, dec, size, true);
				images.add(new SurveyImage("PS1 " + band, years.getOrDefault(band, 0d).intValue(), image, band.equals("z")));
			}
			BufferedImage color = retrievePs1Image("red=%s&green=%s&blue=%s".formatted(files.get("y"), files.get("i"), files.get("g")), ra, dec, size, false);
			images.add(new SurveyImage("PS1 y-i-g", 0, color, false));
			return new SurveyResult("Pan-STARRS", images, getPanstarrsUrl(ra, dec, size, ImageType.WARP));
		}, IMAGE_DOWNLOAD_EXECUTOR).exceptionally(ex -> new SurveyResult("Pan-STARRS", List.of(), null));
	}

	private CompletableFuture<SurveyResult> downloadDesiSurvey(double ra, double dec, int size) {
		return CompletableFuture.supplyAsync(() -> {
			List<SurveyImage> images = new ArrayList<>();
			for (String band : List.of("g", "r", "z")) {
				BufferedImage image = retrieveDesiImage(ra, dec, size, band, true);
				if (image != null) images.add(new SurveyImage("DESI " + band, DESI_LS_EPOCH, image, band.equals("z")));
			}
			BufferedImage color = retrieveDesiImage(ra, dec, size, DESI_FILTERS, false);
			if (color != null) images.add(new SurveyImage("DECaLS", DESI_LS_EPOCH, color, false));
			return new SurveyResult("DESI LS", images, getLegacySingleExposuresUrl(ra, dec, DESI_LS_DR_PARAM));
		}, IMAGE_DOWNLOAD_EXECUTOR).exceptionally(ex -> new SurveyResult("DESI LS", List.of(), null));
	}

	private CompletableFuture<SurveyResult> downloadDesiHistory(double ra, double dec, int size) {
		return CompletableFuture.supplyAsync(() -> {
			List<SurveyImage> images = new ArrayList<>();
			for (String[] layer : new String[][]{{"DESI DR5", "decals-dr5"}, {"DESI DR7", "decals-dr7"},
					{"LS DR8", "ls-dr8"}, {"LS DR9", "ls-dr9"}, {"LS DR10", "ls-dr10"}}) {
				BufferedImage image = retrieveDesiImage(ra, dec, size, DESI_FILTERS, false, layer[1]);
				if (image != null) images.add(new SurveyImage(layer[0], 0, image, false));
			}
			return new SurveyResult("DESI LS time series", images, null);
		}, IMAGE_DOWNLOAD_EXECUTOR).exceptionally(ex -> new SurveyResult("DESI LS time series", List.of(), null));
	}

	private void displayDownloadedSurveys(List<SurveyResult> results) {
		for (SurveyResult result : results) {
			if (result.images().isEmpty()) continue;
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			for (SurveyImage image : result.images()) panel.add(buildImagePanel(image.image(), getImageLabel(image.label(), image.year())));
			if (result.name().equals("DESI LS time series") && result.images().size() > 2) {
				desiImages = result.images().stream()
					.map(image -> new Couple<>(image.label(), image.image())).toList();
				createTimeSeriesTimer(panel, desiImages, desiTimeSeriesTimer = new Timer(500, null));
				addGifSaveButton(panel, desiImages);
			}
			if (result.link() != null) panel.add(createHyperlink(result.name().equals("Pan-STARRS") ? "WARP images" : "Single exposures", result.link()));
			centerPanel.add(panel);
			for (SurveyImage image : result.images()) if (image.timeSeries()) timeSeries.add(new Couple<>(getImageLabel(image.label(), image.year()), new NirImage(image.year(), image.image())));
		}
		timeSeries.sort(Comparator.comparing(c -> c.b().getYear()));
		addCrossSurveyTimeSeries();
		addWiseTimeSeries();
		baseFrame.setVisible(true);
		scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
	}

	private void addCrossSurveyTimeSeries() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		List<Couple<String, BufferedImage>> images = new ArrayList<>();
		for (Couple<String, NirImage> image : timeSeries) images.add(new Couple<>(image.a(), image.b().getImage()));
		createTimeSeriesTimer(panel, images, timeSeriesTimer = new Timer(500, null));
		if (images.size() > 1) addGifSaveButton(panel, images);
		if (panel.getComponentCount() > 0) centerPanel.add(panel);
	}

	private void addWiseTimeSeries() {
		imageViewerTab.waitForFlipbookReady();
		List<FlipbookComponent> flipbook = imageViewerTab.getFlipbook();
		if (!wiseTimeSeries || flipbook == null || flipbook.isEmpty()) return;
		wiseImages = new ArrayList<>();
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		for (int i = 0; i < flipbook.size(); i++) {
			FlipbookComponent component = flipbook.get(i);
			BufferedImage image = imageViewerTab.processImage(component, i);
			panel.add(buildImagePanel(image, component.getTitle()));
			wiseImages.add(new Couple<>(component.getTitle(), image));
		}
		createTimeSeriesTimer(panel, wiseImages, wiseTimeSeriesTimer = new Timer(500, null));
		addGifSaveButton(panel, wiseImages);
		centerPanel.add(panel);
	}

	private void addGifSaveButton(JPanel panel, List<Couple<String, BufferedImage>> images) {
		JButton saveButton = new JButton("Save as GIF");
		panel.add(saveButton);
		saveButton.addActionListener(event -> { try { saveAnimatedGif(images, saveButton); } catch (Exception ex) { showExceptionDialog(baseFrame, ex); } });
	}

	private record SurveyImage(String label, int year, BufferedImage image, boolean timeSeries) { }
	private record SurveyResult(String name, List<SurveyImage> images, String link) { }
	private static final class ImageDownloadThreadFactory implements ThreadFactory {
		private final AtomicInteger number = new AtomicInteger();
		@Override public Thread newThread(Runnable task) {
			Thread thread = new Thread(task, "image-survey-download-" + number.incrementAndGet());
			thread.setDaemon(true);
			return thread;
		}
	}

	private void createTimeSeriesTimer(JPanel bandPanel, List<Couple<String, BufferedImage>> imageList, Timer timer) {
		int componentCount = imageList.size();
		if (componentCount > 1) {
			JPanel displayPanel = new JPanel();
			bandPanel.add(displayPanel);
			Counter imageCounter = new Counter();
			timer.addActionListener((ActionEvent e) -> {
				if (imageCounter.value() > componentCount - 1) {
					imageCounter.init();
				}
				displayPanel.removeAll();
				Couple<String, BufferedImage> imageData = imageList.get(imageCounter.value());
				displayPanel.add(buildImagePanel(imageData.b(), imageData.a()));
				baseFrame.repaint();
				imageCounter.add();
			});
			timer.start();
		}
	}

	private JPanel buildImagePanel(BufferedImage image, String imageLabel) {
		JLabel label = addTextToImage(image, imageLabel);
		JPanel panel = new JPanel();
		panel.add(label);
		return panel;
	}

	private List<CatalogEntry> performQuery(CatalogEntry catalogQuery) throws IOException {
		List<CatalogEntry> catalogEntries = catalogQueryService.getCatalogEntriesByCoords(catalogQuery);
		catalogEntries.forEach(catalogEntry -> {
			catalogEntry.setTargetRa(catalogQuery.getRa());
			catalogEntry.setTargetDec(catalogQuery.getDec());
			catalogEntry.loadCatalogElements();
		});
		if (!catalogEntries.isEmpty()) {
			catalogEntries.sort(Comparator.comparingDouble(CatalogEntry::getTargetDistance));
			CatalogEntry nearestEntry = catalogEntries.get(0);
			if (nearestEntry.getTargetDistance() < 10) {
				switch (nearestEntry.getCatalogName()) {
					case TwoMassCatalogEntry.CATALOG_NAME -> twoMassEntry = (TwoMassCatalogEntry) nearestEntry;
					case AllWiseCatalogEntry.CATALOG_NAME -> allWiseEntry = (AllWiseCatalogEntry) nearestEntry;
					case SdssCatalogEntry.CATALOG_NAME -> sdssEntry = (SdssCatalogEntry) nearestEntry;
					case PanStarrsCatalogEntry.CATALOG_NAME -> panStarrsEntry = (PanStarrsCatalogEntry) nearestEntry;
					case GaiaDR3CatalogEntry.CATALOG_NAME -> gaiaDR3Entry = (GaiaDR3CatalogEntry) nearestEntry;
					case CatWiseCatalogEntry.CATALOG_NAME -> catWiseEntry = (CatWiseCatalogEntry) nearestEntry;
					case NoirlabCatalogEntry.CATALOG_NAME -> noirlabEntry = (NoirlabCatalogEntry) nearestEntry;
					case UkidssCatalogEntry.CATALOG_NAME -> ukidssEntry = (UkidssCatalogEntry) nearestEntry;
				}
			}
			return catalogEntries;
		}
		return null;
	}

	private void displayCatalogResults(List<CatalogEntry> catalogEntries) {
		List<Object[]> list = new ArrayList<>();
		catalogEntries.forEach(entry -> {
			list.add(entry.getColumnValues());
		});
		CatalogEntry catalogEntry = catalogEntries.get(0);
		Object[] columns = catalogEntry.getColumnTitles();
		Object[][] rows = new Object[][]{};
		DefaultTableModel defaultTableModel = new DefaultTableModel(list.toArray(rows), columns);
		JTable catalogTable = new JTable(defaultTableModel);
		alignCatalogColumns(catalogTable, catalogEntry);
		catalogTable.setAutoCreateRowSorter(true);
		catalogTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		catalogTable.setRowSorter(createCatalogTableSorter(defaultTableModel, catalogEntry));
		catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		catalogTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
			if (!e.getValueIsAdjusting()) {
				if (currentTable != null && currentTable != catalogTable) {
					try {
						currentTable.clearSelection();
					} catch (Exception ex) {
					}
				}
				currentTable = catalogTable;
				String sourceId = (String) catalogTable.getValueAt(catalogTable.getSelectedRow(), 1);
				CatalogEntry selected = catalogEntries.stream().filter(entry -> {
					return entry.getSourceId().equals(sourceId);
				}).findFirst().get();
				if (selected != null) {
					activateSelectedCatalogOverlay(imageViewerTab, selected);
					tabbedPane.setSelectedIndex(0);
				}
			}
		});

		resizeColumnWidth(catalogTable);

		JScrollPane catalogScrollPanel = new JScrollPane(catalogTable);
		catalogScrollPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(catalogEntry.getCatalogColor(), 3),
				catalogEntry.getCatalogName() + " results", TitledBorder.LEFT, TitledBorder.TOP));
		bottomPanel.addTab(catalogEntry.getCatalogName(), catalogScrollPanel);
	}

	private void activateSelectedCatalogOverlay(ImageViewerTab imageViewerTab, CatalogEntry selected) {
		switch (selected.getCatalogName()) {
			case SimbadCatalogEntry.CATALOG_NAME -> imageViewerTab.getSimbadOverlay().setSelected(true);
			case AllWiseCatalogEntry.CATALOG_NAME -> imageViewerTab.getAllWiseOverlay().setSelected(true);
			case CatWiseCatalogEntry.CATALOG_NAME -> imageViewerTab.getCatWiseOverlay().setSelected(true);
			case UnWiseCatalogEntry.CATALOG_NAME -> imageViewerTab.getUnWiseOverlay().setSelected(true);
			case GaiaDR2CatalogEntry.CATALOG_NAME -> imageViewerTab.getGaiaOverlay().setSelected(true);
			case GaiaDR3CatalogEntry.CATALOG_NAME -> imageViewerTab.getGaiaDR3Overlay().setSelected(true);
			case NoirlabCatalogEntry.CATALOG_NAME -> imageViewerTab.getNoirlabOverlay().setSelected(true);
			case PanStarrsCatalogEntry.CATALOG_NAME -> imageViewerTab.getPanStarrsOverlay().setSelected(true);
			case SdssCatalogEntry.CATALOG_NAME -> imageViewerTab.getSdssOverlay().setSelected(true);
			case VhsCatalogEntry.CATALOG_NAME -> imageViewerTab.getVhsOverlay().setSelected(true);
			case UhsCatalogEntry.CATALOG_NAME -> imageViewerTab.getUhsOverlay().setSelected(true);
			case UkidssCatalogEntry.CATALOG_NAME -> imageViewerTab.getUkidssOverlay().setSelected(true);
			case TwoMassCatalogEntry.CATALOG_NAME -> imageViewerTab.getTwoMassOverlay().setSelected(true);
			case TessCatalogEntry.CATALOG_NAME -> imageViewerTab.getTessOverlay().setSelected(true);
			case DesCatalogEntry.CATALOG_NAME -> imageViewerTab.getDesOverlay().setSelected(true);
			case GaiaWDCatalogEntry.CATALOG_NAME -> imageViewerTab.getGaiaWDOverlay().setSelected(true);
			case MocaCatalogEntry.CATALOG_NAME -> imageViewerTab.getMocaOverlay().setSelected(true);
		}
	}

	private void deselectedCatalogOverlay(ImageViewerTab imageViewerTab) {
		imageViewerTab.getSimbadOverlay().setSelected(false);
		imageViewerTab.getGaiaOverlay().setSelected(false);
		imageViewerTab.getGaiaDR3Overlay().setSelected(false);
		imageViewerTab.getAllWiseOverlay().setSelected(false);
		imageViewerTab.getCatWiseOverlay().setSelected(false);
		imageViewerTab.getUnWiseOverlay().setSelected(false);
		imageViewerTab.getPanStarrsOverlay().setSelected(false);
		imageViewerTab.getSdssOverlay().setSelected(false);
		imageViewerTab.getTwoMassOverlay().setSelected(false);
		imageViewerTab.getVhsOverlay().setSelected(false);
		imageViewerTab.getGaiaWDOverlay().setSelected(false);
		imageViewerTab.getNoirlabOverlay().setSelected(false);
		imageViewerTab.getTessOverlay().setSelected(false);
		imageViewerTab.getDesOverlay().setSelected(false);
	}

	public void setLegacyImages(boolean legacyImages) {
		this.legacyImages = legacyImages;
	}

	public void setPanstarrsImages(boolean panstarrsImages) {
		this.panstarrsImages = panstarrsImages;
	}

	public void setVhsImages(boolean vhsImages) {
		this.vhsImages = vhsImages;
	}

	public void setUhsImages(boolean uhsImages) {
		this.uhsImages = uhsImages;
	}

	public void setUkidssImages(boolean ukidssImages) {
		this.ukidssImages = ukidssImages;
	}

	public void setSdssImages(boolean sdssImages) {
		this.sdssImages = sdssImages;
	}

	public void setDssImages(boolean dssImages) {
		this.dssImages = dssImages;
	}

	public void setTwoMassImages(boolean twoMassImages) {
		this.twoMassImages = twoMassImages;
	}

	public void setSpitzerImages(boolean spitzerImages) {
		this.spitzerImages = spitzerImages;
	}

	public void setWiseImages(boolean wiseImagesEnabled) {
		this.wiseImagesEnabled = wiseImagesEnabled;
	}

	public void setWiseTimeSeries(boolean wiseTimeSeries) {
		this.wiseTimeSeries = wiseTimeSeries;
	}

}
