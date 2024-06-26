package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.GameMode;
import ch.uzh.ifi.hase.soprafs24.entity.Hand;
import ch.uzh.ifi.hase.soprafs24.entity.Score;
import ch.uzh.ifi.hase.soprafs24.entity.Tile;

import java.util.List;

public class GameGetDTO {
    private Long id;

    private Long currentPlayer;

    private List<Hand> hands;

    private List<Score> scores;

    private List<Tile> playfield;

    private GameMode mode;

    private List<UserSlimGetDTO> players;

    private boolean wordContested;

    private boolean gameOver;

    private boolean isValid;

    private boolean contestingPhase;

    private String invalidWord;

    private List<String> wordsToBeContested;

    private Long gameRound;

    private Boolean allPlayersDecided;

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

    public void setPlayers(List<UserSlimGetDTO> players) {
        this.players = players;
    }

    public List<UserSlimGetDTO> getPlayers() {
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

    public boolean getGameOver()
    {
        return gameOver;
    }

    public void setGameOver(boolean state)
    {
        gameOver=state;
    }

    public void setIsValid(boolean isValid) { this.isValid = isValid; }

    public boolean getIsValid() { return isValid; }

    public void setContestingPhase(boolean contestingPhase) { this.contestingPhase = contestingPhase; }

    public boolean getContestingPhase() { return contestingPhase; }

    public void setInvalidWord(String invalidWord) { this.invalidWord = invalidWord; }

    public String getInvalidWord() { return invalidWord; }

    public void setWordsToBeContested(List<String> wordsToBeContested) { this.wordsToBeContested = wordsToBeContested; }

    public List<String> getWordsToBeContested() { return wordsToBeContested; }

    public void setGameRound(Long gameRound) { this.gameRound = gameRound; }

    public Long getGameRound() { return gameRound; }

    public void setAllPlayersDecided(Boolean allPlayersDecided) { this.allPlayersDecided = allPlayersDecided; }

    public Boolean getAllPlayersDecided() { return allPlayersDecided; }
}
