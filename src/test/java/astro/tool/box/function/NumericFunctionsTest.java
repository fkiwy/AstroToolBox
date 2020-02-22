package astro.tool.box.function;

import java.util.Locale;
import static org.junit.Assert.*;
import org.junit.Test;

public class NumericFunctionsTest {

    @Test
    public void roundDouble() {
        Locale.setDefault(Locale.US);
        String number = NumericFunctions.roundTo3Dec(1.2345);
        assertEquals("1.234", number);
    }

    @Test
    public void test() {
        char[] chars = Integer.toBinaryString(60).toCharArray();
        int x = 1;
        for (int i = chars.length - 1; i > -1; i--) {
            if (chars[i] == '1') {
                System.out.println(x);
            }
            x *= 2;
        }
        /*
        1 = Used within relphot (FEW); skip star.
        2 = Used within relphot (POOR); skip star.
        4 = object IDed with known ICRF quasar (may have ICRF position measurement)
        8 = identified as likely QSO (Hernitschek+ 2015ApJ...801...45H), PQSO≥0.60
        16 = identified as possible QSO (Hernitschek+ 2015ApJ...801...45H), PQSO≥0.05
        32 = identified as likely RR Lyra (Hernitschek+ 2015ApJ...801...45H), PRRLyra≥0.60
        64 = identified as possible RR Lyra (Hernitschek+ 2015ApJ...801...45H), PRRLyra≥0.05
        128 = identified as a variable based on ChiSq (Hernitschek+ 2015ApJ...801...45H)
        256 = identified as a non-periodic (stationary) transient
        512 = at least one detection identified with a known solar-system object (asteroid or other).
        1024 = most detections identified with a known solar-system object (asteroid or other).
        2048 = star with large proper motion
        4096 = simple weighted average position was used (no IRLS fitting)
        8192 = average position was fitted
        16384 = proper motion model was fitted
        32768 = parallax model was fitted
        65536 = average position used (not PM or PAR)
        131072 = proper motion used (not AVE or PAR)
        262144 = parallax used (not AVE or PM)
        524288 = mean astrometry could not be measured
        1048576 = stack position used for mean astrometry
        2097152 = mean astrometry used for stack position
        4194304 = failure to measure proper-motion model
        8388608 = extended in our data (eg, PS)
        16777216 = extended in external data (eg, 2MASS)
        33554432 = good-quality measurement in our data (eg,PS)
        67108864 = good-quality measurement in external data (eg, 2MASS)
        134217728 = good-quality object in the stack (>1 good stack measurement)
        268435456 = the primary stack measurements are the best measurements
        536870912 = suspect object in the stack (no more than 1 good measurement, 2 or more suspect or good stack measurement)
        1073741824 = poor-quality stack object (no more than 1 good or suspect measurement)
         */
    }

}
