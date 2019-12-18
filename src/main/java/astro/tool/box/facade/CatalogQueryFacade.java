package astro.tool.box.facade;

import astro.tool.box.container.catalog.CatalogEntry;
import java.io.IOException;
import java.util.List;

public interface CatalogQueryFacade {

    List<CatalogEntry> getCatalogEntriesByCoords(CatalogEntry catalogEntry) throws IOException;

}
