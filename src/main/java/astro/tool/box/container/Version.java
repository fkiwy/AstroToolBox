package astro.tool.box.container;

import java.time.LocalDate;

public class Version {

    private final String number;

    private final boolean latest;

    private final LocalDate date;

    public Version(String number, boolean latest, int year, int month, int day) {
        this.number = number;
        this.latest = latest;
        date = LocalDate.of(year, month, day);
    }

    @Override
    public String toString() {
        return "Version{" + "number=" + number + ", latest=" + latest + ", date=" + date + '}';
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

}
