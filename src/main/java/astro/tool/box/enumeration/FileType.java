package astro.tool.box.enumeration;

public enum FileType {

    JPEG(".jpg"), PNG(".png"), PDF(".pdf");

    public String val;

    private FileType(String val) {
        this.val = val;
    }

}
