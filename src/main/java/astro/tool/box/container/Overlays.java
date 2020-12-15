package astro.tool.box.container;

public class Overlays {

    enum ID {
        SIMBAD,
        ALLWISE,
        CATWISE,
        UNWISE,
        GAIADR2,
        GAIADR3,
        NOIRLAB,
        PANSTARRS,
        SDSS,
        SPECTRA,
        VHS,
        GAIAWD,
        TWOMASS,
        SPITZER,
        SSO,
        PM_GAIA_DR2,
        PM_GAIA_DR3,
        PM_NOIR_LAB,
        PM_CAT_WISE,
        GHOSTS,
        LATENTS,
        HALOS,
        SPIKES,
        ESTSPT,
        POTBD
    }

    private boolean simbad;
    private boolean allwise;
    private boolean catwise;
    private boolean unwise;
    private boolean gaiadr2;
    private boolean gaiadr3;
    private boolean noirlab;
    private boolean panstar;
    private boolean sdss;
    private boolean spectra;
    private boolean vhs;
    private boolean gaiawd;
    private boolean twomass;
    private boolean spitzer;
    private boolean sso;
    private boolean pmgaiadr2;
    private boolean pmgaiadr3;
    private boolean pmnoirlab;
    private boolean pmcatwise;
    private boolean ghosts;
    private boolean latents;
    private boolean halos;
    private boolean spikes;
    private boolean estspt;
    private boolean potbd;

    public String serialize() {
        StringBuilder overlays = new StringBuilder();
        if (simbad) {
            overlays.append(ID.SIMBAD).append(",");
        }
        if (allwise) {
            overlays.append(ID.ALLWISE).append(",");
        }
        if (catwise) {
            overlays.append(ID.CATWISE).append(",");
        }
        if (unwise) {
            overlays.append(ID.UNWISE).append(",");
        }
        if (gaiadr2) {
            overlays.append(ID.GAIADR2).append(",");
        }
        if (gaiadr3) {
            overlays.append(ID.GAIADR3).append(",");
        }
        if (noirlab) {
            overlays.append(ID.NOIRLAB).append(",");
        }
        if (panstar) {
            overlays.append(ID.PANSTARRS).append(",");
        }
        if (sdss) {
            overlays.append(ID.SDSS).append(",");
        }
        if (spectra) {
            overlays.append(ID.SPECTRA).append(",");
        }
        if (vhs) {
            overlays.append(ID.VHS).append(",");
        }
        if (gaiawd) {
            overlays.append(ID.GAIAWD).append(",");
        }
        if (twomass) {
            overlays.append(ID.TWOMASS).append(",");
        }
        if (spitzer) {
            overlays.append(ID.SPITZER).append(",");
        }
        if (sso) {
            overlays.append(ID.SSO).append(",");
        }
        if (pmgaiadr2) {
            overlays.append(ID.PM_GAIA_DR2).append(",");
        }
        if (pmgaiadr3) {
            overlays.append(ID.PM_GAIA_DR3).append(",");
        }
        if (pmnoirlab) {
            overlays.append(ID.PM_NOIR_LAB).append(",");
        }
        if (pmcatwise) {
            overlays.append(ID.PM_CAT_WISE).append(",");
        }
        if (ghosts) {
            overlays.append(ID.GHOSTS).append(",");
        }
        if (latents) {
            overlays.append(ID.LATENTS).append(",");
        }
        if (halos) {
            overlays.append(ID.HALOS).append(",");
        }
        if (spikes) {
            overlays.append(ID.SPIKES).append(",");
        }
        if (estspt) {
            overlays.append(ID.ESTSPT).append(",");
        }
        if (potbd) {
            overlays.append(ID.POTBD).append(",");
        }
        overlays.setLength(overlays.lastIndexOf(","));
        return overlays.toString();
    }

    public void deserialize(String overlays) {
        if (overlays.contains(ID.SIMBAD.name())) {
            simbad = true;
        }
        if (overlays.contains(ID.ALLWISE.name())) {
            allwise = true;
        }
        if (overlays.contains(ID.CATWISE.name())) {
            catwise = true;
        }
        if (overlays.contains(ID.UNWISE.name())) {
            unwise = true;
        }
        if (overlays.contains(ID.GAIADR2.name())) {
            gaiadr2 = true;
        }
        if (overlays.contains(ID.GAIADR3.name())) {
            gaiadr3 = true;
        }
        if (overlays.contains(ID.NOIRLAB.name())) {
            noirlab = true;
        }
        if (overlays.contains(ID.PANSTARRS.name())) {
            panstar = true;
        }
        if (overlays.contains(ID.SDSS.name())) {
            sdss = true;
        }
        if (overlays.contains(ID.SPECTRA.name())) {
            spectra = true;
        }
        if (overlays.contains(ID.VHS.name())) {
            vhs = true;
        }
        if (overlays.contains(ID.GAIAWD.name())) {
            gaiawd = true;
        }
        if (overlays.contains(ID.TWOMASS.name())) {
            twomass = true;
        }
        if (overlays.contains(ID.SPITZER.name())) {
            spitzer = true;
        }
        if (overlays.contains(ID.SSO.name())) {
            sso = true;
        }
        if (overlays.contains(ID.PM_GAIA_DR2.name())) {
            pmgaiadr2 = true;
        }
        if (overlays.contains(ID.PM_GAIA_DR3.name())) {
            pmgaiadr3 = true;
        }
        if (overlays.contains(ID.PM_NOIR_LAB.name())) {
            pmnoirlab = true;
        }
        if (overlays.contains(ID.PM_CAT_WISE.name())) {
            pmcatwise = true;
        }
        if (overlays.contains(ID.GHOSTS.name())) {
            ghosts = true;
        }
        if (overlays.contains(ID.LATENTS.name())) {
            latents = true;
        }
        if (overlays.contains(ID.HALOS.name())) {
            halos = true;
        }
        if (overlays.contains(ID.SPIKES.name())) {
            spikes = true;
        }
        if (overlays.contains(ID.ESTSPT.name())) {
            estspt = true;
        }
        if (overlays.contains(ID.POTBD.name())) {
            potbd = true;
        }
    }

