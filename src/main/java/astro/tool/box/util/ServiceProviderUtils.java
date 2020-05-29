package astro.tool.box.util;

import astro.tool.box.exception.ADQLException;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.SDSSCatalogEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceProviderUtils {

    private static final String SERVICE_NOT_AVAILABLE = "The %s service is currently not available.";

    public static String createIrsaUrl(String catalogId, double degRA, double degDE, double degRadius) {
        return IRSA_BASE_URL + "?table=" + catalogId + "&RA=" + degRA + "&DEC=" + degDE + "&SR=" + degRadius + "&format=csv";
    }

    public static String createPanStarrsUrl(double degRA, double degDE, double degRadius) {
        return PANSTARRS_BASE_URL + "?ra=" + degRA + "&dec=" + degDE + "&radius=" + degRadius + "&nDetections.gt=2&columns=[objName,objID,qualityFlag,raMean,decMean,raMeanErr,decMeanErr,epochMean,nDetections,gMeanPSFMag,gMeanPSFMagErr,rMeanPSFMag,rMeanPSFMagErr,iMeanPSFMag,iMeanPSFMagErr,zMeanPSFMag,zMeanPSFMagErr,yMeanPSFMag,yMeanPSFMagErr]";
    }

    public static String createSdssUrl(double degRA, double degDE, double degRadius) {
        return SDSS_BASE_URL + "/SkyServerWS/ImagingQuery/Cone?ra=" + degRA + "&dec=" + degDE + "&radius=" + degRadius + "&limit=0&format=csv&imgparams=objid,run,rerun,camcol,field,obj,ra,dec,raErr,decErr,type,clean,mjd,specObjID,u,g,r,i,z,Err_u,Err_g,Err_r,Err_i,Err_z";
    }

    public static String createVHSUrl(double degRA, double degDE, double degRadius) {
        return VIZIER_TAP_URL + "?request=doQuery&lang=adql&format=csv&query=SELECT%20SrcID,%20RAJ2000,%20DEJ2000,%20Mclass,%20Yap3,%20Jap3,%20Hap3,%20Ksap3,%20e_Yap3,%20e_Jap3,%20e_Hap3,%20e_Ksap3,%20%22Y-Jpnt%22,%20%22J-Hpnt%22,%20%22H-Kspnt%22,%20%22J-Kspnt%22%20FROM%20%22II/359/vhs_dr4%22%20WHERE%201=CONTAINS(POINT(%27ICRS%27,%20RAJ2000,%20DEJ2000),%20CIRCLE(%27ICRS%27,%20" + degRA + ",%20" + degDE + ",%20" + degRadius + "))";
    }

    public static String createGaiaWDUrl(double degRA, double degDE, double degRadius) {
        return VIZIER_TAP_URL + "?request=doQuery&lang=adql&format=csv&query=SELECT%20WD,%20%20Source,%20%20RA_ICRS,%20%20DE_ICRS,%20%20Plx,%20%20pmRA,%20pmDE,%20%20%22Gmag%22,%20%20BPmag,%20%20RPmag,%20%20SDSS,%20%20umag,%20%20%22gmag%22,%20%20rmag,%20%20imag,%20%20zmag,%20%20Pwd,%20%20TeffH,%20%20loggH,%20%20MassH,%20%20TeffHe,%20%20loggHe,%20MassHe%20FROM%20%22J/MNRAS/482/4570/gaia2wd%22%20WHERE%201=CONTAINS(POINT(%27ICRS%27,%20RAJ2000,%20DEJ2000),%20CIRCLE(%27ICRS%27,%20" + degRA + ",%20" + degDE + ",%20" + degRadius + "))";
    }

    public static String createUnWiseUrl(double degRA, double degDE, double degRadius) {
        return NOAO_TAP_URL + "?request=doQuery&lang=ADQL&format=csv&query=SELECT%20unwise_objid,%20ra,%20dec,%20mag_w1_vg,%20mag_w2_vg,%20w1_w2_vg,%20qf_w1,%20qf_w2,%20flags_unwise_w1,%20flags_unwise_w2,%20flags_info_w1,%20flags_info_w2%20FROM%20unwise_dr1.object%20WHERE%20ra%20BETWEEN%20" + (degRA - degRadius) + "%20AND%20" + (degRA + degRadius) + "%20AND%20dec%20BETWEEN%20" + (degDE - degRadius) + "%20AND%20" + (degDE + degRadius) + ";";
    }

    public static String createSimbadUrl(double degRA, double degDE, double degRadius) {
        boolean useSimbadMirror = Boolean.parseBoolean(getUserSetting(USE_SIMBAD_MIRROR));
        String simbadBaseUrl = useSimbadMirror ? SIMBAD_MIRROR_URL : SIMBAD_BASE_URL;
        return simbadBaseUrl + "?request=doQuery&lang=adql&format=csv&query=SELECT%20main_id,%20otype_longname,%20sp_type,%20ra,%20dec,%20plx_value,%20plx_err,%20pmra,%20pmdec,%20rvz_radvel,%20rvz_redshift,%20rvz_type,%20U,%20B,%20V,%20R,%20I,%20G,%20J,%20H,%20K,%20u_,%20g_,%20r_,%20i_,%20z_%20,%27.%27%20FROM%20basic%20AS%20b,%20otypedef%20AS%20o%20LEFT%20JOIN%20allfluxes%20ON%20oid%20=%20oidref%20WHERE%20b.otype=%20o.otype%20AND%20otype_txt%20<>%20%27err%27%20AND%201=CONTAINS(POINT(%27ICRS%27,%20ra,%20dec),%20CIRCLE(%27ICRS%27,%20" + degRA + ",%20" + degDE + ",%20" + degRadius + "))";
    }

    public static HttpURLConnection establishHttpConnection(String url) throws IOException {
        Proxy webProxy = null;
        boolean useProxy = Boolean.parseBoolean(getUserSetting(USE_PROXY));
        if (useProxy) {
            String proxyAddress = getUserSetting(PROXY_ADDRESS);
            int proxyPort = Integer.parseInt(getUserSetting(PROXY_PORT));
            if (!proxyAddress.isEmpty() && proxyPort != 0) {
                webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));
            }
        }
        return (HttpURLConnection) new URL(url).openConnection(webProxy == null ? Proxy.NO_PROXY : webProxy);
    }

    public static String readResponse(HttpURLConnection connection, String serviceProvider) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.lines().collect(Collectors.joining(LINE_SEP));
        } catch (Exception ex) {
            if (ex.getMessage().contains(IRSA_TAP_URL)) {
                throw new ADQLException();
            }
            writeErrorLog(ex);
            showInfoDialog(null, String.format(SERVICE_NOT_AVAILABLE, serviceProvider));
            return "";
        }
    }

    public static List<CatalogEntry> transformResponseToCatalogEntries(String response, CatalogEntry catalogEntry) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(response));
        if (catalogEntry instanceof SDSSCatalogEntry) {
            reader.readLine();
        }
        String headerLine = reader.readLine();
        String[] headers = CSVParser.parseLine(headerLine);
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            columns.put(headers[i], i);
        }
        String line;
        List<CatalogEntry> entries = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] values = CSVParser.parseLine(line);
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i].replace(SPLIT_CHAR, SPLIT_CHAR_REPLACEMENT);
            }
            entries.add(catalogEntry.getInstance(columns, values));
        }
        return entries;
    }

}
