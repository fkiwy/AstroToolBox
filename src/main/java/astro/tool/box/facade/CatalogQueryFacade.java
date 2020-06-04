package astro.tool.box.facade;

import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.ProperMotionQuery;
import java.io.IOException;
import java.util.List;

public interface CatalogQueryFacade {

    List<CatalogEntry> getCatalogEntriesByCoords(CatalogEntry catalogEntry) throws IOException;

    List<CatalogEntry> getCatalogEntriesByCoordsAndTpm(ProperMotionQuery properMotionQuery) throws IOException;

}
