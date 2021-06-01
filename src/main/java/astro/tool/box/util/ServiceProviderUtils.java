package astro.tool.box.util;

import astro.tool.box.exception.ADQLException;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.SdssCatalogEntry;
import astro.tool.box.module.tab.AdqlQueryTab;
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

    private static final String SERVICE_NOT_AVAILABLE = "%s is currently not available!";

    public static String createSimbadUrl(double degRA, double degDE, double degRadius) {
        return getSimbadBaseUrl() + "SELECT%20main_id,%20otype_longname,%20sp_type,%20ra,%20dec,%20plx_value,%20plx_err,%20pmra,%20pmdec,%20rvz_radvel,%20rvz_redshift,%20rvz_type,%20U,%20B,%20V,%20R,%20I,%20G,%20J,%20H,%20K,%20u_,%20g_,%20r_,%20i_,%20z_%20,%27.%27%20FROM%20basic%20AS%20b,%20otypedef%20AS%20o%20LEFT%20JOIN%20allfluxes%20ON%20oid%20=%20oidref%20WHERE%20b.otype=%20o.otype%20AND%20otype_txt%20<>%20%27err%27%20AND%201=CONTAINS(POINT(%27ICRS%27,%20ra,%20dec),%20CIRCLE(%27ICRS%27,%20" + degRA + ",%20" + degDE + ",%20" + degRadius + "))";
    }

    public static String getSimbadBaseUrl() {
        boolean useSimbadMirror = Boolean.parseBoolean(getUserSetting(USE_SIMBAD_MIRROR));
        return useSimbadMirror ? SIMBAD_MIRROR_URL : SIMBAD_BASE_URL;
    }

    public static String createVizieRUrl(double degRA, double degDE, double degRadius, String tableName, String raColName, String decColName) {
        return VIZIER_TAP_URL + "SELECT%20*%20FROM%20%22" + tableName + "%22%20WHERE%201=CONTAINS(POINT(%27ICRS%27,%20" + raColName + ",%20" + decColName + "),%20CIRCLE(%27ICRS%27,%20" + degRA + ",%20" + degDE + ",%20" + degRadius + "))";
    }

    public static String createIrsaUrl(double degRA, double degDE, double degRadius, String catalogId) {
        return IRSA_BASE_URL + "?table=" + catalogId + "&RA=" + degRA + "&DEC=" + degDE + "&SR=" + degRadius + "&format=csv";
    }

    public static String createPanStarrsUrl(double degRA, double degDE, double degRadius) {
        return PANSTARRS_BASE_URL + "?ra=" + degRA + "&dec=" + degDE + "&radius=" + degRadius + "&nDetections.gt=2&columns=[objName,objID,qualityFlag,raMean,decMean,raMeanErr,decMeanErr,epochMean,nDetections,gMeanPSFMag,gMeanPSFMagErr,rMeanPSFMag,rMeanPSFMagErr,iMeanPSFMag,iMeanPSFMagErr,zMeanPSFMag,zMeanPSFMagErr,yMeanPSFMag,yMeanPSFMagErr]";
    }

    public static String createSdssUrl(double degRA, double degDE, double degRadius) {
        return SDSS_BASE_URL + "/SkyServerWS/ImagingQuery/Cone?ra=" + degRA + "&dec=" + degDE + "&radius=" + degRadius + "&limit=0&format=csv&imgparams=objid,run,rerun,camcol,field,obj,ra,dec,raErr,decErr,type,clean,mjd,specObjID,u,g,r,i,z,Err_u,Err_g,Err_r,Err_i,Err_z";
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
            if (AdqlQueryTab.QUERY_SERVICE.equals(serviceProvider)) {
                throw new ADQLException(ex);
            }
            writeErrorLog(ex);
            showWarnDialog(null, String.format(SERVICE_NOT_AVAILABLE, serviceProvider));
            return "";
        }
    }

    public static List<CatalogEntry> transformResponseToCatalogEntries(String response, CatalogEntry catalogEntry) throws IOException {
        List<CatalogEntry> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(response))) {
            if (catalogEntry instanceof SdssCatalogEntry) {
                reader.readLine();
            }
            String headerLine = reader.readLine();
            String[] headers = CSVParser.parseLine(headerLine);
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i], i);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = CSVParser.parseLine(line);
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].replace(SPLIT_CHAR, SPLIT_CHAR_REPLACEMENT);
                }
                entries.add(catalogEntry.getInstance(columns, values));
            }
        }
        return entries;
    }

}
