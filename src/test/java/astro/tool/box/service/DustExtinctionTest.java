package astro.tool.box.service;

import static astro.tool.box.service.DustExtinctionService.*;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;

public class DustExtinctionTest {

    @Test
    public void getExtinctionsByBand() throws Exception {
        DustExtinctionService dustExtinctionService = new DustExtinctionService();
        Map<String, Double> extinctionsByBand = dustExtinctionService.getExtinctionsByBand(130.1761688, -41.7988218, 2.0);
        assertEquals((Double) 9.455, extinctionsByBand.get(SDSS_U));
        assertEquals((Double) 7.367, extinctionsByBand.get(SDSS_G));
        assertEquals((Double) 5.097, extinctionsByBand.get(SDSS_R));
        assertEquals((Double) 3.787, extinctionsByBand.get(SDSS_I));
        assertEquals((Double) 2.817, extinctionsByBand.get(SDSS_Z));
        assertEquals((Double) 1.613, extinctionsByBand.get(TWO_MASS_J));
        assertEquals((Double) 1.026, extinctionsByBand.get(TWO_MASS_H));
        assertEquals((Double) 0.691, extinctionsByBand.get(TWO_MASS_K));
        assertEquals((Double) 0.422, extinctionsByBand.get(WISE_1));
        assertEquals((Double) 0.326, extinctionsByBand.get(WISE_2));
    }

}
