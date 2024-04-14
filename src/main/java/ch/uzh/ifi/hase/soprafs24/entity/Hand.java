package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name="Hand")
public class Hand implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column
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
}
