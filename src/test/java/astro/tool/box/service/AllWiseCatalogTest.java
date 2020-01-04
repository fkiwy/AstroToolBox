package astro.tool.box.service;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.module.ServiceProviderUtils.*;
import static astro.tool.box.util.TestData.*;

import astro.tool.box.container.catalog.AllWiseCatalogEntry;
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

public class AllWiseCatalogTest {

    CatalogQueryFacade catalogQueryProxy = new CatalogQueryProxy();
    CatalogQueryFacade catalogQueryService = new CatalogQueryService();
    AllWiseCatalogEntry catalogEntry = new AllWiseCatalogEntry();

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
        String irsaUrl = createIrsaUrl(ALLWISE_CATALOG_ID, DEG_RA, DEG_DE, DEG_RADIUS / DEG_ARCSEC);
        HttpURLConnection connection = establishHttpConnection(irsaUrl);

        assertEquals(200, connection.getResponseCode());
        assertEquals("OK", connection.getResponseMessage());

        BufferedReader reader = new BufferedReader(new StringReader(readResponse(connection)));
        List<String[]> results = reader.lines().map(line -> {
            //System.out.println(line);
            return line.split(SPLIT_CHAR);
        }).collect(Collectors.toList());

        String[] header = results.get(0);
        assertEquals("designation", header[0]);
        assertEquals("ra", header[1]);
        assertEquals("dec", header[2]);
        assertEquals("w1mpro", header[16]);
        assertEquals("w1sigmpro", header[17]);
        assertEquals("w2mpro", header[20]);
        assertEquals("w2sigmpro", header[21]);
        assertEquals("w3mpro", header[24]);
        assertEquals("w3sigmpro", header[25]);
        assertEquals("w4mpro", header[28]);
        assertEquals("w4sigmpro", header[29]);
        assertEquals("pmra", header[45]);
        assertEquals("sigpmra", header[46]);
        assertEquals("pmdec", header[47]);
        assertEquals("sigpmdec", header[48]);
        assertEquals("cc_flags", header[55]);
        assertEquals("ext_flg", header[57]);
        assertEquals("var_flg", header[58]);
        assertEquals("ph_qual", header[59]);
        assertEquals("j_m_2mass", header[288]);
        assertEquals("j_msig_2mass", header[289]);
        assertEquals("h_m_2mass", header[290]);
        assertEquals("h_msig_2mass", header[291]);
        assertEquals("k_m_2mass", header[292]);
        assertEquals("k_msig_2mass", header[293]);

        //for (int i = 0; i < header.length; i++) {
        //    System.out.println(header[i] + " : " + i);
        //}
    }

}
