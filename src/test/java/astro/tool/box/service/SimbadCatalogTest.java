package astro.tool.box.service;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.TestData.*;

import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.proxy.CatalogQueryProxy;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class SimbadCatalogTest {

    CatalogQueryFacade catalogQueryProxy = new CatalogQueryProxy();
    CatalogQueryFacade catalogQueryService = new CatalogQueryService();
    SimbadCatalogEntry catalogEntry = new SimbadCatalogEntry();

    @Before
    public void init() {
        catalogEntry.setRa(DEG_RA);
        catalogEntry.setDec(DEG_DE);
        catalogEntry.setSearchRadius(DEG_RADIUS);
    }

    @Test
    public void printProxyCatalogEntries() throws IOException {
        List<CatalogEntry> entriesFromProxy = catalogQueryProxy.getCatalogEntriesByCoords(catalogEntry);
        entriesFromProxy.forEach((entry) -> {
            System.out.println(entry);
        });
    }

    @Test
    public void getCatalogEntriesByCoords() throws IOException {
        List<CatalogEntry> entriesFromProxy = catalogQueryProxy.getCatalogEntriesByCoords(catalogEntry);
        List<CatalogEntry> entriesFromService = catalogQueryService.getCatalogEntriesByCoords(catalogEntry);
        assertEquals(entriesFromProxy.get(0), entriesFromService.get(0));
    }

    @Test
    public void parseResponse() throws IOException {
        String simbadUrl = createSimbadUrl(DEG_RA, DEG_DE, DEG_RADIUS / DEG_ARCSEC);
        HttpURLConnection connection = establishHttpConnection(simbadUrl);

        assertEquals(200, connection.getResponseCode());
        assertEquals("OK", connection.getResponseMessage());

        BufferedReader reader = new BufferedReader(new StringReader(readResponse(connection)));
        List<String[]> results = reader.lines().map(line -> {
            //System.out.println(line);
            return line.replace("|", ",").replaceAll(REGEXP_SPACES, "").replace("\"", "").split(SPLIT_CHAR);
        }).collect(Collectors.toList());

        String[] header = results.get(0);
        assertEquals("main_id", header[0]);
        assertEquals("otype_longname", header[1]);
        assertEquals("sp_type", header[2]);
        assertEquals("ra", header[3]);
        assertEquals("dec", header[4]);
        assertEquals("plx_value", header[5]);
        assertEquals("plx_err", header[6]);
        assertEquals("pmra", header[7]);
        assertEquals("pmdec", header[8]);
        assertEquals("rvz_radvel", header[9]);
        assertEquals("rvz_redshift", header[10]);
        assertEquals("rvz_type", header[11]);
        assertEquals("U", header[12]);
        assertEquals("B", header[13]);
        assertEquals("V", header[14]);
        assertEquals("R", header[15]);
        assertEquals("I", header[16]);
        assertEquals("G", header[17]);
        assertEquals("J", header[18]);
        assertEquals("H", header[19]);
        assertEquals("K", header[20]);
        assertEquals("u_", header[21]);
        assertEquals("g_", header[22]);
        assertEquals("r_", header[23]);
        assertEquals("i_", header[24]);
        assertEquals("z_", header[25]);

        //for (int i = 0; i < header.length; i++) {
        //    System.out.println(header[i] + " : " + i);
        //}
    }

}
