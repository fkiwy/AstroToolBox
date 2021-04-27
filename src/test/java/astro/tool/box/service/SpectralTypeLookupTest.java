package astro.tool.box.service;

import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.enumeration.Color;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import org.junit.Test;

public class SpectralTypeLookupTest {

    @Test
    public void lookupSpectralType() throws IOException {
        SpectralTypeLookupService spectralTypeLookupService;
        InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(SPLIT_CHAR, SpectralTypeLookupEntry.NUMBER_OF_COLUMNS));
            }).collect(Collectors.toList());
            spectralTypeLookupService = new SpectralTypeLookupService(entries);
        }
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.B_V, 1.950);
        colors.put(Color.BP_RP, 3.900);
        colors.put(Color.G_RP, 1.440);
        colors.put(Color.U_B, 1.320);
        colors.put(Color.J_H, 0.603);
        colors.put(Color.H_K, 0.350);
        colors.put(Color.W1_W2, 0.205);
        List<LookupResult> results = spectralTypeLookupService.lookup(colors);
        //System.out.println(results);
        assertEquals("[SpectralTypeLookupResult{colorKey=B_V, colorValue=1.95, spt=M5.5V, teff=3000, rsun=0.149, msun=0.12, logG=0.0, age=, nearest=1.91, gap=0.040000000000000036}, SpectralTypeLookupResult{colorKey=BP_RP, colorValue=3.9, spt=M6V, teff=2850, rsun=0.127, msun=0.1, logG=0.0, age=, nearest=3.95, gap=0.050000000000000266}, SpectralTypeLookupResult{colorKey=G_RP, colorValue=1.44, spt=M6V, teff=2850, rsun=0.127, msun=0.1, logG=0.0, age=, nearest=1.45, gap=0.010000000000000009}, SpectralTypeLookupResult{colorKey=W1_W2, colorValue=0.205, spt=M6V, teff=2850, rsun=0.127, msun=0.1, logG=0.0, age=, nearest=0.21, gap=0.0050000000000000044}]", results.toString());
    }

}
