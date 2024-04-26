package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name="Bag")
public class Bag implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Tile> tiles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    private void addTile(Tile tile) { tiles.add(tile); }

    public int tilesleft(){
        return this.tiles.size();
    }

    public synchronized List<Tile> getSomeTiles(int numberOfTiles)
    {
        List<Tile> tilesToReturn = new ArrayList<>(tiles.subList(tiles.size()-numberOfTiles, tiles.size()));

        tiles = new ArrayList<>(tiles.subList(0,tiles.size()-numberOfTiles));

        return tilesToReturn;
    }

    public synchronized void putTilesInBag(List<Tile> tilesToBeExchanged) {
        for (Tile tile : tilesToBeExchanged) {
            addTile(tile);
        }
    }

    public void initialisebag()
    {
        // Make list of tiles for bag

        HashMap<String, Integer> tileCounts = new HashMap<String, Integer>();

        tileCounts.put("A", 9);
        tileCounts.put("B", 2);
        tileCounts.put("C", 2);
        tileCounts.put("D", 4);
        tileCounts.put("E", 12);
        tileCounts.put("F", 2);
        tileCounts.put("G", 3);

        tileCounts.put("H", 2);
        tileCounts.put("I", 9);
        tileCounts.put("J", 1);
        tileCounts.put("K", 1);
        tileCounts.put("L", 4);
        tileCounts.put("M", 2);
        tileCounts.put("N", 6);

        tileCounts.put("O", 8);
        tileCounts.put("P", 2);
        tileCounts.put("Q", 1);
        tileCounts.put("R", 6);
        tileCounts.put("S", 4);
        tileCounts.put("T", 6);
        tileCounts.put("U", 4);

        tileCounts.put("V", 2);
        tileCounts.put("W", 2);
        tileCounts.put("X", 1);
        tileCounts.put("Y", 2);
        tileCounts.put("Z", 1);
        tileCounts.put("0", 2);

        HashMap<String, Integer> tileValues = new HashMap<String, Integer>();

        tileValues.put("A", 1);
        tileValues.put("B", 3);
        tileValues.put("C", 3);
        tileValues.put("D", 2);
        tileValues.put("E", 1);
        tileValues.put("F", 4);
        tileValues.put("G", 2);

        tileValues.put("H", 4);
        tileValues.put("I", 1);
        tileValues.put("J", 8);
        tileValues.put("K", 5);
        tileValues.put("L", 1);
        tileValues.put("M", 3);
        tileValues.put("N", 1);

        tileValues.put("O", 1);
        tileValues.put("P", 3);
        tileValues.put("Q", 10);
        tileValues.put("R", 1);
        tileValues.put("S", 1);
        tileValues.put("T", 1);
        tileValues.put("U", 1);

        tileValues.put("V", 4);
        tileValues.put("W", 4);
        tileValues.put("X", 8);
        tileValues.put("Y", 4);
        tileValues.put("Z", 10);
        tileValues.put("0", 0);

        // Generate the tiles, add them to bag:

        List<Tile> tempTiles = new ArrayList<Tile>();

        for (Map.Entry<String, Integer> entry : tileCounts.entrySet()) 
        {
            String key = entry.getKey();
            int count = entry.getValue();
            // System.out.println("Key=" + key + ", Value=" + count);

            for (int i=0;i<count;i++)
            {
                Tile tile = new Tile();
                tile.setLetter(key.charAt(0));
                tile.setValue(tileValues.get(key));
                tempTiles.add(tile);
            }
        }

        // Shuffle the tiles
        Collections.shuffle(tempTiles);

        // Put tiles in bag
        this.setTiles(tempTiles);
    }
}
