package astro.tool.box.container;

import astro.tool.box.enumeration.Band;

import java.util.List;

public record SedBestMatch(String spt, double medianDiffMag, double meanDiffMag, double medianPhotDist,
                           double stdPhotDist, List<Band> photBands) {

}
