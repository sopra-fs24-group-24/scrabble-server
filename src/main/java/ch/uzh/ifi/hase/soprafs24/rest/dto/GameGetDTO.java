package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.GameMode;
import ch.uzh.ifi.hase.soprafs24.entity.Hand;
import ch.uzh.ifi.hase.soprafs24.entity.Score;
import ch.uzh.ifi.hase.soprafs24.entity.Tile;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GameGetDTO {
    private Long id;

    private Long currentPlayer;

    private List<Hand> hands;

    private List<Score> scores;

    private List<Tile> playfield;

    private GameMode mode;

    private List<User> players;

    private boolean wordContested;

    public void setWordContested(boolean wordContested) { this.wordContested = wordContested; }

    public boolean getWordContested() { return wordContested; }

    public void setPlayfield(List<Tile> playfield) {
        this.playfield = playfield;
    }

    public List<Tile> getPlayfield() {
        return playfield;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setPlayers(List<User> players) {
        this.players = players;
    }

    public List<User> getPlayers() {
        return players;
    }

    public void setHands(List<Hand> hands) {
        this.hands = hands;
    }

    public List<Hand> getHands() {
        return hands;
    }

    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Long currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
