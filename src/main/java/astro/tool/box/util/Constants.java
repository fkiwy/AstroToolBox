package astro.tool.box.util;

import java.time.format.DateTimeFormatter;

public class Constants {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final String NOAO_TAP_URL = "https://datalab.noao.edu/tap/sync";

    public static final String VIZIER_TAP_URL = "http://tapvizier.u-strasbg.fr/TAPVizieR/tap/sync/";

    public static final String SIMBAD_BASE_URL = "http://simbad.u-strasbg.fr/simbad/sim-tap/sync";

    public static final String SIMBAD_MIRROR_URL = "http://simbad.cfa.harvard.edu/simbad/sim-tap/sync";

    public static final String PANSTARRS_BASE_URL = "https://catalogs.mast.stsci.edu/api/v0.1/panstarrs/dr1/mean.csv";

    public static final String SDSS_BASE_URL = "http://skyserver.sdss.org/dr16";

    public static final String IRSA_BASE_URL = "https://irsa.ipac.caltech.edu/SCS";

    public static final String IRSA_TAP_URL = "https://irsa.ipac.caltech.edu/TAP";

    public static final String CUTOUT_SERVICE_URL = "http://byw.tools/cutout";

    public static final String ALLWISE_CATALOG_ID = "allwise_p3as_psd";

    public static final String ALLWISE_CATALOG_ID_VIZIER = "II/328/allwise";

    public static final String TWO_MASS_CATALOG_ID = "fp_psc";

    public static final String TWO_MASS_CATALOG_ID_VIZIER = "II/246/out";

    public static final String CATWISE_CATALOG_ID = "catwise_2020";

    public static final String CATWISE_REJECTED_ID = "catwise_2020_reject";

    public static final String GAIA_CATALOG_ID = "gaia_dr2_source";

    public static final String GAIA_CATALOG_ID_VIZIER = "I/345/gaia2";

    public static final String SSO_CATALOG_ID = "neowiser_p1ba_mch";

    public static final String LINE_BREAK = "<br/>";

    public static final String LINE_SEP = System.lineSeparator();

    public static final String LINE_SEP_TEXT_AREA = "\n";

    public static final String SPLIT_CHAR = ",";

    public static final String SPLIT_CHAR_REPLACEMENT = ";";

    public static final String REGEXP_SPACES = "\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    // Dust extinction bandpass keys
    public static final String SDSS_U = "SDSS u";
    public static final String SDSS_G = "SDSS g";
    public static final String SDSS_R = "SDSS r";
    public static final String SDSS_I = "SDSS i";
    public static final String SDSS_Z = "SDSS z";
    public static final String TWO_MASS_H = "2MASS H";
    public static final String TWO_MASS_K = "2MASS Ks";
    public static final String TWO_MASS_J = "2MASS J";
    public static final String WISE_1 = "WISE-1";
    public static final String WISE_2 = "WISE-2";
    public static final String IRAC_1 = "IRAC-1";
    public static final String IRAC_2 = "IRAC-2";

}
