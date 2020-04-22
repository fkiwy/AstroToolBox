package astro.tool.box.service;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.TestData.*;

import astro.tool.box.container.catalog.CatWiseCatalogEntry;
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

public class CatWiseCatalogTest {

    CatalogQueryFacade catalogQueryProxy = new CatalogQueryProxy();
    CatalogQueryFacade catalogQueryService = new CatalogQueryService();
    CatWiseCatalogEntry catalogEntry = new CatWiseCatalogEntry();

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
        String irsaUrl = createIrsaUrl(CATWISE_CATALOG_ID, DEG_RA, DEG_DE, DEG_RADIUS / DEG_ARCSEC);
        HttpURLConnection connection = establishHttpConnection(irsaUrl);

        assertEquals(200, connection.getResponseCode());
        assertEquals("OK", connection.getResponseMessage());

        BufferedReader reader = new BufferedReader(new StringReader(readResponse(connection)));
        List<String[]> results = reader.lines().map(line -> {
            //System.out.println(line);
            return line.split(SPLIT_CHAR);
        }).collect(Collectors.toList());

        String[] header = results.get(0);
        for (int i = 0; i < header.length; i++) {
            System.out.println(header[i] + " : " + i);
        }
    }

}
