package astro.tool.box.container;

import java.time.LocalDate;

public class Version {

    private final String number;

    private final boolean latest;

    private final LocalDate date;

    private final String fileId;

    private final String notesId;

    public Version(String number, boolean latest, int year, int month, int day, String fileId, String notesId) {
        this.number = number;
        this.latest = latest;
        this.date = LocalDate.of(year, month, day);
        this.fileId = fileId;
        this.notesId = notesId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Version{number=").append(number);
        sb.append(", latest=").append(latest);
        sb.append(", date=").append(date);
        sb.append(", fileId=").append(fileId);
        sb.append(", notesId=").append(notesId);
        sb.append('}');
        return sb.toString();
    }

    public String getNumber() {
        return number;
    }

    public boolean isLatest() {
        return latest;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getFileId() {
        return fileId;
    }

    public String getNotesId() {
        return notesId;
    }

}
