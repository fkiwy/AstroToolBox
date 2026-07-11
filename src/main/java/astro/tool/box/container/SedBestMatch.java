package astro.tool.box.container;

import java.util.List;

import astro.tool.box.enumeration.Band;

public record SedBestMatch(String spt, double medianDiffMag, double meanDiffMag, double medianPhotDist,
                           double stdPhotDist, List<Band> photBands) {

}
