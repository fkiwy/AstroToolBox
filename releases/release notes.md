## Release notes

### AstroToolBox v3.4.0
- Removed K dwarfs from MLT spectral type estimates
- Fixed a bug in the photometric distance calculator
- Blocked fields in the object details panel
- Fixed a bug in overlays deserialization

### AstroToolBox v3.3.0
- Added MOCA DB summary table
- Added MOCA overlay
- Implemented a cone search for custom overlays
- Added more colors (J-H, H-K, etc.) to the spectral type estimate features
- Added UHS DR2 catalog
- Added UHS DR2 catalog overlay
- Added UHS DR2 images

### AstroToolBox v3.2.0
- Added a feature to the settings allowing to rearrange tabs
- Upgraded to SDSS DR17
- Changed DESI filters from grz to griz

### AstroToolBox v3.1.0
- Improved the image downloading process
- Removed "NEOWISE years" slider
- Added "Images per blink" slider
- Added "Skip poor quality images" checkbox
- Changed contrast settings
- Added LS DR10 images
- Added PS1 DR2 warp cutout blinks
- Added WISE light curves

### AstroToolBox v3.0.0
- Moved photometric distance estimates and extinction corrections to the Object details panel of the Image Viewer and Catalog Search
- Removed Brown Dwarf and White Dwarf tabs from AstroToolBox
- Replaced the "Pixel range" slider in the Image Viewer by the "Brightness" slider
- Added WISE CCD (Color-Color Diagram: J-W2 vs. W1-W2) accessible via Object details panel (button "WISE CCD")
- Fixed a bug in the Photometric Classifier
- Enabled NEOWISE 8 cutouts in the Image Viewer
- Added job id list per TAP provider
- Added "Remove job ids" button to the ADQL Query tab
- Added "Stop images download process" button to the Image Viewer
- Added the "Best match" checkbox to the SED fitting tool
- Added the "Use common zero points & wavelengths per band" checkbox to the SED fitting tool
- Removed the "Altogether" results from the Photometric Classifier

### AstroToolBox v2.5.1
- Fixed a bug in the deserialization of the saved proper motion overlays

### AstroToolBox v2.5.0
- Added VISTA VHS DR6 and UKIDSS DR11 PLUS images
- Added UKIDSS DR11 catalog including LAS, GCS, GPS, DXS & UDS surveys
- Added photometric uncertainties to the spectral type estimation features (has to be enabled in the Settings tab)
- Removed the Epoch slider from the Image Viewer
- Added the following 3 checkboxes: "Skip intermediate epochs", "Separate scan directions", "Difference imaging"
- Replaced the Main sequence lookup table by the latest version (version 2022.04.16)
- Replaced DES DR1 by DR2
- Replaced IRSA by NOIRLab as a backup TAP provider in the Settings tab
- Added "TAP access URL" and "ADQL query" fields to the Custom Overlays tab
- Moved the External Sources panel from the Catalog Search tab to the Image Viewer tab
- Replaced Gaia EDR3 by DR3

### AstroToolBox v2.4.1
- Fixed a bug in the DECaLS images download
- Implemented a HttpGet to fetch the results from an async ADQL query
- Fixed a bug in the IRSA proper motion query of the CatWISE2020 catalog

### AstroToolBox v2.4.0
- Changed the NSC DR2 catalog base URL from http://datalab.noao.edu to https://datalab.noirlab.edu
- Converted cutout service checkboxes to radio buttons
- Removed a bunch of obsolete features from the Image Viewer
- Changed the way unWISE and DECaLS images are downloaded and processed
- Added errors for Teff, log g and mass to the Gaia eDR3 WD catalog
- Upgraded from Gaia DR2 WD to Gaia eDR3 WD catalog
- Changed contrast settings and sliders
- Added a version info panel that will be displayed at startup if there's a new ATB version available
- Added a reference Gaia CMD G vs.BP-RP image to the Gaia CMD panel
- Reverted Pan-STARRS catalog access back to MAST

### AstroToolBox v2.3.7
- Added NEO7 unWISE coadds to the Image Viewer
- Added the Montreal cooling sequences for white dwarfs to the Gaia CMD
- Added a button to create a PDF of the SED and CMD charts
- Added white dwarf SED panel
- Added a bouton to the SED panel to show the chart data points
- Added CatWISE, unWISE and DES DR1 photometry to the SED feature
- Added DES DR1 catalog
- Added spectral types M0-M9 to the CMD panel

