package astro.tool.box.service;

import astro.tool.box.container.catalog.GaiaCatalogEntry;
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

public class GaiaCatalogTest {

    @Test
    public void parseResponse() throws IOException {
        String irsaUrl = createIrsaUrl(GAIA_CATALOG_ID, DEG_RA, DEG_DE, DEG_RADIUS / DEG_ARCSEC);
        HttpURLConnection connection = establishHttpConnection(irsaUrl);

        assertEquals(200, connection.getResponseCode());
        assertEquals("OK", connection.getResponseMessage());

        BufferedReader reader = new BufferedReader(new StringReader(readResponse(connection, GaiaCatalogEntry.CATALOG_NAME)));
        List<String[]> results = reader.lines().map(line -> {
            System.out.println(line);
            return line.split(SPLIT_CHAR);
        }).collect(Collectors.toList());

        String[] header = results.get(0);
        for (int i = 0; i < header.length; i++) {
            System.out.println(i + ": " + header[i]);
        }
    }

}
