package astro.tool.box.container;

import static astro.tool.box.function.NumericFunctions.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClassificationResult {

    // Catalog name
    private final String catalogName;

    // Right ascension of target
    private final double targetRa;

    // Declination of target
    private final double targetDec;

    // Distance to target
    private final double targetDistance;

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

    // Magnitudes
    private final String magnitudes;

    // Spectral types
    private final List<String> spectralTypes;

    private ClassificationResult(String catalogName, double targetRa, double targetDec, double targetDistance, double ra, double dec, String sourceId, double plx, double pmra, double pmdec, String magnitudes, List<String> spectralTypes) {
        this.catalogName = catalogName;
        this.targetRa = targetRa;
        this.targetDec = targetDec;
        this.targetDistance = targetDistance;
        this.ra = ra;
        this.dec = dec;
        this.sourceId = sourceId;
        this.plx = plx;
        this.pmra = pmra;
        this.pmdec = pmdec;
        this.magnitudes = magnitudes;
        this.spectralTypes = spectralTypes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClassificationResult{catalogName=").append(catalogName);
        sb.append(", targetRa=").append(targetRa);
        sb.append(", targetDec=").append(targetDec);
        sb.append(", targetDistance=").append(targetDistance);
        sb.append(", ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", sourceId=").append(sourceId);
        sb.append(", plx=").append(plx);
        sb.append(", pmra=").append(pmra);
        sb.append(", pmdec=").append(pmdec);
        sb.append(", magnitudes=").append(magnitudes);
        sb.append(", spectralTypes=").append(spectralTypes);
        sb.append('}');
        return sb.toString();
    }

    public Object[] getColumnValues() {
        return getValues().split(",", -1);
    }

    public Object[] getColumnTitles() {
        return getTitles().split(",", -1);
    }

    public String getValues() {
        String values = catalogName + "," + roundTo7Dec(targetRa) + "," + roundTo7Dec(targetDec) + "," + (catalogName.isEmpty() ? roundTo3Dec(targetDistance) : roundTo3DecLZ(targetDistance)) + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + sourceId + "," + roundTo4Dec(plx) + "," + roundTo3Dec(pmra) + "," + roundTo3Dec(pmdec) + "," + magnitudes + "," + joinSpetralTypes();
        return values;
    }

    public String getTitles() {
        String titles = "Catalog,Target RA,Target dec,Target dist,RA,dec,Source id,Plx,pmRA,pmdec,Magnitudes,Spectral types";
        return titles;
    }

    public String joinSpetralTypes() {
        return spectralTypes.stream().collect(Collectors.joining(" "));
    }

    public String getCatalogName() {
        return catalogName;
    }

    public double getTargetRa() {
        return targetRa;
    }

    public double getTargetDec() {
        return targetDec;
    }

    public double getTargetDistance() {
        return targetDistance;
    }

    public double getRa() {
        return ra;
    }

    public double getDec() {
        return dec;
    }

    public String getSourceId() {
        return sourceId;
    }

    public double getPlx() {
        return plx;
    }

    public double getPmra() {
        return pmra;
    }

    public double getPmdec() {
        return pmdec;
    }

    public String getMagnitudes() {
        return magnitudes;
    }

    public List<String> getSpectralTypes() {
        return spectralTypes;
    }

    public static class Builder {

        private String catalogName;
        private double targetRa;
        private double targetDec;
        private double targetDistance;
        private double ra;
        private double dec;
        private String sourceId;
        private double plx;
        private double pmra;
        private double pmdec;
        private String magnitudes;
        private List<String> spectralTypes;

        public Builder setCatalogName(String catalogName) {
            this.catalogName = catalogName;
            return this;
        }

        public Builder setTargetRa(double targetRa) {
            this.targetRa = targetRa;
            return this;
        }

        public Builder setTargetDec(double targetDec) {
            this.targetDec = targetDec;
            return this;
        }

        public Builder setTargetDistance(double targetDistance) {
            this.targetDistance = targetDistance;
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

        public Builder setMagnitudes(String magnitudes) {
            this.magnitudes = magnitudes;
            return this;
        }

        public Builder setSpectralTypes(List<String> spectralTypes) {
            this.spectralTypes = spectralTypes;
            return this;
        }

        public ClassificationResult build() {
            return new ClassificationResult(catalogName, targetRa, targetDec, targetDistance, ra, dec, sourceId, plx, pmra, pmdec, magnitudes, spectralTypes);
        }

    }

}