### AstroToolBox v2.3.6
- Fixed a bug in the Coordinates converter tool (decimal to sexagesimal conversion)
- Added colors for M0-M5 spectral types to the Brown dwarf lookup table
- SEDs can now be overplotted with reference SEDs
- Added WISE epoch #8 (NEO7) to the Image Viewer
- Added the RUWE to the Gaia eDR3 catalog
- Added Gaia CMD to the Image Viewer

### AstroToolBox v2.3.5
- Removed a trailing space character from the VizieR CatWISE source id
- The latest async TAP provider is saved to the settings file
- Added DSS image to the Image Viewer tab
- Added TESS Input Catalog to the Catalog Search and Image Viewer tabs
- Refactored photometry, colors and spectral type lookup
- Added the Photometric Classifier tab
- Added the Image Series tab
- Added SED plots to the object details of the Image Viewer tab
- Added the VizieR catalogs tab
- Fixed a NullPointerException in the Image Viewer
- Added measurements and references to the SIMBAD object details

### AstroToolBox v2.3.4
- Added 2 new themes to the Look and Feel select box in the Settings tab
- Proper motion vectors can be transformed to moving dots (Wiseview-style Gaia overlay)
- Added a button to the Object details panel of the Image Viewer and the Spectral type lookup panel of the Catalog Search tab to fill out the TYGO form automatically
- Added 2 fields to the Settings tab to save your name and email address for the automatic TYGO form filling
- Added VizieR and NOAO as TAP providers to the ADQL Query tab

### AstroToolBox v2.3.3
- Removed any unwanted characters from the user input coordinates
- Added 2 radio buttons to switch between IRSA and VizieR TAP for AllWISE, CatWISE, 2MASS & Gaia DR2 catalogs
- Added a button to the details panel of the Image Viewer to copy a digest of the object info to the clipboard

### AstroToolBox v2.3.2
- Added DECaLS images to the time series and object info sheet
- Added DECaLS regular and magnified images to the Image Viewer
- Added a link to the regular DECaLS image of the Image Viewer pointing to the Legacy Viewer
- Added a checkbox to the Settings Tab to enable/disable the download of DECaLS images
- Removed the Spitzer/WISE photometric catalog
- Changed the name of the ADQL Query tab to IRSA TAP
- Added further constraints to the NSC DR2 proper motion query
- Switched back to IRSA TAP for AllWISE, 2MASS and Gaia DR2

### AstroToolBox v2.3.1
- Switched back to IRSA TAB for CatWISE2020 (VizieR seems to have revoked TAP access for CatWISE2020)

### AstroToolBox v2.3.0
- Assigned shortcut keys to overlays
- Added DECaLS cutouts to the Image Viewer
- Added AB to Vega transformation for NOIRLab, Pan-STARRS and SDSS colors
- Added a split pane to the Image Viewer to change the size of the control panel
- Selected catalog overlays can now be saved (Overlays tab of Image Viewer)
- Overlays detail panel: Added a button to copy coordinates to the clipboard
- Added a marking tool for target coordinates
- Added a setting to save the total proper motion for the PM vectors
- Added a setting to save the FoV when right clicking on an object
- Reorganized the control panel of the Image Viewer into 5 tabs: Controls, Overlays, Mouse, Player, Tools
- Changed the layout of the navigation buttons
- Changed the order of the catalogs and overlays
- Added a colored border around subtracted contrast label
- Changed range of subtracted contrast slider from 1-10 to 1-19 with default is 10
- Added unwise.me cutouts to the Image Viewer
- Added S/N ratio to AllWISE w1,w2,w3,w4 and CatWISE w1,w2
- Added NOIRLab Source Catalog DR2
- Added NOIRLab DR2 overlays and PM vectors
- Added NOIRLab DR2 to the motion checker tool
- Refactored astrometry for PM vectors, the motion tool and the transpose motion feature
- Refactored astrometry for navigation buttons
- Refactored astrometry for custom overlays local file search
- Implemented a new formula to calculate angular distance between 2 stars

### AstroToolBox v2.2.2
- Refactored external resources panel of the Catalog Search tab
- Refactored custom overlays
- Refactored motion checker tool
- Changed the overlay shape size
- Replaced CatWISE2020 by CatWISE Preliminary (CatWISE2020 definitely contains too much junk!)
- Added Gaia EDR3 catalog, overlays and PM vectors. Will only be activated the day after the official release (12/3/2020)

### AstroToolBox v2.2.1
- Added a motion checker tool to the Image Viewer, allowing to check if the observed motion corresponds to the measured one
- Added a check box to the Settings tab, allowing to disable async download of WISE images (async download may cause problems on some Linux distributions)
- Added VizieR catalog support to custom overlays, allowing to produce overlays of any TAP enabled VizieR catalog
- Refactored pixel to WCS and WCS to pixel mapping so that the overlays match exactly the sources in the WISE images
- Added a text field for changing the cutout service URL to the Settings tab
- Migrated AllWISE, 2MASS and Gaia DR2 catalogs from IRSA TAP to VizieR TAP

