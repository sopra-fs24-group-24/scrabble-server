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

    @OneToMany(cascade = CascadeType.ALL)
    private List<Tile> playfield;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Tile> oldPlayfield;

    @Column(nullable = false)
    private boolean wordContested;

    @Column(nullable = false)
    private boolean gameOver;

    @Column(nullable = false)
    private int globalSkipCounter;

    @Column
    @ElementCollection
    private Map<Long, Boolean> decisionPlayersContestation;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Hand> hands;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Score> scores;

    @Column
    private boolean isValid;

    @Column
    private boolean contestingPhase;

    @Column
    private String invalidWord;

    @Column
    @ElementCollection
    private List<String> wordsToBeContested;

    @Column
    private Long gameRound;

    @Column
    private Boolean allPlayersDecided;

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
        List<Tile> filledfield = new ArrayList<>();
        for (int i = 0; i< 225;i++){
            filledfield.add(null);
        }
        for (int i = 0; i<this.oldPlayfield.size(); i++){
            if (this.oldPlayfield.get(i)!= null){
                filledfield.set(this.oldPlayfield.get(i).getBoardidx(), this.oldPlayfield.get(i));
            }
        }
        return filledfield;
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

    public boolean getGameOver()
    {
        return gameOver;
    }

    public int getGlobalSkipCounter()
    {
        return globalSkipCounter;
    }

    public void setGameOver(boolean state)
    {
        gameOver=state;
    }

    public void setGlobalSkipCounter(int count)
    {
        globalSkipCounter=count;
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

    public void initialiseGame(List<User> players)
    {
        gameOver=false;
        globalSkipCounter=0;

        setGameRound(1L);
        setPlayers(players);
        int sizeOfGame = getPlayers().size();
        if (sizeOfGame == 2){
            setAllPlayersDecided(true);
        }
        else{
            setAllPlayersDecided(false);
        }

        // Initialise playfield:
        List<Tile> playfield = new ArrayList<Tile>();
        List<Tile> oldPlayfield = new ArrayList<Tile>();

        for(int i=0;i<225;i++) 
        {
            playfield.add(null);
            oldPlayfield.add(null);
        }
        setPlayfield(playfield);
        setOldPlayfield(oldPlayfield);

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

    private int getTotalTileValue(Hand hand)
    {
        int sum=0;

        for(Tile tile: hand.getHandtiles())
        {
            sum+=tile.getValue();
        }

        return sum;
    }

    private void adjustFinalScores()
    {
        Long emptyHandUserId=-1L;
        int sumOfHandTiles=0;

        for(Hand hand: this.getHands())
        {
            if(hand.getHandtiles().size()==0)
            {
                emptyHandUserId=hand.getHanduserid();
            }
            else
            {
                sumOfHandTiles+=getTotalTileValue(hand);
            }
        }

        /*
            one hand is empty: adjust scores by subtracting sum of tile values from each non-empty hand and adding th
            the total of all the remaining tiles of all hands to the score of the empty hand
        */
        
  
        for(Hand hand: this.getHands())
        {
            for(Score score: this.getScores())
            {
                if(hand.getHanduserid()==score.getScoreUserId())
                {
                    if(score.getScoreUserId()==emptyHandUserId)
                    {
                        score.setScore(score.getScore()+sumOfHandTiles);
                    }
                    else
                    {
                        score.setScore(score.getScore()-getTotalTileValue(hand));
                    }
                }
            }
        }
        
    }

    public Game initialiseGameOver()
    {
        // take care of updating the scores, looking at the different hands...
        adjustFinalScores();

        setGameOver(true);

        return this;
    }

    public void removeHand(Long userId)
    {
        for (Hand hand : getHands()) 
        {
            if(hand.getHanduserid()==userId)
            {
                bag.putTilesInBag(hand.getHandtiles());
                hand.setHanduserid(null);
                hand=null;
                getHands().remove(hand);
                return;
            }    
        }
    }
/*
    public Game removePlayer(User user)
    {
        // if current player, switch to next before removing
        if(getCurrentPlayer()==user.getId())
        {
            User nextPlayer = getNextPlayer();
            setCurrentPlayer(nextPlayer.getId());
        }



        index = 0;
        for (User player : getPlayers()){
            if (Objects.equals(player.getId(), user.getId())){
                break;
            }
            index++;
        }
        getPlayers().remove(index);

        // remove hand
        //removeHand(user.getId());
        //this.players.remove(user);
        return this;
    }*/

   
}
