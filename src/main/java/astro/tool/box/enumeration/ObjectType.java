package astro.tool.box.enumeration;

public enum ObjectType {

    UNSPECIFIED(""),
    STAR("Star"),
    HIGH_PM_STAR("High proper motion star"),
    VARIABLE_STAR("Variable star"),
    LOW_MASS_STAR("Low-mass star"),
    YSO("Young stellar object"),
    BROWN_DWARF("Brown dwarf"),
    WHITE_DWARF("White dwarf"),
    MULTIPLE("Multiple system"),
    BINARY("Binary system"),
    GALAXY("Galaxy"),
    AGN("Active galactic nucleus"),
    PLANET("Planet"),
    SMALL_BODY("Small body"),
    ASTEROID("Asteroid"),
    COMET("Comet"),
    OTHER("Other");

    public String val;

    private ObjectType(String val) {
        this.val = val;
    }

    public static String[] labels() {
        int length = values().length;
        String[] labels = new String[length];
        for (int i = 0; i < length; i++) {
            labels[i] = values()[i].val;
        }
        return labels;
    }

}
