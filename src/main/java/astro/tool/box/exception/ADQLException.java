package astro.tool.box.exception;

public class ADQLException extends RuntimeException {

	public ADQLException() {
	}

	public ADQLException(String string) {
		super(string);
	}

	public ADQLException(String string, Throwable thrwbl) {
		super(string, thrwbl);
	}

	public ADQLException(Throwable thrwbl) {
		super(thrwbl);
	}

	public ADQLException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
		super(string, thrwbl, bln, bln1);
	}

}
