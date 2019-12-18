package astro.tool.box.service;

import static astro.tool.box.module.ServiceProviderUtils.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.facade.CatalogQueryFacade;
import java.io.IOException;
import java.util.List;

public class CatalogQueryService implements CatalogQueryFacade {

    @Override
    public List<CatalogEntry> getCatalogEntriesByCoords(CatalogEntry catalogEntry) throws IOException {
        if (catalogEntry instanceof SimbadCatalogEntry) {
            return transformResponseToCatalogEntries(readSimbadResponse(establishHttpConnection(catalogEntry.getCatalogUrl())), catalogEntry);
        } else {
            return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getCatalogUrl())), catalogEntry);
        }
    }

}
