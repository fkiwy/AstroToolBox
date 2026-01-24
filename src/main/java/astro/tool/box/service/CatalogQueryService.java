package astro.tool.box.service;

import static astro.tool.box.util.ServiceHelper.establishHttpConnection;
import static astro.tool.box.util.ServiceHelper.readResponse;
import static astro.tool.box.util.ServiceHelper.transformResponseToCatalogEntries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.MocaCatalogEntry;
import astro.tool.box.catalog.ProperMotionQuery;
import astro.tool.box.catalog.UhsCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry.Survey;

public class CatalogQueryService {

	public List<CatalogEntry> getCatalogEntriesByCoords(CatalogEntry catalogEntry) throws IOException {
		if (catalogEntry instanceof UkidssCatalogEntry entry) {
			List<CatalogEntry> catalogEntries = new ArrayList();
			for (Survey survey : UkidssCatalogEntry.Survey.values()) {
				entry.setSurvey(survey);
				catalogEntries.addAll(transformResponseToCatalogEntries(
						readResponse(establishHttpConnection(entry.getCatalogQueryUrl()), entry.getCatalogName()),
						entry));
			}
			return catalogEntries;
		}
		if (catalogEntry instanceof UhsCatalogEntry entry) {
			return entry.findCatalogEntries();
		}
		if (catalogEntry instanceof MocaCatalogEntry entry) {
			return entry.findCatalogEntries();
		}
		return transformResponseToCatalogEntries(
				readResponse(establishHttpConnection(catalogEntry.getCatalogQueryUrl()), catalogEntry.getCatalogName()),
				catalogEntry);
	}

	public List<CatalogEntry> getCatalogEntriesByCoordsAndTpm(ProperMotionQuery catalogEntry) throws IOException {
		if (catalogEntry instanceof UkidssCatalogEntry entry) {
			List<CatalogEntry> catalogEntries = new ArrayList();
			for (Survey survey : UkidssCatalogEntry.Survey.motionSurveys()) {
				entry.setSurvey(survey);
				catalogEntries.addAll(transformResponseToCatalogEntries(
						readResponse(establishHttpConnection(entry.getMotionQueryUrl()), entry.getCatalogName()),
						entry));
			}
			return catalogEntries;
		} else if (catalogEntry instanceof UhsCatalogEntry entry) {
			return entry.filterCatalogEntries(); 
		}
		return transformResponseToCatalogEntries(
				readResponse(establishHttpConnection(catalogEntry.getMotionQueryUrl()), catalogEntry.getCatalogName()),
				catalogEntry);
	}

}