    public boolean isSimbad() {
        return simbad;
    }

    public void setSimbad(boolean simbad) {
        this.simbad = simbad;
    }

    public boolean isAllwise() {
        return allwise;
    }

    public void setAllwise(boolean allwise) {
        this.allwise = allwise;
    }

    public boolean isCatwise() {
        return catwise;
    }

    public void setCatwise(boolean catwise) {
        this.catwise = catwise;
    }

    public boolean isUnwise() {
        return unwise;
    }

    public void setUnwise(boolean unwise) {
        this.unwise = unwise;
    }

    public boolean isGaiadr2() {
        return gaiadr2;
    }

    public void setGaiadr2(boolean gaiadr2) {
        this.gaiadr2 = gaiadr2;
    }

    public boolean isGaiadr3() {
        return gaiadr3;
    }

    public void setGaiadr3(boolean gaiadr3) {
        this.gaiadr3 = gaiadr3;
    }

    public boolean isNoirlab() {
        return noirlab;
    }

    public void setNoirlab(boolean noirlab) {
        this.noirlab = noirlab;
    }

    public boolean isPanstar() {
        return panstar;
    }

    public void setPanstar(boolean panstar) {
        this.panstar = panstar;
    }

    public boolean isSdss() {
        return sdss;
    }

    public void setSdss(boolean sdss) {
        this.sdss = sdss;
    }

    public boolean isSpectra() {
        return spectra;
    }

    public void setSpectra(boolean spectra) {
        this.spectra = spectra;
    }

    public boolean isVhs() {
        return vhs;
    }

    public void setVhs(boolean vhs) {
        this.vhs = vhs;
    }

    public boolean isGaiawd() {
        return gaiawd;
    }

    public void setGaiawd(boolean gaiawd) {
        this.gaiawd = gaiawd;
    }

    public boolean isTwomass() {
        return twomass;
    }

    public void setTwomass(boolean twomass) {
        this.twomass = twomass;
    }

    public boolean isSpitzer() {
        return spitzer;
    }

    public void setSpitzer(boolean spitzer) {
        this.spitzer = spitzer;
    }

    public boolean isSso() {
        return sso;
    }

    public void setSso(boolean sso) {
        this.sso = sso;
    }

    public boolean isPmgaiadr2() {
        return pmgaiadr2;
    }

    public void setPmgaiadr2(boolean pmgaiadr2) {
        this.pmgaiadr2 = pmgaiadr2;
    }

    public boolean isPmgaiadr3() {
        return pmgaiadr3;
    }

    public void setPmgaiadr3(boolean pmgaiadr3) {
        this.pmgaiadr3 = pmgaiadr3;
    }

    public boolean isPmnoirlab() {
        return pmnoirlab;
    }

    public void setPmnoirlab(boolean pmnoirlab) {
        this.pmnoirlab = pmnoirlab;
    }

    public boolean isPmcatwise() {
        return pmcatwise;
    }

    public void setPmcatwise(boolean pmcatwise) {
        this.pmcatwise = pmcatwise;
    }

    public boolean isGhosts() {
        return ghosts;
    }

    public void setGhosts(boolean ghosts) {
        this.ghosts = ghosts;
    }

    public boolean isLatents() {
        return latents;
    }

    public void setLatents(boolean latents) {
        this.latents = latents;
    }

    public boolean isHalos() {
        return halos;
    }

    public void setHalos(boolean halos) {
        this.halos = halos;
    }

    public boolean isSpikes() {
        return spikes;
    }

    public void setSpikes(boolean spikes) {
        this.spikes = spikes;
    }

    public boolean isEstspt() {
        return estspt;
    }

    public void setEstspt(boolean estspt) {
        this.estspt = estspt;
    }

    public boolean isPotbd() {
        return potbd;
    }

    public void setPotbd(boolean potbd) {
        this.potbd = potbd;
    }

}
