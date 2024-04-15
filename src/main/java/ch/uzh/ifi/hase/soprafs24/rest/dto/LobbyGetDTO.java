package ch.uzh.ifi.hase.soprafs24.rest.dto;


import ch.uzh.ifi.hase.soprafs24.constant.GameMode;

import java.util.ArrayList;
import java.util.List;

public class LobbyGetDTO {
    private Long id;

    private int numberOfPlayers;

    private int lobbySize;

    private List<Long> usersInLobby;

    private boolean gameStarted;

    private GameMode mode;

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

}
