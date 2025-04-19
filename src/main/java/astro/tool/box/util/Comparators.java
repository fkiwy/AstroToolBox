package astro.tool.box.util;

import java.util.Comparator;

public class Comparators {

	public static Comparator getStringComparator() {
		return (Comparator) (Object o1, Object o2) -> ((String) o1).compareTo((String) o2);
	}

	public static Comparator getIntegerComparator() {
		return (Comparator) (Object o1, Object o2) -> Integer.valueOf(getStringValue(o1))
				.compareTo(Integer.valueOf(getStringValue(o2)));
	}

	public static Comparator getLongComparator() {
		return (Comparator) (Object o1, Object o2) -> Long.valueOf(getStringValue(o1))
				.compareTo(Long.valueOf(getStringValue(o2)));
	}

	public static Comparator getDoubleComparator() {
		return (Comparator) (Object o1, Object o2) -> Double.valueOf(getStringValue(o1))
				.compareTo(Double.valueOf(getStringValue(o2)));
	}

	public static String getStringValue(Object obj) {
		String str = (String) obj;
		return str.isEmpty() ? "0" : str;
	}

}
