package astro.tool.box.function;

import static astro.tool.box.function.AstrometricFunctions.calculateParallacticDistance;
import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import astro.tool.box.container.StringPair;
import astro.tool.box.enumeration.Color;
import astro.tool.box.lookup.LookupResult;
import astro.tool.box.lookup.SpectralTypeLookup;

public class PhotometricFunctions {

	/**
	 * Evaluate spectral type
	 *
	 * @param colorKey
	 * @param colorValue
	 * @param minEntry
	 * @param maxEntry
	 * @return the spectral type
	 */
	public static LookupResult evaluateSpectralType(Color colorKey, double colorValue, SpectralTypeLookup minEntry,
			SpectralTypeLookup maxEntry) {
		Double minColorValue = minEntry.getColors().get(colorKey);
		Double maxColorValue = maxEntry.getColors().get(colorKey);
		if (minColorValue == null || maxColorValue == null || minColorValue == 0 || maxColorValue == 0
				|| colorValue == 0) {
			return null;
		}
		if (minColorValue > maxColorValue) {
			double tempColorValue = minColorValue;
			minColorValue = maxColorValue;
			maxColorValue = tempColorValue;
			SpectralTypeLookup tempEntry = minEntry;
			minEntry = maxEntry;
			maxEntry = tempEntry;
		}
		double offset = 0.5;
		double avgColorValue = (minColorValue + maxColorValue) / 2;
		if (colorValue >= minColorValue && colorValue < avgColorValue && colorValue <= minColorValue + offset) {
			return new LookupResult(colorKey, colorValue, minEntry.getSpt(), minEntry.getTeff(), minEntry.getRsun(),
					minEntry.getMsun(), minColorValue, abs(colorValue - minColorValue));
		} else if (colorValue >= avgColorValue && colorValue <= maxColorValue && colorValue >= maxColorValue - offset) {
			return new LookupResult(colorKey, colorValue, maxEntry.getSpt(), maxEntry.getTeff(), maxEntry.getRsun(),
					maxEntry.getMsun(), maxColorValue, abs(colorValue - maxColorValue));
		} else {
			return null;
		}
	}

	/**
	 * Calculate photometric distance modulus
	 *
	 * @param apparent
	 * @param absolute
	 * @return the photometric distance in parsecs
	 */
	public static double calculatePhotometricDistance(double apparent, double absolute) {
		return pow(10, (apparent - absolute + 5) / 5);
	}

	/**
	 * Calculate absolute magnitude from parallax
	 *
	 * @param apparentMagnitude (mag)
	 * @param parallax          (mas)
	 * @return the absolute magnitude (mag)
	 */
	public static double calculateAbsoluteMagnitudeFromParallax(double apparentMagnitude, double parallax) {
		if (apparentMagnitude == 0 || parallax == 0) {
			return 0;
		} else {
			double absoluteMagnitude = apparentMagnitude + 5 - 5 * log10(calculateParallacticDistance(parallax));
			return Double.isInfinite(absoluteMagnitude) || Double.isNaN(absoluteMagnitude) ? 0 : absoluteMagnitude;
		}
	}

	/**
	 * Calculate absolute magnitude from parallax error
	 *
	 * @param m   (apparent magnitude)
	 * @param e_m (apparent magnitude error)
	 * @param p   (parallax)
	 * @param e_p (parallax error)
	 * @return the absolute magnitude from parallax error
	 */
	public static double calculateAbsoluteMagnitudeFromParallaxError(double m, double e_m, double p, double e_p) {
		if (m == 0 || p == 0) {
			return 0;
		} else {
			return sqrt(e_m * e_m + pow(-((1 / p * 5) / (1000 * log(10)) * -1000) * e_p, 2));
		}
	}

	/**
	 * Calculate absolute magnitude from distance
	 *
	 * @param apparentMagnitude (mag)
	 * @param distance          (pc)
	 * @return the absolute magnitude (mag)
	 */
	public static double calculateAbsoluteMagnitudeFromDistance(double apparentMagnitude, double distance) {
		if (distance == 0) {
			return 0;
		} else {
			return apparentMagnitude + 5 - 5 * log10(distance);
		}
	}

