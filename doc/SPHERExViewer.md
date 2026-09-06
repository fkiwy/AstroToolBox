# SPHEREx Spectrum Viewer

## Overview

The **SPHEREx Spectrum** tab creates an aperture spectrum from public SPHEREx image cutouts at a sky position. It queries the IRSA SPHEREx service, downloads the cutouts needed for the requested field, performs aperture photometry, and plots flux density against wavelength.

The tab also creates a registered RGB composite and detector-stack diagnostic images. The RGB image is interactive: after the first extraction, click another position in the displayed field to measure a new spectrum from the already cached FITS cutouts. No new IRSA query or image download is needed for that within-field measurement.

An internet connection is required for initial downloads and for any calibration data that is not already cached.

## Quick start

1. Open the **SPHEREx Spectrum** tab.
2. Enter a target in **Coordinates**. For example: `150.116321 2.205830`.
3. Leave the default **Cutout (arcsec)** and **Aperture (pixels)** values initially, or adjust them for the target.
4. Choose a **FITS cutouts path**. The default is normally appropriate.
5. Click **Generate spectrum**.
6. Wait for the status line to report completion. The spectrum appears on the left; the RGB composite and detector diagnostics appear on the right.
7. Optionally click an object or feature in the RGB composite to extract another spectrum from the same field.
8. Use **Save CSV** or **Save PNG** to export the result.

## Input controls

| Control | Purpose | Notes |
| --- | --- | --- |
| **Coordinates** | Target position. | Enter decimal RA and Dec in degrees, separated by whitespace, a comma, or a semicolon. Sexagesimal coordinate input is also accepted. A resolvable object name can be used where name resolution is available. RA must be in the range 0 to less than 360 degrees; Dec must be between -90 and +90 degrees. |
| **Cutout (arcsec)** | Width and height of the downloaded sky cutout. | Valid values are 16 to 1800 arcsec. The default is 120 arcsec. A larger cutout is useful for a wider field, but can require more downloading, disk space, and processing time. |
| **Aperture (pixels)** | Radius of the circular photometry aperture in SPHEREx image pixels. | Must be positive. The default is 2.0 pixels. Increase it for an extended source only when the background and nearby-source contamination are acceptable. |
| **FITS cutouts path** | Directory in which image cutouts, settings, and related cached results are stored. | Use the editable path box to paste a location or choose **Browse...** to select a directory. The chooser selects directories, not individual FITS files. |
| **Bin spectrum** | Combines compatible individual measurements into spectral bins. | Enabled by default. Binning generally gives a clearer spectrum with fewer points; disable it to inspect individual measurements. |
| **Delete downloaded files** | Deletes the current directory's cached `cutouts` content before generating. | Select this when a completely fresh download is wanted. The check box clears itself after the cleanup attempt. |
| **Generate spectrum** | Starts the download/reuse, photometry, and display workflow. | Disabled while a generation or a cached click extraction is running. |
| **Save CSV** | Exports the current spectrum table. | Available after a successful extraction. |
| **Save PNG** | Exports the current spectrum chart as a PNG image. | Available after a successful extraction. |

## Coordinates

For decimal input, use RA first and Dec second:

```text
150.116321 2.205830
```

Comma- and semicolon-separated decimal positions are accepted as well:

```text
150.116321, 2.205830
```

Sexagesimal input can be written with spaces or the usual hour/degree, minute, and second markers:

```text
10 00 27.92 +02 12 20.99
10h00m27.92s +02d12m20.99s
```

The tab stores the successfully used coordinate values in decimal degrees. When a new spectrum is extracted by clicking the RGB image, the **Coordinates** field is updated to that clicked position.

## Choosing a cache directory

The default base directory is:

```text
<user-home>/.astro-tool-box/spherex
```

When that default base path is used, the tab creates and switches to a coordinate-specific subdirectory when generating a spectrum. This keeps the downloaded cutouts for different targets separate. The generated subdirectory name starts with `J` followed by formatted coordinates.

You may instead choose a custom directory. This is useful when:

- the default home directory has limited space;
- cutouts should be kept on a project or external drive;
- a previous cache should be deliberately reused.

When you select an existing directory that contains `spherex.properties`, the tab restores its saved coordinates, cutout size, and aperture radius into the corresponding fields. Those restored values are therefore the values used by **Generate spectrum** unless you edit them before generating. If you type a path rather than using **Browse...**, press Enter to load its settings.

The selected directory is created if necessary. It can contain:

```text
<selected directory>/
  cutouts/              Downloaded FITS cutouts
  spherex.properties    Saved coordinate, cutout-size, and aperture settings
```

The default SPHEREx base directory also holds calibration downloads used by the extraction. Do not manually modify FITS or calibration files while the tab is working.

### Cache reuse and refresh behavior

The tab compares the requested position and cutout size with the saved settings for the selected cache directory.

- A target outside the previous cutout field triggers a fresh IRSA query and download.
- Requesting a larger cutout triggers a fresh query and download.
- Requesting a position inside the existing field with the same or a smaller cutout reuses cached cutouts.
- Selecting **Delete downloaded files** removes cached cutouts before the next run, regardless of the comparison.

Changing to a different cache directory starts a separate cache history.

## What happens during generation

After **Generate spectrum** is clicked, the status line reports progress. Depending on cache state, the tab will:

