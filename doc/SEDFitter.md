# Spectral Energy Distribution (SED) Fitter

## Overview

The **SED Fitting** window builds a broadband spectral energy distribution for a selected catalog object, then compares its photometry with ultracool-dwarf or white-dwarf templates. It queries the selected optical, near-infrared, and mid-infrared catalogs around the object's position, converts valid magnitudes to flux, and displays the result on logarithmic wavelength and flux axes.

The default ultracool-dwarf mode fits empirical M0--T9 templates. It reports a robust photometric-distance estimate for the best match. White-dwarf mode instead compares against the bundled white-dwarf model table, and can use Gaia BP, G, and RP photometry in place of the optical-survey bands.

Catalog queries require an internet connection. A result is only as reliable as its catalog cross-matches and input photometry; inspect the plotted points and their tooltips before interpreting a template match as a classification.

## Opening the SED Fitter

The fitter is available from the **Image Viewer** or **Catalog Query** tab for a selected catalog entry.

Open the fitter from the **Image Viewer** tab:
1. Enable the desired catalog overlays in the **Overlays** panel on the left.
2. Click the overlay marker corresponding to the object you want to analyze.
3. The **Object Details** window will open.
4. Click the **SED Fitting** button.

The SED fitter will then query the selected photometric catalogs, retrieve the available photometry, and automatically generate the object's Spectral Energy Distribution (SED). Depending on the selected catalogs and your internet connection, this process may take a while. The initial settings are Pan-STARRS for optical photometry, 2MASS for near-infrared photometry, AllWISE for mid-infrared photometry, a 5-arcsec search radius, and ultracool-dwarf templates.

## Input controls

