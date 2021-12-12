package astro.tool.box.enumeration;

public enum Epoch {

    ALL,
    ASC_SCAN_DIR,
    DESC_SCAN_DIR,
    ASC_DESC_SCAN_DIR,
    ASC_DESC_SUBTRACTED,
    YEAR,
    FIRST_LAST,
    FIRST_LAST_SUBTRACTED,
    FIRST_LAST_PARALLAX;

    public static boolean isFirstLast(Epoch epoch) {
        return epoch.equals(FIRST_LAST)
                || epoch.equals(FIRST_LAST_SUBTRACTED)
                || epoch.equals(FIRST_LAST_PARALLAX);
    }

    public static boolean isSubtracted(Epoch epoch) {
        return epoch.equals(FIRST_LAST_SUBTRACTED)
                || epoch.equals(ASC_DESC_SUBTRACTED);
    }

}
