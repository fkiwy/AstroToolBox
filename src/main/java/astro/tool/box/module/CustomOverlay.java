package astro.tool.box.module;

import java.awt.Color;
import java.io.File;
import javax.swing.JCheckBox;

public class CustomOverlay {

    private String name;

    private Color color;

    private int raColumnIndex;

    private int decColumnIndex;

    private File file;

    private JCheckBox checkBox;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getRaColumnIndex() {
        return raColumnIndex;
    }

    public void setRaColumnIndex(int raColumnIndex) {
        this.raColumnIndex = raColumnIndex;
    }

    public int getDecColumnIndex() {
        return decColumnIndex;
    }

    public void setDecColumnIndex(int decColumnIndex) {
        this.decColumnIndex = decColumnIndex;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public JCheckBox getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(JCheckBox checkBox) {
        this.checkBox = checkBox;
    }

}
