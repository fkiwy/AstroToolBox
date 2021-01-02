package astro.tool.box.util;

public class Counter {

    private int total;

    private final int value;

    public Counter() {
        this.value = 1;
    }

    public Counter(int value) {
        this.value = value;
    }

    public void add(int value) {
        total += value;
    }

    public void add() {
        total += value;
    }

    public int total() {
        return total;
    }

}
