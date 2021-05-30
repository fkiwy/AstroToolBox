package astro.tool.box.service;

import astro.tool.box.util.CSVParser;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.Utils.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimbadQueryService {

    private static final String SERVICE_PROVIDER = "SIMBAD";

    public List<String> getObjectIdentifiers(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select i.ids");
        addRow(query, "from   ids as i, basic as b");
        addRow(query, "where  i.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        String queryUrl = getSimbadBaseUrl() + encodeQuery(query.toString());
        String response = readResponse(establishHttpConnection(queryUrl), SERVICE_PROVIDER);
        BufferedReader reader = new BufferedReader(new StringReader(response));
        reader.readLine(); // omit header
        String line = reader.readLine();
        if (line == null) {
            return Collections.EMPTY_LIST;
        }
        line = removeFirstandLastChar(line);
        return Stream.of(line.split("\\|")).collect(Collectors.toList());
    }

    public List<String[]> getObjectReferences(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select r.oidbib, r.\"year\", r.bibcode, r.title, r.abstract");
        addRow(query, "from   ref as r, has_ref as h, basic as b");
        addRow(query, "where  r.oidbib = h.oidbibref");
        addRow(query, "and    h.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by \"year\" desc");
        String queryUrl = getSimbadBaseUrl() + encodeQuery(query.toString());
        String response = readResponse(establishHttpConnection(queryUrl), SERVICE_PROVIDER);
        BufferedReader reader = new BufferedReader(new StringReader(response));
        reader.readLine(); // omit header
        String line;
        List<String[]> references = new ArrayList();
        while ((line = reader.readLine()) != null) {
            references.add(CSVParser.parseLine(line));
        }
        return references;
    }

    public List<String> getAuthors(Integer bibRef) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select name");
        addRow(query, "from   author");
        addRow(query, "where  oidbibref = " + bibRef);
        addRow(query, "order by pos asc");
        String queryUrl = getSimbadBaseUrl() + encodeQuery(query.toString());
        String response = readResponse(establishHttpConnection(queryUrl), SERVICE_PROVIDER);
        BufferedReader reader = new BufferedReader(new StringReader(response));
        reader.readLine(); // omit header
        String line;
        List<String> references = new ArrayList();
        while ((line = reader.readLine()) != null) {
            line = removeFirstandLastChar(line);
            references.add(line);
        }
        return references;
    }

}
