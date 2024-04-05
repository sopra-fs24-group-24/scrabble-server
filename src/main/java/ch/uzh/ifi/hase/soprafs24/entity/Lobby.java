package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="Lobby")
public class Lobby implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private int numberOfPlayers;

    @Column(nullable = false)
    private int lobbySize;

    @Column(nullable = false)
    @ElementCollection
    private List<Long> usersInLobby;

    @Column(nullable = false)
    private boolean gameStarted;

    private Game gameOfLobby;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumberOfPlayers() { return numberOfPlayers; }

    public void setNumberOfPlayers(int numberOfPlayers) { this.numberOfPlayers = numberOfPlayers; }

    public int getLobbySize() { return lobbySize; }

    public void setLobbySize(int lobbySize) { this.lobbySize = lobbySize; }

    public boolean getGameStarted() { return gameStarted; }

    public void setGameStarted(boolean gameStarted) { this.gameStarted = gameStarted; }

    public List<Long> getUsersInLobby() { return usersInLobby; }

    public void setUsersInLobby(List<Long> usersInLobby) { this.usersInLobby = usersInLobby; }

    public boolean startGame() {
        //Check if lobby already started a Game
        if (this.gameStarted){
            return false;
        }
        this.gameStarted = true;
        this.gameOfLobby = new Game();
        return true;
    }

    public boolean addPlayer(Long UserId) {
        if (this.numberOfPlayers == 0){
            this.usersInLobby = new ArrayList<>();
            this.usersInLobby.add(UserId);
            this.numberOfPlayers += 1;
            return true;
        }
        else if (this.numberOfPlayers > 0 && this.numberOfPlayers < this.lobbySize){
            this.numberOfPlayers += 1;
            this.usersInLobby.add(UserId);
            return true;
        }
        return false;
    }

    public boolean removePlayer(Long UserId){
        int index = this.usersInLobby.indexOf(UserId);

        if (this.numberOfPlayers == 0 || index == -1){
            return false;
        }
        else if (index != -1) {
            this.numberOfPlayers -= 1;
            this.usersInLobby.remove(index);
            return true;
        }
        return false;
    }

}
