package astro.tool.box.module.tab;

import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.module.HtmlText;
import java.awt.FlowLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class HelpTab {

    private static final String TAB_NAME = "Help";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    public HelpTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
    }

    public void init() {
        try {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBorder(new EmptyBorder(5, 5, 5, 5));

            HtmlText text = new HtmlText();
            text.text("The tool set should be mostly straightforward. If not, let me know via this email address: ");
            text.boldText(HELP_EMAIL).newLine();
            text.row("Use this email also for any kind of questions (related to the tool), suggestions and bug reports.");
            text.newLine();
            text.boldRow("Remarks:");
            text.row("AstroToolBox needs an internet connection for the following features: Catalog Search, Image Viewer, ADQL Query and Batch Query.");
            text.row("If you're behind a company firewall and need to connect via a proxy server, go to the " + SettingsTab.TAB_NAME + " tab and enter your proxy's host name an port. Check the 'Use proxy' check box.");
            text.row("There's also a SIMBAD mirror you can use in case the original site is under maintenance. To do so, check the 'Use SIMBAD mirror' check box in the " + SettingsTab.TAB_NAME + " tab.");
            text.newLine();
            text.boldRow("About the " + CatalogQueryTab.TAB_NAME + " tab:");
            text.row("To perform a catalog search, enter the objet’s coordinates, change the search radius if needed and hit the Enter key or press the Search button.");
            text.row("If you deselect one or more catalogs, the object lookup is performed only in the remaining catalog(s). Coordinates can be specified in sexagesimal or decimal format.");
            text.row("If you click on an object's row (catalog entry) in one of the result tables, its coordinates are copied to the clipboard in case you need them anywhere else.");
            text.row("You can change this behaviour in the " + SettingsTab.TAB_NAME + " tab by deselecting the 'Copy coordinates to clipboard' check box and saving the settings.");
            text.row("Further, if you've clicked on a catalog entry, the object's details are displayed in the bottom center panel.");
            text.row("On the bottom left panel, there are some links to external resources, like AladinLite, VizieR, SIMBAD, etc.");
            text.row("If you click on one of those links, your browser opens the corresponding page with your object either centered in case of an image viewer, or listed in the first position");
            text.row("in case of a catalog search.");
            text.row("On the bottom right panel, you have a spectral type evaluation using Eric Mamajek's table: A Modern Mean Dwarf Stellar Color & Effective Temperature Sequence");
            text.newLine();
            text.boldRow("About the " + BrownDwarfTab.TAB_NAME + " tab:");
            text.row("If you have selected an object in the " + CatalogQueryTab.TAB_NAME + " tab and switch to the " + BrownDwarfTab.TAB_NAME + " tab, a spectral type evaluation of the selected object is performed");
            text.row("automatically. Alternatively, you may enter the magnitudes yourself, in case you've got an object not listed in any of the featured catalogs.");
            text.row("The lookup table used is available in the " + LookupTab.TAB_NAME + " tab under MLTY_DWARFS.");
            text.newLine();
            text.boldRow("About the " + WhiteDwarfTab.TAB_NAME + " tab:");
            text.row("If you have selected an object in the " + CatalogQueryTab.TAB_NAME + " tab and switch to the " + WhiteDwarfTab.TAB_NAME + " tab, an effective temperature evaluation of the selected object is performed");
            text.row("automatically.  Alternatively, you may enter the magnitudes yourself, in case you've got an object not listed in any of the featured catalogs.");
            text.row("The lookup tables used are available in the " + LookupTab.TAB_NAME + "tab under WHITE_DWARFS_PURE_H, WHITE_DWARFS_PURE_HE and WHITE_DWARFS_MIX.");
            text.newLine();
            text.boldRow("About the " + ImageViewerTab.TAB_NAME + " tab:");
            text.row("The Image Viewer uses data from unwise.me (http://unwise.me) made available through WiseView's cutout service (http://byw.tools/wiseview).");
            text.row("The Pan-STARRS images are downloaded from MAST PS1 Science Archive (https://panstarrs.stsci.edu).");
            text.row("The SDSS images are pulled from the SLOAN Digital Sky Survey SkyServer (http://skyserver.sdss.org).");
            text.newLine();
            text.row("The fasted way to load some images is to select only one band (W1 or W2) and the FIRST_LAST epoch, which is the default. This will load only four images.");
            text.row("The download time will be significantly increased, if you select W1W2 and one of the following epochs: ALL, ASCENDING_DESCENDING, ASCENDING_DESCENDING_SUBTRACTED,");
            text.row("ASCENDING_DESCENDING_PARALLAX, YEAR, YEAR_SUBTRACTED, FIRST_REMAINING and FIRST_REMAINING_SUBTRACTED. This will load all 24 images currently available.");
            text.row("But once the images are loaded, you can switch between bands and epochs instantly (without having to reload any data).");
            text.newLine();
            text.row("For most cutouts, you have to adjust the contrast by pulling one of the contrast sliders either to the left or to the right.");
            text.row("I recommend starting with the low scale slider and if that's not enough, use can still use the high scale slider.");
            text.newLine();
            text.row("There are four subtracted modes you can select from the 'Epochs' selection box: ASCENDING_DESCENDING_SUBTRACTED, YEAR_SUBTRACTED, FIRST_REMAINING_SUBTRACTED");
            text.row("and FIRST_LAST_SUBTRACTED. If the images are too dark, pull the 'Min pixel value' slider carefully to the right until you see the difference image.");
            text.row("If the images are too bright, pull the 'Max pixel value' slider to the left.");
            text.newLine();
            text.row("If you left-click an object of your choice, a new Catalog Search instance will be opened, showing all catalog entries within a search radius of 10 arcsec.");
            text.row("If you select the 'Recenter images' radio button and left-click an object, the WISE images will be recentered to the coordinates of the clicked object.");
            text.newLine();
            text.row("If you right-click an object, a new Image Viewer instance will be opened, showing your object centered in a smaller field of view (100 arcsec).");
            text.newLine();
            text.row("If you middle-click (mouse wheel) an object, a new window with a PanSTARRS image will be opened, containing your object in a field of view of 15 arcsec.");
            text.row("If you select the 'AllWISE w1/w2/w3/w4 images' radio button and middle-click an object, a new window with AllWISE Atlas images (one for each band) will be opened,");
            text.row("containing your object in a field of view of 30 arcsec. The field of view can be changed by spinning the mouse wheel forward (larger) or backward (smaller),");
            text.row("before clicking on the object of interest. While spinning the wheel, your mouse pointer has to be positioned over the WISE images currently being displayed.");
            text.newLine();
            text.boldRow("About the " + AdqlQueryTab.TAB_NAME + " tab:");
            text.row("You can import existing queries, edit and save them, or create new ones and save them to any location on your file system.");
            text.row("The available tables can be browsed using the 'Browse IRSA tables' button. If you click on a table entry, the table's columns will be displayed on the right.");
            text.row("The query is run asynchronously on the IRSA servers. The execution status of your query is shown in the 'Status' field which is updated automatically every 10 seconds.");
            text.row("When the query has finished, you can fetch the results via the 'Fetch results' button. Once all the results are displayed, you may export them as a .csv file.");
            text.newLine();
            text.boldRow("About the " + BatchQueryTab.TAB_NAME + " tab:");
            text.row("Import your .csv file which has to contain a header row. Then specify the corresponding RA and dec positions and choose the catalog(s) you want to query.");
            text.row("Choose a lookup table and indicate if you want to include the matched colors in the result table. Hit the 'Start Query' button.");
            text.row("The query is performed asynchronously on your computer, making for each row of your .csv file a call to the selected catalogs hosted at IRSA.");
            text.row("For instance, if you have a .csv file containing 500 objects (500 rows) and you select the 4 featured catalogs, then 2000 (500 * 4) individual HTTP connections need");
            text.row("to be established. It's obvious that this process requires some time, also depending on the speed of your internet connection.");
            text.newLine();
            text.boldRow("About the " + FileBrowserTab.TAB_NAME + " tab:");
            text.row("You can import any kind of .csv file for viewing and editing. You can also add new columns to the right and save the edited file. Simply enter the new column names");
            text.row("seperated by a comma in the 'Columns to add' field and press the 'Reload file' button. If everything looks fine, press the 'Save file' button.");
            text.row("If your file contains astronomical objects, you can enter RA and dec column positions. This allows you, after having clicked on an object's row and switched to");
            text.row("the " + CatalogQueryTab.TAB_NAME + " tab, to query the featured catalogs for the selected object. The tool copies RA and dec into the 'Coordinates' field.");
            text.row("You only have to hit the Enter key or press the Search button. The same applies to the results of the " + BatchQueryTab.TAB_NAME + " tab.");
            text.newLine();
            text.boldRow("About the " + ObjectCollectionTab.TAB_NAME + " tab:");
            text.row("The Object Collection feature allows you to collect objects of interest and save them to a file of your choice.");
            text.row("In the Object Collection tab, you can see, edit and delete the objects you've collected so far. The collection table is fully sortable and searchable.");
            text.row("To sort the table, click on a column's header and to search the table, use the 'Search' field at the bottom.");
            text.row("To add an object to the collection simply click the 'Add to object collection' button at the bottom of the object details panel of the Image Viewer and");
            text.row("the Catalog Search tabs. You can choose the object's type before clicking the Add button.");
            text.row("But you may also manually add objects to your Collection by clicking the 'Add row' button in the Object Collection tab.");

            JEditorPane editor = new JEditorPane("text/html", text.toString());
            editor.setBorder(new EmptyBorder(10, 10, 10, 10));
            editor.setEditable(false);
            panel.add(editor);

            tabbedPane.addTab(TAB_NAME, new JScrollPane(panel));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

}