### AstroToolBox v2.2.0
- Image Viewer tab: Made the border around the 1st epoch thicker and red
- Image Viewer tab: Changed the display order of the IRSA images: DSS, 2MASS, SDSS, AllWISE & PanSTARRS
- Image Viewer tab: Added a checkbox to display a static time series panel (DSS2-IR, 2MASS-K, SDSS-z, WISE-W2, PanSTARRS-z)
- Image Viewer tab: Added a checkbox to display an animated time series panel (DSS2-IR, 2MASS-K, SDSS-z, WISE-W2, PanSTARRS-z), specific filters can be omitted
- Catalog & Image Viewer tab: Added a name resolver for astronomical objects to the coordinates input field
- Image Viewer tab: Added a checkbox to show crosshairs with coordinates on WISE images
- Calculators & Converters tab: Added a linear distance calculator (to calculate the real distance between 2 stars)
- Image Viewer tab: Added 2 experimental features that work only with catalog overlays: a checkbox to display estimated spectral types and a checkbox to show potential brown dwarfs only
- Fixed a bug (reported by an attentive user) in the Pan-STARRS catalog: z-y showed the same value than i-z in the catalog details panel
- Optimized the Image Viewer, which now requires less CPU and memory

### AstroToolBox v2.1.0
- Added a photometric (luminosity) distance evaluation feature to the M-L-T-Y Dwarfs tab
- Added a check box called "Skip bad quality images" to the Image Viewer tab
- Added a check box called "Skip single nodes" to the Image Viewer tab
- Added a photometric distance calculator to the Calculators & Converters Tab
- Added a new epoch called ASCENDING_DESCENDING_NOISE_REDUCED to the Image Viewer tab
- Added a service to get dust extinctions by band
- Added a check box called "Consider Galactic dust reddening & extinction" the M-L-T-Y Dwarfs tab
- Reworked photometry of Y dwarfs in the MLTY Dwarfs lookup table
- Added a button to the catalog entry panel of the Image Viewer tab to copy relevant information to the clipboard
- Replaced CatWISE Preliminary by CatWISE2020 catalog
- Added 2 new epochs to the Image Viewer tab: FIRST_LAST_ASCENDING & FIRST_LAST_DESCENDING
- Removed useless sliders "Min pixel value", "Max pixel value" and "Stretch control" from the Image Viewer tab
- Added a check box called "Optimize contrast" to the Image Viewer tab

### AstroToolBox v2.0
- Added 2MASS All-Sky images to the Image Viewer tab
- Added stack images for Pan-STARRS g, r, i, z and y filters
- Preloading and coadding all available epochs (Image Viewer tab)
- Added DSS and SDSS images to the Image Viewer tab
- Optimized image downloading and processing
- Added IRSA Finder Chart images
- Added AllWISE flags as tool tips to the catalog entries
- Added number of epochs field to the Settings tab
- Finally solved the image cut off issue
- Results of Simbad catalog queries are now returned in CSV format
- Added VISTA-VHS catalog to the Catalog and Image Viewer tabs
- Added a PDF data sheet to the Image Viewer tab
- Refactored spectral type lookup features
- Changed number of default epochs from 6 to 7 in the Image Viewer tab
- Added VHS DR4 link to external resources of the Catalog Search tab
- Added NeoWISE images to the PDF data sheet
- Added 2MASS catalog to the Catalog and Image Viewer tabs
- Reworked min/max settings
- Always skipping single nodes no matter which epoch mode
- Added a teff, mass, log g and age lookup feature for white dwarfs
- Added Gaia DR2 WD candidates catalog to the Catalog Search and Image Viewer tabs
- Added the unWISE catalog to the Catalog Search and Image Viewer tabs
- Added Spitzer/WISE photometric catalog to the Image Viewer tab
- Added Spitzer colors to the brown dwarfs lookup table
- Added a live download log to the Image Viewer tab

### AstroToolBox v2.0.1
- Fixed some bugs in the Image Viewer tab
- Changed min/max pixel settings in the Image Viewer tab

