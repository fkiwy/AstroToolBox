package astro.tool.box.service;

import astro.tool.box.container.catalog.SimbadCatalogEntry;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.TestData.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import org.junit.Test;

public class SimbadCatalogTest {

    @Test
    public void parseResponse() throws IOException {
        String simbadUrl = createSimbadUrl(DEG_RA, DEG_DE, DEG_RADIUS / DEG_ARCSEC);
        HttpURLConnection connection = establishHttpConnection(simbadUrl);

        assertEquals(200, connection.getResponseCode());
        assertEquals("OK", connection.getResponseMessage());

        BufferedReader reader = new BufferedReader(new StringReader(readResponse(connection, SimbadCatalogEntry.CATALOG_NAME)));
        List<String[]> results = reader.lines().map(line -> {
            System.out.println(line);
            return line.replace("|", ",").replaceAll(REGEXP_SPACES, "").replace("\"", "").split(SPLIT_CHAR);
        }).collect(Collectors.toList());

        String[] header = results.get(0);
        for (int i = 0; i < header.length; i++) {
            System.out.println(i + ": " + header[i]);
        }
    }

}
