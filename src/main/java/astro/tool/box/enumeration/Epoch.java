package astro.tool.box.enumeration;

public enum Epoch {

    ALL,
    ASCENDING,
    DESCENDING,
    ASCENDING_DESCENDING,
    ASCENDING_DESCENDING_SUBTRACTED,
    ASCENDING_DESCENDING_NOISE_REDUCED,
    ASCENDING_DESCENDING_PARALLAX,
    YEAR,
    FIRST_REMAINING,
    FIRST_REMAINING_SUBTRACTED,
    FIRST_LAST,
    FIRST_LAST_SUBTRACTED,
    FIRST_LAST_PARALLAX;

    public static boolean isSubtracted(Epoch epoch) {
        return epoch.equals(ASCENDING_DESCENDING_SUBTRACTED)
                || epoch.equals(ASCENDING_DESCENDING_NOISE_REDUCED)
                || epoch.equals(FIRST_REMAINING_SUBTRACTED)
                || epoch.equals(FIRST_LAST_SUBTRACTED);
    }

    public static boolean isFirstLast(Epoch epoch) {
        return epoch.equals(FIRST_LAST)
                || epoch.equals(FIRST_LAST_SUBTRACTED)
                || epoch.equals(FIRST_LAST_PARALLAX);
    }

}
