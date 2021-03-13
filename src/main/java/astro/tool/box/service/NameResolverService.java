package astro.tool.box.service;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class NameResolverService {

    private static final String SERVICE_PROVIDER = "Sesame name resolver";
    private static final String BASE_URL = "http://cdsweb.u-strasbg.fr/cgi-bin/nph-sesame/";

    public String getCoordinatesByName(String name) throws Exception {
        String response = readResponse(establishHttpConnection(BASE_URL + "-oxp/~SNV?" + name.replaceAll(" +", "%20")), SERVICE_PROVIDER);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(response)));
        Element root = document.getDocumentElement();
        Node node = root.getElementsByTagName("INFO").item(0);
        if (node != null) {
            String info = node.getTextContent().trim();
            if (!info.isEmpty()) {
                throw new Exception("Invalid designation/Nothing found for " + name);
            }
        }
        String ra = root.getElementsByTagName("jradeg").item(0).getTextContent().trim();
        String dec = root.getElementsByTagName("jdedeg").item(0).getTextContent().trim();
        return roundTo7DecNZ(toDouble(ra)) + " " + roundTo7DecNZ(toDouble(dec));
    }

}
