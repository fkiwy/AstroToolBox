# AstroToolBox

AstroToolBox is a Java toolset for visualizing, identifying and classifying astronomical objects with a focus on low-mass stars and ultra-cool dwarfs.

It contains a catalog search for SIMBAD (measurements & references), AllWISE, CatWISE, unWISE, Gaia, NOIRLab Source Catalog (NSC), Pan-STARRS, SDSS, TESS Input Catalog, VISTA VHS, UKIDSS and 2MASS catalogs, plus a spectral type evaluation feature for main sequence stars including brown dwarfs. The spectral type evaluation is performed by applying the relations described in Pecaut & Mamajek (2013), Best et al. (2018), Carnero Rosell et al. (2019), Skrzypek et al. (2015), Skrzypek et al. (2016) and Kiman et al. (2019).

There's an SED fitting tool for ultra-cool and white dwarfs using Best et al. SEDs for Field Ultracool Dwarfs and the Montreal Cooling Sequences (Bergeron et al.) for white dwarfs, respectively.

A feature that enables the drawing of Gaia color-magnitude diagrams (CMD) with overplotted M0-M9 spectral types is also included. The Montreal Cooling Sequences can be drawn on the white dwarf branch of the Gaia CMD.

You can create WISE light curves from AllWISE Multiepoch and NEOWISE-R Single Exposure (L1b) photometry. The SED, CMD and light curve plots can be saved as PNG or PDF files.

Furthermore, the tool has an image viewer that blinks images from different epochs, using WISE coadds (Meisner et al. 2017), DECaLS cutouts (Dey et al. 2019) and Pan-STARRS WARP images (Waters et al. 2020), in order to visually identify the motion or variability of objects. These images can be saved as PNG or animated GIF files.

Overlays of all featured catalogs can be added as needed. Custom overlays can be created from VizieR catalogs or local files.

The tool displays time series (static or animated) using infrared and optical images of various surveys (DSS, SDSS, 2MASS, AllWISE, DECaLS).

It contains a photometric classifier that uses the photometry of the above mentioned catalogs to create a detailed spectral type classification.

The toolset also includes an ADQL query interface (IRSA, VizieR, NOAO) and a batch spectral type lookup feature that uses a CSV file with object coordinates as input.

In addition, the tool has a file browser linked to the image viewer, which makes it possible to check a large list of objects in a convenient way. Interesting finds can be saved in an object collection for later use. The tool also offers a number of handy astrometric calculators and converters.

AstroToolBox can be cited via its ASCL.net entry <a href="https://ascl.net/2201.002"><img src="https://img.shields.io/badge/ascl-2201.002-blue.svg?colorB=262255" alt="ascl:2201.002" /></a>

[Download latest version](releases/executables/AstroToolBox-3.4.0.jar)

[Release notes](releases/release%20notes.md)

## Screenshots

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

### Calculators & Converters
![Calculators & Converters](images/Calculators%20&%20Converters.png)

### Photometric Relations
![Photometric Relations](images/Photometric%20Relations.png)

### SIMBAD Measurements & References
![SIMBAD Measurements & References](images/SIMBAD%20measurements%20&%20references.png)

### Object details
![Object details](images/Object%20details.png)

### SED fitting for ultra-cool dwarfs
![UC SED](images/SED.png)

### SED fitting for white dwarfs
![WD SED](images/WD%20SED.png)

### Gaia CMD with overplotted M0-M9 spectral types
![Gaia CMD](images/Gaia%20CMD%20RD.png)

### Gaia CMD with overplotted Montreal Cooling Sequences 
![Gaia CMD](images/Gaia%20CMD%20WD.png)

### Color-Color Diagram J-W2 vs. W1-W2
![WISE CCD](images/WISE%20CCD.png)

### WISE light curves (AllWISE Multiepoch & NEOWISE-R Single Exposure photometry)
![WISE LC](images/WISE%20light%20curves.png)
