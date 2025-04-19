package astro.tool.box.enumeration;

import static astro.tool.box.util.Constants.ESAC_BASE_URL;
import static astro.tool.box.util.Constants.IRSA_TAP_URL;
import static astro.tool.box.util.Constants.NOIRLAB_BASE_URL;
import static astro.tool.box.util.Constants.VIZIER_BASE_URL;

public enum TapProvider {

    ESAC(ESAC_BASE_URL),
    IRSA(IRSA_TAP_URL),
    NOIRLAB(NOIRLAB_BASE_URL),
    VIZIER(VIZIER_BASE_URL);

    public String val;

    private TapProvider(String val) {
        this.val = val;
    }

}
