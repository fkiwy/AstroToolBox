package astro.tool.box.enumeration;

public enum WiseBand {

    W1(1), W2(2), W1W2(12);

    public int val;

    private WiseBand(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        String label = "";
        switch (val) {
            case 1:
                label = "Band 1";
                break;
            case 2:
                label = "Band 2";
                break;
            case 12:
                label = "Merged bands (1+2)";
                break;
        }
        return label;
    }

}
