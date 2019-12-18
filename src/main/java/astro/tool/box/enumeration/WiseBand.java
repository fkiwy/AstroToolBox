package astro.tool.box.enumeration;

public enum WiseBand {

    W1(1), W2(2), W1W2(12);

    public int val;

    private WiseBand(int val) {
        this.val = val;
    }

}
