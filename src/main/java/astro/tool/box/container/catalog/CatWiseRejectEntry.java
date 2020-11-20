package astro.tool.box.container.catalog;

import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceProviderUtils.*;

public class CatWiseRejectEntry extends CatWiseCatalogEntry {

    @Override
    public String getCatalogUrl() {
        return createIrsaUrl(CATWISE_REJECT_TABLE_ID, getRa(), getDec(), getSearchRadius() / DEG_ARCSEC);
    }

}
