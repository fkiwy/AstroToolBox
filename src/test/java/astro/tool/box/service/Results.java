package astro.tool.box.service;

import astro.tool.box.container.lookup.LookupResult;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupEntry;
import astro.tool.box.enumeration.Color;
import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Constants.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class Results {

    private static final Map<String, Double> SPECTRAL_TYPES = new HashMap<>();

    static {
        SPECTRAL_TYPES.put("A0V", 0.0);
        SPECTRAL_TYPES.put("A0V", 0.0);
        SPECTRAL_TYPES.put("A1V", 1.0);
        SPECTRAL_TYPES.put("A2V", 2.0);
        SPECTRAL_TYPES.put("A3V", 3.0);
        SPECTRAL_TYPES.put("A4V", 4.0);
        SPECTRAL_TYPES.put("A5V", 5.0);
        SPECTRAL_TYPES.put("A6V", 6.0);
        SPECTRAL_TYPES.put("A7V", 7.0);
        SPECTRAL_TYPES.put("A8V", 8.0);
        SPECTRAL_TYPES.put("A9V", 9.0);
        SPECTRAL_TYPES.put("F0V", 10.0);
        SPECTRAL_TYPES.put("F1V", 11.0);
        SPECTRAL_TYPES.put("F2V", 12.0);
        SPECTRAL_TYPES.put("F3V", 13.0);
        SPECTRAL_TYPES.put("F4V", 14.0);
        SPECTRAL_TYPES.put("F5V", 15.0);
        SPECTRAL_TYPES.put("F6V", 16.0);
        SPECTRAL_TYPES.put("F7V", 17.0);
        SPECTRAL_TYPES.put("F8V", 18.0);
        SPECTRAL_TYPES.put("F9V", 19.0);
        SPECTRAL_TYPES.put("F9.5V", 19.5);
        SPECTRAL_TYPES.put("G0V", 20.0);
        SPECTRAL_TYPES.put("G1V", 21.0);
        SPECTRAL_TYPES.put("G2V", 22.0);
        SPECTRAL_TYPES.put("G3V", 23.0);
        SPECTRAL_TYPES.put("G4V", 24.0);
        SPECTRAL_TYPES.put("G5V", 25.0);
        SPECTRAL_TYPES.put("G6V", 26.0);
        SPECTRAL_TYPES.put("G7V", 27.0);
        SPECTRAL_TYPES.put("G8V", 28.0);
        SPECTRAL_TYPES.put("G9V", 29.0);
        SPECTRAL_TYPES.put("K0V", 30.0);
        SPECTRAL_TYPES.put("K0.5V", 30.5);
        SPECTRAL_TYPES.put("K1V", 31.0);
        SPECTRAL_TYPES.put("K1.5V", 31.5);
        SPECTRAL_TYPES.put("K2V", 32.0);
        SPECTRAL_TYPES.put("K2.5V", 32.5);
        SPECTRAL_TYPES.put("K3V", 33.0);
        SPECTRAL_TYPES.put("K3.5V", 33.5);
        SPECTRAL_TYPES.put("K4V", 34.0);
        SPECTRAL_TYPES.put("K4.5V", 34.5);
        SPECTRAL_TYPES.put("K5V", 35.0);
        SPECTRAL_TYPES.put("K5.5V", 35.5);
        SPECTRAL_TYPES.put("K6V", 36.0);
        SPECTRAL_TYPES.put("K6.5V", 36.5);
        SPECTRAL_TYPES.put("K7V", 37.0);
        SPECTRAL_TYPES.put("K7.5V", 37.5);
        SPECTRAL_TYPES.put("K8V", 38.0);
        SPECTRAL_TYPES.put("K8.5V", 38.5);
        SPECTRAL_TYPES.put("K9V", 39.0);
        SPECTRAL_TYPES.put("K9.5V", 39.5);
        SPECTRAL_TYPES.put("M0V", 40.0);
        SPECTRAL_TYPES.put("M0.5V", 40.5);
        SPECTRAL_TYPES.put("M1V", 41.0);
        SPECTRAL_TYPES.put("M1.5V", 41.5);
        SPECTRAL_TYPES.put("M2V", 42.0);
        SPECTRAL_TYPES.put("M2.5V", 42.5);
        SPECTRAL_TYPES.put("M3V", 43.0);
        SPECTRAL_TYPES.put("M3.5V", 43.5);
        SPECTRAL_TYPES.put("M4V", 44.0);
        SPECTRAL_TYPES.put("M4.5V", 44.5);
        SPECTRAL_TYPES.put("M5V", 45.0);
        SPECTRAL_TYPES.put("M5.5V", 45.5);
        SPECTRAL_TYPES.put("M6V", 46.0);
        SPECTRAL_TYPES.put("M6.5V", 46.5);
        SPECTRAL_TYPES.put("M7V", 47.0);
        SPECTRAL_TYPES.put("M7.5V", 47.5);
        SPECTRAL_TYPES.put("M8V", 48.0);
        SPECTRAL_TYPES.put("M8.5V", 48.5);
        SPECTRAL_TYPES.put("M9V", 49.0);
        SPECTRAL_TYPES.put("M9.5V", 49.5);
        SPECTRAL_TYPES.put("L0V", 50.0);
        SPECTRAL_TYPES.put("L1V", 51.0);
        SPECTRAL_TYPES.put("L2V", 52.0);
        SPECTRAL_TYPES.put("L3V", 53.0);
        SPECTRAL_TYPES.put("L4V", 54.0);
        SPECTRAL_TYPES.put("L5V", 55.0);
        SPECTRAL_TYPES.put("L6V", 56.0);
        SPECTRAL_TYPES.put("L7V", 57.0);
        SPECTRAL_TYPES.put("L8V", 58.0);
        SPECTRAL_TYPES.put("L9V", 59.0);
    }

    @Test
    public void test() throws Exception {
        // 0 angDist
        // 1 RA (BYW)
        // 2 Dec (BYW)
        // 3 Duplicate
        // 4 Classification
        // 5 ra
        // 6 dec
        // 7 parallax
        // 8 parallax_error
        // 9 pmra
        //10 pmra_error
        //11 pmdec
        //12 pmdec_error
        //13 phot_g_mean_mag
        //14 phot_bp_mean_mag
        //15 phot_rp_mean_mag
        //16 bp_rp
        //17 radial_velocity
        //18 radial_velocity_error
        //19 teff_val
        //20 radius_val
        //21 lum_val

        SpectralTypeLookupService spectralTypeLookupService;
        InputStream input = getClass().getResourceAsStream("/SpectralTypeLookupTable.csv");
        try (Stream<String> stream = new BufferedReader(new InputStreamReader(input)).lines()) {
            List<SpectralTypeLookup> entries = stream.skip(1).map(line -> {
                return new SpectralTypeLookupEntry(line.split(SPLIT_CHAR, 30));
            }).collect(Collectors.toList());
            spectralTypeLookupService = new SpectralTypeLookupService(entries);
        }

        StringBuilder results = new StringBuilder();
        Map<String, Integer> spectralTypeCount = new TreeMap<>();
        try (Scanner scanner = new Scanner(new File("c:/temp/BYW GPS x Gaia DR2.csv"))) {
            String line = scanner.nextLine();
            results.append(line).append(",dist,tpm,vtan,spt").append(LINE_SEP);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                String[] values = line.split(SPLIT_CHAR, 22);
                double ra = toDouble(values[5]);
                double dec = toDouble(values[6]);
                if (ra == 0 && dec == 0) {
                    continue;
                }
                double plx = toDouble(values[7]);
                double pmra = toDouble(values[9]);
                double pmdec = toDouble(values[11]);
                double Gmag = toDouble(values[13]);
                double BPmag = toDouble(values[14]);
                double RPmag = toDouble(values[15]);
                double G_RP;
                if (Gmag == 0 || RPmag == 0) {
                    G_RP = 0;
                } else {
                    G_RP = Gmag - RPmag;
                }
                double BP_RP;
                if (BPmag == 0 || RPmag == 0) {
                    BP_RP = 0;
                } else {
                    BP_RP = BPmag - RPmag;
                }
                Map<Color, Double> colors = new LinkedHashMap<>();
                colors.put(Color.G_RP, G_RP);
                colors.put(Color.BP_RP, BP_RP);
                List<LookupResult> list = spectralTypeLookupService.lookup(colors);
                String spt = "";
                for (LookupResult entry : list) {
                    spt = entry.getSpt();
                }
                if (spt.isEmpty()) {
                    continue;
                }
                double dist = calculateActualDistance(plx);
                if (dist == 0 || dist > 1000) {
                    continue;
                }
                double tpm = calculateTotalProperMotion(pmra, pmdec);
                if (tpm < 30) {
                    continue;
                }
                double vtan = calculateTransverseVelocityFromParallax(pmra, pmdec, plx);
                Double sptNum = SPECTRAL_TYPES.get(spt);
                if (sptNum == null) {
                    continue;
                }
                Integer count = spectralTypeCount.get(spt);
                count = count == null ? 0 : count;
                spectralTypeCount.put(spt, count + 1);
                System.out.println("plx=" + plx + " dist=" + dist + " tpm=" + tpm + " vtan=" + vtan + " sptNum=" + sptNum);
                results.append(line).append(",").append(dist).append(",").append(tpm).append(",").append(vtan).append(",").append(roundTo1Dec(sptNum)).append(LINE_SEP);

            }
        }
        int total = 0;
        for (Map.Entry<String, Integer> entry : spectralTypeCount.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
            total += entry.getValue();
        }
        System.out.println("Total : " + total);
        File resultFile = new File("c:/temp/BYW Plane Search results.csv");
        try (FileWriter writer = new FileWriter(resultFile)) {
            writer.write(results.toString());
        }
    }

}
