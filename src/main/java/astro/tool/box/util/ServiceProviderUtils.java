package astro.tool.box.util;

import static astro.tool.box.util.Constants.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import astro.tool.box.container.catalog.CatalogEntry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceProviderUtils {

    private static final String SERVICE_NOT_AVAILABLE = "One of the service providers is currently not reachable.";

    public static String createIrsaUrl(String catalogId, double degRA, double degDE, double degRadius) {
        StringBuilder url = new StringBuilder(IRSA_BASE_URL)
                .append("?table=")
                .append(catalogId)
                .append("&RA=")
                .append(String.valueOf(degRA))
                .append("&DEC=")
                .append(String.valueOf(degDE))
                .append("&SR=")
                .append(String.valueOf(degRadius))
                .append("&format=csv");
        return url.toString();
    }

    public static String createPanStarrsUrl(double degRA, double degDE, double degRadius) {
        StringBuilder url = new StringBuilder(PANSTARRS_BASE_URL)
                .append("?ra=")
                .append(String.valueOf(degRA))
                .append("&dec=")
                .append(String.valueOf(degDE))
                .append("&radius=")
                .append(String.valueOf(degRadius))
                .append("&nDetections.gt=2&columns=[objName,objID,qualityFlag,raMean,decMean,raMeanErr,decMeanErr,epochMean,nDetections,gMeanPSFMag,gMeanPSFMagErr,rMeanPSFMag,rMeanPSFMagErr,iMeanPSFMag,iMeanPSFMagErr,zMeanPSFMag,zMeanPSFMagErr,yMeanPSFMag,yMeanPSFMagErr]");
        return url.toString();
    }

    public static String createSimbadUrl(double degRA, double degDE, double degRadius) {
        boolean useSimbadMirror = Boolean.parseBoolean(getUserSetting(USE_SIMBAD_MIRROR));
        StringBuilder url = new StringBuilder(useSimbadMirror ? SIMBAD_MIRROR_URL : SIMBAD_BASE_URL)
                .append("?request=doQuery&lang=adql&format=text&query=SELECT%20main_id,%20otype_longname,%20sp_type,%20ra,%20dec,%20plx_value,%20plx_err,%20pmra,%20pmdec,%20rvz_radvel,%20rvz_redshift,%20rvz_type,%20U,%20B,%20V,%20R,%20I,%20G,%20J,%20H,%20K,%20u_,%20g_,%20r_,%20i_,%20z_%20,'.'%20FROM%20basic%20AS%20b,%20otypedef%20AS%20o%20LEFT%20JOIN%20allfluxes%20ON%20oid%20=%20oidref%20WHERE%20b.otype=%20o.otype%20AND%20otype_txt%20<>%20%27err%27%20AND%201=CONTAINS(POINT(%27ICRS%27,%20ra,%20dec),%20CIRCLE(%27ICRS%27,%20")
                .append(String.valueOf(degRA))
                .append(",%20")
                .append(String.valueOf(degDE))
                .append(",%20")
                .append(String.valueOf(degRadius))
                .append("))");
        return url.toString();
    }

    public static HttpURLConnection establishHttpConnection(String url) throws MalformedURLException, IOException {
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

    public static String readResponse(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.lines().collect(Collectors.joining(LINE_SEP));
        } catch (IOException ex) {
            showInfoDialog(null, SERVICE_NOT_AVAILABLE);
            throw new RuntimeException(ex);
        }
    }

    public static String readSimbadResponse(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.lines().skip(1).map(line -> {
                return line.replace("|", ",").replaceAll(REGEXP_SPACES, "").replace("\"", "");
            }).collect(Collectors.joining(LINE_SEP));
        } catch (IOException ex) {
            showInfoDialog(null, SERVICE_NOT_AVAILABLE);
            throw new RuntimeException(ex);
        }
    }

    public static List<CatalogEntry> transformResponseToCatalogEntries(String response, CatalogEntry catalogEntry) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(response));
        return reader.lines().skip(1).map(line -> {
            return catalogEntry.getInstance(line.split(SPLIT_CHAR));
        }).collect(Collectors.toList());
    }

}
