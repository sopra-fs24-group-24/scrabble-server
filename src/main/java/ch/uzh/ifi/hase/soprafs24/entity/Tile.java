package ch.uzh.ifi.hase.soprafs24.entity;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="Tile")
public class Tile implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable=false)
    private char letter;

    @Column(nullable=false)
    private int value;

    public Tile(char letter, int value)
    {
        this.setLetter(letter);
        this.setValue(value);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String toString()
    {
        return this.id+":"+ this.getLetter()+" "+this.getValue();
    }
}