| Control | Purpose | Notes |
| --- | --- | --- |
| **Search radius (\")** | Match radius used for catalog queries. | The default is 5 arcsec. Values below 1 are treated as 1 arcsec. A larger radius can help with uncertain coordinates or high proper motion, but increases the chance of an unrelated counterpart. The 2MASS query uses twice this radius. |
| **Gaia** | Uses Gaia DR3 BP, G, and RP photometry. | Available only with **White Dwarf Templates**. Selecting it clears the optical-survey choice. If no Gaia source is found, the fitter switches to Pan-STARRS. |
| **Pan-STARRS**, **NSC**, **DES** | Chooses the optical photometry source. | Choose one optical source. These provide g, r, i, z, and y measurements. NSC and DES use DECam filter references. |
| **2MASS**, **UKIDSS**, **UHS**, **VHS** | Chooses the near-infrared photometry source. | Choose one source for J, H, and K. UKIDSS and UHS are queried only north of Dec -5 degrees; VHS is queried only south of Dec +5 degrees. |
| **AllWISE**, **CatWISE**, **unWISE** | Chooses the mid-infrared photometry source. | Choose one source. AllWISE can supply W1, W2, and W3; CatWISE and unWISE supply W1 and W2. |
| **Ultracool Dwarf Templates** | Fits the M0--T9 ultracool-dwarf template library. | This is the default mode and shows the spectral-type list. |
| **White Dwarf Templates** | Fits the bundled white-dwarf templates. | The ultracool-dwarf spectral-type list is hidden. Gaia becomes available as the optical alternative. |
| **Create SED** | Requeries the selected catalogs and rebuilds the plot and template fit. | Use after changing the search radius, catalog selections, or template mode. |
| **SED templates** | Selects an individual ultracool-dwarf spectral type. | **SELECT** uses automatic matching. Choosing M0 through T9 adds that template for direct visual comparison. This control is unavailable in white-dwarf mode. |
| **Remove templates** | Rebuilds the plot with only the observed SED. | It does not change the retrieved catalog photometry. |
| **Best match** | Limits automatic results to the best template. | Enabled by default. When cleared, up to the three best automatic matches are plotted. |
| **Overplot templates** | Shifts a template to the target's brightness. | Enabled by default. When cleared, templates are shown at their native template magnitudes instead. |
| **Create PDF** | Creates a PDF of the current chart and opens it with the system PDF application. | The PDF is created as a temporary file. |
| **Get SED data points** | Opens the plotted data as wavelength--flux pairs. | The dialog includes the object series and any template series currently added to the chart. |

## Catalog selection and fallback

The three survey families are independent: select one optical source, one near-infrared source, and one mid-infrared source. Clicking a source clears the other choices in its family. The fitter also changes the selected check boxes as it follows a fallback chain, so the visible selections after a run show the sources it attempted or used.

If the preferred source has no matching entry, the following automatic fallbacks are applied during that run:

| Photometry family | Fallback order |
| --- | --- |
| Optical | Pan-STARRS -> NSC -> DES |
| Near infrared | 2MASS -> UKIDSS -> UHS -> VHS |
| Mid infrared | AllWISE -> CatWISE -> unWISE |
| White-dwarf optical, when Gaia is selected | Gaia -> Pan-STARRS -> NSC -> DES |

Coverage restrictions can advance or end the near-infrared chain. UKIDSS and UHS are not queried at Dec -5 degrees or below, and VHS is not queried at Dec +5 degrees or above. A queried catalog entry may also have missing or unusable individual-band photometry; the affected point is omitted rather than treated as a valid measurement.

## Reading the plot

The chart title is **Spectral Energy Distribution**. Both axes use logarithmic scales:

- Horizontal axis: wavelength in micrometres (um).
- Vertical axis: flux F(lambda) in W/m2/um.

The black series is the selected object's observed SED. It has circular markers, connecting lines, and vertical error bars where a magnitude uncertainty is available. Template series use colored lines and markers. Missing or invalid measurements are not plotted.

Hover over an observed black point to see its catalog, band, magnitude and magnitude error, effective wavelength, F(nu) in Jy, lambda F(lambda) in W/m2, and F(lambda) in W/m2/um. Right-click anywhere in the chart to open the standard chart menu, including save, print, and image-copy options.

For an automatically fitted ultracool dwarf, the legend area also reports the median photometric distance, its median-absolute-deviation-based scatter, and the bands used for that distance estimate.

## Template fitting

### Ultracool dwarfs

For every M0--T9 template with shared valid bands, the fitter calculates the magnitude difference

```text
delta_m_i = m_observed,i - m_template,i
```

and uses the median difference as the brightness offset:

```text
delta_m_median = median(delta_m_i)
```

It then measures each shape residual relative to that offset:

```text
r_i = abs(delta_m_i - delta_m_median)
```

Automatic matching requires at least four shared valid bands. A template is rejected when more than two bands have residuals of 0.3 mag or greater. Remaining templates are ranked by the mean residual; the smallest value is the best match. This is a robust shape comparison rather than a chi-squared fit, so it is less dominated by a small number of discrepant measurements.

The brightness offset also produces one photometric-distance estimate per shared band. The displayed distance is their median; its scatter is 1.4826 times the median absolute deviation. Treat it as a photometric estimate, particularly cautiously for unresolved multiples, unusual sources, reddened objects, or questionable cross-matches.

### White dwarfs

White-dwarf mode compares the observed SED with the bundled white-dwarf template entries. The automatic-fit controls still determine whether one or up to three lowest-residual matches are shown, but the ultracool-dwarf spectral-type picker and photometric-distance legend do not apply. Select **Gaia** in this mode to compare Gaia BP, G, and RP bands instead of the g, r, i, z, y optical set.

## Exporting results

Use **Create PDF** for a chart-ready PDF or the chart's right-click menu for image-oriented export and printing. Use **Get SED data points** to copy the numeric wavelength--flux pairs into another analysis tool. The data dialog reflects the visible object series and currently added templates; it is not a catalog-table export with all original magnitude fields.

## Practical workflow

1. Start with the default sources and inspect the automatically generated SED.
2. Hover over outlying points to identify their catalog, band, magnitude, and uncertainty.
3. If a counterpart is implausible, reduce the search radius or choose a different survey in that photometry family, then click **Create SED**.
4. Keep **Best match** selected for a concise classification, or clear it to compare up to three candidates.
5. Toggle **Overplot templates** to distinguish a template's SED shape from its native brightness.
6. For a different population hypothesis, switch between ultracool-dwarf and white-dwarf modes and regenerate the SED.
7. Export the plot or data once the catalog choices and template comparison are satisfactory.

## Troubleshooting

| Symptom | Likely cause and action |
| --- | --- |
| Few or no observed points appear | The selected catalogs may have no counterpart, no usable band measurements, or no coverage. Try a suitable alternative source, a modestly larger radius, or confirm the object's position. |
| A different catalog becomes selected | The preferred catalog did not return a usable counterpart and the fitter followed its fallback chain. The resulting check-box state indicates the current fallback choice. |
| No automatic ultracool-dwarf template appears | Fewer than four valid shared bands were found, or every template failed the residual criterion. Inspect the individual points, revise catalog choices, or choose a template manually for visual comparison. |
| The template fits the shape but not the brightness | Leave **Overplot templates** enabled to apply the median magnitude offset. Disable it only when comparing with the template's native brightness is intended. |
| Gaia is not visible | Gaia is available only after selecting **White Dwarf Templates**. |
| PDF creation does not open a viewer | A system PDF association or desktop-open permission may be missing. Use the chart's context menu to save an image instead. |

## Template sources and limitations

The ultracool-dwarf library uses M0--M5 templates from Deacon et al. (2016) and M6--T9 templates from Best et al. (2018). White-dwarf templates are based on the Bergeron white-dwarf atmosphere and cooling-model photometry included with the application.

SED fitting is intended for broadband photometric inspection and candidate classification. It does not replace spectroscopic classification or a dedicated physical-model fit.
