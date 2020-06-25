package astro.tool.box.service;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class DustExtinctionService {

    private static final String SERVICE_PROVIDER = "IRSA Dust Extinction Tool";
    private static final String BASE_URL = "https://irsa.ipac.caltech.edu/cgi-bin/DUST/nph-dust";
    private static final String CHAR = REGEXP_SPACES;

    public Map<String, Double> getExtinctionsByBand(double degRA, double degDE, double degSize) throws Exception {
        Map<String, Double> extinctionsByBand = new HashMap<>();

        String response = readResponse(establishHttpConnection(BASE_URL + "?locstr=" + degRA + "+" + degDE + "&regSize=" + degSize), SERVICE_PROVIDER);
        if (response.isEmpty()) {
            return extinctionsByBand;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(response)));
        Element root = document.getDocumentElement();
        String tableLink = root.getElementsByTagName("table").item(0).getTextContent().trim();
        response = readResponse(establishHttpConnection(tableLink), SERVICE_PROVIDER);
        if (response.isEmpty()) {
            return extinctionsByBand;
        }

        try (Scanner scanner = new Scanner(response)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains(SDSS_U)) {
                    extinctionsByBand.put(SDSS_U, toDouble(line.split(CHAR)[5]));
                } else if (line.contains(SDSS_G)) {
                    extinctionsByBand.put(SDSS_G, toDouble(line.split(CHAR)[5]));
                } else if (line.contains(SDSS_R)) {
                    extinctionsByBand.put(SDSS_R, toDouble(line.split(CHAR)[5]));
                } else if (line.contains(SDSS_I)) {
                    extinctionsByBand.put(SDSS_I, toDouble(line.split(CHAR)[5]));
                } else if (line.contains(SDSS_Z)) {
                    extinctionsByBand.put(SDSS_Z, toDouble(line.split(CHAR)[5]));
                } else if (line.contains(TWO_MASS_J)) {
                    extinctionsByBand.put(TWO_MASS_J, toDouble(line.split(CHAR)[5]));
                } else if (line.contains(TWO_MASS_H)) {
                    extinctionsByBand.put(TWO_MASS_H, toDouble(line.split(CHAR)[5]));
                } else if (line.contains(TWO_MASS_K)) {
                    extinctionsByBand.put(TWO_MASS_K, toDouble(line.split(CHAR)[5]));
                } else if (line.contains(WISE_1)) {
                    extinctionsByBand.put(WISE_1, toDouble(line.split(CHAR)[4]));
                } else if (line.contains(WISE_2)) {
                    extinctionsByBand.put(WISE_2, toDouble(line.split(CHAR)[4]));
                } else if (line.contains(IRAC_1)) {
                    extinctionsByBand.put(IRAC_1, toDouble(line.split(CHAR)[4]));
                } else if (line.contains(IRAC_2)) {
                    extinctionsByBand.put(IRAC_2, toDouble(line.split(CHAR)[4]));
                }
            }
        }

        return extinctionsByBand;
    }

}
