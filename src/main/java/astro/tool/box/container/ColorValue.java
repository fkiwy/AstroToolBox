package astro.tool.box.container;

import astro.tool.box.enumeration.Color;
import java.util.Objects;

public class ColorValue {

    private final Color color;

    private final double value;

    public ColorValue(Color color, double value) {
        this.color = color;
        this.value = value;
    }

    @Override
    public String toString() {
        return "ColorValue{" + "color=" + color + ", value=" + value + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.color);
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ColorValue other = (ColorValue) obj;
        if (Double.doubleToLongBits(this.value) != Double.doubleToLongBits(other.value)) {
            return false;
        }
        if (this.color != other.color) {
            return false;
        }
        return true;
    }

    public Color getColor() {
        return color;
    }

    public double getValue() {
        return value;
    }

}