### AstroToolBox v1.3.5
- All image overlays are now loaded asynchronously (Image Viewer tab)
- AllWISE W1, W2, W3, W4 and Pan-STARRS images are now displayed asynchronously (Image Viewer tab)
- Added 2MASS All-Sky images (J, H, K) to the Image Viewer tab
- Added stack images for Pan-STARRS g, r, i, z and y filters to the Image Viewer tab
- Added "Display all images" radio button to the Image Viewer tab
- Made Gaia DR2 and CatWISE PM vectors bolder (Image Viewer tab)
- Replaced warning dialogs by message labels (Image Viewer tab)
- If provided, additional coadds are loaded to improve the image quality (Image Viewer tab)
- Added 3 new settings to the Settings tab: "Download additional coadds", "Download Pan-STARRS images" and "Download SDSS images" check boxes

### AstroToolBox v1.3.4
- Optimized Gaia DR2 and CatWISE proper motion overlays for the galactic plane

### AstroToolBox v1.3.3
- Fixed a bug in the AllWISE w1/w2/w3/w4 images display

### AstroToolBox v1.3.2
- Made the parsing of catalog fields resilient against a change of their order in the result sets.

### AstroToolBox v1.3.1
- Fixed a bug in the Image Viewer tab

### AstroToolBox v1.3.0
- Added crosshairs to the Image Viewer tab
- Updated the Help section
- Added "Smooth images" check box to the Image Viewer tab (may produce better results in subtracted modes)
- Added "Keep contrast settings" check box to the Image Viewer tab (keeps the contrast settings between coordinates and/or FoV changes)
- Reworked catalog field labels
- Added SDSS images to the Image Viewer tab
- Added "Mark differences" check box to the Image Viewer tab (marks the differences between successively displayed images)
- Added a new Epoch called ASCENDING_DESCENDING_SUBTRACTED to the Image Viewer tab

### AstroToolBox v1.2.3
- Added Pan-STARRS catalog to the Catalog Search tab
- Added Pan-STARRS overlay to the Image Viewer tab
- Added SDSS catalog to the Catalog Search tab
- Added SDSS overlay to the Image Viewer tab
- Added SDSS spectra overlay to the Image Viewer tab
- Added navigation buttons to the Image Viewer tab allowing you to move thru the sky (left, right, up, down)

### AstroToolBox v1.2.2
- Added WISE artifact overlays for ghosts, halos, latents and diffraction spikes to the Image Viewer tab
- Added a diamond shape to the overlay shapes
- Added a new mode to the Epoch's select box in the Image Viewer tab: ASCENDING_DESCENDING (comparable to Wiseview's "Separate scan directions" check box)
- Added "Skip first epoch (year 2010)" check box to the Image Viewer tab (if checked, the original WISE epoch of 2010 is replaced by the first NEO epoch of 2014, which may improve the subtracted image modes, especially for high populated, large field of views)
- Added AGN and WD warnings for AllWise and Gaia DR2 entries to the Catalog Search tab
- Added Solar System Objects overlay to the Image Viewer tab
- Added WISE Atlas images to the Image Viewer tab
- Resolved the "Image cut off" issue in the Image Viewer tab

### AstroToolBox v1.2.1
- Files are now automatically saved when a row is deleted or when the program is closed (File Browser and Object Collection)
- Added a link to the Legacy Sky Viewer in the Catalog Search tab
- Added links to the Nearest Zooniverse Subjects in the Image Viewer tab
- Redesigned PM vectors in the Image Viewer tab

### AstroToolBox v1.2.0
- Added a modified Julian date converter to the Calculators & Converters tab
- Added check box "Small body help" to the Image Viewer tab. It's a help to enter the data required by JPL's Small Body Identification tool.
- Added image player controls (Play, Stop, Backward, Forward) to the Image Viewer tab
- Added button "Rotate by 90° clockwise" to the Image Viewer tab
- Added buttons "Save as PNG" and "Save as GIF" to the Image Viewer tab. The first button saves the current image as a PNG file. The second button saves all the images as an animated GIF.
- Added a magnifier feature to the Image Viewer tab for WISE and PanSTARRS images
- Added check box "Hide magnifier panel" to the Image Viewer tab
- Added check box "Draw circle" to the Image Viewer tab. This is an object marking tool.
- Added check box "Work with custom overlays" to the Image Viewer tab
- Added Custom Overlays tab
- Added button "Add to object collection" to the details panel of the Image Viewer tab
- Added Object Collection tab
- Added "Add row", "Remove selected row" and "Open new File Browser" buttons to the File Browser tab
- Added check box "Transpose proper motion" to the Image Viewer tab (AstroToolBox's implementation of Wiseview's Shift & Add)
- Added radio buttons "Show catalogs" and "Recenter images"  to the Image Viewer tab
- PM vector overlays are now clickable to get the PM object's details
- Every data value, anywhere in the program, can now be copied and pasted.
- Fixed a bug in the decimal to sexagesimal coordinates converter
- Fixed a bug in the drawing of PM vectors
