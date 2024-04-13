package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name="Playfield")
public class Playfield implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany
    private List<Tile> boardtiles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public void setBoardtiles(List<Tile> boardtiles) {
        this.boardtiles = boardtiles;
    }

    public List<Tile> getBoardtiles() {
        return boardtiles;
    }
}
