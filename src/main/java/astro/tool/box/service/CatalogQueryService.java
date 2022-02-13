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
            return getUkidssEntries((UkidssCatalogEntry) catalogEntry, false);
        }
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getCatalogUrl()), catalogEntry.getCatalogName()), catalogEntry);
    }

    public List<CatalogEntry> getCatalogEntriesByCoordsAndTpm(ProperMotionQuery catalogEntry) throws IOException {
        if (catalogEntry instanceof UkidssCatalogEntry) {
            return getUkidssEntries((UkidssCatalogEntry) catalogEntry, true);
        }
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getProperMotionQueryUrl()), catalogEntry.getCatalogName()), catalogEntry);
    }

    private List<CatalogEntry> getUkidssEntries(UkidssCatalogEntry catalogEntry, boolean properMotionQuery) throws IOException {
        List<CatalogEntry> catalogEntries = new ArrayList();
        for (Survey survey : UkidssCatalogEntry.Survey.values()) {
            catalogEntry.setSurvey(survey);
            String queryUrl;
            if (properMotionQuery) {
                queryUrl = catalogEntry.getProperMotionQueryUrl();
            } else {
                queryUrl = catalogEntry.getCatalogUrl();
            }
            catalogEntries.addAll(transformResponseToCatalogEntries(readResponse(establishHttpConnection(queryUrl), catalogEntry.getCatalogName()), catalogEntry));
        }
        return catalogEntries;
    }

}
