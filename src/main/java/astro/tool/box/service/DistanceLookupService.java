package astro.tool.box.service;

import static astro.tool.box.function.PhotometricFunctions.*;
import astro.tool.box.container.lookup.BrownDwarfLookupEntry;
import astro.tool.box.container.lookup.DistanceLookupResult;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.enumeration.Band;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DistanceLookupService {

    private final List<SpectralTypeLookup> entries;

    public DistanceLookupService(List<SpectralTypeLookup> entries) {
        this.entries = entries;
    }

    public List<DistanceLookupResult> lookup(String spt, Map<Band, Double> apparentMagnitudes) {
        List<DistanceLookupResult> results = new ArrayList<>();
        if (spt == null || apparentMagnitudes.isEmpty()) {
            return results;
        }
        Map<Band, Double> absoluteMagnitudes = null;
        for (SpectralTypeLookup lookupEntry : entries) {
            BrownDwarfLookupEntry entry = (BrownDwarfLookupEntry) lookupEntry;
            if (entry.getSpt().equals(spt)) {
                absoluteMagnitudes = entry.getBands();
            }
        }
        if (absoluteMagnitudes == null) {
            return results;
        }
        for (Map.Entry<Band, Double> entry : apparentMagnitudes.entrySet()) {
            double apparentMagnitude = entry.getValue();
            if (apparentMagnitude == 0) {
                continue;
            }
            double absoluteMagnitude = absoluteMagnitudes.get(entry.getKey());
            if (absoluteMagnitude == 0) {
                continue;
            }
            double distance = calculateDistanceFromMagnitudes(apparentMagnitude, absoluteMagnitude);
            results.add(new DistanceLookupResult(entry.getKey(), entry.getValue(), spt, distance));
        }
        return results;
    }

}
