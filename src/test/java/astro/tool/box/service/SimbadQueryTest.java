package astro.tool.box.service;

import java.io.IOException;
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
        //types.forEach(type -> System.out.println(Arrays.toString(type)));
        assertEquals(3, types.size());
    }

    @Test
    public void getObjectFluxes() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> fluxes = simbadQueryService.getObjectFluxes("2MASS J13570965+5544496");
        //fluxes.forEach(flux -> System.out.println(Arrays.toString(flux)));
        assertEquals(7, fluxes.size());
    }

    @Test
    public void getObjectVariabilities() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> variabilities = simbadQueryService.getObjectVariabilities("2MASS J13570965+5544496");
        //variabilities.forEach(variability -> System.out.println(Arrays.toString(variability)));
        assertEquals(0, variabilities.size());
    }

    @Test
    public void getObjectDistances() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> distances = simbadQueryService.getObjectDistances("2MASS J13570965+5544496");
        //distances.forEach(distance -> System.out.println(Arrays.toString(distance)));
        assertEquals(2, distances.size());
    }

    @Test
    public void getObjectProperMotions() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> properMotions = simbadQueryService.getObjectProperMotions("2MASS J13570965+5544496");
        //properMotions.forEach(properMotion -> System.out.println(Arrays.toString(properMotion)));
        assertEquals(1, properMotions.size());
    }

    @Test
    public void getObjectVelocities() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> velocities = simbadQueryService.getObjectVelocities("2MASS J13570965+5544496");
        //velocities.forEach(velocity -> System.out.println(Arrays.toString(velocity)));
        assertEquals(0, velocities.size());
    }

    @Test
    public void getObjectRotations() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> rotations = simbadQueryService.getObjectRotations("2MASS J13570965+5544496");
        //rotations.forEach(rotation -> System.out.println(Arrays.toString(rotation)));
        assertEquals(0, rotations.size());
    }

    @Test
    public void getObjectSpectralTypes() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> spectralTypes = simbadQueryService.getObjectSpectralTypes("2MASS J13570965+5544496");
        //spectralTypes.forEach(spectralType -> System.out.println(Arrays.toString(spectralType)));
        assertEquals(2, spectralTypes.size());
    }

    @Test
    public void getObjectParallaxes() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> parallaxes = simbadQueryService.getObjectParallaxes("2MASS J13570965+5544496");
        //parallaxes.forEach(parallax -> System.out.println(Arrays.toString(parallax)));
        assertEquals(1, parallaxes.size());
    }

    @Test
    public void getObjectMetallicities() throws IOException {
        SimbadQueryService simbadQueryService = new SimbadQueryService();
        List<String[]> metallicities = simbadQueryService.getObjectMetallicities("2MASS J13570965+5544496");
        //metallicities.forEach(metallicity -> System.out.println(Arrays.toString(metallicity)));
        assertEquals(0, metallicities.size());
    }

}
