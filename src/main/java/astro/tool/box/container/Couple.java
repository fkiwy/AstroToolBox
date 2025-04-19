package astro.tool.box.container;

public class Couple<A, B> {

	private final A a;

	private final B b;

	public Couple(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A getA() {
		return a;
	}

	public B getB() {
		return b;
	}

}
