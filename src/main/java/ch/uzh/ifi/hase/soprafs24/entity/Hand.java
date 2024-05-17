package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Hand")
public class Hand implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column (unique = true)
    private Long handuserid;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<Tile> handtiles;

    public void setHanduserid(Long handuserid) {
        this.handuserid = handuserid;
    }

    public Long getHanduserid() {
        return handuserid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setHandtiles(List<Tile> handtiles) {
        this.handtiles = handtiles;
    }

    public List<Tile> getHandtiles() {
        return handtiles;
    }

    private void removeTile(Tile tile) {
        handtiles.remove(tile);
    }

    private void addTile(Tile tile) {
        handtiles.add(tile);
    }


    public void putTilesInHand(List<Tile> tiles) {
        for (Tile tile : tiles) {
            addTile(tile);
        }
    }

    public void removeTilesFromHand(List<Tile> tiles) {
        List<Tile> tempList = new ArrayList<>(tiles);
        for (Tile tile : tempList) {
            removeTile(tile);
        }
    }
}
