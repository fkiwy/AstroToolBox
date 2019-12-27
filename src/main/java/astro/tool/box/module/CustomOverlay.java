package astro.tool.box.module;

import astro.tool.box.container.catalog.CatalogEntry;
import java.awt.Color;
import java.io.File;
import java.util.List;
import javax.swing.JCheckBox;

public class CustomOverlay {

    private String name;

    private Color color;

    private int raColumnIndex;

    private int decColumnIndex;

    private File file;

    private JCheckBox checkBox;

    private List<CatalogEntry> catalogEntries;

    public String serialize() {
        StringBuilder data = new StringBuilder();
        data.append(name).append(";");
        data.append(color.getRGB()).append(";");
        data.append(raColumnIndex).append(";");
        data.append(decColumnIndex).append(";");
        data.append(file.getPath());
        return data.toString();
    }

    public void deserialize(String data) {
        String[] values = data.split(";");
        name = values[0];
        color = new Color(Integer.valueOf(values[1]));
        raColumnIndex = Integer.valueOf(values[2]);
        decColumnIndex = Integer.valueOf(values[3]);
        file = new File(values[4]);
    }

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

    public List<CatalogEntry> getCatalogEntries() {
        return catalogEntries;
    }

    public void setCatalogEntries(List<CatalogEntry> catalogEntries) {
        this.catalogEntries = catalogEntries;
    }

}
