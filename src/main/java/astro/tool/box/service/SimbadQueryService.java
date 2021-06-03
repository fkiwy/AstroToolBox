package astro.tool.box.service;

import astro.tool.box.util.CSVParser;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.Utils.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimbadQueryService {

    private static final String SERVICE_PROVIDER = "SIMBAD";

    public List<String[]> getObjectIdentifiers(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select i.ids");
        addRow(query, "from   ids as i, basic as b");
        addRow(query, "where  i.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        String queryUrl = getSimbadBaseUrl() + encodeQuery(query.toString());
        String response = readResponse(establishHttpConnection(queryUrl), SERVICE_PROVIDER);
        try (Scanner scanner = new Scanner(response)) {
            scanner.nextLine();
            if (scanner.hasNext()) {
                String identifiers = removeFirstAndLastCharacter(scanner.nextLine());
                return Stream.of(identifiers.split("\\|")).map(identifier -> new String[]{identifier}).collect(Collectors.toList());
            }
        }
        return Collections.EMPTY_LIST;
    }

    public List<String[]> getObjectReferences(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select r.\"year\", r.journal, r.volume, r.title, r.bibcode, r.oidbib");
        addRow(query, "from   ref as r, has_ref as h, basic as b");
        addRow(query, "where  r.oidbib = h.oidbibref");
        addRow(query, "and    h.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by bibcode desc");
        String queryUrl = getSimbadBaseUrl() + encodeQuery(query.toString());
        String response = readResponse(establishHttpConnection(queryUrl), SERVICE_PROVIDER);
        List<String[]> references = new ArrayList();
        try (Scanner scanner = new Scanner(response)) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String[] reference = CSVParser.parseLine(scanner.nextLine());
                references.add(reference);
            }
        }
        return references;
    }

    public String getAbstract(Integer bibRef) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select abstract");
        addRow(query, "from   ref");
        addRow(query, "where  oidbib = " + bibRef);
        String queryUrl = getSimbadBaseUrl() + encodeQuery(query.toString());
        String response = readResponse(establishHttpConnection(queryUrl), SERVICE_PROVIDER);
        try (Scanner scanner = new Scanner(response)) {
            scanner.nextLine();
            if (scanner.hasNext()) {
                return removeFirstAndLastCharacter(scanner.nextLine());
            }
        }
        return "";
    }

    public List<String[]> getAuthors(Integer bibRef) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select name");
        addRow(query, "from   author");
        addRow(query, "where  oidbibref = " + bibRef);
        addRow(query, "order by pos asc");
        String queryUrl = getSimbadBaseUrl() + encodeQuery(query.toString());
        String response = readResponse(establishHttpConnection(queryUrl), SERVICE_PROVIDER);
        List<String[]> authors = new ArrayList();
        try (Scanner scanner = new Scanner(response)) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String author = removeFirstAndLastCharacter(scanner.nextLine());
                authors.add(new String[]{author});
            }
        }
        return authors;
    }

    public List<String> getVizierCatalogs(String bibcode) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select name");
        addRow(query, "from   METAcat");
        addRow(query, "where  bibcode = '" + bibcode + "'");
        addRow(query, "order by name asc");
        String queryUrl = VIZIER_TAP_URL + encodeQuery(query.toString());
        String response = readResponse(establishHttpConnection(queryUrl), SERVICE_PROVIDER);
        List<String> catalogs = new ArrayList();
        try (Scanner scanner = new Scanner(response)) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String catalog = removeFirstAndLastCharacter(scanner.nextLine());
                catalogs.add(catalog);
            }
        }
        return catalogs;
    }

    public List<String[]> getObjectFluxes(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select f.filter, f.flux, f.flux_err, f.qual, i.description, i.unit, f.bibcode ");
        addRow(query, "from   flux as f, filter as i, basic as b");
        addRow(query, "where  f.oidref = b.oid");
        addRow(query, "and    f.filter = i.filtername");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        return executeQuery(query.toString());
    }

    public List<String[]> getObjectTypes(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select d.otype_shortname, d.otype_longname");
        addRow(query, "from   otypes as t, otypedef as d, basic as b");
        addRow(query, "where  t.oidref = b.oid");
        addRow(query, "and    t.otype = d.otype");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        return executeQuery(query.toString());
    }

    public List<String[]> getObjectVariabilities(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select v.vartyp, v.lowVmax, v.vmax, v.r_vmax, v.magtyp, v.uppVmin, v.vmin, v.r_vmin, v.upperiod, v.period, v.r_period, v.epoch, v.r_epoch, v.raisingTime, v.r_raisingTime, v.bibcode");
        addRow(query, "from   mesVar as v, basic as b");
        addRow(query, "where  v.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by mespos asc");
        return executeQuery(query.toString());
    }

    public List<String[]> getObjectDistances(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select d.dist, d.qual, d.unit, d.minus_err, d.plus_err, d.method, d.bibcode");
        addRow(query, "from   mesDistance as d, basic as b");
        addRow(query, "where  d.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by mespos asc");
        return executeQuery(query.toString());
    }

    public List<String[]> getObjectProperMotions(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select p.pmra, p.pmra_err, p.pmde, p.pmde_err, p.coosystem, p.bibcode");
        addRow(query, "from   mesPM as p, basic as b");
        addRow(query, "where  p.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by mespos asc");
        return executeQuery(query.toString());
    }

    public List<String[]> getObjectVelocities(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select v.velType, v.velValue, v.meanError, v.quality, v.nbmes, v.nature, v.qual, v.wdomain, v.resolution, v.d, v.obsdate, v.remarks, v.origin, v.bibcode");
        addRow(query, "from   mesVelocities as v, basic as b");
        addRow(query, "where  v.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by mespos asc");
        return executeQuery(query.toString());
    }

    public List<String[]> getObjectRotations(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select r.upvsini, r.vsini, r.vsini_err, r.nbmes, r.qual, r.bibcode");
        addRow(query, "from   mesRot as r, basic as b");
        addRow(query, "where  r.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by mespos asc");
        return executeQuery(query.toString());
    }

    public List<String[]> getObjectSpectralTypes(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select s.dispsystem, s.mssnote, s.sptype, s.bibcode");
        addRow(query, "from   mesSpT as s, basic as b");
        addRow(query, "where  s.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by mespos asc");
        return executeQuery(query.toString());
    }

    public List<String[]> getObjectParallaxes(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select p.plx, p.plx_err, p.obscode, p.bibcode");
        addRow(query, "from   mesPlx as p, basic as b");
        addRow(query, "where  p.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by mespos asc");
        return executeQuery(query.toString());
    }

    public List<String[]> getObjectMetallicities(String mainIdentifier) throws IOException {
        StringBuilder query = new StringBuilder();
        addRow(query, "select m.teff, m.log_g, m.fe_h, m.flag, m.compstar, m.catno, m.bibcode");
        addRow(query, "from   mesFe_H as m, basic as b");
        addRow(query, "where  m.oidref = b.oid");
        addRow(query, "and    b.main_id = '" + mainIdentifier + "'");
        addRow(query, "order by mespos asc");
        return executeQuery(query.toString());
    }

    private List<String[]> executeQuery(String query) throws IOException {
        String queryUrl = getSimbadBaseUrl() + encodeQuery(query);
        String response = readResponse(establishHttpConnection(queryUrl), SERVICE_PROVIDER);
        List<String[]> references = new ArrayList();
        try (Scanner scanner = new Scanner(response)) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String[] reference = CSVParser.parseLine(scanner.nextLine());
                references.add(reference);
            }
        }
        return references;
    }

}
