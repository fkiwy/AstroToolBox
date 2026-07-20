package astro.tool.box.enumeration;

import static astro.tool.box.util.Constants.*;

public enum TapProvider {

	ESA_EUCLID(ESA_EUCLID_BASE_URL), ESA_GAIA(ESA_GAIA_BASE_URL), IRSA(IRSA_TAP_URL), NOIRLAB(NOIRLAB_BASE_URL),
	VIZIER(VIZIER_BASE_URL);

	public String val;

	TapProvider(String val) {
		this.val = val;
	}

}
