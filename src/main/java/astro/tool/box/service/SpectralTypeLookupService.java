package astro.tool.box.service;

import static astro.tool.box.function.PhotometricFunctions.evaluateSpectralType;
import static astro.tool.box.util.MiscUtils.SPECTRAL_TYPES;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import astro.tool.box.enumeration.Color;
import astro.tool.box.lookup.LookupResult;
import astro.tool.box.lookup.SpectralTypeLookup;

public class SpectralTypeLookupService {

	private final List<SpectralTypeLookup> entries;

	public SpectralTypeLookupService(List<SpectralTypeLookup> entries) {
		this.entries = entries;
	}

	public List<LookupResult> lookup(Map<Color, Double> colors) {
		List<LookupResult> results = new ArrayList<>();
		SpectralTypeLookup minEntry = entries.get(0);
		for (SpectralTypeLookup maxEntry : entries) {
			for (Entry<Color, Double> color : colors.entrySet()) {
				Color colorKey = color.getKey();
				Double colorValue = color.getValue();
				LookupResult result = evaluateSpectralType(colorKey, colorValue, minEntry, maxEntry);
				if (result != null) {
					Double sptNum = SPECTRAL_TYPES.get(result.getSpt().replace("V", ""));
					result.setSptNum(sptNum);
					results.add(result);
				}
			}
			minEntry = maxEntry;
		}
		return results.stream().distinct().sorted(Comparator.comparing(LookupResult::getSptNum))
				.collect(Collectors.toList());
	}

}
