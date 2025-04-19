package astro.tool.box.container;

import java.util.Objects;

public class StringPair {

    private final String s1;

    private final String s2;

    public StringPair(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
    }

    @Override
    public String toString() {
        return "StringPair{" + "s1=" + s1 + ", s2=" + s2 + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.s1);
        hash = 19 * hash + Objects.hashCode(this.s2);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final StringPair other = (StringPair) obj;
        if (!Objects.equals(this.s1, other.s1)) {
            return false;
        }
        return Objects.equals(this.s2, other.s2);
    }

    public String getS1() {
        return s1;
    }

    public String getS2() {
        return s2;
    }

}
