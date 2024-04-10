package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Array;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name="Playfield")
public class Playfield implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany
    private List<Cell> board;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
