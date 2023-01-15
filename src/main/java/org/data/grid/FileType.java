package org.data.grid;

public enum FileType {

    JPEG(".jpg"), PNG(".png"), PDF(".pdf");

    public String val;

    private FileType(String val) {
        this.val = val;
    }

}