1. Create the cache and calibration directories as needed.
2. Query IRSA for public SPHEREx cutouts covering the requested position, unless suitable cached cutouts are being reused.
3. Download FITS cutouts into the cache when required.
4. Read the FITS image and metadata, apply the aperture measurement, and associate the measurement with a detector and wavelength.
5. Discard invalid points and reject detector-wise spectral outliers using a robust median-absolute-deviation method.
6. Optionally bin the measurements into the output spectrum.
7. Plot the spectrum and stack the downloaded FITS images for the image display.

Individual unsuitable or unreadable cutouts may be skipped. A completed status message reports the number of output spectrum points and the number of skipped cutouts. Generation fails if no usable cutouts can be measured.

## Reading the spectrum plot

The left side of the tab shows the aperture spectrum.

- The horizontal axis is **wavelength in micrometres (um)**.
- The vertical axis is **flux density in microjanskys (uJy)**.
- Each black point is a measured or binned spectral point.
- Vertical error bars show the reported flux uncertainty.
- When enough data exist, a red spline is drawn as a robust broad-shape guide. It is a visual guide, not an additional measurement or a fitted physical model.

The plot has a fixed display size and is placed in a scroll pane; use the scroll bars if its viewport is smaller than the plot.

## Images and RGB interaction

After successful image stacking, the right side shows two products.

### Interactive RGB composite

The top image is a 500 by 500 pixel RGB composite made from the aligned detector stacks. Its channels are formed as follows:

| RGB channel | Detector stacks used |
| --- | --- |
| Blue | D1 and D2 |
| Green | D3 and D4 |
| Red | D5 and D6 |

The magenta crosshair marks the coordinate used for the currently displayed image field when it is first created. Clicking inside the RGB image moves the crosshair and requests a new spectrum at that sky position.

The click position must be inside the originally loaded cutout field. A valid click:

- uses the composite's celestial coordinate mapping to determine RA and Dec;
- keeps the existing cutout and calibration configuration;
- remeasures the cached FITS files without another IRSA query or image download;
- replaces the spectrum plot and updates the **Coordinates** field.

If the selected position lies outside the loaded field, the status line reports that it is outside the current field and no new extraction is performed. To inspect a more distant target, enter its coordinates and run **Generate spectrum**.

### Detector image stacks

Below the RGB composite is a diagnostic grid of the aligned detector stacks. It is intended for visual checks of image coverage, registration, source morphology, and possible artifacts. It is not itself clickable.

## Exporting results

### CSV

Click **Save CSV** and choose a destination. The suggested filename is `spherex_spectrum.csv`. The file uses UTF-8 text and contains:

```csv
wavelength_um,flux_ujy,error_ujy,detector,measurements
```

| Column | Meaning |
| --- | --- |
| `wavelength_um` | Wavelength in micrometres. |
| `flux_ujy` | Aperture flux density in microjanskys. |
| `error_ujy` | Flux-density uncertainty in microjanskys. |
| `detector` | SPHEREx detector number associated with the point. |
| `measurements` | Number of measurements represented by the row; useful when binning is enabled. |

### PNG

Click **Save PNG** and choose a destination. The suggested filename is `spherex_spectrum.png`. The export is a 1400 by 900 pixel rendering of the current spectrum chart. It does not include the RGB or detector-stack images.

## Saved settings

The tab writes `spherex.properties` in the active cache directory. It records the most recently used:

- RA and Dec;
- cutout size;
- aperture radius.

At startup, the tab reads these defaults from the base SPHEREx directory. Settings in a custom or coordinate-specific cache directory are used to decide whether the existing cutouts can be reused. Removing the properties file is safe, but causes the tab to treat the next run in that directory as a new field request.

## Troubleshooting

| Symptom | Likely cause and action |
| --- | --- |
| **Coordinates must be provided** | Enter a position before generating. |
| Coordinate validation error | Check RA/Dec order and range. Use decimal degrees or a complete six-part sexagesimal position. |
| Cutout-size or aperture validation error | Use a cutout between 16 and 1800 arcsec and a positive aperture radius. |
| **No SPHEREx cutouts cover these coordinates** | The public service returned no coverage for the requested location. Check the position, or try again later if service availability is suspected. |
| **No usable cutouts were measured** | The available cutouts could not produce valid photometry. Inspect any skipped-cutout count, try a different aperture or cutout size, or select fresh downloads. |
| The result seems stale | Select **Delete downloaded files**, then generate again; or choose a new empty cache directory. |
| A click does not extract a spectrum | Click inside the RGB image itself and within the displayed field. Wait for the initial generation to finish first. |
| The RGB panel is absent | Image stacking runs after spectrum extraction. A successful spectrum can still be produced even when the image stack cannot be displayed. |
| Download or query error | Confirm network connectivity and retry. Initial runs may need both public cutout access and calibration downloads. |

## Practical notes

- The displayed spectrum is an aperture-photometry product. In crowded fields, extended sources, or strong structured backgrounds, adjust the aperture thoughtfully and assess the detector-stack images.
- A larger cutout makes more surrounding field available for RGB-click extraction, but it is not a substitute for choosing an aperture appropriate to the source.
- Keep a project cache directory when repeatability matters: it preserves the exact downloaded FITS inputs used for the spectrum.
- Treat the red spline as an inspection aid. Use the exported points and uncertainties for quantitative analysis.
