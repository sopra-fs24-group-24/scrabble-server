package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.dictionary.Dictionary;
import ch.uzh.ifi.hase.soprafs24.entity.Bag;
import ch.uzh.ifi.hase.soprafs24.entity.Hand;
import ch.uzh.ifi.hase.soprafs24.entity.Tile;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.BagRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.HandRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ScoreRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.Game;

import java.net.http.HttpResponse;
import java.util.*;

@Service
@Transactional
public class GameService {

    private final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;
    private final HandRepository handRepository;
    private final ScoreRepository scoreRepository;
    private final BagRepository bagRepository;
    private final Dictionary dictionary;
    private final Bag bag;


    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository,
                       @Qualifier("handRepository") HandRepository handRepository,
                       @Qualifier("dictionary") Dictionary dictionary,
                       @Qualifier("scoreRepository") ScoreRepository scoreRepository,
                       @Qualifier("bagRepository") BagRepository bagRepository) {
        this.gameRepository = gameRepository;
        this.handRepository = handRepository;
        this.scoreRepository = scoreRepository;
        this.bagRepository = bagRepository;
        this.dictionary = dictionary;
        this.bag = new Bag();
        
    }



    public Game getGameParams(Long gameId)
    {
      Optional<Game> game=gameRepository.findById(gameId);
      if(game.isPresent())
          return game.get();
  
      String baseErrorMessage = "The %s provided can not be found!";
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(baseErrorMessage, "Game ID"));
    }

    public void skipTurn(User user, Long gameId) 
    {
        Optional<Game> game=gameRepository.findById(gameId);

        if (game.isPresent()) 
        {
            Game foundGame=game.get();

            int count=foundGame.getGlobalSkipCounter();
            foundGame.setGlobalSkipCounter(++count);

            // End if all players skip twice in a row
            if(count==2*foundGame.getPlayers().size())
            {
                foundGame.initialiseGameOver();
            }
            else
            {
                authenticateUserForMove(user, gameId);
                makeNextPlayerToCurrentPlayer(gameId);
            }
        } 
        else 
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("The game with id %s does not exist!", gameId));
        }      
    }

    public void makeNextPlayerToCurrentPlayer(Long gameId) {
        Optional<Game> game = gameRepository.findById(gameId);

        if (game.isPresent()) {
            User nextPlayer = game.get().getNextPlayer();
            game.get().setCurrentPlayer(nextPlayer.getId());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("The game with id %s does not exist!", gameId));
        }
    }

    public List<Tile> placeTilesOnBoard(Game game) {
        Game foundGame = checkIfGameExists(game.getId());
        checkIfBoardValid(game.getPlayfield());

        //TODO: add function to check whether userinput is sent from the correct player

        List<Tile> updatedPlayfield = game.getPlayfield();
        List<Tile> persistedPlayfield = foundGame.getPlayfield();

        // check if move is valid
        validMove(updatedPlayfield, persistedPlayfield);

        foundGame.setPlayfield(updatedPlayfield);
        foundGame.setContestingPhase(true);
        foundGame.setWordContested(false);
        gameRepository.save(foundGame);
        gameRepository.flush();

        return foundGame.getPlayfield();
    }

    private List<Integer> getChangedIndexesOfGame (List<Tile> previousField, List<Tile> currentField) {
        List<Integer> indices = new ArrayList<>();

        for (int i = 0; i < previousField.size(); i++) {
            if(previousField.get(i) == null && currentField.get(i) != null) {
                indices.add(i);
            }
        }

        return indices;
    }

    public void contestWord(Long gameId, User user, boolean playerContestWord){
        Game foundGame = checkIfGameExists(gameId);
        checkIfUserPartOfGame(foundGame, user.getId());

        if (foundGame.getCurrentPlayer().equals(user.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The player who placed the word cannot contest!");
        }

        int gameSize = foundGame.getPlayers().size();

        if (foundGame.getDecisionPlayersContestation() == null){
            Map<Long, Boolean> currentPlayer = new HashMap<>();
            currentPlayer.put(user.getId(), playerContestWord);
            foundGame.setDecisionPlayersContestation(currentPlayer);
        }
        else if (foundGame.getDecisionPlayersContestation() != null){
            // check if player already contested
            if (foundGame.getDecisionPlayersContestation().containsKey(user.getId())){
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "A player can only contest once!");
            }
            foundGame.addDecision(user.getId(), playerContestWord);
            // all players have decided whether to contest the word or not
            if (foundGame.getDecisionPlayersContestation().size() == gameSize-1){
                boolean wordContested = false;
                // check if someone wants to contest the word
                for (boolean value : foundGame.getDecisionPlayersContestation().values()){
                    if (value){
                        wordContested = true;
                        break;
                    }
                }
                // word is contested
                if (wordContested){
                    Map<String, Integer> words = getWordsAndScoreForPlayedTiles(foundGame.getPlayfield(), foundGame.getOldPlayfield(), true);
                    foundGame.setWordContested(true);
                    // word is correct - contestation unsuccessful
                    if (!words.isEmpty()) {
                        changeScoresAfterContesting(foundGame, foundGame.getDecisionPlayersContestation(), words);

                        List<Integer> playedTilesIndices = getChangedIndexesOfGame(foundGame.getOldPlayfield(), foundGame.getPlayfield());
                        List<Tile> tiles = new ArrayList<>();

                        for (int playedTileIndex : playedTilesIndices) {
                            tiles.add(foundGame.getPlayfield().get(playedTileIndex));
                        }

                        Hand currentPlayerHand = handRepository.findByHanduserid(foundGame.getCurrentPlayer());
                        currentPlayerHand.removeTilesFromHand(tiles);

                        List<Tile> newTiles;
                        Bag bag = foundGame.getBag();

                        if (bag.tilesleft() < tiles.size()) {
                            newTiles = bag.getSomeTiles(bag.tilesleft());
                        } else {
                            newTiles = foundGame.getBag().getSomeTiles(tiles.size());
                        }

                        currentPlayerHand.putTilesInHand(newTiles);

                        foundGame.setContestingPhase(false);
                        foundGame.setOldPlayfield(foundGame.getPlayfield());
                        foundGame.setIsValid(true);
                    }
                    // word is false - contestation successful
                    else{
                        //TODO: change score function
                        changeScoresAfterContesting(foundGame, foundGame.getDecisionPlayersContestation(), words);
                        foundGame.setPlayfield(foundGame.getOldPlayfield());
                        foundGame.setIsValid(false);
                    }
                }
                // word is not contested
                else{
                    Map<String, Integer> words = getWordsAndScoreForPlayedTiles(foundGame.getPlayfield(), foundGame.getOldPlayfield(), false);

                    int score = 0;

                    for (int wordScore : words.values()) {
                        score += wordScore;
                    }

                    Score playerScore = scoreRepository.findByScoreUserId(foundGame.getCurrentPlayer());
                    playerScore.setScore(playerScore.getScore() + score);

                    List<Integer> playedTilesIndices = getChangedIndexesOfGame(foundGame.getOldPlayfield(), foundGame.getPlayfield());
                    List<Tile> tiles = new ArrayList<>();

                    for (int playedTileIndex : playedTilesIndices) {
                        tiles.add(foundGame.getPlayfield().get(playedTileIndex));
                    }

                    Hand currentPlayerHand = handRepository.findByHanduserid(foundGame.getCurrentPlayer());
                    currentPlayerHand.removeTilesFromHand(tiles);

                    List<Tile> newTiles;
                    Bag bag = foundGame.getBag();

                    if (bag.tilesleft() < tiles.size()) {
                        newTiles = bag.getSomeTiles(bag.tilesleft());
                    } else {
                        newTiles = foundGame.getBag().getSomeTiles(tiles.size());
                    }

                    currentPlayerHand.putTilesInHand(newTiles);
                    foundGame.setOldPlayfield(foundGame.getPlayfield());
                }

                List<Tile> saveOldPlayfield = new ArrayList<>();
                for (int i = 0; i < 225; i++){
                    saveOldPlayfield.add(foundGame.getOldPlayfield().get(i));
                }

                List<Tile> tempPlayfield = new ArrayList<>();
                for (int i = 0; i < 225; i++){
                    tempPlayfield.add(null);
                }

                foundGame.setOldPlayfield(tempPlayfield);
                gameRepository.save(foundGame);
                gameRepository.flush();

                foundGame.setOldPlayfield(saveOldPlayfield);
                foundGame.setContestingPhase(false);
                foundGame.setDecisionPlayersContestation(null);
                makeNextPlayerToCurrentPlayer(gameId);
                gameRepository.save(foundGame);
                gameRepository.flush();
            }
        }
    }

    public boolean validateWord(String word) {
        HttpResponse<String> response = dictionary.getScrabbleScore(word);

        return response.statusCode() == 200;
    }

    public int getScrabbleScore(String word) {
        HttpResponse<String> response = dictionary.getScrabbleScore(word);

        if (response.statusCode() == HttpStatus.OK.value()) {
            return getJSON(response).get("value").asInt();
        } else {
            return 0;
        }
    }

    public void validMove(List<Tile> updatedPlayfield, List<Tile> persistedPlayfield) {
        // save the indices of the new tiles in a list
        List<Integer> updatedIndices = new ArrayList<Integer>();

        // search for the new tiles and add them to the list
        for (int index = 0; index < 225; index++) {
            if (updatedPlayfield.get(index) == null && persistedPlayfield.get(index) == null) {
                continue;
            }
            if (!updatedPlayfield.get(index).equals(persistedPlayfield.get(index))) {
                updatedIndices.add(index);
            }
        }

        int sizeOfUpdatedIndices = updatedIndices.size();
        // sort array with indices in ascending order
        updatedIndices.sort(Comparator.naturalOrder());

        // check if new tiles are placed on board
        if (sizeOfUpdatedIndices == 0){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You need to place at least 1 Tile");
        }

        // check if valid move

        // first word is placed on playfield
        if (persistedPlayfield.stream().allMatch(Objects::isNull)) {
            // since first word needs to be placed across middle of the board, the arraylist needs to include index 112
            if (!updatedIndices.contains(112) || sizeOfUpdatedIndices == 1) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "First word needs to contain at least 2 letters and has to be placed on the center of the board");
            }
            else {
                int indexOfFirstTile = updatedIndices.get(0);
                int indexOfLastTile = updatedIndices.get(sizeOfUpdatedIndices - 1);
                // word is placed horizontally
                if (indexOfFirstTile / 15 == indexOfLastTile / 15) {
                    // check if all newly placed tiles are on the same row and connected with each other
                    for (int j = 1; j < sizeOfUpdatedIndices; j++) {
                        int index = indexOfFirstTile + j;
                        if (!updatedIndices.contains(index)) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                    "You either have to place a word vertically or horizontally");
                        }
                    }
                }
                // word is placed vertically
                else {
                    // check if all newly placed tiles are on the same column and connected with each other
                    for (int j = 1; j < sizeOfUpdatedIndices; j++) {
                        int index = indexOfFirstTile + j*15;
                        if (!updatedIndices.contains(index)) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                    "You either have to place a word vertically or horizontally");
                        }
                    }
                }
            }
        }

        else if (sizeOfUpdatedIndices > 1) {

            boolean newWordConnectedToExistingTile = false;
            int firstElement = updatedIndices.get(0);
            int lastElement = updatedIndices.get(sizeOfUpdatedIndices - 1);

            // check whether word is placed vertically or horizontally
            // word is placed horizontally, since first and last tile are on the same row
            if (firstElement / 15 == lastElement / 15) {

                // check if all newly placed tiles are on the same row
                int rowOfFirstTile = firstElement / 15;
                for (int i = 1; i < sizeOfUpdatedIndices; i++){
                    if (updatedIndices.get(i) / 15 != rowOfFirstTile){
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "You have to place horizontally or vertically connected tiles");
                    }
                }

                // check if all newly placed tiles are directly connected with each other or with an existing tile
                int step = 1;
                while (updatedPlayfield.get(firstElement+step) != null) {
                    int index = firstElement + step;
                    if (updatedIndices.contains(index) && persistedPlayfield.get(index) == null) {
                        step++;
                    }
                    else if (persistedPlayfield.get(index) instanceof Tile && !updatedIndices.contains(index)) {
                        newWordConnectedToExistingTile = true;
                        step++;
                    }
                    else {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "You cannot overwrite an existing tile");
                    }
                }

                // check for already existing tiles at top/below the horizontal word
                // compute row on which first tile is located
                int row = firstElement / 15;

                // if word is located on row 0, only check for neighbor tiles on row 1
                if (row == 0) {
                    if (firstElement != 0) {
                        if (persistedPlayfield.get(firstElement-1) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    if (lastElement != 14) {
                        if (persistedPlayfield.get(firstElement+1) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    for (int j = 0; j < sizeOfUpdatedIndices; j++) {
                        if (persistedPlayfield.get(firstElement + j + 15) != null) {
                            newWordConnectedToExistingTile = true;
                            break;
                        }
                    }
                }
                // if word is located on row 14, only check for neighbor tiles on row 13
                else if (row == 14) {
                    if (firstElement != 210) {
                        if (persistedPlayfield.get(firstElement-1) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    if (lastElement != 224) {
                        if (persistedPlayfield.get(firstElement+1) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    for (int j = 0; j < sizeOfUpdatedIndices; j++) {
                        if (persistedPlayfield.get(firstElement + j - 15) != null) {
                            newWordConnectedToExistingTile = true;
                            break;
                        }
                    }

                }
                // if word is located on row 2-13, check for neighbor tiles at top and below the word
                else {
                    if (firstElement % 15 != 0) {
                        if (persistedPlayfield.get(firstElement-1) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    if ((lastElement+1) % 15 != 0) {
                        if (persistedPlayfield.get(firstElement+1) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    for (int j = 0; j < sizeOfUpdatedIndices; j++) {
                        if (persistedPlayfield.get(firstElement + j - 15) != null || persistedPlayfield.get(firstElement + j + 15) != null) {
                            newWordConnectedToExistingTile = true;
                            break;
                        }
                    }
                }
            }

            // word is placed vertically
            else {
                // check if all newly placed tiles are on the same column
                int columnOfFirstTile = firstElement % 15;
                for (int i = 1; i < sizeOfUpdatedIndices; i++){
                    if (updatedIndices.get(i) % 15 != columnOfFirstTile){
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "You have to place horizontally or vertically connected tiles");
                    }
                }

                // check if all newly placed tiles are connected to each other or to existing tiles
                int step = 1;
                while (firstElement + step*15 <= 224 && updatedPlayfield.get(firstElement + step*15) != null) {
                    int index = firstElement + 15 * step;
                    if (updatedIndices.contains(index) && persistedPlayfield.get(index) == null) {
                        step++;
                    }
                    else if (persistedPlayfield.get(index) instanceof Tile && !updatedIndices.contains(index)) {
                        newWordConnectedToExistingTile = true;
                        step++;
                    }
                    else {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "You cannot overwrite an existing tile");
                    }
                }

                // check for already existing tiles to the right/left of the vertical word
                // compute column on which first tile is located
                int column = firstElement % 15;

                // if word is located on column 0, only check for neighbor tiles to the right of placed word
                if (column == 0) {
                    if (firstElement != 0) {
                        if (persistedPlayfield.get(firstElement-15) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    if (lastElement != 210) {
                        if (persistedPlayfield.get(firstElement+15) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    for (int j = 0; j < sizeOfUpdatedIndices; j++) {
                        if (persistedPlayfield.get(firstElement + j*15 + 1) != null) {
                            newWordConnectedToExistingTile = true;
                            break;
                        }
                    }
                }
                // if word is located on row 14, only check for neighbor tiles on row 13
                else if (column == 14) {
                    if (firstElement != 14) {
                        if (persistedPlayfield.get(firstElement-15) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    if (lastElement != 224) {
                        if (persistedPlayfield.get(firstElement+15) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    for (int j = 0; j < sizeOfUpdatedIndices; j++) {
                        if (persistedPlayfield.get(firstElement + j*15 - 1) != null) {
                            newWordConnectedToExistingTile = true;
                            break;
                        }
                    }
                }
                // if word is located on column 2-13, check for neighbor tiles to the right/left of placed word
                else {
                    if (firstElement > 14) {
                        if (persistedPlayfield.get(firstElement-15) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    if (lastElement < 210) {
                        if (persistedPlayfield.get(firstElement+15) != null){
                            newWordConnectedToExistingTile = true;
                        }
                    }

                    for (int j = 0; j < sizeOfUpdatedIndices; j++) {
                        if (persistedPlayfield.get(firstElement + j*15 - 1) != null || persistedPlayfield.get(firstElement + j*15 + 1) != null) {
                            newWordConnectedToExistingTile = true;
                            break;
                        }
                    }
                }
            }

            // check if placed word is connected with an existing tile
            if (!newWordConnectedToExistingTile) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your placed word needs to be connected with an existing tile");
            }
        }

        // list 'updatedIndices' contains only 1 letter
        else {
            // check where existing tile could be
            boolean newWordConnectedToExistingTile = false;
            int element = updatedIndices.get(0);

            // check if new tile is overwriting an existing tile
            if (persistedPlayfield.get(element) != null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You cannot overwrite an existing tile");
            }

            // calculate row and column of element
            int row = element / 15;
            int column = element % 15;

            // store neighbor cells of new tile in an array
            List<Integer> neighborCells = new ArrayList<Integer>();

            // add valid neighbor cells to array
            for (int j = 0; j < 2; j++) {
                // check if there is a valid neighbor at top/below the specified cell
                if (j % 2 == 0) {
                    if (row - 1 >= 0){
                        neighborCells.add(element - 15);
                    }
                    if (row + 1 <= 14){
                        neighborCells.add(element + 15);
                    }
                }
                // check if there is a valid neighbor to the left/right of the specified cell
                else {
                    if (column - 1 >= 0){
                        neighborCells.add(element - 1);
                    }
                    if (column + 1 <= 14){
                        neighborCells.add(element + 1);
                    }
                }
            }

            // check if a neighbor cell contains a tile
            for (int neighborCell : neighborCells){
                if (persistedPlayfield.get(neighborCell) instanceof Tile){
                    newWordConnectedToExistingTile = true;
                }
            }

            // if no valid neighbor cell contains an existing tile, throw error
            if (!newWordConnectedToExistingTile) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your placed word needs to be connected with an existing tile");
            }

        }

    }

    public void changeScoresAfterContesting(Game userGame, Map<Long, Boolean> contested, Map<String, Integer> words) {
        Optional<Game> optGame = gameRepository.findById(userGame.getId());

        if (optGame.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        Game game = optGame.get();
        boolean isValid = true;
        for (String word : words.keySet()) {
            if (!validateWord(word)) {
                isValid = false;
                break;
            }
        }

        if (words.isEmpty()){
            isValid = false;
        }

        if (contested.containsValue(true) && !isValid) {
            for (Score score : game.getScores()) {
                Long userID = score.getScoreUserId();
                int userScore = score.getScore();

                if (game.getCurrentPlayer().equals(userID)) {
                    for (String word: words.keySet()) {
                        score.setScore(userScore - words.get(word));
                        userScore = score.getScore();
                    }
                } else if (contested.get(userID)) {
                    score.setScore(userScore + 20);
                }
            }
        } else if (contested.containsValue(true) && isValid) {
            for (Score score : game.getScores()) {
                Long userID = score.getScoreUserId();
                int userScore = score.getScore();

                if (game.getCurrentPlayer().equals(userID)) {
                    score.setScore(userScore + 20);
                } else if (contested.get(userID)) {
                    if (userScore >= 20) {
                        score.setScore(userScore - 20);
                    } else {
                        score.setScore(0);
                    }
                }
            }
        }
    }

    public Map<String, Integer> getWordsAndScoreForPlayedTiles(List<Tile> updatedPlayfield, List<Tile> currentPlayfield, boolean validateWord) {
        Integer[] dwsArray = {16, 28, 32, 42, 48, 56, 64, 70, 112, 154, 160, 168, 176, 182, 192, 196, 208};
        List<Integer> dws = Arrays.asList(dwsArray);
        Integer[] twsArray = {0, 7, 14, 105, 119, 210, 217, 224};
        List<Integer> tws = Arrays.asList(twsArray);
        Integer[] dlsArray = {3, 11, 36, 38, 45, 52, 59, 92, 96, 98, 102, 108, 116, 122, 126, 128, 132, 165, 172, 179, 186, 188, 213, 221};
        List<Integer> dls = Arrays.asList(dlsArray);
        Integer[] tlsArray = {20, 24, 76, 80, 84, 88, 136, 140, 144, 148, 200, 204};
        List<Integer> tls = Arrays.asList(tlsArray);

        List<Integer> newTileHorizontal = new ArrayList<Integer>();
        List<Integer> newTileVertical = new ArrayList<Integer>();

        //Get all newly played tiles
        for (int i = 0; i < currentPlayfield.size(); i++) {
            if (currentPlayfield.get(i) == null && updatedPlayfield.get(i) != null) {
                newTileHorizontal.add(i);
                newTileVertical.add(i);
            }
        }

        List<Integer> checkedHorizontal = new ArrayList<Integer>();
        List<Integer> checkedVertical = new ArrayList<Integer>();

        Map<String, Integer> words = new HashMap<>();

        //Check horizontal words
        for(int i : newTileHorizontal) {
            StringBuilder word = new StringBuilder();
            int wordScore = 0;
            int playedTilesScore = 0;
            int wordMultiplier = 1;
            boolean moreThanOneTile = false;

            if (!checkedHorizontal.contains(i)) {
                checkedHorizontal.add(i);
                word.append(updatedPlayfield.get(i).getLetter());

                //check for letter multiplier
                if (dls.contains(i)) {
                    wordScore += updatedPlayfield.get(i).getValue() * 2;
                    playedTilesScore += updatedPlayfield.get(i).getValue() * 2;
                } else if (tls.contains(i)) {
                    wordScore += updatedPlayfield.get(i).getValue() * 3;
                    playedTilesScore += updatedPlayfield.get(i).getValue() * 3;
                } else {
                    wordScore += updatedPlayfield.get(i).getValue();
                    playedTilesScore += updatedPlayfield.get(i).getValue();
                }

                //check for word multiplier
                if (dws.contains(i)) {
                    wordMultiplier *= 2;
                } else if (tws.contains(i)) {
                    wordMultiplier *= 3;
                }

                //check all tiles before played one
                for (int j = i - 1; j >= (i / 15) * 15 && updatedPlayfield.get(j) != null; j--) {
                    moreThanOneTile = true;
                    word.insert(0, updatedPlayfield.get(j).getLetter());

                    if (newTileHorizontal.contains(j)) {
                        checkedHorizontal.add(j);

                        if (dls.contains(j)) {
                            wordScore += updatedPlayfield.get(j).getValue() * 2;
                            playedTilesScore += updatedPlayfield.get(j).getValue() * 2;
                        } else if (tls.contains(j)) {
                            wordScore += updatedPlayfield.get(j).getValue() * 3;
                            playedTilesScore += updatedPlayfield.get(j).getValue() * 3;
                        } else {
                            wordScore += updatedPlayfield.get(j).getValue();
                            playedTilesScore += updatedPlayfield.get(j).getValue();
                        }

                        if (dws.contains(j)) {
                            wordMultiplier *= 2;
                        } else if (tws.contains(j)) {
                            wordMultiplier *= 3;
                        }
                    } else {
                        wordScore += updatedPlayfield.get(j).getValue();
                    }
                }

                //check all tiles after played one
                for (int j = i + 1; j < (i / 15) * 15 + 15 && updatedPlayfield.get(j) != null; j++) {
                    moreThanOneTile = true;
                    word.append(updatedPlayfield.get(j).getLetter());

                    if (newTileHorizontal.contains(j)) {
                        checkedHorizontal.add(j);

                        if (dls.contains(j)) {
                            wordScore += updatedPlayfield.get(j).getValue() * 2;
                            playedTilesScore += updatedPlayfield.get(j).getValue() * 2;
                        } else if (tls.contains(j)) {
                            wordScore += updatedPlayfield.get(j).getValue() * 3;
                            playedTilesScore += updatedPlayfield.get(j).getValue() * 3;
                        } else {
                            wordScore += updatedPlayfield.get(j).getValue();
                            playedTilesScore += updatedPlayfield.get(j).getValue();
                        }

                        if (dws.contains(j)) {
                            wordMultiplier *= 2;
                        } else if (tws.contains(j)) {
                            wordMultiplier *= 3;
                        }
                    } else {
                        wordScore += updatedPlayfield.get(j).getValue();
                    }
                }

                //TODO: Change when implementing constesting
                boolean validWord;
                if (validateWord){
                    validWord = validateWord(word.toString());
                }
                else{
                    validWord = true;
                }

                if (moreThanOneTile && validWord) {
                    if (words.containsKey(word.toString())) {
                        int temp = words.get(word.toString());
                        words.put(word.toString(), temp + wordScore + (playedTilesScore * (wordMultiplier - 1)));
                    } else {
                        words.put(word.toString(), wordScore + (playedTilesScore * (wordMultiplier - 1)));
                    }
                } else if (!validWord && moreThanOneTile) {
                    words.clear();
                    return words;
                }
            }
        }

        //Check vertical words
        for (int i : newTileVertical) {
            StringBuilder word = new StringBuilder();
            int wordScore = 0;
            int playedTilesScore = 0;
            int wordMultiplier = 1;
            boolean moreThanOneTile = false;

            if (!checkedVertical.contains(i)) {
                checkedVertical.add(i);
                word.append(updatedPlayfield.get(i).getLetter());

                if (dls.contains(i)) {
                    wordScore += updatedPlayfield.get(i).getValue() * 2;
                    playedTilesScore += updatedPlayfield.get(i).getValue() * 2;
                } else if (tls.contains(i)) {
                    wordScore += updatedPlayfield.get(i).getValue() * 3;
                    playedTilesScore += updatedPlayfield.get(i).getValue() * 3;
                } else {
                    wordScore += updatedPlayfield.get(i).getValue();
                    playedTilesScore += updatedPlayfield.get(i).getValue();
                }

                if (dws.contains(i)) {
                    wordMultiplier *= 2;
                } else if (tws.contains(i)) {
                    wordMultiplier *= 3;
                }

                //check all tiles before played one
                for (int j = i - 15; j >= i % 15 && updatedPlayfield.get(j) != null; j -= 15) {
                    moreThanOneTile = true;
                    word.insert(0, updatedPlayfield.get(j).getLetter());

                    if (newTileVertical.contains(j)) {
                        checkedVertical.add(j);

                        if (dls.contains(j)) {
                            wordScore += updatedPlayfield.get(j).getValue() * 2;
                            playedTilesScore += updatedPlayfield.get(j).getValue() * 2;
                        } else if (tls.contains(j)) {
                            wordScore += updatedPlayfield.get(j).getValue() * 3;
                            playedTilesScore += updatedPlayfield.get(j).getValue() * 3;
                        } else {
                            wordScore += updatedPlayfield.get(j).getValue();
                            playedTilesScore += updatedPlayfield.get(j).getValue();
                        }

                        if (dws.contains(j)) {
                            wordMultiplier *= 2;
                        } else if (tws.contains(j)) {
                            wordMultiplier *= 3;
                        }
                    } else {
                        wordScore += updatedPlayfield.get(i).getValue();
                    }
                }

                for (int j = i + 15; j < (i % 15) + 15 * 15 && updatedPlayfield.get(j) != null; j += 15) {
                    moreThanOneTile = true;
                    word.append(updatedPlayfield.get(j).getLetter());

                    if (newTileVertical.contains(j)) {
                        checkedVertical.add(j);

                        if (dls.contains(j)) {
                            wordScore += updatedPlayfield.get(j).getValue() * 2;
                            playedTilesScore += updatedPlayfield.get(j).getValue() * 2;
                        } else if (tls.contains(j)) {
                            wordScore += updatedPlayfield.get(j).getValue() * 3;
                            playedTilesScore += updatedPlayfield.get(j).getValue() * 3;
                        } else {
                            wordScore += updatedPlayfield.get(j).getValue();
                            playedTilesScore += updatedPlayfield.get(j).getValue();
                        }

                        if (dws.contains(j)) {
                            wordMultiplier *= 2;
                        } else if (tws.contains(j)) {
                            wordMultiplier *= 3;
                        }
                    } else {
                        wordScore += updatedPlayfield.get(j).getValue();
                    }
                }
            }

            //TODO: Change when implementing constesting
            boolean validWord;
            if (validateWord){
                validWord = validateWord(word.toString());
            }
            else{
                validWord = true;
            }

            if (moreThanOneTile && validWord) {
                if (words.containsKey(word.toString())) {
                    int temp = words.get(word.toString());
                    words.put(word.toString(), temp + wordScore + (playedTilesScore * (wordMultiplier - 1)));
                } else {
                    words.put(word.toString(), wordScore + (playedTilesScore * (wordMultiplier - 1)));
                }
            } else if (!validWord && moreThanOneTile) {
                words.clear();
                return words;
            }
        }

        if (newTileHorizontal.size() == 7) {
            String key = words.keySet().iterator().next();
            int score = words.get(key);
            words.put(key, score + 50);
        }

        return words;
    }

    public List<Tile> swapTiles(Long gameId, Long userId, Long handId, List<Tile> tilesToBeExchanged) {
        Hand foundhand = handRepository.findById(handId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Hand with ID %d not found!", handId)));
        Game foundGame = gameRepository.findById(gameId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Game with ID %d not found!", gameId)));

        // check if userId of found hand equals the indicated userId
        if (!Objects.equals(foundhand.getHanduserid(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Not correct hand!");
        }

        // check if there is a valid number of tiles to be exchanged
        if (tilesToBeExchanged.isEmpty() || tilesToBeExchanged.size() > 7) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can swap 1-7 tiles");
        }

        // check if tiles to be exchanged is a sublist of the actual hand
        for (Tile tile : tilesToBeExchanged) {
            if (foundhand.getHandtiles().contains(tile)) {
                continue;
            }
            else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        String.format("Tile %c not found in hand!", tile.getLetter()));
            }
        }

        Bag bag = bagRepository.findById(foundGame.getBag().getId()).orElseThrow();

        // check if enough tiles are in bag
        if (bag.tilesleft() < tilesToBeExchanged.size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Not enough tiles in bag!");
        }

        // get tiles from bag
        List<Tile> tilesToAddToHand = bag.getSomeTiles(tilesToBeExchanged.size());
        // put tiles to be exchanged in bag
        bag.putTilesInBag(tilesToBeExchanged);
        // remove tiles from hand
        foundhand.removeTilesFromHand(tilesToBeExchanged);
        // add new tiles to hand
        foundhand.putTilesInHand(tilesToAddToHand);

        // Reset skip counter
        foundGame.setGlobalSkipCounter(0);

        makeNextPlayerToCurrentPlayer(foundGame.getId());
        gameRepository.save(foundGame);

        bagRepository.save(bag);

        handRepository.save(foundhand);

        gameRepository.flush();
        bagRepository.flush();
        handRepository.flush();
        // return new hand
        return foundhand.getHandtiles();
    }

    public void authenticateUserForMove(User userInput, Long gameId) {
        Optional<Game> game = gameRepository.findById(gameId);

        if (game.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The game does not exist!");
        } else if (!game.get().getCurrentPlayer().equals(userInput.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "It's not your turn!");
        }
    }

    private JsonNode getJSON(HttpResponse<String> response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(response.body());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Response body could not be parsed!");
        }
    }

    /**
     * This is a helper method that will check whether a game
     * with the specified ID exists or not. The method will return
     * found game or else throw an error.
     *
     * @param gameId
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Game
     */

    private Game checkIfGameExists(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicated Game not found!"));
    }


    /**
     * This is a helper method that will check whether a board
     * has the correct size. The method will return nothing
     * or else throw an error.
     *
     * @param board
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Tile
     */
    private void checkIfBoardValid(List<Tile> board) {
        // check if board size correct
        if (board.size() != 225) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Board Dimensions need to be 15x15");
        }
    }

    /**
     * This is a helper method that will check whether a player
     * is part of the specified game. If the indicated userId is
     * part of the game, the method does nothing
     * or else throw an error.
     * @param game
     * @throws org.springframework.web.server.ResponseStatusException
     * @see Game
     */
    private void checkIfUserPartOfGame(Game game, Long userId) {
        // check if user is part of specific Game
        boolean userPartOfGame = false;
        for (User player : game.getPlayers()){
            if (Objects.equals(player.getId(), userId)) {
                userPartOfGame = true;
                break;
            }
        }
        if (!userPartOfGame){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Indicated User not part of this Game!");
        }
    }
}
