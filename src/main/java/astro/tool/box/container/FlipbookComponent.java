package astro.tool.box.container;

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
                    titleEpoch = "AllWISE";
                    titleNode = ASC_NODE + "+" + DESC_NODE;
                    break;
                case 200:
                    titleEpoch = "NEO" + (epochCount - 1);
                    titleNode = ASC_NODE + "+" + DESC_NODE;
                    break;
                case 300:
                    firstEpoch = true;
                    titleEpoch = "AllWISE+NEO" + (epochCount - 1);
                    titleNode = ASC_NODE;
                    break;
                case 400:
                    titleEpoch = "AllWISE+NEO" + (epochCount - 1);
                    titleNode = DESC_NODE;
                    break;
                default:
                    if (epoch > 500 && epoch < 600) {
                        firstEpoch = epoch == 501;
                        titleEpoch = firstEpoch ? "AllWISE" : "NEO" + (epoch - 500 - 1);
                        titleNode = ASC_NODE + "+" + DESC_NODE;
                    } else if (epoch > 600 && epoch < 700) {
                        firstEpoch = epoch == 603;
                        titleEpoch = firstEpoch ? "AllWISE" : "NEO" + ((epoch - 600) / 2 - 1);
                        titleNode = epoch % 2 == 0 ? ASC_NODE : DESC_NODE;
                    } else if (epoch > 700 && epoch < 800) {
                        firstEpoch = epoch == 702;
                        titleEpoch = firstEpoch ? "AllWISE" : "NEO" + ((epoch - 700) / 2 - 1);
                        titleNode = epoch % 2 == 0 ? ASC_NODE : DESC_NODE;
                    }
                    break;
            }
        } else {
            firstEpoch = epoch == 0 || epoch == 1;
            titleEpoch = firstEpoch ? "AllWISE" : "NEO" + (epoch / 2);
            titleNode = epoch % 2 == 0 ? ASC_NODE : DESC_NODE;
        }
        return titleBand + "  " + titleNode + "  " + titleEpoch;
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
