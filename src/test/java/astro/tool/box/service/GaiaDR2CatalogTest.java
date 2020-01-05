package astro.tool.box.service;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.TestData.*;

import astro.tool.box.container.catalog.GaiaDR2CatalogEntry;
import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.proxy.CatalogQueryProxy;
import astro.tool.box.container.catalog.CatalogEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;

public class GaiaDR2CatalogTest {

    CatalogQueryFacade catalogQueryProxy = new CatalogQueryProxy();
    CatalogQueryFacade catalogQueryService = new CatalogQueryService();
    GaiaDR2CatalogEntry catalogEntry = new GaiaDR2CatalogEntry();

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
        String irsaUrl = createIrsaUrl(GAIADR2_CATALOG_ID, DEG_RA, DEG_DE, DEG_RADIUS / DEG_ARCSEC);
        HttpURLConnection connection = establishHttpConnection(irsaUrl);

        assertEquals(200, connection.getResponseCode());
        assertEquals("OK", connection.getResponseMessage());

        BufferedReader reader = new BufferedReader(new StringReader(readResponse(connection)));
        List<String[]> results = reader.lines().map(line -> {
            //System.out.println(line);
            return line.split(SPLIT_CHAR);
        }).collect(Collectors.toList());

        String[] header = results.get(0);
        assertEquals("source_id", header[2]);
        assertEquals("ra", header[5]);
        assertEquals("dec", header[7]);
        assertEquals("parallax", header[9]);
        assertEquals("parallax_error", header[10]);
        assertEquals("pmra", header[12]);
        assertEquals("pmra_error", header[13]);
        assertEquals("pmdec", header[14]);
        assertEquals("pmdec_error", header[15]);
        assertEquals("phot_g_mean_mag", header[50]);
        assertEquals("phot_bp_mean_mag", header[55]);
        assertEquals("phot_rp_mean_mag", header[60]);
        assertEquals("bp_rp", header[63]);
        assertEquals("bp_g", header[64]);
        assertEquals("g_rp", header[65]);
        assertEquals("radial_velocity", header[66]);
        assertEquals("radial_velocity_error", header[67]);
        assertEquals("teff_val", header[78]);
        assertEquals("radius_val", header[88]);
        assertEquals("lum_val", header[91]);

        //for (int i = 0; i < header.length; i++) {
        //    System.out.println(header[i] + " : " + i);
        //}
    }

}
