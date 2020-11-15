package astro.tool.box.container;

import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.enumeration.Shape;
import java.awt.Color;
import java.io.File;
import java.util.List;
import javax.swing.JCheckBox;

public class CustomOverlay {

    private String name;

    private Color color;

    private Shape shape;

    private int raColumnIndex;

    private int decColumnIndex;

    private File file;

    private String tableName;

    private String raColName;

    private String decColName;

    private JCheckBox checkBox;

    private List<CatalogEntry> catalogEntries;

    public void init() {
        name = null;
        color = null;
        shape = null;
        raColumnIndex = 0;
        decColumnIndex = 0;
        file = null;
        tableName = null;
        raColName = null;
        decColName = null;
        checkBox = null;
        catalogEntries = null;
    }

    public String serialize() {
        StringBuilder data = new StringBuilder();
        data.append(name).append(";");
        data.append(color.getRGB()).append(";");
        data.append(shape.name()).append(";");
        data.append(raColumnIndex).append(";");
        data.append(decColumnIndex).append(";");
        data.append(file == null ? "" : file.getPath()).append(";");
        data.append(tableName).append(";");
        data.append(raColName).append(";");
        data.append(decColName);
        return data.toString();
    }

    public void deserialize(String data) {
        String[] values = data.split(";", 9);
        name = values[0];
        color = new Color(Integer.valueOf(values[1]));
        shape = Shape.valueOf(values[2]);
        raColumnIndex = Integer.valueOf(values[3]);
        decColumnIndex = Integer.valueOf(values[4]);
        if (!values[5].isEmpty()) {
            file = new File(values[5]);
        }
        tableName = values[6];
        raColName = values[7];
        decColName = values[8];
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

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRaColName() {
        return raColName;
    }

    public void setRaColName(String raColName) {
        this.raColName = raColName;
    }

    public String getDecColName() {
        return decColName;
    }

    public void setDecColName(String decColName) {
        this.decColName = decColName;
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
