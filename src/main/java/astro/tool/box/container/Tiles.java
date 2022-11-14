package astro.tool.box.container;

import java.util.List;

public class Tiles {

    private List<Tile> tiles;

    @Override
    public String toString() {
        return "Tiles{" + "tiles=" + tiles + '}';
    }

    public Tile getFirst() {
        if (tiles == null || tiles.isEmpty()) {
            throw new RuntimeException("No WISE tiles found!");
        }
        return tiles.get(0);
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

}
