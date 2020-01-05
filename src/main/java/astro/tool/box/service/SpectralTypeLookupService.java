package astro.tool.box.service;

import static astro.tool.box.function.PhotometricFunctions.*;
import astro.tool.box.container.ColorValue;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
import astro.tool.box.enumeration.Color;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SpectralTypeLookupService {

    private final List<SpectralTypeLookup> entries;

    public SpectralTypeLookupService(List<SpectralTypeLookup> entries) {
        this.entries = entries;
    }

    public Map<SpectralTypeLookupResult, Set<ColorValue>> lookup(Map<Color, Double> colors) {
        Map<SpectralTypeLookupResult, Set<ColorValue>> results = new LinkedHashMap<>();
        SpectralTypeLookup minEntry = entries.get(0);
        for (SpectralTypeLookup maxEntry : entries) {
            for (Entry<Color, Double> color : colors.entrySet()) {
                Color colorKey = color.getKey();
                Double colorValue = color.getValue();
                SpectralTypeLookupResult result = evaluateSpectralType(colorKey, colorValue, minEntry, maxEntry);
                if (result != null) {
                    Set<ColorValue> colorKeys = results.get(result);
                    if (colorKeys == null) {
                        colorKeys = new LinkedHashSet<>();
                    }
                    colorKeys.add(new ColorValue(colorKey, colorValue));
                    results.put(result, colorKeys);
                }
            }
            minEntry = maxEntry;
        }
        return results;
    }

    public List<SpectralTypeLookup> getEntries() {
        return entries;
    }

}
