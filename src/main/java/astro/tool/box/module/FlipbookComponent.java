package astro.tool.box.module;

import static astro.tool.box.util.Constants.*;
import astro.tool.box.function.AstrometricFunctions;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FlipbookComponent {

    private static final String ASC_NODE = "ascending";

    private static final String DESC_NODE = "descending";

    private final int band;

    private final int epoch;

    private int epochCount;

    private boolean isMerged;

    private boolean firstEpoch;

    private double minObsEpoch;

    private double maxObsEpoch;

    public FlipbookComponent(int band, int epoch) {
        this.band = band;
        this.epoch = epoch;
    }

    public FlipbookComponent(int band, int epoch, double minObsEpoch, double maxObsEpoch) {
        this.band = band;
        this.epoch = epoch;
        this.minObsEpoch = minObsEpoch;
        this.maxObsEpoch = maxObsEpoch;
    }

    public FlipbookComponent(int band, int epoch, boolean isMerged) {
        this.band = band;
        this.epoch = epoch;
        this.isMerged = isMerged;
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
                        titleEpoch = String.valueOf(epoch - (epoch / 100) * 100 + 1);
                        node = ASC_NODE + "&" + DESC_NODE;
                    } else if (epoch >= 900 && epoch < 1000) {
                        titleEpoch = String.valueOf(epoch - (epoch / 100) * 100 + 2);
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
        if (minObsEpoch > 0) {
            LocalDateTime ldt = AstrometricFunctions.convertMJDToDateTime(new BigDecimal(Double.toString(minObsEpoch)));
            minObsTime = "Min obs. time=" + ldt.format(DATE_TIME_FORMATTER);
        }
        if (maxObsEpoch > 0) {
            LocalDateTime ldt = AstrometricFunctions.convertMJDToDateTime(new BigDecimal(Double.toString(maxObsEpoch)));
            maxObsTime = " ~ Max obs. time=" + ldt.format(DATE_TIME_FORMATTER) + " ~ ";
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

    public double getMinObsEpoch() {
        return minObsEpoch;
    }

    public double getMaxObsEpoch() {
        return maxObsEpoch;
    }

}
