package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.GameMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;


@Entity
@Table(name="Game")
public class Game implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Long currentPlayer;

    @OneToMany(cascade = CascadeType.ALL)
    private List<User> players;

    @Column
    private GameMode mode;

    @OneToOne(cascade = CascadeType.ALL)
    private Bag bag;

    @OneToMany(cascade = CascadeType.MERGE)
    private List<Tile> playfield;

    @OneToMany(cascade = CascadeType.MERGE)
    private List<Tile> oldPlayfield;

    @Column(nullable = false)
    private boolean wordContested;

    @Column
    @ElementCollection
    private Map<Long, Boolean> decisionPlayersContestation;

    @OneToMany(cascade= CascadeType.ALL)
    private List<Hand> hands;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Score> scores;

    public void setPlayers(List<User> players) {
        this.players = players;
    }

    public List<User> getPlayers() {
        return players;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }

    public List<Score> getScores() {
        return scores;
    }

    public Bag getBag() {
        return bag;
    }

    public void setBag(Bag bag) {
        this.bag = bag;
    }

    public void setPlayfield(List<Tile> playfield) {
        this.playfield = playfield;
    }

    public void setDecisionPlayersContestation(Map<Long, Boolean> decisionPlayersContestation) {
        this.decisionPlayersContestation = decisionPlayersContestation;
    }

    public Map<Long, Boolean> getDecisionPlayersContestation(){
        return decisionPlayersContestation;
    }

    public void addDecision(Long userId, boolean wordContested){
        Map<Long, Boolean> getMap = getDecisionPlayersContestation();
        getMap.put(userId, wordContested);
        setDecisionPlayersContestation(getMap);
    }

    public List<Tile> getPlayfield() {
        List<Tile> filledfield = new ArrayList<>();
        for (int i = 0; i< 225;i++){
            filledfield.add(null);
        }
        for (int i = 0; i<this.playfield.size(); i++){
            if (this.playfield.get(i)!= null){
                filledfield.set(this.playfield.get(i).getBoardidx(), this.playfield.get(i));
            }
        }
        return filledfield;
    }

    public void setOldPlayfield(List<Tile> oldPlayfield) {
        this.oldPlayfield = oldPlayfield;
    }

    public List<Tile> getOldPlayfield() {
        return oldPlayfield;
    }

    public void setWordContested(boolean wordContested) { this.wordContested = wordContested; }

    public boolean getWordContested() { return wordContested; }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public List<Hand> getHands() {
        return hands;
    }

    public void setHands(List<Hand> hands) {
        this.hands = hands;
    }

    public Long getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Long currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void initialiseGame(List<User> players)
    {
        setPlayers(players);

        // Initialise playfield:
        List<Tile> playfield = new ArrayList<Tile>();

        for(int i=0;i<225;i++) {
            playfield.add(null);
        }
        setPlayfield(playfield);
        Bag bag =new Bag();
        bag.initialisebag();
        setBag(bag);

        // Initialise the hands (give them 7 tiles each)
        List<Hand> allHands = new ArrayList<Hand>();
        List<Score> allScores = new ArrayList<Score>();

        for(int i=0;i<this.getPlayers().size();i++)
        {
            Hand temp_hand=new Hand();
            temp_hand.setHanduserid(this.getPlayers().get(i).getId());
            temp_hand.setHandtiles(bag.getSomeTiles(7));
            allHands.add(temp_hand);

            Score temp_score=new Score();
            temp_score.setScoreUserId(this.getPlayers().get(i).getId());
            temp_score.setScore(0);
            allScores.add(temp_score);
        }

        setHands(allHands);
        setScores(allScores);

        // Randomly choose the starting player
        this.setCurrentPlayer(players.get(randomInt(0, players.size()-1)).getId());
    }

    public User getNextPlayer() {
        Optional<User> currentPlayer = getPlayers().stream().filter(player -> player.getId().equals(getCurrentPlayer())).findFirst();

        int currentIndex = getPlayers().indexOf(currentPlayer.get());

        return getPlayers().get((currentIndex + 1) % getPlayers().size());
    }

    private static int randomInt(int min, int max)
    {
        return min + (int)(Math.random() * ((max - min) + 1));
    }
}
