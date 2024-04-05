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
    private int NumberOfPlayers;

    @Column(nullable = false)
    private int LobbySize;

    @Column(nullable = false)
    @ElementCollection
    private List<Long> UsersInLobby;

    @Column(nullable = false)
    private boolean GameStarted;

    private Game GameOfLobby;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumberOfPlayers() { return NumberOfPlayers; }

    public void setNumberOfPlayers(int NumberOfPlayers) { this.NumberOfPlayers = NumberOfPlayers; }

    public int getLobbySize() { return LobbySize; }

    public void setLobbySize(int LobbySize) { this.LobbySize = LobbySize; }

    public boolean getGameStarted() { return GameStarted; }

    public void setGameStarted(boolean GameStarted) { this.GameStarted = GameStarted; }

    public boolean startGame() {
        //Check if lobby already started a Game
        if (this.GameStarted){
            return false;
        }
        this.GameStarted = true;
        this.GameOfLobby = new Game();
        return true;
    }

    public boolean addPlayer(Long UserId) {
        if (this.NumberOfPlayers == 0){
            this.UsersInLobby = new ArrayList<>();
            this.UsersInLobby.add(UserId);
            this.NumberOfPlayers += 1;
            return true;
        }
        else if (this.NumberOfPlayers > 0 && this.NumberOfPlayers < this.LobbySize){
            this.NumberOfPlayers += 1;
            this.UsersInLobby.add(UserId);
            return true;
        }
        return false;
    }

    public boolean removePlayer(Long UserId){
        int index = this.UsersInLobby.indexOf(UserId);

        if (this.NumberOfPlayers == 0 || index == -1){
            return false;
        }
        else if (index != -1) {
            this.NumberOfPlayers -= 1;
            this.UsersInLobby.remove(index);
            return true;
        }
        return false;
    }

}
