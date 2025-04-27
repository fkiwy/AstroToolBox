package astro.tool.box.service;

import static astro.tool.box.function.PhotometricFunctions.calculatePhotometricDistance;
import static astro.tool.box.function.PhotometricFunctions.calculatePhotometricDistanceError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Band;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.DistanceLookupResult;
import astro.tool.box.lookup.SpectralTypeLookup;

public class DistanceLookupService {

	private final List<SpectralTypeLookup> entries;

	public DistanceLookupService(List<SpectralTypeLookup> entries) {
		this.entries = entries;
	}

	public List<DistanceLookupResult> lookup(String spt, Map<Band, NumberPair> apparentMagnitudes) {
		List<DistanceLookupResult> results = new ArrayList<>();
		if (spt == null || apparentMagnitudes.isEmpty()) {
			return results;
		}
		spt = spt.replace("V", "");
		Map<Band, Double> absoluteMagnitudes = null;
		Map<Band, Double> absoluteMagnitudesErrors = null;
		for (SpectralTypeLookup lookupEntry : entries) {
			BrownDwarfLookupEntry entry = (BrownDwarfLookupEntry) lookupEntry;
			if (entry.getSpt().equals(spt)) {
				absoluteMagnitudes = entry.getMagnitudes();
				absoluteMagnitudesErrors = entry.getErrors();
			}
		}
		if (absoluteMagnitudes == null) {
			return results;
		}
		for (Map.Entry<Band, NumberPair> entry : apparentMagnitudes.entrySet()) {
			double apparentMagnitude = entry.getValue().getX();
			if (apparentMagnitude == 0) {
				continue;
			}
			double absoluteMagnitude = absoluteMagnitudes.get(entry.getKey());
			if (absoluteMagnitude == 0) {
				continue;
			}
			double distance = calculatePhotometricDistance(apparentMagnitude, absoluteMagnitude);
			double apparentMagnitudeError = entry.getValue().getY();
			double absoluteMagnitudeError = absoluteMagnitudesErrors.get(entry.getKey());
			if (absoluteMagnitudeError == 0) {
				absoluteMagnitudeError = 0.5;
			}
			double distanceError;
			if (apparentMagnitudeError == 0) {
				distanceError = 0;
			} else {
				distanceError = calculatePhotometricDistanceError(apparentMagnitude, apparentMagnitudeError,
						absoluteMagnitude, absoluteMagnitudeError);
			}
			results.add(new DistanceLookupResult(entry.getKey(), apparentMagnitude, spt, distance, distanceError));
		}
		return results;
	}

}
