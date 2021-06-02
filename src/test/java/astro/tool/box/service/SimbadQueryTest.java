package astro.tool.box.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimbadQueryTest {

    @Test
    public void getObjectIdentifiers() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> identifiers = simbadQueryService.getObjectIdentifiers("2MASS J13570965+5544496");
        //identifiers.forEach(identifier -> System.out.println(Arrays.toString(identifier)));
        assertEquals(7, identifiers.size());
    }

    @Test
    public void getObjectReferences() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> references = simbadQueryService.getObjectReferences("2MASS J13570965+5544496");
        //references.forEach(reference -> System.out.println(Arrays.toString(reference)));
        assertEquals(2, references.size());
    }

    @Test
    public void getAbstract() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        String result = simbadQueryService.getAbstract(206125);
        //System.out.println(result);
        assertEquals(883, result.length());
    }

    @Test
    public void getAuthors() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> authors = simbadQueryService.getAuthors(206125);
        //authors.forEach(author -> System.out.println(Arrays.toString(author)));
        assertEquals(10, authors.size());
    }

    @Test
    public void getVizierCatalogs() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String> catalogs = simbadQueryService.getVizierCatalogs("2015PASA...32...10F");
        //catalogs.forEach(System.out::println);
        assertEquals(5, catalogs.size());
    }

    @Test
    public void getObjectTypes() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> types = simbadQueryService.getObjectTypes("2MASS J13570965+5544496");
        types.forEach(reference -> System.out.println(Arrays.toString(reference)));
        assertEquals(3, types.size());
    }

}
