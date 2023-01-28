package astro.tool.box.container;

import java.util.List;

public class Tile {

    private String coadd_id;
    private List<Epoch> epochs;
    private double px;
    private double py;

    @Override
    public String toString() {
        return "Tile{" + "coaddId=" + coadd_id + ", epochs=" + epochs + ", px=" + px + ", py=" + py + '}';
    }

    public String getCoadd_id() {
        return coadd_id;
    }

    public void setCoadd_id(String coadd_id) {
        this.coadd_id = coadd_id;
    }

    public List<Epoch> getEpochs() {
        return epochs;
    }

    public void setEpochs(List<Epoch> epochs) {
        this.epochs = epochs;
    }

    public double getPx() {
        return px;
    }

    public void setPx(double px) {
        this.px = px;
    }

    public double getPy() {
        return py;
    }

    public void setPy(double py) {
        this.py = py;
    }

}
