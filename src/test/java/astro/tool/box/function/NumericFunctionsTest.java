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

}
