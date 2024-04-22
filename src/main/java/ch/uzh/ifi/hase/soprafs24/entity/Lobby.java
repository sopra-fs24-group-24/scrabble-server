package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.GameMode;

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
    @Column(nullable = true)
    private GameMode mode;

    @Column(nullable = false)
    @ElementCollection
    private List<Long> usersInLobby;

    @OneToMany(cascade = CascadeType.ALL)
    private List<User> players;

    @Column(nullable = false)
    private boolean gameStarted;

    @OneToOne(cascade = CascadeType.ALL)
    private Game gameOfLobby;

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setGameOfLobby(Game gameOfLobby) {
        this.gameOfLobby = gameOfLobby;
    }

    public Game getGameOfLobby() {
        return gameOfLobby;
    }


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

    public List<User> getPlayers() { return players; }

    public void setPlayers(List<User> players) { this.players = players; }

    private boolean startGame() {
        //Check if lobby already started a Game
        if (this.gameStarted){
            return false;
        }
        this.gameStarted = true;
        Game game =new Game();
        game.initialiseGame(new ArrayList<>(this.players));
        setGameOfLobby(game);
        return true;
    }

    public boolean addPlayer(User user, Lobby lobby) {
        if (this.numberOfPlayers > 0 && this.numberOfPlayers < this.lobbySize - 1){
            this.numberOfPlayers += 1;
            this.usersInLobby.add(user.getId());
            List<User> players = lobby.getPlayers();
            players.add(user);
            lobby.setPlayers(players);
            return true;
        }
        // if lobby is full, the method startGame() is called
        else if (this.numberOfPlayers > 0 && this.numberOfPlayers == this.lobbySize - 1) {
            this.numberOfPlayers += 1;
            this.usersInLobby.add(user.getId());
            List<User> players = lobby.getPlayers();
            players.add(user);
            lobby.setPlayers(players);
            startGame();
            return true;
        }
        return false;
    }

    public boolean removePlayer(User user) {
        if (this.numberOfPlayers >= 1) {
            this.numberOfPlayers -= 1;
            this.usersInLobby.remove(user.getId());
            List<User> players = this.players;
            players.remove(user);
            this.players = players;
            return true;
        }
        return false;
    }

}
