package astro.tool.box.catalog;

import java.util.Map;

import astro.tool.box.exception.ExtinctionException;

public interface Extinction extends CatalogEntry {

	void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) throws ExtinctionException;

}
