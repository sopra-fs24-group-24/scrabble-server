package ch.uzh.ifi.hase.soprafs24.rest.dto;


import ch.uzh.ifi.hase.soprafs24.constant.GameMode;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.util.ArrayList;
import java.util.List;

public class LobbyGetDTO {
    private Long id;

    private int numberOfPlayers;

    private int lobbySize;

    private List<Long> usersInLobby;

    private boolean gameStarted;

    private GameGetDTO gameOfLobby;

    private GameMode mode;

    private List<UserSlimGetDTO> players;

    private String title;

    private boolean isPrivate;

    private String pin;

    public void setIsPrivate(boolean IsPrivate) {
        isPrivate = IsPrivate;
    }

    public boolean getIsPrivate(){
        return isPrivate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }


    public void setGameOfLobby(GameGetDTO gameOfLobby) {
        this.gameOfLobby = gameOfLobby;
    }

    public GameGetDTO getGameOfLobby() {
        return gameOfLobby;
    }

    public void setPlayers(List<UserSlimGetDTO> players) {
        this.players = players;
    }

    public List<UserSlimGetDTO> getPlayers() {
        return players;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public int getNumberOfPlayers() { return numberOfPlayers; }

    public void setNumberOfPlayers(int numberOfPlayers) { this.numberOfPlayers = numberOfPlayers; }

    public int getLobbySize() { return lobbySize; }

    public void setLobbySize(int lobbySize) { this.lobbySize = lobbySize; }

    public List<Long> getUsersInLobby() { return usersInLobby; }

    public void setUsersInLobby(List<Long> usersInLobby) { this.usersInLobby = usersInLobby; }

    public boolean getGameStarted() { return gameStarted; }

    public void setGameStarted(boolean gameStarted) { this.gameStarted = gameStarted; }

    public String getPin() { return pin; }

    public void setPin(String pin) { this.pin = pin; }

}
