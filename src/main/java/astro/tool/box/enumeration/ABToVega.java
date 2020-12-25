package astro.tool.box.enumeration;

public enum ABToVega {

    u(0.91),
    g(-0.08),
    r(0.16),
    i(0.37),
    z(0.54),
    Y(0.634);

    public double val;

    private ABToVega(double val) {
        this.val = val;
    }

}
