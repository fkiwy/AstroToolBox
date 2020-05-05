package astro.tool.box.module;

import astro.tool.box.container.NumberPair;
import java.util.List;

public class FlipbookComponent {

    private static final String ASC_NODE = "ascending";

    private static final String DESC_NODE = "descending";

    private final int band;

    private final int epoch;

    private int epochCount;

    private boolean isMerged;

    private boolean firstEpoch;

    private List<NumberPair> diffPixels;

    public FlipbookComponent(int band, int epoch) {
        this.band = band;
        this.epoch = epoch;
    }

    public FlipbookComponent(int band, int epoch, boolean isMerged) {
        this.band = band;
        this.epoch = epoch;
        this.isMerged = isMerged;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FlipbookComponent{band=").append(band);
        sb.append(", epoch=").append(epoch);
        sb.append(", epochCount=").append(epochCount);
        sb.append(", isMerged=").append(isMerged);
        sb.append(", firstEpoch=").append(firstEpoch);
        sb.append(", diffPixels=").append(diffPixels);
        sb.append('}');
        return sb.toString();
    }

    public String getTitle() {
        String titleBand;
        String titleEpoch;
        String node;
        String minObsTime = "";
        String maxObsTime = "";
        if (band == 12) {
            titleBand = "W1&W2";
        } else {
            titleBand = "W" + band;
        }
        if (isMerged) {
            switch (epoch) {
                case 100:
                    firstEpoch = true;
                    titleEpoch = "1";
                    node = ASC_NODE + "&" + DESC_NODE;
                    break;
                case 200:
                    titleEpoch = "" + epochCount;
                    node = ASC_NODE + "&" + DESC_NODE;
                    break;
                case 300:
                    titleEpoch = "2-" + epochCount;
                    node = ASC_NODE + "&" + DESC_NODE;
                    break;
                case 400:
                    firstEpoch = true;
                    titleEpoch = "1&" + epochCount;
                    node = ASC_NODE;
                    break;
                case 500:
                    titleEpoch = "1&" + epochCount;
                    node = DESC_NODE;
                    break;
                case 600:
                    firstEpoch = true;
                    titleEpoch = "1-" + epochCount;
                    node = ASC_NODE;
                    break;
                case 700:
                    titleEpoch = "1-" + epochCount;
                    node = DESC_NODE;
                    break;
                default:
                    if (epoch >= 800 && epoch < 900) {
                        if (epoch == 802) {
                            firstEpoch = true;
                        }
                        titleEpoch = String.valueOf((epoch - 800) / 2);
                        node = epoch % 2 == 0 ? ASC_NODE : DESC_NODE;
                    } else if (epoch >= 900 && epoch < 1000) {
                        if (epoch == 903) {
                            firstEpoch = true;
                        }
                        titleEpoch = String.valueOf((epoch - 900) / 2);
                        node = epoch % 2 == 0 ? ASC_NODE : DESC_NODE;
                    } else if (epoch >= 1000 && epoch < 1100) {
                        titleEpoch = String.valueOf(epoch - 1000 + 1);
                        node = ASC_NODE + "&" + DESC_NODE;
                    } else if (epoch >= 1100 && epoch < 1200) {
                        if (epoch == 1100) {
                            firstEpoch = true;
                        }
                        titleEpoch = String.valueOf(epoch - 1100 + 1);
                        node = ASC_NODE + "&" + DESC_NODE;
                    } else {
                        firstEpoch = epoch == 101;
                        if (epoch > 100 && epoch < 200) {
                            titleEpoch = String.valueOf(epoch - 100);
                            node = ASC_NODE + "&" + DESC_NODE;
                        } else {
                            titleEpoch = "";
                            node = "";
                        }
                    }
                    break;
            }
        } else {
            firstEpoch = epoch == 0 || epoch == 1;
            titleEpoch = String.valueOf((epoch / 2) + 1);
            node = epoch % 2 == 0 ? ASC_NODE : DESC_NODE;
        }
        return "WISE: " + minObsTime + maxObsTime + "Band=" + titleBand + " ~ Epoch=" + titleEpoch + " ~ Node=" + node;
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

    public boolean isFirstEpoch() {
        return firstEpoch;
    }

    public List<NumberPair> getDiffPixels() {
        return diffPixels;
    }

    public void setDiffPixels(List<NumberPair> diffPixels) {
        this.diffPixels = diffPixels;
    }

}
