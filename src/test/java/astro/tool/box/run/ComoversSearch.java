package astro.tool.box.run;

import astro.tool.box.container.NumberPair;
import static astro.tool.box.function.AstrometricFunctions.calculateAngularDistance;
import static astro.tool.box.function.NumericFunctions.*;
import astro.tool.box.util.CSVParser;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.DEG_ARCSEC;
import static astro.tool.box.util.ServiceProviderUtils.establishHttpConnection;
import static astro.tool.box.util.ServiceProviderUtils.readResponse;
import static astro.tool.box.util.Utils.addRow;
import static astro.tool.box.util.Utils.encodeQuery;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.junit.Ignore;
import org.junit.Test;

public class ComoversSearch {

    //@Ignore
    @Test
    public void noirlabComovers() throws Exception {
        int totalRead = 0;
        int totalWritten = 0;
        StringBuilder results = new StringBuilder();
        try (Scanner fileScanner = new Scanner(new File("C:/Users/wcq637/Documents/Private/BYW/NSC DR2/nscdr2Z.csv"))) {
            String headerLine = fileScanner.nextLine();
            results.append(headerLine).append(LINE_SEP);
            String[] headers = CSVParser.parseLine(headerLine);
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i], i);
            }

            while (fileScanner.hasNextLine()) {
                totalRead++;
                if (totalRead % 100 == 0) {
                    System.out.println("read   =" + totalRead);
                    System.out.println("written=" + totalWritten);
                }
                String bodyLine = fileScanner.nextLine();
                String[] values = CSVParser.parseLine(bodyLine);
                double ra = toDouble(values[columns.get("ra")]);
                double dec = toDouble(values[columns.get("dec")]);
                double pmra = toDouble(values[columns.get("pmra")]);
                double pmdec = toDouble(values[columns.get("pmdec")]);

                String comoverQuery = createNoirlabComoverQuery();
                comoverQuery = comoverQuery.replace("[RA]", roundTo7DecNZ(ra));
                comoverQuery = comoverQuery.replace("[DE]", roundTo7DecNZ(dec));
                comoverQuery = comoverQuery.replace("[PMRA]", roundTo3DecNZ(pmra));
                comoverQuery = comoverQuery.replace("[PMDE]", roundTo3DecNZ(pmdec));
                String queryUrl = NOAO_TAP_URL + encodeQuery(comoverQuery);
                String response = readResponse(establishHttpConnection(queryUrl), "NSC DR2");

                try (Scanner responseScanner = new Scanner(response)) {
                    responseScanner.nextLine();
                    while (responseScanner.hasNextLine()) {
                        String resultLine = responseScanner.nextLine();
                        String[] resultValues = resultLine.split(SPLIT_CHAR, -1);
                        double resultRa = toDouble(resultValues[1]);
                        double resultDec = toDouble(resultValues[3]);
                        double distance = calculateAngularDistance(new NumberPair(ra, dec), new NumberPair(resultRa, resultDec), DEG_ARCSEC);
                        if (distance > 1) {
                            results.append(bodyLine).append(LINE_SEP);
                            totalWritten++;
                        }
                    }
                }
            }
        }
        File resultFile = new File("C:/Users/wcq637/Documents/Private/BYW/Co-movers/Results NSC DR2 Z.csv");
        try (FileWriter writer = new FileWriter(resultFile)) {
            writer.write(results.toString());
        }
        System.out.println("totalRead   =" + totalRead);
        System.out.println("totalWritten=" + totalWritten);
    }

    @Ignore
    @Test
    public void catwise() throws Exception {
        int totalRead = 0;
        int totalWritten = 0;
        StringBuilder results = new StringBuilder();
        try (Scanner fileScanner = new Scanner(new File("C:/Users/wcq637/Documents/Private/BYW/Co-movers/CatWISE results.csv"))) {
            String headerLine = fileScanner.nextLine();
            results.append(headerLine).append(LINE_SEP);
            String[] headers = CSVParser.parseLine(headerLine);
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i], i);
            }

            while (fileScanner.hasNextLine()) {
                totalRead++;
                if (totalRead % 100 == 0) {
                    System.out.println("read   =" + totalRead);
                    System.out.println("written=" + totalWritten);
                }
                String bodyLine = fileScanner.nextLine();
                String[] values = CSVParser.parseLine(bodyLine);
                double ra = toDouble(values[columns.get("ra")]);
                double dec = toDouble(values[columns.get("dec")]);
                double pmra = toDouble(values[columns.get("pmra")]);
                double pmdec = toDouble(values[columns.get("pmdec")]);

                String comoverQuery = createComoverWdQuery();
                comoverQuery = comoverQuery.replace("[RA]", roundTo7DecNZ(ra));
                comoverQuery = comoverQuery.replace("[DE]", roundTo7DecNZ(dec));
                comoverQuery = comoverQuery.replace("[PMRA]", roundTo3DecNZ(pmra));
                comoverQuery = comoverQuery.replace("[PMDE]", roundTo3DecNZ(pmdec));
                String queryUrl = ESAC_TAP_URL + encodeQuery(comoverQuery);
                String response = readResponse(establishHttpConnection(queryUrl), "Gaia eDR3");

                try (Scanner responseScanner = new Scanner(response)) {
                    responseScanner.nextLine();
                    while (responseScanner.hasNextLine()) {
                        String resultLine = responseScanner.nextLine();
                        String[] resultValues = resultLine.split(SPLIT_CHAR, -1);
                        double resultRa = toDouble(resultValues[1]);
                        double resultDec = toDouble(resultValues[2]);
                        double distance = calculateAngularDistance(new NumberPair(ra, dec), new NumberPair(resultRa, resultDec), DEG_ARCSEC);
                        if (distance > 1) {
                            results.append(bodyLine).append(LINE_SEP);
                            totalWritten++;
                        }
                    }
                }
            }
        }
        File resultFile = new File("C:/Users/wcq637/Documents/Private/BYW/Co-movers/results catwise.csv");
        try (FileWriter writer = new FileWriter(resultFile)) {
            writer.write(results.toString());
        }
        System.out.println("totalRead   =" + totalRead);
        System.out.println("totalWritten=" + totalWritten);
    }

    @Ignore
    @Test
    public void noirlab() throws Exception {
        int totalRead = 0;
        int totalWritten = 0;
        StringBuilder results = new StringBuilder();
        try (Scanner fileScanner = new Scanner(new File("C:/Users/wcq637/Documents/Private/BYW/NSC DR2/nscdr2N.csv"))) {
            String headerLine = fileScanner.nextLine();
            results.append(headerLine).append(LINE_SEP);
            String[] headers = CSVParser.parseLine(headerLine);
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i], i);
            }

            while (fileScanner.hasNextLine()) {
                totalRead++;
                if (totalRead % 100 == 0) {
                    System.out.println("read   =" + totalRead);
                    System.out.println("written=" + totalWritten);
                }
                String bodyLine = fileScanner.nextLine();
                String[] values = CSVParser.parseLine(bodyLine);
                double ra = toDouble(values[columns.get("ra")]);
                double dec = toDouble(values[columns.get("dec")]);
                double pmra = toDouble(values[columns.get("pmra")]);
                double pmdec = toDouble(values[columns.get("pmdec")]);

                String comoverQuery = createComoverWdQuery();
                comoverQuery = comoverQuery.replace("[RA]", roundTo7DecNZ(ra));
                comoverQuery = comoverQuery.replace("[DE]", roundTo7DecNZ(dec));
                comoverQuery = comoverQuery.replace("[PMRA]", roundTo3DecNZ(pmra));
                comoverQuery = comoverQuery.replace("[PMDE]", roundTo3DecNZ(pmdec));
                String queryUrl = ESAC_TAP_URL + encodeQuery(comoverQuery);
                String response = readResponse(establishHttpConnection(queryUrl), "Gaia eDR3");

                try (Scanner responseScanner = new Scanner(response)) {
                    responseScanner.nextLine();
                    while (responseScanner.hasNextLine()) {
                        String resultLine = responseScanner.nextLine();
                        String[] resultValues = resultLine.split(SPLIT_CHAR, -1);
                        double resultRa = toDouble(resultValues[1]);
                        double resultDec = toDouble(resultValues[2]);
                        double distance = calculateAngularDistance(new NumberPair(ra, dec), new NumberPair(resultRa, resultDec), DEG_ARCSEC);
                        if (distance > 1) {
                            results.append(bodyLine).append(LINE_SEP);
                            totalWritten++;
                        }
                    }
                }
            }
        }
        File resultFile = new File("C:/Users/wcq637/Documents/Private/BYW/Co-movers/results NSC DR2.csv");
        try (FileWriter writer = new FileWriter(resultFile)) {
            writer.write(results.toString());
        }
        System.out.println("totalRead   =" + totalRead);
        System.out.println("totalWritten=" + totalWritten);
    }

    @Ignore
    @Test
    public void classifier() throws Exception {
        int totalRead = 0;
        int totalWritten = 0;
        StringBuilder results = new StringBuilder();
        try (Scanner fileScanner = new Scanner(new File("C:/Users/wcq637/Documents/Private/BYW/Co-movers//CW2020 BYW Classifier Part 1.csv"))) {
            String headerLine = fileScanner.nextLine();
            results.append(headerLine).append(LINE_SEP);
            String[] headers = CSVParser.parseLine(headerLine);
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i], i);
            }

            while (fileScanner.hasNextLine()) {
                totalRead++;
                if (totalRead % 100 == 0) {
                    System.out.println("read   =" + totalRead);
                    System.out.println("written=" + totalWritten);
                }
                String bodyLine = fileScanner.nextLine();
                String[] values = CSVParser.parseLine(bodyLine);
                double ra = toDouble(values[columns.get("ra")]);
                double dec = toDouble(values[columns.get("dec")]);
                double pmra = toDouble(values[columns.get("PMRA")]) * 1000;
                double pmdec = toDouble(values[columns.get("PMDec")]) * 1000;

                String comoverQuery = createComoverWdQuery();
                comoverQuery = comoverQuery.replace("[RA]", roundTo7DecNZ(ra));
                comoverQuery = comoverQuery.replace("[DE]", roundTo7DecNZ(dec));
                comoverQuery = comoverQuery.replace("[PMRA]", roundTo3DecNZ(pmra));
                comoverQuery = comoverQuery.replace("[PMDE]", roundTo3DecNZ(pmdec));
                String queryUrl = ESAC_TAP_URL + encodeQuery(comoverQuery);
                String response = readResponse(establishHttpConnection(queryUrl), "Gaia eDR3");

                try (Scanner responseScanner = new Scanner(response)) {
                    responseScanner.nextLine();
                    while (responseScanner.hasNextLine()) {
                        String resultLine = responseScanner.nextLine();
                        String[] resultValues = resultLine.split(SPLIT_CHAR, -1);
                        double resultRa = toDouble(resultValues[1]);
                        double resultDec = toDouble(resultValues[2]);
                        double distance = calculateAngularDistance(new NumberPair(ra, dec), new NumberPair(resultRa, resultDec), DEG_ARCSEC);
                        if (distance > 1) {
                            results.append(bodyLine).append(LINE_SEP);
                            totalWritten++;
                        }
                    }
                }
            }
        }
        File resultFile = new File("C:/Users/wcq637/Documents/Private/BYW/Co-movers/Results WD part 1.csv");
        try (FileWriter writer = new FileWriter(resultFile)) {
            writer.write(results.toString());
        }
        System.out.println("totalRead   =" + totalRead);
        System.out.println("totalWritten=" + totalWritten);
    }

    @Ignore
    @Test
    public void test() throws Exception {
        int totalRead = 0;
        int totalWritten = 0;
        StringBuilder test = new StringBuilder();
        test.append("ra,dec,pmra,pmdec").append(LINE_SEP);
        test.append("353.2612406,-63.7885657,44.617,-156.7").append(LINE_SEP);
        test.append("28.7563406,-13.1309493,109.075,-128.078").append(LINE_SEP);
        test.append("60.1940385,-26.1918167,-58.654,-110.718").append(LINE_SEP);
        StringBuilder results = new StringBuilder();
        try (Scanner fileScanner = new Scanner(test.toString())) {
            String headerLine = fileScanner.nextLine();
            results.append(headerLine).append(LINE_SEP);
            String[] headers = CSVParser.parseLine(headerLine);
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i], i);
            }

            while (fileScanner.hasNextLine()) {
                totalRead++;
                if (totalRead % 100 == 0) {
                    System.out.println("read   =" + totalRead);
                    System.out.println("written=" + totalWritten);
                }
                String bodyLine = fileScanner.nextLine();
                String[] values = CSVParser.parseLine(bodyLine);
                double ra = toDouble(values[columns.get("ra")]);
                double dec = toDouble(values[columns.get("dec")]);
                double pmra = toDouble(values[columns.get("pmra")]);
                double pmdec = toDouble(values[columns.get("pmdec")]);

                String comoverQuery = createComoverWdQuery();
                comoverQuery = comoverQuery.replace("[RA]", roundTo7DecNZ(ra));
                comoverQuery = comoverQuery.replace("[DE]", roundTo7DecNZ(dec));
                comoverQuery = comoverQuery.replace("[PMRA]", roundTo3DecNZ(pmra));
                comoverQuery = comoverQuery.replace("[PMDE]", roundTo3DecNZ(pmdec));
                String queryUrl = ESAC_TAP_URL + encodeQuery(comoverQuery);
                String response = readResponse(establishHttpConnection(queryUrl), "Gaia eDR3");

                try (Scanner responseScanner = new Scanner(response)) {
                    responseScanner.nextLine();
                    while (responseScanner.hasNextLine()) {
                        String resultLine = responseScanner.nextLine();
                        String[] resultValues = resultLine.split(SPLIT_CHAR, -1);
                        double resultRa = toDouble(resultValues[1]);
                        double resultDec = toDouble(resultValues[2]);
                        double distance = calculateAngularDistance(new NumberPair(ra, dec), new NumberPair(resultRa, resultDec), DEG_ARCSEC);
                        if (distance > 1) {
                            results.append(bodyLine).append(LINE_SEP);
                            totalWritten++;
                        }
                    }
                }
            }
        }
        File resultFile = new File("C:/Users/wcq637/Documents/Private/BYW/Co-movers/results.csv");
        try (FileWriter writer = new FileWriter(resultFile)) {
            writer.write(results.toString());
        }
        System.out.println("totalRead   =" + totalRead);
        System.out.println("totalWritten=" + totalWritten);
    }

    private String createNoirlabComoverQuery() {
        StringBuilder query = new StringBuilder();
        addRow(query, "SELECT id,");
        addRow(query, "       ra,");
        addRow(query, "       raerr,");
        addRow(query, "       dec,");
        addRow(query, "       decerr,");
        addRow(query, "       pmra,");
        addRow(query, "       pmraerr,");
        addRow(query, "       pmdec,");
        addRow(query, "       pmdecerr,");
        addRow(query, "       class_star,");
        addRow(query, "       mjd,");
        addRow(query, "       ndet,");
        addRow(query, "       deltamjd,");
        addRow(query, "       umag,");
        addRow(query, "       uerr,");
        addRow(query, "       gmag,");
        addRow(query, "       gerr,");
        addRow(query, "       rmag,");
        addRow(query, "       rerr,");
        addRow(query, "       imag,");
        addRow(query, "       ierr,");
        addRow(query, "       zmag,");
        addRow(query, "       zerr,");
        addRow(query, "       ymag,");
        addRow(query, "       yerr,");
        addRow(query, "       vrmag,");
        addRow(query, "       vrerr");
        addRow(query, "FROM   nsc_dr2.object");
        addRow(query, "WHERE  't'=q3c_radial_query(ra, dec, [RA], [DE], 0.002777778)"); // 10 arcsec
        addRow(query, "AND    pmra <> 'NaN' AND pmdec <> 'NaN'");
        addRow(query, "AND   (pmra  BETWEEN [PMRA] - ABS([PMRA]) * 0.3 AND [PMRA] + ABS([PMRA]) * 0.3");
        addRow(query, "AND    pmdec BETWEEN [PMDE] - ABS([PMDE]) * 0.3 AND [PMDE] + ABS([PMDE]) * 0.3)");
        //addRow(query, "AND    SQRT(pmra * pmra + pmdec * pmdec) >= 100");
        return query.toString();
    }

    private String createComoverQuery() {
        StringBuilder query = new StringBuilder();
        addRow(query, "SELECT source_id,");
        addRow(query, "       ra,");
        addRow(query, "       dec,");
        addRow(query, "       parallax,");
        addRow(query, "       parallax_error,");
        addRow(query, "       pmra,");
        addRow(query, "       pmra_error,");
        addRow(query, "       pmdec,");
        addRow(query, "       pmdec_error,");
        addRow(query, "       phot_g_mean_mag,");
        addRow(query, "       phot_bp_mean_mag,");
        addRow(query, "       phot_rp_mean_mag,");
        addRow(query, "       bp_rp,");
        addRow(query, "       bp_g,");
        addRow(query, "       g_rp,");
        addRow(query, "       dr2_radial_velocity,");
        addRow(query, "       dr2_radial_velocity_error");
        addRow(query, "FROM   gaiaedr3.gaia_source");
        addRow(query, "WHERE  1=CONTAINS(POINT('ICRS', ra, dec), CIRCLE('ICRS', [RA], [DE], 0.002777778))"); // 10 arcsec
        addRow(query, "AND   (pmra  BETWEEN [PMRA] - ABS([PMRA]) * 0.3 AND [PMRA] + ABS([PMRA]) * 0.3");
        addRow(query, "AND    pmdec BETWEEN [PMDE] - ABS([PMDE]) * 0.3 AND [PMDE] + ABS([PMDE]) * 0.3)");
        //addRow(query, "AND    SQRT(pmra * pmra + pmdec * pmdec) >= 100");
        return query.toString();
    }

    private String createComoverWdQuery() {
        StringBuilder query = new StringBuilder();
        addRow(query, "SELECT source_id,");
        addRow(query, "       ra,");
        addRow(query, "       dec,");
        addRow(query, "       parallax,");
        addRow(query, "       parallax_error,");
        addRow(query, "       pmra,");
        addRow(query, "       pmra_error,");
        addRow(query, "       pmdec,");
        addRow(query, "       pmdec_error,");
        addRow(query, "       phot_g_mean_mag,");
        addRow(query, "       phot_bp_mean_mag,");
        addRow(query, "       phot_rp_mean_mag,");
        addRow(query, "       bp_rp,");
        addRow(query, "       bp_g,");
        addRow(query, "       g_rp,");
        addRow(query, "       dr2_radial_velocity,");
        addRow(query, "       dr2_radial_velocity_error");
        addRow(query, "FROM   gaiaedr3.gaia_source");
        addRow(query, "WHERE  1=CONTAINS(POINT('ICRS', ra, dec), CIRCLE('ICRS', [RA], [DE], 0.002777778))"); // 10 arcsec
        addRow(query, "AND   (pmra  BETWEEN [PMRA] - ABS([PMRA]) * 0.3 AND [PMRA] + ABS([PMRA]) * 0.3");
        addRow(query, "AND    pmdec BETWEEN [PMDE] - ABS([PMDE]) * 0.3 AND [PMDE] + ABS([PMDE]) * 0.3)");
        addRow(query, "AND    bp_rp != 0 AND bp_rp <= 1.5");
        addRow(query, "AND    phot_g_mean_mag + 5 - 5 * LOG10(1 / (parallax / 1000)) BETWEEN 10 AND 15");
        return query.toString();
    }

}