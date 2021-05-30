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
        List<String> identifiers = simbadQueryService.getObjectIdentifiers("2MASS J13570965+5544496");
        //identifiers.forEach(System.out::println);
        assertEquals(Arrays.asList("TIC 441639745,2MASSI J1357096+554449,2MUCD 11179,2MASS J13570965+5544496,SDSS J135709.55+554449.8,SDSS J135709.56+554449.8,Gaia DR2 1657463068194786432".split(",")), identifiers);
    }

    @Test
    public void getObjectReferences() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> references = simbadQueryService.getObjectReferences("2MASS J13570965+5544496");
        //references.forEach(r -> System.out.println(Arrays.toString(r)));
        assertEquals(2, references.size());
    }

    @Test
    public void getAuthors() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String> authors = simbadQueryService.getAuthors(206125);
        //authors.forEach(System.out::println);
        assertEquals(Arrays.asList("CRUZ K.L.,REID I.N.,KIRKPATRICK J.D.,BURGASSER A.J.,LIEBERT J.,SOLOMON A.R.,SCHMIDT S.J.,ALLEN P.R.,HAWLEY S.L.,COVEY K.R.".split(",")), authors);
    }

}
