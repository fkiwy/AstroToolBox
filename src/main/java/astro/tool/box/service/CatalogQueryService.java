package astro.tool.box.service;

import static astro.tool.box.util.ServiceHelper.*;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.ProperMotionQuery;
import java.io.IOException;
import java.util.List;

public class CatalogQueryService {

    public List<CatalogEntry> getCatalogEntriesByCoords(CatalogEntry catalogEntry) throws IOException {
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getCatalogUrl()), catalogEntry.getCatalogName()), catalogEntry);
    }

    public List<CatalogEntry> getCatalogEntriesByCoordsAndTpm(ProperMotionQuery catalogEntry) throws IOException {
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getProperMotionQueryUrl()), catalogEntry.getCatalogName()), catalogEntry);
    }

}
