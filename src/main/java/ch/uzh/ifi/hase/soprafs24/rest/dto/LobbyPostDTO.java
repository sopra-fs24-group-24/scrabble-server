package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.GameMode;

import java.util.List;

public class LobbyPostDTO {
    private int lobbySize;

    private List<Long> usersInLobby;

    private GameMode mode;

    private String title;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    private boolean isPrivate;

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public GameMode getMode() {
        return mode;
    }

    public int getLobbySize() { return lobbySize; }

    public void setLobbySize(int lobbySize) { this.lobbySize = lobbySize; }

    public List<Long> getUsersInLobby() { return usersInLobby; }

    public void setUsersInLobby(List<Long> usersInLobby) { this.usersInLobby = usersInLobby; }

    public void setIsPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public boolean getIsPrivate() { return isPrivate; }
}
