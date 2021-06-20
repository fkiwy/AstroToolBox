package astro.tool.box.container;

public class SpectralType {

    private final String spt;

    private final Integer occurrences;

    private final Double sptNum;

    public SpectralType(String spt, Integer occurrences, Double sptNum) {
        this.spt = spt;
        this.occurrences = occurrences;
        this.sptNum = sptNum;
    }

    @Override
    public String toString() {
        return "SpectralType{" + "spt=" + spt + ", occurrences=" + occurrences + ", sptNum=" + sptNum + '}';
    }

    public String getSpt() {
        return spt;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public Double getSptNum() {
        return sptNum;
    }

}
