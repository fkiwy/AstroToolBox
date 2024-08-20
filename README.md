# AstroToolBox

AstroToolBox is a comprehensive Java suite designed for the visualization, identification, and classification of celestial bodies, particularly focusing on low-mass stars and ultra-cool dwarfs.

This toolkit integrates various astronomical catalogs including SIMBAD (all available measurements & references), AllWISE, CatWISE, unWISE, Gaia, NOIRLab Source Catalog (NSC), Pan-STARRS, SDSS, TESS Input Catalog, UKIRT UHS & UKIDSS, VISTA VHS, and 2MASS. Additionally, it provides a spectral type evaluation tool for main sequence stars, including brown dwarfs, utilizing established relations from Pecaut & Mamajek (2013), Best et al. (2018), Carnero Rosell et al. (2019), Skrzypek et al. (2015), Skrzypek et al. (2016), and Kiman et al. (2019).

For ultra-cool and white dwarfs, AstroToolBox offers an SED fitting tool using Best et al. SEDs for Field Ultracool Dwarfs and the Montreal Cooling Sequences (Bergeron et al.) for white dwarfs. Moreover, it includes a functionality to generate Gaia color-magnitude diagrams (CMD) with overlaid M0-M9 spectral types, along with the capability to plot Montreal Cooling Sequences on the white dwarf branch.

Users can generate WISE light curves from AllWISE Multiepoch and NEOWISE-R Single Exposure (L1b) photometry and save plots as PNG or PDF files. Furthermore, the toolkit facilitates image blinking from different epochs using WISE coadds (Meisner et al. 2017), DECaLS cutouts (Dey et al. 2019), and Pan-STARRS WARP images (Waters et al. 2020) to identify motion or variability of objects, with the option to save images as PNG or animated GIF files.

AstroToolBox allows overlaying of featured catalogs and creation of custom overlays from VizieR catalogs, ADQL queries or local files. Overlay shapes can be clicked to show catalog details. Another feature is the display of time series (static or animated) using infrared and optical images from various surveys (DSS, SDSS, 2MASS, WISE, Spitzer, UKIDSS, UHS, VHS, Pan-STARRS, DECaLS).

The toolkit incorporates a photometric classifier using data from the aforementioned catalogs to provide detailed spectral type classification. It also has an ADQL query interface for searching the ESAC, IRSA, NOIRLab and VizieR data archives.

Additionally, AstroToolBox offers a file browser linked to the image viewer for convenient checking of a large list of objects, allowing users to save interesting finds in an object collection for future reference. It also provides various astrometric calculators and converters for user convenience.

AstroToolBox can be cited via its ASCL.net entry <a href="https://ascl.net/2201.002"><img src="https://img.shields.io/badge/ascl-2201.002-blue.svg?colorB=262255" alt="ascl:2201.002" /></a>

[Download latest version](releases/executables/AstroToolBox-4.1.0.jar)

[Release notes](releases/release%20notes.md)

## Screenshots

[Catalog search](#catalog-search)  
[Image Viewer](#image-viewer)  
[Image Series](#image-series)  
[Photometric Classifier](#photometric-classifier)  
[VizieR Catalogs](#vizier-catalogs)  
[ADQL Query](#adql-query)  
[Batch Query](#batch-query)  
[File Browser](#file-browser)  
[Custom Overlays](#custom-overlays)  
[Calculators and Converters](#calculators-and-converters)  
[Photometric Relations](#photometric-relations)  
[SIMBAD Measurements and References](#simbad-measurements-and-references)  
[Object details](#object-details)  
[SED fitting](#sed-fitting)  
[Gaia CMD](#gaia-cmd)  
[Color-Color Diagram](#color-color-diagram)  
[WISE light curves](#wise-light-curves)  

### Catalog search
![Catalog Search](images/Catalog%20Search.png)

### Image Viewer
![Image Viewer](images/Image%20Viewer.png)

#### WISE image blinks plus AllWISE overlays (green circles)
![WISEA J085510.74-071442.5](images/WISEA%20J085510.74-071442.5.gif)

#### Pan-STARRS WARP image blinks
![Object details](images/PS1%20WARP%20image%20blinks.gif)

#### Same binary in DECaLS, PS1 and WISE imagery
![Binary DECaLS](images/Binary%20DECaLS.gif)
![Binary PS1](images/Binary%20PS1.gif)
![Binary WISE](images/Binary%20WISE.gif)

### Image Series
![Image Series](images/Image%20Series.png)

### Photometric Classifier
![Photometric Classifier](images/Photometric%20Classifier.png)

### VizieR Catalogs
![VizieR Catalogs](images/VizieR%20Catalogs.png)

### ADQL Query
![ADQL Query](images/ADQL%20Query.png)

### Batch Query
![Batch Query](images/Batch%20Query.png)

### File Browser
![File Browser](images/File%20Browser.png)

### Custom Overlays
![Custom Overlays](images/Custom%20Overlays.png)

### Calculators and Converters
![Calculators & Converters](images/Calculators%20&%20Converters.png)

### Photometric Relations
![Photometric Relations](images/Photometric%20Relations.png)

### SIMBAD Measurements and References
![SIMBAD Measurements & References](images/SIMBAD%20measurements%20&%20references.png)

### Object details
![Object details](images/Object%20details.png)

### SED fitting

#### Ultra-cool dwarfs
![UC SED](images/SED.png)

#### White dwarfs
![WD SED](images/WD%20SED.png)

### Gaia CMD

#### Overplotted M0-M9 spectral types
![Gaia CMD](images/Gaia%20CMD%20RD.png)

#### Overplotted Montreal Cooling Sequences 
![Gaia CMD](images/Gaia%20CMD%20WD.png)

### Color-Color Diagram

#### J-W2 vs. W1-W2
![WISE CCD](images/WISE%20CCD.png)

### WISE light curves

#### AllWISE Multiepoch & NEOWISE-R Single Exposure photometry
![WISE LC](images/WISE%20light%20curves.png)
