package astro.tool.box.module;

import java.util.Locale;
import javax.swing.JFrame;

public class Run {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        Application application = new Application();
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.init();
    }

}
