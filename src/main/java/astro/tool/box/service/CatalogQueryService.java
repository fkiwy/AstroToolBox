package astro.tool.box.service;

import static astro.tool.box.util.ServiceHelper.*;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.ProperMotionQuery;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry.Survey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CatalogQueryService {

    public List<CatalogEntry> getCatalogEntriesByCoords(CatalogEntry catalogEntry) throws IOException {
        if (catalogEntry instanceof UkidssCatalogEntry) {
            List<CatalogEntry> catalogEntries = new ArrayList();
            for (Survey survey : UkidssCatalogEntry.Survey.values()) {
                ((UkidssCatalogEntry) catalogEntry).setSurvey(survey);
                catalogEntries.addAll(
                        transformResponseToCatalogEntries(
                                readResponse(
                                        establishHttpConnection(catalogEntry.getCatalogUrl()), catalogEntry.getCatalogName()
                                ), catalogEntry
                        )
                );
            }
            return catalogEntries;
        }
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getCatalogUrl()), catalogEntry.getCatalogName()), catalogEntry);
    }

    public List<CatalogEntry> getCatalogEntriesByCoordsAndTpm(ProperMotionQuery catalogEntry) throws IOException {
        if (catalogEntry instanceof UkidssCatalogEntry) {
            List<CatalogEntry> catalogEntries = new ArrayList();
            for (Survey survey : UkidssCatalogEntry.Survey.values()) {
                ((UkidssCatalogEntry) catalogEntry).setSurvey(survey);
                catalogEntries.addAll(
                        transformResponseToCatalogEntries(
                                readResponse(
                                        establishHttpConnection(catalogEntry.getProperMotionQueryUrl()), catalogEntry.getCatalogName()
                                ), catalogEntry
                        )
                );
            }
            return catalogEntries;
        }
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getProperMotionQueryUrl()), catalogEntry.getCatalogName()), catalogEntry);
    }

}
