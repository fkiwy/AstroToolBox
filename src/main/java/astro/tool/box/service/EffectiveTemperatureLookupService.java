package astro.tool.box.service;

import static astro.tool.box.function.PhotometricFunctions.*;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.enumeration.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class EffectiveTemperatureLookupService {

    private final List<SpectralTypeLookup> entries;

    public EffectiveTemperatureLookupService(List<SpectralTypeLookup> entries) {
        this.entries = entries;
    }

    public List<LookupResult> lookup(Map<Color, Double> colors, double teff, double logG, double msun) {
        List<LookupResult> results = new ArrayList<>();
        SpectralTypeLookup minEntry = entries.get(0);
        for (SpectralTypeLookup maxEntry : entries) {
            for (Entry<Color, Double> color : colors.entrySet()) {
                Color colorKey = color.getKey();
                Double colorValue = color.getValue();
                LookupResult result = evaluateTemperature(colorKey, colorValue, teff, logG, msun, minEntry, maxEntry);
                if (result != null) {
                    results.add(result);
                }
            }
            minEntry = maxEntry;
        }
        return results.stream().distinct().collect(Collectors.toList());
    }

}
