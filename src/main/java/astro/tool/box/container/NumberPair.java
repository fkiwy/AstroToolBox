package astro.tool.box.container;

public class NumberPair {

	private final double x;

	private final double y;

	public NumberPair(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "NumberPair{" + "x=" + x + ", y=" + y + '}';
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 79 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 79 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
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
		final NumberPair other = (NumberPair) obj;
		if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		return Double.doubleToLongBits(this.y) == Double.doubleToLongBits(other.y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

}
