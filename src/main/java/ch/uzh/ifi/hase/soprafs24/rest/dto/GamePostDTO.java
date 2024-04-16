package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.entity.Tile;

import java.util.List;

public class GamePostDTO {
    private Long id;
    private Long currentPlayer;
    private List<Tile> playfield;

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

    public List<Tile> getPlayfield() {
        return playfield;
    }

    public void setPlayfield(List<Tile> playfield) {
        this.playfield = playfield;
    }
}
