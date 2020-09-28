package astro.tool.box.service;

//import static org.junit.Assert.*;
import org.junit.Test;

public class NameResolverTest {

    @Test
    public void getCoordinatesByName() throws Exception {
        NameResolverService nameResolverService = new NameResolverService();
        String coordinates = nameResolverService.getCoordinatesByName("VCC 596"); // NGC 4321 (Messier 100, VCC 596), Andromeda, Ring nebula
        System.out.println("coordinates=" + coordinates);
        //assertEquals("185.72888285 15.82230397", coordinates);
    }

}
