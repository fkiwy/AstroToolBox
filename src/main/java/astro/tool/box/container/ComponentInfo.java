package astro.tool.box.container;

public class ComponentInfo {

    private final int totalEpochs;

    private final int epoch;

    private final String scan;

    public ComponentInfo(int totalEpochs, int epoch, String scan) {
        this.totalEpochs = totalEpochs;
        this.epoch = epoch;
        this.scan = scan;
    }

    public int getTotalEpochs() {
        return totalEpochs;
    }

    public int getEpoch() {
        return epoch;
    }

    public String getScan() {
        return scan;
    }

}
