package astro.tool.box.util;

import java.time.format.DateTimeFormatter;

public class Constants {

	public static final String ENCODING = "UTF-8";

	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static final String VHS_SURVEY_URL = "http://vsa.roe.ac.uk:8080/vdfs/GetImage?database=VHSDR6&programmeID=110&ra=%f&dec=%f&sys=J&filterID=%s&xsize=%s&ysize=%s&obsType=object&frameType=tilestack";

	public static final String UHS_SURVEY_URL = "http://wsa.roe.ac.uk:8080/wsa/GetImage?database=UHSDR2&programmeID=all&ra=%f&dec=%f&sys=J&filterID=%s&xsize=%s&ysize=%s&obsType=object&frameType=stack";

	public static final String UKIDSS_SURVEY_URL = "http://wsa.roe.ac.uk:8080/wsa/GetImage?database=UKIDSSDR11PLUS&programmeID=all&ra=%f&dec=%f&sys=J&filterID=%s&xsize=%s&ysize=%s&obsType=object&frameType=stack";

	public static final String TAP_URL_PARAMS = "/sync?request=doQuery&lang=ADQL&format=csv&query=";

	public static final String NOIRLAB_BASE_URL = "https://datalab.noirlab.edu/tap";

	public static final String NOIRLAB_TAP_URL = NOIRLAB_BASE_URL + TAP_URL_PARAMS;

	public static final String ESA_EUCLID_BASE_URL = "https://eas.esac.esa.int/tap-server/tap";

	public static final String ESA_GAIA_BASE_URL = "https://gea.esac.esa.int/tap-server/tap";

	public static final String ESA_GAIA_TAP_URL = ESA_GAIA_BASE_URL + TAP_URL_PARAMS;

	public static final String VIZIER_BASE_URL = "http://tapvizier.u-strasbg.fr/TAPVizieR/tap";

	public static final String VIZIER_TAP_URL = VIZIER_BASE_URL + TAP_URL_PARAMS;

	public static final String SIMBAD_BASE_URL = "https://simbad.u-strasbg.fr/simbad/sim-tap" + TAP_URL_PARAMS;

	public static final String SIMBAD_MIRROR_URL = "https://simbad.cfa.harvard.edu/simbad/sim-tap" + TAP_URL_PARAMS;

	public static final String PANSTARRS_BASE_URL = "https://catalogs.mast.stsci.edu/api/v0.1/panstarrs/dr2/mean.csv";

	public static final String SDSS_BASE_URL = "https://skyserver.sdss.org/dr17";

	public static final String IRSA_BASE_URL = "https://irsa.ipac.caltech.edu/SCS";

	public static final String IRSA_TAP_URL = "https://irsa.ipac.caltech.edu/TAP";

	public static final String CUTOUT_SERVICE_URL = "http://byw.tools/cutout";

	public static final String VHS_LABEL = "VHS";

	public static final String UHS_LABEL = "UHS";

	public static final String UKIDSS_LABEL = "UKIDSS";

	public static final String SDSS_LABEL = "SDSS 1998-2009";

	public static final int SPITZER_EPOCH = 2003;

	public static final int ALLWISE_EPOCH = 2010;

	public static final int DESI_LS_EPOCH = 2017;

	public static final String DESI_LS_DR_LABEL = "DR10";

	public static final String DESI_LS_DR_PARAM = "ls-dr10";

	public static final String DESI_FILTERS = "griz";

	public static final String MAMAJEK_VERSION = "2022.04.16";

	public static final String LINE_BREAK = "<br/>";

	public static final String LINE_SEP = System.lineSeparator();

	public static final String LINE_SEP_TEXT_AREA = "\n";

	public static final String SPLIT_CHAR = ",";

	public static final String SPLIT_CHAR_REPLACEMENT = ";";

	public static final String REGEXP_SPACES = "\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

	public static final double PIXEL_SCALE_WISE = 2.75;

	public static final double PIXEL_SCALE_DECAM = 0.25;

	public static final double PIXEL_SCALE_PS1 = 0.25;

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
