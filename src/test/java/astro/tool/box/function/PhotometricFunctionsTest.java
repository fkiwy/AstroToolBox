package astro.tool.box.function;

import astro.tool.box.container.StringPair;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

public class PhotometricFunctionsTest {

    @Test
    public void displayPanStarrsObjectInfo() {
        List<StringPair> objectInfo = PhotometricFunctions.getPanStarrsObjectInfoFlags(60);
        objectInfo.forEach((pair) -> {
            System.out.println(pair.getS1() + " = " + pair.getS2());
        });
        assertEquals(objectInfo.get(0).getS1(), "4");
        assertEquals(objectInfo.get(1).getS1(), "8");
        assertEquals(objectInfo.get(2).getS1(), "16");
        assertEquals(objectInfo.get(3).getS1(), "32");
        assertEquals(objectInfo.get(0).getS2(), "object IDed with known ICRF quasar (may have ICRF position measurement)");
        assertEquals(objectInfo.get(1).getS2(), "identified as likely QSO (Hernitschek+ 2015ApJ...801...45H) PQSO>=0.60");
        assertEquals(objectInfo.get(2).getS2(), "identified as possible QSO (Hernitschek+ 2015ApJ...801...45H) PQSO>=0.05");
        assertEquals(objectInfo.get(3).getS2(), "identified as likely RR Lyra (Hernitschek+ 2015ApJ...801...45H) PRRLyra>=0.60");
    }

}