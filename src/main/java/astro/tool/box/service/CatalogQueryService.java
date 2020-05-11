package astro.tool.box.service;

import static astro.tool.box.util.ServiceProviderUtils.*;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.ProperMotionQuery;
import astro.tool.box.facade.CatalogQueryFacade;
import java.io.IOException;
import java.util.List;

public class CatalogQueryService implements CatalogQueryFacade {

    @Override
    public List<CatalogEntry> getCatalogEntriesByCoords(CatalogEntry catalogEntry) throws IOException {
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getCatalogUrl())), catalogEntry);
    }

    @Override
    public List<CatalogEntry> getCatalogEntriesByCoordsAndTpm(ProperMotionQuery properMotionQuery) throws IOException {
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(properMotionQuery.getProperMotionQueryUrl())), properMotionQuery);
    }

}
