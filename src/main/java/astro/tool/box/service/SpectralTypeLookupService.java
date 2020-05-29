package astro.tool.box.service;

import static astro.tool.box.function.PhotometricFunctions.*;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
import astro.tool.box.enumeration.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SpectralTypeLookupService {

    private final List<SpectralTypeLookup> entries;

    public SpectralTypeLookupService(List<SpectralTypeLookup> entries) {
        this.entries = entries;
    }

    public List<SpectralTypeLookupResult> lookup(Map<Color, Double> colors) {
        List<SpectralTypeLookupResult> results = new ArrayList<>();
        SpectralTypeLookup minEntry = entries.get(0);
        for (SpectralTypeLookup maxEntry : entries) {
            for (Entry<Color, Double> color : colors.entrySet()) {
                Color colorKey = color.getKey();
                Double colorValue = color.getValue();
                SpectralTypeLookupResult result = evaluateSpectralType(colorKey, colorValue, minEntry, maxEntry);
                if (result != null) {
                    results.add(result);
                }
            }
            minEntry = maxEntry;
        }
        return results.stream().distinct().collect(Collectors.toList());
    }

    public List<SpectralTypeLookupResult> lookupTeff(Map<Color, Double> colors, double logG, double msun) {
        List<SpectralTypeLookupResult> results = new ArrayList<>();
        SpectralTypeLookup minEntry = entries.get(0);
        for (SpectralTypeLookup maxEntry : entries) {
            for (Entry<Color, Double> color : colors.entrySet()) {
                Color colorKey = color.getKey();
                Double colorValue = color.getValue();
                SpectralTypeLookupResult result = evaluateTemperature(colorKey, colorValue, logG, msun, minEntry, maxEntry);
                if (result != null) {
                    results.add(result);
                }
            }
            minEntry = maxEntry;
        }
        return results.stream().distinct().collect(Collectors.toList());
    }

    public List<SpectralTypeLookup> getEntries() {
        return entries;
    }

}
