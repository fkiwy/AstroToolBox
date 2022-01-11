package astro.tool.box.enumeration;

public enum Epoch {

    ALL,
    ASCENDING,
    DESCENDING,
    ASCENDING_DESCENDING,
    ASCENDING_DESCENDING_SUBTRACTED,
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
        return epoch.equals(ASCENDING_DESCENDING_SUBTRACTED)
                || epoch.equals(FIRST_LAST_SUBTRACTED);
    }

}
