package astro.tool.box.enumeration;

public enum Epoch {

    ALL,
    ASCENDING_SCAN_DIR,
    DESCENDING_SCAN_DIR,
    SEPARATE_SCAN_DIR,
    SEPARATE_SCAN_DIR_SUBTRACTED,
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
                || epoch.equals(SEPARATE_SCAN_DIR_SUBTRACTED);
    }

}
