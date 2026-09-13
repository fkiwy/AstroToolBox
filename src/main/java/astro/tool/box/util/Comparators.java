package astro.tool.box.util;

import java.util.Comparator;

public class Comparators {

	public static Comparator getStringComparator() {
		return Comparator.comparing((Object o) -> ((String) o));
	}

	public static Comparator getIntegerComparator() {
		return Comparator.comparing((Object o) -> Integer.valueOf(getStringValue(o)));
	}

	public static Comparator getLongComparator() {
		return Comparator.comparing((Object o) -> Long.valueOf(getStringValue(o)));
	}

	public static Comparator getDoubleComparator() {
		return Comparator.comparing((Object o) -> Double.valueOf(getStringValue(o)));
	}

	public static String getStringValue(Object obj) {
		String str = (String) obj;
		return str.isEmpty() ? "0" : str;
	}

}
