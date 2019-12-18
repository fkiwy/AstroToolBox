package astro.tool.box.enumeration;

public enum JColor {

    WHITE(java.awt.Color.WHITE),
    RED(java.awt.Color.RED),
    BLUE(java.awt.Color.BLUE),
    GREEN(java.awt.Color.GREEN),
    YELLOW(java.awt.Color.YELLOW),
    GRAY(java.awt.Color.GRAY),
    DARK_RED(java.awt.Color.RED.darker()),
    DARK_BLUE(java.awt.Color.BLUE.darker()),
    DARK_GREEN(java.awt.Color.GREEN.darker()),
    DARK_YELLOW(java.awt.Color.YELLOW.darker()),
    DARK_GRAY(java.awt.Color.DARK_GRAY),
    DARKER_GREEN(new java.awt.Color(0, 100, 0)),
    LIGHT_RED(new java.awt.Color(255, 204, 204)),
    LIGHT_BLUE(new java.awt.Color(204, 229, 255)),
    LIGHT_GREEN(new java.awt.Color(204, 255, 204)),
    LIGHT_YELLOW(new java.awt.Color(255, 255, 204)),
    LIGHT_GRAY(java.awt.Color.LIGHT_GRAY);

    public java.awt.Color val;

    private JColor(java.awt.Color val) {
        this.val = val;
    }

}
