package astro.tool.box.service;

import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.ColorValue;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
import astro.tool.box.enumeration.Color;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                return new SpectralTypeLookupEntry(line.split(SPLIT_CHAR, 30));
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
        Map<SpectralTypeLookupResult, Set<ColorValue>> results = spectralTypeLookupService.lookup(colors);
        System.out.println(results);
        assertEquals("{SpectralTypeLookupResult{spt=M5.5V, teff=3000, rsun=0.149, msun=0.12, nearest=1.91, gap=0.040000000000000036}=[ColorValue{color=B_V, value=1.95}], SpectralTypeLookupResult{spt=M6V, teff=2850, rsun=0.127, msun=0.1, nearest=3.95, gap=0.050000000000000266}=[ColorValue{color=BP_RP, value=3.9}], SpectralTypeLookupResult{spt=M6V, teff=2850, rsun=0.127, msun=0.1, nearest=1.45, gap=0.010000000000000009}=[ColorValue{color=G_RP, value=1.44}], SpectralTypeLookupResult{spt=M6V, teff=2850, rsun=0.127, msun=0.1, nearest=0.21, gap=0.0050000000000000044}=[ColorValue{color=W1_W2, value=0.205}]}", results.toString());
    }

}
