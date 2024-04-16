package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.GameMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name="Game")
public class Game implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long currentPlayer;

    @OneToMany(cascade = CascadeType.ALL)
    private List<User> players;

    @Column
    private GameMode mode;

    @OneToOne(cascade = CascadeType.ALL)
    private Bag bag;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Tile> playfield;

    @OneToMany(cascade= CascadeType.ALL)
    private ArrayList<Hand> hands;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Score> scores;

    public void setPlayers(List<User> players) {
        this.players = players;
    }

    public List<User> getPlayers() {
        return players;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }

    public List<Score> getScores() {
        return scores;
    }

    public Bag getBag() {
        return bag;
    }

    public void setBag(Bag bag) {
        this.bag = bag;
    }

    public void setPlayfield(List<Tile> playfield) {
        this.playfield = playfield;
    }

    public List<Tile> getPlayfield() {
        return playfield;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public ArrayList<Hand> getHands() {
        return hands;
    }

    public void setHands(ArrayList<Hand> hands) {
        this.hands = hands;
    }

    public Long getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Long currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void initialiseGame()
    {
        bag.Initialise();

        // Initialise the hands (give them 7 tiles each)
        ArrayList<Hand> allHands=getHands();

        // Give each hand 7 tiles:
        for(int i=0;i<allHands.size();i++)
        {
            allHands.get(i).setHandtiles(bag.getSomeTiles(7));
        }

        // Randomly choose the starting player
        List<User> players=this.getPlayers();
        this.setCurrentPlayer(players.get(randomInt(0, players.size()-1)).getId());
    }

    private static int randomInt(int min, int max)
    {
        return min + (int)(Math.random() * ((max - min) + 1));
    }
}
