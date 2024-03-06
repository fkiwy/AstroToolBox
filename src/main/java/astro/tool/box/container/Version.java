package astro.tool.box.container;

import java.time.LocalDate;

public class Version {

    private final String number;

    private final boolean latest;

    private final LocalDate date;

    private final String message;

    public Version(String number, boolean latest, int year, int month, int day, String message) {
        this.number = number;
        this.latest = latest;
        this.date = LocalDate.of(year, month, day);
        this.message = message;
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

    public String getMessage() {
        return message;
    }

}
