package astro.tool.box.util;

import astro.tool.box.enumeration.FileType;
import static astro.tool.box.util.ConversionFactors.*;

public class Urls {

    // FoV in arcsec
    public static String getPanstarrsUrl(double degRA, double degDE, int fieldOfView, FileType fileType) {
        return "https://ps1images.stsci.edu/cgi-bin/ps1cutouts?pos=" + degRA + "%20" + degDE + "&filter=color&filetypes=" + fileType.val + "&size=" + fieldOfView * 4 + "&output_size=256&autoscale=99.8";
    }

    // FoV in degrees
    public static String getAladinLiteUrl(double degRA, double degDE, int fieldOfView) {
        return "http://aladin.unistra.fr/AladinLite/?target=" + degRA + "%20" + degDE + "&fov=" + fieldOfView / DEG_ARCSEC + "&survey=allWISE/color";
    }

    // FoV in arcsec
    public static String getWiseViewUrl(double degRA, double degDE, int fieldOfView) {
        int zoom = 1500 / fieldOfView;
        zoom = zoom < 2 ? 2 : zoom;
        return "http://byw.tools/wiseview#ra=" + degRA + "&dec=" + degDE + "&size=" + fieldOfView + "&zoom=" + zoom + "&band=3&speed=300&minbright=-50&maxbright=200&window=0.5&diff_window=1&linear=1&color=&border=0&gaia=0&invert=1&maxdyr=0&scandir=0&neowise=0&diff=0&outer_epochs=0&unique_window=1&smooth_scan=0&shift=0&pmra=0&pmdec=0&synth_a=0&synth_a_sub=0&synth_a_ra=&synth_a_dec=&synth_a_w1=&synth_a_w2=&synth_a_pmra=0&synth_a_pmdec=0&synth_a_mjd=&synth_b=0&synth_b_sub=0&synth_b_ra=&synth_b_dec=&synth_b_w1=&synth_b_w2=&synth_b_pmra=0&synth_b_pmdec=0&synth_b_mjd=";
    }

    // FoV in degrees
    public static String getFinderChartUrl(double degRA, double degDE, int fieldOfView) {
        return "https://irsa.ipac.caltech.edu/applications/finderchart/?__action=table.search&request=%7B%22startIdx%22:0,%22pageSize%22:100,%22id%22:%22QueryFinderChartWeb%22,%22tbl_id%22:%22results%22,%22UserTargetWorldPt%22:%22" + degRA + ";" + degDE + ";EQ_J2000%22,%22imageSizeAndUnit%22:%22" + fieldOfView / DEG_ARCSEC + "%22,%22thumbnail_size%22:%22192%22,%22selectImage%22:%22dss,sdss,2mass,wise%22,%22searchCatalog%22:%22no%22,%22ckgDSS%22:%22dss1Blue,dss1Red,dss2Blue,dss2Red,dss2IR%22,%22ckgSDSS%22:%22u,g,r,z%22,%22ckg2MASS%22:%22j,h,k%22,%22ckgWISE%22:%22w1,w2,w3,w4%22,%22imageSearchOptions%22:%22closed%22,%22META_INFO%22:%7B%22title%22:%22QueryFinderChartWeb%22,%22tbl_id%22:%22results%22%7D%7D&options=%7B%22tbl_group%22:%22results%22,%22removable%22:false,%22showTitle%22:false,%22pageSize%22:100%7D";
    }

    public static String getLegacySkyViewerUrl(double degRA, double degDE, String layer) {
        return "http://legacysurvey.org/viewer?ra=" + degRA + "&dec=" + degDE + "&mark=" + degRA + "," + degDE + "&zoom=15&layer=" + layer;
    }

    public static String getLegacySingleExposuresUrl(double degRA, double degDE, String layer) {
        return "http://legacysurvey.org/viewer/exposures/?ra=" + degRA + "&dec=" + degDE + "&layer=" + layer;
    }

    public static String getDataDiscoveryUrl() {
        return "https://irsa.ipac.caltech.edu/applications/Radar";
    }

    public static String getSimbadUrl(double degRA, double degDE, double degRadius) {
        return "http://simbad.u-strasbg.fr/simbad/sim-coo?Coord=" + degRA + "%20" + degDE + "&Radius=" + degRadius + "&Radius.unit=arcsec&coodisp1=d2&list.pmsel=on&list.plxsel=on&list.rvsel=on&list.bibsel=off&list.notesel=off&output.format=HTML";
    }

    public static String getVizierUrl(double degRA, double degDE, double degRadius, int maxRows, boolean allColumns) {
        String outAll = allColumns ? "&-out.all" : "";
        return "http://vizier.u-strasbg.fr/viz-bin/VizieR?-c=" + degRA + "%20" + degDE + "&-c.rs=" + degRadius + "&-out.max=" + maxRows + "&-out.add=_r&-sort=_r" + outAll;
    }

    public static String getSpecificCatalogsUrl(String catalogName, double degRA, double degDE, double degRadius) {
        return "http://vizier.u-strasbg.fr/viz-bin/VizieR?-source=" + catalogName + "&-c=" + degRA + "%20" + degDE + "&-c.rs=" + degRadius + "&-out.add=_r&-sort=_r";
    }

    public static String getTygoFormUrl() {
        return "https://docs.google.com/forms/d/e/1FAIpQLScse_fHPVnudm2rPgouKRkq18BsBVx11jGqW5rc8mLqj2Lxpw/viewform?";
    }

}
