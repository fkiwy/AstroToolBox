package astro.tool.box.main;

import java.awt.image.BufferedImage;

public class FlipbookComponent {

    private static final String ASC_NODE = "ASC";

    private static final String DESC_NODE = "DESC";

    private final int band;

    private final int epoch;

    private int epochCount;

    private int totalEpochs;

    private boolean isMerged;

    private boolean firstEpoch;

    private BufferedImage image;

    public FlipbookComponent(int band, int epoch) {
        this.band = band;
        this.epoch = epoch;
    }

    public FlipbookComponent(int band, int epoch, boolean isMerged) {
        this.band = band;
        this.epoch = epoch;
        this.isMerged = isMerged;
    }

    public FlipbookComponent(int band, int epoch, int totalEpochs) {
        this.band = band;
        this.epoch = epoch;
        this.totalEpochs = totalEpochs;
    }

    public FlipbookComponent(int band, int epoch, boolean isMerged, int totalEpochs) {
        this.band = band;
        this.epoch = epoch;
        this.isMerged = isMerged;
        this.totalEpochs = totalEpochs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FlipbookComponent{band=").append(band);
        sb.append(", epoch=").append(epoch);
        sb.append(", epochCount=").append(epochCount);
        sb.append(", isMerged=").append(isMerged);
        sb.append(", firstEpoch=").append(firstEpoch);
        sb.append('}');
        return sb.toString();
    }

    public String getTitle() {
        String titleBand;
        if (band == 12) {
            titleBand = "W1+W2";
        } else {
            titleBand = "W" + band;
        }
        String titleEpoch = "";
        String titleNode = "";
        if (isMerged) {
            switch (epoch) {
                case 100:
                    firstEpoch = true;
                    titleEpoch = "1";
                    titleNode = ASC_NODE + "+" + DESC_NODE;
                    break;
                case 200:
                    titleEpoch = "" + epochCount;
                    titleNode = ASC_NODE + "+" + DESC_NODE;
                    break;
                case 300:
                    titleEpoch = "2-" + epochCount;
                    titleNode = ASC_NODE + "+" + DESC_NODE;
                    break;
                case 400:
                    firstEpoch = true;
                    titleEpoch = "1+" + epochCount;
                    titleNode = ASC_NODE;
                    break;
                case 500:
                    titleEpoch = "1+" + epochCount;
                    titleNode = DESC_NODE;
                    break;
                case 600:
                    firstEpoch = true;
                    titleEpoch = "1-" + epochCount;
                    titleNode = ASC_NODE;
                    break;
                case 700:
                    titleEpoch = "1-" + epochCount;
                    titleNode = DESC_NODE;
                    break;
                case 1100:
                    firstEpoch = true;
                    titleEpoch = "1";
                    titleNode = ASC_NODE;
                    break;
                case 1200:
                    titleEpoch = "" + epochCount;
                    titleNode = ASC_NODE;
                    break;
                case 1300:
                    firstEpoch = true;
                    titleEpoch = "1";
                    titleNode = DESC_NODE;
                    break;
                case 1400:
                    titleEpoch = "" + epochCount;
                    titleNode = DESC_NODE;
                    break;
                default:
                    if (epoch > 100 && epoch < 200) {
                        firstEpoch = epoch == 101;
                        titleEpoch = String.valueOf(epoch - 100);
                        titleNode = ASC_NODE + "+" + DESC_NODE;
                    } else if (epoch >= 800 && epoch < 900) {
                        firstEpoch = epoch == 803;
                        titleEpoch = String.valueOf((epoch - 800) / 2);
                        titleNode = epoch % 2 == 0 ? ASC_NODE : DESC_NODE;
                    } else if (epoch >= 900 && epoch < 1000) {
                        firstEpoch = epoch == 902;
                        titleEpoch = String.valueOf((epoch - 900) / 2);
                        titleNode = epoch % 2 == 0 ? ASC_NODE : DESC_NODE;
                    }
                    break;
            }
        } else {
            firstEpoch = epoch == 0 || epoch == 1;
            titleEpoch = String.valueOf((epoch / 2) + 1);
            titleNode = epoch % 2 == 0 ? ASC_NODE : DESC_NODE;
        }
        return "Band=" + titleBand + "   Epoch=" + titleEpoch + "   Node=" + titleNode;
    }

    public int getBand() {
        return band;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setEpochCount(int epochCount) {
        this.epochCount = epochCount;
    }

    public int getTotalEpochs() {
        return totalEpochs;
    }

    public boolean isFirstEpoch() {
        return firstEpoch;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
