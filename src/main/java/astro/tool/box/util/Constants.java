package astro.tool.box.util;

import java.time.format.DateTimeFormatter;

public class Constants {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final String SIMBAD_BASE_URL = "http://simbad.u-strasbg.fr/simbad/sim-tap/sync";

    public static final String SIMBAD_MIRROR_URL = "http://simbad.cfa.harvard.edu/simbad/sim-tap/sync";

    public static final String IRSA_BASE_URL = "https://irsa.ipac.caltech.edu/SCS";

    public static final String IRSA_TAP_URL = "https://irsa.ipac.caltech.edu/TAP";

    public static final String WISE_VIEW_URL = "http://byw.tools/cutout";

    public static final String ALLWISE_CATALOG_ID = "allwise_p3as_psd";

    public static final String CATWISE_CATALOG_ID = "cwcat";

    public static final String GAIADR2_CATALOG_ID = "gaia_dr2_source";

    public static final String LINE_SEP = System.lineSeparator();

    public static final String LINE_SEP_TEXT_AREA = "\n";

    public static final String SPLIT_CHAR = "[,;]";

}
