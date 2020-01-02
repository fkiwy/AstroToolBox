package astro.tool.box.container;

import astro.tool.box.enumeration.ObjectType;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Constants.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CollectedObject {

    // Local date and time
    private final LocalDateTime discoveryDate;

    // Catalog name
    private final String catalogName;

    // Object type
    private final ObjectType objectType;

    // Right ascension
    private final double ra;

    // Declination
    private final double dec;

    // Unique source identifier
    private final String sourceId;

    // Parallax
    private final double plx;

    // Proper motion in right ascension direction
    private final double pmra;

    // Proper motion in declination direction
    private final double pmdec;

    // Spectral types
    private final List<String> spectralTypes;

    // Notes
    private final String notes;

    private CollectedObject(LocalDateTime discoveryDate, ObjectType objectType, String catalogName, double ra, double dec, String sourceId, double plx, double pmra, double pmdec, List<String> spectralTypes, String notes) {
        this.discoveryDate = discoveryDate;
        this.objectType = objectType;
        this.catalogName = catalogName;
        this.ra = ra;
        this.dec = dec;
        this.sourceId = sourceId;
        this.plx = plx;
        this.pmra = pmra;
        this.pmdec = pmdec;
        this.spectralTypes = spectralTypes;
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CollectedObject{" + "discoveryDate=" + discoveryDate + ", catalogName=" + catalogName + ", objectType=" + objectType + ", ra=" + ra + ", dec=" + dec + ", sourceId=" + sourceId + ", plx=" + plx + ", pmra=" + pmra + ", pmdec=" + pmdec + ", spectralTypes=" + spectralTypes + ", notes=" + notes + '}';
    }

    public String[] getColumnValues() {
        return getValues().split(",", 11);
    }

    public String[] getColumnTitles() {
        return getTitles().split(",", 11);
    }

    public String getValues() {
        String values = discoveryDate.format(DATE_TIME_FORMATTER) + "," + objectType.name() + "," + catalogName + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + sourceId + "," + roundTo4Dec(plx) + "," + roundTo3Dec(pmra) + "," + roundTo3Dec(pmdec) + "," + joinSpetralTypes() + "," + notes;
        return values;
    }

    public String getTitles() {
        String titles = "Discovery date,Object type,Catalog,RA,dec,Source id,Plx,pmRA,pmdec,Spectral types,Notes";
        return titles;
    }

    private String joinSpetralTypes() {
        return spectralTypes.stream().collect(Collectors.joining(" "));
    }

    public static class Builder {

        private LocalDateTime discoveryDate;
        private ObjectType objectType;
        private String catalogName;
        private double ra;
        private double dec;
        private String sourceId;
        private double plx;
        private double pmra;
        private double pmdec;
        private List<String> spectralTypes;
        private String notes;

        public Builder setDiscoveryDate(LocalDateTime discoveryDate) {
            this.discoveryDate = discoveryDate;
            return this;
        }

        public Builder setObjectType(ObjectType objectType) {
            this.objectType = objectType;
            return this;
        }

        public Builder setCatalogName(String catalogName) {
            this.catalogName = catalogName;
            return this;
        }

        public Builder setRa(double ra) {
            this.ra = ra;
            return this;
        }

        public Builder setDec(double dec) {
            this.dec = dec;
            return this;
        }

        public Builder setSourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder setPlx(double plx) {
            this.plx = plx;
            return this;
        }

        public Builder setPmra(double pmra) {
            this.pmra = pmra;
            return this;
        }

        public Builder setPmdec(double pmdec) {
            this.pmdec = pmdec;
            return this;
        }

        public Builder setSpectralTypes(List<String> spectralTypes) {
            this.spectralTypes = spectralTypes;
            return this;
        }

        public Builder setNotes(String notes) {
            this.notes = notes;
            return this;
        }

        public CollectedObject build() {
            return new CollectedObject(discoveryDate, objectType, catalogName, ra, dec, sourceId, plx, pmra, pmdec, spectralTypes, notes);
        }

    }

}
