package astro.tool.box.enumeration;

public enum FileType {

    STACK("stack"), WARP("warp"), STACK_AND_WARP("stack,warp");

    public String val;

    private FileType(String val) {
        this.val = val;
    }

}
