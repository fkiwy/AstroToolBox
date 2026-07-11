package astro.tool.box.container;

public record SpectralType(String spt, Integer occurrences, Double sptNum) {

	@Override
	public String toString() {
		return "SpectralType{" + "spt=" + spt + ", occurrences=" + occurrences + ", sptNum=" + sptNum + '}';
	}

}
