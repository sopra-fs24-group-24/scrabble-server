package ch.uzh.ifi.hase.soprafs24.entity;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

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

    @Column(nullable = true)
    private int boardidx;
    public Tile(char letter, int value){
        this.letter = letter;
        this.value = value;
    }

    public Tile() {

    }

    public void setBoardidx(int boardidx) {
        this.boardidx = boardidx;
    }

    public int getBoardidx() {
        return boardidx;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this){
            return true;
        }
        if (o == null){
            return false;
        }
        if (getClass() != o.getClass()){
            return false;
        }
        Tile other = (Tile) o;
        if (!this.id.equals(other.id))
        {
            return false;
        }
        /*if (this.value != other.value){
            return false;
        }*/
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(letter, value);
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
