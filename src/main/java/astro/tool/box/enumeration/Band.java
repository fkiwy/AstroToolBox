package astro.tool.box.enumeration;

import java.util.Arrays;
import java.util.List;

public enum Band {

    G("G"),
    BP("BP"),
    RP("RP"),
    g("g"),
    r("r"),
    i("i"),
    z("z"),
    y("y"),
    J("J"),
    H("H"),
    K("K"),
    W1("W1"),
    W2("W2"),
    W3("W3"),
    W4("W4");

    private static final List<Band> SED_BANDS = Arrays.asList(g, r, i, z, y, J, H, K, W1, W2, W3 /*, W4*/);

    private static final List<Band> WD_SED_BANDS = Arrays.asList(G, BP, RP, g, r, i, z, y, J, H, K, W1, W2, W3 /*, W4*/);

    public String val;

    private Band(String val) {
        this.val = val;
    }

    public static List<Band> getSedBands() {
        return SED_BANDS;
    }

    public static List<Band> getWdSedBands() {
        return WD_SED_BANDS;
    }

}
