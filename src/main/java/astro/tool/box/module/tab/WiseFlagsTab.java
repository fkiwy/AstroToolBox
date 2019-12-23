package astro.tool.box.module.tab;

import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.module.HtmlText;
import java.awt.FlowLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

public class WiseFlagsTab {

    private static final String TAB_NAME = "WISE Flags";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;

    public WiseFlagsTab(JFrame baseFrame, JTabbedPane tabbedPane) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
    }

    public void init() {
        try {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBorder(new EmptyBorder(5, 5, 5, 5));

            HtmlText text = new HtmlText();
            text.boldRow("Contamination and confusion flag, one per band (cc_flags):");
            text.row("One character per band (W1/W2/W3/W4) that indicates that the photometry and/or position measurements of a source may be contaminated or biased due to proximity to an image artifact.");
            text.row("D,d = Diffraction spike. Source may be a spurious detection of or contaminated by a diffraction spike from a nearby bright star on the same image.");
            text.row("P,p = Persistence. Source may be a spurious detection of or contaminated by a latent image left by a bright star.");
            text.row("H,h = Halo. Source photometry may be a spurious detection of or contaminated by the scattered light halo surrounding a nearby bright source.");
            text.row("O,o = (letter \"o\") Optical ghost. Source may be a spurious detection of or contaminated by an optical ghost image caused by a nearby bright source.");
            text.row("0 = (number zero) Source is unaffected by known artifacts.");
            text.newLine();
            text.boldRow("Extended source flag (ext_flg):");
            text.row("0 = The source shape is consistent with a point-source and the source is not associated with or superimposed on a 2MASS XSC source.");
            text.row("1 = The profile-fit photometry goodness-of-fit is &gt; 3.0 in one or more bands.");
            text.row("2 = The source falls within the extrapolated isophotal footprint of a 2MASS XSC source.");
            text.row("3 = The profile-fit photometry goodness-of-fit is &gt; 3.0 in one or more bands, and the source falls within the extrapolated isophotal footprint of a 2MASS XSC source.");
            text.row("4 = The source position falls within 5\" of a 2MASS XSC source.");
            text.row("&gt; 5 = The profile-fit photometry goodness-of-fit is &gt; 3.0 in one or more bands, and the source position falls within 5\" of a 2MASS XSC source.");
            text.newLine();
            text.boldRow("Variability flag, one per band (var_flg):");
            text.row("One character per band (W1/W2/W3/W4) that contains a value related to the probability that the source flux measured on the individual WISE exposures is variable.");
            text.row("n = insufficient or inadequate data to make a determination (&lt; 6 exposures)");
            text.row("0-9 = increasing probabilities of variation");
            text.row("0-5 = most likely not variable");
            text.row("6-7 = likely variables (but susceptible of false-positive variability)");
            text.row("&gt; 7 = highest probability of being true variables");
            text.newLine();
            text.boldRow("Photometric quality flag, one per band (ph_qual):");
            text.row("One character per band (W1/W2/W3/W4) that provides a shorthand summary of the quality of the profile-fit photometry measurement in each band, as derived from the measurement signal-to-noise ratio.");
            text.row("A = Source is detected in this band with a flux signal-to-noise ratio over 10.");
            text.row("B = Source is detected in this band with a flux signal-to-noise ratio between 3 and 10.");
            text.row("C = Source is detected in this band with a flux signal-to-noise ratio between 2 and 3.");
            text.row("U = Upper limit on magnitude. Source measurement is below 2. The profile-fit magnitude is a 95% confidence upper limit.");
            text.row("X = A profile-fit measurement was not possible at this location in this band.");
            text.row("Z = A profile-fit source flux measurement was made at this location, but the flux uncertainty could not be measured.");
            text.newLine();
            text.boldRow("unWISE artifact bitmask contamination flags (ab_flags):");
            text.row("This flag indicates if one or more image pixels in the measurement aperture for this band is confused with nearby objects, is contaminated by saturated or otherwise ususable pixels, or is an upper limit.");
            text.row("The flag value is the integer sum of any of following values which correspond to different conditions. ");
            text.row("0 = nominal: no contamination");
            text.row("1 = source confusion: another source falls within the measurement aperture");
            text.row("2 = bad or fatal pixels: presence of bad pixels in the measurement aperture (bit 2 or 18 set)");
            text.row("4 = non-zero bit flag tripped (other than 2 or 18)");
            text.row("8 = corruption: all pixels are flagged as unusable, or the aperture flux is negative; in the former case, the aperture magnitude is NULL; in the latter case, the aperture magnitude is a 95% confidence upper limit");
            text.row("16 = saturation: here are one or more saturated pixels in the measurement aperture");
            text.row("32 = upper limit: the magnitude is a 95% confidence upper limit");

            JEditorPane editor = new JEditorPane("text/html", text.toString());
            editor.setBorder(new EmptyBorder(10, 10, 10, 10));
            editor.setEditable(false);
            panel.add(editor);

            tabbedPane.addTab(TAB_NAME, new JScrollPane(panel));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

}
