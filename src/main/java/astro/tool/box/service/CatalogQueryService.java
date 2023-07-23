package astro.tool.box.service;

import static astro.tool.box.util.ServiceHelper.*;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.MocaCatalogEntry;
import astro.tool.box.catalog.ProperMotionQuery;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry.Survey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CatalogQueryService {

    public List<CatalogEntry> getCatalogEntriesByCoords(CatalogEntry catalogEntry) throws IOException {
        if (catalogEntry instanceof UkidssCatalogEntry) {
            UkidssCatalogEntry entry = (UkidssCatalogEntry) catalogEntry;
            List<CatalogEntry> catalogEntries = new ArrayList();
            for (Survey survey : UkidssCatalogEntry.Survey.values()) {
                entry.setSurvey(survey);
                catalogEntries.addAll(transformResponseToCatalogEntries(readResponse(establishHttpConnection(entry.getCatalogQueryUrl()), entry.getCatalogName()), entry));
            }
            return catalogEntries;
        }
        if (catalogEntry instanceof MocaCatalogEntry) {
            return ((MocaCatalogEntry) catalogEntry).findCatalogEntries();
        }
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getCatalogQueryUrl()), catalogEntry.getCatalogName()), catalogEntry);
    }

    public List<CatalogEntry> getCatalogEntriesByCoordsAndTpm(ProperMotionQuery catalogEntry) throws IOException {
        if (catalogEntry instanceof UkidssCatalogEntry) {
            UkidssCatalogEntry entry = (UkidssCatalogEntry) catalogEntry;
            List<CatalogEntry> catalogEntries = new ArrayList();
            for (Survey survey : UkidssCatalogEntry.Survey.motionSurveys()) {
                entry.setSurvey(survey);
                catalogEntries.addAll(transformResponseToCatalogEntries(readResponse(establishHttpConnection(entry.getMotionQueryUrl()), entry.getCatalogName()), entry));
            }
            return catalogEntries;
        }
        return transformResponseToCatalogEntries(readResponse(establishHttpConnection(catalogEntry.getMotionQueryUrl()), catalogEntry.getCatalogName()), catalogEntry);
    }

}