	/**
	 * Check if the object in question is a possible AGN
	 *
	 * @param W1_W2
	 * @param W2_W3
	 * @return a boolean indicating that the object is a possible AGN
	 */
	public static boolean isAPossibleAGN(double W1_W2, double W2_W3) {
		return W1_W2 >= 0.5 && W1_W2 <= 2.7 && W2_W3 >= 2.3 && W2_W3 <= 5.7;
	}

	/**
	 * Check if the object in question is a possible white dwarf
	 *
	 * @param MGmag
	 * @param BP_RP
	 * @return a boolean indicating that the object is a possible white dwarf
	 */
	public static boolean isAPossibleWD(double MGmag, double BP_RP) {
		return MGmag >= 10 && MGmag <= 17 && BP_RP != 0 && BP_RP <= 1.5;
	}

	/**
	 * Get binary flag labels
	 *
	 * @param flag
	 * @param flagStore
	 * @return the binary flag labels
	 */
	public static List<StringPair> getFlagLabels(Integer flag, Map<Integer, String> flagStore) {
		List<StringPair> flagLabels = new ArrayList<>();
		char[] chars = Integer.toBinaryString(flag).toCharArray();
		int x = 1;
		for (int i = chars.length - 1; i > -1; i--) {
			if (chars[i] == '1') {
				flagLabels.add(new StringPair(String.valueOf(x), flagStore.get(x)));
			}
			x *= 2;
		}
		return flagLabels;
	}

	/**
	 * Convert magnitude to Janskys (non-SI unit of flux density)
	 *
	 * @param magnitude
	 * @param zeroPointFlux
	 * @return F(λ) (Jy)
	 */
	public static double convertMagnitudeToFluxJansky(double magnitude, double zeroPointFlux) {
		return magnitude == 0 ? 0 : zeroPointFlux * pow(10, -magnitude / 2.5);
	}

	/**
	 * Convert magnitude to flux density λF(λ)
	 *
	 * @param magnitude
	 * @param zeroPointFlux
	 * @param wavelength
	 * @return λF(λ) (W/m^2)
	 */
	public static double convertMagnitudeToFluxDensity(double magnitude, double zeroPointFlux, double wavelength) {
		return convertMagnitudeToFluxJansky(magnitude, zeroPointFlux) * pow(10, -26 /* should be +26 */)
				* (299792458 / wavelength * 1000000 /* should be 10000 */);
	}

	/**
	 * Convert magnitude to flux density F(λ)
	 *
	 * @param magnitude
	 * @param zeroPointFlux
	 * @param wavelength
	 * @return F(λ) (W/m^2/μm)
	 */
	public static double convertMagnitudeToFluxLambda(double magnitude, double zeroPointFlux, double wavelength) {
		return convertMagnitudeToFluxDensity(magnitude, zeroPointFlux, wavelength) / wavelength;
	}

	/**
	 * Calculate photometric distance error
	 *
	 * @param a  (apparent magnitude)
	 * @param ae (apparent magnitude error)
	 * @param b  (absolute magnitude)
	 * @param be (absolute magnitude error)
	 * @return the photometric distance error
	 */
	public static double calculatePhotometricDistanceError(double a, double ae, double b, double be) {
		if (ae == 0 || be == 0) {
			return 0;
		}
		return sqrt(pow(((log(10) * pow(10, ((a - b + 5) / 5))) / 5) * ae, 2)
				+ pow(((pow(10, ((a - b + 5) / 5)) * -1) / 5) * log(10) * be, 2));
	}

	/**
	 * Calculate mean photometric distance error
	 *
	 * @param ae (min. photometric distance error)
	 * @param be (max. photometric distance error
	 * @return the mean photometric distance error
	 */
	public static double calculateMeanPhotometricDistanceError(double ae, double be) {
		return sqrt(pow(0.5 * ae, 2) + pow(0.5 * be, 2));
	}

}
