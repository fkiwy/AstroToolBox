package astro.tool.box.function;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.TestData.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class CatalogFunctionsTest {

    @Test
    public void buildSimbadUrl() {
        String simbadUrl = createSimbadUrl(DEG_RA, DEG_DE, DEG_RADIUS / DEG_ARCSEC);
        assertEquals("http://simbad.u-strasbg.fr/simbad/sim-tap/sync?request=doQuery&lang=adql&format=text&query=SELECT%20main_id,%20otype_txt,%20sp_type,%20ra,%20dec,%20plx_value,%20plx_err,%20pmra,%20pmdec,%20rvz_radvel,%20rvz_redshift,%20rvz_type,%20U,%20B,%20V,%20R,%20I,%20G,%20J,%20H,%20K,%20u_,%20g_,%20r_,%20i_,%20z_%20,'.'%20FROM%20basic%20LEFT%20JOIN%20allfluxes%20ON%20oid%20=%20oidref%20WHERE%201=CONTAINS(POINT(%27ICRS%27,%20ra,%20dec),%20CIRCLE(%27ICRS%27,%20209.2891781,%2055.7474398,%200.002777777777777778))", simbadUrl);
    }

    @Test
    public void buildIrsaUrl() {
        String irsaUrl = createIrsaUrl(ALLWISE_CATALOG_ID, DEG_RA, DEG_DE, DEG_RADIUS / DEG_ARCSEC);
        assertEquals("https://irsa.ipac.caltech.edu/SCS?table=allwise_p3as_psd&RA=209.2891781&DEC=55.7474398&SR=0.002777777777777778&format=csv", irsaUrl);
    }

}
