package astro.tool.box.main;

import java.io.InputStream;
import java.util.Locale;
import java.util.logging.LogManager;
import javax.swing.JFrame;

public class Run {

    public static void main(String[] args) throws Exception {
        InputStream input = Run.class.getResourceAsStream("/logging.properties");
        LogManager.getLogManager().readConfiguration(input);
        Locale.setDefault(Locale.US);
        Application application = new Application();
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.init();
    }

}
