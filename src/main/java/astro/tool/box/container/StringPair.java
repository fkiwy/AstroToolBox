package astro.tool.box.container;

import java.util.Objects;

public record StringPair(String s1, String s2) {

	@Override
	public String toString() {
		return "StringPair{" + "s1=" + s1 + ", s2=" + s2 + '}';
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

}
