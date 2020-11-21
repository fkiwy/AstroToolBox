package astro.tool.box.function;

import astro.tool.box.util.ConversionFactors;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.StringPair;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import static org.junit.Assert.*;
import org.junit.Test;

public class AstrometricFunctionsTest {

    @Test
    public void calculateAngularDistance() {
        //allWISE RA dec: 194.3374822 71.8969964
        //catWISE RA dec: 194.3350473 71.8972063
        double angularDistance = AstrometricFunctions.calculateAngularDistance(
                new NumberPair(194.3374822, 71.8969964),
                new NumberPair(194.3350473, 71.8972063),
                ConversionFactors.DEG_MAS
        );
        assertEquals(Double.valueOf(2826.575566769283), Double.valueOf(angularDistance));
    }

    @Test
    public void calculateProperMotions() {
        //allWISE observation time: 55400
        //catWISE observation time: 56700
        NumberPair properMotions = AstrometricFunctions.calculateProperMotions(
                new NumberPair(194.3374822, 71.8969964),
                new NumberPair(194.3350473, 71.8972063),
                55400,
                56700,
                ConversionFactors.DEG_MAS
        );
        assertEquals(Double.valueOf(-764.7266590340704), Double.valueOf(properMotions.getX()));
        assertEquals(Double.valueOf(212.17590650534777), Double.valueOf(properMotions.getY()));
    }

    @Test
    public void calculateTotalProperMotion() {
        double totalProperMotion = AstrometricFunctions.calculateTotalProperMotion(-764.7309441403478, 212.16046152565366);
        assertEquals(Double.valueOf(793.6154474054586), Double.valueOf(totalProperMotion));
    }

    @Test
    public void calculateLinearDistance() {
        // Linear distance between Sirius and Vega
        double linearDistance = AstrometricFunctions.calculateLinearDistance(new NumberPair(101.2871553, -16.7161159), new NumberPair(279.2347348, 38.783689), 379.21, 130.23);
        assertEquals(Double.valueOf(10.170011231287699), Double.valueOf(linearDistance));
    }

    @Test
    public void convertToDecimalCoords() {
        NumberPair decimalCoords;

        decimalCoords = AstrometricFunctions.convertToDecimalCoords("12h 57m 21.00s", "+71d 53m 49.2s");
        assertEquals(Double.valueOf(194.3375), Double.valueOf(decimalCoords.getX()));
        assertEquals(Double.valueOf(71.897), Double.valueOf(decimalCoords.getY()));

        decimalCoords = AstrometricFunctions.convertToDecimalCoords("12h57m21.00s", "+71d53m49.2s");
        assertEquals(Double.valueOf(194.3375), Double.valueOf(decimalCoords.getX()));
        assertEquals(Double.valueOf(71.897), Double.valueOf(decimalCoords.getY()));

        decimalCoords = AstrometricFunctions.convertToDecimalCoords("12h 57' 21.00\"", "+71° 53' 49.2\"");
        assertEquals(Double.valueOf(194.3375), Double.valueOf(decimalCoords.getX()));
        assertEquals(Double.valueOf(71.897), Double.valueOf(decimalCoords.getY()));

        decimalCoords = AstrometricFunctions.convertToDecimalCoords("12h57'21.00\"", "+71°53'49.2\"");
        assertEquals(Double.valueOf(194.3375), Double.valueOf(decimalCoords.getX()));
        assertEquals(Double.valueOf(71.897), Double.valueOf(decimalCoords.getY()));

        decimalCoords = AstrometricFunctions.convertToDecimalCoords("12:57:21.00", "+71:53:49.2");
        assertEquals(Double.valueOf(194.3375), Double.valueOf(decimalCoords.getX()));
        assertEquals(Double.valueOf(71.897), Double.valueOf(decimalCoords.getY()));
    }

    @Test
    public void convertToSexagesimalCoords() {
        Locale.setDefault(Locale.US);
        StringPair sexagesimalCoords = AstrometricFunctions.convertToSexagesimalCoords(194.3374822, 71.8969964);
        assertEquals("12 57 21.00", sexagesimalCoords.getS1());
        assertEquals("71 53 49.19", sexagesimalCoords.getS2());
    }

    @Test
    public void convertMJDToDateTime() {
        LocalDateTime ldt = AstrometricFunctions.convertMJDToDateTime(new BigDecimal("58432.63880639"));
        assertEquals("2018-11-10T15:19:52.872096", ldt.toString());
        ldt = AstrometricFunctions.convertMJDToDateTime(new BigDecimal("58435.58375519"));
        assertEquals("2018-11-13T14:00:36.448416", ldt.toString());
    }

    @Test
    public void convertDateTimeToMJD() {
        BigDecimal mjd = AstrometricFunctions.convertDateTimeToMJD(LocalDateTime.parse("2018-11-10T15:19:52.872096"));
        assertEquals("58432.638806390", mjd.toString());
        mjd = AstrometricFunctions.convertDateTimeToMJD(LocalDateTime.parse("2018-11-13T14:00:36.448416"));
        assertEquals("58435.583755190", mjd.toString());
    }

}
