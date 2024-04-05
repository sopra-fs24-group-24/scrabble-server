package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class LobbyPostDTO {
    private int LobbySize;

    private List<Long> UsersInLobby;

    public int getLobbySize() { return LobbySize; }

    public void setLobbySize(int LobbySize) { this.LobbySize = LobbySize; }

    public List<Long> getUsersInLobby() { return UsersInLobby; }

    public void setUsersInLobby(List<Long> UsersInLobby) { this.UsersInLobby = UsersInLobby; }
}
