package astro.tool.box.catalog;

import astro.tool.box.exception.ExtinctionException;
import java.util.Map;

public interface Extinction extends CatalogEntry {

    void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) throws ExtinctionException;

}
