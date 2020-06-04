package astro.tool.box.util;

public class Counter {

    private int total;

    private int value;

    public Counter() {
        this.total = 0;
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

    public int getTotal() {
        return total;
    }

}
