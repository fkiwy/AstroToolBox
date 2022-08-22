package astro.tool.box.util;

import astro.tool.box.enumeration.FileType;
import static astro.tool.box.util.ConversionFactors.*;

public class ExternalResources {

    // FoV in arcsec
    public static String getPanstarrsUrl(double degRA, double degDE, int fieldOfView, FileType fileType) {
        return "https://ps1images.stsci.edu/cgi-bin/ps1cutouts?pos=" + degRA + "%20" + degDE + "&filter=color&filetypes=" + fileType.val + "&size=" + fieldOfView * 4 + "&output_size=256&autoscale=99.8";
    }

    // FoV in degrees
    public static String getAladinLiteUrl(double degRA, double degDE, int fieldOfView) {
        return "http://aladin.unistra.fr/AladinLite/?target=" + degRA + "%20" + degDE + "&fov=" + fieldOfView / DEG_ARCSEC + "&survey=allWISE/color";
    }

    // FoV in arcsec
    public static String getWiseViewUrl(double degRA, double degDE, int fieldOfView, int skip, int sep, int diff, int min, int max) {
        int zoom = 1500 / fieldOfView;
        zoom = zoom < 2 ? 2 : zoom;
        return "http://byw.tools/wiseview#ra=" + degRA + "&dec=" + degDE + "&size=" + fieldOfView + "&zoom=" + zoom + "&band=3&speed=200&minbright=" + min + "&maxbright=" + max + "&window=1.0&diff_window=1&linear=1&color=&border=0&gaia=0&invert=1&maxdyr=1&scandir=" + sep + "&neowise=0&diff=" + diff + "&outer_epochs=" + skip + "&unique_window=1&smooth_scan=" + diff + "&shift=0&pmra=0&pmdec=0&synth_a=0&synth_a_sub=0&synth_a_ra=&synth_a_dec=&synth_a_w1=&synth_a_w2=&synth_a_pmra=0&synth_a_pmdec=0&synth_a_mjd=&synth_b=0&synth_b_sub=0&synth_b_ra=&synth_b_dec=&synth_b_w1=&synth_b_w2=&synth_b_pmra=0&synth_b_pmdec=0&synth_b_mjd=";
    }

    // FoV in degrees
    public static String getFinderChartUrl(double degRA, double degDE, int fieldOfView) {
        return "https://irsa.ipac.caltech.edu/applications/finderchart/?__action=table.search&request=%7B%22startIdx%22%3A0%2C%22pageSize%22%3A100%2C%22id%22%3A%22QueryFinderChartWeb%22%2C%22tbl_id%22%3A%22upload-table-id%22%2C%22UserTargetWorldPt%22%3A%22" + degRA + "%3B" + degDE + "%3BEQ_J2000%22%2C%22imageSizeAndUnit%22%3A%22" + fieldOfView / DEG_ARCSEC + "%22%2C%22thumbnail_size%22%3A%22192%22%2C%22selectImage%22%3A%22wise%2C2mass%2Csdss%2Cseip%2Cdss%22%2C%22searchCatalog%22%3A%22no%22%2C%22ckgDSS%22%3A%22dss1Blue%2Cdss1Red%2Cdss2Blue%2Cdss2Red%2Cdss2IR%22%2C%22ckgSDSS%22%3A%22u%2Cg%2Cr%2Cz%2Ci%22%2C%22ckg2MASS%22%3A%22j%2Ch%2Ck%22%2C%22ckgWISE%22%3A%22w1%2Cw2%2Cw3%2Cw4%22%2C%22ckgSEIP%22%3A%22irac1%2Cirac2%2Cirac3%2Cirac4%2Cmips24%22%2C%22imageSearchOptions%22%3A%22closed%22%2C%22META_INFO%22%3A%7B%22title%22%3A%22QueryFinderChartWeb%22%2C%22tbl_id%22%3A%22upload-table-id%22%7D%7D&options=%7B%22tbl_group%22%3A%22upload-table-id%22%2C%22removable%22%3Afalse%2C%22showTitle%22%3Afalse%2C%22pageSize%22%3A100%7D";
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

    public static String getTygoFormUrl() {
        return "https://docs.google.com/forms/d/e/1FAIpQLScse_fHPVnudm2rPgouKRkq18BsBVx11jGqW5rc8mLqj2Lxpw/viewform?";
    }

}
