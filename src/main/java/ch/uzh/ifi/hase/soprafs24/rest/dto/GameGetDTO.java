package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.entity.Hand;
import ch.uzh.ifi.hase.soprafs24.entity.Playfield;
import ch.uzh.ifi.hase.soprafs24.entity.Score;

import java.util.List;

public class GameGetDTO {
    private Long id;

    private Long currentPlayer;

    private List<Hand> hands;

    private List<Score> scores;

    private Playfield playfield;

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

    public Playfield getPlayfield() {
        return playfield;
    }

    public void setPlayfield(Playfield playfield) {
        this.playfield = playfield;
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
