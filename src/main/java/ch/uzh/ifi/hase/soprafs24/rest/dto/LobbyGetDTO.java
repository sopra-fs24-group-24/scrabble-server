package ch.uzh.ifi.hase.soprafs24.rest.dto;


import java.util.List;

public class LobbyGetDTO {
    private Long id;

    private int NumberOfPlayers;

    private int LobbySize;

    private List<Long> UsersInLobby;

    private boolean GameStarted;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public int getNumberOfPlayers() { return NumberOfPlayers; }

    public void setNumberOfPlayers(int NumberOfPlayers) { this.NumberOfPlayers = NumberOfPlayers; }

    public int getLobbySize() { return LobbySize; }

    public void setLobbySize(int LobbySize) { this.LobbySize = LobbySize; }

    public List<Long> getUsersInLobby() { return UsersInLobby; }

    public void setUsersInLobby(List<Long> UsersInLobby) { this.UsersInLobby = UsersInLobby; }

    public boolean getGameStarted() { return GameStarted; }

    public void setGameStarted(boolean GameStarted) { this.GameStarted = GameStarted; }

}
