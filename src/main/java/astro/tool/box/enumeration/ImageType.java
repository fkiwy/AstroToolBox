package astro.tool.box.enumeration;

public enum ImageType {

	STACK("stack"), WARP("warp"), STACK_AND_WARP("stack,warp");

	public String val;

	ImageType(String val) {
		this.val = val;
	}

}
