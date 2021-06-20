package astro.tool.box.util;

public class Counter {

    private int value;

    private final int increment;

    public Counter() {
        this.increment = 1;
    }

    public Counter(int increment) {
        this.increment = increment;
    }

    public void init() {
        this.value = 0;
    }

    public void init(int value) {
        this.value = value;
    }

    public void add() {
        this.value += increment;
    }

    public void add(int increment) {
        this.value += increment;
    }

    public int value() {
        return value;
    }

}
