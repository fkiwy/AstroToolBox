package astro.tool.box.enumeration;

public enum JColor {

    WHITE(java.awt.Color.WHITE),
    RED(java.awt.Color.RED),
    BLUE(java.awt.Color.BLUE),
    GREEN(java.awt.Color.GREEN),
    YELLOW(java.awt.Color.YELLOW),
    ORANGE(new java.awt.Color(255, 153, 0)),
    BROWN(new java.awt.Color(115, 77, 38)),
    STEEL(new java.awt.Color(82, 82, 122)),
    PINK(new java.awt.Color(122, 0, 153)),
    PURPLE(new java.awt.Color(170, 0, 255)),
    MINT(new java.awt.Color(0, 128, 85)),
    LILAC(new java.awt.Color(153, 153, 255, 255)),
    OLIVE(new java.awt.Color(102, 102, 51)),
    NAVY(new java.awt.Color(0, 143, 179)),
    SAND(new java.awt.Color(204, 153, 0)),
    BLOOD(new java.awt.Color(204, 51, 0)),
    GRAY(java.awt.Color.GRAY),
    DARK_RED(java.awt.Color.RED.darker()),
    DARK_BLUE(java.awt.Color.BLUE.darker()),
    DARK_GREEN(java.awt.Color.GREEN.darker()),
    DARK_YELLOW(java.awt.Color.YELLOW.darker()),
    DARK_GRAY(java.awt.Color.DARK_GRAY),
    DARK_ORANGE(new java.awt.Color(255, 102, 0)),
    DIRTY_BLUE(new java.awt.Color(0, 85, 128)),
    BLACK_BLUE(new java.awt.Color(0, 51, 77)),
    LINK_BLUE(new java.awt.Color(0, 102, 204)),
    LIGHT_RED(new java.awt.Color(255, 204, 204)),
    LIGHT_BLUE(new java.awt.Color(204, 229, 255)),
    LIGHT_GREEN(new java.awt.Color(204, 255, 204)),
    LIGHT_YELLOW(new java.awt.Color(255, 255, 204)),
    LIGHT_ORANGE(new java.awt.Color(255, 235, 204)),
    LIGHT_BROWN(new java.awt.Color(230, 204, 179)),
    LIGHT_STEEL(new java.awt.Color(194, 194, 214)),
    LIGHT_PINK(new java.awt.Color(245, 204, 255)),
    LIGHT_PURPLE(new java.awt.Color(238, 204, 255)),
    LIGHT_MINT(new java.awt.Color(179, 255, 230)),
    LIGHT_NAVY(new java.awt.Color(204, 245, 255)),
    LIGHT_GRAY(java.awt.Color.LIGHT_GRAY);

    public java.awt.Color val;

    private JColor(java.awt.Color val) {
        this.val = val;
    }

}
